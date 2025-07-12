package com.jt808.protocol.processor;

import com.jt808.protocol.message.JT808Message;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息处理结果
 */
public class ProcessResult {

    private final Status status;
    private final String processorName;
    private final LocalDateTime processTime;
    private final long processingDuration; // 处理耗时(毫秒)
    private String message;
    private Throwable error;
    private JT808Message responseMessage;
    private JsonObject data;
    private Map<String, Object> metadata;
    private ProcessResult(Builder builder) {
        this.status = builder.status;
        this.processorName = builder.processorName;
        this.processTime = builder.processTime;
        this.processingDuration = builder.processingDuration;
        this.message = builder.message;
        this.error = builder.error;
        this.responseMessage = builder.responseMessage;
        this.data = builder.data;
        this.metadata = builder.metadata;
    }

    /**
     * 创建成功结果
     */
    public static ProcessResult success(String processorName, long duration) {
        return new Builder(Status.SUCCESS, processorName, duration).build();
    }

    /**
     * 创建成功结果（带响应消息）
     */
    public static ProcessResult success(String processorName, long duration, JT808Message responseMessage) {
        return new Builder(Status.SUCCESS, processorName, duration)
                .responseMessage(responseMessage)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ProcessResult failed(String processorName, long duration, String message) {
        return new Builder(Status.FAILED, processorName, duration)
                .message(message)
                .build();
    }

    /**
     * 创建失败结果（带异常）
     */
    public static ProcessResult failed(String processorName, long duration, Throwable error) {
        return new Builder(Status.FAILED, processorName, duration)
                .error(error)
                .message(error.getMessage())
                .build();
    }

    /**
     * 创建跳过结果
     */
    public static ProcessResult skipped(String processorName, String reason) {
        return new Builder(Status.SKIPPED, processorName, 0)
                .message(reason)
                .build();
    }

    /**
     * 创建重试结果
     */
    public static ProcessResult retry(String processorName, long duration, String reason) {
        return new Builder(Status.RETRY, processorName, duration)
                .message(reason)
                .build();
    }

    // Getters
    public Status getStatus() {
        return status;
    }

    public String getProcessorName() {
        return processorName;
    }

    public LocalDateTime getProcessTime() {
        return processTime;
    }

    public long getProcessingDuration() {
        return processingDuration;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getError() {
        return error;
    }

    public JT808Message getResponseMessage() {
        return responseMessage;
    }

    public JsonObject getData() {
        return data;
    }

    public Map<String, Object> getMetadata() {
        return metadata != null ? new ConcurrentHashMap<>(metadata) : new ConcurrentHashMap<>();
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return status == Status.FAILED;
    }

    /**
     * 是否跳过
     */
    public boolean isSkipped() {
        return status == Status.SKIPPED;
    }

    /**
     * 是否需要重试
     */
    public boolean needRetry() {
        return status == Status.RETRY;
    }

    @Override
    public String toString() {
        return "ProcessResult{" +
                "status=" + status +
                ", processorName='" + processorName + '\'' +
                ", processTime=" + processTime +
                ", processingDuration=" + processingDuration + "ms" +
                ", message='" + message + '\'' +
                ", hasError=" + (error != null) +
                ", hasResponseMessage=" + (responseMessage != null) +
                '}';
    }

    /**
     * 处理状态枚举
     */
    public enum Status {
        SUCCESS,    // 处理成功
        FAILED,     // 处理失败
        SKIPPED,    // 跳过处理
        RETRY       // 需要重试
    }

    /**
     * 构建器
     */
    public static class Builder {
        private final Status status;
        private final String processorName;
        private final LocalDateTime processTime;
        private final long processingDuration;

        private String message;
        private Throwable error;
        private JT808Message responseMessage;
        private JsonObject data;
        private Map<String, Object> metadata;

        public Builder(Status status, String processorName, long processingDuration) {
            this.status = status;
            this.processorName = processorName;
            this.processingDuration = processingDuration;
            this.processTime = LocalDateTime.now();
            this.metadata = new ConcurrentHashMap<>();
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder error(Throwable error) {
            this.error = error;
            return this;
        }

        public Builder responseMessage(JT808Message responseMessage) {
            this.responseMessage = responseMessage;
            return this;
        }

        public Builder data(JsonObject data) {
            this.data = data;
            return this;
        }

        public Builder metadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new ConcurrentHashMap<>();
            }
            this.metadata.put(key, value);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            if (this.metadata == null) {
                this.metadata = new ConcurrentHashMap<>();
            }
            this.metadata.putAll(metadata);
            return this;
        }

        public ProcessResult build() {
            return new ProcessResult(this);
        }
    }
}