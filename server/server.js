import fs from 'fs';
import express from 'express';
import cors from 'cors';
import path from 'path';
import { fileURLToPath } from 'url';
import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs';
import { createServer as createViteServer } from 'vite';
import { db } from './db.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const JWT_SECRET = process.env.JWT_SECRET || 'adspay_super_secret_jwt_key_2026_production';
const PORT = process.env.PORT || 3000;

const app = express();

app.use(cors());
app.use(express.json());

// --- Authentication Middleware ---
function verifyAdmin(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Unauthorized: Admin authentication token required.' });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    if (!decoded || !decoded.adminId) {
      return res.status(403).json({ error: 'Forbidden: Invalid admin token payload.' });
    }
    const adminUsers = db.get('adminUsers');
    const admin = adminUsers.find(a => a.id === decoded.adminId);
    if (!admin) {
      return res.status(403).json({ error: 'Forbidden: Admin account not found or removed.' });
    }
    req.admin = admin;
    next();
  } catch (err) {
    return res.status(401).json({ error: 'Unauthorized: Session expired or invalid token.' });
  }
}

// ==========================================
// ADMIN AUTHENTICATION ROUTES
// ==========================================

// Check if any admin account exists (for setup wizard)
app.get('/api/admin/auth/status', (req, res) => {
  const adminUsers = db.get('adminUsers');
  res.json({
    hasAdmin: adminUsers.length > 0,
    count: adminUsers.length
  });
});

// Setup master admin if none exists
app.post('/api/admin/auth/setup', (req, res) => {
  const adminUsers = db.get('adminUsers');
  if (adminUsers.length > 0) {
    return res.status(400).json({ error: 'Admin setup already completed. Please log in.' });
  }

  const { name, email, password } = req.body;
  if (!name || !email || !password || password.length < 6) {
    return res.status(400).json({ error: 'Please provide valid name, email, and password (min 6 chars).' });
  }

  const salt = bcrypt.genSaltSync(10);
  const passwordHash = bcrypt.hashSync(password, salt);

  const newAdmin = {
    id: 'ADMIN-' + Math.random().toString(36).substring(2, 7).toUpperCase(),
    name: name.trim(),
    email: email.trim().toLowerCase(),
    passwordHash,
    role: 'SUPER_ADMIN',
    createdAt: Date.now(),
    lastLoginAt: Date.now()
  };

  db.set('adminUsers', [newAdmin]);
  db.logAudit(newAdmin.id, newAdmin.email, 'MASTER_ADMIN_CREATED', 'ADMIN', newAdmin.id, '', newAdmin.email, 'Initial Admin Account Setup');

  const token = jwt.sign({ adminId: newAdmin.id, email: newAdmin.email, role: newAdmin.role }, JWT_SECRET, { expiresIn: '24h' });

  res.json({
    message: 'Master Admin account initialized successfully!',
    token,
    admin: {
      id: newAdmin.id,
      name: newAdmin.name,
      email: newAdmin.email,
      role: newAdmin.role
    }
  });
});

// Admin Login
app.post('/api/admin/auth/login', (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ error: 'Email and password are required.' });
  }

  const adminUsers = db.get('adminUsers');
  const admin = adminUsers.find(a => a.email.toLowerCase() === email.trim().toLowerCase());

  if (!admin) {
    return res.status(401).json({ error: 'Invalid admin credentials.' });
  }

  const isPasswordValid = bcrypt.compareSync(password, admin.passwordHash);
  if (!isPasswordValid) {
    return res.status(401).json({ error: 'Invalid admin credentials.' });
  }

  // Update last login
  admin.lastLoginAt = Date.now();
  db.set('adminUsers', adminUsers);

  db.logAudit(admin.id, admin.email, 'ADMIN_LOGIN', 'AUTH', admin.id, '', 'Logged In', 'Successful web admin panel authentication');

  const token = jwt.sign({ adminId: admin.id, email: admin.email, role: admin.role }, JWT_SECRET, { expiresIn: '24h' });

  res.json({
    token,
    admin: {
      id: admin.id,
      name: admin.name,
      email: admin.email,
      role: admin.role,
      lastLoginAt: admin.lastLoginAt
    }
  });
});

// Current Admin Info
app.get('/api/admin/auth/me', verifyAdmin, (req, res) => {
  res.json({
    admin: {
      id: req.admin.id,
      name: req.admin.name,
      email: req.admin.email,
      role: req.admin.role,
      lastLoginAt: req.admin.lastLoginAt,
      createdAt: req.admin.createdAt
    }
  });
});

// Change Admin Password
app.post('/api/admin/auth/change-password', verifyAdmin, (req, res) => {
  const { currentPassword, newPassword } = req.body;
  if (!currentPassword || !newPassword || newPassword.length < 6) {
    return res.status(400).json({ error: 'New password must be at least 6 characters.' });
  }

  const isPasswordValid = bcrypt.compareSync(currentPassword, req.admin.passwordHash);
  if (!isPasswordValid) {
    return res.status(400).json({ error: 'Current password does not match.' });
  }

  const salt = bcrypt.genSaltSync(10);
  req.admin.passwordHash = bcrypt.hashSync(newPassword, salt);

  const adminUsers = db.get('adminUsers').map(a => a.id === req.admin.id ? req.admin : a);
  db.set('adminUsers', adminUsers);

  db.logAudit(req.admin.id, req.admin.email, 'CHANGE_PASSWORD', 'ADMIN', req.admin.id, '', 'Password Changed', 'Security credential update');

  res.json({ message: 'Password updated successfully!' });
});

