package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_blocks")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_user_id", nullable = false)
    private User blockedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_by_id", nullable = false)
    private User blockedBy;

    @Column(nullable = false)
    private String reason;

    @CreationTimestamp
    @Column(name = "blocked_at", nullable = false, updatable = false)
    private LocalDateTime blockedAt;

    @Column(name = "is_admin_block", nullable = false)
    private Boolean isAdminBlock = false;

}
