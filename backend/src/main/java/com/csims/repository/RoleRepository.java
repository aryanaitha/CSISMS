package com.csims.repository;

import com.csims.model.Permission;
import com.csims.model.Role;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class RoleRepository {
    private final JdbcTemplate jdbcTemplate;

    public RoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Role> roleRowMapper = (rs, rowNum) -> 
        new Role(rs.getInt("role_id"), rs.getString("role_name"), rs.getString("description"));

    private final RowMapper<Permission> permissionRowMapper = (rs, rowNum) ->
        new Permission(rs.getInt("permission_id"), rs.getString("permission_code"), rs.getString("module"), rs.getString("description"));

    public List<Role> findAll() {
        return jdbcTemplate.query("SELECT * FROM roles ORDER BY role_id ASC", roleRowMapper);
    }

    public Optional<Role> findById(Integer id) {
        try {
            Role role = jdbcTemplate.queryForObject("SELECT * FROM roles WHERE role_id = ?", roleRowMapper, id);
            return Optional.ofNullable(role);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Permission> findAllPermissions() {
        return jdbcTemplate.query("SELECT * FROM permissions ORDER BY module ASC, permission_code ASC", permissionRowMapper);
    }

    public Set<String> findPermissionNamesByRoleId(Integer roleId) {
        String sql = """
            SELECT p.permission_code 
            FROM role_permissions rp 
            JOIN permissions p ON rp.permission_id = p.permission_id 
            WHERE rp.role_id = ?
        """;
        List<String> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("permission_code"), roleId);
        return new HashSet<>(list);
    }

    public List<Permission> findPermissionsByRoleId(Integer roleId) {
        String sql = """
            SELECT p.* 
            FROM role_permissions rp 
            JOIN permissions p ON rp.permission_id = p.permission_id 
            WHERE rp.role_id = ?
            ORDER BY p.module ASC, p.permission_code ASC
        """;
        return jdbcTemplate.query(sql, permissionRowMapper, roleId);
    }

    public void updateRolePermissions(Integer roleId, List<Integer> permissionIds) {
        jdbcTemplate.update("DELETE FROM role_permissions WHERE role_id = ?", roleId);
        String sql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";
        for (Integer permId : permissionIds) {
            jdbcTemplate.update(sql, roleId, permId);
        }
    }
}
