package com.jt808.protocol.processor.impl;

import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.processor.MessageProcessor;
import com.jt808.protocol.processor.ProcessContext;
import com.jt808.protocol.processor.ProcessResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 消息验证处理器
 * 负责验证消息的完整性、格式正确性和业务规则
 */
public class MessageValidator implements MessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MessageValidator.class);
    
    private final String name;
    private final Map<Integer, List<ValidationRule>> validationRules;
    private final List<ValidationRule> globalRules;
    private final boolean strictMode;
    
    public MessageValidator() {
        this(false);
    }
    
    public MessageValidator(boolean strictMode) {
        this.name = "MessageValidator";
        this.validationRules = new ConcurrentHashMap<>();
        this.globalRules = new ArrayList<>();
        this.strictMode = strictMode;
        initializeDefaultRules();
    }
    
    @Override
    public Future<ProcessResult> process(ProcessContext context) {
        long startTime = System.currentTimeMillis();
        Promise<ProcessResult> promise = Promise.promise();
        
        try {
            JT808Message message = context.getMessage();
            ValidationResult result = validateMessage(message, context);
            
            if (!result.isValid()) {
                long duration = System.currentTimeMillis() - startTime;
                String errorMsg = "Message validation failed: " + String.join(", ", result.getErrors());
                
                if (strictMode) {
                    promise.complete(ProcessResult.failed(name, duration, errorMsg));
                } else {
                    logger.warn("Validation warnings for message 0x{}: {}", 
                            Integer.toHexString(message.getMessageId()), errorMsg);
                    ProcessResult successResult = ProcessResult.success(name, duration);
                    // 将验证警告添加到上下文而不是结果中
                    context.setAttribute("validationWarnings", result.getErrors());
                    promise.complete(successResult);
                }
            } else {
                long duration = System.currentTimeMillis() - startTime;
                promise.complete(ProcessResult.success(name, duration));
            }
            
            // 将验证结果添加到上下文中
            context.setAttribute("validationResult", result);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error in message validation", e);
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
        return 15; // 验证应该在路由之前，但在性能监控之后
    }
    
    @Override
    public boolean canProcess(JT808Message message) {
        return true; // 验证所有消息
    }
    
    /**
     * 验证消息
     */
    private ValidationResult validateMessage(JT808Message message, ProcessContext context) {
        ValidationResult result = new ValidationResult();
        
        // 执行全局验证规则
        for (ValidationRule rule : globalRules) {
            try {
                if (!rule.validate(message, context)) {
                    result.addError(rule.getErrorMessage());
                }
            } catch (Exception e) {
                logger.warn("Error executing global validation rule: {}", rule.getName(), e);
                result.addError("Validation rule error: " + rule.getName());
            }
        }
        
        // 执行特定消息类型的验证规则
        List<ValidationRule> messageRules = validationRules.get(message.getMessageId());
        if (messageRules != null) {
            for (ValidationRule rule : messageRules) {
                try {
                    if (!rule.validate(message, context)) {
                        result.addError(rule.getErrorMessage());
                    }
                } catch (Exception e) {
                    logger.warn("Error executing validation rule: {} for message 0x{}", 
                            rule.getName(), Integer.toHexString(message.getMessageId()), e);
                    result.addError("Validation rule error: " + rule.getName());
                }
            }
        }
        
        return result;
    }
    
    /**
     * 添加全局验证规则
     */
    public void addGlobalRule(ValidationRule rule) {
        globalRules.add(rule);
        logger.debug("Added global validation rule: {}", rule.getName());
    }
    
    /**
     * 添加特定消息类型的验证规则
     */
    public void addMessageRule(int messageId, ValidationRule rule) {
        validationRules.computeIfAbsent(messageId, k -> new ArrayList<>()).add(rule);
        logger.debug("Added validation rule: {} for message 0x{}", rule.getName(), Integer.toHexString(messageId));
    }
    
    /**
     * 移除验证规则
     */
    public boolean removeRule(String ruleName) {
        boolean removed = globalRules.removeIf(rule -> rule.getName().equals(ruleName));
        
        for (List<ValidationRule> rules : validationRules.values()) {
            removed |= rules.removeIf(rule -> rule.getName().equals(ruleName));
        }
        
        if (removed) {
            logger.debug("Removed validation rule: {}", ruleName);
        }
        
        return removed;
    }
    
    /**
     * 初始化默认验证规则
     */
    private void initializeDefaultRules() {
        // 全局规则：消息不能为空
        addGlobalRule(new ValidationRule("NotNull", "Message cannot be null") {
            @Override
            public boolean validate(JT808Message message, ProcessContext context) {
                return message != null;
            }
        });
        
        // 全局规则：消息ID必须有效
        addGlobalRule(new ValidationRule("ValidMessageId", "Message ID must be valid") {
            @Override
            public boolean validate(JT808Message message, ProcessContext context) {
                int messageId = message.getMessageId();
                return messageId > 0 && messageId <= 0xFFFF;
            }
        });
        
        // 全局规则：消息头不能为空
        addGlobalRule(new ValidationRule("HeaderNotNull", "Message header cannot be null") {
            @Override
            public boolean validate(JT808Message message, ProcessContext context) {
                return message.getHeader() != null;
            }
        });
        
        // 终端注册消息验证
        addMessageRule(0x0100, new ValidationRule("TerminalRegisterFormat", "Terminal register message format invalid") {
            @Override
            public boolean validate(JT808Message message, ProcessContext context) {
                // TODO: 实现具体的终端注册消息格式验证
                return validateTerminalRegisterMessage(message);
            }
        });
        
        // 位置信息汇报消息验证
        addMessageRule(0x0200, new ValidationRule("LocationReportFormat", "Location report message format invalid") {
            @Override
            public boolean validate(JT808Message message, ProcessContext context) {
                // TODO: 实现具体的位置信息汇报消息格式验证
                return validateLocationReportMessage(message);
            }
        });
        
        logger.info("Initialized message validator with {} global rules and {} message-specific rules", 
                globalRules.size(), validationRules.size());
    }
    
    /**
     * 验证终端注册消息格式
     */
    private boolean validateTerminalRegisterMessage(JT808Message message) {
        // TODO: 根据JT808协议实现具体的验证逻辑
        // 这里应该验证：
        // 1. 省域ID
        // 2. 市县域ID
        // 3. 制造商ID
        // 4. 终端型号
        // 5. 终端ID
        // 6. 车牌颜色
        // 7. 车辆标识
        return true; // 暂时返回true，待具体实现
    }
    
    /**
     * 验证位置信息汇报消息格式
     */
    private boolean validateLocationReportMessage(JT808Message message) {
        // TODO: 根据JT808协议实现具体的验证逻辑
        // 这里应该验证：
        // 1. 报警标志
        // 2. 状态
        // 3. 纬度
        // 4. 经度
        // 5. 高程
        // 6. 速度
        // 7. 方向
        // 8. 时间
        return true; // 暂时返回true，待具体实现
    }
    
    /**
     * 验证规则抽象类
     */
    public abstract static class ValidationRule {
        private final String name;
        private final String errorMessage;
        
        public ValidationRule(String name, String errorMessage) {
            this.name = name;
            this.errorMessage = errorMessage;
        }
        
        public abstract boolean validate(JT808Message message, ProcessContext context);
        
        public String getName() { return name; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final List<String> errors;
        
        public ValidationResult() {
            this.errors = new ArrayList<>();
        }
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public String getErrorSummary() {
            return String.join("; ", errors);
        }
        
        @Override
        public String toString() {
            return String.format("ValidationResult{valid=%s, errors=%s}", isValid(), errors);
        }
    }
    
    /**
     * 创建简单的验证规则
     */
    public static ValidationRule createSimpleRule(String name, String errorMessage, 
                                                 Function<JT808Message, Boolean> validator) {
        return new ValidationRule(name, errorMessage) {
            @Override
            public boolean validate(JT808Message message, ProcessContext context) {
                return validator.apply(message);
            }
        };
    }
    
    /**
     * 获取验证统计信息
     */
    public Map<String, Object> getValidationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("globalRulesCount", globalRules.size());
        stats.put("messageSpecificRulesCount", validationRules.size());
        stats.put("strictMode", strictMode);
        
        Map<String, Integer> rulesByMessage = new HashMap<>();
        validationRules.forEach((messageId, rules) -> {
            rulesByMessage.put("0x" + Integer.toHexString(messageId), rules.size());
        });
        stats.put("rulesByMessage", rulesByMessage);
        
        return stats;
    }
}