package com.farmproduce.repository;

import com.farmproduce.entity.FarmProduce;
import com.farmproduce.enums.ProduceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmProduceRepository extends JpaRepository<FarmProduce, Long> {

    List<FarmProduce> findByFarmer_Id(Long farmerId);

    List<FarmProduce> findByStatus(ProduceStatus status);
}
