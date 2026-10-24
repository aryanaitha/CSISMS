import React from 'react';

export default function MilestoneGrid({ title = 'Task Milestones', items = [] }) {
  const displayItems = items.length > 0 ? items : [
    { title: 'Project Charter', done: true },
    { title: 'Excavation Permit', done: true },
    { title: 'Foundation Pours', done: true },
    { title: 'Structural Columns', done: true },
    { title: 'Electrical Rough-in', done: false },
    { title: 'Plumbing Mains', done: false },
    { title: 'HVAC Ducting', done: false },
    { title: 'Drywall & Enclosure', done: false },
    { title: 'Fire Safety Sign-off', done: false },
    { title: 'Final Inspection', done: false }
  ];

  const doneCount = displayItems.filter(i => i.done || i.status === 'Completed').length;

  return (
    <div className="card milestone-card">
      <div className="card-header-clean">
        <div className="card-title">{title}</div>
        <div className="milestone-status-text">
          {doneCount} of {displayItems.length} completed
        </div>
      </div>
      <div className="milestone-grid">
        {displayItems.map((item, idx) => {
          const isDone = item.done || item.status === 'Completed';
          return (
            <div
              key={idx}
              className={`milestone-box ${isDone ? 'milestone-done' : 'milestone-pending'}`}
              title={`${item.title || 'Milestone ' + (idx + 1)}: ${isDone ? 'Completed' : 'Pending'}`}
            >
              <span className="milestone-box-num">{idx + 1}</span>
            </div>
          );
        })}
      </div>
      <div className="milestone-legend">
        <span className="legend-item"><span className="legend-dot done"></span> Done</span>
        <span className="legend-item"><span className="legend-dot pending"></span> Pending</span>
      </div>
    </div>
  );
}
