import React, { useState, useEffect } from 'react';
import { 
  FileText, 
  Send, 
  Video, 
  Mail, 
  HelpCircle, 
  Save, 
  CheckCircle2, 
  AlertCircle,
  ExternalLink,
  MessageSquare
} from 'lucide-react';
import { api } from '../../api/client';
import { AppSettings } from '../../types';

interface Props {
  settings: AppSettings | null;
  onSettingsUpdated: (newSettings: AppSettings) => void;
}

export const ContentManagementView: React.FC<Props> = ({ settings, onSettingsUpdated }) => {
  const [formData, setFormData] = useState<AppSettings | null>(null);
  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

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
      const res = await api.updateSettings(formData, 'Updated Links & Dynamic App Content');
      onSettingsUpdated(res.settings);
      setSuccessMsg('Dynamic content and community links synchronized to Android User App!');
      setTimeout(() => setSuccessMsg(null), 5000);
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to update content');
    } finally {
      setLoading(false);
    }
  };

  if (!formData) return null;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
            Links & Dynamic Content
          </h1>
          <p className="text-xs md:text-sm text-slate-400 mt-1">
            Update social channels, in-app announcement banners, and step-by-step working guidelines.
          </p>
        </div>

        <button
          onClick={handleSave}
          disabled={loading}
          className="flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white rounded-xl text-xs font-bold shadow-lg shadow-purple-600/30 transition self-start disabled:opacity-50"
        >
          <Save className="w-4 h-4" />
          {loading ? 'Saving...' : 'Publish Content'}
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
        {/* Social & Community Links */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
          <div className="flex items-center gap-2.5 border-b border-slate-800 pb-3">
            <div className="w-9 h-9 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center font-bold">
              <ExternalLink className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-white">Community & Support Links</h2>
              <p className="text-[11px] text-slate-400">Direct links opened when users tap community buttons in app</p>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block font-bold text-slate-300 mb-1 flex items-center gap-1.5">
                <Send className="w-3.5 h-3.5 text-sky-400" /> Telegram Channel URL
              </label>
              <input
                type="url"
                value={formData.telegramUrl}
                onChange={(e) => handleChange('telegramUrl', e.target.value)}
                placeholder="https://t.me/adspayofficial"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-white focus:outline-none focus:border-purple-500"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-300 mb-1 flex items-center gap-1.5">
                <Video className="w-3.5 h-3.5 text-red-400" /> YouTube Official Channel
              </label>
              <input
                type="url"
                value={formData.youtubeUrl}
                onChange={(e) => handleChange('youtubeUrl', e.target.value)}
                placeholder="https://youtube.com/@adspay"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-white focus:outline-none focus:border-purple-500"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-300 mb-1 flex items-center gap-1.5">
                <Mail className="w-3.5 h-3.5 text-purple-400" /> Support Contact Email
              </label>
              <input
                type="text"
                value={formData.supportContact}
                onChange={(e) => handleChange('supportContact', e.target.value)}
                placeholder="support@adspay.app"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-white focus:outline-none focus:border-purple-500"
              />
            </div>
          </div>
        </div>

        {/* Global Announcement Banner */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-3">
          <div className="flex items-center gap-2.5 border-b border-slate-800 pb-3">
            <div className="w-9 h-9 rounded-xl bg-amber-500/10 text-amber-400 flex items-center justify-center font-bold">
              <MessageSquare className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-white">In-App Announcement Banner</h2>
              <p className="text-[11px] text-slate-400">Displayed at the top of the Android user dashboard</p>
            </div>
          </div>

          <div>
            <textarea
              rows={2}
              value={formData.announcementText}
              onChange={(e) => handleChange('announcementText', e.target.value)}
              placeholder="e.g. Welcome to Ads Pay! Complete 5 quizzes to unlock your verified rewarded ad..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-white focus:outline-none focus:border-amber-500"
            />
          </div>
        </div>

        {/* Dynamic Instructional Texts */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* How to Work text */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-3">
            <div className="flex items-center gap-2.5 border-b border-slate-800 pb-3">
              <div className="w-9 h-9 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center font-bold">
                <HelpCircle className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-sm font-bold text-white">"How to Work" Guide</h2>
                <p className="text-[11px] text-slate-400">Step-by-step instructions shown in app</p>
              </div>
            </div>

            <textarea
              rows={8}
              value={formData.howToWorkText}
              onChange={(e) => handleChange('howToWorkText', e.target.value)}
              placeholder="1. Start Task\n2. Wait 10s timer\n3. Complete 5 quizzes..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-white font-mono focus:outline-none focus:border-purple-500 text-xs"
            />
          </div>

          {/* About Ads Pay Text */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-3">
            <div className="flex items-center gap-2.5 border-b border-slate-800 pb-3">
              <div className="w-9 h-9 rounded-xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center font-bold">
                <FileText className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-sm font-bold text-white">"About Ads Pay" Text</h2>
                <p className="text-[11px] text-slate-400">Company mission & terms dialog</p>
              </div>
            </div>

            <textarea
              rows={8}
              value={formData.aboutText}
              onChange={(e) => handleChange('aboutText', e.target.value)}
              placeholder="Ads Pay is a transparent reward platform..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-white focus:outline-none focus:border-emerald-500 text-xs"
            />
          </div>
        </div>
      </form>
    </div>
  );
};
