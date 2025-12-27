package com.example.saas.controller;

import com.example.saas.model.Project;
import com.example.saas.model.Tenant;
import com.example.saas.model.User;
import com.example.saas.repository.ProjectRepository;
import com.example.saas.repository.TaskRepository;
import com.example.saas.repository.TenantRepository;
import com.example.saas.repository.UserRepository;
import com.example.saas.util.ApiResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public TenantController(TenantRepository tenantRepository, UserRepository userRepository,
                           ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    // Get tenant details
    @GetMapping("/{tenantId}")
    public ResponseEntity<?> getTenant(@PathVariable String tenantId,
                                      @RequestAttribute("tenantId") String userTenantId,
                                      @RequestAttribute("role") String role) {
        if (!role.equals("SUPER_ADMIN") && !tenantId.equals(userTenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        Optional<Tenant> tenantOpt = tenantRepository.findById(tenantId);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tenant tenant = tenantOpt.get();
        Map<String, Object> data = buildTenantResponse(tenant);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // Update tenant
    @PutMapping("/{tenantId}")
    public ResponseEntity<?> updateTenant(@PathVariable String tenantId,
                                         @RequestBody UpdateTenantRequest request,
                                         @RequestAttribute("tenantId") String userTenantId,
                                         @RequestAttribute("role") String role) {
        if (!role.equals("SUPER_ADMIN") && !tenantId.equals(userTenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        Optional<Tenant> tenantOpt = tenantRepository.findById(tenantId);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tenant tenant = tenantOpt.get();
        if (request.name != null) tenant.setName(request.name);
        if (request.status != null && role.equals("SUPER_ADMIN")) tenant.setStatus(Tenant.TenantStatus.valueOf(request.status));
        if (request.subscriptionPlan != null && role.equals("SUPER_ADMIN")) {
            tenant.setSubscriptionPlan(Tenant.SubscriptionPlan.valueOf(request.subscriptionPlan));
            updatePlanLimits(tenant);
        }

        Tenant updated = tenantRepository.save(tenant);
        return ResponseEntity.ok(ApiResponse.ok("Tenant updated", buildTenantResponse(updated)));
    }

    // List all tenants (super admin only)
    @GetMapping
    public ResponseEntity<?> listTenants(@RequestAttribute("role") String role,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int limit) {
        if (!role.equals("SUPER_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        Pageable pageable = PageRequest.of(page - 1, Math.min(limit, 100));
        Page<Tenant> tenants = tenantRepository.findAll(pageable);

        List<Map<String, Object>> tenantList = tenants.getContent().stream()
                .map(this::buildTenantResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("tenants", tenantList);
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page);
        pagination.put("totalPages", tenants.getTotalPages());
        pagination.put("totalTenants", tenants.getTotalElements());
        pagination.put("limit", limit);
        response.put("pagination", pagination);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // Add user to tenant
    @PostMapping("/{tenantId}/users")
    public ResponseEntity<?> addUser(@PathVariable String tenantId,
                                    @RequestBody AddUserRequest request,
                                    @RequestAttribute("tenantId") String userTenantId,
                                    @RequestAttribute("role") String role) {
        if (!(role.equals("TENANT_ADMIN") && tenantId.equals(userTenantId)) && !role.equals("SUPER_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        Optional<Tenant> tenantOpt = tenantRepository.findById(tenantId);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tenant tenant = tenantOpt.get();

        // Check user limit
        long userCount = userRepository.countByTenantId(tenantId);
        if (userCount >= tenant.getMaxUsers()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("User limit reached"));
        }

        // Check email exists
        if (userRepository.findByEmailAndTenantId(request.email, tenantId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Email already exists"));
        }

        User user = new User();
        user.setTenant(tenant);
        user.setEmail(request.email);
        user.setPasswordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(request.password));
        user.setFullName(request.fullName);
        user.setRole(User.UserRole.valueOf(request.role != null ? request.role : "USER"));
        user.setIsActive(true);

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("User created", buildUserResponse(saved)));
    }

    // List tenant users
    @GetMapping("/{tenantId}/users")
    public ResponseEntity<?> listUsers(@PathVariable String tenantId,
                                      @RequestAttribute("tenantId") String userTenantId,
                                      @RequestAttribute("role") String role,
                                      @RequestParam(required = false) String search,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "50") int limit) {
        if (!role.equals("SUPER_ADMIN") && !tenantId.equals(userTenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        Pageable pageable = PageRequest.of(page - 1, Math.min(limit, 100));
        Page<User> users;

        if (search != null && !search.isBlank()) {
            users = userRepository.findByTenantIdAndFullNameIgnoreCaseContainingOrTenantIdAndEmailIgnoreCaseContaining(
                    tenantId, search, tenantId, search, pageable);
        } else {
            users = userRepository.findByTenantId(tenantId, pageable);
        }

        List<Map<String, Object>> userList = users.getContent().stream()
                .map(this::buildUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(userList));
    }

    private Map<String, Object> buildTenantResponse(Tenant tenant) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", tenant.getId());
        data.put("name", tenant.getName());
        data.put("subdomain", tenant.getSubdomain());
        data.put("status", tenant.getStatus());
        data.put("subscriptionPlan", tenant.getSubscriptionPlan());
        data.put("maxUsers", tenant.getMaxUsers());
        data.put("maxProjects", tenant.getMaxProjects());
        data.put("createdAt", tenant.getCreatedAt());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.countByTenantId(tenant.getId()));
        stats.put("totalProjects", projectRepository.countByTenantId(tenant.getId()));
        data.put("stats", stats);

        return data;
    }

    private Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("email", user.getEmail());
        data.put("fullName", user.getFullName());
        data.put("role", user.getRole());
        data.put("isActive", user.getIsActive());
        data.put("createdAt", user.getCreatedAt());
        return data;
    }

    private void updatePlanLimits(Tenant tenant) {
        switch (tenant.getSubscriptionPlan()) {
            case PRO:
                tenant.setMaxUsers(25);
                tenant.setMaxProjects(15);
                break;
            case ENTERPRISE:
                tenant.setMaxUsers(100);
                tenant.setMaxProjects(50);
                break;
            default:
                tenant.setMaxUsers(5);
                tenant.setMaxProjects(3);
        }
    }

    @Data
    @NoArgsConstructor
    public static class UpdateTenantRequest {
        private String name;
        private String status;
        private String subscriptionPlan;
    }

    @Data
    @NoArgsConstructor
    public static class AddUserRequest {
        private String email;
        private String password;
        private String fullName;
        private String role;
    }
}
