export interface LoginResponse {
  token: string;
  email: string;
  name: string;
  role: string;
  userId: number;
}

export interface NotificationDto {
  id: number;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export interface AdminDashboardStatsResponse {
  totalStudents: number;
  totalPendingFees: number;
  recentAlerts: NotificationDto[];
}

export interface EnrollmentResponse {
  id: number;
  studentId: number;
  studentName: string;
  courseId: number;
  courseCode: string;
  courseTitle: string;
  enrollmentDate: string;
  status: string;
}

export interface FeePaymentResponse {
  id: number;
  amount: number;
  paymentDate: string;
  status: string;
  transactionId: string;
  studentId: number;
  studentName: string;
  paymentMethod: string;
}
