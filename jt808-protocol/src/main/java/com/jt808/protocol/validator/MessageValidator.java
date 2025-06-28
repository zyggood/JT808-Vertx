package com.jt808.protocol.validator;

import com.jt808.protocol.message.JT808Message;
import io.vertx.core.Future;

import java.util.List;

/**
 * 消息验证器接口
 * 用于统一处理消息数据校验
 */
public interface MessageValidator {
    
    /**
     * 验证消息
     * @param message 要验证的消息
     * @return 验证结果
     */
    Future<ValidationResult> validate(JT808Message message);
    
    /**
     * 获取验证器名称
     */
    String getName();
    
    /**
     * 获取验证器优先级（数字越小优先级越高）
     */
    int getPriority();
    
    /**
     * 判断是否可以验证指定类型的消息
     */
    boolean canValidate(JT808Message message);
    
    /**
     * 是否为严格验证（验证失败时是否阻止后续处理）
     */
    boolean isStrict();
    
    /**
     * 验证结果
     */
    class ValidationResult {
        private final boolean valid;
        private final String validatorName;
        private final List<ValidationError> errors;
        private final List<ValidationWarning> warnings;
        private final long validationTime;
        
        public ValidationResult(boolean valid, String validatorName, 
                              List<ValidationError> errors, 
                              List<ValidationWarning> warnings) {
            this.valid = valid;
            this.validatorName = validatorName;
            this.errors = errors != null ? errors : List.of();
            this.warnings = warnings != null ? warnings : List.of();
            this.validationTime = System.currentTimeMillis();
        }
        
        public static ValidationResult success(String validatorName) {
            return new ValidationResult(true, validatorName, null, null);
        }
        
        public static ValidationResult success(String validatorName, List<ValidationWarning> warnings) {
            return new ValidationResult(true, validatorName, null, warnings);
        }
        
        public static ValidationResult failure(String validatorName, List<ValidationError> errors) {
            return new ValidationResult(false, validatorName, errors, null);
        }
        
        public static ValidationResult failure(String validatorName, ValidationError error) {
            return new ValidationResult(false, validatorName, List.of(error), null);
        }
        
        public static ValidationResult failure(String validatorName, String errorMessage) {
            return failure(validatorName, new ValidationError("VALIDATION_FAILED", errorMessage));
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getValidatorName() { return validatorName; }
        public List<ValidationError> getErrors() { return errors; }
        public List<ValidationWarning> getWarnings() { return warnings; }
        public long getValidationTime() { return validationTime; }
        
        public boolean hasErrors() { return !errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        
        @Override
        public String toString() {
            return String.format("ValidationResult{valid=%s, validator='%s', errors=%d, warnings=%d}",
                    valid, validatorName, errors.size(), warnings.size());
        }
    }
    
    /**
     * 验证错误
     */
    class ValidationError {
        private final String code;
        private final String message;
        private final String field;
        private final Object value;
        
        public ValidationError(String code, String message) {
            this(code, message, null, null);
        }
        
        public ValidationError(String code, String message, String field) {
            this(code, message, field, null);
        }
        
        public ValidationError(String code, String message, String field, Object value) {
            this.code = code;
            this.message = message;
            this.field = field;
            this.value = value;
        }
        
        // Getters
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public String getField() { return field; }
        public Object getValue() { return value; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationError{code='")
              .append(code)
              .append("', message='")
              .append(message)
              .append("'");
            
            if (field != null) {
                sb.append("', field='").append(field).append("'");
            }
            if (value != null) {
                sb.append("', value='").append(value).append("'");
            }
            sb.append("}");
            
            return sb.toString();
        }
    }
    
    /**
     * 验证警告
     */
    class ValidationWarning {
        private final String code;
        private final String message;
        private final String field;
        private final Object value;
        
        public ValidationWarning(String code, String message) {
            this(code, message, null, null);
        }
        
        public ValidationWarning(String code, String message, String field) {
            this(code, message, field, null);
        }
        
        public ValidationWarning(String code, String message, String field, Object value) {
            this.code = code;
            this.message = message;
            this.field = field;
            this.value = value;
        }
        
        // Getters
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public String getField() { return field; }
        public Object getValue() { return value; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationWarning{code='")
              .append(code)
              .append("', message='")
              .append(message)
              .append("'");
            
            if (field != null) {
                sb.append("', field='").append(field).append("'");
            }
            if (value != null) {
                sb.append("', value='").append(value).append("'");
            }
            sb.append("}");
            
            return sb.toString();
        }
    }
}