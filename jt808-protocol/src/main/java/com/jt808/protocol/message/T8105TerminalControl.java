package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * 终端控制消息 (0x8105)
 * 平台向终端发送控制指令
 */
public class T8105TerminalControl extends JT808Message {

    /**
     * 命令字
     */
    private byte commandWord;

    /**
     * 命令参数
     */
    private String commandParameters;

    public T8105TerminalControl() {
        super();
    }

    public T8105TerminalControl(JT808Header header) {
        super(header);
    }

    /**
     * 构造终端控制消息
     *
     * @param commandWord       命令字
     * @param commandParameters 命令参数
     */
    public T8105TerminalControl(byte commandWord, String commandParameters) {
        this.commandWord = commandWord;
        this.commandParameters = commandParameters;
    }

    /**
     * 创建无线升级控制消息
     */
    public static T8105TerminalControl createWirelessUpgrade(String url, String dialPoint,
                                                             String dialUser, String dialPassword, String address, int tcpPort, int udpPort,
                                                             byte[] manufacturerId, String hardwareVersion, String firmwareVersion, int timeLimit) {

        String params = String.join(";",
                url != null ? url : "",
                dialPoint != null ? dialPoint : "",
                dialUser != null ? dialUser : "",
                dialPassword != null ? dialPassword : "",
                address != null ? address : "",
                String.valueOf(tcpPort),
                String.valueOf(udpPort),
                manufacturerId != null ? new String(manufacturerId) : "",
                hardwareVersion != null ? hardwareVersion : "",
                firmwareVersion != null ? firmwareVersion : "",
                String.valueOf(timeLimit)
        );

        return new T8105TerminalControl(ControlCommand.WIRELESS_UPGRADE.getValue(), params);
    }

    /**
     * 创建连接指定服务器控制消息
     */
    public static T8105TerminalControl createConnectServer(byte connectionControl, String authCode,
                                                           String dialPoint, String dialUser, String dialPassword, String address,
                                                           int tcpPort, int udpPort, int timeLimit) {

        String params;
        if (connectionControl == 1) {
            // 切换回原服务器，无后续参数
            params = "1";
        } else {
            // 切换到指定服务器
            params = String.join(";",
                    String.valueOf(connectionControl),
                    authCode != null ? authCode : "",
                    dialPoint != null ? dialPoint : "",
                    dialUser != null ? dialUser : "",
                    dialPassword != null ? dialPassword : "",
                    address != null ? address : "",
                    String.valueOf(tcpPort),
                    String.valueOf(udpPort),
                    String.valueOf(timeLimit)
            );
        }

        return new T8105TerminalControl(ControlCommand.CONNECT_SERVER.getValue(), params);
    }

    /**
     * 创建简单控制命令（无参数）
     */
    public static T8105TerminalControl createSimpleCommand(ControlCommand command) {
        if (command.hasParameters()) {
            throw new IllegalArgumentException("命令 " + command.getDescription() + " 需要参数");
        }
        return new T8105TerminalControl(command.getValue(), "");
    }

    @Override
    public int getMessageId() {
        return 0x8105;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 命令字 (1字节)
        buffer.appendByte(commandWord);

        // 命令参数 (STRING，GBK编码)
        if (commandParameters != null && !commandParameters.isEmpty()) {
            byte[] paramBytes = commandParameters.getBytes(Charset.forName("GBK"));
            buffer.appendBytes(paramBytes);
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        int index = 0;

        // 命令字 (1字节)
        commandWord = body.getByte(index);
        index += 1;

        // 命令参数 (剩余字节，GBK编码)
        if (index < body.length()) {
            byte[] paramBytes = body.getBytes(index, body.length());
            commandParameters = new String(paramBytes, Charset.forName("GBK"));
        } else {
            commandParameters = "";
        }
    }

    /**
     * 获取命令描述
     */
    public String getCommandDescription() {
        ControlCommand command = ControlCommand.fromValue(commandWord);
        return command != null ? command.getDescription() : "未知命令(" + (commandWord & 0xFF) + ")";
    }

    /**
     * 解析命令参数为列表
     *
     * @return 参数列表
     */
    public List<String> getParameterList() {
        if (commandParameters == null || commandParameters.isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(commandParameters.split(";"));
    }

    /**
     * 设置命令参数列表
     *
     * @param parameters 参数列表
     */
    public void setParameterList(List<String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            this.commandParameters = "";
        } else {
            this.commandParameters = String.join(";", parameters);
        }
    }

    // Getters and Setters
    public byte getCommandWord() {
        return commandWord;
    }

    public void setCommandWord(byte commandWord) {
        this.commandWord = commandWord;
    }

    public String getCommandParameters() {
        return commandParameters;
    }

    public void setCommandParameters(String commandParameters) {
        this.commandParameters = commandParameters;
    }

    @Override
    public String toString() {
        return "T8105TerminalControl{" +
                "commandWord=" + getCommandDescription() +
                ", commandParameters='" + commandParameters + '\'' +
                ", header=" + getHeader() +
                '}';
    }

    /**
     * 终端控制命令枚举
     */
    public enum ControlCommand {
        WIRELESS_UPGRADE((byte) 1, "无线升级", true),
        CONNECT_SERVER((byte) 2, "控制终端连接指定服务器", true),
        TERMINAL_SHUTDOWN((byte) 3, "终端关机", false),
        TERMINAL_RESET((byte) 4, "终端复位", false),
        TERMINAL_FACTORY_RESET((byte) 5, "终端恢复出厂设置", false),
        CLOSE_DATA_COMMUNICATION((byte) 6, "关闭数据通信", false),
        CLOSE_ALL_WIRELESS_COMMUNICATION((byte) 7, "关闭所有无线通信", false);

        private final byte value;
        private final String description;
        private final boolean hasParameters;

        ControlCommand(byte value, String description, boolean hasParameters) {
            this.value = value;
            this.description = description;
            this.hasParameters = hasParameters;
        }

        /**
         * 根据值获取命令
         */
        public static ControlCommand fromValue(byte value) {
            for (ControlCommand command : values()) {
                if (command.value == value) {
                    return command;
                }
            }
            return null;
        }

        public byte getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public boolean hasParameters() {
            return hasParameters;
        }
    }

    /**
     * 连接控制参数
     */
    public static class ConnectionControl {
        public static final byte SWITCH_TO_SPECIFIED_SERVER = 0; // 切换到指定监管平台服务器
        public static final byte SWITCH_TO_DEFAULT_SERVER = 1;   // 切换回原缺省监控平台服务器
    }
}