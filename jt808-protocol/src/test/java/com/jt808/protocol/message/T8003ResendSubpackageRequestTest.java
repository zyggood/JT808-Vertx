package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8003补传分包请求消息测试类
 */
public class T8003ResendSubpackageRequestTest {

    private T8003ResendSubpackageRequest message;

    @BeforeEach
    void setUp() {
        message = new T8003ResendSubpackageRequest();
    }

    @Test
    void testMessageId() {
        assertEquals(0x8003, message.getMessageId());
    }

    @Test
    void testConstructorWithParameters() {
        List<Integer> packageIds = Arrays.asList(1, 3, 5, 7);
        T8003ResendSubpackageRequest msg = new T8003ResendSubpackageRequest(1234, packageIds);

        assertEquals(1234, msg.getOriginalSerialNumber());
        assertEquals(4, msg.getRetransmitPackageCount());
        assertTrue(msg.containsPackageId(1));
        assertTrue(msg.containsPackageId(3));
        assertTrue(msg.containsPackageId(5));
        assertTrue(msg.containsPackageId(7));
        assertFalse(msg.containsPackageId(2));
    }

    @Test
    void testEncodeAndDecode() {
        // 设置测试数据
        message.setOriginalSerialNumber(5678);
        message.addRetransmitPackageId(2);
        message.addRetransmitPackageId(4);
        message.addRetransmitPackageId(6);

        // 编码
        Buffer encoded = message.encodeBody();

        // 验证编码结果
        assertEquals(6, encoded.length()); // 2字节流水号 + 1字节包数量 + 3字节包序号
        assertEquals(5678, encoded.getUnsignedShort(0)); // 原始消息流水号
        assertEquals(3, encoded.getUnsignedByte(2)); // 重传包数量
        assertEquals(2, encoded.getUnsignedByte(3)); // 第一个包序号
        assertEquals(4, encoded.getUnsignedByte(4)); // 第二个包序号
        assertEquals(6, encoded.getUnsignedByte(5)); // 第三个包序号

        // 解码
        T8003ResendSubpackageRequest decoded = new T8003ResendSubpackageRequest();
        decoded.decodeBody(encoded);

        // 验证解码结果
        assertEquals(5678, decoded.getOriginalSerialNumber());
        assertEquals(3, decoded.getRetransmitPackageCount());
        assertTrue(decoded.containsPackageId(2));
        assertTrue(decoded.containsPackageId(4));
        assertTrue(decoded.containsPackageId(6));
        assertFalse(decoded.containsPackageId(1));
    }

    @Test
    void testEmptyPackageIds() {
        message.setOriginalSerialNumber(9999);

        // 编码空的重传包列表
        Buffer encoded = message.encodeBody();

        assertEquals(3, encoded.length()); // 2字节流水号 + 1字节包数量(0)
        assertEquals(9999, encoded.getUnsignedShort(0));
        assertEquals(0, encoded.getUnsignedByte(2));

        // 解码
        T8003ResendSubpackageRequest decoded = new T8003ResendSubpackageRequest();
        decoded.decodeBody(encoded);

        assertEquals(9999, decoded.getOriginalSerialNumber());
        assertEquals(0, decoded.getRetransmitPackageCount());
        assertTrue(decoded.getRetransmitPackageIds().isEmpty());
    }

    @Test
    void testAddAndRemovePackageIds() {
        message.addRetransmitPackageId(10);
        message.addRetransmitPackageId(20);
        message.addRetransmitPackageId(30);

        assertEquals(3, message.getRetransmitPackageCount());
        assertTrue(message.containsPackageId(20));

        // 移除包序号
        message.removeRetransmitPackageId(20);
        assertEquals(2, message.getRetransmitPackageCount());
        assertFalse(message.containsPackageId(20));
        assertTrue(message.containsPackageId(10));
        assertTrue(message.containsPackageId(30));

        // 清空所有包序号
        message.clearRetransmitPackageIds();
        assertEquals(0, message.getRetransmitPackageCount());
        assertTrue(message.getRetransmitPackageIds().isEmpty());
    }

    @Test
    void testPackageIdValidation() {
        // 测试有效范围内的包序号
        message.addRetransmitPackageId(1);
        message.addRetransmitPackageId(255);
        assertEquals(2, message.getRetransmitPackageCount());

        // 测试无效的包序号（超出范围）
        int initialCount = message.getRetransmitPackageCount();
        message.addRetransmitPackageId(0);   // 无效：小于1
        message.addRetransmitPackageId(256); // 无效：大于255
        assertEquals(initialCount, message.getRetransmitPackageCount()); // 数量不应该增加
    }

