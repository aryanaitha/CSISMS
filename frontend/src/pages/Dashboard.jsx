import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';
import { useAuth } from '../context/AuthContext';

export default function Dashboard() {
  const { user, hasPermission } = useAuth();
  const navigate = useNavigate();
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/dashboard/metrics')
      .then((res) => {
        if (res.success) {
          setMetrics(res.data);
        }
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2>Dashboard</h2>
        <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
          Welcome back, <strong>{user?.fullName || user?.username}</strong> ({user?.roleName}). Here is the current site intelligence summary.
        </p>
      </div>

      {loading ? (
        <p>Loading site intelligence metrics...</p>
      ) : (
        <>
          {/* KPI Stat Cards Grid */}
          <div className="stats-grid">
            <div className="stat-card">
              <span className="stat-label">Active Projects</span>
              <span className="stat-value">{metrics?.activeProjects || 0}</span>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                of {metrics?.totalProjects || 0} total projects
              </span>
            </div>

            <div className="stat-card">
              <span className="stat-label">Workforce Active</span>
              <span className="stat-value">{metrics?.totalWorkers || 0}</span>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                registered site personnel
              </span>
            </div>

            <div className="stat-card">
              <span className="stat-label">Tasks In Progress</span>
              <span className="stat-value">{metrics?.inProgressTasks || 0}</span>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                of {metrics?.totalTasks || 0} total tasks
              </span>
            </div>

            <div className="stat-card" style={{ borderColor: metrics?.lowStockCount > 0 ? 'var(--color-warning)' : 'var(--color-border)' }}>
              <span className="stat-label">Low Stock Alerts</span>
              <span className="stat-value" style={{ color: metrics?.lowStockCount > 0 ? '#b45309' : 'inherit' }}>
                {metrics?.lowStockCount || 0}
              </span>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                materials below reorder level
              </span>
            </div>

            <div className="stat-card" style={{ borderColor: metrics?.openViolations > 0 ? 'var(--color-danger)' : 'var(--color-border)' }}>
              <span className="stat-label">Open Safety Violations</span>
              <span className="stat-value" style={{ color: metrics?.openViolations > 0 ? 'var(--color-danger)' : 'inherit' }}>
                {metrics?.openViolations || 0}
              </span>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                action required
              </span>
            </div>

            <div className="stat-card" style={{ borderColor: metrics?.openIncidents > 0 ? 'var(--color-danger)' : 'var(--color-border)' }}>
              <span className="stat-label">Active Incidents</span>
              <span className="stat-value" style={{ color: metrics?.openIncidents > 0 ? 'var(--color-danger)' : 'inherit' }}>
                {metrics?.openIncidents || 0}
              </span>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                investigation ongoing
              </span>
            </div>
          </div>

          {/* Quick Action Panels */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem', marginTop: '1.5rem' }}>
            {/* Recent Projects */}
            <div className="card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h3 style={{ margin: 0, fontSize: '1.1rem' }}>Recent Projects</h3>
                {hasPermission('PROJECT_VIEW') && (
                  <button className="btn btn-secondary btn-sm" onClick={() => navigate('/projects')}>
                    View All
                  </button>
                )}
              </div>
              {metrics?.recentProjects?.length === 0 ? (
                <p style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>No projects registered yet.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {metrics?.recentProjects?.map((p) => (
                    <div key={p.project_id} style={{ borderBottom: '1px solid var(--color-border)', paddingBottom: '0.5rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <strong>{p.project_name}</strong>
                        <span className="badge badge-primary">{p.status}</span>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: 'var(--color-text-muted)', marginTop: '0.2rem' }}>
                        <span>Progress: {p.progress_percent}%</span>
                        <span>Budget: ${Number(p.budget || 0).toLocaleString()}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Low Stock Warnings */}
            <div className="card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h3 style={{ margin: 0, fontSize: '1.1rem' }}>Inventory Status</h3>
                {hasPermission('INVENTORY_VIEW') && (
                  <button className="btn btn-secondary btn-sm" onClick={() => navigate('/inventory')}>
                    Manage Stock
                  </button>
                )}
              </div>
              {metrics?.lowStockItems?.length === 0 ? (
                <p style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>All materials are above reorder thresholds.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {metrics?.lowStockItems?.map((item) => (
                    <div key={item.item_id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--color-border)', paddingBottom: '0.5rem' }}>
                      <div>
                        <strong>{item.item_name}</strong>
                        <div style={{ fontSize: '0.8rem', color: 'var(--color-danger)' }}>
                          Stock: {item.current_stock} {item.unit} (Reorder at {item.reorder_level})
                        </div>
                      </div>
                      <span className="badge badge-warning">Low Stock</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
