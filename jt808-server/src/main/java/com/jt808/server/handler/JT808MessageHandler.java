package com.jt808.server.handler;

import com.jt808.common.exception.ProtocolException;
import com.jt808.protocol.codec.JT808Decoder;
import com.jt808.protocol.codec.JT808Encoder;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T0100TerminalRegister;
import com.jt808.protocol.message.T0102TerminalAuth;
import com.jt808.protocol.message.T0200LocationReport;
import com.jt808.server.session.Session;
import com.jt808.server.session.SessionManager;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JT808消息处理器
 */
public class JT808MessageHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(JT808MessageHandler.class);
    
    private final SessionManager sessionManager;
    private final JT808Decoder decoder;
    private final JT808Encoder encoder;
    
    public JT808MessageHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.decoder = new JT808Decoder();
        this.encoder = new JT808Encoder();
    }
    
    /**
     * 处理TCP消息
     * @param sessionId 会话ID
     * @param buffer 消息数据
     */
    public void handleTcpMessage(String sessionId, Buffer buffer) {
        Session session = sessionManager.getSession(sessionId);
        if (session == null) {
            logger.warn("会话不存在: {}", sessionId);
            return;
        }
        
        try {
            // 解码消息
            JT808Message message = decoder.decode(buffer);
            
            logger.debug("收到TCP消息: 会话={}, 消息ID=0x{}, 手机号={}", 
                    sessionId, 
                    Integer.toHexString(message.getMessageId()).toUpperCase(),
                    message.getHeader().getPhoneNumber());
            
            // 更新会话信息
            session.incrementReceivedCount();
            
            // 绑定手机号到会话
            String phoneNumber = message.getHeader().getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.equals(session.getPhoneNumber())) {
                sessionManager.bindPhoneToSession(sessionId, phoneNumber);
            }
            
            // 处理具体消息
            processMessage(session, message);
            
        } catch (ProtocolException e) {
            logger.error("解码TCP消息失败: 会话={}, 错误={}", sessionId, e.getMessage());
            // 可以考虑发送错误响应或关闭连接
        } catch (Exception e) {
            logger.error("处理TCP消息时发生未知错误: 会话={}", sessionId, e);
        }
    }
    
    /**
     * 处理UDP消息
     * @param packet UDP数据包
     */
    public void handleUdpMessage(DatagramPacket packet) {
        try {
            // 解码消息
            JT808Message message = decoder.decode(packet.data());
            
            logger.debug("收到UDP消息: 发送方={}, 消息ID=0x{}, 手机号={}", 
                    packet.sender(),
                    Integer.toHexString(message.getMessageId()).toUpperCase(),
                    message.getHeader().getPhoneNumber());
            
            // UDP消息处理（简化版，实际可能需要更复杂的会话管理）
            String phoneNumber = message.getHeader().getPhoneNumber();
            Session session = sessionManager.getSessionByPhone(phoneNumber);
            
            if (session != null) {
                session.incrementReceivedCount();
                processMessage(session, message);
            } else {
                logger.warn("UDP消息对应的会话不存在: 手机号={}", phoneNumber);
            }
            
        } catch (ProtocolException e) {
            logger.error("解码UDP消息失败: 发送方={}, 错误={}", packet.sender(), e.getMessage());
        } catch (Exception e) {
            logger.error("处理UDP消息时发生未知错误: 发送方={}", packet.sender(), e);
        }
    }
    
    /**
     * 处理具体消息
     * @param session 会话
     * @param message 消息
     */
    private void processMessage(Session session, JT808Message message) {
        int messageId = message.getMessageId();
        
        switch (messageId) {
            case 0x0001: // 终端通用应答
                handleTerminalCommonResponse(session, message);
                break;
            case 0x0002: // 终端心跳
                handleTerminalHeartbeat(session, message);
                break;
            case 0x0100: // 终端注册
                handleTerminalRegister(session, message);
                break;
            case 0x0102: // 终端鉴权
                handleTerminalAuth(session, message);
                break;
            case 0x0200: // 位置信息汇报
                handleLocationReport(session, message);
                break;
            default:
                logger.warn("未处理的消息类型: 0x{}", Integer.toHexString(messageId).toUpperCase());
                // 发送不支持的消息类型响应
                sendCommonResponse(session, message, (byte) 0x03); // 不支持
                break;
        }
    }
    
    /**
     * 处理终端通用应答
     */
    private void handleTerminalCommonResponse(Session session, JT808Message message) {
        logger.debug("处理终端通用应答: 会话={}", session.getSessionId());
        // 实现终端通用应答处理逻辑
    }
    
    /**
     * 处理终端心跳
     */
    private void handleTerminalHeartbeat(Session session, JT808Message message) {
        logger.debug("处理终端心跳: 会话={}", session.getSessionId());
        
        // 发送平台通用应答
        sendCommonResponse(session, message, (byte) 0x00); // 成功
    }
    
    /**
     * 处理终端注册
     */
    private void handleTerminalRegister(Session session, JT808Message message) {
        logger.debug("处理终端注册: 会话={}", session.getSessionId());
        
        if (message instanceof T0100TerminalRegister registerMsg) {

            logger.info("终端注册信息: 省域ID={}, 市县域ID={}, 制造商ID={}, 终端型号={}, 终端ID={}, 车牌号={}",
                registerMsg.getProvinceId(), registerMsg.getCityId(), 
                registerMsg.getManufacturerId(), registerMsg.getTerminalModel(),
                registerMsg.getTerminalId(), registerMsg.getPlateNumber());
            
            // 这里可以添加注册验证逻辑
            // 例如：验证终端是否在白名单中，车辆信息是否正确等
            
            // 简化处理，直接返回注册成功
            String authCode = generateAuthCode(registerMsg.getTerminalId());
            sendRegisterResponse(session, message, (byte) 0x00, authCode);
        } else {
            logger.warn("收到非T0100类型的注册消息: {}", message.getClass().getSimpleName());
            sendRegisterResponse(session, message, (byte) 0x01, null); // 失败
        }
    }
    
    /**
     * 处理终端鉴权
     */
    private void handleTerminalAuth(Session session, JT808Message message) {
        logger.debug("处理终端鉴权: 会话={}", session.getSessionId());
        
        if (message instanceof T0102TerminalAuth authMsg) {

            logger.info("终端鉴权信息: 鉴权码={}, IMEI={}, 软件版本={}, 2019版本={}",
                authMsg.getAuthCode(), authMsg.getImei(), 
                authMsg.getSoftwareVersion(), authMsg.is2019Version());
            
            // 验证鉴权码
            if (authMsg.isAuthCodeValid() && validateAuthCode(authMsg.getAuthCode())) {
                session.setAuthenticated(true);
                logger.info("终端鉴权成功: 会话={}, 鉴权码={}", session.getSessionId(), authMsg.getAuthCode());
                sendCommonResponse(session, message, (byte) 0x00); // 成功
            } else {
                logger.warn("终端鉴权失败: 会话={}, 鉴权码={}", session.getSessionId(), authMsg.getAuthCode());
                sendCommonResponse(session, message, (byte) 0x01); // 失败
            }
        } else {
            logger.warn("收到非T0102类型的鉴权消息: {}", message.getClass().getSimpleName());
            sendCommonResponse(session, message, (byte) 0x02); // 消息有误
        }
    }
    
    /**
     * 处理位置信息汇报
     */
    private void handleLocationReport(Session session, JT808Message message) {
        logger.debug("处理位置信息汇报: 会话={}", session.getSessionId());
        
        if (message instanceof T0200LocationReport locationMsg) {

            logger.info("位置信息: 会话={}, 纬度={}, 经度={}, 速度={}km/h, 方向={}°, 时间={}",
                session.getSessionId(), locationMsg.getLatitudeDegrees(), 
                locationMsg.getLongitudeDegrees(), locationMsg.getSpeedKmh(),
                locationMsg.getDirection(), locationMsg.getDateTime());
            
            // 检查报警标志
            if (locationMsg.getAlarmFlag() != 0) {
                logger.warn("收到报警信息: 会话={}, 报警标志=0x{}", 
                    session.getSessionId(), Integer.toHexString(locationMsg.getAlarmFlag()).toUpperCase());
                // 这里可以添加报警处理逻辑
            }
            
            // 这里可以将位置信息存储到数据库或转发给业务系统
            // 例如：locationService.saveLocation(session.getPhoneNumber(), locationMsg);
            
            sendCommonResponse(session, message, (byte) 0x00); // 成功
        } else {
            logger.warn("收到非T0200类型的位置汇报消息: {}", message.getClass().getSimpleName());
            sendCommonResponse(session, message, (byte) 0x02); // 消息有误
        }
    }
    
    /**
     * 发送平台通用应答
     */
    private void sendCommonResponse(Session session, JT808Message originalMessage, byte result) {
        // 这里需要实现具体的响应消息构造和发送逻辑
        // 暂时记录日志
        logger.debug("发送平台通用应答: 会话={}, 结果={}", session.getSessionId(), result);
    }
    
    /**
     * 发送终端注册应答
     */
    private void sendRegisterResponse(Session session, JT808Message originalMessage, byte result, String authCode) {
        // 这里需要实现具体的注册应答消息构造和发送逻辑
        // 暂时记录日志
        logger.debug("发送终端注册应答: 会话={}, 结果={}, 鉴权码={}", session.getSessionId(), result, authCode);
    }
    
    /**
     * 生成鉴权码
     * @param terminalId 终端ID
     * @return 鉴权码
     */
    private String generateAuthCode(String terminalId) {
        // 简单的鉴权码生成逻辑，实际项目中应该使用更安全的算法
        long timestamp = System.currentTimeMillis();
        String rawCode = terminalId + timestamp;
        return "AUTH" + Integer.toHexString(rawCode.hashCode()).toUpperCase();
    }
    
    /**
     * 验证鉴权码
     * @param authCode 鉴权码
     * @return 是否有效
     */
    private boolean validateAuthCode(String authCode) {
        // 简单的鉴权码验证逻辑
        // 实际项目中应该从数据库或缓存中验证鉴权码的有效性
        return authCode != null && authCode.startsWith("AUTH") && authCode.length() > 4;
    }
}