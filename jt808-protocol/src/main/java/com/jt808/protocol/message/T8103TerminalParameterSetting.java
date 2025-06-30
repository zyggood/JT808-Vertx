package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import java.util.*;

/**
 * 设置终端参数消息 (0x8103)
 * 平台向终端发送参数设置指令
 */
public class T8103TerminalParameterSetting extends JT808Message {
    
    /** 参数项列表 */
    private List<ParameterItem> parameterItems;
    
    public T8103TerminalParameterSetting() {
        super();
        this.parameterItems = new ArrayList<>();
    }
    
    public T8103TerminalParameterSetting(JT808Header header) {
        super(header);
        this.parameterItems = new ArrayList<>();
    }
    
    @Override
    public int getMessageId() {
        return 0x8103;
    }
    
    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 参数总数 (1字节)
        buffer.appendByte((byte) parameterItems.size());
        
        // 参数项列表
        for (ParameterItem item : parameterItems) {
            // 参数ID (4字节)
            buffer.appendUnsignedInt(item.getParameterId());
            
            // 参数值字节数组
            byte[] valueBytes = item.getValueBytes();
            
            // 参数长度 (1字节)
            buffer.appendByte((byte) valueBytes.length);
            
            // 参数值
            buffer.appendBytes(valueBytes);
        }
        
