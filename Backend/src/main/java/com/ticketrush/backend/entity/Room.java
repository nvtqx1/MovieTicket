package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entity đại diện cho Phòng chiếu trong Rạp.
 *
 * Soft Delete Strategy:
 * - @SQLDelete: Khi gọi roomRepository.deleteById(id), Hibernate sẽ KHÔNG chạy
 *   câu lệnh DELETE FROM rooms WHERE id=?. Thay vào đó, nó sẽ chạy:
 *   UPDATE rooms SET is_deleted = true WHERE id=?
 *   → Dữ liệu vẫn còn trong DB, chỉ bị đánh dấu là "đã xóa".
 *
 * - @SQLRestriction: Tự động thêm điều kiện "is_deleted = false" vào MỌI câu
 *   SELECT mà Hibernate sinh ra cho entity này. Nghĩa là:
 *   roomRepository.findAll()         → SELECT ... WHERE is_deleted = false
 *   roomRepository.findById(id)      → SELECT ... WHERE id=? AND is_deleted = false
 *   roomRepository.findByTheaterId() → SELECT ... WHERE theater_id=? AND is_deleted = false
 *   → Phòng đã xóa mềm sẽ tự động bị ẩn khỏi tất cả query, KHÔNG cần sửa Repository.
 *
 * @author TicketRush Team
 * @version 2.0
 */
@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE rooms SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "matrix_rows")
    private Integer matrixRows;

    @Column(name = "matrix_cols")
    private Integer matrixCols;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;
}
