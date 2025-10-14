package com.rwm.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * One-off lightweight DB migration runner.
 * It alters users.fitness_goal to JSON with default payload and creates new social tables if absent.
 * Safe to run multiple times (DDL uses IF NOT EXISTS; ALTER is guarded; UPDATE sets default only when empty or null).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OneOffDbMigrationRunner {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @PostConstruct
    public void migrate() {
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement st = conn.createStatement()) {
            log.info("[DB MIGRATION] Starting one-off migration...");

            // 1) Normalize existing fitness_goal values (for legacy text columns)
            try {
                String defaultJson = "{\\\"weeklyDistanceKm\\\":10.0}";
                st.executeUpdate("UPDATE users SET fitness_goal = '" + defaultJson + "' WHERE fitness_goal IS NULL OR JSON_VALID(fitness_goal) = 0");
                log.info("Filled default fitness_goal JSON for NULL/invalid rows (pre-alter)");
            } catch (SQLException e) {
                log.warn("Pre-alter default fill skipped: {}", e.getMessage());
            }

            // 1.1) users.fitness_goal -> JSON if not already
            // try: modify column type to JSON (ignore error if already JSON or already converted)
            try {
                st.executeUpdate("ALTER TABLE users MODIFY fitness_goal JSON");
                log.info("Altered users.fitness_goal to JSON");
            } catch (SQLException e) {
                log.warn("Modify users.fitness_goal to JSON skipped: {}", e.getMessage());
            }

            // 2) create tables if not exists
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `groups` (\n" +
                    "    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    name VARCHAR(100) NOT NULL,\n" +
                    "    owner_id BIGINT NOT NULL,\n" +
                    "    member_limit INT DEFAULT 6,\n" +
                    "    coupon_count INT DEFAULT 0,\n" +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                    "    deleted BOOLEAN DEFAULT FALSE,\n" +
                    "    INDEX idx_groups_owner (owner_id)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS `group_members` (\n" +
                    "    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    group_id BIGINT NOT NULL,\n" +
                    "    user_id BIGINT NOT NULL,\n" +
                    "    role ENUM('ADMIN','MEMBER') DEFAULT 'MEMBER',\n" +
                    "    joined_at DATETIME,\n" +
                    "    weekly_like_count INT DEFAULT 0,\n" +
                    "    weekly_remind_count INT DEFAULT 0,\n" +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                    "    deleted BOOLEAN DEFAULT FALSE,\n" +
                    "    UNIQUE KEY uk_group_user (group_id, user_id),\n" +
                    "    INDEX idx_group_members_group (group_id),\n" +
                    "    INDEX idx_group_members_user (user_id)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS `group_join_applications` (\n" +
                    "    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    group_id BIGINT NOT NULL,\n" +
                    "    applicant_user_id BIGINT NOT NULL,\n" +
                    "    inviter_user_id BIGINT NULL,\n" +
                    "    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',\n" +
                    "    reason VARCHAR(255),\n" +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                    "    deleted BOOLEAN DEFAULT FALSE,\n" +
                    "    INDEX idx_group_join_app_group (group_id),\n" +
                    "    INDEX idx_group_join_app_applicant (applicant_user_id)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS `group_weekly_stats` (\n" +
                    "    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    group_id BIGINT NOT NULL,\n" +
                    "    week_start DATE NOT NULL,\n" +
                    "    week_end DATE NOT NULL,\n" +
                    "    weekly_points INT DEFAULT 0,\n" +
                    "    total_points INT DEFAULT 0,\n" +
                    "    coupon_earned INT DEFAULT 0,\n" +
                    "    full_attendance_bonus_applied BOOLEAN DEFAULT FALSE,\n" +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                    "    UNIQUE KEY uk_group_week (group_id, week_start),\n" +
                    "    INDEX idx_group_weekly_stats_group (group_id)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS `user_weekly_contributions` (\n" +
                    "    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    user_id BIGINT NOT NULL,\n" +
                    "    group_id BIGINT NOT NULL,\n" +
                    "    week_start DATE NOT NULL,\n" +
                    "    week_end DATE NOT NULL,\n" +
                    "    individual_completed BOOLEAN DEFAULT FALSE,\n" +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                    "    UNIQUE KEY uk_user_week (user_id, week_start)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS `notifications` (\n" +
                    "    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    user_id BIGINT NOT NULL,\n" +
                    "    type VARCHAR(30) NOT NULL,\n" +
                    "    title VARCHAR(100),\n" +
                    "    content VARCHAR(500),\n" +
                    "    `read` BOOLEAN DEFAULT FALSE,\n" +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    INDEX idx_notifications_user (user_id),\n" +
                    "    INDEX idx_notifications_type (type)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            log.info("[DB MIGRATION] Completed.");
        } catch (SQLException e) {
            log.error("[DB MIGRATION] Failed: {}", e.getMessage(), e);
        }
    }
}
