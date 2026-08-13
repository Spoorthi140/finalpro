package com.pharmacy.inventory.repository;

import com.pharmacy.inventory.entity.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    // Basic count methods
    Long countByQuantityLessThan(Integer quantity);
    Long countByQuantity(Integer quantity);
    Long countByQuantityGreaterThan(Integer quantity);
    Long countByQuantityBetween(Integer minQuantity, Integer maxQuantity);

    // For expiring soon medications
    List<Medication> findByExpirationDateBetween(LocalDate startDate, LocalDate endDate);
    Long countByExpirationDateBefore(LocalDate date);

    // For recent medications
    List<Medication> findTop10ByOrderByCreatedAtDesc();

    // For total inventory value
    @Query("SELECT COALESCE(SUM(m.quantity * m.price), 0) FROM Medication m WHERE m.deleted = false")
    BigDecimal getTotalInventoryValue();

    // Find by type (corrected from medicationType to type)
    List<Medication> findByType(String type);

    // Search by name
    List<Medication> findByNameContainingIgnoreCase(String name);

    // Find by exact name
    Optional<Medication> findByName(String name);

    // Find medications by category
    List<Medication> findByCategoryId(Long categoryId);

    // Find medications by supplier
    List<Medication> findBySupplierId(Long supplierId);

    // Find active medications (not deleted)
    List<Medication> findByDeletedFalse();

    // Find active medications with pagination
    Page<Medication> findByDeletedFalse(Pageable pageable);

    // Find low stock medications
    @Query("SELECT m FROM Medication m WHERE m.quantity < m.lowStockThreshold AND m.deleted = false")
    List<Medication> findLowStockMedications();

    // Find medications by name ignoring case and not deleted
    List<Medication> findByNameContainingIgnoreCaseAndDeletedFalse(String name);

    // Find medications by category and not deleted
    List<Medication> findByCategoryIdAndDeletedFalse(Long categoryId);

    // Find medications by type and not deleted
    List<Medication> findByTypeAndDeletedFalse(String type);
}