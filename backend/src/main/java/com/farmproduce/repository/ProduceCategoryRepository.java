package com.farmproduce.repository;

import com.farmproduce.entity.ProduceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProduceCategoryRepository extends JpaRepository<ProduceCategory, Long> {
}
