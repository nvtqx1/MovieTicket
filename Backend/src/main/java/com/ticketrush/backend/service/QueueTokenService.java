package com.ticketrush.backend.service;

import com.ticketrush.backend.entity.QueueToken;
import com.ticketrush.backend.entity.Showtime;
import com.ticketrush.backend.entity.User;
import com.ticketrush.backend.entity.enums.QueueTokenStatus;
import com.ticketrush.backend.repository.QueueTokenRepository;
import com.ticketrush.backend.repository.ShowtimeRepository;
import com.ticketrush.backend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueTokenService {

    private final QueueTokenRepository queueTokenRepository;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${ticketrush.app.jwtSecret}")
    private String jwtSecret;

    @Value("${ticketrush.queue.jwt-expiration-ms:300000}")
    private long queueJwtExpirationMs;

    private static final String QUEUE_REDIS_PREFIX = "queue_token:";

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Sinh token cấp phép cho User vào mua vé
     */
    public String generateAndSaveQueueToken(Long userId, Long showtimeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found: " + showtimeId));

        // 1. Sinh UUID duy nhất cho token này
        String tokenId = UUID.randomUUID().toString();

        // 2. Tính toán thời gian hết hạn
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + queueJwtExpirationMs);
        LocalDateTime expiresAt = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // 3. Tạo JWT ngắn hạn đặc biệt
        String jwtToken = Jwts.builder()
                .setId(tokenId)
                .setSubject(userId.toString())
                .claim("showtimeId", showtimeId)
                .claim("purpose", "QUEUE_ACCESS")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();

        // 4. Lưu vào MySQL (để audit và quản lý)
        QueueToken queueToken = new QueueToken();
        queueToken.setUser(user);
        queueToken.setShowtime(showtime);
        queueToken.setToken(jwtToken);
        queueToken.setStatus(QueueTokenStatus.ACTIVE);
        queueToken.setExpiresAt(expiresAt);
        queueTokenRepository.save(queueToken);

        // 5. Lưu vào Redis (để filter check cực nhanh)
        String redisKey = QUEUE_REDIS_PREFIX + userId + ":" + showtimeId;
        redisTemplate.opsForValue().set(redisKey, jwtToken, queueJwtExpirationMs, TimeUnit.MILLISECONDS);

        return jwtToken;
    }

    /**
     * Xác thực Token khi User gọi API /seats/lock
     */
    public boolean validateQueueToken(String token, Long userId, Long showtimeId) {
        try {
            // 1. Check signature và expiration của JWT
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 2. Validate payload
            if (!claims.getSubject().equals(userId.toString())) {
                log.warn("Queue token subject mismatch: expected {}, got {}", userId, claims.getSubject());
                return false;
            }

            Long tokenShowtimeId = claims.get("showtimeId", Long.class);
            if (!showtimeId.equals(tokenShowtimeId)) {
                log.warn("Queue token showtime mismatch: expected {}, got {}", showtimeId, tokenShowtimeId);
                return false;
            }

            if (!"QUEUE_ACCESS".equals(claims.get("purpose"))) {
                log.warn("Queue token purpose mismatch");
                return false;
            }

            // 3. Check Redis xem token còn hiệu lực/chưa bị thu hồi không
            String redisKey = QUEUE_REDIS_PREFIX + userId + ":" + showtimeId;
            String redisToken = redisTemplate.opsForValue().get(redisKey);

            return token.equals(redisToken);

        } catch (Exception e) {
            log.error("Invalid queue token: {}", e.getMessage());
            return false;
        }
    }
}
