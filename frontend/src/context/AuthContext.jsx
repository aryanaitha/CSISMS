import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../api/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [forcePasswordChange, setForcePasswordChange] = useState(false);

  const checkAuth = useCallback(async () => {
    try {
      const res = await api.get('/auth/me');
      if (res && res.success && res.data) {
        setUser(res.data);
        if (res.data.mustChangePassword) {
          setForcePasswordChange(true);
        }
      } else {
        setUser(null);
      }
    } catch (err) {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  const login = async (username, password) => {
    const res = await api.post('/auth/login', { username, password });
    if (res && res.success && res.data) {
      setUser(res.data);
      if (res.data.mustChangePassword) {
        setForcePasswordChange(true);
      } else {
        setForcePasswordChange(false);
      }
      return res.data;
    }
    throw new Error(res.message || 'Login failed');
  };

  const logout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (e) {
      // Ignore error on logout
    } finally {
      setUser(null);
      setForcePasswordChange(false);
    }
  };

  const changePassword = async (oldPassword, newPassword) => {
    const res = await api.post('/auth/change-password', { oldPassword, newPassword });
    if (res && res.success) {
      setForcePasswordChange(false);
      if (user) {
        setUser({ ...user, mustChangePassword: false });
      }
    }
    return res;
  };

  const hasPermission = (permissionCode) => {
    if (!user) return false;
    if (user.roleName === 'Administrator') return true;
    return user.permissions && user.permissions.includes(permissionCode);
  };

  const hasRole = (roleName) => {
    if (!user) return false;
    return user.roleName === roleName;
  };

  const value = {
    user,
    loading,
    forcePasswordChange,
    setForcePasswordChange,
    login,
    logout,
    changePassword,
    hasPermission,
    hasRole,
    checkAuth
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
