// Generic API wrapper matching backend ApiResponse<T>
export interface ApiResponse<T = unknown> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}

// Pagination
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// Error shape from backend
export interface ApiError {
  status: number;
  message: string;
  errors?: Record<string, string>;
}
