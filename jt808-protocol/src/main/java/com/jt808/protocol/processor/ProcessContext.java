package com.jt808.protocol.processor;

import com.jt808.protocol.message.JT808Message;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息处理上下文
 * 包含消息处理过程中的所有相关信息
 */
public class ProcessContext {
    
    private final String contextId;
    private final JT808Message message;
    private final Vertx vertx;
    private final LocalDateTime createTime;
    private final Map<String, Object> attributes;
    private final JsonObject config;
    
    // 处理链相关
    private int currentProcessorIndex = 0;
    private boolean shouldContinue = true;
    private ProcessResult lastResult;
    
    public ProcessContext(String contextId, JT808Message message, Vertx vertx) {
        this(contextId, message, vertx, new JsonObject());
    }
    
    public ProcessContext(String contextId, JT808Message message, Vertx vertx, JsonObject config) {
        this.contextId = contextId;
        this.message = message;
        this.vertx = vertx;
        this.config = config;
        this.createTime = LocalDateTime.now();
        this.attributes = new ConcurrentHashMap<>();
    }
    
    /**
     * 获取上下文ID
     */
    public String getContextId() {
        return contextId;
    }
    
    /**
     * 获取消息
     */
    public JT808Message getMessage() {
        return message;
    }
    
    /**
     * 获取Vertx实例
     */
    public Vertx getVertx() {
        return vertx;
    }
    
    /**
     * 获取创建时间
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    /**
     * 获取配置
     */
    public JsonObject getConfig() {
        return config;
    }
    
    /**
     * 设置属性
     */
    public ProcessContext setAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }
    
    /**
     * 获取属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }
    
    /**
     * 获取属性，如果不存在则返回默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }
    
    /**
     * 移除属性
     */
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }
    
    /**
     * 获取所有属性
     */
    public Map<String, Object> getAttributes() {
        return new ConcurrentHashMap<>(attributes);
    }
    
    /**
     * 获取当前处理器索引
     */
    public int getCurrentProcessorIndex() {
        return currentProcessorIndex;
    }
    
    /**
     * 设置当前处理器索引
     */
    public void setCurrentProcessorIndex(int index) {
        this.currentProcessorIndex = index;
    }
    
    /**
     * 是否应该继续处理
     */
    public boolean shouldContinue() {
        return shouldContinue;
    }
    
    /**
     * 停止处理链
     */
    public void stopProcessing() {
        this.shouldContinue = false;
    }
    
    /**
     * 继续处理链
     */
    public void continueProcessing() {
        this.shouldContinue = true;
    }
    
    /**
     * 获取上一个处理结果
     */
    public ProcessResult getLastResult() {
        return lastResult;
    }
    
    /**
     * 设置上一个处理结果
     */
    public void setLastResult(ProcessResult result) {
        this.lastResult = result;
    }
    
    /**
     * 创建子上下文
     */
    public ProcessContext createSubContext(String subContextId) {
        ProcessContext subContext = new ProcessContext(subContextId, message, vertx, config);
        subContext.attributes.putAll(this.attributes);
        return subContext;
    }
    
    @Override
    public String toString() {
        return "ProcessContext{" +
                "contextId='" + contextId + '\'' +
                ", messageType=" + (message != null ? "0x" + Integer.toHexString(message.getMessageId()) : "null") +
                ", createTime=" + createTime +
                ", currentProcessorIndex=" + currentProcessorIndex +
                ", shouldContinue=" + shouldContinue +
                '}';
    }
}