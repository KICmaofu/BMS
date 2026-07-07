import request from './request';
import { User, PageResult, Role } from '@/types';

export interface UserQueryParams {
  pageNum: number;
  pageSize: number;
  keyword?: string;
}

export const getUserPage = (params: UserQueryParams): Promise<PageResult<User>> => {
  return request.get('/user/page', { params });
};

export const getUserById = (id: number): Promise<User> => {
  return request.get(`/user/${id}`);
};

export const addUser = (data: any): Promise<void> => {
  return request.post('/user', data);
};

export const updateUser = (id: number, data: any): Promise<void> => {
  return request.put(`/user/${id}`, data);
};

export const deleteUser = (id: number): Promise<void> => {
  return request.delete(`/user/${id}`);
};

export const getRoleList = (): Promise<Role[]> => {
  return request.get('/user/roles');
};

export const getSimpleUserList = (): Promise<User[]> => {
  return request.get('/user/simple-list');
};
