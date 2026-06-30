import { Routes, Route, Navigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useUserStore } from '@/store/userStore';
import Login from '@/pages/Login';
import MainLayout from '@/layouts/MainLayout';
import Dashboard from '@/pages/Dashboard';
import BookList from '@/pages/BookList';
import CategoryList from '@/pages/CategoryList';
import BorrowList from '@/pages/BorrowList';
import UserList from '@/pages/UserList';
import NotFound from '@/pages/NotFound';

const PrivateRoute = ({ children }: { children: JSX.Element }) => {
  const token = useUserStore((state) => state.token);
  const userInfo = useUserStore((state) => state.userInfo);
  const fetchUserInfo = useUserStore((state) => state.fetchUserInfo);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const init = async () => {
      if (token && !userInfo) {
        try {
          await fetchUserInfo();
        } catch (error) {
          console.error('获取用户信息失败', error);
        }
      }
      setLoading(false);
    };
    init();
  }, [token, userInfo, fetchUserInfo]);

  if (loading) {
    return <div style={{ padding: '50px', textAlign: 'center' }}>加载中...</div>;
  }

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

const App = () => {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <PrivateRoute>
            <MainLayout />
          </PrivateRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="books" element={<BookList />} />
        <Route path="categories" element={<CategoryList />} />
        <Route path="borrow" element={<BorrowList />} />
        <Route path="users" element={<UserList />} />
      </Route>
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

export default App;
