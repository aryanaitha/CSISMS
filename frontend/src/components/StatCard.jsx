import React from 'react';

export default function StatCard({ label, value, unit, subtitle }) {
  return (
    <div className="card stat-card">
      <div className="stat-label">{label}</div>
      <div className="stat-value-row">
        <span className="stat-value">{value != null ? value : '—'}</span>
        {unit && <span className="stat-unit">{unit}</span>}
      </div>
      {subtitle && <div className="stat-subtitle">{subtitle}</div>}
    </div>
  );
}
