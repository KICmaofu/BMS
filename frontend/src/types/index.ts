export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  rows: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResult {
  token: string;
  userId: number;
  username: string;
  nickname: string;
  roles: string[];
}

export interface Book {
  id: number;
  isbn: string;
  title: string;
  author: string;
  publisher: string;
  publishDate: string;
  totalCount: number;
  availableCount: number;
  categoryId: number;
  categoryName: string;
  coverUrl: string;
  locationCode: string;
  description: string;
  price: number;
  createdAt: string;
}

export interface Category {
  id: number;
  name: string;
  code: string;
  parentId: number;
  sortOrder: number;
}

export interface User {
  id: number;
  username: string;
  nickname: string;
  email: string;
  phone: string;
  status: number;
  roles: string[];
  createdAt: string;
}

export interface Role {
  id: number;
  name: string;
  code: string;
  description: string;
}

export interface BorrowRecord {
  id: number;
  bookId: number;
  bookTitle: string;
  bookIsbn: string;
  userId: number;
  username: string;
  userNickname: string;
  borrowDate: string;
  dueDate: string;
  returnDate: string;
  status: number;
  statusText: string;
  fineAmount: number;
  finePaid: number;
  renewCount: number;
  remark: string;
}

export interface DashboardStats {
  totalBooks: number;
  totalUsers: number;
  totalBorrowing: number;
  totalOverdue: number;
}
