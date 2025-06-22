package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * 终端鉴权消息 (0x0102)
 * 终端向平台发送鉴权码进行身份验证
 */
public class T0102TerminalAuth extends JT808Message {
    
    /** 鉴权码 */
    private String authCode;
    
    /** IMEI (可选，2019版本新增) */
    private String imei;
    
    /** 软件版本号 (可选，2019版本新增) */
    private String softwareVersion;
    
    public T0102TerminalAuth() {
        super();
    }
    
    public T0102TerminalAuth(JT808Header header) {
        super(header);
    }
    
    /**
     * 构造终端鉴权消息
     * @param authCode 鉴权码
     */
    public T0102TerminalAuth(String authCode) {
        this.authCode = authCode;
    }
    
    /**
     * 构造终端鉴权消息（2019版本）
     * @param authCode 鉴权码
     * @param imei IMEI
     * @param softwareVersion 软件版本号
     */
    public T0102TerminalAuth(String authCode, String imei, String softwareVersion) {
        this.authCode = authCode;
        this.imei = imei;
        this.softwareVersion = softwareVersion;
    }
    
    @Override
    public int getMessageId() {
        return 0x0102;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 鉴权码长度 (1字节)
        if (authCode != null) {
            byte[] authBytes = authCode.getBytes();
            buffer.appendByte((byte) authBytes.length);
            buffer.appendBytes(authBytes);
        } else {
            buffer.appendByte((byte) 0);
        }
        
        // 2019版本新增字段
        if (imei != null || softwareVersion != null) {
            // IMEI (15字节，不足补0)
            if (imei != null) {
                byte[] imeiBytes = new byte[15];
                byte[] srcBytes = imei.getBytes();
                System.arraycopy(srcBytes, 0, imeiBytes, 0, Math.min(srcBytes.length, 15));
                buffer.appendBytes(imeiBytes);
            } else {
                buffer.appendBytes(new byte[15]);
            }
            
            // 软件版本号长度 (1字节) + 软件版本号
            if (softwareVersion != null) {
                byte[] versionBytes = softwareVersion.getBytes();
                buffer.appendByte((byte) versionBytes.length);
                buffer.appendBytes(versionBytes);
            } else {
                buffer.appendByte((byte) 0);
            }
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer body) {
        int index = 0;
        
        if (body.length() == 0) {
            return;
        }
        
        // 鉴权码长度 (1字节)
        int authCodeLength = body.getUnsignedByte(index);
        index += 1;
        
        // 鉴权码
        if (authCodeLength > 0 && index + authCodeLength <= body.length()) {
            byte[] authBytes = body.getBytes(index, index + authCodeLength);
            authCode = new String(authBytes);
            index += authCodeLength;
        }
        
        // 2019版本新增字段
        if (index < body.length()) {
            // IMEI (15字节)
            if (index + 15 <= body.length()) {
                byte[] imeiBytes = body.getBytes(index, index + 15);
                imei = new String(imeiBytes).trim().replace("\0", "");
                index += 15;
            }
            
            // 软件版本号
            if (index < body.length()) {
                int versionLength = body.getUnsignedByte(index);
                index += 1;
                
                if (versionLength > 0 && index + versionLength <= body.length()) {
                    byte[] versionBytes = body.getBytes(index, index + versionLength);
                    softwareVersion = new String(versionBytes);
                }
            }
        }
    }
    
    /**
     * 判断是否为2019版本格式
     */
    public boolean is2019Version() {
        return imei != null || softwareVersion != null;
    }
    
    /**
     * 验证鉴权码是否有效
     */
    public boolean isAuthCodeValid() {
        return authCode != null && !authCode.trim().isEmpty();
    }
    
    // Getters and Setters
    public String getAuthCode() {
        return authCode;
    }
    
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }
    
    public String getImei() {
        return imei;
    }
    
    public void setImei(String imei) {
        this.imei = imei;
    }
    
    public String getSoftwareVersion() {
        return softwareVersion;
    }
    
    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }
    
    @Override
    public String toString() {
        return "T0102TerminalAuth{" +
                "authCode='" + authCode + '\'' +
                ", imei='" + imei + '\'' +
                ", softwareVersion='" + softwareVersion + '\'' +
                ", is2019Version=" + is2019Version() +
                ", header=" + getHeader() +
                '}';
    }
}