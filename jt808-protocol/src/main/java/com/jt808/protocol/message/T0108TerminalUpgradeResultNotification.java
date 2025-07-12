package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * T0108 终端升级结果通知
 * 消息ID: 0x0108
 * <p>
 * 终端向平台报告升级结果的消息
 *
 * @author JT808-Vertx
 * @since 1.0
 */
public class T0108TerminalUpgradeResultNotification extends JT808Message {

    // 升级类型常量定义
    public static final byte UPGRADE_TYPE_TERMINAL = 0x00;           // 终端
    public static final byte UPGRADE_TYPE_IC_CARD_READER = 0x12;     // 道路运输证IC卡读卡器
    public static final byte UPGRADE_TYPE_BEIDOU_MODULE = 0x52;      // 北斗卫星定位模块
    // 升级结果常量定义
    public static final byte UPGRADE_RESULT_SUCCESS = 0x00;          // 成功
    public static final byte UPGRADE_RESULT_FAILURE = 0x01;          // 失败
    public static final byte UPGRADE_RESULT_CANCEL = 0x02;           // 取消
    /**
     * 升级类型
     */
    private byte upgradeType;
    /**
     * 升级结果
     */
    private byte upgradeResult;

    public T0108TerminalUpgradeResultNotification() {
        super();
    }

    public T0108TerminalUpgradeResultNotification(JT808Header header) {
        super(header);
    }

    /**
     * 构造终端升级结果通知消息
     *
     * @param upgradeType   升级类型
     * @param upgradeResult 升级结果
     */
    public T0108TerminalUpgradeResultNotification(byte upgradeType, byte upgradeResult) {
        this();
        this.upgradeType = upgradeType;
        this.upgradeResult = upgradeResult;
    }

    /**
     * 创建终端升级成功通知
     */
    public static T0108TerminalUpgradeResultNotification createTerminalUpgradeSuccess() {
        return new T0108TerminalUpgradeResultNotification(UPGRADE_TYPE_TERMINAL, UPGRADE_RESULT_SUCCESS);
    }

    /**
     * 创建终端升级失败通知
     */
    public static T0108TerminalUpgradeResultNotification createTerminalUpgradeFailure() {
        return new T0108TerminalUpgradeResultNotification(UPGRADE_TYPE_TERMINAL, UPGRADE_RESULT_FAILURE);
    }

    /**
     * 创建终端升级取消通知
     */
    public static T0108TerminalUpgradeResultNotification createTerminalUpgradeCancel() {
        return new T0108TerminalUpgradeResultNotification(UPGRADE_TYPE_TERMINAL, UPGRADE_RESULT_CANCEL);
    }

    /**
     * 创建IC卡读卡器升级成功通知
     */
    public static T0108TerminalUpgradeResultNotification createIcCardReaderUpgradeSuccess() {
        return new T0108TerminalUpgradeResultNotification(UPGRADE_TYPE_IC_CARD_READER, UPGRADE_RESULT_SUCCESS);
    }

    /**
     * 创建IC卡读卡器升级失败通知
     */
    public static T0108TerminalUpgradeResultNotification createIcCardReaderUpgradeFailure() {
        return new T0108TerminalUpgradeResultNotification(UPGRADE_TYPE_IC_CARD_READER, UPGRADE_RESULT_FAILURE);
    }

    /**
     * 创建IC卡读卡器升级取消通知
     */
    public static T0108TerminalUpgradeResultNotification createIcCardReaderUpgradeCancel() {
        return new T0108TerminalUpgradeResultNotification(UPGRADE_TYPE_IC_CARD_READER, UPGRADE_RESULT_CANCEL);
    }

    /**
     * 创建北斗模块升级成功通知
     */
    public static T0108TerminalUpgradeResultNotification createBeidouModuleUpgradeSuccess() {
        return new T0108TerminalUpgradeResultNotification(UPGRADE_TYPE_BEIDOU_MODULE, UPGRADE_RESULT_SUCCESS);
    }

    /**
     * 创建北斗模块升级失败通知
     */
    public static T0108TerminalUpgradeResultNotification createBeidouModuleUpgradeFailure() {
        return new T0108TerminalUpgradeResultNotification(UPGRADE_TYPE_BEIDOU_MODULE, UPGRADE_RESULT_FAILURE);
    }

    /**
     * 创建北斗模块升级取消通知
     */
    public static T0108TerminalUpgradeResultNotification createBeidouModuleUpgradeCancel() {
        return new T0108TerminalUpgradeResultNotification(UPGRADE_TYPE_BEIDOU_MODULE, UPGRADE_RESULT_CANCEL);
    }

    @Override
    public int getMessageId() {
        return 0x0108;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 升级类型 (1字节)
        buffer.appendByte(upgradeType);

        // 升级结果 (1字节)
        buffer.appendByte(upgradeResult);

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        int index = 0;

        // 升级类型 (1字节)
        upgradeType = body.getByte(index);
        index += 1;

        // 升级结果 (1字节)
        upgradeResult = body.getByte(index);
    }

    /**
     * 获取升级类型描述
     */
    public String getUpgradeTypeDescription() {
        switch (upgradeType) {
            case UPGRADE_TYPE_TERMINAL:
                return "终端";
            case UPGRADE_TYPE_IC_CARD_READER:
                return "道路运输证IC卡读卡器";
            case UPGRADE_TYPE_BEIDOU_MODULE:
                return "北斗卫星定位模块";
            default:
                return "未知类型(" + (upgradeType & 0xFF) + ")";
        }
    }

    /**
     * 获取升级结果描述
     */
    public String getUpgradeResultDescription() {
        switch (upgradeResult) {
            case UPGRADE_RESULT_SUCCESS:
                return "成功";
            case UPGRADE_RESULT_FAILURE:
                return "失败";
            case UPGRADE_RESULT_CANCEL:
                return "取消";
            default:
                return "未知结果(" + (upgradeResult & 0xFF) + ")";
        }
    }

    // Getters and Setters
    public byte getUpgradeType() {
        return upgradeType;
    }

    public void setUpgradeType(byte upgradeType) {
        this.upgradeType = upgradeType;
    }

    public byte getUpgradeResult() {
        return upgradeResult;
    }

    public void setUpgradeResult(byte upgradeResult) {
        this.upgradeResult = upgradeResult;
    }

    @Override
    public String toString() {
        return "T0108TerminalUpgradeResultNotification{" +
                "upgradeType=" + getUpgradeTypeDescription() +
                ", upgradeResult=" + getUpgradeResultDescription() +
                ", header=" + getHeader() +
                '}';
    }
}