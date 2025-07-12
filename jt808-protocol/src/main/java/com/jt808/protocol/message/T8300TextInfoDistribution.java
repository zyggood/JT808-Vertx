package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * 文本信息下发消息 (0x8300)
 * 平台向终端下发文本信息
 */
public class T8300TextInfoDistribution extends JT808Message {

    /**
     * 文本信息标志
     */
    private byte textFlag;

    /**
     * 文本信息内容
     */
    private String textInfo;

    public T8300TextInfoDistribution() {
        super();
    }

    public T8300TextInfoDistribution(JT808Header header) {
        super(header);
    }

    /**
     * 构造文本信息下发消息
     *
     * @param textFlag 文本信息标志
     * @param textInfo 文本信息内容
     */
    public T8300TextInfoDistribution(byte textFlag, String textInfo) {
        this.textFlag = textFlag;
        this.textInfo = textInfo;
    }

    /**
     * 创建紧急文本信息
     *
     * @param textInfo        文本内容
     * @param terminalDisplay 是否终端显示
     * @param terminalTTS     是否TTS播读
     * @return 文本信息下发消息
     */
    public static T8300TextInfoDistribution createEmergencyText(String textInfo, boolean terminalDisplay, boolean terminalTTS) {
        byte flag = TextFlag.EMERGENCY;
        if (terminalDisplay) {
            flag |= TextFlag.TERMINAL_DISPLAY;
        }
        if (terminalTTS) {
            flag |= TextFlag.TERMINAL_TTS;
        }
        return new T8300TextInfoDistribution(flag, textInfo);
    }

    /**
     * 创建普通文本信息
     *
     * @param textInfo             文本内容
     * @param terminalDisplay      是否终端显示
     * @param terminalTTS          是否TTS播读
     * @param advertisementDisplay 是否广告屏显示
     * @return 文本信息下发消息
     */
    public static T8300TextInfoDistribution createNormalText(String textInfo, boolean terminalDisplay,
                                                             boolean terminalTTS, boolean advertisementDisplay) {
        byte flag = 0;
        if (terminalDisplay) {
            flag |= TextFlag.TERMINAL_DISPLAY;
        }
        if (terminalTTS) {
            flag |= TextFlag.TERMINAL_TTS;
        }
        if (advertisementDisplay) {
            flag |= TextFlag.ADVERTISEMENT_DISPLAY;
        }
        return new T8300TextInfoDistribution(flag, textInfo);
    }

    /**
     * 创建CAN故障码信息
     *
     * @param textInfo        故障码信息
     * @param terminalDisplay 是否终端显示
     * @return 文本信息下发消息
     */
    public static T8300TextInfoDistribution createCANFaultInfo(String textInfo, boolean terminalDisplay) {
        byte flag = TextFlag.CAN_FAULT_INFO;
        if (terminalDisplay) {
            flag |= TextFlag.TERMINAL_DISPLAY;
        }
        return new T8300TextInfoDistribution(flag, textInfo);
    }

    @Override
    public int getMessageId() {
        return 0x8300;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 文本信息标志 (1字节)
        buffer.appendByte(textFlag);

        // 文本信息 (STRING，GBK编码，最长1024字节)
        if (textInfo != null && !textInfo.isEmpty()) {
            byte[] textBytes = textInfo.getBytes(Charset.forName("GBK"));
            if (textBytes.length > 1024) {
                throw new IllegalArgumentException("文本信息长度不能超过1024字节，当前长度: " + textBytes.length + " 字节");
            }
            buffer.appendBytes(textBytes);
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 1) {
            throw new IllegalArgumentException("文本信息下发消息体长度至少为1字节，实际长度: " + (body != null ? body.length() : 0) + " 字节");
        }

        int index = 0;

        // 文本信息标志 (1字节)
        textFlag = body.getByte(index);
        index += 1;

        // 文本信息 (剩余字节，GBK编码)
        if (index < body.length()) {
            byte[] textBytes = body.getBytes(index, body.length());
            textInfo = new String(textBytes, Charset.forName("GBK"));
        } else {
            textInfo = "";
        }
    }

