import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/api';
import Modal from '../components/Modal';
import FormField from '../components/FormField';

const REMEMBER_KEY = 'csims_remembered_username';

export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, user } = useAuth();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Forgot Password modal state
  const [forgotModalOpen, setForgotModalOpen] = useState(false);
  const [forgotInput, setForgotInput] = useState('');
  const [forgotMsg, setForgotMsg] = useState(null);
  const [forgotLoading, setForgotLoading] = useState(false);

  const usernameRef = useRef(null);
  const passwordRef = useRef(null);

  useEffect(() => {
    // If already authenticated and doesn't need password change, navigate to dashboard
    if (user && !user.mustChangePassword) {
      const destination = location.state?.from?.pathname || '/dashboard';
      navigate(destination, { replace: true });
    }
  }, [user, navigate, location]);

  useEffect(() => {
    // Check remembered username
    const savedUsername = localStorage.getItem(REMEMBER_KEY);
    if (savedUsername) {
      setUsername(savedUsername);
      setRememberMe(true);
      if (passwordRef.current) passwordRef.current.focus();
    } else {
      if (usernameRef.current) usernameRef.current.focus();
    }
  }, []);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (rememberMe) {
        localStorage.setItem(REMEMBER_KEY, username);
      } else {
        localStorage.removeItem(REMEMBER_KEY);
      }

      const userData = await login(username, password);

      if (!userData.mustChangePassword) {
        const destination = location.state?.from?.pathname || '/dashboard';
        navigate(destination, { replace: true });
      }
    } catch (err) {
      setError(err.message || 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    setForgotLoading(true);
    setForgotMsg(null);

    try {
      const res = await api.post('/auth/forgot-password', { usernameOrEmail: forgotInput });
      setForgotMsg({
        type: 'success',
        text: res.message || 'If an account with that username or email exists, a reset request has been submitted to administrators.'
      });
      setForgotInput('');
    } catch (err) {
      setForgotMsg({
        type: 'error',
        text: err.message || 'Error submitting password reset request'
      });
    } finally {
      setForgotLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h2 className="login-title">Sign In to CSIMS</h2>
        <p className="login-subtitle">
          Construction Site Intelligence & Safety Management System
        </p>

        {error && (
          <div className="alert alert-danger" style={{ marginBottom: '1.25rem' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label className="form-label" htmlFor="username">
              Username or Email
            </label>
            <input
              id="username"
              ref={usernameRef}
              type="text"
              className="form-input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter your username or email"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">
              Password
            </label>
            <input
              id="password"
              ref={passwordRef}
              type={showPassword ? 'text' : 'password'}
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.85rem', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={showPassword}
                onChange={(e) => setShowPassword(e.target.checked)}
              />
              Show password
            </label>

            <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.85rem', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
              />
              Remember username
            </label>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
              style={{ width: '100%', justifyContent: 'center' }}
            >
              {loading ? 'Authenticating...' : 'Login'}
            </button>

            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => navigate('/')}
              style={{ width: '100%', justifyContent: 'center' }}
            >
              Back to Home
            </button>
          </div>

          <div style={{ marginTop: '1.25rem', textAlign: 'center' }}>
            <button
              type="button"
              onClick={() => {
                setForgotModalOpen(true);
                setForgotMsg(null);
              }}
              style={{
                background: 'none',
                border: 'none',
                color: 'var(--color-primary)',
                cursor: 'pointer',
                fontSize: '0.85rem',
                textDecoration: 'underline'
              }}
            >
              Forgot password?
            </button>
          </div>
        </form>
      </div>

      {/* Forgot Password Modal */}
      <Modal
        isOpen={forgotModalOpen}
        onClose={() => {
          setForgotModalOpen(false);
          setForgotMsg(null);
        }}
        title="Reset Password Request"
        maxWidth="440px"
      >
        <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>
          Enter your username or registered email address. An administrator will review your reset request and provide a temporary password.
        </p>

        {forgotMsg && (
          <div className={`alert alert-${forgotMsg.type === 'success' ? 'success' : 'danger'}`}>
            {forgotMsg.text}
          </div>
        )}

        <form onSubmit={handleForgotPassword}>
          <FormField
            label="Username or Email"
            name="forgotInput"
            value={forgotInput}
            onChange={(e) => setForgotInput(e.target.value)}
            required
            placeholder="e.g. jdoe or jdoe@csims.local"
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => {
                setForgotModalOpen(false);
                setForgotMsg(null);
              }}
            >
              Close
            </button>
            <button type="submit" className="btn btn-primary" disabled={forgotLoading}>
              {forgotLoading ? 'Submitting...' : 'Request Reset'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
