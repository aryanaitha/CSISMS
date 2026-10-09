import { useAuth } from '../context/AuthContext';

export function usePermission() {
  const { hasPermission, hasRole, user } = useAuth();
  return { hasPermission, hasRole, user };
}

export default usePermission;
