import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Auth.css';

function Register() {
  const [formData, setFormData] = useState({
    tenantName: '',
    subdomain: '',
    subscriptionPlan: 'FREE',
    adminEmail: '',
    adminPassword: '',
    adminFullName: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { registerTenant } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await registerTenant(formData);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1><span className="logo-mark">WS</span> WorkStack</h1>
        <h2>Create Your Tenant</h2>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="tenantName">Company Name</label>
            <input
              type="text"
              id="tenantName"
              name="tenantName"
              value={formData.tenantName}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="subdomain">Subdomain</label>
            <input
              type="text"
              id="subdomain"
              name="subdomain"
              value={formData.subdomain}
              onChange={handleChange}
              placeholder="your-company"
              required
              disabled={loading}
            />
            <small style={{ color: 'var(--text-secondary)' }}>Your URL will be: your-company.saas.com</small>
          </div>

          <div className="form-group">
            <label htmlFor="subscriptionPlan">Subscription Plan</label>
            <select
              id="subscriptionPlan"
              name="subscriptionPlan"
              value={formData.subscriptionPlan}
              onChange={handleChange}
              disabled={loading}
            >
              <option value="FREE">Free (5 users, 3 projects)</option>
              <option value="PRO">Pro (25 users, 15 projects)</option>
              <option value="ENTERPRISE">Enterprise (100 users, 50 projects)</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="adminFullName">Your Name</label>
            <input
              type="text"
              id="adminFullName"
              name="adminFullName"
              value={formData.adminFullName}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="adminEmail">Your Email</label>
            <input
              type="email"
              id="adminEmail"
              name="adminEmail"
              value={formData.adminEmail}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="adminPassword">Password</label>
            <input
              type="password"
              id="adminPassword"
              name="adminPassword"
              value={formData.adminPassword}
              onChange={handleChange}
              required
              minLength="6"
              disabled={loading}
            />
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating...' : 'Create Tenant'}
          </button>
        </form>

        <div className="auth-footer">
          <p>Already have an account? <Link to="/login">Sign in</Link></p>
        </div>
      </div>
    </div>
  );
}

export default Register;
