# Fix Render Deployment

This guide explains how to correctly deploy the Backend to Render using the new configuration.

## 1. Push Changes

First, push the new `render.yaml` file to your GitHub repository:

```bash
git add render.yaml
git commit -m "Add render.yaml configuration"
git push
```

## 2. Deploy on Render

1. Go to your [Render Dashboard](https://dashboard.render.com/).
2. Click **New +** and select **Blueprint**.
3. Connect your repository (`sanjaysahoo21/Multi-Tenant-SaaS-Platform-`).
4. Render will automatically detect the `render.yaml` file.
5. Click **Apply** or **Create Service**.

## 3. Post-Deployment Configuration

Once the services are created:

1. **Get the Backend URL**:
    * Find the `workstack-backend` service in your dashboard.
    * Copy the URL (e.g., `https://workstack-backend.onrender.com`).

2. **Update Frontend (Vercel)**:
    * Go to Vercel -> Your Project -> Settings -> Environment Variables.
    * Update `VITE_API_BASE_URL` to the Backend URL (append `/api` if needed, e.g., `https://workstack-backend.onrender.com/api`).
    * **Redeploy** the Frontend.

3. **Update Backend (Render)**:
    * In Render, go to the `workstack-backend` service -> **Environment**.
    * Update the `FRONTEND_URL` variable to your actual Vercel URL (e.g., `https://your-app.vercel.app`).
    * Render will restart the service automatically.

## Troubleshooting

* **Database Error**: If the app fails to start, check the logs. auto-configured `SPRING_DATASOURCE_URL` should work, but verify the Internal DB URL is correct.
* **CORS Error**: Ensure `FRONTEND_URL` in Render matches *exactly* your Vercel URL (no trailing slash).
