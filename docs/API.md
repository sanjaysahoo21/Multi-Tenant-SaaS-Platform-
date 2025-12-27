# API Documentation

## Overview

The WorkStack API provides a comprehensive RESTful interface for multi-tenant SaaS project management and task tracking. All endpoints require JWT authentication (except login/register) and enforce role-based access control (RBAC).

**Base URL:** `http://localhost:5000/api`

**Authentication:** Include JWT token in Authorization header:
```
Authorization: Bearer {token}
```

---

## Authentication & Authorization

### 1. Register New Tenant

**Endpoint:** `POST /auth/register`

**Description:** Create a new tenant account with admin user

**Request Body:**
```json
{
  "tenantName": "Acme Corporation",
  "email": "admin@acme.com",
  "password": "SecurePassword123!",
  "fullName": "John Doe"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Tenant created successfully",
  "data": {
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "userId": "660e8400-e29b-41d4-a716-446655440000",
    "email": "admin@acme.com",
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbkBhY21lLmNvbSIsImlhdCI6MTcwMzY3MzI4NCwiZXhwIjoxNzAzNzU5Njg0fQ...",
    "expiresIn": 86400,
    "role": "TENANT_ADMIN"
  }
}
```

**Error Responses:**
```json
// 400 Bad Request
{
  "success": false,
  "message": "Tenant name is required"
}

// 409 Conflict
{
  "success": false,
  "message": "Email already exists"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:5000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "tenantName": "Acme Corp",
    "email": "admin@acme.com",
    "password": "SecurePass123!",
    "fullName": "John Doe"
  }'
```

---

### 2. Login

**Endpoint:** `POST /auth/login`

**Description:** Authenticate user and receive JWT token

**Request Body:**
```json
{
  "email": "user@company.com",
  "password": "UserPassword123!"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@company.com",
    "fullName": "Jane Smith",
    "tenantId": "660e8400-e29b-41d4-a716-446655440000",
    "tenantName": "Acme Corporation",
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "expiresIn": 86400,
    "role": "USER"
  }
}
```

**Error Responses:**
```json
// 401 Unauthorized
{
  "success": false,
  "message": "Invalid credentials"
}

// 404 Not Found
{
  "success": false,
  "message": "User not found"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@company.com",
    "password": "UserPassword123!"
  }'
```

---

### 3. Validate Token

**Endpoint:** `GET /auth/validate`

**Description:** Verify JWT token validity

**Headers Required:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Token is valid",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@company.com",
    "role": "USER",
    "tenantId": "660e8400-e29b-41d4-a716-446655440000",
    "expiresAt": 1703759684
  }
}
```

**Error Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Token is invalid or expired"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:5000/api/auth/validate \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

---

## Tenant Management

### 4. Get All Tenants (Super Admin Only)

**Endpoint:** `GET /tenants`

**Description:** Retrieve all tenants in the system

**Headers Required:**
```
Authorization: Bearer {token}
Role Required: SUPER_ADMIN
```

**Query Parameters:**
```
page=0
size=20
sort=createdAt,desc
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Tenants retrieved successfully",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Acme Corporation",
      "subdomain": "acme-corp",
      "subscriptionPlan": "PROFESSIONAL",
      "status": "ACTIVE",
      "maxUsers": 50,
      "maxProjects": 20,
      "userCount": 12,
      "projectCount": 5,
      "createdAt": "2025-12-20T10:30:00Z",
      "updatedAt": "2025-12-27T14:15:00Z"
    }
  ]
}
```

**Error Response (403 Forbidden):**
```json
{
  "success": false,
  "message": "Only SUPER_ADMIN can access this resource"
}
```

---

### 5. Get Tenant Details

**Endpoint:** `GET /tenants/{tenantId}`

**Description:** Retrieve specific tenant details

**Headers Required:**
```
Authorization: Bearer {token}
Role Required: TENANT_ADMIN
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Tenant details retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Acme Corporation",
    "subdomain": "acme-corp",
    "subscriptionPlan": "PROFESSIONAL",
    "status": "ACTIVE",
    "maxUsers": 50,
    "maxProjects": 20,
    "userCount": 12,
    "projectCount": 5,
    "createdAt": "2025-12-20T10:30:00Z",
    "updatedAt": "2025-12-27T14:15:00Z"
  }
}
```

---

### 6. Update Tenant

**Endpoint:** `PUT /tenants/{tenantId}`

**Description:** Update tenant settings

**Headers Required:**
```
Authorization: Bearer {token}
Role Required: TENANT_ADMIN
```

**Request Body:**
```json
{
  "name": "Acme Corp Updated",
  "subscriptionPlan": "ENTERPRISE",
  "status": "ACTIVE"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Tenant updated successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Acme Corp Updated",
    "subscriptionPlan": "ENTERPRISE",
    "status": "ACTIVE",
    "updatedAt": "2025-12-27T14:15:00Z"
  }
}
```

---

### 7. Delete Tenant (Super Admin Only)

**Endpoint:** `DELETE /tenants/{tenantId}`

**Description:** Delete tenant and all associated data

**Headers Required:**
```
Authorization: Bearer {token}
Role Required: SUPER_ADMIN
```

**Response (204 No Content)**

**Error Response (404 Not Found):**
```json
{
  "success": false,
  "message": "Tenant not found"
}
```

---

## User Management

### 8. Get All Users (Tenant Level)

**Endpoint:** `GET /users`

**Description:** Retrieve all users in current tenant

**Headers Required:**
```
Authorization: Bearer {token}
Role Required: TENANT_ADMIN or USER
```

**Query Parameters:**
```
page=0
size=20
role=USER
status=ACTIVE
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "john.doe@acme.com",
      "fullName": "John Doe",
      "role": "TENANT_ADMIN",
      "status": "ACTIVE",
      "lastLogin": "2025-12-27T12:00:00Z",
      "createdAt": "2025-12-20T10:30:00Z"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440000",
      "email": "jane.smith@acme.com",
      "fullName": "Jane Smith",
      "role": "USER",
      "status": "ACTIVE",
      "lastLogin": "2025-12-27T11:30:00Z",
      "createdAt": "2025-12-21T09:15:00Z"
    }
  ]
}
```

---

### 9. Get User Details

**Endpoint:** `GET /users/{userId}`

**Description:** Retrieve specific user details

**Headers Required:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john.doe@acme.com",
    "fullName": "John Doe",
    "role": "TENANT_ADMIN",
    "status": "ACTIVE",
    "tenantId": "770e8400-e29b-41d4-a716-446655440000",
    "lastLogin": "2025-12-27T12:00:00Z",
    "createdAt": "2025-12-20T10:30:00Z",
    "updatedAt": "2025-12-27T14:15:00Z"
  }
}
```

