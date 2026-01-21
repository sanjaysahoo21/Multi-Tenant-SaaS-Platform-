# Hybrid Deployment Guide

This guide explains how to deploy the **Backend on Coolify** while keeping the **Frontend on Vercel**.

## Architecture

* **Frontend**: Hosted on Vercel (Existing URL).
* **Backend & Database**: Hosted on your self-hosted Coolify instance.

---

## Part 1: Deploy Backend to Coolify

### 1. Create Backend Project in Coolify

1. Open Coolify Dashboard.
2. Create a **New Project** -> **Production**.
3. Add **New Resource** -> **Git Repository**.
4. Select this repository (`sanjaysahoo21/Multi-Tenant-SaaS-Platform-`).
5. **Important**: When configuring the build, you only want to deploy the backend and database here.

### 2. Configure Docker Compose for Backend Only

Since we are NOT hosting the frontend on Coolify, we need to tell Coolify to ignore the frontend container.

1. In Coolify, under **Configuration** > **Docker Compose**, paste this modified configuration:

```yaml
services:
  database:
    image: postgres:16-alpine
    container_name: workstack-database
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-saasdb}
      POSTGRES_USER: ${POSTGRES_USER:-saasuser}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-saasuser} -d ${POSTGRES_DB:-saasdb}"]
      interval: 10s
      retries: 5
    networks:
      - saas-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: workstack-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://database:5432/${POSTGRES_DB:-saasdb}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-saasuser}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      # IMPORTANT: Set this to your Vercel URL
      FRONTEND_URL: ${FRONTEND_URL}
      SERVER_PORT: 5000
    ports:
      - "5000:5000"
    depends_on:
      database:
        condition: service_healthy
    networks:
      - saas-network

volumes:
  postgres_data:

networks:
  saas-network:
    driver: bridge
```

### 3. Set Environment Variables (Coolify)

Go to the **Environment Variables** tab in Coolify and set these:

| Variable | Value |
| :--- | :--- |
| `POSTGRES_PASSWORD` | (Generate a strong password) |
| `JWT_SECRET` | (Generate a secure random string) |
| `FRONTEND_URL` | **`https://your-app-name.vercel.app`** (Your Vercel URL) |

### 4. Deploy & Get URL

1. Click **Deploy**.
2. Once deployed, go to the **Settings** of the `backend` service.
3. Note down the **Domain/URL** (e.g., `https://api.coolify.me`).

---

## Part 2: Connect Vercel Frontend

Now we point your existing Vercel deployment to this new backend.

1. Go to your **Vercel Dashboard**.
2. Select your Project -> **Settings** -> **Environment Variables**.
3. Add/Update the following variable:
    * **Key**: `VITE_API_BASE_URL`
    * **Value**: `https://api.coolify.me/api` (The URL you got from Coolify + `/api`)
4. **Redeploy** your Vercel project (Go to Deployments -> Redeploy) for changes to take effect.

---

## Summary

* **Coolify**: Runs Backend + DB. It knows to accept requests from Vercel because of `FRONTEND_URL`.
* **Vercel**: Runs Frontend. It knows to send requests to Coolify because of `VITE_API_BASE_URL`.
