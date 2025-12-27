package com.example.saas.controller;

import com.example.saas.model.Project;
import com.example.saas.model.Task;
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
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ProjectController(ProjectRepository projectRepository, TenantRepository tenantRepository,
                           UserRepository userRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Create a new project
     * @param request Project creation request containing name, description, owner
     * @param tenantId Tenant ID from JWT token
     * @param userId User ID from JWT token
     * @param role User role from JWT token
     * @return Created project details or error response
     */
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody CreateProjectRequest request,
                                          @RequestAttribute("tenantId") String tenantId,
                                          @RequestAttribute("userId") String userId,
                                          @RequestAttribute("role") String role) {
        String targetTenantId = tenantId;
        if ("SUPER_ADMIN".equals(role) && request.tenantId != null && !request.tenantId.isBlank()) {
            targetTenantId = request.tenantId;
        }

        if (!"SUPER_ADMIN".equals(role) && !"TENANT_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        if (targetTenantId == null || targetTenantId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Tenant id is required"));
        }

        Optional<Tenant> tenantOpt = tenantRepository.findById(targetTenantId);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tenant tenant = tenantOpt.get();

        // Check project limit
        long projectCount = projectRepository.countByTenantId(targetTenantId);
        if (projectCount >= tenant.getMaxProjects()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Project limit reached"));
        }

        Optional<User> userOpt = userRepository.findById(userId);
        Project project = new Project();
        project.setTenant(tenant);
        project.setName(request.name);
        project.setDescription(request.description);
        project.setStatus(Project.ProjectStatus.ACTIVE);
        project.setCreatedBy(userOpt.orElse(null));

        Project saved = projectRepository.save(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Project created", buildProjectResponse(saved)));
    }

    // List projects
    @GetMapping
    public ResponseEntity<?> listProjects(@RequestAttribute("tenantId") String tenantId,
                                         @RequestAttribute("role") String role,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int limit) {
        Pageable pageable = PageRequest.of(page - 1, Math.min(limit, 100));
        Page<Project> projects = "SUPER_ADMIN".equals(role)
                ? projectRepository.findAll(pageable)
                : projectRepository.findByTenantId(tenantId, pageable);

        List<Map<String, Object>> projectList = projects.getContent().stream()
                .map(this::buildProjectResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(projectList));
    }

    // Get project
    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProject(@PathVariable String projectId,
                                       @RequestAttribute("tenantId") String tenantId,
                                       @RequestAttribute("role") String role) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Project project = projectOpt.get();
        if (!"SUPER_ADMIN".equals(role) && !project.getTenant().getId().equals(tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        return ResponseEntity.ok(ApiResponse.ok(buildProjectResponse(project)));
    }

    // Update project
    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(@PathVariable String projectId,
                                          @RequestBody UpdateProjectRequest request,
                                          @RequestAttribute("tenantId") String tenantId,
                                          @RequestAttribute("role") String role) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Project project = projectOpt.get();
        // Authorization: only tenant_admin and super_admin can update projects
        if (!"SUPER_ADMIN".equals(role)
                && !("TENANT_ADMIN".equals(role) && project.getTenant().getId().equals(tenantId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        if (request.name != null) project.setName(request.name);
        if (request.description != null) project.setDescription(request.description);
        if (request.status != null) project.setStatus(Project.ProjectStatus.valueOf(request.status));

        Project updated = projectRepository.save(project);
        return ResponseEntity.ok(ApiResponse.ok("Project updated", buildProjectResponse(updated)));
    }

    // Delete project
    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable String projectId,
                                          @RequestAttribute("tenantId") String tenantId,
                                          @RequestAttribute("role") String role) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Project project = projectOpt.get();
        // Authorization: only tenant_admin and super_admin can delete projects
        if (!"SUPER_ADMIN".equals(role)
                && !("TENANT_ADMIN".equals(role) && project.getTenant().getId().equals(tenantId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        projectRepository.delete(project);
        return ResponseEntity.ok(ApiResponse.ok("Project deleted", null));
    }

    private Map<String, Object> buildProjectResponse(Project project) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", project.getId());
        data.put("tenantId", project.getTenant().getId());
        data.put("name", project.getName());
        data.put("description", project.getDescription());
        data.put("status", project.getStatus());

        if (project.getCreatedBy() != null) {
            Map<String, Object> creator = new HashMap<>();
            creator.put("id", project.getCreatedBy().getId());
            creator.put("fullName", project.getCreatedBy().getFullName());
            data.put("createdBy", creator);
        }

        data.put("taskCount", taskRepository.countByProjectId(project.getId()));
        data.put("createdAt", project.getCreatedAt());

        return data;
    }

    @Data
    @NoArgsConstructor
    public static class CreateProjectRequest {
        private String name;
        private String description;
        private String tenantId; // used only by super admin when creating on behalf of a tenant
    }

    @Data
    @NoArgsConstructor
    public static class UpdateProjectRequest {
        private String name;
        private String description;
        private String status;
    }
}
