import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';

export default function ReportsList() {
  const { hasPermission } = useAuth();
  const [reportType, setReportType] = useState('projects');
  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);
  const [message, setMessage] = useState(null);

  const reportOptions = [
    { id: 'projects', label: 'Project Summary Report', desc: 'Overall status, progress percentage, budget, and project milestones' },
    { id: 'workforce', label: 'Workforce & Personnel Report', desc: 'Active tradesmen, contact details, status, and contractor assignments' },
    { id: 'attendance', label: 'Daily Attendance Report', desc: 'Worker attendance logs, presence statuses, and supervisory remarks' },
    { id: 'tasks', label: 'Task Execution & Progress Report', desc: 'Work breakdown structure, assigned personnel, deadlines, and priorities' },
    { id: 'inventory', label: 'Inventory & Stock Valuation Report', desc: 'Material catalog, current stock on-hand, minimum safety thresholds, and valuation' },
    { id: 'equipment', label: 'Equipment & Fleet Status Report', desc: 'Machinery operational statuses and preventative maintenance schedules' },
    { id: 'safety', label: 'Safety Violations & Incidents Report', desc: 'Consolidated safety infractions, hazard severity, and accident reports' },
    { id: 'audit', label: 'System Audit Trail Report', desc: 'Immutable compliance trail of security events, CRUD actions, and user sessions' },
  ];

  const fetchReport = async (type) => {
    try {
      setLoading(true);
      setMessage(null);
      const res = await api.get(`/reports/preview?type=${type}`);
      if (res.data) {
        setReportData(res.data);
      }
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to load report data' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReport(reportType);
  }, [reportType]);

  const handleDownloadCsv = async () => {
    setExporting(true);
    try {
      const filename = `${reportType}_report_${new Date().toISOString().split('T')[0]}.csv`;
      await api.downloadBlob(`/reports/export/csv?type=${reportType}`, filename);
      setMessage({ type: 'success', text: `Downloaded ${filename} successfully (RFC-4180 CSV)` });
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to download CSV export' });
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className="page-container">
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2>System Intelligence & Reports Engine</h2>
          <p className="text-muted">Generate real-time intelligence digests and export standards-compliant RFC-4180 CSV data</p>
        </div>
        <button
          className="btn btn-primary"
          onClick={handleDownloadCsv}
          disabled={exporting || loading || !reportData}
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
        >
          {exporting ? 'Generating CSV...' : '⬇ Export RFC-4180 CSV'}
        </button>
      </div>

      {message && (
        <div className={`alert alert-${message.type}`} style={{ marginBottom: '1rem' }}>
          {message.text}
          <button className="btn btn-link btn-sm" style={{ float: 'right' }} onClick={() => setMessage(null)}>✕</button>
        </div>
      )}

      {/* Report Selection Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: '0.75rem', marginBottom: '1.5rem' }}>
        {reportOptions.map((opt) => {
          const isSelected = reportType === opt.id;
          return (
            <div
              key={opt.id}
              onClick={() => setReportType(opt.id)}
              style={{
                padding: '0.875rem 1rem',
                border: isSelected ? '2px solid var(--primary-color)' : '1px solid var(--border-color)',
                borderRadius: '6px',
                background: isSelected ? 'rgba(37, 99, 235, 0.05)' : '#ffffff',
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              <div style={{ fontWeight: isSelected ? '600' : '500', color: isSelected ? 'var(--primary-color)' : 'inherit', marginBottom: '0.25rem' }}>
                {opt.label}
              </div>
              <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>
                {opt.desc}
              </div>
            </div>
          );
        })}
      </div>

      {/* Report Preview Container */}
      <div className="card" style={{ padding: '1rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h3 style={{ margin: 0 }}>
            {reportData ? reportData.reportTitle : 'Loading Report...'}
          </h3>
          {reportData && (
            <span className="badge badge-info">
              {reportData.rows ? reportData.rows.length : 0} Total Records
            </span>
          )}
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem' }}>
            <p className="text-muted">Compiling real-time report data...</p>
          </div>
        ) : reportData && reportData.headers && reportData.headers.length > 0 ? (
          <div style={{ overflowX: 'auto' }}>
            <table className="data-table" style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ background: '#f8fafc', borderBottom: '2px solid var(--border-color)' }}>
                  {reportData.headers.map((h, idx) => (
                    <th key={idx} style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.85rem' }}>
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {reportData.rows && reportData.rows.length > 0 ? (
                  reportData.rows.map((row, rIdx) => (
                    <tr key={rIdx} style={{ borderBottom: '1px solid var(--border-color)' }}>
                      {row.map((val, cIdx) => (
                        <td key={cIdx} style={{ padding: '0.65rem 0.75rem', fontSize: '0.85rem' }}>
                          {val}
                        </td>
                      ))}
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={reportData.headers.length} style={{ textAlign: 'center', padding: '2rem' }} className="text-muted">
                      No records found for this report criteria.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '2rem' }} className="text-muted">
            No report dataset available.
          </div>
        )}
      </div>
    </div>
  );
}
