package com.farmproduce.repository;

import com.farmproduce.entity.QualityGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QualityGradeRepository extends JpaRepository<QualityGrade, Long> {

    Optional<QualityGrade> findByCode(String code);

    @Query(
            "SELECT g FROM QualityGrade g WHERE g.minScore <= :score AND g.maxScore >= :score ORDER BY g.minScore DESC")
    List<QualityGrade> findMatchingScore(@Param("score") int score);
}
