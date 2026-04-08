package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "master_data",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_master_data", columnNames = {"component_type_id", "data_value"})
        }
)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MasterData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_value", nullable = false, length = 50)
    private String dataValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_type_id", nullable = false)
    private ComponentType componentType;

}