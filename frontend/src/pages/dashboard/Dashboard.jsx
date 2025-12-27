import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Navbar from '../../components/Navbar';
import api from '../../api/axios';
import './Dashboard.css';

function Dashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState({ projects: 0, tasks: 0, users: 0 });
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    if (user) {
      loadStats();
    }
  }, [user]);

  const loadStats = async () => {
    try {
      const [projectsRes, usersRes] = await Promise.all([
        api.get('/projects'),
        user.role === 'TENANT_ADMIN' ? api.get(`/tenants/${user.tenant.id}/users`) : Promise.resolve({ data: { data: [] } })
      ]);

      const projectList = Array.isArray(projectsRes.data.data) ? projectsRes.data.data : [];
      const taskCount = projectList.reduce((sum, p) => sum + (p.taskCount || 0), 0);

      setStats({
        projects: projectList.length,
        tasks: taskCount,
        users: usersRes.data.data.length
      });
    } catch (error) {
      console.error('Failed to load stats:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <Navbar />
      <div className="content">
        <h1>Dashboard</h1>
        <p className="subtitle">Welcome back, {user?.fullName}!</p>

        {loading ? (
          <div className="loading-card">Loading...</div>
        ) : (
          <div className="stats-grid">
            <div className="stat-card" onClick={() => navigate('/projects')}>
              <div className="stat-icon">📁</div>
              <div className="stat-value">{stats.projects}</div>
              <div className="stat-label">Projects</div>
            </div>

            <div className="stat-card">
              <div className="stat-icon">✓</div>
              <div className="stat-value">{stats.tasks}</div>
              <div className="stat-label">Tasks</div>
            </div>

            {user?.role === 'TENANT_ADMIN' && (
              <div className="stat-card" onClick={() => navigate('/users')}>
                <div className="stat-icon">👥</div>
                <div className="stat-value">{stats.users}</div>
                <div className="stat-label">Users</div>
              </div>
            )}

            <div className="stat-card">
              <div className="stat-icon">🏢</div>
              <div className="stat-value">{user?.tenant?.subscriptionPlan}</div>
              <div className="stat-label">Plan</div>
            </div>
          </div>
        )}

        <div className="quick-actions">
          <h2>Quick Actions</h2>
          <div className="actions-grid">
            <button className="action-btn" onClick={() => navigate('/projects')}>
              <span className="action-icon">📁</span>
              <span>View Projects</span>
            </button>
            {user?.role === 'TENANT_ADMIN' && (
              <button className="action-btn" onClick={() => navigate('/users')}>
                <span className="action-icon">👥</span>
                <span>Manage Users</span>
              </button>
            )}
          </div>
        </div>

        <div className="info-section">
          <div className="card">
            <h3>Tenant Information</h3>
            <div className="info-row">
              <span className="info-label">Company:</span>
              <span className="info-value">{user?.tenant?.name}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Subdomain:</span>
              <span className="info-value">{user?.tenant?.subdomain}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Status:</span>
              <span className={`status-badge ${(user?.tenant?.status || '').toLowerCase()}`}>
                {user?.tenant?.status || 'UNKNOWN'}
              </span>
            </div>
            <div className="info-row">
              <span className="info-label">Max Users:</span>
              <span className="info-value">{user?.tenant?.maxUsers}</span>
            </div>
            <div className="info-row">
              <span className="info-label">Max Projects:</span>
              <span className="info-value">{user?.tenant?.maxProjects}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