// ==========================================
// ADMIN DASHBOARD & STATISTICS
// ==========================================
app.get('/api/admin/dashboard', verifyAdmin, (req, res) => {
  const users = db.get('users');
  const withdrawals = db.get('withdrawals');
  const quizzes = db.get('quizzes');
  const transactions = db.get('rewardTransactions');
  const settings = db.getSettings();

  const totalUsers = users.length;
  const blockedUsers = users.filter(u => u.isBlocked).length;
  const activeUsers = users.filter(u => !u.isBlocked && (Date.now() - (u.lastActiveAt || u.createdAt)) < 86400000 * 3).length;

  const totalPointsIssued = users.reduce((acc, u) => acc + (u.totalEarned || 0), 0);
  const totalPointsWithdrawn = users.reduce((acc, u) => acc + (u.totalWithdrawn || 0), 0);
  const currentTotalUserBalance = users.reduce((acc, u) => acc + (u.points || 0), 0);

  const pendingWithdrawals = withdrawals.filter(w => w.status === 'PENDING');
  const completedWithdrawals = withdrawals.filter(w => w.status === 'PAID');
  const rejectedWithdrawals = withdrawals.filter(w => w.status === 'REJECTED');
  const processingWithdrawals = withdrawals.filter(w => w.status === 'PROCESSING' || w.status === 'APPROVED');

  const pendingAmount = pendingWithdrawals.reduce((acc, w) => acc + w.amountCurrency, 0);
  const paidAmount = completedWithdrawals.reduce((acc, w) => acc + w.amountCurrency, 0);

  // Referral statistics
  const referredUsersCount = users.filter(u => Boolean(u.referredBy)).length;
  const totalReferralCommissionPaid = transactions
    .filter(t => t.type === 'REFERRAL_BONUS')
    .reduce((acc, t) => acc + t.points, 0);

  // Task & ad statistics
  const totalTasksCompleted = users.reduce((acc, u) => acc + (u.completedQuizzesCount || 0), 0);
  const totalRewardCyclesCompleted = transactions.filter(t => t.type === 'REWARD_CYCLE').length;

  // Chart data: Last 7 days activity
  const now = Date.now();
  const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const weeklyTrend = [];

  for (let i = 6; i >= 0; i--) {
    const d = new Date(now - i * 86400000);
    const dayLabel = dayNames[d.getDay()];
    const startOfDay = new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
    const endOfDay = startOfDay + 86400000;

    const dayTasks = transactions.filter(t => t.type === 'REWARD_CYCLE' && t.timestamp >= startOfDay && t.timestamp < endOfDay).length;
    const dayUsers = users.filter(u => u.createdAt >= startOfDay && u.createdAt < endOfDay).length;
    const dayWithdrawals = withdrawals.filter(w => w.requestDate >= startOfDay && w.requestDate < endOfDay).reduce((acc, w) => acc + w.amountCurrency, 0);

    weeklyTrend.push({
      name: dayLabel,
      rewards: dayTasks,
      signups: dayUsers,
      payouts: dayWithdrawals
    });
  }

  // Method breakdown
  const methodStats = {
    BKASH: withdrawals.filter(w => w.method === 'BKASH').length,
    NAGAD: withdrawals.filter(w => w.method === 'NAGAD').length,
    USDT_BEP20: withdrawals.filter(w => w.method === 'USDT_BEP20').length
  };

  res.json({
    metrics: {
      totalUsers,
      activeUsers,
      blockedUsers,
      totalPointsIssued: Number(totalPointsIssued.toFixed(2)),
      totalPointsWithdrawn: Number(totalPointsWithdrawn.toFixed(2)),
      currentTotalUserBalance: Number(currentTotalUserBalance.toFixed(2)),
      totalTasksCompleted,
      totalRewardCyclesCompleted,
      activeQuizzesCount: quizzes.filter(q => q.isActive).length,
      totalQuizzesCount: quizzes.length,
      startIoAppId: settings.startIoAppId,
      isMaintenanceMode: settings.isMaintenanceMode,
      pendingWithdrawalsCount: pendingWithdrawals.length,
      pendingWithdrawalsAmount: Number(pendingAmount.toFixed(2)),
      completedWithdrawalsCount: completedWithdrawals.length,
      completedWithdrawalsAmount: Number(paidAmount.toFixed(2)),
      rejectedWithdrawalsCount: rejectedWithdrawals.length,
      processingWithdrawalsCount: processingWithdrawals.length,
      referredUsersCount,
      totalReferralCommissionPaid: Number(totalReferralCommissionPaid.toFixed(2))
    },
    weeklyTrend,
    methodStats,
    recentWithdrawals: withdrawals.slice(0, 5),
    recentTransactions: transactions.slice(0, 8),
    recentUsers: [...users].sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0)).slice(0, 5)
  });
});

// ==========================================
// USER MANAGEMENT
// ==========================================

// Get list of users with search and filter
app.get('/api/admin/users', verifyAdmin, (req, res) => {
  let users = [...db.get('users')];
  const { search, status, sort } = req.query;

  if (search) {
    const q = String(search).toLowerCase().trim();
    users = users.filter(u =>
      (u.name && u.name.toLowerCase().includes(q)) ||
      (u.email && u.email.toLowerCase().includes(q)) ||
      (u.phone && u.phone.includes(q)) ||
      (u.referralCode && u.referralCode.toLowerCase().includes(q)) ||
      (u.id && u.id.toLowerCase().includes(q))
    );
  }

  if (status === 'BLOCKED') {
    users = users.filter(u => u.isBlocked);
  } else if (status === 'ACTIVE') {
    users = users.filter(u => !u.isBlocked);
  } else if (status === 'TASK_DISABLED') {
    users = users.filter(u => u.isTaskDisabled);
  } else if (status === 'WITHDRAW_DISABLED') {
    users = users.filter(u => u.isWithdrawDisabled);
  }

  // Sort
  if (sort === 'POINTS_DESC') {
    users.sort((a, b) => (b.points || 0) - (a.points || 0));
  } else if (sort === 'EARNED_DESC') {
    users.sort((a, b) => (b.totalEarned || 0) - (a.totalEarned || 0));
  } else {
    users.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
  }

  res.json({
    total: users.length,
    users
  });
});

