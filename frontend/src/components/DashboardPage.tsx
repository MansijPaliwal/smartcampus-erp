import React, { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import apiClient from '../services/apiClient';
import { 
  AdminDashboardStatsResponse, 
  EnrollmentResponse, 
  FeePaymentResponse,
  NotificationDto 
} from '../types/api';
import '../design-system.css';

interface Message {
  id: number;
  sender: 'system' | 'user';
  text: string;
}

export const DashboardPage: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Dashboard state variables
  const [totalStudents, setTotalStudents] = useState<number | null>(null);
  const [totalPendingFees, setTotalPendingFees] = useState<number>(0.0);
  const [recentAlerts, setRecentAlerts] = useState<NotificationDto[]>([]);
  const [enrollments, setEnrollments] = useState<EnrollmentResponse[]>([]);
  const [payments, setPayments] = useState<FeePaymentResponse[]>([]);
  
  // Chatbot floating widget state
  const [chatOpen, setChatOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 1,
      sender: 'system',
      text: 'Hi! I am your SmartCampus assistant. How can I help you navigate user management, billing reports, or administrative settings today?'
    }
  ]);
  const [chatInput, setChatInput] = useState('');

  const { email, theme, toggleTheme, clearSession, isAuthenticated } = useAuth();

  const adminEmail = email || 'Admin';
  const avatarInitials = adminEmail.substring(0, 2).toUpperCase();

  useEffect(() => {
    if (!isAuthenticated) {
      window.location.href = '/login';
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (isAuthenticated) {
      loadDashboardData();
    }
  }, [isAuthenticated]);

  const loadDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      await Promise.all([
        fetchDashboardStats(),
        fetchRecentEnrollments(),
        fetchRecentPayments()
      ]);
    } catch (err: any) {
      setError('Failed to fetch dashboard data. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const fetchDashboardStats = async () => {
    const res = await apiClient.get<AdminDashboardStatsResponse>('/api/admin/dashboard/stats');
    setTotalStudents(res.data.totalStudents);
    setTotalPendingFees(res.data.totalPendingFees);
    setRecentAlerts(res.data.recentAlerts || []);
  };

  const fetchRecentEnrollments = async () => {
    const res = await apiClient.get<EnrollmentResponse[]>('/api/admin/enrollments');
    setEnrollments(res.data);
  };

  const fetchRecentPayments = async () => {
    const res = await apiClient.get<FeePaymentResponse[]>('/api/fees');
    setPayments(res.data);
  };

  const handleLogout = async (e: React.MouseEvent) => {
    e.preventDefault();
    try {
      await apiClient.post('/api/auth/logout');
    } catch (err) {
      // Suppress logging
    }
    clearSession();
    window.location.href = '/login';
  };

  const handleSendMessage = async () => {
    if (!chatInput.trim()) return;
    const userMsg: Message = { id: Date.now(), sender: 'user', text: chatInput };
    setMessages(prev => [...prev, userMsg]);
    const currentInput = chatInput;
    setChatInput('');

    try {
      const res = await apiClient.post<{ reply?: string; response?: string }>('/api/advisor/chat', {
        message: currentInput
      });
      const botMsg: Message = { id: Date.now() + 1, sender: 'system', text: res.data.reply || res.data.response || "I couldn't process that query." };
      setMessages(prev => [...prev, botMsg]);
    } catch (err) {
      const errMsg: Message = { id: Date.now() + 1, sender: 'system', text: "Error communicating with advisor." };
      setMessages(prev => [...prev, errMsg]);
    }
  };

  return (
    <div className="app-container">
      {/* SIDEBAR */}
      <aside className={`sidebar ${collapsed ? 'collapsed' : ''}`} id="app-sidebar">
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <span className="smart">Smart</span>
            <span className="campus">Campus</span>
            {!collapsed && <span className="erp">ERP</span>}
          </div>
        </div>
        <ul className="sidebar-menu">
          <li className="sidebar-item">
            <a href="/dashboard" className="sidebar-link active">
              <span className="material-symbols-outlined">dashboard</span>
              {!collapsed && <span>Dashboard</span>}
            </a>
          </li>
          <li className="sidebar-item">
            <a href="/admin/users" className="sidebar-link">
              <span className="material-symbols-outlined">group</span>
              {!collapsed && <span>User Management</span>}
            </a>
          </li>
          <li className="sidebar-item">
            <a href="/admin/settings" className="sidebar-link">
              <span className="material-symbols-outlined">settings</span>
              {!collapsed && <span>Settings</span>}
            </a>
          </li>
          <li className="sidebar-item" style={{ marginTop: 'auto', paddingTop: 'var(--space-4)' }}>
            <a href="#" className="sidebar-link" onClick={handleLogout} style={{ color: 'var(--color-danger)' }}>
              <span className="material-symbols-outlined">logout</span>
              {!collapsed && <span>Logout</span>}
            </a>
          </li>
        </ul>
      </aside>

      {/* MAIN BODY */}
      <div className="main-content">
        {/* TOPBAR */}
        <header className="topbar">
          <div className="topbar-left">
            <button className="menu-toggle" id="btn-toggle-menu" onClick={() => setCollapsed(!collapsed)}>
              <span className="material-symbols-outlined">menu</span>
            </button>
            <span className="topbar-title">Dashboard</span>
          </div>
          <div className="topbar-right">
            <button className="theme-toggle-btn" id="theme-toggle" onClick={toggleTheme}>
              <span className="material-symbols-outlined">{theme === 'dark' ? 'light_mode' : 'dark_mode'}</span>
            </button>
            <div className="user-avatar" id="header-avatar" onClick={() => window.location.href = '/admin/settings'}>
              {avatarInitials}
            </div>
          </div>
        </header>

        {/* CONTENT BODY */}
        <main className="content-viewport" id="main-viewport" style={{ opacity: 1 }}>
          <div className="content-header">
            <div>
              <h1 className="text-2xl">Dashboard</h1>
              <p className="text-sm text-secondary">Academic & Operations Ledger</p>
            </div>
          </div>

          {error && (
            <div className="error-alert mb-6 p-4" style={{ display: 'block', margin: 'var(--space-4) 0' }}>
              {error}
            </div>
          )}

          {loading ? (
            <div style={{ display: 'flex', justifyContent: 'center', padding: '100px 0' }}>
              <div className="spinner"></div>
            </div>
          ) : (
            <>
              {/* 3-COLUMN DATA WIDGET LAYOUT */}
              <div className="dashboard-stats" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', marginBottom: 'var(--space-8)' }}>
                <div className="stat-card">
                  <div className="stat-header">
                    <span className="stat-title">Total Students Enrolled</span>
                    <span className="material-symbols-outlined stat-icon">school</span>
                  </div>
                  <div className="stat-value">{totalStudents !== null ? totalStudents : '—'}</div>
                  <div className="stat-trend">
                    <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>check_circle</span>
                    <span>Active enrollments</span>
                  </div>
                </div>

                <div className="stat-card alert-card">
                  <div className="stat-header">
                    <span className="stat-title">Total Pending Fees</span>
                    <span className="material-symbols-outlined stat-icon">payments</span>
                  </div>
                  <div className="stat-value">₹{totalPendingFees.toFixed(2)}</div>
                  <div className="stat-trend">
                    <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>error</span>
                    <span>Awaiting billing clearance</span>
                  </div>
                </div>

                <div className="stat-card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'flex-start', gap: 'var(--space-2)' }}>
                  <div className="stat-header" style={{ marginBottom: 0 }}>
                    <span className="stat-title">Recent System Alerts</span>
                    <span className="material-symbols-outlined stat-icon" style={{ color: 'var(--color-warning)' }}>notifications</span>
                  </div>
                  <ul id="widgetAlertsList" style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 'var(--space-2)', fontSize: 'var(--text-xs)', color: 'var(--color-text-secondary)', textAlign: 'left' }}>
                    {recentAlerts.length > 0 ? (
                      recentAlerts.map(alert => {
                        const timeStr = new Date(alert.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                        return (
                          <li key={alert.id} style={{ display: 'flex', alignItems: 'flex-start', gap: 'var(--space-2)', borderBottom: '1px solid var(--color-border)', paddingBottom: 'var(--space-1)' }}>
                            <span className="material-symbols-outlined" style={{ fontSize: '14px', color: 'var(--color-brand)', marginTop: '2px' }}>info</span>
                            <div style={{ flex: 1 }}>
                              <strong style={{ color: 'var(--color-text-primary)', fontSize: 'var(--text-xs)' }}>{alert.title}</strong>
                              <div style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>{alert.message}</div>
                              <span style={{ fontSize: '9px', color: 'var(--color-text-disabled)', display: 'block', marginTop: '1px' }}>{timeStr}</span>
                            </div>
                          </li>
                        );
                      })
                    ) : (
                      <li>No recent system alerts.</li>
                    )}
                  </ul>
                </div>
              </div>

              {/* GRID DETAIL TABLES */}
              <div className="dashboard-grid">
                {/* RECENT ENROLLMENTS */}
                <div className="panel-card">
                  <div className="panel-header">
                    <span className="panel-title">Recent Course Enrollments</span>
                    <span className="material-symbols-outlined text-secondary" style={{ fontSize: '20px' }}>assignment_ind</span>
                  </div>
                  <div className="table-container" style={{ maxHeight: '380px', overflowY: 'auto' }}>
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>Student</th>
                          <th>Course</th>
                          <th>Enroll Date</th>
                          <th>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {enrollments.length > 0 ? (
                          enrollments.slice(-8).reverse().map(e => {
                            const initials = e.studentName ? e.studentName.substring(0, 2).toUpperCase() : 'ST';
                            const colors = ['#E2F1E8', '#E3F2FD', '#FCE4EC', '#F3E5F5', '#E8EAF6', '#E0F2F1'];
                            const textColors = ['#137333', '#1A73E8', '#C2185B', '#7B1FA2', '#303F9F', '#00796B'];
                            const hash = initials.charCodeAt(0) + (initials.length > 1 ? initials.charCodeAt(1) : 0);
                            const colorIdx = hash % colors.length;
                            const bg = colors[colorIdx];
                            const fg = textColors[colorIdx];
                            return (
                              <tr key={e.id}>
                                <td>
                                  <div className="avatar-cell">
                                    <div className="name-avatar" style={{ backgroundColor: bg, color: fg, fontWeight: 600 }}>{initials}</div>
                                    <div>
                                      <div style={{ fontWeight: 600, color: 'var(--color-text-primary)' }}>{e.studentName}</div>
                                    </div>
                                  </div>
                                </td>
                                <td>{e.courseCode} — {e.courseTitle}</td>
                                <td>{new Date(e.enrollmentDate).toLocaleDateString()}</td>
                                <td>
                                  <span className="badge badge-success">ACTIVE</span>
                                </td>
                              </tr>
                            );
                          })
                        ) : (
                          <tr>
                            <td colSpan={4} className="text-center text-secondary py-8">No active enrollment stream.</td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>

                {/* TRANSACTIONS LEDGER */}
                <div className="panel-card">
                  <div className="panel-header">
                    <span className="panel-title">System Transaction Ledger</span>
                    <span className="material-symbols-outlined text-secondary" style={{ fontSize: '20px' }}>receipt_long</span>
                  </div>
                  <div className="table-container" style={{ maxHeight: '380px', overflowY: 'auto' }}>
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>Student</th>
                          <th>Amount</th>
                          <th>Method</th>
                          <th>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {payments.length > 0 ? (
                          payments.slice(-8).reverse().map(p => {
                            const initials = p.studentName ? p.studentName.substring(0, 2).toUpperCase() : 'ST';
                            const colors = ['#E2F1E8', '#E3F2FD', '#FCE4EC', '#F3E5F5', '#E8EAF6', '#E0F2F1'];
                            const textColors = ['#137333', '#1A73E8', '#C2185B', '#7B1FA2', '#303F9F', '#00796B'];
                            const hash = initials.charCodeAt(0) + (initials.length > 1 ? initials.charCodeAt(1) : 0);
                            const colorIdx = hash % colors.length;
                            const bg = colors[colorIdx];
                            const fg = textColors[colorIdx];
                            const badgeClass = p.status === 'PAID' ? 'badge-success' : p.status === 'PENDING' ? 'badge-warning' : 'badge-danger';
                            return (
                              <tr key={p.id}>
                                <td>
                                  <div className="avatar-cell">
                                    <div className="name-avatar" style={{ backgroundColor: bg, color: fg, fontWeight: 600 }}>{initials}</div>
                                    <div>
                                      <div style={{ fontWeight: 600, color: 'var(--color-text-primary)' }}>{p.studentName}</div>
                                      <div style={{ fontSize: 'var(--text-xs)', color: 'var(--color-text-secondary)' }}>Student ID: {p.studentId}</div>
                                    </div>
                                  </div>
                                </td>
                                <td style={{ fontWeight: 600, color: 'var(--color-text-primary)' }}>₹{p.amount.toFixed(2)}</td>
                                <td>{p.paymentMethod || 'N/A'}</td>
                                <td>
                                  <span className={`badge ${badgeClass}`}>{p.status}</span>
                                </td>
                              </tr>
                            );
                          })
                        ) : (
                          <tr>
                            <td colSpan={4} className="text-center text-secondary py-8">No recent transactions ledger.</td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </>
          )}
        </main>
      </div>

      {/* Floating Support Chatbot Widget */}
      <div id="support-chatbot-widget" className="chatbot-widget">
        <button id="chatbot-toggle-btn" className="chatbot-toggle-btn" onClick={() => setChatOpen(!chatOpen)} title="Ask Assistant">
          <span className="material-symbols-outlined">{chatOpen ? 'close' : 'chat'}</span>
        </button>
        {chatOpen && (
          <div id="chatbot-container" className="chatbot-container">
            <div className="chatbot-header">
              <div className="chatbot-header-title">
                <span className="material-symbols-outlined">smart_toy</span>
                <span>Campus Assistant</span>
              </div>
              <button id="chatbot-close-btn" className="chatbot-close-btn" onClick={() => setChatOpen(false)}>
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
            <div id="chatbot-messages" className="chatbot-messages" style={{ overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
              {messages.map(msg => (
                <div key={msg.id} className={`message ${msg.sender}-message`}>
                  {msg.text}
                </div>
              ))}
            </div>
            <div className="chatbot-input-container">
              <input
                type="text"
                id="chatbot-input"
                placeholder="Type a message..."
                value={chatInput}
                onChange={e => setChatInput(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleSendMessage()}
                autoComplete="off"
              />
              <button id="chatbot-send-btn" onClick={handleSendMessage}>
                <span className="material-symbols-outlined">send</span>
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
