package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0802存储多媒体数据检索应答消息测试
 */
@DisplayName("T0802存储多媒体数据检索应答消息测试")
class T0802StoredMultimediaDataRetrievalResponseTest {

    private T0802StoredMultimediaDataRetrievalResponse message;
    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        message = new T0802StoredMultimediaDataRetrievalResponse();
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x0802, message.getMessageId());
        assertEquals(MessageTypes.Terminal.STORED_MULTIMEDIA_DATA_RETRIEVAL_RESPONSE, message.getMessageId());
    }

    @Test
    @DisplayName("测试基本构造函数")
    void testBasicConstructor() {
        T0802StoredMultimediaDataRetrievalResponse msg = new T0802StoredMultimediaDataRetrievalResponse(12345, 10);
        assertEquals(12345, msg.getResponseSerialNumber());
        assertEquals(10, msg.getTotalCount());
        assertTrue(msg.getRetrievalItems().isEmpty());
    }

    @Test
    @DisplayName("测试完整构造函数")
    void testFullConstructor() {
        List<T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem> items = new ArrayList<>();
        byte[] locationInfo = new byte[28];
        Arrays.fill(locationInfo, (byte) 0x01);
        
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                123456L, 
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE,
                1,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND,
                locationInfo
            );
        items.add(item);
        
        T0802StoredMultimediaDataRetrievalResponse msg = new T0802StoredMultimediaDataRetrievalResponse(12345, 1, items);
        assertEquals(12345, msg.getResponseSerialNumber());
        assertEquals(1, msg.getTotalCount());
        assertEquals(1, msg.getRetrievalItems().size());
    }

    @Test
    @DisplayName("测试编码和解码")
    void testEncodeAndDecode() {
        // 创建测试数据
        T0802StoredMultimediaDataRetrievalResponse original = new T0802StoredMultimediaDataRetrievalResponse(12345, 2);
        
        // 添加第一个检索项
        byte[] locationInfo1 = new byte[28];
        Arrays.fill(locationInfo1, (byte) 0x01);
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item1 = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                123456L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE,
                1,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND,
                locationInfo1
            );
        original.addRetrievalItem(item1);
        
        // 添加第二个检索项
        byte[] locationInfo2 = new byte[28];
        Arrays.fill(locationInfo2, (byte) 0x02);
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item2 = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                789012L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.VIDEO,
                2,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.ROBBERY_ALARM,
                locationInfo2
            );
        original.addRetrievalItem(item2);
        
        // 编码
        Buffer encoded = original.encode();
        assertNotNull(encoded);
        assertEquals(4 + 35 * 2, encoded.length()); // 4字节头部 + 2个35字节的检索项
        
        // 解码
        T0802StoredMultimediaDataRetrievalResponse decoded = T0802StoredMultimediaDataRetrievalResponse.decode(encoded);
        assertNotNull(decoded);
        assertEquals(original.getResponseSerialNumber(), decoded.getResponseSerialNumber());
        assertEquals(original.getTotalCount(), decoded.getTotalCount());
        assertEquals(original.getRetrievalItems().size(), decoded.getRetrievalItems().size());
        
        // 验证第一个检索项
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem decodedItem1 = decoded.getRetrievalItems().get(0);
        assertEquals(item1.getMultimediaId(), decodedItem1.getMultimediaId());
        assertEquals(item1.getMultimediaType(), decodedItem1.getMultimediaType());
        assertEquals(item1.getChannelId(), decodedItem1.getChannelId());
        assertEquals(item1.getEventCode(), decodedItem1.getEventCode());
        assertArrayEquals(item1.getLocationInfo(), decodedItem1.getLocationInfo());
        
        // 验证第二个检索项
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem decodedItem2 = decoded.getRetrievalItems().get(1);
        assertEquals(item2.getMultimediaId(), decodedItem2.getMultimediaId());
        assertEquals(item2.getMultimediaType(), decodedItem2.getMultimediaType());
        assertEquals(item2.getChannelId(), decodedItem2.getChannelId());
        assertEquals(item2.getEventCode(), decodedItem2.getEventCode());
        assertArrayEquals(item2.getLocationInfo(), decodedItem2.getLocationInfo());
    }

    @Test
    @DisplayName("测试空检索项列表的编码解码")
    void testEmptyRetrievalItemsEncodeAndDecode() {
        T0802StoredMultimediaDataRetrievalResponse original = new T0802StoredMultimediaDataRetrievalResponse(54321, 0);
        
        // 编码
        Buffer encoded = original.encode();
        assertNotNull(encoded);
        assertEquals(4, encoded.length()); // 只有4字节头部
        
        // 解码
        T0802StoredMultimediaDataRetrievalResponse decoded = T0802StoredMultimediaDataRetrievalResponse.decode(encoded);
        assertNotNull(decoded);
        assertEquals(original.getResponseSerialNumber(), decoded.getResponseSerialNumber());
        assertEquals(original.getTotalCount(), decoded.getTotalCount());
        assertTrue(decoded.getRetrievalItems().isEmpty());
    }

    @Test
    @DisplayName("测试多媒体类型枚举")
    void testMultimediaTypeEnum() {
        assertEquals(0, T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE.getValue());
        assertEquals(1, T0802StoredMultimediaDataRetrievalResponse.MultimediaType.AUDIO.getValue());
        assertEquals(2, T0802StoredMultimediaDataRetrievalResponse.MultimediaType.VIDEO.getValue());
        
        assertEquals("图像", T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE.getDescription());
        assertEquals("音频", T0802StoredMultimediaDataRetrievalResponse.MultimediaType.AUDIO.getDescription());
        assertEquals("视频", T0802StoredMultimediaDataRetrievalResponse.MultimediaType.VIDEO.getDescription());
        
        // 测试fromValue方法
        assertEquals(T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE, 
                    T0802StoredMultimediaDataRetrievalResponse.MultimediaType.fromValue(0));
        assertEquals(T0802StoredMultimediaDataRetrievalResponse.MultimediaType.AUDIO, 
                    T0802StoredMultimediaDataRetrievalResponse.MultimediaType.fromValue(1));
        assertEquals(T0802StoredMultimediaDataRetrievalResponse.MultimediaType.VIDEO, 
                    T0802StoredMultimediaDataRetrievalResponse.MultimediaType.fromValue(2));
        assertEquals(T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE, 
                    T0802StoredMultimediaDataRetrievalResponse.MultimediaType.fromValue(99)); // 默认值
    }

    @Test
    @DisplayName("测试事件编码枚举")
    void testEventCodeEnum() {
        assertEquals(0, T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND.getValue());
        assertEquals(1, T0802StoredMultimediaDataRetrievalResponse.EventCode.TIMED_ACTION.getValue());
        assertEquals(2, T0802StoredMultimediaDataRetrievalResponse.EventCode.ROBBERY_ALARM.getValue());
        assertEquals(3, T0802StoredMultimediaDataRetrievalResponse.EventCode.COLLISION_ROLLOVER_ALARM.getValue());
        
        assertEquals("平台下发指令", T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND.getDescription());
        assertEquals("定时动作", T0802StoredMultimediaDataRetrievalResponse.EventCode.TIMED_ACTION.getDescription());
        assertEquals("抢劫报警触发", T0802StoredMultimediaDataRetrievalResponse.EventCode.ROBBERY_ALARM.getDescription());
        assertEquals("碰撞侧翻报警触发", T0802StoredMultimediaDataRetrievalResponse.EventCode.COLLISION_ROLLOVER_ALARM.getDescription());
        
        // 测试fromValue方法
        assertEquals(T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND, 
                    T0802StoredMultimediaDataRetrievalResponse.EventCode.fromValue(0));
        assertEquals(T0802StoredMultimediaDataRetrievalResponse.EventCode.TIMED_ACTION, 
                    T0802StoredMultimediaDataRetrievalResponse.EventCode.fromValue(1));
        assertEquals(T0802StoredMultimediaDataRetrievalResponse.EventCode.ROBBERY_ALARM, 
                    T0802StoredMultimediaDataRetrievalResponse.EventCode.fromValue(2));
        assertEquals(T0802StoredMultimediaDataRetrievalResponse.EventCode.COLLISION_ROLLOVER_ALARM, 
                    T0802StoredMultimediaDataRetrievalResponse.EventCode.fromValue(3));
        assertEquals(T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND, 
                    T0802StoredMultimediaDataRetrievalResponse.EventCode.fromValue(99)); // 默认值
    }

    @Test
    @DisplayName("测试检索项的getter和setter")
    void testRetrievalItemGettersAndSetters() {
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem();
        
        // 测试多媒体ID
        item.setMultimediaId(999999L);
        assertEquals(999999L, item.getMultimediaId());
        
        // 测试多媒体类型
        item.setMultimediaType(T0802StoredMultimediaDataRetrievalResponse.MultimediaType.AUDIO);
        assertEquals(T0802StoredMultimediaDataRetrievalResponse.MultimediaType.AUDIO, item.getMultimediaType());
        
        // 测试通道ID
        item.setChannelId(5);
        assertEquals(5, item.getChannelId());
        
        // 测试事件编码
        item.setEventCode(T0802StoredMultimediaDataRetrievalResponse.EventCode.TIMED_ACTION);
        assertEquals(T0802StoredMultimediaDataRetrievalResponse.EventCode.TIMED_ACTION, item.getEventCode());
        
        // 测试位置信息
        byte[] locationInfo = new byte[28];
        Arrays.fill(locationInfo, (byte) 0xFF);
        item.setLocationInfo(locationInfo);
        assertArrayEquals(locationInfo, item.getLocationInfo());
        
        // 测试设置错误长度的位置信息
        byte[] wrongLengthInfo = new byte[10];
        item.setLocationInfo(wrongLengthInfo);
        assertEquals(28, item.getLocationInfo().length); // 应该保持28字节
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        T0802StoredMultimediaDataRetrievalResponse msg = new T0802StoredMultimediaDataRetrievalResponse(12345, 1);
        String str = msg.toString();
        assertNotNull(str);
        assertTrue(str.contains("T0802StoredMultimediaDataRetrievalResponse"));
        assertTrue(str.contains("responseSerialNumber=12345"));
        assertTrue(str.contains("totalCount=1"));
        
        // 测试检索项的toString
        byte[] locationInfo = new byte[28];
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                123456L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE,
                1,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND,
                locationInfo
            );
        String itemStr = item.toString();
        assertNotNull(itemStr);
        assertTrue(itemStr.contains("MultimediaRetrievalItem"));
        assertTrue(itemStr.contains("multimediaId=123456"));
        assertTrue(itemStr.contains("locationInfoLength=28"));
    }

    @Test
    @DisplayName("测试equals和hashCode方法")
    void testEqualsAndHashCode() {
        byte[] locationInfo = new byte[28];
        Arrays.fill(locationInfo, (byte) 0x01);
        
        T0802StoredMultimediaDataRetrievalResponse msg1 = new T0802StoredMultimediaDataRetrievalResponse(12345, 1);
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item1 = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                123456L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE,
                1,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND,
                locationInfo
            );
        msg1.addRetrievalItem(item1);
        
        T0802StoredMultimediaDataRetrievalResponse msg2 = new T0802StoredMultimediaDataRetrievalResponse(12345, 1);
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item2 = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                123456L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE,
                1,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND,
                locationInfo.clone()
            );
        msg2.addRetrievalItem(item2);
        
        // 测试相等性
        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
        
        // 测试检索项的相等性
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
        
        // 测试不相等的情况
        T0802StoredMultimediaDataRetrievalResponse msg3 = new T0802StoredMultimediaDataRetrievalResponse(54321, 1);
        assertNotEquals(msg1, msg3);
    }

    @Test
    @DisplayName("测试异常处理")
    void testExceptionHandling() {
        // 测试解码时缓冲区太短
        Buffer shortBuffer = Buffer.buffer(new byte[]{0x01, 0x02});
        assertThrows(IllegalArgumentException.class, () -> {
            T0802StoredMultimediaDataRetrievalResponse.decode(shortBuffer);
        });
        
        // 测试检索项解码时缓冲区太短
        Buffer shortItemBuffer = Buffer.buffer(new byte[10]);
        assertThrows(IllegalArgumentException.class, () -> {
            T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem.decode(shortItemBuffer);
        });
    }

    @Test
    @DisplayName("测试工厂创建消息")
    void testFactoryCreateMessage() {
        JT808Message msg = factory.createMessage(0x0802);
        assertNotNull(msg);
        assertInstanceOf(T0802StoredMultimediaDataRetrievalResponse.class, msg);
        assertEquals(0x0802, msg.getMessageId());
    }

    @Test
    @DisplayName("测试添加检索项")
    void testAddRetrievalItem() {
        T0802StoredMultimediaDataRetrievalResponse msg = new T0802StoredMultimediaDataRetrievalResponse();
        assertEquals(0, msg.getRetrievalItems().size());
        
        byte[] locationInfo = new byte[28];
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                123456L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE,
                1,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND,
                locationInfo
            );
        
        msg.addRetrievalItem(item);
        assertEquals(1, msg.getRetrievalItems().size());
        
        // 测试添加null项
        msg.addRetrievalItem(null);
        assertEquals(1, msg.getRetrievalItems().size()); // 不应该增加
    }

    @Test
    @DisplayName("测试设置检索项列表")
    void testSetRetrievalItems() {
        T0802StoredMultimediaDataRetrievalResponse msg = new T0802StoredMultimediaDataRetrievalResponse();
        
        List<T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem> items = new ArrayList<>();
        byte[] locationInfo = new byte[28];
        T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem item = 
            new T0802StoredMultimediaDataRetrievalResponse.MultimediaRetrievalItem(
                123456L,
                T0802StoredMultimediaDataRetrievalResponse.MultimediaType.IMAGE,
                1,
                T0802StoredMultimediaDataRetrievalResponse.EventCode.PLATFORM_COMMAND,
                locationInfo
            );
        items.add(item);
        
        msg.setRetrievalItems(items);
        assertEquals(1, msg.getRetrievalItems().size());
        
        // 测试设置null列表
        msg.setRetrievalItems(null);
        assertTrue(msg.getRetrievalItems().isEmpty());
    }
}