import React, { useState, useEffect } from 'react';
import { 
  Users, 
  Search, 
  Filter, 
  Eye, 
  ShieldAlert, 
  ShieldCheck, 
  Coins, 
  Lock, 
  Unlock, 
  RefreshCw, 
  AlertCircle, 
  X, 
  Clock, 
  CheckCircle2, 
  XCircle,
  Share2,
  Calendar,
  Phone,
  Mail,
  Edit3,
  Award
} from 'lucide-react';
import { api } from '../../api/client';
import { User, WithdrawalRequest, RewardTransaction } from '../../types';

export const UsersView: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [sortOption, setSortOption] = useState('CREATED_DESC');

  // Selected User Modal
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [userDetails, setUserDetails] = useState<{
    user: User;
    transactions: RewardTransaction[];
    withdrawals: WithdrawalRequest[];
    referredUsers: User[];
    taskAttempts: any[];
  } | null>(null);
  const [loadingDetails, setLoadingDetails] = useState(false);

  // Manual Points Adjustment Modal
  const [adjustingUser, setAdjustingUser] = useState<User | null>(null);
  const [adjustAmount, setAdjustAmount] = useState('');
  const [adjustReason, setAdjustReason] = useState('');
  const [adjustSubmitting, setAdjustSubmitting] = useState(false);
  const [adjustError, setAdjustError] = useState<string | null>(null);

  // Status Action Modal
  const [statusTargetUser, setStatusTargetUser] = useState<User | null>(null);
  const [statusActionType, setStatusActionType] = useState<'BLOCK' | 'TASK' | 'WITHDRAW' | null>(null);
  const [statusReason, setStatusReason] = useState('');
  const [statusSubmitting, setStatusSubmitting] = useState(false);

  useEffect(() => {
    fetchUsers();
  }, [statusFilter, sortOption]);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const res = await api.getUsers({
        search: search.trim() || undefined,
        status: statusFilter || undefined,
        sort: sortOption
      });
      setUsers(res.users);
      setTotal(res.total);
    } catch (err) {
      console.error('Failed to fetch users', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchUsers();
  };

  const openUserDetails = async (user: User) => {
    setSelectedUser(user);
    setLoadingDetails(true);
    try {
      const res = await api.getUserDetails(user.id);
      setUserDetails(res);
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingDetails(false);
    }
  };

  const handleAdjustPointsSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!adjustingUser) return;
    if (!adjustReason.trim()) {
      setAdjustError('A mandatory explanation/reason is required for auditing.');
      return;
    }
    const amt = parseFloat(adjustAmount);
    if (isNaN(amt) || amt === 0) {
      setAdjustError('Please enter a valid non-zero point value.');
      return;
    }

    setAdjustSubmitting(true);
    setAdjustError(null);
    try {
      await api.adjustUserPoints(adjustingUser.id, amt, adjustReason.trim());
      setAdjustingUser(null);
      setAdjustAmount('');
      setAdjustReason('');
      fetchUsers();
      if (selectedUser && selectedUser.id === adjustingUser.id) {
        openUserDetails(adjustingUser);
      }
    } catch (err: any) {
      setAdjustError(err.message || 'Failed to adjust points');
    } finally {
      setAdjustSubmitting(false);
    }
  };

  const handleToggleStatusConfirm = async () => {
    if (!statusTargetUser || !statusActionType) return;
    setStatusSubmitting(true);
    try {
      const updates: any = { reason: statusReason.trim() || 'Admin action' };
      if (statusActionType === 'BLOCK') {
        updates.isBlocked = !statusTargetUser.isBlocked;
      } else if (statusActionType === 'TASK') {
        updates.isTaskDisabled = !statusTargetUser.isTaskDisabled;
      } else if (statusActionType === 'WITHDRAW') {
        updates.isWithdrawDisabled = !statusTargetUser.isWithdrawDisabled;
      }

      await api.updateUserStatus(statusTargetUser.id, updates);
      setStatusTargetUser(null);
      setStatusActionType(null);
      setStatusReason('');
      fetchUsers();
      if (selectedUser && selectedUser.id === statusTargetUser.id) {
        openUserDetails(statusTargetUser);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setStatusSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
            User Management ({total})
          </h1>
          <p className="text-xs md:text-sm text-slate-400 mt-1">
            Search, inspect, adjust balances, and enforce role-based safety restrictions.
          </p>
        </div>

        <button
          onClick={fetchUsers}
          disabled={loading}
          className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition self-start"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          Refresh Users
        </button>
      </div>

      {/* Search and Filters Bar */}
      <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl flex flex-col md:flex-row gap-3 items-center justify-between">
        <form onSubmit={handleSearchSubmit} className="relative w-full md:w-96">
          <Search className="w-4 h-4 absolute left-3.5 top-3 text-slate-500" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by Name, Email, Phone, Ref, ID..."
            className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-10 pr-20 py-2 text-xs text-white placeholder:text-slate-500 focus:outline-none focus:border-purple-500 focus:ring-1 focus:ring-purple-500"
          />
          <button
            type="submit"
            className="absolute right-1.5 top-1.5 px-3 py-1 bg-purple-600 hover:bg-purple-500 text-white rounded-lg text-xs font-bold transition"
          >
            Search
          </button>
        </form>

        <div className="flex items-center gap-2 w-full md:w-auto overflow-x-auto">
          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-950 border border-slate-800 text-slate-300 rounded-xl px-3 py-2 text-xs focus:outline-none focus:border-purple-500"
          >
            <option value="">All Account Statuses</option>
            <option value="ACTIVE">Active & Verified</option>
            <option value="BLOCKED">Blocked / Suspended</option>
            <option value="TASK_DISABLED">Task Disabled</option>
            <option value="WITHDRAW_DISABLED">Withdraw Disabled</option>
          </select>

          {/* Sort Option */}
          <select
            value={sortOption}
            onChange={(e) => setSortOption(e.target.value)}
            className="bg-slate-950 border border-slate-800 text-slate-300 rounded-xl px-3 py-2 text-xs focus:outline-none focus:border-purple-500"
          >
            <option value="CREATED_DESC">Newest First</option>
            <option value="POINTS_DESC">Highest Points Balance</option>
            <option value="EARNED_DESC">Highest Total Earned</option>
          </select>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/80 text-slate-400 uppercase tracking-wider font-semibold border-b border-slate-800">
              <tr>
                <th className="py-3 px-4">User</th>
                <th className="py-3 px-4">Phone</th>
                <th className="py-3 px-4">Balance</th>
                <th className="py-3 px-4">Completed Tasks</th>
                <th className="py-3 px-4">Referral Code</th>
                <th className="py-3 px-4">Status & Access</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {loading ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <div className="inline-block w-6 h-6 border-2 border-purple-500/20 border-t-purple-500 rounded-full animate-spin"></div>
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-500">
                    No users matching criteria found.
                  </td>
                </tr>
              ) : (
                users.map(user => (
                  <tr key={user.id} className="hover:bg-slate-800/40 transition">
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2.5">
                        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-purple-500/20 to-indigo-500/20 border border-purple-500/30 text-purple-300 font-bold flex items-center justify-center text-xs shrink-0">
                          {user.name.charAt(0)}
                        </div>
                        <div>
                          <div className="font-bold text-white flex items-center gap-1.5">
                            <span>{user.name}</span>
                            <span className="text-[10px] font-mono text-slate-400 bg-slate-950 px-1.5 py-0.5 rounded border border-slate-800">{user.id}</span>
                          </div>
                          <div className="text-[11px] text-slate-400">{user.email}</div>
                        </div>
                      </div>
                    </td>
                    <td className="py-3 px-4 text-slate-300 font-mono text-[11px]">
                      {user.phone || 'N/A'}
                    </td>
                    <td className="py-3 px-4">
                      <div className="font-extrabold text-amber-300">{user.points} pts</div>
                      <div className="text-[10px] text-slate-400">Total: {user.totalEarned} pts</div>
                    </td>
                    <td className="py-3 px-4 text-slate-300">
                      <span className="font-bold text-purple-300">{user.completedQuizzesCount || 0}</span> quizzes
                      <div className="text-[10px] text-slate-500">Cycle: {user.currentCycleQuizzes || 0}/5</div>
                    </td>
                    <td className="py-3 px-4">
                      <span className="font-mono text-xs font-bold text-purple-400 bg-purple-500/10 px-2 py-0.5 rounded border border-purple-500/20">
                        {user.referralCode}
                      </span>
                      {user.referredBy && (
                        <div className="text-[10px] text-slate-500">by {user.referredBy}</div>
                      )}
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex flex-wrap gap-1">
                        {user.isBlocked ? (
                          <span className="bg-red-500/20 text-red-400 border border-red-500/30 px-1.5 py-0.5 rounded text-[10px] font-bold">
                            BLOCKED
                          </span>
                        ) : (
                          <span className="bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 px-1.5 py-0.5 rounded text-[10px] font-bold">
                            ACTIVE
                          </span>
                        )}
                        {user.isTaskDisabled && (
                          <span className="bg-amber-500/20 text-amber-400 border border-amber-500/30 px-1.5 py-0.5 rounded text-[10px]">
                            Task Locked
                          </span>
                        )}
                        {user.isWithdrawDisabled && (
                          <span className="bg-rose-500/20 text-rose-400 border border-rose-500/30 px-1.5 py-0.5 rounded text-[10px]">
                            Cashout Locked
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="py-3 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => openUserDetails(user)}
                          title="View Full Profile & Histories"
                          className="p-1.5 text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-lg transition"
                        >
                          <Eye className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => {
                            setAdjustingUser(user);
                            setAdjustAmount('');
                            setAdjustReason('');
                            setAdjustError(null);
                          }}
                          title="Adjust Points Balance"
                          className="p-1.5 text-amber-400 hover:text-amber-300 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/20 rounded-lg transition"
                        >
                          <Coins className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => {
                            setStatusTargetUser(user);
                            setStatusActionType('BLOCK');
                            setStatusReason('');
                          }}
                          title={user.isBlocked ? 'Unblock User' : 'Block User'}
                          className={`p-1.5 rounded-lg border transition ${
                            user.isBlocked 
                              ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20 hover:bg-emerald-500/20' 
                              : 'text-red-400 bg-red-500/10 border-red-500/20 hover:bg-red-500/20'
                          }`}
                        >
                          {user.isBlocked ? <Unlock className="w-3.5 h-3.5" /> : <Lock className="w-3.5 h-3.5" />}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* User Details Modal */}
      {selectedUser && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-3xl w-full max-h-[90vh] overflow-y-auto shadow-2xl p-6 relative">
            <button
              onClick={() => setSelectedUser(null)}
              className="absolute top-4 right-4 text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-3 mb-6">
              <div className="w-12 h-12 rounded-xl bg-purple-600/20 border border-purple-500/30 text-purple-400 flex items-center justify-center font-bold text-lg">
                {selectedUser.name.charAt(0)}
              </div>
              <div>
                <h2 className="text-xl font-bold text-white flex items-center gap-2">
                  {selectedUser.name}
                  <span className="text-xs font-mono text-slate-400 bg-slate-950 px-2 py-0.5 rounded border border-slate-800">
                    {selectedUser.id}
                  </span>
                </h2>
                <div className="text-xs text-slate-400 flex items-center gap-3 mt-0.5">
                  <span className="flex items-center gap-1"><Mail className="w-3 h-3" /> {selectedUser.email}</span>
                  <span className="flex items-center gap-1"><Phone className="w-3 h-3" /> {selectedUser.phone || 'N/A'}</span>
                </div>
              </div>
            </div>

            {/* Quick Metrics Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-6 text-xs">
              <div className="bg-slate-950 border border-slate-800 p-3 rounded-xl">
                <div className="text-slate-400">Current Points</div>
                <div className="text-lg font-extrabold text-amber-300">{selectedUser.points} pts</div>
              </div>
              <div className="bg-slate-950 border border-slate-800 p-3 rounded-xl">
                <div className="text-slate-400">Total Earned</div>
                <div className="text-lg font-extrabold text-emerald-400">{selectedUser.totalEarned} pts</div>
              </div>
              <div className="bg-slate-950 border border-slate-800 p-3 rounded-xl">
                <div className="text-slate-400">Total Withdrawn</div>
                <div className="text-lg font-extrabold text-slate-200">{selectedUser.totalWithdrawn} pts</div>
              </div>
              <div className="bg-slate-950 border border-slate-800 p-3 rounded-xl">
                <div className="text-slate-400">Quizzes Done</div>
                <div className="text-lg font-extrabold text-purple-400">{selectedUser.completedQuizzesCount}</div>
              </div>
            </div>

            {/* Restrictions & Controls */}
            <div className="bg-slate-950 border border-slate-800 p-4 rounded-xl mb-6">
              <div className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-3">
                Security & Access Toggles
              </div>
              <div className="flex flex-wrap gap-2">
                <button
                  onClick={() => {
                    setStatusTargetUser(selectedUser);
                    setStatusActionType('BLOCK');
                  }}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold flex items-center gap-1.5 transition ${
                    selectedUser.isBlocked ? 'bg-red-600 text-white' : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                  }`}
                >
                  <Lock className="w-3.5 h-3.5" />
                  {selectedUser.isBlocked ? 'Account Blocked' : 'Block User'}
                </button>
                <button
                  onClick={() => {
                    setStatusTargetUser(selectedUser);
                    setStatusActionType('TASK');
                  }}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold flex items-center gap-1.5 transition ${
                    selectedUser.isTaskDisabled ? 'bg-amber-600 text-white' : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                  }`}
                >
                  <ShieldAlert className="w-3.5 h-3.5" />
                  {selectedUser.isTaskDisabled ? 'Tasks Disabled' : 'Disable Tasks'}
                </button>
                <button
                  onClick={() => {
                    setStatusTargetUser(selectedUser);
                    setStatusActionType('WITHDRAW');
                  }}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold flex items-center gap-1.5 transition ${
                    selectedUser.isWithdrawDisabled ? 'bg-rose-600 text-white' : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                  }`}
                >
                  <Coins className="w-3.5 h-3.5" />
                  {selectedUser.isWithdrawDisabled ? 'Withdrawals Disabled' : 'Disable Withdrawals'}
                </button>
                <button
                  onClick={() => {
                    setAdjustingUser(selectedUser);
                    setAdjustAmount('');
                    setAdjustReason('');
                    setAdjustError(null);
                  }}
                  className="px-3 py-1.5 rounded-lg text-xs font-bold bg-amber-500/20 text-amber-300 hover:bg-amber-500/30 border border-amber-500/30 flex items-center gap-1.5"
                >
                  <Edit3 className="w-3.5 h-3.5" />
                  Manual Point Adjustment
                </button>
              </div>
            </div>

            {/* Transactions & Cashouts tabs */}
            <div className="space-y-4">
              <h3 className="text-sm font-bold text-white">Recent Transactions & Rewarded Ads</h3>
              <div className="space-y-2 max-h-48 overflow-y-auto">
                {loadingDetails ? (
                  <div className="text-center py-4 text-xs text-slate-500">Loading ledger...</div>
                ) : userDetails?.transactions.length === 0 ? (
                  <div className="text-center py-4 text-xs text-slate-500">No transactions recorded yet.</div>
                ) : (
                  userDetails?.transactions.map(tx => (
                    <div key={tx.id} className="p-2.5 rounded-lg bg-slate-950 border border-slate-800/80 flex items-center justify-between text-xs">
                      <div>
                        <div className="font-bold text-slate-200">{tx.title}</div>
                        <div className="text-[10px] text-slate-400">{tx.description}</div>
                      </div>
                      <div className="text-right">
                        <div className={`font-bold ${tx.points >= 0 ? 'text-amber-300' : 'text-rose-400'}`}>
                          {tx.points >= 0 ? '+' : ''}{tx.points} pts
                        </div>
                        <div className="text-[9px] text-slate-500">
                          {new Date(tx.timestamp).toLocaleString()}
                        </div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Manual Points Adjustment Modal */}
      {adjustingUser && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-bold text-white flex items-center gap-2">
                <Coins className="w-5 h-5 text-amber-400" />
                Adjust Points for {adjustingUser.name}
              </h3>
              <button onClick={() => setAdjustingUser(null)} className="text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="mb-4 p-3 bg-slate-950 border border-slate-800 rounded-xl text-xs flex justify-between">
              <span className="text-slate-400">Current Balance:</span>
              <span className="font-bold text-amber-300">{adjustingUser.points} points</span>
            </div>

            {adjustError && (
              <div className="mb-4 p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-start gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{adjustError}</span>
              </div>
            )}

            <form onSubmit={handleAdjustPointsSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1">
                  Point Amount (+ to credit, - to deduct)
                </label>
                <input
                  type="number"
                  step="0.1"
                  required
                  value={adjustAmount}
                  onChange={(e) => setAdjustAmount(e.target.value)}
                  placeholder="e.g. 10 or -5"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-amber-500"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1">
                  Mandatory Audit Reason / Note *
                </label>
                <textarea
                  required
                  rows={3}
                  value={adjustReason}
                  onChange={(e) => setAdjustReason(e.target.value)}
                  placeholder="Explain why this manual balance adjustment is made (recorded in immutable admin audit log)..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-xs text-white focus:outline-none focus:border-amber-500"
                />
              </div>

              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setAdjustingUser(null)}
                  className="flex-1 px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-bold transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={adjustSubmitting}
                  className="flex-1 px-4 py-2.5 bg-amber-500 hover:bg-amber-600 text-slate-950 rounded-xl text-xs font-bold transition disabled:opacity-50"
                >
                  {adjustSubmitting ? 'Applying...' : 'Confirm Adjustment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Toggle Status Confirmation Modal */}
      {statusTargetUser && statusActionType && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <h3 className="text-lg font-bold text-white mb-2 flex items-center gap-2">
              <ShieldAlert className="w-5 h-5 text-amber-400" />
              Update Account Status
            </h3>
            <p className="text-xs text-slate-400 mb-4">
              You are modifying access controls for <strong className="text-slate-200">{statusTargetUser.name}</strong> ({statusTargetUser.id}).
            </p>

            <div className="mb-4">
              <label className="block text-xs font-bold text-slate-300 mb-1">
                Reason / Note (Optional)
              </label>
              <input
                type="text"
                value={statusReason}
                onChange={(e) => setStatusReason(e.target.value)}
                placeholder="e.g. Anti-fraud violation, user request..."
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-xs text-white focus:outline-none focus:border-purple-500"
              />
            </div>

            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => {
                  setStatusTargetUser(null);
                  setStatusActionType(null);
                }}
                className="flex-1 px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-bold"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleToggleStatusConfirm}
                disabled={statusSubmitting}
                className="flex-1 px-4 py-2.5 bg-purple-600 hover:bg-purple-500 text-white rounded-xl text-xs font-bold disabled:opacity-50"
              >
                {statusSubmitting ? 'Updating...' : 'Confirm Action'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
