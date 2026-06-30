import request from './request';
import { Book, PageResult, Category } from '@/types';

export interface BookQueryParams {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  categoryId?: number;
}

export const getBookPage = (params: BookQueryParams): Promise<PageResult<Book>> => {
  return request.get('/book/page', { params });
};

export const getBookById = (id: number): Promise<Book> => {
  return request.get(`/book/${id}`);
};

export const addBook = (data: Partial<Book>): Promise<void> => {
  return request.post('/book', data);
};

export const updateBook = (id: number, data: Partial<Book>): Promise<void> => {
  return request.put(`/book/${id}`, data);
};

export const deleteBook = (id: number): Promise<void> => {
  return request.delete(`/book/${id}`);
};

export const getHotBooks = (limit?: number): Promise<Book[]> => {
  return request.get('/book/hot', { params: { limit } });
};

export const getCategoryList = (): Promise<Category[]> => {
  return request.get('/category/list');
};

export const addCategory = (data: Partial<Category>): Promise<void> => {
  return request.post('/category', data);
};

export const updateCategory = (id: number, data: Partial<Category>): Promise<void> => {
  return request.put(`/category/${id}`, data);
};

export const deleteCategory = (id: number): Promise<void> => {
  return request.delete(`/category/${id}`);
};
