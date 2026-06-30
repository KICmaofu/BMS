import { useEffect, useState } from 'react';
import { Card, Row, Col, Statistic, List, Tag, Space } from 'antd';
import {
  BookOutlined,
  UserOutlined,
  SwapOutlined,
  WarningOutlined,
} from '@ant-design/icons';
import { getDashboardStats } from '@/api/record';
import { getHotBooks } from '@/api/book';
import { DashboardStats, Book } from '@/types';

const Dashboard = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [hotBooks, setHotBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [statsData, booksData] = await Promise.all([
        getDashboardStats(),
        getHotBooks(10),
      ]);
      setStats(statsData);
      setHotBooks(booksData);
    } catch (error) {
      console.error('获取仪表盘数据失败', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>数据概览</h2>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="图书总数"
              value={stats?.totalBooks || 0}
              prefix={<BookOutlined style={{ color: '#1890ff' }} />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="注册用户"
              value={stats?.totalUsers || 0}
              prefix={<UserOutlined style={{ color: '#52c41a' }} />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="借阅中"
              value={stats?.totalBorrowing || 0}
              prefix={<SwapOutlined style={{ color: '#faad14' }} />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={loading}>
            <Statistic
              title="逾期数量"
              value={stats?.totalOverdue || 0}
              prefix={<WarningOutlined style={{ color: '#f5222d' }} />}
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col span={24}>
          <Card title="热门图书 TOP 10" loading={loading}>
            <List
              dataSource={hotBooks}
              renderItem={(item, index) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <Space>
                        <Tag
                          color={
                            index < 3 ? 'gold' : index < 6 ? 'blue' : 'default'
                          }
                        >
                          TOP {index + 1}
                        </Tag>
                        <span>{item.title}</span>
                      </Space>
                    }
                    description={
                      <Space size="large">
                        <span>作者：{item.author || '未知'}</span>
                        <span>出版社：{item.publisher || '未知'}</span>
                        <span>
                          馆藏：{item.totalCount} 本 / 可借：
                          {item.availableCount} 本
                        </span>
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
