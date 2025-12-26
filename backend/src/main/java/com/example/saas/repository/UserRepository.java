package com.example.saas.repository;

import com.example.saas.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailAndTenantId(String email, String tenantId);
    Optional<User> findByEmail(String email);
    Page<User> findByTenantId(String tenantId, Pageable pageable);
    Page<User> findByTenantIdAndFullNameIgnoreCaseContainingOrTenantIdAndEmailIgnoreCaseContaining(String tenantId1, String name, String tenantId2, String email, Pageable pageable);
    long countByTenantId(String tenantId);
}
