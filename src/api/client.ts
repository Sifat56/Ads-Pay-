import { AppSettings, AdminUser, User, Quiz, WithdrawalRequest, AppNotification, AdminAuditLog, DashboardMetrics } from '../types';

const API_BASE = '/api/admin';

class ApiClient {
  private token: string | null = null;

  constructor() {
    this.token = localStorage.getItem('adspay_admin_token');
  }

  setToken(token: string | null) {
    this.token = token;
    if (token) {
      localStorage.setItem('adspay_admin_token', token);
    } else {
      localStorage.removeItem('adspay_admin_token');
    }
  }

  getToken() {
    return this.token;
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string> || {}),
    };

    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    });

    if (response.status === 401) {
      this.setToken(null);
      window.dispatchEvent(new Event('admin:unauthorized'));
      throw new Error('Session expired. Please log in again.');
    }

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.error || 'API request failed');
    }

    return data as T;
  }

  // Auth
  async checkAuthStatus() {
    return this.request<{ hasAdmin: boolean; count: number }>('/auth/status');
  }

  async setupInitialAdmin(payload: { name: string; email: string; password: string }) {
    const res = await this.request<{ token: string; admin: AdminUser }>('/auth/setup', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    this.setToken(res.token);
    return res;
  }

  async login(payload: { email: string; password: string }) {
    const res = await this.request<{ token: string; admin: AdminUser }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    this.setToken(res.token);
    return res;
  }

  async getMe() {
    return this.request<{ admin: AdminUser }>('/auth/me');
  }

  async changePassword(currentPassword: string, newPassword: string) {
    return this.request<{ message: string }>('/auth/change-password', {
      method: 'POST',
      body: JSON.stringify({ currentPassword, newPassword }),
    });
  }

  // Dashboard
  async getDashboard() {
    return this.request<{
      metrics: DashboardMetrics;
      weeklyTrend: Array<{ name: string; rewards: number; signups: number; payouts: number }>;
      methodStats: { BKASH: number; NAGAD: number; USDT_BEP20: number };
      recentWithdrawals: WithdrawalRequest[];
      recentTransactions: any[];
      recentUsers: User[];
    }>('/dashboard');
  }

  // Users
  async getUsers(params: { search?: string; status?: string; sort?: string } = {}) {
    const query = new URLSearchParams();
    if (params.search) query.set('search', params.search);
    if (params.status) query.set('status', params.status);
    if (params.sort) query.set('sort', params.sort);
    return this.request<{ total: number; users: User[] }>(`/users?${query.toString()}`);
  }

  async getUserDetails(id: string) {
    return this.request<{
      user: User;
      transactions: any[];
      withdrawals: WithdrawalRequest[];
      referredUsers: User[];
      taskAttempts: any[];
    }>(`/users/${id}`);
  }

  async updateUserStatus(id: string, updates: {
    isBlocked?: boolean;
    isTaskDisabled?: boolean;
    isWithdrawDisabled?: boolean;
    isReferralDisabled?: boolean;
    reason?: string;
  }) {
    return this.request<{ message: string; user: User }>(`/users/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify(updates),
    });
  }

  async adjustUserPoints(id: string, amount: number, reason: string) {
    return this.request<{ message: string; user: User }>(`/users/${id}/adjust-points`, {
      method: 'POST',
      body: JSON.stringify({ amount, reason }),
    });
  }

  // Quizzes
  async getQuizzes() {
    return this.request<{ quizzes: Quiz[] }>('/quizzes');
  }

  async createQuiz(payload: Omit<Quiz, 'id'>) {
    return this.request<{ message: string; quiz: Quiz }>('/quizzes', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  async updateQuiz(id: string, payload: Partial<Quiz>) {
    return this.request<{ message: string; quiz: Quiz }>(`/quizzes/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  }

  async toggleQuiz(id: string) {
    return this.request<{ message: string; quiz: Quiz }>(`/quizzes/${id}/toggle`, {
      method: 'PATCH',
    });
  }

  async deleteQuiz(id: string) {
    return this.request<{ message: string }>(`/quizzes/${id}`, {
      method: 'DELETE',
    });
  }

  // Settings
  async getSettings() {
    return this.request<{ settings: AppSettings }>('/settings');
  }

  async updateSettings(settings: Partial<AppSettings>, auditReason?: string) {
    return this.request<{ message: string; settings: AppSettings }>('/settings', {
      method: 'PUT',
      body: JSON.stringify({ ...settings, auditReason }),
    });
  }

  // Withdrawals
  async getWithdrawals(params: { status?: string; search?: string; method?: string } = {}) {
    const query = new URLSearchParams();
    if (params.status) query.set('status', params.status);
    if (params.search) query.set('search', params.search);
    if (params.method) query.set('method', params.method);
    return this.request<{ total: number; withdrawals: WithdrawalRequest[] }>(`/withdrawals?${query.toString()}`);
  }

  async updateWithdrawal(id: string, status: string, adminNote: string) {
    return this.request<{ message: string; withdrawal: WithdrawalRequest }>(`/withdrawals/${id}`, {
      method: 'PATCH',
      body: JSON.stringify({ status, adminNote }),
    });
  }

  // Notifications
  async getNotifications() {
    return this.request<{ notifications: AppNotification[] }>('/notifications');
  }

  async sendNotification(payload: { title: string; message: string; type?: string; targetUserId?: string }) {
    return this.request<{ message: string; notification: AppNotification }>('/notifications', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  // Audit Logs
  async getAuditLogs(params: { search?: string; action?: string } = {}) {
    const query = new URLSearchParams();
    if (params.search) query.set('search', params.search);
    if (params.action) query.set('action', params.action);
    return this.request<{ total: number; logs: AdminAuditLog[] }>(`/audit-logs?${query.toString()}`);
  }
}

export const api = new ApiClient();
