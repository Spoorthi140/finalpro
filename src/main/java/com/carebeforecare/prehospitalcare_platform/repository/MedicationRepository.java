package com.carebeforecare.prehospitalcare_platform.repository;

import com.carebeforecare.prehospitalcare_platform.model.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    List<Medication> findByDrugClass(String drugClass);

    List<Medication> findByPrescriptionRequired(Boolean prescriptionRequired);

    @Query("SELECT m FROM Medication m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.drugClass) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Medication> searchMedications(@Param("query") String query);

    @Query("SELECT DISTINCT m.drugClass FROM Medication m")
    List<String> findDistinctDrugClasses();
}