---

### 10. Invite User to Tenant

**Endpoint:** `POST /users/invite`

**Description:** Invite new user to tenant

**Headers Required:**
```
Authorization: Bearer {token}
Role Required: TENANT_ADMIN
```

**Request Body:**
```json
{
  "email": "newuser@acme.com",
  "fullName": "New User",
  "role": "USER"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "User invited successfully. Email sent to newuser@acme.com",
  "data": {
    "id": "880e8400-e29b-41d4-a716-446655440000",
    "email": "newuser@acme.com",
    "fullName": "New User",
    "role": "USER",
    "status": "INVITED",
    "invitationToken": "invite_token_xxx",
    "createdAt": "2025-12-27T14:15:00Z"
  }
}
```

---

### 11. Update User

**Endpoint:** `PUT /users/{userId}`

**Description:** Update user information

**Headers Required:**
```
Authorization: Bearer {token}
Role Required: TENANT_ADMIN or own user
```

**Request Body:**
```json
{
  "fullName": "Updated Name",
  "role": "USER",
  "status": "ACTIVE"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john.doe@acme.com",
    "fullName": "Updated Name",
    "role": "USER",
    "status": "ACTIVE",
    "updatedAt": "2025-12-27T14:30:00Z"
  }
}
```

---

## Project Management

### 12. Get All Projects

**Endpoint:** `GET /projects`

**Description:** Retrieve all projects in current tenant

**Headers Required:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
```
page=0
size=20
status=ACTIVE
sort=createdAt,desc
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Projects retrieved successfully",
  "data": [
    {
      "id": "990e8400-e29b-41d4-a716-446655440000",
      "name": "Website Redesign",
      "description": "Complete redesign of company website",
      "status": "IN_PROGRESS",
      "owner": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "email": "john.doe@acme.com",
        "fullName": "John Doe"
      },
      "memberCount": 5,
      "taskCount": 12,
      "dueDate": "2026-03-31",
      "priority": "HIGH",
      "createdAt": "2025-12-20T10:30:00Z",
      "updatedAt": "2025-12-27T14:15:00Z"
    }
  ]
}
```

---

### 13. Create Project

**Endpoint:** `POST /projects`

**Description:** Create new project

**Headers Required:**
```
Authorization: Bearer {token}
Role Required: TENANT_ADMIN or USER
```

**Request Body:**
```json
{
  "name": "Mobile App Development",
  "description": "Native iOS and Android app",
  "dueDate": "2026-06-30",
  "priority": "HIGH"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Project created successfully",
  "data": {
    "id": "aa0e8400-e29b-41d4-a716-446655440000",
    "name": "Mobile App Development",
    "description": "Native iOS and Android app",
    "status": "ACTIVE",
    "dueDate": "2026-06-30",
    "priority": "HIGH",
    "taskCount": 0,
    "memberCount": 1,
    "createdAt": "2025-12-27T14:15:00Z"
  }
}
```

---

### 14. Update Project

**Endpoint:** `PUT /projects/{projectId}`

**Description:** Update project details

