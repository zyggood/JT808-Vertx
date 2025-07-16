package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * T8500车辆控制消息
 * <p>
 * 消息ID: 0x8500
 * 消息体长度: 1字节
 * <p>
 * 该消息用于平台向终端发送车辆控制指令。
 * 终端收到此消息后，执行相应的车辆控制操作。
 * <p>
 * 消息体结构:
 * - 控制标志 (BYTE, 1字节): 控制指令标志位，按位定义
 *   位0: 0-车门解锁；1-车门加锁
 *   位1-7: 保留
 *
 * @author JT808 Protocol Team
 * @version 1.0
 * @since 1.0
 */
public class T8500VehicleControl extends JT808Message {

    /**
     * 控制标志
     */
    private byte controlFlag;

    /**
     * 默认构造函数
     */
    public T8500VehicleControl() {
        super();
    }

    /**
     * 构造车辆控制消息
     *
     * @param controlFlag 控制标志
     */
    public T8500VehicleControl(byte controlFlag) {
        this();
        this.controlFlag = controlFlag;
    }

    /**
     * 创建车门解锁控制消息
     *
     * @return T8500VehicleControl实例
     */
    public static T8500VehicleControl createDoorUnlock() {
        return new T8500VehicleControl(ControlFlag.DOOR_UNLOCK);
    }

    /**
     * 创建车门加锁控制消息
     *
     * @return T8500VehicleControl实例
     */
    public static T8500VehicleControl createDoorLock() {
        return new T8500VehicleControl(ControlFlag.DOOR_LOCK);
    }

    /**
     * 获取消息ID
     *
     * @return 消息ID 0x8500
     */
    @Override
    public int getMessageId() {
        return 0x8500;
    }

    /**
     * 编码消息体
     *
     * @return 编码后的消息体
     */
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(controlFlag);
        return buffer;
    }

    /**
     * 解码消息体
     *
     * @param body 消息体数据
     */
    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 1) {
            throw new IllegalArgumentException("消息体长度不足，至少需要1字节");
        }
        if (body.length() != 1) {
            throw new IllegalArgumentException("消息体长度错误，应为1字节，实际为" + body.length() + "字节");
        }
        
        this.controlFlag = body.getByte(0);
    }

    /**
     * 检查是否为车门解锁控制
     *
     * @return 如果是车门解锁返回true
     */
    public boolean isDoorUnlock() {
        return (controlFlag & 0x01) == 0;
    }

    /**
     * 检查是否为车门加锁控制
     *
     * @return 如果是车门加锁返回true
     */
    public boolean isDoorLock() {
        return (controlFlag & 0x01) != 0;
    }

    /**
     * 获取消息描述
     *
     * @return 消息描述
     */
    public String getMessageDescription() {
        return "车辆控制消息";
    }

    /**
     * 获取控制操作描述
     *
     * @return 控制操作描述
     */
    public String getControlDescription() {
        if (isDoorUnlock()) {
            return "车门解锁";
        } else if (isDoorLock()) {
            return "车门加锁";
        } else {
            return "未知控制操作";
        }
    }

    /**
     * 获取控制标志
     *
     * @return 控制标志
     */
    public byte getControlFlag() {
        return controlFlag;
    }

    /**
     * 获取控制标志的无符号值
     *
     * @return 控制标志的无符号值
     */
    public int getControlFlagUnsigned() {
        return controlFlag & 0xFF;
    }

    /**
     * 设置控制标志
     *
     * @param controlFlag 控制标志
     */
    public void setControlFlag(byte controlFlag) {
        this.controlFlag = controlFlag;
    }

    @Override
    public String toString() {
        return String.format("T8500VehicleControl{" +
                        "controlFlag=0x%02X, " +
                        "controlDescription='%s'}",
                controlFlag & 0xFF,
                getControlDescription());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        T8500VehicleControl that = (T8500VehicleControl) obj;
        return controlFlag == that.controlFlag;
    }

    @Override
    public int hashCode() {
        return Byte.hashCode(controlFlag);
    }

    /**
     * 控制标志定义
     */
    public static class ControlFlag {
        /**
         * 车门解锁（位0=0）
         */
        public static final byte DOOR_UNLOCK = 0x00;

        /**
         * 车门加锁（位0=1）
         */
        public static final byte DOOR_LOCK = 0x01;

        /**
         * 检查是否包含指定的控制标志
         *
         * @param controlFlag 控制标志值
         * @param checkFlag   要检查的控制标志
         * @return 如果包含返回true
         */
        public static boolean hasControlFlag(byte controlFlag, byte checkFlag) {
            return (controlFlag & checkFlag) != 0;
        }

        /**
         * 获取控制标志描述
         *
         * @param controlFlag 控制标志值
         * @return 控制标志描述
         */
        public static String getControlFlagDescription(byte controlFlag) {
            if ((controlFlag & 0x01) == 0) {
                return "车门解锁";
            } else {
                return "车门加锁";
            }
        }
    }
}