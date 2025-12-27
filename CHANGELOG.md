# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Role-based access control (RBAC) with three permission levels
- Super admin cross-tenant access capabilities
- User role restrictions for task status updates only
- Tenant admin project and task management
- Role-based UI controls in frontend
- JavaDoc documentation for core classes
- .gitattributes for consistent line endings
- .editorconfig for code formatting standards
- CONTRIBUTING.md with development guidelines

### Changed
- Project creation restricted to admins
- Task operations require admin role (except status updates)
- Email uniqueness scoped to individual tenants
- Dashboard task count calculation using project.taskCount

### Fixed
- Email validation now properly scoped to target tenant
- Task count display in dashboard statistics
- Tenant controller role attribute compilation error

### Security
- Enhanced authorization checks across all controllers
- Proper tenant isolation for non-admin users
- Super admin privilege validation

## [1.0.0] - 2025-12-27

### Added
- Initial project setup with Spring Boot backend
- React frontend with Vite build tool
- Multi-tenant architecture with subdomain routing
- JWT-based authentication
- Database migrations with Flyway
- Project and task management endpoints
- User management with role support
- Docker containerization
- Comprehensive README documentation
- Test credentials and seed data

### Security
- BCrypt password hashing
- CORS configuration
- SQL injection protection via JPA
