package com.carebeforecare.prehospitalcare_platform.repository;

import com.carebeforecare.prehospitalcare_platform.model.entity.FirstAidGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstAidGuideRepository extends JpaRepository<FirstAidGuide, Long> {

    List<FirstAidGuide> findByCategory(String category);

    @Query("SELECT f FROM FirstAidGuide f WHERE LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FirstAidGuide> searchFirstAidGuides(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT f.category FROM FirstAidGuide f")
    List<String> findDistinctCategories();

    List<FirstAidGuide> findByCategoryAndIdNot(String category, Long id);
}