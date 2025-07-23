package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 多媒体数据上传应答消息 (0x8801)
 * 平台向终端应答多媒体数据上传，指示需要重传的数据包
 */
public class T8801MultimediaDataUploadResponse extends JT808Message {

    /**
     * 多媒体ID (DWORD)
     * >0，如收到全部数据包则没有后续字段
     */
    private long multimediaId;

    /**
     * 重传包总数 (BYTE)
     * n
     */
    private int retransmissionPacketCount;

    /**
     * 重传包ID列表 (BYTE[2*n])
     * 重传包序号顺序排列，如"包ID1 包ID2...... 包IDn"
     */
    private List<Integer> retransmissionPacketIds;

    public T8801MultimediaDataUploadResponse() {
        super();
        this.retransmissionPacketIds = new ArrayList<>();
    }

    public T8801MultimediaDataUploadResponse(JT808Header header) {
        super(header);
        this.retransmissionPacketIds = new ArrayList<>();
    }

    /**
     * 创建收到全部数据包的应答（无需重传）
     * 
     * @param multimediaId 多媒体ID
     * @return 应答消息实例
     */
    public static T8801MultimediaDataUploadResponse createCompleteResponse(long multimediaId) {
        T8801MultimediaDataUploadResponse response = new T8801MultimediaDataUploadResponse();
        response.setMultimediaId(multimediaId);
        response.setRetransmissionPacketCount(0);
        response.setRetransmissionPacketIds(new ArrayList<>());
        return response;
    }

    /**
     * 创建需要重传的应答
     * 
     * @param multimediaId 多媒体ID
     * @param retransmissionPacketIds 需要重传的包ID列表
     * @return 应答消息实例
     */
    public static T8801MultimediaDataUploadResponse createRetransmissionResponse(long multimediaId, List<Integer> retransmissionPacketIds) {
        T8801MultimediaDataUploadResponse response = new T8801MultimediaDataUploadResponse();
        response.setMultimediaId(multimediaId);
        response.setRetransmissionPacketCount(retransmissionPacketIds.size());
        response.setRetransmissionPacketIds(new ArrayList<>(retransmissionPacketIds));
        return response;
    }

    /**
     * 创建需要重传的应答
     * 
     * @param multimediaId 多媒体ID
     * @param retransmissionPacketIds 需要重传的包ID数组
     * @return 应答消息实例
     */
    public static T8801MultimediaDataUploadResponse createRetransmissionResponse(long multimediaId, int... retransmissionPacketIds) {
        List<Integer> packetIds = new ArrayList<>();
        for (int packetId : retransmissionPacketIds) {
            packetIds.add(packetId);
        }
        return createRetransmissionResponse(multimediaId, packetIds);
    }

    @Override
    public int getMessageId() {
        return MessageTypes.Platform.MULTIMEDIA_DATA_UPLOAD_RESPONSE;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 多媒体ID (4字节)
        buffer.appendUnsignedInt(multimediaId);

        // 如果没有重传包，则消息体只包含多媒体ID
        if (retransmissionPacketCount == 0 || retransmissionPacketIds.isEmpty()) {
            return buffer;
        }

        // 重传包总数 (1字节)
        buffer.appendUnsignedByte((short) retransmissionPacketCount);

        // 重传包ID列表 (每个包ID占2字节)
        for (Integer packetId : retransmissionPacketIds) {
            buffer.appendUnsignedShort(packetId);
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body.length() < 4) {
            throw new IllegalArgumentException("消息体长度不足，至少需要4字节（多媒体ID）");
        }

        int index = 0;

        // 多媒体ID (4字节)
        this.multimediaId = body.getUnsignedInt(index);
        index += 4;

        // 如果消息体只有4字节，表示收到全部数据包
        if (body.length() == 4) {
            this.retransmissionPacketCount = 0;
            this.retransmissionPacketIds = new ArrayList<>();
            return;
        }

        // 重传包总数 (1字节)
        if (index >= body.length()) {
            throw new IllegalArgumentException("消息体长度不足，缺少重传包总数字段");
        }
        this.retransmissionPacketCount = body.getUnsignedByte(index);
        index += 1;

        // 验证消息体长度
        int expectedLength = 4 + 1 + (retransmissionPacketCount * 2);
        if (body.length() != expectedLength) {
            throw new IllegalArgumentException(
                String.format("消息体长度不匹配，期望%d字节，实际%d字节", expectedLength, body.length())
            );
        }

        // 重传包ID列表
        this.retransmissionPacketIds = new ArrayList<>();
        for (int i = 0; i < retransmissionPacketCount; i++) {
            if (index + 1 >= body.length()) {
                throw new IllegalArgumentException(
                    String.format("消息体长度不足，缺少第%d个重传包ID", i + 1)
                );
            }
            int packetId = body.getUnsignedShort(index);
            this.retransmissionPacketIds.add(packetId);
            index += 2;
        }
    }

