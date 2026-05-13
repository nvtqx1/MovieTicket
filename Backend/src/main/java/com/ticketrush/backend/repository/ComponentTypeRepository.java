package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu loại component master.
 */
@Repository
public interface ComponentTypeRepository extends JpaRepository<ComponentType, Long> {
}
