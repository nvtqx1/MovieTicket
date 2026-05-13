package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.request.CreateVoucherRequest;
import com.ticketrush.backend.dto.response.VoucherCheckResponse;
import com.ticketrush.backend.dto.response.VoucherResponse;
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
 * Controller quản lý mã giảm giá.
 *
 * Endpoint kiểm tra voucher phục vụ frontend hiển thị mức giảm trước khi tạo
 * đơn đặt vé.
 */
@RestController
@RequestMapping("/v1/vouchers")
@RequiredArgsConstructor
@Tag(name = "Voucher Management", description = "API quản lý mã giảm giá")
public class VoucherController {

    private final VoucherService voucherService;

    /**
     * Tạo voucher mới.
     *
     * @param request dữ liệu voucher cần tạo.
     * @return voucher vừa được tạo.
     */
    @PostMapping
    @Operation(
        summary = "Admin tạo mã giảm giá",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tạo voucher thành công",
            content = @Content(schema = @Schema(implementation = VoucherResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "409", description = "Mã voucher đã tồn tại")
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
     * Kiểm tra tính hợp lệ và mức giảm của voucher.
     *
     * @param code mã voucher cần kiểm tra.
     * @return kết quả kiểm tra voucher.
     */
    @GetMapping("/check")
    @Operation(summary = "Kiểm tra mã giảm giá")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Kiểm tra thành công",
            content = @Content(schema = @Schema(implementation = VoucherCheckResponse.class))),
        @ApiResponse(responseCode = "400", description = "Mã voucher không hợp lệ")
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

    /**
     * Lấy danh sách toàn bộ voucher.
     *
     * @return danh sách voucher.
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách voucher")
    public ResponseEntity<java.util.List<VoucherResponse>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    /**
     * Cập nhật voucher.
     *
     * @param id ID voucher cần cập nhật.
     * @param request dữ liệu voucher mới.
     * @return voucher sau cập nhật.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật voucher")
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable Long id,
            @RequestBody CreateVoucherRequest request) {
        try {
            return ResponseEntity.ok(voucherService.updateVoucher(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Xóa voucher.
     *
     * @param id ID voucher cần xóa.
     * @return phản hồi rỗng khi xóa thành công.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa voucher")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        try {
            voucherService.deleteVoucher(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Kiểm tra trạng thái dịch vụ voucher.
     *
     * @return thông báo dịch vụ đang hoạt động.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Kiểm tra dịch vụ voucher")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Dịch vụ Voucher hoạt động bình thường");
    }
}
