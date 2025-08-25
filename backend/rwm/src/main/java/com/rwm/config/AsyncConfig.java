package com.rwm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * 为异步操作提供专用的线程池，避免与主线程争用资源
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * 缓存清理专用线程池
     * 用于异步清理过期的缓存数据，确保不影响主业务流程
     */
    @Bean("cacheCleanupExecutor")
    public Executor cacheCleanupExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 线程池配置
        executor.setCorePoolSize(2);              // 核心线程数
        executor.setMaxPoolSize(4);               // 最大线程数
        executor.setQueueCapacity(100);           // 队列容量
        executor.setKeepAliveSeconds(60);         // 线程空闲时间
        
        // 线程名称前缀
        executor.setThreadNamePrefix("cache-cleanup-");
        
        // 拒绝策略：调用者运行策略，确保任务不会丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // 初始化线程池
        executor.initialize();
        
        log.info("Cache cleanup thread pool initialized: core={}, max={}, queue={}", 
            executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * 通用异步任务线程池
     * 用于其他异步操作
     */
    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 线程池配置
        executor.setCorePoolSize(4);              // 核心线程数
        executor.setMaxPoolSize(8);               // 最大线程数
        executor.setQueueCapacity(200);           // 队列容量
        executor.setKeepAliveSeconds(60);         // 线程空闲时间
        
        // 线程名称前缀
        executor.setThreadNamePrefix("async-task-");
        
        // 拒绝策略：调用者运行策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // 初始化线程池
        executor.initialize();
        
        log.info("Async task thread pool initialized: core={}, max={}, queue={}", 
            executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}
