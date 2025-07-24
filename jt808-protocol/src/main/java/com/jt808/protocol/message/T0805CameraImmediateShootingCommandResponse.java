package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * T0805 摄像头立即拍摄命令应答消息 (0x0805)
 * 终端应答监控中心下发的摄像头立即拍摄命令0x8801
 * 
 * 消息体数据格式：
 * - 应答流水号 (2字节): 对应平台摄像头立即拍摄命令的消息流水号
 * - 结果 (1字节): 0=成功, 1=失败, 2=通道不支持
 * - 多媒体ID个数 (2字节): n，拍摄成功的多媒体个数（结果=0时才有效）
 * - 多媒体ID列表 (4*n字节): 多媒体ID列表（结果=0时才有效）
 * 
 * @author JT808 Protocol
 * @version 1.0
 */
public class T0805CameraImmediateShootingCommandResponse extends JT808Message {

    /**
     * 应答流水号 (WORD)
     * 对应平台摄像头立即拍摄命令的消息流水号
     */
    private int responseSerialNumber;

    /**
     * 结果 (BYTE)
     * 0：成功；1：失败；2：通道不支持
     */
    private int result;

    /**
     * 多媒体ID个数 (WORD)
     * n，拍摄成功的多媒体个数（结果=0时才有效）
     */
    private int multimediaIdCount;

    /**
     * 多媒体ID列表 (BYTE[4*n])
     * 多媒体ID列表（结果=0时才有效）
     */
    private List<Long> multimediaIds;

    /**
     * 结果枚举
     */
    public enum Result {
        SUCCESS(0, "成功"),
        FAILURE(1, "失败"),
        CHANNEL_NOT_SUPPORTED(2, "通道不支持");

        private final int value;
        private final String description;

        Result(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static Result fromValue(int value) {
            for (Result result : values()) {
                if (result.value == value) {
                    return result;
                }
            }
            return null;
        }
    }

    /**
     * 默认构造函数
     */
    public T0805CameraImmediateShootingCommandResponse() {
        super();
        this.multimediaIds = new ArrayList<>();
    }

    /**
     * 带消息头的构造函数
     * 
     * @param header 消息头
     */
    public T0805CameraImmediateShootingCommandResponse(JT808Header header) {
        super(header);
        this.multimediaIds = new ArrayList<>();
    }

    @Override
    public int getMessageId() {
        return 0x0805;
    }

    /**
     * 创建成功应答
     * 
     * @param responseSerialNumber 应答流水号
     * @param multimediaIds 多媒体ID列表
     * @return 应答消息实例
     */
    public static T0805CameraImmediateShootingCommandResponse createSuccessResponse(
            int responseSerialNumber, List<Long> multimediaIds) {
        T0805CameraImmediateShootingCommandResponse response = 
                new T0805CameraImmediateShootingCommandResponse();
        response.setResponseSerialNumber(responseSerialNumber);
        response.setResult(Result.SUCCESS.getValue());
        response.setMultimediaIds(multimediaIds != null ? multimediaIds : new ArrayList<>());
        response.setMultimediaIdCount(response.getMultimediaIds().size());
        return response;
    }

    /**
     * 创建成功应答（可变参数）
     * 
     * @param responseSerialNumber 应答流水号
     * @param multimediaIds 多媒体ID数组
     * @return 应答消息实例
     */
    public static T0805CameraImmediateShootingCommandResponse createSuccessResponse(
            int responseSerialNumber, long... multimediaIds) {
        List<Long> idList = new ArrayList<>();
        for (long id : multimediaIds) {
            idList.add(id);
        }
        return createSuccessResponse(responseSerialNumber, idList);
    }

    /**
     * 创建失败应答
     * 
     * @param responseSerialNumber 应答流水号
     * @param result 失败结果
     * @return 应答消息实例
     */
    public static T0805CameraImmediateShootingCommandResponse createFailureResponse(
            int responseSerialNumber, Result result) {
        if (result == Result.SUCCESS) {
            throw new IllegalArgumentException("失败应答不能使用SUCCESS结果");
        }
        T0805CameraImmediateShootingCommandResponse response = 
                new T0805CameraImmediateShootingCommandResponse();
        response.setResponseSerialNumber(responseSerialNumber);
        response.setResult(result.getValue());
        response.setMultimediaIdCount(0);
        response.setMultimediaIds(new ArrayList<>());
        return response;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 应答流水号 (2字节)
        buffer.appendUnsignedShort((short) responseSerialNumber);
        
        // 结果 (1字节)
        buffer.appendUnsignedByte((short) result);
        
        // 如果结果为成功，才编码后续字段
        if (result == Result.SUCCESS.getValue()) {
            // 多媒体ID个数 (2字节)
            buffer.appendUnsignedShort((short) multimediaIdCount);
            
            // 多媒体ID列表 (4*n字节)
            for (Long multimediaId : multimediaIds) {
                buffer.appendUnsignedInt(multimediaId);
            }
        }
        
        return buffer;
    }

    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer.length() < 3) {
            throw new IllegalArgumentException("消息体长度不足");
        }
        
