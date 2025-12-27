# Technical Specification & Development Setup

## 1. Project Structure

```
Multi-Tenant-SaaS-Platform/
├── backend/                          # Spring Boot Java API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/saas/
│   │   │   │   ├── controller/       # REST API endpoints
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── TenantController.java
│   │   │   │   │   ├── UserController.java
│   │   │   │   │   ├── ProjectController.java
│   │   │   │   │   └── TaskController.java
│   │   │   │   ├── service/         # Business logic
│   │   │   │   │   ├── AuthService.java
│   │   │   │   │   ├── TenantService.java
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   ├── ProjectService.java
│   │   │   │   │   ├── TaskService.java
│   │   │   │   │   └── PermissionService.java
│   │   │   │   ├── repository/      # Data access
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   ├── TenantRepository.java
│   │   │   │   │   ├── ProjectRepository.java
│   │   │   │   │   └── TaskRepository.java
│   │   │   │   ├── entity/          # JPA entities
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Tenant.java
│   │   │   │   │   ├── Project.java
│   │   │   │   │   └── Task.java
│   │   │   │   ├── security/        # Authentication & Authorization
│   │   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   │   └── SecurityConfig.java
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── UserDTO.java
│   │   │   │   │   └── ApiResponse.java
│   │   │   │   ├── exception/       # Custom exceptions
│   │   │   │   │   ├── UnauthorizedException.java
│   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   └── ValidationException.java
│   │   │   │   └── SaasBackendApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── db/migration/    # Flyway migrations
│   │   │       │   ├── V1__Create_tenants_table.sql
│   │   │       │   ├── V2__Create_users_table.sql
│   │   │       │   ├── V3__Create_projects_table.sql
│   │   │       │   ├── V4__Create_tasks_table.sql
│   │   │       │   └── V5__Seed_data.sql
│   │   │       └── static/
│   │   └── test/
│   │       └── java/com/example/saas/
│   │           ├── AuthControllerTest.java
│   │           ├── ProjectServiceTest.java
│   │           └── UserRepositoryTest.java
│   ├── pom.xml                      # Maven configuration
│   └── Dockerfile                   # Backend container image
│
├── frontend/                         # React + Vite frontend
│   ├── src/
│   │   ├── components/              # Reusable components
│   │   │   ├── Navbar.jsx
│   │   │   ├── Navbar.css
│   │   │   └── ErrorBoundary.jsx
│   │   ├── context/                 # State management
│   │   │   ├── AuthContext.jsx
│   │   │   └── ThemeContext.jsx
│   │   ├── pages/                   # Page components
│   │   │   ├── auth/
│   │   │   │   ├── Login.jsx
│   │   │   │   ├── Register.jsx
│   │   │   │   └── Auth.css
│   │   │   ├── dashboard/
│   │   │   │   ├── Dashboard.jsx
│   │   │   │   └── Dashboard.css
│   │   │   ├── projects/
│   │   │   │   ├── Projects.jsx
│   │   │   │   ├── ProjectDetails.jsx
│   │   │   │   ├── Projects.css
│   │   │   │   └── ProjectDetails.css
│   │   │   ├── users/
│   │   │   │   ├── Users.jsx
│   │   │   │   └── Users.css
│   │   │   └── tenants/
│   │   │       ├── Tenants.jsx
│   │   │       └── Tenants.css
│   │   ├── api/                     # API client
│   │   │   └── axios.js
│   │   ├── App.jsx
│   │   ├── App.css
│   │   ├── main.jsx
│   │   └── index.css
│   ├── public/                      # Static assets
│   ├── package.json
│   ├── vite.config.js
│   ├── Dockerfile                   # Frontend container image
│   └── nginx.conf                   # Nginx configuration
│
├── docs/                            # Documentation
│   ├── research.md                  # Multi-tenancy research
│   ├── PRD.md                       # Product requirements
│   ├── architecture.md              # System architecture
│   ├── technical-spec.md            # Technical specification
│   ├── API.md                       # API documentation
│   ├── DOCKER.md                    # Docker setup guide
│   └── images/
│       ├── system-architecture.png
│       └── database-erd.png
│
├── docker-compose.yml               # Docker Compose configuration
├── .gitignore
├── README.md                        # Main project README
├── CHANGELOG.md                     # Project changelog
├── CONTRIBUTING.md                 # Contribution guidelines
├── LICENSE                          # Project license
└── submission.json                  # Test credentials & metadata
```

