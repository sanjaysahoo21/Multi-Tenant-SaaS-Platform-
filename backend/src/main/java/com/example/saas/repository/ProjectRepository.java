package com.example.saas.repository;

import com.example.saas.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    Page<Project> findByTenantId(String tenantId, Pageable pageable);
    long countByTenantId(String tenantId);
}
