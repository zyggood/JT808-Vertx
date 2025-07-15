package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * 信息服务消息 (0x8304)
 * 平台向终端下发信息服务内容
 */
public class T8304InfoService extends JT808Message {

    public static final int MESSAGE_ID = 0x8304;

    /**
     * 信息类型
     */
    private byte infoType;

    /**
     * 信息长度
     */
    private int infoLength;

    /**
     * 信息内容
     */
    private String infoContent;

    public T8304InfoService() {
        super();
    }

    public T8304InfoService(JT808Header header) {
        super(header);
    }

    /**
     * 构造信息服务消息
     *
     * @param infoType    信息类型
     * @param infoContent 信息内容
     */
    public T8304InfoService(byte infoType, String infoContent) {
        this.infoType = infoType;
        this.infoContent = infoContent;
        // 自动计算信息长度
        if (infoContent != null) {
            this.infoLength = infoContent.getBytes(Charset.forName("GBK")).length;
        } else {
            this.infoLength = 0;
        }
    }

    /**
     * 创建信息服务消息
     *
     * @param infoType    信息类型
     * @param infoContent 信息内容
     * @return 信息服务消息
     */
    public static T8304InfoService createInfoService(byte infoType, String infoContent) {
        return new T8304InfoService(infoType, infoContent);
    }

    /**
     * 创建新闻信息服务
     *
     * @param newsContent 新闻内容
     * @return 信息服务消息
     */
    public static T8304InfoService createNewsService(String newsContent) {
        return new T8304InfoService((byte) 0x01, newsContent);
    }

    /**
     * 创建天气信息服务
     *
     * @param weatherContent 天气内容
     * @return 信息服务消息
     */
    public static T8304InfoService createWeatherService(String weatherContent) {
        return new T8304InfoService((byte) 0x02, weatherContent);
    }

    /**
     * 创建交通信息服务
     *
     * @param trafficContent 交通信息内容
     * @return 信息服务消息
     */
    public static T8304InfoService createTrafficService(String trafficContent) {
        return new T8304InfoService((byte) 0x03, trafficContent);
    }

    /**
     * 创建股票信息服务
     *
     * @param stockContent 股票信息内容
     * @return 信息服务消息
     */
    public static T8304InfoService createStockService(String stockContent) {
        return new T8304InfoService((byte) 0x04, stockContent);
    }

    @Override
    public int getMessageId() {
        return MESSAGE_ID;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 信息类型 (1字节)
        buffer.appendByte(infoType);

        // 计算信息内容的GBK编码字节长度
        byte[] contentBytes = new byte[0];
        if (infoContent != null && !infoContent.isEmpty()) {
            contentBytes = infoContent.getBytes(Charset.forName("GBK"));
        }

        // 信息长度 (2字节，WORD)
        buffer.appendUnsignedShort(contentBytes.length);

        // 信息内容 (STRING，GBK编码)
        if (contentBytes.length > 0) {
            buffer.appendBytes(contentBytes);
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 3) {
            throw new IllegalArgumentException("信息服务消息体长度至少为3字节，实际长度: " + (body != null ? body.length() : 0) + " 字节");
        }

        int index = 0;

        // 信息类型 (1字节)
        infoType = body.getByte(index);
        index += 1;

        // 信息长度 (2字节，WORD)
        infoLength = body.getUnsignedShort(index);
        index += 2;

        // 验证消息体长度
        if (body.length() < index + infoLength) {
            throw new IllegalArgumentException("消息体长度不足，期望: " + (index + infoLength) + " 字节，实际: " + body.length() + " 字节");
        }

        // 信息内容 (STRING，GBK编码)
        if (infoLength > 0) {
            byte[] contentBytes = body.getBytes(index, index + infoLength);
            infoContent = new String(contentBytes, Charset.forName("GBK"));
        } else {
            infoContent = "";
        }
    }

    /**
     * 获取信息类型的无符号值
     *
     * @return 信息类型的无符号值 (0-255)
     */
    public int getInfoTypeUnsigned() {
        return Byte.toUnsignedInt(infoType);
    }

    /**
     * 获取信息类型描述
     *
     * @return 信息类型描述
     */
    public String getInfoTypeDescription() {
        switch (infoType) {
            case 0x01:
                return "新闻";
            case 0x02:
                return "天气";
            case 0x03:
                return "交通";
            case 0x04:
                return "股票";
            case 0x05:
                return "彩票";
            case 0x06:
                return "娱乐";
            case 0x07:
                return "广告";
            case 0x08:
                return "其他";
            default:
                return "未知类型(" + getInfoTypeUnsigned() + ")";
        }
    }

    /**
     * 检查是否为新闻信息
     *
     * @return true表示新闻信息
     */
    public boolean isNewsInfo() {
        return infoType == 0x01;
    }

    /**
     * 检查是否为天气信息
     *
     * @return true表示天气信息
     */
    public boolean isWeatherInfo() {
        return infoType == 0x02;
    }

    /**
     * 检查是否为交通信息
     *
     * @return true表示交通信息
     */
    public boolean isTrafficInfo() {
        return infoType == 0x03;
    }

    /**
     * 检查是否为股票信息
     *
     * @return true表示股票信息
     */
    public boolean isStockInfo() {
        return infoType == 0x04;
    }

    // Getter和Setter方法
    public byte getInfoType() {
        return infoType;
    }

    public void setInfoType(byte infoType) {
        this.infoType = infoType;
    }

    public int getInfoLength() {
        return infoLength;
    }

    public void setInfoLength(int infoLength) {
        this.infoLength = infoLength;
    }

    public String getInfoContent() {
        return infoContent;
    }

    public void setInfoContent(String infoContent) {
        this.infoContent = infoContent;
        // 自动更新信息长度
        if (infoContent != null) {
            this.infoLength = infoContent.getBytes(Charset.forName("GBK")).length;
        } else {
            this.infoLength = 0;
        }
    }

    @Override
    public String toString() {
        return "T8304InfoService{" +
                "infoType=" + getInfoTypeUnsigned() +
                " (" + getInfoTypeDescription() + ")" +
                ", infoLength=" + infoLength +
                ", infoContent='" + infoContent + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8304InfoService that = (T8304InfoService) o;
        return infoType == that.infoType &&
                infoLength == that.infoLength &&
                Objects.equals(infoContent, that.infoContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(infoType, infoLength, infoContent);
    }

    /**
     * 信息类型常量定义
     */
    public static class InfoType {
        /** 新闻 */
        public static final byte NEWS = 0x01;
        /** 天气 */
        public static final byte WEATHER = 0x02;
        /** 交通 */
        public static final byte TRAFFIC = 0x03;
        /** 股票 */
        public static final byte STOCK = 0x04;
        /** 彩票 */
        public static final byte LOTTERY = 0x05;
        /** 娱乐 */
        public static final byte ENTERTAINMENT = 0x06;
        /** 广告 */
        public static final byte ADVERTISEMENT = 0x07;
        /** 其他 */
        public static final byte OTHER = 0x08;
    }
}