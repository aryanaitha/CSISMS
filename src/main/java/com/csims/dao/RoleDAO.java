package com.csims.dao;

import com.csims.model.Role;
import java.util.List;
import java.util.Optional;

public interface RoleDAO {
    Optional<Role> findById(int roleId);
    Optional<Role> findByName(String roleName);
    List<Role> findAll();
}
