import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="nav-container">
        <Link to="/dashboard" className="nav-brand">
          Multi-Tenant SaaS
        </Link>

        <div className="nav-menu">
          <Link to="/dashboard" className="nav-link">Dashboard</Link>
          <Link to="/projects" className="nav-link">Projects</Link>
          {(user?.role === 'TENANT_ADMIN' || user?.role === 'SUPER_ADMIN') && (
            <Link to="/users" className="nav-link">Users</Link>
          )}
          {user?.role === 'SUPER_ADMIN' && (
            <Link to="/tenants" className="nav-link">Tenants</Link>
          )}
        </div>

        <div className="nav-user">
          <span className="user-info">
            {user?.fullName} ({user?.role})
          </span>
          <button onClick={handleLogout} className="btn btn-secondary">
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
