# System Architecture & Design

## 1. High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         Web Browser (React + Vite Frontend)              │  │
│  │  - Light/Dark Theme Support                              │  │
│  │  - Role-Based UI Rendering                               │  │
│  │  - JWT Token Management                                  │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                           ↓ HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                  LOAD BALANCER / REVERSE PROXY                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Nginx / ALB                            │  │
│  │  - SSL/TLS Termination                                   │  │
│  │  - Request Routing                                       │  │
│  │  - Rate Limiting                                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│                      API GATEWAY LAYER                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │            Spring Boot REST API (Port 5000)              │  │
│  │  - JWT Authentication Filter                             │  │
│  │  - CORS Configuration                                    │  │
│  │  - Request Validation                                    │  │
│  │  - Error Handling                                        │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                           ↓
┌──────────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                              │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │            Service Layer (Business Logic)                  │  │
│  │  ┌──────────────────────────────────────────────────────┐  │  │
│  │  │ AuthService      ProjectService    UserService       │  │  │
│  │  │ TenantService    TaskService       PermissionService │  │  │
│  │  └──────────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │           Repository Layer (Data Access)                   │  │
│  │  ┌──────────────────────────────────────────────────────┐  │  │
│  │  │ JPA Repositories with @Query methods                 │  │  │
│  │  │ Automatic tenant_id filtering on all queries         │  │  │
│  │  └──────────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
                           ↓
┌──────────────────────────────────────────────────────────────────┐
│                   PERSISTENCE LAYER                               │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │    PostgreSQL 16 (Port 5432)                               │  │
│  │  - Multi-tenant Schema (shared schema, row isolation)      │  │
│  │  - 6 Core Tables                                           │  │
│  │  - Indexes for performance                                 │  │
│  │  - Automated backups                                       │  │
│  │  - Flyway migrations                                       │  │
│  └────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

## 2. Database Schema (Entity Relationship Diagram)

```
┌──────────────────────┐         ┌──────────────────────┐
│      tenants         │ 1    *  │        users         │
├──────────────────────┤─────────┤──────────────────────┤
│ id (PK)              │         │ id (PK)              │
│ name                 │         │ email                │
│ subdomain (UNIQUE)   │         │ password_hash        │
│ subscription_plan    │         │ full_name            │
│ status               │         │ role                 │
│ created_at           │         │ tenant_id (FK)       │
│ updated_at           │         │ status               │
│ max_users            │         │ created_at           │
│ max_projects         │         │ updated_at           │
│                      │         │                      │
└──────────────────────┘         └──────────────────────┘
         1 │                              │ 1
           │                              │
           │ * │                    * │
           │   │                      │
           └───┼──────────┬───────────┘
               │          │
        ┌──────────────────┴──────┐
        │                         │
        │                         │
   ┌─────────────┐       ┌──────────────────┐
   │  projects   │ 1   * │      tasks       │
   ├─────────────┤───────┤──────────────────┤
   │ id (PK)     │       │ id (PK)          │
   │ name        │       │ title            │
   │ description │       │ description      │
   │ status      │       │ priority         │
   │ tenant_id   │       │ status           │
   │ (FK)        │       │ project_id (FK)  │
   │ created_by  │       │ assigned_to (FK) │
   │ created_at  │       │ due_date         │
   │ updated_at  │       │ created_at       │
   │             │       │ updated_at       │
   └─────────────┘       └──────────────────┘
```

## 3. API Endpoints (19 Total)

### Authentication Endpoints (3)
```
POST   /api/auth/login              - User login
POST   /api/auth/register           - Tenant registration
POST   /api/auth/logout             - User logout (optional)
```

### Tenant Management Endpoints (4)
```
GET    /api/tenants                 - List all tenants (SUPER_ADMIN)
GET    /api/tenants/{tenantId}      - Get tenant details
PUT    /api/tenants/{tenantId}      - Update tenant (SUPER_ADMIN)
DELETE /api/tenants/{tenantId}      - Delete tenant (SUPER_ADMIN)
```

### User Management Endpoints (4)
```
GET    /api/tenants/{tenantId}/users          - List users in tenant
POST   /api/tenants/{tenantId}/users          - Invite user (TENANT_ADMIN)
PUT    /api/tenants/{tenantId}/users/{userId} - Update user (TENANT_ADMIN)
DELETE /api/tenants/{tenantId}/users/{userId} - Delete user (TENANT_ADMIN)
```

