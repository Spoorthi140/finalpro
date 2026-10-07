package com.smarturban.backend.repository;

import com.smarturban.backend.entity.ComplaintFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComplaintFeedbackRepository extends JpaRepository<ComplaintFeedback, Long> {
    Optional<ComplaintFeedback> findByComplaintId(Long complaintId);
    boolean existsByComplaintId(Long complaintId);
}
