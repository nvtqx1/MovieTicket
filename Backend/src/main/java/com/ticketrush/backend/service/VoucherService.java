package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.CreateVoucherRequest;
import com.ticketrush.backend.dto.VoucherCheckResponse;
import com.ticketrush.backend.dto.VoucherResponse;

/**
 * Service interface quản lý mã giảm giá (Vouchers)
 * 
 * 🎯 CÔNG DỤNG:
 * - Admin tạo mã giảm giá cho flash sale
 * - Frontend kiểm tra mã trước khi đặt vé
 * 
 * @author TicketRush Team
 * @version 1.0
 */
public interface VoucherService {

    /**
     * Admin API: Tạo mã giảm giá mới
     * 
     * POST /v1/admin/vouchers
     * 
     * @param request CreateVoucherRequest chứa code, discount%, period
     * @return VoucherResponse thông tin voucher vừa tạo
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc code trùng
     */
    VoucherResponse createVoucher(CreateVoucherRequest request);

    /**
     * Frontend API: Kiểm tra mã giảm giá
     * 
     * GET /v1/vouchers/check?code=TICKETRUSH
     * 
     * ⚡ Dùng để user nhập mã giảm giá và xem tiền giảm ngay trùc trên màn hình
     * Không cần chờ submit đơn hàng
     * 
     * @param code Mã voucher (VD: "TICKETRUSH")
     * @return VoucherCheckResponse kết quả check gồm:
     *         - isValid: có hợp lệ không
     *         - discountPercentage: % giảm
     *         - maxDiscountAmount: số tiền giảm tối đa
     *         - remainingUsage: số lần còn lại
     *         - message: thông báo để FE hiển thị
     * @throws IllegalArgumentException nếu code không tồn tại hoặc hết hạn
     */
    VoucherCheckResponse checkVoucher(String code);

    /**
     * Admin API: Lấy danh sách tất cả mã giảm giá
     * 
     * GET /v1/vouchers
     */
    java.util.List<VoucherResponse> getAllVouchers();

    /**
     * Admin API: Cập nhật mã giảm giá
     * 
     * PUT /v1/vouchers/{id}
     */
    VoucherResponse updateVoucher(Long id, CreateVoucherRequest request);

    /**
     * Admin API: Xóa mã giảm giá
     * 
     * DELETE /v1/vouchers/{id}
     */
    void deleteVoucher(Long id);
}

