package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8401设置电话本消息测试类
 */
class T8401PhonebookSettingTest {

    private T8401PhonebookSetting message;
    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        message = new T8401PhonebookSetting();
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    void testMessageId() {
        assertEquals(0x8401, message.getMessageId());
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(message.getContactItems());
        assertTrue(message.getContactItems().isEmpty());
        assertEquals(0, message.getContactCount());
    }

    @Test
    void testConstructorWithHeader() {
        JT808Header header = new JT808Header();
        T8401PhonebookSetting msg = new T8401PhonebookSetting(header);
        assertNotNull(msg.getContactItems());
        assertTrue(msg.getContactItems().isEmpty());
        assertEquals(0, msg.getContactCount());
    }

    @Test
    void testConstructorWithSettingType() {
        T8401PhonebookSetting msg = new T8401PhonebookSetting(T8401PhonebookSetting.SettingType.DELETE_ALL);
        assertEquals(T8401PhonebookSetting.SettingType.DELETE_ALL, msg.getSettingType());
        assertNotNull(msg.getContactItems());
        assertTrue(msg.getContactItems().isEmpty());
    }

    @Test
    void testConstructorWithSettingTypeAndContacts() {
        List<T8401PhonebookSetting.ContactItem> contacts = new ArrayList<>();
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三"));
        
        T8401PhonebookSetting msg = new T8401PhonebookSetting(T8401PhonebookSetting.SettingType.UPDATE, contacts);
        assertEquals(T8401PhonebookSetting.SettingType.UPDATE, msg.getSettingType());
        assertEquals(1, msg.getContactCount());
        assertEquals(1, msg.getContactItems().size());
    }

    @Test
    void testCreateDeleteAll() {
        T8401PhonebookSetting msg = T8401PhonebookSetting.createDeleteAll();
        assertEquals(T8401PhonebookSetting.SettingType.DELETE_ALL, msg.getSettingType());
        assertTrue(msg.isDeleteAll());
        assertTrue(msg.getContactItems().isEmpty());
    }

