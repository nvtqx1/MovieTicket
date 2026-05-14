package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity biểu diễn ghế cố định trong phòng chiếu.
 *
 * Dữ liệu này là sơ đồ ghế mẫu của phòng và được dùng để tạo ghế theo từng
 * suất chiếu.
 */
@Entity
@Table(name = "room_seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_type_id", nullable = false)
    private SeatType seatType;

    @Column(name = "row_index", nullable = false)
    private Integer rowIndex;

    @Column(name = "col_index", nullable = false)
    private Integer colIndex;
}
