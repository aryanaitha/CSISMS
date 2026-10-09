import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';

const MAINT_STATUSES = [
  { value: 'Due', label: 'Due' },
  { value: 'In Progress', label: 'In Progress' },
  { value: 'Completed', label: 'Completed' }
];

const MAINT_TYPES = [
  { value: 'Preventative', label: 'Preventative' },
  { value: 'Corrective', label: 'Corrective' },
  { value: 'Safety Inspection', label: 'Safety Inspection' }
];

export default function MaintenanceList() {
  const { hasPermission } = useAuth();
  const [records, setRecords] = useState([]);
  const [equipmentList, setEquipmentList] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  // Schedule Modal
  const [scheduleOpen, setScheduleOpen] = useState(false);
  const [scheduleForm, setScheduleForm] = useState({
    equipmentId: '',
    maintenanceType: 'Preventative',
    status: 'Due',
    scheduledDate: new Date().toISOString().split('T')[0],
    cost: '',
    technician: '',
    notes: ''
  });

  // Edit / Complete Modal
  const [editOpen, setEditOpen] = useState(false);
  const [editRecord, setEditRecord] = useState(null);

  const fetchData = async () => {
    try {
      let query = statusFilter ? `?status=${statusFilter}` : '';
      const [mRes, eRes] = await Promise.all([
        api.get(`/maintenance${query}`),
        api.get('/equipment')
      ]);
      if (mRes.success) setRecords(mRes.data);
      if (eRes.success) {
        setEquipmentList(eRes.data);
        if (eRes.data.length > 0 && !scheduleForm.equipmentId) {
          setScheduleForm(prev => ({ ...prev, equipmentId: eRes.data[0].id }));
        }
      }
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to fetch maintenance orders' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [statusFilter]);

  const handleScheduleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/maintenance', {
        ...scheduleForm,
        equipmentId: Number(scheduleForm.equipmentId),
        cost: scheduleForm.cost ? Number(scheduleForm.cost) : 0
      });
      setMessage({ type: 'success', text: 'Maintenance service order scheduled successfully.' });
      setScheduleOpen(false);
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to schedule maintenance' });
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.put(`/maintenance/${editRecord.id}`, {
        ...editRecord,
        cost: editRecord.cost ? Number(editRecord.cost) : 0
      });
      setMessage({ type: 'success', text: `Work order #${editRecord.id} updated.` });
      setEditOpen(false);
      setEditRecord(null);
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to update maintenance' });
    }
  };

  const columns = [
    { header: 'Order #', accessor: 'id', width: '60px' },
    { header: 'Equipment', render: (row) => <strong>{row.equipmentName} ({row.equipmentCode})</strong> },
    { header: 'Service Type', accessor: 'maintenanceType' },
    {
      header: 'Status',
      accessor: 'status',
      render: (row) => (
        <span className={`badge badge-${row.status === 'Completed' ? 'success' : row.status === 'In Progress' ? 'primary' : 'warning'}`}>
          {row.status}
        </span>
      )
    },
    { header: 'Scheduled Date', accessor: 'scheduledDate' },
    { header: 'Completed Date', accessor: 'completedDate', render: (row) => row.completedDate || '—' },
    { header: 'Technician', accessor: 'technician', render: (row) => row.technician || '—' },
    { header: 'Cost', accessor: 'cost', render: (row) => row.cost ? `$${Number(row.cost).toFixed(2)}` : '$0.00' },
    {
      header: 'Actions',
      render: (row) => hasPermission('MAINTENANCE_UPDATE') && (
        <button
          className="btn btn-secondary btn-sm"
          onClick={() => {
            setEditRecord({ ...row });
            setEditOpen(true);
          }}
        >
          Update Order
        </button>
      )
    }
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2>Equipment Maintenance</h2>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Preventative servicing, emergency repairs, equipment work orders and cost accounting.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <select
            className="form-input"
            style={{ width: 'auto' }}
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">All Statuses</option>
            {MAINT_STATUSES.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
          </select>

          {hasPermission('MAINTENANCE_SCHEDULE') && (
            <button className="btn btn-primary" onClick={() => setScheduleOpen(true)}>
              + Schedule Service
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
        <p>Loading maintenance records...</p>
      ) : (
        <DataTable
          columns={columns}
          data={records}
          keyField="id"
          searchPlaceholder="Search maintenance orders by equipment, technician..."
        />
      )}

      {/* Schedule Modal */}
      <Modal
        isOpen={scheduleOpen}
        onClose={() => setScheduleOpen(false)}
        title="Schedule Maintenance Service"
        maxWidth="500px"
      >
        <form onSubmit={handleScheduleSubmit}>
          <FormField
            label="Equipment Machinery"
            name="equipmentId"
            type="select"
            value={scheduleForm.equipmentId}
            onChange={(e) => setScheduleForm({ ...scheduleForm, equipmentId: e.target.value })}
            options={equipmentList.map(e => ({ value: e.id, label: `${e.name} (${e.equipmentCode})` }))}
            required
          />

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <FormField
              label="Service Type"
              name="maintenanceType"
              type="select"
              value={scheduleForm.maintenanceType}
              onChange={(e) => setScheduleForm({ ...scheduleForm, maintenanceType: e.target.value })}
              options={MAINT_TYPES}
            />
            <FormField
              label="Initial Status"
              name="status"
              type="select"
              value={scheduleForm.status}
              onChange={(e) => setScheduleForm({ ...scheduleForm, status: e.target.value })}
              options={MAINT_STATUSES}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <FormField
              label="Scheduled Date"
              name="scheduledDate"
              type="date"
              value={scheduleForm.scheduledDate}
              onChange={(e) => setScheduleForm({ ...scheduleForm, scheduledDate: e.target.value })}
              required
            />
            <FormField
              label="Assigned Technician"
              name="technician"
              value={scheduleForm.technician}
              onChange={(e) => setScheduleForm({ ...scheduleForm, technician: e.target.value })}
              placeholder="e.g. John Miller"
            />
          </div>

          <FormField
            label="Estimated Cost ($)"
            name="cost"
            type="number"
            value={scheduleForm.cost}
            onChange={(e) => setScheduleForm({ ...scheduleForm, cost: e.target.value })}
            placeholder="e.g. 350.00"
          />

          <FormField
            label="Service Instructions / Notes"
            name="notes"
            type="textarea"
            value={scheduleForm.notes}
            onChange={(e) => setScheduleForm({ ...scheduleForm, notes: e.target.value })}
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setScheduleOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Schedule Work Order
            </button>
          </div>
        </form>
      </Modal>

      {/* Edit / Complete Modal */}
      <Modal
        isOpen={editOpen}
        onClose={() => {
          setEditOpen(false);
          setEditRecord(null);
        }}
        title={`Update Service Order #${editRecord?.id}`}
        maxWidth="500px"
      >
        {editRecord && (
          <form onSubmit={handleEditSubmit}>
            <div style={{ background: 'var(--color-table-zebra)', padding: '0.75rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.85rem' }}>
              <div>Equipment: <strong>{editRecord.equipmentName} ({editRecord.equipmentCode})</strong></div>
              <div>Type: {editRecord.maintenanceType}</div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <FormField
                label="Status"
                name="status"
                type="select"
                value={editRecord.status}
                onChange={(e) => {
                  const s = e.target.value;
                  const compDate = s === 'Completed' && !editRecord.completedDate ? new Date().toISOString().split('T')[0] : editRecord.completedDate;
                  setEditRecord({ ...editRecord, status: s, completedDate: compDate });
                }}
                options={MAINT_STATUSES}
              />
              <FormField
                label="Completed Date"
                name="completedDate"
                type="date"
                value={editRecord.completedDate || ''}
                onChange={(e) => setEditRecord({ ...editRecord, completedDate: e.target.value })}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <FormField
                label="Technician"
                name="technician"
                value={editRecord.technician || ''}
                onChange={(e) => setEditRecord({ ...editRecord, technician: e.target.value })}
              />
              <FormField
                label="Final Cost ($)"
                name="cost"
                type="number"
                value={editRecord.cost || ''}
                onChange={(e) => setEditRecord({ ...editRecord, cost: e.target.value })}
              />
            </div>

            <FormField
              label="Technician Notes / Findings"
              name="notes"
              type="textarea"
              value={editRecord.notes || ''}
              onChange={(e) => setEditRecord({ ...editRecord, notes: e.target.value })}
            />

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  setEditOpen(false);
                  setEditRecord(null);
                }}
              >
                Cancel
              </button>
              <button type="submit" className="btn btn-primary">
                Save Changes
              </button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
}
