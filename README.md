# WorkStack

A production-ready, enterprise-grade multi-tenant SaaS platform built with Spring Boot, React, and PostgreSQL. Features include tenant isolation, role-based access control, JWT authentication, and comprehensive project/task management capabilities.

## 🚀 Features

- **Multi-Tenancy**: Complete tenant isolation with subdomain-based routing
- **Authentication & Authorization**: JWT-based stateless authentication with 3 role levels (Super Admin, Tenant Admin, User)
- **Subscription Plans**: FREE, PRO, and ENTERPRISE tiers with configurable limits
- **Project Management**: Full CRUD operations with tenant isolation
- **Task Management**: Assign tasks, set priorities, track progress
- **User Management**: Invite users, manage roles, enforce plan limits
- **Database Migrations**: Flyway-based schema versioning with seed data
- **API Documentation**: 19 REST API endpoints with consistent response format
- **Security**: BCrypt password hashing, CORS configured, SQL injection protection

## 📋 Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Build Tool**: Maven
- **Database**: PostgreSQL 16
- **ORM**: JPA/Hibernate
- **Authentication**: JWT (JJWT 0.12.3)
- **Password Hashing**: BCrypt
- **Migrations**: Flyway
- **Security**: Spring Security 6

### Frontend
- **Framework**: React 18
- **Build Tool**: Vite
- **Routing**: React Router
- **State Management**: Context API
- **HTTP Client**: Axios
- **Styling**: CSS3 (customizable)

### DevOps
- **Containerization**: Docker & Docker Compose
- **Database**: PostgreSQL container
- **Ports**: Backend (5000), Frontend (3000), Database (5432)

## 🏗️ Architecture

### Database Schema

**6 Main Tables:**
1. `tenants` - Organization/company data with subscription plans
2. `users` - User accounts with per-tenant email uniqueness
3. `projects` - Projects owned by tenants
4. `tasks` - Tasks within projects with assignments
5. `audit_logs` - Audit trail for compliance

**Key Features:**
- UUID primary keys for security
- Foreign keys with CASCADE delete
- Composite unique constraints
- Indexed for performance
- Proper enum types for status/roles

### API Structure

**19 RESTful Endpoints:**

**Authentication (4)**
- `POST /api/auth/register-tenant` - Register new tenant with admin
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/auth/me` - Get current user profile
- `POST /api/auth/logout` - Logout (client-side)

**Tenant Management (5)**
- `GET /api/tenants/{id}` - Get tenant details
- `PUT /api/tenants/{id}` - Update tenant
- `GET /api/tenants` - List all tenants (super_admin only)
- `POST /api/tenants/{id}/users` - Add user to tenant
- `GET /api/tenants/{id}/users` - List tenant users

**User Management (3)**
- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

**Project Management (5)**
- `POST /api/projects` - Create project
- `GET /api/projects` - List projects
- `GET /api/projects/{id}` - Get project details
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project

**Task Management (5)**
- `POST /api/projects/{projectId}/tasks` - Create task
- `GET /api/projects/{projectId}/tasks` - List tasks
- `PUT /api/tasks/{id}` - Update task
- `PATCH /api/tasks/{id}/status` - Update task status only
- `DELETE /api/tasks/{id}` - Delete task

**Health Check (1)**
- `GET /api/health` - System health and database status

## 🚦 Getting Started

### Prerequisites

```bash
- Java 17 (LTS recommended) or Java 24
- Maven 3.8+
- Node.js 18+ and npm
- PostgreSQL 16
- Docker & Docker Compose (optional)
```

### Known Build Issue & Solutions

**Issue**: Lombok annotation processing may fail with Java 24 due to compatibility issues.

**Solutions:**

**Option 1: Use Java 17 LTS (Recommended)**
```bash
# Download Java 17 from: https://adoptium.net/
# Set JAVA_HOME to Java 17 installation
export JAVA_HOME=/path/to/java17
mvn clean package
```

**Option 2: Update Lombok version (if using Spring Boot 3.2+)**
```xml
<!-- In pom.xml -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <optional>true</optional>
</dependency>
```

**Option 3: Remove Lombok (Production-ready approach)**
The models can be updated to use explicit getters/setters instead of Lombok annotations. This is actually more maintainable for enterprise applications.

### Installation

**1. Clone the repository**
```bash
git clone https://github.com/sanjaysahoo21/Multi-Tenant-SaaS-Platform-.git
cd Multi-Tenant-SaaS-Platform
```

**2. Backend Setup**
```bash
cd backend

