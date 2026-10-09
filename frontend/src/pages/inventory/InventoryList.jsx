import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';

export default function InventoryList() {
  const { hasPermission } = useAuth();
  const [activeTab, setActiveTab] = useState('catalog'); // 'catalog' | 'history'
  const [materials, setMaterials] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [projects, setProjects] = useState([]);
  const [lowStockOnly, setLowStockOnly] = useState(false);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  // Add Material Modal
  const [addOpen, setAddOpen] = useState(false);
  const [materialForm, setMaterialForm] = useState({
    materialCode: '',
    name: '',
    category: 'Structural',
    unit: 'Bags',
    quantityOnHand: 0,
    minStock: 10,
    maxStock: 500,
    unitPrice: 0,
    location: 'Warehouse A'
  });

  // Stock Movement Modal (Receipt or Issue)
  const [movementModal, setMovementModal] = useState({ open: false, type: 'RECEIPT', material: null });
  const [movementForm, setMovementForm] = useState({
    quantity: '',
    projectId: '',
    referenceNo: '',
    remarks: ''
  });
  const [submitting, setSubmitting] = useState(false);

  const fetchData = async () => {
    try {
      const [mRes, pRes] = await Promise.all([
        api.get(`/inventory${lowStockOnly ? '?lowStockOnly=true' : ''}`),
        api.get('/projects')
      ]);
      if (mRes.success) setMaterials(mRes.data);
      if (pRes.success) setProjects(pRes.data);
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to fetch inventory' });
    } finally {
      setLoading(false);
    }
  };

  const fetchTransactions = async () => {
    try {
      const res = await api.get('/inventory/transactions');
      if (res.success) setTransactions(res.data);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchData();
  }, [lowStockOnly]);

  useEffect(() => {
    if (activeTab === 'history') {
      fetchTransactions();
    }
  }, [activeTab]);

  const handleAddSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/inventory', {
        ...materialForm,
        quantityOnHand: Number(materialForm.quantityOnHand),
        minStock: Number(materialForm.minStock),
        maxStock: Number(materialForm.maxStock),
        unitPrice: Number(materialForm.unitPrice)
      });
      setMessage({ type: 'success', text: `Material "${materialForm.name}" added to catalog.` });
      setAddOpen(false);
      setMaterialForm({
        materialCode: '',
        name: '',
        category: 'Structural',
        unit: 'Bags',
        quantityOnHand: 0,
        minStock: 10,
        maxStock: 500,
        unitPrice: 0,
        location: 'Warehouse A'
      });
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to add material' });
    }
  };

  const handleMovementSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    const { type, material } = movementModal;

    try {
      const endpoint = type === 'RECEIPT' ? '/inventory/receipt' : '/inventory/issue';
      const payload = {
        materialId: material.id,
        quantity: Number(movementForm.quantity),
        referenceNo: movementForm.referenceNo,
        remarks: movementForm.remarks
      };

      if (type === 'ISSUE') {
        payload.projectId = movementForm.projectId ? Number(movementForm.projectId) : null;
      }

      await api.post(endpoint, payload);
      setMessage({
        type: 'success',
        text: `Successfully recorded ${type === 'RECEIPT' ? 'receipt' : 'issue'} of ${movementForm.quantity} ${material.unit} for ${material.name}.`
      });

      setMovementModal({ open: false, type: 'RECEIPT', material: null });
      setMovementForm({ quantity: '', projectId: '', referenceNo: '', remarks: '' });
      fetchData();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Transaction failed' });
    } finally {
      setSubmitting(false);
    }
  };

  const catalogColumns = [
    { header: 'Code', accessor: 'materialCode', width: '90px', render: (row) => <code>{row.materialCode}</code> },
    { header: 'Material Name', accessor: 'name', render: (row) => <strong>{row.name}</strong> },
    { header: 'Category', accessor: 'category', render: (row) => <span className="badge badge-secondary">{row.category}</span> },
    {
      header: 'Stock Level',
      render: (row) => {
        const isLow = Number(row.quantityOnHand) <= Number(row.minStock);
        return (
          <span style={{ fontWeight: 'bold', color: isLow ? 'var(--color-danger)' : 'inherit' }}>
            {row.quantityOnHand} {row.unit} {isLow && <span className="badge badge-danger" style={{ marginLeft: '4px' }}>LOW</span>}
          </span>
        );
      }
    },
    { header: 'Min Stock', accessor: 'minStock', render: (row) => `${row.minStock} ${row.unit}` },
    { header: 'Unit Price', accessor: 'unitPrice', render: (row) => `$${Number(row.unitPrice || 0).toFixed(2)}` },
    {
      header: 'Total Value',
      render: (row) => `$${(Number(row.quantityOnHand || 0) * Number(row.unitPrice || 0)).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
    },
    { header: 'Location', accessor: 'location', render: (row) => row.location || '—' },
    {
      header: 'Stock Actions',
      render: (row) => (
        <div style={{ display: 'flex', gap: '0.4rem' }}>
          {hasPermission('INVENTORY_RECEIPT') && (
            <button
              className="btn btn-success btn-sm"
              onClick={() => {
                setMovementModal({ open: true, type: 'RECEIPT', material: row });
                setMovementForm({ quantity: '', projectId: '', referenceNo: '', remarks: '' });
              }}
            >
              + Receive
            </button>
          )}

          {hasPermission('INVENTORY_ISSUE') && (
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => {
                setMovementModal({ open: true, type: 'ISSUE', material: row });
                setMovementForm({ quantity: '', projectId: projects[0]?.id || '', referenceNo: '', remarks: '' });
              }}
            >
              - Issue
            </button>
          )}
        </div>
      )
    }
  ];

  const transactionColumns = [
    { header: 'ID', accessor: 'id', width: '50px' },
    {
      header: 'Type',
      accessor: 'transactionType',
      render: (row) => (
        <span className={`badge badge-${row.transactionType === 'RECEIPT' ? 'success' : 'primary'}`}>
          {row.transactionType}
        </span>
      )
    },
    { header: 'Material', render: (row) => `${row.materialName} (${row.materialCode})` },
    { header: 'Quantity', accessor: 'quantity' },
    { header: 'Project Site', accessor: 'projectName', render: (row) => row.projectName || '—' },
    { header: 'Reference', accessor: 'referenceNo', render: (row) => row.referenceNo || '—' },
    { header: 'Logged By', accessor: 'performerName', render: (row) => row.performerName || 'System' },
    {
      header: 'Timestamp',
      accessor: 'transactionDate',
      render: (row) => row.transactionDate ? new Date(row.transactionDate).toLocaleString() : ''
    }
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2>Inventory Management</h2>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Material stock levels, procurement receipts, site issues and automatic low-stock notifications.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          {activeTab === 'catalog' && (
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.85rem', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={lowStockOnly}
                onChange={(e) => setLowStockOnly(e.target.checked)}
              />
              Show Low Stock Only
            </label>
          )}

          {hasPermission('INVENTORY_CREATE') && activeTab === 'catalog' && (
            <button className="btn btn-primary" onClick={() => setAddOpen(true)}>
              + Add Material
            </button>
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
          className={`btn ${activeTab === 'catalog' ? 'btn-primary' : 'btn-secondary'}`}
          style={{ borderBottomLeftRadius: 0, borderBottomRightRadius: 0 }}
          onClick={() => setActiveTab('catalog')}
        >
          Materials Catalog ({materials.length})
        </button>
        <button
          className={`btn ${activeTab === 'history' ? 'btn-primary' : 'btn-secondary'}`}
          style={{ borderBottomLeftRadius: 0, borderBottomRightRadius: 0 }}
          onClick={() => setActiveTab('history')}
        >
          Transaction History
        </button>
      </div>

      {loading ? (
        <p>Loading inventory...</p>
      ) : activeTab === 'catalog' ? (
        <DataTable
          columns={catalogColumns}
          data={materials}
          keyField="id"
          searchPlaceholder="Search materials by name, code, category..."
        />
      ) : (
        <DataTable
          columns={transactionColumns}
          data={transactions}
          keyField="id"
          searchPlaceholder="Search transaction trail..."
        />
      )}

      {/* Add Material Modal */}
      <Modal
        isOpen={addOpen}
        onClose={() => setAddOpen(false)}
        title="Add Material to Catalog"
        maxWidth="500px"
      >
        <form onSubmit={handleAddSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1rem' }}>
            <FormField
              label="Material Code"
              name="materialCode"
              value={materialForm.materialCode}
              onChange={(e) => setMaterialForm({ ...materialForm, materialCode: e.target.value })}
              required
              placeholder="e.g. CEM-43"
            />
            <FormField
              label="Material Name"
              name="name"
              value={materialForm.name}
              onChange={(e) => setMaterialForm({ ...materialForm, name: e.target.value })}
              required
              placeholder="e.g. Portland Cement Grade 43"
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <FormField
              label="Category"
              name="category"
              value={materialForm.category}
              onChange={(e) => setMaterialForm({ ...materialForm, category: e.target.value })}
              required
            />
            <FormField
              label="Unit of Measurement"
              name="unit"
              value={materialForm.unit}
              onChange={(e) => setMaterialForm({ ...materialForm, unit: e.target.value })}
              required
              placeholder="e.g. Bags, Tons, Pieces"
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '0.75rem' }}>
            <FormField
              label="Initial Stock"
              name="quantityOnHand"
              type="number"
              value={materialForm.quantityOnHand}
              onChange={(e) => setMaterialForm({ ...materialForm, quantityOnHand: e.target.value })}
              required
            />
            <FormField
              label="Min Reorder Level"
              name="minStock"
              type="number"
              value={materialForm.minStock}
              onChange={(e) => setMaterialForm({ ...materialForm, minStock: e.target.value })}
              required
            />
            <FormField
              label="Unit Price ($)"
              name="unitPrice"
              type="number"
              value={materialForm.unitPrice}
              onChange={(e) => setMaterialForm({ ...materialForm, unitPrice: e.target.value })}
              required
            />
          </div>

          <FormField
            label="Storage Location"
            name="location"
            value={materialForm.location}
            onChange={(e) => setMaterialForm({ ...materialForm, location: e.target.value })}
            placeholder="e.g. Warehouse 1, Bay C"
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setAddOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Save Material
            </button>
          </div>
        </form>
      </Modal>

      {/* Movement Modal (Receive / Issue) */}
      <Modal
        isOpen={movementModal.open}
        onClose={() => setMovementModal({ open: false, type: 'RECEIPT', material: null })}
        title={`${movementModal.type === 'RECEIPT' ? 'Receive' : 'Issue'} Stock: ${movementModal.material?.name}`}
        maxWidth="460px"
      >
        <form onSubmit={handleMovementSubmit}>
          <div style={{ background: 'var(--color-table-zebra)', padding: '0.75rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.85rem' }}>
            <div>Current Stock: <strong>{movementModal.material?.quantityOnHand} {movementModal.material?.unit}</strong></div>
            <div>Reorder Level: {movementModal.material?.minStock} {movementModal.material?.unit}</div>
          </div>

          <FormField
            label={`Quantity to ${movementModal.type === 'RECEIPT' ? 'Receive' : 'Issue'} (${movementModal.material?.unit})`}
            name="quantity"
            type="number"
            value={movementForm.quantity}
            onChange={(e) => setMovementForm({ ...movementForm, quantity: e.target.value })}
            required
            placeholder="e.g. 50"
          />

          {movementModal.type === 'ISSUE' && (
            <FormField
              label="Destination Project Site"
              name="projectId"
              type="select"
              value={movementForm.projectId}
              onChange={(e) => setMovementForm({ ...movementForm, projectId: e.target.value })}
              options={projects.map(p => ({ value: p.id, label: p.projectName }))}
              required
            />
          )}

          <FormField
            label="Reference No. (PO # / Request #)"
            name="referenceNo"
            value={movementForm.referenceNo}
            onChange={(e) => setMovementForm({ ...movementForm, referenceNo: e.target.value })}
            placeholder="e.g. PO-88321"
          />

          <FormField
            label="Remarks"
            name="remarks"
            value={movementForm.remarks}
            onChange={(e) => setMovementForm({ ...movementForm, remarks: e.target.value })}
            placeholder="Optional transaction note..."
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setMovementModal({ open: false, type: 'RECEIPT', material: null })}
            >
              Cancel
            </button>
            <button
              type="submit"
              className={`btn btn-${movementModal.type === 'RECEIPT' ? 'success' : 'primary'}`}
              disabled={submitting}
            >
              {submitting ? 'Recording...' : movementModal.type === 'RECEIPT' ? 'Confirm Receipt' : 'Confirm Stock Issue'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