    @Test
    void testGetRetransmitPackageIdsArray() {
        message.addRetransmitPackageId(5);
        message.addRetransmitPackageId(10);
        message.addRetransmitPackageId(15);

        int[] array = message.getRetransmitPackageIdsArray();
        assertEquals(3, array.length);

        // 验证数组内容（注意：顺序可能与添加顺序相同）
        List<Integer> arrayList = Arrays.stream(array).boxed().toList();
        assertTrue(arrayList.contains(5));
        assertTrue(arrayList.contains(10));
        assertTrue(arrayList.contains(15));
    }

    @Test
    void testStaticCreateMethods() {
        // 测试使用数组创建
        T8003ResendSubpackageRequest msg1 = T8003ResendSubpackageRequest.create(1111, 1, 2, 3, 4);
        assertEquals(1111, msg1.getOriginalSerialNumber());
        assertEquals(4, msg1.getRetransmitPackageCount());

        // 测试使用列表创建
        List<Integer> packageIds = Arrays.asList(10, 20, 30);
        T8003ResendSubpackageRequest msg2 = T8003ResendSubpackageRequest.create(2222, packageIds);
        assertEquals(2222, msg2.getOriginalSerialNumber());
        assertEquals(3, msg2.getRetransmitPackageCount());
        assertTrue(msg2.containsPackageId(20));
    }

    @Test
    void testSetRetransmitPackageIds() {
        List<Integer> packageIds = new ArrayList<>(Arrays.asList(100, 200, 255));
        message.setRetransmitPackageIds(packageIds);

        assertEquals(3, message.getRetransmitPackageCount());
        assertTrue(message.containsPackageId(100));
        assertTrue(message.containsPackageId(200));
        assertTrue(message.containsPackageId(255));

        // 验证返回的是副本，修改原列表不影响消息对象
        packageIds.add(50);
        assertEquals(3, message.getRetransmitPackageCount()); // 数量不变
        assertFalse(message.containsPackageId(50));
    }

    @Test
    void testGetRetransmitPackageIds() {
        message.addRetransmitPackageId(11);
        message.addRetransmitPackageId(22);

        List<Integer> packageIds = message.getRetransmitPackageIds();
        assertEquals(2, packageIds.size());

        // 验证返回的是副本，修改返回的列表不影响消息对象
        packageIds.add(33);
        assertEquals(2, message.getRetransmitPackageCount()); // 原对象数量不变
        assertFalse(message.containsPackageId(33));
    }

    @Test
    void testToString() {
        message.setOriginalSerialNumber(12345);
        message.addRetransmitPackageId(1);
        message.addRetransmitPackageId(3);

        String str = message.toString();
        assertTrue(str.contains("T8003ResendSubpackageRequest"));
        assertTrue(str.contains("originalSerialNumber=12345"));
        assertTrue(str.contains("retransmitPackageCount=2"));
        assertTrue(str.contains("retransmitPackageIds"));
    }

    @Test
    void testLargePackageList() {
        // 测试大量包序号的情况
        message.setOriginalSerialNumber(65535);

        // 添加100个包序号
        for (int i = 1; i <= 100; i++) {
            message.addRetransmitPackageId(i);
        }

        assertEquals(100, message.getRetransmitPackageCount());

        // 编码和解码
        Buffer encoded = message.encodeBody();
        assertEquals(103, encoded.length()); // 2 + 1 + 100

        T8003ResendSubpackageRequest decoded = new T8003ResendSubpackageRequest();
        decoded.decodeBody(encoded);

        assertEquals(65535, decoded.getOriginalSerialNumber());
        assertEquals(100, decoded.getRetransmitPackageCount());

        // 验证所有包序号都正确解码
        for (int i = 1; i <= 100; i++) {
            assertTrue(decoded.containsPackageId(i));
        }
    }

    @Test
    void testNullPackageIdsList() {
        // 测试设置null列表
        message.setRetransmitPackageIds(null);
        assertEquals(0, message.getRetransmitPackageCount());
        assertTrue(message.getRetransmitPackageIds().isEmpty());

        // 测试构造函数传入null
        T8003ResendSubpackageRequest msg = new T8003ResendSubpackageRequest(1234, null);
        assertEquals(1234, msg.getOriginalSerialNumber());
        assertEquals(0, msg.getRetransmitPackageCount());
    }
}