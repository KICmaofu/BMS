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
  DatePicker,
  message,
  Popconfirm,
  Tag,
} from 'antd';
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import {
  getBookPage,
  addBook,
  updateBook,
  deleteBook,
  getCategoryList,
  BookQueryParams,
} from '@/api/book';
import { Book, Category } from '@/types';
import { useUserStore } from '@/store/userStore';
import dayjs from 'dayjs';

const BookList = () => {
  const [data, setData] = useState<Book[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState<number | undefined>();
  const [categories, setCategories] = useState<Category[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<Book | null>(null);
  const [form] = Form.useForm();
  const { hasAnyRole } = useUserStore();
  const canEdit = hasAnyRole(['ADMIN', 'LIBRARIAN']);

  const fetchData = async () => {
    setLoading(true);
    try {
      const params: BookQueryParams = {
        pageNum,
        pageSize,
        keyword: keyword || undefined,
        categoryId,
      };
      const result = await getBookPage(params);
      setData(result.rows);
      setTotal(result.total);
    } catch (error) {
      console.error('获取图书列表失败', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const data = await getCategoryList();
      setCategories(data);
    } catch (error) {
      console.error('获取分类列表失败', error);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    fetchData();
  }, [pageNum, pageSize, categoryId]);

  const handleSearch = () => {
    setPageNum(1);
    fetchData();
  };

  const handleAdd = () => {
    setEditingRecord(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Book) => {
    setEditingRecord(record);
    form.setFieldsValue({
      ...record,
      publishDate: record.publishDate ? dayjs(record.publishDate) : undefined,
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteBook(id);
      message.success('删除成功');
      fetchData();
    } catch (error) {
      console.error('删除失败', error);
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const submitData = {
        ...values,
        publishDate: values.publishDate ? values.publishDate.format('YYYY-MM-DD') : undefined,
      };

      if (editingRecord) {
        await updateBook(editingRecord.id, submitData);
        message.success('修改成功');
      } else {
        await addBook(submitData);
        message.success('添加成功');
      }
      setModalVisible(false);
      fetchData();
    } catch (error) {
      console.error('提交失败', error);
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: 'ISBN',
      dataIndex: 'isbn',
      width: 140,
    },
    {
      title: '书名',
      dataIndex: 'title',
      ellipsis: true,
    },
    {
      title: '作者',
      dataIndex: 'author',
      width: 120,
    },
    {
      title: '分类',
      dataIndex: 'categoryName',
      width: 100,
      render: (text: string) => <Tag color="blue">{text || '-'}</Tag>,
    },
    {
      title: '馆藏数',
      dataIndex: 'totalCount',
      width: 90,
    },
    {
      title: '可借数',
      dataIndex: 'availableCount',
      width: 90,
      render: (count: number) => (
        <Tag color={count > 0 ? 'green' : 'red'}>
          {count}
        </Tag>
      ),
    },
    {
      title: '位置',
      dataIndex: 'locationCode',
      width: 100,
    },
    ...(canEdit
      ? [
          {
            title: '操作',
            key: 'action',
            width: 150,
            render: (_: any, record: Book) => (
              <Space>
                <Button
                  type="link"
                  size="small"
                  icon={<EditOutlined />}
                  onClick={() => handleEdit(record)}
                >
                  编辑
                </Button>
                <Popconfirm
                  title="确定要删除这本图书吗？"
                  onConfirm={() => handleDelete(record.id)}
                  okText="确定"
                  cancelText="取消"
                >
                  <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                    删除
                  </Button>
                </Popconfirm>
              </Space>
            ),
          },
        ]
      : []),
  ];

  return (
    <div>
      <div className="page-header">
        <h2 className="page-title">图书管理</h2>
        {canEdit && (
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增图书
          </Button>
        )}
      </div>

      <div className="search-bar">
        <Space wrap>
          <Input
            placeholder="搜索书名/作者/ISBN"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: 240 }}
            onPressEnter={handleSearch}
          />
          <Select
            placeholder="选择分类"
            value={categoryId}
            onChange={(value) => {
              setCategoryId(value);
              setPageNum(1);
            }}
            style={{ width: 180 }}
            allowClear
          >
            {categories.map((item) => (
              <Select.Option key={item.id} value={item.id}>
                {item.name}
              </Select.Option>
            ))}
          </Select>
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
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
        title={editingRecord ? '编辑图书' : '新增图书'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="title"
            label="书名"
            rules={[{ required: true, message: '请输入书名' }]}
          >
            <Input placeholder="请输入书名" />
          </Form.Item>
          <div style={{ display: 'flex', gap: 16 }}>
            <Form.Item name="isbn" label="ISBN" style={{ flex: 1 }}>
              <Input placeholder="请输入ISBN" />
            </Form.Item>
            <Form.Item name="author" label="作者" style={{ flex: 1 }}>
              <Input placeholder="请输入作者" />
            </Form.Item>
          </div>
          <div style={{ display: 'flex', gap: 16 }}>
            <Form.Item name="publisher" label="出版社" style={{ flex: 1 }}>
              <Input placeholder="请输入出版社" />
            </Form.Item>
            <Form.Item name="publishDate" label="出版日期" style={{ flex: 1 }}>
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
          </div>
          <div style={{ display: 'flex', gap: 16 }}>
            <Form.Item name="categoryId" label="分类" style={{ flex: 1 }}>
              <Select placeholder="请选择分类">
                {categories.map((item) => (
                  <Select.Option key={item.id} value={item.id}>
                    {item.name}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item name="totalCount" label="馆藏数量" style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
          </div>
          <div style={{ display: 'flex', gap: 16 }}>
            <Form.Item name="locationCode" label="索书号位置" style={{ flex: 1 }}>
              <Input placeholder="请输入索书号" />
            </Form.Item>
            <Form.Item name="price" label="价格(元)" style={{ flex: 1 }}>
              <InputNumber min={0} step={0.01} style={{ width: '100%' }} />
            </Form.Item>
          </div>
          <Form.Item name="description" label="图书简介">
            <Input.TextArea rows={3} placeholder="请输入图书简介" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default BookList;
