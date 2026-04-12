package com.farmproduce.controller;

import com.farmproduce.entity.ProduceInventory;
import com.farmproduce.service.ProduceInventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class ProduceInventoryController {

    private final ProduceInventoryService produceInventoryService;

    public ProduceInventoryController(ProduceInventoryService produceInventoryService) {
        this.produceInventoryService = produceInventoryService;
    }

    @GetMapping
    public List<ProduceInventory> list() {
        return produceInventoryService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduceInventory> get(@PathVariable Long id) {
        return produceInventoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProduceInventory> create(@RequestBody ProduceInventory inventory) {
        ProduceInventory saved = produceInventoryService.save(inventory);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProduceInventory> update(@PathVariable Long id, @RequestBody ProduceInventory inventory) {
        return produceInventoryService.update(id, inventory)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!produceInventoryService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
