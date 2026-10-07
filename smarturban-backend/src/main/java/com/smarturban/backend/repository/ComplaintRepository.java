package com.smarturban.backend.repository;

import com.smarturban.backend.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Complaint> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
    long countByCategoryId(Long categoryId);
    long countByDepartmentId(Long departmentId);
}
