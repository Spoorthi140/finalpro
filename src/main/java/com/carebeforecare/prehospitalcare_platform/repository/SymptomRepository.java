package com.carebeforecare.prehospitalcare_platform.repository;

import com.carebeforecare.prehospitalcare_platform.model.entity.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, Long> {

    List<Symptom> findByCategory(String category);

    @Query("SELECT s FROM Symptom s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Symptom> searchSymptoms(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT s.category FROM Symptom s")
    List<String> findDistinctCategories();
}