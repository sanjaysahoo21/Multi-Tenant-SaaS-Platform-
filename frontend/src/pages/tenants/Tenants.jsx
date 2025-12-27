import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import Navbar from '../../components/Navbar';
import api from '../../api/axios';
import './Tenants.css';

const PLAN_OPTIONS = ['FREE', 'PRO', 'ENTERPRISE'];

function Tenants() {
  const { user } = useAuth();
  const [tenants, setTenants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [savingTenantId, setSavingTenantId] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (user?.role === 'SUPER_ADMIN') {
      loadTenants();
    }
  }, [user]);

  const loadTenants = async () => {
    try {
      setLoading(true);
      setError('');
      setSuccess('');
      const response = await api.get('/tenants');
      const data = response?.data?.data?.tenants;
      setTenants(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to load tenants:', err);
      setError(err.response?.data?.message || 'Failed to load tenants');
    } finally {
      setLoading(false);
    }
  };

  const handlePlanChange = (tenantId, newPlan) => {
    setTenants((prev) => prev.map((t) => (t.id === tenantId ? { ...t, subscriptionPlan: newPlan } : t)));
  };

  const handleSavePlan = async (tenant) => {
    try {
      setSavingTenantId(tenant.id);
      setError('');
      setSuccess('');
      await api.put(`/tenants/${tenant.id}`, { subscriptionPlan: tenant.subscriptionPlan });
      setSuccess(`Updated ${tenant.name} to ${tenant.subscriptionPlan} plan`);
      await loadTenants();
    } catch (err) {
      console.error('Failed to update plan:', err);
      setError(err.response?.data?.message || 'Failed to update subscription plan');
    } finally {
      setSavingTenantId('');
    }
  };

  if (user?.role !== 'SUPER_ADMIN') {
    return (
      <div className="page-container">
        <Navbar />
        <div className="content">
          <div className="error-message">You don't have permission to access this page.</div>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="page-container">
        <Navbar />
        <div className="content">
          <div className="loading-card">Loading tenants...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <Navbar />
      <div className="content">
        <div className="page-header">
          <h1>Tenants</h1>
        </div>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <div className="tenants-table-container">
          <table className="tenants-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Subdomain</th>
                <th>Status</th>
                <th>Plan</th>
                <th>Users</th>
                <th>Projects</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {tenants.map((tenant) => (
                <tr key={tenant.id}>
                  <td>{tenant.name}</td>
                  <td>{tenant.subdomain}</td>
                  <td>
                    <span className={`status-badge ${(tenant.status || '').toLowerCase()}`}>
                      {tenant.status}
                    </span>
                  </td>
                  <td>
                    <select
                      value={tenant.subscriptionPlan}
                      onChange={(e) => handlePlanChange(tenant.id, e.target.value)}
                    >
                      {PLAN_OPTIONS.map((plan) => (
                        <option key={plan} value={plan}>{plan}</option>
                      ))}
                    </select>
                  </td>
                  <td>{tenant.stats?.totalUsers ?? tenant.maxUsers}</td>
                  <td>{tenant.stats?.totalProjects ?? tenant.maxProjects}</td>
                  <td>
                    <button
                      className="btn btn-primary"
                      onClick={() => handleSavePlan(tenant)}
                      disabled={savingTenantId === tenant.id}
                    >
                      {savingTenantId === tenant.id ? 'Saving...' : 'Save Plan'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default Tenants;
