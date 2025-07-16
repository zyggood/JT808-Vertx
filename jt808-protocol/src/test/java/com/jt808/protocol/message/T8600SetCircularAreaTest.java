package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.T8600SetCircularArea.CircularAreaItem;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8600SetCircularArea 单元测试
 * 
 * @author JT808-Vertx
 */
class T8600SetCircularAreaTest {
    
    private static final Logger logger = LoggerFactory.getLogger(T8600SetCircularAreaTest.class);
    
    private T8600SetCircularArea message;
    private CircularAreaItem areaItem;
    
    @BeforeEach
    void setUp() {
        message = new T8600SetCircularArea();
        
        // 创建测试用的圆形区域项
        areaItem = new CircularAreaItem();
        areaItem.setAreaId(12345);
        areaItem.setAreaAttribute(T8600SetCircularArea.ATTR_TIME_BASED | T8600SetCircularArea.ATTR_SPEED_LIMIT);
        areaItem.setCenterLatitude(39906000); // 北京纬度 39.906°
        areaItem.setCenterLongitude(116407000); // 北京经度 116.407°
        areaItem.setRadius(1000); // 1000米
        areaItem.setStartTime(LocalDateTime.of(2024, 1, 1, 8, 30, 0));
        areaItem.setEndTime(LocalDateTime.of(2024, 1, 1, 18, 0, 0));
        areaItem.setMaxSpeed(60); // 60km/h
        areaItem.setOverspeedDuration(5); // 5秒
    }
    
    @Test
    void testMessageId() {
        // 测试消息ID
        assertEquals(0x8600, message.getMessageId());
        assertEquals(0x8600, T8600SetCircularArea.MESSAGE_ID);
        
        logger.info("消息ID测试通过");
    }
    
    @Test
    void testConstructors() {
        // 测试默认构造函数
        T8600SetCircularArea defaultMessage = new T8600SetCircularArea();
        assertNotNull(defaultMessage.getAreaItems());
        assertTrue(defaultMessage.getAreaItems().isEmpty());
        assertEquals(0, defaultMessage.getAreaCount());
        
        // 测试带参数构造函数
        List<CircularAreaItem> items = new ArrayList<>();
        items.add(areaItem);
        T8600SetCircularArea paramMessage = new T8600SetCircularArea(T8600SetCircularArea.SETTING_UPDATE, items);
        assertEquals(T8600SetCircularArea.SETTING_UPDATE, paramMessage.getSettingAttribute());
        assertEquals(1, paramMessage.getAreaCount());
        assertEquals(1, paramMessage.getAreaItems().size());
        
        logger.info("构造函数测试通过");
    }
    
    @Test
    void testStaticFactoryMethods() {
        List<CircularAreaItem> items = new ArrayList<>();
        items.add(areaItem);
        
        // 测试创建更新区域消息
        T8600SetCircularArea updateMessage = T8600SetCircularArea.createUpdate(items);
        assertEquals(T8600SetCircularArea.SETTING_UPDATE, updateMessage.getSettingAttribute());
        assertEquals("更新区域", updateMessage.getSettingAttributeDescription());
        
        // 测试创建追加区域消息
        T8600SetCircularArea appendMessage = T8600SetCircularArea.createAppend(items);
        assertEquals(T8600SetCircularArea.SETTING_APPEND, appendMessage.getSettingAttribute());
        assertEquals("追加区域", appendMessage.getSettingAttributeDescription());
        
        // 测试创建修改区域消息
        T8600SetCircularArea modifyMessage = T8600SetCircularArea.createModify(items);
        assertEquals(T8600SetCircularArea.SETTING_MODIFY, modifyMessage.getSettingAttribute());
        assertEquals("修改区域", modifyMessage.getSettingAttributeDescription());
        
        logger.info("静态工厂方法测试通过");
    }
    
