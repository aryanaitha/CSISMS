package com.csims.dao.impl;

import com.csims.dao.AttendanceDAO;
import com.csims.model.Attendance;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttendanceDAOImpl implements AttendanceDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setAttendanceId(rs.getInt("attendance_id"));
        a.setWorkerId(rs.getInt("worker_id"));
        a.setProjectId(rs.getInt("project_id"));

        Date aDate = rs.getDate("attendance_date");
        if (aDate != null) a.setAttendanceDate(aDate.toLocalDate());

        a.setStatus(rs.getString("status"));
        a.setRemarks(rs.getString("remarks"));

        int recBy = rs.getInt("recorded_by");
        if (!rs.wasNull()) a.setRecordedBy(recBy);

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) a.setCreatedAt(cTs.toLocalDateTime());

        Timestamp uTs = rs.getTimestamp("updated_at");
        if (uTs != null) a.setUpdatedAt(uTs.toLocalDateTime());

        try {
            a.setWorkerName(rs.getString("worker_name"));
            a.setTrade(rs.getString("trade"));
        } catch (SQLException ignored) {}

        try {
            a.setProjectName(rs.getString("project_name"));
        } catch (SQLException ignored) {}

        try {
            a.setRecorderName(rs.getString("recorder_name"));
        } catch (SQLException ignored) {}

        return a;
    }

    @Override
    public Optional<Attendance> findById(int attendanceId) {
        String sql = "SELECT a.*, w.full_name as worker_name, w.trade, p.project_name, u.username as recorder_name " +
                     "FROM attendance a " +
                     "JOIN workers w ON a.worker_id = w.worker_id " +
                     "JOIN projects p ON a.project_id = p.project_id " +
                     "LEFT JOIN users u ON a.recorded_by = u.user_id " +
                     "WHERE a.attendance_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attendanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding attendance: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Attendance> findByWorkerProjectDate(int workerId, int projectId, LocalDate date) {
        String sql = "SELECT a.*, w.full_name as worker_name, w.trade, p.project_name, u.username as recorder_name " +
                     "FROM attendance a " +
                     "JOIN workers w ON a.worker_id = w.worker_id " +
                     "JOIN projects p ON a.project_id = p.project_id " +
                     "LEFT JOIN users u ON a.recorded_by = u.user_id " +
                     "WHERE a.worker_id = ? AND a.project_id = ? AND a.attendance_date = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            ps.setInt(2, projectId);
            ps.setDate(3, DateUtil.toSqlDate(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding attendance by key: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Attendance> search(Integer projectId, Integer workerId, LocalDate startDate, LocalDate endDate, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT a.*, w.full_name as worker_name, w.trade, p.project_name, u.username as recorder_name " +
            "FROM attendance a " +
            "JOIN workers w ON a.worker_id = w.worker_id " +
            "JOIN projects p ON a.project_id = p.project_id " +
            "LEFT JOIN users u ON a.recorded_by = u.user_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (projectId != null && projectId > 0) {
            sql.append("AND a.project_id = ? ");
            params.add(projectId);
        }

        if (workerId != null && workerId > 0) {
            sql.append("AND a.worker_id = ? ");
            params.add(workerId);
        }

        if (startDate != null) {
            sql.append("AND a.attendance_date >= ? ");
            params.add(DateUtil.toSqlDate(startDate));
        }

        if (endDate != null) {
            sql.append("AND a.attendance_date <= ? ");
            params.add(DateUtil.toSqlDate(endDate));
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND a.status = ? ");
            params.add(status);
        }

        sql.append("ORDER BY a.attendance_date DESC, w.full_name ASC");

        List<Attendance> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching attendance: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int record(Attendance attendance) {
        String sql = "INSERT INTO attendance (worker_id, project_id, attendance_date, status, remarks, recorded_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE status = VALUES(status), remarks = VALUES(remarks), recorded_by = VALUES(recorded_by)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, attendance.getWorkerId());
            ps.setInt(2, attendance.getProjectId());
            ps.setDate(3, DateUtil.toSqlDate(attendance.getAttendanceDate()));
            ps.setString(4, attendance.getStatus() != null ? attendance.getStatus() : "Present");
            ps.setString(5, attendance.getRemarks());
            if (attendance.getRecordedBy() != null) {
                ps.setInt(6, attendance.getRecordedBy());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    attendance.setAttendanceId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error recording attendance: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Attendance attendance) {
        String sql = "UPDATE attendance SET status = ?, remarks = ?, recorded_by = ? WHERE attendance_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, attendance.getStatus());
            ps.setString(2, attendance.getRemarks());
            if (attendance.getRecordedBy() != null) {
                ps.setInt(3, attendance.getRecordedBy());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, attendance.getAttendanceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating attendance: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int attendanceId) {
        String sql = "DELETE FROM attendance WHERE attendance_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attendanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting attendance: " + e.getMessage(), e);
        }
    }

    @Override
    public double calculateAttendanceRate(Integer projectId, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) as total, SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) as present_count " +
            "FROM attendance WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (projectId != null && projectId > 0) {
            sql.append("AND project_id = ? ");
            params.add(projectId);
        }
        if (startDate != null) {
            sql.append("AND attendance_date >= ? ");
            params.add(DateUtil.toSqlDate(startDate));
        }
        if (endDate != null) {
            sql.append("AND attendance_date <= ? ");
            params.add(DateUtil.toSqlDate(endDate));
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int present = rs.getInt("present_count");
                    if (total > 0) {
                        return (present * 100.0) / total;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating attendance rate: " + e.getMessage(), e);
        }
        return 0.0;
    }
}
