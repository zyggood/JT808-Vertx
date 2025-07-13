package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8303InfoMenuSetting 信息点播菜单设置消息测试
 */
public class T8303InfoMenuSettingTest {

    @Test
    public void testMessageId() {
        T8303InfoMenuSetting message = new T8303InfoMenuSetting();
        assertEquals(0x8303, message.getMessageId());
    }

    @Test
    public void testConstructors() {
        // 默认构造函数
        T8303InfoMenuSetting message1 = new T8303InfoMenuSetting();
        assertNotNull(message1.getInfoItems());
        assertTrue(message1.getInfoItems().isEmpty());

        // 带设置类型的构造函数
        T8303InfoMenuSetting message2 = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.UPDATE);
        assertEquals(T8303InfoMenuSetting.SettingType.UPDATE, message2.getSettingType());
        assertTrue(message2.getInfoItems().isEmpty());

        // 带设置类型和信息项列表的构造函数
        List<T8303InfoMenuSetting.InfoItem> items = new ArrayList<>();
        items.add(new T8303InfoMenuSetting.InfoItem((byte) 1, "天气预报"));
        T8303InfoMenuSetting message3 = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.APPEND, items);
        assertEquals(T8303InfoMenuSetting.SettingType.APPEND, message3.getSettingType());
        assertEquals(1, message3.getInfoItems().size());
        assertEquals(1, message3.getInfoItemCount());
    }

    @Test
    public void testStaticFactoryMethods() {
        // 删除全部信息项
        T8303InfoMenuSetting deleteAll = T8303InfoMenuSetting.createDeleteAll();
        assertEquals(T8303InfoMenuSetting.SettingType.DELETE_ALL, deleteAll.getSettingType());
        assertTrue(deleteAll.isDeleteAll());

        // 更新菜单
        List<T8303InfoMenuSetting.InfoItem> items = new ArrayList<>();
        items.add(new T8303InfoMenuSetting.InfoItem((byte) 1, "新闻资讯"));
        T8303InfoMenuSetting update = T8303InfoMenuSetting.createUpdate(items);
        assertEquals(T8303InfoMenuSetting.SettingType.UPDATE, update.getSettingType());
        assertTrue(update.isUpdate());
        assertEquals(1, update.getInfoItems().size());

        // 追加菜单
        T8303InfoMenuSetting append = T8303InfoMenuSetting.createAppend(items);
        assertEquals(T8303InfoMenuSetting.SettingType.APPEND, append.getSettingType());
        assertTrue(append.isAppend());

        // 修改菜单
        T8303InfoMenuSetting modify = T8303InfoMenuSetting.createModify(items);
        assertEquals(T8303InfoMenuSetting.SettingType.MODIFY, modify.getSettingType());
        assertTrue(modify.isModify());
    }

    @Test
    public void testEncodeBodyDeleteAll() {
        T8303InfoMenuSetting message = T8303InfoMenuSetting.createDeleteAll();
        Buffer encoded = message.encodeBody();

        assertEquals(1, encoded.length());
        assertEquals(T8303InfoMenuSetting.SettingType.DELETE_ALL, encoded.getByte(0));
    }

    @Test
    public void testEncodeBodyWithInfoItems() {
        T8303InfoMenuSetting message = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.UPDATE);
        message.addInfoItem((byte) 1, "天气预报");
        message.addInfoItem((byte) 2, "交通信息");

        Buffer encoded = message.encodeBody();

        // 验证编码结果
        int index = 0;
        assertEquals(T8303InfoMenuSetting.SettingType.UPDATE, encoded.getByte(index++));
        assertEquals(2, encoded.getByte(index++)); // 信息项总数

        // 第一个信息项
        assertEquals((byte) 1, encoded.getByte(index++)); // 信息类型
        byte[] name1Bytes = "天气预报".getBytes(Charset.forName("GBK"));
        assertEquals(name1Bytes.length, encoded.getUnsignedShort(index)); // 信息名称长度
        index += 2;
        byte[] actualName1 = encoded.getBytes(index, index + name1Bytes.length);
        assertArrayEquals(name1Bytes, actualName1);
        index += name1Bytes.length;

        // 第二个信息项
        assertEquals((byte) 2, encoded.getByte(index++)); // 信息类型
        byte[] name2Bytes = "交通信息".getBytes(Charset.forName("GBK"));
        assertEquals(name2Bytes.length, encoded.getUnsignedShort(index)); // 信息名称长度
        index += 2;
        byte[] actualName2 = encoded.getBytes(index, index + name2Bytes.length);
        assertArrayEquals(name2Bytes, actualName2);
    }

    @Test
    public void testDecodeBodyDeleteAll() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(T8303InfoMenuSetting.SettingType.DELETE_ALL);

        T8303InfoMenuSetting message = new T8303InfoMenuSetting();
        message.decodeBody(buffer);

        assertEquals(T8303InfoMenuSetting.SettingType.DELETE_ALL, message.getSettingType());
        assertEquals(0, message.getInfoItemCount());
        assertTrue(message.getInfoItems().isEmpty());
        assertTrue(message.isDeleteAll());
    }

    @Test
    public void testDecodeBodyWithInfoItems() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(T8303InfoMenuSetting.SettingType.UPDATE); // 设置类型
        buffer.appendByte((byte) 2); // 信息项总数

        // 第一个信息项
        buffer.appendByte((byte) 1); // 信息类型
        byte[] name1Bytes = "天气预报".getBytes(Charset.forName("GBK"));
        buffer.appendUnsignedShort(name1Bytes.length); // 信息名称长度
        buffer.appendBytes(name1Bytes); // 信息名称

        // 第二个信息项
        buffer.appendByte((byte) 2); // 信息类型
        byte[] name2Bytes = "交通信息".getBytes(Charset.forName("GBK"));
        buffer.appendUnsignedShort(name2Bytes.length); // 信息名称长度
        buffer.appendBytes(name2Bytes); // 信息名称

        T8303InfoMenuSetting message = new T8303InfoMenuSetting();
        message.decodeBody(buffer);

        assertEquals(T8303InfoMenuSetting.SettingType.UPDATE, message.getSettingType());
        assertEquals(2, message.getInfoItemCount());
        assertEquals(2, message.getInfoItems().size());

        T8303InfoMenuSetting.InfoItem item1 = message.getInfoItems().get(0);
        assertEquals((byte) 1, item1.getInfoType());
        assertEquals("天气预报", item1.getInfoName());

        T8303InfoMenuSetting.InfoItem item2 = message.getInfoItems().get(1);
        assertEquals((byte) 2, item2.getInfoType());
        assertEquals("交通信息", item2.getInfoName());
    }

    @Test
    public void testEncodeDecodeConsistency() {
        T8303InfoMenuSetting original = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.APPEND);
        original.addInfoItem((byte) 1, "新闻资讯");
        original.addInfoItem((byte) 2, "股票行情");
        original.addInfoItem((byte) 3, "生活服务");

        // 编码
        Buffer encoded = original.encodeBody();

        // 解码
        T8303InfoMenuSetting decoded = new T8303InfoMenuSetting();
        decoded.decodeBody(encoded);

        // 验证一致性
        assertEquals(original.getSettingType(), decoded.getSettingType());
        assertEquals(original.getInfoItemCount(), decoded.getInfoItemCount());
        assertEquals(original.getInfoItems().size(), decoded.getInfoItems().size());

        for (int i = 0; i < original.getInfoItems().size(); i++) {
            T8303InfoMenuSetting.InfoItem originalItem = original.getInfoItems().get(i);
            T8303InfoMenuSetting.InfoItem decodedItem = decoded.getInfoItems().get(i);
            assertEquals(originalItem.getInfoType(), decodedItem.getInfoType());
            assertEquals(originalItem.getInfoName(), decodedItem.getInfoName());
        }
    }

    @Test
    public void testEmptyInfoName() {
        T8303InfoMenuSetting message = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.UPDATE);
        message.addInfoItem((byte) 1, "");
        message.addInfoItem((byte) 2, null);

        Buffer encoded = message.encodeBody();
        T8303InfoMenuSetting decoded = new T8303InfoMenuSetting();
        decoded.decodeBody(encoded);

        assertEquals(2, decoded.getInfoItems().size());
        assertEquals("", decoded.getInfoItems().get(0).getInfoName());
        assertEquals("", decoded.getInfoItems().get(1).getInfoName());
    }

    @Test
    public void testInfoNameLengthLimit() {
        // 创建一个超长的信息名称（超过65535字节）
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 33000; i++) { // 每个中文字符在GBK中占2字节，33000个字符约66000字节，超过65535
            longName.append("测");
        }

        T8303InfoMenuSetting.InfoItem item = new T8303InfoMenuSetting.InfoItem();
        assertThrows(IllegalArgumentException.class, () -> {
            item.setInfoName(longName.toString());
        });

        T8303InfoMenuSetting message = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.UPDATE);
        assertThrows(IllegalArgumentException.class, () -> {
            message.addInfoItem((byte) 1, longName.toString());
        });
    }

    @Test
    public void testInfoItemOperations() {
        T8303InfoMenuSetting message = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.UPDATE);

        // 添加信息项
        message.addInfoItem((byte) 1, "天气预报");
        message.addInfoItem((byte) 2, "交通信息");
        assertEquals(2, message.getInfoItems().size());

        // 获取信息项
        T8303InfoMenuSetting.InfoItem item = message.getInfoItem((byte) 1);
        assertNotNull(item);
        assertEquals("天气预报", item.getInfoName());

        // 获取不存在的信息项
        assertNull(message.getInfoItem((byte) 99));

        // 移除信息项
        assertTrue(message.removeInfoItem((byte) 1));
        assertEquals(1, message.getInfoItems().size());
        assertFalse(message.removeInfoItem((byte) 99));

        // 清空信息项
        message.clearInfoItems();
        assertTrue(message.getInfoItems().isEmpty());
        assertEquals(0, message.getInfoItemCount());
    }

    @Test
    public void testSettingTypeChecks() {
        T8303InfoMenuSetting message = new T8303InfoMenuSetting();

        message.setSettingType(T8303InfoMenuSetting.SettingType.DELETE_ALL);
        assertTrue(message.isDeleteAll());
        assertFalse(message.isUpdate());
        assertFalse(message.isAppend());
        assertFalse(message.isModify());

        message.setSettingType(T8303InfoMenuSetting.SettingType.UPDATE);
        assertFalse(message.isDeleteAll());
        assertTrue(message.isUpdate());
        assertFalse(message.isAppend());
        assertFalse(message.isModify());

        message.setSettingType(T8303InfoMenuSetting.SettingType.APPEND);
        assertFalse(message.isDeleteAll());
        assertFalse(message.isUpdate());
        assertTrue(message.isAppend());
        assertFalse(message.isModify());

        message.setSettingType(T8303InfoMenuSetting.SettingType.MODIFY);
        assertFalse(message.isDeleteAll());
        assertFalse(message.isUpdate());
        assertFalse(message.isAppend());
        assertTrue(message.isModify());
    }

    @Test
    public void testSettingTypeDescription() {
        T8303InfoMenuSetting message = new T8303InfoMenuSetting();

        message.setSettingType(T8303InfoMenuSetting.SettingType.DELETE_ALL);
        assertEquals("删除终端全部信息项", message.getSettingTypeDescription());

        message.setSettingType(T8303InfoMenuSetting.SettingType.UPDATE);
        assertEquals("更新菜单", message.getSettingTypeDescription());

        message.setSettingType(T8303InfoMenuSetting.SettingType.APPEND);
        assertEquals("追加菜单", message.getSettingTypeDescription());

        message.setSettingType(T8303InfoMenuSetting.SettingType.MODIFY);
        assertEquals("修改菜单", message.getSettingTypeDescription());

        message.setSettingType((byte) 99);
        assertEquals("未知设置类型(99)", message.getSettingTypeDescription());
    }

    @Test
    public void testInfoItemClass() {
        // 默认构造函数
        T8303InfoMenuSetting.InfoItem item1 = new T8303InfoMenuSetting.InfoItem();
        assertEquals((byte) 0, item1.getInfoType());
        assertNull(item1.getInfoName());

        // 带参数构造函数
        T8303InfoMenuSetting.InfoItem item2 = new T8303InfoMenuSetting.InfoItem((byte) 1, "天气预报");
        assertEquals((byte) 1, item2.getInfoType());
        assertEquals("天气预报", item2.getInfoName());

        // 无符号类型值
        item2.setInfoType((byte) 255);
        assertEquals(255, item2.getInfoTypeUnsigned());

        // 名称字节长度
        assertEquals(8, item2.getInfoNameByteLength()); // "天气预报"在GBK中占8字节

        item2.setInfoName("");
        assertEquals(0, item2.getInfoNameByteLength());

        item2.setInfoName(null);
        assertEquals(0, item2.getInfoNameByteLength());
    }

    @Test
    public void testInvalidDecodeBody() {
        T8303InfoMenuSetting message = new T8303InfoMenuSetting();

        // 空消息体
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });

        // 消息体长度不足
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(Buffer.buffer());
        });

        // 缺少信息项总数字段
        Buffer buffer1 = Buffer.buffer();
        buffer1.appendByte(T8303InfoMenuSetting.SettingType.UPDATE);
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer1);
        });

        // 信息项数据不完整
        Buffer buffer2 = Buffer.buffer();
        buffer2.appendByte(T8303InfoMenuSetting.SettingType.UPDATE);
        buffer2.appendByte((byte) 1); // 声明有1个信息项，但没有提供数据
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer2);
        });

        // 信息名称长度字段不完整
        Buffer buffer3 = Buffer.buffer();
        buffer3.appendByte(T8303InfoMenuSetting.SettingType.UPDATE);
        buffer3.appendByte((byte) 1);
        buffer3.appendByte((byte) 1); // 信息类型
        buffer3.appendByte((byte) 0); // 只有信息名称长度的一个字节
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer3);
        });

        // 信息名称内容不完整
        Buffer buffer4 = Buffer.buffer();
        buffer4.appendByte(T8303InfoMenuSetting.SettingType.UPDATE);
        buffer4.appendByte((byte) 1);
        buffer4.appendByte((byte) 1); // 信息类型
        buffer4.appendUnsignedShort(10); // 声明信息名称长度为10字节，但没有提供数据
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer4);
        });
    }

    @Test
    public void testToString() {
        T8303InfoMenuSetting message = T8303InfoMenuSetting.createDeleteAll();
        String str = message.toString();
        assertTrue(str.contains("T8303InfoMenuSetting"));
        assertTrue(str.contains("删除终端全部信息项"));

        message = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.UPDATE);
        message.addInfoItem((byte) 1, "天气预报");
        str = message.toString();
        assertTrue(str.contains("更新菜单"));
        assertTrue(str.contains("天气预报"));
    }

    @Test
    public void testEqualsAndHashCode() {
        T8303InfoMenuSetting message1 = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.UPDATE);
        message1.addInfoItem((byte) 1, "天气预报");

        T8303InfoMenuSetting message2 = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.UPDATE);
        message2.addInfoItem((byte) 1, "天气预报");

        T8303InfoMenuSetting message3 = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.APPEND);
        message3.addInfoItem((byte) 1, "天气预报");

        // 测试equals
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "string");

        // 测试hashCode
        assertEquals(message1.hashCode(), message2.hashCode());

        // 测试InfoItem的equals和hashCode
        T8303InfoMenuSetting.InfoItem item1 = new T8303InfoMenuSetting.InfoItem((byte) 1, "天气预报");
        T8303InfoMenuSetting.InfoItem item2 = new T8303InfoMenuSetting.InfoItem((byte) 1, "天气预报");
        T8303InfoMenuSetting.InfoItem item3 = new T8303InfoMenuSetting.InfoItem((byte) 2, "天气预报");

        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    public void testGetInfoItemCountUnsigned() {
        T8303InfoMenuSetting message = new T8303InfoMenuSetting(T8303InfoMenuSetting.SettingType.UPDATE);

        // 添加255个信息项（测试无符号字节的边界）
        for (int i = 1; i <= 255; i++) {
            message.addInfoItem((byte) i, "信息项" + i);
        }

        assertEquals(255, message.getInfoItemCountUnsigned());
        assertEquals(-1, message.getInfoItemCount()); // 有符号字节值为-1
    }
}