package com.pharmacy.inventory.repository;

import com.pharmacy.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Remove all "DeletedFalse" methods and use regular JPA methods
    boolean existsByName(String name);

    // That's it! Just use the default JpaRepository methods:
    // findAll(), findById(), save(), delete(), etc.
}