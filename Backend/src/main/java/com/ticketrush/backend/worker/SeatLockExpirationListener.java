package com.ticketrush.backend.worker;

import com.ticketrush.backend.service.SeatLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener xử lý sự kiện Redis key hết hạn cho khóa ghế.
 */
@Slf4j
@Component
public class SeatLockExpirationListener extends KeyExpirationEventMessageListener {

    private static final Pattern SEAT_LOCK_KEY = Pattern.compile("^seat:(\\d+)$");

    private final SeatLockService seatLockService;

    // Khởi tạo listener với container và service để xử lý khi khóa hết hạn
    /**
     * Khởi tạo listener nhận sự kiện hết hạn key từ Redis.
     *
     * @param listenerContainer container đăng ký Redis message listener.
     * @param seatLockService service giải phóng khóa ghế đã hết hạn.
     */
    public SeatLockExpirationListener(
            RedisMessageListenerContainer listenerContainer,
            SeatLockService seatLockService
    ) {
        super(listenerContainer);
        this.seatLockService = seatLockService;
    }

    /**
     * Xử lý Redis key hết hạn, chỉ nhận key có dạng {@code seat:{id}}.
     *
     * @param message message Redis chứa key đã hết hạn.
     * @param pattern pattern channel Redis đã match.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);
        Matcher matcher = SEAT_LOCK_KEY.matcher(expiredKey);

        if(!matcher.matches()) {
            return;
        }

        Long seatId = Long.valueOf(matcher.group(1));

        try {
            seatLockService.releaseExpiredSeatLock(seatId);
            log.info("Handled expired Redis lock key {}", expiredKey);
        } catch (Exception e) {
            log.error("Could not release expired seat lock for key {}: {}", expiredKey, e.getMessage(), e);
        }
    }


}
