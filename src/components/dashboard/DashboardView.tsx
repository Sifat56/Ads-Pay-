import React, { useState, useEffect } from 'react';
import { 
  Users, 
  UserCheck, 
  UserX, 
  Coins, 
  CheckCircle2, 
  Clock, 
  XCircle, 
  TrendingUp, 
  Award, 
  Share2, 
  Tv, 
  AlertTriangle,
  RefreshCw,
  ArrowUpRight,
  Shield,
  CreditCard,
  Plus
} from 'lucide-react';
import { 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell
} from 'recharts';
import { api } from '../../api/client';
import { DashboardMetrics, WithdrawalRequest } from '../../types';

interface DashboardViewProps {
  onNavigate: (tab: string) => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({ onNavigate }) => {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<{
    metrics: DashboardMetrics;
    weeklyTrend: Array<{ name: string; rewards: number; signups: number; payouts: number }>;
    methodStats: { BKASH: number; NAGAD: number; USDT_BEP20: number };
    recentWithdrawals: WithdrawalRequest[];
    recentTransactions: any[];
    recentUsers: any[];
  } | null>(null);

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      const res = await api.getDashboard();
      setData(res);
    } catch (err) {
      console.error('Failed to load dashboard metrics', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading && !data) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="w-8 h-8 border-4 border-purple-500/20 border-t-purple-500 rounded-full animate-spin"></div>
      </div>
    );
  }

  const metrics = data?.metrics;
  const methodPieData = [
    { name: 'bKash', value: data?.methodStats.BKASH || 0, color: '#ec4899' },
    { name: 'Nagad', value: data?.methodStats.NAGAD || 0, color: '#f97316' },
    { name: 'BEP20 USDT', value: data?.methodStats.USDT_BEP20 || 0, color: '#10b981' }
  ];

  return (
    <div className="space-y-6">
      {/* Header with Title and Quick Refresh */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
            System Overview & Dashboard
          </h1>
          <p className="text-xs md:text-sm text-slate-400 mt-1">
            Real-time analytics for Ads Pay Android User App & Start.io monetization network.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchDashboard}
            disabled={loading}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh Stats
          </button>
          <button
            onClick={() => onNavigate('withdrawals')}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 text-xs font-bold text-white shadow-lg shadow-purple-600/30 transition"
          >
            <CreditCard className="w-3.5 h-3.5" />
            Review Cashouts ({metrics?.pendingWithdrawalsCount || 0})
          </button>
        </div>
      </div>

      {/* Primary Key Performance Metrics Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3.5 md:gap-4">
        {/* Total Users */}
        <div className="bg-slate-900/90 border border-slate-800 p-4 rounded-2xl">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Users</span>
            <Users className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-2xl md:text-3xl font-extrabold text-white">
            {metrics?.totalUsers || 0}
          </div>
          <div className="mt-2 flex items-center justify-between text-[11px]">
            <span className="text-emerald-400 flex items-center gap-1">
              <UserCheck className="w-3 h-3" /> {metrics?.activeUsers || 0} active
            </span>
            <span className="text-rose-400 flex items-center gap-1">
              <UserX className="w-3 h-3" /> {metrics?.blockedUsers || 0} blocked
            </span>
          </div>
        </div>

        {/* Total Points Issued */}
        <div className="bg-slate-900/90 border border-slate-800 p-4 rounded-2xl">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Points Issued</span>
            <Coins className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl md:text-3xl font-extrabold text-amber-300">
            {metrics?.totalPointsIssued || 0} <span className="text-xs text-slate-400 font-normal">pts</span>
          </div>
          <div className="mt-2 text-[11px] text-slate-400">
            User Pool Balance: <strong className="text-slate-200">{metrics?.currentTotalUserBalance || 0} pts</strong>
          </div>
        </div>

        {/* Rewarded Ads / Cycles */}
        <div className="bg-slate-900/90 border border-slate-800 p-4 rounded-2xl">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Verified Ad Cycles</span>
            <Tv className="w-4 h-4 text-indigo-400" />
          </div>
          <div className="text-2xl md:text-3xl font-extrabold text-indigo-300">
            {metrics?.totalRewardCyclesCompleted || 0}
          </div>
          <div className="mt-2 text-[11px] text-slate-400">
            Tasks Completed: <strong className="text-slate-200">{metrics?.totalTasksCompleted || 0}</strong>
          </div>
        </div>

        {/* Pending Withdrawals */}
        <div className="bg-slate-900/90 border border-slate-800 p-4 rounded-2xl">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Pending Payouts</span>
            <Clock className="w-4 h-4 text-rose-400" />
          </div>
          <div className="text-2xl md:text-3xl font-extrabold text-rose-300">
            ৳{metrics?.pendingWithdrawalsAmount || 0}
          </div>
          <div className="mt-2 flex items-center justify-between text-[11px]">
            <span className="text-amber-400 font-bold">{metrics?.pendingWithdrawalsCount || 0} pending queue</span>
            <span className="text-emerald-400">৳{metrics?.completedWithdrawalsAmount || 0} paid</span>
          </div>
        </div>
      </div>

      {/* Secondary Quick Metrics Row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 bg-slate-900/50 border border-slate-800/80 p-3.5 rounded-2xl text-xs">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center font-bold">
            <Share2 className="w-4 h-4" />
          </div>
          <div>
            <div className="text-slate-400">Referred Users</div>
            <div className="font-extrabold text-white text-sm">{metrics?.referredUsersCount || 0} accounts</div>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-purple-500/10 text-purple-400 flex items-center justify-center font-bold">
            <Award className="w-4 h-4" />
          </div>
          <div>
            <div className="text-slate-400">Referral Commissions</div>
            <div className="font-extrabold text-purple-300 text-sm">{metrics?.totalReferralCommissionPaid || 0} pts</div>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-blue-500/10 text-blue-400 flex items-center justify-center font-bold">
            <Shield className="w-4 h-4" />
          </div>
          <div>
            <div className="text-slate-400">Start.io App ID</div>
            <div className="font-mono font-bold text-white text-sm">{metrics?.startIoAppId || '207226080'}</div>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <div className={`w-8 h-8 rounded-lg ${metrics?.isMaintenanceMode ? 'bg-amber-500/20 text-amber-400' : 'bg-emerald-500/10 text-emerald-400'} flex items-center justify-center font-bold`}>
            {metrics?.isMaintenanceMode ? <AlertTriangle className="w-4 h-4" /> : <CheckCircle2 className="w-4 h-4" />}
          </div>
          <div>
            <div className="text-slate-400">System State</div>
            <div className={`font-bold text-sm ${metrics?.isMaintenanceMode ? 'text-amber-400' : 'text-emerald-400'}`}>
              {metrics?.isMaintenanceMode ? 'Maintenance Mode' : 'Online & Active'}
            </div>
          </div>
        </div>
      </div>

      {/* Analytics Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Weekly Task & Ad Reward Cycles Area Chart */}
        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 p-5 rounded-2xl">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="font-bold text-white text-base">Weekly Activity & Rewards Trend</h2>
              <p className="text-xs text-slate-400">Daily rewarded ad cycles and user registrations</p>
            </div>
            <span className="text-xs font-mono bg-purple-500/10 text-purple-400 px-2.5 py-1 rounded-lg border border-purple-500/20">
              Last 7 Days
            </span>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={data?.weeklyTrend || []} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="rewardsGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#9333ea" stopOpacity={0.4}/>
                    <stop offset="95%" stopColor="#9333ea" stopOpacity={0.0}/>
                  </linearGradient>
                  <linearGradient id="signupsGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#06b6d4" stopOpacity={0.4}/>
                    <stop offset="95%" stopColor="#06b6d4" stopOpacity={0.0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="name" stroke="#64748b" fontSize={12} />
                <YAxis stroke="#64748b" fontSize={12} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px', fontSize: '12px' }}
                />
                <Area type="monotone" dataKey="rewards" name="Rewarded Ad Cycles" stroke="#9333ea" strokeWidth={2} fillOpacity={1} fill="url(#rewardsGrad)" />
                <Area type="monotone" dataKey="signups" name="New User Signups" stroke="#06b6d4" strokeWidth={2} fillOpacity={1} fill="url(#signupsGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Withdrawal Method Breakdown */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl flex flex-col justify-between">
          <div>
            <h2 className="font-bold text-white text-base">Withdrawal Gateways</h2>
            <p className="text-xs text-slate-400 mb-4">Payout requests by payment provider</p>

            <div className="h-44 flex items-center justify-center">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={methodPieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={45}
                    outerRadius={65}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {methodPieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px', fontSize: '12px' }} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="space-y-2 pt-2 border-t border-slate-800 text-xs">
            <div className="flex items-center justify-between">
              <span className="flex items-center gap-2 text-slate-300">
                <span className="w-2.5 h-2.5 rounded-full bg-pink-500"></span> bKash (Bangladesh)
              </span>
              <span className="font-bold text-white">{data?.methodStats.BKASH || 0} reqs</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="flex items-center gap-2 text-slate-300">
                <span className="w-2.5 h-2.5 rounded-full bg-orange-500"></span> Nagad (Bangladesh)
              </span>
              <span className="font-bold text-white">{data?.methodStats.NAGAD || 0} reqs</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="flex items-center gap-2 text-slate-300">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span> USDT (BEP20 BSC)
              </span>
              <span className="font-bold text-white">{data?.methodStats.USDT_BEP20 || 0} reqs</span>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Activity: Pending Withdrawals & Recent Transactions */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Pending Withdrawals Queue */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="font-bold text-white text-base">Pending Cashout Requests</h2>
              <p className="text-xs text-slate-400">Users awaiting manual payout processing</p>
            </div>
            <button
              onClick={() => onNavigate('withdrawals')}
              className="text-xs text-purple-400 hover:text-purple-300 font-bold flex items-center gap-1"
            >
              View All <ArrowUpRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="space-y-2.5">
            {data?.recentWithdrawals.filter(w => w.status === 'PENDING').length === 0 ? (
              <div className="text-center py-8 text-xs text-slate-500">
                No pending withdrawals. All cashouts processed!
              </div>
            ) : (
              data?.recentWithdrawals.filter(w => w.status === 'PENDING').slice(0, 4).map(w => (
                <div key={w.id} className="p-3 rounded-xl bg-slate-950/60 border border-slate-800 flex items-center justify-between">
                  <div>
                    <div className="font-bold text-xs text-slate-200">{w.userName} ({w.method})</div>
                    <div className="text-[11px] text-slate-400 font-mono">{w.accountInfo}</div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold text-xs text-emerald-400">৳{w.amountCurrency}</div>
                    <div className="text-[10px] text-slate-400">{w.points} pts</div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Recent Ledger Events */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="font-bold text-white text-base">Live Earning Ledger</h2>
              <p className="text-xs text-slate-400">Recent rewarded ad completions & bonuses</p>
            </div>
            <button
              onClick={() => onNavigate('users')}
              className="text-xs text-purple-400 hover:text-purple-300 font-bold flex items-center gap-1"
            >
              Inspect Users <ArrowUpRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="space-y-2.5">
            {data?.recentTransactions.slice(0, 4).map(tx => (
              <div key={tx.id} className="p-3 rounded-xl bg-slate-950/60 border border-slate-800 flex items-center justify-between">
                <div>
                  <div className="font-bold text-xs text-slate-200">{tx.title}</div>
                  <div className="text-[11px] text-slate-400 truncate max-w-xs">{tx.description}</div>
                </div>
                <div className="text-right">
                  <div className={`font-bold text-xs ${tx.points >= 0 ? 'text-amber-300' : 'text-rose-400'}`}>
                    {tx.points >= 0 ? '+' : ''}{tx.points} pts
                  </div>
                  <div className="text-[10px] text-slate-500">
                    {new Date(tx.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
