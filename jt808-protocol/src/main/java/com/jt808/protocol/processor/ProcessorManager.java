package com.jt808.protocol.processor;

import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.processor.impl.MessageRouter;
import com.jt808.protocol.processor.impl.MessageValidator;
import com.jt808.protocol.processor.impl.PerformanceMonitor;
import com.jt808.protocol.processor.impl.SessionHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理器管理器
 * 负责管理和协调所有消息处理器的工作
 */
public class ProcessorManager {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorManager.class);
    
    private final Vertx vertx;
    private final MessageProcessorChain processorChain;
    private final Map<String, MessageProcessor> processors;
    private final JsonObject config;
    
    // 各个处理器实例
    private PerformanceMonitor performanceMonitor;
    private MessageValidator messageValidator;
    private SessionHandler sessionHandler;
    private MessageRouter messageRouter;
    
    public ProcessorManager(Vertx vertx) {
        this(vertx, new JsonObject());
    }
    
    public ProcessorManager(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        this.processorChain = new MessageProcessorChain(vertx);
        this.processors = new ConcurrentHashMap<>();
        
        initializeProcessors();
    }
    
    /**
     * 初始化所有处理器
     */
    private void initializeProcessors() {
        try {
            // 创建性能监控器
            boolean enableDetailedLogging = config.getBoolean("performance.detailedLogging", false);
            performanceMonitor = new PerformanceMonitor(enableDetailedLogging);
            addProcessor(performanceMonitor);
            
            // 创建消息验证器
            boolean strictValidation = config.getBoolean("validation.strictMode", false);
            messageValidator = new MessageValidator(strictValidation);
            addProcessor(messageValidator);
            
            // 创建会话处理器
            long sessionTimeout = config.getLong("session.timeoutMs", 30 * 60 * 1000L);
            sessionHandler = new SessionHandler(sessionTimeout);
            addProcessor(sessionHandler);
            
            // 创建消息路由器
            messageRouter = new MessageRouter();
            addProcessor(messageRouter);
            
            logger.info("Initialized ProcessorManager with {} processors", processors.size());
            
        } catch (Exception e) {
            logger.error("Failed to initialize processors", e);
            throw new RuntimeException("Processor initialization failed", e);
        }
    }
    
    /**
     * 添加处理器到处理链
     */
    private void addProcessor(MessageProcessor processor) {
        processors.put(processor.getName(), processor);
        processorChain.addProcessor(processor);
        logger.debug("Added processor: {} with priority: {}", processor.getName(), processor.getPriority());
    }
    
    /**
     * 处理消息
     */
    public Future<List<ProcessResult>> processMessage(JT808Message message) {
        return processorChain.process(message)
            .onSuccess(results -> {
                // 记录处理完成事件到性能监控器
                if (performanceMonitor != null) {
                    ProcessContext context = createProcessContext(message);
                    results.forEach(result -> performanceMonitor.recordProcessingComplete(context, result));
                }
            })
            .onFailure(error -> {
                logger.error("Error processing message 0x{}", Integer.toHexString(message.getMessageId()), error);
            });
    }
    
    /**
     * 处理消息（使用指定上下文）
     */
    public Future<List<ProcessResult>> processMessage(ProcessContext context) {
        return processorChain.process(context)
            .onSuccess(results -> {
                // 记录处理完成事件到性能监控器
                if (performanceMonitor != null) {
                    results.forEach(result -> performanceMonitor.recordProcessingComplete(context, result));
                }
            })
            .onFailure(error -> {
                logger.error("Error processing message 0x{} with context {}", 
                        Integer.toHexString(context.getMessage().getMessageId()), 
                        context.getContextId(), error);
            });
    }
    
    /**
     * 创建处理上下文
     */
    private ProcessContext createProcessContext(JT808Message message) {
        String contextId = "ctx-" + System.currentTimeMillis() + "-" + message.getMessageId();
        return new ProcessContext(contextId, message, vertx, config);
    }
    
    /**
     * 获取处理器
     */
    public MessageProcessor getProcessor(String name) {
        return processors.get(name);
    }
    
    /**
     * 获取性能监控器
     */
    public PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }
    
    /**
     * 获取消息验证器
     */
    public MessageValidator getMessageValidator() {
        return messageValidator;
    }
    
    /**
     * 获取会话处理器
     */
    public SessionHandler getSessionHandler() {
        return sessionHandler;
    }
    
    /**
     * 获取消息路由器
     */
    public MessageRouter getMessageRouter() {
        return messageRouter;
    }
    
    /**
     * 获取处理器链
     */
    public MessageProcessorChain getProcessorChain() {
        return processorChain;
    }
    
    /**
     * 启用或禁用处理器
     */
    public boolean setProcessorEnabled(String processorName, boolean enabled) {
        MessageProcessor processor = processors.get(processorName);
        if (processor != null) {
            if (enabled) {
                if (!processorChain.getProcessorNames().contains(processorName)) {
                    processorChain.addProcessor(processor);
                }
            } else {
                processorChain.removeProcessor(processorName);
            }
            logger.info("Processor {} {}", processorName, enabled ? "enabled" : "disabled");
            return true;
        }
        return false;
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig(JsonObject newConfig) {
        logger.info("Reloading processor configuration");
        
        // 更新配置
        config.clear().mergeIn(newConfig);
        
        // 重新初始化处理器（如果需要）
        // 这里可以根据配置变化决定是否需要重新创建处理器
        
        logger.info("Configuration reloaded successfully");
    }
    
    /**
     * 获取系统状态
     */
    public JsonObject getSystemStatus() {
        JsonObject status = new JsonObject();
        
        // 基本信息
        status.put("processorsCount", processors.size());
        status.put("activeProcessors", processorChain.getProcessorNames().size());
        
        // 性能统计
        if (performanceMonitor != null) {
            status.put("performance", performanceMonitor.generatePerformanceReport());
        }
        
        // 会话统计
        if (sessionHandler != null) {
            status.put("activeSessions", sessionHandler.getActiveSessionCount());
        }
        
        // 验证统计
        if (messageValidator != null) {
            status.put("validation", messageValidator.getValidationStats());
        }
        
        // 处理器链统计
        JsonObject chainStats = new JsonObject();
        processorChain.getAllStats().forEach((name, stats) -> {
            JsonObject processorStats = new JsonObject()
                    .put("totalCount", stats.getTotalCount())
                    .put("successCount", stats.getSuccessCount())
                    .put("failedCount", stats.getFailedCount())
                    .put("successRate", stats.getSuccessRate())
                    .put("averageDuration", stats.getAverageDuration());
            chainStats.put(name, processorStats);
        });
        status.put("processorChain", chainStats);
        
        return status;
    }
    
    /**
     * 重置所有统计信息
     */
    public void resetStats() {
        if (performanceMonitor != null) {
            performanceMonitor.resetStats();
        }
        processorChain.clearStats();
        logger.info("All processor statistics reset");
    }
    
    /**
     * 关闭处理器管理器
     */
    public void shutdown() {
        logger.info("Shutting down ProcessorManager");
        
        // 清理资源
        processors.clear();
        
        // 这里可以添加其他清理逻辑
        
        logger.info("ProcessorManager shutdown completed");
    }
    
    /**
     * 健康检查
     */
    public boolean isHealthy() {
        try {
            // 检查关键处理器是否正常
            boolean healthy = processors.size() >= 4 && // 至少有4个核心处理器
                             performanceMonitor != null &&
                             messageValidator != null &&
                             sessionHandler != null &&
                             messageRouter != null;
            
            if (!healthy) {
                logger.warn("System health check failed - missing critical processors");
            }
            
            return healthy;
        } catch (Exception e) {
            logger.error("Health check failed with exception", e);
            return false;
        }
    }
}