    @Test
    void testCreateUpdate() {
        List<T8401PhonebookSetting.ContactItem> contacts = new ArrayList<>();
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.OUTGOING, "13900139000", "李四"));
        
        T8401PhonebookSetting msg = T8401PhonebookSetting.createUpdate(contacts);
        assertEquals(T8401PhonebookSetting.SettingType.UPDATE, msg.getSettingType());
        assertTrue(msg.isUpdate());
        assertEquals(1, msg.getContactItems().size());
    }

    @Test
    void testCreateAppend() {
        List<T8401PhonebookSetting.ContactItem> contacts = new ArrayList<>();
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.BIDIRECTIONAL, "13700137000", "王五"));
        
        T8401PhonebookSetting msg = T8401PhonebookSetting.createAppend(contacts);
        assertEquals(T8401PhonebookSetting.SettingType.APPEND, msg.getSettingType());
        assertTrue(msg.isAppend());
        assertEquals(1, msg.getContactItems().size());
    }

    @Test
    void testCreateModify() {
        List<T8401PhonebookSetting.ContactItem> contacts = new ArrayList<>();
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.INCOMING, "13600136000", "赵六"));
        
        T8401PhonebookSetting msg = T8401PhonebookSetting.createModify(contacts);
        assertEquals(T8401PhonebookSetting.SettingType.MODIFY, msg.getSettingType());
        assertTrue(msg.isModify());
        assertEquals(1, msg.getContactItems().size());
    }

    @Test
    void testEncodeBodyDeleteAll() {
        T8401PhonebookSetting msg = T8401PhonebookSetting.createDeleteAll();
        Buffer buffer = msg.encodeBody();
        
        assertEquals(1, buffer.length());
        assertEquals(T8401PhonebookSetting.SettingType.DELETE_ALL, buffer.getByte(0));
    }

    @Test
    void testEncodeBodyWithContacts() {
        List<T8401PhonebookSetting.ContactItem> contacts = new ArrayList<>();
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三"));
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.OUTGOING, "13900139000", "李四"));
        
        T8401PhonebookSetting msg = T8401PhonebookSetting.createUpdate(contacts);
        Buffer buffer = msg.encodeBody();
        
        assertNotNull(buffer);
        assertTrue(buffer.length() > 2); // 至少包含设置类型和联系人总数
        assertEquals(T8401PhonebookSetting.SettingType.UPDATE, buffer.getByte(0));
        assertEquals(2, buffer.getByte(1)); // 联系人总数
    }

    @Test
    void testDecodeBodyDeleteAll() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(T8401PhonebookSetting.SettingType.DELETE_ALL);
        
        message.decodeBody(buffer);
        
        assertEquals(T8401PhonebookSetting.SettingType.DELETE_ALL, message.getSettingType());
        assertEquals(0, message.getContactCount());
        assertTrue(message.getContactItems().isEmpty());
        assertTrue(message.isDeleteAll());
    }

    @Test
    void testDecodeBodyWithContacts() {
        // 构造测试数据
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(T8401PhonebookSetting.SettingType.UPDATE); // 设置类型
        buffer.appendByte((byte) 1); // 联系人总数
        
        // 联系人项1
        buffer.appendByte(T8401PhonebookSetting.ContactFlag.INCOMING); // 标志
        String phone = "13800138000";
        buffer.appendByte((byte) phone.length()); // 号码长度
        buffer.appendBytes(phone.getBytes()); // 电话号码
        String name = "张三";
        byte[] nameBytes = name.getBytes(java.nio.charset.Charset.forName("GBK"));
        buffer.appendByte((byte) nameBytes.length); // 联系人长度
        buffer.appendBytes(nameBytes); // 联系人姓名
        
        message.decodeBody(buffer);
        
        assertEquals(T8401PhonebookSetting.SettingType.UPDATE, message.getSettingType());
        assertEquals(1, message.getContactCount());
        assertEquals(1, message.getContactItems().size());
        
        T8401PhonebookSetting.ContactItem contact = message.getContactItems().get(0);
        assertEquals(T8401PhonebookSetting.ContactFlag.INCOMING, contact.getFlag());
        assertEquals(phone, contact.getPhoneNumber());
        assertEquals(name, contact.getContactName());
    }

    @Test
    void testEncodeDecodeConsistency() {
        List<T8401PhonebookSetting.ContactItem> contacts = new ArrayList<>();
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三"));
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.OUTGOING, "13900139000", "李四"));
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.BIDIRECTIONAL, "13700137000", "王五"));
        
        T8401PhonebookSetting original = T8401PhonebookSetting.createUpdate(contacts);
        Buffer buffer = original.encodeBody();
        
        T8401PhonebookSetting decoded = new T8401PhonebookSetting();
        decoded.decodeBody(buffer);
        
        assertEquals(original.getSettingType(), decoded.getSettingType());
        assertEquals(original.getContactCount(), decoded.getContactCount());
        assertEquals(original.getContactItems().size(), decoded.getContactItems().size());
        
        for (int i = 0; i < original.getContactItems().size(); i++) {
            T8401PhonebookSetting.ContactItem originalContact = original.getContactItems().get(i);
            T8401PhonebookSetting.ContactItem decodedContact = decoded.getContactItems().get(i);
            
            assertEquals(originalContact.getFlag(), decodedContact.getFlag());
            assertEquals(originalContact.getPhoneNumber(), decodedContact.getPhoneNumber());
            assertEquals(originalContact.getContactName(), decodedContact.getContactName());
        }
    }

    @Test
    void testEmptyPhoneNumberAndContactName() {
        List<T8401PhonebookSetting.ContactItem> contacts = new ArrayList<>();
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.INCOMING, "", ""));
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.OUTGOING, null, null));
        
        T8401PhonebookSetting msg = T8401PhonebookSetting.createUpdate(contacts);
        Buffer buffer = msg.encodeBody();
        
        T8401PhonebookSetting decoded = new T8401PhonebookSetting();
        decoded.decodeBody(buffer);
        
        assertEquals(2, decoded.getContactItems().size());
        
        T8401PhonebookSetting.ContactItem contact1 = decoded.getContactItems().get(0);
        assertEquals("", contact1.getPhoneNumber());
        assertEquals("", contact1.getContactName());
        
        T8401PhonebookSetting.ContactItem contact2 = decoded.getContactItems().get(1);
        assertEquals("", contact2.getPhoneNumber());
        assertEquals("", contact2.getContactName());
    }

    @Test
    void testMaxLengthPhoneNumber() {
        StringBuilder longPhone = new StringBuilder();
        for (int i = 0; i < 255; i++) {
            longPhone.append("1");
        }
        
        T8401PhonebookSetting.ContactItem contact = new T8401PhonebookSetting.ContactItem();
        contact.setPhoneNumber(longPhone.toString());
        assertEquals(255, contact.getPhoneNumberByteLength());
    }

    @Test
    void testMaxLengthContactName() {
        // 创建一个接近255字节的中文姓名（每个中文字符在GBK编码中占2字节）
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 127; i++) {
            longName.append("中");
        }
        
        T8401PhonebookSetting.ContactItem contact = new T8401PhonebookSetting.ContactItem();
        contact.setContactName(longName.toString());
        assertEquals(254, contact.getContactNameByteLength()); // 127 * 2 = 254
    }

    @Test
    void testExceedMaxPhoneNumberLength() {
        StringBuilder tooLongPhone = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            tooLongPhone.append("1");
        }
        
        T8401PhonebookSetting.ContactItem contact = new T8401PhonebookSetting.ContactItem();
        assertThrows(IllegalArgumentException.class, () -> {
            contact.setPhoneNumber(tooLongPhone.toString());
        });
    }

    @Test
    void testExceedMaxContactNameLength() {
        // 创建一个超过255字节的中文姓名
        StringBuilder tooLongName = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            tooLongName.append("中");
        }
        
        T8401PhonebookSetting.ContactItem contact = new T8401PhonebookSetting.ContactItem();
        assertThrows(IllegalArgumentException.class, () -> {
            contact.setContactName(tooLongName.toString());
        });
    }

    @Test
    void testGbkEncoding() {
        String chineseName = "测试联系人";
        T8401PhonebookSetting.ContactItem contact = new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", chineseName);
        
        List<T8401PhonebookSetting.ContactItem> contacts = new ArrayList<>();
        contacts.add(contact);
        
        T8401PhonebookSetting msg = T8401PhonebookSetting.createUpdate(contacts);
        Buffer buffer = msg.encodeBody();
        
        T8401PhonebookSetting decoded = new T8401PhonebookSetting();
        decoded.decodeBody(buffer);
        
        assertEquals(chineseName, decoded.getContactItems().get(0).getContactName());
    }

    @Test
    void testSettingTypeConstants() {
        assertEquals(0, T8401PhonebookSetting.SettingType.DELETE_ALL);
        assertEquals(1, T8401PhonebookSetting.SettingType.UPDATE);
        assertEquals(2, T8401PhonebookSetting.SettingType.APPEND);
        assertEquals(3, T8401PhonebookSetting.SettingType.MODIFY);
    }

    @Test
    void testContactFlagConstants() {
        assertEquals(1, T8401PhonebookSetting.ContactFlag.INCOMING);
        assertEquals(2, T8401PhonebookSetting.ContactFlag.OUTGOING);
        assertEquals(3, T8401PhonebookSetting.ContactFlag.BIDIRECTIONAL);
    }

    @Test
    void testContactFlagMethods() {
        T8401PhonebookSetting.ContactItem contact1 = new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三");
        assertTrue(contact1.isIncoming());
        assertFalse(contact1.isOutgoing());
        assertFalse(contact1.isBidirectional());
        
        T8401PhonebookSetting.ContactItem contact2 = new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.OUTGOING, "13900139000", "李四");
        assertFalse(contact2.isIncoming());
        assertTrue(contact2.isOutgoing());
        assertFalse(contact2.isBidirectional());
        
        T8401PhonebookSetting.ContactItem contact3 = new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.BIDIRECTIONAL, "13700137000", "王五");
        assertFalse(contact3.isIncoming());
        assertFalse(contact3.isOutgoing());
        assertTrue(contact3.isBidirectional());
    }

    @Test
    void testContactFlagDescription() {
        T8401PhonebookSetting.ContactItem contact1 = new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三");
        assertEquals("呼入", contact1.getFlagDescription());
        
        T8401PhonebookSetting.ContactItem contact2 = new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.OUTGOING, "13900139000", "李四");
        assertEquals("呼出", contact2.getFlagDescription());
        
        T8401PhonebookSetting.ContactItem contact3 = new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.BIDIRECTIONAL, "13700137000", "王五");
        assertEquals("呼入/呼出", contact3.getFlagDescription());
        
        T8401PhonebookSetting.ContactItem contact4 = new T8401PhonebookSetting.ContactItem(
                (byte) 99, "13600136000", "赵六");
        assertEquals("未知标志: 99", contact4.getFlagDescription());
    }

    @Test
    void testSettingTypeDescription() {
        message.setSettingType(T8401PhonebookSetting.SettingType.DELETE_ALL);
        assertEquals("删除终端上所有存储的联系人", message.getSettingTypeDescription());
        
        message.setSettingType(T8401PhonebookSetting.SettingType.UPDATE);
        assertEquals("更新电话本（删除终端中已有全部联系人并追加消息中的联系人）", message.getSettingTypeDescription());
        
        message.setSettingType(T8401PhonebookSetting.SettingType.APPEND);
        assertEquals("追加电话本", message.getSettingTypeDescription());
        
        message.setSettingType(T8401PhonebookSetting.SettingType.MODIFY);
        assertEquals("修改电话本（以联系人为索引）", message.getSettingTypeDescription());
        
        message.setSettingType((byte) 99);
        assertEquals("未知设置类型: 99", message.getSettingTypeDescription());
    }

    @Test
    void testUnsignedValues() {
        message.setContactCount((byte) -1);
        assertEquals(255, message.getContactCountUnsigned());
        
        T8401PhonebookSetting.ContactItem contact = new T8401PhonebookSetting.ContactItem();
        contact.setFlag((byte) -1);
        assertEquals(255, contact.getFlagUnsigned());
    }

    @Test
    void testToString() {
        List<T8401PhonebookSetting.ContactItem> contacts = new ArrayList<>();
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三"));
        
        T8401PhonebookSetting msg = T8401PhonebookSetting.createUpdate(contacts);
        String str = msg.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("T8401PhonebookSetting"));
        assertTrue(str.contains("settingType"));
        assertTrue(str.contains("contactCount"));
    }

    @Test
    void testContactItemToString() {
        T8401PhonebookSetting.ContactItem contact = new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三");
        String str = contact.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("ContactItem"));
        assertTrue(str.contains("flag"));
        assertTrue(str.contains("phoneNumber"));
        assertTrue(str.contains("contactName"));
    }

    @Test
    void testEquals() {
        List<T8401PhonebookSetting.ContactItem> contacts1 = new ArrayList<>();
        contacts1.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三"));
        
        List<T8401PhonebookSetting.ContactItem> contacts2 = new ArrayList<>();
        contacts2.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三"));
        
        T8401PhonebookSetting msg1 = T8401PhonebookSetting.createUpdate(contacts1);
        T8401PhonebookSetting msg2 = T8401PhonebookSetting.createUpdate(contacts2);
        
        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    void testContactItemEquals() {
        T8401PhonebookSetting.ContactItem contact1 = new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三");
        T8401PhonebookSetting.ContactItem contact2 = new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三");
        
        assertEquals(contact1, contact2);
        assertEquals(contact1.hashCode(), contact2.hashCode());
    }

    @Test
    void testGettersAndSetters() {
        message.setSettingType(T8401PhonebookSetting.SettingType.UPDATE);
        assertEquals(T8401PhonebookSetting.SettingType.UPDATE, message.getSettingType());
        
        message.setContactCount((byte) 5);
        assertEquals(5, message.getContactCount());
        
        List<T8401PhonebookSetting.ContactItem> contacts = new ArrayList<>();
        contacts.add(new T8401PhonebookSetting.ContactItem(T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三"));
        message.setContactItems(contacts);
        assertEquals(1, message.getContactItems().size());
        assertEquals(1, message.getContactCount()); // 应该自动更新
    }

    @Test
    void testContactItemGettersAndSetters() {
        T8401PhonebookSetting.ContactItem contact = new T8401PhonebookSetting.ContactItem();
        
        contact.setFlag(T8401PhonebookSetting.ContactFlag.OUTGOING);
        assertEquals(T8401PhonebookSetting.ContactFlag.OUTGOING, contact.getFlag());
        
        contact.setPhoneNumber("13900139000");
        assertEquals("13900139000", contact.getPhoneNumber());
        
        contact.setContactName("李四");
        assertEquals("李四", contact.getContactName());
    }

    @Test
    void testAddContactItem() {
        T8401PhonebookSetting.ContactItem contact = new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三");
        
        message.addContactItem(contact);
        assertEquals(1, message.getContactItems().size());
        assertEquals(1, message.getContactCount());
        
        message.addContactItem(T8401PhonebookSetting.ContactFlag.OUTGOING, "13900139000", "李四");
        assertEquals(2, message.getContactItems().size());
        assertEquals(2, message.getContactCount());
    }

    @Test
    void testGetContactItem() {
        message.addContactItem(T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三");
        message.addContactItem(T8401PhonebookSetting.ContactFlag.OUTGOING, "13900139000", "李四");
        
        T8401PhonebookSetting.ContactItem found = message.getContactItem("13800138000");
        assertNotNull(found);
        assertEquals("张三", found.getContactName());
        
        T8401PhonebookSetting.ContactItem notFound = message.getContactItem("99999999999");
        assertNull(notFound);
    }

    @Test
    void testRemoveContactItem() {
        message.addContactItem(T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三");
        message.addContactItem(T8401PhonebookSetting.ContactFlag.OUTGOING, "13900139000", "李四");
        
        assertTrue(message.removeContactItem("13800138000"));
        assertEquals(1, message.getContactItems().size());
        assertEquals(1, message.getContactCount());
        
        assertFalse(message.removeContactItem("99999999999"));
        assertEquals(1, message.getContactItems().size());
    }

    @Test
    void testClearContactItems() {
        message.addContactItem(T8401PhonebookSetting.ContactFlag.INCOMING, "13800138000", "张三");
        message.addContactItem(T8401PhonebookSetting.ContactFlag.OUTGOING, "13900139000", "李四");
        
        message.clearContactItems();
        assertTrue(message.getContactItems().isEmpty());
        assertEquals(0, message.getContactCount());
    }

    @Test
    void testInvalidMessageBodyDecode() {
        // 测试null消息体
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });
        
        // 测试空消息体
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(Buffer.buffer());
        });
        
        // 测试消息体长度不足（缺少联系人总数）
        Buffer buffer1 = Buffer.buffer();
        buffer1.appendByte(T8401PhonebookSetting.SettingType.UPDATE);
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer1);
        });
        
        // 测试消息体长度不足（缺少联系人项数据）
        Buffer buffer2 = Buffer.buffer();
        buffer2.appendByte(T8401PhonebookSetting.SettingType.UPDATE);
        buffer2.appendByte((byte) 1); // 声明有1个联系人，但没有数据
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer2);
        });
    }

    @Test
    void testMessageFactoryCreation() {
        JT808Message created = factory.createMessage(0x8401);
        assertNotNull(created);
        assertTrue(created instanceof T8401PhonebookSetting);
        assertEquals(0x8401, created.getMessageId());
    }

    @Test
    void testMessageFactorySupport() {
        assertTrue(factory.isSupported(0x8401));
    }

    @Test
    void testRealWorldScenario1_EmergencyContacts() {
        // 场景1：设置紧急联系人电话本
        List<T8401PhonebookSetting.ContactItem> emergencyContacts = new ArrayList<>();
        emergencyContacts.add(new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.BIDIRECTIONAL, "110", "报警电话"));
        emergencyContacts.add(new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.BIDIRECTIONAL, "120", "急救电话"));
        emergencyContacts.add(new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.BIDIRECTIONAL, "119", "消防电话"));
        
        T8401PhonebookSetting msg = T8401PhonebookSetting.createUpdate(emergencyContacts);
        Buffer buffer = msg.encodeBody();
        
        T8401PhonebookSetting decoded = new T8401PhonebookSetting();
        decoded.decodeBody(buffer);
        
        assertEquals(3, decoded.getContactItems().size());
        assertEquals("报警电话", decoded.getContactItem("110").getContactName());
        assertEquals("急救电话", decoded.getContactItem("120").getContactName());
        assertEquals("消防电话", decoded.getContactItem("119").getContactName());
    }

    @Test
    void testRealWorldScenario2_CompanyContacts() {
        // 场景2：追加公司联系人
        List<T8401PhonebookSetting.ContactItem> companyContacts = new ArrayList<>();
        companyContacts.add(new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.OUTGOING, "400-123-4567", "客服热线"));
        companyContacts.add(new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.BIDIRECTIONAL, "13800138000", "调度中心"));
        companyContacts.add(new T8401PhonebookSetting.ContactItem(
                T8401PhonebookSetting.ContactFlag.INCOMING, "13900139000", "监控中心"));
        
        T8401PhonebookSetting msg = T8401PhonebookSetting.createAppend(companyContacts);
        Buffer buffer = msg.encodeBody();
        
        T8401PhonebookSetting decoded = new T8401PhonebookSetting();
        decoded.decodeBody(buffer);
        
        assertTrue(decoded.isAppend());
        assertEquals(3, decoded.getContactItems().size());
        
        T8401PhonebookSetting.ContactItem customerService = decoded.getContactItem("400-123-4567");
        assertNotNull(customerService);
        assertTrue(customerService.isOutgoing());
        assertEquals("客服热线", customerService.getContactName());
    }

    @Test
    void testRealWorldScenario3_DeleteAllContacts() {
        // 场景3：清空所有联系人
        T8401PhonebookSetting msg = T8401PhonebookSetting.createDeleteAll();
        Buffer buffer = msg.encodeBody();
        
        assertEquals(1, buffer.length()); // 只有设置类型字节
        
        T8401PhonebookSetting decoded = new T8401PhonebookSetting();
        decoded.decodeBody(buffer);
        
        assertTrue(decoded.isDeleteAll());
        assertEquals(0, decoded.getContactCount());
        assertTrue(decoded.getContactItems().isEmpty());
        assertEquals("删除终端上所有存储的联系人", decoded.getSettingTypeDescription());
    }
}