        int index = 0;
        
        // 应答流水号 (2字节)
        this.responseSerialNumber = buffer.getUnsignedShort(index);
        index += 2;
        
        // 结果 (1字节)
        this.result = buffer.getUnsignedByte(index);
        index += 1;
        
        // 如果结果为成功，解码后续字段
        if (this.result == Result.SUCCESS.getValue()) {
            if (buffer.length() < index + 2) {
                throw new IllegalArgumentException("成功应答消息体长度不足");
            }
            
            // 多媒体ID个数 (2字节)
            this.multimediaIdCount = buffer.getUnsignedShort(index);
            index += 2;
            
            // 验证剩余长度
            int expectedLength = this.multimediaIdCount * 4;
            if (buffer.length() < index + expectedLength) {
                throw new IllegalArgumentException("多媒体ID列表长度不足");
            }
            
            // 多媒体ID列表 (4*n字节)
            this.multimediaIds = new ArrayList<>();
            for (int i = 0; i < this.multimediaIdCount; i++) {
                long multimediaId = buffer.getUnsignedInt(index);
                this.multimediaIds.add(multimediaId);
                index += 4;
            }
        } else {
            // 失败时清空多媒体ID相关字段
            this.multimediaIdCount = 0;
            this.multimediaIds = new ArrayList<>();
        }
    }

    /**
     * 是否成功
     * 
     * @return true=成功, false=失败
     */
    public boolean isSuccess() {
        return result == Result.SUCCESS.getValue();
    }

    /**
     * 是否失败
     * 
     * @return true=失败, false=成功
     */
    public boolean isFailure() {
        return result != Result.SUCCESS.getValue();
    }

    /**
     * 获取结果枚举
     * 
     * @return 结果枚举
     */
    public Result getResultEnum() {
        return Result.fromValue(result);
    }

    /**
     * 获取结果描述
     * 
     * @return 结果描述
     */
    public String getResultDescription() {
        Result resultEnum = getResultEnum();
        return resultEnum != null ? resultEnum.getDescription() : "未知结果(" + result + ")";
    }

    /**
     * 添加多媒体ID
     * 
     * @param multimediaId 多媒体ID
     */
    public void addMultimediaId(long multimediaId) {
        if (this.multimediaIds == null) {
            this.multimediaIds = new ArrayList<>();
        }
        this.multimediaIds.add(multimediaId);
        this.multimediaIdCount = this.multimediaIds.size();
    }

    /**
     * 清空多媒体ID列表
     */
    public void clearMultimediaIds() {
        if (this.multimediaIds != null) {
            this.multimediaIds.clear();
        }
        this.multimediaIdCount = 0;
    }

    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getMessageDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("摄像头立即拍摄命令应答: ");
        sb.append("应答流水号=").append(responseSerialNumber);
        sb.append(", 结果=").append(getResultDescription());
        if (isSuccess()) {
            sb.append(", 多媒体ID个数=").append(multimediaIdCount);
            if (multimediaIdCount > 0) {
                sb.append(", 多媒体ID列表=").append(multimediaIds);
            }
        }
        return sb.toString();
    }

    // Getter和Setter方法
    public int getResponseSerialNumber() {
        return responseSerialNumber;
    }

    public void setResponseSerialNumber(int responseSerialNumber) {
        this.responseSerialNumber = responseSerialNumber;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getMultimediaIdCount() {
        return multimediaIdCount;
    }

    public void setMultimediaIdCount(int multimediaIdCount) {
        this.multimediaIdCount = multimediaIdCount;
    }

    public List<Long> getMultimediaIds() {
        return multimediaIds;
    }

    public void setMultimediaIds(List<Long> multimediaIds) {
        this.multimediaIds = multimediaIds != null ? multimediaIds : new ArrayList<>();
        this.multimediaIdCount = this.multimediaIds.size();
    }

    @Override
    public String toString() {
        return "T0805CameraImmediateShootingCommandResponse{" +
                "responseSerialNumber=" + responseSerialNumber +
                ", result=" + result + "(" + getResultDescription() + ")" +
                ", multimediaIdCount=" + multimediaIdCount +
                ", multimediaIds=" + multimediaIds +
                ", messageId=0x" + Integer.toHexString(getMessageId()).toUpperCase() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T0805CameraImmediateShootingCommandResponse that = (T0805CameraImmediateShootingCommandResponse) o;
        return responseSerialNumber == that.responseSerialNumber &&
                result == that.result &&
                multimediaIdCount == that.multimediaIdCount &&
                Objects.equals(multimediaIds, that.multimediaIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseSerialNumber, result, multimediaIdCount, multimediaIds);
    }
}