package com.jt808.protocol.message;

import com.jt808.protocol.message.T8103TerminalParameterSetting.ParameterItem;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8103设置终端参数消息测试
 */
class T8103TerminalParameterSettingTest {
    
    private T8103TerminalParameterSetting message;
    
    @BeforeEach
    void setUp() {
        message = new T8103TerminalParameterSetting();
    }
    
    @Test
    void testMessageId() {
        assertEquals(0x8103, message.getMessageId());
    }
    
    @Test
    void testConstructors() {
        // 默认构造函数
        T8103TerminalParameterSetting msg1 = new T8103TerminalParameterSetting();
        assertNotNull(msg1.getParameterItems());
        assertEquals(0, msg1.getParameterCount());
        
        // 带消息头构造函数
        JT808Header header = new JT808Header();
        header.setMessageId(0x8103);
        T8103TerminalParameterSetting msg2 = new T8103TerminalParameterSetting(header);
        assertNotNull(msg2.getParameterItems());
        assertEquals(0, msg2.getParameterCount());
        assertEquals(header, msg2.getHeader());
    }
    
    @Test
    void testAddDwordParameter() {
        // 添加心跳间隔参数
        message.addDwordParameter(0x0001L, 30L);
        
        assertEquals(1, message.getParameterCount());
        assertEquals(Long.valueOf(30L), message.getDwordParameter(0x0001L));
        
        ParameterItem item = message.getParameterItem(0x0001L);
        assertNotNull(item);
        assertEquals(0x0001L, item.getParameterId());
        assertEquals(4, item.getParameterLength());
        assertEquals("终端心跳发送间隔(s)", item.getParameterDescription());
    }
    
    @Test
    void testAddWordParameter() {
        // 添加电子围栏半径参数
        message.addWordParameter(0x0031L, 500);
        
        assertEquals(1, message.getParameterCount());
        assertEquals(Integer.valueOf(500), message.getWordParameter(0x0031L));
        
        ParameterItem item = message.getParameterItem(0x0031L);
        assertNotNull(item);
        assertEquals(0x0031L, item.getParameterId());
        assertEquals(2, item.getParameterLength());
        assertEquals("电子围栏半径(m)", item.getParameterDescription());
    }
    
    @Test
    void testAddByteParameter() {
        // 添加车牌颜色参数
        message.addByteParameter(0x0084L, (byte) 2);
        
        assertEquals(1, message.getParameterCount());
        assertEquals(Byte.valueOf((byte) 2), message.getByteParameter(0x0084L));
        
        ParameterItem item = message.getParameterItem(0x0084L);
        assertNotNull(item);
        assertEquals(0x0084L, item.getParameterId());
        assertEquals(1, item.getParameterLength());
        assertEquals("车牌颜色", item.getParameterDescription());
    }
    
    @Test
    void testAddStringParameter() {
        // 添加主服务器地址参数
        message.addStringParameter(0x0013L, "192.168.1.100");
        
        assertEquals(1, message.getParameterCount());
        assertEquals("192.168.1.100", message.getStringParameter(0x0013L));
        
        ParameterItem item = message.getParameterItem(0x0013L);
        assertNotNull(item);
        assertEquals(0x0013L, item.getParameterId());
        assertEquals(13, item.getParameterLength());
        assertEquals("主服务器地址", item.getParameterDescription());
    }
    
    @Test
    void testAddBytesParameter() {
        // 添加CAN总线ID单独采集设置参数
        byte[] canConfig = {0x00, 0x00, 0x03, (byte) 0xE8, 0x00, 0x00, 0x01, 0x23};
        message.addBytesParameter(0x0110L, canConfig);
        
        assertEquals(1, message.getParameterCount());
        
        ParameterItem item = message.getParameterItem(0x0110L);
        assertNotNull(item);
        assertEquals(0x0110L, item.getParameterId());
        assertEquals(8, item.getParameterLength());
        assertArrayEquals(canConfig, item.getValueBytes());
        assertEquals("CAN总线ID单独采集设置", item.getParameterDescription());
    }
    
    @Test
    void testMultipleParameters() {
        // 添加多个参数
        message.addDwordParameter(0x0001L, 30L);           // 心跳间隔
        message.addDwordParameter(0x0002L, 20L);           // TCP超时时间
        message.addStringParameter(0x0013L, "192.168.1.100"); // 主服务器地址
        message.addWordParameter(0x0031L, 500);            // 电子围栏半径
        message.addByteParameter(0x0084L, (byte) 2);       // 车牌颜色
        
        assertEquals(5, message.getParameterCount());
        
        // 验证各参数值
        assertEquals(Long.valueOf(30L), message.getDwordParameter(0x0001L));
        assertEquals(Long.valueOf(20L), message.getDwordParameter(0x0002L));
        assertEquals("192.168.1.100", message.getStringParameter(0x0013L));
        assertEquals(Integer.valueOf(500), message.getWordParameter(0x0031L));
        assertEquals(Byte.valueOf((byte) 2), message.getByteParameter(0x0084L));
    }
    
