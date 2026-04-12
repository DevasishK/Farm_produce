package com.farmproduce.controller;

import com.farmproduce.entity.ProcurementOrder;
import com.farmproduce.service.ProcurementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procurement")
public class ProcurementController {

    private final ProcurementService procurementService;

    public ProcurementController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    @GetMapping
    public List<ProcurementOrder> list() {
        return procurementService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcurementOrder> get(@PathVariable Long id) {
        return procurementService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProcurementOrder> create(@RequestBody ProcurementOrder order) {
        ProcurementOrder saved = procurementService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ProcurementOrder> approve(@PathVariable Long id) {
        return procurementService.approveOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ProcurementOrder> complete(@PathVariable Long id) {
        return procurementService.completeOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ProcurementOrder> cancel(@PathVariable Long id) {
        return procurementService.cancelOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcurementOrder> update(@PathVariable Long id, @RequestBody ProcurementOrder order) {
        return procurementService.update(id, order)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!procurementService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
