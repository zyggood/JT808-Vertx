package com.jt808.server.handler;

import com.jt808.common.exception.ProtocolException;
import com.jt808.protocol.codec.JT808Decoder;
import com.jt808.protocol.codec.JT808Encoder;
import com.jt808.protocol.message.*;
import com.jt808.server.session.Session;
import com.jt808.server.session.SessionManager;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * JT808消息处理器
 * 负责处理所有JT808协议消息的解码、分发和响应
 * <p>
 * 主要功能：
 * 1. TCP/UDP消息处理
 * 2. 消息路由和分发
 * 3. 会话管理集成
 * 4. 错误处理和监控
 * 5. 性能统计
 */
public class JT808MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(JT808MessageHandler.class);

    // 核心组件
    private final SessionManager sessionManager;
    private final JT808Decoder decoder;
    private final JT808Encoder encoder;

    // 消息处理器映射
    private final ConcurrentHashMap<Integer, BiConsumer<Session, JT808Message>> messageHandlers;

    // 性能统计
    private final AtomicLong totalMessagesProcessed = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    // 配置参数
    private final boolean enablePerformanceLogging;
    private final long performanceLogInterval;

    // 消息缓存和批处理相关字段
    private final Map<String, List<JT808Message>> messageCache = new ConcurrentHashMap<>();
    private final AtomicLong cacheSize = new AtomicLong(0);
    private final int maxCacheSize = 10000; // 最大缓存消息数
    private final int batchSize = 100; // 批处理大小
    private final long batchTimeout = 5000; // 批处理超时时间（毫秒）

    // 分包消息重组相关字段
    private final Map<String, Map<Integer, JT808Message>> subpackageCache = new ConcurrentHashMap<>();
    private final Map<String, Long> subpackageTimestamp = new ConcurrentHashMap<>();
    private final long subpackageTimeout = 30000; // 分包超时时间（30秒）

    public JT808MessageHandler(SessionManager sessionManager) {
        this(sessionManager, false, 60000); // 默认不启用性能日志，间隔60秒
    }

    /**
     * 初始化消息处理器映射
     */
    private void initializeMessageHandlers() {
        // 终端上行消息
        messageHandlers.put(0x0001, this::handleTerminalCommonResponse);
        messageHandlers.put(0x0002, this::handleTerminalHeartbeat);
        messageHandlers.put(0x0100, this::handleTerminalRegister);
        messageHandlers.put(0x0102, this::handleTerminalAuth);
        messageHandlers.put(0x0104, this::handleQueryTerminalParametersResponse);
        messageHandlers.put(0x0107, this::handleQueryTerminalPropertyResponse);
        messageHandlers.put(0x0108, this::handleTerminalUpgradeResultNotification);
        messageHandlers.put(0x0200, this::handleLocationReport);
        messageHandlers.put(0x0201, this::handlePositionInfoQueryResponse);

        // 平台下行消息（用于处理终端的应答）
        messageHandlers.put(0x8001, this::handlePlatformCommonResponse);
        messageHandlers.put(0x8003, this::handleResendSubpackageRequest);
        messageHandlers.put(0x8100, this::handleTerminalRegisterResponse);
        messageHandlers.put(0x8103, this::handleTerminalParameterSetting);
        messageHandlers.put(0x8104, this::handleQueryTerminalParameters);
        messageHandlers.put(0x8105, this::handleTerminalControl);
        messageHandlers.put(0x8106, this::handleQuerySpecificTerminalParameters);
        messageHandlers.put(0x8107, this::handleQueryTerminalProperty);
        messageHandlers.put(0x8108, this::handleTerminalUpgradePackage);
        messageHandlers.put(0x8201, this::handlePositionInfoQuery);
        messageHandlers.put(0x8202, this::handleTemporaryLocationTrackingControl);
        messageHandlers.put(0x8203, this::handleManualAlarmConfirmation);
        messageHandlers.put(0x8300, this::handleTextInfoDistribution);
        messageHandlers.put(0x8301, this::handleEventSetting);

        logger.info("消息处理器初始化完成，支持 {} 种消息类型", messageHandlers.size());
    }

    /**
     * 启动性能监控
     */
    private void startPerformanceMonitoring() {
        // 这里可以启动一个定时任务来记录性能统计
        logger.info("性能监控已启用，统计间隔: {} ms", performanceLogInterval);
    }

    public JT808MessageHandler(SessionManager sessionManager, boolean enablePerformanceLogging, long performanceLogInterval) {
        this.sessionManager = sessionManager;
        this.decoder = new JT808Decoder();
        this.encoder = new JT808Encoder();
        this.enablePerformanceLogging = enablePerformanceLogging;
        this.performanceLogInterval = performanceLogInterval;
        this.messageHandlers = new ConcurrentHashMap<>();

        // 初始化消息处理器
        initializeMessageHandlers();

        // 启动性能监控
        if (enablePerformanceLogging) {
            startPerformanceMonitoring();
        }
    }

    /**
     * 处理TCP消息
     *
     * @param sessionId 会话ID
     * @param buffer    消息数据
     */
    public void handleTcpMessage(String sessionId, Buffer buffer) {
        long startTime = System.nanoTime();
        Session session = sessionManager.getSession(sessionId);

        if (session == null) {
            logger.warn("TCP消息处理失败: 会话不存在 - {}", sessionId);
            errorCount.incrementAndGet();
            return;
        }

        try {
            // 解码消息
            JT808Message message = decoder.decode(buffer);

            if (logger.isDebugEnabled()) {
                logger.debug("收到TCP消息: 会话={}, 消息ID=0x{}, 手机号={}, 流水号={}",
                        sessionId,
                        Integer.toHexString(message.getMessageId()).toUpperCase(),
                        message.getHeader().getPhoneNumber(),
                        message.getHeader().getSerialNumber());
            }

            // 更新会话信息
            session.incrementReceivedCount();

            // 绑定手机号到会话
            String phoneNumber = message.getHeader().getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.equals(session.getPhoneNumber())) {
                sessionManager.bindPhoneToSession(sessionId, phoneNumber);
                logger.info("会话绑定手机号: {} -> {}", sessionId, phoneNumber);
            }

            // 处理具体消息
            processMessage(session, message);

            // 统计处理时间
            long processingTime = System.nanoTime() - startTime;
            totalMessagesProcessed.incrementAndGet();
            totalProcessingTime.addAndGet(processingTime);

        } catch (ProtocolException e) {
            logger.error("TCP消息解码失败: 会话={}, 错误={}", sessionId, e.getMessage());
            errorCount.incrementAndGet();
            // 发送错误响应
            sendErrorResponse(session, null, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        } catch (Exception e) {
            logger.error("TCP消息处理异常: 会话={}", sessionId, e);
            errorCount.incrementAndGet();
            // 发送通用错误响应
            sendErrorResponse(session, null, T8001PlatformCommonResponse.RESULT_FAILURE);
        }
    }

    /**
     * 处理UDP消息
     *
     * @param packet UDP数据包
     */
    public void handleUdpMessage(DatagramPacket packet) {
        long startTime = System.nanoTime();

        try {
            // 解码消息
            JT808Message message = decoder.decode(packet.data());

            if (logger.isDebugEnabled()) {
                logger.debug("收到UDP消息: 发送方={}, 消息ID=0x{}, 手机号={}, 流水号={}",
                        packet.sender(),
                        Integer.toHexString(message.getMessageId()).toUpperCase(),
                        message.getHeader().getPhoneNumber(),
                        message.getHeader().getSerialNumber());
            }

            // UDP消息处理（简化版，实际可能需要更复杂的会话管理）
            String phoneNumber = message.getHeader().getPhoneNumber();
            Session session = sessionManager.getSessionByPhone(phoneNumber);

            if (session != null) {
                session.incrementReceivedCount();
                processMessage(session, message);
            } else {
                logger.warn("UDP消息处理失败: 会话不存在 - 手机号={}", phoneNumber);
                errorCount.incrementAndGet();
                // UDP无法直接响应，记录日志即可
            }

            // 统计处理时间
            long processingTime = System.nanoTime() - startTime;
            totalMessagesProcessed.incrementAndGet();
            totalProcessingTime.addAndGet(processingTime);

        } catch (ProtocolException e) {
            logger.error("UDP消息解码失败: 发送方={}, 错误={}", packet.sender(), e.getMessage());
            errorCount.incrementAndGet();
        } catch (Exception e) {
            logger.error("UDP消息处理异常: 发送方={}", packet.sender(), e);
            errorCount.incrementAndGet();
        }
    }

    /**
     * 处理具体消息
     *
     * @param session 会话
     * @param message 消息
     */
    private void processMessage(Session session, JT808Message message) {
        int messageId = message.getMessageId();

        try {
            // 更新会话活跃状态
            updateSessionActivity(session, message);

            // 处理分包逻辑
            if (!handleMessagePackaging(session, message)) {
                logger.debug("分包消息处理失败，跳过当前消息");
                return;
            }

            // 验证会话认证状态
//            if (!validateSessionAuthentication(session, message)) {
//                logger.debug("会话认证验证失败，已发送认证失败响应");
//                return;
//            }

            // 使用处理器映射进行消息分发
            BiConsumer<Session, JT808Message> handler = messageHandlers.get(messageId);

            if (handler != null) {
                logger.debug("分发消息: 会话={}, 消息ID=0x{}, 流水号={}",
                        session.getSessionId(),
                        Integer.toHexString(messageId).toUpperCase(),
                        message.getHeader().getSerialNumber());

                handler.accept(session, message);

                // 记录消息处理统计
                logMessageStatistics();
            } else {
                logger.warn("未支持的消息类型: 会话={}, 消息ID=0x{}",
                        session.getSessionId(), Integer.toHexString(messageId).toUpperCase());
                // 发送不支持的消息类型响应
                sendErrorResponse(session, message, (byte) T8001PlatformCommonResponse.RESULT_NOT_SUPPORTED);
            }
        } catch (Exception e) {
            logger.error("消息处理异常: 会话={}, 消息ID=0x{}",
                    session.getSessionId(), Integer.toHexString(messageId).toUpperCase(), e);
            // 发送处理失败响应
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_FAILURE);
        }
    }

    /**
     * 处理终端通用应答
     */
    private void handleTerminalCommonResponse(Session session, JT808Message message) {
        if (message instanceof T0001TerminalCommonResponse response) {
            logger.debug("处理终端通用应答: 会话={}, 应答流水号={}, 应答消息ID=0x{}, 结果={}",
                    session.getSessionId(), response.getResponseSerialNumber(),
                    Integer.toHexString(response.getResponseMessageId()).toUpperCase(),
                    response.getResultDescription());

            // 这里可以根据应答结果进行相应处理
            // 例如：更新消息发送状态、重发失败消息等
        } else {
            logger.warn("收到非T0001类型的通用应答消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
        }
    }

    /**
     * 处理终端心跳
     */
    private void handleTerminalHeartbeat(Session session, JT808Message message) {
        if (message instanceof T0002TerminalHeartbeat) {
            logger.debug("处理终端心跳: 会话={}, 手机号={}",
                    session.getSessionId(), session.getPhoneNumber());

            // 更新会话活跃时间（已在session.incrementReceivedCount()中处理）
            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);
        } else {
            logger.warn("收到非T0002类型的心跳消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理终端注册
     */
    private void handleTerminalRegister(Session session, JT808Message message) {
        if (message instanceof T0100TerminalRegister registerMsg) {
            logger.info("终端注册: 会话={}, 省域ID={}, 市县域ID={}, 制造商ID={}, 终端型号={}, 终端ID={}, 车牌号={}",
                    session.getSessionId(), registerMsg.getProvinceId(), registerMsg.getCityId(),
                    registerMsg.getManufacturerId(), registerMsg.getTerminalModel(),
                    registerMsg.getTerminalId(), registerMsg.getPlateNumber());

            // 这里可以添加注册验证逻辑
            // 例如：验证终端是否在白名单中，车辆信息是否正确等
            boolean registrationValid = validateTerminalRegistration(registerMsg);

            if (registrationValid) {
                // 生成鉴权码
                String authCode = generateAuthCode(registerMsg.getTerminalId());

                // 更新会话信息
                session.setPhoneNumber(message.getHeader().getPhoneNumber());

                logger.info("终端注册成功: 会话={}, 终端ID={}, 鉴权码={}",
                        session.getSessionId(), registerMsg.getTerminalId(), authCode);

                sendRegisterResponse(session, message, T8100TerminalRegisterResponse.RESULT_SUCCESS, authCode);
            } else {
                logger.warn("终端注册失败: 会话={}, 终端ID={}",
                        session.getSessionId(), registerMsg.getTerminalId());
                sendRegisterResponse(session, message, T8100TerminalRegisterResponse.RESULT_TERMINAL_NOT_IN_DATABASE, null);
            }
        } else {
            logger.warn("收到非T0100类型的注册消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendRegisterResponse(session, message, T8100TerminalRegisterResponse.RESULT_TERMINAL_NOT_IN_DATABASE, null);
        }
    }

    /**
     * 处理终端鉴权
     */
    private void handleTerminalAuth(Session session, JT808Message message) {
        if (message instanceof T0102TerminalAuth authMsg) {
            logger.info("终端鉴权: 会话={}, 鉴权码={}, IMEI={}, 软件版本={}, 2019版本={}",
                    session.getSessionId(), authMsg.getAuthCode(), authMsg.getImei(),
                    authMsg.getSoftwareVersion(), authMsg.is2019Version());

            // 验证鉴权码
            if (authMsg.isAuthCodeValid() && validateAuthCode(authMsg.getAuthCode())) {
                session.setAuthenticated(true);

                // 更新协议版本信息
                if (authMsg.is2019Version()) {
                    session.setProtocolVersion((byte) 0x01); // 2019版本
                }

                logger.info("终端鉴权成功: 会话={}, 手机号={}, 鉴权码={}",
                        session.getSessionId(), session.getPhoneNumber(), authMsg.getAuthCode());

                sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);
            } else {
                logger.warn("终端鉴权失败: 会话={}, 鉴权码={}",
                        session.getSessionId(), authMsg.getAuthCode());
                sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_FAILURE);
            }
        } else {
            logger.warn("收到非T0102类型的鉴权消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理位置信息汇报
     */
    private void handleLocationReport(Session session, JT808Message message) {
        if (message instanceof T0200LocationReport locationMsg) {
            logger.info("位置信息汇报: 会话={}, 报警标志={}, 状态={}, 纬度={}, 经度={}, 高程={}, 速度={}, 方向={}, 时间={}",
                    session.getSessionId(),
                    String.format("0x%08X", locationMsg.getAlarmFlag()),
                    String.format("0x%08X", locationMsg.getStatusFlag()),
                    locationMsg.getLatitude(), locationMsg.getLongitude(),
                    locationMsg.getAltitude(), locationMsg.getSpeed(),
                    locationMsg.getDirection(), locationMsg.getDateTime());

            // 检查是否有报警信息
            if (locationMsg.getAlarmFlag() != 0) {
                logger.warn("终端报警: 会话={}, 报警标志={}, 位置=({}, {})",
                        session.getSessionId(),
                        String.format("0x%08X", locationMsg.getAlarmFlag()),
                        locationMsg.getLatitude(), locationMsg.getLongitude());

                // 处理报警逻辑
                handleAlarmEvent(session, locationMsg);
            }

            // 检查附加信息
            if (locationMsg.getAdditionalInfoList() != null && !locationMsg.getAdditionalInfoList().isEmpty()) {
                logger.debug("位置汇报包含附加信息: 会话={}, 附加信息数量={}",
                        session.getSessionId(), locationMsg.getAdditionalInfoList().size());

                // 处理附加信息
                processAdditionalInfo(session, locationMsg);
            }

            // 存储位置信息
            storeLocationData(session, locationMsg);

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

            logger.debug("位置信息处理完成: 会话={}, 流水号={}",
                    session.getSessionId(), message.getHeader().getSerialNumber());
        } else {
            logger.warn("收到非T0200类型的位置汇报消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 发送平台通用应答
     */
    private void sendCommonResponse(Session session, JT808Message originalMessage, byte result) {
        try {
            T8001PlatformCommonResponse response = new T8001PlatformCommonResponse();
            response.setResponseSerialNumber(originalMessage.getHeader().getSerialNumber());
            response.setResponseMessageId(originalMessage.getHeader().getMessageId());
            response.setResult(result);

            // 构造响应消息头
            JT808Header header = new JT808Header();
            header.setMessageId(T8001PlatformCommonResponse.MESSAGE_ID);
            header.setPhoneNumber(session.getPhoneNumber());
            header.setSerialNumber(session.nextSerialNumber());
            response.setHeader(header);

            encoder.encode(response);

            // 编码并发送
            Buffer buffer = response.encodeBody();
            session.send(buffer);

            logger.debug("发送平台通用应答: 会话={}, 原消息ID=0x{}, 流水号={}, 结果={}",
                    session.getSessionId(),
                    String.format("%04X", originalMessage.getHeader().getMessageId()),
                    originalMessage.getHeader().getSerialNumber(), result);

        } catch (Exception e) {
            logger.error("发送平台通用应答失败: 会话={}, 错误={}",
                    session.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * 发送终端注册应答
     */
    private void sendRegisterResponse(Session session, JT808Message originalMessage, byte result, String authCode) {
        try {
            T8100TerminalRegisterResponse response = new T8100TerminalRegisterResponse();
            response.setResponseSerialNumber(originalMessage.getHeader().getSerialNumber());
            response.setResult(result);
            if (authCode != null) {
                response.setAuthCode(authCode);
            }

            // 构造响应消息头
            JT808Header header = new JT808Header();
            header.setMessageId(response.getMessageId());
            header.setPhoneNumber(session.getPhoneNumber());
            header.setSerialNumber(session.nextSerialNumber());

            // 创建完整的响应消息
            response.setHeader(header);

            // 编码并发送
            Buffer buffer = encoder.encode(response);
            session.send(buffer);

            logger.debug("发送终端注册应答: 会话={}, 流水号={}, 结果={}, 鉴权码={}",
                    session.getSessionId(), originalMessage.getHeader().getSerialNumber(),
                    result, authCode);

        } catch (Exception e) {
            logger.error("发送终端注册应答失败: 会话={}, 错误={}",
                    session.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * 生成鉴权码
     *
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
     * 验证终端注册信息
     */
    private boolean validateTerminalRegistration(T0100TerminalRegister registerMsg) {
        // 这里可以添加具体的验证逻辑
        // 例如：检查终端是否在白名单中，验证车辆信息等

        // 基本验证
        if (registerMsg.getTerminalId() == null || registerMsg.getTerminalId().trim().isEmpty()) {
            logger.warn("终端ID为空");
            return false;
        }

        if (registerMsg.getPlateNumber() == null || registerMsg.getPlateNumber().trim().isEmpty()) {
            logger.warn("车牌号为空");
            return false;
        }

        // 可以添加更多验证逻辑
        // - 检查终端ID格式
        // - 验证车牌号格式
        // - 查询数据库确认终端是否已注册
        // - 验证制造商ID是否合法

        return true; // 简化处理，默认通过验证
    }

    /**
     * 验证鉴权码
     */
    private boolean validateAuthCode(String authCode) {
        // 这里可以添加鉴权码验证逻辑
        // 例如：检查鉴权码是否有效，是否已过期等

        if (authCode == null || authCode.trim().isEmpty()) {
            return false;
        }

        // 可以添加更多验证逻辑
        // - 检查鉴权码格式
        // - 验证鉴权码是否由平台生成
        // - 检查鉴权码是否已过期
        // - 查询数据库确认鉴权码状态

        return true; // 简化处理，默认通过验证
    }
    

    /**
     * 处理报警事件
     */
    private void handleAlarmEvent(Session session, T0200LocationReport locationMsg) {
        int alarmFlag = locationMsg.getAlarmFlag();

        // 解析具体的报警类型
        if ((alarmFlag & 0x00000001) != 0) {
            logger.warn("紧急报警: 会话={}", session.getSessionId());
            // 处理紧急报警
        }

        if ((alarmFlag & 0x00000002) != 0) {
            logger.warn("超速报警: 会话={}", session.getSessionId());
            // 处理超速报警
        }

        if ((alarmFlag & 0x00000004) != 0) {
            logger.warn("疲劳驾驶报警: 会话={}", session.getSessionId());
            // 处理疲劳驾驶报警
        }

        if ((alarmFlag & 0x00000008) != 0) {
            logger.warn("危险预警: 会话={}", session.getSessionId());
            // 处理危险预警
        }

        if ((alarmFlag & 0x00000010) != 0) {
            logger.warn("GNSS模块发生故障: 会话={}", session.getSessionId());
            // 处理GNSS故障
        }

        if ((alarmFlag & 0x00000020) != 0) {
            logger.warn("GNSS天线未接或被剪断: 会话={}", session.getSessionId());
            // 处理GNSS天线故障
        }

        if ((alarmFlag & 0x00000040) != 0) {
            logger.warn("GNSS天线短路: 会话={}", session.getSessionId());
            // 处理GNSS天线短路
        }

        if ((alarmFlag & 0x00000080) != 0) {
            logger.warn("终端主电源欠压: 会话={}", session.getSessionId());
            // 处理主电源欠压
        }

        if ((alarmFlag & 0x00000100) != 0) {
            logger.warn("终端主电源掉电: 会话={}", session.getSessionId());
            // 处理主电源掉电
        }

        if ((alarmFlag & 0x00000200) != 0) {
            logger.warn("终端LCD或显示器故障: 会话={}", session.getSessionId());
            // 处理显示器故障
        }

        // 可以继续添加其他报警类型的处理

        // 这里可以添加报警处理逻辑
        // 例如：发送报警通知、记录报警日志、触发应急响应等
    }

    /**
     * 处理附加信息
     */
    private void processAdditionalInfo(Session session, T0200LocationReport locationMsg) {
        // 这里可以处理位置汇报中的附加信息
        // 例如：里程、油量、速度、行驶记录仪速度、需要人工确认报警事件的ID等

        logger.debug("处理位置汇报附加信息: 会话={}", session.getSessionId());

        // 可以根据具体的附加信息类型进行处理
        // 附加信息的格式通常是：ID(1字节) + 长度(1字节) + 数据(N字节)
    }

    /**
     * 存储位置数据
     */
    private void storeLocationData(Session session, T0200LocationReport locationMsg) {
        // 这里可以实现位置数据的存储逻辑
        // 例如：存储到数据库、缓存、消息队列等

        logger.debug("存储位置数据: 会话={}, 纬度={}, 经度={}",
                session.getSessionId(), locationMsg.getLatitude(), locationMsg.getLongitude());

        // 示例：可以将位置数据发送到消息队列进行异步处理
        // messageQueue.send(createLocationMessage(session, locationMsg));

        // 示例：可以存储到数据库
        // locationRepository.save(createLocationEntity(session, locationMsg));
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(Session session, JT808Message originalMessage, byte result) {
        if (session == null) {
            logger.warn("无法发送错误响应: 会话为空");
            return;
        }

        try {
            if (originalMessage != null) {
                // 发送平台通用应答
                sendCommonResponse(session, originalMessage, result);
            } else {
                // 构造一个简单的错误响应
                T8001PlatformCommonResponse response = new T8001PlatformCommonResponse();
                response.setResponseSerialNumber(0); // 无法获取原始流水号
                response.setResponseMessageId(0x0000); // 无法获取原始消息ID
                response.setResult(result);

                // 构造响应消息头
                JT808Header header = new JT808Header();
                header.setMessageId(T8001PlatformCommonResponse.MESSAGE_ID);
                header.setPhoneNumber(session.getPhoneNumber());
                header.setSerialNumber(session.nextSerialNumber());

                // 创建完整的响应消息
                JT808Message responseMessage = response;
                responseMessage.setHeader(header);

                // 编码并发送
                Buffer buffer = encoder.encode(responseMessage);
                session.send(buffer);

                logger.debug("发送错误响应: 会话={}, 结果={}", session.getSessionId(), result);
            }
        } catch (Exception e) {
            logger.error("发送错误响应失败: 会话={}, 错误={}",
                    session.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * 检查会话是否已认证（对于需要认证的消息）
     */
    private boolean isAuthenticationRequired(int messageId) {
        // 这些消息不需要认证
        switch (messageId) {
            case 0x0100: // 终端注册
            case 0x0102: // 终端鉴权
                return false;
            default:
                return true; // 其他消息都需要认证
        }
    }

    /**
     * 验证消息是否需要认证并检查会话状态
     */
    private boolean validateSessionAuthentication(Session session, JT808Message message) {
        int messageId = message.getMessageId();

        if (isAuthenticationRequired(messageId) && !session.isAuthenticated()) {
            logger.warn("收到未认证会话的消息: 会话={}, 消息ID=0x{}",
                    session.getSessionId(), Integer.toHexString(messageId).toUpperCase());

            // 发送认证失败响应
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_FAILURE);
            return false;
        }

        return true;
    }

    /**
     * 记录消息处理统计
     */
    private void logMessageStatistics() {
        if (enablePerformanceLogging) {
            long totalMessages = totalMessagesProcessed.get();
            if (totalMessages > 0 && totalMessages % 1000 == 0) {
                logger.info("消息处理统计: {}", getPerformanceStats());
            }
        }
    }

    /**
     * 处理消息分包逻辑
     */
    private boolean handleMessagePackaging(Session session, JT808Message message) {
        JT808Header header = message.getHeader();

        // 检查是否为分包消息
        if (header.isSubpackage()) {
            String sessionId = session.getSessionId();
            String packageKey = sessionId + "_" + header.getMessageId() + "_" + header.getSerialNumber();

            logger.debug("收到分包消息: 会话={}, 消息ID=0x{}, 包总数={}, 包序号={}",
                    sessionId,
                    Integer.toHexString(header.getMessageId()).toUpperCase(),
                    header.getPackageInfo().getTotalPackages(), header.getPackageInfo().getPackageSequence());

            // 获取或创建分包缓存
            Map<Integer, JT808Message> packages = subpackageCache.computeIfAbsent(packageKey,
                    k -> new ConcurrentHashMap<>());

            // 存储当前分包
            packages.put(header.getPackageInfo().getPackageSequence(), message);
            subpackageTimestamp.put(packageKey, System.currentTimeMillis());

            // 检查是否收齐所有分包
            if (packages.size() == header.getPackageInfo().getTotalPackages()) {
                logger.debug("分包消息收齐: 会话={}, 消息ID=0x{}, 总包数={}",
                        sessionId, Integer.toHexString(header.getMessageId()).toUpperCase(),
                        header.getPackageInfo().getTotalPackages());

                // 重组完整消息
                JT808Message completeMessage = reassembleMessage(packages, header);
                if (completeMessage != null) {
                    // 清理缓存
                    subpackageCache.remove(packageKey);
                    subpackageTimestamp.remove(packageKey);

                    // 处理重组后的完整消息
                    return processReassembledMessage(session, completeMessage);
                } else {
                    logger.error("分包消息重组失败: 会话={}, 消息ID=0x{}",
                            sessionId, Integer.toHexString(header.getMessageId()).toUpperCase());
                    // 清理失败的缓存
                    subpackageCache.remove(packageKey);
                    subpackageTimestamp.remove(packageKey);
                    return false;
                }
            } else {
                // 还未收齐所有分包，等待后续分包
                logger.debug("等待更多分包: 会话={}, 消息ID=0x{}, 已收到={}/{}",
                        sessionId, Integer.toHexString(header.getMessageId()).toUpperCase(),
                        packages.size(), header.getPackageInfo().getTotalPackages());
                return false; // 不处理当前分包
            }
        }

        return true; // 非分包消息直接处理
    }

    /**
     * 重组分包消息
     */
    private JT808Message reassembleMessage(Map<Integer, JT808Message> packages, JT808Header originalHeader) {
        try {
            // 按包序号排序并合并消息体
            StringBuilder bodyBuilder = new StringBuilder();
            for (int i = 1; i <= packages.size(); i++) {
                JT808Message pkg = packages.get(i);
                if (pkg == null) {
                    logger.error("分包缺失: 包序号={}", i);
                    return null;
                }
                // 这里需要根据实际的消息体格式进行合并
                // 暂时简化处理
                bodyBuilder.append(pkg.getBody().toString());
            }

            // 创建重组后的消息
            // TODO: 这里需要根据消息ID创建具体的消息类型实例
            // 暂时返回null，需要后续完善分包重组逻辑
            logger.warn("分包重组功能暂未完全实现，需要根据消息ID创建具体消息类型");
            return null;
        } catch (Exception e) {
            logger.error("分包重组异常", e);
            return null;
        }
    }

    /**
     * 处理重组后的完整消息
     */
    private boolean processReassembledMessage(Session session, JT808Message message) {
        try {
            logger.info("处理重组消息: 会话={}, 消息ID=0x{}",
                    session.getSessionId(),
                    Integer.toHexString(message.getMessageId()).toUpperCase());

            // 递归调用消息处理逻辑（但此时消息已经不是分包了）
            return true; // 返回true表示可以继续处理
        } catch (Exception e) {
            logger.error("处理重组消息失败", e);
            return false;
        }
    }

    /**
     * 清理过期的分包缓存
     */
    public void cleanupExpiredSubpackages() {
        long currentTime = System.currentTimeMillis();

        subpackageTimestamp.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > subpackageTimeout) {
                String packageKey = entry.getKey();
                subpackageCache.remove(packageKey);
                logger.debug("清理过期分包缓存: {}", packageKey);
                return true;
            }
            return false;
        });
    }

    /**
     * 更新会话活跃状态
     */
    private void updateSessionActivity(Session session, JT808Message message) {
        session.updateActiveTime();

        // 更新协议版本信息（如果消息头包含版本信息）
        JT808Header header = message.getHeader();
        if (header.getProtocolVersion() != 0) {
            session.setProtocolVersion(header.getProtocolVersion());
        }
    }

    /**
     * 处理查询终端参数应答
     */
    private void handleQueryTerminalParametersResponse(Session session, JT808Message message) {
        if (message instanceof T0104QueryTerminalParametersResponse response) {
            logger.info("查询终端参数应答: 会话={}, 应答流水号={}, 参数个数={}",
                    session.getSessionId(), response.getResponseSerialNumber(), response.getParameterCount());

            // 处理参数列表
            if (response.getParameterItems() != null && !response.getParameterItems().isEmpty()) {
                for (ParameterItem param : response.getParameterItems()) {
                    logger.debug("终端参数: ID=0x{}, 长度={}, 值={}",
                            String.format("%08X", param.getParameterId()),
                            param.getParameterLength(),
                            param.getParameterDescription());
                }
            }

            // 这里可以添加参数处理逻辑
            // 例如：更新终端配置缓存、触发配置变更事件等

        } else {
            logger.warn("收到非T0104类型的参数应答消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
        }
    }

    /**
     * 处理查询终端属性应答
     */
    private void handleQueryTerminalPropertyResponse(Session session, JT808Message message) {
        if (message instanceof T0107QueryTerminalPropertyResponse response) {
            logger.info("查询终端属性应答: 会话={}, 终端类型={}, 制造商ID={}, 终端型号={}, 终端ID={}, ICCID={}, 硬件版本={}, 固件版本={}, GNSS模块属性={}, 通信模块属性={}",
                    session.getSessionId(), response.getTerminalType(), response.getManufacturerId(),
                    response.getTerminalModel(), response.getTerminalId(), response.getIccid(),
                    response.getHardwareVersion(), response.getFirmwareVersion(),
                    response.getGnssAttribute(), response.getCommunicationAttribute());

            // 这里可以添加属性处理逻辑
            // 例如：更新终端信息、记录设备档案等

        } else {
            logger.warn("收到非T0107类型的属性应答消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
        }
    }

    /**
     * 处理终端升级结果通知
     */
    private void handleTerminalUpgradeResultNotification(Session session, JT808Message message) {
        if (message instanceof T0108TerminalUpgradeResultNotification notification) {
            logger.info("终端升级结果通知: 会话={}, 升级类型={}, 升级结果={}",
                    session.getSessionId(), notification.getUpgradeType(), notification.getUpgradeResult());

            // 处理升级结果
            if (notification.getUpgradeResult() == 0) {
                logger.info("终端升级成功: 会话={}", session.getSessionId());
                // 升级成功处理逻辑
            } else {
                logger.warn("终端升级失败: 会话={}, 结果码={}",
                        session.getSessionId(), notification.getUpgradeResult());
                // 升级失败处理逻辑
            }

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

        } else {
            logger.warn("收到非T0108类型的升级结果通知消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理位置信息查询应答
     */
    private void handlePositionInfoQueryResponse(Session session, JT808Message message) {
        if (message instanceof T0201PositionInfoQueryResponse response) {
            logger.info("位置信息查询应答: 会话={}, 应答流水号={}, 位置信息={}",
                    session.getSessionId(), response.getResponseSerialNumber(),
                    response.getLocationReport() != null ? "已包含" : "未包含");

            // 处理位置信息
            if (response.getLocationReport() != null) {
                T0200LocationReport locationInfo = response.getLocationReport();
                logger.info("查询到的位置: 纬度={}, 经度={}, 时间={}",
                        locationInfo.getLatitude(), locationInfo.getLongitude(), locationInfo.getDateTime());

                // 这里可以添加位置信息处理逻辑
                storeLocationData(session, locationInfo);
            }

        } else {
            logger.warn("收到非T0201类型的位置查询应答消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
        }
    }

    // ==================== 平台下行消息处理器 ====================

    /**
     * 处理平台通用应答（通常不会收到，但为了完整性添加）
     */
    private void handlePlatformCommonResponse(Session session, JT808Message message) {
        logger.debug("收到平台通用应答: 会话={}", session.getSessionId());
        // 平台通常不会收到这个消息，记录日志即可
    }

    /**
     * 处理补传分包请求
     */
    private void handleResendSubpackageRequest(Session session, JT808Message message) {
        if (message instanceof T8003ResendSubpackageRequest request) {
            logger.info("补传分包请求: 会话={}, 原消息流水号={}, 重传包ID列表={}",
                    session.getSessionId(), request.getOriginalSerialNumber(), request.getRetransmitPackageIds());

            // 这里需要实现分包重传逻辑
            // 例如：查找原始消息、重新发送指定的分包等

        } else {
            logger.warn("收到非T8003类型的补传分包请求消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
        }
    }

    /**
     * 处理终端注册应答（通常不会收到，但为了完整性添加）
     */
    private void handleTerminalRegisterResponse(Session session, JT808Message message) {
        logger.debug("收到终端注册应答: 会话={}", session.getSessionId());
        // 平台通常不会收到这个消息，记录日志即可
    }

    /**
     * 处理设置终端参数
     */
    private void handleTerminalParameterSetting(Session session, JT808Message message) {
        if (message instanceof T8103TerminalParameterSetting setting) {
            logger.info("设置终端参数: 会话={}, 参数个数={}",
                    session.getSessionId(), setting.getParameterCount());

            // 这里需要实现参数设置逻辑
            // 例如：验证参数、应用配置、发送应答等

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

        } else {
            logger.warn("收到非T8103类型的参数设置消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理查询终端参数
     */
    private void handleQueryTerminalParameters(Session session, JT808Message message) {
        if (message instanceof T8104QueryTerminalParameters query) {
            logger.info("查询终端参数: 会话={}", session.getSessionId());

            // 这里需要实现参数查询逻辑
            // 例如：获取终端配置、构造应答消息等

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

        } else {
            logger.warn("收到非T8104类型的参数查询消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理终端控制
     */
    private void handleTerminalControl(Session session, JT808Message message) {
        if (message instanceof T8105TerminalControl control) {
            logger.info("终端控制: 会话={}, 命令字={}, 命令参数={}",
                    session.getSessionId(), control.getCommandWord(), control.getCommandParameters());

            // 这里需要实现终端控制逻辑
            // 例如：执行控制命令、更新终端状态等

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

        } else {
            logger.warn("收到非T8105类型的终端控制消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理查询指定终端参数
     */
    private void handleQuerySpecificTerminalParameters(Session session, JT808Message message) {
        if (message instanceof T8106QuerySpecificTerminalParameters query) {
            logger.info("查询指定终端参数: 会话={}, 参数ID个数={}",
                    session.getSessionId(), query.getParameterCount());

            // 这里需要实现指定参数查询逻辑

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

        } else {
            logger.warn("收到非T8106类型的指定参数查询消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理查询终端属性
     */
    private void handleQueryTerminalProperty(Session session, JT808Message message) {
        if (message instanceof T8107QueryTerminalProperty query) {
            logger.info("查询终端属性: 会话={}", session.getSessionId());

            // 这里需要实现属性查询逻辑

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

        } else {
            logger.warn("收到非T8107类型的属性查询消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理终端升级包
     */
    private void handleTerminalUpgradePackage(Session session, JT808Message message) {
        if (message instanceof T8108TerminalUpgradePackage upgrade) {
            logger.info("终端升级包: 会话={}, 升级类型={}, 制造商ID={}, 版本号={}, 升级数据包长度={}",
                    session.getSessionId(), upgrade.getUpgradeType(), upgrade.getManufacturerId(),
                    upgrade.getVersion(), upgrade.getUpgradeDataLength());

            // 这里需要实现升级包处理逻辑

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

        } else {
            logger.warn("收到非T8108类型的升级包消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理位置信息查询
     */
    private void handlePositionInfoQuery(Session session, JT808Message message) {
        if (message instanceof T8201PositionInfoQuery query) {
            logger.info("位置信息查询: 会话={}", session.getSessionId());

            // 这里需要实现位置查询逻辑

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

        } else {
            logger.warn("收到非T8201类型的位置查询消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理临时位置跟踪控制
     */
    private void handleTemporaryLocationTrackingControl(Session session, JT808Message message) {
        if (message instanceof T8202TemporaryLocationTrackingControl control) {
            logger.info("临时位置跟踪控制: 会话={}, 时间间隔={}, 位置跟踪有效期={}",
                    session.getSessionId(), control.getTimeInterval(), control.getValidityPeriod());

            // 这里需要实现临时跟踪控制逻辑

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

        } else {
            logger.warn("收到非T8202类型的临时跟踪控制消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理人工确认报警消息
     */
    private void handleManualAlarmConfirmation(Session session, JT808Message message) {
        if (message instanceof T8203ManualAlarmConfirmation confirmation) {
            logger.info("人工确认报警消息: 会话={}, 报警消息流水号={}, 人工确认报警类型={}",
                    session.getSessionId(), confirmation.getAlarmSequenceNumber(), confirmation.getConfirmationAlarmType());

            // 这里需要实现报警确认逻辑

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

        } else {
            logger.warn("收到非T8203类型的报警确认消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理文本信息下发
     */
    private void handleTextInfoDistribution(Session session, JT808Message message) {
        if (message instanceof T8300TextInfoDistribution textInfo) {
            logger.info("文本信息下发: 会话={}, 文本信息标志={}, 文本信息={}",
                    session.getSessionId(), textInfo.getTextFlag(), textInfo.getTextInfo());

            // 这里需要实现文本信息处理逻辑

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

        } else {
            logger.warn("收到非T8300类型的文本信息消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 处理事件设置
     */
    private void handleEventSetting(Session session, JT808Message message) {
        if (message instanceof T8301EventSetting eventSetting) {
            logger.info("事件设置: 会话={}, 设置类型={}, 事件项总数={}",
                    session.getSessionId(), eventSetting.getSettingType(), eventSetting.getEventCount());

            // 这里需要实现事件设置逻辑

            // 发送平台通用应答
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_SUCCESS);

        } else {
            logger.warn("收到非T8301类型的事件设置消息: 会话={}, 类型={}",
                    session.getSessionId(), message.getClass().getSimpleName());
            sendCommonResponse(session, message, T8001PlatformCommonResponse.RESULT_MESSAGE_ERROR);
        }
    }

    /**
     * 获取性能统计信息
     */
    public String getPerformanceStats() {
        long totalMessages = totalMessagesProcessed.get();
        long totalTime = totalProcessingTime.get();
        long errors = errorCount.get();

        double avgProcessingTime = totalMessages > 0 ? (double) totalTime / totalMessages / 1_000_000.0 : 0.0;

        return String.format("总消息数: %d, 平均处理时间: %.2f ms, 错误数: %d, 错误率: %.2f%%, 缓存大小: %d",
                totalMessages, avgProcessingTime, errors,
                totalMessages > 0 ? (double) errors / totalMessages * 100.0 : 0.0, cacheSize.get());
    }

    /**
     * 重置性能统计
     */
    public void resetPerformanceStats() {
        totalMessagesProcessed.set(0);
        totalProcessingTime.set(0);
        errorCount.set(0);
        logger.info("性能统计已重置");
    }

    /**
     * 添加消息到缓存（用于批处理）
     */
    public void addMessageToCache(String sessionId, JT808Message message) {
        if (cacheSize.get() >= maxCacheSize) {
            logger.warn("消息缓存已满，丢弃消息: 会话={}, 消息ID=0x{}",
                    sessionId, Integer.toHexString(message.getMessageId()).toUpperCase());
            return;
        }

        messageCache.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
        cacheSize.incrementAndGet();

        // 检查是否需要触发批处理
        List<JT808Message> sessionMessages = messageCache.get(sessionId);
        if (sessionMessages != null && sessionMessages.size() >= batchSize) {
            processBatchMessages(sessionId);
        }
    }

    /**
     * 处理批量消息
     */
    private void processBatchMessages(String sessionId) {
        List<JT808Message> messages = messageCache.remove(sessionId);
        if (messages == null || messages.isEmpty()) {
            return;
        }

        Session session = sessionManager.getSession(sessionId);
        if (session == null) {
            logger.warn("批处理消息失败: 会话不存在 - {}", sessionId);
            cacheSize.addAndGet(-messages.size());
            return;
        }

        logger.debug("开始批处理消息: 会话={}, 消息数量={}", sessionId, messages.size());

        for (JT808Message message : messages) {
            try {
                processMessage(session, message);
            } catch (Exception e) {
                logger.error("批处理消息失败: 会话={}, 消息ID=0x{}",
                        sessionId, Integer.toHexString(message.getMessageId()).toUpperCase(), e);
                errorCount.incrementAndGet();
            }
        }

        cacheSize.addAndGet(-messages.size());
        logger.debug("批处理消息完成: 会话={}, 处理数量={}", sessionId, messages.size());
    }

    /**
     * 强制处理所有缓存的消息
     */
    public void flushAllCachedMessages() {
        logger.info("开始清空所有缓存消息，当前缓存大小: {}", cacheSize.get());

        for (String sessionId : messageCache.keySet()) {
            processBatchMessages(sessionId);
        }

        logger.info("缓存消息清空完成，剩余缓存大小: {}", cacheSize.get());
    }

    /**
     * 定期清理过期缓存和统计信息
     */
    public void performMaintenance() {
        // 清理过期分包
        cleanupExpiredSubpackages();

        // 强制处理超时的缓存消息
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, List<JT808Message>> entry : messageCache.entrySet()) {
            String sessionId = entry.getKey();
            List<JT808Message> messages = entry.getValue();

            if (!messages.isEmpty()) {
                // 检查最早的消息是否超时（这里简化处理，实际应该记录消息的时间戳）
                processBatchMessages(sessionId);
            }
        }

        // 记录性能统计
        if (enablePerformanceLogging) {
            logger.info("维护任务完成 - {}", getPerformanceStats());
        }
    }

    /**
     * 获取当前缓存状态
     */
    public Map<String, Object> getCacheStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("totalCacheSize", cacheSize.get());
        status.put("sessionCacheCount", messageCache.size());
        status.put("subpackageCacheCount", subpackageCache.size());
        status.put("maxCacheSize", maxCacheSize);
        status.put("batchSize", batchSize);
        return status;
    }
}