### Project Management Endpoints (4)
```
GET    /api/projects                - List all projects in tenant
POST   /api/projects                - Create project (TENANT_ADMIN/USER)
PUT    /api/projects/{projectId}    - Update project (TENANT_ADMIN)
DELETE /api/projects/{projectId}    - Delete project (TENANT_ADMIN)
```

### Task Management Endpoints (4)
```
GET    /api/projects/{projectId}/tasks           - List tasks in project
POST   /api/projects/{projectId}/tasks           - Create task
PUT    /api/projects/{projectId}/tasks/{taskId}  - Update task
DELETE /api/projects/{projectId}/tasks/{taskId}  - Delete task
```

### System Endpoints (1)
```
GET    /api/health                  - Health check endpoint
```

---

## 4. Data Flow Diagram

```
┌──────────────────┐
│   User Login     │
└────────┬─────────┘
         │
         ↓
  ┌─────────────────────────────────┐
  │ POST /api/auth/login             │
  │ {email, password}                │
  └────────┬────────────────────────┘
           │
           ↓
  ┌─────────────────────────────────┐
  │ AuthService.login()              │
  │ 1. Find user by email            │
  │ 2. Verify password (BCrypt)      │
  │ 3. Generate JWT token            │
  └────────┬────────────────────────┘
           │
           ↓
  ┌─────────────────────────────────┐
  │ Return JWT Token                 │
  │ {                                │
  │   "token": "eyJhbGc...",         │
  │   "expiresIn": 86400             │
  │ }                                │
  └────────┬────────────────────────┘
           │
           ↓ Client stores token in localStorage
  ┌──────────────────────────────────┐
  │ Subsequent API Requests          │
  │ Header: Authorization: Bearer... │
  └────────┬─────────────────────────┘
           │
           ↓
  ┌──────────────────────────────────┐
  │ JwtAuthenticationFilter           │
  │ 1. Extract token from header     │
  │ 2. Validate signature            │
  │ 3. Extract tenant_id, user_id    │
  │ 4. Set SecurityContext           │
  └────────┬─────────────────────────┘
           │
           ↓
  ┌──────────────────────────────────┐
  │ @PreAuthorize check              │
  │ Verify role permission           │
  └────────┬─────────────────────────┘
           │
           ↓
  ┌──────────────────────────────────┐
  │ Service Layer                    │
  │ Execute business logic           │
  └────────┬─────────────────────────┘
           │
           ↓
  ┌──────────────────────────────────┐
  │ Repository Layer                 │
  │ All queries include:             │
  │ WHERE tenant_id = ? AND ...      │
  └────────┬─────────────────────────┘
           │
           ↓
  ┌──────────────────────────────────┐
  │ PostgreSQL Database              │
  │ Return tenant-isolated data      │
  └────────┬─────────────────────────┘
           │
           ↓
  ┌──────────────────────────────────┐
  │ Response to Client               │
  │ 200 OK with data                 │
  └──────────────────────────────────┘
```

---

## 5. Deployment Architecture

### Local Development
```
Docker Compose (Single Command)
├── postgres:16-alpine (Port 5432)
│   └── saasdb database
├── backend (Port 5000)
│   ├── Spring Boot application
│   ├── Auto migrations (Flyway)
│   └── Auto seed data
└── frontend (Port 3000)
    ├── React + Vite
    └── Nginx serving static files
```

### Production (Recommended)
```
AWS / Azure Cloud
├── RDS PostgreSQL (Multi-AZ, automated backups)
├── ECS/AKS Container Service
│   ├── Backend Service (auto-scaling)
│   └── Frontend Service (static files via S3/Blob)
├── ALB/AppGateway (Load balancing, SSL/TLS)
├── CloudWatch/Azure Monitor (Logging, metrics)
└── Secrets Manager (Environment variables, credentials)
```

---

