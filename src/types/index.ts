export interface AdminUser {
  id: string;
  name: string;
  email: string;
  role: 'SUPER_ADMIN' | 'ADMIN' | 'MODERATOR';
  createdAt?: number;
  lastLoginAt?: number;
}

export interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  points: number;
  totalEarned: number;
  totalWithdrawn: number;
  completedQuizzesCount: number;
  currentCycleQuizzes: number;
  referralCode: string;
  referredBy: string | null;
  isBlocked: boolean;
  isTaskDisabled: boolean;
  isWithdrawDisabled: boolean;
  isReferralDisabled: boolean;
  createdAt: number;
  lastActiveAt: number;
}

export interface Quiz {
  id: string;
  question: string;
  options: string[];
  correctOptionIndex: number;
  timerSeconds: number;
  category: string;
  isActive: boolean;
  order: number;
}

export interface AppSettings {
  appName: string;
  rewardCycleQuizzesCount: number;
  quizTimerSeconds: number;
  rewardPointsPerCycle: number;
  pointMonetaryValue: number;
  currencySymbol: string;
  referralCommissionPercent: number;
  minWithdrawalPoints: number;
  maxWithdrawalPoints: number;
  dailyTaskLimit: number;
  hourlyTaskLimit: number;
  startIoAppId: string;
  isRegistrationEnabled: boolean;
  isLoginEnabled: boolean;
  isTaskSystemEnabled: boolean;
  isQuizEnabled: boolean;
  isBannerAdsEnabled: boolean;
  isRewardedAdsEnabled: boolean;
  isReferralEnabled: boolean;
  isWithdrawEnabled: boolean;
  isBkashEnabled: boolean;
  isNagadEnabled: boolean;
  isUsdtEnabled: boolean;
  isLeaderboardEnabled: boolean;
  isNotificationsEnabled: boolean;
  isMaintenanceMode: boolean;
  maintenanceMessage: string;
  announcementText: string;
  telegramUrl: string;
  youtubeUrl: string;
  supportContact: string;
  aboutText: string;
  howToWorkText: string;
}

export interface WithdrawalRequest {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  points: number;
  amountCurrency: number;
  currencySymbol: string;
  method: 'BKASH' | 'NAGAD' | 'USDT_BEP20';
  accountInfo: string;
  accountHolderName: string;
  status: 'PENDING' | 'PROCESSING' | 'APPROVED' | 'PAID' | 'REJECTED' | 'CANCELLED';
  requestDate: number;
  processedDate: number | null;
  adminNote: string;
}

export interface RewardTransaction {
  id: string;
  userId: string;
  points: number;
  type: 'REWARD_CYCLE' | 'SIGNUP_BONUS' | 'REFERRAL_BONUS' | 'WITHDRAWAL_DEDUCT' | 'WITHDRAWAL_REFUND' | 'MANUAL_ADJUSTMENT';
  title: string;
  description: string;
  timestamp: number;
  referenceId?: string;
}

export interface AppNotification {
  id: string;
  title: string;
  message: string;
  type: string;
  targetUserId: string;
  timestamp: number;
}

export interface AdminAuditLog {
  id: string;
  adminId: string;
  adminEmail: string;
  action: string;
  targetType: string;
  targetId: string;
  previousValue: string;
  newValue: string;
  reason: string;
  timestamp: number;
}

export interface DashboardMetrics {
  totalUsers: number;
  activeUsers: number;
  blockedUsers: number;
  totalPointsIssued: number;
  totalPointsWithdrawn: number;
  currentTotalUserBalance: number;
  totalTasksCompleted: number;
  totalRewardCyclesCompleted: number;
  activeQuizzesCount: number;
  totalQuizzesCount: number;
  startIoAppId: string;
  isMaintenanceMode: boolean;
  pendingWithdrawalsCount: number;
  pendingWithdrawalsAmount: number;
  completedWithdrawalsCount: number;
  completedWithdrawalsAmount: number;
  rejectedWithdrawalsCount: number;
  processingWithdrawalsCount: number;
  referredUsersCount: number;
  totalReferralCommissionPaid: number;
}
