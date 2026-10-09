import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';

export default function AuditLogs() {
  const [logs, setLogs] = useState([]);
  const [actionFilter, setActionFilter] = useState('');
  const [moduleFilter, setModuleFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [selectedLog, setSelectedLog] = useState(null);

  const fetchLogs = async () => {
    try {
      let query = '?size=100';
      if (actionFilter) query += `&action=${actionFilter}`;
      if (moduleFilter) query += `&module=${moduleFilter}`;
      const res = await api.get(`/audit-logs${query}`);
      if (res.success && res.data) {
        setLogs(res.data.items || []);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, [actionFilter, moduleFilter]);

  const columns = [
    { header: 'ID', accessor: 'id', width: '50px' },
    {
      header: 'Timestamp',
      accessor: 'timestamp',
      render: (row) => row.timestamp ? new Date(row.timestamp).toLocaleString() : ''
    },
    { header: 'User', accessor: 'username' },
    { header: 'Action', accessor: 'action', render: (row) => <strong>{row.action}</strong> },
    { header: 'Module', accessor: 'module', render: (row) => <span className="badge badge-secondary">{row.module}</span> },
    { header: 'Entity', render: (row) => row.entityType ? `${row.entityType} #${row.entityId || ''}` : '—' },
    {
      header: 'Status',
      accessor: 'status',
      render: (row) => (
        <span className={`badge badge-${row.status === 'SUCCESS' ? 'success' : 'danger'}`}>
          {row.status}
        </span>
      )
    },
    { header: 'Description', accessor: 'description' },
    {
      header: 'Details',
      render: (row) => (
        <button className="btn btn-secondary btn-sm" onClick={() => setSelectedLog(row)}>
          View
        </button>
      )
    }
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2>System Audit Log</h2>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Append-only, immutable activity log tracking logins, updates, workflow transitions, and security actions.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <select
            className="form-input"
            style={{ width: 'auto' }}
            value={moduleFilter}
            onChange={(e) => setModuleFilter(e.target.value)}
          >
            <option value="">All Modules</option>
            <option value="AUTH">AUTH</option>
            <option value="USER">USER</option>
            <option value="ROLE">ROLE</option>
            <option value="ACCOUNT_REQUEST">ACCOUNT_REQUEST</option>
            <option value="PROJECT">PROJECT</option>
            <option value="WORKFORCE">WORKFORCE</option>
            <option value="TASK">TASK</option>
            <option value="INVENTORY">INVENTORY</option>
            <option value="EQUIPMENT">EQUIPMENT</option>
            <option value="SAFETY">SAFETY</option>
          </select>
        </div>
      </div>

      {loading ? (
        <p>Loading audit records...</p>
      ) : (
        <DataTable
          columns={columns}
          data={logs}
          keyField="id"
          searchPlaceholder="Search audit trail..."
        />
      )}

      {/* Details Modal */}
      <Modal
        isOpen={Boolean(selectedLog)}
        onClose={() => setSelectedLog(null)}
        title={`Audit Event #${selectedLog?.id}`}
        maxWidth="500px"
      >
        {selectedLog && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.9rem' }}>
            <div><strong>Timestamp:</strong> {new Date(selectedLog.timestamp).toLocaleString()}</div>
            <div><strong>User:</strong> {selectedLog.username} (ID: {selectedLog.userId || 'N/A'})</div>
            <div><strong>Action:</strong> {selectedLog.action}</div>
            <div><strong>Module:</strong> {selectedLog.module}</div>
            <div><strong>Target Entity:</strong> {selectedLog.entityType} ({selectedLog.entityId || 'N/A'})</div>
            <div><strong>Status:</strong> <span className={`badge badge-${selectedLog.status === 'SUCCESS' ? 'success' : 'danger'}`}>{selectedLog.status}</span></div>
            <div><strong>Description:</strong> {selectedLog.description}</div>
            {selectedLog.oldValue && (
              <div><strong>Old Value:</strong> <pre style={{ background: 'var(--color-table-zebra)', padding: '0.5rem', borderRadius: '4px' }}>{selectedLog.oldValue}</pre></div>
            )}
            {selectedLog.newValue && (
              <div><strong>New Value:</strong> <pre style={{ background: 'var(--color-table-zebra)', padding: '0.5rem', borderRadius: '4px' }}>{selectedLog.newValue}</pre></div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}
