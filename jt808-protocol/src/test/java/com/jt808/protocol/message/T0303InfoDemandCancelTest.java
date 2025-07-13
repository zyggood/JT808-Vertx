package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0303信息点播/取消消息测试
 */
@DisplayName("T0303信息点播/取消消息测试")
class T0303InfoDemandCancelTest {

    private T0303InfoDemandCancel message;
    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        message = new T0303InfoDemandCancel();
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x0303, message.getMessageId());
        assertEquals(T0303InfoDemandCancel.MESSAGE_ID, message.getMessageId());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        T0303InfoDemandCancel msg = new T0303InfoDemandCancel();
        assertEquals(0, msg.getInfoType());
        assertEquals(0, msg.getDemandFlag());
        assertFalse(msg.isDemand());
        assertTrue(msg.isCancel());
    }

    @Test
    @DisplayName("测试完整构造函数")
    void testFullConstructor() {
        byte infoType = 0x05;
        byte demandFlag = T0303InfoDemandCancel.FLAG_DEMAND;
        
        T0303InfoDemandCancel msg = new T0303InfoDemandCancel(infoType, demandFlag);
        assertEquals(infoType, msg.getInfoType());
        assertEquals(demandFlag, msg.getDemandFlag());
        assertTrue(msg.isDemand());
        assertFalse(msg.isCancel());
    }

    @Test
    @DisplayName("测试带消息头的构造函数")
    void testConstructorWithHeader() {
        JT808Header header = new JT808Header();
        header.setMessageId(0x0303);
        header.setPhoneNumber("12345678901");
        
        byte infoType = 0x03;
        byte demandFlag = T0303InfoDemandCancel.FLAG_CANCEL;
        
        T0303InfoDemandCancel msg = new T0303InfoDemandCancel(header, infoType, demandFlag);
        assertEquals(header, msg.getHeader());
        assertEquals(infoType, msg.getInfoType());
        assertEquals(demandFlag, msg.getDemandFlag());
        assertFalse(msg.isDemand());
        assertTrue(msg.isCancel());
    }

    @Test
    @DisplayName("测试静态工厂方法 - 创建点播消息")
    void testCreateDemand() {
        byte infoType = 0x10;
        T0303InfoDemandCancel msg = T0303InfoDemandCancel.createDemand(infoType);
        
        assertEquals(infoType, msg.getInfoType());
        assertEquals(T0303InfoDemandCancel.FLAG_DEMAND, msg.getDemandFlag());
        assertTrue(msg.isDemand());
        assertFalse(msg.isCancel());
        assertEquals("点播", msg.getOperationDescription());
    }

    @Test
    @DisplayName("测试静态工厂方法 - 创建取消消息")
    void testCreateCancel() {
        byte infoType = 0x20;
        T0303InfoDemandCancel msg = T0303InfoDemandCancel.createCancel(infoType);
        
        assertEquals(infoType, msg.getInfoType());
        assertEquals(T0303InfoDemandCancel.FLAG_CANCEL, msg.getDemandFlag());
        assertFalse(msg.isDemand());
        assertTrue(msg.isCancel());
        assertEquals("取消", msg.getOperationDescription());
    }

    @Test
    @DisplayName("测试消息编码")
    void testEncodeBody() {
        byte infoType = 0x15;
        byte demandFlag = T0303InfoDemandCancel.FLAG_DEMAND;
        
        message.setInfoType(infoType);
        message.setDemandFlag(demandFlag);
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(2, encoded.length());
        assertEquals(infoType, encoded.getByte(0));
        assertEquals(demandFlag, encoded.getByte(1));
    }

    @Test
    @DisplayName("测试消息解码")
    void testDecodeBody() {
        byte infoType = 0x25;
        byte demandFlag = T0303InfoDemandCancel.FLAG_CANCEL;
        
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(infoType);
        buffer.appendByte(demandFlag);
        
        message.decodeBody(buffer);
        assertEquals(infoType, message.getInfoType());
        assertEquals(demandFlag, message.getDemandFlag());
        assertFalse(message.isDemand());
        assertTrue(message.isCancel());
    }

    @Test
    @DisplayName("测试编解码一致性")
    void testEncodeDecodeConsistency() {
        byte infoType = (byte) 0xFF;
        byte demandFlag = T0303InfoDemandCancel.FLAG_DEMAND;
        
        // 设置原始数据
        message.setInfoType(infoType);
        message.setDemandFlag(demandFlag);
        
        // 编码
        Buffer encoded = message.encodeBody();
        
        // 创建新消息并解码
        T0303InfoDemandCancel decoded = new T0303InfoDemandCancel();
        decoded.decodeBody(encoded);
        
        // 验证一致性
        assertEquals(message.getInfoType(), decoded.getInfoType());
        assertEquals(message.getDemandFlag(), decoded.getDemandFlag());
        assertEquals(message.isDemand(), decoded.isDemand());
        assertEquals(message.isCancel(), decoded.isCancel());
    }

    @Test
    @DisplayName("测试无符号值获取")
    void testUnsignedValues() {
        byte infoType = (byte) 0xFF;  // -1 as signed, 255 as unsigned
        byte demandFlag = (byte) 0x80; // -128 as signed, 128 as unsigned
        
        message.setInfoType(infoType);
        message.setDemandFlag(demandFlag);
        
        assertEquals(255, message.getInfoTypeUnsigned());
        assertEquals(128, message.getDemandFlagUnsigned());
    }

    @Test
    @DisplayName("测试标志常量")
    void testFlagConstants() {
        assertEquals(0x00, T0303InfoDemandCancel.FLAG_CANCEL);
        assertEquals(0x01, T0303InfoDemandCancel.FLAG_DEMAND);
    }

    @Test
    @DisplayName("测试操作判断方法")
    void testOperationMethods() {
        // 测试点播
        message.setDemandFlag(T0303InfoDemandCancel.FLAG_DEMAND);
        assertTrue(message.isDemand());
        assertFalse(message.isCancel());
        assertEquals("点播", message.getOperationDescription());
        
        // 测试取消
        message.setDemandFlag(T0303InfoDemandCancel.FLAG_CANCEL);
        assertFalse(message.isDemand());
        assertTrue(message.isCancel());
        assertEquals("取消", message.getOperationDescription());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        byte infoType = 0x12;
        byte demandFlag = T0303InfoDemandCancel.FLAG_DEMAND;
        
        message.setInfoType(infoType);
        message.setDemandFlag(demandFlag);
        
        String str = message.toString();
        assertTrue(str.contains("T0303InfoDemandCancel"));
        assertTrue(str.contains("infoType=18"));
        assertTrue(str.contains("0x12"));
        assertTrue(str.contains("点播"));
    }

    @Test
    @DisplayName("测试equals和hashCode")
    void testEqualsAndHashCode() {
        byte infoType = 0x30;
        byte demandFlag = T0303InfoDemandCancel.FLAG_CANCEL;
        
        T0303InfoDemandCancel msg1 = new T0303InfoDemandCancel(infoType, demandFlag);
        T0303InfoDemandCancel msg2 = new T0303InfoDemandCancel(infoType, demandFlag);
        T0303InfoDemandCancel msg3 = new T0303InfoDemandCancel((byte) 0x31, demandFlag);
        
        // 测试equals
        assertEquals(msg1, msg2);
        assertNotEquals(msg1, msg3);
        assertNotEquals(msg1, null);
        assertNotEquals(msg1, "not a message");
        
        // 测试hashCode
        assertEquals(msg1.hashCode(), msg2.hashCode());
        assertNotEquals(msg1.hashCode(), msg3.hashCode());
    }

    @Test
    @DisplayName("测试解码异常 - 空消息体")
    void testDecodeBodyWithNullBuffer() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> message.decodeBody(null)
        );
        assertTrue(exception.getMessage().contains("信息点播/取消消息体不能为空"));
    }

    @Test
    @DisplayName("测试解码异常 - 消息体长度不足")
    void testDecodeBodyWithInsufficientLength() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x01); // 只有1字节，需要2字节
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> message.decodeBody(buffer)
        );
        assertTrue(exception.getMessage().contains("至少需要2字节"));
    }

    @Test
    @DisplayName("测试消息工厂创建")
    void testFactoryCreation() {
        JT808Message created = factory.createMessage(0x0303);
        assertNotNull(created);
        assertInstanceOf(T0303InfoDemandCancel.class, created);
        assertEquals(0x0303, created.getMessageId());
    }

    @Test
    @DisplayName("测试消息工厂支持检查")
    void testFactorySupport() {
        assertTrue(factory.isSupported(0x0303));
        assertTrue(factory.getSupportedMessageIds().contains(0x0303));
    }

    @Test
    @DisplayName("测试边界值")
    void testBoundaryValues() {
        // 测试最小值
        message.setInfoType((byte) 0x00);
        message.setDemandFlag((byte) 0x00);
        assertEquals(0, message.getInfoTypeUnsigned());
        assertEquals(0, message.getDemandFlagUnsigned());
        assertTrue(message.isCancel());
        
        // 测试最大值
        message.setInfoType((byte) 0xFF);
        message.setDemandFlag((byte) 0xFF);
        assertEquals(255, message.getInfoTypeUnsigned());
        assertEquals(255, message.getDemandFlagUnsigned());
        assertFalse(message.isDemand()); // 0xFF != 0x01
        assertFalse(message.isCancel()); // 0xFF != 0x00
    }

    @Test
    @DisplayName("测试实际使用场景")
    void testRealWorldScenarios() {
        // 场景1：点播交通信息
        T0303InfoDemandCancel trafficDemand = T0303InfoDemandCancel.createDemand((byte) 0x01);
        assertEquals(1, trafficDemand.getInfoTypeUnsigned());
        assertTrue(trafficDemand.isDemand());
        
        // 场景2：取消天气信息
        T0303InfoDemandCancel weatherCancel = T0303InfoDemandCancel.createCancel((byte) 0x02);
        assertEquals(2, weatherCancel.getInfoTypeUnsigned());
        assertTrue(weatherCancel.isCancel());
        
        // 场景3：点播新闻信息
        T0303InfoDemandCancel newsDemand = new T0303InfoDemandCancel((byte) 0x03, T0303InfoDemandCancel.FLAG_DEMAND);
        assertEquals(3, newsDemand.getInfoTypeUnsigned());
        assertTrue(newsDemand.isDemand());
        assertEquals("点播", newsDemand.getOperationDescription());
    }
}