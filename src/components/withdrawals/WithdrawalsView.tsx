import React, { useState, useEffect } from 'react';
import { 
  Wallet, 
  Search, 
  Filter, 
  CheckCircle2, 
  Clock, 
  XCircle, 
  AlertCircle, 
  RefreshCw, 
  ArrowUpRight, 
  X,
  CreditCard,
  Building,
  User,
  Coins,
  Send
} from 'lucide-react';
import { api } from '../../api/client';
import { WithdrawalRequest } from '../../types';

export const WithdrawalsView: React.FC = () => {
  const [withdrawals, setWithdrawals] = useState<WithdrawalRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [methodFilter, setMethodFilter] = useState('');
  const [search, setSearch] = useState('');

  // Processing Modal
  const [selectedWithdrawal, setSelectedWithdrawal] = useState<WithdrawalRequest | null>(null);
  const [newStatus, setNewStatus] = useState<string>('PAID');
  const [adminNote, setAdminNote] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  useEffect(() => {
    fetchWithdrawals();
  }, [statusFilter, methodFilter]);

  const fetchWithdrawals = async () => {
    try {
      setLoading(true);
      const res = await api.getWithdrawals({
        status: statusFilter || undefined,
        method: methodFilter || undefined,
        search: search.trim() || undefined
      });
      setWithdrawals(res.withdrawals);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchWithdrawals();
  };

  const openProcessModal = (w: WithdrawalRequest) => {
    setSelectedWithdrawal(w);
    setNewStatus(w.status === 'PENDING' ? 'PROCESSING' : w.status === 'PROCESSING' ? 'PAID' : w.status);
    setAdminNote(w.adminNote || '');
    setErrorMsg(null);
  };

  const handleProcessSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedWithdrawal) return;

    setSubmitting(true);
    setErrorMsg(null);

    try {
      await api.updateWithdrawal(selectedWithdrawal.id, newStatus, adminNote.trim());
      setSelectedWithdrawal(null);
      fetchWithdrawals();
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to update withdrawal');
    } finally {
      setSubmitting(false);
    }
  };

  const pendingCount = withdrawals.filter(w => w.status === 'PENDING').length;
  const paidCount = withdrawals.filter(w => w.status === 'PAID').length;
  const rejectedCount = withdrawals.filter(w => w.status === 'REJECTED').length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">
            Withdrawal Management
          </h1>
          <p className="text-xs md:text-sm text-slate-400 mt-1">
            Review user cashout requests, record merchant transaction IDs, and process payouts.
          </p>
        </div>

        <button
          onClick={fetchWithdrawals}
          disabled={loading}
          className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition self-start"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          Refresh Payouts
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="flex flex-wrap gap-2 text-xs">
        <button
          onClick={() => setStatusFilter('')}
          className={`px-3.5 py-2 rounded-xl font-bold transition ${
            statusFilter === '' ? 'bg-purple-600 text-white shadow-lg shadow-purple-600/30' : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-white'
          }`}
        >
          All Requests ({withdrawals.length})
        </button>
        <button
          onClick={() => setStatusFilter('PENDING')}
          className={`px-3.5 py-2 rounded-xl font-bold transition flex items-center gap-1.5 ${
            statusFilter === 'PENDING' ? 'bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/30' : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-amber-400'
          }`}
        >
          <Clock className="w-3.5 h-3.5" />
          Pending ({pendingCount})
        </button>
        <button
          onClick={() => setStatusFilter('PROCESSING')}
          className={`px-3.5 py-2 rounded-xl font-bold transition ${
            statusFilter === 'PROCESSING' ? 'bg-blue-600 text-white shadow-lg shadow-blue-600/30' : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-white'
          }`}
        >
          Processing
        </button>
        <button
          onClick={() => setStatusFilter('PAID')}
          className={`px-3.5 py-2 rounded-xl font-bold transition flex items-center gap-1.5 ${
            statusFilter === 'PAID' ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-600/30' : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-emerald-400'
          }`}
        >
          <CheckCircle2 className="w-3.5 h-3.5" />
          Completed & Paid ({paidCount})
        </button>
        <button
          onClick={() => setStatusFilter('REJECTED')}
          className={`px-3.5 py-2 rounded-xl font-bold transition flex items-center gap-1.5 ${
            statusFilter === 'REJECTED' ? 'bg-red-600 text-white shadow-lg shadow-red-600/30' : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-red-400'
          }`}
        >
          <XCircle className="w-3.5 h-3.5" />
          Rejected ({rejectedCount})
        </button>
      </div>

      {/* Search and Method Filter */}
      <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl flex flex-col sm:flex-row gap-3 items-center justify-between">
        <form onSubmit={handleSearchSubmit} className="relative w-full sm:w-80">
          <Search className="w-4 h-4 absolute left-3.5 top-3 text-slate-500" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by User, Email, Account No..."
            className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-10 pr-20 py-2 text-xs text-white placeholder:text-slate-500 focus:outline-none focus:border-purple-500"
          />
          <button
            type="submit"
            className="absolute right-1.5 top-1.5 px-3 py-1 bg-purple-600 hover:bg-purple-500 text-white rounded-lg text-xs font-bold transition"
          >
            Search
          </button>
        </form>

        <select
          value={methodFilter}
          onChange={(e) => setMethodFilter(e.target.value)}
          className="bg-slate-950 border border-slate-800 text-slate-300 rounded-xl px-3 py-2 text-xs focus:outline-none focus:border-purple-500 w-full sm:w-auto"
        >
          <option value="">All Payment Methods</option>
          <option value="BKASH">bKash (Bangladesh)</option>
          <option value="NAGAD">Nagad (Bangladesh)</option>
          <option value="USDT_BEP20">USDT (BEP20 BSC)</option>
        </select>
      </div>

      {/* Withdrawals Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/80 text-slate-400 uppercase tracking-wider font-semibold border-b border-slate-800">
              <tr>
                <th className="py-3 px-4">Request ID</th>
                <th className="py-3 px-4">User</th>
                <th className="py-3 px-4">Method & Account</th>
                <th className="py-3 px-4">Points</th>
                <th className="py-3 px-4">Fiat Payout</th>
                <th className="py-3 px-4">Date</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {loading ? (
                <tr>
                  <td colSpan={8} className="text-center py-12 text-slate-400">
                    <div className="inline-block w-6 h-6 border-2 border-purple-500/20 border-t-purple-500 rounded-full animate-spin"></div>
                  </td>
                </tr>
              ) : withdrawals.length === 0 ? (
                <tr>
                  <td colSpan={8} className="text-center py-12 text-slate-500">
                    No cashout requests found.
                  </td>
                </tr>
              ) : (
                withdrawals.map(w => (
                  <tr key={w.id} className="hover:bg-slate-800/40 transition">
                    <td className="py-3 px-4 font-mono font-bold text-slate-300">
                      {w.id}
                    </td>
                    <td className="py-3 px-4">
                      <div className="font-bold text-white">{w.userName}</div>
                      <div className="text-[10px] text-slate-400">{w.userEmail}</div>
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-1.5 mb-0.5">
                        <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                          w.method === 'BKASH' 
                            ? 'bg-pink-500/20 text-pink-400 border border-pink-500/30' 
                            : w.method === 'NAGAD'
                            ? 'bg-orange-500/20 text-orange-400 border border-orange-500/30'
                            : 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                        }`}>
                          {w.method}
                        </span>
                      </div>
                      <div className="font-mono text-[11px] text-slate-300">{w.accountInfo}</div>
                    </td>
                    <td className="py-3 px-4 font-bold text-amber-300">
                      {w.points} pts
                    </td>
                    <td className="py-3 px-4 font-bold text-emerald-400 text-sm">
                      {w.currencySymbol}{w.amountCurrency}
                    </td>
                    <td className="py-3 px-4 text-slate-400 text-[11px]">
                      {new Date(w.requestDate).toLocaleDateString()}
                      <div className="text-[9px] text-slate-500">{new Date(w.requestDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div>
                    </td>
                    <td className="py-3 px-4">
                      <span className={`px-2.5 py-1 rounded-full text-[10px] font-bold border ${
                        w.status === 'PAID'
                          ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                          : w.status === 'PENDING'
                          ? 'bg-amber-500/20 text-amber-300 border-amber-500/30 animate-pulse'
                          : w.status === 'PROCESSING' || w.status === 'APPROVED'
                          ? 'bg-blue-500/20 text-blue-300 border-blue-500/30'
                          : 'bg-red-500/20 text-red-400 border-red-500/30'
                      }`}>
                        {w.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      <button
                        onClick={() => openProcessModal(w)}
                        className="px-3 py-1.5 bg-slate-800 hover:bg-purple-600 text-slate-300 hover:text-white rounded-xl text-xs font-bold transition"
                      >
                        Process
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Process Modal */}
      {selectedWithdrawal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-bold text-white flex items-center gap-2">
                <Wallet className="w-5 h-5 text-purple-400" />
                Process Cashout {selectedWithdrawal.id}
              </h3>
              <button onClick={() => setSelectedWithdrawal(null)} className="text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Request Summary */}
            <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5 mb-4 text-xs space-y-1.5">
              <div className="flex justify-between">
                <span className="text-slate-400">Recipient:</span>
                <span className="font-bold text-white">{selectedWithdrawal.userName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Payment Gateway:</span>
                <span className="font-bold text-purple-300">{selectedWithdrawal.method}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Account / Wallet:</span>
                <span className="font-mono font-bold text-emerald-400">{selectedWithdrawal.accountInfo}</span>
              </div>
              <div className="flex justify-between pt-1 border-t border-slate-800">
                <span className="text-slate-400">Cashout Amount:</span>
                <span className="font-bold text-emerald-400 text-sm">{selectedWithdrawal.currencySymbol}{selectedWithdrawal.amountCurrency} ({selectedWithdrawal.points} pts)</span>
              </div>
            </div>

            {errorMsg && (
              <div className="mb-4 p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-start gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{errorMsg}</span>
              </div>
            )}

            <form onSubmit={handleProcessSubmit} className="space-y-4 text-xs">
              <div>
                <label className="block font-bold text-slate-300 mb-1">
                  Change Status
                </label>
                <select
                  value={newStatus}
                  onChange={(e) => setNewStatus(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-purple-500"
                >
                  <option value="PENDING">PENDING</option>
                  <option value="PROCESSING">PROCESSING</option>
                  <option value="APPROVED">APPROVED</option>
                  <option value="PAID">PAID (Payout Dispatched)</option>
                  <option value="REJECTED">REJECTED (Auto-Refund Points to User)</option>
                </select>
              </div>

              <div>
                <label className="block font-bold text-slate-300 mb-1">
                  Transaction Note / Reference ID (TrxID)
                </label>
                <input
                  type="text"
                  value={adminNote}
                  onChange={(e) => setAdminNote(e.target.value)}
                  placeholder="e.g. bKash Merchant TrxID: BK7829103"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-purple-500"
                />
              </div>

              {newStatus === 'REJECTED' && (
                <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-xl text-[11px] text-red-300">
                  ⚠️ <strong>Automatic Refund Notice:</strong> Marking this request as REJECTED will automatically refund <strong>{selectedWithdrawal.points} points</strong> back to the user's available balance and write a refund ledger entry.
                </div>
              )}

              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setSelectedWithdrawal(null)}
                  className="flex-1 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl font-bold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="flex-1 py-2.5 bg-purple-600 hover:bg-purple-500 text-white rounded-xl font-bold transition disabled:opacity-50"
                >
                  {submitting ? 'Saving...' : 'Update Cashout'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
