package com.farmproduce.service;

import com.farmproduce.entity.ProduceCategory;
import com.farmproduce.repository.ProduceCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProduceCategoryService {

    private final ProduceCategoryRepository produceCategoryRepository;

    public ProduceCategoryService(ProduceCategoryRepository produceCategoryRepository) {
        this.produceCategoryRepository = produceCategoryRepository;
    }

    public List<ProduceCategory> findAll() {
        return produceCategoryRepository.findAll();
    }

    public Optional<ProduceCategory> findById(Long id) {
        return produceCategoryRepository.findById(id);
    }

    @Transactional
    public ProduceCategory save(ProduceCategory category) {
        return produceCategoryRepository.save(category);
    }

    @Transactional
    public Optional<ProduceCategory> update(Long id, ProduceCategory patch) {
        return produceCategoryRepository.findById(id).map(existing -> {
            if (patch.getCategoryName() != null) {
                existing.setCategoryName(patch.getCategoryName());
            }
            if (patch.getDescription() != null) {
                existing.setDescription(patch.getDescription());
            }
            return produceCategoryRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!produceCategoryRepository.existsById(id)) {
            return false;
        }
        produceCategoryRepository.deleteById(id);
        return true;
    }
}
