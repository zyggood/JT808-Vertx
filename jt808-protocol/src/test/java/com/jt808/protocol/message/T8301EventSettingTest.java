package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8301事件设置消息测试
 */
class T8301EventSettingTest {

    private T8301EventSetting message;
    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        message = new T8301EventSetting();
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    void testMessageId() {
        assertEquals(0x8301, message.getMessageId());
    }

    @Test
    void testConstructors() {
        // 默认构造函数
        T8301EventSetting msg1 = new T8301EventSetting();
        assertNotNull(msg1.getEventItems());
        assertEquals(0, msg1.getEventCount());

        // 带设置类型的构造函数
        T8301EventSetting msg2 = new T8301EventSetting(T8301EventSetting.SettingType.UPDATE);
        assertEquals(T8301EventSetting.SettingType.UPDATE, msg2.getSettingType());
        assertNotNull(msg2.getEventItems());

        // 带消息头的构造函数
        JT808Header header = new JT808Header();
        T8301EventSetting msg3 = new T8301EventSetting(header);
        assertEquals(header, msg3.getHeader());
        assertNotNull(msg3.getEventItems());
    }

    @Test
    void testEncodeDecodeDeleteAll() {
        // 测试删除所有事件
        message.setSettingType(T8301EventSetting.SettingType.DELETE_ALL);

        Buffer encoded = message.encodeBody();
        assertEquals(1, encoded.length()); // 只有设置类型1字节
        assertEquals(0, encoded.getByte(0)); // 设置类型为0

        T8301EventSetting decoded = new T8301EventSetting();
        decoded.decodeBody(encoded);

        assertEquals(T8301EventSetting.SettingType.DELETE_ALL, decoded.getSettingType());
        assertEquals(0, decoded.getEventCount());
        assertTrue(decoded.isDeleteAll());
    }

    @Test
    void testEncodeDecodeWithEvents() {
        // 测试带事件项的编解码
        message.setSettingType(T8301EventSetting.SettingType.UPDATE);
        message.addEventItem((byte) 1, "紧急事件");
        message.addEventItem((byte) 2, "报警事件");

        Buffer encoded = message.encodeBody();

        T8301EventSetting decoded = new T8301EventSetting();
        decoded.decodeBody(encoded);

        assertEquals(T8301EventSetting.SettingType.UPDATE, decoded.getSettingType());
        assertEquals(2, decoded.getEventCount());

        T8301EventSetting.EventItem item1 = decoded.getEventItem((byte) 1);
        assertNotNull(item1);
        assertEquals((byte) 1, item1.getEventId());
        assertEquals("紧急事件", item1.getContentString());

        T8301EventSetting.EventItem item2 = decoded.getEventItem((byte) 2);
        assertNotNull(item2);
        assertEquals((byte) 2, item2.getEventId());
        assertEquals("报警事件", item2.getContentString());
    }

    @Test
    void testEncodeDecodeDeleteSpecific() {
        // 测试删除特定事件
        message.setSettingType(T8301EventSetting.SettingType.DELETE_SPECIFIC);
        message.addEventId((byte) 1);
        message.addEventId((byte) 3);

        Buffer encoded = message.encodeBody();

        T8301EventSetting decoded = new T8301EventSetting();
        decoded.decodeBody(encoded);

        assertEquals(T8301EventSetting.SettingType.DELETE_SPECIFIC, decoded.getSettingType());
        assertEquals(2, decoded.getEventCount());
        assertTrue(decoded.isDeleteSpecific());

        T8301EventSetting.EventItem item1 = decoded.getEventItem((byte) 1);
        assertNotNull(item1);
        assertEquals((byte) 1, item1.getEventId());
        assertEquals(0, item1.getContentLength());

        T8301EventSetting.EventItem item3 = decoded.getEventItem((byte) 3);
        assertNotNull(item3);
        assertEquals((byte) 3, item3.getEventId());
        assertEquals(0, item3.getContentLength());
    }

    @Test
    void testEncodeDecodeConsistency() {
        // 测试编解码一致性
        message.setSettingType(T8301EventSetting.SettingType.APPEND);
        message.addEventItem((byte) 10, "测试事件内容");
        message.addEventItem((byte) 20, "Another event");
        message.addEventItem((byte) 30, ""); // 空内容

        Buffer encoded = message.encodeBody();

        T8301EventSetting decoded = new T8301EventSetting();
        decoded.decodeBody(encoded);

        assertEquals(message.getSettingType(), decoded.getSettingType());
        assertEquals(message.getEventCount(), decoded.getEventCount());
        assertEquals(message.getEventItems(), decoded.getEventItems());
    }

