package com.ticketrush.backend.exception;

/**
 * Exception biểu diễn lỗi không tìm thấy tài nguyên.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Tạo exception với thông báo lỗi cụ thể.
     *
     * @param message thông báo mô tả tài nguyên không tìm thấy.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
