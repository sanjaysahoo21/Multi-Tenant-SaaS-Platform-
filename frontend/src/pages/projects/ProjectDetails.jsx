import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Edit2, Trash2 } from 'lucide-react';
import Navbar from '../../components/Navbar';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import './ProjectDetails.css';

function ProjectDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [project, setProject] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    status: 'TODO',
    priority: 'MEDIUM',
    assignedToId: '',
    dueDate: ''
  });
  const [error, setError] = useState('');

  useEffect(() => {
    loadData();
  }, [id]);

  const loadData = async () => {
    try {
      const [projectRes, tasksRes, usersRes] = await Promise.all([
        api.get(`/projects/${id}`),
        api.get(`/projects/${id}/tasks`),
        api.get(`/tenants/${localStorage.getItem('tenantId')}/users`).catch(() => ({ data: { data: [] } }))
      ]);

      const projectData = projectRes.data.data;
      const tasksData = tasksRes.data.data;
      const usersData = usersRes.data.data;

      setProject(projectData);
      setTasks(Array.isArray(tasksData) ? tasksData : (tasksData?.tasks || []));
      setUsers(Array.isArray(usersData) ? usersData : (usersData?.users || []));
    } catch (error) {
      console.error('Failed to load data:', error);
      setError('Failed to load project data');
    } finally {
      setLoading(false);
    }
  };

  const isAdmin = user?.role === 'TENANT_ADMIN' || user?.role === 'SUPER_ADMIN';

  const handleOpenModal = (task = null) => {
    if (!isAdmin) {
      setError('You are not authorized to create or edit tasks');
      return;
    }
    if (task) {
      setEditingTask(task);
      setFormData({
        title: task.title,
        description: task.description || '',
        status: task.status,
        priority: task.priority,
        assignedToId: task.assignedTo?.id || '',
        dueDate: task.dueDate ? task.dueDate.split('T')[0] : ''
      });
    } else {
      setEditingTask(null);
      setFormData({
        title: '',
        description: '',
        status: 'TODO',
        priority: 'MEDIUM',
        assignedToId: '',
        dueDate: ''
      });
    }
    setShowModal(true);
    setError('');
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingTask(null);
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

    try {
      if (!isAdmin) {
        setError('You are not authorized to create or edit tasks');
        return;
      }
      const taskData = {
        ...formData,
        assignedToId: formData.assignedToId || null,
        dueDate: formData.dueDate || null
      };

      if (editingTask) {
        await api.put(`/tasks/${editingTask.id}`, taskData);
      } else {
        await api.post(`/projects/${id}/tasks`, taskData);
      }
      await loadData();
      handleCloseModal();
    } catch (err) {
      setError(err.response?.data?.message || 'Operation failed');
    }
  };

  const handleDeleteTask = async (taskId) => {
    if (!window.confirm('Are you sure you want to delete this task?')) {
      return;
    }

    try {
      if (!isAdmin) {
        alert('You are not authorized to delete tasks');
        return;
      }
      await api.delete(`/tasks/${taskId}`);
      await loadData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete task');
    }
  };

  const handleStatusChange = async (taskId, newStatus) => {
    try {
      await api.patch(`/tasks/${taskId}/status`, { status: newStatus });
      await loadData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update status');
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <Navbar />
        <div className="content">
          <div className="loading-card">Loading...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <Navbar />
      <div className="content">
        <button className="btn btn-secondary" onClick={() => navigate('/projects')}>
          ← Back to Projects
        </button>

        <div className="project-info">
          <div className="project-info-header">
            <h1>{project?.name}</h1>
            <span className={`status-badge ${(project?.status || '').toLowerCase()}`}>
              {project?.status || 'UNKNOWN'}
            </span>
          </div>
          <p>{project?.description || 'No description'}</p>
        </div>

        <div className="page-header">
          <h2>Tasks</h2>
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => handleOpenModal()}>
              + New Task
            </button>
          )}
        </div>

        {error && <div className="error-message">{error}</div>}

        {tasks.length === 0 ? (
          <div className="empty-state">
            <p>No tasks yet. Create your first task!</p>
            <button className="btn btn-primary" onClick={() => handleOpenModal()}>
              Create Task
            </button>
          </div>
        ) : (
          <div className="tasks-columns">
            <div className="task-column">
              <h3>To Do</h3>
              <div className="task-list">
                {tasks.filter(t => t.status === 'TODO').map(task => (
                  <TaskCard 
                    key={task.id} 
                    task={task} 
                    onEdit={isAdmin ? handleOpenModal : null}
                    onDelete={isAdmin ? handleDeleteTask : null}
                    onStatusChange={handleStatusChange}
                  />
                ))}
              </div>
            </div>

            <div className="task-column">
              <h3>In Progress</h3>
              <div className="task-list">
                {tasks.filter(t => t.status === 'IN_PROGRESS').map(task => (
                  <TaskCard 
                    key={task.id} 
                    task={task} 
                    onEdit={isAdmin ? handleOpenModal : null}
                    onDelete={isAdmin ? handleDeleteTask : null}
                    onStatusChange={handleStatusChange}
                  />
                ))}
              </div>
            </div>

            <div className="task-column">
              <h3>Completed</h3>
              <div className="task-list">
                {tasks.filter(t => t.status === 'COMPLETED').map(task => (
                  <TaskCard 
                    key={task.id} 
                    task={task} 
                    onEdit={isAdmin ? handleOpenModal : null}
                    onDelete={isAdmin ? handleDeleteTask : null}
                    onStatusChange={handleStatusChange}
                  />
                ))}
              </div>
            </div>
          </div>
        )}

        {showModal && (
          <div className="modal-overlay" onClick={handleCloseModal}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <h2>{editingTask ? 'Edit Task' : 'Create New Task'}</h2>
              {error && <div className="error-message">{error}</div>}
              
              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label htmlFor="title">Title *</label>
                  <input
                    type="text"
                    id="title"
                    name="title"
                    value={formData.title}
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
                    rows="3"
                  />
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="status">Status</label>
                    <select
                      id="status"
                      name="status"
                      value={formData.status}
                      onChange={handleChange}
                    >
                      <option value="TODO">To Do</option>
                      <option value="IN_PROGRESS">In Progress</option>
                      <option value="COMPLETED">Completed</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label htmlFor="priority">Priority</label>
                    <select
                      id="priority"
                      name="priority"
                      value={formData.priority}
                      onChange={handleChange}
                    >
                      <option value="LOW">Low</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="HIGH">High</option>
                    </select>
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="assignedToId">Assign To</label>
                    <select
                      id="assignedToId"
                      name="assignedToId"
                      value={formData.assignedToId}
                      onChange={handleChange}
                    >
                      <option value="">Unassigned</option>
                      {users.map(user => (
                        <option key={user.id} value={user.id}>
                          {user.fullName}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label htmlFor="dueDate">Due Date</label>
                    <input
                      type="date"
                      id="dueDate"
                      name="dueDate"
                      value={formData.dueDate}
                      onChange={handleChange}
                    />
                  </div>
                </div>

                <div className="modal-actions">
                  <button type="button" className="btn btn-secondary" onClick={handleCloseModal}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary">
                    {editingTask ? 'Update' : 'Create'}
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

function TaskCard({ task, onEdit, onDelete, onStatusChange }) {
  return (
    <div className="task-card">
      <div className="task-header">
        <h4>{task.title}</h4>
        <span className={`priority-badge ${(task.priority || '').toLowerCase()}`}>
          {task.priority || 'UNKNOWN'}
        </span>
      </div>
      
      {task.description && <p className="task-description">{task.description}</p>}
      
      {task.assignedTo && (
        <div className="task-assignee">
          👤 {task.assignedTo.fullName}
        </div>
      )}
      
      {task.dueDate && (
        <div className="task-due-date">
          📅 {new Date(task.dueDate).toLocaleDateString()}
        </div>
      )}
      
      <div className="task-actions">
        <select
          value={task.status}
          onChange={(e) => onStatusChange(task.id, e.target.value)}
          className="status-select"
        >
          <option value="TODO">To Do</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="COMPLETED">Completed</option>
        </select>
        {onEdit && (
          <button className="btn-icon" onClick={() => onEdit(task)} title="Edit">
            <Edit2 size={16} />
          </button>
        )}
        {onDelete && (
          <button className="btn-icon" onClick={() => onDelete(task.id)} title="Delete">
            <Trash2 size={16} />
          </button>
        )}
      </div>
    </div>
  );
}

export default ProjectDetails;
