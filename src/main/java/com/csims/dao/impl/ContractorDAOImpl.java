package com.csims.dao.impl;

import com.csims.dao.ContractorDAO;
import com.csims.model.Contractor;
import com.csims.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContractorDAOImpl implements ContractorDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private Contractor mapRow(ResultSet rs) throws SQLException {
        Contractor c = new Contractor();
        c.setContractorId(rs.getInt("contractor_id"));
        c.setCompanyName(rs.getString("company_name"));
        c.setContactPerson(rs.getString("contact_person"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            c.setCreatedAt(ts.toLocalDateTime());
        }
        return c;
    }

    @Override
    public Optional<Contractor> findById(int contractorId) {
        String sql = "SELECT * FROM contractors WHERE contractor_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contractorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding contractor: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Contractor> findAll() {
        return search(null);
    }

    @Override
    public List<Contractor> search(String keyword) {
        StringBuilder sql = new StringBuilder("SELECT * FROM contractors WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(company_name) LIKE ? OR LOWER(contact_person) LIKE ? OR LOWER(email) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        sql.append("ORDER BY company_name ASC");

        List<Contractor> list = new ArrayList<>();
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
            throw new RuntimeException("Error listing contractors: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int create(Contractor contractor) {
        String sql = "INSERT INTO contractors (company_name, contact_person, email, phone, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, contractor.getCompanyName());
            ps.setString(2, contractor.getContactPerson());
            ps.setString(3, contractor.getEmail());
            ps.setString(4, contractor.getPhone());
            ps.setString(5, contractor.getAddress());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    contractor.setContractorId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating contractor: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Contractor contractor) {
        String sql = "UPDATE contractors SET company_name = ?, contact_person = ?, email = ?, phone = ?, address = ? WHERE contractor_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contractor.getCompanyName());
            ps.setString(2, contractor.getContactPerson());
            ps.setString(3, contractor.getEmail());
            ps.setString(4, contractor.getPhone());
            ps.setString(5, contractor.getAddress());
            ps.setInt(6, contractor.getContractorId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating contractor: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int contractorId) {
        String sql = "DELETE FROM contractors WHERE contractor_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contractorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting contractor: " + e.getMessage(), e);
        }
    }
}
