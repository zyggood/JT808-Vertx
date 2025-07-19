package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 0x0702 驾驶员身份信息采集上报消息
 * 终端→平台
 * 
 * 终端从业资格证IC卡插入或拔出后，自动触发本指令。收到0x8702指令后，使用本指令应答。
 * 
 * 消息体包含：
 * - 状态 (BYTE): 0x01-IC卡插入(驾驶员上班), 0x02-IC卡拔出(驾驶员下班)
 * - 时间 (BCD[6]): 插卡/拔卡时间，YY-MM-DD-hh-mm-ss
 * - IC卡读取结果 (BYTE): 状态为0x01时有效
 * - 驾驶员姓名长度 (BYTE): IC卡读取结果为0x00时有效
 * - 驾驶员姓名 (STRING): IC卡读取结果为0x00时有效
 * - 从业资格证编码 (STRING): 长度20位，不足补0x00，IC卡读取结果为0x00时有效
 * - 发证机构名称长度 (BYTE): IC卡读取结果为0x00时有效
 * - 发证机构名称 (STRING): IC卡读取结果为0x00时有效
 * - 证件有效期 (BCD[4]): YYYYMMDD，IC卡读取结果为0x00时有效
 * 
 * @author JT808-Vertx
 */
public class T0702DriverIdentityInfoReport extends JT808Message {
    
    /** 消息ID */
    public static final int MESSAGE_ID = 0x0702;
    
    // 状态常量
    /** IC卡插入（驾驶员上班） */
    public static final byte STATUS_CARD_INSERT = 0x01;
    /** IC卡拔出（驾驶员下班） */
    public static final byte STATUS_CARD_REMOVE = 0x02;
    
    // IC卡读取结果常量
    /** IC卡读卡成功 */
    public static final byte READ_SUCCESS = 0x00;
    /** 读卡失败，原因为卡片密钥认证未通过 */
    public static final byte READ_FAIL_AUTH = 0x01;
    /** 读卡失败，原因为卡片已被锁定 */
    public static final byte READ_FAIL_LOCKED = 0x02;
    /** 读卡失败，原因为卡片被拔出 */
    public static final byte READ_FAIL_REMOVED = 0x03;
    /** 读卡失败，原因为数据校验错误 */
    public static final byte READ_FAIL_CHECKSUM = 0x04;
    
    /** 状态 */
    private byte status;
    
    /** 插卡/拔卡时间 */
    private LocalDateTime operationTime;
    
    /** IC卡读取结果 - 状态为0x01时有效 */
    private Byte icCardReadResult;
    
    /** 驾驶员姓名长度 - IC卡读取结果为0x00时有效 */
    private Byte driverNameLength;
    
    /** 驾驶员姓名 - IC卡读取结果为0x00时有效 */
    private String driverName;
    
    /** 从业资格证编码 - 长度20位，不足补0x00，IC卡读取结果为0x00时有效 */
    private String qualificationCode;
    
    /** 发证机构名称长度 - IC卡读取结果为0x00时有效 */
    private Byte issuerNameLength;
    
    /** 发证机构名称 - IC卡读取结果为0x00时有效 */
    private String issuerName;
    
    /** 证件有效期 - YYYYMMDD，IC卡读取结果为0x00时有效 */
    private LocalDateTime validityPeriod;
    
    /**
     * 默认构造函数
     */
    public T0702DriverIdentityInfoReport() {
        super();
    }
    
    /**
     * 构造函数 - IC卡拔出
     * 
     * @param operationTime 拔卡时间
     */
    public T0702DriverIdentityInfoReport(LocalDateTime operationTime) {
        this.status = STATUS_CARD_REMOVE;
        this.operationTime = operationTime;
    }
    
    /**
     * 构造函数 - IC卡插入但读卡失败
     * 
     * @param operationTime 插卡时间
     * @param readResult 读卡结果
     */
    public T0702DriverIdentityInfoReport(LocalDateTime operationTime, byte readResult) {
        this.status = STATUS_CARD_INSERT;
        this.operationTime = operationTime;
        this.icCardReadResult = readResult;
    }
    
