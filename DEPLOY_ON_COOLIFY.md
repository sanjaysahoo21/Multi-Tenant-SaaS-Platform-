# Deploying to Coolify

This guide explains how to deploy the Multi-Tenant SaaS Platform to your self-hosted Coolify instance.

## Prerequisites

1. A self-hosted **Coolify** instance running.
2. Your GitHub repository connected to Coolify.

## Step-by-Step Deployment

### 1. Create a New Project

1. Open your Coolify Dashboard.
2. Click **+ New Project**.
3. Choose **Production** (or your environment name).
4. Select **+ New Resource**.

### 2. Select Source

1. Choose **Git Repository** (Public or Private depending on your repo).
2. Connect your repository: `sanjaysahoo21/Multi-Tenant-SaaS-Platform-` (or your specific URL).
3. Branch: `main`.

### 3. Configuration

1. **Build Pack**: Select **Docker Compose**.
2. **Docker Compose File**: Look for the field to specify the compose file path.
    * Enter: `docker-compose.prod.yml`
    * *Note: If Coolify asks for the file content directly, copy-paste the content of `docker-compose.prod.yml`.*

### 4. Environment Variables

Go to the **Environment Variables** (Secrets) tab in Coolify and set these values for a production setup:

| Variable | Description | Recommended Value |
| :--- | :--- | :--- |
| `POSTGRES_USER` | Database Username | e.g., `admin_user` |
| `POSTGRES_PASSWORD` | Database Password | **Generate a strong password** |
| `POSTGRES_DB` | Database Name | `saasdb` |
| `JWT_SECRET` | HS256 Secret Key | **Generate a 32+ char random string** |
| `FRONTEND_URL` | URL of your Frontend | `https://your-frontend-domain.com` |
| `VITE_API_BASE_URL` | Backend API URL | `https://your-backend-domain.com/api` |

### 5. Domains

1. **Backend Service**:
    * Go to the Backend service settings.
    * Set **Domains** to `https://api.yourdomain.com` (or similar).
    * Port: `5000`.

2. **Frontend Service**:
    * Go to the Frontend service settings.
    * Set **Domains** to `https://app.yourdomain.com`.
    * Port: `3000`.

### 6. Deploy

Click **Deploy**. Coolify will:

1. Pull your code.
2. Build the Docker images using `docker-compose.prod.yml`.
3. Start the services (Database, Backend, Frontend).
4. Provision SSL certificates automatically for your domains.

## Troubleshooting

* **Database Connectivity**: Ensure the `POSTGRES_PASSWORD` matches in both the Database service and Backend service variables.
* **CORS Errors**: If the frontend cannot call the backend, check that `FRONTEND_URL` in the Backend variables matches your actual Frontend Domain.
* **Build Failures**: Check the "Build Logs" tab in Coolify for detailed errors.
