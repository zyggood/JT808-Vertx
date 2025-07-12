package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("T8105终端控制消息测试")
class T8105TerminalControlTest {

    private T8105TerminalControl message;
    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        message = new T8105TerminalControl();
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x8105, message.getMessageId());
    }

    @Test
    @DisplayName("测试构造函数")
    void testConstructors() {
        // 测试默认构造函数
        T8105TerminalControl msg1 = new T8105TerminalControl();
        assertNotNull(msg1);
        assertEquals(0x8105, msg1.getMessageId());

        // 测试带头部的构造函数
        JT808Header header = new JT808Header();
        header.setMessageId(0x8105);
        T8105TerminalControl msg2 = new T8105TerminalControl(header);
        assertNotNull(msg2);
        assertEquals(header, msg2.getHeader());

        // 测试带参数的构造函数
        T8105TerminalControl msg3 = new T8105TerminalControl((byte) 3, "");
        assertEquals((byte) 3, msg3.getCommandWord());
        assertEquals("", msg3.getCommandParameters());
    }

    @Test
    @DisplayName("测试工厂创建")
    void testFactoryCreation() {
        JT808Message factoryMessage = factory.createMessage(0x8105);
        assertInstanceOf(T8105TerminalControl.class, factoryMessage);
        assertEquals(0x8105, factoryMessage.getMessageId());
    }

    @Test
    @DisplayName("测试简单控制命令编解码")
    void testSimpleCommandEncodeDecode() {
        // 测试终端关机命令
        message.setCommandWord((byte) 3);
        message.setCommandParameters("");

        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(1, encoded.length()); // 只有命令字
        assertEquals((byte) 3, encoded.getByte(0));

        // 解码测试
        T8105TerminalControl decoded = new T8105TerminalControl();
        decoded.decodeBody(encoded);
        assertEquals((byte) 3, decoded.getCommandWord());
        assertEquals("", decoded.getCommandParameters());
    }

    @Test
    @DisplayName("测试带参数的控制命令编解码")
    void testParameterCommandEncodeDecode() {
        // 测试无线升级命令
        message.setCommandWord((byte) 1);
        String params = "http://update.server.com;CMNET;user;pass;192.168.1.100;8080;9090;12345;v1.0;v2.0;60";
        message.setCommandParameters(params);

        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 1);
        assertEquals((byte) 1, encoded.getByte(0));

        // 解码测试
        T8105TerminalControl decoded = new T8105TerminalControl();
        decoded.decodeBody(encoded);
        assertEquals((byte) 1, decoded.getCommandWord());
        assertEquals(params, decoded.getCommandParameters());
    }

    @Test
    @DisplayName("测试命令描述")
    void testCommandDescription() {
        message.setCommandWord((byte) 1);
        assertEquals("无线升级", message.getCommandDescription());

        message.setCommandWord((byte) 2);
        assertEquals("控制终端连接指定服务器", message.getCommandDescription());

        message.setCommandWord((byte) 3);
        assertEquals("终端关机", message.getCommandDescription());

        message.setCommandWord((byte) 4);
        assertEquals("终端复位", message.getCommandDescription());

        message.setCommandWord((byte) 5);
        assertEquals("终端恢复出厂设置", message.getCommandDescription());

        message.setCommandWord((byte) 6);
        assertEquals("关闭数据通信", message.getCommandDescription());

        message.setCommandWord((byte) 7);
        assertEquals("关闭所有无线通信", message.getCommandDescription());

        message.setCommandWord((byte) 99);
        assertTrue(message.getCommandDescription().contains("未知命令"));
    }

    @Test
    @DisplayName("测试参数列表解析")
    void testParameterList() {
        // 测试空参数
        message.setCommandParameters("");
        List<String> params = message.getParameterList();
        assertTrue(params.isEmpty());

        message.setCommandParameters(null);
        params = message.getParameterList();
        assertTrue(params.isEmpty());

        // 测试单个参数
        message.setCommandParameters("param1");
        params = message.getParameterList();
        assertEquals(1, params.size());
        assertEquals("param1", params.get(0));

        // 测试多个参数
        message.setCommandParameters("param1;param2;param3");
        params = message.getParameterList();
        assertEquals(3, params.size());
        assertEquals("param1", params.get(0));
        assertEquals("param2", params.get(1));
        assertEquals("param3", params.get(2));

        // 测试包含空参数
        message.setCommandParameters("param1;;param3");
        params = message.getParameterList();
        assertEquals(3, params.size());
        assertEquals("param1", params.get(0));
        assertEquals("", params.get(1));
        assertEquals("param3", params.get(2));
    }

    @Test
    @DisplayName("测试设置参数列表")
    void testSetParameterList() {
        // 测试空列表
        message.setParameterList(null);
        assertEquals("", message.getCommandParameters());

        message.setParameterList(Arrays.asList());
        assertEquals("", message.getCommandParameters());

        // 测试单个参数
        message.setParameterList(Arrays.asList("param1"));
        assertEquals("param1", message.getCommandParameters());

        // 测试多个参数
        message.setParameterList(Arrays.asList("param1", "param2", "param3"));
        assertEquals("param1;param2;param3", message.getCommandParameters());

        // 测试包含空参数
        message.setParameterList(Arrays.asList("param1", "", "param3"));
        assertEquals("param1;;param3", message.getCommandParameters());
    }

    @Test
    @DisplayName("测试创建无线升级控制消息")
    void testCreateWirelessUpgrade() {
        T8105TerminalControl upgrade = T8105TerminalControl.createWirelessUpgrade(
                "http://update.server.com",
                "CMNET",
                "user",
                "password",
                "192.168.1.100",
                8080,
                9090,
                "12345".getBytes(),
                "v1.0",
                "v2.0",
                60
        );

        assertEquals((byte) 1, upgrade.getCommandWord());
        assertEquals("无线升级", upgrade.getCommandDescription());

        List<String> params = upgrade.getParameterList();
        assertEquals(11, params.size());
        assertEquals("http://update.server.com", params.get(0));
        assertEquals("CMNET", params.get(1));
        assertEquals("user", params.get(2));
        assertEquals("password", params.get(3));
        assertEquals("192.168.1.100", params.get(4));
        assertEquals("8080", params.get(5));
        assertEquals("9090", params.get(6));
        assertEquals("12345", params.get(7));
        assertEquals("v1.0", params.get(8));
        assertEquals("v2.0", params.get(9));
        assertEquals("60", params.get(10));
    }

    @Test
    @DisplayName("测试创建连接服务器控制消息")
    void testCreateConnectServer() {
        // 测试切换到指定服务器
        T8105TerminalControl connect = T8105TerminalControl.createConnectServer(
                (byte) 0,
                "AUTH123",
                "CMNET",
                "user",
                "password",
                "192.168.1.200",
                8080,
                9090,
                120
        );

        assertEquals((byte) 2, connect.getCommandWord());
        assertEquals("控制终端连接指定服务器", connect.getCommandDescription());

        List<String> params = connect.getParameterList();
        assertEquals(9, params.size());
        assertEquals("0", params.get(0));
        assertEquals("AUTH123", params.get(1));
        assertEquals("CMNET", params.get(2));
        assertEquals("user", params.get(3));
        assertEquals("password", params.get(4));
        assertEquals("192.168.1.200", params.get(5));
        assertEquals("8080", params.get(6));
        assertEquals("9090", params.get(7));
        assertEquals("120", params.get(8));

        // 测试切换回原服务器
        T8105TerminalControl switchBack = T8105TerminalControl.createConnectServer(
                (byte) 1, null, null, null, null, null, 0, 0, 0
        );

        assertEquals((byte) 2, switchBack.getCommandWord());
        List<String> backParams = switchBack.getParameterList();
        assertEquals(1, backParams.size());
        assertEquals("1", backParams.get(0));
    }

    @Test
    @DisplayName("测试创建简单控制命令")
    void testCreateSimpleCommand() {
        // 测试终端关机
        T8105TerminalControl shutdown = T8105TerminalControl.createSimpleCommand(
                T8105TerminalControl.ControlCommand.TERMINAL_SHUTDOWN
        );
        assertEquals((byte) 3, shutdown.getCommandWord());
        assertEquals("", shutdown.getCommandParameters());

        // 测试终端复位
        T8105TerminalControl reset = T8105TerminalControl.createSimpleCommand(
                T8105TerminalControl.ControlCommand.TERMINAL_RESET
        );
        assertEquals((byte) 4, reset.getCommandWord());
        assertEquals("", reset.getCommandParameters());

        // 测试异常情况 - 需要参数的命令
        assertThrows(IllegalArgumentException.class, () -> {
            T8105TerminalControl.createSimpleCommand(
                    T8105TerminalControl.ControlCommand.WIRELESS_UPGRADE
            );
        });
    }

    @Test
    @DisplayName("测试控制命令枚举")
    void testControlCommandEnum() {
        // 测试fromValue方法
        assertEquals(T8105TerminalControl.ControlCommand.WIRELESS_UPGRADE,
                T8105TerminalControl.ControlCommand.fromValue((byte) 1));
        assertEquals(T8105TerminalControl.ControlCommand.CONNECT_SERVER,
                T8105TerminalControl.ControlCommand.fromValue((byte) 2));
        assertEquals(T8105TerminalControl.ControlCommand.TERMINAL_SHUTDOWN,
                T8105TerminalControl.ControlCommand.fromValue((byte) 3));

        assertNull(T8105TerminalControl.ControlCommand.fromValue((byte) 99));

        // 测试hasParameters方法
        assertTrue(T8105TerminalControl.ControlCommand.WIRELESS_UPGRADE.hasParameters());
        assertTrue(T8105TerminalControl.ControlCommand.CONNECT_SERVER.hasParameters());
        assertFalse(T8105TerminalControl.ControlCommand.TERMINAL_SHUTDOWN.hasParameters());
        assertFalse(T8105TerminalControl.ControlCommand.TERMINAL_RESET.hasParameters());
        assertFalse(T8105TerminalControl.ControlCommand.TERMINAL_FACTORY_RESET.hasParameters());
        assertFalse(T8105TerminalControl.ControlCommand.CLOSE_DATA_COMMUNICATION.hasParameters());
        assertFalse(T8105TerminalControl.ControlCommand.CLOSE_ALL_WIRELESS_COMMUNICATION.hasParameters());
    }

    @Test
    @DisplayName("测试连接控制常量")
    void testConnectionControlConstants() {
        assertEquals(0, T8105TerminalControl.ConnectionControl.SWITCH_TO_SPECIFIED_SERVER);
        assertEquals(1, T8105TerminalControl.ConnectionControl.SWITCH_TO_DEFAULT_SERVER);
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        message.setCommandWord((byte) 3);
        message.setCommandParameters("");

        String str = message.toString();
        assertTrue(str.contains("T8105TerminalControl"));
        assertTrue(str.contains("终端关机"));
        assertTrue(str.contains("commandParameters=''"));
    }

    @Test
    @DisplayName("测试GBK编码")
    void testGBKEncoding() {
        // 测试中文参数
        String chineseParams = "中文参数1;中文参数2";
        message.setCommandWord((byte) 1);
        message.setCommandParameters(chineseParams);

        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);

        T8105TerminalControl decoded = new T8105TerminalControl();
        decoded.decodeBody(encoded);

        assertEquals((byte) 1, decoded.getCommandWord());
        assertEquals(chineseParams, decoded.getCommandParameters());
    }

    @Test
    @DisplayName("测试空参数处理")
    void testNullParameterHandling() {
        // 测试null参数的无线升级
        T8105TerminalControl upgrade = T8105TerminalControl.createWirelessUpgrade(
                null, null, null, null, null, 0, 0, null, null, null, 0
        );

        List<String> params = upgrade.getParameterList();
        assertEquals(11, params.size());
        for (int i = 0; i < 11; i++) {
            if (i == 5 || i == 6 || i == 10) { // 端口和时限
                assertEquals("0", params.get(i));
            } else {
                assertEquals("", params.get(i));
            }
        }
    }
}