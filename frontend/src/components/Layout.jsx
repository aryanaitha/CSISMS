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

  // Idle session manager (15 min)
  useIdleTimer(() => {
    alert('Session expired due to 15 minutes of inactivity.');
    logout().then(() => navigate('/login'));
  });

  // Topbar Modals
  const [logoutConfirmOpen, setLogoutConfirmOpen] = useState(false);
  const [changePwdOpen, setChangePwdOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);
  const [accountMenuOpen, setAccountMenuOpen] = useState(false);

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
    const interval = setInterval(fetchUnreadCount, 30000);
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
    { label: 'Safety', path: '/safety', show: hasPermission('SAFETY_INSPECTION_VIEW') || hasPermission('SAFETY_VIOLATION_VIEW') },
    { label: 'Incidents', path: '/incidents', show: hasPermission('INCIDENT_VIEW') },
    { label: 'Reports', path: '/reports', show: hasPermission('REPORT_VIEW') },
    { label: 'Audit Logs', path: '/audit-logs', show: hasPermission('AUDIT_VIEW') },
    { label: 'Users', path: '/users', show: hasPermission('USER_VIEW') },
    { label: 'Roles', path: '/roles', show: hasPermission('ROLE_VIEW') },
    { label: 'Requests', path: '/account-requests', show: hasPermission('ACCOUNT_REQUEST_VIEW') }
  ];

  return (
    <div className="app-shell">
      {/* Topbar */}
      <header className="topbar">
        <div className="topbar-left">
          <span className="brand-pill">CSIMS</span>
          <span className="topbar-user-info">
            Logged in as <strong>{user?.fullName || user?.username}</strong> ({user?.roleName})
          </span>
        </div>

        <div className="topbar-right">
          {/* Notification Button with Pill Count */}
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={handleOpenNotifications}
            title="Notifications"
          >
            Notifications
            {unreadCount > 0 && (
              <span className="pill pill-teal" style={{ marginLeft: '6px' }}>
                {unreadCount}
              </span>
            )}
          </button>

          {/* Change Password Button */}
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => {
              setChangePwdOpen(true);
              setPwdMsg(null);
            }}
            title="Change Password"
          >
            Change Password
          </button>

          {/* Direct Sign Out Button */}
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            style={{ color: '#9c2828' }}
            onClick={() => setLogoutConfirmOpen(true)}
            title="Sign Out"
          >
            Sign Out
          </button>
        </div>
      </header>

      {/* Main Body with Narrow Text-Only Sidebar + Outlet */}
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
        title="Confirm Sign Out"
        message="Are you sure you want to end your current session and sign out?"
        confirmText="Sign Out"
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

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '16px' }}>
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

      {/* Notifications Modal */}
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
          <p style={{ color: 'var(--muted)', textAlign: 'center', margin: '24px 0' }}>
            No notifications found.
          </p>
        ) : (
          <div style={{ maxHeight: '360px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {notifications.map((n) => (
              <div
                key={n.id}
                style={{
                  padding: '12px 14px',
                  borderRadius: '12px',
                  border: '1px solid var(--border)',
                  background: n.readStatus ? 'transparent' : 'var(--accent-soft)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-start',
                  gap: '12px'
                }}
              >
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                    <span className="pill pill-neutral">
                      {n.type}
                    </span>
                    <strong style={{ fontSize: '13px' }}>{n.title}</strong>
                  </div>
                  <p style={{ margin: 0, fontSize: '12px', color: 'var(--text)' }}>
                    {n.message}
                  </p>
                  <small style={{ color: 'var(--muted)', fontSize: '11px' }}>
                    {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
                  </small>
                </div>
                {!n.readStatus && (
                  <button
                    className="btn btn-secondary btn-sm"
                    style={{ fontSize: '11px', padding: '3px 8px' }}
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
