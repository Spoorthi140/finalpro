package com.smarturban.backend.service;

import com.smarturban.backend.entity.Complaint;
import com.smarturban.backend.entity.ComplaintFeedback;
import com.smarturban.backend.entity.User;
import com.smarturban.backend.repository.ComplaintFeedbackRepository;
import com.smarturban.backend.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FeedbackService {

    @Autowired
    private ComplaintFeedbackRepository feedbackRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Transactional
    public ComplaintFeedback submitFeedback(User citizen, Long complaintId, Integer rating, String comments) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + complaintId));

        if (!complaint.getUser().getId().equals(citizen.getId())) {
            throw new RuntimeException("Unauthorized: You can only submit feedback for your own complaints.");
        }

        if (!"Resolved".equalsIgnoreCase(complaint.getStatus())) {
            throw new RuntimeException("Feedback can only be submitted after the complaint is resolved.");
        }

        if (feedbackRepository.existsByComplaintId(complaintId)) {
            throw new RuntimeException("Feedback has already been submitted for this complaint.");
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be an integer between 1 and 5 stars.");
        }

        ComplaintFeedback feedback = new ComplaintFeedback(complaint, citizen, rating, comments);
        return feedbackRepository.save(feedback);
    }

    public Optional<ComplaintFeedback> getFeedbackForComplaint(Long complaintId) {
        return feedbackRepository.findByComplaintId(complaintId);
    }

    public java.util.List<ComplaintFeedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }
}
