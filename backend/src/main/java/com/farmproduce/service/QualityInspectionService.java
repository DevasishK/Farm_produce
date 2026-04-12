package com.farmproduce.service;

import com.farmproduce.entity.FarmProduce;
import com.farmproduce.entity.QualityGrade;
import com.farmproduce.entity.QualityInspection;
import com.farmproduce.entity.User;
import com.farmproduce.enums.InspectionStatus;
import com.farmproduce.enums.ProduceStatus;
import com.farmproduce.repository.FarmProduceRepository;
import com.farmproduce.repository.QualityGradeRepository;
import com.farmproduce.repository.QualityInspectionRepository;
import com.farmproduce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QualityInspectionService {

    private final QualityInspectionRepository qualityInspectionRepository;
    private final FarmProduceRepository farmProduceRepository;
    private final UserRepository userRepository;
    private final QualityGradeRepository qualityGradeRepository;

    public QualityInspectionService(
            QualityInspectionRepository qualityInspectionRepository,
            FarmProduceRepository farmProduceRepository,
            UserRepository userRepository,
            QualityGradeRepository qualityGradeRepository) {
        this.qualityInspectionRepository = qualityInspectionRepository;
        this.farmProduceRepository = farmProduceRepository;
        this.userRepository = userRepository;
        this.qualityGradeRepository = qualityGradeRepository;
    }

    public List<QualityInspection> findAll() {
        return qualityInspectionRepository.findAll();
    }

    public Optional<QualityInspection> findById(Long id) {
        return qualityInspectionRepository.findById(id);
    }

    private QualityGrade resolveGradeEntity(int score) {
        List<QualityGrade> matches = qualityGradeRepository.findMatchingScore(score);
        if (matches.isEmpty()) {
            throw new IllegalStateException("No quality grade covers score " + score + "; check grade ranges in DB");
        }
        return matches.get(0);
    }

    // First step: link inspector to produce (ASSIGNED). One inspection per lot.
    @Transactional
    public QualityInspection createAssignment(QualityInspection inspection) {
        if (inspection.getFarmProduce() == null || inspection.getFarmProduce().getId() == null) {
            throw new IllegalStateException("Farm produce id is required");
        }
        if (inspection.getInspector() == null || inspection.getInspector().getId() == null) {
            throw new IllegalStateException("Inspector id is required");
        }
        Long produceId = inspection.getFarmProduce().getId();
        if (qualityInspectionRepository.existsByFarmProduceId(produceId)) {
            throw new IllegalStateException("This produce already has an inspection");
        }

        FarmProduce produce = farmProduceRepository.findById(produceId)
                .orElseThrow(() -> new IllegalStateException("Farm produce not found"));
        ProduceStatus st = produce.getStatus();
        if (st != ProduceStatus.SUBMITTED && st != ProduceStatus.UNDER_INSPECTION) {
            throw new IllegalStateException("Produce must be SUBMITTED or UNDER_INSPECTION to assign inspection");
        }

        User inspector = userRepository.findById(inspection.getInspector().getId())
                .orElseThrow(() -> new IllegalStateException("Inspector not found"));

        if (produce.getStatus() == ProduceStatus.SUBMITTED) {
            produce.setStatus(ProduceStatus.UNDER_INSPECTION);
            farmProduceRepository.save(produce);
        }

        QualityInspection entity = new QualityInspection();
        entity.setFarmProduce(produce);
        entity.setInspector(inspector);
        entity.setInspectionStatus(InspectionStatus.ASSIGNED);
        entity.setInspectedAt(inspection.getInspectedAt() != null ? inspection.getInspectedAt() : java.time.LocalDateTime.now());

        return qualityInspectionRepository.save(entity);
    }

    @Transactional
    public Optional<QualityInspection> recordInspection(Long id, Integer score) {
        return qualityInspectionRepository.findById(id).map(inspection -> {
            if (inspection.getInspectionStatus() != InspectionStatus.ASSIGNED) {
                throw new IllegalStateException("Can only record inspection when status is ASSIGNED");
            }
            if (score == null) {
                throw new IllegalStateException("Score is required");
            }
            if (score < 0 || score > 100) {
                throw new IllegalStateException("Score must be between 0 and 100");
            }
            inspection.setScore(score);
            inspection.setQualityGrade(resolveGradeEntity(score));
            inspection.setInspectionStatus(InspectionStatus.INSPECTED);
            return qualityInspectionRepository.save(inspection);
        });
    }

    @Transactional
    public Optional<QualityInspection> approveInspection(Long id) {
        return qualityInspectionRepository.findById(id).map(inspection -> {
            if (inspection.getInspectionStatus() != InspectionStatus.INSPECTED) {
                throw new IllegalStateException("Can only approve when status is INSPECTED");
            }
            inspection.setInspectionStatus(InspectionStatus.APPROVED);
            FarmProduce produce = inspection.getFarmProduce();
            // procurement checks this — not just "graded"
            produce.setStatus(ProduceStatus.GRADED);
            farmProduceRepository.save(produce);
            return qualityInspectionRepository.save(inspection);
        });
    }

    @Transactional
    public Optional<QualityInspection> rejectInspection(Long id) {
        return qualityInspectionRepository.findById(id).map(inspection -> {
            InspectionStatus s = inspection.getInspectionStatus();
            if (s != InspectionStatus.ASSIGNED && s != InspectionStatus.INSPECTED) {
                throw new IllegalStateException("Can only reject from ASSIGNED or INSPECTED");
            }
            inspection.setInspectionStatus(InspectionStatus.REJECTED);
            inspection.setScore(null);
            inspection.setQualityGrade(null);
            FarmProduce produce = inspection.getFarmProduce();
            produce.setStatus(ProduceStatus.REJECTED);
            farmProduceRepository.save(produce);
            return qualityInspectionRepository.save(inspection);
        });
    }

    @Transactional
    public Optional<QualityInspection> update(Long id, QualityInspection patch) {
        return qualityInspectionRepository.findById(id).map(existing -> {
            if (existing.getInspectionStatus() != InspectionStatus.ASSIGNED) {
                throw new IllegalStateException("Updates only allowed while inspection is ASSIGNED");
            }
            if (patch.getInspector() != null && patch.getInspector().getId() != null) {
                User inspector = userRepository.findById(patch.getInspector().getId())
                        .orElseThrow(() -> new IllegalStateException("Inspector not found"));
                existing.setInspector(inspector);
            }
            return qualityInspectionRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        QualityInspection inspection = qualityInspectionRepository.findById(id).orElse(null);
        if (inspection == null) {
            return false;
        }
        if (inspection.getInspectionStatus() != InspectionStatus.ASSIGNED) {
            throw new IllegalStateException("Can only delete an inspection while ASSIGNED");
        }
        qualityInspectionRepository.deleteById(id);
        return true;
    }
}