// Get single user detailed view (with histories)
app.get('/api/admin/users/:id', verifyAdmin, (req, res) => {
  const user = db.get('users').find(u => u.id === req.params.id);
  if (!user) {
    return res.status(404).json({ error: 'User not found.' });
  }

  const userTransactions = db.get('rewardTransactions').filter(t => t.userId === user.id);
  const userWithdrawals = db.get('withdrawals').filter(w => w.userId === user.id);
  const referredUsers = db.get('users').filter(u => u.referredBy === user.referralCode);
  const attempts = db.get('taskAttempts').filter(a => a.userId === user.id);

  res.json({
    user,
    transactions: userTransactions,
    withdrawals: userWithdrawals,
    referredUsers,
    taskAttempts: attempts
  });
});

// Toggle user restrictions (block, task access, withdraw access, referral access)
app.patch('/api/admin/users/:id/status', verifyAdmin, (req, res) => {
  const users = db.get('users');
  const userIndex = users.findIndex(u => u.id === req.params.id);
  if (userIndex === -1) {
    return res.status(404).json({ error: 'User not found.' });
  }

  const target = users[userIndex];
  const { isBlocked, isTaskDisabled, isWithdrawDisabled, isReferralDisabled, reason } = req.body;

  const previousState = `Blocked:${target.isBlocked}, TaskOff:${target.isTaskDisabled}, WthOff:${target.isWithdrawDisabled}`;

  if (typeof isBlocked === 'boolean') target.isBlocked = isBlocked;
  if (typeof isTaskDisabled === 'boolean') target.isTaskDisabled = isTaskDisabled;
  if (typeof isWithdrawDisabled === 'boolean') target.isWithdrawDisabled = isWithdrawDisabled;
  if (typeof isReferralDisabled === 'boolean') target.isReferralDisabled = isReferralDisabled;

  users[userIndex] = target;
  db.set('users', users);

  const newState = `Blocked:${target.isBlocked}, TaskOff:${target.isTaskDisabled}, WthOff:${target.isWithdrawDisabled}`;
  db.logAudit(req.admin.id, req.admin.email, 'TOGGLE_USER_PERMISSIONS', 'USER', target.id, previousState, newState, reason || 'Admin restriction toggle');

  res.json({ message: 'User status updated successfully.', user: target });
});

// Manual point adjustment (Requires Reason)
app.post('/api/admin/users/:id/adjust-points', verifyAdmin, (req, res) => {
  const { amount, reason } = req.body;
  if (!reason || reason.trim().length === 0) {
    return res.status(400).json({ error: 'Mandatory reason is required for manual point adjustment.' });
  }

  const numAmount = Number(amount);
  if (isNaN(numAmount) || numAmount === 0) {
    return res.status(400).json({ error: 'Please enter a non-zero number for point adjustment.' });
  }

  const users = db.get('users');
  const userIndex = users.findIndex(u => u.id === req.params.id);
  if (userIndex === -1) {
    return res.status(404).json({ error: 'User not found.' });
  }

  const target = users[userIndex];
  const oldPoints = target.points;
  const newPoints = Math.max(0, Number((oldPoints + numAmount).toFixed(2)));

  target.points = newPoints;
  if (numAmount > 0) {
    target.totalEarned = Number((target.totalEarned + numAmount).toFixed(2));
  }
  users[userIndex] = target;
  db.set('users', users);

  // Record transaction
  const transactions = db.get('rewardTransactions');
  const tx = {
    id: 'tx_man_' + Math.random().toString(36).substring(2, 8),
    userId: target.id,
    points: numAmount,
    type: 'MANUAL_ADJUSTMENT',
    title: numAmount >= 0 ? 'Admin Point Credit' : 'Admin Point Deduction',
    description: `Manual adjustment by ${req.admin.email}: ${reason.trim()}`,
    timestamp: Date.now(),
    referenceId: req.admin.id
  };
  transactions.unshift(tx);
  db.set('rewardTransactions', transactions);

  // Log audit
  db.logAudit(
    req.admin.id,
    req.admin.email,
    'ADJUST_POINTS',
    'USER',
    target.id,
    `${oldPoints} pts`,
    `${newPoints} pts (${numAmount >= 0 ? '+' : ''}${numAmount})`,
    reason.trim()
  );

  res.json({ message: 'Points adjusted successfully.', user: target, transaction: tx });
});

// ==========================================
// QUIZ & TASK MANAGEMENT
// ==========================================

// List quizzes
app.get('/api/admin/quizzes', verifyAdmin, (req, res) => {
  const quizzes = db.get('quizzes');
  res.json({ quizzes });
});