    /**
     * 检查是否需要重传
     * 
     * @return true表示需要重传，false表示收到全部数据包
     */
    public boolean needsRetransmission() {
        return retransmissionPacketCount > 0 && !retransmissionPacketIds.isEmpty();
    }

    /**
     * 获取多媒体ID的无符号值
     * 
     * @return 多媒体ID的无符号值
     */
    public long getMultimediaIdUnsigned() {
        return multimediaId & 0xFFFFFFFFL;
    }

    /**
     * 获取重传包总数的无符号值
     * 
     * @return 重传包总数的无符号值
     */
    public int getRetransmissionPacketCountUnsigned() {
        return retransmissionPacketCount & 0xFF;
    }

    /**
     * 添加重传包ID
     * 
     * @param packetId 包ID
     */
    public void addRetransmissionPacketId(int packetId) {
        if (retransmissionPacketIds == null) {
            retransmissionPacketIds = new ArrayList<>();
        }
        retransmissionPacketIds.add(packetId);
        retransmissionPacketCount = retransmissionPacketIds.size();
    }

    /**
     * 清空重传包ID列表
     */
    public void clearRetransmissionPacketIds() {
        if (retransmissionPacketIds != null) {
            retransmissionPacketIds.clear();
        }
        retransmissionPacketCount = 0;
    }

    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getMessageDescription() {
        if (needsRetransmission()) {
            return String.format("多媒体数据上传应答（需重传%d个包）", retransmissionPacketCount);
        } else {
            return "多媒体数据上传应答（接收完成）";
        }
    }

    /**
     * 获取重传包ID列表的描述
     * 
     * @return 重传包ID列表描述
     */
    public String getRetransmissionPacketIdsDescription() {
        if (retransmissionPacketIds == null || retransmissionPacketIds.isEmpty()) {
            return "无";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < retransmissionPacketIds.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(retransmissionPacketIds.get(i));
        }
        return sb.toString();
    }

    // Getter and Setter methods
    public long getMultimediaId() {
        return multimediaId;
    }

    public void setMultimediaId(long multimediaId) {
        this.multimediaId = multimediaId;
    }

    public int getRetransmissionPacketCount() {
        return retransmissionPacketCount;
    }

    public void setRetransmissionPacketCount(int retransmissionPacketCount) {
        this.retransmissionPacketCount = retransmissionPacketCount;
    }

    public List<Integer> getRetransmissionPacketIds() {
        return retransmissionPacketIds;
    }

    public void setRetransmissionPacketIds(List<Integer> retransmissionPacketIds) {
        if (retransmissionPacketIds == null) {
            this.retransmissionPacketIds = new ArrayList<>();
            this.retransmissionPacketCount = 0;
        } else {
            this.retransmissionPacketIds = retransmissionPacketIds;
            this.retransmissionPacketCount = retransmissionPacketIds.size();
        }
    }

    @Override
    public String toString() {
        return String.format(
            "T8801MultimediaDataUploadResponse{" +
            "multimediaId=%d(0x%08X), " +
            "retransmissionPacketCount=%d, " +
            "retransmissionPacketIds=%s, " +
            "needsRetransmission=%s, " +
            "description='%s'}" +
            "}",
            multimediaId, multimediaId,
            retransmissionPacketCount,
            getRetransmissionPacketIdsDescription(),
            needsRetransmission(),
            getMessageDescription()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        T8801MultimediaDataUploadResponse that = (T8801MultimediaDataUploadResponse) o;
        return multimediaId == that.multimediaId &&
               retransmissionPacketCount == that.retransmissionPacketCount &&
               Objects.equals(retransmissionPacketIds, that.retransmissionPacketIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), multimediaId, retransmissionPacketCount, retransmissionPacketIds);
    }
}