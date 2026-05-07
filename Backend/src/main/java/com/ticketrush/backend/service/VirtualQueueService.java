package com.ticketrush.backend.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ════════════════════════════════════════════════════════
 * TASK 4.1: VIRTUAL QUEUE — HÀNG CHỜ ẢO
 * ════════════════════════════════════════════════════════
 * 
 * Thiết kế:
 * - Khi traffic đột biến cho 1 showtime, user được đẩy vào Waiting Room
 * - Mỗi 5 giây, hệ thống nhả 1 batch (mặc định 50 người) vào trang chọn ghế
 * - User nhận token khi được nhả → dùng token để truy cập trang booking
 * 
 * Data structure: In-Memory ConcurrentLinkedQueue (không cần Redis cho monolith)
 * 
 * Luồng hoạt động:
 *   1. User gọi POST /queue/join → Nhận position
 *   2. FE subscribe WebSocket /topic/queue/{showtimeId}
 *   3. Mỗi 5s, scheduler nhả batch → broadcast cập nhật position
 *   4. Khi user.position = 0 → nhận token → redirect sang booking
 *   5. Booking API validate token trước khi cho vào
 */
@Slf4j
@Service
public class VirtualQueueService {

    /** Queue cho từng showtime */
    private final Map<Long, ConcurrentLinkedQueue<QueueEntry>> queues = new ConcurrentHashMap<>();

    /** Active tokens (đã được nhả vào booking) */
    private final Map<String, TokenInfo> activeTokens = new ConcurrentHashMap<>();

    /** Counter cho position */
    private final Map<Long, AtomicInteger> positionCounters = new ConcurrentHashMap<>();

    /** Số user được nhả mỗi batch */
    private static final int BATCH_SIZE = 50;

    /** Token hết hạn sau 5 phút (nếu không dùng) */
    private static final long TOKEN_TTL_SECONDS = 300;

    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * User tham gia hàng chờ
     * 
     * @param showtimeId ID suất chiếu
     * @param userId ID user
     * @return QueueStatus với position hiện tại
     */
    public QueueStatus joinQueue(Long showtimeId, Long userId) {
        ConcurrentLinkedQueue<QueueEntry> queue = queues.computeIfAbsent(
                showtimeId, k -> new ConcurrentLinkedQueue<>());
        AtomicInteger counter = positionCounters.computeIfAbsent(
                showtimeId, k -> new AtomicInteger(0));

        // Kiểm tra user đã trong queue chưa
        Optional<QueueEntry> existing = queue.stream()
                .filter(e -> e.getUserId().equals(userId))
                .findFirst();

        if (existing.isPresent()) {
            // Đã trong queue → trả về position hiện tại
            int position = getPosition(queue, userId);
            return new QueueStatus(showtimeId, userId, position, null,
                    "Bạn đã trong hàng chờ", queue.size());
        }

        int position = counter.incrementAndGet();
        QueueEntry entry = new QueueEntry(userId, null, Instant.now(), position);
        queue.add(entry);

        log.info("📋 [QUEUE] User {} tham gia hàng chờ showtime {} - Vị trí: {}", userId, showtimeId, position);

        return new QueueStatus(showtimeId, userId, position, null,
                "Đã tham gia hàng chờ", queue.size());
    }

    /**
     * Kiểm tra trạng thái queue của user
     */
    public QueueStatus getStatus(Long showtimeId, Long userId) {
        ConcurrentLinkedQueue<QueueEntry> queue = queues.get(showtimeId);
        if (queue == null) {
            return new QueueStatus(showtimeId, userId, 0, null, "Không có hàng chờ", 0);
        }

        // Check xem user đã có token chưa
        Optional<QueueEntry> entry = queue.stream()
                .filter(e -> e.getUserId().equals(userId))
                .findFirst();

        if (entry.isPresent() && entry.get().getToken() != null) {
            return new QueueStatus(showtimeId, userId, 0, entry.get().getToken(),
                    "Bạn đã được vào!", queue.size());
        }

        int position = entry.map(e -> getPosition(queue, userId)).orElse(0);
        return new QueueStatus(showtimeId, userId, position, null,
                position > 0 ? "Đang chờ..." : "Bạn chưa trong hàng chờ", queue.size());
    }