// Create quiz
app.post('/api/admin/quizzes', verifyAdmin, (req, res) => {
  const { question, options, correctOptionIndex, timerSeconds, category, order, isActive } = req.body;
  if (!question || !Array.isArray(options) || options.length < 2) {
    return res.status(400).json({ error: 'Please provide a valid question and at least 2 options.' });
  }

  const quizzes = db.get('quizzes');
  const newQuiz = {
    id: 'q_' + Math.random().toString(36).substring(2, 8),
    question: question.trim(),
    options: options.map(o => String(o).trim()),
    correctOptionIndex: Number(correctOptionIndex) || 0,
    timerSeconds: Number(timerSeconds) || 10,
    category: category ? category.trim() : 'General',
    isActive: isActive !== false,
    order: Number(order) || (quizzes.length + 1)
  };

  quizzes.push(newQuiz);
  db.set('quizzes', quizzes);

  db.logAudit(req.admin.id, req.admin.email, 'ADD_QUIZ', 'QUIZ', newQuiz.id, '', newQuiz.question, 'Added new task quiz');
  res.json({ message: 'Quiz created successfully!', quiz: newQuiz });
});

// Update quiz
app.put('/api/admin/quizzes/:id', verifyAdmin, (req, res) => {
  const quizzes = db.get('quizzes');
  const index = quizzes.findIndex(q => q.id === req.params.id);
  if (index === -1) {
    return res.status(404).json({ error: 'Quiz not found.' });
  }

  const { question, options, correctOptionIndex, timerSeconds, category, order, isActive } = req.body;
  const oldQuestion = quizzes[index].question;

  quizzes[index] = {
    ...quizzes[index],
    question: question ? question.trim() : quizzes[index].question,
    options: Array.isArray(options) ? options.map(o => String(o).trim()) : quizzes[index].options,
    correctOptionIndex: typeof correctOptionIndex === 'number' ? correctOptionIndex : quizzes[index].correctOptionIndex,
    timerSeconds: Number(timerSeconds) || quizzes[index].timerSeconds,
    category: category ? category.trim() : quizzes[index].category,
    order: typeof order === 'number' ? order : quizzes[index].order,
    isActive: typeof isActive === 'boolean' ? isActive : quizzes[index].isActive
  };

  db.set('quizzes', quizzes);
  db.logAudit(req.admin.id, req.admin.email, 'UPDATE_QUIZ', 'QUIZ', req.params.id, oldQuestion, quizzes[index].question, 'Updated quiz details');

  res.json({ message: 'Quiz updated successfully!', quiz: quizzes[index] });
});

// Toggle quiz active/inactive
app.patch('/api/admin/quizzes/:id/toggle', verifyAdmin, (req, res) => {
  const quizzes = db.get('quizzes');
  const index = quizzes.findIndex(q => q.id === req.params.id);
  if (index === -1) {
    return res.status(404).json({ error: 'Quiz not found.' });
  }

  quizzes[index].isActive = !quizzes[index].isActive;
  db.set('quizzes', quizzes);

  db.logAudit(req.admin.id, req.admin.email, 'TOGGLE_QUIZ', 'QUIZ', req.params.id, '', quizzes[index].isActive ? 'Published' : 'Unpublished', 'Toggled quiz status');
  res.json({ message: `Quiz ${quizzes[index].isActive ? 'Published' : 'Unpublished'} successfully.`, quiz: quizzes[index] });
});

// Delete quiz
app.delete('/api/admin/quizzes/:id', verifyAdmin, (req, res) => {
  let quizzes = db.get('quizzes');
  const target = quizzes.find(q => q.id === req.params.id);
  if (!target) {
    return res.status(404).json({ error: 'Quiz not found.' });
  }

  quizzes = quizzes.filter(q => q.id !== req.params.id);
  db.set('quizzes', quizzes);

  db.logAudit(req.admin.id, req.admin.email, 'DELETE_QUIZ', 'QUIZ', req.params.id, target.question, 'DELETED', 'Deleted quiz item');
  res.json({ message: 'Quiz removed successfully.' });
});

// ==========================================
// REWARD & APP SETTINGS
// ==========================================

// Get current settings
app.get('/api/admin/settings', verifyAdmin, (req, res) => {
  res.json({ settings: db.getSettings() });
});

// Update settings
app.put('/api/admin/settings', verifyAdmin, (req, res) => {
  const oldSettings = db.getSettings();
  const newSettings = req.body;

  const updated = db.updateSettings(newSettings);

  db.logAudit(
    req.admin.id,
    req.admin.email,
    'UPDATE_APP_SETTINGS',
    'SETTINGS',
    'GLOBAL',
    `Quizzes:${oldSettings.rewardCycleQuizzesCount}, Reward:${oldSettings.rewardPointsPerCycle}, Maintenance:${oldSettings.isMaintenanceMode}`,
    `Quizzes:${updated.rewardCycleQuizzesCount}, Reward:${updated.rewardPointsPerCycle}, Maintenance:${updated.isMaintenanceMode}`,
    req.body.auditReason || 'Updated system application settings'
  );

  res.json({ message: 'Settings saved and applied successfully!', settings: updated });
});

// ==========================================
// WITHDRAWAL MANAGEMENT
// ==========================================

// List all withdrawals with search and filters
app.get('/api/admin/withdrawals', verifyAdmin, (req, res) => {
  let withdrawals = [...db.get('withdrawals')];
  const { status, search, method } = req.query;

  if (status) {
    withdrawals = withdrawals.filter(w => w.status === status);
  }
  if (method) {
    withdrawals = withdrawals.filter(w => w.method === method);
  }
  if (search) {
    const q = String(search).toLowerCase().trim();
    withdrawals = withdrawals.filter(w =>
      (w.userName && w.userName.toLowerCase().includes(q)) ||
      (w.userEmail && w.userEmail.toLowerCase().includes(q)) ||
      (w.accountInfo && w.accountInfo.includes(q)) ||
      (w.id && w.id.toLowerCase().includes(q))
    );
  }

  withdrawals.sort((a, b) => b.requestDate - a.requestDate);

  res.json({
    total: withdrawals.length,
    withdrawals
  });
});

