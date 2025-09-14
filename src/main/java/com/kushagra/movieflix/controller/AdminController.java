package com.kushagra.movieflix.controller;

import com.kushagra.movieflix.dto.CustomResponse;
import com.kushagra.movieflix.entity.Movie;
import com.kushagra.movieflix.entity.User;
import com.kushagra.movieflix.service.AdminService;
import com.kushagra.movieflix.service.AuthService;
import com.kushagra.movieflix.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;


    @PostMapping("/movies/create")
    public ResponseEntity<CustomResponse> addMovie(@RequestBody Movie request) {
        return ResponseEntity.ok(adminService.addMovie(request.getExternalId()));
    }

    @PostMapping("/movies/delete")
    public ResponseEntity<CustomResponse> deleteMovie(@RequestBody Movie request) {
        return ResponseEntity.ok(adminService.deleteMovie(request.getExternalId()));
    }

    @GetMapping("/users")
    public ResponseEntity<CustomResponse> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<CustomResponse> createUser(@RequestBody User user) {
        return ResponseEntity.ok(adminService.createUser(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<CustomResponse> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.deleteUser(id));
    }
}