    /**
     * 构造函数 - IC卡插入且读卡成功
     * 
     * @param operationTime 插卡时间
     * @param driverName 驾驶员姓名
     * @param qualificationCode 从业资格证编码
     * @param issuerName 发证机构名称
     * @param validityPeriod 证件有效期
     */
    public T0702DriverIdentityInfoReport(LocalDateTime operationTime, String driverName, 
                                        String qualificationCode, String issuerName, 
                                        LocalDateTime validityPeriod) {
        this.status = STATUS_CARD_INSERT;
        this.operationTime = operationTime;
        this.icCardReadResult = READ_SUCCESS;
        this.driverName = driverName;
        this.qualificationCode = qualificationCode;
        this.issuerName = issuerName;
        this.validityPeriod = validityPeriod;
        
        // 自动计算长度
        if (driverName != null) {
            this.driverNameLength = (byte) driverName.getBytes(Charset.forName("GBK")).length;
        }
        if (issuerName != null) {
            this.issuerNameLength = (byte) issuerName.getBytes(Charset.forName("GBK")).length;
        }
    }
    
    /**
     * 创建IC卡拔出消息
     * 
     * @param operationTime 拔卡时间
     * @return 消息实例
     */
    public static T0702DriverIdentityInfoReport createCardRemove(LocalDateTime operationTime) {
        return new T0702DriverIdentityInfoReport(operationTime);
    }
    
    /**
     * 创建IC卡插入但读卡失败消息
     * 
     * @param operationTime 插卡时间
     * @param readResult 读卡结果
     * @return 消息实例
     */
    public static T0702DriverIdentityInfoReport createCardInsertFailed(LocalDateTime operationTime, byte readResult) {
        return new T0702DriverIdentityInfoReport(operationTime, readResult);
    }
    
    /**
     * 创建IC卡插入且读卡成功消息
     * 
     * @param operationTime 插卡时间
     * @param driverName 驾驶员姓名
     * @param qualificationCode 从业资格证编码
     * @param issuerName 发证机构名称
     * @param validityPeriod 证件有效期
     * @return 消息实例
     */
    public static T0702DriverIdentityInfoReport createCardInsertSuccess(LocalDateTime operationTime, 
                                                                        String driverName, 
                                                                        String qualificationCode, 
                                                                        String issuerName, 
                                                                        LocalDateTime validityPeriod) {
        return new T0702DriverIdentityInfoReport(operationTime, driverName, qualificationCode, issuerName, validityPeriod);
    }
    
    /**
     * 创建空消息实例
     * 
     * @return 消息实例
     */
    public static T0702DriverIdentityInfoReport create() {
        return new T0702DriverIdentityInfoReport();
    }
    