// Update withdrawal status (Approve, Process, Pay, Reject with Auto-Refund)
app.patch('/api/admin/withdrawals/:id', verifyAdmin, (req, res) => {
  const { status, adminNote } = req.body;
  if (!status) {
    return res.status(400).json({ error: 'Status is required.' });
  }

  const withdrawals = db.get('withdrawals');
  const index = withdrawals.findIndex(w => w.id === req.params.id);
  if (index === -1) {
    return res.status(404).json({ error: 'Withdrawal not found.' });
  }

  const target = withdrawals[index];
  const oldStatus = target.status;

  target.status = status;
  target.adminNote = adminNote || target.adminNote;
  target.processedDate = Date.now();
  withdrawals[index] = target;
  db.set('withdrawals', withdrawals);

  // If rejected or cancelled, auto-refund points to user account
  if ((status === 'REJECTED' || status === 'CANCELLED') && oldStatus !== 'REJECTED' && oldStatus !== 'CANCELLED') {
    const users = db.get('users');
    const userIndex = users.findIndex(u => u.id === target.userId);
    if (userIndex !== -1) {
      users[userIndex].points = Number((users[userIndex].points + target.points).toFixed(2));
      users[userIndex].totalWithdrawn = Math.max(0, Number((users[userIndex].totalWithdrawn - target.points).toFixed(2)));
      db.set('users', users);

      // Record refund transaction
      const transactions = db.get('rewardTransactions');
      transactions.unshift({
        id: 'tx_ref_' + Math.random().toString(36).substring(2, 8),
        userId: target.userId,
        points: target.points,
        type: 'WITHDRAWAL_REFUND',
        title: 'Withdrawal Refunded',
        description: `Refund for rejected cashout request ${target.id} (${adminNote || 'Rejected by admin'})`,
        timestamp: Date.now(),
        referenceId: target.id
      });
      db.set('rewardTransactions', transactions);
    }
  }

  // Add in-app notification for user
  const notifs = db.get('notifications');
  const statusMsg = status === 'PAID'
    ? `Your withdrawal for ${target.amountCurrency} ${target.currencySymbol} via ${target.method} has been PAID! Note: ${adminNote || 'Successful'}`
    : status === 'REJECTED'
    ? `Your withdrawal request ${target.id} was rejected and ${target.points} points have been refunded. Reason: ${adminNote || 'Details incorrect'}`
    : `Your withdrawal request ${target.id} is now ${status}.`;

  notifs.unshift({
    id: 'notif_' + Math.random().toString(36).substring(2, 8),
    title: `Withdrawal ${status}`,
    message: statusMsg,
    type: 'WITHDRAWAL',
    targetUserId: target.userId,
    timestamp: Date.now()
  });
  db.set('notifications', notifs);

  db.logAudit(
    req.admin.id,
    req.admin.email,
    'UPDATE_WITHDRAWAL',
    'WITHDRAWAL',
    target.id,
    oldStatus,
    status,
    adminNote || `Withdrawal marked as ${status}`
  );

  res.json({ message: `Withdrawal marked as ${status}.`, withdrawal: target });
});

// ==========================================
// NOTIFICATIONS & ANNOUNCEMENTS
// ==========================================

// Get notifications
app.get('/api/admin/notifications', verifyAdmin, (req, res) => {
  const notifications = db.get('notifications');
  res.json({ notifications });
});

// Send notification
app.post('/api/admin/notifications', verifyAdmin, (req, res) => {
  const { title, message, type, targetUserId } = req.body;
  if (!title || !message) {
    return res.status(400).json({ error: 'Title and message are required.' });
  }

  const notifications = db.get('notifications');
  const newNotif = {
    id: 'notif_' + Math.random().toString(36).substring(2, 8),
    title: title.trim(),
    message: message.trim(),
    type: type || 'ANNOUNCEMENT',
    targetUserId: targetUserId ? targetUserId.trim() : 'ALL',
    timestamp: Date.now()
  };

  notifications.unshift(newNotif);
  db.set('notifications', notifications);

  db.logAudit(req.admin.id, req.admin.email, 'BROADCAST_NOTIFICATION', 'NOTIFICATION', newNotif.id, '', newNotif.title, `Target: ${newNotif.targetUserId}`);

  res.json({ message: 'Notification broadcast successfully!', notification: newNotif });
});

// ==========================================
// AUDIT LOGS
// ==========================================
app.get('/api/admin/audit-logs', verifyAdmin, (req, res) => {
  let logs = [...(db.data.adminAuditLogs || [])];
  const { search, action } = req.query;

  if (action) {
    logs = logs.filter(l => l.action === action);
  }
  if (search) {
    const q = String(search).toLowerCase().trim();
    logs = logs.filter(l =>
      (l.adminEmail && l.adminEmail.toLowerCase().includes(q)) ||
      (l.action && l.action.toLowerCase().includes(q)) ||
      (l.targetId && l.targetId.toLowerCase().includes(q)) ||
      (l.reason && l.reason.toLowerCase().includes(q))
    );
  }

  logs.sort((a, b) => b.timestamp - a.timestamp);

  res.json({
    total: logs.length,
    logs
  });
});

// ==========================================
// SHARED CLIENT APP REST API (For User App)
// ==========================================

