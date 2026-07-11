import { useState, useEffect } from 'react';

export function useAuth() {
  const [token, setToken] = useState<string | null>(
    localStorage.getItem('jwtToken') || localStorage.getItem('token')
  );
  const [role, setRole] = useState<string | null>(localStorage.getItem('userRole'));
  const [email, setEmail] = useState<string | null>(localStorage.getItem('userEmail'));
  const [userId, setUserId] = useState<string | null>(localStorage.getItem('userId'));
  const [theme, setTheme] = useState<string>(localStorage.getItem('sc_theme') || 'light');

  // Handle setting/removing tokens programmatically
  const saveSession = (jwtToken: string, userRole: string, userEmail: string, userUserId: string) => {
    localStorage.setItem('jwtToken', jwtToken);
    localStorage.setItem('token', jwtToken);
    localStorage.setItem('userRole', userRole);
    localStorage.setItem('userEmail', userEmail);
    localStorage.setItem('userId', userUserId);
    setToken(jwtToken);
    setRole(userRole);
    setEmail(userEmail);
    setUserId(userUserId);
  };

  const clearSession = () => {
    localStorage.clear();
    setToken(null);
    setRole(null);
    setEmail(null);
    setUserId(null);
  };

  // Sync theme configurations
  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.setAttribute('data-theme', 'dark');
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
    localStorage.setItem('sc_theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'));
  };

  const isAuthenticated = !!token;

  return {
    token,
    role,
    email,
    userId,
    theme,
    setTheme,
    toggleTheme,
    saveSession,
    clearSession,
    isAuthenticated,
  };
}
