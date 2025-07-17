package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8602SetRectangularArea 测试类
 * 
 * @author JT808-Vertx
 */
class T8602SetRectangularAreaTest {
    
    @Test
    void testMessageId() {
        T8602SetRectangularArea message = new T8602SetRectangularArea();
        assertEquals(0x8602, message.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        T8602SetRectangularArea message = new T8602SetRectangularArea();
        assertEquals(0, message.getAreaCount());
        assertEquals(0, message.getUnsignedAreaCount());
        assertTrue(message.getAreaItems().isEmpty());
        assertEquals("更新区域", message.getSettingAttributeDescription());
    }
    
    @Test
    void testConstructorWithAreaItems() {
        List<T8602SetRectangularArea.RectangularAreaItem> items = Arrays.asList(
            new T8602SetRectangularArea.RectangularAreaItem(1001, 0x0003, 
                116000000, 39000000, 116100000, 39100000),
            new T8602SetRectangularArea.RectangularAreaItem(1002, 0x0001, 
                117000000, 40000000, 117100000, 40100000)
        );
        
        T8602SetRectangularArea message = new T8602SetRectangularArea(T8602SetRectangularArea.SETTING_APPEND, items);
        assertEquals(T8602SetRectangularArea.SETTING_APPEND, message.getSettingAttribute());
        assertEquals(2, message.getAreaCount());
        assertEquals(2, message.getUnsignedAreaCount());
        assertEquals(2, message.getAreaItems().size());
        assertEquals("追加区域", message.getSettingAttributeDescription());
    }
    
    @Test
    void testCreateUpdate() {
        List<T8602SetRectangularArea.RectangularAreaItem> items = Arrays.asList(
            new T8602SetRectangularArea.RectangularAreaItem(1001, 0x0002, 
                116000000, 39000000, 116100000, 39100000)
        );
        
        T8602SetRectangularArea message = T8602SetRectangularArea.createUpdate(items);
        assertEquals(T8602SetRectangularArea.SETTING_UPDATE, message.getSettingAttribute());
        assertEquals(1, message.getAreaCount());
        assertEquals("更新区域", message.getSettingAttributeDescription());
    }
    
    @Test
    void testCreateAppend() {
        List<T8602SetRectangularArea.RectangularAreaItem> items = Arrays.asList(
            new T8602SetRectangularArea.RectangularAreaItem(1001, 0x0001, 
                116000000, 39000000, 116100000, 39100000)
        );
        
        T8602SetRectangularArea message = T8602SetRectangularArea.createAppend(items);
        assertEquals(T8602SetRectangularArea.SETTING_APPEND, message.getSettingAttribute());
        assertEquals("追加区域", message.getSettingAttributeDescription());
    }
    
    @Test
    void testCreateModify() {
        List<T8602SetRectangularArea.RectangularAreaItem> items = Arrays.asList(
            new T8602SetRectangularArea.RectangularAreaItem(1001, 0x0002, 
                116000000, 39000000, 116100000, 39100000)
        );
        
        T8602SetRectangularArea message = T8602SetRectangularArea.createModify(items);
        assertEquals(T8602SetRectangularArea.SETTING_MODIFY, message.getSettingAttribute());
        assertEquals("修改区域", message.getSettingAttributeDescription());
    }
    
    @Test
    void testAddAreaItem() {
        T8602SetRectangularArea message = new T8602SetRectangularArea();
        assertEquals(0, message.getAreaCount());
        
        T8602SetRectangularArea.RectangularAreaItem item = new T8602SetRectangularArea.RectangularAreaItem(
            1001, 0x0001, 116000000, 39000000, 116100000, 39100000);
        message.addAreaItem(item);
        
        assertEquals(1, message.getAreaCount());
        assertEquals(1, message.getAreaItems().size());
        
        // 测试添加null
        message.addAreaItem(null);
        assertEquals(1, message.getAreaCount());
    }
    
    @Test
    void testEncodeBodyBasic() {
        T8602SetRectangularArea.RectangularAreaItem item = new T8602SetRectangularArea.RectangularAreaItem(
            1001, 0x0000, 116000000, 39000000, 116100000, 39100000);
        
        T8602SetRectangularArea message = T8602SetRectangularArea.createUpdate(Arrays.asList(item));
        Buffer buffer = message.encodeBody();
        
        // 验证基本结构：设置属性(1) + 区域总数(1) + 区域项(22字节基础)
        assertTrue(buffer.length() >= 24);
        assertEquals(T8602SetRectangularArea.SETTING_UPDATE, buffer.getByte(0));
        assertEquals(1, buffer.getByte(1));
    }
    
    @Test
    void testEncodeBodyWithSpeedLimit() {
        T8602SetRectangularArea.RectangularAreaItem item = new T8602SetRectangularArea.RectangularAreaItem(
            1001, 0x0002, 116000000, 39000000, 116100000, 39100000); // 有限速属性
        item.setMaxSpeed(80);
        item.setOverspeedDuration(10);
        
        T8602SetRectangularArea message = T8602SetRectangularArea.createUpdate(Arrays.asList(item));
        Buffer buffer = message.encodeBody();
        
        // 验证包含限速字段：基础22字节 + 设置属性1字节 + 区域总数1字节 + 最高速度2字节 + 超速持续时间1字节 = 27字节
        assertEquals(27, buffer.length());
    }
    
    @Test
    void testDecodeBodyBasic() {
        // 创建测试数据
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) T8602SetRectangularArea.SETTING_APPEND); // 设置属性
        buffer.appendByte((byte) 1); // 区域总数
        
