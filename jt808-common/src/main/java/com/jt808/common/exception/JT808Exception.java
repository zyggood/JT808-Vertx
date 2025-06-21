package com.jt808.common.exception;

/**
 * JT808协议异常基类
 */
public class JT808Exception extends Exception {
    
    private final int errorCode;
    
    public JT808Exception(String message) {
        super(message);
        this.errorCode = -1;
    }
    
    public JT808Exception(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = -1;
    }
    
    public JT808Exception(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public JT808Exception(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}