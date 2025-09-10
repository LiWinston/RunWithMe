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

-- 创建运动记录表
CREATE TABLE IF NOT EXISTS workouts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID（逻辑外键）',
    workout_type ENUM('OUTDOOR_RUN', 'TREADMILL', 'WALK', 'CYCLING', 'SWIMMING', 'OTHER') NOT NULL COMMENT '运动类型',
    distance DECIMAL(8,3) COMMENT '距离(km)',
    duration INT COMMENT '持续时间(秒)',
    steps INT COMMENT '步数',
    calories DECIMAL(8,2) COMMENT '卡路里',
    avg_speed DECIMAL(6,2) COMMENT '平均速度(km/h)',
    avg_pace INT COMMENT '平均配速(秒/km)',
    avg_heart_rate INT COMMENT '平均心率(bpm)',
    max_heart_rate INT COMMENT '最大心率(bpm)',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status ENUM('STARTED', 'PAUSED', 'COMPLETED', 'STOPPED') DEFAULT 'STARTED' COMMENT '运动状态',
    visibility ENUM('PUBLIC', 'GROUP', 'PRIVATE') DEFAULT 'PRIVATE' COMMENT '可见性',
    goal_achieved BOOLEAN DEFAULT FALSE COMMENT '是否达成日目标',
    group_id BIGINT COMMENT '关联群组ID（逻辑外键）',
    notes TEXT COMMENT '运动备注',
    weather_condition VARCHAR(50) COMMENT '天气条件',
    temperature DECIMAL(4,1) COMMENT '温度(℃)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_workouts_user_id (user_id),
    INDEX idx_workouts_workout_type (workout_type),
    INDEX idx_workouts_start_time (start_time),
    INDEX idx_workouts_status (status),
    INDEX idx_workouts_visibility (visibility),
    INDEX idx_workouts_group_id (group_id),
    INDEX idx_workouts_goal_achieved (goal_achieved),
    INDEX idx_workouts_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运动记录表';

-- 创建运动路线轨迹表
CREATE TABLE IF NOT EXISTS workout_routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workout_id BIGINT NOT NULL COMMENT '运动记录ID（逻辑外键）',
    latitude DECIMAL(10,8) NOT NULL COMMENT '纬度',
    longitude DECIMAL(11,8) NOT NULL COMMENT '经度',
    altitude DECIMAL(8,2) COMMENT '海拔(米)',
    accuracy DECIMAL(6,2) COMMENT 'GPS精度(米)',
    speed DECIMAL(6,2) COMMENT '当前速度(km/h)',
    heart_rate INT COMMENT '当前心率(bpm)',
    timestamp DATETIME NOT NULL COMMENT '记录时间戳',
    sequence_order INT NOT NULL COMMENT '路线点顺序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_workout_routes_workout_id (workout_id),
    INDEX idx_workout_routes_timestamp (timestamp),
    INDEX idx_workout_routes_sequence (workout_id, sequence_order),
    INDEX idx_workout_routes_location (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运动路线轨迹表';