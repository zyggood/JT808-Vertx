package com.jt808.protocol.processor.impl;

import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.processor.MessageProcessor;
import com.jt808.protocol.processor.ProcessContext;
import com.jt808.protocol.processor.ProcessResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话处理器
 * 负责管理终端会话状态、连接管理和会话验证
 */
public class SessionHandler implements MessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SessionHandler.class);
    
    private final String name;
    private final Map<String, SessionInfo> sessions;
    private final long sessionTimeoutMs;
    
    public SessionHandler() {
        this(30 * 60 * 1000L); // 默认30分钟超时
    }
    
    public SessionHandler(long sessionTimeoutMs) {
        this.name = "SessionHandler";
        this.sessions = new ConcurrentHashMap<>();
        this.sessionTimeoutMs = sessionTimeoutMs;
    }
    
    @Override
    public Future<ProcessResult> process(ProcessContext context) {
        long startTime = System.currentTimeMillis();
        Promise<ProcessResult> promise = Promise.promise();
        
        try {
            JT808Message message = context.getMessage();
            String terminalId = extractTerminalId(message);
            
            if (terminalId == null) {
                long duration = System.currentTimeMillis() - startTime;
                promise.complete(ProcessResult.skipped(name, "No terminal ID found in message"));
                return promise.future();
            }
            
            // 更新或创建会话
            SessionInfo session = updateSession(terminalId, message);
            context.setAttribute("session", session);
            context.setAttribute("terminalId", terminalId);
            
            // 验证会话状态
            if (!validateSession(session, message)) {
                long duration = System.currentTimeMillis() - startTime;
                promise.complete(ProcessResult.failed(name, duration, "Session validation failed for terminal: " + terminalId));
                return promise.future();
            }
            
            // 清理过期会话
            cleanupExpiredSessions();
            
            long duration = System.currentTimeMillis() - startTime;
            promise.complete(ProcessResult.success(name, duration));
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error in session handling", e);
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
        return 20; // 会话处理应该在路由之后
    }
    
    @Override
    public boolean canProcess(JT808Message message) {
        return extractTerminalId(message) != null;
    }
    
    /**
     * 从消息中提取终端ID
     * 使用消息头中的终端手机号作为唯一标识
     */
    private String extractTerminalId(JT808Message message) {
        try {
            // 从消息头中获取终端手机号作为终端ID
            if (message != null && message.getHeader() != null) {
                String phoneNumber = message.getHeader().getPhoneNumber();
                if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                    return phoneNumber.trim();
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to extract terminal ID from message: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 更新会话信息
     */
    private SessionInfo updateSession(String terminalId, JT808Message message) {
        SessionInfo session = sessions.computeIfAbsent(terminalId, k -> new SessionInfo(terminalId));
        session.updateLastActivity();
        session.incrementMessageCount();
        
        // 根据消息类型更新会话状态
        updateSessionState(session, message);
        
        logger.debug("Updated session for terminal: {}, total messages: {}", 
                    terminalId, session.getMessageCount());
        
        return session;
    }
    
    /**
     * 根据消息类型更新会话状态
     */
    private void updateSessionState(SessionInfo session, JT808Message message) {
        int messageId = message.getMessageId();
        
        switch (messageId) {
            case 0x0100: // 终端注册
                session.setState(SessionState.REGISTERING);
                break;
            case 0x0102: // 终端鉴权
                session.setState(SessionState.AUTHENTICATING);
                break;
            case 0x0002: // 心跳
            case 0x0200: // 位置信息汇报
                if (session.getState() == SessionState.AUTHENTICATING) {
                    session.setState(SessionState.AUTHENTICATED);
                }
                break;
            default:
                // 其他消息类型保持当前状态
                break;
        }
    }
    
    /**
     * 验证会话状态
     */
    private boolean validateSession(SessionInfo session, JT808Message message) {
        int messageId = message.getMessageId();
        SessionState state = session.getState();
        
        // 终端注册消息总是允许的
        if (messageId == 0x0100) {
            return true;
        }
        
        // 终端鉴权消息需要在注册后
        if (messageId == 0x0102) {
            return state == SessionState.REGISTERING || state == SessionState.AUTHENTICATED;
        }
        
        // 其他消息需要在鉴权后
        return state == SessionState.AUTHENTICATED;
    }
    
    /**
     * 清理过期会话
     */
    private void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> {
            SessionInfo session = entry.getValue();
            boolean expired = (currentTime - session.getLastActivityTime()) > sessionTimeoutMs;
            if (expired) {
                logger.info("Removed expired session for terminal: {}", entry.getKey());
            }
            return expired;
        });
    }
    
    /**
     * 获取会话信息
     */
    public SessionInfo getSession(String terminalId) {
        return sessions.get(terminalId);
    }
    
    /**
     * 移除会话
     */
    public boolean removeSession(String terminalId) {
        SessionInfo removed = sessions.remove(terminalId);
        if (removed != null) {
            logger.info("Manually removed session for terminal: {}", terminalId);
            return true;
        }
        return false;
    }
    
    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
    
    /**
     * 会话信息类
     */
    public static class SessionInfo {
        private final String terminalId;
        private final LocalDateTime createTime;
        private volatile long lastActivityTime;
        private volatile SessionState state;
        private volatile long messageCount;
        
        public SessionInfo(String terminalId) {
            this.terminalId = terminalId;
            this.createTime = LocalDateTime.now();
            this.lastActivityTime = System.currentTimeMillis();
            this.state = SessionState.INITIAL;
            this.messageCount = 0;
        }
        
        public void updateLastActivity() {
            this.lastActivityTime = System.currentTimeMillis();
        }
        
        public void incrementMessageCount() {
            this.messageCount++;
        }
        
        // Getters and setters
        public String getTerminalId() { return terminalId; }
        public LocalDateTime getCreateTime() { return createTime; }
        public long getLastActivityTime() { return lastActivityTime; }
        public SessionState getState() { return state; }
        public void setState(SessionState state) { this.state = state; }
        public long getMessageCount() { return messageCount; }
        
        @Override
        public String toString() {
            return String.format("SessionInfo{terminalId='%s', state=%s, messageCount=%d, lastActivity=%d}",
                    terminalId, state, messageCount, lastActivityTime);
        }
    }
    
    /**
     * 会话状态枚举
     */
    public enum SessionState {
        INITIAL,        // 初始状态
        REGISTERING,    // 注册中
        AUTHENTICATING, // 鉴权中
        AUTHENTICATED,  // 已鉴权
        DISCONNECTED    // 已断开
    }
}