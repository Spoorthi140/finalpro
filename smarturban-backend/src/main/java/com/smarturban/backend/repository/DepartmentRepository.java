package com.smarturban.backend.repository;

import com.smarturban.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByEnabledTrue();
    Optional<Department> findByName(String name);
}
