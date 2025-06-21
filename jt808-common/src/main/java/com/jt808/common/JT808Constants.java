package com.jt808.common;

/**
 * JT808协议常量定义
 */
public final class JT808Constants {
    
    private JT808Constants() {
        // 工具类，禁止实例化
    }
    
    /** 协议版本 */
    public static final class ProtocolVersion {
        public static final byte VERSION_2011 = 0x00;
        public static final byte VERSION_2013 = 0x01;
        public static final byte VERSION_2019 = 0x02;
    }
    
    /** 消息ID */
    public static final class MessageId {
        // 终端通用应答
        public static final int TERMINAL_COMMON_RESPONSE = 0x0001;
        // 平台通用应答
        public static final int PLATFORM_COMMON_RESPONSE = 0x8001;
        // 终端心跳
        public static final int TERMINAL_HEARTBEAT = 0x0002;
        // 查询服务器时间请求
        public static final int QUERY_SERVER_TIME_REQUEST = 0x0004;
        // 查询服务器时间应答
        public static final int QUERY_SERVER_TIME_RESPONSE = 0x8004;
        // 服务器补传分包请求
        public static final int SERVER_RESEND_SUBPACKAGE_REQUEST = 0x8003;
        // 终端补传分包请求
        public static final int TERMINAL_RESEND_SUBPACKAGE_REQUEST = 0x0005;
        // 终端注册
        public static final int TERMINAL_REGISTER = 0x0100;
        // 终端注册应答
        public static final int TERMINAL_REGISTER_RESPONSE = 0x8100;
        // 终端注销
        public static final int TERMINAL_LOGOUT = 0x0003;
        // 终端鉴权
        public static final int TERMINAL_AUTH = 0x0102;
        // 设置终端参数
        public static final int SET_TERMINAL_PARAMS = 0x8103;
        // 查询终端参数
        public static final int QUERY_TERMINAL_PARAMS = 0x8104;
        // 查询终端参数应答
        public static final int QUERY_TERMINAL_PARAMS_RESPONSE = 0x0104;
        // 终端控制
        public static final int TERMINAL_CONTROL = 0x8105;
        // 查询终端属性
        public static final int QUERY_TERMINAL_ATTRIBUTES = 0x8107;
        // 查询终端属性应答
        public static final int QUERY_TERMINAL_ATTRIBUTES_RESPONSE = 0x0107;
        // 下发终端升级包
        public static final int ISSUE_TERMINAL_UPGRADE_PACKAGE = 0x8108;
        // 终端升级结果应答
        public static final int TERMINAL_UPGRADE_RESULT = 0x0108;
        // 位置信息汇报 
        public static final int LOCATION_REPORT = 0x0200;
        // 位置信息查询
        public static final int LOCATION_QUERY = 0x8201;
        // 位置信息查询应答
        public static final int LOCATION_QUERY_RESPONSE = 0x0201;
        // 临时位置跟踪控制
        public static final int TEMP_LOCATION_TRACKING = 0x8202;
        // 人工确认报警消息
        public static final int MANUAL_CONFIRM_ALARM = 0x8203;
        // 链路检测
        public static final int LINK_TEST = 0x8204;
        // 文本信息下发
        public static final int TEXT_MESSAGE = 0x8300;
        // 电话回拨
        public static final int PHONE_CALLBACK = 0x8400;
        // 设置电话本
        public static final int SET_PHONE_BOOK = 0x8401;
        // 车辆控制
        public static final int VEHICLE_CONTROL = 0x8500;
        // 设置圆形区域
        public static final int SET_CIRCLE_REGION = 0x8600;
        // 删除圆形区域
        public static final int DELETE_CIRCLE_REGION = 0x8601;
        // 设置矩形区域
        public static final int SET_RECTANGLE_REGION = 0x8602;
        // 删除矩形区域
        public static final int DELETE_RECTANGLE_REGION = 0x8603;
        // 设置多边形区域
        public static final int SET_POLYGON_REGION = 0x8604;
        // 删除多边形区域
        public static final int DELETE_POLYGON_REGION = 0x8605;
        // 设置路线
        public static final int SET_ROUTE = 0x8606;
        // 删除路线
        public static final int DELETE_ROUTE = 0x8607;
        // 查询区域或线路数据
        public static final int QUERY_REGION_OR_ROUTE = 0x8608;
        // 查询区域或线路数据应答
        public static final int QUERY_REGION_OR_ROUTE_RESPONSE = 0x0608;
        // 行驶记录数据采集命令
        public static final int DRIVING_RECORD_DATA_COLLECTION = 0x8700;
        // 行驶记录数据上传
        public static final int DRIVING_RECORD_DATA_UPLOAD = 0x0700;
        // 行驶记录参数下传
        public static final int DRIVING_RECORD_PARAMS_DOWNLOAD = 0x8702;
        // 电子运单上报

        // 上报驾驶员身份信息请求

        // 驾驶员身份信息采集上报

        // 定位数据批量上传

    }
    
    /** 消息属性 */
    public static final class MessageProperty {
        // 消息体长度掩码
        public static final int BODY_LENGTH_MASK = 0x03FF;
        // 数据加密方式掩码
        public static final int ENCRYPT_MASK = 0x1C00;
        // 分包标志掩码
        public static final int SUBPACKAGE_MASK = 0x2000;
        // 保留位掩码
        public static final int RESERVED_MASK = 0xC000;
    }
    
    /** 应答结果 */
    public static final class ResponseResult {
        public static final byte SUCCESS = 0x00;
        public static final byte FAILURE = 0x01;
        public static final byte MESSAGE_ERROR = 0x02;
        public static final byte NOT_SUPPORT = 0x03;
        public static final byte ALARM_CONFIRM = 0x04;
    }
    
    /** 终端注册结果 */
    public static final class RegisterResult {
        public static final byte SUCCESS = 0x00;
        public static final byte VEHICLE_REGISTERED = 0x01;
        public static final byte VEHICLE_NOT_IN_DATABASE = 0x02;
        public static final byte TERMINAL_REGISTERED = 0x03;
        public static final byte TERMINAL_NOT_IN_DATABASE = 0x04;
    }
    
    /** 协议标识 */
    public static final byte PROTOCOL_FLAG = 0x7E;
    
    /** 转义字符 */
    public static final byte ESCAPE_FLAG = 0x7D;
    public static final byte ESCAPE_7E = 0x02;
    public static final byte ESCAPE_7D = 0x01;
    
    /** 默认端口 */
    public static final int DEFAULT_TCP_PORT = 7611;
    public static final int DEFAULT_UDP_PORT = 7612;
    public static final int DEFAULT_HTTP_PORT = 8080;
    
    /** 编码格式 */
    public static final String CHARSET_GBK = "GBK";
    public static final String CHARSET_UTF8 = "UTF-8";
}