package com.pharmacy.inventory.repository;

import com.pharmacy.inventory.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);

    List<Sale> findTop10ByOrderBySaleDateDescIdDesc();

    @Query("SELECT COALESCE(SUM(s.totalPrice), 0) FROM Sale s WHERE s.saleDate = :date")
    BigDecimal getTotalSalesByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(s.totalPrice), 0) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSalesBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM((s.unitPrice - COALESCE(s.costPrice, s.unitPrice * 0.7)) * s.quantity), 0) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalProfitBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s.medication.name, SUM(s.quantity), SUM(s.totalPrice) FROM Sale s GROUP BY s.medication.id, s.medication.name ORDER BY SUM(s.quantity) DESC")
    List<Object[]> findTopSellingMedicines();
}