---

## 2. Development Setup Guide

### Prerequisites
- Docker & Docker Compose (v20.10+)
- Git
- (Optional) Java 17 SDK (for local backend development)
- (Optional) Node.js 18+ (for local frontend development)

### Quick Start with Docker

#### Step 1: Clone Repository
```bash
git clone https://github.com/sanjaysahoo21/Multi-Tenant-SaaS-Platform-.git
cd Multi-Tenant-SaaS-Platform
```

#### Step 2: Start Services
```bash
docker compose up -d --build
```

**Expected Output:**
```
✓ Creating saas-postgres     ... done
✓ Creating saas-backend      ... done
✓ Creating saas-frontend     ... done

Services started successfully!
```

#### Step 3: Verify Services
```bash
# Check all services are running
docker compose ps

# Expected output:
NAME                 STATUS
saas-postgres        Up 2 minutes
saas-backend         Up 1 minute
saas-frontend        Up 1 minute
```

#### Step 4: Verify Application
```bash
# Test health check endpoint
curl http://localhost:5000/api/health

# Expected response:
{
  "status": "UP",
  "database": "CONNECTED",
  "jwt_validation": "WORKING",
  "timestamp": "2025-12-27T13:21:24"
}
```

#### Step 5: Access Application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:5000/api
- **Database**: localhost:5432 (PostgreSQL)

---

### Local Development Setup (Without Docker)

#### Backend Setup

**1. Prerequisites**
```bash
# Install Java 17
# Install Maven 3.8+
java -version    # Should show Java 17
mvn -version     # Should show Maven 3.8+
```

**2. Start PostgreSQL** (Docker only)
```bash
docker run -d \
  --name saas-postgres \
  -e POSTGRES_DB=saasdb \
  -e POSTGRES_USER=saasuser \
  -e POSTGRES_PASSWORD=saaspass123 \
  -p 5432:5432 \
  postgres:16-alpine
```

**3. Configure Backend**
```bash
cd backend
# Edit application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/saasdb
spring.datasource.username=saasuser
spring.datasource.password=saaspass123
```

**4. Run Backend**
```bash
mvn spring-boot:run
# Backend running on http://localhost:5000
```

#### Frontend Setup

**1. Prerequisites**
```bash
node -v    # Should show Node 18+
npm -v     # Should show npm 9+
```

**2. Install Dependencies**
```bash
cd frontend
npm install
```

**3. Configure API Base URL**
```javascript
// src/api/axios.js
const API_BASE_URL = 'http://localhost:5000/api';
```

**4. Run Development Server**
```bash
npm run dev
# Frontend running on http://localhost:5173
```

---

## 3. Docker Configuration Details

### docker-compose.yml Structure

**Services:**
```yaml
services:
  database:          # PostgreSQL container
  backend:          # Spring Boot API container
  frontend:         # React/Nginx container
```

**Port Mappings:**
```yaml
database: 5432 → 5432
backend: 5000 → 5000
frontend: 3000 → 80 (nginx)
```

**Volumes:**
```yaml
postgres_data:  # Persistent database volume
```

**Environment Variables:**
```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://database:5432/saasdb
SPRING_DATASOURCE_USERNAME: saasuser
SPRING_DATASOURCE_PASSWORD: saaspass123
JWT_SECRET: {minimum-32-character-secret}
JWT_EXPIRATION: 86400
```