    /**
     * 检查是否为紧急文本
     *
     * @return true表示紧急
     */
    public boolean isEmergency() {
        return (textFlag & TextFlag.EMERGENCY) != 0;
    }

    /**
     * 检查是否需要终端显示器显示
     *
     * @return true表示需要显示
     */
    public boolean isTerminalDisplay() {
        return (textFlag & TextFlag.TERMINAL_DISPLAY) != 0;
    }

    /**
     * 检查是否需要终端TTS播读
     *
     * @return true表示需要播读
     */
    public boolean isTerminalTTS() {
        return (textFlag & TextFlag.TERMINAL_TTS) != 0;
    }

    /**
     * 检查是否需要广告屏显示
     *
     * @return true表示需要显示
     */
    public boolean isAdvertisementDisplay() {
        return (textFlag & TextFlag.ADVERTISEMENT_DISPLAY) != 0;
    }

    /**
     * 检查信息类型
     *
     * @return true表示CAN故障码信息，false表示中心导航信息
     */
    public boolean isCANFaultInfo() {
        return (textFlag & TextFlag.CAN_FAULT_INFO) != 0;
    }

    /**
     * 获取文本标志描述
     *
     * @return 标志描述
     */
    public String getTextFlagDescription() {
        StringBuilder sb = new StringBuilder();

        if (isEmergency()) {
            sb.append("紧急; ");
        }
        if (isTerminalDisplay()) {
            sb.append("终端显示器显示; ");
        }
        if (isTerminalTTS()) {
            sb.append("终端TTS播读; ");
        }
        if (isAdvertisementDisplay()) {
            sb.append("广告屏显示; ");
        }
        if (isCANFaultInfo()) {
            sb.append("CAN故障码信息; ");
        } else {
            sb.append("中心导航信息; ");
        }

        String result = sb.toString();
        return result.endsWith("; ") ? result.substring(0, result.length() - 2) : result;
    }

    // Getters and Setters
    public byte getTextFlag() {
        return textFlag;
    }

    public void setTextFlag(byte textFlag) {
        this.textFlag = textFlag;
    }

    public String getTextInfo() {
        return textInfo;
    }

    public void setTextInfo(String textInfo) {
        if (textInfo != null) {
            byte[] textBytes = textInfo.getBytes(Charset.forName("GBK"));
            if (textBytes.length > 1024) {
                throw new IllegalArgumentException("文本信息长度不能超过1024字节，当前长度: " + textBytes.length + " 字节");
            }
        }
        this.textInfo = textInfo;
    }

    @Override
    public String toString() {
        return "T8300TextInfoDistribution{" +
                "textFlag=0x" + String.format("%02X", textFlag & 0xFF) +
                " (" + getTextFlagDescription() + ")" +
                ", textInfo='" + textInfo + '\'' +
                ", header=" + getHeader() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8300TextInfoDistribution that = (T8300TextInfoDistribution) o;
        return textFlag == that.textFlag && Objects.equals(textInfo, that.textInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(textFlag, textInfo);
    }

    /**
     * 文本信息标志位常量
     */
    public static class TextFlag {
        /**
         * 位0: 紧急
         */
        public static final byte EMERGENCY = 0x01;

        /**
         * 位1: 保留
         */
        public static final byte RESERVED_1 = 0x02;

        /**
         * 位2: 终端显示器显示
         */
        public static final byte TERMINAL_DISPLAY = 0x04;

        /**
         * 位3: 终端TTS播读
         */
        public static final byte TERMINAL_TTS = 0x08;

        /**
         * 位4: 广告屏显示
         */
        public static final byte ADVERTISEMENT_DISPLAY = 0x10;

        /**
         * 位5: 0-中心导航信息，1-CAN故障码信息
         */
        public static final byte CAN_FAULT_INFO = 0x20;

        /**
         * 位6-7: 保留
         */
        public static final byte RESERVED_6_7 = (byte) 0xC0;
    }
}