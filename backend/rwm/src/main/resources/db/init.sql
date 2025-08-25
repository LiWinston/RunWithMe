-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(30) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密后）',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    first_name VARCHAR(50) NOT NULL COMMENT '姓',
    last_name VARCHAR(50) NOT NULL COMMENT '名',
    gender ENUM('MALE', 'FEMALE', 'OTHER') COMMENT '性别',
    age INT COMMENT '年龄',
    phone_number VARCHAR(20) COMMENT '手机号',
    fitness_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') COMMENT '健身水平',
    height DECIMAL(5,2) COMMENT '身高(cm)',
    weight DECIMAL(5,2) COMMENT '体重(kg)',
    fitness_goal TEXT COMMENT '健身目标',
    weekly_availability VARCHAR(500) COMMENT '每周可用时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);
