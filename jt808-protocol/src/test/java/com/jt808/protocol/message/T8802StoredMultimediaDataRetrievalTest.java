package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8802存储多媒体数据检索消息测试类
 */
class T8802StoredMultimediaDataRetrievalTest {

    @Test
    void testMessageId() {
        T8802StoredMultimediaDataRetrieval message = new T8802StoredMultimediaDataRetrieval();
        assertEquals(MessageTypes.Platform.STORED_MULTIMEDIA_DATA_RETRIEVAL, message.getMessageId());
        assertEquals(0x8802, message.getMessageId());
    }

    @Test
    void testCreateRetrievalMessageWithoutTimeRange() {
        // 创建检索所有图像数据的消息（不按时间范围）
        T8802StoredMultimediaDataRetrieval message = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE,
                0, // 所有通道
                T8802StoredMultimediaDataRetrieval.EventCode.PLATFORM_COMMAND
        );

        assertEquals(0, message.getMultimediaType());
        assertEquals(0, message.getChannelId());
        assertEquals(0, message.getEventCode());
        assertEquals("00-00-00-00-00-00", message.getStartTime());
        assertEquals("00-00-00-00-00-00", message.getEndTime());
        assertTrue(message.isFullTimeRange());
        assertTrue(message.isAllChannels());
    }

    @Test
    void testCreateRetrievalMessageWithTimeRange() {
        // 创建带时间范围的检索消息
        T8802StoredMultimediaDataRetrieval message = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.VIDEO,
                1, // 通道1
                T8802StoredMultimediaDataRetrieval.EventCode.ROBBERY_ALARM,
                "23-12-25-10-30-45", // 起始时间
                "23-12-25-18-30-45"  // 结束时间
        );

        assertEquals(2, message.getMultimediaType());
        assertEquals(1, message.getChannelId());
        assertEquals(2, message.getEventCode());
        assertEquals("23-12-25-10-30-45", message.getStartTime());
        assertEquals("23-12-25-18-30-45", message.getEndTime());
        assertFalse(message.isFullTimeRange());
        assertFalse(message.isAllChannels());
    }

    @Test
    void testCreateRetrievalMessageWithLocalDateTime() {
        LocalDateTime startTime = LocalDateTime.of(2023, 12, 25, 10, 30, 45);
        LocalDateTime endTime = LocalDateTime.of(2023, 12, 25, 18, 30, 45);

        T8802StoredMultimediaDataRetrieval message = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.AUDIO,
                2, // 通道2
                T8802StoredMultimediaDataRetrieval.EventCode.COLLISION_ROLLOVER_ALARM,
                startTime,
                endTime
        );

        assertEquals(1, message.getMultimediaType());
        assertEquals(2, message.getChannelId());
        assertEquals(3, message.getEventCode());
        assertEquals("23-12-25-10-30-45", message.getStartTime());
        assertEquals("23-12-25-18-30-45", message.getEndTime());
    }

    @Test
    void testEncodeAndDecodeFullTimeRange() {
        // 测试不按时间范围的编码解码
        T8802StoredMultimediaDataRetrieval original = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE,
                0,
                T8802StoredMultimediaDataRetrieval.EventCode.PLATFORM_COMMAND
        );

        // 编码
        Buffer encoded = original.encodeBody();
        assertEquals(15, encoded.length()); // 1+1+1+6+6 = 15字节

        // 验证编码内容
        assertEquals(0, encoded.getUnsignedByte(0)); // 多媒体类型
        assertEquals(0, encoded.getUnsignedByte(1)); // 通道ID
        assertEquals(0, encoded.getUnsignedByte(2)); // 事件项编码
        
        // 验证起始时间BCD编码 (00-00-00-00-00-00)
        for (int i = 3; i < 9; i++) {
            assertEquals(0x00, encoded.getUnsignedByte(i));
        }
        
        // 验证结束时间BCD编码 (00-00-00-00-00-00)
        for (int i = 9; i < 15; i++) {
            assertEquals(0x00, encoded.getUnsignedByte(i));
        }

        // 解码
        T8802StoredMultimediaDataRetrieval decoded = new T8802StoredMultimediaDataRetrieval();
        decoded.decodeBody(encoded);

        // 验证解码结果
        assertEquals(original.getMultimediaType(), decoded.getMultimediaType());
        assertEquals(original.getChannelId(), decoded.getChannelId());
        assertEquals(original.getEventCode(), decoded.getEventCode());
        assertEquals(original.getStartTime(), decoded.getStartTime());
        assertEquals(original.getEndTime(), decoded.getEndTime());
        assertEquals(original, decoded);
    }

    @Test
    void testEncodeAndDecodeWithTimeRange() {
        // 测试带时间范围的编码解码
        T8802StoredMultimediaDataRetrieval original = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.VIDEO,
                1,
                T8802StoredMultimediaDataRetrieval.EventCode.TIMED_ACTION,
                "23-12-25-10-30-45",
                "23-12-25-18-30-45"
        );

        // 编码
        Buffer encoded = original.encodeBody();
        assertEquals(15, encoded.length());

        // 验证编码内容
        assertEquals(2, encoded.getUnsignedByte(0)); // 多媒体类型：视频
        assertEquals(1, encoded.getUnsignedByte(1)); // 通道ID：1
        assertEquals(1, encoded.getUnsignedByte(2)); // 事件项编码：定时动作
        
        // 验证起始时间BCD编码 (23-12-25-10-30-45)
        assertEquals(0x23, encoded.getUnsignedByte(3)); // 年
        assertEquals(0x12, encoded.getUnsignedByte(4)); // 月
        assertEquals(0x25, encoded.getUnsignedByte(5)); // 日
        assertEquals(0x10, encoded.getUnsignedByte(6)); // 时
        assertEquals(0x30, encoded.getUnsignedByte(7)); // 分
        assertEquals(0x45, encoded.getUnsignedByte(8)); // 秒
        
        // 验证结束时间BCD编码 (23-12-25-18-30-45)
        assertEquals(0x23, encoded.getUnsignedByte(9));  // 年
        assertEquals(0x12, encoded.getUnsignedByte(10)); // 月
        assertEquals(0x25, encoded.getUnsignedByte(11)); // 日
        assertEquals(0x18, encoded.getUnsignedByte(12)); // 时
        assertEquals(0x30, encoded.getUnsignedByte(13)); // 分
        assertEquals(0x45, encoded.getUnsignedByte(14)); // 秒

        // 解码
        T8802StoredMultimediaDataRetrieval decoded = new T8802StoredMultimediaDataRetrieval();
        decoded.decodeBody(encoded);

        // 验证解码结果
        assertEquals(original.getMultimediaType(), decoded.getMultimediaType());
        assertEquals(original.getChannelId(), decoded.getChannelId());
        assertEquals(original.getEventCode(), decoded.getEventCode());
        assertEquals(original.getStartTime(), decoded.getStartTime());
        assertEquals(original.getEndTime(), decoded.getEndTime());
        assertEquals(original, decoded);
    }

    @Test
    void testMultimediaTypeEnum() {
        // 测试多媒体类型枚举
        assertEquals(0, T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE.getValue());
        assertEquals(1, T8802StoredMultimediaDataRetrieval.MultimediaType.AUDIO.getValue());
        assertEquals(2, T8802StoredMultimediaDataRetrieval.MultimediaType.VIDEO.getValue());
        
        assertEquals("图像", T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE.getDescription());
        assertEquals("音频", T8802StoredMultimediaDataRetrieval.MultimediaType.AUDIO.getDescription());
        assertEquals("视频", T8802StoredMultimediaDataRetrieval.MultimediaType.VIDEO.getDescription());
        
        assertEquals(T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE, 
                T8802StoredMultimediaDataRetrieval.MultimediaType.fromValue(0));
        assertEquals(T8802StoredMultimediaDataRetrieval.MultimediaType.AUDIO, 
                T8802StoredMultimediaDataRetrieval.MultimediaType.fromValue(1));
        assertEquals(T8802StoredMultimediaDataRetrieval.MultimediaType.VIDEO, 
                T8802StoredMultimediaDataRetrieval.MultimediaType.fromValue(2));
        
        assertThrows(IllegalArgumentException.class, () -> 
                T8802StoredMultimediaDataRetrieval.MultimediaType.fromValue(99));
    }

    @Test
    void testEventCodeEnum() {
        // 测试事件项编码枚举
        assertEquals(0, T8802StoredMultimediaDataRetrieval.EventCode.PLATFORM_COMMAND.getValue());
        assertEquals(1, T8802StoredMultimediaDataRetrieval.EventCode.TIMED_ACTION.getValue());
        assertEquals(2, T8802StoredMultimediaDataRetrieval.EventCode.ROBBERY_ALARM.getValue());
        assertEquals(3, T8802StoredMultimediaDataRetrieval.EventCode.COLLISION_ROLLOVER_ALARM.getValue());
        
        assertEquals("平台下发指令", T8802StoredMultimediaDataRetrieval.EventCode.PLATFORM_COMMAND.getDescription());
        assertEquals("定时动作", T8802StoredMultimediaDataRetrieval.EventCode.TIMED_ACTION.getDescription());
        assertEquals("抢劫报警触发", T8802StoredMultimediaDataRetrieval.EventCode.ROBBERY_ALARM.getDescription());
        assertEquals("碰撞侧翻报警触发", T8802StoredMultimediaDataRetrieval.EventCode.COLLISION_ROLLOVER_ALARM.getDescription());
        
        assertEquals(T8802StoredMultimediaDataRetrieval.EventCode.PLATFORM_COMMAND, 
                T8802StoredMultimediaDataRetrieval.EventCode.fromValue(0));
        assertEquals(T8802StoredMultimediaDataRetrieval.EventCode.TIMED_ACTION, 
                T8802StoredMultimediaDataRetrieval.EventCode.fromValue(1));
        assertEquals(T8802StoredMultimediaDataRetrieval.EventCode.ROBBERY_ALARM, 
                T8802StoredMultimediaDataRetrieval.EventCode.fromValue(2));
        assertEquals(T8802StoredMultimediaDataRetrieval.EventCode.COLLISION_ROLLOVER_ALARM, 
                T8802StoredMultimediaDataRetrieval.EventCode.fromValue(3));
        
        assertThrows(IllegalArgumentException.class, () -> 
                T8802StoredMultimediaDataRetrieval.EventCode.fromValue(99));
    }

    @Test
    void testGettersAndSetters() {
        T8802StoredMultimediaDataRetrieval message = new T8802StoredMultimediaDataRetrieval();
        
        message.setMultimediaType(1);
        message.setChannelId(2);
        message.setEventCode(3);
        message.setStartTime("23-01-01-08-00-00");
        message.setEndTime("23-01-01-18-00-00");
        
        assertEquals(1, message.getMultimediaType());
        assertEquals(2, message.getChannelId());
        assertEquals(3, message.getEventCode());
        assertEquals("23-01-01-08-00-00", message.getStartTime());
        assertEquals("23-01-01-18-00-00", message.getEndTime());
    }

    @Test
    void testEnumMethods() {
        T8802StoredMultimediaDataRetrieval message = new T8802StoredMultimediaDataRetrieval();
        message.setMultimediaType(2); // 视频
        message.setEventCode(1);      // 定时动作
        
        assertEquals(T8802StoredMultimediaDataRetrieval.MultimediaType.VIDEO, message.getMultimediaTypeEnum());
        assertEquals(T8802StoredMultimediaDataRetrieval.EventCode.TIMED_ACTION, message.getEventCodeEnum());
        assertEquals("视频", message.getMultimediaTypeDescription());
        assertEquals("定时动作", message.getEventCodeDescription());
    }

    @Test
    void testUnknownEnumValues() {
        T8802StoredMultimediaDataRetrieval message = new T8802StoredMultimediaDataRetrieval();
        message.setMultimediaType(99); // 未知类型
        message.setEventCode(99);      // 未知事件
        
        assertEquals("未知类型(99)", message.getMultimediaTypeDescription());
        assertEquals("未知事件(99)", message.getEventCodeDescription());
    }

    @Test
    void testParseDateTime() {
        T8802StoredMultimediaDataRetrieval message = new T8802StoredMultimediaDataRetrieval();
        
        // 测试正常时间解析
        LocalDateTime dateTime = message.parseDateTime("23-12-25-10-30-45");
        assertNotNull(dateTime);
        assertEquals(2023, dateTime.getYear());
        assertEquals(12, dateTime.getMonthValue());
        assertEquals(25, dateTime.getDayOfMonth());
        assertEquals(10, dateTime.getHour());
        assertEquals(30, dateTime.getMinute());
        assertEquals(45, dateTime.getSecond());
        
        // 测试空时间解析
        LocalDateTime nullDateTime = message.parseDateTime("00-00-00-00-00-00");
        assertNull(nullDateTime);
    }

    @Test
    void testMessageDescription() {
        // 测试全时间范围消息描述
        T8802StoredMultimediaDataRetrieval message1 = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE,
                0,
                T8802StoredMultimediaDataRetrieval.EventCode.PLATFORM_COMMAND
        );
        
        String desc1 = message1.getMessageDescription();
        assertTrue(desc1.contains("存储多媒体数据检索"));
        assertTrue(desc1.contains("图像"));
        assertTrue(desc1.contains("所有通道"));
        assertTrue(desc1.contains("平台下发指令"));
        assertTrue(desc1.contains("全部"));
        
        // 测试带时间范围的消息描述
        T8802StoredMultimediaDataRetrieval message2 = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.VIDEO,
                1,
                T8802StoredMultimediaDataRetrieval.EventCode.ROBBERY_ALARM,
                "23-12-25-10-30-45",
                "23-12-25-18-30-45"
        );
        
        String desc2 = message2.getMessageDescription();
        assertTrue(desc2.contains("视频"));
        assertTrue(desc2.contains("通道1"));
        assertTrue(desc2.contains("抢劫报警触发"));
        assertTrue(desc2.contains("23-12-25-10-30-45~23-12-25-18-30-45"));
    }

    @Test
    void testToString() {
        T8802StoredMultimediaDataRetrieval message = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.AUDIO,
                2,
                T8802StoredMultimediaDataRetrieval.EventCode.COLLISION_ROLLOVER_ALARM,
                "23-12-25-10-30-45",
                "23-12-25-18-30-45"
        );
        
        String str = message.toString();
        assertTrue(str.contains("T8802StoredMultimediaDataRetrieval"));
        assertTrue(str.contains("multimediaType=1"));
        assertTrue(str.contains("channelId=2"));
        assertTrue(str.contains("eventCode=3"));
        assertTrue(str.contains("startTime='23-12-25-10-30-45'"));
        assertTrue(str.contains("endTime='23-12-25-18-30-45'"));
    }

    @Test
    void testEqualsAndHashCode() {
        T8802StoredMultimediaDataRetrieval message1 = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE,
                1,
                T8802StoredMultimediaDataRetrieval.EventCode.PLATFORM_COMMAND,
                "23-12-25-10-30-45",
                "23-12-25-18-30-45"
        );
        
        T8802StoredMultimediaDataRetrieval message2 = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.IMAGE,
                1,
                T8802StoredMultimediaDataRetrieval.EventCode.PLATFORM_COMMAND,
                "23-12-25-10-30-45",
                "23-12-25-18-30-45"
        );
        
        T8802StoredMultimediaDataRetrieval message3 = T8802StoredMultimediaDataRetrieval.createRetrievalMessage(
                T8802StoredMultimediaDataRetrieval.MultimediaType.VIDEO,
                1,
                T8802StoredMultimediaDataRetrieval.EventCode.PLATFORM_COMMAND,
                "23-12-25-10-30-45",
                "23-12-25-18-30-45"
        );
        
        // 测试相等性
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
        
        // 测试哈希码
        assertEquals(message1.hashCode(), message2.hashCode());
        assertNotEquals(message1.hashCode(), message3.hashCode());
    }

    @Test
    void testInvalidTimeFormat() {
        T8802StoredMultimediaDataRetrieval message = new T8802StoredMultimediaDataRetrieval();
        
        // 测试无效时间格式
        assertThrows(IllegalArgumentException.class, () -> {
            message.setStartTime("invalid-time");
            message.encodeBody();
        });
    }

    @Test
    void testInvalidBufferLength() {
        T8802StoredMultimediaDataRetrieval message = new T8802StoredMultimediaDataRetrieval();
        Buffer shortBuffer = Buffer.buffer(new byte[10]); // 长度不足
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(shortBuffer);
        });
    }

    @Test
    void testBcdTimeEncoding() {
        T8802StoredMultimediaDataRetrieval message = new T8802StoredMultimediaDataRetrieval();
        message.setMultimediaType(0);
        message.setChannelId(0);
        message.setEventCode(0);
        message.setStartTime("23-12-25-10-30-45");
        message.setEndTime("23-12-25-18-30-45");
        
        Buffer encoded = message.encodeBody();
        
        // 验证BCD编码是否正确
        // 起始时间: 23-12-25-10-30-45
        assertEquals(0x23, encoded.getUnsignedByte(3));
        assertEquals(0x12, encoded.getUnsignedByte(4));
        assertEquals(0x25, encoded.getUnsignedByte(5));
        assertEquals(0x10, encoded.getUnsignedByte(6));
        assertEquals(0x30, encoded.getUnsignedByte(7));
        assertEquals(0x45, encoded.getUnsignedByte(8));
        
        // 结束时间: 23-12-25-18-30-45
        assertEquals(0x23, encoded.getUnsignedByte(9));
        assertEquals(0x12, encoded.getUnsignedByte(10));
        assertEquals(0x25, encoded.getUnsignedByte(11));
        assertEquals(0x18, encoded.getUnsignedByte(12));
        assertEquals(0x30, encoded.getUnsignedByte(13));
        assertEquals(0x45, encoded.getUnsignedByte(14));
    }
}