    /**
     * Validate token — Booking API gọi trước khi cho user vào trang chọn ghế
     */
    public boolean validateToken(String token) {
        TokenInfo info = activeTokens.get(token);
        if (info == null) return false;
        if (Instant.now().isAfter(info.getExpiresAt())) {
            activeTokens.remove(token);
            return false;
        }
        return true;
    }

    /**
     * ════════════════════════════════════
     * SCHEDULER: Nhả batch mỗi 5 giây
     * ════════════════════════════════════
     * 
     * Với mỗi showtime có queue:
     * 1. Lấy BATCH_SIZE entries đầu tiên chưa có token
     * 2. Gán token cho họ
     * 3. Broadcast cập nhật position qua WebSocket
     */
    @Scheduled(fixedRate = 5000) // 5 giây
    public void releaseBatch() {
        for (Map.Entry<Long, ConcurrentLinkedQueue<QueueEntry>> entry : queues.entrySet()) {
            Long showtimeId = entry.getKey();
            ConcurrentLinkedQueue<QueueEntry> queue = entry.getValue();

            if (queue.isEmpty()) continue;

            List<QueueEntry> toRelease = new ArrayList<>();
            int count = 0;

            for (QueueEntry qe : queue) {
                if (qe.getToken() != null) continue; // Đã có token rồi
                if (count >= BATCH_SIZE) break;

                String token = UUID.randomUUID().toString().substring(0, 12);
                qe.setToken(token);

                activeTokens.put(token, new TokenInfo(
                        qe.getUserId(), showtimeId,
                        Instant.now().plusSeconds(TOKEN_TTL_SECONDS)));

                toRelease.add(qe);
                count++;
            }

            if (!toRelease.isEmpty()) {
                log.info("🚪 [QUEUE] Nhả {} user vào showtime {}", toRelease.size(), showtimeId);

                // Broadcast cho từng user được nhả
                for (QueueEntry qe : toRelease) {
                    try {
                        messagingTemplate.convertAndSend(
                                "/topic/queue/" + showtimeId,
                                (Object) Map.of(
                                        "type", "RELEASED",
                                        "userId", qe.getUserId(),
                                        "token", qe.getToken(),
                                        "message", "Đến lượt bạn! Hãy vào chọn ghế."
                                ));
                    } catch (Exception e) {
                        log.error("❌ Lỗi broadcast queue: {}", e.getMessage());
                    }
                }

                // Xóa entries đã có token khỏi queue
                queue.removeIf(qe -> qe.getToken() != null);
            }

            // Broadcast cập nhật position cho tất cả user còn trong queue
            if (!queue.isEmpty()) {
                int remaining = queue.size();
                try {
                    messagingTemplate.convertAndSend(
                            "/topic/queue/" + showtimeId,
                            (Object) Map.of(
                                    "type", "POSITION_UPDATE",
                                    "queueSize", remaining,
                                    "estimatedWaitSeconds", remaining / BATCH_SIZE * 5
                            ));
                } catch (Exception e) {
                    log.error("❌ Lỗi broadcast position update: {}", e.getMessage());
                }
            }
        }

        // Cleanup expired tokens
        activeTokens.entrySet().removeIf(e -> Instant.now().isAfter(e.getValue().getExpiresAt()));
    }

    // ═══════ HELPER ═══════

    private int getPosition(ConcurrentLinkedQueue<QueueEntry> queue, Long userId) {
        int pos = 1;
        for (QueueEntry e : queue) {
            if (e.getToken() != null) continue; // Skip đã nhả
            if (e.getUserId().equals(userId)) return pos;
            pos++;
        }
        return 0;
    }

    // ═══════ INNER CLASSES ═══════

    @Data
    @AllArgsConstructor
    public static class QueueEntry {
        private Long userId;
        private String token;
        private Instant joinedAt;
        private int assignedPosition;
    }

    @Data
    @AllArgsConstructor
    public static class QueueStatus {
        private Long showtimeId;
        private Long userId;
        private int position;
        private String token;
        private String message;
        private int totalInQueue;
    }

    @Data
    @AllArgsConstructor
    public static class TokenInfo {
        private Long userId;
        private Long showtimeId;
        private Instant expiresAt;
    }
}
