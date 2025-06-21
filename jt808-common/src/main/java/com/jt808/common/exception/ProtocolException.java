package com.jt808.common.exception;

/**
 * 协议解析异常
 */
public class ProtocolException extends JT808Exception {
    
    public ProtocolException(String message) {
        super(message);
    }
    
    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ProtocolException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    public ProtocolException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}