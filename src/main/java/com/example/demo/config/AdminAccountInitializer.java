package com.example.demo.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminAccountInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() && adminPassword.isBlank()) {
            demoteUnconfiguredAdmins();
            return;
        }
        if (adminEmail.isBlank() || adminPassword.length() < 12) {
            throw new IllegalStateException("Set ADMIN_EMAIL and an ADMIN_PASSWORD of at least 12 characters");
        }

        demoteUnconfiguredAdmins();
        User admin = userRepository.findByEmail(adminEmail)
                .orElseGet(() -> new User("BloodConnect Administrator", adminEmail, adminPassword, "ADMIN"));
        admin.setEmail(adminEmail);
        admin.setRole("ADMIN");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        userRepository.save(admin);
    }

    private void demoteUnconfiguredAdmins() {
        userRepository.findAll().stream()
                .filter(user -> "ADMIN".equals(user.getRole()))
                .filter(user -> !user.getEmail().equalsIgnoreCase(adminEmail))
                .forEach(user -> {
                    user.setRole("PATIENT");
                    userRepository.save(user);
                });
    }
}