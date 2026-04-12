package com.farmproduce.controller;

import com.farmproduce.entity.FarmProduce;
import com.farmproduce.enums.ProduceStatus;
import com.farmproduce.service.FarmProduceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produce")
public class FarmProduceController {

    private final FarmProduceService farmProduceService;

    public FarmProduceController(FarmProduceService farmProduceService) {
        this.farmProduceService = farmProduceService;
    }

    @GetMapping
    public List<FarmProduce> list(
            @RequestParam(required = false) Long farmerId,
            @RequestParam(required = false) ProduceStatus status) {
        if (farmerId != null) {
            return farmProduceService.findByFarmerId(farmerId);
        }
        if (status != null) {
            return farmProduceService.findByStatus(status);
        }
        return farmProduceService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FarmProduce> get(@PathVariable Long id) {
        return farmProduceService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FarmProduce> create(@RequestBody FarmProduce produce) {
        FarmProduce saved = farmProduceService.save(produce);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FarmProduce> update(@PathVariable Long id, @RequestBody FarmProduce produce) {
        return farmProduceService.update(id, produce)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/under-inspection")
    public ResponseEntity<FarmProduce> underInspection(@PathVariable Long id) {
        return farmProduceService.markUnderInspection(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!farmProduceService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
