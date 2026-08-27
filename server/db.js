import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import bcrypt from 'bcryptjs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const DATA_DIR = path.join(__dirname, '../data');
const DB_FILE = path.join(DATA_DIR, 'adspay_db.json');

if (!fs.existsSync(DATA_DIR)) {
  fs.mkdirSync(DATA_DIR, { recursive: true });
}

const DEFAULT_SETTINGS = {
  appName: 'Ads Pay',
  rewardCycleQuizzesCount: 5,
  quizTimerSeconds: 10,
  rewardPointsPerCycle: 1.0,
  pointMonetaryValue: 0.20,
  currencySymbol: '৳',
  referralCommissionPercent: 10.0,
  minWithdrawalPoints: 50.0,
  maxWithdrawalPoints: 10000.0,
  dailyTaskLimit: 50,
  hourlyTaskLimit: 10,
  startIoAppId: '207226080',
  isRegistrationEnabled: true,
  isLoginEnabled: true,
  isTaskSystemEnabled: true,
  isQuizEnabled: true,
  isBannerAdsEnabled: true,
  isRewardedAdsEnabled: true,
  isReferralEnabled: true,
  isWithdrawEnabled: true,
  isBkashEnabled: true,
  isNagadEnabled: true,
  isUsdtEnabled: true,
  isLeaderboardEnabled: true,
  isNotificationsEnabled: true,
  isMaintenanceMode: false,
  maintenanceMessage: 'Ads Pay is currently undergoing scheduled system maintenance. Please check back shortly!',
  announcementText: '🔥 Welcome to Ads Pay! Complete 5 quizzes to unlock your Start.io rewarded ad and earn instant points.',
  telegramUrl: 'https://t.me/adspayofficial',
  youtubeUrl: 'https://youtube.com/@adspayofficial',
  supportContact: 'support@adspay.app',
  aboutText: 'Ads Pay is a transparent, trusted reward application where users complete quizzes, view Start.io rewarded video ads, and withdraw their earnings directly to bKash, Nagad, or BEP20 USDT.',
  howToWorkText: '1. Tap Start Task to begin a quiz session.\n2. Answer each quiz question and wait for the 10-second countdown timer.\n3. Complete 5 consecutive valid quizzes to unlock a Start.io Rewarded Video Ad.\n4. Watch the full rewarded ad to earn 1 point automatically.\n5. Invite your friends using your referral code to receive 10% lifetime commission.\n6. Request cashouts once you reach the 50 points minimum threshold.'
};

