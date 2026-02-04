# Technical Specification & Development Setup

## 1. Project Overview

### 1.1 Technology Stack & Version Matrix

| Component | Technology | Version | Description |
| :--- | :--- | :--- | :--- |
| **Backend** | Java / Spring Boot | **17 (LTS) / 3.2.0** | Core API logic, Spring Security |
| **Frontend** | React / Vite | **18 / 5.0** | Single Page Application (SPA) |
| **Database** | PostgreSQL | **16-alpine** | Relational Database with RLS |
| **Container** | Docker Engine | **24.0+** | Container Runtime |
| **Build Tool** | Maven | **3.8+** | Backend Dependency Management |
| **Pkg Manager**| NPM | **9.0+** | Frontend Dependency Management |

### 1.2 Project Structure

```
Multi-Tenant-SaaS-Platform/
├── backend/                          # Spring Boot Java API
│   ├── src/main/java/com/example/saas/
│   │   ├── controller/               # REST Endpoints
│   │   ├── service/                  # Business Logic
│   │   ├── repository/               # JPA Repositories
│   │   ├── entity/                   # Database Entities
│   │   └── security/                 # JWT & RBAC Logic
│   └── src/main/resources/db/migration/ # Flyway SQL Migrations
├── frontend/                         # React + Vite frontend
│   ├── src/
│   │   ├── components/               # Reusable UI Atoms
│   │   ├── pages/                    # Route Views
│   │   ├── context/                  # React Context (Auth, Theme)
│   │   └── api/                      # Axios Interceptors
├── docs/                             # Project Documentation
├── docker-compose.yml                # Orchestration Config
└── submission.json                   # Project Metadata
```

---

## 2. Development Setup Guide

### 2.1 System Requirements
*   **CPU**: Dual Core 2GHz+ (Recommended: Quad Core)
*   **RAM**: 4GB Minimum (Recommended: 8GB for full Docker environment)
*   **Disk**: 10GB Free Space
*   **OS**: Linux (Ubuntu 20.04+), macOS (M1/Intel), or Windows 10+ (WSL2)

### 2.2 Quick Start via Docker (Recommended)

1.  **Clone Repository**
    ```bash
    git clone https://github.com/sanjaysahoo21/Multi-Tenant-SaaS-Platform-.git
    cd Multi-Tenant-SaaS-Platform
    ```

2.  **Start Services** (Unified Command)
    ```bash
    docker compose up -d --build
    ```

3.  **Verify Status**
    ```bash
    docker compose ps
    # Ensure all 3 services (backend, frontend, database) are "Up"
    ```

4.  **Access Application**
    *   **Frontend**: [http://localhost:3000](http://localhost:3000)
    *   **Backend Health**: [http://localhost:5000/api/health](http://localhost:5000/api/health)
    *   **API Docs**: [http://localhost:5000/swagger-ui.html](http://localhost:5000/swagger-ui.html) (if enabled)

---

## 3. Local Development (Hybrid)

For developers who want to run the backend/frontend locally for debugging while keeping the database in Docker.

### 3.1 Debugging Backend (IntelliJ / VS Code)
1.  **Start Database Only**:
    ```bash
    docker compose up -d database
    ```
2.  **Environment Configuration**:
    *   Set `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/saasdb`
    *   (Note: Ensure port 5433 matches your docker-compose mapping)
3.  **Run Application**:
    *   **IntelliJ**: Open `SaasBackendApplication.java` -> Right Click -> Debug
    *   **VS Code**: Use the "Java: Run Java" extension.

### 3.2 Debugging Frontend
1.  **Install Dependencies**:
    ```bash
    cd frontend && npm install
    ```
2.  **Start Dev Server**:
    ```bash
    npm run dev
    # Runs on http://localhost:5173 (React default)
    ```

---

## 4. Database Schema & Migrations

We use **Flyway** for version-controlled database changes.

*   **Location**: `backend/src/main/resources/db/migration`
*   **Format**: `V{Version}__Description.sql`

| Version | Description | Key Changes |
| :--- | :--- | :--- |
| **V1** | Create Tenants | Core tenant tables with isolation fields |
| **V2** | Create Users | Auth tables with BCrypt passwords |
| **V3** | Create Projects | Project management schema |
| **V4** | Create Tasks | Task tracking and assignment |
| **V5** | Audit Logs | **Added details, user_agent, old/new value** |

**To Reset Database:**
```bash
docker compose down -v
# -v flag deletes the persistent volume, forcing a fresh migration run
```

---

## 5. Docker Configuration

### Services Breakdown
*   **`backend`**: OpenJDK 17 Alpine image. Multi-stage build to keep image size small (~150MB).
*   **`frontend`**: Nginx Alpine image. Serves static React build. Configured to handle client-side routing.
*   **`database`**: Postgres 16 Alpine. Initializes with `init.sql` if no volume exists.

### Environment Variables
Sensitive data is injected via environment variables (checking `application.properties`):
*   `JWT_SECRET`: Signing key for tokens.
*   `POSTGRES_PASSWORD`: DB credentials.

---

## 6. Testing Strategy

### Unit Testing
*   **Backend**: JUnit 5 + Mockito. Focus on Service layer logic.
    ```bash
    mvn test
    ```
*   **Frontend**: Vitest + React Testing Library. Focus on Component rendering.
    ```bash
    npm test
    ```

### Integration Testing
*   **API Verification**: A shell script `verify_api.sh` is included to curl all endpoints and verify 200 OK responses against a running Docker instance.

---

## 7. Troubleshooting Guide

| Issue | Probable Cause | Fix |
| :--- | :--- | :--- |
| **Backend Connection Refused** | Port Conflict (5000/5432) | Check `lsof -i :5000`. Edit `docker-compose.yml` ports. |
| **Database Auth Failed** | Old Volume Data | Run `docker compose down -v` to reset password. |
| **Frontend "Network Error"** | CORS / Backend Down | Check connection to localhost:5000. Check CORS setup. |

---

## 8. CI/CD Pipeline Configuration

We recommend a standard GitHub Actions workflow for automated quality checks.

### `.github/workflows/ci.yml` (Proposed)

```yaml
name: CI Pipeline

on: [push, pull_request]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    # Backend CI
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Build and Test Backend
      run: cd backend && mvn clean verify
      
    # Frontend CI
    - name: Set up Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
    - name: Build Frontend
      run: cd frontend && npm install && npm run build
```

---

**Last Updated**: January 2026
**Authored By**: Application Architect
