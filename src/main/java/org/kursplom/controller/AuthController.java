package org.kursplom.controller;

import org.kursplom.dto.ApiResponse;
import org.kursplom.dto.SignUpRequest;
import org.kursplom.model.User;
import org.kursplom.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Базовый путь для аутентификации
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup") // Эндпоинт для регистрации (POST запрос)
    public ResponseEntity<?> registerUser(@RequestBody /* @Valid */ SignUpRequest signUpRequest) {
        try {
            User registeredUser = authService.registerNewUser(signUpRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "User registered successfully!"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "An unexpected error occurred during registration."));
        }
    }

}