function getInitialData() {
  const salt = bcrypt.genSaltSync(10);
  const defaultAdminPasswordHash = bcrypt.hashSync('Admin@AdsPay2026!', salt);

  return {
    adminUsers: [
      {
        id: 'ADMIN-001',
        name: 'Master Administrator',
        email: 'admin@adspay.app',
        passwordHash: defaultAdminPasswordHash,
        role: 'SUPER_ADMIN',
        createdAt: Date.now() - 86400000 * 30,
        lastLoginAt: Date.now()
      }
    ],
    users: [
      {
        id: 'AP-10824',
        name: 'Sifat Islam',
        email: 'sifat@example.com',
        phone: '+8801812345678',
        points: 45.0,
        totalEarned: 95.0,
        totalWithdrawn: 50.0,
        completedQuizzesCount: 45,
        currentCycleQuizzes: 3,
        referralCode: 'PAY1082',
        referredBy: null,
        isBlocked: false,
        isTaskDisabled: false,
        isWithdrawDisabled: false,
        isReferralDisabled: false,
        createdAt: Date.now() - 86400000 * 15,
        lastActiveAt: Date.now() - 3600000 * 2
      },
      {
        id: 'AP-77312',
        name: 'Tanvir Ahmed',
        email: 'tanvir@example.com',
        phone: '+8801912345678',
        points: 142.0,
        totalEarned: 210.0,
        totalWithdrawn: 68.0,
        completedQuizzesCount: 95,
        currentCycleQuizzes: 4,
        referralCode: 'PAY7731',
        referredBy: 'PAY1082',
        isBlocked: false,
        isTaskDisabled: false,
        isWithdrawDisabled: false,
        isReferralDisabled: false,
        createdAt: Date.now() - 86400000 * 10,
        lastActiveAt: Date.now() - 3600000 * 5
      },
      {
        id: 'AP-88421',
        name: 'Rahim Mia',
        email: 'rahim@example.com',
        phone: '+8801612345678',
        points: 88.0,
        totalEarned: 150.0,
        totalWithdrawn: 62.0,
        completedQuizzesCount: 60,
        currentCycleQuizzes: 1,
        referralCode: 'PAY8842',
        referredBy: 'PAY1082',
        isBlocked: false,
        isTaskDisabled: false,
        isWithdrawDisabled: false,
        isReferralDisabled: false,
        createdAt: Date.now() - 86400000 * 7,
        lastActiveAt: Date.now() - 3600000 * 12
      },
      {
        id: 'AP-99014',
        name: 'Fraud Test User',
        email: 'bot_test@example.com',
        phone: '+8801512345678',
        points: 0.0,
        totalEarned: 0.0,
        totalWithdrawn: 0.0,
        completedQuizzesCount: 2,
        currentCycleQuizzes: 0,
        referralCode: 'PAY9901',
        referredBy: null,
        isBlocked: true,
        isTaskDisabled: true,
        isWithdrawDisabled: true,
        isReferralDisabled: true,
        createdAt: Date.now() - 86400000 * 2,
        lastActiveAt: Date.now() - 86400000
      }
    ],
    quizzes: [
      {
        id: 'q1',
        question: 'What is the primary cryptocurrency created by Satoshi Nakamoto?',
        options: ['Bitcoin (BTC)', 'Ethereum (ETH)', 'Solana (SOL)', 'Dogecoin (DOGE)'],
        correctOptionIndex: 0,
        timerSeconds: 10,
        category: 'Crypto',
        isActive: true,
        order: 1
      },
      {
        id: 'q2',
        question: 'What does "CPU" stand for in computer science?',
        options: ['Central Process Unit', 'Central Processing Unit', 'Computer Personal Unit', 'Core Power Unit'],
        correctOptionIndex: 1,
        timerSeconds: 10,
        category: 'Tech',
        isActive: true,
        order: 2
      },
      {
        id: 'q3',
        question: 'What is 45 + 55 × 2?',
        options: ['200', '155', '145', '190'],
        correctOptionIndex: 1,
        timerSeconds: 10,
        category: 'Math',
        isActive: true,
        order: 3
      },
      {
        id: 'q4',
        question: 'Which mobile operating system is developed by Google?',
        options: ['iOS', 'Android', 'Windows Mobile', 'Symbian'],
        correctOptionIndex: 1,
        timerSeconds: 10,
        category: 'Mobile',
        isActive: true,
        order: 4
      },
      {
        id: 'q5',
        question: 'How many quizzes are required to unlock a Rewarded Video Ad in Ads Pay?',
        options: ['3 Quizzes', '5 Quizzes', '10 Quizzes', '1 Quiz'],
        correctOptionIndex: 1,
        timerSeconds: 10,
        category: 'Ads Pay',
        isActive: true,
        order: 5
      },
      {
        id: 'q6',
        question: 'What is the chemical symbol for Gold?',
        options: ['Ag', 'Au', 'Fe', 'Cu'],
        correctOptionIndex: 1,
        timerSeconds: 10,
        category: 'Science',
        isActive: true,
        order: 6
      },
      {
        id: 'q7',
        question: 'Which of the following is a fast mobile financial service in Bangladesh?',
        options: ['bKash', 'PayPal', 'Venmo', 'CashApp'],
        correctOptionIndex: 0,
        timerSeconds: 10,
        category: 'General',
        isActive: true,
        order: 7
      },
      {
        id: 'q8',
        question: 'Which network standard does BEP20 belong to?',
        options: ['BNB Smart Chain (BSC)', 'Ethereum Mainnet', 'Bitcoin Lightning', 'Polygon POS'],
        correctOptionIndex: 0,
        timerSeconds: 10,
        category: 'Crypto',
        isActive: true,
        order: 8
      }
    ],
    taskAttempts: [],
    rewardTransactions: [
      {
        id: 'tx_101',
        userId: 'AP-10824',
        points: 1.0,
        type: 'REWARD_CYCLE',
        title: 'Rewarded Ad Completed',
        description: 'Completed 5 quizzes cycle and verified Start.io video ad',
        timestamp: Date.now() - 3600000 * 2,
        referenceId: ''
      },
      {
        id: 'tx_102',
        userId: 'AP-10824',
        points: 0.1,
        type: 'REFERRAL_BONUS',
        title: 'Referral Commission',
        description: '10% commission from referral Tanvir Ahmed task completion',
        timestamp: Date.now() - 3600000 * 6,
        referenceId: ''
      },
      {
        id: 'tx_103',
        userId: 'AP-77312',
        points: 1.0,
        type: 'REWARD_CYCLE',
        title: 'Rewarded Ad Completed',
        description: 'Completed 5 quizzes cycle and verified Start.io video ad',
        timestamp: Date.now() - 3600000 * 7,
        referenceId: ''
      }
    ],
    withdrawals: [
      {
        id: 'WTH-984210',
        userId: 'AP-10824',
        userName: 'Sifat Islam',
        userEmail: 'sifat@example.com',
        points: 50.0,
        amountCurrency: 10.0,
        currencySymbol: '৳',
        method: 'BKASH',
        accountInfo: '01812345678',
        accountHolderName: 'Sifat Islam',
        status: 'PAID',
        requestDate: Date.now() - 86400000 * 3,
        processedDate: Date.now() - 86400000 * 2,
        adminNote: 'Paid via bKash Merchant TrxID: BK7829103'
      },
      {
        id: 'WTH-881294',
        userId: 'AP-77312',
        userName: 'Tanvir Ahmed',
        userEmail: 'tanvir@example.com',
        points: 100.0,
        amountCurrency: 20.0,
        currencySymbol: '৳',
        method: 'NAGAD',
        accountInfo: '01912345678',
        accountHolderName: 'Tanvir Ahmed',
        status: 'PENDING',
        requestDate: Date.now() - 3600000 * 4,
        processedDate: null,
        adminNote: ''
      },
      {
        id: 'WTH-551982',
        userId: 'AP-88421',
        userName: 'Rahim Mia',
        userEmail: 'rahim@example.com',
        points: 50.0,
        amountCurrency: 10.0,
        currencySymbol: '৳',
        method: 'USDT_BEP20',
        accountInfo: '0x71C...829aB4',
        accountHolderName: 'Rahim Wallet',
        status: 'PROCESSING',
        requestDate: Date.now() - 3600000 * 8,
        processedDate: null,
        adminNote: 'Queued for Binance BEP20 payout batch'
      }
    ],
    notifications: [
      {
        id: 'notif_1',
        title: 'Welcome to Ads Pay 🎉',
        message: 'Earn daily rewards by answering 5 easy quizzes and watching verified Start.io video ads!',
        type: 'ANNOUNCEMENT',
        targetUserId: 'ALL',
        timestamp: Date.now() - 86400000 * 2
      }
    ],
    appSettings: { ...DEFAULT_SETTINGS },
    adminAuditLogs: [
      {
        id: 'log_001',
        adminId: 'ADMIN-001',
        adminEmail: 'admin@adspay.app',
        action: 'SYSTEM_INITIALIZED',
        targetType: 'SYSTEM',
        targetId: 'GLOBAL',
        previousValue: 'None',
        newValue: 'Initialized with Start.io App ID 207226080',
        reason: 'Master system provisioning',
        timestamp: Date.now() - 86400000 * 10
      }
    ]
  };
}

