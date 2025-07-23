package com.jt808.protocol.constants;

/**
 * JT808协议消息类型常量定义
 * 按照消息类型分组管理所有消息ID
 */
public final class MessageTypes {

    private MessageTypes() {
        // 工具类，禁止实例化
    }

    /**
     * 终端消息类型 (0x0xxx)
     */
    public static final class Terminal {
        /** 终端通用应答 */
        public static final int COMMON_RESPONSE = 0x0001;
        /** 终端心跳 */
        public static final int HEARTBEAT = 0x0002;
        /** 终端注册 */
        public static final int REGISTER = 0x0100;
        /** 终端鉴权 */
        public static final int AUTH = 0x0102;
        /** 查询终端参数应答 */
        public static final int QUERY_PARAMETERS_RESPONSE = 0x0104;
        /** 查询终端属性应答 */
        public static final int QUERY_PROPERTY_RESPONSE = 0x0107;
        /** 终端升级结果通知 */
        public static final int UPGRADE_RESULT_NOTIFICATION = 0x0108;
        /** 位置信息汇报 */
        public static final int LOCATION_REPORT = 0x0200;
        /** 位置信息查询应答 */
        public static final int POSITION_INFO_QUERY_RESPONSE = 0x0201;
        /** 事件报告 */
        public static final int EVENT_REPORT = 0x0301;
        /** 信息点播取消 */
        public static final int INFO_DEMAND_CANCEL = 0x0303;
        /** 车辆控制应答 */
        public static final int VEHICLE_CONTROL_RESPONSE = 0x0500;
        /** 行驶记录数据上传 */
        public static final int DRIVING_RECORD_DATA_UPLOAD = 0x0700;
        /** 电子运单上报 */
        public static final int ELECTRONIC_WAYBILL_REPORT = 0x0701;
        /** 驾驶员身份信息上报 */
        public static final int DRIVER_IDENTITY_INFO_REPORT = 0x0702;
        /** 定位数据批量上传 */
        public static final int LOCATION_DATA_BATCH_UPLOAD = 0x0704;
        /** CAN总线数据上传 */
        public static final int CAN_BUS_DATA_UPLOAD = 0x0705;
        /** 多媒体数据上传 */
        public static final int MULTIMEDIA_DATA_UPLOAD = 0x0801;

        private Terminal() {}
    }

    /**
     * 平台消息类型 (0x8xxx)
     */
    public static final class Platform {
        /** 平台通用应答 */
        public static final int COMMON_RESPONSE = 0x8001;
        /** 补传分包请求 */
        public static final int RESEND_SUBPACKAGE_REQUEST = 0x8003;
        /** 终端注册应答 */
        public static final int REGISTER_RESPONSE = 0x8100;
        /** 设置终端参数 */
        public static final int PARAMETER_SETTING = 0x8103;
        /** 查询终端参数 */
        public static final int QUERY_PARAMETERS = 0x8104;
        /** 终端控制 */
        public static final int TERMINAL_CONTROL = 0x8105;
        /** 查询指定终端参数 */
        public static final int QUERY_SPECIFIC_PARAMETERS = 0x8106;
        /** 查询终端属性 */
        public static final int QUERY_PROPERTY = 0x8107;
        /** 下发终端升级包 */
        public static final int UPGRADE_PACKAGE = 0x8108;
        /** 位置信息查询 */
        public static final int POSITION_INFO_QUERY = 0x8201;
        /** 临时位置跟踪控制 */
        public static final int TEMPORARY_LOCATION_TRACKING_CONTROL = 0x8202;
        /** 人工确认报警消息 */
        public static final int MANUAL_ALARM_CONFIRMATION = 0x8203;
        /** 文本信息下发 */
        public static final int TEXT_INFO_DISTRIBUTION = 0x8300;
        /** 事件设置 */
        public static final int EVENT_SETTING = 0x8301;
        /** 提问下发 */
        public static final int QUESTION_DISTRIBUTION = 0x8302;
        /** 信息点播菜单设置 */
        public static final int INFO_MENU_SETTING = 0x8303;
        /** 信息服务 */
        public static final int INFO_SERVICE = 0x8304;
        /** 电话回拨 */
        public static final int PHONE_CALLBACK = 0x8400;
        /** 设置电话本 */
        public static final int PHONEBOOK_SETTING = 0x8401;
        /** 车辆控制 */
        public static final int VEHICLE_CONTROL = 0x8500;
        /** 设置圆形区域 */
        public static final int SET_CIRCULAR_AREA = 0x8600;
        /** 删除圆形区域 */
        public static final int DELETE_CIRCULAR_AREA = 0x8601;
        /** 设置矩形区域 */
        public static final int SET_RECTANGULAR_AREA = 0x8602;
        /** 删除矩形区域 */
        public static final int DELETE_RECTANGULAR_AREA = 0x8603;
        /** 设置多边形区域 */
        public static final int SET_POLYGON_AREA = 0x8604;
        /** 删除多边形区域 */
        public static final int DELETE_POLYGON_AREA = 0x8605;
        /** 设置路线 */
        public static final int SET_ROUTE = 0x8606;
        /** 删除路线 */
        public static final int DELETE_ROUTE = 0x8607;
        /** 行驶记录数据采集命令 */
        public static final int DRIVING_RECORD_DATA_COLLECTION = 0x8700;
        /** 行驶记录参数下传命令 */
        public static final int DRIVING_RECORD_PARAMETER_TRANSMISSION = 0x8701;
        /** 上报驾驶员身份信息请求 */
        public static final int DRIVER_IDENTITY_INFO_REQUEST = 0x8702;
        /** 多媒体数据上传应答 */
        public static final int MULTIMEDIA_DATA_UPLOAD_RESPONSE = 0x8800;

        private Platform() {}
    }

    /**
     * 扩展消息类型
     * 用于厂商自定义或协议扩展消息
     */
    public static final class Extension {
        // 预留给扩展消息使用
        
        private Extension() {}
    }
}