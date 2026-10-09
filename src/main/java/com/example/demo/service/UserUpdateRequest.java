package com.example.demo.service;

public record UserUpdateRequest(String name, String email, String phone, String bloodGroup,
        String location, String role) {
}