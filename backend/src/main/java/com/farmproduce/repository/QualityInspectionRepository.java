package com.farmproduce.repository;

import com.farmproduce.entity.QualityInspection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QualityInspectionRepository extends JpaRepository<QualityInspection, Long> {

    Optional<QualityInspection> findByFarmProduceId(Long farmProduceId);

    boolean existsByFarmProduceId(Long farmProduceId);
}
