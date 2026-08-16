package com.pharmacy.inventory.service;

import com.pharmacy.inventory.dto.request.CreateSaleRequest;
import com.pharmacy.inventory.dto.response.SalesReportResponse;
import com.pharmacy.inventory.entity.Sale;

import java.util.List;

public interface SaleService {

    SalesReportResponse getSalesReport();

    Sale createSale(CreateSaleRequest request);

    List<Sale> getAllSales();
}
