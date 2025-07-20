package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CAN总线数据上传消息
 * 消息ID: 0x0705
 */
public class T0705CanBusDataUpload extends JT808Message {

    /**
     * 数据项个数
     */
    private int itemCount;

    /**
     * CAN总线数据接收时间 (hh-mm-ss-msms)
     */
    private LocalTime receiveTime;

    /**
     * CAN总线数据项列表
     */
    private List<CanBusDataItem> canDataItems;

    public T0705CanBusDataUpload() {
        this.canDataItems = new ArrayList<>();
    }

    @Override
    public int getMessageId() {
        return 0x0705;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 数据项个数 (WORD)
        buffer.appendUnsignedShort(itemCount);

        // CAN总线数据接收时间 (BCD[5])
        if (receiveTime != null) {
            // 转换为BCD格式：hh-mm-ss-msms (5字节)
            int hour = receiveTime.getHour();
            int minute = receiveTime.getMinute();
            int second = receiveTime.getSecond();
            int millis = receiveTime.getNano() / 1_000_000;
            
            // BCD编码：每个字节存储两个十进制数字
            buffer.appendByte((byte) ((hour / 10) << 4 | (hour % 10)));
            buffer.appendByte((byte) ((minute / 10) << 4 | (minute % 10)));
            buffer.appendByte((byte) ((second / 10) << 4 | (second % 10)));
            // 毫秒部分（2字节，大端序）
            buffer.appendUnsignedShort(millis);
        } else {
            // 如果时间为空，填充0
            buffer.appendBytes(new byte[5]);
        }

        // CAN总线数据项
        if (canDataItems != null) {
            for (CanBusDataItem item : canDataItems) {
                buffer.appendBuffer(item.encode());
            }
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        int pos = 0;

        // 数据项个数 (WORD)
        itemCount = body.getUnsignedShort(pos);
        pos += 2;

        // CAN总线数据接收时间 (BCD[5])
        int hourBcd = body.getUnsignedByte(pos++);
        int minuteBcd = body.getUnsignedByte(pos++);
        int secondBcd = body.getUnsignedByte(pos++);
        int millis = body.getUnsignedShort(pos);
        pos += 2;
        
        // BCD解码：每个字节的高4位和低4位分别是十位和个位
        int hour = ((hourBcd >> 4) & 0x0F) * 10 + (hourBcd & 0x0F);
        int minute = ((minuteBcd >> 4) & 0x0F) * 10 + (minuteBcd & 0x0F);
        int second = ((secondBcd >> 4) & 0x0F) * 10 + (secondBcd & 0x0F);
        
        receiveTime = LocalTime.of(hour, minute, second, millis * 1_000_000);

        // CAN总线数据项
        canDataItems = new ArrayList<>();
        for (int i = 0; i < itemCount && pos < body.length(); i++) {
            CanBusDataItem item = new CanBusDataItem();
            pos += item.decode(body, pos);
            canDataItems.add(item);
        }
    }

    /**
     * CAN总线数据项
     */
    public static class CanBusDataItem {
        /**
         * CAN ID (4字节)
         * bit31: CAN通道号，0：CAN1，1：CAN2
         * bit30: 帧类型，0：标准帧，1：扩展帧
         * bit29: 数据采集方式，0：原始数据，1：采集区间的平均值
         * bit28-bit0: CAN总线ID
         */
        private long canId;

        /**
         * CAN数据 (8字节)
         */
        private byte[] canData;

        public CanBusDataItem() {
            this.canData = new byte[8];
        }

        public CanBusDataItem(long canId, byte[] canData) {
            this.canId = canId;
            this.canData = canData != null ? canData : new byte[8];
        }

        /**
         * 编码CAN数据项
         */
        public Buffer encode() {
            Buffer buffer = Buffer.buffer();
            
            // CAN ID (4字节)
            buffer.appendUnsignedInt(canId);
            
            // CAN DATA (8字节)
            buffer.appendBytes(canData);
            
            return buffer;
        }

        /**
         * 解码CAN数据项
         * @param buffer 数据缓冲区
         * @param offset 起始位置
         * @return 解码的字节数
         */
        public int decode(Buffer buffer, int offset) {
            int pos = offset;
            
            // CAN ID (4字节)
            canId = buffer.getUnsignedInt(pos);
            pos += 4;
            
            // CAN DATA (8字节)
            canData = buffer.getBytes(pos, pos + 8);
            pos += 8;
            
            return pos - offset;
        }

        // Getters and Setters
        public long getCanId() {
            return canId;
        }

        public void setCanId(long canId) {
            this.canId = canId;
        }

        public byte[] getCanData() {
            return canData;
        }

        public void setCanData(byte[] canData) {
            this.canData = canData != null ? canData : new byte[8];
        }

        /**
         * 获取CAN通道号
         * @return 0：CAN1，1：CAN2
         */
        public int getCanChannel() {
            return (int) ((canId >> 31) & 0x1);
        }

        /**
         * 设置CAN通道号
         * @param channel 0：CAN1，1：CAN2
         */
        public void setCanChannel(int channel) {
            if (channel == 1) {
                canId |= 0x80000000L;
            } else {
                canId &= 0x7FFFFFFFL;
            }
        }

        /**
         * 获取帧类型
         * @return 0：标准帧，1：扩展帧
         */
        public int getFrameType() {
            return (int) ((canId >> 30) & 0x1);
        }

        /**
         * 设置帧类型
         * @param frameType 0：标准帧，1：扩展帧
         */
        public void setFrameType(int frameType) {
            if (frameType == 1) {
                canId |= 0x40000000L;
            } else {
                canId &= 0xBFFFFFFFL;
            }
        }

        /**
         * 获取数据采集方式
         * @return 0：原始数据，1：采集区间的平均值
         */
        public int getDataCollectionMethod() {
            return (int) ((canId >> 29) & 0x1);
        }

        /**
         * 设置数据采集方式
         * @param method 0：原始数据，1：采集区间的平均值
         */
        public void setDataCollectionMethod(int method) {
            if (method == 1) {
                canId |= 0x20000000L;
            } else {
                canId &= 0xDFFFFFFFL;
            }
        }

        /**
         * 获取CAN总线ID
         * @return CAN总线ID (bit28-bit0)
         */
        public long getCanBusId() {
            return canId & 0x1FFFFFFFL;
        }

        /**
         * 设置CAN总线ID
         * @param busId CAN总线ID (bit28-bit0)
         */
        public void setCanBusId(long busId) {
            canId = (canId & 0xE0000000L) | (busId & 0x1FFFFFFFL);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("CanBusDataItem{");
            sb.append("canId=0x").append(Long.toHexString(canId).toUpperCase());
            sb.append(", channel=").append(getCanChannel());
            sb.append(", frameType=").append(getFrameType());
            sb.append(", collectionMethod=").append(getDataCollectionMethod());
            sb.append(", busId=0x").append(Long.toHexString(getCanBusId()).toUpperCase());
            sb.append(", data=[");
            for (int i = 0; i < canData.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(String.format("0x%02X", canData[i] & 0xFF));
            }
            sb.append("]}");
            return sb.toString();
        }
    }

    // Getters and Setters
    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public LocalTime getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(LocalTime receiveTime) {
        this.receiveTime = receiveTime;
    }

    public List<CanBusDataItem> getCanDataItems() {
        return canDataItems;
    }

    public void setCanDataItems(List<CanBusDataItem> canDataItems) {
        this.canDataItems = canDataItems != null ? canDataItems : new ArrayList<>();
        this.itemCount = this.canDataItems.size();
    }

    /**
     * 添加CAN数据项
     */
    public void addCanDataItem(CanBusDataItem item) {
        if (canDataItems == null) {
            canDataItems = new ArrayList<>();
        }
        canDataItems.add(item);
        itemCount = canDataItems.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("T0705CanBusDataUpload{\n");
        sb.append("  itemCount=").append(itemCount).append(",\n");
        sb.append("  receiveTime=").append(receiveTime).append(",\n");
        sb.append("  canDataItems=[\n");
        if (canDataItems != null) {
            for (int i = 0; i < canDataItems.size(); i++) {
                sb.append("    [").append(i).append("] ").append(canDataItems.get(i)).append("\n");
            }
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }
}