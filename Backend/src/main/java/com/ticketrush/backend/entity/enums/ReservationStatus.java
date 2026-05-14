package com.ticketrush.backend.entity.enums;

/**
 * Enum biểu diễn trạng thái đơn đặt vé.
 *
 * Mỗi trạng thái có mã số riêng để lưu xuống database.
 */
public enum ReservationStatus {
    LOCKED(1),
    PAID(2),
    CANCELED(3);

    private final int value;

    ReservationStatus(int value) {
        this.value = value;
    }

    /**
     * Lấy mã số của trạng thái đặt vé.
     *
     * @return mã số dùng để lưu trạng thái trong database.
     */
    public int getValue() {
        return value;
    }

    /**
     * Chuyển mã số từ database sang enum trạng thái đặt vé.
     *
     * @param value mã số trạng thái.
     * @return enum trạng thái tương ứng.
     * @throws IllegalArgumentException khi mã số không thuộc trạng thái hợp lệ.
     */
    public static ReservationStatus fromValue(long value) {
        for (ReservationStatus status : ReservationStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ReservationStatus value: " + value);
    }
}
