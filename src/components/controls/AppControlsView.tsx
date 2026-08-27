import React, { useState, useEffect } from 'react';
import { 
  SlidersHorizontal, 
  AlertTriangle, 
  CheckCircle2, 
  Save, 
  Smartphone, 
  Power, 
  ShieldCheck,
  ToggleLeft,
  ToggleRight,
  Info
} from 'lucide-react';
import { api } from '../../api/client';
import { AppSettings } from '../../types';

interface Props {
  settings: AppSettings | null;
  onSettingsUpdated: (newSettings: AppSettings) => void;
}

export const AppControlsView: React.FC<Props> = ({ settings, onSettingsUpdated }) => {
  const [formData, setFormData] = useState<AppSettings | null>(null);
  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  useEffect(() => {
    if (settings) {
      setFormData({ ...settings });
    }
  }, [settings]);

  const handleToggle = (key: keyof AppSettings) => {
    if (!formData) return;
    setFormData({ ...formData, [key]: !formData[key] });
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData) return;

    setLoading(true);
    setSuccessMsg(null);
    setErrorMsg(null);

    try {
      const res = await api.updateSettings(formData, 'Updated Master App Controls & Maintenance State');
      onSettingsUpdated(res.settings);
      setSuccessMsg('Feature toggles and system state updated successfully!');
      setTimeout(() => setSuccessMsg(null), 5000);
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to update controls');
    } finally {
      setLoading(false);
    }
  };

  if (!formData) return null;

  const featureToggles = [
    { key: 'isRegistrationEnabled' as keyof AppSettings, label: 'User Registration', desc: 'Allow new accounts to sign up' },
    { key: 'isLoginEnabled' as keyof AppSettings, label: 'User Login', desc: 'Allow existing users to authenticate' },
    { key: 'isTaskSystemEnabled' as keyof AppSettings, label: 'Task Engine', desc: 'Allow users to launch quiz task cycles' },
    { key: 'isQuizEnabled' as keyof AppSettings, label: 'Quiz Questions', desc: 'Serve questions in the active task view' },
    { key: 'isBannerAdsEnabled' as keyof AppSettings, label: 'Start.io Banner Ads', desc: 'Display banner units on home and task screens' },
    { key: 'isRewardedAdsEnabled' as keyof AppSettings, label: 'Start.io Rewarded Ads', desc: 'Trigger video ads after 5 quiz completions' },
    { key: 'isReferralEnabled' as keyof AppSettings, label: 'Referral Program', desc: 'Enable 10% lifetime commission credit' },
    { key: 'isWithdrawEnabled' as keyof AppSettings, label: 'Withdrawal Engine', desc: 'Allow users to submit cashout requests' },
    { key: 'isBkashEnabled' as keyof AppSettings, label: 'bKash Payouts', desc: 'Enable Bangladesh bKash mobile financial service' },
    { key: 'isNagadEnabled' as keyof AppSettings, label: 'Nagad Payouts', desc: 'Enable Bangladesh Nagad mobile financial service' },
    { key: 'isUsdtEnabled' as keyof AppSettings, label: 'BEP20 USDT Payouts', desc: 'Enable Binance Smart Chain USDT crypto cashout' },
    { key: 'isLeaderboardEnabled' as keyof AppSettings, label: 'Leaderboard Rankings', desc: 'Display top earners ranking table' },
    { key: 'isNotificationsEnabled' as keyof AppSettings, label: 'In-App Alerts', desc: 'Show broadcast notices and messages' },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
            App Controls & Maintenance
          </h1>
          <p className="text-xs md:text-sm text-slate-400 mt-1">
            Global feature switches, security gates, and maintenance blackout controls.
          </p>
        </div>

        <button
          onClick={handleSave}
          disabled={loading}
          className="flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white rounded-xl text-xs font-bold shadow-lg shadow-purple-600/30 transition self-start disabled:opacity-50"
        >
          <Save className="w-4 h-4" />
          {loading ? 'Saving...' : 'Apply Controls'}
        </button>
      </div>

      {successMsg && (
        <div className="p-3.5 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs flex items-center gap-2.5">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{successMsg}</span>
        </div>
      )}

      {errorMsg && (
        <div className="p-3.5 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-center gap-2.5">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{errorMsg}</span>
        </div>
      )}

      <form onSubmit={handleSave} className="space-y-6">
        {/* Maintenance Mode Controller */}
        <div className={`p-6 rounded-2xl border transition-all ${
          formData.isMaintenanceMode 
            ? 'bg-amber-950/30 border-amber-500/50 shadow-xl shadow-amber-500/10' 
            : 'bg-slate-900 border-slate-800'
        }`}>
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-4 mb-4">
            <div className="flex items-center gap-3">
              <div className={`w-12 h-12 rounded-xl flex items-center justify-center font-bold ${
                formData.isMaintenanceMode ? 'bg-amber-500 text-slate-950 animate-pulse' : 'bg-slate-800 text-slate-400'
              }`}>
                <AlertTriangle className="w-6 h-6" />
              </div>
              <div>
                <h2 className="text-base font-bold text-white">Emergency Maintenance Mode</h2>
                <p className="text-xs text-slate-400">
                  When active, normal users opening the Android User App will be blocked with a full-screen maintenance overlay.
                </p>
              </div>
            </div>

            <button
              type="button"
              onClick={() => handleToggle('isMaintenanceMode')}
              className={`px-5 py-2.5 rounded-xl font-bold text-xs flex items-center gap-2 transition ${
                formData.isMaintenanceMode 
                  ? 'bg-amber-500 text-slate-950 hover:bg-amber-400 shadow-lg shadow-amber-500/30' 
                  : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
              }`}
            >
              <Power className="w-4 h-4" />
              {formData.isMaintenanceMode ? 'MAINTENANCE ACTIVE' : 'Normal Operation'}
            </button>
          </div>

          <div className="space-y-2">
            <label className="block text-xs font-bold text-slate-300">
              Custom Maintenance Message Displayed to Android Users
            </label>
            <textarea
              rows={2}
              value={formData.maintenanceMessage}
              onChange={(e) => setFormData({ ...formData, maintenanceMessage: e.target.value })}
              placeholder="Enter message for app users..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-xs text-white focus:outline-none focus:border-amber-500"
            />
          </div>
        </div>

        {/* Master Feature Toggles Grid */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
          <div className="flex items-center gap-2.5 border-b border-slate-800 pb-3 mb-4">
            <div className="w-9 h-9 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center font-bold">
              <SlidersHorizontal className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-white">Granular Feature Gates</h2>
              <p className="text-[11px] text-slate-400">Instantly activate or disable individual app modules</p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            {featureToggles.map(toggle => {
              const isEnabled = Boolean(formData[toggle.key]);
              return (
                <div
                  key={toggle.key}
                  onClick={() => handleToggle(toggle.key)}
                  className={`p-3.5 rounded-xl border cursor-pointer transition flex items-center justify-between ${
                    isEnabled 
                      ? 'bg-slate-950 border-purple-500/30 hover:border-purple-500/50' 
                      : 'bg-slate-950/40 border-slate-800/80 opacity-60 hover:opacity-100'
                  }`}
                >
                  <div>
                    <div className="text-xs font-bold text-white">{toggle.label}</div>
                    <div className="text-[10px] text-slate-400">{toggle.desc}</div>
                  </div>
                  <div className={`w-9 h-5 rounded-full p-0.5 transition ${isEnabled ? 'bg-purple-600' : 'bg-slate-800'}`}>
                    <div className={`w-4 h-4 rounded-full bg-white transition transform ${isEnabled ? 'translate-x-4' : 'translate-x-0'}`} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </form>
    </div>
  );
};
