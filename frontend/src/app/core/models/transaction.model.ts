export type TransactionType = 'INCOME' | 'EXPENSE';

export interface TransactionRequest {
  title: string;
  amount: number;
  category: string;
  type: TransactionType;
  transactionDate: string; // ISO date format YYYY-MM-DD
  notes?: string | null;
}

export interface TransactionResponse {
  id: number;
  title: string;
  amount: number;
  category: string;
  type: TransactionType;
  transactionDate: string;
  notes: string | null;
  createdAt: string;
}

export interface FinancialSummary {
  totalIncome: number;
  totalExpense: number;
  netBalance: number;
  totalTransactions: number;
  incomeCount: number;
  expenseCount: number;
}

export interface TransactionFilter {
  type?: TransactionType | '';
  category?: string;
  startDate?: string;
  endDate?: string;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
}