// Public Config & Settings
app.get(['/api/app/config', '/api/app/settings'], (req, res) => {
  const settings = db.getSettings();
  res.json({
    appName: settings.appName,
    rewardCycleQuizzesCount: settings.rewardCycleQuizzesCount,
    quizTimerSeconds: settings.quizTimerSeconds,
    rewardPointsPerCycle: settings.rewardPointsPerCycle,
    pointMonetaryValue: settings.pointMonetaryValue,
    currencySymbol: settings.currencySymbol,
    referralCommissionPercent: settings.referralCommissionPercent,
    minWithdrawalPoints: settings.minWithdrawalPoints,
    maxWithdrawalPoints: settings.maxWithdrawalPoints,
    isRegistrationEnabled: settings.isRegistrationEnabled,
    isLoginEnabled: settings.isLoginEnabled,
    isTaskSystemEnabled: settings.isTaskSystemEnabled,
    isQuizEnabled: settings.isQuizEnabled,
    isBannerAdsEnabled: settings.isBannerAdsEnabled,
    isRewardedAdsEnabled: settings.isRewardedAdsEnabled,
    isReferralEnabled: settings.isReferralEnabled,
    isWithdrawEnabled: settings.isWithdrawEnabled,
    isBkashEnabled: settings.isBkashEnabled,
    isNagadEnabled: settings.isNagadEnabled,
    isUsdtEnabled: settings.isUsdtEnabled,
    isLeaderboardEnabled: settings.isLeaderboardEnabled,
    isNotificationsEnabled: settings.isNotificationsEnabled,
    isMaintenanceMode: settings.isMaintenanceMode,
    maintenanceMessage: settings.maintenanceMessage,
    announcementText: settings.announcementText,
    startIoAppId: settings.startIoAppId,
    telegramUrl: settings.telegramUrl,
    youtubeUrl: settings.youtubeUrl,
    supportContact: settings.supportContact,
    aboutText: settings.aboutText,
    howToWorkText: settings.howToWorkText
  });
});

// Public Quizzes for User App
app.get('/api/app/quizzes', (req, res) => {
  const settings = db.getSettings();
  if (settings.isMaintenanceMode || !settings.isTaskSystemEnabled || !settings.isQuizEnabled) {
    return res.json({ quizzes: [] });
  }
  const quizzes = db.get('quizzes').filter(q => q.isActive).sort((a, b) => a.order - b.order);
  res.json({ quizzes });
});

// User App Login
app.post('/api/app/auth/login', (req, res) => {
  const settings = db.getSettings();
  if (settings.isMaintenanceMode) {
    return res.status(503).json({ error: settings.maintenanceMessage });
  }
  if (!settings.isLoginEnabled) {
    return res.status(403).json({ error: 'User login is temporarily paused.' });
  }

  const { emailOrPhone, password } = req.body;
  if (!emailOrPhone || !password) {
    return res.status(400).json({ error: 'Email/phone and password are required.' });
  }

  const clean = emailOrPhone.trim().toLowerCase();
  const cleanPhone = emailOrPhone.trim();
  const users = db.get('users');
  const user = users.find(u => u.email.toLowerCase() === clean || u.phone === cleanPhone);

  if (!user) {
    return res.status(404).json({ error: 'Account not found. Please Sign Up to create your account.' });
  }
  if (user.isBlocked) {
    return res.status(403).json({ error: 'Your account has been suspended by administration.' });
  }

  // Verify password hash if present
  if (user.passwordHash) {
    const isMatch = bcrypt.compareSync(password, user.passwordHash);
    if (!isMatch) {
      return res.status(401).json({ error: 'Incorrect password. Please check and try again.' });
    }
  }

  user.lastActiveAt = Date.now();
  db.set('users', users);

  // Return user without sensitive password hash
  const { passwordHash, ...safeUser } = user;
  res.json({ user: safeUser });
});

// User App Registration (Unique Account, Safe Password Hash, 0 Starting Points)
app.post('/api/app/auth/register', (req, res) => {
  const settings = db.getSettings();
  if (settings.isMaintenanceMode) {
    return res.status(503).json({ error: settings.maintenanceMessage });
  }
  if (!settings.isRegistrationEnabled) {
    return res.status(403).json({ error: 'New user registrations are currently closed.' });
  }

  const { name, email, phone, password, referralCode } = req.body;
  if (!name || !email || !phone || !password) {
    return res.status(400).json({ error: 'All fields (Name, Email, Phone, Password) are required.' });
  }

  if (password.length < 6) {
    return res.status(400).json({ error: 'Password must be at least 6 characters long.' });
  }

  const users = db.get('users');
  if (users.some(u => u.email.toLowerCase() === email.trim().toLowerCase())) {
    return res.status(400).json({ error: 'An account with this email already exists. Please login.' });
  }

  if (users.some(u => u.phone.trim() === phone.trim())) {
    return res.status(400).json({ error: 'An account with this phone number already exists. Please login.' });
  }

  let ref = null;
  if (referralCode && referralCode.trim()) {
    const code = referralCode.trim().toUpperCase();
    const referrer = users.find(u => u.referralCode.toUpperCase() === code);
    if (!referrer) {
      return res.status(400).json({ error: 'Invalid referral code. Please check or leave blank.' });
    }
    ref = referrer.referralCode;
  }

  const salt = bcrypt.genSaltSync(10);
  const passwordHash = bcrypt.hashSync(password, salt);

  const newUser = {
    id: 'AP-' + Math.floor(10000 + Math.random() * 90000),
    name: name.trim(),
    email: email.trim().toLowerCase(),
    phone: phone.trim(),
    passwordHash,
    points: 0.0, // Newly registered user starts with 0 points
    totalEarned: 0.0,
    totalWithdrawn: 0.0,
    completedQuizzesCount: 0,
    currentCycleQuizzes: 0,
    referralCode: 'PAY' + Math.floor(1000 + Math.random() * 9000),
    referredBy: ref,
    isBlocked: false,
    isTaskDisabled: false,
    isWithdrawDisabled: false,
    isReferralDisabled: false,
    role: 'USER',
    createdAt: Date.now(),
    lastActiveAt: Date.now()
  };

  users.push(newUser);
  db.set('users', users);

  const { passwordHash: _, ...safeUser } = newUser;
  res.setHeader('Content-Type', 'application/json');
  res.json({ success: true, message: 'Registration successful!', user: safeUser });
});

