package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu master data.
 */
@Repository
public interface MasterDataRepository extends JpaRepository<MasterData, Long> {
}
