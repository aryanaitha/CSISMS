import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';
import Modal from '../components/Modal';
import FormField from '../components/FormField';

const NON_ADMIN_ROLES = [
  { value: 2, label: 'Project Manager' },
  { value: 3, label: 'Site Engineer' },
  { value: 4, label: 'Safety Officer' },
  { value: 5, label: 'Inventory Manager' },
  { value: 6, label: 'Equipment Manager' },
  { value: 7, label: 'Contractor' },
  { value: 8, label: 'Worker' }
];

const MODULE_LIST = [
  { name: 'Projects', desc: 'Track budgets, milestones, progress, and overall site delivery.' },
  { name: 'Workforce', desc: 'Manage tradesmen, subcontractors, and site labor allocations.' },
  { name: 'Tasks', desc: 'Coordinate site activities with priorities, stages, and deadlines.' },
  { name: 'Attendance', desc: 'Daily worker check-ins, timekeeping, and presence verification.' },
  { name: 'Inventory', desc: 'Material catalogs, reorder thresholds, receipts, and issues.' },
  { name: 'Equipment', desc: 'Heavy machinery tracking, fleet allocation, and status monitoring.' },
  { name: 'Maintenance', desc: 'Preventative servicing, repair scheduling, and work order tracking.' },
  { name: 'Safety', desc: 'OSHA 5-factor inspections, hazard ratings, and compliance audits.' },
  { name: 'Incidents', desc: 'Accident reporting, root cause investigation, and corrective action.' },
  { name: 'Reports', desc: 'Real-time summaries, data previews, and RFC-4180 CSV exports.' }
];

const ROLE_NAMES = [
  'Administrator',
  'Project Manager',
  'Site Engineer',
  'Safety Officer',
  'Inventory Manager',
  'Equipment Manager',
  'Contractor',
  'Worker'
];

// Lightweight hook for scroll-triggered entrance animations
function useInView(options = {}) {
  const [inView, setInView] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    if (typeof IntersectionObserver === 'undefined') {
      setInView(true);
      return;
    }
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        setInView(true);
        observer.disconnect();
      }
    }, { threshold: 0.15, ...options });

    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  return [ref, inView];
}

// Lightweight hook for single smooth number count-up (<800ms)
function useCountUp(target, duration = 650, active = true) {
  const [val, setVal] = useState(0);

  useEffect(() => {
    if (!active) return;
    let startTime = null;
    let frameId;

    const step = (now) => {
      if (!startTime) startTime = now;
      const progress = Math.min((now - startTime) / duration, 1);
      // Ease-out cubic
      const current = Math.round((1 - Math.pow(1 - progress, 3)) * target);
      setVal(current);
      if (progress < 1) {
        frameId = requestAnimationFrame(step);
      } else {
        setVal(target);
      }
    };

    frameId = requestAnimationFrame(step);
    return () => cancelAnimationFrame(frameId);
  }, [target, duration, active]);

  return val;
}

