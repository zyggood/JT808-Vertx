package com.jt808.protocol.processor.impl;

import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.processor.MessageProcessor;
import com.jt808.protocol.processor.ProcessContext;
import com.jt808.protocol.processor.ProcessResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 性能监控处理器
 * 负责收集和统计消息处理的性能指标
 */
public class PerformanceMonitor implements MessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final String name;
    private final Map<Integer, MessageStats> messageStats;
    private final GlobalStats globalStats;
    private final boolean enableDetailedLogging;
    
    public PerformanceMonitor() {
        this(false);
    }
    
    public PerformanceMonitor(boolean enableDetailedLogging) {
        this.name = "PerformanceMonitor";
        this.messageStats = new ConcurrentHashMap<>();
        this.globalStats = new GlobalStats();
        this.enableDetailedLogging = enableDetailedLogging;
    }
    
    @Override
    public Future<ProcessResult> process(ProcessContext context) {
        long startTime = System.currentTimeMillis();
        Promise<ProcessResult> promise = Promise.promise();
        
        try {
            JT808Message message = context.getMessage();
            int messageId = message.getMessageId();
            
            // 记录消息开始处理时间
            context.setAttribute("monitorStartTime", startTime);
            
            // 更新统计信息
            updateMessageStats(messageId, startTime);
            updateGlobalStats(startTime);
            
            // 详细日志记录
            if (enableDetailedLogging) {
                logMessageDetails(message, context);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            promise.complete(ProcessResult.success(name, duration));
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error in performance monitoring", e);
            promise.complete(ProcessResult.failed(name, duration, e));
        }
        
        return promise.future();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public int getPriority() {
        return 5; // 性能监控应该有很高的优先级，尽早开始计时
    }
    
    @Override
    public boolean canProcess(JT808Message message) {
        return true; // 监控所有消息
    }
    
    /**
     * 更新消息统计信息
     */
    private void updateMessageStats(int messageId, long timestamp) {
        MessageStats stats = messageStats.computeIfAbsent(messageId, k -> new MessageStats(messageId));
        stats.recordMessage(timestamp);
    }
    
    /**
     * 更新全局统计信息
     */
    private void updateGlobalStats(long timestamp) {
        globalStats.recordMessage(timestamp);
    }
    
    /**
     * 记录消息详细信息
     */
    private void logMessageDetails(JT808Message message, ProcessContext context) {
        String terminalId = context.getAttribute("terminalId", "unknown");
        logger.debug("Processing message - ID: 0x{}, Terminal: {}, Context: {}, Time: {}",
                Integer.toHexString(message.getMessageId()),
                terminalId,
                context.getContextId(),
                LocalDateTime.now().format(TIME_FORMATTER));
    }
    
    /**
     * 记录消息处理完成
     */
    public void recordProcessingComplete(ProcessContext context, ProcessResult result) {
        Long startTime = context.getAttribute("monitorStartTime");
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            int messageId = context.getMessage().getMessageId();
            
            MessageStats stats = messageStats.get(messageId);
            if (stats != null) {
                stats.recordProcessingTime(duration, result.isSuccess());
            }
            
            globalStats.recordProcessingTime(duration, result.isSuccess());
            
            if (enableDetailedLogging) {
                logger.debug("Completed processing - ID: 0x{}, Duration: {}ms, Success: {}",
                        Integer.toHexString(messageId), duration, result.isSuccess());
            }
        }
    }
    
    /**
     * 获取指定消息类型的统计信息
     */
    public MessageStats getMessageStats(int messageId) {
        return messageStats.get(messageId);
    }
    
    /**
     * 获取全局统计信息
     */
    public GlobalStats getGlobalStats() {
        return globalStats;
    }
    
    /**
     * 获取所有消息统计信息
     */
    public Map<Integer, MessageStats> getAllMessageStats() {
        return new ConcurrentHashMap<>(messageStats);
    }
    
    /**
     * 生成性能报告
     */
    public JsonObject generatePerformanceReport() {
        JsonObject report = new JsonObject();
        
        // 全局统计
        JsonObject globalReport = new JsonObject()
                .put("totalMessages", globalStats.getTotalMessages())
                .put("successfulMessages", globalStats.getSuccessfulMessages())
                .put("failedMessages", globalStats.getFailedMessages())
                .put("averageProcessingTime", globalStats.getAverageProcessingTime())
                .put("maxProcessingTime", globalStats.getMaxProcessingTime())
                .put("minProcessingTime", globalStats.getMinProcessingTime())
                .put("messagesPerSecond", globalStats.getMessagesPerSecond())
                .put("successRate", globalStats.getSuccessRate());
        
        report.put("global", globalReport);
        
        // 按消息类型统计
        JsonObject messageTypeStats = new JsonObject();
        messageStats.forEach((messageId, stats) -> {
            JsonObject messageReport = new JsonObject()
                    .put("messageId", "0x" + Integer.toHexString(messageId))
                    .put("totalCount", stats.getTotalCount())
                    .put("successCount", stats.getSuccessCount())
                    .put("failedCount", stats.getFailedCount())
                    .put("averageProcessingTime", stats.getAverageProcessingTime())
                    .put("maxProcessingTime", stats.getMaxProcessingTime())
                    .put("minProcessingTime", stats.getMinProcessingTime())
                    .put("successRate", stats.getSuccessRate());
            
            messageTypeStats.put("0x" + Integer.toHexString(messageId), messageReport);
        });
        
        report.put("messageTypes", messageTypeStats);
        report.put("reportTime", LocalDateTime.now().format(TIME_FORMATTER));
        
        return report;
    }
    
    /**
     * 重置所有统计信息
     */
    public void resetStats() {
        messageStats.clear();
        globalStats.reset();
        logger.info("Performance statistics reset");
    }
    
    /**
     * 消息统计信息类
     */
    public static class MessageStats {
        private final int messageId;
        private final LongAdder totalCount = new LongAdder();
        private final LongAdder successCount = new LongAdder();
        private final LongAdder failedCount = new LongAdder();
        private final LongAdder totalProcessingTime = new LongAdder();
        private final AtomicLong maxProcessingTime = new AtomicLong(0);
        private final AtomicLong minProcessingTime = new AtomicLong(Long.MAX_VALUE);
        private volatile long firstMessageTime;
        private volatile long lastMessageTime;
        
        public MessageStats(int messageId) {
            this.messageId = messageId;
            this.firstMessageTime = System.currentTimeMillis();
            this.lastMessageTime = System.currentTimeMillis();
        }
        
        public void recordMessage(long timestamp) {
            if (firstMessageTime == 0) {
                firstMessageTime = timestamp;
            }
            lastMessageTime = timestamp;
        }
        
        public void recordProcessingTime(long duration, boolean success) {
            totalCount.increment();
            if (success) {
                successCount.increment();
            } else {
                failedCount.increment();
            }
            
            totalProcessingTime.add(duration);
            
            // 更新最大处理时间
            maxProcessingTime.updateAndGet(current -> Math.max(current, duration));
            
            // 更新最小处理时间
            minProcessingTime.updateAndGet(current -> Math.min(current, duration));
        }
        
        // Getters
        public int getMessageId() { return messageId; }
        public long getTotalCount() { return totalCount.sum(); }
        public long getSuccessCount() { return successCount.sum(); }
        public long getFailedCount() { return failedCount.sum(); }
        public long getMaxProcessingTime() { return maxProcessingTime.get(); }
        public long getMinProcessingTime() { 
            long min = minProcessingTime.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
        
        public double getAverageProcessingTime() {
            long total = getTotalCount();
            return total > 0 ? (double) totalProcessingTime.sum() / total : 0.0;
        }
        
        public double getSuccessRate() {
            long total = getTotalCount();
            return total > 0 ? (double) getSuccessCount() / total * 100 : 0.0;
        }
    }
    
    /**
     * 全局统计信息类
     */
    public static class GlobalStats {
        private final LongAdder totalMessages = new LongAdder();
        private final LongAdder successfulMessages = new LongAdder();
        private final LongAdder failedMessages = new LongAdder();
        private final LongAdder totalProcessingTime = new LongAdder();
        private final AtomicLong maxProcessingTime = new AtomicLong(0);
        private final AtomicLong minProcessingTime = new AtomicLong(Long.MAX_VALUE);
        private volatile long startTime;
        private volatile long lastMessageTime;
        
        public GlobalStats() {
            this.startTime = System.currentTimeMillis();
            this.lastMessageTime = System.currentTimeMillis();
        }
        
        public void recordMessage(long timestamp) {
            if (startTime == 0) {
                startTime = timestamp;
            }
            lastMessageTime = timestamp;
        }
        
        public void recordProcessingTime(long duration, boolean success) {
            totalMessages.increment();
            if (success) {
                successfulMessages.increment();
            } else {
                failedMessages.increment();
            }
            
            totalProcessingTime.add(duration);
            maxProcessingTime.updateAndGet(current -> Math.max(current, duration));
            minProcessingTime.updateAndGet(current -> Math.min(current, duration));
        }
        
        public void reset() {
            totalMessages.reset();
            successfulMessages.reset();
            failedMessages.reset();
            totalProcessingTime.reset();
            maxProcessingTime.set(0);
            minProcessingTime.set(Long.MAX_VALUE);
            startTime = System.currentTimeMillis();
            lastMessageTime = System.currentTimeMillis();
        }
        
        // Getters
        public long getTotalMessages() { return totalMessages.sum(); }
        public long getSuccessfulMessages() { return successfulMessages.sum(); }
        public long getFailedMessages() { return failedMessages.sum(); }
        public long getMaxProcessingTime() { return maxProcessingTime.get(); }
        public long getMinProcessingTime() { 
            long min = minProcessingTime.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
        
        public double getAverageProcessingTime() {
            long total = getTotalMessages();
            return total > 0 ? (double) totalProcessingTime.sum() / total : 0.0;
        }
        
        public double getSuccessRate() {
            long total = getTotalMessages();
            return total > 0 ? (double) getSuccessfulMessages() / total * 100 : 0.0;
        }
        
        public double getMessagesPerSecond() {
            long duration = lastMessageTime - startTime;
            return duration > 0 ? (double) getTotalMessages() / (duration / 1000.0) : 0.0;
        }
    }
}