---

## 4. Building & Deployment

### Build Docker Images

```bash
# Build all services
docker compose build

# Build specific service
docker compose build backend
docker compose build frontend
```

### Push to Registry

```bash
# Tag images
docker tag saas-backend:latest yourregistry/saas-backend:latest
docker tag saas-frontend:latest yourregistry/saas-frontend:latest

# Push to registry
docker push yourregistry/saas-backend:latest
docker push yourregistry/saas-frontend:latest
```

### Production Deployment

```bash
# Deploy on AWS ECS
aws ecs create-service --cluster saas \
  --service-name workstack \
  --task-definition saas-backend:1

# Deploy on Azure AKS
kubectl apply -f k8s-deployment.yaml

# Deploy on Heroku
git push heroku main
```

---

## 5. Database Migrations

### Flyway Migration Files

**Location:** `backend/src/main/resources/db/migration/`

**Naming Convention:** `V{number}__{description}.sql`

**Example:**
```sql
-- V1__Create_tenants_table.sql
CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    subdomain VARCHAR(255) UNIQUE NOT NULL,
    subscription_plan VARCHAR(50) DEFAULT 'FREE',
    status VARCHAR(50) DEFAULT 'ACTIVE',
    max_users INTEGER DEFAULT 5,
    max_projects INTEGER DEFAULT 3,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- V2__Create_users_table.sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role VARCHAR(50) DEFAULT 'USER',
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(tenant_id, email)
);
```

### Running Migrations

**Automatic (On Application Startup):**
```
Flyway automatically runs migrations when backend starts
```

**Manual:**
```bash
# Via Maven
mvn flyway:migrate

# Via Spring Boot CLI
spring flyway:migrate
```

---

## 6. Configuration Properties

### application.properties

```properties
# Server
server.port=5000
server.servlet.context-path=/api

# Database
spring.datasource.url=jdbc:postgresql://database:5432/saasdb
spring.datasource.username=saasuser
spring.datasource.password=saaspass123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# JWT
app.jwt.secret=${JWT_SECRET:your-secret-key-minimum-32-characters-long}
app.jwt.expirationInMs=86400000

# CORS
app.cors.allowedOrigins=http://localhost:3000,https://workstack.com

# Logging
logging.level.root=INFO
logging.level.com.example.saas=DEBUG
```

---

## 7. Testing Strategy

### Backend Testing

```bash
# Unit Tests
mvn test

# Integration Tests
mvn verify

# Test Coverage
mvn jacoco:report

# Specific test class
mvn test -Dtest=AuthControllerTest
```

### Frontend Testing

```bash
# Unit Tests
npm test

# E2E Tests
npm run test:e2e

# Test Coverage
npm test -- --coverage
```

---

## 8. Troubleshooting

### Docker Issues

**Problem:** Container fails to start
```bash
# Check logs
docker compose logs backend

# Restart services
docker compose restart

# Full reset
docker compose down -v
docker compose up -d --build
```

**Problem:** Database connection refused
```bash
# Check database is running
docker compose ps

# Check network connectivity
docker exec saas-backend ping database

# Check database logs
docker compose logs database
```

### Port Conflicts

```bash
# Check if ports are in use
lsof -i :5432
lsof -i :5000
lsof -i :3000

# Change ports in docker-compose.yml
# Restart services
docker compose down
docker compose up -d
```

---

## 9. Performance Tuning

### Database Performance

```sql
-- Create indexes
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_tenant_email ON users(tenant_id, email);

-- Check query plans
EXPLAIN ANALYZE SELECT * FROM users WHERE tenant_id = ? AND email = ?;
```

### Application Performance

```properties
# Connection pooling
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# Batch processing
spring.jpa.properties.hibernate.jdbc.batch_size=20

# Caching
spring.cache.type=caffeine
```

---

This technical specification provides comprehensive guidance for development, deployment, and troubleshooting of the WorkStack platform.
