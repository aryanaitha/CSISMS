import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';

const ATT_STATUSES = [
  { value: 'Present', label: 'Present' },
  { value: 'Absent', label: 'Absent' },
  { value: 'Half Day', label: 'Half Day' },
  { value: 'Leave', label: 'On Leave' }
];

export default function AttendanceSheet() {
  const { hasPermission } = useAuth();
  const [attendance, setAttendance] = useState([]);
  const [projects, setProjects] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  // Record Attendance Modal
  const [recordOpen, setRecordOpen] = useState(false);
  const [recordForm, setRecordForm] = useState({
    workerId: '',
    projectId: '',
    attendanceDate: new Date().toISOString().split('T')[0],
    status: 'Present',
    remarks: ''
  });

  // Correct Attendance Modal
  const [correctOpen, setCorrectOpen] = useState(false);
  const [correctTarget, setCorrectTarget] = useState(null);
  const [correctForm, setCorrectForm] = useState({ status: 'Present', remarks: '' });

  const fetchDropdowns = async () => {
    try {
      const [pRes, wRes] = await Promise.all([
        api.get('/projects'),
        api.get('/workforce/workers?status=ACTIVE')
      ]);
      if (pRes.success) {
        setProjects(pRes.data);
        if (pRes.data.length > 0 && !recordForm.projectId) {
          setRecordForm(prev => ({ ...prev, projectId: pRes.data[0].id }));
        }
      }
      if (wRes.success) {
        setWorkers(wRes.data);
        if (wRes.data.length > 0 && !recordForm.workerId) {
          setRecordForm(prev => ({ ...prev, workerId: wRes.data[0].id }));
        }
      }
    } catch (e) {
      console.error(e);
    }
  };

  const fetchAttendance = async () => {
    try {
      let query = `?date=${selectedDate}&`;
      if (selectedProjectId) query += `projectId=${selectedProjectId}&`;
      const res = await api.get(`/attendance${query}`);
      if (res.success) setAttendance(res.data);
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to fetch attendance' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDropdowns();
  }, []);

  useEffect(() => {
    fetchAttendance();
  }, [selectedDate, selectedProjectId]);

  const handleRecordSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/attendance', {
        ...recordForm,
        workerId: Number(recordForm.workerId),
        projectId: Number(recordForm.projectId)
      });
      setMessage({ type: 'success', text: 'Attendance logged successfully.' });
      setRecordOpen(false);
      fetchAttendance();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to record attendance' });
    }
  };

  const handleCorrectSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.patch(`/attendance/${correctTarget.id}/correct`, {
        status: correctForm.status,
        remarks: correctForm.remarks
      });
      setMessage({ type: 'success', text: `Attendance corrected for ${correctTarget.workerName}.` });
      setCorrectOpen(false);
      setCorrectTarget(null);
      fetchAttendance();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to correct attendance' });
    }
  };

  const columns = [
    { header: 'ID', accessor: 'id', width: '50px' },
    { header: 'Worker Name', accessor: 'workerName', render: (row) => <strong>{row.workerName}</strong> },
    { header: 'Project', accessor: 'projectName' },
    { header: 'Date', accessor: 'attendanceDate' },
    {
      header: 'Status',
      accessor: 'status',
      render: (row) => (
        <span className={`badge badge-${row.status === 'Present' ? 'success' : row.status === 'Absent' ? 'danger' : 'warning'}`}>
          {row.status}
        </span>
      )
    },
    { header: 'Remarks', accessor: 'remarks', render: (row) => row.remarks || '—' },
    { header: 'Logged By', accessor: 'recorderName', render: (row) => row.recorderName || 'System' },
    {
      header: 'Actions',
      render: (row) => hasPermission('ATTENDANCE_CORRECT') && (
        <button
          className="btn btn-secondary btn-sm"
          onClick={() => {
            setCorrectTarget(row);
            setCorrectForm({ status: row.status, remarks: '' });
            setCorrectOpen(true);
          }}
        >
          Correct
        </button>
      )
    }
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2>Workforce Attendance</h2>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Daily attendance check-ins, site presence records and audited corrections.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <input
            type="date"
            className="form-input"
            style={{ width: 'auto' }}
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
          />

          <select
            className="form-input"
            style={{ width: 'auto' }}
            value={selectedProjectId}
            onChange={(e) => setSelectedProjectId(e.target.value)}
          >
            <option value="">All Projects</option>
            {projects.map(p => <option key={p.id} value={p.id}>{p.projectName}</option>)}
          </select>

          {hasPermission('ATTENDANCE_RECORD') && (
            <button className="btn btn-primary" onClick={() => setRecordOpen(true)}>
              + Mark Attendance
            </button>
          )}
        </div>
      </div>

      {message && (
        <div className={`alert alert-${message.type}`} style={{ marginBottom: '1rem' }}>
          {message.text}
        </div>
      )}

      {loading ? (
        <p>Loading attendance sheet...</p>
      ) : (
        <DataTable
          columns={columns}
          data={attendance}
          keyField="id"
          searchPlaceholder="Search by worker name, project..."
        />
      )}

      {/* Record Attendance Modal */}
      <Modal
        isOpen={recordOpen}
        onClose={() => setRecordOpen(false)}
        title="Mark Daily Attendance"
        maxWidth="460px"
      >
        <form onSubmit={handleRecordSubmit}>
          <FormField
            label="Worker"
            name="workerId"
            type="select"
            value={recordForm.workerId}
            onChange={(e) => setRecordForm({ ...recordForm, workerId: e.target.value })}
            options={workers.map(w => ({ value: w.id, label: `${w.fullName} (${w.trade})` }))}
            required
          />
          <FormField
            label="Project Site"
            name="projectId"
            type="select"
            value={recordForm.projectId}
            onChange={(e) => setRecordForm({ ...recordForm, projectId: e.target.value })}
            options={projects.map(p => ({ value: p.id, label: p.projectName }))}
            required
          />
          <FormField
            label="Date"
            name="attendanceDate"
            type="date"
            value={recordForm.attendanceDate}
            onChange={(e) => setRecordForm({ ...recordForm, attendanceDate: e.target.value })}
            required
          />
          <FormField
            label="Status"
            name="status"
            type="select"
            value={recordForm.status}
            onChange={(e) => setRecordForm({ ...recordForm, status: e.target.value })}
            options={ATT_STATUSES}
            required
          />
          <FormField
            label="Remarks"
            name="remarks"
            value={recordForm.remarks}
            onChange={(e) => setRecordForm({ ...recordForm, remarks: e.target.value })}
            placeholder="Optional check-in notes..."
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setRecordOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Submit Record
            </button>
          </div>
        </form>
      </Modal>

      {/* Correct Attendance Modal */}
      <Modal
        isOpen={correctOpen}
        onClose={() => setCorrectOpen(false)}
        title={`Correct Attendance: ${correctTarget?.workerName}`}
        maxWidth="440px"
      >
        <form onSubmit={handleCorrectSubmit}>
          <p style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>
            Corrections are permanently audited with your username and timestamp.
          </p>

          <FormField
            label="New Status"
            name="status"
            type="select"
            value={correctForm.status}
            onChange={(e) => setCorrectForm({ ...correctForm, status: e.target.value })}
            options={ATT_STATUSES}
            required
          />
          <FormField
            label="Correction Justification / Remarks"
            name="remarks"
            type="textarea"
            value={correctForm.remarks}
            onChange={(e) => setCorrectForm({ ...correctForm, remarks: e.target.value })}
            required
            placeholder="Reason for changing status (required for audit trail)..."
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setCorrectOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Confirm Correction
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
