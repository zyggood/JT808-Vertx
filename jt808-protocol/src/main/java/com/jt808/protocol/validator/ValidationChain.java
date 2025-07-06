package com.jt808.protocol.validator;

import com.jt808.protocol.message.JT808Message;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 验证器链
 * 管理和执行多个消息验证器
 */
public class ValidationChain {

    private static final Logger logger = LoggerFactory.getLogger(ValidationChain.class);
    
    private final Vertx vertx;
    private final List<MessageValidator> validators;
    private final Map<String, MessageValidator> validatorMap;
    private final ValidationConfig config;
    
    public ValidationChain(Vertx vertx) {
        this(vertx, new ValidationConfig());
    }
    
    public ValidationChain(Vertx vertx, ValidationConfig config) {
        this.vertx = vertx;
        this.validators = new ArrayList<>();
        this.validatorMap = new ConcurrentHashMap<>();
        this.config = config;
    }
    
    /**
     * 添加验证器
     */
    public ValidationChain addValidator(MessageValidator validator) {
        if (validator == null) {
            throw new IllegalArgumentException("Validator cannot be null");
        }
        
        if (validatorMap.containsKey(validator.getName())) {
            throw new IllegalArgumentException("Validator with name '" + validator.getName() + "' already exists");
        }
        
        validators.add(validator);
        validatorMap.put(validator.getName(), validator);
        
        // 按优先级排序
        validators.sort(Comparator.comparingInt(MessageValidator::getPriority));
        
        logger.info("Added validator: {} with priority: {}", validator.getName(), validator.getPriority());
        return this;
    }
    
    /**
     * 移除验证器
     */
    public boolean removeValidator(String validatorName) {
        MessageValidator validator = validatorMap.remove(validatorName);
        if (validator != null) {
            validators.remove(validator);
            logger.info("Removed validator: {}", validatorName);
            return true;
        }
        return false;
    }
    
    /**
     * 获取验证器
     */
    public MessageValidator getValidator(String validatorName) {
        return validatorMap.get(validatorName);
    }
    
    /**
     * 验证消息
     */
    public Future<ValidationChainResult> validate(JT808Message message) {
        if (validators.isEmpty()) {
            return Future.succeededFuture(ValidationChainResult.success(Collections.emptyList()));
        }
        
        // 筛选可以验证此消息的验证器
        List<MessageValidator> applicableValidators = validators.stream()
                .filter(validator -> validator.canValidate(message))
                .collect(Collectors.toList());
        
        if (applicableValidators.isEmpty()) {
            logger.debug("No applicable validators found for message type: {}", message.getClass().getSimpleName());
            return Future.succeededFuture(ValidationChainResult.success(Collections.emptyList()));
        }
        
        logger.debug("Validating message with {} validators", applicableValidators.size());
        
        if (config.isParallelValidation()) {
            return validateParallel(message, applicableValidators);
        } else {
            return validateSequential(message, applicableValidators);
        }
    }
    
    /**
     * 并行验证
     */
    private Future<ValidationChainResult> validateParallel(JT808Message message, List<MessageValidator> validators) {
        List<Future<MessageValidator.ValidationResult>> futures = validators.stream()
                .map(validator -> {
                    try {
                        return validator.validate(message)
                                .recover(error -> {
                                    logger.error("Validator {} failed with error", validator.getName(), error);
                                    return Future.succeededFuture(
                                            MessageValidator.ValidationResult.failure(
                                                    validator.getName(), 
                                                    "Validator execution failed: " + error.getMessage()
                                            )
                                    );
                                });
                    } catch (Exception e) {
                        logger.error("Validator {} threw exception", validator.getName(), e);
                        return Future.succeededFuture(
                                MessageValidator.ValidationResult.failure(
                                        validator.getName(), 
                                        "Validator execution failed: " + e.getMessage()
                                )
                        );
                    }
                })
                .toList();
        
        return Future.all(new ArrayList<>(futures))
                .map(compositeFuture -> {
                    List<MessageValidator.ValidationResult> results = compositeFuture.list();
                    return createChainResult(results);
                });
    }
    
    /**
     * 顺序验证
     */
    private Future<ValidationChainResult> validateSequential(JT808Message message, List<MessageValidator> validators) {
        Promise<ValidationChainResult> promise = Promise.promise();
        List<MessageValidator.ValidationResult> results = new ArrayList<>();
        
        validateNext(message, validators, 0, results, promise);
        
        return promise.future();
    }
    
