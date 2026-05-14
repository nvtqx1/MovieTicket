package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity biểu diễn suất chiếu của một phim trong một phòng chiếu.
 */
@Entity
@Table(name = "showtimes")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @Column(name = "show_time", nullable = false)
    private LocalTime showTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "is_flash_sale", nullable = false)
    private Boolean isFlashSale = false;

    /**
     * Ghép ngày chiếu và giờ chiếu thành thời điểm bắt đầu.
     *
     * @return thời điểm bắt đầu suất chiếu, hoặc null nếu thiếu ngày hoặc giờ.
     */
    public LocalDateTime getStartTime() {
        if (showDate != null && showTime != null) {
            return LocalDateTime.of(showDate, showTime);
        }
        return null;
    }

    /**
     * Lấy rạp chiếu thông qua phòng chiếu.
     *
     * Annotation {@link Transient} đánh dấu đây là thuộc tính tính toán, không
     * ánh xạ thành cột trong database.
     *
     * @return rạp của phòng chiếu, hoặc null nếu chưa có phòng.
     */
    @Transient
    public Theater getTheater() {
        return room != null ? room.getTheater() : null;
    }
}
