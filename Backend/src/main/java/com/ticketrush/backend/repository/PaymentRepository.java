package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu thanh toán.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Tìm giao dịch thanh toán theo đơn đặt vé.
     *
     * @param reservationId ID đơn đặt vé.
     * @return giao dịch thanh toán của đơn, hoặc null nếu chưa có.
     */
    Payment findByReservationId(Long reservationId);
}
