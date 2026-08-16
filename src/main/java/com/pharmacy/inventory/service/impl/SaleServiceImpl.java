package com.pharmacy.inventory.service.impl;

import com.pharmacy.inventory.dto.request.CreateSaleRequest;
import com.pharmacy.inventory.dto.response.SalesReportResponse;
import com.pharmacy.inventory.entity.Medication;
import com.pharmacy.inventory.entity.Sale;
import com.pharmacy.inventory.repository.MedicationRepository;
import com.pharmacy.inventory.repository.SaleRepository;
import com.pharmacy.inventory.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final MedicationRepository medicationRepository;

    @Autowired
    public SaleServiceImpl(SaleRepository saleRepository, MedicationRepository medicationRepository) {
        this.saleRepository = saleRepository;
        this.medicationRepository = medicationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReportResponse getSalesReport() {
        SalesReportResponse response = new SalesReportResponse();
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate startOfYear = today.withDayOfYear(1);

        // Summary metrics
        BigDecimal todaySales = saleRepository.getTotalSalesByDate(today);
        BigDecimal thisMonthRevenue = saleRepository.getTotalSalesBetween(startOfMonth, today);
        BigDecimal totalRevenue = saleRepository.getTotalSalesBetween(startOfYear, today);
        BigDecimal totalProfit = saleRepository.getTotalProfitBetween(startOfYear, today);
        Long totalOrders = saleRepository.count();

        List<Medication> lowStockMeds = medicationRepository.findLowStockMedications();
        Long lowStockItems = (long) lowStockMeds.size();

        response.setTodaySales(todaySales);
        response.setThisMonthRevenue(thisMonthRevenue);
        response.setTotalRevenue(totalRevenue);
        response.setTotalProfit(totalProfit);
        response.setTotalOrders(totalOrders);
        response.setLowStockItems(lowStockItems);

        // Daily sales trend (last 7 days)
        List<Map<String, Object>> dailySales = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            BigDecimal dayTotal = saleRepository.getTotalSalesByDate(date);
            Map<String, Object> map = new HashMap<>();
            map.put("date", date.toString());
            map.put("total", dayTotal);
            dailySales.add(map);
        }
        response.setDailySales(dailySales);

        // Monthly sales trend (last 6 months)
        List<Map<String, Object>> monthlySales = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            if (monthEnd.isAfter(today)) {
                monthEnd = today;
            }
            BigDecimal monthTotal = saleRepository.getTotalSalesBetween(monthStart, monthEnd);
            Map<String, Object> map = new HashMap<>();
            map.put("month", monthStart.getMonth().name().substring(0, 3) + " " + monthStart.getYear());
            map.put("revenue", monthTotal);
            monthlySales.add(map);
        }
        response.setMonthlySales(monthlySales);

        // Top 5 medicines
        List<Object[]> topMedicinesRaw = saleRepository.findTopSellingMedicines();
        List<Map<String, Object>> topSellingMedicines = new ArrayList<>();
        int count = 0;
        for (Object[] row : topMedicinesRaw) {
            if (count >= 5) break;
            Map<String, Object> map = new HashMap<>();
            map.put("name", row[0]);
            map.put("quantity", row[1]);
            map.put("revenue", row[2]);
            topSellingMedicines.add(map);
            count++;
        }
        response.setTopSellingMedicines(topSellingMedicines);

        // Recent transactions
        List<Sale> recentSales = saleRepository.findTop10ByOrderBySaleDateDescIdDesc();
        List<Map<String, Object>> recentTransactions = new ArrayList<>();
        for (Sale sale : recentSales) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sale.getId());
            map.put("date", sale.getSaleDate().toString());
            map.put("medicationName", sale.getMedication() != null ? sale.getMedication().getName() : "Unknown");
            map.put("quantity", sale.getQuantity());
            map.put("unitPrice", sale.getUnitPrice());
            map.put("totalPrice", sale.getTotalPrice());
            recentTransactions.add(map);
        }
        response.setRecentTransactions(recentTransactions);

        return response;
    }

    @Override
    public Sale createSale(CreateSaleRequest request) {
        Medication medication = medicationRepository.findById(request.getMedicationId())
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + request.getMedicationId()));

        if (medication.getDeleted()) {
            throw new RuntimeException("Cannot sell deleted medication");
        }

        if (medication.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock for medication: " + medication.getName());
        }

        medication.setQuantity(medication.getQuantity() - request.getQuantity());
        medicationRepository.save(medication);

        BigDecimal unitPrice = medication.getPrice();
        BigDecimal costPrice = unitPrice.multiply(BigDecimal.valueOf(0.7)); // Estimated 30% margin
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        Sale sale = new Sale(LocalDate.now(), request.getQuantity(), unitPrice, costPrice, totalPrice, medication);
        return saleRepository.save(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }
}
