package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8604SetPolygonArea 设置多边形区域消息测试类
 * 
 * @author JT808-Vertx
 * @since 1.0.0
 */
class T8604SetPolygonAreaTest {
    
    @Test
    void testMessageId() {
        T8604SetPolygonArea message = new T8604SetPolygonArea();
        assertEquals(0x8604, message.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        T8604SetPolygonArea message = new T8604SetPolygonArea();
        assertNotNull(message);
        assertEquals(0, message.getAreaId());
        assertEquals(0, message.getAreaAttribute());
        assertEquals(0, message.getVertexCount());
        assertTrue(message.getVertices().isEmpty());
        assertNull(message.getStartTime());
        assertNull(message.getEndTime());
        assertNull(message.getMaxSpeed());
        assertNull(message.getOverspeedDuration());
    }
    
    @Test
    void testConstructorWithParameters() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 18, 0, 0);
        List<T8604SetPolygonArea.PolygonVertex> vertices = Arrays.asList(
            new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L),
            new T8604SetPolygonArea.PolygonVertex(39100000L, 116000000L),
            new T8604SetPolygonArea.PolygonVertex(39100000L, 116100000L),
            new T8604SetPolygonArea.PolygonVertex(39000000L, 116100000L)
        );
        
        T8604SetPolygonArea message = new T8604SetPolygonArea(1001L, 0x0003, startTime, endTime, vertices);
        
        assertEquals(1001L, message.getAreaId());
        assertEquals(0x0003, message.getAreaAttribute());
        assertEquals(startTime, message.getStartTime());
        assertEquals(endTime, message.getEndTime());
        assertEquals(4, message.getVertexCount());
        assertEquals(vertices, message.getVertices());
    }
    
    @Test
    void testPolygonVertexConstructor() {
        T8604SetPolygonArea.PolygonVertex vertex = new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L);
        assertEquals(39000000L, vertex.getLatitude());
        assertEquals(116000000L, vertex.getLongitude());
        assertEquals(39.0, vertex.getLatitudeDegrees(), 0.000001);
        assertEquals(116.0, vertex.getLongitudeDegrees(), 0.000001);
    }
    
    @Test
    void testPolygonVertexDegreesConversion() {
        T8604SetPolygonArea.PolygonVertex vertex = new T8604SetPolygonArea.PolygonVertex();
        
        vertex.setLatitudeDegrees(39.123456);
        vertex.setLongitudeDegrees(116.654321);
        
        assertEquals(39123456L, vertex.getLatitude());
        assertEquals(116654321L, vertex.getLongitude());
        assertEquals(39.123456, vertex.getLatitudeDegrees(), 0.000001);
        assertEquals(116.654321, vertex.getLongitudeDegrees(), 0.000001);
    }
    
    @Test
    void testAddVertex() {
        T8604SetPolygonArea message = new T8604SetPolygonArea();
        T8604SetPolygonArea.PolygonVertex vertex = new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L);
        
        message.addVertex(vertex);
        assertEquals(1, message.getVertexCount());
        assertTrue(message.getVertices().contains(vertex));
        
        // 测试添加null顶点
        message.addVertex(null);
        assertEquals(1, message.getVertexCount()); // 数量不应该变化
    }
    
    @Test
    void testAreaAttributes() {
        T8604SetPolygonArea message = new T8604SetPolygonArea();
        
        // 测试时间属性
        message.setAreaAttribute(T8604SetPolygonArea.ATTR_TIME_BASED);
        assertTrue(message.hasTimeAttribute());
        assertTrue(message.hasAttribute(T8604SetPolygonArea.ATTR_TIME_BASED));
        assertFalse(message.hasSpeedLimitAttribute());
        
        // 测试速度限制属性
        message.setAreaAttribute(T8604SetPolygonArea.ATTR_SPEED_LIMIT);
        assertTrue(message.hasSpeedLimitAttribute());
        assertTrue(message.hasAttribute(T8604SetPolygonArea.ATTR_SPEED_LIMIT));
        assertFalse(message.hasTimeAttribute());
        
        // 测试组合属性
        message.setAreaAttribute(T8604SetPolygonArea.ATTR_TIME_BASED | T8604SetPolygonArea.ATTR_SPEED_LIMIT);
        assertTrue(message.hasTimeAttribute());
        assertTrue(message.hasSpeedLimitAttribute());
    }
    
    @Test
    void testAreaAttributeDescription() {
        T8604SetPolygonArea message = new T8604SetPolygonArea();
        
        // 测试单个属性
        message.setAreaAttribute(T8604SetPolygonArea.ATTR_TIME_BASED);
        String description = message.getAreaAttributeDescription();
        assertTrue(description.contains("根据时间"));
        
        // 测试多个属性
        message.setAreaAttribute(T8604SetPolygonArea.ATTR_TIME_BASED | 
                               T8604SetPolygonArea.ATTR_SPEED_LIMIT | 
                               T8604SetPolygonArea.ATTR_ENTER_ALARM_DRIVER);
        description = message.getAreaAttributeDescription();
        assertTrue(description.contains("根据时间"));
        assertTrue(description.contains("限速"));
        assertTrue(description.contains("进区域报警给驾驶员"));
    }
    
    @Test
    void testEncodeBodyWithoutSpeedLimit() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 18, 0, 0);
        List<T8604SetPolygonArea.PolygonVertex> vertices = Arrays.asList(
            new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L),
            new T8604SetPolygonArea.PolygonVertex(39100000L, 116100000L)
        );
        
        T8604SetPolygonArea message = new T8604SetPolygonArea(1001L, 0x0000, startTime, endTime, vertices);
        Buffer encoded = message.encodeBody();
        
        // 验证编码长度：4(区域ID) + 2(区域属性) + 6(起始时间) + 6(结束时间) + 2(顶点数) + 16(2个顶点*8字节) = 36字节
        assertEquals(36, encoded.length());
        
        // 验证区域ID
        assertEquals(1001L, encoded.getUnsignedInt(0));
        
        // 验证区域属性
        assertEquals(0x0000, encoded.getUnsignedShort(4));
        
        // 验证顶点数
        assertEquals(2, encoded.getUnsignedShort(18));
    }
    
    @Test
    void testEncodeBodyWithSpeedLimit() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 18, 0, 0);
        List<T8604SetPolygonArea.PolygonVertex> vertices = Arrays.asList(
            new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L)
        );
        
        T8604SetPolygonArea message = new T8604SetPolygonArea(1001L, T8604SetPolygonArea.ATTR_SPEED_LIMIT, 
                                                             startTime, endTime, vertices);
        message.setMaxSpeed(80);
        message.setOverspeedDuration(10);
        
        Buffer encoded = message.encodeBody();
        
        // 验证编码长度：4+2+6+6+2+1+2+8 = 31字节
        assertEquals(31, encoded.length());
        
        // 验证最高速度
        assertEquals(80, encoded.getUnsignedShort(18));
        
        // 验证超速持续时间
        assertEquals(10, Byte.toUnsignedInt(encoded.getByte(20)));
        
        // 验证顶点数
        assertEquals(1, encoded.getUnsignedShort(21));
    }
    
    @Test
    void testDecodeBodyWithoutSpeedLimit() {
        // 创建测试数据
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedInt(1001L); // 区域ID
        buffer.appendUnsignedShort(0x0000); // 区域属性（无速度限制）
        buffer.appendBytes(new byte[]{0x24, 0x01, 0x01, 0x10, 0x00, 0x00}); // 起始时间 2024-01-01 10:00:00
        buffer.appendBytes(new byte[]{0x24, 0x12, 0x31, 0x18, 0x00, 0x00}); // 结束时间 2024-12-31 18:00:00
        buffer.appendUnsignedShort(2); // 顶点数
        buffer.appendUnsignedInt(39000000L); // 第一个顶点纬度
        buffer.appendUnsignedInt(116000000L); // 第一个顶点经度
        buffer.appendUnsignedInt(39100000L); // 第二个顶点纬度
        buffer.appendUnsignedInt(116100000L); // 第二个顶点经度
        
        T8604SetPolygonArea message = new T8604SetPolygonArea();
        message.decodeBody(buffer);
        
        assertEquals(1001L, message.getAreaId());
        assertEquals(0x0000, message.getAreaAttribute());
        assertFalse(message.hasSpeedLimitAttribute());
        assertEquals(2, message.getVertexCount());
        assertEquals(2, message.getVertices().size());
        
        List<T8604SetPolygonArea.PolygonVertex> vertices = message.getVertices();
        assertEquals(39000000L, vertices.get(0).getLatitude());
        assertEquals(116000000L, vertices.get(0).getLongitude());
        assertEquals(39100000L, vertices.get(1).getLatitude());
        assertEquals(116100000L, vertices.get(1).getLongitude());
    }
    
    @Test
    void testDecodeBodyWithSpeedLimit() {
        // 创建测试数据
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedInt(1001L); // 区域ID
        buffer.appendUnsignedShort(T8604SetPolygonArea.ATTR_SPEED_LIMIT); // 区域属性（有速度限制）
        buffer.appendBytes(new byte[]{0x24, 0x01, 0x01, 0x10, 0x00, 0x00}); // 起始时间
        buffer.appendBytes(new byte[]{0x24, 0x12, 0x31, 0x18, 0x00, 0x00}); // 结束时间
        buffer.appendUnsignedShort(80); // 最高速度
        buffer.appendByte((byte) 10); // 超速持续时间
        buffer.appendUnsignedShort(1); // 顶点数
        buffer.appendUnsignedInt(39000000L); // 顶点纬度
        buffer.appendUnsignedInt(116000000L); // 顶点经度
        
        T8604SetPolygonArea message = new T8604SetPolygonArea();
        message.decodeBody(buffer);
        
        assertEquals(1001L, message.getAreaId());
        assertEquals(T8604SetPolygonArea.ATTR_SPEED_LIMIT, message.getAreaAttribute());
        assertTrue(message.hasSpeedLimitAttribute());
        assertEquals(80, message.getMaxSpeed().intValue());
        assertEquals(10, message.getOverspeedDuration().intValue());
        assertEquals(1, message.getVertexCount());
    }
    
    @Test
    void testDecodeBodyInvalidLength() {
        // 测试消息体长度不足
        Buffer shortBuffer = Buffer.buffer();
        shortBuffer.appendBytes(new byte[10]); // 只有10字节，不足最小长度18字节
        
        T8604SetPolygonArea message = new T8604SetPolygonArea();
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(shortBuffer);
        });
    }
    
    @Test
    void testDecodeBodyMissingSpeedFields() {
        // 创建有速度限制属性但缺少速度字段的数据
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedInt(1001L); // 区域ID
        buffer.appendUnsignedShort(T8604SetPolygonArea.ATTR_SPEED_LIMIT); // 区域属性（有速度限制）
        buffer.appendBytes(new byte[]{0x24, 0x01, 0x01, 0x10, 0x00, 0x00}); // 起始时间
        buffer.appendBytes(new byte[]{0x24, 0x12, 0x31, 0x18, 0x00, 0x00}); // 结束时间
        // 缺少速度字段
        
        T8604SetPolygonArea message = new T8604SetPolygonArea();
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
    }
    
    @Test
    void testDecodeBodyInsufficientVertexData() {
        // 创建声明有2个顶点但只提供1个顶点数据的测试数据
        Buffer buffer = Buffer.buffer();
        buffer.appendUnsignedInt(1001L); // 区域ID
        buffer.appendUnsignedShort(0x0000); // 区域属性
        buffer.appendBytes(new byte[]{0x24, 0x01, 0x01, 0x10, 0x00, 0x00}); // 起始时间
        buffer.appendBytes(new byte[]{0x24, 0x12, 0x31, 0x18, 0x00, 0x00}); // 结束时间
        buffer.appendUnsignedShort(2); // 声明有2个顶点
        buffer.appendUnsignedInt(39000000L); // 只提供1个顶点的纬度
        buffer.appendUnsignedInt(116000000L); // 只提供1个顶点的经度
        // 缺少第二个顶点数据
        
        T8604SetPolygonArea message = new T8604SetPolygonArea();
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
    }
    
    @Test
    void testEncodeDecodeConsistency() {
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 15, 12, 30, 45);
        LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        List<T8604SetPolygonArea.PolygonVertex> vertices = Arrays.asList(
            new T8604SetPolygonArea.PolygonVertex(39123456L, 116654321L),
            new T8604SetPolygonArea.PolygonVertex(39234567L, 116765432L),
            new T8604SetPolygonArea.PolygonVertex(39345678L, 116876543L)
        );
        
        T8604SetPolygonArea original = new T8604SetPolygonArea(1001L, 
            T8604SetPolygonArea.ATTR_TIME_BASED | T8604SetPolygonArea.ATTR_SPEED_LIMIT, 
            startTime, endTime, vertices);
        original.setMaxSpeed(120);
        original.setOverspeedDuration(15);
        
        // 编码
        Buffer encoded = original.encodeBody();
        
        // 解码
        T8604SetPolygonArea decoded = new T8604SetPolygonArea();
        decoded.decodeBody(encoded);
        
        // 验证一致性
        assertEquals(original.getAreaId(), decoded.getAreaId());
        assertEquals(original.getAreaAttribute(), decoded.getAreaAttribute());
        assertEquals(original.getMaxSpeed(), decoded.getMaxSpeed());
        assertEquals(original.getOverspeedDuration(), decoded.getOverspeedDuration());
        assertEquals(original.getVertexCount(), decoded.getVertexCount());
        assertEquals(original.getVertices(), decoded.getVertices());
        assertEquals(original.hasSpeedLimitAttribute(), decoded.hasSpeedLimitAttribute());
        assertEquals(original.hasTimeAttribute(), decoded.hasTimeAttribute());
    }
    
    @Test
    void testPolygonVertexEncodeDecodeConsistency() {
        T8604SetPolygonArea.PolygonVertex original = new T8604SetPolygonArea.PolygonVertex(39123456L, 116654321L);
        
        // 编码
        Buffer encoded = original.encode();
        assertEquals(8, encoded.length()); // 4字节纬度 + 4字节经度
        
        // 解码
        T8604SetPolygonArea.PolygonVertex decoded = new T8604SetPolygonArea.PolygonVertex();
        int nextIndex = decoded.decode(encoded, 0);
        
        assertEquals(8, nextIndex);
        assertEquals(original.getLatitude(), decoded.getLatitude());
        assertEquals(original.getLongitude(), decoded.getLongitude());
        assertEquals(original, decoded);
    }
    
    @Test
    void testGetDescription() {
        T8604SetPolygonArea message = new T8604SetPolygonArea();
        message.setAreaId(1001L);
        message.setVertexCount(4);
        
        String description = message.getDescription();
        assertTrue(description.contains("设置多边形区域消息"));
        assertTrue(description.contains("1001"));
        assertTrue(description.contains("4"));
    }
    
    @Test
    void testToString() {
        List<T8604SetPolygonArea.PolygonVertex> vertices = Arrays.asList(
            new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L)
        );
        
        T8604SetPolygonArea message = new T8604SetPolygonArea(1001L, 0x0003, null, null, vertices);
        String str = message.toString();
        
        assertTrue(str.contains("T8604SetPolygonArea"));
        assertTrue(str.contains("areaId=1001"));
        assertTrue(str.contains("areaAttribute=0x0003"));
        assertTrue(str.contains("vertexCount=1"));
    }
    
    @Test
    void testPolygonVertexToString() {
        T8604SetPolygonArea.PolygonVertex vertex = new T8604SetPolygonArea.PolygonVertex(39123456L, 116654321L);
        String str = vertex.toString();
        
        assertTrue(str.contains("PolygonVertex"));
        assertTrue(str.contains("latitude=39123456"));
        assertTrue(str.contains("longitude=116654321"));
        assertTrue(str.contains("39.123456"));
        assertTrue(str.contains("116.654321"));
    }
    
    @Test
    void testEquals() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 18, 0, 0);
        List<T8604SetPolygonArea.PolygonVertex> vertices1 = Arrays.asList(
            new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L)
        );
        List<T8604SetPolygonArea.PolygonVertex> vertices2 = Arrays.asList(
            new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L)
        );
        List<T8604SetPolygonArea.PolygonVertex> vertices3 = Arrays.asList(
            new T8604SetPolygonArea.PolygonVertex(39100000L, 116100000L)
        );
        
        T8604SetPolygonArea message1 = new T8604SetPolygonArea(1001L, 0x0003, startTime, endTime, vertices1);
        T8604SetPolygonArea message2 = new T8604SetPolygonArea(1001L, 0x0003, startTime, endTime, vertices2);
        T8604SetPolygonArea message3 = new T8604SetPolygonArea(1002L, 0x0003, startTime, endTime, vertices3);
        
        // 测试相等性
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
        
        // 测试自反性
        assertEquals(message1, message1);
    }
    
    @Test
    void testPolygonVertexEquals() {
        T8604SetPolygonArea.PolygonVertex vertex1 = new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L);
        T8604SetPolygonArea.PolygonVertex vertex2 = new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L);
        T8604SetPolygonArea.PolygonVertex vertex3 = new T8604SetPolygonArea.PolygonVertex(39100000L, 116100000L);
        
        assertEquals(vertex1, vertex2);
        assertNotEquals(vertex1, vertex3);
        assertNotEquals(vertex1, null);
        assertNotEquals(vertex1, "not a vertex");
        assertEquals(vertex1, vertex1);
    }
    
    @Test
    void testHashCode() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 18, 0, 0);
        List<T8604SetPolygonArea.PolygonVertex> vertices1 = Arrays.asList(
            new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L)
        );
        List<T8604SetPolygonArea.PolygonVertex> vertices2 = Arrays.asList(
            new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L)
        );
        
        T8604SetPolygonArea message1 = new T8604SetPolygonArea(1001L, 0x0003, startTime, endTime, vertices1);
        T8604SetPolygonArea message2 = new T8604SetPolygonArea(1001L, 0x0003, startTime, endTime, vertices2);
        
        assertEquals(message1.hashCode(), message2.hashCode());
    }
    
    @Test
    void testPolygonVertexHashCode() {
        T8604SetPolygonArea.PolygonVertex vertex1 = new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L);
        T8604SetPolygonArea.PolygonVertex vertex2 = new T8604SetPolygonArea.PolygonVertex(39000000L, 116000000L);
        
        assertEquals(vertex1.hashCode(), vertex2.hashCode());
    }
    
    @Test
    void testMessageFactoryIntegration() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 测试工厂是否支持该消息类型
        assertTrue(factory.isSupported(0x8604));
        
        // 测试工厂创建消息实例
        JT808Message message = factory.createMessage(0x8604);
        assertNotNull(message);
        assertTrue(message instanceof T8604SetPolygonArea);
        assertEquals(0x8604, message.getMessageId());
    }
    
    @Test
    void testAttributeConstants() {
        assertEquals(0x0001, T8604SetPolygonArea.ATTR_TIME_BASED);
        assertEquals(0x0002, T8604SetPolygonArea.ATTR_SPEED_LIMIT);
        assertEquals(0x0004, T8604SetPolygonArea.ATTR_ENTER_ALARM_DRIVER);
        assertEquals(0x0008, T8604SetPolygonArea.ATTR_ENTER_ALARM_PLATFORM);
        assertEquals(0x0010, T8604SetPolygonArea.ATTR_EXIT_ALARM_DRIVER);
        assertEquals(0x0020, T8604SetPolygonArea.ATTR_EXIT_ALARM_PLATFORM);
        assertEquals(0x0040, T8604SetPolygonArea.ATTR_SOUTH_LATITUDE);
        assertEquals(0x0080, T8604SetPolygonArea.ATTR_WEST_LONGITUDE);
        assertEquals(0x0100, T8604SetPolygonArea.ATTR_DOOR_FORBIDDEN);
        assertEquals(0x4000, T8604SetPolygonArea.ATTR_ENTER_CLOSE_COMM);
        assertEquals(0x8000, T8604SetPolygonArea.ATTR_ENTER_COLLECT_GNSS);
    }
    
    @Test
    void testBoundaryValues() {
        T8604SetPolygonArea message = new T8604SetPolygonArea();
        
        // 测试最大区域ID
        message.setAreaId(0xFFFFFFFFL);
        assertEquals(0xFFFFFFFFL, message.getAreaId());
        
        // 测试最大区域属性
        message.setAreaAttribute(0xFFFF);
        assertEquals(0xFFFF, message.getAreaAttribute());
        
        // 测试顶点坐标边界值
        T8604SetPolygonArea.PolygonVertex vertex = new T8604SetPolygonArea.PolygonVertex();
        vertex.setLatitude(0xFFFFFFFFL);
        vertex.setLongitude(0xFFFFFFFFL);
        assertEquals(0xFFFFFFFFL, vertex.getLatitude());
        assertEquals(0xFFFFFFFFL, vertex.getLongitude());
    }
    
    @Test
    void testRealWorldScenario() {
        // 模拟真实场景：设置北京市某区域的多边形电子围栏
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 8, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 20, 0, 0);
        
        // 定义一个矩形区域的四个顶点（北京市中心某区域）
        List<T8604SetPolygonArea.PolygonVertex> vertices = Arrays.asList(
            new T8604SetPolygonArea.PolygonVertex(39904200L, 116407396L), // 天安门广场附近
            new T8604SetPolygonArea.PolygonVertex(39904200L, 116417396L), // 东北角
            new T8604SetPolygonArea.PolygonVertex(39894200L, 116417396L), // 东南角
            new T8604SetPolygonArea.PolygonVertex(39894200L, 116407396L)  // 西南角
        );
        
        T8604SetPolygonArea message = new T8604SetPolygonArea(2001L, 
            T8604SetPolygonArea.ATTR_TIME_BASED | 
            T8604SetPolygonArea.ATTR_SPEED_LIMIT | 
            T8604SetPolygonArea.ATTR_ENTER_ALARM_PLATFORM | 
            T8604SetPolygonArea.ATTR_EXIT_ALARM_PLATFORM, 
            startTime, endTime, vertices);
        message.setMaxSpeed(60); // 限速60km/h
        message.setOverspeedDuration(5); // 超速持续5秒报警
        
        // 验证消息属性
        assertEquals(0x8604, message.getMessageId());
        assertEquals(2001L, message.getAreaId());
        assertTrue(message.hasTimeAttribute());
        assertTrue(message.hasSpeedLimitAttribute());
        assertTrue(message.hasAttribute(T8604SetPolygonArea.ATTR_ENTER_ALARM_PLATFORM));
        assertTrue(message.hasAttribute(T8604SetPolygonArea.ATTR_EXIT_ALARM_PLATFORM));
        assertEquals(60, message.getMaxSpeed().intValue());
        assertEquals(5, message.getOverspeedDuration().intValue());
        assertEquals(4, message.getVertexCount());
        
        // 编码消息
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 0);
        
        // 解码消息
        T8604SetPolygonArea decoded = new T8604SetPolygonArea();
        decoded.decodeBody(encoded);
        
        // 验证解码结果
        assertEquals(message.getAreaId(), decoded.getAreaId());
        assertEquals(message.getAreaAttribute(), decoded.getAreaAttribute());
        assertEquals(message.getMaxSpeed(), decoded.getMaxSpeed());
        assertEquals(message.getOverspeedDuration(), decoded.getOverspeedDuration());
        assertEquals(message.getVertexCount(), decoded.getVertexCount());
        assertEquals(message.getVertices(), decoded.getVertices());
        assertEquals(message, decoded);
        
        // 验证描述信息
        String description = decoded.getDescription();
        assertTrue(description.contains("设置多边形区域消息"));
        assertTrue(description.contains("2001"));
        assertTrue(description.contains("4"));
        
        String attrDescription = decoded.getAreaAttributeDescription();
        assertTrue(attrDescription.contains("根据时间"));
        assertTrue(attrDescription.contains("限速"));
        assertTrue(attrDescription.contains("进区域报警给平台"));
        assertTrue(attrDescription.contains("出区域报警给平台"));
    }
}