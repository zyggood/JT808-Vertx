package com.jt808.protocol.example;

import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T8105TerminalControl;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * T8105终端控制消息使用示例
 * 演示如何创建、编码、解码和使用T8105终端控制消息
 */
public class T8105TerminalControlExample {

    private static final Logger logger = LoggerFactory.getLogger(T8105TerminalControlExample.class);

    @Test
    public void demonstrateT8105Usage() {
        logger.info("=== T8105终端控制消息功能演示 ===");

        // 1. 使用工厂创建消息
        logger.info("\n1. 使用工厂创建T8105消息");
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message factoryMessage = factory.createMessage(0x8105);
        assertInstanceOf(T8105TerminalControl.class, factoryMessage);
        logger.info("工厂创建成功: {}", factoryMessage.getClass().getSimpleName());

        // 2. 演示简单控制命令
        logger.info("\n2. 简单控制命令演示");
        demonstrateSimpleCommands();

        // 3. 演示无线升级命令
        logger.info("\n3. 无线升级命令演示");
        demonstrateWirelessUpgrade();

        // 4. 演示连接服务器控制命令
        logger.info("\n4. 连接服务器控制命令演示");
        demonstrateConnectServer();

        // 5. 演示消息编解码
        logger.info("\n5. 消息编解码演示");
        demonstrateEncodeDecode();

        // 6. 演示完整的消息处理流程
        logger.info("\n6. 完整消息处理流程演示");
        demonstrateCompleteFlow();
    }

    /**
     * 演示简单控制命令
     */
    private void demonstrateSimpleCommands() {
        // 终端关机
        T8105TerminalControl shutdown = T8105TerminalControl.createSimpleCommand(
                T8105TerminalControl.ControlCommand.TERMINAL_SHUTDOWN
        );
        logger.info("终端关机命令: {}", shutdown.getCommandDescription());
        logger.info("命令字: 0x{:02X}", shutdown.getCommandWord());
        logger.info("参数: '{}'", shutdown.getCommandParameters());

        // 终端复位
        T8105TerminalControl reset = T8105TerminalControl.createSimpleCommand(
                T8105TerminalControl.ControlCommand.TERMINAL_RESET
        );
        logger.info("终端复位命令: {}", reset.getCommandDescription());

        // 终端恢复出厂设置
        T8105TerminalControl factoryReset = T8105TerminalControl.createSimpleCommand(
                T8105TerminalControl.ControlCommand.TERMINAL_FACTORY_RESET
        );
        logger.info("恢复出厂设置命令: {}", factoryReset.getCommandDescription());

        // 关闭数据通信
        T8105TerminalControl closeData = T8105TerminalControl.createSimpleCommand(
                T8105TerminalControl.ControlCommand.CLOSE_DATA_COMMUNICATION
        );
        logger.info("关闭数据通信命令: {}", closeData.getCommandDescription());

        // 关闭所有无线通信
        T8105TerminalControl closeWireless = T8105TerminalControl.createSimpleCommand(
                T8105TerminalControl.ControlCommand.CLOSE_ALL_WIRELESS_COMMUNICATION
        );
        logger.info("关闭所有无线通信命令: {}", closeWireless.getCommandDescription());
    }

    /**
     * 演示无线升级命令
     */
    private void demonstrateWirelessUpgrade() {
        T8105TerminalControl upgrade = T8105TerminalControl.createWirelessUpgrade(
                "http://firmware.update.com/v2.1.0/firmware.bin",  // URL地址
                "CMNET",                                            // 拨号点名称
                "user",                                             // 拨号用户名
                "password",                                         // 拨号密码
                "192.168.100.200",                                 // 服务器地址
                8080,                                               // TCP端口
                9090,                                               // UDP端口
                "ABCDE".getBytes(),                                // 制造商ID
                "HW_V1.0",                                         // 硬件版本
                "FW_V2.1.0",                                       // 固件版本
                60                                                  // 连接时限(分钟)
        );

        logger.info("无线升级命令创建成功");
        logger.info("命令描述: {}", upgrade.getCommandDescription());
        logger.info("命令字: 0x{:02X}", upgrade.getCommandWord());

        List<String> params = upgrade.getParameterList();
        logger.info("参数列表 (共{}个):", params.size());
        String[] paramNames = {
                "URL地址", "拨号点名称", "拨号用户名", "拨号密码", "服务器地址",
                "TCP端口", "UDP端口", "制造商ID", "硬件版本", "固件版本", "连接时限"
        };

        for (int i = 0; i < params.size() && i < paramNames.length; i++) {
            logger.info("  {}: {}", paramNames[i], params.get(i));
        }
    }

