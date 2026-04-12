package com.farmproduce.controller;

import com.farmproduce.entity.ProduceCategory;
import com.farmproduce.service.ProduceCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class ProduceCategoryController {

    private final ProduceCategoryService produceCategoryService;

    public ProduceCategoryController(ProduceCategoryService produceCategoryService) {
        this.produceCategoryService = produceCategoryService;
    }

    @GetMapping
    public List<ProduceCategory> list() {
        return produceCategoryService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduceCategory> get(@PathVariable Long id) {
        return produceCategoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProduceCategory> create(@RequestBody ProduceCategory category) {
        ProduceCategory saved = produceCategoryService.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProduceCategory> update(@PathVariable Long id, @RequestBody ProduceCategory category) {
        return produceCategoryService.update(id, category)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!produceCategoryService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
