package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Donation;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByDonorEmailOrderByDonationDateDesc(String donorEmail);
}
