import request from './request';
import { LoginParams, LoginResult } from '@/types';

export const login = (data: LoginParams): Promise<LoginResult> => {
  return request.post('/auth/login', data);
};

export const getUserInfo = (): Promise<LoginResult> => {
  return request.get('/auth/info');
};

export const logout = (): Promise<void> => {
  return request.post('/auth/logout');
};
