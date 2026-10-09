package com.csims.dao;

import com.csims.model.Permission;
import java.util.List;
import java.util.Set;

public interface PermissionDAO {
    List<Permission> findAll();
    List<Permission> findByRoleId(int roleId);
    Set<String> findPermissionCodesByRoleId(int roleId);
    boolean updateRolePermissions(int roleId, Set<Integer> permissionIds);
}
