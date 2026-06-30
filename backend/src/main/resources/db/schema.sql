-- 创建数据库
CREATE DATABASE IF NOT EXISTS lib_sys DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lib_sys;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码BCrypt加密',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0 COMMENT '软删除标志'
) ENGINE=InnoDB COMMENT='用户信息表';

-- 2. 角色表
CREATE TABLE IF NOT EXISTS t_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名',
    code VARCHAR(50) UNIQUE COMMENT '角色代码',
    description VARCHAR(255) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='角色表';

-- 3. 用户角色关联表
CREATE TABLE IF NOT EXISTS t_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB COMMENT='用户角色关联';

-- 4. 图书分类表
CREATE TABLE IF NOT EXISTS t_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    code VARCHAR(50) UNIQUE COMMENT '分类编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0
) ENGINE=InnoDB COMMENT='图书分类表';

-- 5. 图书信息表
CREATE TABLE IF NOT EXISTS t_book (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20) UNIQUE COMMENT 'ISBN号',
    title VARCHAR(100) NOT NULL COMMENT '书名',
    author VARCHAR(100) COMMENT '作者',
    publisher VARCHAR(100) COMMENT '出版社',
    publish_date DATE COMMENT '出版日期',
    total_count INT DEFAULT 0 COMMENT '总馆藏数',
    available_count INT DEFAULT 0 COMMENT '当前可借数',
    category_id BIGINT COMMENT '分类ID',
    cover_url VARCHAR(255) COMMENT '封面URL',
    location_code VARCHAR(20) COMMENT '索书号位置',
    description TEXT COMMENT '图书简介',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '价格',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0,
    INDEX idx_category (category_id),
    INDEX idx_title (title)
) ENGINE=InnoDB COMMENT='图书信息表';

-- 6. 借阅记录表
CREATE TABLE IF NOT EXISTS t_borrow_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL COMMENT '借阅人ID',
    borrow_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借出时间',
    due_date DATETIME NOT NULL COMMENT '应还时间',
    return_date DATETIME COMMENT '实际归还时间',
    status TINYINT DEFAULT 0 COMMENT '状态 0-借阅中 1-已还 2-逾期 3-丢失',
    fine_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '罚款金额',
    fine_paid TINYINT DEFAULT 0 COMMENT '罚款是否已缴 0-未缴 1-已缴',
    renew_count INT DEFAULT 0 COMMENT '续借次数',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status),
    INDEX idx_book_status (book_id, status),
    INDEX idx_borrow_date (borrow_date),
    FOREIGN KEY (book_id) REFERENCES t_book(id),
    FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB COMMENT='借阅记录表';

-- 7. 系统日志表
CREATE TABLE IF NOT EXISTS t_sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) COMMENT '操作用户',
    operation VARCHAR(100) COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    cost_time BIGINT COMMENT '耗时(ms)',
    status TINYINT DEFAULT 1 COMMENT '状态 0-失败 1-成功',
    error_msg TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='系统操作日志表';

-- 初始化数据
-- 插入角色
INSERT INTO t_role (name, code, description) VALUES
('超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限'),
('图书管理员', 'LIBRARIAN', '图书管理员，负责图书管理和借阅操作'),
('普通读者', 'READER', '普通读者，只能借阅和查询图书');

-- 插入管理员用户 (密码: admin123，BCrypt加密)
INSERT INTO t_user (username, password, nickname, email, status) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 'a****@********', 1),
('librarian', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '图书管理员', 'l********@********', 1),
('reader', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '普通读者', 'r*****@********', 1);

-- 分配角色
INSERT INTO t_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3);

-- 插入图书分类
INSERT INTO t_category (name, code, parent_id, sort_order) VALUES
('计算机科学', 'COMPUTER', 0, 1),
('文学', 'LITERATURE', 0, 2),
('历史', 'HISTORY', 0, 3),
('经济管理', 'ECONOMY', 0, 4),
('自然科学', 'SCIENCE', 0, 5),
('编程语言', 'PROGRAMMING', 1, 1),
('数据库', 'DATABASE', 1, 2),
('网络技术', 'NETWORK', 1, 3),
('小说', 'NOVEL', 2, 1),
('散文', 'PROSE', 2, 2);

-- 插入示例图书
INSERT INTO t_book (isbn, title, author, publisher, total_count, available_count, category_id, location_code, description, price) VALUES
('9787111213826', '深入理解计算机系统', 'Randal E.Bryant', '机械工业出版社', 5, 5, 1, 'TP3-01/123', '从程序员的视角详细阐述计算机系统的本质概念', 99.00),
('9787115428028', '算法导论', 'Thomas H.Cormen', '人民邮电出版社', 3, 3, 1, 'TP311.1-01/456', '全面、深入地介绍了计算机算法领域的核心知识', 128.00),
('9787111544937', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', 8, 8, 6, 'TP312JA-01/789', 'Java学习经典著作', 108.00),
('9787115357854', '高性能MySQL', 'Baron Schwartz', '人民邮电出版社', 4, 4, 7, 'TP311.138-01/234', 'MySQL性能优化权威指南', 128.00),
('9787020002207', '红楼梦', '曹雪芹', '人民文学出版社', 10, 10, 9, 'I242.47/1', '中国古典四大名著之一', 59.70),
('9787020042485', '三国演义', '罗贯中', '人民文学出版社', 10, 10, 9, 'I242.47/2', '中国古典四大名著之一', 39.50),
('9787100000000', '史记', '司马迁', '中华书局', 6, 6, 3, 'K204.2/1', '中国第一部纪传体通史', 98.00),
('9787111111111', '经济学原理', '曼昆', '北京大学出版社', 5, 5, 4, 'F0-01/1', '经济学入门经典教材', 88.00),
('9787532732746', '百年孤独', '加西亚·马尔克斯', '上海译文出版社', 7, 7, 9, 'I775.45/1', '魔幻现实主义文学代表作', 39.00),
('9787115222222', '计算机网络', '谢希仁', '电子工业出版社', 6, 6, 8, 'TP393-01/1', '计算机网络经典教材', 49.00);
