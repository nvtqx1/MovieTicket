package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity lưu vai trò phân quyền của người dùng.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

}
