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

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public TaskController(TaskRepository taskRepository, ProjectRepository projectRepository,
                        UserRepository userRepository, TenantRepository tenantRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
    }

    // Create task
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<?> createTask(@PathVariable String projectId,
                                       @RequestBody CreateTaskRequest request,
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

        // Only tenant admin or super admin can create tasks
        if (!"SUPER_ADMIN".equals(role) && !"TENANT_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        Task task = new Task();
        task.setProject(project);
        task.setTenant(project.getTenant());
        task.setTitle(request.title);
        task.setDescription(request.description);
        task.setStatus(Task.TaskStatus.TODO);
        task.setPriority(Task.TaskPriority.valueOf(request.priority != null ? request.priority : "MEDIUM"));

        if (request.assignedTo != null) {
            Optional<User> userOpt = userRepository.findById(request.assignedTo);
            String targetTenantId = project.getTenant().getId();
            if (userOpt.isPresent() && userOpt.get().getTenant().getId().equals(targetTenantId)) {
                task.setAssignedTo(userOpt.get());
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid assigned user"));
            }
        }

        if (request.dueDate != null) {
            task.setDueDate(LocalDate.parse(request.dueDate));
        }

        Task saved = taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Task created", buildTaskResponse(saved)));
    }

    // List project tasks
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<?> listTasks(@PathVariable String projectId,
                                      @RequestAttribute("tenantId") String tenantId,
                                      @RequestAttribute("role") String role,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "50") int limit) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Project project = projectOpt.get();
        if (!"SUPER_ADMIN".equals(role) && !project.getTenant().getId().equals(tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        Pageable pageable = PageRequest.of(page - 1, Math.min(limit, 100));
        Page<Task> tasks = "SUPER_ADMIN".equals(role)
            ? taskRepository.findByProjectId(projectId, pageable)
            : taskRepository.findByProjectIdAndTenantId(projectId, tenantId, pageable);

        List<Map<String, Object>> taskList = tasks.getContent().stream()
                .map(this::buildTaskResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("tasks", taskList);
        response.put("total", tasks.getTotalElements());
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page);
        pagination.put("totalPages", tasks.getTotalPages());
        pagination.put("limit", limit);
        response.put("pagination", pagination);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // Update task
    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable String taskId,
                                       @RequestBody UpdateTaskRequest request,
                                       @RequestAttribute("tenantId") String tenantId,
                                       @RequestAttribute("role") String role) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOpt.get();
        if (!"SUPER_ADMIN".equals(role) && !task.getTenant().getId().equals(tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        // Only admins can edit task details (non-status changes)
        if (!"SUPER_ADMIN".equals(role) && !"TENANT_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        if (request.title != null) task.setTitle(request.title);
        if (request.description != null) task.setDescription(request.description);
        if (request.status != null) task.setStatus(Task.TaskStatus.valueOf(request.status));
        if (request.priority != null) task.setPriority(Task.TaskPriority.valueOf(request.priority));
        if (request.dueDate != null) task.setDueDate(LocalDate.parse(request.dueDate));

        if (request.assignedTo != null) {
            Optional<User> userOpt = userRepository.findById(request.assignedTo);
            String targetTenantId = task.getTenant().getId();
            if (userOpt.isPresent() && userOpt.get().getTenant().getId().equals(targetTenantId)) {
                task.setAssignedTo(userOpt.get());
            }
        } else if (request.assignedTo == null && request.toString().contains("assignedTo")) {
            task.setAssignedTo(null);
        }

        Task updated = taskRepository.save(task);
        return ResponseEntity.ok(ApiResponse.ok("Task updated", buildTaskResponse(updated)));
    }

    // Update task status only
    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable String taskId,
                                             @RequestBody UpdateTaskStatusRequest request,
                                             @RequestAttribute("tenantId") String tenantId,
                                             @RequestAttribute("role") String role) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOpt.get();
        if (!"SUPER_ADMIN".equals(role) && !task.getTenant().getId().equals(tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        task.setStatus(Task.TaskStatus.valueOf(request.status));
        Task updated = taskRepository.save(task);
        return ResponseEntity.ok(ApiResponse.ok(buildTaskResponse(updated)));
    }

    // Delete task
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable String taskId,
                                       @RequestAttribute("tenantId") String tenantId,
                                       @RequestAttribute("role") String role) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOpt.get();
        if (!"SUPER_ADMIN".equals(role) && !task.getTenant().getId().equals(tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        // Only admins can delete tasks
        if (!"SUPER_ADMIN".equals(role) && !"TENANT_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        taskRepository.delete(task);
        return ResponseEntity.ok(ApiResponse.ok("Task deleted", null));
    }

    private Map<String, Object> buildTaskResponse(Task task) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", task.getId());
        data.put("projectId", task.getProject().getId());
        data.put("title", task.getTitle());
        data.put("description", task.getDescription());
        data.put("status", task.getStatus());
        data.put("priority", task.getPriority());
        data.put("dueDate", task.getDueDate());

        if (task.getAssignedTo() != null) {
            Map<String, Object> assignee = new HashMap<>();
            assignee.put("id", task.getAssignedTo().getId());
            assignee.put("fullName", task.getAssignedTo().getFullName());
            assignee.put("email", task.getAssignedTo().getEmail());
            data.put("assignedTo", assignee);
        }

        data.put("createdAt", task.getCreatedAt());
        data.put("updatedAt", task.getUpdatedAt());

        return data;
    }

    @Data
    @NoArgsConstructor
    public static class CreateTaskRequest {
        private String title;
        private String description;
        private String assignedTo;
        private String priority;
        private String dueDate;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateTaskRequest {
        private String title;
        private String description;
        private String status;
        private String priority;
        private String assignedTo;
        private String dueDate;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateTaskStatusRequest {
        private String status;
    }

    // List tasks across tenants (super admin only)
    @GetMapping("/tasks")
    public ResponseEntity<?> listAllTasks(@RequestAttribute("role") String role,
                                          @RequestParam(required = false) String tenantId,
                                          @RequestParam(required = false) String projectId,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "50") int limit) {
        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Unauthorized"));
        }

        Pageable pageable = PageRequest.of(page - 1, Math.min(limit, 100));

        Page<Task> tasks;
        if (projectId != null && !projectId.isBlank()) {
            tasks = taskRepository.findByProjectId(projectId, pageable);
        } else if (tenantId != null && !tenantId.isBlank()) {
            tasks = taskRepository.findByTenantId(tenantId, pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }

        List<Map<String, Object>> taskList = tasks.getContent().stream()
                .filter(t -> status == null || t.getStatus().name().equals(status))
                .map(this::buildTaskResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("tasks", taskList);
        response.put("total", tasks.getTotalElements());
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page);
        pagination.put("totalPages", tasks.getTotalPages());
        pagination.put("limit", limit);
        response.put("pagination", pagination);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
