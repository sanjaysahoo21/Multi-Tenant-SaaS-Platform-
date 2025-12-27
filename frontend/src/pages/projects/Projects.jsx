import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../components/Navbar';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import './Projects.css';

function Projects() {
  const { user } = useAuth();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editingProject, setEditingProject] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    status: 'ACTIVE'
  });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = async () => {
    try {
      setError('');
      setLoading(true);
      const response = await api.get('/projects');
      console.log('Projects loaded:', response.data.data);
      setProjects(Array.isArray(response.data.data) ? response.data.data : []);
    } catch (error) {
      console.error('Failed to load projects:', error);
      setError('Failed to load projects');
      setProjects([]);
    } finally {
      setLoading(false);
    }
  };

  const isAdmin = user?.role === 'TENANT_ADMIN' || user?.role === 'SUPER_ADMIN';

  const handleOpenModal = (project = null) => {
    if (!isAdmin) return;
    if (project) {
      setEditingProject(project);
      setFormData({
        name: project.name,
        description: project.description || '',
        status: project.status
      });
    } else {
      setEditingProject(null);
      setFormData({ name: '', description: '', status: 'ACTIVE' });
    }
    setShowModal(true);
    setError('');
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingProject(null);
    setFormData({ name: '', description: '', status: 'ACTIVE' });
    setError('');
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      if (!isAdmin) {
        setError('You are not authorized to manage projects');
        return;
      }
      if (editingProject) {
        await api.put(`/projects/${editingProject.id}`, formData);
      } else {
        await api.post('/projects', formData);
      }
      console.log('Project saved successfully');
      handleCloseModal();
      // Reload projects after closing modal to ensure fresh data
      await loadProjects();
    } catch (err) {
      console.error('Submit error:', err);
      setError(err.response?.data?.message || err.message || 'Operation failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this project?')) {
      return;
    }

    try {
      if (!isAdmin) {
        alert('You are not authorized to delete projects');
        return;
      }
      await api.delete(`/projects/${id}`);
      await loadProjects();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete project');
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <Navbar />
        <div className="content">
          <div className="loading-card">Loading projects...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <Navbar />
      <div className="content">
        <div className="page-header">
          <h1>Projects</h1>
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => handleOpenModal()}>
              + New Project
            </button>
          )}
        </div>

        {error && <div className="error-message">{error}</div>}

        {projects.length === 0 ? (
          <div className="empty-state">
            <p>No projects yet. Create your first project!</p>
            <button className="btn btn-primary" onClick={() => handleOpenModal()}>
              Create Project
            </button>
          </div>
        ) : (
          <div className="projects-grid">
            {projects.map((project) => (
              <div key={project.id} className="project-card">
                <div className="project-header">
                  <h3>{project.name}</h3>
                  <span className={`status-badge ${(project.status || '').toLowerCase()}`}>
                    {project.status || 'UNKNOWN'}
                  </span>
                </div>
                <p className="project-description">
                  {project.description || 'No description'}
                </p>
                <div className="project-footer">
                  <button
                    className="btn btn-secondary"
                    onClick={() => navigate(`/projects/${project.id}`)}
                  >
                    View Details
                  </button>
                  {isAdmin && (
                    <div className="project-actions">
                      <button
                        className="btn-icon"
                        onClick={() => handleOpenModal(project)}
                        title="Edit"
                      >
                        ✏️
                      </button>
                      <button
                        className="btn-icon"
                        onClick={() => handleDelete(project.id)}
                        title="Delete"
                      >
                        🗑️
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        {showModal && (
          <div className="modal-overlay" onClick={handleCloseModal}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <h2>{editingProject ? 'Edit Project' : 'Create New Project'}</h2>
              {error && <div className="error-message">{error}</div>}
              
              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label htmlFor="name">Project Name *</label>
                  <input
                    type="text"
                    id="name"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="description">Description</label>
                  <textarea
                    id="description"
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    rows="4"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="status">Status</label>
                  <select
                    id="status"
                    name="status"
                    value={formData.status}
                    onChange={handleChange}
                  >
                    <option value="ACTIVE">Active</option>
                    <option value="COMPLETED">Completed</option>
                    <option value="ARCHIVED">Archived</option>
                  </select>
                </div>

                <div className="modal-actions">
                  <button type="button" className="btn btn-secondary" onClick={handleCloseModal} disabled={submitting}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting ? 'Processing...' : (editingProject ? 'Update' : 'Create')}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Projects;
