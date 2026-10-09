import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';

const TASK_STATUSES = [
  { value: 'Created', label: 'Created' },
  { value: 'In Progress', label: 'In Progress' },
  { value: 'Under Review', label: 'Under Review' },
  { value: 'Completed', label: 'Completed' },
  { value: 'Blocked', label: 'Blocked' }
];

const PRIORITIES = [
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
  { value: 'URGENT', label: 'Urgent' }
];

export default function TasksList() {
  const { hasPermission } = useAuth();
  const [tasks, setTasks] = useState([]);
  const [projects, setProjects] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  // Create Task Modal
  const [createOpen, setCreateOpen] = useState(false);
  const [createForm, setCreateForm] = useState({
    projectId: '',
    title: '',
    description: '',
    priority: 'MEDIUM',
    status: 'Created',
    assignedWorkerId: '',
    startDate: '',
    dueDate: '',
    completionPercent: 0
  });

  // Edit / Workflow Modal
  const [editOpen, setEditOpen] = useState(false);
  const [editTask, setEditTask] = useState(null);

  const fetchDropdowns = async () => {
    try {
      const [pRes, wRes] = await Promise.all([
        api.get('/projects'),
        api.get('/workforce/workers?status=ACTIVE')
      ]);
      if (pRes.success) {
        setProjects(pRes.data);
        if (pRes.data.length > 0 && !createForm.projectId) {
          setCreateForm(prev => ({ ...prev, projectId: pRes.data[0].id }));
        }
      }
      if (wRes.success) setWorkers(wRes.data);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchTasks = async () => {
    try {
      let query = '?';
      if (selectedProjectId) query += `projectId=${selectedProjectId}&`;
      if (selectedStatus) query += `status=${selectedStatus}&`;
      const res = await api.get(`/tasks${query}`);
      if (res.success) setTasks(res.data);
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to fetch tasks' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDropdowns();
  }, []);

  useEffect(() => {
    fetchTasks();
  }, [selectedProjectId, selectedStatus]);

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/tasks', {
        ...createForm,
        projectId: Number(createForm.projectId),
        assignedWorkerId: createForm.assignedWorkerId ? Number(createForm.assignedWorkerId) : null,
        completionPercent: Number(createForm.completionPercent)
      });
      setMessage({ type: 'success', text: `Task "${createForm.title}" created successfully.` });
      setCreateOpen(false);
      setCreateForm({
        projectId: projects[0]?.id || '',
        title: '',
        description: '',
        priority: 'MEDIUM',
        status: 'Created',
        assignedWorkerId: '',
        startDate: '',
        dueDate: '',
        completionPercent: 0
      });
      fetchTasks();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to create task' });
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.put(`/tasks/${editTask.id}`, {
        ...editTask,
        assignedWorkerId: editTask.assignedWorkerId ? Number(editTask.assignedWorkerId) : null,
        completionPercent: Number(editTask.completionPercent)
      });
      setMessage({ type: 'success', text: `Task "${editTask.title}" updated.` });
      setEditOpen(false);
      setEditTask(null);
      fetchTasks();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to update task' });
    }
  };

  const columns = [
    { header: 'ID', accessor: 'id', width: '50px' },
    { header: 'Task Title', accessor: 'title', render: (row) => <strong>{row.title}</strong> },
    { header: 'Project', accessor: 'projectName' },
    {
      header: 'Priority',
      accessor: 'priority',
      render: (row) => (
        <span className={`badge badge-${row.priority === 'URGENT' || row.priority === 'HIGH' ? 'danger' : row.priority === 'MEDIUM' ? 'warning' : 'secondary'}`}>
          {row.priority}
        </span>
      )
    },
    {
      header: 'Status',
      accessor: 'status',
      render: (row) => (
        <span className={`badge badge-${row.status === 'Completed' ? 'success' : row.status === 'In Progress' ? 'primary' : row.status === 'Blocked' ? 'danger' : 'warning'}`}>
          {row.status}
        </span>
      )
    },
    {
      header: 'Progress',
      accessor: 'completionPercent',
      render: (row) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', minWidth: '80px' }}>
          <div style={{ flex: 1, height: '6px', background: 'var(--color-table-zebra)', borderRadius: '3px', overflow: 'hidden' }}>
            <div style={{ width: `${row.completionPercent}%`, height: '100%', background: 'var(--color-primary)' }} />
          </div>
          <span style={{ fontSize: '0.75rem' }}>{row.completionPercent}%</span>
        </div>
      )
    },
    { header: 'Assigned Worker', accessor: 'assignedWorkerName', render: (row) => row.assignedWorkerName || 'Unassigned' },
    { header: 'Due Date', accessor: 'dueDate' },
    {
      header: 'Actions',
      render: (row) => hasPermission('TASK_UPDATE') && (
        <button
          className="btn btn-secondary btn-sm"
          onClick={() => {
            setEditTask({ ...row });
            setEditOpen(true);
          }}
        >
          Update / Transition
        </button>
      )
    }
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2>Task Management</h2>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Coordinate daily on-site task assignments, execution progress and workflow states.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <select
            className="form-input"
            style={{ width: 'auto' }}
            value={selectedProjectId}
            onChange={(e) => setSelectedProjectId(e.target.value)}
          >
            <option value="">All Projects</option>
            {projects.map(p => <option key={p.id} value={p.id}>{p.projectName}</option>)}
          </select>

          <select
            className="form-input"
            style={{ width: 'auto' }}
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
          >
            <option value="">All Statuses</option>
            {TASK_STATUSES.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
          </select>

          {hasPermission('TASK_CREATE') && (
            <button className="btn btn-primary" onClick={() => setCreateOpen(true)}>
              + New Task
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
        <p>Loading tasks...</p>
      ) : (
        <DataTable
          columns={columns}
          data={tasks}
          keyField="id"
          searchPlaceholder="Search tasks by title, worker, project..."
        />
      )}

      {/* Create Task Modal */}
      <Modal
        isOpen={createOpen}
        onClose={() => setCreateOpen(false)}
        title="Create New Task"
        maxWidth="500px"
      >
        <form onSubmit={handleCreateSubmit}>
          <FormField
            label="Project"
            name="projectId"
            type="select"
            value={createForm.projectId}
            onChange={(e) => setCreateForm({ ...createForm, projectId: e.target.value })}
            options={projects.map(p => ({ value: p.id, label: p.projectName }))}
            required
          />
          <FormField
            label="Task Title"
            name="title"
            value={createForm.title}
            onChange={(e) => setCreateForm({ ...createForm, title: e.target.value })}
            required
            placeholder="e.g. Foundation concrete pour"
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
              label="Priority"
              name="priority"
              type="select"
              value={createForm.priority}
              onChange={(e) => setCreateForm({ ...createForm, priority: e.target.value })}
              options={PRIORITIES}
            />
            <FormField
              label="Assigned Worker"
              name="assignedWorkerId"
              type="select"
              value={createForm.assignedWorkerId}
              onChange={(e) => setCreateForm({ ...createForm, assignedWorkerId: e.target.value })}
              options={[
                { value: '', label: 'Unassigned' },
                ...workers.map(w => ({ value: w.id, label: `${w.fullName} (${w.trade})` }))
              ]}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <FormField
              label="Start Date"
              name="startDate"
              type="date"
              value={createForm.startDate}
              onChange={(e) => setCreateForm({ ...createForm, startDate: e.target.value })}
            />
            <FormField
              label="Due Date"
              name="dueDate"
              type="date"
              value={createForm.dueDate}
              onChange={(e) => setCreateForm({ ...createForm, dueDate: e.target.value })}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setCreateOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Create Task
            </button>
          </div>
        </form>
      </Modal>

      {/* Edit / Transition Modal */}
      <Modal
        isOpen={editOpen}
        onClose={() => {
          setEditOpen(false);
          setEditTask(null);
        }}
        title={`Task Workflow: ${editTask?.title}`}
        maxWidth="500px"
      >
        {editTask && (
          <form onSubmit={handleEditSubmit}>
            <FormField
              label="Task Title"
              name="title"
              value={editTask.title}
              onChange={(e) => setEditTask({ ...editTask, title: e.target.value })}
              required
            />
            <FormField
              label="Description"
              name="description"
              type="textarea"
              value={editTask.description || ''}
              onChange={(e) => setEditTask({ ...editTask, description: e.target.value })}
            />

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <FormField
                label="Workflow Status"
                name="status"
                type="select"
                value={editTask.status}
                onChange={(e) => {
                  const newStatus = e.target.value;
                  const newPercent = newStatus === 'Completed' ? 100 : editTask.completionPercent;
                  setEditTask({ ...editTask, status: newStatus, completionPercent: newPercent });
                }}
                options={TASK_STATUSES}
              />
              <FormField
                label="Assigned Worker"
                name="assignedWorkerId"
                type="select"
                value={editTask.assignedWorkerId || ''}
                onChange={(e) => setEditTask({ ...editTask, assignedWorkerId: e.target.value })}
                options={[
                  { value: '', label: 'Unassigned' },
                  ...workers.map(w => ({ value: w.id, label: `${w.fullName} (${w.trade})` }))
                ]}
              />
            </div>

            <FormField
              label={`Completion Progress: ${editTask.completionPercent}%`}
              name="completionPercent"
            >
              <input
                type="range"
                min="0"
                max="100"
                value={editTask.completionPercent || 0}
                onChange={(e) => setEditTask({ ...editTask, completionPercent: Number(e.target.value) })}
                style={{ width: '100%' }}
              />
            </FormField>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <FormField
                label="Start Date"
                name="startDate"
                type="date"
                value={editTask.startDate || ''}
                onChange={(e) => setEditTask({ ...editTask, startDate: e.target.value })}
              />
              <FormField
                label="Due Date"
                name="dueDate"
                type="date"
                value={editTask.dueDate || ''}
                onChange={(e) => setEditTask({ ...editTask, dueDate: e.target.value })}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  setEditOpen(false);
                  setEditTask(null);
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
    </div>
  );
}
