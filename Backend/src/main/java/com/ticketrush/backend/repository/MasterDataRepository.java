package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterDataRepository extends JpaRepository<MasterData, Long> {
}