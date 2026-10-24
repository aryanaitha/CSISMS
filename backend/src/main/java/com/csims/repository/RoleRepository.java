package com.csims.repository;

import com.csims.model.Permission;
import com.csims.model.Role;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class RoleRepository {
    private final UserRepository userRepository;

    public RoleRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<Role> findAll() {
        return userRepository.findAllRoles();
    }

    public Optional<Role> findById(Integer id) {
        return userRepository.findRoleById(id);
    }

    public List<Permission> findAllPermissions() {
        return userRepository.findAllPermissions();
    }

    public Set<String> findPermissionNamesByRoleId(Integer roleId) {
        return userRepository.findPermissionNamesByRoleId(roleId);
    }

    public List<Permission> findPermissionsByRoleId(Integer roleId) {
        return userRepository.findPermissionsByRoleId(roleId);
    }

    public void updateRolePermissions(Integer roleId, List<Integer> permissionIds) {
        userRepository.updateRolePermissions(roleId, permissionIds);
    }
}
