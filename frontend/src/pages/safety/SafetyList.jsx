import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';

export default function SafetyList() {
  const { hasPermission } = useAuth();
  const [activeTab, setActiveTab] = useState('inspections'); // 'inspections' | 'violations'
  const [inspections, setInspections] = useState([]);
  const [violations, setViolations] = useState([]);
  const [projects, setProjects] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  // New Inspection Modal
  const [inspectionModalOpen, setInspectionModalOpen] = useState(false);
  const [inspectionForm, setInspectionForm] = useState({
    projectId: '',
    inspectionDate: new Date().toISOString().split('T')[0],
    ppeScore: 85,
    workerSafetyScore: 80,
    equipmentScore: 90,
    workAreaScore: 85,
    emergencyScore: 90,
    remarks: ''
  });

  // Log Violation Modal
  const [violationModalOpen, setViolationModalOpen] = useState(false);
  const [violationForm, setViolationForm] = useState({
    projectId: '',
    workerId: '',
    severity: 'MEDIUM',
    description: '',
    correctiveAction: ''
  });

  // Resolve / Update Violation Modal
  const [editViolationModalOpen, setEditViolationModalOpen] = useState(false);
  const [selectedViolation, setSelectedViolation] = useState(null);
  const [editViolationForm, setEditViolationForm] = useState({
    status: 'OPEN',
    correctiveAction: '',
    resolutionDate: ''
  });

  // View Inspection Checklist Detail Modal
  const [viewInspectionModalOpen, setViewInspectionModalOpen] = useState(false);
  const [selectedInspection, setSelectedInspection] = useState(null);

  const [submitting, setSubmitting] = useState(false);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [inspRes, violRes, projRes, workRes] = await Promise.all([
        hasPermission('SAFETY_INSPECTION_VIEW') ? api.get('/safety/inspections') : Promise.resolve({ data: [] }),
        hasPermission('SAFETY_VIOLATION_VIEW') ? api.get('/safety/violations') : Promise.resolve({ data: [] }),
        api.get('/projects'),
        api.get('/workforce/workers')
      ]);

      if (inspRes.data) setInspections(inspRes.data);
      if (violRes.data) setViolations(violRes.data);
      if (projRes.data) setProjects(projRes.data);
      if (workRes.data) setWorkers(workRes.data);
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to load safety data' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Compute live inspection overall score
  const computedAverage = (
    (Number(inspectionForm.ppeScore || 0) +
      Number(inspectionForm.workerSafetyScore || 0) +
      Number(inspectionForm.equipmentScore || 0) +
      Number(inspectionForm.workAreaScore || 0) +
      Number(inspectionForm.emergencyScore || 0)) / 5
  ).toFixed(1);

  const handleConductInspection = async (e) => {
    e.preventDefault();
    if (!inspectionForm.projectId) {
      setMessage({ type: 'danger', text: 'Please select a project' });
      return;
    }
    setSubmitting(true);
    try {
      await api.post('/safety/inspections', {
        projectId: Number(inspectionForm.projectId),
        inspectionDate: inspectionForm.inspectionDate,
        ppeScore: Number(inspectionForm.ppeScore),
        workerSafetyScore: Number(inspectionForm.workerSafetyScore),
        equipmentScore: Number(inspectionForm.equipmentScore),
        workAreaScore: Number(inspectionForm.workAreaScore),
        emergencyScore: Number(inspectionForm.emergencyScore),
        remarks: inspectionForm.remarks
      });

      setMessage({ type: 'success', text: `Safety inspection recorded. Overall score: ${computedAverage}%` });
      setInspectionModalOpen(false);
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to record inspection' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleLogViolation = async (e) => {
    e.preventDefault();
    if (!violationForm.projectId || !violationForm.description) {
      setMessage({ type: 'danger', text: 'Project and Description are required' });
      return;
    }
    setSubmitting(true);
    try {
      await api.post('/safety/violations', {
        projectId: Number(violationForm.projectId),
        workerId: violationForm.workerId ? Number(violationForm.workerId) : null,
        severity: violationForm.severity,
        description: violationForm.description,
        correctiveAction: violationForm.correctiveAction,
        status: 'OPEN'
      });

      setMessage({ type: 'success', text: 'Safety violation logged successfully' });
      setViolationModalOpen(false);
      setViolationForm({
        projectId: '',
        workerId: '',
        severity: 'MEDIUM',
        description: '',
        correctiveAction: ''
      });
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to log violation' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleOpenEditViolation = (viol) => {
    setSelectedViolation(viol);
    setEditViolationForm({
      status: viol.status || 'OPEN',
      correctiveAction: viol.correctiveAction || '',
      resolutionDate: viol.resolutionDate || (viol.status === 'RESOLVED' ? new Date().toISOString().split('T')[0] : '')
    });
    setEditViolationModalOpen(true);
  };

  const handleUpdateViolation = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.put(`/safety/violations/${selectedViolation.id}`, {
        ...selectedViolation,
        status: editViolationForm.status,
        correctiveAction: editViolationForm.correctiveAction,
        resolutionDate: editViolationForm.status === 'RESOLVED' ? (editViolationForm.resolutionDate || new Date().toISOString().split('T')[0]) : null
      });

      setMessage({ type: 'success', text: `Violation #${selectedViolation.id} status updated to ${editViolationForm.status}` });
      setEditViolationModalOpen(false);
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to update violation' });
    } finally {
      setSubmitting(false);
    }
  };

  const inspectionColumns = [
    { header: 'ID', accessor: 'id' },
    { header: 'Project', accessor: 'projectName' },
    {
      header: 'Score',
      render: (row) => {
        const score = Number(row.overallScore || 0);
        const isGood = score >= 70;
        return (
          <span className={`badge ${isGood ? 'badge-success' : 'badge-danger'}`} style={{ fontWeight: 'bold' }}>
            {score.toFixed(1)}% {isGood ? '✓' : '⚠ Action Req.'}
          </span>
        );
      }
    },
    { header: 'Date', accessor: 'inspectionDate' },
    { header: 'Inspector', accessor: 'inspectorName' },
    { header: 'Remarks', accessor: 'remarks' },
    {
      header: 'Actions',
      render: (row) => (
        <button
          className="btn btn-secondary btn-sm"
          onClick={() => {
            setSelectedInspection(row);
            setViewInspectionModalOpen(true);
          }}
        >
          View Checklist
        </button>
      )
    }
  ];

  const violationColumns = [
    { header: 'ID', accessor: 'id' },
    { header: 'Project', accessor: 'projectName' },
    {
      header: 'Severity',
      render: (row) => {
        const sev = (row.severity || '').toUpperCase();
        let badgeClass = 'badge-secondary';
        if (sev === 'CRITICAL') badgeClass = 'badge-danger';
        else if (sev === 'HIGH' || sev === 'MAJOR') badgeClass = 'badge-warning';
        else if (sev === 'MEDIUM') badgeClass = 'badge-info';
        return <span className={`badge ${badgeClass}`}>{row.severity}</span>;
      }
    },
    { header: 'Description', accessor: 'description' },
    { header: 'Worker', render: (row) => row.workerName || 'Site General' },
    {
      header: 'Status',
      render: (row) => {
        const st = (row.status || '').toUpperCase();
        const badgeClass = st === 'RESOLVED' ? 'badge-success' : st === 'IN_PROGRESS' ? 'badge-warning' : 'badge-danger';
        return <span className={`badge ${badgeClass}`}>{row.status}</span>;
      }
    },
    { header: 'Corrective Action', accessor: 'correctiveAction' },
    {
      header: 'Actions',
      render: (row) => (
        hasPermission('SAFETY_VIOLATION_UPDATE') && (
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => handleOpenEditViolation(row)}
          >
            Update
          </button>
        )
      )
    }
  ];

  return (
    <div className="page-container">
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2>Safety Management</h2>
          <p className="text-muted">OSHA/Safety inspections, hazard violation logs, and compliance scorecards</p>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          {activeTab === 'inspections' && hasPermission('SAFETY_INSPECTION_CREATE') && (
            <button className="btn btn-primary" onClick={() => setInspectionModalOpen(true)}>
              + Conduct Inspection
            </button>
          )}
          {activeTab === 'violations' && hasPermission('SAFETY_VIOLATION_CREATE') && (
            <button className="btn btn-danger" onClick={() => setViolationModalOpen(true)}>
              + Log Safety Violation
            </button>
          )}
        </div>
      </div>

      {message && (
        <div className={`alert alert-${message.type}`} style={{ marginBottom: '1rem' }}>
          {message.text}
          <button className="btn btn-link btn-sm" style={{ float: 'right' }} onClick={() => setMessage(null)}>✕</button>
        </div>
      )}

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '1rem', borderBottom: '2px solid var(--border-color)', marginBottom: '1rem' }}>
        <button
          className={`btn btn-link ${activeTab === 'inspections' ? 'active-tab' : ''}`}
          style={{
            padding: '0.75rem 1rem',
            fontWeight: activeTab === 'inspections' ? '600' : 'normal',
            borderBottom: activeTab === 'inspections' ? '3px solid var(--primary-color)' : 'none',
            borderRadius: 0,
            color: activeTab === 'inspections' ? 'var(--primary-color)' : 'inherit',
            textDecoration: 'none'
          }}
          onClick={() => setActiveTab('inspections')}
        >
          Site Inspections ({inspections.length})
        </button>
        <button
          className={`btn btn-link ${activeTab === 'violations' ? 'active-tab' : ''}`}
          style={{
            padding: '0.75rem 1rem',
            fontWeight: activeTab === 'violations' ? '600' : 'normal',
            borderBottom: activeTab === 'violations' ? '3px solid var(--primary-color)' : 'none',
            borderRadius: 0,
            color: activeTab === 'violations' ? 'var(--primary-color)' : 'inherit',
            textDecoration: 'none'
          }}
          onClick={() => setActiveTab('violations')}
        >
          Safety Violations ({violations.length})
        </button>
      </div>

      {/* Active Tab Content */}
      {activeTab === 'inspections' ? (
        <DataTable
          columns={inspectionColumns}
          data={inspections}
          loading={loading}
          searchPlaceholder="Search inspections by project, inspector..."
        />
      ) : (
        <DataTable
          columns={violationColumns}
          data={violations}
          loading={loading}
          searchPlaceholder="Search violations by severity, description..."
        />
      )}

      {/* Conduct Inspection Modal */}
      <Modal
        title="Conduct Safety Inspection"
        isOpen={inspectionModalOpen}
        onClose={() => setInspectionModalOpen(false)}
      >
        <form onSubmit={handleConductInspection}>
          <FormField label="Project *" required>
            <select
              className="form-control"
              value={inspectionForm.projectId}
              onChange={(e) => setInspectionForm({ ...inspectionForm, projectId: e.target.value })}
              required
            >
              <option value="">-- Select Project --</option>
              {projects.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </FormField>

          <FormField label="Inspection Date *" required>
            <input
              type="date"
              className="form-control"
              value={inspectionForm.inspectionDate}
              onChange={(e) => setInspectionForm({ ...inspectionForm, inspectionDate: e.target.value })}
              required
            />
          </FormField>

          <div style={{ background: 'var(--bg-light)', padding: '1rem', borderRadius: '4px', marginBottom: '1rem' }}>
            <h4 style={{ margin: '0 0 0.75rem 0' }}>Safety Checklist Scores (0 - 100)</h4>
            
            <FormField label={`1. PPE Compliance (${inspectionForm.ppeScore}%)`}>
              <input
                type="range"
                min="0"
                max="100"
                className="form-control"
                value={inspectionForm.ppeScore}
                onChange={(e) => setInspectionForm({ ...inspectionForm, ppeScore: e.target.value })}
              />
            </FormField>

            <FormField label={`2. Worker Safe Practices (${inspectionForm.workerSafetyScore}%)`}>
              <input
                type="range"
                min="0"
                max="100"
                className="form-control"
                value={inspectionForm.workerSafetyScore}
                onChange={(e) => setInspectionForm({ ...inspectionForm, workerSafetyScore: e.target.value })}
              />
            </FormField>

            <FormField label={`3. Heavy Equipment & Tools (${inspectionForm.equipmentScore}%)`}>
              <input
                type="range"
                min="0"
                max="100"
                className="form-control"
                value={inspectionForm.equipmentScore}
                onChange={(e) => setInspectionForm({ ...inspectionForm, equipmentScore: e.target.value })}
              />
            </FormField>

            <FormField label={`4. Work Area Hazards & Housekeeping (${inspectionForm.workAreaScore}%)`}>
              <input
                type="range"
                min="0"
                max="100"
                className="form-control"
                value={inspectionForm.workAreaScore}
                onChange={(e) => setInspectionForm({ ...inspectionForm, workAreaScore: e.target.value })}
              />
            </FormField>

            <FormField label={`5. Emergency Preparedness & First Aid (${inspectionForm.emergencyScore}%)`}>
              <input
                type="range"
                min="0"
                max="100"
                className="form-control"
                value={inspectionForm.emergencyScore}
                onChange={(e) => setInspectionForm({ ...inspectionForm, emergencyScore: e.target.value })}
              />
            </FormField>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem', borderTop: '1px solid var(--border-color)', paddingTop: '0.75rem' }}>
              <strong>Overall Average Score:</strong>
              <span className={`badge ${Number(computedAverage) >= 70 ? 'badge-success' : 'badge-danger'}`} style={{ fontSize: '1.1rem' }}>
                {computedAverage}% {Number(computedAverage) < 70 ? '(Urgent Warning Triggered)' : '(Pass)'}
              </span>
            </div>
          </div>

          <FormField label="Inspector Observations / Remarks">
            <textarea
              className="form-control"
              rows={3}
              value={inspectionForm.remarks}
              onChange={(e) => setInspectionForm({ ...inspectionForm, remarks: e.target.value })}
              placeholder="Notes on site conditions, specific safety hazards, or remedial instructions..."
            />
          </FormField>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setInspectionModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Submitting...' : 'Submit Inspection'}
            </button>
          </div>
        </form>
      </Modal>

      {/* View Checklist Details Modal */}
      <Modal
        title={`Inspection #${selectedInspection?.id} Details`}
        isOpen={viewInspectionModalOpen}
        onClose={() => setViewInspectionModalOpen(false)}
      >
        {selectedInspection && (
          <div>
            <p><strong>Project:</strong> {selectedInspection.projectName}</p>
            <p><strong>Inspector:</strong> {selectedInspection.inspectorName}</p>
            <p><strong>Date:</strong> {selectedInspection.inspectionDate}</p>
            <p><strong>Overall Score:</strong> <span className={`badge ${Number(selectedInspection.overallScore) >= 70 ? 'badge-success' : 'badge-danger'}`}>{selectedInspection.overallScore}%</span></p>

            <div style={{ background: 'var(--bg-light)', padding: '1rem', borderRadius: '4px', margin: '1rem 0' }}>
              <h4>Score Breakdown</h4>
              <p>PPE Compliance: <strong>{selectedInspection.ppeScore}%</strong></p>
              <p>Worker Safety: <strong>{selectedInspection.workerSafetyScore}%</strong></p>
              <p>Equipment Safety: <strong>{selectedInspection.equipmentScore}%</strong></p>
              <p>Work Area Hazards: <strong>{selectedInspection.workAreaScore}%</strong></p>
              <p>Emergency Readiness: <strong>{selectedInspection.emergencyScore}%</strong></p>
            </div>

            <p><strong>Inspector Remarks:</strong> {selectedInspection.remarks || 'None'}</p>

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="btn btn-secondary" onClick={() => setViewInspectionModalOpen(false)}>Close</button>
            </div>
          </div>
        )}
      </Modal>

      {/* Log Violation Modal */}
      <Modal
        title="Log Safety Violation"
        isOpen={violationModalOpen}
        onClose={() => setViolationModalOpen(false)}
      >
        <form onSubmit={handleLogViolation}>
          <FormField label="Project *" required>
            <select
              className="form-control"
              value={violationForm.projectId}
              onChange={(e) => setViolationForm({ ...violationForm, projectId: e.target.value })}
              required
            >
              <option value="">-- Select Project --</option>
              {projects.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </FormField>

          <FormField label="Involved Worker (Optional)">
            <select
              className="form-control"
              value={violationForm.workerId}
              onChange={(e) => setViolationForm({ ...violationForm, workerId: e.target.value })}
            >
              <option value="">-- General Site / Not Worker-Specific --</option>
              {workers.map((w) => (
                <option key={w.id} value={w.id}>{w.fullName} ({w.trade})</option>
              ))}
            </select>
          </FormField>

          <FormField label="Severity Level *" required>
            <select
              className="form-control"
              value={violationForm.severity}
              onChange={(e) => setViolationForm({ ...violationForm, severity: e.target.value })}
              required
            >
              <option value="CRITICAL">CRITICAL (Immediate site hazard / Work stop)</option>
              <option value="MAJOR">MAJOR (Substantial safety risk)</option>
              <option value="MEDIUM">MEDIUM (Standard non-compliance)</option>
              <option value="MINOR">MINOR (Minor infraction / Warning)</option>
            </select>
          </FormField>

          <FormField label="Violation Description *" required>
            <textarea
              className="form-control"
              rows={3}
              value={violationForm.description}
              onChange={(e) => setViolationForm({ ...violationForm, description: e.target.value })}
              placeholder="Detail the hazard, missing PPE, unguarded machinery, etc."
              required
            />
          </FormField>

          <FormField label="Corrective Action Required">
            <textarea
              className="form-control"
              rows={2}
              value={violationForm.correctiveAction}
              onChange={(e) => setViolationForm({ ...violationForm, correctiveAction: e.target.value })}
              placeholder="Immediate remediation instructions..."
            />
          </FormField>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setViolationModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-danger" disabled={submitting}>
              {submitting ? 'Logging...' : 'Log Violation'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Edit / Resolve Violation Modal */}
      <Modal
        title={`Update Safety Violation #${selectedViolation?.id}`}
        isOpen={editViolationModalOpen}
        onClose={() => setEditViolationModalOpen(false)}
      >
        <form onSubmit={handleUpdateViolation}>
          <FormField label="Status *">
            <select
              className="form-control"
              value={editViolationForm.status}
              onChange={(e) => setEditViolationForm({ ...editViolationForm, status: e.target.value })}
            >
              <option value="OPEN">OPEN (Remediation Pending)</option>
              <option value="IN_PROGRESS">IN_PROGRESS (Remediation Ongoing)</option>
              <option value="RESOLVED">RESOLVED (Remediated & Verified)</option>
            </select>
          </FormField>

          <FormField label="Corrective Action Taken">
            <textarea
              className="form-control"
              rows={3}
              value={editViolationForm.correctiveAction}
              onChange={(e) => setEditViolationForm({ ...editViolationForm, correctiveAction: e.target.value })}
              placeholder="What actions were taken to fix this violation?"
            />
          </FormField>

          {editViolationForm.status === 'RESOLVED' && (
            <FormField label="Resolution Date">
              <input
                type="date"
                className="form-control"
                value={editViolationForm.resolutionDate}
                onChange={(e) => setEditViolationForm({ ...editViolationForm, resolutionDate: e.target.value })}
              />
            </FormField>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setEditViolationModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
