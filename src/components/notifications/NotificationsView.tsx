import React, { useState, useEffect } from 'react';
import { 
  Bell, 
  Send, 
  Users, 
  User, 
  CheckCircle2, 
  AlertCircle, 
  Clock, 
  RefreshCw,
  MessageSquare
} from 'lucide-react';
import { api } from '../../api/client';
import { AppNotification } from '../../types';

export const NotificationsView: React.FC = () => {
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // Form
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [targetType, setTargetType] = useState<'ALL' | 'SPECIFIC'>('ALL');
  const [targetUserId, setTargetUserId] = useState('');
  const [type, setType] = useState('ANNOUNCEMENT');

  useEffect(() => {
    fetchNotifications();
  }, []);

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      const res = await api.getNotifications();
      setNotifications(res.notifications);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !message.trim()) {
      setErrorMsg('Title and message are required.');
      return;
    }
    if (targetType === 'SPECIFIC' && !targetUserId.trim()) {
      setErrorMsg('Please specify the recipient User ID.');
      return;
    }

    setSubmitting(true);
    setErrorMsg(null);
    setSuccessMsg(null);

    try {
      await api.sendNotification({
        title: title.trim(),
        message: message.trim(),
        type,
        targetUserId: targetType === 'ALL' ? 'ALL' : targetUserId.trim()
      });

      setSuccessMsg('Broadcast notification sent to Android users successfully!');
      setTitle('');
      setMessage('');
      setTargetUserId('');
      fetchNotifications();
      setTimeout(() => setSuccessMsg(null), 5000);
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to dispatch notification');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
            Push Alerts & Broadcasts
          </h1>
          <p className="text-xs md:text-sm text-slate-400 mt-1">
            Dispatch urgent system announcements and personalized in-app notifications.
          </p>
        </div>

        <button
          onClick={fetchNotifications}
          disabled={loading}
          className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition self-start"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          Refresh List
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

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Composer Form */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
          <h2 className="text-base font-bold text-white mb-4 flex items-center gap-2">
            <Send className="w-4 h-4 text-purple-400" />
            Compose Notification
          </h2>

          <form onSubmit={handleSubmit} className="space-y-4 text-xs">
            <div>
              <label className="block font-bold text-slate-300 mb-1">
                Recipient Audience
              </label>
              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => setTargetType('ALL')}
                  className={`py-2 px-3 rounded-xl font-bold flex items-center justify-center gap-1.5 transition ${
                    targetType === 'ALL' ? 'bg-purple-600 text-white' : 'bg-slate-950 border border-slate-800 text-slate-400'
                  }`}
                >
                  <Users className="w-3.5 h-3.5" /> All Users
                </button>
                <button
                  type="button"
                  onClick={() => setTargetType('SPECIFIC')}
                  className={`py-2 px-3 rounded-xl font-bold flex items-center justify-center gap-1.5 transition ${
                    targetType === 'SPECIFIC' ? 'bg-purple-600 text-white' : 'bg-slate-950 border border-slate-800 text-slate-400'
                  }`}
                >
                  <User className="w-3.5 h-3.5" /> Specific User
                </button>
              </div>
            </div>

            {targetType === 'SPECIFIC' && (
              <div>
                <label className="block font-bold text-slate-300 mb-1">
                  Recipient User ID (e.g. AP-10824)
                </label>
                <input
                  type="text"
                  required
                  value={targetUserId}
                  onChange={(e) => setTargetUserId(e.target.value)}
                  placeholder="AP-10824"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-white font-mono focus:outline-none focus:border-purple-500"
                />
              </div>
            )}

            <div>
              <label className="block font-bold text-slate-300 mb-1">
                Notification Category / Type
              </label>
              <select
                value={type}
                onChange={(e) => setType(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-purple-500"
              >
                <option value="ANNOUNCEMENT">General Announcement</option>
                <option value="REWARD">Reward / Bonus Alert</option>
                <option value="SYSTEM">System Alert</option>
                <option value="MAINTENANCE">Maintenance Notice</option>
              </select>
            </div>

            <div>
              <label className="block font-bold text-slate-300 mb-1">
                Notification Title *
              </label>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Weekend Point Boost Active! 🎉"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-white focus:outline-none focus:border-purple-500"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-300 mb-1">
                Message Body *
              </label>
              <textarea
                required
                rows={4}
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="Write message content here..."
                className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-white focus:outline-none focus:border-purple-500"
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white rounded-xl font-bold transition disabled:opacity-50 flex items-center justify-center gap-2"
            >
              <Send className="w-4 h-4" />
              {submitting ? 'Sending...' : 'Broadcast Notification'}
            </button>
          </form>
        </div>

        {/* History Table */}
        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-6">
          <h2 className="text-base font-bold text-white mb-4 flex items-center gap-2">
            <Bell className="w-4 h-4 text-purple-400" />
            Sent Notifications History ({notifications.length})
          </h2>

          <div className="space-y-3 max-h-[500px] overflow-y-auto">
            {loading ? (
              <div className="text-center py-12">
                <div className="inline-block w-6 h-6 border-2 border-purple-500/20 border-t-purple-500 rounded-full animate-spin"></div>
              </div>
            ) : notifications.length === 0 ? (
              <div className="text-center py-12 text-xs text-slate-500">
                No notifications sent yet.
              </div>
            ) : (
              notifications.map(n => (
                <div key={n.id} className="p-4 rounded-xl bg-slate-950 border border-slate-800 text-xs">
                  <div className="flex items-center justify-between mb-1.5">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-white">{n.title}</span>
                      <span className="bg-purple-500/10 text-purple-400 border border-purple-500/20 px-2 py-0.5 rounded text-[10px] font-mono">
                        {n.targetUserId === 'ALL' ? 'Broadcast (All)' : `User: ${n.targetUserId}`}
                      </span>
                    </div>
                    <span className="text-[10px] text-slate-500">
                      {new Date(n.timestamp).toLocaleString()}
                    </span>
                  </div>
                  <p className="text-slate-400 text-xs mt-1">
                    {n.message}
                  </p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
