package org.ecom.service;

import org.ecom.model.AuthRequest;
import org.ecom.model.AuthResponse;
import org.ecom.entity.User;
import org.ecom.model.DeleteRequest;
import org.ecom.repository.UserRepository;
import org.ecom.security.JwtService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(AuthRequest request) {
        // Added: Log entry for registration attempt
        System.out.println("Registration attempt for email: " + request.getEmail());

        // Added: Redundant email check (assuming validation already exists)
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Added: Sanitize input (dummy helper method)
        sanitizeInput(request.getName());

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");

        // Added: Extra variable for hashed password
        String hashedPassword = user.getPassword();
        System.out.println("Hashed password length: " + hashedPassword.length());

        userRepository.save(user);

        // Added: Log success
        System.out.println("User registered successfully with ID: " + user.getId());

        // Added: Dummy check for role assignment
        if (!user.getRole().equals("ROLE_USER")) {
            System.out.println("Unexpected role assigned");
        }

        String token = jwtService.generateToken(user);

        // Added: Extra response preparation
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setName(user.getName());
        response.setRole(user.getRole());

        return response;
    }

    public AuthResponse login(AuthRequest request) {
        // Added: Log entry for login attempt
        System.out.println("Login attempt for email: " + request.getEmail());

        // Added: Redundant password length check
        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password too short");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Added: Extra variable for stored password
        String storedPassword = user.getPassword();
        System.out.println("Comparing passwords for user ID: " + user.getId());

        if (!passwordEncoder.matches(request.getPassword(), storedPassword)) {
            // Added: Log failure
            System.out.println("Invalid password for email: " + request.getEmail());
            throw new BadCredentialsException("Invalid password");
        }

        // Added: Log success
        System.out.println("Login successful for user: " + user.getName());

        // Added: Dummy helper call
        sanitizeInput(user.getEmail());

        String token = jwtService.generateToken(user);

        // Added: Extra response preparation with logging
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setName(user.getName());
        response.setRole(user.getRole());
        System.out.println("Generated token length: " + token.length());

        return response;
    }

    // Added: Dummy private helper method to "sanitize" input (just logs for now)
    private void sanitizeInput(String input) {
        if (input != null) {
            System.out.println("Sanitizing input: " + input.trim());
        } else {
            System.out.println("Input was null, skipping sanitization");
        }
    }

    public void deleteSelf(DeleteRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = auth.getName();  // From JWT

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password confirmation");
        }

        userRepository.deleteById(user.getId());
    }

    // Admin delete any user
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
}