# Update application.properties with your database credentials
# src/main/resources/application.properties

# Build (use Java 17 or fix Lombok as described above)
mvn clean package -DskipTests

# Run
java -jar target/multi-tenant-saas-1.0.0.jar
```

**3. Frontend Setup**
```bash
cd frontend
npm install
npm run dev
```

**4. Docker Setup** (Alternative)
```bash
# Build and run all services
docker-compose up -d

# Check logs
docker-compose logs -f

# Stop
docker-compose down
```

## 🔐 Authentication & Authorization

### JWT Token Structure
```json
{
  "sub": "userId",
  "tenantId": "tenantId",
  "role": "TENANT_ADMIN",
  "exp": 1640995200
}
```

### Role Hierarchy
- **SUPER_ADMIN**: Full system access, manage all tenants
- **TENANT_ADMIN**: Manage own tenant, all users/projects
- **USER**: View/edit assigned projects and tasks

### Test Credentials

**Super Admin**
- Email: `superadmin@system.com`
- Password: `Admin@123`
- Tenant: None (system-wide access)

**Demo Tenant Admin**
- Email: `admin@demo.com`
- Password: `Demo@123`
- Subdomain: `demo`

**Demo Users**
- Email: `user1@demo.com` / `user2@demo.com`
- Password: `User@123`
- Subdomain: `demo`

## 📊 Subscription Plans

| Feature | FREE | PRO | ENTERPRISE |
|---------|------|-----|------------|
| Max Users | 5 | 25 | 100 |
| Max Projects | 3 | 15 | 50 |
| API Access | ✓ | ✓ | ✓ |
| Audit Logs | ✗ | ✓ | ✓ |
| Priority Support | ✗ | ✗ | ✓ |

## 🗄️ Database Migrations

Flyway automatically runs migrations on startup:

- `V001__Create_tenants_table.sql`
- `V002__Create_users_table.sql`
- `V003__Create_projects_table.sql`
- `V004__Create_tasks_table.sql`
- `V005__Create_audit_logs_table.sql`
- `V006__Seed_initial_data.sql`

## 🔧 Configuration

### Backend (application.properties)
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/saas_db
spring.datasource.username=postgres
spring.datasource.password=vinay2122@

# JWT
jwt.secret=your-256-bit-secret-key-here
jwt.expiration=86400

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# CORS
frontend.url=http://localhost:3000
```

### Frontend (vite.config.js)
```javascript
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': 'http://localhost:5000'
    }
  }
})
```

## 📝 API Response Format

All API responses follow this consistent format:

```json
{
  "success": true|false,
  "message": "Optional message",
  "data": { ... }
}
```

## 🧪 Testing

```bash
# Backend unit tests
cd backend
mvn test

# Frontend tests
cd frontend
npm test

# Integration tests
mvn verify
```

## 📦 Project Structure

```
WorkStack/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/saas/
│   │   │   │   ├── controller/      # REST API controllers
│   │   │   │   ├── model/           # JPA entities
│   │   │   │   ├── repository/      # Data access layer
│   │   │   │   ├── service/         # Business logic
│   │   │   │   ├── filter/          # JWT filter
│   │   │   │   ├── config/          # Security, CORS config
│   │   │   │   └── util/            # JWT utility, API response
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/    # Flyway SQL scripts
│   │   └── test/                    # Unit tests
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── pages/                   # React pages
│   │   ├── components/              # Reusable components
│   │   ├── api/                     # API client
│   │   ├── context/                 # React Context
│   │   └── App.jsx
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml
├── .gitignore
└── README.md
```

