import React, { useState, useMemo } from 'react';

export default function DataTable({
  columns = [],
  data = [],
  keyField = 'id',
  searchable = true,
  searchPlaceholder = 'Search records...',
  searchFilter,
  actions,
  pageSize = 10,
  emptyMessage = 'No records found.'
}) {
  const [search, setSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(1);

  // Filter data
  const filteredData = useMemo(() => {
    if (!search.trim()) return data;
    const query = search.toLowerCase();
    if (searchFilter) {
      return data.filter((item) => searchFilter(item, query));
    }
    return data.filter((item) =>
      columns.some((col) => {
        const val = col.accessor ? item[col.accessor] : null;
        return val != null && String(val).toLowerCase().includes(query);
      })
    );
  }, [data, search, searchFilter, columns]);

  // Pagination
  const totalPages = Math.max(1, Math.ceil(filteredData.length / pageSize));
  const paginatedData = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return filteredData.slice(start, start + pageSize);
  }, [filteredData, currentPage, pageSize]);

  const handleSearchChange = (e) => {
    setSearch(e.target.value);
    setCurrentPage(1);
  };

  return (
    <div className="card table-card">
      {(searchable || actions) && (
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', gap: '12px', flexWrap: 'wrap' }}>
          {searchable ? (
            <input
              type="text"
              style={{ maxWidth: '280px' }}
              placeholder={searchPlaceholder}
              value={search}
              onChange={handleSearchChange}
            />
          ) : <div />}

          {actions && <div>{actions}</div>}
        </div>
      )}

      <div className="table-wrapper">
        <table className="table">
          <thead>
            <tr>
              {columns.map((col, idx) => (
                <th key={idx} style={{ textAlign: col.align || 'left', width: col.width || 'auto' }}>
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {paginatedData.length === 0 ? (
              <tr>
                <td colSpan={columns.length} style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--muted)' }}>
                  {emptyMessage}
                </td>
              </tr>
            ) : (
              paginatedData.map((row, rIdx) => {
                const rowKey = row[keyField] != null ? row[keyField] : rIdx;
                return (
                  <tr key={rowKey}>
                    {columns.map((col, cIdx) => (
                      <td key={cIdx} style={{ textAlign: col.align || 'left' }}>
                        {col.render ? col.render(row) : (row[col.accessor] != null ? String(row[col.accessor]) : '—')}
                      </td>
                    ))}
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '16px', fontSize: '12px' }}>
          <span style={{ color: 'var(--muted)' }}>
            Showing {Math.min(filteredData.length, (currentPage - 1) * pageSize + 1)} to {Math.min(filteredData.length, currentPage * pageSize)} of {filteredData.length} records
          </span>

          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <button
              className="btn btn-secondary btn-sm"
              disabled={currentPage === 1}
              onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
            >
              Previous
            </button>
            <span style={{ color: 'var(--muted)' }}>
              Page {currentPage} of {totalPages}
            </span>
            <button
              className="btn btn-secondary btn-sm"
              disabled={currentPage === totalPages}
              onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
