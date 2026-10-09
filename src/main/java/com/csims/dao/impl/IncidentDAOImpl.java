package com.csims.dao.impl;

import com.csims.dao.IncidentDAO;
import com.csims.model.Incident;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class IncidentDAOImpl implements IncidentDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private Incident mapRow(ResultSet rs) throws SQLException {
        Incident inc = new Incident();
        inc.setIncidentId(rs.getInt("incident_id"));
        inc.setProjectId(rs.getInt("project_id"));

        Timestamp iTs = rs.getTimestamp("incident_date");
        if (iTs != null) inc.setIncidentDate(iTs.toLocalDateTime());

        inc.setSeverity(rs.getString("severity"));
        inc.setTitle(rs.getString("title"));
        inc.setDescription(rs.getString("description"));

        int invId = rs.getInt("investigator_id");
        if (!rs.wasNull()) inc.setInvestigatorId(invId);

        inc.setActionsTaken(rs.getString("actions_taken"));
        inc.setStatus(rs.getString("status"));

        int repBy = rs.getInt("reported_by");
        if (!rs.wasNull()) inc.setReportedBy(repBy);

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) inc.setCreatedAt(cTs.toLocalDateTime());

        Timestamp uTs = rs.getTimestamp("updated_at");
        if (uTs != null) inc.setUpdatedAt(uTs.toLocalDateTime());

        try {
            inc.setProjectName(rs.getString("project_name"));
        } catch (SQLException ignored) {}

        try {
            inc.setInvestigatorName(rs.getString("investigator_name"));
        } catch (SQLException ignored) {}

        try {
            inc.setReporterName(rs.getString("reporter_name"));
        } catch (SQLException ignored) {}

        return inc;
    }

    @Override
    public Optional<Incident> findById(int incidentId) {
        String sql = "SELECT inc.*, p.project_name, u1.full_name as investigator_name, u2.username as reporter_name " +
                     "FROM incidents inc " +
                     "JOIN projects p ON inc.project_id = p.project_id " +
                     "LEFT JOIN users u1 ON inc.investigator_id = u1.user_id " +
                     "LEFT JOIN users u2 ON inc.reported_by = u2.user_id " +
                     "WHERE inc.incident_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, incidentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding incident: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Incident> findAll() {
        return search(null, null, null, null, null);
    }

    @Override
    public List<Incident> search(Integer projectId, String severity, String status, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT inc.*, p.project_name, u1.full_name as investigator_name, u2.username as reporter_name " +
            "FROM incidents inc " +
            "JOIN projects p ON inc.project_id = p.project_id " +
            "LEFT JOIN users u1 ON inc.investigator_id = u1.user_id " +
            "LEFT JOIN users u2 ON inc.reported_by = u2.user_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (projectId != null && projectId > 0) {
            sql.append("AND inc.project_id = ? ");
            params.add(projectId);
        }

        if (severity != null && !severity.trim().isEmpty() && !severity.equalsIgnoreCase("ALL")) {
            sql.append("AND inc.severity = ? ");
            params.add(severity);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND inc.status = ? ");
            params.add(status);
        }

        if (startDate != null) {
            sql.append("AND inc.incident_date >= ? ");
            params.add(DateUtil.toSqlTimestamp(startDate.atStartOfDay()));
        }

        if (endDate != null) {
            sql.append("AND inc.incident_date <= ? ");
            params.add(DateUtil.toSqlTimestamp(endDate.atTime(23, 59, 59)));
        }

        sql.append("ORDER BY inc.incident_date DESC");

        List<Incident> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching incidents: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int create(Incident incident) {
        String sql = "INSERT INTO incidents (project_id, incident_date, severity, title, description, " +
                     "investigator_id, actions_taken, status, reported_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, incident.getProjectId());
            ps.setTimestamp(2, DateUtil.toSqlTimestamp(incident.getIncidentDate()));
            ps.setString(3, incident.getSeverity() != null ? incident.getSeverity() : "Minor");
            ps.setString(4, incident.getTitle());
            ps.setString(5, incident.getDescription());
            if (incident.getInvestigatorId() != null) {
                ps.setInt(6, incident.getInvestigatorId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, incident.getActionsTaken());
            ps.setString(8, incident.getStatus() != null ? incident.getStatus() : "Reported");
            if (incident.getReportedBy() != null) {
                ps.setInt(9, incident.getReportedBy());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    incident.setIncidentId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating incident: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Incident incident) {
        String sql = "UPDATE incidents SET project_id = ?, incident_date = ?, severity = ?, title = ?, " +
                     "description = ?, investigator_id = ?, actions_taken = ?, status = ? WHERE incident_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, incident.getProjectId());
            ps.setTimestamp(2, DateUtil.toSqlTimestamp(incident.getIncidentDate()));
            ps.setString(3, incident.getSeverity());
            ps.setString(4, incident.getTitle());
            ps.setString(5, incident.getDescription());
            if (incident.getInvestigatorId() != null) {
                ps.setInt(6, incident.getInvestigatorId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, incident.getActionsTaken());
            ps.setString(8, incident.getStatus());
            ps.setInt(9, incident.getIncidentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating incident: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatusAndInvestigation(int incidentId, String status, Integer investigatorId, String actionsTaken) {
        String sql = "UPDATE incidents SET status = ?, investigator_id = ?, actions_taken = ? WHERE incident_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (investigatorId != null) {
                ps.setInt(2, investigatorId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, actionsTaken);
            ps.setInt(4, incidentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating incident status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int incidentId) {
        String sql = "DELETE FROM incidents WHERE incident_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, incidentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting incident: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Integer> countBySeverity() {
        String sql = "SELECT severity, COUNT(*) as cnt FROM incidents GROUP BY severity";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("severity"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting incidents by severity: " + e.getMessage(), e);
        }
        return map;
    }

    @Override
    public int countActiveIncidents() {
        String sql = "SELECT COUNT(*) FROM incidents WHERE status != 'Resolved'";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting active incidents: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM incidents";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting total incidents: " + e.getMessage(), e);
        }
        return 0;
    }
}
