import React from 'react';

export default function InfoCard({ title, subtitle, action, children }) {
  return (
    <div className="card info-card">
      {(title || action) && (
        <div className="card-header-clean">
          <div>
            {title && <div className="card-title">{title}</div>}
            {subtitle && <div className="card-subtitle">{subtitle}</div>}
          </div>
          {action && <div className="card-action">{action}</div>}
        </div>
      )}
      <div className="card-body-clean">{children}</div>
    </div>
  );
}