export default function Landing() {
  const navigate = useNavigate();

  // Health Status
  const [serverOnline, setServerOnline] = useState(null);
  const [dbConnected, setDbConnected] = useState(null);
  const [healthChecking, setHealthChecking] = useState(true);

  // Request Access Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [reqFullName, setReqFullName] = useState('');
  const [reqEmail, setReqEmail] = useState('');
  const [reqUsername, setReqUsername] = useState('');
  const [reqRole, setReqRole] = useState(2);
  const [reqReason, setReqReason] = useState('');
  const [reqStatus, setReqStatus] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // Scroll In-View Refs
  const [modulesRef, modulesInView] = useInView();
  const [rolesRef, rolesInView] = useInView();
  const [previewRef, previewInView] = useInView();

  // Animated Numbers for Live Preview Strip
  const activeProjectsCount = useCountUp(3, 700, previewInView);
  const openViolationsCount = useCountUp(1, 600, previewInView);
  const lowStockCount = useCountUp(2, 650, previewInView);

  useEffect(() => {
    api.get('/health')
      .then((res) => {
        setServerOnline(true);
        if (res && res.success && res.data && res.data.database === 'CONNECTED') {
          setDbConnected(true);
        } else {
          setDbConnected(false);
        }
      })
      .catch(() => {
        setServerOnline(false);
        setDbConnected(false);
      })
      .finally(() => setHealthChecking(false));
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
        text: res.message || 'Access request submitted. An administrator will review your submission.'
      });

      setReqFullName('');
      setReqEmail('');
      setReqUsername('');
      setReqReason('');
    } catch (err) {
      setReqStatus({
        type: 'danger',
        text: err.message || 'Failed to submit registration request'
      });
    } finally {
      setSubmitting(false);
    }
  };

  const isSystemReady = dbConnected === true;

  return (
    <div className="landing-page">
      {/* 1. Minimal Top Bar */}
      <header className="landing-topbar">
        <div className="landing-topbar-brand">
          <span className="brand-logo-text">CSIMS</span>
          <span className="brand-subtext">Construction Site Intelligence & Safety Management</span>
        </div>
        <div className="landing-topbar-actions">
          <button
            className="btn btn-primary"
            onClick={() => navigate('/login')}
            disabled={!isSystemReady && !healthChecking}
          >
            Login
          </button>
          <button
            className="btn btn-secondary"
            onClick={() => setIsModalOpen(true)}
          >
            Request Access
          </button>
        </div>
      </header>

      {/* 2. Hero Section */}
      <main className="landing-content">
        <section className="landing-hero">
          <h1 className="hero-headline landing-anim-fade-up-1">
            Manage projects, people, materials, equipment and safety in one place.
          </h1>
          <p className="hero-subtitle landing-anim-fade-up-2">
            A high-performance intelligence and governance platform built for rigorous field coordination and real-time OSHA safety compliance.
          </p>

          <div className="hero-actions landing-anim-fade-up-3">
            <button
              className="btn btn-primary btn-lg"
              onClick={() => navigate('/login')}
              disabled={!isSystemReady && !healthChecking}
            >
              Login to Platform
            </button>
            <button
              className="btn btn-secondary btn-lg"
              onClick={() => setIsModalOpen(true)}
            >
              Request Access
            </button>
          </div>

          {/* Live System & DB Status Indicator */}
          <div className="landing-status-bar landing-anim-fade-up-4">
            <div className="status-indicator-group">
              <span className={`status-dot ${serverOnline ? 'online' : healthChecking ? 'checking' : 'offline'}`} />
              <span className="status-label">
                Server: {healthChecking ? 'Checking...' : serverOnline ? 'Connected' : 'Not connected'}
              </span>
            </div>
            <div className="status-indicator-group">
              <span className={`status-dot ${dbConnected ? 'online' : healthChecking ? 'checking' : 'offline'}`} />
              <span className="status-label">
                Database: {healthChecking ? 'Checking...' : dbConnected ? 'Connected' : 'Not connected'}
              </span>
            </div>
            {!healthChecking && !isSystemReady && (
              <p className="status-alert-text">
                Database connection unavailable. Please verify MySQL service and application.properties.
              </p>
            )}
          </div>
        </section>

        {/* 3. Key Modules Grid */}
        <section className="landing-section" ref={modulesRef}>
          <div className="section-header">
            <span className="section-eyebrow">Enterprise Architecture</span>
            <h2 className="section-title">Key Operational Modules</h2>
          </div>

          <div className={`modules-grid ${modulesInView ? 'in-view' : ''}`}>
            {MODULE_LIST.map((m, idx) => (
              <div
                key={m.name}
                className="module-card"
                style={{ '--anim-order': idx }}
              >
                <h3 className="module-name">{m.name}</h3>
                <p className="module-desc">{m.desc}</p>
              </div>
            ))}
          </div>
        </section>

        {/* 4. Who Uses It: 8 Role Chips */}
        <section className="landing-section" ref={rolesRef}>
          <div className="section-header">
            <span className="section-eyebrow">Role-Based Access Control</span>
            <h2 className="section-title">Who Uses CSIMS</h2>
          </div>

          <div className={`roles-chip-row ${rolesInView ? 'in-view' : ''}`}>
            {ROLE_NAMES.map((r, idx) => (
              <span
                key={r}
                className="role-chip"
                style={{ '--anim-order': idx }}
              >
                {r}
              </span>
            ))}
          </div>
        </section>

        {/* 5. Mini Live Preview Strip (CSS Segmented Bar + Stat Cards) */}
        <section className="landing-section" ref={previewRef}>
          <div className="section-header">
            <span className="section-eyebrow">Live Dashboard Intelligence</span>
            <h2 className="section-title">Card Dashboard Preview</h2>
          </div>

          <div className={`preview-strip-container ${previewInView ? 'in-view' : ''}`}>
            <div className="preview-stat-cards">
              <div className="preview-stat-card">
                <span className="preview-stat-label">Active Projects</span>
                <div className="preview-stat-val-group">
                  <span className="preview-stat-value">{activeProjectsCount}</span>
                  <span className="preview-stat-unit">sites</span>
                </div>
              </div>

              <div className="preview-stat-card">
                <span className="preview-stat-label">Open Violations</span>
                <div className="preview-stat-val-group">
                  <span className="preview-stat-value">{openViolationsCount}</span>
                  <span className="preview-stat-unit">item</span>
                </div>
              </div>

              <div className="preview-stat-card">
                <span className="preview-stat-label">Low Stock Items</span>
                <div className="preview-stat-val-group">
                  <span className="preview-stat-value">{lowStockCount}</span>
                  <span className="preview-stat-unit">materials</span>
                </div>
              </div>
            </div>

            {/* Segmented Progress Bar */}
            <div className="preview-progress-box">
              <div className="preview-progress-header">
                <span className="preview-progress-title">Aggregate Project Execution</span>
                <span className="preview-progress-pct">65%</span>
              </div>
              <div className="preview-segmented-bar">
                {Array.from({ length: 20 }).map((_, i) => (
                  <div
                    key={i}
                    className={`progress-bar-segment ${previewInView && i < 13 ? 'segment-filled' : ''}`}
                    style={{ '--segment-index': i }}
                  />
                ))}
              </div>
            </div>
          </div>
        </section>
      </main>

      {/* 6. Footer */}
      <footer className="landing-footer">
        <p>Construction Site Intelligence & Safety Management System &bull; v1.0.0 &bull; Mini Project</p>
      </footer>

      {/* Request Access Modal (Existing unchanged logic) */}
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
          <div className={`alert alert-${reqStatus.type}`}>
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
            placeholder="e.g. David Ross"
          />
          <FormField
            label="Email Address"
            name="email"
            type="email"
            value={reqEmail}
            onChange={(e) => setReqEmail(e.target.value)}
            required
            placeholder="e.g. david@contractor.com"
          />
          <FormField
            label="Desired Username"
            name="username"
            value={reqUsername}
            onChange={(e) => setReqUsername(e.target.value)}
            required
            placeholder="e.g. david_ross"
          />
          <FormField
            label="Requested Role"
            name="role"
            type="select"
            value={reqRole}
            onChange={(e) => setReqRole(e.target.value)}
            options={NON_ADMIN_ROLES}
            required
          />
          <FormField
            label="Reason for Access"
            name="reason"
            type="textarea"
            value={reqReason}
            onChange={(e) => setReqReason(e.target.value)}
            required
            placeholder="Describe your role on site or project requirements..."
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '18px' }}>
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
