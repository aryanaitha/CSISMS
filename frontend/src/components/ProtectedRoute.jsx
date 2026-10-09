import React, { useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Modal from './Modal';
import FormField from './FormField';

export default function ProtectedRoute({ children, requiredPermission, requiredRole }) {
  const { user, loading, forcePasswordChange, changePassword, logout } = useAuth();
  const location = useLocation();

  // State for forced password change modal
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [pwdError, setPwdError] = useState('');
  const [pwdLoading, setPwdLoading] = useState(false);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <p>Loading application...</p>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const handleForcedPasswordSubmit = async (e) => {
    e.preventDefault();
    setPwdError('');

    if (newPassword.length < 8) {
      setPwdError('New password must be at least 8 characters long.');
      return;
    }
    if (!/[a-zA-Z]/.test(newPassword) || !/[0-9]/.test(newPassword)) {
      setPwdError('New password must contain at least one letter and one number.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setPwdError('New passwords do not match.');
      return;
    }
    if (newPassword === oldPassword) {
      setPwdError('New password must differ from current password.');
      return;
    }

    setPwdLoading(true);
    try {
      await changePassword(oldPassword, newPassword);
    } catch (err) {
      setPwdError(err.message || 'Failed to update password');
    } finally {
      setPwdLoading(false);
    }
  };

  // If user must change password, block view and force change
  if (forcePasswordChange || user.mustChangePassword) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--color-bg)' }}>
        <Modal
          isOpen={true}
          title="Password Change Required"
          footer={null}
          maxWidth="460px"
        >
          <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>
            Your account requires a password change before proceeding to the system.
          </p>

          {pwdError && <div className="alert alert-danger" style={{ marginBottom: '1rem' }}>{pwdError}</div>}

          <form onSubmit={handleForcedPasswordSubmit}>
            <FormField
              label="Current / Temporary Password"
              name="oldPassword"
              type="password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
              required
            />
            <FormField
              label="New Password (min 8 chars, 1 letter, 1 number)"
              name="newPassword"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
            <FormField
              label="Confirm New Password"
              name="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />

            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '1.5rem' }}>
              <button type="button" className="btn btn-secondary" onClick={logout}>
                Cancel & Logout
              </button>
              <button type="submit" className="btn btn-primary" disabled={pwdLoading}>
                {pwdLoading ? 'Saving...' : 'Set New Password'}
              </button>
            </div>
          </form>
        </Modal>
      </div>
    );
  }

  // Permission and role checks
  if (user.roleName !== 'Administrator') {
    if (requiredRole && user.roleName !== requiredRole) {
      return (
        <div style={{ padding: '2rem' }}>
          <div className="alert alert-danger">
            <strong>Access Denied:</strong> This page requires the <code>{requiredRole}</code> role.
          </div>
        </div>
      );
    }

    if (requiredPermission && (!user.permissions || !user.permissions.includes(requiredPermission))) {
      return (
        <div style={{ padding: '2rem' }}>
          <div className="alert alert-danger">
            <strong>Access Denied:</strong> You do not have permission (<code>{requiredPermission}</code>) to view this page.
          </div>
        </div>
      );
    }
  }

  return children;
}
