package com.pharmacy.inventory.repository;

import com.pharmacy.inventory.entity.Medication;
import com.pharmacy.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Medication, Long> {

    // Total medications count
    @Query("SELECT COUNT(m) FROM Medication m WHERE m.deleted = false")
    Long countTotalMedications();

    // Low stock items (quantity < lowStockThreshold)
    @Query("SELECT COUNT(m) FROM Medication m WHERE m.quantity < m.lowStockThreshold AND m.deleted = false")
    Long countLowStockItems();

    // Out of stock items
    @Query("SELECT COUNT(m) FROM Medication m WHERE m.quantity <= 0 AND m.deleted = false")
    Long countOutOfStockItems();

    // Total categories count
    @Query("SELECT COUNT(c) FROM Category c")
    Long countTotalCategories();

    // Total inventory value
    @Query("SELECT COALESCE(SUM(m.quantity * m.price), 0) FROM Medication m WHERE m.deleted = false")
    BigDecimal calculateTotalInventoryValue();

    // Medications expiring soon (within next 30 days)
    @Query("SELECT COUNT(m) FROM Medication m WHERE m.expirationDate BETWEEN :startDate AND :endDate AND m.deleted = false")
    Long countMedicationsExpiringSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Recent medications (last 10 added)
    @Query("SELECT m FROM Medication m WHERE m.deleted = false ORDER BY m.createdAt DESC LIMIT 10")
    List<Medication> findRecentMedications();

    // Category distribution
    @Query("SELECT c.name, COUNT(m) FROM Category c LEFT JOIN c.medications m WHERE m.deleted = false OR m IS NULL GROUP BY c.id, c.name")
    List<Object[]> findCategoryDistribution();

    // Stock distribution by category
    @Query("SELECT c.name, COALESCE(SUM(m.quantity), 0) FROM Category c LEFT JOIN c.medications m WHERE m.deleted = false OR m IS NULL GROUP BY c.id, c.name")
    List<Object[]> findStockDistribution();

    // Medications expiring soon with details
    @Query("SELECT m FROM Medication m WHERE m.expirationDate BETWEEN :startDate AND :endDate AND m.deleted = false ORDER BY m.expirationDate ASC")
    List<Medication> findExpiringSoonMedications(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}