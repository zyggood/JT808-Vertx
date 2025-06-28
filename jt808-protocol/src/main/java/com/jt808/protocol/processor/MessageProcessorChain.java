package com.jt808.protocol.processor;

import com.jt808.protocol.message.JT808Message;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息处理器链
 * 支持中间件模式的消息处理
 */
public class MessageProcessorChain {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessorChain.class);
    
    private final Vertx vertx;
    private final List<MessageProcessor> processors;
    private final Map<String, MessageProcessor> processorMap;
    private final AtomicLong contextIdGenerator;
    
    // 统计信息
    private final Map<String, ProcessorStats> stats;
    
    public MessageProcessorChain(Vertx vertx) {
        this.vertx = vertx;
        this.processors = new ArrayList<>();
        this.processorMap = new ConcurrentHashMap<>();
        this.contextIdGenerator = new AtomicLong(0);
        this.stats = new ConcurrentHashMap<>();
    }
    
    /**
     * 添加处理器
     */
    public MessageProcessorChain addProcessor(MessageProcessor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("Processor cannot be null");
        }
        
        if (processorMap.containsKey(processor.getName())) {
            throw new IllegalArgumentException("Processor with name '" + processor.getName() + "' already exists");
        }
        
        processors.add(processor);
        processorMap.put(processor.getName(), processor);
        stats.put(processor.getName(), new ProcessorStats());
        
        // 按优先级排序
        processors.sort(Comparator.comparingInt(MessageProcessor::getPriority));
        
        logger.info("Added processor: {} with priority: {}", processor.getName(), processor.getPriority());
        return this;
    }
    
    /**
     * 移除处理器
     */
    public boolean removeProcessor(String processorName) {
        MessageProcessor processor = processorMap.remove(processorName);
        if (processor != null) {
            processors.remove(processor);
            stats.remove(processorName);
            logger.info("Removed processor: {}", processorName);
            return true;
        }
        return false;
    }
    
    /**
     * 获取处理器
     */
    public MessageProcessor getProcessor(String processorName) {
        return processorMap.get(processorName);
    }
    
    /**
     * 获取所有处理器名称
     */
    public Set<String> getProcessorNames() {
        return new HashSet<>(processorMap.keySet());
    }
    
    /**
     * 处理消息
     */
    public Future<List<ProcessResult>> process(JT808Message message) {
        String contextId = "ctx-" + contextIdGenerator.incrementAndGet();
        ProcessContext context = new ProcessContext(contextId, message, vertx);
        return process(context);
    }
    
    /**
     * 处理消息（使用指定上下文）
     */
    public Future<List<ProcessResult>> process(ProcessContext context) {
        Promise<List<ProcessResult>> promise = Promise.promise();
        List<ProcessResult> results = new ArrayList<>();
        
        logger.debug("Starting message processing chain for context: {}", context.getContextId());
        
        processNext(context, 0, results, promise);
        
        return promise.future();
    }
    
    /**
     * 递归处理下一个处理器
     */
    private void processNext(ProcessContext context, int index, List<ProcessResult> results, Promise<List<ProcessResult>> promise) {
        if (index >= processors.size() || !context.shouldContinue()) {
            logger.debug("Processing chain completed for context: {}, total results: {}", 
                    context.getContextId(), results.size());
            promise.complete(results);
            return;
        }
        
        MessageProcessor processor = processors.get(index);
        context.setCurrentProcessorIndex(index);
        
        // 检查处理器是否可以处理此消息
        if (!processor.canProcess(context.getMessage())) {
            ProcessResult skipResult = ProcessResult.skipped(processor.getName(), "Cannot process this message type");
            results.add(skipResult);
            updateStats(processor.getName(), skipResult);
            
            // 继续下一个处理器
            processNext(context, index + 1, results, promise);
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        logger.debug("Processing message with processor: {} for context: {}", 
                processor.getName(), context.getContextId());
        
        // 执行处理器
        processor.process(context)
                .onSuccess(result -> {
                    long duration = System.currentTimeMillis() - startTime;
                    
                    // 更新统计信息
                    updateStats(processor.getName(), result);
                    
                    results.add(result);
                    context.setLastResult(result);
                    
                    logger.debug("Processor {} completed for context: {}, status: {}, duration: {}ms", 
                            processor.getName(), context.getContextId(), result.getStatus(), duration);
                    
                    // 如果处理失败且不应该继续，则停止处理链
                    if (result.isFailed() && !shouldContinueOnFailure(processor, result)) {
                        context.stopProcessing();
                    }
                    
                    // 继续下一个处理器
                    if (processor.isAsync()) {
                        vertx.runOnContext(v -> processNext(context, index + 1, results, promise));
                    } else {
                        processNext(context, index + 1, results, promise);
                    }
                })
                .onFailure(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    ProcessResult errorResult = ProcessResult.failed(processor.getName(), duration, error);
                    
                    updateStats(processor.getName(), errorResult);
                    results.add(errorResult);
                    context.setLastResult(errorResult);
                    
                    logger.error("Processor {} failed for context: {}, duration: {}ms", 
                            processor.getName(), context.getContextId(), duration, error);
                    
                    // 根据错误处理策略决定是否继续
                    if (shouldContinueOnError(processor, error)) {
                        processNext(context, index + 1, results, promise);
                    } else {
                        promise.fail(error);
                    }
                });
    }
    
    /**
     * 判断处理失败时是否应该继续
     */
    private boolean shouldContinueOnFailure(MessageProcessor processor, ProcessResult result) {
        // 默认策略：跳过的处理器继续，失败的处理器停止
        return result.isSkipped();
    }
    
    /**
     * 判断出现错误时是否应该继续
     */
    private boolean shouldContinueOnError(MessageProcessor processor, Throwable error) {
        // 默认策略：出现错误时停止处理链
        return false;
    }
    
    /**
     * 更新处理器统计信息
     */
    private void updateStats(String processorName, ProcessResult result) {
        ProcessorStats stat = stats.get(processorName);
        if (stat != null) {
            stat.update(result);
        }
    }
    
    /**
     * 获取处理器统计信息
     */
    public ProcessorStats getStats(String processorName) {
        return stats.get(processorName);
    }
    
    /**
     * 获取所有统计信息
     */
    public Map<String, ProcessorStats> getAllStats() {
        return new HashMap<>(stats);
    }
    
    /**
     * 清空统计信息
     */
    public void clearStats() {
        stats.values().forEach(ProcessorStats::reset);
    }
    
    /**
     * 处理器统计信息
     */
    public static class ProcessorStats {
        private long totalCount = 0;
        private long successCount = 0;
        private long failedCount = 0;
        private long skippedCount = 0;
        private long retryCount = 0;
        private long totalDuration = 0;
        private long maxDuration = 0;
        private long minDuration = Long.MAX_VALUE;
        
        public synchronized void update(ProcessResult result) {
            totalCount++;
            totalDuration += result.getProcessingDuration();
            
            if (result.getProcessingDuration() > maxDuration) {
                maxDuration = result.getProcessingDuration();
            }
            if (result.getProcessingDuration() < minDuration) {
                minDuration = result.getProcessingDuration();
            }
            
            switch (result.getStatus()) {
                case SUCCESS:
                    successCount++;
                    break;
                case FAILED:
                    failedCount++;
                    break;
                case SKIPPED:
                    skippedCount++;
                    break;
                case RETRY:
                    retryCount++;
                    break;
            }
        }
        
        public synchronized void reset() {
            totalCount = 0;
            successCount = 0;
            failedCount = 0;
            skippedCount = 0;
            retryCount = 0;
            totalDuration = 0;
            maxDuration = 0;
            minDuration = Long.MAX_VALUE;
        }
        
        // Getters
        public long getTotalCount() { return totalCount; }
        public long getSuccessCount() { return successCount; }
        public long getFailedCount() { return failedCount; }
        public long getSkippedCount() { return skippedCount; }
        public long getRetryCount() { return retryCount; }
        public long getTotalDuration() { return totalDuration; }
        public long getMaxDuration() { return maxDuration; }
        public long getMinDuration() { return minDuration == Long.MAX_VALUE ? 0 : minDuration; }
        
        public double getAverageDuration() {
            return totalCount > 0 ? (double) totalDuration / totalCount : 0;
        }
        
        public double getSuccessRate() {
            return totalCount > 0 ? (double) successCount / totalCount : 0;
        }
        
        @Override
        public String toString() {
            return String.format("ProcessorStats{total=%d, success=%d(%.2f%%), failed=%d, skipped=%d, retry=%d, avgDuration=%.2fms}",
                    totalCount, successCount, getSuccessRate() * 100, failedCount, skippedCount, retryCount, getAverageDuration());
        }
    }
}