    /**
     * 递归验证下一个验证器
     */
    private void validateNext(JT808Message message, List<MessageValidator> validators, int index,
                             List<MessageValidator.ValidationResult> results, 
                             Promise<ValidationChainResult> promise) {
        
        if (index >= validators.size()) {
            promise.complete(createChainResult(results));
            return;
        }
        
        MessageValidator validator = validators.get(index);
        
        try {
            validator.validate(message)
                    .onSuccess(result -> {
                        results.add(result);
                        
                        // 如果是严格验证且验证失败，停止后续验证
                        if (validator.isStrict() && !result.isValid() && config.isStopOnFirstStrictFailure()) {
                            logger.warn("Strict validator {} failed, stopping validation chain", validator.getName());
                            promise.complete(createChainResult(results));
                        } else {
                            validateNext(message, validators, index + 1, results, promise);
                        }
                    })
                    .onFailure(error -> {
                        logger.error("Validator {} failed with error", validator.getName(), error);
                        MessageValidator.ValidationResult errorResult = MessageValidator.ValidationResult.failure(
                                validator.getName(), 
                                "Validator execution failed: " + error.getMessage()
                        );
                        results.add(errorResult);
                        
                        if (validator.isStrict() && config.isStopOnFirstStrictFailure()) {
                            promise.complete(createChainResult(results));
                        } else {
                            validateNext(message, validators, index + 1, results, promise);
                        }
                    });
        } catch (Exception e) {
            logger.error("Validator {} threw exception", validator.getName(), e);
            MessageValidator.ValidationResult errorResult = MessageValidator.ValidationResult.failure(
                    validator.getName(), 
                    "Validator execution failed: " + e.getMessage()
            );
            results.add(errorResult);
            
            if (validator.isStrict() && config.isStopOnFirstStrictFailure()) {
                promise.complete(createChainResult(results));
            } else {
                validateNext(message, validators, index + 1, results, promise);
            }
        }
    }
    
    /**
     * 创建验证链结果
     */
    private ValidationChainResult createChainResult(List<MessageValidator.ValidationResult> results) {
        boolean allValid = results.stream().allMatch(MessageValidator.ValidationResult::isValid);
        
        List<MessageValidator.ValidationError> allErrors = results.stream()
                .flatMap(result -> result.getErrors().stream())
                .collect(Collectors.toList());
        
        List<MessageValidator.ValidationWarning> allWarnings = results.stream()
                .flatMap(result -> result.getWarnings().stream())
                .collect(Collectors.toList());
        
        return new ValidationChainResult(allValid, results, allErrors, allWarnings);
    }
    
    /**
     * 获取所有验证器名称
     */
    public Set<String> getValidatorNames() {
        return new HashSet<>(validatorMap.keySet());
    }
    
    /**
     * 获取验证配置
     */
    public ValidationConfig getConfig() {
        return config;
    }
    
    /**
     * 验证链结果
     */
    public static class ValidationChainResult {
        private final boolean valid;
        private final List<MessageValidator.ValidationResult> validatorResults;
        private final List<MessageValidator.ValidationError> allErrors;
        private final List<MessageValidator.ValidationWarning> allWarnings;
        private final long validationTime;
        
        public ValidationChainResult(boolean valid, 
                                   List<MessageValidator.ValidationResult> validatorResults,
                                   List<MessageValidator.ValidationError> allErrors,
                                   List<MessageValidator.ValidationWarning> allWarnings) {
            this.valid = valid;
            this.validatorResults = validatorResults != null ? validatorResults : Collections.emptyList();
            this.allErrors = allErrors != null ? allErrors : Collections.emptyList();
            this.allWarnings = allWarnings != null ? allWarnings : Collections.emptyList();
            this.validationTime = System.currentTimeMillis();
        }
        
        public static ValidationChainResult success(List<MessageValidator.ValidationResult> results) {
            List<MessageValidator.ValidationWarning> warnings = results.stream()
                    .flatMap(result -> result.getWarnings().stream())
                    .collect(Collectors.toList());
            return new ValidationChainResult(true, results, Collections.emptyList(), warnings);
        }
        
        public static ValidationChainResult failure(List<MessageValidator.ValidationResult> results,
                                                   List<MessageValidator.ValidationError> errors) {
            List<MessageValidator.ValidationWarning> warnings = results.stream()
                    .flatMap(result -> result.getWarnings().stream())
                    .collect(Collectors.toList());
            return new ValidationChainResult(false, results, errors, warnings);
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public List<MessageValidator.ValidationResult> getValidatorResults() { return validatorResults; }
        public List<MessageValidator.ValidationError> getAllErrors() { return allErrors; }
        public List<MessageValidator.ValidationWarning> getAllWarnings() { return allWarnings; }
        public long getValidationTime() { return validationTime; }
        
        public boolean hasErrors() { return !allErrors.isEmpty(); }
        public boolean hasWarnings() { return !allWarnings.isEmpty(); }
        
        public int getValidatorCount() { return validatorResults.size(); }
        public int getSuccessfulValidatorCount() {
            return (int) validatorResults.stream().filter(MessageValidator.ValidationResult::isValid).count();
        }
        public int getFailedValidatorCount() {
            return (int) validatorResults.stream().filter(result -> !result.isValid()).count();
        }
        
        @Override
        public String toString() {
            return String.format("ValidationChainResult{valid=%s, validators=%d, errors=%d, warnings=%d}",
                    valid, validatorResults.size(), allErrors.size(), allWarnings.size());
        }
    }
    
    /**
     * 验证配置
     */
    public static class ValidationConfig {
        private boolean parallelValidation = false;
        private boolean stopOnFirstStrictFailure = true;
        private long timeoutMs = 5000;
        
        public boolean isParallelValidation() { return parallelValidation; }
        public ValidationConfig setParallelValidation(boolean parallelValidation) {
            this.parallelValidation = parallelValidation;
            return this;
        }
        
        public boolean isStopOnFirstStrictFailure() { return stopOnFirstStrictFailure; }
        public ValidationConfig setStopOnFirstStrictFailure(boolean stopOnFirstStrictFailure) {
            this.stopOnFirstStrictFailure = stopOnFirstStrictFailure;
            return this;
        }
        
        public long getTimeoutMs() { return timeoutMs; }
        public ValidationConfig setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }
    }
}