## 🚀 Deployment

### Production Checklist
- [ ] Use Java 17 LTS
- [ ] Configure proper JWT secret (256+ bits)
- [ ] Enable HTTPS/TLS
- [ ] Set up database backups
- [ ] Configure environment-specific properties
- [ ] Enable audit logging
- [ ] Set up monitoring (Prometheus, Grafana)
- [ ] Configure log aggregation
- [ ] Set up CI/CD pipeline
- [ ] Perform security audit

### Environment Variables
```bash
# Backend
DB_HOST=localhost
DB_PORT=5432
DB_NAME=saas_db
DB_USER=postgres
DB_PASSWORD=your_password
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400
FRONTEND_URL=http://localhost:3000

# Frontend
VITE_API_URL=http://localhost:5000/api
```

## 🔧 Troubleshooting

### Fresh Clone: Login/Registration Fails

**Problem:** After cloning and running `docker compose up`, login shows "Login failed" or registration shows "Registration failed".

**Root Causes:**
1. **Old Docker images cached** – images built before fixes were applied
2. **Wrong credentials** – using example creds from `submission.json` instead of seeded accounts
3. **Missing database migrations** – Flyway hasn't run yet

**Solution:**

1. **Clean rebuild** (removes old images and volumes):
```bash
git pull

docker compose down -v
docker image rm multi-tenant-saas-platform-frontend -ErrorAction SilentlyContinue
docker image rm multi-tenant-saas-platform-backend -ErrorAction SilentlyContinue

docker compose build --no-cache
docker compose up -d
```

2. **Wait for database to initialize** (~30 seconds):
```bash
docker compose ps
# All services should show "Up" or "Healthy"
```

3. **Verify backend health**:
```bash
curl http://localhost:5000/api/health
# Response: {"success":true,"data":{"status":"ok","database":"connected"}}
```

4. **Use correct demo credentials** (seeded by Flyway):
   - **Super Admin**: `superadmin@system.com` / `Admin@123`
   - **Tenant Admin**: `admin@demo.com` / `Demo@123`
   - **Demo User**: `user1@demo.com` / `User@123`

5. **Clear browser storage** and refresh:
   - Open DevTools → Application → Local Storage → delete `token`
   - Refresh `http://localhost:3000`

**If Still Failing:**

- Check backend logs: `docker compose logs backend -n 200`
- Check frontend logs: `docker compose logs frontend -n 200`
- Verify database is healthy: `docker compose logs database -n 50`
- Ensure you're on the latest commit: `git log --oneline -1`

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Sanjay Sahoo** - *Initial work* - [sanjaysahoo21](https://github.com/sanjaysahoo21)

## 🙏 Acknowledgments

- Spring Boot team for the amazing framework
- React team for the frontend library
- PostgreSQL community for the reliable database
- All contributors and testers

## 📞 Support

For support, email sanjaysahoo2101@gmail.com or open an issue in the GitHub repository.

## 🔄 Version History

- **v1.0.0** (Current) - Initial release with full multi-tenant capabilities
  - 19 REST API endpoints
  - JWT authentication
  - Role-based access control
  - 3 subscription tiers
  - Docker support
  - Complete documentation

## 🎯 Roadmap

- [ ] Add email notifications
- [ ] Implement real-time updates with WebSockets
- [ ] Add file upload/download for tasks
- [ ] Implement dashboard analytics
- [ ] Add API rate limiting
- [ ] Implement OAuth2 social login
- [ ] Add multi-language support
- [ ] Create mobile app (React Native)
- [ ] Add Kubernetes deployment configs
- [ ] Implement comprehensive logging

---

**Built with ❤️ using Spring Boot and React**
