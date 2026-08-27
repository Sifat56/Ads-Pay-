import React, { useState, useEffect } from 'react';
import { 
  Coins, 
  Tv, 
  Share2, 
  ShieldCheck, 
  Save, 
  RefreshCw, 
  AlertCircle, 
  CheckCircle2,
  Lock,
  Layers,
  Clock,
  Sparkles,
  Info
} from 'lucide-react';
import { api } from '../../api/client';
import { AppSettings } from '../../types';

interface Props {
  settings: AppSettings | null;
  onSettingsUpdated: (newSettings: AppSettings) => void;
}

export const RewardAdsSettingsView: React.FC<Props> = ({ settings, onSettingsUpdated }) => {
  const [formData, setFormData] = useState<AppSettings | null>(null);
  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [auditReason, setAuditReason] = useState('');

  useEffect(() => {
    if (settings) {
      setFormData({ ...settings });
    }
  }, [settings]);

  const handleChange = (field: keyof AppSettings, value: any) => {
    if (!formData) return;
    setFormData({ ...formData, [field]: value });
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData) return;

    setLoading(true);
    setSuccessMsg(null);
    setErrorMsg(null);

    try {
      const res = await api.updateSettings(formData, auditReason.trim() || 'Updated Reward & Ads Settings');
      onSettingsUpdated(res.settings);
      setSuccessMsg('Reward and Start.io monetization settings successfully synchronized!');
      setAuditReason('');
      setTimeout(() => setSuccessMsg(null), 5000);
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to save settings');
    } finally {
      setLoading(false);
    }
  };

  if (!formData) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="w-8 h-8 border-4 border-purple-500/20 border-t-purple-500 rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
            Reward & Start.io Ad Settings
          </h1>
          <p className="text-xs md:text-sm text-slate-400 mt-1">
            Configure task reward payouts, monetary rates, and official Start.io SDK integrations.
          </p>
        </div>

        <button
          onClick={handleSave}
          disabled={loading}
          className="flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white rounded-xl text-xs font-bold shadow-lg shadow-purple-600/30 transition self-start disabled:opacity-50"
        >
          <Save className="w-4 h-4" />
          {loading ? 'Saving...' : 'Save Configuration'}
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

      <form onSubmit={handleSave} className="space-y-6 text-xs">
        {/* Start.io Official SDK Integration Card */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 md:p-6 space-y-4">
          <div className="flex items-center justify-between border-b border-slate-800 pb-3">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-indigo-500/10 text-indigo-400 flex items-center justify-center font-bold">
                <Tv className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-sm font-bold text-white">Start.io Ad Network Integration</h2>
                <p className="text-[11px] text-slate-400">Official Android SDK Monetization</p>
              </div>
            </div>
            <span className="bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 px-2.5 py-1 rounded-lg text-[10px] font-mono font-bold">
              SDK v5.1.0 Ready
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block font-bold text-slate-300 mb-1">
                Start.io Application ID
              </label>
              <div className="relative">
                <input
                  type="text"
                  required
                  value={formData.startIoAppId}
                  onChange={(e) => handleChange('startIoAppId', e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-white font-mono focus:outline-none focus:border-purple-500"
                />
                <span className="absolute right-3 top-2.5 text-[10px] text-emerald-400 font-bold">Verified</span>
              </div>
              <p className="text-[10px] text-slate-500 mt-1">
                Linked to Android package: <strong className="text-slate-400 font-mono">com.adspay.app</strong>
              </p>
            </div>

            <div className="space-y-2 pt-1">
              <label className="flex items-center justify-between p-2.5 rounded-xl bg-slate-950 border border-slate-800 cursor-pointer">
                <span className="font-bold text-slate-300">Banner Advertisements</span>
                <input
                  type="checkbox"
                  checked={formData.isBannerAdsEnabled}
                  onChange={(e) => handleChange('isBannerAdsEnabled', e.target.checked)}
                  className="rounded text-purple-600 focus:ring-purple-500 w-4 h-4 bg-slate-900 border-slate-700"
                />
              </label>

              <label className="flex items-center justify-between p-2.5 rounded-xl bg-slate-950 border border-slate-800 cursor-pointer">
                <span className="font-bold text-slate-300">Rewarded Video Ads (5-Quiz Trigger)</span>
                <input
                  type="checkbox"
                  checked={formData.isRewardedAdsEnabled}
                  onChange={(e) => handleChange('isRewardedAdsEnabled', e.target.checked)}
                  className="rounded text-purple-600 focus:ring-purple-500 w-4 h-4 bg-slate-900 border-slate-700"
                />
              </label>
            </div>
          </div>

          {/* Ad Fraud Rule Reminder */}
          <div className="bg-slate-950/80 border border-slate-800 rounded-xl p-3 text-[11px] text-slate-400 flex items-start gap-2.5">
            <Info className="w-4 h-4 text-purple-400 shrink-0 mt-0.5" />
            <div>
              <strong className="text-slate-200">Start.io Compliance Policy:</strong> Ads Pay never rewards ad clicks or uses fake placeholders. Points are awarded exclusively upon verified onAdCompleted callbacks from the official Start.io SDK.
            </div>
          </div>
        </div>

        {/* Task Cycle & Points Rules */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 md:p-6 space-y-4">
          <div className="flex items-center gap-2.5 border-b border-slate-800 pb-3">
            <div className="w-9 h-9 rounded-xl bg-amber-500/10 text-amber-400 flex items-center justify-center font-bold">
              <Coins className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-white">Task Reward & Point Economy</h2>
              <p className="text-[11px] text-slate-400">Rules for completing cycles and currency valuation</p>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block font-bold text-slate-300 mb-1">
                Quizzes per Reward Cycle
              </label>
              <input
                type="number"
                min="1"
                max="20"
                value={formData.rewardCycleQuizzesCount}
                onChange={(e) => handleChange('rewardCycleQuizzesCount', parseInt(e.target.value) || 5)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-white focus:outline-none focus:border-purple-500"
              />
              <p className="text-[10px] text-slate-500 mt-1">Default is 5 consecutive quizzes</p>
            </div>

            <div>
              <label className="block font-bold text-slate-300 mb-1">
                Points Rewarded per Cycle
              </label>
              <input
                type="number"
                step="0.1"
                min="0.1"
                value={formData.rewardPointsPerCycle}
                onChange={(e) => handleChange('rewardPointsPerCycle', parseFloat(e.target.value) || 1.0)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-white focus:outline-none focus:border-purple-500"
              />
              <p className="text-[10px] text-slate-500 mt-1">Default: 1.0 point</p>
            </div>

            <div>
              <label className="block font-bold text-slate-300 mb-1">
                Quiz Countdown Timer (Seconds)
              </label>
              <input
                type="number"
                min="5"
                max="60"
                value={formData.quizTimerSeconds}
                onChange={(e) => handleChange('quizTimerSeconds', parseInt(e.target.value) || 10)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-white focus:outline-none focus:border-purple-500"
              />
              <p className="text-[10px] text-slate-500 mt-1">Anti-fraud timer: 10s</p>
            </div>

            <div>
              <label className="block font-bold text-slate-300 mb-1">
                Monetary Value per Point
              </label>
              <div className="flex">
                <span className="bg-slate-800 border border-r-0 border-slate-800 rounded-l-xl px-3 py-2.5 text-slate-400 font-bold">
                  {formData.currencySymbol}
                </span>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={formData.pointMonetaryValue}
                  onChange={(e) => handleChange('pointMonetaryValue', parseFloat(e.target.value) || 0.20)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-r-xl px-3.5 py-2.5 text-white focus:outline-none focus:border-purple-500"
                />
              </div>
              <p className="text-[10px] text-slate-500 mt-1">e.g. 1 pt = ৳0.20 BDT</p>
            </div>

            <div>
              <label className="block font-bold text-slate-300 mb-1">
                Currency Symbol
              </label>
              <input
                type="text"
                value={formData.currencySymbol}
                onChange={(e) => handleChange('currencySymbol', e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-white focus:outline-none focus:border-purple-500"
              />
              <p className="text-[10px] text-slate-500 mt-1">e.g. ৳ or $</p>
            </div>

            <div>
              <label className="block font-bold text-slate-300 mb-1">
                Daily Task Limit (per User)
              </label>
              <input
                type="number"
                min="5"
                max="500"
                value={formData.dailyTaskLimit}
                onChange={(e) => handleChange('dailyTaskLimit', parseInt(e.target.value) || 50)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-white focus:outline-none focus:border-purple-500"
              />
              <p className="text-[10px] text-slate-500 mt-1">Prevents bot farming</p>
            </div>
          </div>
        </div>

        {/* Referral System Settings */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 md:p-6 space-y-4">
          <div className="flex items-center justify-between border-b border-slate-800 pb-3">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center font-bold">
                <Share2 className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-sm font-bold text-white">Referral Program Rules</h2>
                <p className="text-[11px] text-slate-400">Commission rates for inviting friends</p>
              </div>
            </div>
            <label className="flex items-center gap-2 cursor-pointer">
              <span className="text-slate-300 font-bold text-xs">Enable Referral System</span>
              <input
                type="checkbox"
                checked={formData.isReferralEnabled}
                onChange={(e) => handleChange('isReferralEnabled', e.target.checked)}
                className="rounded text-purple-600 focus:ring-purple-500 w-4 h-4 bg-slate-950 border-slate-700"
              />
            </label>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block font-bold text-slate-300 mb-1">
                Referral Commission Rate (%)
              </label>
              <input
                type="number"
                min="0"
                max="50"
                step="1"
                value={formData.referralCommissionPercent}
                onChange={(e) => handleChange('referralCommissionPercent', parseFloat(e.target.value) || 10)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-white focus:outline-none focus:border-purple-500"
              />
              <p className="text-[10px] text-slate-500 mt-1">
                Referrers receive {formData.referralCommissionPercent}% whenever their invited users complete a rewarded ad cycle.
              </p>
            </div>
          </div>
        </div>

        {/* Audit Log Reason */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4">
          <label className="block font-bold text-slate-300 mb-1">
            Audit Reason for this Update (Recorded in immutable log)
          </label>
          <input
            type="text"
            value={auditReason}
            onChange={(e) => setAuditReason(e.target.value)}
            placeholder="e.g. Adjusted point monetary value and Start.io parameters"
            className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-white focus:outline-none focus:border-purple-500"
          />
        </div>
      </form>
    </div>
  );
};
