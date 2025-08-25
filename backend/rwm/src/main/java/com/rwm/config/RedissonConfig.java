package com.rwm.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.net.URI;

/**
 * Redisson自定义配置
 * 解决Spring Data Redis URL格式与Redisson配置不兼容的问题
 */
@Slf4j
@Configuration
public class RedissonConfig {
    
    @Value("${spring.data.redis.url:rediss://red-croo37l6l47c73fqrus0:ww41TwtRqH8XFcvPOI0IogGwSSTmp9Ij@singapore-keyvalue.render.com:6379}")
    private String redisUrl;
    
    @Value("${spring.data.redis.host}")
    private String redisHost;
    
    @Value("${spring.data.redis.port}")
    private int redisPort;
    
    @Value("${spring.data.redis.password}")
    private String redisPassword;
    
    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;
    
    @Bean
    @Primary
    public RedissonClient redissonClient() {
        try {
            Config config = new Config();
            
            // 配置使用JsonJacksonCodec，避免JDK序列化的类加载器敏感问题
            config.setCodec(new JsonJacksonCodec());
            
            // 如果有URL配置，优先解析URL
            if (redisUrl != null && !redisUrl.trim().isEmpty()) {
                log.info("使用Redis URL配置Redisson: {}", maskPassword(redisUrl));
                configureFromUrl(config, redisUrl);
            } else {
                // 否则使用传统的host/port配置
                log.info("使用传统Redis配置Redisson: host={}, port={}", redisHost, redisPort);
                configureFromHostPort(config);
            }
            
            RedissonClient redissonClient = Redisson.create(config);
            log.info("Redisson客户端创建成功，使用JsonJacksonCodec编码器");
            return redissonClient;
            
        } catch (Exception e) {
            log.error("创建Redisson客户端失败: {}", e.getMessage(), e);
            throw new RuntimeException("Redisson客户端创建失败", e);
        }
    }
    
    /**
     * 从Redis URL配置Redisson
     */
    private void configureFromUrl(Config config, String url) {
        try {
            URI uri = new URI(url);
            
            // 解析协议
            String scheme = uri.getScheme();
            boolean useSsl = "rediss".equals(scheme);
            
            // 解析主机和端口
            String host = uri.getHost();
            int port = uri.getPort() != -1 ? uri.getPort() : 6379;
            
            // 解析用户名和密码
            String userInfo = uri.getUserInfo();
            String username = null;
            String password = null;
            
            if (userInfo != null) {
                String[] parts = userInfo.split(":");
                if (parts.length == 2) {
                    username = parts[0];
                    password = parts[1];
                } else if (parts.length == 1) {
                    password = parts[0]; // 只有密码，没有用户名
                }
            }
            
            // 解析数据库
            String path = uri.getPath();
            int database = 0;
            if (path != null && path.length() > 1) {
                try {
                    database = Integer.parseInt(path.substring(1));
                } catch (NumberFormatException e) {
                    log.warn("无法解析数据库编号: {}, 使用默认值0", path);
                }
            }
            
            // 构建Redis地址
            String redisAddress = (useSsl ? "rediss://" : "redis://") + host + ":" + port;
            
            SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(redisAddress)
                .setDatabase(database)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(10)
                .setConnectTimeout(10000)
                .setTimeout(10000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);
            
            // 设置SSL
            if (useSsl) {
                serverConfig.setSslEnableEndpointIdentification(true);
            }
            
            // 设置密码
            if (password != null && !password.trim().isEmpty()) {
                serverConfig.setPassword(password);
            }
            
            // 设置用户名（如果有）
            if (username != null && !username.trim().isEmpty()) {
                serverConfig.setUsername(username);
            }
            
            log.info("Redisson配置: address={}, database={}, ssl={}, hasAuth={}", 
                    redisAddress, database, useSsl, password != null);
                    
        } catch (Exception e) {
            log.error("解析Redis URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("Redis URL解析失败", e);
        }
    }
    
    /**
     * 从传统host/port配置Redisson
     */
    private void configureFromHostPort(Config config) {
        String address = "redis://" + redisHost + ":" + redisPort;
        
        SingleServerConfig serverConfig = config.useSingleServer()
            .setAddress(address)
            .setDatabase(redisDatabase)
            .setConnectionMinimumIdleSize(1)
            .setConnectionPoolSize(10)
            .setConnectTimeout(10000)
            .setTimeout(10000)
            .setRetryAttempts(3)
            .setRetryInterval(1500);
        
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            serverConfig.setPassword(redisPassword);
        }
    }
    
    /**
     * 屏蔽密码用于日志输出
     */
    private String maskPassword(String url) {
        if (url == null) return null;
        
        try {
            URI uri = new URI(url);
            String userInfo = uri.getUserInfo();
            if (userInfo != null && userInfo.contains(":")) {
                String maskedUserInfo = userInfo.split(":")[0] + ":***";
                return url.replace(userInfo, maskedUserInfo);
            }
        } catch (Exception e) {
            // 如果解析失败，返回通用屏蔽信息
            return url.replaceAll("://[^@]*@", "://***@");
        }
        
        return url;
    }
} 