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
  const [dbConnected, setDbConnected] = useState(null);

  // Forgot Password modal state
  const [forgotModalOpen, setForgotModalOpen] = useState(false);
  const [forgotInput, setForgotInput] = useState('');
  const [forgotMsg, setForgotMsg] = useState(null);
  const [forgotLoading, setForgotLoading] = useState(false);

  const usernameRef = useRef(null);
  const passwordRef = useRef(null);

  useEffect(() => {
    api.get('/health')
      .then((res) => {
        if (res && res.success && res.data && res.data.database === 'CONNECTED') {
          setDbConnected(true);
        } else {
          setDbConnected(false);
        }
      })
      .catch(() => setDbConnected(false));
  }, []);

  useEffect(() => {
    if (user && !user.mustChangePassword) {
      const destination = location.state?.from?.pathname || '/dashboard';
      navigate(destination, { replace: true });
    }
  }, [user, navigate, location]);

  useEffect(() => {
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
        text: res.message || 'If an account with that username or email exists, a reset request has been submitted.'
      });
      setForgotInput('');
    } catch (err) {
      setForgotMsg({
        type: 'danger',
        text: err.message || 'Error submitting password reset request'
      });
    } finally {
      setForgotLoading(false);
    }
  };

  return (
    <div className="centered-auth-container">
      <div className="centered-card">
        <h1 className="auth-title">Sign In</h1>
        <p className="auth-tagline">
          Construction Site Intelligence & Safety Management System
        </p>

        {/* Small connected / not connected text */}
        <div style={{ marginBottom: '16px' }}>
          {dbConnected === null ? (
            <span style={{ fontSize: '12px', color: 'var(--muted)' }}>Checking database...</span>
          ) : dbConnected ? (
            <span style={{ fontSize: '12px', color: '#1b7a42' }}>Database: Connected</span>
          ) : (
            <span style={{ fontSize: '12px', color: '#9c2828' }}>Database: Not connected</span>
          )}
        </div>

        {error && (
          <div className="alert alert-danger">
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} style={{ textAlign: 'left' }}>
          <div className="form-group">
            <label className="form-label" htmlFor="username">Username or Email</label>
            <input
              id="username"
              ref={usernameRef}
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Username or email"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">Password</label>
            <input
              id="password"
              ref={passwordRef}
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Password"
              required
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', fontSize: '12px', color: 'var(--muted)' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer', margin: 0 }}>
              <input
                type="checkbox"
                checked={showPassword}
                onChange={(e) => setShowPassword(e.target.checked)}
              />
              Show password
            </label>

            <label style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer', margin: 0 }}>
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
              />
              Remember me
            </label>
          </div>

          <div className="auth-actions" style={{ flexDirection: 'column', gap: '8px', marginTop: '12px' }}>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading || dbConnected === false}
              style={{ width: '100%' }}
            >
              {loading ? 'Authenticating...' : 'Sign In'}
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => navigate('/')}
              style={{ width: '100%' }}
            >
              Back
            </button>
          </div>

          <div style={{ marginTop: '16px', textAlign: 'center' }}>
            <button
              type="button"
              onClick={() => {
                setForgotModalOpen(true);
                setForgotMsg(null);
              }}
              style={{
                background: 'none',
                border: 'none',
                color: 'var(--accent)',
                cursor: 'pointer',
                fontSize: '12px',
                padding: 0
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
        <p style={{ fontSize: '13px', color: 'var(--muted)', marginBottom: '14px' }}>
          Enter your username or email address. An administrator will review your request.
        </p>

        {forgotMsg && (
          <div className={`alert alert-${forgotMsg.type}`}>
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
            placeholder="e.g. admin or admin@csims.local"
          />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '16px' }}>
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
