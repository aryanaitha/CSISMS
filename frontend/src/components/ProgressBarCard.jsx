import React from 'react';

export default function ProgressBarCard({ title, spent, limit, percentage, unit = '' }) {
  const totalSegments = 20;
  let calcPct = 0;
  if (percentage != null) {
    calcPct = Math.min(100, Math.max(0, Number(percentage)));
  } else if (limit != null && limit > 0) {
    calcPct = Math.min(100, Math.max(0, ((Number(spent || 0) / Number(limit)) * 100)));
  }
  const filledCount = Math.round((calcPct / 100) * totalSegments);

  return (
    <div className="card progress-card">
      <div className="card-header-clean">
        <div className="card-title">{title}</div>
        <div className="progress-percent-badge">{Math.round(calcPct)}%</div>
      </div>
      <div className="segment-bar">
        {Array.from({ length: totalSegments }).map((_, idx) => (
          <div
            key={idx}
            className={`segment ${idx < filledCount ? 'segment-filled' : 'segment-empty'}`}
          />
        ))}
      </div>
      {(spent != null || limit != null) && (
        <div className="progress-footer">
          <span>{spent != null ? `${spent}${unit ? ' ' + unit : ''}` : ''}</span>
          <span className="text-muted">Target: {limit != null ? `${limit}${unit ? ' ' + unit : ''}` : '100%'}</span>
        </div>
      )}
    </div>
  );
}
