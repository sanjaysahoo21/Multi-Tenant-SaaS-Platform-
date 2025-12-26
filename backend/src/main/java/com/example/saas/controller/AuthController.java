package com.example.saas.controller;

import com.example.saas.model.Tenant;
import com.example.saas.model.User;
import com.example.saas.repository.AuditLogRepository;
import com.example.saas.repository.TenantRepository;
import com.example.saas.repository.UserRepository;
import com.example.saas.util.ApiResponse;
import com.example.saas.util.JwtUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(TenantRepository tenantRepository, UserRepository userRepository,
                         AuditLogRepository auditLogRepository, JwtUtil jwtUtil,
                         PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // Register Tenant
    @PostMapping("/register-tenant")
    public ResponseEntity<?> registerTenant(@RequestBody RegisterTenantRequest request) {
        // Validate input
        if (request.tenantName == null || request.tenantName.isBlank() ||
            request.subdomain == null || request.subdomain.isBlank() ||
            request.adminEmail == null || request.adminEmail.isBlank() ||
            request.adminPassword == null || request.adminPassword.length() < 8 ||
            request.adminFullName == null || request.adminFullName.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid input"));
        }

        // Check if subdomain exists
        if (tenantRepository.findBySubdomain(request.subdomain).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Subdomain already exists"));
        }

        // Check if email exists
        if (userRepository.findByEmail(request.adminEmail).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Email already exists"));
        }

        try {
            // Create tenant
            Tenant tenant = new Tenant();
            tenant.setName(request.tenantName);
            tenant.setSubdomain(request.subdomain);
            tenant.setStatus(Tenant.TenantStatus.ACTIVE);
            tenant.setSubscriptionPlan(Tenant.SubscriptionPlan.FREE);
            tenant.setMaxUsers(5);
            tenant.setMaxProjects(3);
            Tenant savedTenant = tenantRepository.save(tenant);

            // Create admin user
            User adminUser = new User();
            adminUser.setTenant(savedTenant);
            adminUser.setEmail(request.adminEmail);
            adminUser.setPasswordHash(passwordEncoder.encode(request.adminPassword));
            adminUser.setFullName(request.adminFullName);
            adminUser.setRole(User.UserRole.TENANT_ADMIN);
            adminUser.setIsActive(true);
            User savedUser = userRepository.save(adminUser);

            // Response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("tenantId", savedTenant.getId());
            responseData.put("subdomain", savedTenant.getSubdomain());
            Map<String, Object> adminData = new HashMap<>();
            adminData.put("id", savedUser.getId());
            adminData.put("email", savedUser.getEmail());
            adminData.put("fullName", savedUser.getFullName());
            adminData.put("role", savedUser.getRole());
            responseData.put("adminUser", adminData);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Tenant registered successfully", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.email == null || request.email.isBlank() ||
            request.password == null || request.password.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid input"));
        }

        try {
            // Get tenant
            Optional<Tenant> tenantOpt;
            if (request.tenantSubdomain != null && !request.tenantSubdomain.isBlank()) {
                tenantOpt = tenantRepository.findBySubdomain(request.tenantSubdomain);
            } else {
                // Allow login without subdomain; still enforce tenant status when present
                Optional<User> userOpt = userRepository.findByEmail(request.email);
                if (userOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid credentials"));
                }
                User user = userOpt.get();
                if (!passwordEncoder.matches(request.password, user.getPasswordHash())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid credentials"));
                }
                if (!user.getIsActive()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Account is inactive"));
                }
                if (user.getTenant() != null && user.getTenant().getStatus() != Tenant.TenantStatus.ACTIVE) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Tenant is not active"));
                }

                String token = jwtUtil.generateToken(user.getId(), user.getTenant() != null ? user.getTenant().getId() : null, user.getRole().toString());
                return ResponseEntity.ok(buildLoginResponse(user, token));
            }

            if (tenantOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Tenant not found"));
            }

            Tenant tenant = tenantOpt.get();
            if (tenant.getStatus() != Tenant.TenantStatus.ACTIVE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Tenant is not active"));
            }

            // Get user
            Optional<User> userOpt = userRepository.findByEmailAndTenantId(request.email, tenant.getId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid credentials"));
            }

            User user = userOpt.get();
            if (!passwordEncoder.matches(request.password, user.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid credentials"));
            }

            if (!user.getIsActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Account is inactive"));
            }

            String token = jwtUtil.generateToken(user.getId(), tenant.getId(), user.getRole().toString());
            return ResponseEntity.ok(buildLoginResponse(user, token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    // Get current user
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestAttribute("userId") String userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("fullName", user.getFullName());
            userData.put("role", user.getRole());
            userData.put("isActive", user.getIsActive());

            if (user.getTenant() != null) {
                Map<String, Object> tenantData = new HashMap<>();
                tenantData.put("id", user.getTenant().getId());
                tenantData.put("name", user.getTenant().getName());
                tenantData.put("subdomain", user.getTenant().getSubdomain());
                tenantData.put("subscriptionPlan", user.getTenant().getSubscriptionPlan());
                tenantData.put("maxUsers", user.getTenant().getMaxUsers());
                tenantData.put("maxProjects", user.getTenant().getMaxProjects());
                userData.put("tenant", tenantData);
            }

            return ResponseEntity.ok(ApiResponse.ok(userData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get user: " + e.getMessage()));
        }
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestAttribute("userId") String userId) {
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    // Helper method
    private ApiResponse<?> buildLoginResponse(User user, String token) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("fullName", user.getFullName());
        userData.put("role", user.getRole());
        if (user.getTenant() != null) {
            userData.put("tenantId", user.getTenant().getId());
            Map<String, Object> tenantData = new HashMap<>();
            tenantData.put("id", user.getTenant().getId());
            tenantData.put("name", user.getTenant().getName());
            tenantData.put("subdomain", user.getTenant().getSubdomain());
            tenantData.put("status", user.getTenant().getStatus());
            tenantData.put("subscriptionPlan", user.getTenant().getSubscriptionPlan());
            tenantData.put("maxUsers", user.getTenant().getMaxUsers());
            tenantData.put("maxProjects", user.getTenant().getMaxProjects());
            userData.put("tenant", tenantData);
        }
        response.put("user", userData);
        response.put("token", token);
        response.put("expiresIn", 86400);
        return ApiResponse.ok(response);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterTenantRequest {
        private String tenantName;
        private String subdomain;
        private String adminEmail;
        private String adminPassword;
        private String adminFullName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String email;
        private String password;
        private String tenantSubdomain;
    }
}
