package com.example.saas.controller;

import com.example.saas.model.User;
import com.example.saas.repository.UserRepository;
import com.example.saas.util.ApiResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // List users (super admin only, optional tenant filter)
    @GetMapping
    public ResponseEntity<?> listUsers(@RequestAttribute("role") String role,
                                       @RequestParam(required = false) String tenantId,
                                       @RequestParam(required = false) String search,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "50") int limit) {
        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        List<User> users;
        if (tenantId != null && !tenantId.isBlank()) {
            if (search != null && !search.isBlank()) {
                users = userRepository
                        .findByTenantIdAndFullNameIgnoreCaseContainingOrTenantIdAndEmailIgnoreCaseContaining(
                                tenantId, search, tenantId, search, PageRequest.of(page - 1, Math.min(limit, 100)))
                        .getContent();
            } else {
                users = userRepository.findByTenantId(tenantId, PageRequest.of(page - 1, Math.min(limit, 100))).getContent();
            }
        } else {
            // No tenant filter: search across all users
            List<User> pageContent = userRepository.findAll(PageRequest.of(page - 1, Math.min(limit, 100))).getContent();
            if (search != null && !search.isBlank()) {
                users = pageContent.stream()
                        .filter(u -> u.getFullName().toLowerCase().contains(search.toLowerCase())
                                || u.getEmail().toLowerCase().contains(search.toLowerCase()))
                        .toList();
            } else {
                users = pageContent;
            }
        }

        List<Map<String, Object>> payload = users.stream()
                .map(this::buildUserResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(payload));
    }

    // Get user profile
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId,
                                    @RequestAttribute("tenantId") String tenantId,
                                    @RequestAttribute("userId") String currentUserId,
                                    @RequestAttribute("role") String currentRole) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        // Allow user to view own profile or tenant admin/super_admin to view any user in tenant
        if (!currentUserId.equals(userId) && !currentRole.equals("SUPER_ADMIN") &&
            !(currentRole.equals("TENANT_ADMIN") && user.getTenant().getId().equals(tenantId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        if (!currentRole.equals("SUPER_ADMIN") && user.getTenant() != null && !user.getTenant().getId().equals(tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        return ResponseEntity.ok(ApiResponse.ok(buildUserResponse(user)));
    }

    // Update user profile
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId,
                                       @RequestBody UpdateUserRequest request,
                                       @RequestAttribute("tenantId") String tenantId,
                                       @RequestAttribute("userId") String currentUserId,
                                       @RequestAttribute("role") String currentRole) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        // Authorization: user can update own profile, tenant_admin can update any user in tenant,
        // super_admin can update anyone
        if (!currentUserId.equals(userId) && !currentRole.equals("SUPER_ADMIN") &&
            !(currentRole.equals("TENANT_ADMIN") && user.getTenant().getId().equals(tenantId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        if (!currentRole.equals("SUPER_ADMIN") && user.getTenant() != null && !user.getTenant().getId().equals(tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        // Regular users can only update their own display name
        if (currentRole.equals("USER")) {
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
            }
            if (request.fullName != null) {
                user.setFullName(request.fullName);
            }
            User updated = userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.ok("User updated", buildUserResponse(updated)));
        }

        // Admins and super admins can update broader fields
        if (request.fullName != null) user.setFullName(request.fullName);
        if (request.email != null) {
            // Check email uniqueness within tenant
            String targetTenantId = user.getTenant() != null ? user.getTenant().getId() : tenantId;
            Optional<User> existingUser = userRepository.findByEmailAndTenantId(request.email, targetTenantId);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Email already in use"));
            }
            user.setEmail(request.email);
        }
        if (request.password != null) {
            if (request.password.length() < 8) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Password must be at least 8 characters"));
            }
            user.setPasswordHash(passwordEncoder.encode(request.password));
        }
        if (request.isActive != null && currentRole.equals("SUPER_ADMIN")) {
            user.setIsActive(request.isActive);
        }

        User updated = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("User updated", buildUserResponse(updated)));
    }

    // Delete user
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId,
                                       @RequestAttribute("tenantId") String tenantId,
                                       @RequestAttribute("userId") String currentUserId,
                                       @RequestAttribute("role") String role) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        // Authorization: only tenant_admin and super_admin can delete users
        if (!role.equals("SUPER_ADMIN") && !(role.equals("TENANT_ADMIN") && user.getTenant().getId().equals(tenantId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        // Tenant admin cannot delete themselves
        if (role.equals("TENANT_ADMIN") && user.getId().equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Cannot delete own account"));
        }

        if (!role.equals("SUPER_ADMIN") && user.getTenant() != null && !user.getTenant().getId().equals(tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        userRepository.delete(user);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }

    private Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("email", user.getEmail());
        data.put("fullName", user.getFullName());
        data.put("role", user.getRole());
        data.put("isActive", user.getIsActive());
        if (user.getTenant() != null) {
            data.put("tenantId", user.getTenant().getId());
        }
        data.put("createdAt", user.getCreatedAt());
        data.put("updatedAt", user.getUpdatedAt());
        return data;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateUserRequest {
        private String email;
        private String fullName;
        private String password;
        private Boolean isActive;
    }
}
