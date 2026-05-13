package com.ticketrush.backend.entity;

import com.ticketrush.backend.entity.enums.ReservationStatus;
import com.ticketrush.backend.entity.enums.ReservationStatusConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity lưu đơn đặt vé của người dùng.
 *
 * Trạng thái đơn được lưu bằng {@link ReservationStatusConverter} để ánh xạ enum
 * sang mã số trong database.
 */
@Entity
@Table(name = "reservations")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @CreationTimestamp
    @Column(name = "reservation_time", nullable = false, updatable = false)
    private LocalDateTime reservationTime;

    @Convert(converter = ReservationStatusConverter.class)
    @Column(name = "status_id", nullable = false)
    private ReservationStatus status = ReservationStatus.LOCKED;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Column(nullable = false)
    private Boolean paid = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "qr_code_hash", length = 255)
    private String qrCodeHash;

    @Column(name = "checkin_time")
    private LocalDateTime checkinTime;

}
