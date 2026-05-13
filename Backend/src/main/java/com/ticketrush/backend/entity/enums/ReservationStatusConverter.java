package com.ticketrush.backend.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter ánh xạ {@link ReservationStatus} sang số nguyên trong database.
 *
 * Annotation {@link Converter} cho phép JPA tự áp dụng converter này khi gặp
 * trường kiểu ReservationStatus.
 */
@Converter(autoApply = true)
public class ReservationStatusConverter implements AttributeConverter<ReservationStatus, Integer> {

    /**
     * Chuyển enum trạng thái đặt vé thành mã số lưu database.
     *
     * @param attribute trạng thái đặt vé trong entity.
     * @return mã số trạng thái, hoặc null nếu attribute null.
     */
    @Override
    public Integer convertToDatabaseColumn(ReservationStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    /**
     * Chuyển mã số trong database thành enum trạng thái đặt vé.
     *
     * @param dbData mã số trạng thái trong database.
     * @return enum trạng thái tương ứng, hoặc null nếu dbData null.
     * @throws IllegalArgumentException khi mã số không hợp lệ.
     */
    @Override
    public ReservationStatus convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return ReservationStatus.fromValue(dbData);
    }
}
