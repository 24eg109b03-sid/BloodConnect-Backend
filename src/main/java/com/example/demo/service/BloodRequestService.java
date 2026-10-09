package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.BloodRequest;
import com.example.demo.repository.BloodRequestRepository;

@Service
public class BloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;

    public BloodRequestService(BloodRequestRepository bloodRequestRepository) {
        this.bloodRequestRepository = bloodRequestRepository;
    }

    public BloodRequest create(BloodRequest request) {
        request.setStatus("OPEN");
        request.setCreatedAt(LocalDateTime.now());
        return bloodRequestRepository.save(request);
    }

    public List<BloodRequest> openRequests() {
        return bloodRequestRepository.findByStatusOrderByCreatedAtDesc("OPEN");
    }

    public BloodRequest findById(Long id) {
        return bloodRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blood request not found"));
    }

    public List<BloodRequest> search(String bloodGroup, String location) {
        return bloodRequestRepository
                .findByBloodGroupAndLocationContainingIgnoreCaseAndStatus(bloodGroup, location, "OPEN");
    }

    public BloodRequest updateStatus(Long id, String status) {
        BloodRequest request = findById(id);
        request.setStatus(status.toUpperCase());
        return bloodRequestRepository.save(request);
    }

    public BloodRequest update(Long id, BloodRequest changes) {
        BloodRequest request = findById(id);
        request.setBloodGroup(changes.getBloodGroup());
        request.setQuantity(changes.getQuantity());
        request.setUrgency(changes.getUrgency());
        request.setLocation(changes.getLocation());
        return bloodRequestRepository.save(request);
    }

    public void delete(Long id) {
        BloodRequest request = findById(id);
        bloodRequestRepository.delete(request);
    }
}
