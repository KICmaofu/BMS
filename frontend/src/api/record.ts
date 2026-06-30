import request from './request';
import { BorrowRecord, PageResult, DashboardStats } from '@/types';

export interface RecordQueryParams {
  pageNum: number;
  pageSize: number;
  userId?: number;
  bookId?: number;
  status?: number;
  keyword?: string;
}

export interface BorrowParams {
  bookId: number;
  userId: number;
  borrowDays?: number;
}

export const getRecordPage = (params: RecordQueryParams): Promise<PageResult<BorrowRecord>> => {
  return request.get('/record/page', { params });
};

export const getRecordById = (id: number): Promise<BorrowRecord> => {
  return request.get(`/record/${id}`);
};

export const borrowBook = (data: BorrowParams): Promise<void> => {
  return request.post('/record/borrow', data);
};

export const returnBook = (id: number): Promise<void> => {
  return request.put(`/record/return/${id}`);
};

export const renewBook = (id: number, days?: number): Promise<void> => {
  return request.put(`/record/renew/${id}`, null, { params: { days } });
};

export const markLost = (id: number): Promise<void> => {
  return request.put(`/record/lost/${id}`);
};

export const payFine = (id: number): Promise<void> => {
  return request.put(`/record/pay-fine/${id}`);
};

export const getMyBorrowingCount = (userId: number): Promise<number> => {
  return request.get('/record/my/count', { params: { userId } });
};

export const getDashboardStats = (): Promise<DashboardStats> => {
  return request.get('/dashboard/stats');
};
