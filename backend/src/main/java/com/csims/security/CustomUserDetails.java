package com.csims.security;

import com.csims.model.Models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CustomUserDetails implements UserDetails {
    private final User user;
    private final Set<String> permissionNames;
    private final Set<GrantedAuthority> authorities;

    public CustomUserDetails(User user, Set<String> permissionNames) {
        this.user = user;
        this.permissionNames = permissionNames != null ? permissionNames : new HashSet<>();
        
        Set<GrantedAuthority> auths = new HashSet<>();
        if (user.getRoleName() != null) {
            auths.add(new SimpleGrantedAuthority("ROLE_" + user.getRoleName()));
        }
        for (String perm : this.permissionNames) {
            auths.add(new SimpleGrantedAuthority(perm));
        }
        this.authorities = auths;
    }

    public User getUser() {
        return user;
    }

    public Long getId() {
        return user.getId();
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getFullName() {
        return user.getFullName();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getRoleName() {
        return user.getRoleName();
    }

    public Integer getRoleId() {
        return user.getRoleId();
    }

    public boolean isMustChangePassword() {
        return user.isMustChangePassword();
    }

    public Set<String> getPermissionNames() {
        return permissionNames;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (user.getLockedUntil() == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(user.getLockedUntil());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
