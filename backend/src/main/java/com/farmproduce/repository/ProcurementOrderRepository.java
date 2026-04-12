package com.farmproduce.repository;

import com.farmproduce.entity.ProcurementOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcurementOrderRepository extends JpaRepository<ProcurementOrder, Long> {
}
