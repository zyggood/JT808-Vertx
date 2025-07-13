package com.jt808.protocol.factory;

import com.jt808.common.exception.ProtocolException;
import com.jt808.protocol.codec.JT808Decoder;
import com.jt808.protocol.codec.JT808Encoder;
import com.jt808.protocol.message.*;
import io.vertx.core.buffer.Buffer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * JT808消息工厂类
 * 负责统一管理消息的创建和解析
 */
public class JT808MessageFactory {

    private static final JT808MessageFactory INSTANCE = new JT808MessageFactory();
    private final Map<Integer, Supplier<JT808Message>> messageCreators;
    private final JT808Decoder decoder;
    private final JT808Encoder encoder;

    private JT808MessageFactory() {
        this.messageCreators = new HashMap<>();
        this.decoder = new JT808Decoder();
        this.encoder = new JT808Encoder();
        initMessageCreators();
    }

    /**
     * 获取工厂实例
     */
    public static JT808MessageFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化消息创建器映射
     */
    private void initMessageCreators() {
        // 终端消息
        messageCreators.put(0x0001, T0001TerminalCommonResponse::new);
        messageCreators.put(0x0002, T0002TerminalHeartbeat::new);
        messageCreators.put(0x0100, T0100TerminalRegister::new);
        messageCreators.put(0x0102, T0102TerminalAuth::new);
        messageCreators.put(0x0104, T0104QueryTerminalParametersResponse::new);
        messageCreators.put(0x0107, T0107QueryTerminalPropertyResponse::new);
        messageCreators.put(0x0108, T0108TerminalUpgradeResultNotification::new);
        messageCreators.put(0x0200, T0200LocationReport::new);
        messageCreators.put(0x0201, T0201PositionInfoQueryResponse::new);
        messageCreators.put(0x0301, T0301EventReport::new);
        messageCreators.put(0x8201, T8201PositionInfoQuery::new);
        messageCreators.put(0x8202, T8202TemporaryLocationTrackingControl::new);

        // 平台消息
        messageCreators.put(0x8001, T8001PlatformCommonResponse::new);
        messageCreators.put(0x8003, T8003ResendSubpackageRequest::new);
        messageCreators.put(0x8100, T8100TerminalRegisterResponse::new);
        messageCreators.put(0x8103, T8103TerminalParameterSetting::new);
        messageCreators.put(0x8104, T8104QueryTerminalParameters::new);
        messageCreators.put(0x8105, T8105TerminalControl::new);
        messageCreators.put(0x8106, T8106QuerySpecificTerminalParameters::new);
        messageCreators.put(0x8107, T8107QueryTerminalProperty::new);
        messageCreators.put(0x8108, T8108TerminalUpgradePackage::new);
        messageCreators.put(0x8203, T8203ManualAlarmConfirmation::new);
        messageCreators.put(0x8300, T8300TextInfoDistribution::new);
        messageCreators.put(0x8301, T8301EventSetting::new);
    }

    /**
     * 根据消息ID创建消息实例
     *
     * @param messageId 消息ID
     * @return 消息实例
     */
    public JT808Message createMessage(int messageId) {
        Supplier<JT808Message> creator = messageCreators.get(messageId);
        if (creator != null) {
            return creator.get();
        }

        // 未知消息类型，使用通用消息
        return new GenericJT808Message(messageId);
    }

    /**
     * 解析消息
     *
     * @param buffer 原始数据
     * @return 解析后的消息
     * @throws ProtocolException 协议异常
     */
    public JT808Message parseMessage(Buffer buffer) throws ProtocolException {
        return decoder.decode(buffer);
    }

    /**
     * 编码消息
     *
     * @param message 消息对象
     * @return 编码后的数据
     */
    public Buffer encodeMessage(JT808Message message) {
        return encoder.encode(message);
    }

    /**
     * 注册自定义消息类型
     *
     * @param messageId 消息ID
     * @param creator   消息创建器
     */
    public void registerMessage(int messageId, Supplier<JT808Message> creator) {
        messageCreators.put(messageId, creator);
    }

    /**
     * 检查是否支持指定的消息类型
     *
     * @param messageId 消息ID
     * @return 是否支持
     */
    public boolean isSupported(int messageId) {
        return messageCreators.containsKey(messageId);
    }

    /**
     * 获取所有支持的消息ID
     *
     * @return 消息ID集合
     */
    public java.util.Set<Integer> getSupportedMessageIds() {
        return messageCreators.keySet();
    }

    /**
     * 通用JT808消息实现
     * 用于处理未知或未实现的消息类型
     */
    public static class GenericJT808Message extends JT808Message {
        private final int messageId;
        private Buffer bodyData;

        public GenericJT808Message(int messageId) {
            this.messageId = messageId;
        }

        @Override
        public int getMessageId() {
            return messageId;
        }

        @Override
        public Buffer encodeBody() {
            return bodyData != null ? bodyData : Buffer.buffer();
        }

        @Override
        public void decodeBody(Buffer body) {
            this.bodyData = body;
        }

        /**
         * 获取原始消息体数据
         *
         * @return 消息体数据
         */
        public Buffer getBodyData() {
            return bodyData;
        }

        /**
         * 设置消息体数据
         *
         * @param bodyData 消息体数据
         */
        public void setBodyData(Buffer bodyData) {
            this.bodyData = bodyData;
        }
    }
}