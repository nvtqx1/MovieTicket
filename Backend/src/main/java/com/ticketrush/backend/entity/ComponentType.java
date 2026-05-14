package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity lưu loại dữ liệu master dùng để phân nhóm các giá trị cấu hình.
 */
@Entity
@Table(name = "component_types")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ComponentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

}
