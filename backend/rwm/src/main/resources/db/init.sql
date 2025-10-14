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
    fitness_goal JSON COMMENT '健身目标(JSON): {"weeklyDistanceKm":10.0,...}',
    weekly_availability VARCHAR(500) COMMENT '每周可用时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);

-- 创建运动记录表（重构版：使用JSON存储动态数据）
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
    
    -- JSON动态数据列：存储运动过程中的所有时序数据
    workout_data JSON COMMENT '运动动态数据: {
        "route": [{"lat": 39.904, "lng": 116.407, "altitude": 50, "timestamp": "2025-01-01T10:00:00", "sequence": 1}],
        "speed_samples": [{"speed": 10.5, "timestamp": "2025-01-01T10:00:00"}],
        "heart_rate_samples": [{"heart_rate": 150, "timestamp": "2025-01-01T10:00:00"}],
        "elevation_samples": [{"elevation": 50, "timestamp": "2025-01-01T10:00:00"}],
        "pace_samples": [{"pace": 350, "timestamp": "2025-01-01T10:00:00"}],
        "cadence_samples": [{"cadence": 180, "timestamp": "2025-01-01T10:00:00"}],
        "location_accuracy": [{"accuracy": 5.0, "timestamp": "2025-01-01T10:00:00"}]
    }',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    -- 基础索引
    INDEX idx_workouts_user_id (user_id),
    INDEX idx_workouts_workout_type (workout_type),
    INDEX idx_workouts_start_time (start_time),
    INDEX idx_workouts_status (status),
    INDEX idx_workouts_visibility (visibility),
    INDEX idx_workouts_group_id (group_id),
    INDEX idx_workouts_goal_achieved (goal_achieved),
    INDEX idx_workouts_created_at (created_at),
    INDEX idx_workouts_distance (distance),
    INDEX idx_workouts_duration (duration),
    
    -- JSON列性能索引（MySQL 8.0+支持）
    INDEX idx_workout_data_route_count ((JSON_LENGTH(workout_data->'$.route'))),
    INDEX idx_workout_data_start_lat ((CAST(workout_data->'$.route[0].lat' AS DECIMAL(10,8)))),
    INDEX idx_workout_data_start_lng ((CAST(workout_data->'$.route[0].lng' AS DECIMAL(11,8))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运动记录表（JSON重构版）';

-- 删除旧的路线轨迹表
DROP TABLE IF EXISTS workout_routes;

-- 群组表
CREATE TABLE IF NOT EXISTS `groups` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    owner_id BIGINT NOT NULL,
    member_limit INT DEFAULT 6,
    coupon_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    INDEX idx_groups_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组表';

-- 群组成员表
CREATE TABLE IF NOT EXISTS `group_members` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('ADMIN','MEMBER') DEFAULT 'MEMBER',
    joined_at DATETIME,
    weekly_like_count INT DEFAULT 0,
    weekly_remind_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    UNIQUE KEY uk_group_user (group_id, user_id),
    INDEX idx_group_members_group (group_id),
    INDEX idx_group_members_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组成员表';

-- 入群申请表
CREATE TABLE IF NOT EXISTS `group_join_applications` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    applicant_user_id BIGINT NOT NULL,
    inviter_user_id BIGINT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    INDEX idx_group_join_app_group (group_id),
    INDEX idx_group_join_app_applicant (applicant_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入群申请表';

-- 群组每周统计
CREATE TABLE IF NOT EXISTS `group_weekly_stats` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    week_start DATE NOT NULL,
    week_end DATE NOT NULL,
    weekly_points INT DEFAULT 0,
    total_points INT DEFAULT 0,
    coupon_earned INT DEFAULT 0,
    full_attendance_bonus_applied BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_group_week (group_id, week_start),
    INDEX idx_group_weekly_stats_group (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组每周统计';

-- 用户每周贡献约束（防刷分）
CREATE TABLE IF NOT EXISTS `user_weekly_contributions` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    week_start DATE NOT NULL,
    week_end DATE NOT NULL,
    individual_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_week (user_id, week_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户每周贡献约束';

-- 通知表
CREATE TABLE IF NOT EXISTS `notifications` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(100),
    content VARCHAR(500),
    `read` BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';