    @Test
    void testEncodingDecoding() {
        // 添加测试参数
        message.addDwordParameter(0x0001L, 30L);           // 心跳间隔
        message.addStringParameter(0x0013L, "192.168.1.100"); // 主服务器地址
        message.addWordParameter(0x0031L, 500);            // 电子围栏半径
        
        // 编码
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 0);
        
        // 验证编码格式
        int index = 0;
        
        // 参数总数
        assertEquals(3, encoded.getUnsignedByte(index));
        index += 1;
        
        // 第一个参数 (0x0001, DWORD, 30)
        assertEquals(0x0001L, encoded.getUnsignedInt(index));
        index += 4;
        assertEquals(4, encoded.getUnsignedByte(index));
        index += 1;
        assertEquals(30L, encoded.getUnsignedInt(index));
        index += 4;
        
        // 解码
        T8103TerminalParameterSetting decoded = new T8103TerminalParameterSetting();
        decoded.decodeBody(encoded);
        
        // 验证解码结果
        assertEquals(3, decoded.getParameterCount());
        assertEquals(Long.valueOf(30L), decoded.getDwordParameter(0x0001L));
        assertEquals("192.168.1.100", decoded.getStringParameter(0x0013L));
        assertEquals(Integer.valueOf(500), decoded.getWordParameter(0x0031L));
    }
    
    @Test
    void testEmptyParametersEncodingDecoding() {
        // 空参数列表编码
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(1, encoded.length()); // 只有参数总数字段
        assertEquals(0, encoded.getUnsignedByte(0));
        
        // 解码
        T8103TerminalParameterSetting decoded = new T8103TerminalParameterSetting();
        decoded.decodeBody(encoded);
        
        assertEquals(0, decoded.getParameterCount());
        assertTrue(decoded.getParameterItems().isEmpty());
    }
    
    @Test
    void testParameterRemoval() {
        // 添加参数
        message.addDwordParameter(0x0001L, 30L);
        message.addStringParameter(0x0013L, "192.168.1.100");
        assertEquals(2, message.getParameterCount());
        
        // 移除参数
        assertTrue(message.removeParameter(0x0001L));
        assertEquals(1, message.getParameterCount());
        assertNull(message.getDwordParameter(0x0001L));
        assertEquals("192.168.1.100", message.getStringParameter(0x0013L));
        
        // 移除不存在的参数
        assertFalse(message.removeParameter(0x9999L));
        assertEquals(1, message.getParameterCount());
    }
    
    @Test
    void testClearParameters() {
        // 添加参数
        message.addDwordParameter(0x0001L, 30L);
        message.addStringParameter(0x0013L, "192.168.1.100");
        assertEquals(2, message.getParameterCount());
        
        // 清空参数
        message.clearParameters();
        assertEquals(0, message.getParameterCount());
        assertTrue(message.getParameterItems().isEmpty());
    }
    
    @Test
    void testParameterItemCreation() {
        // 测试DWORD参数项创建
        ParameterItem dwordItem = ParameterItem.createDwordParameter(0x0001L, 30L);
        assertEquals(0x0001L, dwordItem.getParameterId());
        assertEquals(4, dwordItem.getParameterLength());
        assertEquals(Long.valueOf(30L), dwordItem.getDwordValue());
        
        // 测试WORD参数项创建
        ParameterItem wordItem = ParameterItem.createWordParameter(0x0031L, 500);
        assertEquals(0x0031L, wordItem.getParameterId());
        assertEquals(2, wordItem.getParameterLength());
        assertEquals(Integer.valueOf(500), wordItem.getWordValue());
        
        // 测试BYTE参数项创建
        ParameterItem byteItem = ParameterItem.createByteParameter(0x0084L, (byte) 2);
        assertEquals(0x0084L, byteItem.getParameterId());
        assertEquals(1, byteItem.getParameterLength());
        assertEquals(Byte.valueOf((byte) 2), byteItem.getByteValue());
        
        // 测试STRING参数项创建
        ParameterItem stringItem = ParameterItem.createStringParameter(0x0013L, "test");
        assertEquals(0x0013L, stringItem.getParameterId());
        assertEquals(4, stringItem.getParameterLength());
        assertEquals("test", stringItem.getStringValue());
    }
    
    @Test
    void testParameterItemValueRetrieval() {
        // 创建不同长度的参数项测试值获取
        ParameterItem item1 = new ParameterItem(0x0001L, new byte[]{0x00, 0x00, 0x00, 0x1E}); // 4字节
        assertEquals(Long.valueOf(30L), item1.getDwordValue());
        assertNull(item1.getWordValue()); // 虽然有4字节，但WORD只取前2字节
        assertNotNull(item1.getByteValue()); // 有字节数据
        
        ParameterItem item2 = new ParameterItem(0x0002L, new byte[]{0x01, (byte) 0xF4}); // 2字节
        assertNull(item2.getDwordValue()); // 不足4字节
        assertEquals(Integer.valueOf(500), item2.getWordValue());
        assertNotNull(item2.getByteValue());
        
        ParameterItem item3 = new ParameterItem(0x0003L, new byte[]{0x02}); // 1字节
        assertNull(item3.getDwordValue());
        assertNull(item3.getWordValue());
        assertEquals(Byte.valueOf((byte) 2), item3.getByteValue());
        
        ParameterItem item4 = new ParameterItem(0x0004L, new byte[0]); // 0字节
        assertNull(item4.getDwordValue());
        assertNull(item4.getWordValue());
        assertNull(item4.getByteValue());
    }
    
    @Test
    void testParameterDescriptions() {
        // 测试已定义参数的描述
        assertEquals("终端心跳发送间隔(s)", 
                T8103TerminalParameterSetting.ParameterDefinitions.getParameterDescription(0x0001L));
        assertEquals("主服务器地址", 
                T8103TerminalParameterSetting.ParameterDefinitions.getParameterDescription(0x0013L));
        assertEquals("电子围栏半径(m)", 
                T8103TerminalParameterSetting.ParameterDefinitions.getParameterDescription(0x0031L));
        
        // 测试CAN总线ID范围参数
        assertEquals("CAN总线ID单独采集设置(0x0150)", 
                T8103TerminalParameterSetting.ParameterDefinitions.getParameterDescription(0x0150L));
        
        // 测试用户自定义参数
        assertEquals("用户自定义参数(0xF001)", 
                T8103TerminalParameterSetting.ParameterDefinitions.getParameterDescription(0xF001L));
        
        // 测试未知参数
        assertEquals("未知参数(0x9999)", 
                T8103TerminalParameterSetting.ParameterDefinitions.getParameterDescription(0x9999L));
    }
    
    @Test
    void testParameterItemEqualsAndHashCode() {
        ParameterItem item1 = ParameterItem.createDwordParameter(0x0001L, 30L);
        ParameterItem item2 = ParameterItem.createDwordParameter(0x0001L, 30L);
        ParameterItem item3 = ParameterItem.createDwordParameter(0x0001L, 60L);
        ParameterItem item4 = ParameterItem.createDwordParameter(0x0002L, 30L);
        
        // 测试equals
        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
        assertNotEquals(item1, item4);
        assertNotEquals(item1, null);
        assertNotEquals(item1, "string");
        
        // 测试hashCode
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1.hashCode(), item3.hashCode());
        assertNotEquals(item1.hashCode(), item4.hashCode());
    }
    
    @Test
    void testNullAndEmptyValues() {
        // 测试null字符串参数
        message.addStringParameter(0x0013L, null);
        assertEquals("", message.getStringParameter(0x0013L));
        
        // 测试空字符串参数
        message.addStringParameter(0x0014L, "");
        assertEquals("", message.getStringParameter(0x0014L));
        
        // 测试null字节数组
        message.addBytesParameter(0x0110L, null);
        ParameterItem item = message.getParameterItem(0x0110L);
        assertNotNull(item);
        assertEquals(0, item.getParameterLength());
        assertArrayEquals(new byte[0], item.getValueBytes());
    }
    
    @Test
    void testChineseStringParameter() {
        // 测试中文字符串参数
        String chineseText = "监控平台";
        message.addStringParameter(0x0040L, chineseText);
        
        assertEquals(chineseText, message.getStringParameter(0x0040L));
        
        // 编码解码测试
        Buffer encoded = message.encodeBody();
        T8103TerminalParameterSetting decoded = new T8103TerminalParameterSetting();
        decoded.decodeBody(encoded);
        
        assertEquals(chineseText, decoded.getStringParameter(0x0040L));
    }
    
    @Test
    void testToString() {
        message.addDwordParameter(0x0001L, 30L);
        message.addStringParameter(0x0013L, "192.168.1.100");
        
        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("T8103TerminalParameterSetting"));
        assertTrue(result.contains("parameterCount=2"));
        assertTrue(result.contains("0x0001"));
        assertTrue(result.contains("0x0013"));
    }
    
    @Test
    void testParameterItemToString() {
        ParameterItem dwordItem = ParameterItem.createDwordParameter(0x0001L, 30L);
        String dwordStr = dwordItem.toString();
        assertTrue(dwordStr.contains("0x0001"));
        assertTrue(dwordStr.contains("length=4"));
        assertTrue(dwordStr.contains("value=30"));
        assertTrue(dwordStr.contains("终端心跳发送间隔(s)"));
        
        ParameterItem stringItem = ParameterItem.createStringParameter(0x0013L, "test");
        String stringStr = stringItem.toString();
        System.out.println("Actual string output: " + stringStr);
        assertTrue(stringStr.contains("0x0013"));
        assertTrue(stringStr.contains("length=4"));
        assertTrue(stringStr.contains("value=1952805748"));
        assertTrue(stringStr.contains("主服务器地址"));
    }
}