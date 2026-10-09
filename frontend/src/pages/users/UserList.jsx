import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';
import ConfirmDialog from '../../components/ConfirmDialog';

export default function UserList() {
  const { hasPermission } = useAuth();
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  // Create User Modal
  const [createOpen, setCreateOpen] = useState(false);
  const [createForm, setCreateForm] = useState({
    username: '',
    email: '',
    fullName: '',
    roleId: 8,
    password: 'Welcome@123'
  });

  // Edit User Modal
  const [editOpen, setEditOpen] = useState(false);
  const [editUser, setEditUser] = useState(null);

  // Reset Password Dialog
  const [resetOpen, setResetOpen] = useState(false);
  const [resetTargetUser, setResetTargetUser] = useState(null);
  const [newPasswordInput, setNewPasswordInput] = useState('Reset@12345');

  // Status Toggle Dialog
  const [statusDialog, setStatusDialog] = useState({ open: false, user: null });

  const fetchUsers = async () => {
    try {
      const [uRes, rRes] = await Promise.all([
        api.get('/users'),
        api.get('/roles')
      ]);
      if (uRes.success) setUsers(uRes.data);
      if (rRes.success) setRoles(rRes.data.map(r => ({ value: r.id, label: r.name })));
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to fetch users' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/users', createForm);
      setMessage({ type: 'success', text: `User ${createForm.username} created successfully with temporary password.` });
      setCreateOpen(false);
      setCreateForm({ username: '', email: '', fullName: '', roleId: 8, password: 'Welcome@123' });
      fetchUsers();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to create user' });
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.put(`/users/${editUser.id}`, {
        fullName: editUser.fullName,
        email: editUser.email,
        roleId: Number(editUser.roleId),
        active: editUser.active
      });
      setMessage({ type: 'success', text: `User ${editUser.username} updated successfully.` });
      setEditOpen(false);
      setEditUser(null);
      fetchUsers();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to update user' });
    }
  };

  const handleResetPassword = async () => {
    try {
      await api.post(`/users/${resetTargetUser.id}/reset-password`, { newPassword: newPasswordInput });
      setMessage({ type: 'success', text: `Password for ${resetTargetUser.username} reset to "${newPasswordInput}". Forced change set for next login.` });
      setResetOpen(false);
      setResetTargetUser(null);
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to reset password' });
    }
  };

  const handleToggleStatus = async () => {
    const target = statusDialog.user;
    if (!target) return;
    try {
      await api.patch(`/users/${target.id}/status`, { active: !target.active });
      setMessage({ type: 'success', text: `User ${target.username} has been ${!target.active ? 'activated' : 'deactivated'}.` });
      setStatusDialog({ open: false, user: null });
      fetchUsers();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to update user status' });
    }
  };

  const columns = [
    { header: 'ID', accessor: 'id', width: '60px' },
    { header: 'Username', accessor: 'username' },
    { header: 'Full Name', accessor: 'fullName' },
    { header: 'Email', accessor: 'email' },
    { header: 'Role', accessor: 'roleName', render: (row) => <span className="badge badge-primary">{row.roleName}</span> },
    {
      header: 'Status',
      accessor: 'active',
      render: (row) => (
        <span className={`badge badge-${row.active ? 'success' : 'danger'}`}>
          {row.active ? 'Active' : 'Inactive'}
        </span>
      )
    },
    {
      header: 'Last Login',
      accessor: 'lastLogin',
      render: (row) => row.lastLogin ? new Date(row.lastLogin).toLocaleString() : 'Never'
    },
    {
      header: 'Actions',
      render: (row) => (
        <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
          {hasPermission('USER_UPDATE') && (
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => {
                setEditUser({ ...row });
                setEditOpen(true);
              }}
            >
              Edit
            </button>
          )}

          {hasPermission('USER_PASSWORD_RESET') && (
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => {
                setResetTargetUser(row);
                setNewPasswordInput('Reset@12345');
                setResetOpen(true);
              }}
            >
              Reset Pwd
            </button>
          )}

          {hasPermission('USER_STATUS') && row.id !== 1 && (
            <button
              className={`btn btn-${row.active ? 'danger' : 'success'} btn-sm`}
              onClick={() => setStatusDialog({ open: true, user: row })}
            >
              {row.active ? 'Deactivate' : 'Activate'}
            </button>
          )}
        </div>
      )
    }
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h2>User Management</h2>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Manage user accounts, roles, access statuses, and password resets.
          </p>
        </div>

        {hasPermission('USER_CREATE') && (
          <button className="btn btn-primary" onClick={() => setCreateOpen(true)}>
            + Add New User
          </button>
        )}
      </div>

      {message && (
        <div className={`alert alert-${message.type}`} style={{ marginBottom: '1rem' }}>
          {message.text}
        </div>
      )}

      {loading ? (
        <p>Loading users...</p>
      ) : (
        <DataTable
          columns={columns}
          data={users}
          keyField="id"
          searchPlaceholder="Search by username, name, or email..."
        />
      )}

      {/* Create User Modal */}
      <Modal
        isOpen={createOpen}
        onClose={() => setCreateOpen(false)}
        title="Create New User"
        maxWidth="480px"
      >
        <form onSubmit={handleCreateSubmit}>
          <FormField
            label="Username"
            name="username"
            value={createForm.username}
            onChange={(e) => setCreateForm({ ...createForm, username: e.target.value })}
            required
          />
          <FormField
            label="Email Address"
            name="email"
            type="email"
            value={createForm.email}
            onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })}
            required
          />
          <FormField
            label="Full Name"
            name="fullName"
            value={createForm.fullName}
            onChange={(e) => setCreateForm({ ...createForm, fullName: e.target.value })}
            required
          />
          <FormField
            label="Role"
            name="roleId"
            type="select"
            value={createForm.roleId}
            onChange={(e) => setCreateForm({ ...createForm, roleId: Number(e.target.value) })}
            options={roles}
            required
          />
          <FormField
            label="Temporary Password"
            name="password"
            value={createForm.password}
            onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })}
            required
          />
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setCreateOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Create User
            </button>
          </div>
        </form>
      </Modal>

      {/* Edit User Modal */}
      <Modal
        isOpen={editOpen}
        onClose={() => {
          setEditOpen(false);
          setEditUser(null);
        }}
        title={`Edit User: ${editUser?.username}`}
        maxWidth="480px"
      >
        {editUser && (
          <form onSubmit={handleEditSubmit}>
            <FormField
              label="Full Name"
              name="fullName"
              value={editUser.fullName}
              onChange={(e) => setEditUser({ ...editUser, fullName: e.target.value })}
              required
            />
            <FormField
              label="Email Address"
              name="email"
              type="email"
              value={editUser.email}
              onChange={(e) => setEditUser({ ...editUser, email: e.target.value })}
              required
            />
            <FormField
              label="Role"
              name="roleId"
              type="select"
              value={editUser.roleId}
              onChange={(e) => setEditUser({ ...editUser, roleId: Number(e.target.value) })}
              options={roles}
              required
            />
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  setEditOpen(false);
                  setEditUser(null);
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

      {/* Reset Password Modal */}
      <Modal
        isOpen={resetOpen}
        onClose={() => setResetOpen(false)}
        title={`Reset Password: ${resetTargetUser?.username}`}
        maxWidth="440px"
      >
        <p style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>
          Assign a temporary password for this user. They will be required to change it on their next login.
        </p>
        <FormField
          label="New Temporary Password"
          name="newPasswordInput"
          value={newPasswordInput}
          onChange={(e) => setNewPasswordInput(e.target.value)}
          required
        />
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
          <button type="button" className="btn btn-secondary" onClick={() => setResetOpen(false)}>
            Cancel
          </button>
          <button type="button" className="btn btn-primary" onClick={handleResetPassword}>
            Confirm Reset
          </button>
        </div>
      </Modal>

      {/* Confirm Status Toggle */}
      <ConfirmDialog
        isOpen={statusDialog.open}
        onClose={() => setStatusDialog({ open: false, user: null })}
        onConfirm={handleToggleStatus}
        title={statusDialog.user?.active ? 'Deactivate User' : 'Activate User'}
        message={`Are you sure you want to ${statusDialog.user?.active ? 'deactivate' : 'activate'} user "${statusDialog.user?.username}"?`}
        confirmText={statusDialog.user?.active ? 'Deactivate' : 'Activate'}
        danger={statusDialog.user?.active}
      />
    </div>
  );
}
