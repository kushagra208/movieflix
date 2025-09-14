package com.kushagra.movieflix.controller;

import com.kushagra.movieflix.dto.CustomResponse;
import com.kushagra.movieflix.entity.User;
import com.kushagra.movieflix.service.AuthService;
import com.kushagra.movieflix.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtService jwtService; // needed to extract username from token

    @PostMapping("/signup")
    public ResponseEntity<CustomResponse> signup(@RequestBody User user) {
        log.info("Entered into function signup in AuthController");
        return ResponseEntity.ok(authService.signup(user));
    }

    @PostMapping("/login")
    public ResponseEntity<CustomResponse> login(@RequestBody Map<String, String> loginData) {
        log.info("Entered into function login in AuthController");
        return ResponseEntity.ok(authService.login(loginData.get("username"), loginData.get("password")));
    }

    @PostMapping("/logout")
    public ResponseEntity<CustomResponse> logout() {
        log.info("Entered into function logout in AuthController");
        return ResponseEntity.ok(authService.logout());
    }
}