**Headers Required:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "name": "Mobile App Development v2",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Project updated successfully",
  "data": {
    "id": "aa0e8400-e29b-41d4-a716-446655440000",
    "name": "Mobile App Development v2",
    "status": "IN_PROGRESS",
    "priority": "MEDIUM",
    "updatedAt": "2025-12-27T14:30:00Z"
  }
}
```

---

## Task Management

### 15. Get All Tasks

**Endpoint:** `GET /tasks`

**Description:** Retrieve all tasks in current tenant

**Headers Required:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
```
projectId=aa0e8400-e29b-41d4-a716-446655440000
status=TODO
assignedTo=550e8400-e29b-41d4-a716-446655440000
page=0
size=20
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Tasks retrieved successfully",
  "data": [
    {
      "id": "bb0e8400-e29b-41d4-a716-446655440000",
      "title": "Design UI mockups",
      "description": "Create wireframes and mockups for app screens",
      "status": "TODO",
      "priority": "HIGH",
      "projectId": "aa0e8400-e29b-41d4-a716-446655440000",
      "assignedTo": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "email": "john.doe@acme.com",
        "fullName": "John Doe"
      },
      "dueDate": "2026-01-15",
      "createdAt": "2025-12-20T10:30:00Z",
      "updatedAt": "2025-12-27T14:15:00Z"
    }
  ]
}
```

---

### 16. Create Task

**Endpoint:** `POST /tasks`

**Description:** Create new task

**Headers Required:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "title": "Implement authentication",
  "description": "Add JWT authentication to API",
  "projectId": "aa0e8400-e29b-41d4-a716-446655440000",
  "assignedTo": "550e8400-e29b-41d4-a716-446655440000",
  "dueDate": "2026-01-20",
  "priority": "HIGH"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Task created successfully",
  "data": {
    "id": "bb0e8400-e29b-41d4-a716-446655440000",
    "title": "Implement authentication",
    "description": "Add JWT authentication to API",
    "status": "TODO",
    "priority": "HIGH",
    "projectId": "aa0e8400-e29b-41d4-a716-446655440000",
    "assignedTo": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "John Doe"
    },
    "dueDate": "2026-01-20",
    "createdAt": "2025-12-27T14:15:00Z"
  }
}
```

---

### 17. Update Task

**Endpoint:** `PUT /tasks/{taskId}`

**Description:** Update task details

**Headers Required:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "title": "Implement JWT authentication",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM",
  "assignedTo": "660e8400-e29b-41d4-a716-446655440000"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Task updated successfully",
  "data": {
    "id": "bb0e8400-e29b-41d4-a716-446655440000",
    "title": "Implement JWT authentication",
    "status": "IN_PROGRESS",
    "priority": "MEDIUM",
    "updatedAt": "2025-12-27T14:30:00Z"
  }
}
```

---

### 18. Delete Task

**Endpoint:** `DELETE /tasks/{taskId}`

**Description:** Delete task

**Headers Required:**
```
Authorization: Bearer {token}
```

**Response (204 No Content)**

---

## Health & System

### 19. Health Check

**Endpoint:** `GET /health`

**Description:** Check application and database health

**Headers Required:** None (public endpoint)

**Response (200 OK):**
```json
{
  "status": "UP",
  "database": "CONNECTED",
  "jwt_validation": "WORKING",
  "timestamp": "2025-12-27T14:35:00Z",
  "uptime": 3600000,
  "version": "1.0.0"
}
```

**cURL Example:**
```bash
curl http://localhost:5000/api/health
```

---

## Error Handling

All endpoints return consistent error responses with HTTP status codes:

### HTTP Status Codes

- **200 OK:** Successful request
- **201 Created:** Resource created
- **204 No Content:** Successful deletion
- **400 Bad Request:** Invalid input
- **401 Unauthorized:** Missing or invalid token
- **403 Forbidden:** Insufficient permissions
- **404 Not Found:** Resource not found
- **409 Conflict:** Resource already exists
- **500 Internal Server Error:** Server error

### Error Response Format

```json
{
  "success": false,
  "message": "Error description",
  "error": {
    "code": "ERROR_CODE",
    "details": "Additional error details"
  }
}
```

---

## Rate Limiting

- Requests per minute: 60 per user
- Concurrent requests: 10 per user
- Request timeout: 30 seconds

---

## Pagination

All list endpoints support pagination with these parameters:

```
page=0          # Zero-indexed page number
size=20         # Items per page (max 100)
sort=field,asc  # Sort field and direction (asc/desc)
```

Example:
```bash
curl http://localhost:5000/api/projects?page=0&size=20&sort=createdAt,desc \
  -H "Authorization: Bearer {token}"
```

---

## Testing All Endpoints

Use the provided [Postman Collection](./postman-collection.json) or test with cURL:

```bash
# 1. Register tenant
curl -X POST http://localhost:5000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"tenantName":"Test","email":"test@test.com","password":"Pass123!","fullName":"Test User"}'

# 2. Login and capture token
TOKEN=$(curl -s -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Pass123!"}' | jq -r '.data.token')

# 3. Test authenticated endpoints
curl -H "Authorization: Bearer $TOKEN" http://localhost:5000/api/users

# 4. Create project
curl -X POST http://localhost:5000/api/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Project","priority":"HIGH"}'
```

---

## Webhooks (Future)

Future endpoint for webhook subscriptions:
- `POST /webhooks/subscribe` - Subscribe to events
- `POST /webhooks/unsubscribe` - Unsubscribe from events

---

This API documentation covers all 19 endpoints with request/response examples, error handling, and testing guidance for the WorkStack SaaS platform.
