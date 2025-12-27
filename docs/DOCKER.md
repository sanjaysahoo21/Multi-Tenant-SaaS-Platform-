# WorkStack - Docker Deployment

This document provides instructions for deploying WorkStack using Docker.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose installed (comes with Docker Desktop)
- At least 4GB of RAM available
- Ports 3000, 8080, and 5432 available

## Quick Start

1. **Clone the repository:**
   ```bash
   git clone https://github.com/sanjaysahoo21/Multi-Tenant-SaaS-Platform-.git
   cd Multi-Tenant-SaaS-Platform
   ```

2. **Build and start all services:**
   ```bash
   docker-compose up --build
   ```

3. **Access the application:**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api
   - Database: localhost:5432

## Services

### PostgreSQL Database
- **Image:** postgres:16-alpine
- **Port:** 5432
- **Database:** saasdb
- **Username:** saasuser
- **Password:** saaspass123

### Spring Boot Backend
- **Port:** 8080
- **Health Check:** http://localhost:8080/api/health
- **API Documentation:** See README.md

### React Frontend
- **Port:** 3000 (mapped from internal port 80)
- **Served by:** nginx

## Docker Commands

### Start services in detached mode:
```bash
docker-compose up -d
```

### Stop services:
```bash
docker-compose down
```

### View logs:
```bash
docker-compose logs -f
```

### View logs for specific service:
```bash
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### Rebuild services:
```bash
docker-compose up --build
```

### Remove all containers and volumes:
```bash
docker-compose down -v
```

## Environment Variables

You can customize the deployment by creating a `.env` file:

```env
# Database
POSTGRES_DB=saasdb
POSTGRES_USER=saasuser
POSTGRES_PASSWORD=saaspass123

# JWT
JWT_SECRET=your-very-secure-secret-key-minimum-32-characters-long-for-HS256
JWT_EXPIRATION=86400

# Ports
BACKEND_PORT=8080
FRONTEND_PORT=3000
POSTGRES_PORT=5432
```

## Database Migrations

Database migrations are automatically applied on backend startup using Flyway. The following migrations are included:

1. Create tenants table
2. Create users table
3. Create projects table
4. Create tasks table
5. Create audit_logs table
6. Insert seed data

## Default Credentials

After starting the services, you can login with these demo accounts:

**Super Admin:**
- Email: superadmin@system.com
- Password: Admin@123

**Demo Tenant Admin:**
- Email: admin@demo.com
- Password: Demo@123

**Demo Users:**
- Email: user1@demo.com
- Password: User@123
- Email: user2@demo.com
- Password: User@123

## Health Checks

All services include health checks:

- **Backend:** http://localhost:5000/api/health
- **PostgreSQL:** Automatic pg_isready check

## Troubleshooting

### Backend fails to start:
1. Check if PostgreSQL is healthy: `docker compose ps`
2. View backend logs: `docker compose logs backend`
3. Ensure port 5000 is not in use

### Frontend cannot connect to backend:
1. Check backend health: curl http://localhost:5000/api/health
2. Check nginx configuration in frontend/nginx.conf
3. View frontend logs: `docker compose logs frontend`

### Database connection issues:
1. Verify PostgreSQL is running: `docker compose ps database`
2. Check database logs: `docker compose logs database`
3. Verify credentials in docker-compose.yml

### Port conflicts:
If ports 3000, 5000, or 5432 are already in use, modify the port mappings in docker-compose.yml:
```yaml
ports:
  - "5001:5000"  # Change 5000 to 5001
```

## Production Considerations

For production deployment:

1. **Change all default passwords and secrets**
2. **Use environment variables for sensitive data**
3. **Enable SSL/TLS**
4. **Configure proper logging**
5. **Set up regular database backups**
6. **Use Docker secrets for credentials**
7. **Configure resource limits:**

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

## Backup and Restore

### Backup Database:
```bash
docker exec saas-postgres pg_dump -U saasuser saasdb > backup.sql
```

### Restore Database:
```bash
docker exec -i saas-postgres psql -U saasuser saasdb < backup.sql
```

## Monitoring

Monitor resource usage:
```bash
docker stats
```

Check container status:
```bash
docker-compose ps
```

## Support

For issues and questions:
- GitHub Issues: https://github.com/sanjaysahoo21/Multi-Tenant-SaaS-Platform-/issues
- Email: sanjaysahoo@example.com
