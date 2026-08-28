import React, { useState, useEffect } from 'react';
import { api } from './api/client';
import { AdminUser, AppSettings } from './types';
import { LoginView } from './components/auth/LoginView';
import { AdminLayout } from './components/layout/AdminLayout';
import { DashboardView } from './components/dashboard/DashboardView';
import { UsersView } from './components/users/UsersView';
import { QuizzesView } from './components/quizzes/QuizzesView';
import { RewardAdsSettingsView } from './components/rewards/RewardAdsSettingsView';
import { WithdrawalsView } from './components/withdrawals/WithdrawalsView';
import { AppControlsView } from './components/controls/AppControlsView';
import { ContentManagementView } from './components/content/ContentManagementView';
import { NotificationsView } from './components/notifications/NotificationsView';
import { AuditLogsView } from './components/audit/AuditLogsView';
import { KeyRound, X, AlertCircle, CheckCircle2, WifiOff, RefreshCw } from 'lucide-react';

export const App: React.FC = () => {
  const [admin, setAdmin] = useState<AdminUser | null>(null);
  const [settings, setSettings] = useState<AppSettings | null>(null);
  const [loading, setLoading] = useState(true);
  const [isOnline, setIsOnline] = useState<boolean>(navigator.onLine);
  const [currentTab, setCurrentTab] = useState('dashboard');

  // Change Password Modal
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
  const [currPassword, setCurrPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [passwordSuccess, setPasswordSuccess] = useState<string | null>(null);
  const [passwordSubmitting, setPasswordSubmitting] = useState(false);

  useEffect(() => {
    initApp();

    const handleUnauthorized = () => {
      setAdmin(null);
    };

    const handleOnline = () => {
      setIsOnline(true);
      initApp();
    };

    const handleOffline = () => {
      setIsOnline(false);
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    window.addEventListener('admin:unauthorized', handleUnauthorized);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      window.removeEventListener('admin:unauthorized', handleUnauthorized);
    };
  }, []);


  const initApp = async () => {
    try {
      const token = api.getToken();
      if (token) {
        try {
          const [meRes, settingsRes] = await Promise.all([
            api.getMe(),
            api.getSettings()
          ]);
          setAdmin(meRes.admin);
          setSettings(settingsRes.settings);
        } catch (authErr: any) {
          // If token was rejected/expired or could not be verified, clean up token
          api.setToken(null);
          setAdmin(null);
        }
      }
    } catch {
      api.setToken(null);
      setAdmin(null);
    } finally {
      setLoading(false);
    }
  };

  const handleLoginSuccess = async (loggedAdmin: AdminUser) => {
    setAdmin(loggedAdmin);
    try {
      const s = await api.getSettings();
      setSettings(s.settings);
    } catch (e) {
      console.error(e);
    }
  };

  const handleLogout = () => {
    api.setToken(null);
    setAdmin(null);
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setPasswordError(null);
    setPasswordSuccess(null);
    setPasswordSubmitting(true);

    try {
      await api.changePassword(currPassword, newPassword);
      setPasswordSuccess('Password updated successfully!');
      setCurrPassword('');
      setNewPassword('');
      setTimeout(() => {
        setIsPasswordModalOpen(false);
        setPasswordSuccess(null);
      }, 1500);
    } catch (err: any) {
      setPasswordError(err.message || 'Failed to update password');
    } finally {
      setPasswordSubmitting(false);
    }
  };

  if (!isOnline) {
    return (
      <div className="min-h-screen bg-slate-950 flex flex-col items-center justify-center p-6 text-center">
        <div className="w-16 h-16 rounded-2xl bg-red-500/10 border border-red-500/20 flex items-center justify-center text-red-400 mb-4 shadow-xl shadow-red-500/5">
          <WifiOff className="w-8 h-8" />
        </div>
        <h2 className="text-xl font-bold text-white mb-2">Internet Connection Required</h2>
        <p className="text-sm text-slate-400 max-w-sm mb-6">
          Ads Pay is an online-only platform. An active internet connection is required to communicate with the server and manage operations.
        </p>
        <button
          onClick={() => {
            if (navigator.onLine) {
              setIsOnline(true);
              initApp();
            }
          }}
          className="flex items-center gap-2 px-5 py-2.5 bg-purple-600 hover:bg-purple-500 text-white rounded-xl text-sm font-semibold transition-all shadow-lg shadow-purple-600/20 active:scale-95 cursor-pointer"
        >
          <RefreshCw className="w-4 h-4" />
          Retry Connection
        </button>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-purple-500/20 border-t-purple-500 rounded-full animate-spin"></div>
          <span className="text-xs text-slate-400 font-mono">Connecting to Ads Pay Admin Server...</span>
        </div>
      </div>
    );
  }


  if (!admin) {
    return <LoginView onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <AdminLayout
      currentTab={currentTab}
      onSelectTab={setCurrentTab}
      admin={admin}
      settings={settings}
      onLogout={handleLogout}
      onChangePasswordClick={() => {
        setIsPasswordModalOpen(true);
        setPasswordError(null);
        setPasswordSuccess(null);
      }}
    >
      {currentTab === 'dashboard' && <DashboardView onNavigate={setCurrentTab} />}
      {currentTab === 'users' && <UsersView />}
      {currentTab === 'quizzes' && <QuizzesView />}
      {currentTab === 'rewards' && (
        <RewardAdsSettingsView
          settings={settings}
          onSettingsUpdated={setSettings}
        />
      )}
      {currentTab === 'withdrawals' && <WithdrawalsView />}
      {currentTab === 'controls' && (
        <AppControlsView
          settings={settings}
          onSettingsUpdated={setSettings}
        />
      )}
      {currentTab === 'content' && (
        <ContentManagementView
          settings={settings}
          onSettingsUpdated={setSettings}
        />
      )}
      {currentTab === 'notifications' && <NotificationsView />}
      {currentTab === 'audit' && <AuditLogsView />}

      {/* Change Password Modal */}
      {isPasswordModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-bold text-white flex items-center gap-2">
                <KeyRound className="w-5 h-5 text-purple-400" />
                Change Admin Password
              </h3>
              <button onClick={() => setIsPasswordModalOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>

            {passwordError && (
              <div className="mb-4 p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-start gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{passwordError}</span>
              </div>
            )}

            {passwordSuccess && (
              <div className="mb-4 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 shrink-0" />
                <span>{passwordSuccess}</span>
              </div>
            )}

            <form onSubmit={handlePasswordSubmit} className="space-y-4 text-xs">
              <div>
                <label className="block font-bold text-slate-300 mb-1">
                  Current Password
                </label>
                <input
                  type="password"
                  required
                  value={currPassword}
                  onChange={(e) => setCurrPassword(e.target.value)}
                  placeholder="••••••••••••"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-white focus:outline-none focus:border-purple-500"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-300 mb-1">
                  New Password (min 6 characters)
                </label>
                <input
                  type="password"
                  required
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="••••••••••••"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-white focus:outline-none focus:border-purple-500"
                />
              </div>

              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setIsPasswordModalOpen(false)}
                  className="flex-1 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl font-bold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={passwordSubmitting}
                  className="flex-1 py-2.5 bg-purple-600 hover:bg-purple-500 text-white rounded-xl font-bold transition disabled:opacity-50"
                >
                  {passwordSubmitting ? 'Updating...' : 'Update Password'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AdminLayout>
  );
};
