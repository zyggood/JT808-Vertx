package com.jt808.protocol.validator.impl;

import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.validator.MessageValidator;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础消息验证器
 * 验证JT808消息的基本格式和必要字段
 */
public class BasicMessageValidator implements MessageValidator {

    private static final Logger logger = LoggerFactory.getLogger(BasicMessageValidator.class);

    private final boolean strict;

    public BasicMessageValidator() {
        this(true);
    }

    public BasicMessageValidator(boolean strict) {
        this.strict = strict;
    }

    @Override
    public Future<ValidationResult> validate(JT808Message message) {
        List<ValidationError> errors = new ArrayList<>();
        List<ValidationWarning> warnings = new ArrayList<>();

        try {
            // 验证消息不为空
            if (message == null) {
                errors.add(new ValidationError("NULL_MESSAGE", "Message cannot be null"));
                return Future.succeededFuture(ValidationResult.failure(getName(), errors));
            }

            // 验证消息头
            validateHeader(message.getHeader(), errors, warnings);

            // 验证消息体长度
            validateBodyLength(message, errors, warnings);

            // 验证消息ID
            validateMessageId(message.getHeader(), errors, warnings);

            // 验证终端手机号
            validateTerminalPhone(message.getHeader(), errors, warnings);

            // 验证流水号
            validateSerialNumber(message.getHeader(), errors, warnings);

            boolean isValid = errors.isEmpty();

            if (isValid) {
                logger.debug("Message validation passed for message ID: 0x{}",
                        Integer.toHexString(message.getHeader().getMessageId()).toUpperCase());
                return Future.succeededFuture(ValidationResult.success(getName(), warnings));
            } else {
                logger.warn("Message validation failed with {} errors for message ID: 0x{}",
                        errors.size(), Integer.toHexString(message.getHeader().getMessageId()).toUpperCase());
                return Future.succeededFuture(ValidationResult.failure(getName(), errors));
            }

        } catch (Exception e) {
            logger.error("Exception during message validation", e);
            errors.add(new ValidationError("VALIDATION_EXCEPTION", "Validation failed with exception: " + e.getMessage()));
            return Future.succeededFuture(ValidationResult.failure(getName(), errors));
        }
    }

    /**
     * 验证消息头
     */
    private void validateHeader(JT808Header header, List<ValidationError> errors, List<ValidationWarning> warnings) {
        if (header == null) {
            errors.add(new ValidationError("NULL_HEADER", "Message header cannot be null"));
            return;
        }

        // 验证消息属性
        if (header.getMessageProperty() < 0) {
            errors.add(new ValidationError("INVALID_MESSAGE_PROPERTIES",
                    "Message properties cannot be negative", "messageProperties", header.getMessageProperty()));
        }

        // 验证消息体长度属性
        int bodyLength = header.getMessageProperty() & 0x3FF; // 低10位为消息体长度
    }

    /**
     * 验证消息体长度
     */
    private void validateBodyLength(JT808Message message, List<ValidationError> errors, List<ValidationWarning> warnings) {
        JT808Header header = message.getHeader();
        if (header == null) return;

        int declaredLength = header.getMessageProperty() & 0x3FF;

        // 如果消息有编码后的数据，验证长度一致性
        try {
            byte[] encodedBody = message.encodeBody().getBytes();
            if (encodedBody != null && encodedBody.length != declaredLength) {
                if (strict) {
                    errors.add(new ValidationError("BODY_LENGTH_MISMATCH",
                            String.format("Declared body length (%d) does not match actual length (%d)",
                                    declaredLength, encodedBody.length),
                            "bodyLength", declaredLength));
                } else {
                    warnings.add(new ValidationWarning("BODY_LENGTH_MISMATCH",
                            String.format("Declared body length (%d) does not match actual length (%d)",
                                    declaredLength, encodedBody.length),
                            "bodyLength", declaredLength));
                }
            }
        } catch (Exception e) {
            warnings.add(new ValidationWarning("BODY_ENCODING_FAILED",
                    "Could not encode message body for length validation: " + e.getMessage()));
        }
    }

    /**
     * 验证消息ID
     */
    private void validateMessageId(JT808Header header, List<ValidationError> errors, List<ValidationWarning> warnings) {
        if (header == null) return;

        int messageId = header.getMessageId();

        // 验证消息ID范围（JT808协议定义的有效范围）
        if (messageId <= 0 || messageId > 0xFFFF) {
            errors.add(new ValidationError("INVALID_MESSAGE_ID",
                    "Message ID must be between 0x0001 and 0xFFFF", "messageId", messageId));
        }

        // 检查是否为已知的消息类型
        if (!isKnownMessageId(messageId)) {
            warnings.add(new ValidationWarning("UNKNOWN_MESSAGE_ID",
                    "Message ID is not in the list of known message types", "messageId", messageId));
        }
    }

