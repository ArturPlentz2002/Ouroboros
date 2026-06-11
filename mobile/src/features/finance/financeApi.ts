import { ApiClient } from '../../api/client';

export type EntryType = 'EXPENSE' | 'INCOME';

export interface Category {
  id: string;
  name: string;
  color: string | null;
  createdAt: string;
}

export interface CategoryInput {
  name: string;
  color?: string;
}

export interface FinanceEntry {
  id: string;
  categoryId: string | null;
  type: EntryType;
  amount: number;
  description: string | null;
  occurredOn: string;
  eventId: string | null;
  createdAt: string;
}

export interface FinanceEntryInput {
  categoryId?: string;
  type: EntryType;
  amount: number;
  description?: string;
  occurredOn: string;
  eventId?: string;
}

export interface CategoryTotal {
  categoryId: string | null;
  categoryName: string | null;
  type: EntryType;
  total: number;
}

export interface MonthlySummary {
  month: string;
  totalIncome: number;
  totalExpense: number;
  balance: number;
  byCategory: CategoryTotal[];
}

const BASE = '/api/v1/finance';
const CATEGORIES = `${BASE}/categories`;
const ENTRIES = `${BASE}/entries`;

/** Cliente de financas (espelha o service-finance), escopado pelo JWT do {@link ApiClient}. */
export class FinanceApi {
  constructor(private readonly client: ApiClient) {}

  listCategories(): Promise<Category[]> {
    return this.client.get<Category[]>(CATEGORIES);
  }

  getCategory(id: string): Promise<Category> {
    return this.client.get<Category>(`${CATEGORIES}/${id}`);
  }

  createCategory(input: CategoryInput): Promise<Category> {
    return this.client.post<Category>(CATEGORIES, input);
  }

  updateCategory(id: string, input: CategoryInput): Promise<Category> {
    return this.client.put<Category>(`${CATEGORIES}/${id}`, input);
  }

  removeCategory(id: string): Promise<void> {
    return this.client.delete<void>(`${CATEGORIES}/${id}`);
  }

  listEntries(): Promise<FinanceEntry[]> {
    return this.client.get<FinanceEntry[]>(ENTRIES);
  }

  getEntry(id: string): Promise<FinanceEntry> {
    return this.client.get<FinanceEntry>(`${ENTRIES}/${id}`);
  }

  createEntry(input: FinanceEntryInput): Promise<FinanceEntry> {
    return this.client.post<FinanceEntry>(ENTRIES, input);
  }

  updateEntry(id: string, input: FinanceEntryInput): Promise<FinanceEntry> {
    return this.client.put<FinanceEntry>(`${ENTRIES}/${id}`, input);
  }

  removeEntry(id: string): Promise<void> {
    return this.client.delete<void>(`${ENTRIES}/${id}`);
  }

  /** Resumo do mes informado (formato `YYYY-MM`); sem argumento, o backend usa o mes atual. */
  summary(month?: string): Promise<MonthlySummary> {
    const path = month ? `${BASE}/summary?month=${encodeURIComponent(month)}` : `${BASE}/summary`;
    return this.client.get<MonthlySummary>(path);
  }
}
