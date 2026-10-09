import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';

export default function RolePermissions() {
  const { hasPermission } = useAuth();
  const [roles, setRoles] = useState([]);
  const [selectedRole, setSelectedRole] = useState(null);
  const [allPermissions, setAllPermissions] = useState([]);
  const [assignedPermIds, setAssignedPermIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);

  const fetchData = async () => {
    try {
      const [rRes, pRes] = await Promise.all([
        api.get('/roles'),
        api.get('/roles/permissions')
      ]);
      if (rRes.success) {
        setRoles(rRes.data);
        if (rRes.data.length > 0) {
          setSelectedRole(rRes.data[0]);
        }
      }
      if (pRes.success) {
        setAllPermissions(pRes.data);
      }
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to load roles and permissions' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Fetch assigned permissions whenever selectedRole changes
  useEffect(() => {
    if (!selectedRole) return;
    api.get(`/roles/${selectedRole.id}/permissions`)
      .then((res) => {
        if (res.success) {
          const ids = new Set(res.data.map(p => p.id));
          setAssignedPermIds(ids);
        }
      })
      .catch(console.error);
  }, [selectedRole]);

  const handleTogglePerm = (permId) => {
    if (selectedRole?.id === 1) return; // Admin is fixed
    const next = new Set(assignedPermIds);
    if (next.has(permId)) {
      next.delete(permId);
    } else {
      next.add(permId);
    }
    setAssignedPermIds(next);
  };

  const handleSave = async () => {
    if (!selectedRole || selectedRole.id === 1) return;
    setSaving(true);
    setMessage(null);
    try {
      await api.put(`/roles/${selectedRole.id}/permissions`, {
        permissionIds: Array.from(assignedPermIds)
      });
      setMessage({ type: 'success', text: `Permissions updated for ${selectedRole.name}.` });
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to save role permissions' });
    } finally {
      setSaving(false);
    }
  };

  // Group permissions by module
  const groupedPermissions = allPermissions.reduce((acc, p) => {
    const mod = p.module || 'GENERAL';
    if (!acc[mod]) acc[mod] = [];
    acc[mod].push(p);
    return acc;
  }, {});

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2>Roles & Permissions Matrix</h2>
        <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
          Configure granular permission assignments for each system role.
        </p>
      </div>

      {message && (
        <div className={`alert alert-${message.type}`} style={{ marginBottom: '1rem' }}>
          {message.text}
        </div>
      )}

      {loading ? (
        <p>Loading role permissions...</p>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '260px 1fr', gap: '1.5rem' }}>
          {/* Roles Selector */}
          <div className="card" style={{ padding: '1rem' }}>
            <h4 style={{ marginBottom: '1rem' }}>Roles</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {roles.map((r) => (
                <button
                  key={r.id}
                  className={`btn ${selectedRole?.id === r.id ? 'btn-primary' : 'btn-secondary'}`}
                  style={{ justifyContent: 'flex-start', textAlign: 'left' }}
                  onClick={() => {
                    setSelectedRole(r);
                    setMessage(null);
                  }}
                >
                  {r.name}
                </button>
              ))}
            </div>
          </div>

          {/* Permissions Matrix */}
          <div className="card" style={{ padding: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', borderBottom: '1px solid var(--color-border)', paddingBottom: '0.75rem' }}>
              <div>
                <h3 style={{ margin: 0 }}>{selectedRole?.name}</h3>
                <p style={{ margin: '0.25rem 0 0', color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>
                  {selectedRole?.description}
                </p>
              </div>

              {hasPermission('ROLE_UPDATE') && selectedRole?.id !== 1 && (
                <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
                  {saving ? 'Saving...' : 'Save Permissions'}
                </button>
              )}
            </div>

            {selectedRole?.id === 1 && (
              <div className="alert alert-info" style={{ marginBottom: '1rem' }}>
                The Administrator role automatically retains all permissions and cannot be modified.
              </div>
            )}

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              {Object.entries(groupedPermissions).map(([module, perms]) => (
                <div key={module} style={{ border: '1px solid var(--color-border)', borderRadius: '4px', padding: '1rem' }}>
                  <h4 style={{ margin: '0 0 0.75rem', color: 'var(--color-text-secondary)', textTransform: 'uppercase', fontSize: '0.8rem', letterSpacing: '0.05em' }}>
                    Module: {module}
                  </h4>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '0.75rem' }}>
                    {perms.map((p) => {
                      const isChecked = selectedRole?.id === 1 || assignedPermIds.has(p.id);
                      return (
                        <label
                          key={p.id}
                          style={{
                            display: 'flex',
                            alignItems: 'flex-start',
                            gap: '0.5rem',
                            fontSize: '0.85rem',
                            cursor: selectedRole?.id === 1 ? 'default' : 'pointer'
                          }}
                        >
                          <input
                            type="checkbox"
                            checked={isChecked}
                            disabled={selectedRole?.id === 1 || !hasPermission('ROLE_UPDATE')}
                            onChange={() => handleTogglePerm(p.id)}
                            style={{ marginTop: '0.2rem' }}
                          />
                          <div>
                            <strong>{p.name}</strong>
                            <div style={{ color: 'var(--color-text-muted)', fontSize: '0.75rem' }}>
                              {p.description}
                            </div>
                          </div>
                        </label>
                      );
                    })}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
