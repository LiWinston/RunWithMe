package com.rwm.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Slf4j
@Configuration
public class DbAlterRunner {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    @PostConstruct
    public void alter() {
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement st = conn.createStatement()) {
            log.info("[DB ALTER] Start notifications table alter (idempotent)...");
            try { st.executeUpdate("ALTER TABLE `notifications` ADD COLUMN actor_user_id BIGINT NULL"); } catch (Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE `notifications` ADD COLUMN target_user_id BIGINT NULL"); } catch (Exception ignored) {}
            try { st.executeUpdate("ALTER TABLE `notifications` ADD COLUMN group_id BIGINT NULL"); } catch (Exception ignored) {}
            try { st.executeUpdate("CREATE INDEX idx_notifications_actor ON `notifications` (actor_user_id)"); } catch (Exception ignored) {}
            try { st.executeUpdate("CREATE INDEX idx_notifications_target ON `notifications` (target_user_id)"); } catch (Exception ignored) {}
            log.info("[DB ALTER] Completed.");
        } catch (Exception e) {
            log.error("[DB ALTER] Failed: {}", e.getMessage(), e);
        }
    }
}