    @Test
    void testSettingTypeChecks() {
        // 测试设置类型检查方法
        message.setSettingType(T8301EventSetting.SettingType.DELETE_ALL);
        assertTrue(message.isDeleteAll());
        assertFalse(message.isUpdate());
        assertFalse(message.isAppend());
        assertFalse(message.isModify());
        assertFalse(message.isDeleteSpecific());

        message.setSettingType(T8301EventSetting.SettingType.UPDATE);
        assertFalse(message.isDeleteAll());
        assertTrue(message.isUpdate());
        assertFalse(message.isAppend());
        assertFalse(message.isModify());
        assertFalse(message.isDeleteSpecific());

        message.setSettingType(T8301EventSetting.SettingType.APPEND);
        assertTrue(message.isAppend());

        message.setSettingType(T8301EventSetting.SettingType.MODIFY);
        assertTrue(message.isModify());

        message.setSettingType(T8301EventSetting.SettingType.DELETE_SPECIFIC);
        assertTrue(message.isDeleteSpecific());
    }

    @Test
    void testSettingTypeDescription() {
        message.setSettingType(T8301EventSetting.SettingType.DELETE_ALL);
        assertEquals("删除终端现有所有事件", message.getSettingTypeDescription());

        message.setSettingType(T8301EventSetting.SettingType.UPDATE);
        assertEquals("更新事件", message.getSettingTypeDescription());

        message.setSettingType(T8301EventSetting.SettingType.APPEND);
        assertEquals("追加事件", message.getSettingTypeDescription());

        message.setSettingType(T8301EventSetting.SettingType.MODIFY);
        assertEquals("修改事件", message.getSettingTypeDescription());

        message.setSettingType(T8301EventSetting.SettingType.DELETE_SPECIFIC);
        assertEquals("删除特定几项事件", message.getSettingTypeDescription());

        message.setSettingType((byte) 99);
        assertEquals("未知设置类型(99)", message.getSettingTypeDescription());
    }

    @Test
    void testFactoryMethods() {
        // 测试静态工厂方法
        T8301EventSetting deleteAll = T8301EventSetting.createDeleteAll();
        assertTrue(deleteAll.isDeleteAll());

        T8301EventSetting update = T8301EventSetting.createUpdate();
        assertTrue(update.isUpdate());

        T8301EventSetting append = T8301EventSetting.createAppend();
        assertTrue(append.isAppend());

        T8301EventSetting modify = T8301EventSetting.createModify();
        assertTrue(modify.isModify());

        T8301EventSetting deleteSpecific = T8301EventSetting.createDeleteSpecific();
        assertTrue(deleteSpecific.isDeleteSpecific());
    }

    @Test
    void testEventItemManagement() {
        // 测试事件项管理
        T8301EventSetting.EventItem item1 = new T8301EventSetting.EventItem((byte) 1, "事件1");
        T8301EventSetting.EventItem item2 = new T8301EventSetting.EventItem((byte) 2, "事件2");

        message.addEventItem(item1);
        message.addEventItem(item2);
        assertEquals(2, message.getEventCount());

        // 测试获取事件项
        T8301EventSetting.EventItem found = message.getEventItem((byte) 1);
        assertNotNull(found);
        assertEquals("事件1", found.getContentString());

        // 测试移除事件项
        assertTrue(message.removeEventItem((byte) 1));
        assertEquals(1, message.getEventCount());
        assertNull(message.getEventItem((byte) 1));

        // 测试清空事件项
        message.clearEventItems();
        assertEquals(0, message.getEventCount());
    }

    @Test
    void testEventItem() {
        // 测试事件项类
        T8301EventSetting.EventItem item = new T8301EventSetting.EventItem((byte) 5, "测试内容");
        assertEquals((byte) 5, item.getEventId());
        assertEquals("测试内容", item.getContentString());
        assertTrue(item.getContentLength() > 0);

        // 测试设置内容
        item.setContentString("新内容");
        assertEquals("新内容", item.getContentString());

        // 测试空内容
        T8301EventSetting.EventItem emptyItem = new T8301EventSetting.EventItem((byte) 6, "");
        assertEquals("", emptyItem.getContentString());
        assertEquals(0, emptyItem.getContentLength());

        // 测试null内容
        T8301EventSetting.EventItem nullItem = new T8301EventSetting.EventItem((byte) 7, (String) null);
        assertEquals("", nullItem.getContentString());
        assertEquals(0, nullItem.getContentLength());
    }

    @Test
    void testChineseContent() {
        // 测试中文内容
        message.setSettingType(T8301EventSetting.SettingType.UPDATE);
        message.addEventItem((byte) 1, "紧急情况处理");
        message.addEventItem((byte) 2, "车辆故障报警");

        Buffer encoded = message.encodeBody();

        T8301EventSetting decoded = new T8301EventSetting();
        decoded.decodeBody(encoded);

        assertEquals("紧急情况处理", decoded.getEventItem((byte) 1).getContentString());
        assertEquals("车辆故障报警", decoded.getEventItem((byte) 2).getContentString());
    }

