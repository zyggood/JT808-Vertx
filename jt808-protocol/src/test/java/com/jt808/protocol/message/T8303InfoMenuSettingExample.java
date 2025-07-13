package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

/**
 * T8303 信息点播菜单设置消息使用示例
 *
 * @author JT808-Vertx
 * @version 1.0
 */
public class T8303InfoMenuSettingExample {

    public static void main(String[] args) {
        System.out.println("=== T8303 信息点播菜单设置消息使用示例 ===\n");

        // 示例1：删除终端全部信息项
        demonstrateDeleteAll();

        // 示例2：更新菜单
        demonstrateUpdate();

        // 示例3：追加菜单
        demonstrateAppend();

        // 示例4：修改菜单
        demonstrateModify();

        // 示例5：消息编码和解码
        demonstrateEncodeAndDecode();
    }

    /**
     * 示例1：删除终端全部信息项
     */
    private static void demonstrateDeleteAll() {
        System.out.println("1. 删除终端全部信息项");

        // 创建删除全部信息项的消息
        T8303InfoMenuSetting message = T8303InfoMenuSetting.createDeleteAll();

        System.out.println("   消息类型: " + message.getSettingTypeDescription());
        System.out.println("   信息项数量: " + message.getInfoItemCountUnsigned());
        System.out.println("   消息内容: " + message);
        System.out.println();
    }

    /**
     * 示例2：更新菜单
     */
    private static void demonstrateUpdate() {
        System.out.println("2. 更新菜单");

        // 创建更新菜单的消息
        T8303InfoMenuSetting message = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.UPDATE);

        // 添加信息项
        message.addInfoItem((byte) 1, "天气预报");
        message.addInfoItem((byte) 2, "交通信息");
        message.addInfoItem((byte) 3, "新闻资讯");
        message.addInfoItem((byte) 4, "股票行情");

        System.out.println("   消息类型: " + message.getSettingTypeDescription());
        System.out.println("   信息项数量: " + message.getInfoItemCountUnsigned());
        System.out.println("   信息项列表:");
        for (T8303InfoMenuSetting.InfoItem item : message.getInfoItems()) {
            System.out.println("     - 类型: " + item.getInfoTypeUnsigned() +
                    ", 名称: \"" + item.getInfoName() + "\"" +
                    ", 字节长度: " + item.getInfoNameByteLength());
        }
        System.out.println();
    }

    /**
     * 示例3：追加菜单
     */
    private static void demonstrateAppend() {
        System.out.println("3. 追加菜单");

        // 创建追加菜单的消息
        T8303InfoMenuSetting message = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.APPEND);

        // 添加新的信息项
        message.addInfoItem((byte) 5, "生活服务");
        message.addInfoItem((byte) 6, "娱乐资讯");

        System.out.println("   消息类型: " + message.getSettingTypeDescription());
        System.out.println("   信息项数量: " + message.getInfoItemCountUnsigned());
        System.out.println("   新增信息项:");
        for (T8303InfoMenuSetting.InfoItem item : message.getInfoItems()) {
            System.out.println("     - 类型: " + item.getInfoTypeUnsigned() +
                    ", 名称: \"" + item.getInfoName() + "\"");
        }
        System.out.println();
    }

    /**
     * 示例4：修改菜单
     */
    private static void demonstrateModify() {
        System.out.println("4. 修改菜单");

        // 创建修改菜单的消息
        T8303InfoMenuSetting message = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.MODIFY);

        // 修改现有信息项
        message.addInfoItem((byte) 1, "实时天气");  // 修改类型1的名称
        message.addInfoItem((byte) 3, "热点新闻");  // 修改类型3的名称

        System.out.println("   消息类型: " + message.getSettingTypeDescription());
        System.out.println("   信息项数量: " + message.getInfoItemCountUnsigned());
        System.out.println("   修改的信息项:");
        for (T8303InfoMenuSetting.InfoItem item : message.getInfoItems()) {
            System.out.println("     - 类型: " + item.getInfoTypeUnsigned() +
                    ", 新名称: \"" + item.getInfoName() + "\"");
        }
        System.out.println();
    }

    /**
     * 示例5：消息编码和解码
     */
    private static void demonstrateEncodeAndDecode() {
        System.out.println("5. 消息编码和解码示例");

        // 创建原始消息
        T8303InfoMenuSetting originalMessage = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.UPDATE);
        originalMessage.addInfoItem((byte) 1, "天气预报");
        originalMessage.addInfoItem((byte) 2, "路况信息");

        System.out.println("   原始消息: " + originalMessage);

        // 编码消息体
        Buffer encodedBody = originalMessage.encodeBody();
        System.out.println("   编码后字节数: " + encodedBody.length());
        System.out.print("   编码数据: ");
        for (int i = 0; i < encodedBody.length(); i++) {
            System.out.printf("%02X ", encodedBody.getByte(i) & 0xFF);
        }
        System.out.println();

        // 解码消息体
        T8303InfoMenuSetting decodedMessage = new T8303InfoMenuSetting();
        decodedMessage.decodeBody(encodedBody);

        System.out.println("   解码后消息: " + decodedMessage);

        // 验证编码解码的一致性
        boolean isEqual = originalMessage.equals(decodedMessage);
        System.out.println("   编码解码一致性: " + (isEqual ? "✓ 通过" : "✗ 失败"));
        System.out.println();
    }
}