    @Test
    void testGettersAndSetters() {
        // 测试设置属性
        message.setSettingAttribute(T8600SetCircularArea.SETTING_APPEND);
        assertEquals(T8600SetCircularArea.SETTING_APPEND, message.getSettingAttribute());
        
        // 测试区域总数
        message.setAreaCount((byte) 2);
        assertEquals(2, message.getAreaCount());
        assertEquals(2, message.getUnsignedAreaCount());
        
        // 测试区域项列表
        List<CircularAreaItem> items = new ArrayList<>();
        items.add(areaItem);
        message.setAreaItems(items);
        assertEquals(1, message.getAreaItems().size());
        assertEquals(1, message.getAreaCount()); // 应该自动更新
        
        logger.info("Getter/Setter测试通过");
    }
    
    @Test
    void testAddAreaItem() {
        // 测试添加区域项
        assertEquals(0, message.getAreaItems().size());
        
        message.addAreaItem(areaItem);
        assertEquals(1, message.getAreaItems().size());
        assertEquals(1, message.getAreaCount());
        
        // 测试添加null
        message.addAreaItem(null);
        assertEquals(1, message.getAreaItems().size());
        
        logger.info("添加区域项测试通过");
    }
    
    @Test
    void testSettingAttributeDescription() {
        // 测试设置属性描述
        message.setSettingAttribute(T8600SetCircularArea.SETTING_UPDATE);
        assertEquals("更新区域", message.getSettingAttributeDescription());
        
        message.setSettingAttribute(T8600SetCircularArea.SETTING_APPEND);
        assertEquals("追加区域", message.getSettingAttributeDescription());
        
        message.setSettingAttribute(T8600SetCircularArea.SETTING_MODIFY);
        assertEquals("修改区域", message.getSettingAttributeDescription());
        
        message.setSettingAttribute((byte) 99);
        assertTrue(message.getSettingAttributeDescription().contains("未知设置属性"));
        
        logger.info("设置属性描述测试通过");
    }
    
    @Test
    void testEncodeWithoutOptionalFields() {
        // 测试编码不包含可选字段的消息
        CircularAreaItem simpleItem = new CircularAreaItem(1, 0, 39906000, 116407000, 500);
        List<CircularAreaItem> items = new ArrayList<>();
        items.add(simpleItem);
        
        message.setSettingAttribute(T8600SetCircularArea.SETTING_UPDATE);
        message.setAreaItems(items);
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(20, encoded.length()); // 2字节头部 + 18字节区域项
        
        // 验证头部
        assertEquals(T8600SetCircularArea.SETTING_UPDATE, encoded.getByte(0));
        assertEquals(1, encoded.getByte(1));
        
        logger.info("无可选字段编码测试通过，编码长度: {} 字节", encoded.length());
    }
    
    @Test
    void testEncodeWithOptionalFields() {
        // 测试编码包含可选字段的消息
        List<CircularAreaItem> items = new ArrayList<>();
        items.add(areaItem);
        
        message.setSettingAttribute(T8600SetCircularArea.SETTING_APPEND);
        message.setAreaItems(items);
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 20); // 包含可选字段
        
        // 验证头部
        assertEquals(T8600SetCircularArea.SETTING_APPEND, encoded.getByte(0));
        assertEquals(1, encoded.getByte(1));
        
