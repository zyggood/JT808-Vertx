package com.jt808.server.session;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 会话管理器
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    private final Vertx vertx;
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> phoneToSessionMap = new ConcurrentHashMap<>();
    private final AtomicLong sessionIdGenerator = new AtomicLong(0);
    
    public SessionManager(Vertx vertx) {
        this.vertx = vertx;
        
        // 定期清理超时会话
        vertx.setPeriodic(60000, id -> cleanupExpiredSessions());
    }
    
    /**
     * 创建新会话
     * @param socket 网络连接
     * @return 会话ID
     */
    public String createSession(NetSocket socket) {
        String sessionId = "session_" + sessionIdGenerator.incrementAndGet();
        Session session = new Session(sessionId, socket);
        sessions.put(sessionId, session);
        
        logger.debug("创建新会话: {}, 远程地址: {}", sessionId, socket.remoteAddress());
        return sessionId;
    }
    
    /**
     * 获取会话
     * @param sessionId 会话ID
     * @return 会话对象
     */
    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * 根据手机号获取会话
     * @param phoneNumber 手机号
     * @return 会话对象
     */
    public Session getSessionByPhone(String phoneNumber) {
        String sessionId = phoneToSessionMap.get(phoneNumber);
        return sessionId != null ? sessions.get(sessionId) : null;
    }
    
    /**
     * 绑定手机号到会话
     * @param sessionId 会话ID
     * @param phoneNumber 手机号
     */
    public void bindPhoneToSession(String sessionId, String phoneNumber) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            // 移除旧的绑定
            if (session.getPhoneNumber() != null) {
                phoneToSessionMap.remove(session.getPhoneNumber());
            }
            
            // 建立新的绑定
            session.setPhoneNumber(phoneNumber);
            phoneToSessionMap.put(phoneNumber, sessionId);
            
            logger.debug("绑定手机号 {} 到会话 {}", phoneNumber, sessionId);
        }
    }
    
    /**
     * 移除会话
     * @param sessionId 会话ID
     */
    public void removeSession(String sessionId) {
        Session session = sessions.remove(sessionId);
        if (session != null) {
            // 移除手机号绑定
            if (session.getPhoneNumber() != null) {
                phoneToSessionMap.remove(session.getPhoneNumber());
            }
            
            logger.debug("移除会话: {}", sessionId);
        }
    }
    
    /**
     * 获取所有会话
     * @return 会话映射
     */
    public ConcurrentHashMap<String, Session> getAllSessions() {
        return new ConcurrentHashMap<>(sessions);
    }
    
    /**
     * 获取在线会话数量
     * @return 会话数量
     */
    public int getSessionCount() {
        return sessions.size();
    }
    
    /**
     * 清理过期会话
     */
    private void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        long timeout = 300000; // 5分钟超时
        
        sessions.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            if (currentTime - session.getLastActiveTime() > timeout) {
                logger.debug("清理过期会话: {}", entry.getKey());
                
                // 移除手机号绑定
                if (session.getPhoneNumber() != null) {
                    phoneToSessionMap.remove(session.getPhoneNumber());
                }
                
                // 关闭连接
                if (session.getSocket() != null) {
                    session.getSocket().close();
                }
                
                return true;
            }
            return false;
        });
    }
}