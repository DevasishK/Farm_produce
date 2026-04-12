package com.farmproduce.service;

import com.farmproduce.entity.ProduceCategory;
import com.farmproduce.entity.ProduceInventory;
import com.farmproduce.enums.InventoryStatus;
import com.farmproduce.repository.ProduceCategoryRepository;
import com.farmproduce.repository.ProduceInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProduceInventoryService {

    public static final BigDecimal LOW_STOCK_BELOW = new BigDecimal("20");

    private final ProduceInventoryRepository produceInventoryRepository;
    private final ProduceCategoryRepository produceCategoryRepository;

    public ProduceInventoryService(
            ProduceInventoryRepository produceInventoryRepository,
            ProduceCategoryRepository produceCategoryRepository) {
        this.produceInventoryRepository = produceInventoryRepository;
        this.produceCategoryRepository = produceCategoryRepository;
    }

    public static InventoryStatus statusForQuantity(BigDecimal qty) {
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            return InventoryStatus.OUT_OF_STOCK;
        }
        if (qty.compareTo(LOW_STOCK_BELOW) < 0) {
            return InventoryStatus.LOW_STOCK;
        }
        return InventoryStatus.AVAILABLE;
    }

    public List<ProduceInventory> findAll() {
        return produceInventoryRepository.findAll();
    }

    public Optional<ProduceInventory> findById(Long id) {
        return produceInventoryRepository.findById(id);
    }

    @Transactional
    public ProduceInventory save(ProduceInventory inventory) {
        if (inventory.getCategory() == null || inventory.getCategory().getId() == null) {
            throw new IllegalStateException("Category id is required");
        }
        ProduceCategory category = produceCategoryRepository.findById(inventory.getCategory().getId())
                .orElseThrow(() -> new IllegalStateException("Category not found"));
        inventory.setCategory(category);
        if (inventory.getAvailableQuantity() == null) {
            inventory.setAvailableQuantity(BigDecimal.ZERO);
        }
        inventory.setInventoryStatus(statusForQuantity(inventory.getAvailableQuantity()));
        inventory.setLastUpdated(LocalDateTime.now());
        return produceInventoryRepository.save(inventory);
    }

    // runs when procurement hits COMPLETED
    @Transactional
    public void addToInventory(Long categoryId, BigDecimal amount) {
        ProduceCategory category = produceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("Category not found"));
        ProduceInventory inv = produceInventoryRepository.findByCategoryId(categoryId).orElseGet(() -> {
            ProduceInventory row = new ProduceInventory();
            row.setCategory(category);
            row.setAvailableQuantity(BigDecimal.ZERO);
            return row;
        });
        BigDecimal next = inv.getAvailableQuantity().add(amount);
        inv.setAvailableQuantity(next);
        inv.setInventoryStatus(statusForQuantity(next));
        inv.setLastUpdated(LocalDateTime.now());
        produceInventoryRepository.save(inv);
    }

    @Transactional
    public Optional<ProduceInventory> update(Long id, ProduceInventory patch) {
        return produceInventoryRepository.findById(id).map(existing -> {
            if (patch.getAvailableQuantity() != null) {
                existing.setAvailableQuantity(patch.getAvailableQuantity());
                existing.setInventoryStatus(statusForQuantity(existing.getAvailableQuantity()));
            }
            existing.setLastUpdated(LocalDateTime.now());
            return produceInventoryRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!produceInventoryRepository.existsById(id)) {
            return false;
        }
        produceInventoryRepository.deleteById(id);
        return true;
    }
}
