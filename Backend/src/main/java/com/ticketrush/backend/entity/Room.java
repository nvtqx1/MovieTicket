package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entity biểu diễn phòng chiếu thuộc một rạp.
 *
 * Annotation {@link SQLDelete} thực hiện xóa mềm bằng cách cập nhật
 * {@code is_deleted}; {@link SQLRestriction} tự động ẩn các phòng đã xóa mềm
 * khỏi truy vấn Hibernate.
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
