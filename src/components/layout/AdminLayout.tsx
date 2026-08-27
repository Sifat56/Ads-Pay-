import React, { useState } from 'react';
import { 
  LayoutDashboard, 
  Users, 
  HelpCircle, 
  Coins, 
  Wallet, 
  SlidersHorizontal, 
  FileText, 
  Bell, 
  ShieldAlert, 
  LogOut, 
  Menu, 
  X, 
  AlertTriangle,
  Radio,
  ExternalLink,
  Lock,
  KeyRound
} from 'lucide-react';
import { AdminUser, AppSettings } from '../../types';

interface AdminLayoutProps {
  currentTab: string;
  onSelectTab: (tab: string) => void;
  admin: AdminUser | null;
  settings: AppSettings | null;
  onLogout: () => void;
  children: React.ReactNode;
  onChangePasswordClick: () => void;
}

export const AdminLayout: React.FC<AdminLayoutProps> = ({
  currentTab,
  onSelectTab,
  admin,
  settings,
  onLogout,
  children,
  onChangePasswordClick
}) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [profileDropdownOpen, setProfileDropdownOpen] = useState(false);

  const navigationItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard, badge: null },
    { id: 'users', label: 'User Management', icon: Users, badge: null },
    { id: 'quizzes', label: 'Tasks & Quizzes', icon: HelpCircle, badge: `${settings?.rewardCycleQuizzesCount || 5} req` },
    { id: 'rewards', label: 'Reward & Ads (Start.io)', icon: Coins, badge: '207226080' },
    { id: 'withdrawals', label: 'Withdrawal Requests', icon: Wallet, badge: 'Live' },
    { id: 'controls', label: 'App Controls & Status', icon: SlidersHorizontal, badge: settings?.isMaintenanceMode ? 'MAINT' : null },
    { id: 'content', label: 'Links & Dynamic Content', icon: FileText, badge: null },
    { id: 'notifications', label: 'Push & Announcements', icon: Bell, badge: null },
    { id: 'audit', label: 'Audit Trail Logs', icon: ShieldAlert, badge: null },
  ];

  const handleNavClick = (id: string) => {
    onSelectTab(id);
    setMobileMenuOpen(false);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col md:flex-row">
      {/* Top Banner if Maintenance Mode is Active */}
      {settings?.isMaintenanceMode && (
        <div className="fixed top-0 left-0 right-0 z-50 bg-amber-500 text-slate-950 px-4 py-2 text-xs md:text-sm font-bold flex items-center justify-between shadow-lg">
          <div className="flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 animate-bounce" />
            <span>MAINTENANCE MODE ACTIVE: Normal Android app users are blocked with maintenance banner. Admin Web Panel remains fully operational.</span>
          </div>
          <button 
            onClick={() => onSelectTab('controls')} 
            className="bg-slate-950 text-white px-2.5 py-1 rounded text-xs hover:bg-slate-800 transition"
          >
            Manage Controls
          </button>
        </div>
      )}

      {/* Desktop Sidebar */}
      <aside className={`hidden md:flex flex-col w-64 lg:w-72 bg-slate-900 border-r border-slate-800 p-4 sticky top-0 h-screen ${settings?.isMaintenanceMode ? 'pt-14' : ''}`}>
        {/* Brand Header */}
        <div className="flex items-center gap-3 px-2 py-4 mb-3 border-b border-slate-800/80">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-purple-600 to-indigo-500 flex items-center justify-center shadow-lg shadow-purple-600/30">
            <Radio className="w-6 h-6 text-white" />
          </div>
          <div>
            <div className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-purple-400 to-indigo-200 bg-clip-text text-transparent">
              Ads Pay Admin
            </div>
            <div className="text-[11px] text-slate-400 font-mono flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              Start.io SDK • Live
            </div>
          </div>
        </div>

        {/* Navigation List */}
        <nav className="flex-1 space-y-1.5 overflow-y-auto pr-1">
          {navigationItems.map(item => {
            const IconComponent = item.icon;
            const isActive = currentTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => handleNavClick(item.id)}
                className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-purple-600 text-white shadow-lg shadow-purple-600/25 font-semibold'
                    : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/70'
                }`}
              >
                <div className="flex items-center gap-3">
                  <IconComponent className={`w-5 h-5 ${isActive ? 'text-white' : 'text-slate-400'}`} />
                  <span>{item.label}</span>
                </div>
                {item.badge && (
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-mono font-bold ${
                    isActive 
                      ? 'bg-purple-800 text-purple-100' 
                      : item.badge === 'MAINT'
                      ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                      : 'bg-slate-800 text-slate-400'
                  }`}>
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>

        {/* Admin User Footer Card */}
        <div className="mt-auto pt-4 border-t border-slate-800/80">
          <div className="bg-slate-950/60 border border-slate-800 rounded-xl p-3 flex items-center justify-between">
            <div className="flex items-center gap-2.5 overflow-hidden">
              <div className="w-8 h-8 rounded-lg bg-purple-500/20 border border-purple-500/30 text-purple-400 flex items-center justify-center font-bold text-xs">
                {admin?.name?.charAt(0) || 'A'}
              </div>
              <div className="overflow-hidden">
                <div className="text-xs font-bold truncate text-slate-200">{admin?.name || 'Administrator'}</div>
                <div className="text-[10px] text-slate-400 truncate">{admin?.email}</div>
              </div>
            </div>
            <div className="flex items-center gap-1">
              <button
                onClick={onChangePasswordClick}
                title="Change Password"
                className="p-1.5 text-slate-400 hover:text-purple-400 hover:bg-slate-800 rounded-lg transition"
              >
                <KeyRound className="w-4 h-4" />
              </button>
              <button
                onClick={onLogout}
                title="Logout"
                className="p-1.5 text-slate-400 hover:text-red-400 hover:bg-slate-800 rounded-lg transition"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </aside>

      {/* Mobile Top Navbar */}
      <header className={`md:hidden flex items-center justify-between p-4 bg-slate-900 border-b border-slate-800 sticky top-0 z-40 ${settings?.isMaintenanceMode ? 'mt-9' : ''}`}>
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-purple-600 to-indigo-500 flex items-center justify-center">
            <Radio className="w-4 h-4 text-white" />
          </div>
          <span className="font-extrabold text-base tracking-tight bg-gradient-to-r from-purple-400 to-indigo-200 bg-clip-text text-transparent">
            Ads Pay Admin
          </span>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-2 rounded-lg bg-slate-800 text-slate-300 hover:bg-slate-700 transition"
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </header>

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden fixed inset-0 z-40 bg-slate-950/90 backdrop-blur-sm flex flex-col pt-16 p-4">
          <nav className="space-y-2 flex-1 overflow-y-auto py-4">
            {navigationItems.map(item => {
              const IconComponent = item.icon;
              const isActive = currentTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => handleNavClick(item.id)}
                  className={`w-full flex items-center justify-between p-3 rounded-xl text-sm font-medium ${
                    isActive ? 'bg-purple-600 text-white font-bold' : 'text-slate-300 hover:bg-slate-800'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <IconComponent className="w-5 h-5" />
                    <span>{item.label}</span>
                  </div>
                  {item.badge && (
                    <span className="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 font-mono">{item.badge}</span>
                  )}
                </button>
              );
            })}
          </nav>
          <div className="pt-4 border-t border-slate-800 flex items-center justify-between">
            <div className="text-xs">
              <div className="font-bold text-slate-200">{admin?.name}</div>
              <div className="text-slate-400">{admin?.email}</div>
            </div>
            <button
              onClick={onLogout}
              className="flex items-center gap-1.5 px-3 py-2 rounded-lg bg-red-600/20 text-red-400 hover:bg-red-600/30 text-xs font-bold"
            >
              <LogOut className="w-4 h-4" />
              Logout
            </button>
          </div>
        </div>
      )}

      {/* Main Content Viewport */}
      <main className={`flex-1 flex flex-col min-w-0 bg-slate-950 overflow-y-auto ${settings?.isMaintenanceMode ? 'md:pt-10' : ''}`}>
        <div className="p-4 md:p-8 max-w-7xl w-full mx-auto space-y-6">
          {children}
        </div>
      </main>
    </div>
  );
};