    @Override
    public int getMessageId() {
        return MESSAGE_ID;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 状态 (BYTE)
        buffer.appendByte(status);
        
        // 时间 (BCD[6])
        buffer.appendBuffer(encodeBcdTime(operationTime));
        
        // 以下字段在状态为0x01时才有效
        if (status == STATUS_CARD_INSERT) {
            // IC卡读取结果 (BYTE)
            buffer.appendByte(icCardReadResult != null ? icCardReadResult : READ_FAIL_REMOVED);
            
            // 以下字段在IC卡读取结果等于0x00时才有效
            if (icCardReadResult != null && icCardReadResult == READ_SUCCESS) {
                // 驾驶员姓名长度 (BYTE)
                byte nameLength = driverNameLength != null ? driverNameLength : 0;
                buffer.appendByte(nameLength);
                
                // 驾驶员姓名 (STRING)
                if (driverName != null && !driverName.isEmpty()) {
                    buffer.appendBuffer(Buffer.buffer(driverName.getBytes(Charset.forName("GBK"))));
                }
                
                // 从业资格证编码 (STRING) - 长度20位，不足补0x00
                byte[] qualificationBytes = new byte[20];
                if (qualificationCode != null && !qualificationCode.isEmpty()) {
                    byte[] codeBytes = qualificationCode.getBytes(Charset.forName("UTF-8"));
                    System.arraycopy(codeBytes, 0, qualificationBytes, 0, Math.min(codeBytes.length, 20));
                }
                buffer.appendBytes(qualificationBytes);
                
                // 发证机构名称长度 (BYTE)
                byte issuerLength = issuerNameLength != null ? issuerNameLength : 0;
                buffer.appendByte(issuerLength);
                
                // 发证机构名称 (STRING)
                if (issuerName != null && !issuerName.isEmpty()) {
                    buffer.appendBuffer(Buffer.buffer(issuerName.getBytes(Charset.forName("GBK"))));
                }
                
                // 证件有效期 (BCD[4]) - YYYYMMDD
                buffer.appendBuffer(encodeBcdDate(validityPeriod));
            }
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer.length() < 7) { // 最小长度：1+6 = 7字节
            throw new IllegalArgumentException("驾驶员身份信息采集上报消息体长度不足，至少需要7字节，实际: " + buffer.length() + " 字节");
        }
        
        int index = 0;
        
        // 状态 (BYTE)
        status = buffer.getByte(index++);
        
        // 时间 (BCD[6])
        operationTime = decodeBcdTime(buffer, index);
        index += 6;
        
        // 以下字段在状态为0x01时才有效
        if (status == STATUS_CARD_INSERT) {
            if (index >= buffer.length()) {
                throw new IllegalArgumentException("IC卡插入状态下缺少IC卡读取结果字段");
            }
            
            // IC卡读取结果 (BYTE)
            icCardReadResult = buffer.getByte(index++);
            
            // 以下字段在IC卡读取结果等于0x00时才有效
            if (icCardReadResult == READ_SUCCESS) {
                if (index >= buffer.length()) {
                    throw new IllegalArgumentException("IC卡读取成功状态下缺少驾驶员姓名长度字段");
                }
                
                // 驾驶员姓名长度 (BYTE)
                driverNameLength = buffer.getByte(index++);
                
                // 驾驶员姓名 (STRING)
                if (driverNameLength > 0) {
                    if (index + driverNameLength > buffer.length()) {
                        throw new IllegalArgumentException("驾驶员姓名数据不足");
                    }
                    byte[] nameBytes = buffer.getBytes(index, index + driverNameLength);
                    driverName = new String(nameBytes, Charset.forName("GBK"));
                    index += driverNameLength;
                }
                
                // 从业资格证编码 (STRING) - 长度20位
                if (index + 20 > buffer.length()) {
                    throw new IllegalArgumentException("从业资格证编码数据不足");
                }
                byte[] qualificationBytes = buffer.getBytes(index, index + 20);
                // 去除尾部的0x00填充
                int actualLength = 20;
                for (int i = 19; i >= 0; i--) {
                    if (qualificationBytes[i] != 0) {
                        actualLength = i + 1;
                        break;
                    }
                }
                if (actualLength > 0) {
                    byte[] actualBytes = new byte[actualLength];
                    System.arraycopy(qualificationBytes, 0, actualBytes, 0, actualLength);
                    qualificationCode = new String(actualBytes, Charset.forName("UTF-8"));
                }
                index += 20;
                
                // 发证机构名称长度 (BYTE)
                if (index >= buffer.length()) {
                    throw new IllegalArgumentException("缺少发证机构名称长度字段");
                }
                issuerNameLength = buffer.getByte(index++);
                
                // 发证机构名称 (STRING)
                if (issuerNameLength > 0) {
                    if (index + issuerNameLength > buffer.length()) {
                        throw new IllegalArgumentException("发证机构名称数据不足");
                    }
                    byte[] issuerBytes = buffer.getBytes(index, index + issuerNameLength);
                    issuerName = new String(issuerBytes, Charset.forName("GBK"));
                    index += issuerNameLength;
                }
                
                // 证件有效期 (BCD[4]) - YYYYMMDD
                if (index + 4 > buffer.length()) {
                    throw new IllegalArgumentException("证件有效期数据不足");
                }
                validityPeriod = decodeBcdDate(buffer, index);
            }
        }
    }
    
