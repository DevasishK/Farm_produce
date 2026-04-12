package com.farmproduce.entity;

import com.farmproduce.enums.InspectionStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quality_inspections")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class QualityInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "farm_produce_id", unique = true)
    private FarmProduce farmProduce;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "inspector_id")
    private User inspector;

    @JsonProperty("qualityScore")
    @JsonAlias("score")
    private Integer score;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "quality_grade_id")
    @JsonProperty("assignedGrade")
    @JsonAlias("qualityGrade")
    private QualityGrade qualityGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionStatus inspectionStatus = InspectionStatus.ASSIGNED;

    @Column(nullable = false)
    @JsonProperty("inspectionDate")
    @JsonAlias("inspectedAt")
    private LocalDateTime inspectedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FarmProduce getFarmProduce() {
        return farmProduce;
    }

    public void setFarmProduce(FarmProduce farmProduce) {
        this.farmProduce = farmProduce;
    }

    public User getInspector() {
        return inspector;
    }

    public void setInspector(User inspector) {
        this.inspector = inspector;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public QualityGrade getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(QualityGrade qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public InspectionStatus getInspectionStatus() {
        return inspectionStatus;
    }

    public void setInspectionStatus(InspectionStatus inspectionStatus) {
        this.inspectionStatus = inspectionStatus;
    }

    public LocalDateTime getInspectedAt() {
        return inspectedAt;
    }

    public void setInspectedAt(LocalDateTime inspectedAt) {
        this.inspectedAt = inspectedAt;
    }
}