## 6. Security Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  CLIENT REQUEST                         │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
        ┌─────────────────────────────┐
        │   SSL/TLS Encryption        │
        │   (HTTPS only)              │
        └────────────┬────────────────┘
                     │
                     ↓
        ┌─────────────────────────────┐
        │   CORS Validation           │
        │   (Whitelist origin)        │
        └────────────┬────────────────┘
                     │
                     ↓
        ┌─────────────────────────────┐
        │   JWT Token Verification    │
        │   (Signature & expiry)      │
        └────────────┬────────────────┘
                     │
                     ↓
        ┌─────────────────────────────┐
        │   Input Validation          │
        │   (@Valid annotations)      │
        └────────────┬────────────────┘
                     │
                     ↓
        ┌─────────────────────────────┐
        │   Authorization Check       │
        │   (Role-based access)       │
        └────────────┬────────────────┘
                     │
                     ↓
        ┌─────────────────────────────┐
        │   Tenant Ownership Check    │
        │   (Verify tenant_id match)  │
        └────────────┬────────────────┘
                     │
                     ↓
        ┌─────────────────────────────┐
        │   Parameterized Queries     │
        │   (Prevent SQL injection)   │
        └────────────┬────────────────┘
                     │
                     ↓
        ┌─────────────────────────────┐
        │   Audit Logging             │
        │   (Log all data access)     │
        └────────────┬────────────────┘
                     │
                     ↓
        ┌─────────────────────────────┐
        │   APPROVED REQUEST          │
        │   Data isolated by tenant   │
        └─────────────────────────────┘
```

---

## 7. Technology Stack Decision Tree

```
WHY SPRING BOOT?
├── Security: Built-in Spring Security + OAuth2 support
├── Enterprise: Used by Fortune 500 companies
├── Ecosystem: Largest Java ecosystem
├── Multi-tenant: Excellent JPA/Hibernate support
└── Performance: Sub-100ms response times achievable

WHY REACT?
├── Component Reusability: Build complex UIs efficiently
├── Performance: Virtual DOM for optimal rendering
├── Ecosystem: npm has 1M+ packages
├── Developer UX: Hot Module Replacement (HMR)
└── Mobile: React Native for future mobile apps

WHY POSTGRESQL?
├── Reliability: ACID compliance for financial data
├── Security: Row-level security policies (RLS)
├── Features: JSON types, full-text search, partitioning
├── Scalability: Handles billions of rows
└── Cost: Open source, no licensing fees

WHY JWT?
├── Stateless: Scales horizontally (no session storage)
├── RESTful: Perfect for REST APIs and SPAs
├── Security: Cryptographic signing prevents tampering
├── Cross-Domain: Works across microservices
└── Mobile: Ideal for mobile and IoT devices

WHY DOCKER?
├── Consistency: Same environment dev → production
├── Isolation: Containers don't interfere with each other
├── Scaling: Easy horizontal scaling with orchestration
├── CI/CD: Automated deployment pipelines
└── Evaluation: Reproducible environment for grading
```

---

## 8. Performance Optimization Strategies

### Database Level
```sql
-- Indexing Strategy
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_projects_tenant_id ON projects(tenant_id);
CREATE INDEX idx_tasks_project_id ON tasks(project_id);

-- Composite Index for common filters
CREATE INDEX idx_users_tenant_email ON users(tenant_id, email);

-- Query Optimization
SELECT p.*, COUNT(t.id) as task_count
FROM projects p
LEFT JOIN tasks t ON p.id = t.project_id
WHERE p.tenant_id = ?
GROUP BY p.id;
```

### Application Level
```java
// Connection Pooling
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

// Batch Operations
List<Task> tasks = new ArrayList<>();
for (int i = 0; i < 1000; i++) {
    tasks.add(new Task(...));
    if (i % 100 == 0) {
        taskRepository.saveAll(tasks);
        tasks.clear();
    }
}

// Caching Strategy
@Cacheable(value = "projects", key = "#tenantId")
public List<Project> getProjects(UUID tenantId) { }
```

### Frontend Level
```javascript
// Code Splitting & Lazy Loading
const Dashboard = lazy(() => import('./pages/Dashboard'));
const Projects = lazy(() => import('./pages/Projects'));

// Memoization
const ProjectCard = memo(({ project }) => {
    return <Card>{project.name}</Card>;
});
```

---

This architecture ensures scalability, security, and maintainability while supporting an unlimited number of tenants.
