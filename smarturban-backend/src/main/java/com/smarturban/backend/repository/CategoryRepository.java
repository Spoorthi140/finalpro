package com.smarturban.backend.repository;

import com.smarturban.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByEnabledTrue();
    Optional<Category> findByName(String name);
}
