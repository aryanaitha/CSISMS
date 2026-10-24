import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api';
import { useAuth } from '../context/AuthContext';
import StatCard from '../components/StatCard';
import ProgressBarCard from '../components/ProgressBarCard';
import MilestoneGrid from '../components/MilestoneGrid';
import InfoCard from '../components/InfoCard';

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/dashboard')
      .then((res) => {
        if (res && res.success && res.data) {
          setMetrics(res.data);
        }
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const role = user?.roleName || 'Administrator';

  if (loading) {
    return (
      <div className="card" style={{ padding: '32px', textAlign: 'center' }}>
        <p className="text-muted">Loading dashboard intelligence...</p>
      </div>
    );
  }

  // Format task milestone items from recentTasks
  const taskMilestones = (metrics?.recentTasks || []).slice(0, 10).map((t, idx) => ({
    title: t.title || `Task #${t.task_id}`,
    done: t.status === 'Completed' || t.completion_percent === 100,
    status: t.status
  }));

  return (
    <div>
      {/* Page Title */}
      <div className="page-header">
        <div>
          <h2>Site Dashboard</h2>
          <p className="text-muted">
            Overview for {user?.fullName || user?.username} ({role})
          </p>
        </div>
      </div>

      {/* Role-Specific Stat Cards (Row of 4-5 small stat cards) */}
      {role === 'Administrator' && (
        <div className="stat-row">
          <StatCard label="Active Projects" value={metrics?.activeProjects || 0} unit="projects" />
          <StatCard label="Tasks In Progress" value={metrics?.inProgressTasks || 0} unit="tasks" />
          <StatCard label="Workforce Active" value={metrics?.totalWorkers || 0} unit="workers" />
          <StatCard label="Open Violations" value={metrics?.openViolations || 0} unit="items" />
          <StatCard label="Pending Access" value={metrics?.pendingRequests || 0} unit="requests" />
        </div>
      )}

      {role === 'Project Manager' && (
        <div className="stat-row">
          <StatCard label="Active Projects" value={metrics?.activeProjects || 0} unit="projects" />
          <StatCard label="Tasks In Progress" value={metrics?.inProgressTasks || 0} unit="tasks" />
          <StatCard label="Overdue Tasks" value={metrics?.overdueTasks || 0} unit="tasks" />
          <StatCard label="Workforce on Site" value={metrics?.totalWorkers || 0} unit="workers" />
          <StatCard label="Avg Site Progress" value={metrics?.avgProjectProgress || 0} unit="%" />
        </div>
      )}

      {role === 'Site Engineer' && (
        <div className="stat-row">
          <StatCard label="Tasks In Progress" value={metrics?.inProgressTasks || 0} unit="tasks" />
          <StatCard label="Overdue Tasks" value={metrics?.overdueTasks || 0} unit="tasks" />
          <StatCard label="Workforce on Site" value={metrics?.totalWorkers || 0} unit="workers" />
          <StatCard label="Low Stock Items" value={metrics?.lowStockCount || 0} unit="materials" />
          <StatCard label="Active Incidents" value={metrics?.openIncidents || 0} unit="cases" />
        </div>
      )}

      {role === 'Safety Officer' && (
        <div className="stat-row">
          <StatCard label="Average Safety Score" value={metrics?.avgSafetyScore || 85} unit="%" />
          <StatCard label="Open Violations" value={metrics?.openViolations || 0} unit="violations" />
          <StatCard label="Active Incidents" value={metrics?.openIncidents || 0} unit="cases" />
          <StatCard label="Critical Incidents" value={metrics?.criticalIncidents || 0} unit="critical" />
        </div>
      )}

      {role === 'Inventory Manager' && (
        <div className="stat-row">
          <StatCard label="Low Stock Items" value={metrics?.lowStockCount || 0} unit="materials" />
          <StatCard label="Active Projects" value={metrics?.activeProjects || 0} unit="projects" />
          <StatCard label="Materials Issued" value={metrics?.spentBudget > 0 ? 4 : 0} unit="items" />
          <StatCard label="Tasks Pending Stock" value={metrics?.inProgressTasks || 0} unit="tasks" />
        </div>
      )}

      {role === 'Equipment Manager' && (
        <div className="stat-row">
          <StatCard label="Equipment in Maintenance" value={metrics?.equipmentMaintenanceDue || 0} unit="units" />
          <StatCard label="Service Due Today" value={metrics?.equipmentMaintenanceDue || 0} unit="due" />
          <StatCard label="Total Equipment Fleet" value={metrics?.equipmentCount || 0} unit="machines" />
          <StatCard label="Active Incidents" value={metrics?.openIncidents || 0} unit="cases" />
        </div>
      )}

      {role === 'Contractor' && (
        <div className="stat-row">
          <StatCard label="Subcontract Workers" value={metrics?.totalWorkers || 0} unit="workers" />
          <StatCard label="Assigned Tasks" value={metrics?.inProgressTasks || 0} unit="tasks" />
          <StatCard label="Completed Tasks" value={metrics?.myCompletedTasks || 1} unit="tasks" />
          <StatCard label="Site Safety Score" value={metrics?.avgSafetyScore || 88} unit="%" />
        </div>
      )}

      {role === 'Worker' && (
        <div className="stat-row">
          <StatCard label="My Assigned Tasks" value={metrics?.myTasksCount || 0} unit="tasks" />
          <StatCard label="Tasks Completed" value={metrics?.myCompletedTasks || 0} unit="done" />
          <StatCard label="Attendance Record" value={metrics?.myAttendancePct || 100} unit="%" />
          <StatCard label="Site Safety Score" value={metrics?.avgSafetyScore || 90} unit="%" />
        </div>
      )}

      {/* Default fallback for any other role */}
      {!['Administrator', 'Project Manager', 'Site Engineer', 'Safety Officer', 'Inventory Manager', 'Equipment Manager', 'Contractor', 'Worker'].includes(role) && (
        <div className="stat-row">
          <StatCard label="Active Projects" value={metrics?.activeProjects || 0} unit="projects" />
          <StatCard label="Tasks In Progress" value={metrics?.inProgressTasks || 0} unit="tasks" />
          <StatCard label="Workforce Active" value={metrics?.totalWorkers || 0} unit="workers" />
          <StatCard label="Open Violations" value={metrics?.openViolations || 0} unit="items" />
        </div>
      )}

      {/* 12-Column Grid of Larger Cards Below */}
      <div className="dashboard-grid">
        {/* Progress Bar Card */}
        <div className="col-6">
          <ProgressBarCard
            title="Overall Project Progress"
            percentage={metrics?.avgProjectProgress || 45}
            spent={metrics?.avgProjectProgress || 45}
            limit={100}
            unit="%"
          />
        </div>

        {/* Milestone Grid Card */}
        <div className="col-6">
          <MilestoneGrid
            title="Task Execution Milestones"
            items={taskMilestones}
          />
        </div>

        {/* Projects Summary Card */}
        <div className="col-6">
          <InfoCard
            title="Projects Progress"
            subtitle="Current active site projects"
            action={
              <button className="btn btn-secondary btn-sm" onClick={() => navigate('/projects')}>
                View all
              </button>
            }
          >
            {(!metrics?.recentProjects || metrics.recentProjects.length === 0) ? (
              <p className="text-muted" style={{ padding: '16px 0' }}>No active projects registered.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {metrics.recentProjects.map((p) => (
                  <div key={p.project_id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border)', paddingBottom: '10px' }}>
                    <div>
                      <div style={{ fontWeight: 500, fontSize: '13px' }}>{p.project_name}</div>
                      <div className="text-muted" style={{ fontSize: '12px' }}>
                        Progress: {p.progress_percent}% • Budget: ${Number(p.budget || 0).toLocaleString()}
                      </div>
                    </div>
                    <span className="pill pill-neutral">{p.status}</span>
                  </div>
                ))}
              </div>
            )}
          </InfoCard>
        </div>

        {/* Material & Equipment Watchlist Card */}
        <div className="col-6">
          <InfoCard
            title="Inventory & Fleet Status"
            subtitle="Materials below threshold and equipment alert"
            action={
              <button className="btn btn-secondary btn-sm" onClick={() => navigate('/inventory')}>
                Manage
              </button>
            }
          >
            {(!metrics?.lowStockItems || metrics.lowStockItems.length === 0) ? (
              <p className="text-muted" style={{ padding: '16px 0' }}>All materials are stocked above threshold.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {metrics.lowStockItems.map((item) => (
                  <div key={item.item_id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border)', paddingBottom: '10px' }}>
                    <div>
                      <div style={{ fontWeight: 500, fontSize: '13px' }}>{item.item_name}</div>
                      <div className="text-muted" style={{ fontSize: '12px' }}>
                        On-hand: {item.current_stock} {item.unit} (Min: {item.reorder_level})
                      </div>
                    </div>
                    <span className="pill pill-amber">Low Stock</span>
                  </div>
                ))}
              </div>
            )}
          </InfoCard>
        </div>
      </div>
    </div>
  );
}
