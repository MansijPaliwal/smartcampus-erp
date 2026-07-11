import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  
  const { theme, toggleTheme, saveSession } = useAuth();

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email, password })
      });

      if (!response.ok) {
        const errData = await response.json().catch(() => ({ message: 'Invalid credentials' }));
        throw new Error(errData.message || 'Authentication failed. Please verify credentials.');
      }

      const data = await response.json();
      
      // Save JWT & user details
      saveSession(data.token, data.role, data.email, String(data.userId));

      // Redirect based on role
      if (data.role === 'ADMIN') {
        window.location.href = '/dashboard';
      } else if (data.role === 'STUDENT') {
        window.location.href = '/student-dashboard';
      } else if (data.role === 'FACULTY') {
        window.location.href = '/faculty-dashboard';
      } else {
        window.location.href = '/';
      }

    } catch (err: any) {
      setErrorMsg(err.message || 'Authentication failed.');
      setLoading(false);
    }
  };

  return (
    <>
      <div className="theme-toggle-container">
        <button 
          className="theme-toggle-btn" 
          id="theme-toggle" 
          title="Toggle Light/Dark Mode"
          onClick={toggleTheme}
        >
          <span className="material-symbols-outlined">
            {theme === 'dark' ? 'light_mode' : 'dark_mode'}
          </span>
        </button>
      </div>

      <div className="login-card">
        <div className="wordmark-container">
          <div className="wordmark-title">
            <span className="smart">Smart</span><span className="campus">Campus</span>
            <span className="erp">ERP</span>
          </div>
          <div className="wordmark-subtitle">Administration Portal</div>
        </div>

        {errorMsg && (
          <div className="error-alert" id="error-box" style={{ display: 'block' }}>
            {errorMsg}
          </div>
        )}

        <form id="login-form" onSubmit={handleLoginSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email address</label>
            <input 
              type="email" 
              id="email" 
              className="form-control" 
              placeholder="name@smartcampus.edu" 
              required 
              autoComplete="username"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <div className="password-wrapper">
              <input 
                type={showPassword ? 'text' : 'password'} 
                id="password" 
                className="form-control" 
                placeholder="••••••••" 
                required 
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <button 
                type="button" 
                className="password-toggle" 
                id="password-toggle" 
                title="Show/Hide Password"
                onClick={() => setShowPassword(prev => !prev)}
              >
                <span className="material-symbols-outlined" id="eye-icon">
                  {showPassword ? 'visibility_off' : 'visibility'}
                </span>
              </button>
            </div>
          </div>

          <div className="form-options">
            <label className="remember-me">
              <input 
                type="checkbox" 
                id="remember" 
                checked={remember}
                onChange={(e) => setRemember(e.target.checked)}
              />
              <span>Remember me</span>
            </label>
            <a href="/forgot-password" className="forgot-link">Forgot password?</a>
          </div>

          <button 
            type="submit" 
            className="btn btn-primary" 
            style={{ width: '100%', height: '44px' }} 
            id="btn-submit"
            disabled={loading}
          >
            {loading ? (
              <div className="spinner"></div>
            ) : (
              <span>Sign in</span>
            )}
          </button>
        </form>
      </div>

      <div className="footer-text">
        &copy; 2026 SmartCampus ERP System
      </div>
    </>
  );
};
