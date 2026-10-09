package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.BloodRequest;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findByStatusOrderByCreatedAtDesc(String status);

    List<BloodRequest> findByBloodGroupAndLocationContainingIgnoreCaseAndStatus(
            String bloodGroup, String location, String status);
}
