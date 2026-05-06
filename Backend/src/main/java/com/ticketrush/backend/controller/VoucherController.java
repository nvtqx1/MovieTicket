package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.CreateVoucherRequest;
import com.ticketrush.backend.dto.VoucherCheckResponse;
import com.ticketrush.backend.dto.VoucherResponse;
import com.ticketrush.backend.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API Controller quản lý mã giảm giá (Vouchers)
 * 
 * 🎫 CHỨC NĂNG QUAN TRỌNG - Phục vụ chiến dịch Flash Sale
 * 
 * Endpoints:
 * - POST /v1/admin/vouchers: Admin tạo mã giảm giá
 * - GET /v1/vouchers/check: Frontend kiểm tra mã (hiển thị tiền giảm ngay)
 * 
 * @author TicketRush Team
 * @version 1.0
 */
@RestController
@RequestMapping("/v1/vouchers")
@RequiredArgsConstructor
@Tag(name = "🎟️ Voucher Management", description = "API quản lý mã giảm giá")
public class VoucherController {

    private final VoucherService voucherService;

    /**
     * 🎯 TÍNH NĂNG QUAN TRỌNG - Admin tạo mã giảm giá
     * 
     * POST /v1/admin/vouchers
     * 
     * Tạo mã giảm giá (voucher) để phục vụ flash sale, khuyến mãi
     * 
     * Ví dụ: 
     * - Code: "TICKETRUSH"
     * - Discount: 50% (tối đa 200.000đ)
     * - Valid: 01/05 - 31/05/2026
     * - Max uses: 100 lần
     * 
     * @param request CreateVoucherRequest chứa code, discount%, maxAmount, duration
     * @return VoucherResponse thông tin voucher vừa tạo
     */
    @PostMapping
    @Operation(
        summary = "➕ Admin: Tạo mã giảm giá",
        description = "Admin API để tạo mã giảm giá cho flash sale. " +
            "Frontend sẽ gọi GET /v1/vouchers/check để kiểm tra mã khi user nhập.",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "✅ Tạo voucher thành công",
            content = @Content(schema = @Schema(implementation = VoucherResponse.class))),
        @ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "❌ Chưa đăng nhập"),
        @ApiResponse(responseCode = "409", description = "❌ Mã voucher đã tồn tại")
    })
    public ResponseEntity<VoucherResponse> createVoucher(@RequestBody CreateVoucherRequest request) {
        try {
            VoucherResponse response = voucherService.createVoucher(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 🎯 TÍNH NĂNG QUAN TRỌNG - Frontend kiểm tra mã giảm giá
     * 
     * GET /v1/vouchers/check?code=TICKETRUSH
     * 
     * ⚡ QUAN TRỌNG: Gọi lúc user gõ mã vào ô input để:
     * - Kiểm tra mã có tồn tại không
     * - Hiển thị % giảm ngay trên màn hình
     * - Tính tiền giảm dự tính để user xem trước
     * 
     * Trả về VoucherCheckResponse gồm:
     * - isValid: có hợp lệ không
     * - discountPercentage: % giảm
     * - maxDiscountAmount: số tiền giảm tối đa
     * - remainingUsage: số lần còn lại
     * - message: thông báo để FE hiển thị (VD: "Giảm 50% tối đa 200.000đ")
     * 
     * Benefit: UX tốt hơn vì user thấy tiền sô diện ngay không cần chờ submit form
     * 
     * @param code Mã voucher (VD: "TICKETRUSH")
     * @return VoucherCheckResponse kết quả check
     */
    @GetMapping("/check")
    @Operation(
        summary = "🔍 Frontend: Kiểm tra mã giảm giá",
        description = "Frontend gọi API này khi user gõ mã vào ô giảm giá. " +
            "Trả về % giảm, số tiền giảm max, và thông báo hiển thị. " +
            "User có thể xem trước tiền giảm mà không cần submit đơn hàng."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "✅ Kiểm tra thành công",
            content = @Content(schema = @Schema(implementation = VoucherCheckResponse.class))),
        @ApiResponse(responseCode = "400", description = "❌ Mã voucher không hợp lệ")
    })
    public ResponseEntity<VoucherCheckResponse> checkVoucher(
        @RequestParam String code
    ) {
        try {
            VoucherCheckResponse response = voucherService.checkVoucher(code);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "📋 Lấy danh sách voucher", description = "Admin API để lấy tất cả voucher")
    public ResponseEntity<java.util.List<VoucherResponse>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @PutMapping("/{id}")
    @Operation(summary = "✏️ Cập nhật voucher", description = "Admin API để sửa voucher")
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable Long id,
            @RequestBody CreateVoucherRequest request) {
        try {
            return ResponseEntity.ok(voucherService.updateVoucher(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "🗑️ Xóa voucher", description = "Admin API để xóa voucher")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        try {
            voucherService.deleteVoucher(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "❤️ Health check", description = "Kiểm tra dịch vụ voucher")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ 🎟️ Dịch vụ Voucher hoạt động bình thường");
    }
}

