package com.farmproduce.controller;

import com.farmproduce.entity.QualityInspection;
import com.farmproduce.service.QualityInspectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inspections")
public class QualityInspectionController {

    private final QualityInspectionService qualityInspectionService;

    public QualityInspectionController(QualityInspectionService qualityInspectionService) {
        this.qualityInspectionService = qualityInspectionService;
    }

    public record ScoreRequest(Integer score, Integer qualityScore) {
        Integer resolved() {
            return qualityScore != null ? qualityScore : score;
        }
    }

    @GetMapping
    public List<QualityInspection> list() {
        return qualityInspectionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QualityInspection> get(@PathVariable Long id) {
        return qualityInspectionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<QualityInspection> create(@RequestBody QualityInspection inspection) {
        QualityInspection saved = qualityInspectionService.createAssignment(inspection);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}/inspect")
    public ResponseEntity<QualityInspection> inspect(
            @PathVariable Long id, @RequestBody(required = false) ScoreRequest body) {
        Integer score = body == null ? null : body.resolved();
        return qualityInspectionService.recordInspection(id, score)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<QualityInspection> approve(@PathVariable Long id) {
        return qualityInspectionService.approveInspection(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<QualityInspection> reject(@PathVariable Long id) {
        return qualityInspectionService.rejectInspection(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<QualityInspection> update(@PathVariable Long id, @RequestBody QualityInspection inspection) {
        return qualityInspectionService.update(id, inspection)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!qualityInspectionService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
