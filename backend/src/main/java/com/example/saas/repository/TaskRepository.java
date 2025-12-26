package com.example.saas.repository;

import com.example.saas.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    Page<Task> findByProjectIdAndTenantId(String projectId, String tenantId, Pageable pageable);
    long countByProjectId(String projectId);
}
