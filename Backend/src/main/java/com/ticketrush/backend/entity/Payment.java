package com.ticketrush.backend.entity;

import com.ticketrush.backend.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity lưu giao dịch thanh toán của một đơn đặt vé.
 *
 * Annotation {@link CreationTimestamp} và {@link UpdateTimestamp} tự động ghi
 * thời điểm tạo và cập nhật bản ghi.
 */
@Entity
@Table(name = "payments")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "transaction_reference", nullable = false)
    private String transactionReference;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PaymentStatus status;

    @Column(name = "receipt_url")
    private String receiptURL;

    @Column(name = "pdf_receipt_path")
    private String pdfReceiptPath;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
