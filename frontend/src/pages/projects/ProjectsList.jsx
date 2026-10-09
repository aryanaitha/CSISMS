import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import ConfirmDialog from '../../components/ConfirmDialog';

const STATUS_OPTIONS = [
  { value: 'Planning', label: 'Planning' },
  { value: 'In Progress', label: 'In Progress' },
  { value: 'On Hold', label: 'On Hold' },
  { value: 'Completed', label: 'Completed' },
  { value: 'Archived', label: 'Archived' }
];

export default function ProjectsList() {
  const { hasPermission } = useAuth();
  const [projects, setProjects] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  // Create Project Modal
  const [createOpen, setCreateOpen] = useState(false);
  const [createForm, setCreateForm] = useState({
    projectName: '',
    description: '',
    location: '',
    startDate: new Date().toISOString().split('T')[0],
    endDate: '',
    budget: '',
    status: 'Planning',
    progressPercent: 0
  });

  // Edit Project Modal
  const [editOpen, setEditOpen] = useState(false);
  const [editProject, setEditProject] = useState(null);

  // Workers Modal
  const [workersOpen, setWorkersOpen] = useState(false);
  const [selectedProject, setSelectedProject] = useState(null);
  const [projectWorkers, setProjectWorkers] = useState([]);
  const [allWorkers, setAllWorkers] = useState([]);
  const [assignWorkerId, setAssignWorkerId] = useState('');
  const [assignRole, setAssignRole] = useState('Site Worker');

  const fetchProjects = async () => {
    try {
      const url = statusFilter ? `/projects?status=${statusFilter}` : '/projects';
      const res = await api.get(url);
      if (res.success) setProjects(res.data);
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to fetch projects' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjects();
  }, [statusFilter]);

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/projects', {
        ...createForm,
        budget: createForm.budget ? Number(createForm.budget) : 0,
        progressPercent: Number(createForm.progressPercent)
      });
      setMessage({ type: 'success', text: `Project "${createForm.projectName}" created.` });
      setCreateOpen(false);
      setCreateForm({
        projectName: '',
        description: '',
        location: '',
        startDate: new Date().toISOString().split('T')[0],
        endDate: '',
        budget: '',
        status: 'Planning',
        progressPercent: 0
      });
      fetchProjects();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to create project' });
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.put(`/projects/${editProject.id}`, {
        ...editProject,
        budget: editProject.budget ? Number(editProject.budget) : 0,
        progressPercent: Number(editProject.progressPercent)
      });
      setMessage({ type: 'success', text: `Project "${editProject.projectName}" updated.` });
      setEditOpen(false);
      setEditProject(null);
      fetchProjects();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to update project' });
    }
  };

  const openWorkersModal = async (project) => {
    setSelectedProject(project);
    setWorkersOpen(true);
    try {
      const [pwRes, wRes] = await Promise.all([
        api.get(`/projects/${project.id}/workers`),
        api.get('/workforce/workers?status=ACTIVE')
      ]);
      if (pwRes.success) setProjectWorkers(pwRes.data);
      if (wRes.success) setAllWorkers(wRes.data);
    } catch (e) {
      console.error(e);
    }
  };

  const handleAssignWorker = async (e) => {
    e.preventDefault();
    if (!assignWorkerId) return;
    try {
      await api.post(`/projects/${selectedProject.id}/workers`, {
        workerId: Number(assignWorkerId),
        roleInProject: assignRole
      });
      const pwRes = await api.get(`/projects/${selectedProject.id}/workers`);
      if (pwRes.success) setProjectWorkers(pwRes.data);
      setAssignWorkerId('');
    } catch (err) {
      alert(err.message || 'Failed to assign worker');
    }
  };

  const handleRemoveWorker = async (workerId) => {
    try {
      await api.delete(`/projects/${selectedProject.id}/workers/${workerId}`);
      setProjectWorkers(prev => prev.filter(w => w.workerId !== workerId));
    } catch (err) {
      alert(err.message || 'Failed to remove worker');
    }
  };

  const columns = [
    { header: 'ID', accessor: 'id', width: '50px' },
    { header: 'Project Name', accessor: 'projectName', render: (row) => <strong>{row.projectName}</strong> },
    { header: 'Location', accessor: 'location' },
    {
      header: 'Status',
      accessor: 'status',
      render: (row) => (
        <span className={`badge badge-${row.status === 'Completed' ? 'success' : row.status === 'In Progress' ? 'primary' : row.status === 'On Hold' ? 'warning' : 'secondary'}`}>
          {row.status}
        </span>
      )
    },
    {
      header: 'Progress',
      accessor: 'progressPercent',
      render: (row) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', minWidth: '100px' }}>
          <div style={{ flex: 1, height: '8px', background: 'var(--color-table-zebra)', borderRadius: '4px', overflow: 'hidden' }}>
            <div style={{ width: `${row.progressPercent}%`, height: '100%', background: 'var(--color-primary)' }} />
          </div>
          <span style={{ fontSize: '0.8rem' }}>{row.progressPercent}%</span>
        </div>
      )
    },
    {
      header: 'Budget',
      accessor: 'budget',
      render: (row) => row.budget ? `$${Number(row.budget).toLocaleString()}` : '$0'
    },
    { header: 'Start Date', accessor: 'startDate' },
    { header: 'End Date', accessor: 'endDate' },
    {
      header: 'Actions',
      render: (row) => (
        <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
          {hasPermission('PROJECT_UPDATE') && (
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => {
                setEditProject({ ...row });
                setEditOpen(true);
              }}
            >
              Edit
            </button>
          )}

          <button
            className="btn btn-secondary btn-sm"
            onClick={() => openWorkersModal(row)}
          >
            Workforce
          </button>
        </div>
      )
    }
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2>Projects Management</h2>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Oversee construction sites, schedule timelines, track budgets and allocate workforce.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <select
            className="form-input"
            style={{ width: 'auto' }}
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">All Statuses</option>
            {STATUS_OPTIONS.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
          </select>

          {hasPermission('PROJECT_CREATE') && (
            <button className="btn btn-primary" onClick={() => setCreateOpen(true)}>
              + New Project
            </button>
          )}
        </div>
      </div>

      {message && (
        <div className={`alert alert-${message.type}`} style={{ marginBottom: '1rem' }}>
          {message.text}
        </div>
      )}

      {loading ? (
        <p>Loading projects...</p>
      ) : (
        <DataTable
          columns={columns}
          data={projects}
          keyField="id"
          searchPlaceholder="Search projects by name, location..."
        />
      )}

      {/* Create Modal */}
      <Modal
        isOpen={createOpen}
        onClose={() => setCreateOpen(false)}
        title="Create New Project"
        maxWidth="500px"
      >
        <form onSubmit={handleCreateSubmit}>
          <FormField
            label="Project Name"
            name="projectName"
            value={createForm.projectName}
            onChange={(e) => setCreateForm({ ...createForm, projectName: e.target.value })}
            required
            placeholder="e.g. Metro Line Extension - Phase 2"
          />
          <FormField
            label="Location"
            name="location"
            value={createForm.location}
            onChange={(e) => setCreateForm({ ...createForm, location: e.target.value })}
            required
            placeholder="e.g. North Sector, Lot 14"
          />
          <FormField
            label="Description"
            name="description"
            type="textarea"
            value={createForm.description}
            onChange={(e) => setCreateForm({ ...createForm, description: e.target.value })}
          />

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <FormField
              label="Start Date"
              name="startDate"
              type="date"
              value={createForm.startDate}
              onChange={(e) => setCreateForm({ ...createForm, startDate: e.target.value })}
              required
            />
            <FormField
              label="Estimated End Date"
              name="endDate"
              type="date"
              value={createForm.endDate}
              onChange={(e) => setCreateForm({ ...createForm, endDate: e.target.value })}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <FormField
              label="Budget ($)"
              name="budget"
              type="number"
              value={createForm.budget}
              onChange={(e) => setCreateForm({ ...createForm, budget: e.target.value })}
              placeholder="e.g. 500000"
            />
            <FormField
              label="Status"
              name="status"
              type="select"
              value={createForm.status}
              onChange={(e) => setCreateForm({ ...createForm, status: e.target.value })}
              options={STATUS_OPTIONS}
            />
          </div>

          <FormField
            label={`Initial Progress: ${createForm.progressPercent}%`}
            name="progressPercent"
          >
            <input
              type="range"
              min="0"
              max="100"
              value={createForm.progressPercent}
              onChange={(e) => setCreateForm({ ...createForm, progressPercent: e.target.value })}
              style={{ width: '100%' }}
            />
          </FormField>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setCreateOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Create Project
            </button>
          </div>
        </form>
      </Modal>

      {/* Edit Modal */}
      <Modal
        isOpen={editOpen}
        onClose={() => {
          setEditOpen(false);
          setEditProject(null);
        }}
        title={`Edit Project: ${editProject?.projectName}`}
        maxWidth="500px"
      >
        {editProject && (
          <form onSubmit={handleEditSubmit}>
            <FormField
              label="Project Name"
              name="projectName"
              value={editProject.projectName}
              onChange={(e) => setEditProject({ ...editProject, projectName: e.target.value })}
              required
            />
            <FormField
              label="Location"
              name="location"
              value={editProject.location}
              onChange={(e) => setEditProject({ ...editProject, location: e.target.value })}
              required
            />
            <FormField
              label="Description"
              name="description"
              type="textarea"
              value={editProject.description || ''}
              onChange={(e) => setEditProject({ ...editProject, description: e.target.value })}
            />

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <FormField
                label="Start Date"
                name="startDate"
                type="date"
                value={editProject.startDate || ''}
                onChange={(e) => setEditProject({ ...editProject, startDate: e.target.value })}
                required
              />
              <FormField
                label="Estimated End Date"
                name="endDate"
                type="date"
                value={editProject.endDate || ''}
                onChange={(e) => setEditProject({ ...editProject, endDate: e.target.value })}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <FormField
                label="Budget ($)"
                name="budget"
                type="number"
                value={editProject.budget || ''}
                onChange={(e) => setEditProject({ ...editProject, budget: e.target.value })}
              />
              <FormField
                label="Status"
                name="status"
                type="select"
                value={editProject.status}
                onChange={(e) => setEditProject({ ...editProject, status: e.target.value })}
                options={STATUS_OPTIONS}
              />
            </div>

            <FormField
              label={`Progress: ${editProject.progressPercent}%`}
              name="progressPercent"
            >
              <input
                type="range"
                min="0"
                max="100"
                value={editProject.progressPercent || 0}
                onChange={(e) => setEditProject({ ...editProject, progressPercent: e.target.value })}
                style={{ width: '100%' }}
              />
            </FormField>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  setEditOpen(false);
                  setEditProject(null);
                }}
              >
                Cancel
              </button>
              <button type="submit" className="btn btn-primary">
                Save Changes
              </button>
            </div>
          </form>
        )}
      </Modal>

      {/* Workforce Modal */}
      <Modal
        isOpen={workersOpen}
        onClose={() => setWorkersOpen(false)}
        title={`Workforce Allocated: ${selectedProject?.projectName}`}
        maxWidth="550px"
      >
        {hasPermission('WORKFORCE_ASSIGN') && (
          <form onSubmit={handleAssignWorker} style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.25rem', alignItems: 'flex-end' }}>
            <div style={{ flex: 2 }}>
              <label className="form-label" style={{ fontSize: '0.8rem' }}>Add Worker to Site</label>
              <select
                className="form-input"
                value={assignWorkerId}
                onChange={(e) => setAssignWorkerId(e.target.value)}
                required
              >
                <option value="">Select worker...</option>
                {allWorkers.map(w => (
                  <option key={w.id} value={w.id}>{w.fullName} ({w.trade})</option>
                ))}
              </select>
            </div>
            <div style={{ flex: 1.5 }}>
              <label className="form-label" style={{ fontSize: '0.8rem' }}>Role on Site</label>
              <input
                type="text"
                className="form-input"
                value={assignRole}
                onChange={(e) => setAssignRole(e.target.value)}
                placeholder="e.g. Electrician"
              />
            </div>
            <button type="submit" className="btn btn-primary btn-sm" style={{ height: '36px' }}>
              Assign
            </button>
          </form>
        )}

        <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
          {projectWorkers.length === 0 ? (
            <p style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>No workers currently assigned to this project.</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Worker Name</th>
                  <th>Trade</th>
                  <th>Role</th>
                  <th>Assigned Date</th>
                  {hasPermission('WORKFORCE_ASSIGN') && <th>Action</th>}
                </tr>
              </thead>
              <tbody>
                {projectWorkers.map((pw) => (
                  <tr key={pw.workerId}>
                    <td><strong>{pw.workerName}</strong></td>
                    <td>{pw.trade}</td>
                    <td>{pw.roleInProject}</td>
                    <td>{pw.assignedDate}</td>
                    {hasPermission('WORKFORCE_ASSIGN') && (
                      <td>
                        <button
                          className="btn btn-danger btn-sm"
                          style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem' }}
                          onClick={() => handleRemoveWorker(pw.workerId)}
                        >
                          Remove
                        </button>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </Modal>
    </div>
  );
}