// Start Task Attempt
app.post('/api/app/task/start', (req, res) => {
  const { userId, quizId } = req.body;
  const users = db.get('users');
  const user = users.find(u => u.id === userId);

  if (!user || user.isBlocked || user.isTaskDisabled) {
    return res.status(403).json({ error: 'Task access restricted.' });
  }

  const attempt = {
    id: 'att_' + Math.random().toString(36).substring(2, 9),
    userId,
    quizId,
    startTime: Date.now(),
    cycleIndex: (user.currentCycleQuizzes || 0) + 1,
    token: Math.random().toString(36).substring(2, 15)
  };

  const attempts = db.get('taskAttempts');
  attempts.push(attempt);
  db.set('taskAttempts', attempts);

  res.json({ attempt });
});

// Complete Quiz (10-second Anti-Fraud Check)
app.post('/api/app/task/complete', (req, res) => {
  const { attemptId, selectedOptionIndex } = req.body;
  const attempts = db.get('taskAttempts');
  const attempt = attempts.find(a => a.id === attemptId);

  if (!attempt) {
    return res.status(400).json({ error: 'Invalid or expired attempt.' });
  }

  const quiz = db.get('quizzes').find(q => q.id === attempt.quizId);
  if (!quiz) {
    return res.status(404).json({ error: 'Quiz not found.' });
  }

  const timeElapsed = Date.now() - attempt.startTime;
  const requiredMs = (quiz.timerSeconds * 1000) - 1000;

  if (timeElapsed < requiredMs) {
    return res.status(400).json({ error: `Timer validation failed (${timeElapsed}ms). Anti-fraud check requires waiting full 10 seconds.` });
  }

  const isCorrect = selectedOptionIndex === quiz.correctOptionIndex;
  attempt.completedTime = Date.now();
  attempt.isCorrect = isCorrect;
  attempt.isVerified = true;
  db.set('taskAttempts', attempts);

  const users = db.get('users');
  const user = users.find(u => u.id === attempt.userId);
  const settings = db.getSettings();

  if (user) {
    user.completedQuizzesCount = (user.completedQuizzesCount || 0) + 1;
    const newCycle = (user.currentCycleQuizzes || 0) + 1;
    const required = settings.rewardCycleQuizzesCount || 5;
    user.currentCycleQuizzes = Math.min(newCycle, required);
    user.lastActiveAt = Date.now();
    db.set('users', users);

    res.json({
      isCorrect,
      correctIndex: quiz.correctOptionIndex,
      currentCycleProgress: user.currentCycleQuizzes,
      requiredCycleQuizzes: required,
      isRewardCycleReady: user.currentCycleQuizzes >= required
    });
  } else {
    res.status(404).json({ error: 'User not found' });
  }
});

// Claim Rewarded Ad Points (Atomic Server Update)
app.post('/api/app/task/claim-reward', (req, res) => {
  const { userId } = req.body;
  const users = db.get('users');
  const user = users.find(u => u.id === userId);
  const settings = db.getSettings();

  if (!user || user.isBlocked || user.isTaskDisabled) {
    return res.status(403).json({ error: 'Reward claiming restricted.' });
  }

  const required = settings.rewardCycleQuizzesCount || 5;
  if ((user.currentCycleQuizzes || 0) < required) {
    return res.status(400).json({ error: `You must complete ${required} quizzes before watching rewarded ad.` });
  }

  const reward = settings.rewardPointsPerCycle || 1.0;
  user.points = Number((user.points + reward).toFixed(2));
  user.totalEarned = Number((user.totalEarned + reward).toFixed(2));
  user.currentCycleQuizzes = 0; // Reset cycle cleanly
  user.lastActiveAt = Date.now();
  db.set('users', users);

  // Record reward transaction
  const transactions = db.get('rewardTransactions');
  transactions.unshift({
    id: 'tx_rew_' + Math.random().toString(36).substring(2, 8),
    userId: user.id,
    points: reward,
    type: 'REWARD_CYCLE',
    title: 'Rewarded Ad Completed',
    description: `Verified Start.io Rewarded Video Ad after ${required} valid quizzes`,
    timestamp: Date.now(),
    referenceId: ''
  });

  // Commission to referrer (10% default)
  if (user.referredBy && settings.isReferralEnabled) {
    const referrer = users.find(u => u.referralCode === user.referredBy && !u.isReferralDisabled);
    if (referrer) {
      const commission = Number((reward * (settings.referralCommissionPercent / 100)).toFixed(2));
      if (commission > 0) {
        referrer.points = Number((referrer.points + commission).toFixed(2));
        referrer.totalEarned = Number((referrer.totalEarned + commission).toFixed(2));
        db.set('users', users);

        transactions.unshift({
          id: 'tx_ref_' + Math.random().toString(36).substring(2, 8),
          userId: referrer.id,
          points: commission,
          type: 'REFERRAL_BONUS',
          title: 'Referral Commission',
          description: `${settings.referralCommissionPercent}% commission from referral ${user.name}'s rewarded ad completion`,
          timestamp: Date.now(),
          referenceId: user.id
        });
      }
    }
  }

  db.set('rewardTransactions', transactions);

  res.json({
    message: 'Reward claimed successfully!',
    rewardPoints: reward,
    newBalance: user.points
  });
});

