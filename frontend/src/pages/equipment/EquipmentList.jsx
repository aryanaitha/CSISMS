import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';

const EQUIP_STATUSES = [
  { value: 'Available', label: 'Available' },
  { value: 'In Use', label: 'In Use' },
  { value: 'Maintenance', label: 'Maintenance' },
  { value: 'Retired', label: 'Retired' }
];

export default function EquipmentList() {
  const { hasPermission } = useAuth();
  const [equipment, setEquipment] = useState([]);
  const [projects, setProjects] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  // Add Equipment Modal
  const [addOpen, setAddOpen] = useState(false);
  const [equipForm, setEquipForm] = useState({
    equipmentCode: '',
    name: '',
    category: 'Heavy Machinery',
    status: 'Available',
    projectId: '',
    purchaseDate: '',
    lastServiceDate: '',
    nextServiceDue: ''
  });

  // Edit Equipment Modal
  const [editOpen, setEditOpen] = useState(false);
  const [editEquip, setEditEquip] = useState(null);

  const fetchData = async () => {
    try {
      let query = statusFilter ? `?status=${statusFilter}` : '';
      const [eRes, pRes] = await Promise.all([
        api.get(`/equipment${query}`),
        api.get('/projects')
      ]);
      if (eRes.success) setEquipment(eRes.data);
      if (pRes.success) setProjects(pRes.data);
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to fetch equipment' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [statusFilter]);

  const handleAddSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/equipment', {
        ...equipForm,
        projectId: equipForm.projectId ? Number(equipForm.projectId) : null
      });
      setMessage({ type: 'success', text: `Equipment "${equipForm.name}" registered.` });
      setAddOpen(false);
      setEquipForm({
        equipmentCode: '',
        name: '',
        category: 'Heavy Machinery',
        status: 'Available',
        projectId: '',
        purchaseDate: '',
        lastServiceDate: '',
        nextServiceDue: ''
      });
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to register equipment' });
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.put(`/equipment/${editEquip.id}`, {
        ...editEquip,
        projectId: editEquip.projectId ? Number(editEquip.projectId) : null
      });
      setMessage({ type: 'success', text: `Equipment "${editEquip.name}" updated.` });
      setEditOpen(false);
      setEditEquip(null);
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to update equipment' });
    }
  };

  const columns = [
    { header: 'ID', accessor: 'id', width: '50px' },
    { header: 'Code', accessor: 'equipmentCode', render: (row) => <code>{row.equipmentCode}</code> },
    { header: 'Name', accessor: 'name', render: (row) => <strong>{row.name}</strong> },
    { header: 'Category', accessor: 'category', render: (row) => <span className="badge badge-secondary">{row.category}</span> },
    {
      header: 'Status',
      accessor: 'status',
      render: (row) => (
        <span className={`badge badge-${row.status === 'Available' ? 'success' : row.status === 'In Use' ? 'primary' : row.status === 'Maintenance' ? 'danger' : 'secondary'}`}>
          {row.status}
        </span>
      )
    },
    { header: 'Current Project', accessor: 'projectName', render: (row) => row.projectName || 'Warehouse / Depo' },
    { header: 'Last Service', accessor: 'lastServiceDate' },
    {
      header: 'Next Due',
      accessor: 'nextServiceDue',
      render: (row) => {
        if (!row.nextServiceDue) return '—';
        const isPast = new Date(row.nextServiceDue) < new Date();
        return (
          <span style={{ color: isPast ? 'var(--color-danger)' : 'inherit', fontWeight: isPast ? 'bold' : 'normal' }}>
            {row.nextServiceDue} {isPast && '(Overdue)'}
          </span>
        );
      }
    },
    {
      header: 'Actions',
      render: (row) => hasPermission('EQUIPMENT_UPDATE') && (
        <button
          className="btn btn-secondary btn-sm"
          onClick={() => {
            setEditEquip({ ...row });
            setEditOpen(true);
          }}
        >
          Edit
        </button>
      )
    }
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2>Equipment & Machinery</h2>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Heavy machinery, operational availability, on-site deployment and maintenance schedules.
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
            {EQUIP_STATUSES.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
          </select>

          {hasPermission('EQUIPMENT_CREATE') && (
            <button className="btn btn-primary" onClick={() => setAddOpen(true)}>
              + Register Equipment
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
        <p>Loading equipment catalog...</p>
      ) : (
        <DataTable
          columns={columns}
          data={equipment}
          keyField="id"
          searchPlaceholder="Search equipment by code, name, category..."
        />
      )}

      {/* Add Equipment Modal */}
      <Modal
        isOpen={addOpen}
        onClose={() => setAddOpen(false)}
        title="Register New Machinery / Equipment"
        maxWidth="500px"
      >
        <form onSubmit={handleAddSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1rem' }}>
            <FormField
              label="Equipment Code"
              name="equipmentCode"
              value={equipForm.equipmentCode}
              onChange={(e) => setEquipForm({ ...equipForm, equipmentCode: e.target.value })}
              required
              placeholder="e.g. CRN-001"
            />
            <FormField
              label="Equipment Name"
              name="name"
              value={equipForm.name}
              onChange={(e) => setEquipForm({ ...equipForm, name: e.target.value })}
              required
              placeholder="e.g. Tower Crane 50T"
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <FormField
              label="Category"
              name="category"
              value={equipForm.category}
              onChange={(e) => setEquipForm({ ...equipForm, category: e.target.value })}
              required
            />
            <FormField
              label="Status"
              name="status"
              type="select"
              value={equipForm.status}
              onChange={(e) => setEquipForm({ ...equipForm, status: e.target.value })}
              options={EQUIP_STATUSES}
            />
          </div>

          <FormField
            label="Assigned Project Site"
            name="projectId"
            type="select"
            value={equipForm.projectId}
            onChange={(e) => setEquipForm({ ...equipForm, projectId: e.target.value })}
            options={[
              { value: '', label: 'None (In Warehouse Depot)' },
              ...projects.map(p => ({ value: p.id, label: p.projectName }))
            ]}
          />

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '0.75rem' }}>
            <FormField
              label="Purchase Date"
              name="purchaseDate"
              type="date"
              value={equipForm.purchaseDate}
              onChange={(e) => setEquipForm({ ...equipForm, purchaseDate: e.target.value })}
            />
            <FormField
              label="Last Serviced"
              name="lastServiceDate"
              type="date"
              value={equipForm.lastServiceDate}
              onChange={(e) => setEquipForm({ ...equipForm, lastServiceDate: e.target.value })}
            />
            <FormField
              label="Next Service Due"
              name="nextServiceDue"
              type="date"
              value={equipForm.nextServiceDue}
              onChange={(e) => setEquipForm({ ...equipForm, nextServiceDue: e.target.value })}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setAddOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Register
            </button>
          </div>
        </form>
      </Modal>

      {/* Edit Equipment Modal */}
      <Modal
        isOpen={editOpen}
        onClose={() => {
          setEditOpen(false);
          setEditEquip(null);
        }}
        title={`Edit Equipment: ${editEquip?.name}`}
        maxWidth="500px"
      >
        {editEquip && (
          <form onSubmit={handleEditSubmit}>
            <FormField
              label="Equipment Name"
              name="name"
              value={editEquip.name}
              onChange={(e) => setEditEquip({ ...editEquip, name: e.target.value })}
              required
            />
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <FormField
                label="Category"
                name="category"
                value={editEquip.category}
                onChange={(e) => setEditEquip({ ...editEquip, category: e.target.value })}
                required
              />
              <FormField
                label="Status"
                name="status"
                type="select"
                value={editEquip.status}
                onChange={(e) => setEditEquip({ ...editEquip, status: e.target.value })}
                options={EQUIP_STATUSES}
              />
            </div>

            <FormField
              label="Assigned Project Site"
              name="projectId"
              type="select"
              value={editEquip.projectId || ''}
              onChange={(e) => setEditEquip({ ...editEquip, projectId: e.target.value })}
              options={[
                { value: '', label: 'None (In Warehouse Depot)' },
                ...projects.map(p => ({ value: p.id, label: p.projectName }))
              ]}
            />

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <FormField
                label="Last Serviced"
                name="lastServiceDate"
                type="date"
                value={editEquip.lastServiceDate || ''}
                onChange={(e) => setEditEquip({ ...editEquip, lastServiceDate: e.target.value })}
              />
              <FormField
                label="Next Service Due"
                name="nextServiceDue"
                type="date"
                value={editEquip.nextServiceDue || ''}
                onChange={(e) => setEditEquip({ ...editEquip, nextServiceDue: e.target.value })}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  setEditOpen(false);
                  setEditEquip(null);
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
