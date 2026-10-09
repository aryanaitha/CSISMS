import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';

export default function IncidentsList() {
  const { hasPermission } = useAuth();
  const [incidents, setIncidents] = useState([]);
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  // Filter state
  const [severityFilter, setSeverityFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  // Report Modal
  const [reportModalOpen, setReportModalOpen] = useState(false);
  const [reportForm, setReportForm] = useState({
    projectId: '',
    title: '',
    incidentDate: new Date().toISOString().slice(0, 16),
    severity: 'NEAR_MISS',
    description: ''
  });

  // Edit / Investigate Modal
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [selectedIncident, setSelectedIncident] = useState(null);
  const [editForm, setEditForm] = useState({
    status: 'Reported',
    investigatorId: '',
    actionsTaken: ''
  });

  const [submitting, setSubmitting] = useState(false);

  const fetchData = async () => {
    try {
      setLoading(true);
      let url = '/incidents?';
      if (severityFilter) url += `severity=${severityFilter}&`;
      if (statusFilter) url += `status=${statusFilter}&`;

      const [incRes, projRes, userRes] = await Promise.all([
        api.get(url),
        api.get('/projects'),
        hasPermission('USER_VIEW') ? api.get('/users') : Promise.resolve({ data: [] })
      ]);

      if (incRes.data) setIncidents(incRes.data);
      if (projRes.data) setProjects(projRes.data);
      if (userRes.data) setUsers(userRes.data);
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to load incidents' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [severityFilter, statusFilter]);

  const handleReportIncident = async (e) => {
    e.preventDefault();
    if (!reportForm.projectId || !reportForm.title || !reportForm.description) {
      setMessage({ type: 'danger', text: 'Project, Title, and Description are required' });
      return;
    }
    setSubmitting(true);
    try {
      await api.post('/incidents', {
        projectId: Number(reportForm.projectId),
        title: reportForm.title,
        incidentDate: reportForm.incidentDate,
        severity: reportForm.severity,
        description: reportForm.description,
        status: 'Reported'
      });

      setMessage({
        type: reportForm.severity === 'FATALITY' || reportForm.severity === 'INJURY' ? 'danger' : 'success',
        text: `Incident reported successfully. ${
          reportForm.severity === 'FATALITY' || reportForm.severity === 'INJURY'
            ? 'Emergency broadcast notification sent to safety personnel.'
            : ''
        }`
      });
      setReportModalOpen(false);
      setReportForm({
        projectId: '',
        title: '',
        incidentDate: new Date().toISOString().slice(0, 16),
        severity: 'NEAR_MISS',
        description: ''
      });
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to submit incident report' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleOpenEdit = (inc) => {
    setSelectedIncident(inc);
    setEditForm({
      status: inc.status || 'Reported',
      investigatorId: inc.investigatorId || '',
      actionsTaken: inc.actionsTaken || ''
    });
    setEditModalOpen(true);
  };

  const handleUpdateIncident = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.put(`/incidents/${selectedIncident.id}`, {
        ...selectedIncident,
        status: editForm.status,
        investigatorId: editForm.investigatorId ? Number(editForm.investigatorId) : null,
        actionsTaken: editForm.actionsTaken
      });

      setMessage({ type: 'success', text: `Incident #${selectedIncident.id} updated successfully` });
      setEditModalOpen(false);
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to update incident' });
    } finally {
      setSubmitting(false);
    }
  };

  const columns = [
    { header: 'ID', accessor: 'id' },
    { header: 'Project', accessor: 'projectName' },
    {
      header: 'Severity',
      render: (row) => {
        const sev = (row.severity || '').toUpperCase();
        let badgeClass = 'badge-secondary';
        if (sev === 'FATALITY') badgeClass = 'badge-danger';
        else if (sev === 'INJURY') badgeClass = 'badge-danger';
        else if (sev === 'PROPERTY_DAMAGE') badgeClass = 'badge-warning';
        else if (sev === 'NEAR_MISS') badgeClass = 'badge-info';
        return <span className={`badge ${badgeClass}`}>{row.severity}</span>;
      }
    },
    { header: 'Title', accessor: 'title' },
    {
      header: 'Incident Date',
      render: (row) => row.incidentDate ? row.incidentDate.replace('T', ' ').slice(0, 16) : ''
    },
    { header: 'Reported By', accessor: 'reporterName' },
    { header: 'Investigator', render: (row) => row.investigatorName || 'Pending' },
    {
      header: 'Status',
      render: (row) => {
        const st = (row.status || '').toLowerCase();
        let badgeClass = 'badge-secondary';
        if (st.includes('resolved') || st.includes('closed')) badgeClass = 'badge-success';
        else if (st.includes('investigation')) badgeClass = 'badge-warning';
        else badgeClass = 'badge-danger';
        return <span className={`badge ${badgeClass}`}>{row.status}</span>;
      }
    },
    {
      header: 'Actions',
      render: (row) => (
        <div style={{ display: 'flex', gap: '0.25rem' }}>
          {hasPermission('INCIDENT_UPDATE') && (
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => handleOpenEdit(row)}
            >
              Investigate / Update
            </button>
          )}
        </div>
      )
    }
  ];

  return (
    <div className="page-container">
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2>Incident & Accident Tracking</h2>
          <p className="text-muted">OSHA accident logging, root cause analysis, and corrective resolution workflows</p>
        </div>
        {hasPermission('INCIDENT_CREATE') && (
          <button className="btn btn-danger" onClick={() => setReportModalOpen(true)}>
            + Report Site Incident
          </button>
        )}
      </div>

      {message && (
        <div className={`alert alert-${message.type}`} style={{ marginBottom: '1rem' }}>
          {message.text}
          <button className="btn btn-link btn-sm" style={{ float: 'right' }} onClick={() => setMessage(null)}>✕</button>
        </div>
      )}

      {/* Filters */}
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', alignItems: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <label style={{ fontSize: '0.875rem' }}>Severity:</label>
          <select
            className="form-control"
            style={{ width: '180px' }}
            value={severityFilter}
            onChange={(e) => setSeverityFilter(e.target.value)}
          >
            <option value="">All Severities</option>
            <option value="FATALITY">FATALITY</option>
            <option value="INJURY">INJURY</option>
            <option value="PROPERTY_DAMAGE">PROPERTY_DAMAGE</option>
            <option value="NEAR_MISS">NEAR_MISS</option>
          </select>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <label style={{ fontSize: '0.875rem' }}>Status:</label>
          <select
            className="form-control"
            style={{ width: '180px' }}
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">All Statuses</option>
            <option value="Reported">Reported</option>
            <option value="Under Investigation">Under Investigation</option>
            <option value="Resolved">Resolved</option>
            <option value="Closed">Closed</option>
          </select>
        </div>
      </div>

      <DataTable
        columns={columns}
        data={incidents}
        loading={loading}
        searchPlaceholder="Search incidents by title, project, reporter..."
      />

      {/* Report Incident Modal */}
      <Modal
        title="Report Safety Incident / Accident"
        isOpen={reportModalOpen}
        onClose={() => setReportModalOpen(false)}
      >
        <form onSubmit={handleReportIncident}>
          <FormField label="Project *" required>
            <select
              className="form-control"
              value={reportForm.projectId}
              onChange={(e) => setReportForm({ ...reportForm, projectId: e.target.value })}
              required
            >
              <option value="">-- Select Project --</option>
              {projects.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </FormField>

          <FormField label="Severity Classification *" required>
            <select
              className="form-control"
              value={reportForm.severity}
              onChange={(e) => setReportForm({ ...reportForm, severity: e.target.value })}
              required
            >
              <option value="FATALITY">FATALITY (Loss of life / Immediate shut down)</option>
              <option value="INJURY">INJURY (Personal injury / Medical treatment required)</option>
              <option value="PROPERTY_DAMAGE">PROPERTY_DAMAGE (Equipment or structure damage)</option>
              <option value="NEAR_MISS">NEAR_MISS (Near miss hazardous incident)</option>
            </select>
          </FormField>

          <FormField label="Incident Title *" required>
            <input
              type="text"
              className="form-control"
              value={reportForm.title}
              onChange={(e) => setReportForm({ ...reportForm, title: e.target.value })}
              placeholder="e.g. Scaffolding plank collapse on Section 3"
              required
            />
          </FormField>

          <FormField label="Date & Time of Incident *" required>
            <input
              type="datetime-local"
              className="form-control"
              value={reportForm.incidentDate}
              onChange={(e) => setReportForm({ ...reportForm, incidentDate: e.target.value })}
              required
            />
          </FormField>

          <FormField label="Detailed Description *" required>
            <textarea
              className="form-control"
              rows={4}
              value={reportForm.description}
              onChange={(e) => setReportForm({ ...reportForm, description: e.target.value })}
              placeholder="Explain how the incident unfolded, persons involved, environmental factors..."
              required
            />
          </FormField>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setReportModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-danger" disabled={submitting}>
              {submitting ? 'Submitting...' : 'Submit Incident Report'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Edit / Investigate Modal */}
      <Modal
        title={`Incident Investigation #${selectedIncident?.id}`}
        isOpen={editModalOpen}
        onClose={() => setEditModalOpen(false)}
      >
        <form onSubmit={handleUpdateIncident}>
          {selectedIncident && (
            <div style={{ background: 'var(--bg-light)', padding: '0.75rem', borderRadius: '4px', marginBottom: '1rem' }}>
              <p><strong>Title:</strong> {selectedIncident.title}</p>
              <p><strong>Severity:</strong> <span className="badge badge-danger">{selectedIncident.severity}</span></p>
              <p><strong>Description:</strong> {selectedIncident.description}</p>
            </div>
          )}

          <FormField label="Investigation Status *">
            <select
              className="form-control"
              value={editForm.status}
              onChange={(e) => setEditForm({ ...editForm, status: e.target.value })}
            >
              <option value="Reported">Reported</option>
              <option value="Under Investigation">Under Investigation</option>
              <option value="Resolved">Resolved</option>
              <option value="Closed">Closed</option>
            </select>
          </FormField>

          <FormField label="Assigned Safety Investigator">
            <select
              className="form-control"
              value={editForm.investigatorId}
              onChange={(e) => setEditForm({ ...editForm, investigatorId: e.target.value })}
            >
              <option value="">-- Unassigned --</option>
              {users.map((u) => (
                <option key={u.userId} value={u.userId}>{u.fullName} ({u.roleName})</option>
              ))}
            </select>
          </FormField>

          <FormField label="Corrective Actions Taken / Investigation Findings">
            <textarea
              className="form-control"
              rows={4}
              value={editForm.actionsTaken}
              onChange={(e) => setEditForm({ ...editForm, actionsTaken: e.target.value })}
              placeholder="Outline investigative conclusions and safety countermeasures implemented..."
            />
          </FormField>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setEditModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : 'Save Investigation Record'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
