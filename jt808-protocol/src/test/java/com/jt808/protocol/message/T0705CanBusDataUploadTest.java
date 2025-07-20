package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0705CanBusDataUpload 测试类
 */
public class T0705CanBusDataUploadTest {

    @Test
    public void testMessageId() {
        T0705CanBusDataUpload message = new T0705CanBusDataUpload();
        assertEquals(0x0705, message.getMessageId());
    }

    @Test
    public void testEncodeDecodeNormalCanData() {
        T0705CanBusDataUpload originalMessage = new T0705CanBusDataUpload();
        
        // 设置接收时间：14:30:25.123
        originalMessage.setReceiveTime(LocalTime.of(14, 30, 25, 123_000_000));
        
        // 创建CAN数据项
        T0705CanBusDataUpload.CanBusDataItem item1 = new T0705CanBusDataUpload.CanBusDataItem();
        item1.setCanChannel(0); // CAN1
        item1.setFrameType(0);  // 标准帧
        item1.setDataCollectionMethod(0); // 原始数据
        item1.setCanBusId(0x123); // CAN总线ID
        item1.setCanData(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08});
        
        T0705CanBusDataUpload.CanBusDataItem item2 = new T0705CanBusDataUpload.CanBusDataItem();
        item2.setCanChannel(1); // CAN2
        item2.setFrameType(1);  // 扩展帧
        item2.setDataCollectionMethod(1); // 采集区间的平均值
        item2.setCanBusId(0x456); // CAN总线ID
        item2.setCanData(new byte[]{(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xEE, (byte)0xFF, 0x00, 0x11});
        
        originalMessage.addCanDataItem(item1);
        originalMessage.addCanDataItem(item2);
        
        // 编码
        Buffer encoded = originalMessage.encodeBody();
        assertNotNull(encoded);
        
        // 解码
        T0705CanBusDataUpload decodedMessage = new T0705CanBusDataUpload();
        decodedMessage.decodeBody(encoded);
        
        // 验证基本属性
        assertEquals(2, decodedMessage.getItemCount());
        assertEquals(LocalTime.of(14, 30, 25, 123_000_000), decodedMessage.getReceiveTime());
        assertEquals(2, decodedMessage.getCanDataItems().size());
        
        // 验证第一个CAN数据项
        T0705CanBusDataUpload.CanBusDataItem decodedItem1 = decodedMessage.getCanDataItems().get(0);
        assertEquals(0, decodedItem1.getCanChannel());
        assertEquals(0, decodedItem1.getFrameType());
        assertEquals(0, decodedItem1.getDataCollectionMethod());
        assertEquals(0x123, decodedItem1.getCanBusId());
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08}, decodedItem1.getCanData());
        
        // 验证第二个CAN数据项
        T0705CanBusDataUpload.CanBusDataItem decodedItem2 = decodedMessage.getCanDataItems().get(1);
        assertEquals(1, decodedItem2.getCanChannel());
        assertEquals(1, decodedItem2.getFrameType());
        assertEquals(1, decodedItem2.getDataCollectionMethod());
        assertEquals(0x456, decodedItem2.getCanBusId());
        assertArrayEquals(new byte[]{(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xEE, (byte)0xFF, 0x00, 0x11}, decodedItem2.getCanData());
    }

    @Test
    public void testEmptyCanDataItems() {
        T0705CanBusDataUpload message = new T0705CanBusDataUpload();
        message.setReceiveTime(LocalTime.of(10, 0, 0));
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        
        T0705CanBusDataUpload decoded = new T0705CanBusDataUpload();
        decoded.decodeBody(encoded);
        
        assertEquals(0, decoded.getItemCount());
        assertEquals(LocalTime.of(10, 0, 0), decoded.getReceiveTime());
        assertTrue(decoded.getCanDataItems().isEmpty());
    }

    @Test
    public void testCanDataItemProperties() {
        T0705CanBusDataUpload.CanBusDataItem item = new T0705CanBusDataUpload.CanBusDataItem();
        
        // 测试CAN通道号
        item.setCanChannel(0);
        assertEquals(0, item.getCanChannel());
        item.setCanChannel(1);
        assertEquals(1, item.getCanChannel());
        
        // 测试帧类型
        item.setFrameType(0);
        assertEquals(0, item.getFrameType());
        item.setFrameType(1);
        assertEquals(1, item.getFrameType());
        
        // 测试数据采集方式
        item.setDataCollectionMethod(0);
        assertEquals(0, item.getDataCollectionMethod());
        item.setDataCollectionMethod(1);
        assertEquals(1, item.getDataCollectionMethod());
        
        // 测试CAN总线ID
        item.setCanBusId(0x1FFFFFFF);
        assertEquals(0x1FFFFFFF, item.getCanBusId());
        
        // 测试组合设置
        item.setCanChannel(1);
        item.setFrameType(1);
        item.setDataCollectionMethod(1);
        item.setCanBusId(0x123456);
        
        assertEquals(1, item.getCanChannel());
        assertEquals(1, item.getFrameType());
        assertEquals(1, item.getDataCollectionMethod());
        assertEquals(0x123456, item.getCanBusId());
    }

    @Test
    public void testCanDataItemEncodeDecode() {
        T0705CanBusDataUpload.CanBusDataItem originalItem = new T0705CanBusDataUpload.CanBusDataItem();
        originalItem.setCanChannel(1);
        originalItem.setFrameType(0);
        originalItem.setDataCollectionMethod(1);
        originalItem.setCanBusId(0x789ABC);
        originalItem.setCanData(new byte[]{0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte)0x88});
        
        // 编码
        Buffer encoded = originalItem.encode();
        assertEquals(12, encoded.length()); // 4字节CAN ID + 8字节CAN DATA
        
        // 解码
        T0705CanBusDataUpload.CanBusDataItem decodedItem = new T0705CanBusDataUpload.CanBusDataItem();
        int decodedBytes = decodedItem.decode(encoded, 0);
        
        assertEquals(12, decodedBytes);
        assertEquals(1, decodedItem.getCanChannel());
        assertEquals(0, decodedItem.getFrameType());
        assertEquals(1, decodedItem.getDataCollectionMethod());
        assertEquals(0x789ABC, decodedItem.getCanBusId());
        assertArrayEquals(new byte[]{0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte)0x88}, decodedItem.getCanData());
    }

    @Test
    public void testSetCanDataItems() {
        T0705CanBusDataUpload message = new T0705CanBusDataUpload();
        
        List<T0705CanBusDataUpload.CanBusDataItem> items = new ArrayList<>();
        items.add(new T0705CanBusDataUpload.CanBusDataItem(0x123, new byte[8]));
        items.add(new T0705CanBusDataUpload.CanBusDataItem(0x456, new byte[8]));
        
        message.setCanDataItems(items);
        
        assertEquals(2, message.getItemCount());
        assertEquals(2, message.getCanDataItems().size());
    }

    @Test
    public void testAddCanDataItem() {
        T0705CanBusDataUpload message = new T0705CanBusDataUpload();
        
        assertEquals(0, message.getItemCount());
        
        T0705CanBusDataUpload.CanBusDataItem item1 = new T0705CanBusDataUpload.CanBusDataItem(0x111, new byte[8]);
        message.addCanDataItem(item1);
        
        assertEquals(1, message.getItemCount());
        assertEquals(1, message.getCanDataItems().size());
        
        T0705CanBusDataUpload.CanBusDataItem item2 = new T0705CanBusDataUpload.CanBusDataItem(0x222, new byte[8]);
        message.addCanDataItem(item2);
        
        assertEquals(2, message.getItemCount());
        assertEquals(2, message.getCanDataItems().size());
    }

    @Test
    public void testToString() {
        T0705CanBusDataUpload message = new T0705CanBusDataUpload();
        message.setReceiveTime(LocalTime.of(12, 30, 45, 500_000_000));
        
        T0705CanBusDataUpload.CanBusDataItem item = new T0705CanBusDataUpload.CanBusDataItem();
        item.setCanChannel(1);
        item.setFrameType(1);
        item.setDataCollectionMethod(0);
        item.setCanBusId(0xABC);
        item.setCanData(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08});
        
        message.addCanDataItem(item);
        
        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("T0705CanBusDataUpload"));
        assertTrue(result.contains("itemCount=1"));
        assertTrue(result.contains("12:30:45.500"));
        
        String itemResult = item.toString();
        assertNotNull(itemResult);
        assertTrue(itemResult.contains("CanBusDataItem"));
        assertTrue(itemResult.contains("channel=1"));
        assertTrue(itemResult.contains("frameType=1"));
        assertTrue(itemResult.contains("collectionMethod=0"));
        assertTrue(itemResult.contains("busId=0xABC"));
    }

    @Test
    public void testNullTimeHandling() {
        T0705CanBusDataUpload message = new T0705CanBusDataUpload();
        // 不设置时间，应该编码为全0
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        
        T0705CanBusDataUpload decoded = new T0705CanBusDataUpload();
        decoded.decodeBody(encoded);
        
        assertEquals(0, decoded.getItemCount());
        assertEquals(LocalTime.of(0, 0, 0), decoded.getReceiveTime());
    }

    @Test
    public void testCanIdBitOperations() {
        T0705CanBusDataUpload.CanBusDataItem item = new T0705CanBusDataUpload.CanBusDataItem();
        
        // 测试所有位的组合
        item.setCanChannel(1);           // bit31 = 1
        item.setFrameType(1);            // bit30 = 1  
        item.setDataCollectionMethod(1); // bit29 = 1
        item.setCanBusId(0x1FFFFFFF);    // bit28-0 = 全1
        
        long expectedCanId = 0xFFFFFFFFL; // 所有位都是1
        assertEquals(expectedCanId, item.getCanId());
        
        // 测试清零操作
        item.setCanChannel(0);           // bit31 = 0
        item.setFrameType(0);            // bit30 = 0
        item.setDataCollectionMethod(0); // bit29 = 0
        item.setCanBusId(0);             // bit28-0 = 0
        
        assertEquals(0, item.getCanId());
    }
}