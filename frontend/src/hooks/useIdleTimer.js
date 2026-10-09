import { useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';

const IDLE_TIMEOUT_MS = 15 * 60 * 1000; // 15 minutes idle timeout

export function useIdleTimer(onIdle) {
  const { user, logout } = useAuth();
  const timerRef = useRef(null);

  const resetTimer = () => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
    }
    if (user) {
      timerRef.current = setTimeout(() => {
        if (onIdle) {
          onIdle();
        } else {
          alert('You have been logged out due to 15 minutes of inactivity.');
          logout();
        }
      }, IDLE_TIMEOUT_MS);
    }
  };

  useEffect(() => {
    if (!user) {
      if (timerRef.current) clearTimeout(timerRef.current);
      return;
    }

    const events = ['mousemove', 'mousedown', 'keydown', 'scroll', 'touchstart'];
    const handleActivity = () => resetTimer();

    events.forEach(e => window.addEventListener(e, handleActivity));
    resetTimer();

    return () => {
      events.forEach(e => window.removeEventListener(e, handleActivity));
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, [user]);

  return { resetTimer };
}

export default useIdleTimer;
