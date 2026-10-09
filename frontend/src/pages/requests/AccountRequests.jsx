import React, { useState, useEffect } from 'react';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import FormField from '../../components/FormField';

export default function AccountRequests() {
  const { hasPermission } = useAuth();
  const [requests, setRequests] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  // Review modal state
  const [reviewDialog, setReviewDialog] = useState({ open: false, request: null, action: 'approve' });
  const [reviewNotes, setReviewNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchRequests = async () => {
    try {
      const url = statusFilter ? `/account-requests?status=${statusFilter}` : '/account-requests';
      const res = await api.get(url);
      if (res.success) {
        setRequests(res.data);
      }
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to fetch account requests' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, [statusFilter]);

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    const { request, action } = reviewDialog;
    if (!request) return;

    setSubmitting(true);
    try {
      const endpoint = `/account-requests/${request.id}/${action}`;
      await api.post(endpoint, { reviewNotes });

      setMessage({
        type: 'success',
        text: `Request for ${request.username} successfully ${action === 'approve' ? 'approved' : 'rejected'}.`
      });

      setReviewDialog({ open: false, request: null, action: 'approve' });
      setReviewNotes('');
      fetchRequests();
    } catch (err) {
      setMessage({ type: 'danger', text: err.message || 'Failed to review request' });
    } finally {
      setSubmitting(false);
    }
  };

  const columns = [
    { header: 'ID', accessor: 'id', width: '50px' },
    {
      header: 'Type',
      accessor: 'requestType',
      render: (row) => (
        <span className={`badge badge-${row.requestType === 'REGISTRATION' ? 'primary' : 'warning'}`}>
          {row.requestType}
        </span>
      )
    },
    { header: 'Full Name', accessor: 'fullName' },
    { header: 'Username', accessor: 'username' },
    { header: 'Email', accessor: 'email' },
    { header: 'Requested Role', accessor: 'requestedRoleName', render: (row) => row.requestedRoleName || '—' },
    { header: 'Reason', accessor: 'reason' },
    {
      header: 'Status',
      accessor: 'status',
      render: (row) => (
        <span className={`badge badge-${row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'warning'}`}>
          {row.status}
        </span>
      )
    },
    {
      header: 'Date',
      accessor: 'createdAt',
      render: (row) => row.createdAt ? new Date(row.createdAt).toLocaleDateString() : ''
    },
    {
      header: 'Actions',
      render: (row) => {
        if (row.status !== 'PENDING') {
          return <span style={{ color: 'var(--color-text-muted)', fontSize: '0.8rem' }}>Reviewed</span>;
        }
        if (!hasPermission('ACCOUNT_REQUEST_REVIEW')) return null;

        return (
          <div style={{ display: 'flex', gap: '0.4rem' }}>
            <button
              className="btn btn-success btn-sm"
              onClick={() => {
                setReviewDialog({ open: true, request: row, action: 'approve' });
                setReviewNotes('Approved by administrator');
              }}
            >
              Approve
            </button>
            <button
              className="btn btn-danger btn-sm"
              onClick={() => {
                setReviewDialog({ open: true, request: row, action: 'reject' });
                setReviewNotes('');
              }}
            >
              Reject
            </button>
          </div>
        );
      }
    }
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2>Account Requests</h2>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Review new user registration requests and password reset submissions.
          </p>
        </div>

        {/* Status Filter */}
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <label style={{ fontSize: '0.85rem' }}>Status:</label>
          <select
            className="form-input"
            style={{ width: 'auto' }}
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">All Statuses</option>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </div>
      </div>

      {message && (
        <div className={`alert alert-${message.type}`} style={{ marginBottom: '1rem' }}>
          {message.text}
        </div>
      )}

      {loading ? (
        <p>Loading requests...</p>
      ) : (
        <DataTable
          columns={columns}
          data={requests}
          keyField="id"
          searchPlaceholder="Search requests..."
        />
      )}

      {/* Approve / Reject Dialog */}
      <Modal
        isOpen={reviewDialog.open}
        onClose={() => setReviewDialog({ open: false, request: null, action: 'approve' })}
        title={`${reviewDialog.action === 'approve' ? 'Approve' : 'Reject'} Request: ${reviewDialog.request?.username}`}
        maxWidth="440px"
      >
        <form onSubmit={handleReviewSubmit}>
          <p style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>
            {reviewDialog.action === 'approve'
              ? reviewDialog.request?.requestType === 'REGISTRATION'
                ? 'Approving will create an active account with temporary password "Welcome@123" and force a password change upon login.'
                : 'Approving will reset the account password to "Reset@12345" and force a password change upon login.'
              : 'Please provide notes/reason for rejecting this request.'}
          </p>

          <FormField
            label="Review Notes / Reason"
            name="reviewNotes"
            type="textarea"
            value={reviewNotes}
            onChange={(e) => setReviewNotes(e.target.value)}
            required={reviewDialog.action === 'reject'}
            placeholder={reviewDialog.action === 'reject' ? 'Reason for rejection...' : 'Optional approval notes...'}
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => setReviewDialog({ open: false, request: null, action: 'approve' })}
            >
              Cancel
            </button>
            <button
              type="submit"
              className={`btn btn-${reviewDialog.action === 'approve' ? 'success' : 'danger'}`}
              disabled={submitting}
            >
              {submitting ? 'Processing...' : reviewDialog.action === 'approve' ? 'Confirm Approval' : 'Confirm Rejection'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
