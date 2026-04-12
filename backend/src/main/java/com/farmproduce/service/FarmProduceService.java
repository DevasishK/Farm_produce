package com.farmproduce.service;

import com.farmproduce.entity.FarmProduce;
import com.farmproduce.entity.ProduceCategory;
import com.farmproduce.entity.User;
import com.farmproduce.enums.ProduceStatus;
import com.farmproduce.repository.FarmProduceRepository;
import com.farmproduce.repository.ProduceCategoryRepository;
import com.farmproduce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FarmProduceService {

    private final FarmProduceRepository farmProduceRepository;
    private final UserRepository userRepository;
    private final ProduceCategoryRepository produceCategoryRepository;

    public FarmProduceService(
            FarmProduceRepository farmProduceRepository,
            UserRepository userRepository,
            ProduceCategoryRepository produceCategoryRepository) {
        this.farmProduceRepository = farmProduceRepository;
        this.userRepository = userRepository;
        this.produceCategoryRepository = produceCategoryRepository;
    }

    public List<FarmProduce> findAll() {
        return farmProduceRepository.findAll();
    }

    public List<FarmProduce> findByFarmerId(Long farmerId) {
        return farmProduceRepository.findByFarmer_Id(farmerId);
    }

    public List<FarmProduce> findByStatus(ProduceStatus status) {
        return farmProduceRepository.findByStatus(status);
    }

    public Optional<FarmProduce> findById(Long id) {
        return farmProduceRepository.findById(id);
    }

    @Transactional
    public FarmProduce save(FarmProduce produce) {
        if (produce.getFarmer() == null || produce.getFarmer().getId() == null) {
            throw new IllegalStateException("Farmer id is required");
        }
        if (produce.getCategory() == null || produce.getCategory().getId() == null) {
            throw new IllegalStateException("Category id is required");
        }
        User farmer = userRepository.findById(produce.getFarmer().getId())
                .orElseThrow(() -> new IllegalStateException("Farmer not found"));
        ProduceCategory category = produceCategoryRepository.findById(produce.getCategory().getId())
                .orElseThrow(() -> new IllegalStateException("Category not found"));

        produce.setFarmer(farmer);
        produce.setCategory(category);
        if (produce.getQuantity() != null) {
            produce.setRemainingQuantity(produce.getQuantity());
        }
        if (produce.getStatus() == null) {
            produce.setStatus(ProduceStatus.SUBMITTED);
        }
        return farmProduceRepository.save(produce);
    }

    @Transactional
    public Optional<FarmProduce> update(Long id, FarmProduce patch) {
        return farmProduceRepository.findById(id).map(existing -> {
            if (patch.getCategory() != null && patch.getCategory().getId() != null) {
                ProduceCategory category = produceCategoryRepository.findById(patch.getCategory().getId())
                        .orElseThrow(() -> new IllegalStateException("Category not found"));
                existing.setCategory(category);
            }
            if (patch.getQuantity() != null) {
                existing.setQuantity(patch.getQuantity());
            }
            if (patch.getRemainingQuantity() != null) {
                existing.setRemainingQuantity(patch.getRemainingQuantity());
            }
            if (patch.getUnitType() != null) {
                existing.setUnitType(patch.getUnitType());
            }
            if (patch.getHarvestDate() != null) {
                existing.setHarvestDate(patch.getHarvestDate());
            }
            if (patch.getStatus() != null) {
                existing.setStatus(patch.getStatus());
            }
            return farmProduceRepository.save(existing);
        });
    }

    @Transactional
    public Optional<FarmProduce> markUnderInspection(Long id) {
        return farmProduceRepository.findById(id).map(produce -> {
            if (produce.getStatus() != ProduceStatus.SUBMITTED) {
                throw new IllegalStateException("Only SUBMITTED produce can move to UNDER_INSPECTION");
            }
            produce.setStatus(ProduceStatus.UNDER_INSPECTION);
            return farmProduceRepository.save(produce);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!farmProduceRepository.existsById(id)) {
            return false;
        }
        farmProduceRepository.deleteById(id);
        return true;
    }
}
