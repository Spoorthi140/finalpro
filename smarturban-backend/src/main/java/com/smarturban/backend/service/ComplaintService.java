package com.smarturban.backend.service;

import com.smarturban.backend.dto.ComplaintRequest;
import com.smarturban.backend.entity.*;
import com.smarturban.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintStatusHistoryRepository historyRepository;

    @Transactional
    public Complaint createComplaint(User user, ComplaintRequest request, String imageUrl) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));

        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setCategory(category);
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());
        complaint.setLocationName(request.getLocationName());
        complaint.setImageUrl(imageUrl);
        complaint.setStatus("Submitted");

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Record initial status history
        ComplaintStatusHistory history = new ComplaintStatusHistory(
                savedComplaint,
                null,
                "Submitted",
                user.getFullName(),
                "Complaint registered successfully."
        );
        historyRepository.save(history);

        return savedComplaint;
    }

    public List<Complaint> getComplaintsByUser(Long userId) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }

    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + id));
    }

    @Transactional
    public Complaint updateComplaintStatusAndDepartment(Long complaintId, String newStatus, Long departmentId, String changedBy, String remarks) {
        Complaint complaint = getComplaintById(complaintId);
        String oldStatus = complaint.getStatus();

        if (newStatus != null && !newStatus.trim().isEmpty()) {
            complaint.setStatus(newStatus);
        }

        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));
            complaint.setDepartment(department);
        }

        Complaint updatedComplaint = complaintRepository.save(complaint);

        // Log status history
        ComplaintStatusHistory history = new ComplaintStatusHistory(
                updatedComplaint,
                oldStatus,
                complaint.getStatus(),
                changedBy,
                remarks != null ? remarks : "Status updated by admin"
        );
        historyRepository.save(history);

        return updatedComplaint;
    }

    public List<ComplaintStatusHistory> getComplaintHistory(Long complaintId) {
        return historyRepository.findByComplaintIdOrderByChangedAtAsc(complaintId);
    }
}
