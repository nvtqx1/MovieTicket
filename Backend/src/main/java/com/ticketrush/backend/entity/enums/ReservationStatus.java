package com.ticketrush.backend.entity.enums;

public enum ReservationStatus {
    LOCKED(1),
    PAID(2),
    CANCELED(3);

    private final int value;

    ReservationStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ReservationStatus fromValue(long value) {
        for (ReservationStatus status : ReservationStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ReservationStatus value: " + value);
    }
}