    /**
     * 验证终端手机号
     */
    private void validateTerminalPhone(JT808Header header, List<ValidationError> errors, List<ValidationWarning> warnings) {
        if (header == null) return;

        String terminalPhone = header.getPhoneNumber();

        if (terminalPhone == null || terminalPhone.trim().isEmpty()) {
            errors.add(new ValidationError("EMPTY_TERMINAL_PHONE",
                    "Terminal phone number cannot be null or empty", "phoneNumber", terminalPhone));
            return;
        }

        // 验证手机号格式（应该是数字）
        if (!terminalPhone.matches("\\d+")) {
            errors.add(new ValidationError("INVALID_TERMINAL_PHONE_FORMAT",
                    "Terminal phone number should contain only digits", "terminalPhone", terminalPhone));
        }

        // 验证手机号长度（通常为11位或12位）
        if (terminalPhone.length() < 10 || terminalPhone.length() > 12) {
            warnings.add(new ValidationWarning("UNUSUAL_TERMINAL_PHONE_LENGTH",
                    "Terminal phone number length is unusual (expected 10-12 digits)",
                    "terminalPhone", terminalPhone));
        }
    }

    /**
     * 验证流水号
     */
    private void validateSerialNumber(JT808Header header, List<ValidationError> errors, List<ValidationWarning> warnings) {
        if (header == null) return;

        int serialNumber = header.getSerialNumber();

        // 验证流水号范围
        if (serialNumber < 0 || serialNumber > 0xFFFF) {
            errors.add(new ValidationError("INVALID_SERIAL_NUMBER",
                    "Serial number must be between 0 and 65535", "serialNumber", serialNumber));
        }
    }

    /**
     * 检查是否为已知的消息ID
     */
    private boolean isKnownMessageId(int messageId) {
        // 常见的JT808消息ID
        switch (messageId) {
            case 0x0001: // 终端通用应答
            case 0x0002: // 终端心跳
            case 0x0100: // 终端注册
            case 0x0102: // 终端鉴权
            case 0x0200: // 位置信息汇报
            case 0x0201: // 位置信息查询应答
            case 0x0301: // 事件报告
            case 0x0302: // 提问应答
            case 0x0303: // 信息点播/取消
            case 0x0704: // 定位数据批量上传
            case 0x0705: // CAN总线数据上传
            case 0x0800: // 多媒体事件信息上传
            case 0x0801: // 多媒体数据上传
            case 0x0802: // 存储多媒体数据检索应答
            case 0x0805: // 摄像头立即拍摄命令应答
            case 0x8001: // 平台通用应答
            case 0x8100: // 终端注册应答
            case 0x8103: // 设置终端参数
            case 0x8104: // 查询终端参数
            case 0x8105: // 终端控制
            case 0x8106: // 查询指定终端参数
            case 0x8201: // 位置信息查询
            case 0x8202: // 临时位置跟踪控制
            case 0x8300: // 文本信息下发
            case 0x8301: // 事件设置
            case 0x8302: // 提问下发
            case 0x8303: // 信息点播菜单设置
            case 0x8304: // 信息服务
            case 0x8400: // 电话回拨
            case 0x8401: // 设置电话本
            case 0x8500: // 车辆控制
            case 0x8600: // 设置圆形区域
            case 0x8601: // 删除圆形区域
            case 0x8602: // 设置矩形区域
            case 0x8603: // 删除矩形区域
            case 0x8604: // 设置多边形区域
            case 0x8605: // 删除多边形区域
            case 0x8606: // 设置路线
            case 0x8607: // 删除路线
            case 0x8700: // 行驶记录仪数据采集命令
            case 0x8701: // 行驶记录仪参数下传命令
            case 0x8702: // 上报驾驶员身份信息请求
            case 0x8800: // 多媒体数据上传应答
            case 0x8801: // 摄像头立即拍摄命令
            case 0x8802: // 存储多媒体数据检索
            case 0x8803: // 存储多媒体数据上传
            case 0x8804: // 录音开始命令
            case 0x8805: // 单条存储多媒体数据检索上传命令
                return true;
            default:
                return false;
        }
    }

    @Override
    public String getName() {
        return "BasicMessageValidator";
    }

    @Override
    public int getPriority() {
        return 100; // 基础验证器优先级较高
    }

    @Override
    public boolean canValidate(JT808Message message) {
        return message != null; // 可以验证所有消息
    }

    @Override
    public boolean isStrict() {
        return strict;
    }
}