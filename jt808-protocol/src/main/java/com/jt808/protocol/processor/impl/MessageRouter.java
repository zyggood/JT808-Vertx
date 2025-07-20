package com.jt808.protocol.processor.impl;

import com.jt808.protocol.message.*;
import com.jt808.protocol.message.T0001TerminalCommonResponse;
import com.jt808.protocol.processor.MessageProcessor;
import com.jt808.protocol.processor.ProcessContext;
import com.jt808.protocol.processor.ProcessResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 消息路由器
 * 负责根据消息类型将消息路由到对应的处理方法
 */
public class MessageRouter implements MessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MessageRouter.class);
    
    private final Map<Integer, Function<JT808Message, Future<JT808Message>>> messageHandlers;
    private final String name;
    
    public MessageRouter() {
        this.name = "MessageRouter";
        this.messageHandlers = new ConcurrentHashMap<>();
        initializeMessageHandlers();
    }
    
    @Override
    public Future<ProcessResult> process(ProcessContext context) {
        long startTime = System.currentTimeMillis();
        Promise<ProcessResult> promise = Promise.promise();
        
        try {
            JT808Message message = context.getMessage();
            int messageId = message.getMessageId();
            
            Function<JT808Message, Future<JT808Message>> handler = messageHandlers.get(messageId);
            
            if (handler == null) {
                logger.warn("No handler found for message ID: 0x{}", Integer.toHexString(messageId));
                long duration = System.currentTimeMillis() - startTime;
                promise.complete(ProcessResult.skipped(name, "No handler for message ID: 0x" + Integer.toHexString(messageId)));
                return promise.future();
            }
            
            handler.apply(message)
                .onSuccess(responseMessage -> {
                    long duration = System.currentTimeMillis() - startTime;
                    if (responseMessage != null) {
                        context.setAttribute("responseMessage", responseMessage);
                    }
                    promise.complete(ProcessResult.success(name, duration, responseMessage));
                })
                .onFailure(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.error("Error processing message ID: 0x{}", Integer.toHexString(messageId), error);
                    promise.complete(ProcessResult.failed(name, duration, error));
                });
                
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Unexpected error in message routing", e);
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
        return 10; // 路由器应该有较高优先级
    }
    
    @Override
    public boolean canProcess(JT808Message message) {
        return messageHandlers.containsKey(message.getMessageId());
    }
    
    /**
     * 注册消息处理器
     * 
     * @param messageId 消息ID
     * @param handler 处理函数
     */
    public void registerHandler(int messageId, Function<JT808Message, Future<JT808Message>> handler) {
        messageHandlers.put(messageId, handler);
        logger.debug("Registered handler for message ID: 0x{}", Integer.toHexString(messageId));
    }
    
    /**
     * 移除消息处理器
     * 
     * @param messageId 消息ID
     */
    public void removeHandler(int messageId) {
        messageHandlers.remove(messageId);
        logger.debug("Removed handler for message ID: 0x{}", Integer.toHexString(messageId));
    }
    
    /**
     * 获取已注册的消息ID集合
     */
    public java.util.Set<Integer> getRegisteredMessageIds() {
        return messageHandlers.keySet();
    }
    
    /**
     * 初始化消息处理器映射
     * 这里先创建基础框架，具体的处理逻辑将在后续步骤中从JT808MessageHandler迁移过来
     */
    private void initializeMessageHandlers() {
        // 终端上行消息
        registerHandler(0x0001, this::handleTerminalCommonResponse);
        registerHandler(0x0002, this::handleTerminalHeartbeat);
        registerHandler(0x0100, this::handleTerminalRegister);
        registerHandler(0x0102, this::handleTerminalAuth);
        registerHandler(0x0104, this::handleQueryTerminalParametersResponse);
        registerHandler(0x0107, this::handleQueryTerminalPropertyResponse);
        registerHandler(0x0108, this::handleTerminalUpgradeResultNotification);
        registerHandler(0x0200, this::handleLocationReport);
        registerHandler(0x0201, this::handlePositionInfoQueryResponse);

        // 平台下行消息（用于处理终端的应答）
        registerHandler(0x8001, this::handlePlatformCommonResponse);
        registerHandler(0x8003, this::handleResendSubpackageRequest);
        registerHandler(0x8100, this::handleTerminalRegisterResponse);
        registerHandler(0x8103, this::handleTerminalParameterSetting);
        registerHandler(0x8104, this::handleQueryTerminalParameters);
        registerHandler(0x8105, this::handleTerminalControl);
        registerHandler(0x8106, this::handleQuerySpecificTerminalParameters);
        registerHandler(0x8107, this::handleQueryTerminalProperty);
        registerHandler(0x8108, this::handleTerminalUpgradePackage);
        registerHandler(0x8201, this::handlePositionInfoQuery);
        registerHandler(0x8202, this::handleTemporaryLocationTrackingControl);
        registerHandler(0x8203, this::handleManualAlarmConfirmation);
        registerHandler(0x8300, this::handleTextInfoDistribution);
        registerHandler(0x8301, this::handleEventSetting);
        
        logger.info("Initialized message router with {} handlers", messageHandlers.size());
    }
    
    // 终端上行消息处理方法
    private Future<JT808Message> handleTerminalCommonResponse(JT808Message message) {
        // 处理终端通用应答消息 (0x0001)
        if (!(message instanceof T0001TerminalCommonResponse)) {
            logger.warn("消息类型不匹配，期望T0001TerminalCommonResponse，实际: {}", message.getClass().getSimpleName());
            return Future.succeededFuture(null);
        }

        T0001TerminalCommonResponse response = (T0001TerminalCommonResponse) message;
        String terminalPhone = message.getHeader().getPhoneNumber();
        
        logger.info("收到终端[{}]通用应答: 流水号={}, 消息ID=0x{}, 结果={}", 
                   terminalPhone, 
                   response.getResponseSerialNumber(),
                   Integer.toHexString(response.getResponseMessageId()).toUpperCase(),
                   response.getResult());

        // 记录应答结果
        if (response.getResult() != T0001TerminalCommonResponse.RESULT_SUCCESS) {
            logger.warn("终端[{}]应答失败: 消息ID=0x{}, 结果={}", 
                       terminalPhone,
                       Integer.toHexString(response.getResponseMessageId()).toUpperCase(),
                       response.getResult());
        }

        // 通用应答通常不需要回复
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalHeartbeat(JT808Message message) {
        // 处理终端心跳消息 (0x0002)
        if (!(message instanceof T0002TerminalHeartbeat)) {
            logger.debug("消息类型不是T0002TerminalHeartbeat，但messageId为0x0002，按心跳消息处理: {}", message.getClass().getSimpleName());
        }

        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.debug("收到终端[{}]心跳消息", terminalPhone);

        // 创建平台通用应答
        T8001PlatformCommonResponse response = new T8001PlatformCommonResponse();
        JT808Header responseHeader = new JT808Header(
            0x8001, 
            terminalPhone, 
            message.getHeader().getSerialNumber()
        );
        response.setHeader(responseHeader);
        response.setResponseSerialNumber(message.getHeader().getSerialNumber());
        response.setResponseMessageId(0x0002);
        response.setResult(T8001PlatformCommonResponse.RESULT_SUCCESS);

        return Future.succeededFuture(response);
    }
    
    private Future<JT808Message> handleTerminalRegister(JT808Message message) {
        // 处理终端注册消息 (0x0100)
        if (!(message instanceof T0100TerminalRegister)) {
            logger.warn("消息类型不匹配，期望T0100TerminalRegister，实际: {}", message.getClass().getSimpleName());
            return Future.succeededFuture(null);
        }

        T0100TerminalRegister register = (T0100TerminalRegister) message;
        String terminalPhone = message.getHeader().getPhoneNumber();
        
        logger.info("收到终端[{}]注册请求: 省域ID={}, 市县域ID={}, 制造商ID={}, 终端型号={}, 终端ID={}, 车牌号={}",
                   terminalPhone,
                   register.getProvinceId(),
                   register.getCityId(), 
                   register.getManufacturerId(),
                   register.getTerminalModel(),
                   register.getTerminalId(),
                   register.getPlateNumber());

        // 创建终端注册应答
        T8100TerminalRegisterResponse response = new T8100TerminalRegisterResponse();
        JT808Header responseHeader = new JT808Header(
            0x8100,
            terminalPhone,
            message.getHeader().getSerialNumber()
        );
        response.setHeader(responseHeader);
        response.setResponseSerialNumber(message.getHeader().getSerialNumber());
        
        // 简单的注册逻辑：总是成功，生成鉴权码
        response.setResult(T8100TerminalRegisterResponse.RESULT_SUCCESS);
        String authCode = generateAuthCode(terminalPhone);
        response.setAuthCode(authCode);
        
        logger.info("终端[{}]注册成功，鉴权码: {}", terminalPhone, authCode);
        
        return Future.succeededFuture(response);
    }
    
    private Future<JT808Message> handleTerminalAuth(JT808Message message) {
        // 处理终端鉴权消息 (0x0102)
        if (!(message instanceof T0102TerminalAuth)) {
            logger.warn("消息类型不匹配，期望T0102TerminalAuth，实际: {}", message.getClass().getSimpleName());
            return Future.succeededFuture(null);
        }

        T0102TerminalAuth auth = (T0102TerminalAuth) message;
        String terminalPhone = message.getHeader().getPhoneNumber();
        String authCode = auth.getAuthCode();
        
        logger.info("收到终端[{}]鉴权请求: 鉴权码={}", terminalPhone, authCode);

        // 创建平台通用应答
        T8001PlatformCommonResponse response = new T8001PlatformCommonResponse();
        JT808Header responseHeader = new JT808Header(
            0x8001,
            terminalPhone,
            message.getHeader().getSerialNumber()
        );
        response.setHeader(responseHeader);
        response.setResponseSerialNumber(message.getHeader().getSerialNumber());
        response.setResponseMessageId(0x0102);
        
        // 简单的鉴权逻辑：验证鉴权码格式
        if (authCode != null && !authCode.trim().isEmpty()) {
            response.setResult(T8001PlatformCommonResponse.RESULT_SUCCESS);
            logger.info("终端[{}]鉴权成功", terminalPhone);
        } else {
            response.setResult(T8001PlatformCommonResponse.RESULT_FAILURE);
            logger.warn("终端[{}]鉴权失败: 鉴权码为空", terminalPhone);
        }
        
        return Future.succeededFuture(response);
    }
    
    private Future<JT808Message> handleQueryTerminalParametersResponse(JT808Message message) {
        // 处理查询终端参数应答消息 (0x0104)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("收到终端[{}]参数查询应答", terminalPhone);
        
        // 参数查询应答通常不需要回复
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleQueryTerminalPropertyResponse(JT808Message message) {
        // 处理查询终端属性应答消息 (0x0107)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("收到终端[{}]属性查询应答", terminalPhone);
        
        // 属性查询应答通常不需要回复
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalUpgradeResultNotification(JT808Message message) {
        // 处理终端升级结果通知消息 (0x0108)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("收到终端[{}]升级结果通知", terminalPhone);
        
        // 创建平台通用应答
        T8001PlatformCommonResponse response = new T8001PlatformCommonResponse();
        JT808Header responseHeader = new JT808Header(
            0x8001,
            terminalPhone,
            message.getHeader().getSerialNumber()
        );
        response.setHeader(responseHeader);
        response.setResponseSerialNumber(message.getHeader().getSerialNumber());
        response.setResponseMessageId(0x0108);
        response.setResult(T8001PlatformCommonResponse.RESULT_SUCCESS);
        
        return Future.succeededFuture(response);
    }
    
    private Future<JT808Message> handleLocationReport(JT808Message message) {
        // 处理位置信息汇报消息 (0x0200)
        if (!(message instanceof T0200LocationReport)) {
            logger.warn("消息类型不匹配，期望T0200LocationReport，实际: {}", message.getClass().getSimpleName());
            return Future.succeededFuture(null);
        }

        T0200LocationReport locationReport = (T0200LocationReport) message;
        String terminalPhone = message.getHeader().getPhoneNumber();
        
        // 记录位置信息
        logger.info("收到终端[{}]位置信息: 纬度={}, 经度={}, 速度={}km/h, 方向={}°, 时间={}",
                   terminalPhone,
                   locationReport.getLatitudeDegrees(),
                   locationReport.getLongitudeDegrees(),
                   locationReport.getSpeedKmh(),
                   locationReport.getDirection(),
                   locationReport.getDateTime());

        // 检查报警信息
        if (locationReport.getAlarmFlag() != 0) {
            logger.warn("终端[{}]报警: 报警标志=0x{}", 
                       terminalPhone, 
                       Integer.toHexString(locationReport.getAlarmFlag()).toUpperCase());
            
            // 记录具体的报警类型
            List<String> alarmDescriptions = locationReport.getActiveAlarmDescriptions();
            if (!alarmDescriptions.isEmpty()) {
                logger.warn("终端[{}]报警详情: {}", terminalPhone, String.join(", ", alarmDescriptions));
            }
        }

        // 创建平台通用应答
        T8001PlatformCommonResponse response = new T8001PlatformCommonResponse();
        JT808Header responseHeader = new JT808Header(
            0x8001,
            terminalPhone,
            message.getHeader().getSerialNumber()
        );
        response.setHeader(responseHeader);
        response.setResponseSerialNumber(message.getHeader().getSerialNumber());
        response.setResponseMessageId(0x0200);
        response.setResult(T8001PlatformCommonResponse.RESULT_SUCCESS);
        
        return Future.succeededFuture(response);
    }
    
    private Future<JT808Message> handlePositionInfoQueryResponse(JT808Message message) {
        // 处理位置信息查询应答消息 (0x0201)
        if (!(message instanceof T0200LocationReport)) {
            logger.warn("消息类型不匹配，期望T0200LocationReport，实际: {}", message.getClass().getSimpleName());
            return Future.succeededFuture(null);
        }

        T0200LocationReport locationReport = (T0200LocationReport) message;
        String terminalPhone = message.getHeader().getPhoneNumber();
        
        logger.info("收到终端[{}]位置查询应答: 纬度={}, 经度={}, 时间={}",
                   terminalPhone,
                   locationReport.getLatitudeDegrees(),
                   locationReport.getLongitudeDegrees(),
                   locationReport.getDateTime());
        
        // 位置查询应答通常不需要回复
        return Future.succeededFuture(null);
    }
    
    // 平台下行消息处理方法
    private Future<JT808Message> handlePlatformCommonResponse(JT808Message message) {
        // 处理平台通用应答消息 (0x8001)
        if (!(message instanceof T8001PlatformCommonResponse)) {
            logger.warn("消息类型不匹配，期望T8001PlatformCommonResponse，实际: {}", message.getClass().getSimpleName());
            return Future.succeededFuture(null);
        }

        T8001PlatformCommonResponse response = (T8001PlatformCommonResponse) message;
        String terminalPhone = message.getHeader().getPhoneNumber();
        
        logger.info("处理平台通用应答: 终端[{}], 应答流水号={}, 应答消息ID=0x{}, 结果={}",
                   terminalPhone,
                   response.getResponseSerialNumber(),
                   Integer.toHexString(response.getResponseMessageId()).toUpperCase(),
                   response.getResult());
        
        // 平台通用应答通常不需要回复
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleResendSubpackageRequest(JT808Message message) {
        // 处理补传分包请求消息 (0x8003)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("发送补传分包请求给终端[{}]", terminalPhone);
        
        // 补传分包请求是平台主动发送的，通常不需要特殊处理
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalRegisterResponse(JT808Message message) {
        // 处理终端注册应答消息 (0x8100)
        if (!(message instanceof T8100TerminalRegisterResponse)) {
            logger.warn("消息类型不匹配，期望T8100TerminalRegisterResponse，实际: {}", message.getClass().getSimpleName());
            return Future.succeededFuture(null);
        }

        T8100TerminalRegisterResponse response = (T8100TerminalRegisterResponse) message;
        String terminalPhone = message.getHeader().getPhoneNumber();
        
        logger.info("处理终端注册应答: 终端[{}], 结果={}, 鉴权码={}",
                   terminalPhone,
                   response.getResult(),
                   response.getAuthCode());
        
        // 终端注册应答是平台发送的，通常不需要回复
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalParameterSetting(JT808Message message) {
        // 处理设置终端参数消息 (0x8103)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("发送设置终端参数命令给终端[{}]", terminalPhone);
        
        // 设置终端参数是平台主动发送的命令，等待终端应答
        // 这里可以记录发送状态，等待终端的通用应答
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleQueryTerminalParameters(JT808Message message) {
        // 处理查询终端参数消息 (0x8104)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("发送查询终端参数命令给终端[{}]", terminalPhone);
        
        // 查询终端参数是平台主动发送的命令，等待终端应答
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalControl(JT808Message message) {
        // 处理终端控制消息 (0x8105)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("发送终端控制命令给终端[{}]", terminalPhone);
        
        // 终端控制是平台主动发送的命令，等待终端应答
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleQuerySpecificTerminalParameters(JT808Message message) {
        // 处理查询指定终端参数消息 (0x8106)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("发送查询指定终端参数命令给终端[{}]", terminalPhone);
        
        // 查询指定终端参数是平台主动发送的命令，等待终端应答
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleQueryTerminalProperty(JT808Message message) {
        // 处理查询终端属性消息 (0x8107)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("发送查询终端属性命令给终端[{}]", terminalPhone);
        
        // 查询终端属性是平台主动发送的命令，等待终端应答
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalUpgradePackage(JT808Message message) {
        // 处理下发终端升级包消息 (0x8108)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("发送终端升级包给终端[{}]", terminalPhone);
        
        // 下发终端升级包是平台主动发送的，等待终端升级结果通知
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handlePositionInfoQuery(JT808Message message) {
        // 处理位置信息查询消息 (0x8201)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("发送位置信息查询命令给终端[{}]", terminalPhone);
        
        // 位置信息查询是平台主动发送的命令，等待终端应答位置信息
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTemporaryLocationTrackingControl(JT808Message message) {
        // 处理临时位置跟踪控制消息 (0x8202)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("发送临时位置跟踪控制命令给终端[{}]", terminalPhone);
        
        // 临时位置跟踪控制是平台主动发送的命令，等待终端应答
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleManualAlarmConfirmation(JT808Message message) {
        // 处理人工确认报警消息 (0x8203)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("发送人工确认报警消息给终端[{}]", terminalPhone);
        
        // 人工确认报警消息是平台主动发送的，等待终端应答
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTextInfoDistribution(JT808Message message) {
        // 处理文本信息下发消息 (0x8300)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("发送文本信息下发给终端[{}]", terminalPhone);
        
        // 文本信息下发是平台主动发送的，等待终端应答
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleEventSetting(JT808Message message) {
        // 处理事件设置消息 (0x8301)
        String terminalPhone = message.getHeader().getPhoneNumber();
        logger.info("发送事件设置命令给终端[{}]", terminalPhone);
        
        // 事件设置是平台主动发送的命令，等待终端应答
        return Future.succeededFuture(null);
    }
    
    /**
     * 生成鉴权码
     * 
     * @param terminalPhone 终端手机号
     * @return 鉴权码
     */
    private String generateAuthCode(String terminalPhone) {
        // 简单的鉴权码生成逻辑：使用时间戳和终端号码
        long timestamp = System.currentTimeMillis();
        return "AUTH_" + terminalPhone + "_" + timestamp;
    }
}