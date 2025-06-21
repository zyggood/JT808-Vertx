package com.jt808.common.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808异常类测试
 */
class JT808ExceptionTest {
    
    @Test
    void testJT808ExceptionWithMessage() {
        String message = "测试异常消息";
        JT808Exception exception = new JT808Exception(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(-1, exception.getErrorCode());
        assertNull(exception.getCause());
    }
    
    @Test
    void testJT808ExceptionWithMessageAndCause() {
        String message = "测试异常消息";
        Throwable cause = new RuntimeException("原因异常");
        JT808Exception exception = new JT808Exception(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(-1, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testJT808ExceptionWithErrorCodeAndMessage() {
        int errorCode = 1001;
        String message = "测试异常消息";
        JT808Exception exception = new JT808Exception(errorCode, message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getCause());
    }
    
    @Test
    void testJT808ExceptionWithErrorCodeMessageAndCause() {
        int errorCode = 1002;
        String message = "测试异常消息";
        Throwable cause = new IllegalArgumentException("参数异常");
        JT808Exception exception = new JT808Exception(errorCode, message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testProtocolException() {
        String message = "协议解析异常";
        ProtocolException exception = new ProtocolException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(-1, exception.getErrorCode());
        assertTrue(exception instanceof JT808Exception);
    }
    
    @Test
    void testProtocolExceptionWithCause() {
        String message = "协议解析异常";
        Throwable cause = new NumberFormatException("数字格式异常");
        ProtocolException exception = new ProtocolException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(-1, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testProtocolExceptionWithErrorCode() {
        int errorCode = 2001;
        String message = "协议解析异常";
        ProtocolException exception = new ProtocolException(errorCode, message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
    }
    
    @Test
    void testProtocolExceptionWithErrorCodeAndCause() {
        int errorCode = 2002;
        String message = "协议解析异常";
        Throwable cause = new IllegalStateException("状态异常");
        ProtocolException exception = new ProtocolException(errorCode, message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testExceptionInheritance() {
        ProtocolException protocolException = new ProtocolException("协议异常");
        
        // 测试继承关系
        assertTrue(protocolException instanceof JT808Exception);
        assertTrue(protocolException instanceof Exception);
        assertTrue(protocolException instanceof Throwable);
    }
    
    @Test
    void testExceptionSerialization() {
        // 测试异常的基本属性
        JT808Exception exception = new JT808Exception(500, "服务器内部错误");
        
        assertNotNull(exception.toString());
        assertTrue(exception.toString().contains("服务器内部错误"));
    }
}