import { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Space,
  Input,
  Select,
  Modal,
  Form,
  InputNumber,
  message,
  Tag,
  Popconfirm,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  ReloadOutlined,
  SyncOutlined,
  ExclamationCircleOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import {
  getRecordPage,
  borrowBook,
  returnBook,
  renewBook,
  markLost,
  payFine,
  RecordQueryParams,
} from '@/api/record';
import { getBookPage } from '@/api/book';
import { getSimpleUserList } from '@/api/user';
import { BorrowRecord, Book, User } from '@/types';
import { useUserStore } from '@/store/userStore';
import dayjs from 'dayjs';

const BorrowList = () => {
  const [data, setData] = useState<BorrowRecord[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [status, setStatus] = useState<number | undefined>();
  const [keyword, setKeyword] = useState('');
  const [borrowModalVisible, setBorrowModalVisible] = useState(false);
  const [borrowForm] = Form.useForm();
  const [books, setBooks] = useState<Book[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const { hasAnyRole } = useUserStore();
  const canEdit = hasAnyRole(['ADMIN', 'LIBRARIAN']);

  const fetchData = async () => {
    setLoading(true);
    try {
      const params: RecordQueryParams = {
        pageNum,
        pageSize,
        status,
        keyword: keyword || undefined,
      };
      const result = await getRecordPage(params);
      setData(result.rows);
      setTotal(result.total);
    } catch (error) {
      console.error('获取借阅记录失败', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchBooks = async () => {
    try {
      const result = await getBookPage({ pageNum: 1, pageSize: 100 });
      setBooks(result.rows.filter((b) => b.availableCount > 0));
    } catch (error) {
      console.error('获取图书列表失败', error);
    }
  };

  const fetchUsers = async () => {
    try {
      const result = await getSimpleUserList();
      setUsers(result);
    } catch (error) {
      console.error('获取用户列表失败', error);
    }
  };

  useEffect(() => {
    fetchData();
  }, [pageNum, pageSize, status]);

  const handleSearch = () => {
    setPageNum(1);
    fetchData();
  };

  const handleBorrow = () => {
    borrowForm.resetFields();
    fetchBooks();
    fetchUsers();
    setBorrowModalVisible(true);
  };

  const handleBorrowSubmit = async () => {
    try {
      const values = await borrowForm.validateFields();
      await borrowBook(values);
      message.success('借阅成功');
      setBorrowModalVisible(false);
      fetchData();
    } catch (error) {
      console.error('借阅失败', error);
    }
  };

  const handleReturn = async (id: number) => {
    try {
      await returnBook(id);
      message.success('归还成功');
      fetchData();
    } catch (error) {
      console.error('归还失败', error);
    }
  };

  const handleRenew = async (id: number) => {
    try {
      await renewBook(id, 30);
      message.success('续借成功');
      fetchData();
    } catch (error) {
      console.error('续借失败', error);
    }
  };

  const handleLost = async (id: number) => {
    try {
      await markLost(id);
      message.success('已标记为丢失');
      fetchData();
    } catch (error) {
      console.error('操作失败', error);
    }
  };

  const handlePayFine = async (id: number) => {
    try {
      await payFine(id);
      message.success('罚款已缴纳');
      fetchData();
    } catch (error) {
      console.error('操作失败', error);
    }
  };

  const getStatusTag = (status: number) => {
    const statusMap: Record<number, { color: string; text: string }> = {
      0: { color: 'blue', text: '借阅中' },
      1: { color: 'green', text: '已归还' },
      2: { color: 'red', text: '已逾期' },
      3: { color: 'orange', text: '已丢失' },
    };
    const item = statusMap[status] || { color: 'default', text: '未知' };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: '图书名称',
      dataIndex: 'bookTitle',
      ellipsis: true,
    },
    {
      title: 'ISBN',
      dataIndex: 'bookIsbn',
      width: 140,
    },
    {
      title: '借阅人',
      dataIndex: 'userNickname',
      width: 100,
      render: (text: string, record: BorrowRecord) => text || record.username,
    },
    {
      title: '借阅时间',
      dataIndex: 'borrowDate',
      width: 170,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '应还时间',
      dataIndex: 'dueDate',
      width: 170,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (s: number) => getStatusTag(s),
    },
    {
      title: '罚款',
      dataIndex: 'fineAmount',
      width: 100,
      render: (amount: number, record: BorrowRecord) => {
        if (!amount || amount <= 0) return '-';
        return (
          <span style={{ color: '#f5222d', fontWeight: 'bold' }}>
            ¥{amount.toFixed(2)}
            {record.finePaid === 1 && (
              <Tag color="green" style={{ marginLeft: 4 }}>
                已缴
              </Tag>
            )}
          </span>
        );
      },
    },
    ...(canEdit
      ? [
          {
            title: '操作',
            key: 'action',
            width: 280,
            render: (_: any, record: BorrowRecord) => (
              <Space size="small" wrap>
                {(record.status === 0 || record.status === 2) && (
                  <>
                    <Popconfirm
                      title="确认归还图书？"
                      onConfirm={() => handleReturn(record.id)}
                    >
                      <Button
                        type="link"
                        size="small"
                        icon={<CheckCircleOutlined />}
                      >
                        归还
                      </Button>
                    </Popconfirm>
                    {record.renewCount < 2 && (
                      <Popconfirm
                        title="确认续借30天？"
                        onConfirm={() => handleRenew(record.id)}
                      >
                        <Button
                          type="link"
                          size="small"
                          icon={<SyncOutlined />}
                        >
                          续借
                        </Button>
                      </Popconfirm>
                    )}
                    <Popconfirm
                      title="确认标记为丢失？将按书价赔偿"
                      onConfirm={() => handleLost(record.id)}
                    >
                      <Button
                        type="link"
                        size="small"
                        danger
                        icon={<ExclamationCircleOutlined />}
                      >
                        丢失
                      </Button>
                    </Popconfirm>
                  </>
                )}
                {record.fineAmount &&
                  record.fineAmount > 0 &&
                  record.finePaid === 0 && (
                    <Popconfirm
                      title="确认缴纳罚款？"
                      onConfirm={() => handlePayFine(record.id)}
                    >
                      <Button type="link" size="small">
                        缴罚款
                      </Button>
                    </Popconfirm>
                  )}
              </Space>
            ),
          },
        ]
      : []),
  ];

  return (
    <div>
      <div className="page-header">
        <h2 className="page-title">借阅管理</h2>
        {canEdit && (
          <Button type="primary" icon={<PlusOutlined />} onClick={handleBorrow}>
            新建借阅
          </Button>
        )}
      </div>

      <div className="search-bar">
        <Space wrap>
          <Input
            placeholder="搜索图书/借阅人"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: 240 }}
            onPressEnter={handleSearch}
          />
          <Select
            placeholder="选择状态"
            value={status}
            onChange={(value) => {
              setStatus(value);
              setPageNum(1);
            }}
            style={{ width: 150 }}
            allowClear
          >
            <Select.Option value={0}>借阅中</Select.Option>
            <Select.Option value={1}>已归还</Select.Option>
            <Select.Option value={2}>已逾期</Select.Option>
            <Select.Option value={3}>已丢失</Select.Option>
          </Select>
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button icon={<ReloadOutlined />} onClick={fetchData}>
            刷新
          </Button>
        </Space>
      </div>

      <div className="table-container">
        <Table
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          pagination={{
            current: pageNum,
            pageSize,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (t) => `共 ${t} 条`,
            onChange: (page, size) => {
              setPageNum(page);
              setPageSize(size);
            },
          }}
        />
      </div>

      <Modal
        title="新建借阅"
        open={borrowModalVisible}
        onOk={handleBorrowSubmit}
        onCancel={() => setBorrowModalVisible(false)}
        destroyOnClose
      >
        <Form form={borrowForm} layout="vertical">
          <Form.Item
            name="bookId"
            label="选择图书"
            rules={[{ required: true, message: '请选择图书' }]}
          >
            <Select
              placeholder="请选择图书"
              showSearch
              optionFilterProp="children"
            >
              {books.map((book) => (
                <Select.Option key={book.id} value={book.id}>
                  {book.title} (可借: {book.availableCount})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="userId"
            label="选择借阅人"
            rules={[{ required: true, message: '请选择借阅人' }]}
          >
            <Select
              placeholder="请选择借阅人"
              showSearch
              optionFilterProp="children"
            >
              {users.map((user) => (
                <Select.Option key={user.id} value={user.id}>
                  {user.nickname || user.username}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="borrowDays"
            label="借阅天数"
            rules={[{ required: true, message: '请输入借阅天数' }]}
          >
            <InputNumber min={1} max={90} defaultValue={30} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default BorrowList;