    @Test
    void testMaxContentLength() {
        // 测试最大内容长度
        StringBuilder validContent = new StringBuilder();
        for (int i = 0; i < 120; i++) {
            validContent.append("a"); // 每个字符1字节，总共120字节，在255字节限制内
        }

        // 应该能正常添加
        assertDoesNotThrow(() -> {
            message.addEventItem((byte) 1, validContent.toString());
        });

        // 测试中文内容在限制内
        StringBuilder chineseContent = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            chineseContent.append("测"); // 每个中文字符2字节，总共200字节，在255字节限制内
        }

        assertDoesNotThrow(() -> {
            message.addEventItem((byte) 3, chineseContent.toString());
        });

        // 测试超长内容
        StringBuilder tooLongContent = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            tooLongContent.append("测试"); // 800字节，超过255字节限制
        }

        assertThrows(RuntimeException.class, () -> {
            message.addEventItem((byte) 2, tooLongContent.toString());
        });
    }

    @Test
    void testDecodeBodyExceptions() {
        // 测试解码异常
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(Buffer.buffer());
        });

        // 测试长度不足的情况
        Buffer invalidBuffer = Buffer.buffer();
        invalidBuffer.appendByte(T8301EventSetting.SettingType.UPDATE); // 设置类型
        // 缺少设置总数字段

        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(invalidBuffer);
        });
    }

    @Test
    void testToString() {
        message.setSettingType(T8301EventSetting.SettingType.UPDATE);
        message.addEventItem((byte) 1, "事件1");
        message.addEventItem((byte) 2, "事件2");

        String str = message.toString();
        assertTrue(str.contains("T8301EventSetting"));
        assertTrue(str.contains("更新事件"));
        assertTrue(str.contains("eventCount=2"));
    }

    @Test
    void testEqualsAndHashCode() {
        T8301EventSetting msg1 = new T8301EventSetting(T8301EventSetting.SettingType.UPDATE);
        msg1.addEventItem((byte) 1, "事件1");

        T8301EventSetting msg2 = new T8301EventSetting(T8301EventSetting.SettingType.UPDATE);
        msg2.addEventItem((byte) 1, "事件1");

        T8301EventSetting msg3 = new T8301EventSetting(T8301EventSetting.SettingType.APPEND);
        msg3.addEventItem((byte) 1, "事件1");

        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
        assertNotEquals(msg1, msg3);
        assertNotEquals(msg1, null);
        assertNotEquals(msg1, "string");
    }

    @Test
    void testEventItemEqualsAndHashCode() {
        T8301EventSetting.EventItem item1 = new T8301EventSetting.EventItem((byte) 1, "内容");
        T8301EventSetting.EventItem item2 = new T8301EventSetting.EventItem((byte) 1, "内容");
        T8301EventSetting.EventItem item3 = new T8301EventSetting.EventItem((byte) 2, "内容");
        T8301EventSetting.EventItem item4 = new T8301EventSetting.EventItem((byte) 1, "其他内容");

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1, item3);
        assertNotEquals(item1, item4);
        assertNotEquals(item1, null);
        assertNotEquals(item1, "string");
    }

    @Test
    void testBoundaryValues() {
        // 测试边界值
        message.setSettingType(T8301EventSetting.SettingType.UPDATE);

        // 测试事件ID边界值
        message.addEventItem((byte) 0, "事件0");
        message.addEventItem((byte) 127, "事件127");
        message.addEventItem((byte) -1, "事件255"); // -1在byte中表示255
        message.addEventItem((byte) -128, "事件128"); // -128在byte中表示128

        Buffer encoded = message.encodeBody();

        T8301EventSetting decoded = new T8301EventSetting();
        decoded.decodeBody(encoded);

        assertEquals(4, decoded.getEventCount());
        assertNotNull(decoded.getEventItem((byte) 0));
        assertNotNull(decoded.getEventItem((byte) 127));
        assertNotNull(decoded.getEventItem((byte) -1));
        assertNotNull(decoded.getEventItem((byte) -128));
    }

    @Test
    void testCompleteFlow() {
        // 测试完整流程
        T8301EventSetting original = T8301EventSetting.createUpdate();
        original.addEventItem((byte) 1, "紧急事件处理流程");
        original.addEventItem((byte) 2, "故障诊断程序");
        original.addEventItem((byte) 3, "维护提醒事项");

        // 编码
        Buffer encoded = original.encodeBody();

        // 解码
        T8301EventSetting decoded = new T8301EventSetting();
        decoded.decodeBody(encoded);

        // 验证
        assertEquals(original, decoded);
        assertEquals(original.getSettingType(), decoded.getSettingType());
        assertEquals(original.getEventCount(), decoded.getEventCount());

        for (T8301EventSetting.EventItem originalItem : original.getEventItems()) {
            T8301EventSetting.EventItem decodedItem = decoded.getEventItem(originalItem.getEventId());
            assertNotNull(decodedItem);
            assertEquals(originalItem.getContentString(), decodedItem.getContentString());
        }
    }
}