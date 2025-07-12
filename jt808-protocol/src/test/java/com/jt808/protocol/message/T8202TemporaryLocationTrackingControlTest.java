package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8202临时位置跟踪控制消息测试类
 *
 * @author JT808 Protocol Team
 * @version 1.0
 * @since 1.0
 */
@DisplayName("T8202临时位置跟踪控制消息测试")
class T8202TemporaryLocationTrackingControlTest {

    private T8202TemporaryLocationTrackingControl message;

    @BeforeEach
    void setUp() {
        message = new T8202TemporaryLocationTrackingControl();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x8202, message.getMessageId());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        assertEquals(0, message.getTimeInterval());
        assertEquals(0, message.getValidityPeriod());
        assertTrue(message.isStopTracking());
    }

    @Test
    @DisplayName("测试带参数构造函数")
    void testParameterizedConstructor() {
        T8202TemporaryLocationTrackingControl msg = new T8202TemporaryLocationTrackingControl(30, 3600);
        assertEquals(30, msg.getTimeInterval());
        assertEquals(3600, msg.getValidityPeriod());
        assertFalse(msg.isStopTracking());
    }

    @Test
    @DisplayName("测试编码消息体 - 开始跟踪")
    void testEncodeBodyStartTracking() {
        message.setTimeInterval(30);  // 30秒间隔
        message.setValidityPeriod(3600);  // 1小时有效期

        Buffer encoded = message.encodeBody();

        assertEquals(6, encoded.length());

        // 验证时间间隔 (WORD, 大端序)
        assertEquals(30, encoded.getUnsignedShort(0));

        // 验证有效期 (DWORD, 大端序)
        assertEquals(3600, encoded.getUnsignedInt(2));
    }

    @Test
    @DisplayName("测试编码消息体 - 停止跟踪")
    void testEncodeBodyStopTracking() {
        message.setTimeInterval(0);  // 停止跟踪
        message.setValidityPeriod(0);

        Buffer encoded = message.encodeBody();

        assertEquals(6, encoded.length());
        assertEquals(0, encoded.getUnsignedShort(0));
        assertEquals(0, encoded.getUnsignedInt(2));
    }

    @Test
    @DisplayName("测试编码消息体 - 边界值")
    void testEncodeBodyBoundaryValues() {
        // 测试最大值
        message.setTimeInterval(65535);  // WORD最大值
        message.setValidityPeriod(4294967295L);  // DWORD最大值

        Buffer encoded = message.encodeBody();

        assertEquals(6, encoded.length());
        assertEquals(65535, encoded.getUnsignedShort(0));
        assertEquals(4294967295L, encoded.getUnsignedInt(2));
    }

    @Test
    @DisplayName("测试解码消息体 - 开始跟踪")
    void testDecodeBodyStartTracking() {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedShort(60);    // 60秒间隔
        buffer.appendUnsignedInt(7200);    // 2小时有效期

        message.decodeBody(buffer);

        assertEquals(60, message.getTimeInterval());
        assertEquals(7200, message.getValidityPeriod());
        assertFalse(message.isStopTracking());
    }

    @Test
    @DisplayName("测试解码消息体 - 停止跟踪")
    void testDecodeBodyStopTracking() {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedShort(0);     // 停止跟踪
        buffer.appendUnsignedInt(0);

        message.decodeBody(buffer);

        assertEquals(0, message.getTimeInterval());
        assertEquals(0, message.getValidityPeriod());
        assertTrue(message.isStopTracking());
    }

    @Test
    @DisplayName("测试解码消息体 - 边界值")
    void testDecodeBodyBoundaryValues() {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedShort(65535);         // WORD最大值
        buffer.appendUnsignedInt(4294967295L);     // DWORD最大值

        message.decodeBody(buffer);

        assertEquals(65535, message.getTimeInterval());
        assertEquals(4294967295L, message.getValidityPeriod());
    }

    @Test
    @DisplayName("测试解码异常 - 空消息体")
    void testDecodeBodyNullException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> message.decodeBody(null)
        );
        assertEquals("消息体不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("测试解码异常 - 消息体长度不足")
    void testDecodeBodyInsufficientLength() {
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedShort(30);  // 只有2字节，缺少4字节的有效期

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> message.decodeBody(buffer)
        );
        assertTrue(exception.getMessage().contains("临时位置跟踪控制消息体长度应为6字节"));
    }

    @Test
    @DisplayName("测试编解码一致性")
    void testEncodeDecodeConsistency() {
        // 设置原始数据
        message.setTimeInterval(120);
        message.setValidityPeriod(86400);  // 24小时

        // 编码
        Buffer encoded = message.encodeBody();

        // 解码到新对象
        T8202TemporaryLocationTrackingControl decoded = new T8202TemporaryLocationTrackingControl();
        decoded.decodeBody(encoded);

        // 验证一致性
        assertEquals(message.getTimeInterval(), decoded.getTimeInterval());
        assertEquals(message.getValidityPeriod(), decoded.getValidityPeriod());
        assertEquals(message.isStopTracking(), decoded.isStopTracking());
    }

    @Test
    @DisplayName("测试工厂方法 - 创建开始跟踪")
    void testCreateStartTracking() {
        T8202TemporaryLocationTrackingControl msg =
                T8202TemporaryLocationTrackingControl.createStartTracking(45, 1800);

        assertEquals(45, msg.getTimeInterval());
        assertEquals(1800, msg.getValidityPeriod());
        assertFalse(msg.isStopTracking());
    }

    @Test
    @DisplayName("测试工厂方法 - 创建停止跟踪")
    void testCreateStopTracking() {
        T8202TemporaryLocationTrackingControl msg =
                T8202TemporaryLocationTrackingControl.createStopTracking();

        assertEquals(0, msg.getTimeInterval());
        assertEquals(0, msg.getValidityPeriod());
        assertTrue(msg.isStopTracking());
    }

    @Test
    @DisplayName("测试工厂方法异常 - 无效时间间隔")
    void testCreateStartTrackingInvalidInterval() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> T8202TemporaryLocationTrackingControl.createStartTracking(0, 3600)
        );
        assertEquals("时间间隔必须大于0秒", exception.getMessage());

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> T8202TemporaryLocationTrackingControl.createStartTracking(-1, 3600)
        );
        assertEquals("时间间隔必须大于0秒", exception.getMessage());
    }

    @Test
    @DisplayName("测试工厂方法异常 - 无效有效期")
    void testCreateStartTrackingInvalidValidity() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> T8202TemporaryLocationTrackingControl.createStartTracking(30, 0)
        );
        assertEquals("有效期必须大于0秒", exception.getMessage());

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> T8202TemporaryLocationTrackingControl.createStartTracking(30, -1)
        );
        assertEquals("有效期必须大于0秒", exception.getMessage());
    }

    @Test
    @DisplayName("测试Setter异常 - 负数时间间隔")
    void testSetTimeIntervalNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> message.setTimeInterval(-1)
        );
        assertEquals("时间间隔不能为负数", exception.getMessage());
    }

    @Test
    @DisplayName("测试Setter异常 - 负数有效期")
    void testSetValidityPeriodNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> message.setValidityPeriod(-1)
        );
        assertEquals("有效期不能为负数", exception.getMessage());
    }

    @Test
    @DisplayName("测试equals方法")
    void testEquals() {
        T8202TemporaryLocationTrackingControl msg1 = new T8202TemporaryLocationTrackingControl(30, 3600);
        T8202TemporaryLocationTrackingControl msg2 = new T8202TemporaryLocationTrackingControl(30, 3600);
        T8202TemporaryLocationTrackingControl msg3 = new T8202TemporaryLocationTrackingControl(60, 3600);

        assertEquals(msg1, msg2);
        assertNotEquals(msg1, msg3);
        assertNotEquals(msg1, null);
        assertNotEquals(msg1, "not a message");
        assertEquals(msg1, msg1);
    }

    @Test
    @DisplayName("测试hashCode方法")
    void testHashCode() {
        T8202TemporaryLocationTrackingControl msg1 = new T8202TemporaryLocationTrackingControl(30, 3600);
        T8202TemporaryLocationTrackingControl msg2 = new T8202TemporaryLocationTrackingControl(30, 3600);
        T8202TemporaryLocationTrackingControl msg3 = new T8202TemporaryLocationTrackingControl(60, 3600);

        assertEquals(msg1.hashCode(), msg2.hashCode());
        assertNotEquals(msg1.hashCode(), msg3.hashCode());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        message.setTimeInterval(30);
        message.setValidityPeriod(3600);

        String str = message.toString();

        assertTrue(str.contains("T8202TemporaryLocationTrackingControl"));
        assertTrue(str.contains("messageId=0x8202"));
        assertTrue(str.contains("timeInterval=30"));
        assertTrue(str.contains("validityPeriod=3600"));
        assertTrue(str.contains("临时位置跟踪控制"));
    }

    @Test
    @DisplayName("测试跟踪状态描述")
    void testTrackingStatusDescription() {
        // 停止跟踪
        message.setTimeInterval(0);
        assertEquals("停止跟踪", message.getTrackingStatusDescription());

        // 开始跟踪
        message.setTimeInterval(30);
        message.setValidityPeriod(3600);
        assertEquals("开始跟踪 - 间隔:30秒, 有效期:3600秒", message.getTrackingStatusDescription());
    }

    @Test
    @DisplayName("测试消息描述")
    void testMessageDescription() {
        assertEquals("临时位置跟踪控制", message.getMessageDescription());
    }
}