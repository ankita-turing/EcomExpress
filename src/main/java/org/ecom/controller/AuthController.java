package org.ecom.controller;

import jakarta.validation.Valid;
import org.ecom.model.AuthRequest;
import org.ecom.model.AuthResponse;
import org.ecom.model.DeleteRequest;
import org.ecom.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));

    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteSelf(@RequestBody DeleteRequest request) {
        authService.deleteSelf(request);
        return ResponseEntity.noContent().build();
    }

    // Admin delete
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        authService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
