import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';
import Modal from '../components/Modal';
import FormField from '../components/FormField';

const MODULES = [
  'Projects', 'Workforce', 'Tasks', 'Attendance', 'Inventory',
  'Equipment', 'Maintenance', 'Safety Inspections', 'Incidents', 'Reports & Audits'
];

const ROLES = [
  { value: 2, label: 'Project Manager' },
  { value: 3, label: 'Site Engineer' },
  { value: 4, label: 'Safety Officer' },
  { value: 5, label: 'Inventory Manager' },
  { value: 6, label: 'Equipment Manager' },
  { value: 7, label: 'Contractor' },
  { value: 8, label: 'Worker' }
];

export default function Landing() {
  const navigate = useNavigate();
  const [dbConnected, setDbConnected] = useState(null);
  const [dbLoading, setDbLoading] = useState(true);

  // Request Access Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [reqFullName, setReqFullName] = useState('');
  const [reqEmail, setReqEmail] = useState('');
  const [reqUsername, setReqUsername] = useState('');
  const [reqRole, setReqRole] = useState(2);
  const [reqReason, setReqReason] = useState('');
  const [reqStatus, setReqStatus] = useState(null); // { type: 'success'|'error', text: '' }
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    api.get('/health')
      .then((res) => {
        if (res && res.success && res.data && res.data.database === 'CONNECTED') {
          setDbConnected(true);
        } else {
          setDbConnected(false);
        }
      })
      .catch(() => setDbConnected(false))
      .finally(() => setDbLoading(false));
  }, []);

  const handleRequestSubmit = async (e) => {
    e.preventDefault();
    setReqStatus(null);
    setSubmitting(true);

    try {
      const res = await api.post('/auth/register-request', {
        fullName: reqFullName,
        email: reqEmail,
        username: reqUsername,
        requestedRoleId: Number(reqRole),
        reason: reqReason
      });

      setReqStatus({
        type: 'success',
        text: res.message || 'Request submitted successfully. An administrator will review it.'
      });

      // Reset form
      setReqFullName('');
      setReqEmail('');
      setReqUsername('');
      setReqReason('');
    } catch (err) {
      setReqStatus({
        type: 'error',
        text: err.message || 'Failed to submit registration request'
      });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="landing-container">
      <div className="landing-card">
        <h1 className="landing-title">
          Construction Site Intelligence & Safety Management System
        </h1>
        <p className="landing-tagline">
          Manage projects, people, materials, equipment and safety in one place.
        </p>

        {/* Database Status indicator */}
        <div style={{ marginBottom: '1.5rem', textAlign: 'center' }}>
          {dbLoading ? (
            <span className="badge badge-secondary">Checking Database...</span>
          ) : dbConnected ? (
            <span className="badge badge-success">Database: Connected</span>
          ) : (
            <div>
              <span className="badge badge-danger">Database: Not Connected</span>
              <p style={{ color: 'var(--color-danger)', fontSize: '0.8rem', marginTop: '0.25rem' }}>
                Please check database server and configuration in application.properties.
              </p>
            </div>
          )}
        </div>

        {/* Key Modules section */}
        <div className="landing-section">
          <h3>Key Modules</h3>
          <div className="modules-grid">
            {MODULES.map((mod) => (
              <div key={mod} className="module-item">
                • {mod}
              </div>
            ))}
          </div>
        </div>

        {/* Who uses it section */}
        <div className="landing-section">
          <h3>Who uses it</h3>
          <p className="roles-line">
            Administrator, Project Manager, Site Engineer, Safety Officer, Inventory Manager, Equipment Manager, Contractor, Worker
          </p>
        </div>

        {/* Action Buttons */}
        <div className="landing-actions">
          <button
            className="btn btn-primary"
            onClick={() => navigate('/login')}
            disabled={dbConnected === false}
          >
            Login
          </button>
          <button
            className="btn btn-secondary"
            onClick={() => setIsModalOpen(true)}
            disabled={dbConnected === false}
          >
            Request Access
          </button>
          <button
            className="btn btn-secondary"
            onClick={() => window.close()}
          >
            Exit
          </button>
        </div>

        <div className="landing-footer">
          CSIMS v1.0 • Mini Project • Full-Stack Web Architecture
        </div>
      </div>

      {/* Request Access Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setReqStatus(null);
        }}
        title="Request System Access"
        maxWidth="480px"
      >
        {reqStatus && (
          <div className={`alert alert-${reqStatus.type === 'success' ? 'success' : 'danger'}`}>
            {reqStatus.text}
          </div>
        )}

        <form onSubmit={handleRequestSubmit}>
          <FormField
            label="Full Name"
            name="fullName"
            value={reqFullName}
            onChange={(e) => setReqFullName(e.target.value)}
            required
            placeholder="e.g. Jane Doe"
          />
          <FormField
            label="Email Address"
            name="email"
            type="email"
            value={reqEmail}
            onChange={(e) => setReqEmail(e.target.value)}
            required
            placeholder="e.g. jane@company.com"
          />
          <FormField
            label="Desired Username"
            name="username"
            value={reqUsername}
            onChange={(e) => setReqUsername(e.target.value)}
            required
            placeholder="e.g. jane_doe"
          />
          <FormField
            label="Requested Role"
            name="role"
            type="select"
            value={reqRole}
            onChange={(e) => setReqRole(e.target.value)}
            options={ROLES}
            required
          />
          <FormField
            label="Reason for Access"
            name="reason"
            type="textarea"
            value={reqReason}
            onChange={(e) => setReqReason(e.target.value)}
            required
            placeholder="Explain why you require system access..."
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.5rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => {
                setIsModalOpen(false);
                setReqStatus(null);
              }}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Submitting...' : 'Submit Request'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
