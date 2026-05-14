package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thao tác dữ liệu voucher.
 */
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    /**
     * Tìm voucher theo mã, không phân biệt hoa thường.
     *
     * @param code mã voucher cần tìm.
     * @return voucher nếu tồn tại.
     */
    Optional<Voucher> findByCodeIgnoreCase(String code);
}
