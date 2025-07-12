package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * 下发终端升级包消息 (0x8108)
 * 平台向终端发送升级包数据
 */
public class T8108TerminalUpgradePackage extends JT808Message {

    // 升级类型常量定义
    public static final byte UPGRADE_TYPE_TERMINAL = 0x00;           // 终端
    public static final byte UPGRADE_TYPE_IC_CARD_READER = 0x12;     // 道路运输证IC卡读卡器
    public static final byte UPGRADE_TYPE_BEIDOU_MODULE = 0x52;      // 北斗卫星定位模块
    /**
     * 升级类型
     */
    private byte upgradeType;
    /**
     * 制造商ID (5字节)
     */
    private byte[] manufacturerId;
    /**
     * 版本号长度
     */
    private byte versionLength;
    /**
     * 版本号
     */
    private String version;
    /**
     * 升级数据包长度
     */
    private int upgradeDataLength;
    /**
     * 升级数据包
     */
    private byte[] upgradeData;

    public T8108TerminalUpgradePackage() {
        super();
        this.manufacturerId = new byte[5];
    }

    public T8108TerminalUpgradePackage(JT808Header header) {
        super(header);
        this.manufacturerId = new byte[5];
    }

    /**
     * 构造终端升级包消息
     *
     * @param upgradeType    升级类型
     * @param manufacturerId 制造商ID (5字节)
     * @param version        版本号
     * @param upgradeData    升级数据包
     */
    public T8108TerminalUpgradePackage(byte upgradeType, byte[] manufacturerId, String version, byte[] upgradeData) {
        this();
        this.upgradeType = upgradeType;
        if (manufacturerId != null && manufacturerId.length == 5) {
            System.arraycopy(manufacturerId, 0, this.manufacturerId, 0, 5);
        }
        this.version = version;
        this.versionLength = (byte) (version != null ? version.getBytes().length : 0);
        this.upgradeData = upgradeData;
        this.upgradeDataLength = upgradeData != null ? upgradeData.length : 0;
    }

    /**
     * 创建终端升级包消息
     */
    public static T8108TerminalUpgradePackage createTerminalUpgrade(byte[] manufacturerId, String version, byte[] upgradeData) {
        return new T8108TerminalUpgradePackage(UPGRADE_TYPE_TERMINAL, manufacturerId, version, upgradeData);
    }

    /**
     * 创建IC卡读卡器升级包消息
     */
    public static T8108TerminalUpgradePackage createIcCardReaderUpgrade(byte[] manufacturerId, String version, byte[] upgradeData) {
        return new T8108TerminalUpgradePackage(UPGRADE_TYPE_IC_CARD_READER, manufacturerId, version, upgradeData);
    }

    /**
     * 创建北斗模块升级包消息
     */
    public static T8108TerminalUpgradePackage createBeidouModuleUpgrade(byte[] manufacturerId, String version, byte[] upgradeData) {
        return new T8108TerminalUpgradePackage(UPGRADE_TYPE_BEIDOU_MODULE, manufacturerId, version, upgradeData);
    }

    @Override
    public int getMessageId() {
        return 0x8108;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 升级类型 (1字节)
        buffer.appendByte(upgradeType);

        // 制造商ID (5字节)
        buffer.appendBytes(manufacturerId);

        // 版本号长度 (1字节)
        buffer.appendByte(versionLength);

        // 版本号
        if (version != null && !version.isEmpty()) {
            buffer.appendBytes(version.getBytes());
        }

        // 升级数据包长度 (4字节)
        buffer.appendUnsignedInt(upgradeDataLength);

        // 升级数据包
        if (upgradeData != null && upgradeData.length > 0) {
            buffer.appendBytes(upgradeData);
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        int index = 0;

        // 升级类型 (1字节)
        upgradeType = body.getByte(index);
        index += 1;

        // 制造商ID (5字节)
        manufacturerId = body.getBytes(index, index + 5);
        index += 5;

        // 版本号长度 (1字节)
        versionLength = body.getByte(index);
        index += 1;

        // 版本号
        if (versionLength > 0) {
            byte[] versionBytes = body.getBytes(index, index + versionLength);
            version = new String(versionBytes);
            index += versionLength;
        }

        // 升级数据包长度 (4字节)
        upgradeDataLength = (int) body.getUnsignedInt(index);
        index += 4;

        // 升级数据包
        if (upgradeDataLength > 0 && index < body.length()) {
            int dataLength = Math.min(upgradeDataLength, body.length() - index);
            upgradeData = body.getBytes(index, index + dataLength);
        }
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

    // Getters and Setters
    public byte getUpgradeType() {
        return upgradeType;
    }

    public void setUpgradeType(byte upgradeType) {
        this.upgradeType = upgradeType;
    }

    public byte[] getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(byte[] manufacturerId) {
        if (manufacturerId != null && manufacturerId.length == 5) {
            System.arraycopy(manufacturerId, 0, this.manufacturerId, 0, 5);
        }
    }

    public byte getVersionLength() {
        return versionLength;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
        this.versionLength = (byte) (version != null ? version.getBytes().length : 0);
    }

    public int getUpgradeDataLength() {
        return upgradeDataLength;
    }

    public byte[] getUpgradeData() {
        return upgradeData;
    }

    public void setUpgradeData(byte[] upgradeData) {
        this.upgradeData = upgradeData;
        this.upgradeDataLength = upgradeData != null ? upgradeData.length : 0;
    }

    @Override
    public String toString() {
        return "T8108TerminalUpgradePackage{" +
                "upgradeType=" + getUpgradeTypeDescription() +
                ", manufacturerId=" + java.util.Arrays.toString(manufacturerId) +
                ", version='" + version + '\'' +
                ", upgradeDataLength=" + upgradeDataLength +
                ", upgradeDataSize=" + (upgradeData != null ? upgradeData.length : 0) +
                ", header=" + getHeader() +
                '}';
    }
}