package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * T8203人工确认报警消息
 * <p>
 * 消息ID: 0x8203
 * 消息体长度: 6字节
 * <p>
 * 该消息用于平台向终端发送人工确认报警指令。
 * 终端收到此消息后，确认相应的报警信息。
 * <p>
 * 消息体结构:
 * - 报警消息流水号 (WORD, 2字节): 需人工确认的报警消息流水号，0表示该报警类型所有消息
 * - 人工确认报警类型 (DWORD, 4字节): 按位定义的报警类型
 *
 * @author JT808 Protocol Team
 * @version 1.0
 * @since 1.0
 */
public class T8203ManualAlarmConfirmation extends JT808Message {

    /**
     * 报警消息流水号，0表示该报警类型所有消息
     */
    private int alarmSequenceNumber;

    /**
     * 人工确认报警类型
     */
    private long confirmationAlarmType;

    /**
     * 默认构造函数
     */
    public T8203ManualAlarmConfirmation() {
        super();
    }

    /**
     * 构造人工确认报警消息
     *
     * @param alarmSequenceNumber   报警消息流水号，0表示该报警类型所有消息
     * @param confirmationAlarmType 人工确认报警类型
     */
    public T8203ManualAlarmConfirmation(int alarmSequenceNumber, long confirmationAlarmType) {
        this();
        this.alarmSequenceNumber = alarmSequenceNumber;
        this.confirmationAlarmType = confirmationAlarmType;
    }

    /**
     * 创建确认指定流水号报警的消息
     *
     * @param sequenceNumber 报警消息流水号
     * @param alarmType      报警类型
     * @return T8203ManualAlarmConfirmation实例
     */
    public static T8203ManualAlarmConfirmation createConfirmSpecificAlarm(int sequenceNumber, long alarmType) {
        if (sequenceNumber < 0 || sequenceNumber > 0xFFFF) {
            throw new IllegalArgumentException("报警消息流水号必须在0-65535范围内");
        }
        if (alarmType < 0) {
            throw new IllegalArgumentException("报警类型不能为负数");
        }
        return new T8203ManualAlarmConfirmation(sequenceNumber, alarmType);
    }

    /**
     * 创建确认所有指定类型报警的消息
     *
     * @param alarmType 报警类型
     * @return T8203ManualAlarmConfirmation实例
     */
    public static T8203ManualAlarmConfirmation createConfirmAllAlarms(long alarmType) {
        if (alarmType < 0) {
            throw new IllegalArgumentException("报警类型不能为负数");
        }
        return new T8203ManualAlarmConfirmation(0, alarmType);
    }

    /**
     * 获取消息ID
     *
     * @return 消息ID 0x8203
     */
    @Override
    public int getMessageId() {
        return 0x8203;
    }

    /**
     * 编码消息体
     *
     * @return 编码后的消息体
     */
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 报警消息流水号 (WORD, 2字节，大端序)
        buffer.appendUnsignedShort(alarmSequenceNumber & 0xFFFF);

        // 人工确认报警类型 (DWORD, 4字节，大端序)
        buffer.appendUnsignedInt(confirmationAlarmType & 0xFFFFFFFFL);

