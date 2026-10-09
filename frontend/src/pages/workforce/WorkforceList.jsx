import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';

export default function WorkforceList() {
  const { hasPermission } = useAuth();
  const [activeTab, setActiveTab] = useState('workers'); // 'workers' | 'contractors'
  const [workers, setWorkers] = useState([]);
  const [contractors, setContractors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  // Worker Modal
  const [workerModalOpen, setWorkerModalOpen] = useState(false);
  const [editingWorker, setEditingWorker] = useState(null);
  const [workerForm, setWorkerForm] = useState({
    fullName: '',
    email: '',
    phone: '',
    trade: 'General Labor',
    contractorId: '',
    status: 'ACTIVE'
  });

  // Contractor Modal
  const [contractorModalOpen, setContractorModalOpen] = useState(false);
  const [editingContractor, setEditingContractor] = useState(null);
  const [contractorForm, setContractorForm] = useState({
    companyName: '',
    contactPerson: '',
    email: '',
    phone: '',
    address: ''
  });

  const fetchData = async () => {
    try {
      const [wRes, cRes] = await Promise.all([
        api.get('/workforce/workers'),
        api.get('/workforce/contractors')
      ]);
      if (wRes.success) setWorkers(wRes.data);
      if (cRes.success) setContractors(cRes.data);
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to fetch workforce data' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleWorkerSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        ...workerForm,
        contractorId: workerForm.contractorId ? Number(workerForm.contractorId) : null
      };

      if (editingWorker) {
        await api.put(`/workforce/workers/${editingWorker.id}`, payload);
        setMessage({ type: 'success', text: `Worker ${payload.fullName} updated.` });
      } else {
        await api.post('/workforce/workers', payload);
        setMessage({ type: 'success', text: `Worker ${payload.fullName} registered.` });
      }

      setWorkerModalOpen(false);
      setEditingWorker(null);
      setWorkerForm({ fullName: '', email: '', phone: '', trade: 'General Labor', contractorId: '', status: 'ACTIVE' });
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to save worker' });
    }
  };

  const handleContractorSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingContractor) {
        await api.put(`/workforce/contractors/${editingContractor.id}`, contractorForm);
        setMessage({ type: 'success', text: `Contractor ${contractorForm.companyName} updated.` });
      } else {
        await api.post('/workforce/contractors', contractorForm);
        setMessage({ type: 'success', text: `Contractor ${contractorForm.companyName} added.` });
      }

      setContractorModalOpen(false);
      setEditingContractor(null);
      setContractorForm({ companyName: '', contactPerson: '', email: '', phone: '', address: '' });
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to save contractor' });
    }
  };

  const workerColumns = [
    { header: 'ID', accessor: 'id', width: '50px' },
    { header: 'Full Name', accessor: 'fullName', render: (row) => <strong>{row.fullName}</strong> },
    { header: 'Trade', accessor: 'trade', render: (row) => <span className="badge badge-primary">{row.trade}</span> },
    { header: 'Contractor', accessor: 'contractorName', render: (row) => row.contractorName || 'Direct Hire' },
    { header: 'Phone', accessor: 'phone' },
    { header: 'Email', accessor: 'email' },
    {
      header: 'Status',
      accessor: 'status',
      render: (row) => (
        <span className={`badge badge-${row.status === 'ACTIVE' ? 'success' : 'danger'}`}>
          {row.status}
        </span>
      )
    },
    {
      header: 'Actions',
      render: (row) => hasPermission('WORKFORCE_UPDATE') && (
        <button
          className="btn btn-secondary btn-sm"
          onClick={() => {
            setEditingWorker(row);
            setWorkerForm({
              fullName: row.fullName,
              email: row.email || '',
              phone: row.phone || '',
              trade: row.trade,
              contractorId: row.contractorId || '',
              status: row.status
            });
            setWorkerModalOpen(true);
          }}
        >
          Edit
        </button>
      )
    }
  ];

  const contractorColumns = [
    { header: 'ID', accessor: 'id', width: '50px' },
    { header: 'Company Name', accessor: 'companyName', render: (row) => <strong>{row.companyName}</strong> },
    { header: 'Contact Person', accessor: 'contactPerson' },
    { header: 'Phone', accessor: 'phone' },
    { header: 'Email', accessor: 'email' },
    { header: 'Address', accessor: 'address' },
    {
      header: 'Actions',
      render: (row) => hasPermission('WORKFORCE_UPDATE') && (
        <button
          className="btn btn-secondary btn-sm"
          onClick={() => {
            setEditingContractor(row);
            setContractorForm({
              companyName: row.companyName,
              contactPerson: row.contactPerson || '',
              email: row.email || '',
              phone: row.phone || '',
              address: row.address || ''
            });
            setContractorModalOpen(true);
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
          <h2>Workforce & Contractors</h2>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Manage site labor, specialized trade workers, and external subcontractors.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem' }}>
          {hasPermission('WORKFORCE_CREATE') && (
            activeTab === 'workers' ? (
              <button
                className="btn btn-primary"
                onClick={() => {
                  setEditingWorker(null);
                  setWorkerForm({ fullName: '', email: '', phone: '', trade: 'General Labor', contractorId: '', status: 'ACTIVE' });
                  setWorkerModalOpen(true);
                }}
              >
                + Add Worker
              </button>
            ) : (
              <button
                className="btn btn-primary"
                onClick={() => {
                  setEditingContractor(null);
                  setContractorForm({ companyName: '', contactPerson: '', email: '', phone: '', address: '' });
                  setContractorModalOpen(true);
                }}
              >
                + Add Contractor
              </button>
            )
          )}
        </div>
      </div>

      {message && (
        <div className={`alert alert-${message.type}`} style={{ marginBottom: '1rem' }}>
          {message.text}
        </div>
      )}

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', borderBottom: '1px solid var(--color-border)', marginBottom: '1.5rem' }}>
        <button
          className={`btn ${activeTab === 'workers' ? 'btn-primary' : 'btn-secondary'}`}
          style={{ borderBottomLeftRadius: 0, borderBottomRightRadius: 0 }}
          onClick={() => setActiveTab('workers')}
        >
          Workers ({workers.length})
        </button>
        <button
          className={`btn ${activeTab === 'contractors' ? 'btn-primary' : 'btn-secondary'}`}
          style={{ borderBottomLeftRadius: 0, borderBottomRightRadius: 0 }}
          onClick={() => setActiveTab('contractors')}
        >
          Contractor Companies ({contractors.length})
        </button>
      </div>

      {loading ? (
        <p>Loading workforce records...</p>
      ) : activeTab === 'workers' ? (
        <DataTable
          columns={workerColumns}
          data={workers}
          keyField="id"
          searchPlaceholder="Search workers by name, trade, contractor..."
        />
      ) : (
        <DataTable
          columns={contractorColumns}
          data={contractors}
          keyField="id"
          searchPlaceholder="Search contractor companies..."
        />
      )}

      {/* Worker Modal */}
      <Modal
        isOpen={workerModalOpen}
        onClose={() => setWorkerModalOpen(false)}
        title={editingWorker ? `Edit Worker: ${editingWorker.fullName}` : 'Add New Worker'}
        maxWidth="480px"
      >
        <form onSubmit={handleWorkerSubmit}>
          <FormField
            label="Full Name"
            name="fullName"
            value={workerForm.fullName}
            onChange={(e) => setWorkerForm({ ...workerForm, fullName: e.target.value })}
            required
            placeholder="e.g. John Smith"
          />
          <FormField
            label="Trade / Specialization"
            name="trade"
            value={workerForm.trade}
            onChange={(e) => setWorkerForm({ ...workerForm, trade: e.target.value })}
            required
            placeholder="e.g. Electrician, Carpenter, Mason, Welder"
          />
          <FormField
            label="Contractor"
            name="contractorId"
            type="select"
            value={workerForm.contractorId}
            onChange={(e) => setWorkerForm({ ...workerForm, contractorId: e.target.value })}
            options={[
              { value: '', label: 'Direct Hire (No Contractor)' },
              ...contractors.map(c => ({ value: c.id, label: c.companyName }))
            ]}
          />
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <FormField
              label="Phone Number"
              name="phone"
              value={workerForm.phone}
              onChange={(e) => setWorkerForm({ ...workerForm, phone: e.target.value })}
            />
            <FormField
              label="Email"
              name="email"
              type="email"
              value={workerForm.email}
              onChange={(e) => setWorkerForm({ ...workerForm, email: e.target.value })}
            />
          </div>
          <FormField
            label="Status"
            name="status"
            type="select"
            value={workerForm.status}
            onChange={(e) => setWorkerForm({ ...workerForm, status: e.target.value })}
            options={[
              { value: 'ACTIVE', label: 'Active' },
              { value: 'INACTIVE', label: 'Inactive' }
            ]}
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setWorkerModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              {editingWorker ? 'Save Changes' : 'Register Worker'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Contractor Modal */}
      <Modal
        isOpen={contractorModalOpen}
        onClose={() => setContractorModalOpen(false)}
        title={editingContractor ? `Edit Contractor: ${editingContractor.companyName}` : 'Add New Contractor Company'}
        maxWidth="480px"
      >
        <form onSubmit={handleContractorSubmit}>
          <FormField
            label="Company Name"
            name="companyName"
            value={contractorForm.companyName}
            onChange={(e) => setContractorForm({ ...contractorForm, companyName: e.target.value })}
            required
            placeholder="e.g. Apex Electricals Ltd."
          />
          <FormField
            label="Contact Person"
            name="contactPerson"
            value={contractorForm.contactPerson}
            onChange={(e) => setContractorForm({ ...contractorForm, contactPerson: e.target.value })}
            placeholder="e.g. Dave Miller"
          />
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <FormField
              label="Phone"
              name="phone"
              value={contractorForm.phone}
              onChange={(e) => setContractorForm({ ...contractorForm, phone: e.target.value })}
            />
            <FormField
              label="Email"
              name="email"
              type="email"
              value={contractorForm.email}
              onChange={(e) => setContractorForm({ ...contractorForm, email: e.target.value })}
            />
          </div>
          <FormField
            label="Address"
            name="address"
            type="textarea"
            value={contractorForm.address}
            onChange={(e) => setContractorForm({ ...contractorForm, address: e.target.value })}
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setContractorModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              {editingContractor ? 'Save Changes' : 'Register Contractor'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
