package com.jt808.protocol.processor.impl;

import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.processor.MessageProcessor;
import com.jt808.protocol.processor.ProcessContext;
import com.jt808.protocol.processor.ProcessResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        logger.debug("Processing terminal common response: {}", message);
        // TODO: 实现终端通用应答逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalHeartbeat(JT808Message message) {
        logger.debug("Processing terminal heartbeat: {}", message);
        // TODO: 实现终端心跳逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalRegister(JT808Message message) {
        logger.debug("Processing terminal register: {}", message);
        // TODO: 实现终端注册逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalAuth(JT808Message message) {
        logger.debug("Processing terminal auth: {}", message);
        // TODO: 实现终端鉴权逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleQueryTerminalParametersResponse(JT808Message message) {
        logger.debug("Processing query terminal parameters response: {}", message);
        // TODO: 实现查询终端参数应答逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleQueryTerminalPropertyResponse(JT808Message message) {
        logger.debug("Processing query terminal property response: {}", message);
        // TODO: 实现查询终端属性应答逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalUpgradeResultNotification(JT808Message message) {
        logger.debug("Processing terminal upgrade result notification: {}", message);
        // TODO: 实现终端升级结果通知逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleLocationReport(JT808Message message) {
        logger.debug("Processing location report: {}", message);
        // TODO: 实现位置信息汇报逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handlePositionInfoQueryResponse(JT808Message message) {
        logger.debug("Processing position info query response: {}", message);
        // TODO: 实现位置信息查询应答逻辑
        return Future.succeededFuture(null);
    }
    
    // 平台下行消息处理方法
    private Future<JT808Message> handlePlatformCommonResponse(JT808Message message) {
        logger.debug("Processing platform common response: {}", message);
        // TODO: 实现平台通用应答逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleResendSubpackageRequest(JT808Message message) {
        logger.debug("Processing resend subpackage request: {}", message);
        // TODO: 实现补传分包请求逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalRegisterResponse(JT808Message message) {
        logger.debug("Processing terminal register response: {}", message);
        // TODO: 实现终端注册应答逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalParameterSetting(JT808Message message) {
        logger.debug("Processing terminal parameter setting: {}", message);
        // TODO: 实现设置终端参数逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleQueryTerminalParameters(JT808Message message) {
        logger.debug("Processing query terminal parameters: {}", message);
        // TODO: 实现查询终端参数逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalControl(JT808Message message) {
        logger.debug("Processing terminal control: {}", message);
        // TODO: 实现终端控制逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleQuerySpecificTerminalParameters(JT808Message message) {
        logger.debug("Processing query specific terminal parameters: {}", message);
        // TODO: 实现查询指定终端参数逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleQueryTerminalProperty(JT808Message message) {
        logger.debug("Processing query terminal property: {}", message);
        // TODO: 实现查询终端属性逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTerminalUpgradePackage(JT808Message message) {
        logger.debug("Processing terminal upgrade package: {}", message);
        // TODO: 实现下发终端升级包逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handlePositionInfoQuery(JT808Message message) {
        logger.debug("Processing position info query: {}", message);
        // TODO: 实现位置信息查询逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTemporaryLocationTrackingControl(JT808Message message) {
        logger.debug("Processing temporary location tracking control: {}", message);
        // TODO: 实现临时位置跟踪控制逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleManualAlarmConfirmation(JT808Message message) {
        logger.debug("Processing manual alarm confirmation: {}", message);
        // TODO: 实现人工确认报警消息逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleTextInfoDistribution(JT808Message message) {
        logger.debug("Processing text info distribution: {}", message);
        // TODO: 实现文本信息下发逻辑
        return Future.succeededFuture(null);
    }
    
    private Future<JT808Message> handleEventSetting(JT808Message message) {
        logger.debug("Processing event setting: {}", message);
        // TODO: 实现事件设置逻辑
        return Future.succeededFuture(null);
    }
}