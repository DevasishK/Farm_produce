package com.farmproduce.repository;

import com.farmproduce.entity.ProduceInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProduceInventoryRepository extends JpaRepository<ProduceInventory, Long> {

    Optional<ProduceInventory> findByCategoryId(Long categoryId);
}
