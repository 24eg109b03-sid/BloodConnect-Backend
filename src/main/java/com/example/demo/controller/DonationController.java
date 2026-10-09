package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Donation;
import com.example.demo.repository.DonationRepository;

@RestController
@RequestMapping("/donations")
@CrossOrigin(origins = "*")
public class DonationController {

    private final DonationRepository donationRepository;

    public DonationController(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    @GetMapping
    public List<Donation> history(@RequestParam String donorEmail) {
        return donationRepository.findByDonorEmailOrderByDonationDateDesc(donorEmail);
    }

    @PostMapping
    public Donation record(@RequestBody Donation donation) {
        if (donation.getDonationDate() == null) donation.setDonationDate(LocalDate.now());
        if (donation.getStatus() == null) donation.setStatus("COMPLETED");
        return donationRepository.save(donation);
    }
}
