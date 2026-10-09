import React, { useState, useEffect } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import useIdleTimer from '../hooks/useIdleTimer';
import api from '../api/api';
import Modal from './Modal';
import FormField from './FormField';
import ConfirmDialog from './ConfirmDialog';

export default function Layout() {
  const { user, logout, hasPermission, changePassword } = useAuth();
  const navigate = useNavigate();

  // Idle session manager
  useIdleTimer(() => {
    alert('Session expired due to 15 minutes of inactivity.');
    logout().then(() => navigate('/login'));
  });

  // Topbar Modals
  const [logoutConfirmOpen, setLogoutConfirmOpen] = useState(false);
  const [changePwdOpen, setChangePwdOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);

  // Notifications State
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);

  // Change Password state
  const [oldPwd, setOldPwd] = useState('');
  const [newPwd, setNewPwd] = useState('');
  const [confirmPwd, setConfirmPwd] = useState('');
  const [pwdMsg, setPwdMsg] = useState(null);
  const [pwdSubmitting, setPwdSubmitting] = useState(false);

  const fetchUnreadCount = async () => {
    if (!user) return;
    try {
      const res = await api.get('/notifications/unread-count');
      if (res && res.success && res.data) {
        setUnreadCount(res.data.unreadCount || 0);
      }
    } catch (e) {
      // Ignore polling errors
    }
  };

  const fetchNotifications = async () => {
    try {
      const res = await api.get('/notifications');
      if (res && res.success && res.data) {
        setNotifications(res.data);
      }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchUnreadCount();
    const interval = setInterval(fetchUnreadCount, 30000); // Poll every 30s
    return () => clearInterval(interval);
  }, [user]);

  const handleOpenNotifications = () => {
    fetchNotifications();
    setNotifOpen(true);
  };

  const handleMarkAsRead = async (id) => {
    try {
      await api.post(`/notifications/${id}/read`);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, readStatus: true } : n));
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (e) {
      console.error(e);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await api.post('/notifications/read-all');
      setNotifications(prev => prev.map(n => ({ ...n, readStatus: true })));
      setUnreadCount(0);
    } catch (e) {
      console.error(e);
    }
  };

  const handleChangePasswordSubmit = async (e) => {
    e.preventDefault();
    setPwdMsg(null);

    if (newPwd.length < 8) {
      setPwdMsg({ type: 'danger', text: 'Password must be at least 8 characters long.' });
      return;
    }
    if (!/[a-zA-Z]/.test(newPwd) || !/[0-9]/.test(newPwd)) {
      setPwdMsg({ type: 'danger', text: 'Password must contain at least one letter and one number.' });
      return;
    }
    if (newPwd !== confirmPwd) {
      setPwdMsg({ type: 'danger', text: 'New passwords do not match.' });
      return;
    }
    if (newPwd === oldPwd) {
      setPwdMsg({ type: 'danger', text: 'New password must differ from current password.' });
      return;
    }

    setPwdSubmitting(true);
    try {
      await changePassword(oldPwd, newPwd);
      setPwdMsg({ type: 'success', text: 'Password changed successfully.' });
      setOldPwd('');
      setNewPwd('');
      setConfirmPwd('');
    } catch (err) {
      setPwdMsg({ type: 'danger', text: err.message || 'Failed to change password' });
    } finally {
      setPwdSubmitting(false);
    }
  };

  const navItems = [
    { label: 'Dashboard', path: '/dashboard', show: true },
    { label: 'Projects', path: '/projects', show: hasPermission('PROJECT_VIEW') },
    { label: 'Workforce', path: '/workforce', show: hasPermission('WORKFORCE_VIEW') },
    { label: 'Tasks', path: '/tasks', show: hasPermission('TASK_VIEW') },
    { label: 'Attendance', path: '/attendance', show: hasPermission('ATTENDANCE_VIEW') },
    { label: 'Inventory', path: '/inventory', show: hasPermission('INVENTORY_VIEW') },
    { label: 'Equipment', path: '/equipment', show: hasPermission('EQUIPMENT_VIEW') },
    { label: 'Maintenance', path: '/maintenance', show: hasPermission('MAINTENANCE_VIEW') },
    { label: 'Safety Inspections', path: '/safety', show: hasPermission('SAFETY_INSPECTION_VIEW') || hasPermission('SAFETY_VIOLATION_VIEW') },
    { label: 'Incidents', path: '/incidents', show: hasPermission('INCIDENT_VIEW') },
    { label: 'Reports', path: '/reports', show: hasPermission('REPORT_VIEW') },
    { label: 'Audit Log', path: '/audit-logs', show: hasPermission('AUDIT_VIEW') },
    { label: 'User Management', path: '/users', show: hasPermission('USER_VIEW') },
    { label: 'Roles & Permissions', path: '/roles', show: hasPermission('ROLE_VIEW') },
    { label: 'Account Requests', path: '/account-requests', show: hasPermission('ACCOUNT_REQUEST_VIEW') }
  ];

  return (
    <div className="app-shell">
      {/* Topbar */}
      <header className="topbar">
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <h1 className="brand-title">CSIMS</h1>
          <span style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
            Construction Site Intelligence
          </span>
        </div>

        <div className="topbar-actions">
          {/* Notification Button */}
          <button
            className="btn btn-secondary btn-sm"
            onClick={handleOpenNotifications}
            style={{ position: 'relative' }}
            title="Notifications"
          >
            Notifications
            {unreadCount > 0 && (
              <span
                style={{
                  background: 'var(--color-danger)',
                  color: '#fff',
                  borderRadius: '10px',
                  padding: '2px 6px',
                  fontSize: '0.7rem',
                  marginLeft: '0.4rem',
                  fontWeight: 'bold'
                }}
              >
                {unreadCount}
              </span>
            )}
          </button>

          {/* User profile & role badge */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ fontSize: '0.875rem', fontWeight: 500 }}>
              {user?.fullName || user?.username}
            </span>
            <span className="badge badge-primary">{user?.roleName}</span>
          </div>

          {/* Change Password */}
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => {
              setChangePwdOpen(true);
              setPwdMsg(null);
            }}
          >
            Change Password
          </button>

          {/* Logout */}
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => setLogoutConfirmOpen(true)}
          >
            Logout
          </button>
        </div>
      </header>

      {/* Main Body with Sidebar + Outlet */}
      <div className="main-wrapper">
        <aside className="sidebar">
          <nav className="nav-menu">
            {navItems.filter(item => item.show).map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </aside>

        <main className="content-area">
          <Outlet />
        </main>
      </div>

      {/* Confirm Logout Dialog */}
      <ConfirmDialog
        isOpen={logoutConfirmOpen}
        onClose={() => setLogoutConfirmOpen(false)}
        onConfirm={async () => {
          await logout();
          navigate('/login');
        }}
        title="Confirm Logout"
        message="Are you sure you want to end your current session and log out?"
        confirmText="Log Out"
      />

      {/* Change Password Modal */}
      <Modal
        isOpen={changePwdOpen}
        onClose={() => {
          setChangePwdOpen(false);
          setPwdMsg(null);
        }}
        title="Change Password"
        maxWidth="440px"
      >
        {pwdMsg && (
          <div className={`alert alert-${pwdMsg.type}`}>
            {pwdMsg.text}
          </div>
        )}

        <form onSubmit={handleChangePasswordSubmit}>
          <FormField
            label="Current Password"
            name="oldPwd"
            type="password"
            value={oldPwd}
            onChange={(e) => setOldPwd(e.target.value)}
            required
          />
          <FormField
            label="New Password"
            name="newPwd"
            type="password"
            value={newPwd}
            onChange={(e) => setNewPwd(e.target.value)}
            required
            placeholder="Min 8 chars, 1 letter, 1 number"
          />
          <FormField
            label="Confirm New Password"
            name="confirmPwd"
            type="password"
            value={confirmPwd}
            onChange={(e) => setConfirmPwd(e.target.value)}
            required
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => {
                setChangePwdOpen(false);
                setPwdMsg(null);
              }}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={pwdSubmitting}>
              {pwdSubmitting ? 'Updating...' : 'Update Password'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Notifications Drawer/Modal */}
      <Modal
        isOpen={notifOpen}
        onClose={() => setNotifOpen(false)}
        title="Notifications"
        maxWidth="500px"
        footer={
          <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
            <button className="btn btn-secondary btn-sm" onClick={handleMarkAllRead}>
              Mark all as read
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => setNotifOpen(false)}>
              Close
            </button>
          </div>
        }
      >
        {notifications.length === 0 ? (
          <p style={{ color: 'var(--color-text-muted)', textAlign: 'center', margin: '2rem 0' }}>
            No notifications found.
          </p>
        ) : (
          <div style={{ maxHeight: '360px', overflowY: 'auto' }}>
            {notifications.map((n) => (
              <div
                key={n.id}
                style={{
                  padding: '0.75rem',
                  borderBottom: '1px solid var(--color-border)',
                  backgroundColor: n.readStatus ? 'transparent' : 'var(--color-table-zebra)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-start'
                }}
              >
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                    <span className={`badge badge-${n.type === 'DANGER' ? 'danger' : n.type === 'WARNING' ? 'warning' : 'primary'}`}>
                      {n.type}
                    </span>
                    <strong style={{ fontSize: '0.9rem' }}>{n.title}</strong>
                  </div>
                  <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--color-text)' }}>
                    {n.message}
                  </p>
                  <small style={{ color: 'var(--color-text-muted)', fontSize: '0.75rem' }}>
                    {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
                  </small>
                </div>
                {!n.readStatus && (
                  <button
                    className="btn btn-secondary btn-sm"
                    style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem' }}
                    onClick={() => handleMarkAsRead(n.id)}
                  >
                    Read
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </Modal>
    </div>
  );
}
