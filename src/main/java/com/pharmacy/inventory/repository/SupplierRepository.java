package com.pharmacy.inventory.repository;

import com.pharmacy.inventory.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // Check if supplier with same name already exists
    boolean existsByName(String name);

    // Check if supplier with same email already exists
    boolean existsByEmail(String email);

    // Check if supplier with same phone already exists
    boolean existsByPhone(String phone);

    // Find supplier by name
    Optional<Supplier> findByName(String name);

    // Find supplier by email
    Optional<Supplier> findByEmail(String email);

    // Find supplier by phone
    Optional<Supplier> findByPhone(String phone);

    // Search suppliers by name, email, or phone
    @Query("SELECT s FROM Supplier s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Supplier> searchSuppliers(@Param("searchTerm") String searchTerm);

    // Search with pagination
    @Query("SELECT s FROM Supplier s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Supplier> searchSuppliers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find active suppliers
    List<Supplier> findByActiveTrue();

    // Find active suppliers with pagination
    Page<Supplier> findByActiveTrue(Pageable pageable);

    // Find suppliers by status
    List<Supplier> findByActive(Boolean active);

    // Find suppliers by status with pagination
    Page<Supplier> findByActive(Boolean active, Pageable pageable);

    // Find suppliers created after specific date
    List<Supplier> findByCreatedAtAfter(LocalDateTime date);

    // Find suppliers by contact person
    List<Supplier> findByContactPersonContainingIgnoreCase(String contactPerson);
}