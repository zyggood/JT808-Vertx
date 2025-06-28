package com.jt808.protocol.logging;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 结构化日志工具类
 * 提供统一的结构化日志输出格式
 */
public class StructuredLogger {
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    
    private final Logger logger;
    private final String component;
    
    public StructuredLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.component = clazz.getSimpleName();
    }
    
    public StructuredLogger(String name) {
        this.logger = LoggerFactory.getLogger(name);
        this.component = name;
    }
    
    /**
     * 创建日志构建器
     */
    public LogBuilder info() {
        return new LogBuilder(LogLevel.INFO);
    }
    
    public LogBuilder debug() {
        return new LogBuilder(LogLevel.DEBUG);
    }
    
    public LogBuilder warn() {
        return new LogBuilder(LogLevel.WARN);
    }
    
    public LogBuilder error() {
        return new LogBuilder(LogLevel.ERROR);
    }
    
    public LogBuilder trace() {
        return new LogBuilder(LogLevel.TRACE);
    }
    
    /**
     * 记录消息处理日志
     */
    public void logMessageProcessing(String action, String messageId, String terminalPhone, 
                                   long duration, boolean success, String details) {
        info()
            .event("message_processing")
            .field("action", action)
            .field("message_id", messageId)
            .field("terminal_phone", terminalPhone)
            .field("duration_ms", duration)
            .field("success", success)
            .field("details", details)
            .log();
    }
    
    /**
     * 记录性能指标
     */
    public void logPerformanceMetric(String operation, long duration, Map<String, Object> metrics) {
        LogBuilder builder = info()
            .event("performance_metric")
            .field("operation", operation)
            .field("duration_ms", duration);
        
        if (metrics != null) {
            metrics.forEach(builder::field);
        }
        
        builder.log();
    }
    
    /**
     * 记录错误日志
     */
    public void logError(String operation, Throwable error, Map<String, Object> context) {
        LogBuilder builder = error()
            .event("error")
            .field("operation", operation)
            .field("error_type", error.getClass().getSimpleName())
            .field("error_message", error.getMessage());
        
        if (context != null) {
            context.forEach(builder::field);
        }
        
        builder.exception(error).log();
    }
    
    /**
     * 记录连接事件
     */
    public void logConnectionEvent(String event, String remoteAddress, String terminalPhone, 
                                 Map<String, Object> details) {
        LogBuilder builder = info()
            .event("connection_event")
            .field("connection_event", event)
            .field("remote_address", remoteAddress)
            .field("terminal_phone", terminalPhone);
        
        if (details != null) {
            details.forEach(builder::field);
        }
        
        builder.log();
    }
    
    /**
     * 记录验证结果
     */
    public void logValidationResult(String validator, boolean success, int errorCount, 
                                  int warningCount, long duration) {
        info()
            .event("validation_result")
            .field("validator", validator)
            .field("success", success)
            .field("error_count", errorCount)
            .field("warning_count", warningCount)
            .field("duration_ms", duration)
            .log();
    }
    
    /**
     * 日志级别枚举
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
    
    /**
     * 日志构建器
     */
    public class LogBuilder {
        private final LogLevel level;
        private final Map<String, Object> fields;
        private String message;
        private String event;
        private Throwable exception;
        
        public LogBuilder(LogLevel level) {
            this.level = level;
            this.fields = new HashMap<>();
        }
        
        /**
         * 设置日志消息
         */
        public LogBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        /**
         * 设置事件类型
         */
        public LogBuilder event(String event) {
            this.event = event;
            return this;
        }
        
        /**
         * 添加字段
         */
        public LogBuilder field(String key, Object value) {
            if (key != null && value != null) {
                fields.put(key, value);
            }
            return this;
        }
        
        /**
         * 添加多个字段
         */
        public LogBuilder fields(Map<String, Object> fields) {
            if (fields != null) {
                this.fields.putAll(fields);
            }
            return this;
        }
        
        /**
         * 设置异常
         */
        public LogBuilder exception(Throwable exception) {
            this.exception = exception;
            return this;
        }
        
        /**
         * 输出日志
         */
        public void log() {
            JsonObject logEntry = createLogEntry();
            String logMessage = logEntry.encode();
            
            switch (level) {
                case TRACE:
                    if (exception != null) {
                        logger.trace(logMessage, exception);
                    } else {
                        logger.trace(logMessage);
                    }
                    break;
                case DEBUG:
                    if (exception != null) {
                        logger.debug(logMessage, exception);
                    } else {
                        logger.debug(logMessage);
                    }
                    break;
                case INFO:
                    if (exception != null) {
                        logger.info(logMessage, exception);
                    } else {
                        logger.info(logMessage);
                    }
                    break;
                case WARN:
                    if (exception != null) {
                        logger.warn(logMessage, exception);
                    } else {
                        logger.warn(logMessage);
                    }
                    break;
                case ERROR:
                    if (exception != null) {
                        logger.error(logMessage, exception);
                    } else {
                        logger.error(logMessage);
                    }
                    break;
            }
        }
        
        /**
         * 创建结构化日志条目
         */
        private JsonObject createLogEntry() {
            JsonObject logEntry = new JsonObject();
            
            // 基础字段
            logEntry.put("timestamp", ISO_FORMATTER.format(Instant.now()));
            logEntry.put("level", level.name());
            logEntry.put("component", component);
            
            // 事件类型
            if (event != null) {
                logEntry.put("event", event);
            }
            
            // 消息
            if (message != null) {
                logEntry.put("message", message);
            }
            
            // 自定义字段
            fields.forEach(logEntry::put);
            
            // 异常信息
            if (exception != null) {
                JsonObject errorInfo = new JsonObject();
                errorInfo.put("type", exception.getClass().getName());
                errorInfo.put("message", exception.getMessage());
                
                // 添加堆栈跟踪（仅在DEBUG级别以上）
                if (level == LogLevel.DEBUG || level == LogLevel.TRACE) {
                    StringBuilder stackTrace = new StringBuilder();
                    for (StackTraceElement element : exception.getStackTrace()) {
                        stackTrace.append(element.toString()).append("\n");
                    }
                    errorInfo.put("stack_trace", stackTrace.toString());
                }
                
                logEntry.put("error", errorInfo);
            }
            
            return logEntry;
        }
    }
    
    /**
     * 创建性能监控器
     */
    public PerformanceMonitor createPerformanceMonitor(String operation) {
        return new PerformanceMonitor(operation);
    }
    
    /**
     * 性能监控器
     */
    public class PerformanceMonitor {
        private final String operation;
        private final long startTime;
        private final Map<String, Object> metrics;
        
        public PerformanceMonitor(String operation) {
            this.operation = operation;
            this.startTime = System.currentTimeMillis();
            this.metrics = new HashMap<>();
        }
        
        /**
         * 添加指标
         */
        public PerformanceMonitor metric(String key, Object value) {
            metrics.put(key, value);
            return this;
        }
        
        /**
         * 完成监控并记录日志
         */
        public void finish() {
            long duration = System.currentTimeMillis() - startTime;
            logPerformanceMetric(operation, duration, metrics);
        }
        
        /**
         * 完成监控并记录日志（带结果）
         */
        public void finish(boolean success) {
            metrics.put("success", success);
            finish();
        }
        
        /**
         * 完成监控并记录日志（带结果和详情）
         */
        public void finish(boolean success, String details) {
            metrics.put("success", success);
            metrics.put("details", details);
            finish();
        }
    }
}