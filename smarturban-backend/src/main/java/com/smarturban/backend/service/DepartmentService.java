package com.smarturban.backend.service;

import com.smarturban.backend.entity.Department;
import com.smarturban.backend.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public List<Department> getActiveDepartments() {
        return departmentRepository.findByEnabledTrue();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + id));
    }

    public Department saveDepartment(Department department) {
        if (department.getName() != null) {
            String trimmedName = department.getName().trim();
            boolean exists = departmentRepository.findAll().stream()
                    .anyMatch(d -> d.getName().equalsIgnoreCase(trimmedName) && !d.getId().equals(department.getId()));
            if (exists) {
                throw new RuntimeException("A department with the name '" + trimmedName + "' already exists.");
            }
        }
        return departmentRepository.save(department);
    }

    public void deleteOrDeactivateDepartment(Long id) {
        Department department = getDepartmentById(id);
        department.setEnabled(false);
        departmentRepository.save(department);
    }
}
