package com.ticketrush.backend.service.impl;

import com.ticketrush.backend.dto.request.CreateVoucherRequest;
import com.ticketrush.backend.dto.response.VoucherCheckResponse;
import com.ticketrush.backend.dto.response.VoucherResponse;
import com.ticketrush.backend.entity.Voucher;
import com.ticketrush.backend.repository.VoucherRepository;
import com.ticketrush.backend.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Triển khai của VoucherService interface
 * 
 * Chức năng:
 * - Admin tạo mã giảm giá
 * - Frontend kiểm tra mã (xem tiền giảm)
 * 
 * @author TicketRush Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    /**
     * Admin API: Tạo mã giảm giá mới
     */
    @Override
    @Transactional  // Override readOnly = true để có thể lưu database
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        try {
            log.info("🎟️ Admin tạo voucher: {}", request.getCode());

            // Validate
            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                throw new IllegalArgumentException("❌ Mã voucher không được để trống");
            }
            if (request.getDiscountPercentage() == null || 
                request.getDiscountPercentage().compareTo(BigDecimal.ZERO) <= 0 ||
                request.getDiscountPercentage().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("❌ Phần trăm giảm phải từ 0 đến 100");
            }
            if (request.getMaxUsage() == null || request.getMaxUsage() <= 0) {
                throw new IllegalArgumentException("❌ Số lần sử dụng tối đa phải > 0");
            }
            if (request.getStartTime() == null || request.getEndTime() == null) {
                throw new IllegalArgumentException("❌ Thời gian bắt đầu/kết thúc không được trống");
            }
            if (request.getStartTime().isAfter(request.getEndTime())) {
                throw new IllegalArgumentException("❌ Thời gian bắt đầu phải trước thời gian kết thúc");
            }

            // Kiểm tra code có trùng không
            if (voucherRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
                throw new IllegalArgumentException("❌ Mã voucher đã tồn tại");
            }

            // Tạo Voucher entity
            Voucher voucher = new Voucher();
            voucher.setCode(request.getCode().toUpperCase());
            voucher.setDescription(request.getDescription());
            voucher.setDiscountPercentage(request.getDiscountPercentage());
            voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
            voucher.setMaxUsage(request.getMaxUsage());
            voucher.setCurrentUsage(0);
            voucher.setStartTime(request.getStartTime());
            voucher.setEndTime(request.getEndTime());

            // Lưu vào database
            Voucher savedVoucher = voucherRepository.save(voucher);
            log.info("✅ Tạo voucher thành công: ID = {}", savedVoucher.getId());

            return toResponse(savedVoucher);

        } catch (Exception e) {
            log.error("❌ Lỗi tạo voucher: {}", e.getMessage(), e);
            throw new IllegalArgumentException("❌ Lỗi tạo voucher: " + e.getMessage());
        }
    }

    /**
     * Frontend API: Kiểm tra mã giảm giá
     * 
     * Trả về thông tin voucher để Frontend hiển thị tiền giảm ngay trên màn hình
     */
    @Override
    public VoucherCheckResponse checkVoucher(String code) {
        try {
            log.info("🔍 Kiểm tra voucher: {}", code);

            if (code == null || code.trim().isEmpty()) {
                throw new IllegalArgumentException("❌ Mã voucher không được để trống");
            }

            // Tìm voucher
            Voucher voucher = voucherRepository.findByCodeIgnoreCase(code.trim())
                    .orElseThrow(() -> new IllegalArgumentException("❌ Mã voucher không tồn tại"));

            // Kiểm tra thời gian
            LocalDateTime now = LocalDateTime.now();
            boolean isValid = !now.isBefore(voucher.getStartTime()) && !now.isAfter(voucher.getEndTime());

            // Kiểm tra số lần còn lại
            Integer remainingUsage = voucher.getMaxUsage() - (voucher.getCurrentUsage() != null ? voucher.getCurrentUsage() : 0);
            boolean canUse = isValid && remainingUsage > 0;

            // Tạo message hiển thị cho Frontend
            String message;
            if (!isValid) {
                message = "❌ Mã voucher đã hết hạn hoặc chưa áp dụng";
            } else if (remainingUsage <= 0) {
                message = "❌ Mã voucher đã hết lần sử dụng";
            } else {
                // Format message: "Giảm 50% (tối đa 200.000đ) - Còn 45/100 lần"
                message = String.format("✅ Giảm %s%% (tối đa %,d đ) - Còn %d/%d lần",
                        voucher.getDiscountPercentage(),
                        voucher.getMaxDiscountAmount().longValue(),
                        remainingUsage,
                        voucher.getMaxUsage()
                );
            }

            log.info("✅ Kiểm tra voucher thành công: valid={}, message={}", canUse, message);

            return new VoucherCheckResponse(
                    voucher.getCode(),
                    canUse,
                    voucher.getDescription(),
                    voucher.getDiscountPercentage(),
                    voucher.getMaxDiscountAmount(),
                    remainingUsage,
                    message
            );

        } catch (Exception e) {
            log.error("❌ Lỗi kiểm tra voucher: {}", e.getMessage(), e);
            throw new IllegalArgumentException("❌ Lỗi kiểm tra voucher: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách tất cả voucher.
     *
     * @return danh sách voucher.
     */
    @Override
    public java.util.List<VoucherResponse> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Cập nhật thông tin voucher.
     * {@code @Transactional} ghi đè read-only của class để cho phép lưu database.
     *
     * @param id ID voucher cần cập nhật.
     * @param request dữ liệu cập nhật voucher.
     * @return voucher sau khi cập nhật.
     * @throws IllegalArgumentException nếu voucher không tồn tại, code rỗng hoặc code bị trùng.
     */
    @Override
    @Transactional
    public VoucherResponse updateVoucher(Long id, CreateVoucherRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("❌ Không tìm thấy voucher ID: " + id));

        // Validate
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("❌ Mã voucher không được để trống");
        }
        
        // Kiểm tra code trùng (trừ chính nó)
        voucherRepository.findByCodeIgnoreCase(request.getCode())
                .ifPresent(v -> {
                    if (!v.getId().equals(id)) {
                        throw new IllegalArgumentException("❌ Mã voucher đã tồn tại");
                    }
                });

        voucher.setCode(request.getCode().toUpperCase());
        voucher.setDescription(request.getDescription());
        voucher.setDiscountPercentage(request.getDiscountPercentage());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setMaxUsage(request.getMaxUsage());
        voucher.setStartTime(request.getStartTime());
        voucher.setEndTime(request.getEndTime());

        return toResponse(voucherRepository.save(voucher));
    }

    /**
     * Xóa voucher theo ID.
     * {@code @Transactional} ghi đè read-only của class để cho phép xóa database.
     *
     * @param id ID voucher cần xóa.
     * @throws IllegalArgumentException nếu voucher không tồn tại.
     */
    @Override
    @Transactional
    public void deleteVoucher(Long id) {
        if (!voucherRepository.existsById(id)) {
            throw new IllegalArgumentException("❌ Không tìm thấy voucher ID: " + id);
        }
        voucherRepository.deleteById(id);
    }

    /**
     * Map Voucher entity sang VoucherResponse DTO
     */
    /**
     * Chuyển Voucher sang DTO và tính trạng thái hiệu lực hiện tại.
     *
     * @param voucher entity voucher cần chuyển đổi.
     * @return DTO voucher.
     */
    private VoucherResponse toResponse(Voucher voucher) {
        // Xác định trạng thái
        LocalDateTime now = LocalDateTime.now();
        String status;
        if (now.isBefore(voucher.getStartTime())) {
            status = "COMING_SOON";
        } else if (now.isAfter(voucher.getEndTime())) {
            status = "EXPIRED";
        } else if ((voucher.getCurrentUsage() != null ? voucher.getCurrentUsage() : 0) >= voucher.getMaxUsage()) {
            status = "USED_UP";
        } else {
            status = "ACTIVE";
        }

        return new VoucherResponse(
                voucher.getId(),
                voucher.getCode(),
                voucher.getDescription(),
                voucher.getDiscountPercentage(),
                voucher.getMaxDiscountAmount(),
                voucher.getMaxUsage(),
                voucher.getCurrentUsage() != null ? voucher.getCurrentUsage() : 0,
                voucher.getStartTime(),
                voucher.getEndTime(),
                status
        );
    }
}

