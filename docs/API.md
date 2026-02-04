# API Documentation

## Overview

The WorkStack API provides a comprehensive RESTful interface for multi-tenant SaaS project management. All endpoints (except Auth/Health) require JWT authentication.

**Base URL:** `http://localhost:5000/api`

**Authentication Scheme:** `Bearer <token>`

---

## 1. Authentication & System

### 1.1 Register Tenant
**POST** `/auth/register`
Create a new tenant workspace and admin account.

*   **Request Body**:
    ```json
    {
      "tenantName": "Acme Corp",
      "subdomain": "acme",
      "adminEmail": "admin@acme.com",
      "adminPassword": "Password123!",
      "adminFullName": "Admin User"
    }
    ```
*   **Response (201 Created)**:
    ```json
    {
      "success": true,
      "data": { "tenantId": "uuid", "token": "jwt-token" }
    }
    ```
*   **Errors**: `400 Bad Request`, `409 Conflict` (Email/Subdomain exists).

### 1.2 Login
**POST** `/auth/login`
Authenticate user using email and password.

*   **Request Body**:
    ```json
    {
      "email": "user@acme.com",
      "password": "Password123!",
      "tenantSubdomain": "acme"
    }
    ```
*   **Response (200 OK)**:
    ```json
    {
      "success": true,
      "data": { "token": "jwt-token", "user": { ... } }
    }
    ```
*   **Errors**: `401 Unauthorized`.

### 1.3 Validate Token
**GET** `/auth/validate`
Check if current token is valid.

*   **Response (200 OK)**: `{"valid": true}`
*   **Errors**: `401 Unauthorized`.

### 1.4 Get Current User
**GET** `/auth/me`
Get details of the currently logged-in user.

*   **Response (200 OK)**: User profile object.
*   **Errors**: `401 Unauthorized`.

### 1.5 Logout
**POST** `/auth/logout`
Invalidate current session (client-side only for JWT).

*   **Response (200 OK)**: `{"message": "Logged out"}`

### 1.6 Health Check
**GET** `/health`
System and database health status.

*   **Response (200 OK)**: `{"status": "UP", "database": "CONNECTED"}`

---

## 2. Tenant Management

### 2.1 Get All Tenants (Super Admin)
**GET** `/tenants`
List all registered tenants.

*   **Headers**: `Authorization: Bearer <token>`
*   **Response (200 OK)**: List of tenants.
*   **Errors**: `403 Forbidden` (non-super-admin).

### 2.2 Get Tenant Details
**GET** `/tenants/{tenantId}`
Get detailed profile of a specific tenant.

*   **Response (200 OK)**: Tenant object.
*   **Errors**: `403 Forbidden` (accessing other tenant), `404 Not Found`.

### 2.3 Update Tenant
**PUT** `/tenants/{tenantId}`
Update tenant settings (Plan, Name).

*   **Request Body**: `{"name": "New Name", "subscriptionPlan": "PRO"}`
*   **Response (200 OK)**: Updated tenant object.
*   **Errors**: `403 Forbidden` (non-admin).

### 2.4 Delete Tenant (Super Admin)
**DELETE** `/tenants/{tenantId}`
Permanently remove a tenant and its data.

*   **Response (204 No Content)**
*   **Errors**: `403 Forbidden`, `404 Not Found`.

---

## 3. User Management

### 3.1 Get All Users
**GET** `/users`
List users belonging to the current tenant.

*   **Response (200 OK)**: List of users.

### 3.2 Get User Details
**GET** `/users/{userId}`
Get specific user profile.

*   **Response (200 OK)**: User object.
*   **Errors**: `404 Not Found`.

### 3.3 Invite User
**POST** `/users/invite`
Add a new user to the tenant.

*   **Request Body**:
    ```json
    { "email": "brandont@acme.com", "fullName": "Brandon T", "role": "USER" }
    ```
*   **Response (201 Created)**: User object with status `INVITED`.
*   **Errors**: `409 Conflict` (User exists).

### 3.4 Update User
**PUT** `/users/{userId}`
Update user role or details.

*   **Request Body**: `{"role": "TENANT_ADMIN"}`
*   **Response (200 OK)**: Updated user.

### 3.5 Delete User
**DELETE** `/users/{userId}`
Remove a user from the tenant.

*   **Response (204 No Content)**

---

## 4. Project Management

### 4.1 Get All Projects
**GET** `/projects`
List projects for the current tenant.

*   **Response (200 OK)**: List of projects.

### 4.2 Create Project
**POST** `/projects`
Create a new project workspace.

*   **Request Body**:
    ```json
    { "name": "Q4 Goals", "description": "...", "priority": "HIGH" }
    ```
*   **Response (201 Created)**: Project object.

### 4.3 Update Project
**PUT** `/projects/{projectId}`
Update project status or details.

*   **Request Body**: `{"status": "COMPLETED"}`
*   **Response (200 OK)**: Updated project.

### 4.4 Delete Project
**DELETE** `/projects/{projectId}`
Delete a project and its tasks.

*   **Response (204 No Content)**

---

## 5. Task Management

### 5.1 Get All Tasks
**GET** `/tasks`
List tasks (can filter by projectId).

*   **Query Params**: `?projectId=...`
*   **Response (200 OK)**: List of tasks.

### 5.2 Create Task
**POST** `/tasks` (or `/projects/{projectId}/tasks`)
Add a task to a project.

*   **Request Body**:
    ```json
    { "title": "Design Logo", "projectId": "uuid", "priority": "MEDIUM" }
    ```
*   **Response (201 Created)**: Task object.

### 5.3 Update Task
**PUT** `/tasks/{taskId}`
Update status or assignment.

*   **Request Body**: `{"status": "DONE"}`
*   **Response (200 OK)**: Updated task.

### 5.4 Delete Task
**DELETE** `/tasks/{taskId}`
Remove a task.

*   **Response (204 No Content)**