    /**
     * 演示连接服务器控制命令
     */
    private void demonstrateConnectServer() {
        // 切换到指定监管平台服务器
        T8105TerminalControl connectSpecified = T8105TerminalControl.createConnectServer(
                T8105TerminalControl.ConnectionControl.SWITCH_TO_SPECIFIED_SERVER,
                "SUPERVISION_AUTH_2024",                           // 监管平台鉴权码
                "CMNET",                                            // 拨号点名称
                "supervisor",                                       // 拨号用户名
                "sup_password",                                     // 拨号密码
                "supervision.platform.com",                        // 监管平台地址
                8888,                                               // TCP端口
                9999,                                               // UDP端口
                120                                                 // 连接时限(分钟)
        );

        logger.info("切换到指定监管平台命令创建成功");
        logger.info("命令描述: {}", connectSpecified.getCommandDescription());

        List<String> params = connectSpecified.getParameterList();
        logger.info("参数列表 (共{}个):", params.size());
        String[] paramNames = {
                "连接控制", "监管平台鉴权码", "拨号点名称", "拨号用户名", "拨号密码",
                "服务器地址", "TCP端口", "UDP端口", "连接时限"
        };

        for (int i = 0; i < params.size() && i < paramNames.length; i++) {
            logger.info("  {}: {}", paramNames[i], params.get(i));
        }

        // 切换回原缺省监控平台服务器
        T8105TerminalControl connectDefault = T8105TerminalControl.createConnectServer(
                T8105TerminalControl.ConnectionControl.SWITCH_TO_DEFAULT_SERVER,
                null, null, null, null, null, 0, 0, 0
        );

        logger.info("\n切换回原监控平台命令创建成功");
        logger.info("命令描述: {}", connectDefault.getCommandDescription());
        List<String> defaultParams = connectDefault.getParameterList();
        logger.info("参数列表: {}", defaultParams);
    }

    /**
     * 演示消息编解码
     */
    private void demonstrateEncodeDecode() {
        // 创建一个带中文参数的控制消息
        T8105TerminalControl original = new T8105TerminalControl();
        original.setCommandWord((byte) 1);
        original.setCommandParameters("升级服务器;中国移动;用户名;密码;192.168.1.100;8080;9090;厂商;硬件版本;固件版本;60");

        logger.info("原始消息: {}", original);

        // 编码
        Buffer encoded = original.encodeBody();
        logger.info("编码后数据长度: {} 字节", encoded.length());
        logger.info("编码后数据 (前20字节): {}",
                encoded.getBytes(0, Math.min(20, encoded.length())));

        // 解码
        T8105TerminalControl decoded = new T8105TerminalControl();
        decoded.decodeBody(encoded);

        logger.info("解码后消息: {}", decoded);
        logger.info("编解码一致性检查: {}",
                original.getCommandWord() == decoded.getCommandWord() &&
                        original.getCommandParameters().equals(decoded.getCommandParameters()));

        // 验证参数解析
        List<String> decodedParams = decoded.getParameterList();
        logger.info("解码后参数列表 (共{}个):", decodedParams.size());
        for (int i = 0; i < decodedParams.size(); i++) {
            logger.info("  参数{}: {}", i + 1, decodedParams.get(i));
        }
    }

    /**
     * 演示完整的消息处理流程
     */
    private void demonstrateCompleteFlow() {
        // 1. 创建消息头
        JT808Header header = new JT808Header();
        header.setMessageId(0x8105);
        header.setPhoneNumber("13800138000");
        header.setSerialNumber(12345);

        // 2. 创建终端控制消息
        T8105TerminalControl message = new T8105TerminalControl(header);
        message.setCommandWord((byte) 4); // 终端复位
        message.setCommandParameters("");

        logger.info("创建完整消息成功");
        logger.info("消息头: {}", header);
        logger.info("消息体: {}", message);

        // 3. 使用工厂进行完整编码
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        Buffer fullEncoded = factory.encodeMessage(message);
        logger.info("完整编码后数据长度: {} 字节", fullEncoded.length());

        // 4. 模拟发送和接收
        logger.info("\n模拟消息发送和接收流程:");
        logger.info("1. 平台创建终端控制消息");
        logger.info("2. 消息编码: {} 字节", fullEncoded.length());
        logger.info("3. 通过网络发送到终端");
        logger.info("4. 终端接收并解码消息");

        try {
            JT808Message receivedMessage = factory.parseMessage(fullEncoded);
            if (receivedMessage instanceof T8105TerminalControl) {
                T8105TerminalControl controlMsg = (T8105TerminalControl) receivedMessage;
                logger.info("5. 终端解析控制命令: {}", controlMsg.getCommandDescription());
                logger.info("6. 终端执行相应操作");
            }
        } catch (Exception e) {
            logger.error("消息解析失败", e);
        }

        // 5. 演示所有控制命令类型
        logger.info("\n支持的所有控制命令:");
        for (T8105TerminalControl.ControlCommand command : T8105TerminalControl.ControlCommand.values()) {
            logger.info("命令字 0x{:02X}: {} (需要参数: {})",
                    command.getValue(),
                    command.getDescription(),
                    command.hasParameters() ? "是" : "否");
        }
    }
}