    /**
     * 编码BCD时间 (YY-MM-DD-HH-MM-SS)
     * 
     * @param time 时间
     * @return 编码后的Buffer
     */
    private Buffer encodeBcdTime(LocalDateTime time) {
        Buffer buffer = Buffer.buffer();
        if (time != null) {
            String timeStr = time.format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
            for (int i = 0; i < timeStr.length(); i += 2) {
                int high = Character.getNumericValue(timeStr.charAt(i));
                int low = Character.getNumericValue(timeStr.charAt(i + 1));
                buffer.appendByte((byte) ((high << 4) | low));
            }
        } else {
            // 如果时间为null，填充6个0字节
            buffer.appendBytes(new byte[6]);
        }
        return buffer;
    }
    
    /**
     * 解码BCD时间 (YY-MM-DD-HH-MM-SS)
     * 
     * @param buffer 数据缓冲区
     * @param index 起始索引
     * @return 解码后的时间
     */
    private LocalDateTime decodeBcdTime(Buffer buffer, int index) {
        StringBuilder timeStr = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            byte b = buffer.getByte(index + i);
            int high = (b >> 4) & 0x0F;
            int low = b & 0x0F;
            timeStr.append(high).append(low);
        }
        
        try {
            return LocalDateTime.parse("20" + timeStr.toString(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 编码BCD日期 (YYYY-MM-DD)
     * 
     * @param date 日期
     * @return 编码后的Buffer
     */
    private Buffer encodeBcdDate(LocalDateTime date) {
        Buffer buffer = Buffer.buffer();
        if (date != null) {
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            for (int i = 0; i < dateStr.length(); i += 2) {
                int high = Character.getNumericValue(dateStr.charAt(i));
                int low = Character.getNumericValue(dateStr.charAt(i + 1));
                buffer.appendByte((byte) ((high << 4) | low));
            }
        } else {
            // 如果日期为null，填充4个0字节
            buffer.appendBytes(new byte[4]);
        }
        return buffer;
    }
    
    /**
     * 解码BCD日期 (YYYY-MM-DD)
     * 
     * @param buffer 数据缓冲区
     * @param index 起始索引
     * @return 解码后的日期
     */
    private LocalDateTime decodeBcdDate(Buffer buffer, int index) {
        StringBuilder dateStr = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            byte b = buffer.getByte(index + i);
            int high = (b >> 4) & 0x0F;
            int low = b & 0x0F;
            dateStr.append(high).append(low);
        }
        
        try {
            return LocalDateTime.parse(dateStr.toString() + "000000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 检查是否为IC卡插入状态
     * 
     * @return true-IC卡插入，false-IC卡拔出
     */
    public boolean isCardInsert() {
        return status == STATUS_CARD_INSERT;
    }
    
    /**
     * 检查是否为IC卡拔出状态
     * 
     * @return true-IC卡拔出，false-IC卡插入
     */
    public boolean isCardRemove() {
        return status == STATUS_CARD_REMOVE;
    }
    
    /**
     * 检查IC卡读取是否成功
     * 
     * @return true-读取成功，false-读取失败或不适用
     */
    public boolean isReadSuccess() {
        return icCardReadResult != null && icCardReadResult == READ_SUCCESS;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getStatusDescription() {
        switch (status) {
            case STATUS_CARD_INSERT:
                return "IC卡插入（驾驶员上班）";
            case STATUS_CARD_REMOVE:
                return "IC卡拔出（驾驶员下班）";
            default:
                return "未知状态(" + (status & 0xFF) + ")";
        }
    }
    
    /**
     * 获取IC卡读取结果描述
     * 
     * @return 读取结果描述
     */
    public String getReadResultDescription() {
        if (icCardReadResult == null) {
            return "不适用";
        }
        
        switch (icCardReadResult) {
            case READ_SUCCESS:
                return "IC卡读卡成功";
            case READ_FAIL_AUTH:
                return "读卡失败，原因为卡片密钥认证未通过";
            case READ_FAIL_LOCKED:
                return "读卡失败，原因为卡片已被锁定";
            case READ_FAIL_REMOVED:
                return "读卡失败，原因为卡片被拔出";
            case READ_FAIL_CHECKSUM:
                return "读卡失败，原因为数据校验错误";
            default:
                return "未知读取结果(" + (icCardReadResult & 0xFF) + ")";
        }
    }
    
    /**
     * 获取消息描述
     * 
     * @return 消息描述
     */
    public String getMessageDescription() {
        return "驾驶员身份信息采集上报";
    }
    
    // Getters and Setters
    public byte getStatus() {
        return status;
    }
    
    public void setStatus(byte status) {
        this.status = status;
    }
    
    public LocalDateTime getOperationTime() {
        return operationTime;
    }
    
    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }
    
    public Byte getIcCardReadResult() {
        return icCardReadResult;
    }
    
    public void setIcCardReadResult(Byte icCardReadResult) {
        this.icCardReadResult = icCardReadResult;
    }
    
    public Byte getDriverNameLength() {
        return driverNameLength;
    }
    
    public void setDriverNameLength(Byte driverNameLength) {
        this.driverNameLength = driverNameLength;
    }
    
    public String getDriverName() {
        return driverName;
    }
    
    public void setDriverName(String driverName) {
        this.driverName = driverName;
        if (driverName != null) {
            this.driverNameLength = (byte) driverName.getBytes(Charset.forName("GBK")).length;
        } else {
            this.driverNameLength = null;
        }
    }
    
    public String getQualificationCode() {
        return qualificationCode;
    }
    
    public void setQualificationCode(String qualificationCode) {
        this.qualificationCode = qualificationCode;
    }
    
    public Byte getIssuerNameLength() {
        return issuerNameLength;
    }
    
    public void setIssuerNameLength(Byte issuerNameLength) {
        this.issuerNameLength = issuerNameLength;
    }
    
    public String getIssuerName() {
        return issuerName;
    }
    
    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
        if (issuerName != null) {
            this.issuerNameLength = (byte) issuerName.getBytes(Charset.forName("GBK")).length;
        } else {
            this.issuerNameLength = null;
        }
    }
    
    public LocalDateTime getValidityPeriod() {
        return validityPeriod;
    }
    
    public void setValidityPeriod(LocalDateTime validityPeriod) {
        this.validityPeriod = validityPeriod;
    }
    
    @Override
    public String toString() {
        return "T0702DriverIdentityInfoReport{" +
                "messageId=0x" + Integer.toHexString(getMessageId()).toUpperCase() +
                ", status=" + (status & 0xFF) +
                ", statusDescription='" + getStatusDescription() + '\'' +
                ", operationTime=" + operationTime +
                ", icCardReadResult=" + (icCardReadResult != null ? (icCardReadResult & 0xFF) : "null") +
                ", readResultDescription='" + getReadResultDescription() + '\'' +
                ", driverName='" + driverName + '\'' +
                ", qualificationCode='" + qualificationCode + '\'' +
                ", issuerName='" + issuerName + '\'' +
                ", validityPeriod=" + validityPeriod +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        T0702DriverIdentityInfoReport that = (T0702DriverIdentityInfoReport) o;
        
        return status == that.status &&
                Objects.equals(operationTime, that.operationTime) &&
                Objects.equals(icCardReadResult, that.icCardReadResult) &&
                Objects.equals(driverNameLength, that.driverNameLength) &&
                Objects.equals(driverName, that.driverName) &&
                Objects.equals(qualificationCode, that.qualificationCode) &&
                Objects.equals(issuerNameLength, that.issuerNameLength) &&
                Objects.equals(issuerName, that.issuerName) &&
                Objects.equals(validityPeriod, that.validityPeriod);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(status, operationTime, icCardReadResult, driverNameLength, 
                           driverName, qualificationCode, issuerNameLength, issuerName, validityPeriod);
    }
}