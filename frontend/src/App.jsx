import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';

// Auth & Public Pages
import Landing from './pages/Landing';
import Login from './pages/Login';

// App Pages
import Dashboard from './pages/Dashboard';
import UserList from './pages/users/UserList';
import RolePermissions from './pages/roles/RolePermissions';
import AccountRequests from './pages/requests/AccountRequests';
import AuditLogs from './pages/audit/AuditLogs';

// Phase 4-7 Placeholders (to be linked as we build each module)
import ProjectsList from './pages/projects/ProjectsList';
import WorkforceList from './pages/workforce/WorkforceList';
import TasksList from './pages/tasks/TasksList';
import AttendanceSheet from './pages/attendance/AttendanceSheet';
import InventoryList from './pages/inventory/InventoryList';
import EquipmentList from './pages/equipment/EquipmentList';
import MaintenanceList from './pages/maintenance/MaintenanceList';
import SafetyList from './pages/safety/SafetyList';
import IncidentsList from './pages/incidents/IncidentsList';
import ReportsList from './pages/reports/ReportsList';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />

          {/* Protected App Routes inside Shell */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route path="dashboard" element={<Dashboard />} />
            
            {/* Phase 3: Admin & Security */}
            <Route path="users" element={<UserList />} />
            <Route path="roles" element={<RolePermissions />} />
            <Route path="account-requests" element={<AccountRequests />} />
            <Route path="audit-logs" element={<AuditLogs />} />

            {/* Phase 4: Core Construction Operations */}
            <Route path="projects" element={<ProjectsList />} />
            <Route path="workforce" element={<WorkforceList />} />
            <Route path="tasks" element={<TasksList />} />
            <Route path="attendance" element={<AttendanceSheet />} />

            {/* Phase 5: Inventory, Equipment, Maintenance */}
            <Route path="inventory" element={<InventoryList />} />
            <Route path="equipment" element={<EquipmentList />} />
            <Route path="maintenance" element={<MaintenanceList />} />

            {/* Phase 6: Safety & Incidents */}
            <Route path="safety" element={<SafetyList />} />
            <Route path="incidents" element={<IncidentsList />} />

            {/* Phase 7: Reports */}
            <Route path="reports" element={<ReportsList />} />
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
