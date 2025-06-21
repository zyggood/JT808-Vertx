package com.jt808.server.session;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 会话对象
 */
public class Session {
    
    /** 会话ID */
    private final String sessionId;
    
    /** 网络连接 */
    private final NetSocket socket;
    
    /** 终端手机号 */
    private String phoneNumber;
    
    /** 终端认证状态 */
    private boolean authenticated = false;
    
    /** 创建时间 */
    private final long createTime;
    
    /** 最后活跃时间 */
    private volatile long lastActiveTime;
    
    /** 消息流水号生成器 */
    private final AtomicInteger serialNumberGenerator = new AtomicInteger(1);
    
    /** 接收消息计数 */
    private final AtomicInteger receivedMessageCount = new AtomicInteger(0);
    
    /** 发送消息计数 */
    private final AtomicInteger sentMessageCount = new AtomicInteger(0);
    
    /** 协议版本 */
    private byte protocolVersion = 0;
    
    public Session(String sessionId, NetSocket socket) {
        this.sessionId = sessionId;
        this.socket = socket;
        this.createTime = System.currentTimeMillis();
        this.lastActiveTime = createTime;
    }
    
    /**
     * 更新最后活跃时间
     */
    public void updateActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }
    
    /**
     * 生成下一个流水号
     * @return 流水号
     */
    public int nextSerialNumber() {
        int serialNumber = serialNumberGenerator.getAndIncrement();
        if (serialNumber > 0xFFFF) {
            serialNumberGenerator.set(1);
            return 1;
        }
        return serialNumber;
    }
    
    /**
     * 发送数据
     * @param buffer 数据缓冲区
     */
    public void send(Buffer buffer) {
        if (socket != null) {
            socket.write(buffer);
            sentMessageCount.incrementAndGet();
            updateActiveTime();
        }
    }
    
    /**
     * 增加接收消息计数
     */
    public void incrementReceivedCount() {
        receivedMessageCount.incrementAndGet();
        updateActiveTime();
    }
    
    /**
     * 获取远程地址
     * @return 远程地址
     */
    public SocketAddress getRemoteAddress() {
        return socket != null ? socket.remoteAddress() : null;
    }
    
    /**
     * 检查连接是否活跃
     * @return true表示活跃
     */
    public boolean isActive() {
        return socket != null;
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public NetSocket getSocket() {
        return socket;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
    
    public long getCreateTime() {
        return createTime;
    }
    
    public long getLastActiveTime() {
        return lastActiveTime;
    }
    
    public int getReceivedMessageCount() {
        return receivedMessageCount.get();
    }
    
    public int getSentMessageCount() {
        return sentMessageCount.get();
    }
    
    public byte getProtocolVersion() {
        return protocolVersion;
    }
    
    public void setProtocolVersion(byte protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", authenticated=" + authenticated +
                ", remoteAddress=" + getRemoteAddress() +
                ", createTime=" + createTime +
                ", lastActiveTime=" + lastActiveTime +
                ", receivedCount=" + receivedMessageCount.get() +
                ", sentCount=" + sentMessageCount.get() +
                '}';
    }
}