class Database {
  constructor() {
    this.data = null;
    this.load();
  }

  load() {
    try {
      if (fs.existsSync(DB_FILE)) {
        const raw = fs.readFileSync(DB_FILE, 'utf-8');
        this.data = JSON.parse(raw);
      } else {
        this.data = getInitialData();
        this.save();
      }
    } catch (err) {
      console.error('Error loading database, resetting to default:', err);
      this.data = getInitialData();
      this.save();
    }
  }

  save() {
    try {
      fs.writeFileSync(DB_FILE, JSON.stringify(this.data, null, 2), 'utf-8');
    } catch (err) {
      console.error('Error saving database:', err);
    }
  }

  get(collection) {
    return this.data[collection] || [];
  }

  set(collection, value) {
    this.data[collection] = value;
    this.save();
  }

  getSettings() {
    return this.data.appSettings || DEFAULT_SETTINGS;
  }

  updateSettings(newSettings) {
    this.data.appSettings = { ...this.data.appSettings, ...newSettings };
    this.save();
    return this.data.appSettings;
  }

  logAudit(adminId, adminEmail, action, targetType, targetId, prevValue, newValue, reason) {
    const log = {
      id: 'log_' + Math.random().toString(36).substring(2, 9),
      adminId,
      adminEmail,
      action,
      targetType,
      targetId,
      previousValue: String(prevValue || ''),
      newValue: String(newValue || ''),
      reason: reason || 'Admin action',
      timestamp: Date.now()
    };
    if (!this.data.adminAuditLogs) this.data.adminAuditLogs = [];
    this.data.adminAuditLogs.unshift(log);
    // Keep last 1000 logs
    if (this.data.adminAuditLogs.length > 1000) {
      this.data.adminAuditLogs = this.data.adminAuditLogs.slice(0, 1000);
    }
    this.save();
    return log;
  }
}

export const db = new Database();