        logger.info("包含可选字段编码测试通过，编码长度: {} 字节", encoded.length());
    }
    
    @Test
    void testDecodeWithoutOptionalFields() {
        // 测试解码不包含可选字段的消息
        Buffer data = Buffer.buffer()
                .appendByte(T8600SetCircularArea.SETTING_UPDATE) // 设置属性
                .appendByte((byte) 1) // 区域总数
                .appendInt(12345) // 区域ID
                .appendShort((short) 0) // 区域属性（无可选字段）
                .appendInt(39906000) // 中心点纬度
                .appendInt(116407000) // 中心点经度
                .appendInt(1000); // 半径
        
        message.decodeBody(data);
        
        assertEquals(T8600SetCircularArea.SETTING_UPDATE, message.getSettingAttribute());
        assertEquals(1, message.getUnsignedAreaCount());
        assertEquals(1, message.getAreaItems().size());
        
        CircularAreaItem item = message.getAreaItems().get(0);
        assertEquals(12345, item.getAreaId());
        assertEquals(0, item.getAreaAttribute());
        assertEquals(39906000, item.getCenterLatitude());
        assertEquals(116407000, item.getCenterLongitude());
        assertEquals(1000, item.getRadius());
        assertFalse(item.hasTimeAttribute());
        assertFalse(item.hasSpeedLimitAttribute());
        
        logger.info("无可选字段解码测试通过");
    }
    
    @Test
    void testDecodeWithOptionalFields() {
        // 先编码一个包含可选字段的消息
        List<CircularAreaItem> items = new ArrayList<>();
        items.add(areaItem);
        T8600SetCircularArea originalMessage = new T8600SetCircularArea(T8600SetCircularArea.SETTING_MODIFY, items);
        Buffer encoded = originalMessage.encodeBody();
        
        // 解码
        T8600SetCircularArea decodedMessage = new T8600SetCircularArea();
        decodedMessage.decodeBody(encoded);
        
        // 验证解码结果
        assertEquals(T8600SetCircularArea.SETTING_MODIFY, decodedMessage.getSettingAttribute());
        assertEquals(1, decodedMessage.getUnsignedAreaCount());
        assertEquals(1, decodedMessage.getAreaItems().size());
        
        CircularAreaItem decodedItem = decodedMessage.getAreaItems().get(0);
        assertEquals(areaItem.getAreaId(), decodedItem.getAreaId());
        assertEquals(areaItem.getAreaAttribute(), decodedItem.getAreaAttribute());
        assertEquals(areaItem.getCenterLatitude(), decodedItem.getCenterLatitude());
        assertEquals(areaItem.getCenterLongitude(), decodedItem.getCenterLongitude());
        assertEquals(areaItem.getRadius(), decodedItem.getRadius());
        assertTrue(decodedItem.hasTimeAttribute());
        assertTrue(decodedItem.hasSpeedLimitAttribute());
        
        logger.info("包含可选字段解码测试通过");
    }
    
    @Test
    void testEncodeDecodeConsistency() {
        // 测试编解码一致性
        List<CircularAreaItem> items = new ArrayList<>();
        items.add(areaItem);
        
        message.setSettingAttribute(T8600SetCircularArea.SETTING_APPEND);
        message.setAreaItems(items);
        
        Buffer encoded = message.encodeBody();
        T8600SetCircularArea decoded = new T8600SetCircularArea();
        decoded.decodeBody(encoded);
        
        assertEquals(message.getSettingAttribute(), decoded.getSettingAttribute());
        assertEquals(message.getUnsignedAreaCount(), decoded.getUnsignedAreaCount());
        assertEquals(message.getAreaItems().size(), decoded.getAreaItems().size());
        
        logger.info("编解码一致性测试通过");
    }
    
    @Test
    void testDecodeInvalidData() {
        // 测试解码无效数据
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(Buffer.buffer().appendByte((byte) 0x01)); // 长度不足
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(Buffer.buffer()); // 空Buffer
        });
        
        logger.info("无效数据解码异常测试通过");
    }
    
    @Test
    void testCircularAreaItemAttributes() {
        // 测试圆形区域项属性
        CircularAreaItem item = new CircularAreaItem();
        
        // 测试时间属性
        item.setAreaAttribute(T8600SetCircularArea.ATTR_TIME_BASED);
        assertTrue(item.hasTimeAttribute());
        assertTrue(item.hasAttribute(T8600SetCircularArea.ATTR_TIME_BASED));
        
        // 测试限速属性
        item.setAreaAttribute(T8600SetCircularArea.ATTR_SPEED_LIMIT);
        assertTrue(item.hasSpeedLimitAttribute());
        assertTrue(item.hasAttribute(T8600SetCircularArea.ATTR_SPEED_LIMIT));
        
        // 测试组合属性
        item.setAreaAttribute(T8600SetCircularArea.ATTR_TIME_BASED | T8600SetCircularArea.ATTR_ENTER_ALARM_DRIVER);
        assertTrue(item.hasTimeAttribute());
        assertTrue(item.hasAttribute(T8600SetCircularArea.ATTR_ENTER_ALARM_DRIVER));
        assertFalse(item.hasSpeedLimitAttribute());
        
        logger.info("圆形区域项属性测试通过");
    }
    
    @Test
    void testAreaAttributeDescription() {
        // 测试区域属性描述
        CircularAreaItem item = new CircularAreaItem();
        
        item.setAreaAttribute(0);
        assertEquals("无", item.getAreaAttributeDescription());
        
        item.setAreaAttribute(T8600SetCircularArea.ATTR_TIME_BASED);
        assertEquals("时间限制", item.getAreaAttributeDescription());
        
        item.setAreaAttribute(T8600SetCircularArea.ATTR_TIME_BASED | T8600SetCircularArea.ATTR_SPEED_LIMIT);
        assertTrue(item.getAreaAttributeDescription().contains("时间限制"));
        assertTrue(item.getAreaAttributeDescription().contains("限速"));
        
        logger.info("区域属性描述测试通过");
    }
    
    @Test
    void testToString() {
        // 测试toString方法
        List<CircularAreaItem> items = new ArrayList<>();
        items.add(areaItem);
        message.setSettingAttribute(T8600SetCircularArea.SETTING_UPDATE);
        message.setAreaItems(items);
        
        String str = message.toString();
        assertNotNull(str);
        assertTrue(str.contains("T8600SetCircularArea"));
        assertTrue(str.contains("settingAttribute"));
        assertTrue(str.contains("areaCount"));
        
        // 测试区域项toString
        String itemStr = areaItem.toString();
        assertNotNull(itemStr);
        assertTrue(itemStr.contains("CircularAreaItem"));
        assertTrue(itemStr.contains("areaId"));
        
        logger.info("toString方法测试通过");
    }
    
    @Test
    void testEquals() {
        // 测试equals方法
        List<CircularAreaItem> items = new ArrayList<>();
        items.add(areaItem);
        
        T8600SetCircularArea message1 = new T8600SetCircularArea(T8600SetCircularArea.SETTING_UPDATE, items);
        T8600SetCircularArea message2 = new T8600SetCircularArea(T8600SetCircularArea.SETTING_UPDATE, items);
        T8600SetCircularArea message3 = new T8600SetCircularArea(T8600SetCircularArea.SETTING_APPEND, items);
        
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "string");
        
        // 测试区域项equals
        CircularAreaItem item1 = new CircularAreaItem(1, 0, 1000, 2000, 500);
        CircularAreaItem item2 = new CircularAreaItem(1, 0, 1000, 2000, 500);
        CircularAreaItem item3 = new CircularAreaItem(2, 0, 1000, 2000, 500);
        
        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
        
        logger.info("equals方法测试通过");
    }
    
    @Test
    void testHashCode() {
        // 测试hashCode方法
        List<CircularAreaItem> items = new ArrayList<>();
        items.add(areaItem);
        
        T8600SetCircularArea message1 = new T8600SetCircularArea(T8600SetCircularArea.SETTING_UPDATE, items);
        T8600SetCircularArea message2 = new T8600SetCircularArea(T8600SetCircularArea.SETTING_UPDATE, items);
        
        assertEquals(message1.hashCode(), message2.hashCode());
        
        // 测试区域项hashCode
        CircularAreaItem item1 = new CircularAreaItem(1, 0, 1000, 2000, 500);
        CircularAreaItem item2 = new CircularAreaItem(1, 0, 1000, 2000, 500);
        
        assertEquals(item1.hashCode(), item2.hashCode());
        
        logger.info("hashCode方法测试通过");
    }
    
    @Test
    void testAreaAttributeConstants() {
        // 测试区域属性常量
        assertEquals(0x0001, T8600SetCircularArea.ATTR_TIME_BASED);
        assertEquals(0x0002, T8600SetCircularArea.ATTR_SPEED_LIMIT);
        assertEquals(0x0004, T8600SetCircularArea.ATTR_ENTER_ALARM_DRIVER);
        assertEquals(0x0008, T8600SetCircularArea.ATTR_ENTER_ALARM_PLATFORM);
        assertEquals(0x0010, T8600SetCircularArea.ATTR_EXIT_ALARM_DRIVER);
        assertEquals(0x0020, T8600SetCircularArea.ATTR_EXIT_ALARM_PLATFORM);
        assertEquals(0x0040, T8600SetCircularArea.ATTR_SOUTH_LATITUDE);
        assertEquals(0x0080, T8600SetCircularArea.ATTR_WEST_LONGITUDE);
        assertEquals(0x0100, T8600SetCircularArea.ATTR_DOOR_FORBIDDEN);
        assertEquals(0x4000, T8600SetCircularArea.ATTR_ENTER_CLOSE_COMM);
        assertEquals(0x8000, T8600SetCircularArea.ATTR_ENTER_COLLECT_GNSS);
        
        logger.info("区域属性常量测试通过");
    }
    
    @Test
    void testSettingAttributeConstants() {
        // 测试设置属性常量
        assertEquals(0x00, T8600SetCircularArea.SETTING_UPDATE);
        assertEquals(0x01, T8600SetCircularArea.SETTING_APPEND);
        assertEquals(0x02, T8600SetCircularArea.SETTING_MODIFY);
        
        logger.info("设置属性常量测试通过");
    }
    
    @Test
    void testMessageFactoryIntegration() {
        // 测试消息工厂集成
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        assertNotNull(factory);
        
        // 测试创建消息
        T8600SetCircularArea createdMessage = (T8600SetCircularArea) factory.createMessage(0x8600);
        assertNotNull(createdMessage);
        assertEquals(0x8600, createdMessage.getMessageId());
        
        logger.info("消息工厂集成测试通过");
    }
    
    @Test
    void testDescription() {
        // 测试描述方法
        List<CircularAreaItem> items = new ArrayList<>();
        items.add(areaItem);
        
        message.setSettingAttribute(T8600SetCircularArea.SETTING_UPDATE);
        message.setAreaItems(items);
        
        String description = message.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("设置圆形区域"));
        assertTrue(description.contains("更新区域"));
        assertTrue(description.contains("区域数量: 1"));
        
        logger.info("描述方法测试通过");
    }
    
    @Test
    void testRealWorldScenario() {
        // 测试真实场景：设置多个圆形区域
        List<CircularAreaItem> areas = new ArrayList<>();
        
        // 区域1：北京市中心限速区域
        CircularAreaItem area1 = new CircularAreaItem();
        area1.setAreaId(1001);
        area1.setAreaAttribute(T8600SetCircularArea.ATTR_SPEED_LIMIT | 
                              T8600SetCircularArea.ATTR_ENTER_ALARM_PLATFORM);
        area1.setCenterLatitude(39906000); // 北京
        area1.setCenterLongitude(116407000);
        area1.setRadius(2000); // 2公里
        area1.setMaxSpeed(40); // 40km/h
        area1.setOverspeedDuration(3);
        areas.add(area1);
        
        // 区域2：上海浦东机场时间限制区域
        CircularAreaItem area2 = new CircularAreaItem();
        area2.setAreaId(1002);
        area2.setAreaAttribute(T8600SetCircularArea.ATTR_TIME_BASED | 
                              T8600SetCircularArea.ATTR_ENTER_ALARM_DRIVER |
                              T8600SetCircularArea.ATTR_EXIT_ALARM_DRIVER);
        area2.setCenterLatitude(31143000); // 上海浦东机场
        area2.setCenterLongitude(121805000);
        area2.setRadius(5000); // 5公里
        area2.setStartTime(LocalDateTime.of(2024, 6, 1, 6, 0, 0));
        area2.setEndTime(LocalDateTime.of(2024, 6, 1, 22, 0, 0));
        areas.add(area2);
        
        // 创建设置圆形区域消息
        T8600SetCircularArea setAreaMessage = T8600SetCircularArea.createUpdate(areas);
        
        // 验证消息
        assertEquals(T8600SetCircularArea.SETTING_UPDATE, setAreaMessage.getSettingAttribute());
        assertEquals(2, setAreaMessage.getUnsignedAreaCount());
        assertEquals(2, setAreaMessage.getAreaItems().size());
        
        // 编解码测试
        Buffer encoded = setAreaMessage.encodeBody();
        T8600SetCircularArea decoded = new T8600SetCircularArea();
        decoded.decodeBody(encoded);
        
        assertEquals(setAreaMessage.getSettingAttribute(), decoded.getSettingAttribute());
        assertEquals(setAreaMessage.getUnsignedAreaCount(), decoded.getUnsignedAreaCount());
        assertEquals(setAreaMessage.getAreaItems().size(), decoded.getAreaItems().size());
        
        logger.info("真实场景测试通过：设置了{}个圆形区域", areas.size());
    }
    
    @Test
    void testMultipleAreasEncoding() {
        // 测试多个区域的编码
        List<CircularAreaItem> areas = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            CircularAreaItem area = new CircularAreaItem();
            area.setAreaId(1000 + i);
            area.setAreaAttribute(0); // 无可选字段
            area.setCenterLatitude(39000000 + i * 1000);
            area.setCenterLongitude(116000000 + i * 1000);
            area.setRadius(500 + i * 100);
            areas.add(area);
        }
        
        T8600SetCircularArea message = T8600SetCircularArea.createAppend(areas);
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(2 + 3 * 18, encoded.length()); // 2字节头部 + 3个区域项(每个18字节)
        
        // 解码验证
        T8600SetCircularArea decoded = new T8600SetCircularArea();
        decoded.decodeBody(encoded);
        
        assertEquals(3, decoded.getUnsignedAreaCount());
        assertEquals(3, decoded.getAreaItems().size());
        
        logger.info("多区域编码测试通过，编码长度: {} 字节", encoded.length());
    }
    
    @Test
    void testBoundaryValues() {
        // 测试边界值
        CircularAreaItem item = new CircularAreaItem();
        
        // 测试最大值
        item.setAreaId(Integer.MAX_VALUE);
        item.setAreaAttribute(0xFFFF);
        item.setCenterLatitude(Integer.MAX_VALUE);
        item.setCenterLongitude(Integer.MAX_VALUE);
        item.setRadius(Integer.MAX_VALUE);
        item.setMaxSpeed(65535);
        item.setOverspeedDuration(255);
        
        List<CircularAreaItem> items = new ArrayList<>();
        items.add(item);
        
        T8600SetCircularArea message = new T8600SetCircularArea(T8600SetCircularArea.SETTING_MODIFY, items);
        
        // 编解码测试
        Buffer encoded = message.encodeBody();
        T8600SetCircularArea decoded = new T8600SetCircularArea();
        decoded.decodeBody(encoded);
        
        CircularAreaItem decodedItem = decoded.getAreaItems().get(0);
        assertEquals(item.getAreaId(), decodedItem.getAreaId());
        assertEquals(item.getAreaAttribute(), decodedItem.getAreaAttribute());
        assertEquals(item.getCenterLatitude(), decodedItem.getCenterLatitude());
        assertEquals(item.getCenterLongitude(), decodedItem.getCenterLongitude());
        assertEquals(item.getRadius(), decodedItem.getRadius());
        
        logger.info("边界值测试通过");
    }
}