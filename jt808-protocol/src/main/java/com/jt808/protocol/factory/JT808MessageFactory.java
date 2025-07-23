package com.jt808.protocol.factory;

import com.jt808.common.exception.ProtocolException;
import com.jt808.protocol.codec.JT808Decoder;
import com.jt808.protocol.codec.JT808Encoder;
import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.message.*;
import com.jt808.protocol.message.T0705CanBusDataUpload;
import com.jt808.protocol.message.T0801MultimediaDataUpload;
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
        initTerminalMessages();
        initPlatformMessages();
        initExtensionMessages();
    }

    /**
     * 初始化终端消息创建器 (0x0xxx)
     */
    private void initTerminalMessages() {
        messageCreators.put(MessageTypes.Terminal.COMMON_RESPONSE, T0001TerminalCommonResponse::new);
        messageCreators.put(MessageTypes.Terminal.HEARTBEAT, T0002TerminalHeartbeat::new);
        messageCreators.put(MessageTypes.Terminal.REGISTER, T0100TerminalRegister::new);
        messageCreators.put(MessageTypes.Terminal.AUTH, T0102TerminalAuth::new);
        messageCreators.put(MessageTypes.Terminal.QUERY_PARAMETERS_RESPONSE, T0104QueryTerminalParametersResponse::new);
        messageCreators.put(MessageTypes.Terminal.QUERY_PROPERTY_RESPONSE, T0107QueryTerminalPropertyResponse::new);
        messageCreators.put(MessageTypes.Terminal.UPGRADE_RESULT_NOTIFICATION, T0108TerminalUpgradeResultNotification::new);
        messageCreators.put(MessageTypes.Terminal.LOCATION_REPORT, T0200LocationReport::new);
        messageCreators.put(MessageTypes.Terminal.POSITION_INFO_QUERY_RESPONSE, T0201PositionInfoQueryResponse::new);
        messageCreators.put(MessageTypes.Terminal.EVENT_REPORT, T0301EventReport::new);
        messageCreators.put(MessageTypes.Terminal.INFO_DEMAND_CANCEL, T0303InfoDemandCancel::new);
        messageCreators.put(MessageTypes.Terminal.VEHICLE_CONTROL_RESPONSE, T0500VehicleControlResponse::new);
        messageCreators.put(MessageTypes.Terminal.DRIVING_RECORD_DATA_UPLOAD, T0700DrivingRecordDataUpload::new);
        messageCreators.put(MessageTypes.Terminal.ELECTRONIC_WAYBILL_REPORT, T0701ElectronicWaybillReport::new);
        messageCreators.put(MessageTypes.Terminal.DRIVER_IDENTITY_INFO_REPORT, T0702DriverIdentityInfoReport::new);
        messageCreators.put(MessageTypes.Terminal.LOCATION_DATA_BATCH_UPLOAD, T0704LocationDataBatchUpload::new);
        messageCreators.put(MessageTypes.Terminal.CAN_BUS_DATA_UPLOAD, T0705CanBusDataUpload::new);
        messageCreators.put(MessageTypes.Terminal.MULTIMEDIA_DATA_UPLOAD, T0801MultimediaDataUpload::new);
    }

    /**
     * 初始化平台消息创建器 (0x8xxx)
     */
    private void initPlatformMessages() {
        messageCreators.put(MessageTypes.Platform.COMMON_RESPONSE, T8001PlatformCommonResponse::new);
        messageCreators.put(MessageTypes.Platform.RESEND_SUBPACKAGE_REQUEST, T8003ResendSubpackageRequest::new);
        messageCreators.put(MessageTypes.Platform.REGISTER_RESPONSE, T8100TerminalRegisterResponse::new);
        messageCreators.put(MessageTypes.Platform.PARAMETER_SETTING, T8103TerminalParameterSetting::new);
        messageCreators.put(MessageTypes.Platform.QUERY_PARAMETERS, T8104QueryTerminalParameters::new);
        messageCreators.put(MessageTypes.Platform.TERMINAL_CONTROL, T8105TerminalControl::new);
        messageCreators.put(MessageTypes.Platform.QUERY_SPECIFIC_PARAMETERS, T8106QuerySpecificTerminalParameters::new);
        messageCreators.put(MessageTypes.Platform.QUERY_PROPERTY, T8107QueryTerminalProperty::new);
        messageCreators.put(MessageTypes.Platform.UPGRADE_PACKAGE, T8108TerminalUpgradePackage::new);
        messageCreators.put(MessageTypes.Platform.POSITION_INFO_QUERY, T8201PositionInfoQuery::new);
        messageCreators.put(MessageTypes.Platform.TEMPORARY_LOCATION_TRACKING_CONTROL, T8202TemporaryLocationTrackingControl::new);
        messageCreators.put(MessageTypes.Platform.MANUAL_ALARM_CONFIRMATION, T8203ManualAlarmConfirmation::new);
        messageCreators.put(MessageTypes.Platform.TEXT_INFO_DISTRIBUTION, T8300TextInfoDistribution::new);
        messageCreators.put(MessageTypes.Platform.EVENT_SETTING, T8301EventSetting::new);
        messageCreators.put(MessageTypes.Platform.QUESTION_DISTRIBUTION, T8302QuestionDistribution::new);
        messageCreators.put(MessageTypes.Platform.INFO_MENU_SETTING, T8303InfoMenuSetting::new);
        messageCreators.put(MessageTypes.Platform.INFO_SERVICE, T8304InfoService::new);
        messageCreators.put(MessageTypes.Platform.PHONE_CALLBACK, T8400PhoneCallback::new);
        messageCreators.put(MessageTypes.Platform.PHONEBOOK_SETTING, T8401PhonebookSetting::new);
        messageCreators.put(MessageTypes.Platform.VEHICLE_CONTROL, T8500VehicleControl::new);
        messageCreators.put(MessageTypes.Platform.SET_CIRCULAR_AREA, T8600SetCircularArea::new);
        messageCreators.put(MessageTypes.Platform.DELETE_CIRCULAR_AREA, T8601DeleteCircularArea::new);
        messageCreators.put(MessageTypes.Platform.SET_RECTANGULAR_AREA, T8602SetRectangularArea::new);
        messageCreators.put(MessageTypes.Platform.DELETE_RECTANGULAR_AREA, T8603DeleteRectangularArea::new);
        messageCreators.put(MessageTypes.Platform.SET_POLYGON_AREA, T8604SetPolygonArea::new);
        messageCreators.put(MessageTypes.Platform.DELETE_POLYGON_AREA, T8605DeletePolygonArea::new);
        messageCreators.put(MessageTypes.Platform.SET_ROUTE, T8606SetRoute::new);
        messageCreators.put(MessageTypes.Platform.DELETE_ROUTE, T8607DeleteRoute::new);
        messageCreators.put(MessageTypes.Platform.DRIVING_RECORD_DATA_COLLECTION, T8700DrivingRecordDataCollection::new);
        messageCreators.put(MessageTypes.Platform.DRIVING_RECORD_PARAMETER_TRANSMISSION, T8701DrivingRecordParameterTransmission::new);
        messageCreators.put(MessageTypes.Platform.DRIVER_IDENTITY_INFO_REQUEST, T8702DriverIdentityInfoRequest::new);
    }

    /**
     * 初始化扩展消息创建器
     * 用于厂商自定义或协议扩展消息
     */
    private void initExtensionMessages() {
        // 预留给扩展消息使用
        // 示例：messageCreators.put(MessageTypes.Extension.CUSTOM_MESSAGE, CustomMessage::new);
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