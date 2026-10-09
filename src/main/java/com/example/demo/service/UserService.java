package com.example.demo.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User registerUser(User user) {
        String email = user.getEmail().trim().toLowerCase(Locale.ROOT);

        Optional<User> existingUser =
            userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        String role = user.getRole() == null ? "PATIENT" : user.getRole().trim().toUpperCase(Locale.ROOT);
        if (!List.of("DONOR", "PATIENT", "HOSPITAL").contains(role)) {
            throw new RuntimeException("Invalid account role");
        }
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {

        Optional<User> user =
                userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT));

        if (user.isPresent()) {

            User existingUser = user.get();

                if (passwordEncoder.matches(password, existingUser.getPassword())
                    || existingUser.getPassword().equals(password)) {
                return existingUser;
            }
        }

        throw new RuntimeException("Invalid email or password");
    }

    public LoginResult loginWithToken(String email, String password) {
        User user = loginUser(email, password);
        return new LoginResult(user, jwtService.createToken(user));
    }

    public record LoginResult(User user, String token) {
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, UserUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("User details are required");
        }
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if ("ADMIN".equals(existingUser.getRole())) {
            throw new RuntimeException("The configured administrator account cannot be edited here");
        }

        String name = trim(request.name());
        String email = trim(request.email()).toLowerCase(Locale.ROOT);
        String role = trim(request.role()).toUpperCase(Locale.ROOT);
        if (name.isBlank() || email.isBlank() || role.isBlank()) {
            throw new RuntimeException("Name, email, and role are required");
        }
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.getId().equals(id)) {
                throw new RuntimeException("Email already registered");
            }
        });
        if (!List.of("DONOR", "PATIENT", "HOSPITAL").contains(role)) {
            throw new RuntimeException("Invalid account role");
        }

        existingUser.setName(name);
        existingUser.setEmail(email);
        existingUser.setPhone(trim(request.phone()));
        existingUser.setBloodGroup(trim(request.bloodGroup()));
        existingUser.setLocation(trim(request.location()));
        existingUser.setRole(role);
        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("The configured administrator account cannot be deleted here");
        }
        userRepository.delete(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}