        return buffer;
    }

    /**
     * 解码消息体
     *
     * @param body 消息体数据
     * @throws IllegalArgumentException 如果消息体长度不正确
     */
    @Override
    public void decodeBody(Buffer body) {
        if (body == null) {
            throw new IllegalArgumentException("消息体不能为空");
        }

        // 检查消息体长度
        if (body.length() < 6) {
            throw new IllegalArgumentException(
                    "人工确认报警消息体长度应为6字节，实际长度: " + body.length() + " 字节");
        }

        int index = 0;

        // 报警消息流水号 (WORD, 2字节，大端序)
        alarmSequenceNumber = body.getUnsignedShort(index);
        index += 2;

        // 人工确认报警类型 (DWORD, 4字节，大端序)
        confirmationAlarmType = body.getUnsignedInt(index);
    }

    /**
     * 检查是否为确认所有报警
     *
     * @return 如果流水号为0则返回true
     */
    public boolean isConfirmAllAlarms() {
        return alarmSequenceNumber == 0;
    }

    /**
     * 获取消息描述
     *
     * @return 消息描述
     */
    public String getMessageDescription() {
        return "人工确认报警";
    }

    /**
     * 获取确认状态描述
     *
     * @return 确认状态描述
     */
    public String getConfirmationDescription() {
        String alarmDesc = AlarmConfirmationType.getAlarmTypeDescription(confirmationAlarmType);
        if (isConfirmAllAlarms()) {
            return String.format("确认所有[%s]报警", alarmDesc);
        } else {
            return String.format("确认流水号[%d]的[%s]报警", alarmSequenceNumber, alarmDesc);
        }
    }

    /**
     * 获取报警消息流水号
     *
     * @return 报警消息流水号
     */
    public int getAlarmSequenceNumber() {
        return alarmSequenceNumber;
    }

    // Getters and Setters

    /**
     * 设置报警消息流水号
     *
     * @param alarmSequenceNumber 报警消息流水号，0表示该报警类型所有消息
     */
    public void setAlarmSequenceNumber(int alarmSequenceNumber) {
        if (alarmSequenceNumber < 0 || alarmSequenceNumber > 0xFFFF) {
            throw new IllegalArgumentException("报警消息流水号必须在0-65535范围内");
        }
        this.alarmSequenceNumber = alarmSequenceNumber;
    }

    /**
     * 获取人工确认报警类型
     *
     * @return 人工确认报警类型
     */
    public long getConfirmationAlarmType() {
        return confirmationAlarmType;
    }

    /**
     * 设置人工确认报警类型
     *
     * @param confirmationAlarmType 人工确认报警类型
     */
    public void setConfirmationAlarmType(long confirmationAlarmType) {
        if (confirmationAlarmType < 0) {
            throw new IllegalArgumentException("报警类型不能为负数");
        }
        this.confirmationAlarmType = confirmationAlarmType;
    }

    @Override
    public String toString() {
        return String.format("T8203ManualAlarmConfirmation{" +
                        "alarmSequenceNumber=%d, " +
                        "confirmationAlarmType=0x%08X, " +
                        "description='%s'}",
                alarmSequenceNumber, confirmationAlarmType, getConfirmationDescription());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        T8203ManualAlarmConfirmation that = (T8203ManualAlarmConfirmation) obj;
        return alarmSequenceNumber == that.alarmSequenceNumber &&
                confirmationAlarmType == that.confirmationAlarmType;
    }

    @Override
    public int hashCode() {
        int result = alarmSequenceNumber;
        result = 31 * result + (int) (confirmationAlarmType ^ (confirmationAlarmType >>> 32));
        return result;
    }

    /**
     * 人工确认报警类型定义
     */
    public static class AlarmConfirmationType {
        /**
         * 确认紧急报警
         */
        public static final long EMERGENCY_ALARM = 0x00000001L;

        /**
         * 确认危险预警
         */
        public static final long DANGER_WARNING = 0x00000008L;

        /**
         * 确认进出区域报警
         */
        public static final long AREA_ALARM = 0x00100000L;

        /**
         * 确认进出路线报警
         */
        public static final long ROUTE_ALARM = 0x00200000L;

        /**
         * 确认路段行驶时间不足/过长报警
         */
        public static final long DRIVING_TIME_ALARM = 0x00400000L;

        /**
         * 确认车辆非法点火报警
         */
        public static final long ILLEGAL_IGNITION_ALARM = 0x08000000L;

        /**
         * 确认车辆非法位移报警
         */
        public static final long ILLEGAL_DISPLACEMENT_ALARM = 0x10000000L;

        /**
         * 检查是否包含指定的报警类型
         *
         * @param alarmType 报警类型值
         * @param checkType 要检查的报警类型
         * @return 如果包含返回true
         */
        public static boolean hasAlarmType(long alarmType, long checkType) {
            return (alarmType & checkType) != 0;
        }

        /**
         * 获取报警类型描述
         *
         * @param alarmType 报警类型值
         * @return 报警类型描述
         */
        public static String getAlarmTypeDescription(long alarmType) {
            StringBuilder sb = new StringBuilder();

            if (hasAlarmType(alarmType, EMERGENCY_ALARM)) {
                sb.append("紧急报警,");
            }
            if (hasAlarmType(alarmType, DANGER_WARNING)) {
                sb.append("危险预警,");
            }
            if (hasAlarmType(alarmType, AREA_ALARM)) {
                sb.append("进出区域报警,");
            }
            if (hasAlarmType(alarmType, ROUTE_ALARM)) {
                sb.append("进出路线报警,");
            }
            if (hasAlarmType(alarmType, DRIVING_TIME_ALARM)) {
                sb.append("路段行驶时间报警,");
            }
            if (hasAlarmType(alarmType, ILLEGAL_IGNITION_ALARM)) {
                sb.append("非法点火报警,");
            }
            if (hasAlarmType(alarmType, ILLEGAL_DISPLACEMENT_ALARM)) {
                sb.append("非法位移报警,");
            }

            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1); // 移除最后的逗号
            } else {
                sb.append("未知报警类型");
            }

            return sb.toString();
        }
    }
}