        // 区域项数据
        buffer.appendUnsignedInt(1001); // 区域ID
        buffer.appendUnsignedShort(0x0000); // 区域属性
        buffer.appendUnsignedInt(116000000); // 左上点纬度
        buffer.appendUnsignedInt(39000000); // 左上点经度
        buffer.appendUnsignedInt(116100000); // 右下点纬度
        buffer.appendUnsignedInt(39100000); // 右下点经度
        buffer.appendBytes(new byte[12]); // 起始时间和结束时间（各6字节）
        
        T8602SetRectangularArea message = new T8602SetRectangularArea();
        message.decodeBody(buffer);
        
        assertEquals(T8602SetRectangularArea.SETTING_APPEND, message.getSettingAttribute());
        assertEquals(1, message.getAreaCount());
        assertEquals(1, message.getAreaItems().size());
        
        T8602SetRectangularArea.RectangularAreaItem item = message.getAreaItems().get(0);
        assertEquals(1001, item.getAreaId());
        assertEquals(0x0000, item.getAreaAttribute());
        assertEquals(116000000, item.getTopLeftLatitude());
        assertEquals(39000000, item.getTopLeftLongitude());
        assertEquals(116100000, item.getBottomRightLatitude());
        assertEquals(39100000, item.getBottomRightLongitude());
    }
    
    @Test
    void testDecodeBodyWithSpeedLimit() {
        // 创建测试数据
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) T8602SetRectangularArea.SETTING_MODIFY); // 设置属性
        buffer.appendByte((byte) 1); // 区域总数
        
        // 区域项数据
        buffer.appendUnsignedInt(1002); // 区域ID
        buffer.appendUnsignedShort(0x0002); // 区域属性（有限速）
        buffer.appendUnsignedInt(117000000); // 左上点纬度
        buffer.appendUnsignedInt(40000000); // 左上点经度
        buffer.appendUnsignedInt(117100000); // 右下点纬度
        buffer.appendUnsignedInt(40100000); // 右下点经度
        buffer.appendBytes(new byte[12]); // 起始时间和结束时间（各6字节）
        buffer.appendUnsignedShort(80); // 最高速度
        buffer.appendByte((byte) 10); // 超速持续时间
        
        T8602SetRectangularArea message = new T8602SetRectangularArea();
        message.decodeBody(buffer);
        
        assertEquals(T8602SetRectangularArea.SETTING_MODIFY, message.getSettingAttribute());
        assertEquals(1, message.getAreaCount());
        
        T8602SetRectangularArea.RectangularAreaItem item = message.getAreaItems().get(0);
        assertEquals(1002, item.getAreaId());
        assertEquals(0x0002, item.getAreaAttribute());
        assertTrue(item.hasSpeedLimitAttribute());
        assertEquals(80, item.getMaxSpeed().intValue());
        assertEquals(10, item.getOverspeedDuration().intValue());
    }
    
    @Test
    void testDecodeBodyInvalidLength() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0); // 只有1字节，不足最小长度
        
        T8602SetRectangularArea message = new T8602SetRectangularArea();
        assertThrows(IllegalArgumentException.class, () -> message.decodeBody(buffer));
    }
    
    @Test
    void testEncodeDecodeConsistency() {
        // 测试基础区域项的编解码一致性
        T8602SetRectangularArea.RectangularAreaItem item1 = new T8602SetRectangularArea.RectangularAreaItem(
            1001, 0x0001, 116000000, 39000000, 116100000, 39100000);
        item1.setStartTime(LocalDateTime.of(2023, 12, 25, 10, 30, 45));
        item1.setEndTime(LocalDateTime.of(2023, 12, 25, 18, 30, 45));
        
        T8602SetRectangularArea original = T8602SetRectangularArea.createAppend(Arrays.asList(item1));
        Buffer buffer = original.encodeBody();
        
        T8602SetRectangularArea decoded = new T8602SetRectangularArea();
        decoded.decodeBody(buffer);
        
        assertEquals(original.getSettingAttribute(), decoded.getSettingAttribute());
        assertEquals(original.getAreaCount(), decoded.getAreaCount());
        assertEquals(original.getAreaItems().size(), decoded.getAreaItems().size());
        
        T8602SetRectangularArea.RectangularAreaItem decodedItem = decoded.getAreaItems().get(0);
        assertEquals(item1.getAreaId(), decodedItem.getAreaId());
        assertEquals(item1.getAreaAttribute(), decodedItem.getAreaAttribute());
        assertEquals(item1.getTopLeftLatitude(), decodedItem.getTopLeftLatitude());
        assertEquals(item1.getTopLeftLongitude(), decodedItem.getTopLeftLongitude());
        assertEquals(item1.getBottomRightLatitude(), decodedItem.getBottomRightLatitude());
        assertEquals(item1.getBottomRightLongitude(), decodedItem.getBottomRightLongitude());
        assertEquals(item1.getStartTime(), decodedItem.getStartTime());
        assertEquals(item1.getEndTime(), decodedItem.getEndTime());
        
        // 测试带限速的区域项编解码一致性
        T8602SetRectangularArea.RectangularAreaItem item2 = new T8602SetRectangularArea.RectangularAreaItem(
            1002, 0x0002, 117000000, 40000000, 117100000, 40100000);
        item2.setMaxSpeed(60);
        item2.setOverspeedDuration(15);
        
        T8602SetRectangularArea original2 = T8602SetRectangularArea.createModify(Arrays.asList(item2));
        Buffer buffer2 = original2.encodeBody();
        
        T8602SetRectangularArea decoded2 = new T8602SetRectangularArea();
        decoded2.decodeBody(buffer2);
        
        T8602SetRectangularArea.RectangularAreaItem decodedItem2 = decoded2.getAreaItems().get(0);
        assertEquals(item2.getMaxSpeed(), decodedItem2.getMaxSpeed());
        assertEquals(item2.getOverspeedDuration(), decodedItem2.getOverspeedDuration());
    }
    
    @Test
    void testRectangularAreaItemAttributes() {
        T8602SetRectangularArea.RectangularAreaItem item = new T8602SetRectangularArea.RectangularAreaItem();
        
        // 测试时间属性
        item.setAreaAttribute(T8602SetRectangularArea.ATTR_TIME_BASED);
        assertTrue(item.hasTimeAttribute());
        assertTrue(item.hasAttribute(T8602SetRectangularArea.ATTR_TIME_BASED));
        assertFalse(item.hasSpeedLimitAttribute());
        
        // 测试限速属性
        item.setAreaAttribute(T8602SetRectangularArea.ATTR_SPEED_LIMIT);
        assertTrue(item.hasSpeedLimitAttribute());
        assertFalse(item.hasTimeAttribute());
        
        // 测试组合属性
        item.setAreaAttribute(T8602SetRectangularArea.ATTR_TIME_BASED | T8602SetRectangularArea.ATTR_SPEED_LIMIT);
        assertTrue(item.hasTimeAttribute());
        assertTrue(item.hasSpeedLimitAttribute());
    }
    
    @Test
    void testRectangularAreaItemDescription() {
        T8602SetRectangularArea.RectangularAreaItem item = new T8602SetRectangularArea.RectangularAreaItem();
        
        // 测试无属性
        item.setAreaAttribute(0);
        assertEquals("无", item.getAreaAttributeDescription());
        
        // 测试单个属性
        item.setAreaAttribute(T8602SetRectangularArea.ATTR_TIME_BASED);
        assertEquals("根据时间", item.getAreaAttributeDescription());
        
        // 测试多个属性
        item.setAreaAttribute(T8602SetRectangularArea.ATTR_TIME_BASED | T8602SetRectangularArea.ATTR_SPEED_LIMIT);
        String description = item.getAreaAttributeDescription();
        assertTrue(description.contains("根据时间"));
        assertTrue(description.contains("限速"));
    }
    
    @Test
    void testGetDescription() {
        T8602SetRectangularArea message = new T8602SetRectangularArea();
        message.setSettingAttribute(T8602SetRectangularArea.SETTING_UPDATE);
        message.setAreaCount((byte) 2);
        
        String description = message.getDescription();
        assertTrue(description.contains("设置矩形区域"));
        assertTrue(description.contains("更新区域"));
        assertTrue(description.contains("2"));
    }
    
    @Test
    void testToString() {
        T8602SetRectangularArea.RectangularAreaItem item = new T8602SetRectangularArea.RectangularAreaItem(
            1001, 0x0003, 116000000, 39000000, 116100000, 39100000);
        item.setStartTime(LocalDateTime.of(2023, 12, 25, 10, 30, 45));
        item.setEndTime(LocalDateTime.of(2023, 12, 25, 18, 30, 45));
        item.setMaxSpeed(80);
        item.setOverspeedDuration(10);
        
        T8602SetRectangularArea message = T8602SetRectangularArea.createAppend(Arrays.asList(item));
        
        String messageStr = message.toString();
        assertTrue(messageStr.contains("T8602SetRectangularArea"));
        assertTrue(messageStr.contains("0x8602"));
        assertTrue(messageStr.contains("追加区域"));
        
        String itemStr = item.toString();
        assertTrue(itemStr.contains("RectangularAreaItem"));
        assertTrue(itemStr.contains("1001"));
        assertTrue(itemStr.contains("116000000"));
        assertTrue(itemStr.contains("2023-12-25 10:30:45"));
    }
    
    @Test
    void testEquals() {
        T8602SetRectangularArea.RectangularAreaItem item1 = new T8602SetRectangularArea.RectangularAreaItem(
            1001, 0x0001, 116000000, 39000000, 116100000, 39100000);
        T8602SetRectangularArea.RectangularAreaItem item2 = new T8602SetRectangularArea.RectangularAreaItem(
            1001, 0x0001, 116000000, 39000000, 116100000, 39100000);
        T8602SetRectangularArea.RectangularAreaItem item3 = new T8602SetRectangularArea.RectangularAreaItem(
            1002, 0x0001, 116000000, 39000000, 116100000, 39100000);
        
        T8602SetRectangularArea message1 = new T8602SetRectangularArea(T8602SetRectangularArea.SETTING_UPDATE, Arrays.asList(item1));
        T8602SetRectangularArea message2 = new T8602SetRectangularArea(T8602SetRectangularArea.SETTING_UPDATE, Arrays.asList(item2));
        T8602SetRectangularArea message3 = new T8602SetRectangularArea(T8602SetRectangularArea.SETTING_UPDATE, Arrays.asList(item3));
        
        // 测试消息相等性
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
        
        // 测试区域项相等性
        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
        assertEquals(item1, item1); // 自反性
    }
    
    @Test
    void testHashCode() {
        T8602SetRectangularArea.RectangularAreaItem item1 = new T8602SetRectangularArea.RectangularAreaItem(
            1001, 0x0001, 116000000, 39000000, 116100000, 39100000);
        T8602SetRectangularArea.RectangularAreaItem item2 = new T8602SetRectangularArea.RectangularAreaItem(
            1001, 0x0001, 116000000, 39000000, 116100000, 39100000);
        
        T8602SetRectangularArea message1 = new T8602SetRectangularArea(T8602SetRectangularArea.SETTING_UPDATE, Arrays.asList(item1));
        T8602SetRectangularArea message2 = new T8602SetRectangularArea(T8602SetRectangularArea.SETTING_UPDATE, Arrays.asList(item2));
        
        assertEquals(message1.hashCode(), message2.hashCode());
        assertEquals(item1.hashCode(), item2.hashCode());
    }
    
    @Test
    void testMessageFactoryIntegration() {
        // 测试消息工厂是否能正确创建消息实例
        JT808Message message = JT808MessageFactory.getInstance().createMessage(0x8602);
        assertNotNull(message);
        assertInstanceOf(T8602SetRectangularArea.class, message);
        assertEquals(0x8602, message.getMessageId());
    }
    
    @Test
    void testBoundaryValues() {
        // 测试边界值
        T8602SetRectangularArea.RectangularAreaItem item = new T8602SetRectangularArea.RectangularAreaItem();
        
        // 测试最大值
        item.setAreaId(Integer.MAX_VALUE);
        item.setAreaAttribute(0xFFFF);
        item.setTopLeftLatitude(Integer.MAX_VALUE);
        item.setTopLeftLongitude(Integer.MAX_VALUE);
        item.setBottomRightLatitude(Integer.MAX_VALUE);
        item.setBottomRightLongitude(Integer.MAX_VALUE);
        item.setMaxSpeed(65535);
        item.setOverspeedDuration(255);
        
        assertDoesNotThrow(() -> {
            Buffer encoded = item.encode();
            T8602SetRectangularArea.RectangularAreaItem decoded = new T8602SetRectangularArea.RectangularAreaItem();
            decoded.decode(encoded, 0);
        });
    }
    
    @Test
    void testRealWorldScenario() {
        // 模拟真实场景：设置北京市区的矩形电子围栏
        T8602SetRectangularArea.RectangularAreaItem schoolArea = new T8602SetRectangularArea.RectangularAreaItem(
            2001, 
            T8602SetRectangularArea.ATTR_TIME_BASED | T8602SetRectangularArea.ATTR_SPEED_LIMIT | 
            T8602SetRectangularArea.ATTR_ENTER_ALARM_PLATFORM,
            39916000, 116397000, // 左上点：北京天安门附近
            39915000, 116398000  // 右下点
        );
        schoolArea.setStartTime(LocalDateTime.of(2023, 9, 1, 8, 0, 0));
        schoolArea.setEndTime(LocalDateTime.of(2023, 9, 1, 17, 0, 0));
        schoolArea.setMaxSpeed(30); // 限速30km/h
        schoolArea.setOverspeedDuration(5); // 超速5秒报警
        
        T8602SetRectangularArea message = T8602SetRectangularArea.createUpdate(Arrays.asList(schoolArea));
        
        // 编码消息
        Buffer buffer = message.encodeBody();
        
        // 解码验证
        T8602SetRectangularArea decoded = new T8602SetRectangularArea();
        decoded.decodeBody(buffer);
        
        assertEquals(1, decoded.getAreaItems().size());
        T8602SetRectangularArea.RectangularAreaItem decodedArea = decoded.getAreaItems().get(0);
        assertEquals(2001, decodedArea.getAreaId());
        assertTrue(decodedArea.hasTimeAttribute());
        assertTrue(decodedArea.hasSpeedLimitAttribute());
        assertTrue(decodedArea.hasAttribute(T8602SetRectangularArea.ATTR_ENTER_ALARM_PLATFORM));
        assertEquals(30, decodedArea.getMaxSpeed().intValue());
        assertEquals(5, decodedArea.getOverspeedDuration().intValue());
        
        // 验证时间解码
        assertNotNull(decodedArea.getStartTime());
        assertNotNull(decodedArea.getEndTime());
        assertEquals(8, decodedArea.getStartTime().getHour());
        assertEquals(17, decodedArea.getEndTime().getHour());
    }
    
    @Test
    void testSettingAttributeConstants() {
        assertEquals(0x00, T8602SetRectangularArea.SETTING_UPDATE);
        assertEquals(0x01, T8602SetRectangularArea.SETTING_APPEND);
        assertEquals(0x02, T8602SetRectangularArea.SETTING_MODIFY);
    }
    
    @Test
    void testAreaAttributeConstants() {
        assertEquals(0x0001, T8602SetRectangularArea.ATTR_TIME_BASED);
        assertEquals(0x0002, T8602SetRectangularArea.ATTR_SPEED_LIMIT);
        assertEquals(0x0004, T8602SetRectangularArea.ATTR_ENTER_ALARM_DRIVER);
        assertEquals(0x0008, T8602SetRectangularArea.ATTR_ENTER_ALARM_PLATFORM);
        assertEquals(0x0010, T8602SetRectangularArea.ATTR_EXIT_ALARM_DRIVER);
        assertEquals(0x0020, T8602SetRectangularArea.ATTR_EXIT_ALARM_PLATFORM);
        assertEquals(0x0040, T8602SetRectangularArea.ATTR_SOUTH_LATITUDE);
        assertEquals(0x0080, T8602SetRectangularArea.ATTR_WEST_LONGITUDE);
        assertEquals(0x0100, T8602SetRectangularArea.ATTR_DOOR_FORBIDDEN);
        assertEquals(0x4000, T8602SetRectangularArea.ATTR_ENTER_CLOSE_COMM);
        assertEquals(0x8000, T8602SetRectangularArea.ATTR_ENTER_COLLECT_GNSS);
    }
}