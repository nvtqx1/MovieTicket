package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho Ghế cố định trong Phòng chiếu.
 * 
 * Mỗi Room có 1 bộ ghế cố định (VD: A1-A15, B1-B15...).
 * Khi Admin tạo Showtime, hệ thống sẽ copy danh sách ghế này
 * sang bảng seats (ghế theo suất chiếu) để tracking đặt vé.
 *
 * Quan hệ:
 *   Room (1) ←→ (N) RoomSeat
 *   SeatType (1) ←→ (N) RoomSeat
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
