package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity lưu loại ghế và hệ số nhân giá vé.
 */
@Entity
@Table(name = "seat_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "price_multiplier", nullable = false, precision = 4, scale = 2)
    private BigDecimal priceMultiplier = BigDecimal.ONE;
}