        return buffer;
    }
    
    @Override
    public void decodeBody(Buffer body) {
        int index = 0;
        parameterItems.clear();
        
        // 参数总数 (1字节)
        int parameterCount = body.getUnsignedByte(index);
        index += 1;
        
        // 解析参数项列表
        for (int i = 0; i < parameterCount; i++) {
            // 参数ID (4字节)
            long parameterId = body.getUnsignedInt(index);
            index += 4;
            
            // 参数长度 (1字节)
            int parameterLength = body.getUnsignedByte(index);
            index += 1;
            
            // 参数值
            byte[] valueBytes = body.getBytes(index, index + parameterLength);
            index += parameterLength;
            
            // 创建参数项
            ParameterItem item = new ParameterItem(parameterId, valueBytes);
            parameterItems.add(item);
        }
    }
    
    /**
     * 添加DWORD类型参数
     */
    public void addDwordParameter(long parameterId, long value) {
        parameterItems.add(ParameterItem.createDwordParameter(parameterId, value));
    }
    
    /**
     * 添加WORD类型参数
     */
    public void addWordParameter(long parameterId, int value) {
        parameterItems.add(ParameterItem.createWordParameter(parameterId, value));
    }
    
    /**
     * 添加BYTE类型参数
     */
    public void addByteParameter(long parameterId, byte value) {
        parameterItems.add(ParameterItem.createByteParameter(parameterId, value));
    }
    
    /**
     * 添加STRING类型参数
     */
    public void addStringParameter(long parameterId, String value) {
        parameterItems.add(ParameterItem.createStringParameter(parameterId, value));
    }
    
    /**
     * 添加字节数组类型参数
     */
    public void addBytesParameter(long parameterId, byte[] value) {
        parameterItems.add(new ParameterItem(parameterId, value));
    }
    
    /**
     * 获取DWORD类型参数值
     */
    public Long getDwordParameter(long parameterId) {
        ParameterItem item = getParameterItem(parameterId);
        return item != null ? item.getDwordValue() : null;
    }
    
    /**
     * 获取WORD类型参数值
     */
    public Integer getWordParameter(long parameterId) {
        ParameterItem item = getParameterItem(parameterId);
        return item != null ? item.getWordValue() : null;
    }
    
    /**
     * 获取BYTE类型参数值
     */
    public Byte getByteParameter(long parameterId) {
        ParameterItem item = getParameterItem(parameterId);
        return item != null ? item.getByteValue() : null;
    }
    
    /**
     * 获取STRING类型参数值
     */
    public String getStringParameter(long parameterId) {
        ParameterItem item = getParameterItem(parameterId);
        return item != null ? item.getStringValue() : null;
    }
    
    /**
     * 获取参数项
     */
    public ParameterItem getParameterItem(long parameterId) {
        return parameterItems.stream()
                .filter(item -> item.getParameterId() == parameterId)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 移除参数
     */
    public boolean removeParameter(long parameterId) {
        return parameterItems.removeIf(item -> item.getParameterId() == parameterId);
    }
    
    /**
     * 清空所有参数
     */
    public void clearParameters() {
        parameterItems.clear();
    }
    
    // Getters and Setters
    public List<ParameterItem> getParameterItems() {
        return new ArrayList<>(parameterItems);
    }
    
    public void setParameterItems(List<ParameterItem> parameterItems) {
        this.parameterItems = new ArrayList<>(parameterItems);
    }
    
    /**
     * 获取参数总数
     */
    public int getParameterCount() {
        return parameterItems.size();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("T8103TerminalParameterSetting{");
        sb.append("parameterCount=").append(parameterItems.size());
        sb.append(", parameters=[");
        for (int i = 0; i < parameterItems.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameterItems.get(i));
        }
        sb.append("], header=").append(getHeader());
        sb.append('}');
        return sb.toString();
    }
    
    /**
     * 终端参数项
     */
    public static class ParameterItem {
        /** 参数ID */
        private long parameterId;
        
        /** 参数值字节数组 */
        private byte[] valueBytes;
        
        public ParameterItem(long parameterId, byte[] valueBytes) {
            this.parameterId = parameterId;
            this.valueBytes = valueBytes != null ? valueBytes.clone() : new byte[0];
        }
        
        /**
         * 创建DWORD类型参数项
         */
        public static ParameterItem createDwordParameter(long parameterId, long value) {
            Buffer buffer = Buffer.buffer();
            buffer.appendUnsignedInt(value);
            return new ParameterItem(parameterId, buffer.getBytes());
        }
        
        /**
         * 创建WORD类型参数项
         */
        public static ParameterItem createWordParameter(long parameterId, int value) {
            Buffer buffer = Buffer.buffer();
            buffer.appendUnsignedShort(value);
            return new ParameterItem(parameterId, buffer.getBytes());
        }
        
        /**
         * 创建BYTE类型参数项
         */
        public static ParameterItem createByteParameter(long parameterId, byte value) {
            return new ParameterItem(parameterId, new byte[]{value});
        }
        
        /**
         * 创建STRING类型参数项
         */
        public static ParameterItem createStringParameter(long parameterId, String value) {
            byte[] bytes = value != null ? value.getBytes() : new byte[0];
            return new ParameterItem(parameterId, bytes);
        }
        
        /**
         * 获取DWORD值
         */
        public Long getDwordValue() {
            if (valueBytes.length >= 4) {
                Buffer buffer = Buffer.buffer(valueBytes);
                return buffer.getUnsignedInt(0);
            }
            return null;
        }
        
        /**
         * 获取WORD值
         */
        public Integer getWordValue() {
            if (valueBytes.length == 2) {
                Buffer buffer = Buffer.buffer(valueBytes);
                return buffer.getUnsignedShort(0);
            }
            return null;
        }
        
        /**
         * 获取BYTE值
         */
        public Byte getByteValue() {
            if (valueBytes.length >= 1) {
                return valueBytes[0];
            }
            return null;
        }
        
        /**
         * 获取STRING值
         */
        public String getStringValue() {
            return new String(valueBytes);
        }
        
        /**
         * 获取参数类型描述
         */
        public String getParameterDescription() {
            return ParameterDefinitions.getParameterDescription(parameterId);
        }
        
        // Getters and Setters
        public long getParameterId() {
            return parameterId;
        }
        
        public void setParameterId(long parameterId) {
            this.parameterId = parameterId;
        }
        
        public byte[] getValueBytes() {
            return valueBytes.clone();
        }
        
        public void setValueBytes(byte[] valueBytes) {
            this.valueBytes = valueBytes != null ? valueBytes.clone() : new byte[0];
        }
        
        /**
         * 获取参数长度
         */
        public int getParameterLength() {
            return valueBytes.length;
        }
        
        @Override
        public String toString() {
            String description = getParameterDescription();
            String valueStr;
            
            // 根据参数类型显示值
            if (valueBytes.length == 4) {
                Long dwordValue = getDwordValue();
                valueStr = dwordValue != null ? String.valueOf(dwordValue) : "null";
            } else if (valueBytes.length == 2) {
                Integer wordValue = getWordValue();
                valueStr = wordValue != null ? String.valueOf(wordValue) : "null";
            } else if (valueBytes.length == 1) {
                Byte byteValue = getByteValue();
                valueStr = byteValue != null ? String.valueOf(byteValue & 0xFF) : "null";
            } else {
                valueStr = getStringValue();
            }
            
            return String.format("ParameterItem{id=0x%04X, length=%d, value=%s, description=%s}", 
                    parameterId, valueBytes.length, valueStr, description);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParameterItem that = (ParameterItem) o;
            return parameterId == that.parameterId && Arrays.equals(valueBytes, that.valueBytes);
        }
        
        @Override
        public int hashCode() {
            int result = Objects.hash(parameterId);
            result = 31 * result + Arrays.hashCode(valueBytes);
            return result;
        }
    }
    
    /**
     * 终端参数定义
     */
    public static class ParameterDefinitions {
        private static final Map<Long, String> PARAMETER_DESCRIPTIONS = new HashMap<>();
        
        static {
            // 通信参数
            PARAMETER_DESCRIPTIONS.put(0x0001L, "终端心跳发送间隔(s)");
            PARAMETER_DESCRIPTIONS.put(0x0002L, "TCP消息应答超时时间(s)");
            PARAMETER_DESCRIPTIONS.put(0x0003L, "TCP消息重传次数");
            PARAMETER_DESCRIPTIONS.put(0x0004L, "UDP消息应答超时时间(s)");
            PARAMETER_DESCRIPTIONS.put(0x0005L, "UDP消息重传次数");
            PARAMETER_DESCRIPTIONS.put(0x0006L, "SMS消息应答超时时间(s)");
            PARAMETER_DESCRIPTIONS.put(0x0007L, "SMS消息重传次数");
            
            // 网络参数
            PARAMETER_DESCRIPTIONS.put(0x0010L, "主服务器APN");
            PARAMETER_DESCRIPTIONS.put(0x0011L, "主服务器无线通信拨号用户名");
            PARAMETER_DESCRIPTIONS.put(0x0012L, "主服务器无线通信拨号密码");
            PARAMETER_DESCRIPTIONS.put(0x0013L, "主服务器地址");
            PARAMETER_DESCRIPTIONS.put(0x0014L, "备份服务器APN");
            PARAMETER_DESCRIPTIONS.put(0x0015L, "备份服务器无线通信拨号用户名");
            PARAMETER_DESCRIPTIONS.put(0x0016L, "备份服务器无线通信拨号密码");
            PARAMETER_DESCRIPTIONS.put(0x0017L, "备份服务器地址");
            PARAMETER_DESCRIPTIONS.put(0x0018L, "服务器TCP端口");
            PARAMETER_DESCRIPTIONS.put(0x0019L, "服务器UDP端口");
            PARAMETER_DESCRIPTIONS.put(0x001AL, "道路运输证IC卡认证主服务器IP地址或域名");
            PARAMETER_DESCRIPTIONS.put(0x001BL, "道路运输证IC卡认证主服务器TCP端口");
            PARAMETER_DESCRIPTIONS.put(0x001CL, "道路运输证IC卡认证主服务器UDP端口");
            PARAMETER_DESCRIPTIONS.put(0x001DL, "道路运输证IC卡认证备份服务器IP地址或域名");
            
            // 位置汇报参数
            PARAMETER_DESCRIPTIONS.put(0x0020L, "位置汇报策略");
            PARAMETER_DESCRIPTIONS.put(0x0021L, "位置汇报方案");
            PARAMETER_DESCRIPTIONS.put(0x0022L, "驾驶员未登录汇报时间间隔(s)");
            PARAMETER_DESCRIPTIONS.put(0x0027L, "休眠时汇报时间间隔(s)");
            PARAMETER_DESCRIPTIONS.put(0x0028L, "紧急报警时汇报时间间隔(s)");
            PARAMETER_DESCRIPTIONS.put(0x0029L, "缺省时间汇报间隔(s)");
            PARAMETER_DESCRIPTIONS.put(0x002CL, "缺省距离汇报间隔(m)");
            PARAMETER_DESCRIPTIONS.put(0x002DL, "驾驶员未登录汇报距离间隔(m)");
            PARAMETER_DESCRIPTIONS.put(0x002EL, "休眠时汇报距离间隔(m)");
            PARAMETER_DESCRIPTIONS.put(0x002FL, "紧急报警时汇报距离间隔(m)");
            PARAMETER_DESCRIPTIONS.put(0x0030L, "拐点补传角度");
            PARAMETER_DESCRIPTIONS.put(0x0031L, "电子围栏半径(m)");
            
            // 电话参数
            PARAMETER_DESCRIPTIONS.put(0x0040L, "监控平台电话号码");
            PARAMETER_DESCRIPTIONS.put(0x0041L, "复位电话号码");
            PARAMETER_DESCRIPTIONS.put(0x0042L, "恢复出厂设置电话号码");
            PARAMETER_DESCRIPTIONS.put(0x0043L, "监控平台SMS电话号码");
            PARAMETER_DESCRIPTIONS.put(0x0044L, "接收终端SMS文本报警号码");
            PARAMETER_DESCRIPTIONS.put(0x0045L, "终端电话接听策略");
            PARAMETER_DESCRIPTIONS.put(0x0046L, "每次最长通话时间(s)");
            PARAMETER_DESCRIPTIONS.put(0x0047L, "当月最长通话时间(s)");
            PARAMETER_DESCRIPTIONS.put(0x0048L, "监听电话号码");
            PARAMETER_DESCRIPTIONS.put(0x0049L, "监管平台特权短信号码");
            
            // 报警参数
            PARAMETER_DESCRIPTIONS.put(0x0050L, "报警屏蔽字");
            PARAMETER_DESCRIPTIONS.put(0x0051L, "报警发送文本SMS开关");
            PARAMETER_DESCRIPTIONS.put(0x0052L, "报警拍摄开关");
            PARAMETER_DESCRIPTIONS.put(0x0053L, "报警拍摄存储标志");
            PARAMETER_DESCRIPTIONS.put(0x0054L, "关键标志");
            PARAMETER_DESCRIPTIONS.put(0x0055L, "最高速度(km/h)");
            PARAMETER_DESCRIPTIONS.put(0x0056L, "超速持续时间(s)");
            PARAMETER_DESCRIPTIONS.put(0x0057L, "连续驾驶时间门限(s)");
            PARAMETER_DESCRIPTIONS.put(0x0058L, "当天累计驾驶时间门限(s)");
            PARAMETER_DESCRIPTIONS.put(0x0059L, "最小休息时间(s)");
            PARAMETER_DESCRIPTIONS.put(0x005AL, "最长停车时间(s)");
            PARAMETER_DESCRIPTIONS.put(0x005BL, "超速报警预警差值(1/10km/h)");
            PARAMETER_DESCRIPTIONS.put(0x005CL, "疲劳驾驶预警差值(s)");
            PARAMETER_DESCRIPTIONS.put(0x005DL, "碰撞报警参数设置");
            PARAMETER_DESCRIPTIONS.put(0x005EL, "侧翻报警参数设置");
            
            // 拍照参数
            PARAMETER_DESCRIPTIONS.put(0x0064L, "定时拍照控制");
            PARAMETER_DESCRIPTIONS.put(0x0065L, "定距拍照控制");
            
            // 图像参数
            PARAMETER_DESCRIPTIONS.put(0x0070L, "图像/视频质量");
            PARAMETER_DESCRIPTIONS.put(0x0071L, "亮度");
            PARAMETER_DESCRIPTIONS.put(0x0072L, "对比度");
            PARAMETER_DESCRIPTIONS.put(0x0073L, "饱和度");
            PARAMETER_DESCRIPTIONS.put(0x0074L, "色度");
            
            // 车辆参数
            PARAMETER_DESCRIPTIONS.put(0x0080L, "车辆里程表读数(1/10km)");
            PARAMETER_DESCRIPTIONS.put(0x0081L, "车辆所在的省域ID");
            PARAMETER_DESCRIPTIONS.put(0x0082L, "车辆所在的市域ID");
            PARAMETER_DESCRIPTIONS.put(0x0083L, "公安交通管理部门颁发的机动车号牌");
            PARAMETER_DESCRIPTIONS.put(0x0084L, "车牌颜色");
            
            // GNSS参数
            PARAMETER_DESCRIPTIONS.put(0x0090L, "GNSS定位模式");
            PARAMETER_DESCRIPTIONS.put(0x0091L, "GNSS波特率");
            PARAMETER_DESCRIPTIONS.put(0x0092L, "GNSS模块详细定位数据输出频率");
            PARAMETER_DESCRIPTIONS.put(0x0093L, "GNSS模块详细定位数据采集频率(s)");
            PARAMETER_DESCRIPTIONS.put(0x0094L, "GNSS模块详细定位数据上传方式");
            PARAMETER_DESCRIPTIONS.put(0x0095L, "GNSS模块详细定位数据上传设置");
            
            // CAN总线参数
            PARAMETER_DESCRIPTIONS.put(0x0100L, "CAN总线通道1采集时间间隔(ms)");
            PARAMETER_DESCRIPTIONS.put(0x0101L, "CAN总线通道1上传时间间隔(s)");
            PARAMETER_DESCRIPTIONS.put(0x0102L, "CAN总线通道2采集时间间隔(ms)");
            PARAMETER_DESCRIPTIONS.put(0x0103L, "CAN总线通道2上传时间间隔(s)");
            PARAMETER_DESCRIPTIONS.put(0x0110L, "CAN总线ID单独采集设置");
        }
        
        /**
         * 获取参数描述
         */
        public static String getParameterDescription(long parameterId) {
            String description = PARAMETER_DESCRIPTIONS.get(parameterId);
            if (description != null) {
                return description;
            }
            
            // 处理范围参数
            if (parameterId >= 0x0111L && parameterId <= 0x01FFL) {
                return "CAN总线ID单独采集设置(" + String.format("0x%04X", parameterId) + ")";
            } else if (parameterId >= 0xF000L && parameterId <= 0xFFFFL) {
                return "用户自定义参数(" + String.format("0x%04X", parameterId) + ")";
            } else {
                return "未知参数(" + String.format("0x%04X", parameterId) + ")";
            }
        }
        
        /**
         * 获取所有已定义的参数ID
         */
        public static Set<Long> getAllDefinedParameterIds() {
            return new HashSet<>(PARAMETER_DESCRIPTIONS.keySet());
        }
    }
}