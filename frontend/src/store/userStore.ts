import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { LoginResult } from '@/types';
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/auth';

interface UserState {
  token: string;
  userInfo: LoginResult | null;
  login: (username: string, password: string) => Promise<void>;
  fetchUserInfo: () => Promise<void>;
  logout: () => Promise<void>;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
}

export const useUserStore = create<UserState>()(
  persist(
    (set, get) => ({
      token: '',
      userInfo: null,

      login: async (username: string, password: string) => {
        const result = await loginApi({ username, password });
        set({
          token: result.token,
          userInfo: result,
        });
        localStorage.setItem('token', result.token);
        localStorage.setItem('userInfo', JSON.stringify(result));
      },

      fetchUserInfo: async () => {
        try {
          const result = await getUserInfo();
          set({ userInfo: result });
          localStorage.setItem('userInfo', JSON.stringify(result));
        } catch (error) {
          console.error('获取用户信息失败', error);
        }
      },

      logout: async () => {
        try {
          await logoutApi();
        } catch (error) {
          console.error('登出失败', error);
        } finally {
          set({ token: '', userInfo: null });
          localStorage.removeItem('token');
          localStorage.removeItem('userInfo');
        }
      },

      hasRole: (role: string) => {
        const userInfo = get().userInfo;
        if (!userInfo?.roles) return false;
        return userInfo.roles.includes(role);
      },

      hasAnyRole: (roles: string[]) => {
        const userInfo = get().userInfo;
        if (!userInfo?.roles) return false;
        return roles.some(role => userInfo.roles.includes(role));
      },
    }),
    {
      name: 'user-store',
    }
  )
);