// User Withdrawal Request
app.post('/api/app/withdraw', (req, res) => {
  const { userId, method, points, accountInfo, accountHolderName } = req.body;
  const settings = db.getSettings();

  if (settings.isMaintenanceMode || !settings.isWithdrawEnabled) {
    return res.status(403).json({ error: 'Withdrawals are currently disabled by administration.' });
  }

  const users = db.get('users');
  const user = users.find(u => u.id === userId);

  if (!user || user.isBlocked || user.isWithdrawDisabled) {
    return res.status(403).json({ error: 'Withdrawal access restricted.' });
  }

  const pts = Number(points);
  if (pts < settings.minWithdrawalPoints) {
    return res.status(400).json({ error: `Minimum withdrawal is ${settings.minWithdrawalPoints} points.` });
  }
  if (user.points < pts) {
    return res.status(400).json({ error: `Insufficient points. You have ${user.points} points.` });
  }
  if (!accountInfo || accountInfo.trim().length < 6) {
    return res.status(400).json({ error: 'Please enter a valid account or wallet address.' });
  }

  const fiatAmount = Number((pts * settings.pointMonetaryValue).toFixed(2));

  // Deduct points
  user.points = Number((user.points - pts).toFixed(2));
  user.totalWithdrawn = Number((user.totalWithdrawn + pts).toFixed(2));
  db.set('users', users);

  const request = {
    id: 'WTH-' + Math.random().toString(36).substring(2, 8).toUpperCase(),
    userId: user.id,
    userName: user.name,
    userEmail: user.email,
    points: pts,
    amountCurrency: fiatAmount,
    currencySymbol: settings.currencySymbol,
    method,
    accountInfo: accountInfo.trim(),
    accountHolderName: (accountHolderName || user.name).trim(),
    status: 'PENDING',
    requestDate: Date.now(),
    processedDate: null,
    adminNote: ''
  };

  const withdrawals = db.get('withdrawals');
  withdrawals.unshift(request);
  db.set('withdrawals', withdrawals);

  const transactions = db.get('rewardTransactions');
  transactions.unshift({
    id: 'tx_wth_' + Math.random().toString(36).substring(2, 8),
    userId: user.id,
    points: -pts,
    type: 'WITHDRAWAL_DEDUCT',
    title: `Withdrawal Requested (${method})`,
    description: `Cashout request of ${pts} pts (${fiatAmount} ${settings.currencySymbol}) to ${accountInfo}`,
    timestamp: Date.now(),
    referenceId: request.id
  });
  db.set('rewardTransactions', transactions);

  res.json({ message: 'Withdrawal submitted successfully!', withdrawal: request });
});

// User Profile / State Sync
app.get('/api/app/user/profile', (req, res) => {
  const { userId } = req.query;
  if (!userId) return res.status(400).json({ error: 'User ID required' });

  const users = db.get('users');
  const user = users.find(u => u.id === userId || u.email.toLowerCase() === String(userId).toLowerCase() || u.phone === String(userId));
  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }

  const { passwordHash, ...safeUser } = user;
  res.json({ user: safeUser });
});

// User History
app.get('/api/app/user/history', (req, res) => {
  const { userId } = req.query;
  if (!userId) return res.status(400).json({ error: 'User ID required' });

  const transactions = db.get('rewardTransactions').filter(t => t.userId === userId);
  const withdrawals = db.get('withdrawals').filter(w => w.userId === userId);

  res.json({ transactions, withdrawals });
});

// Direct APK Download endpoint
app.get(['/download/app-debug.apk', '/download/apk', '/api/download/apk', '/app-debug.apk'], (req, res) => {
  const possiblePaths = [
    path.join(__dirname, '../public/app-debug.apk'),
    path.join(__dirname, '../.build-outputs/app-debug.apk'),
    path.join(__dirname, '../app/build/outputs/apk/debug/app-debug.apk')
  ];

  for (const p of possiblePaths) {
    if (fs.existsSync(p)) {
      res.setHeader('Content-Type', 'application/vnd.android.package-archive');
      res.setHeader('Content-Disposition', 'attachment; filename="AdsPay-debug.apk"');
      res.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate');
      res.setHeader('Pragma', 'no-cache');
      res.setHeader('Expires', '0');
      return res.sendFile(p);
    }
  }

  res.status(404).json({ error: 'APK build file not found' });
});

// Catch-all for API endpoints to ensure they ALWAYS return JSON and never fall through to Vite/static HTML
app.use('/api', (req, res) => {
  res.status(404).json({
    success: false,
    error: `API endpoint '${req.method} ${req.originalUrl || req.url}' not found`
  });
});

// ==========================================
// VITE INTEGRATION / STATIC CLIENT SERVING
// ==========================================
async function startServer() {
  const isProduction = process.env.NODE_ENV === 'production';

  if (!isProduction) {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa'
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(__dirname, '../dist');
    app.use(express.static(distPath));
    app.get('*all', (req, res) => {
      if (req.path.startsWith('/api/')) {
        return res.status(404).json({ error: 'API endpoint not found' });
      }
      const indexPath = path.join(distPath, 'index.html');
      if (fs.existsSync(indexPath)) {
        res.sendFile(indexPath);
      } else {
        res.status(200).send('Ads Pay Admin API server running.');
      }
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Ads Pay Backend & Web Admin Server running on http://0.0.0.0:${PORT}`);
  });
}

startServer();
