package com.cravingapp.craving.controller;


import jakarta.persistence.Column;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    public record LoginRequest(String username, String password) {}
    public record RegisterRequest(
            String username,
            String email,
            String password // Parola în text simplu
    ) {}
    public record UserResponse(
            Integer userId,
            String username,
            String email,
            String bio
    ) {}
}
