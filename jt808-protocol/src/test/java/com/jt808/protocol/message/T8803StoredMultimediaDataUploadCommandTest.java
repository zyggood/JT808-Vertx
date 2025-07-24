package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8803存储多媒体数据上传命令测试类
 */
class T8803StoredMultimediaDataUploadCommandTest {
    
    @Test
    void testMessageId() {
        T8803StoredMultimediaDataUploadCommand message = new T8803StoredMultimediaDataUploadCommand();
        assertEquals(MessageTypes.Platform.STORED_MULTIMEDIA_DATA_UPLOAD_COMMAND, message.getMessageId());
        assertEquals(0x8803, message.getMessageId());
    }
    
    @Test
    void testConstructorAndGetters() {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 24, 10, 30, 45);
        LocalDateTime endTime = LocalDateTime.of(2024, 7, 24, 11, 30, 45);
        
        T8803StoredMultimediaDataUploadCommand message = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE,
            1,
            T8803StoredMultimediaDataUploadCommand.EventCode.PLATFORM_COMMAND,
            startTime,
            endTime,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP
        );
        
        assertEquals(T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE, message.getMultimediaType());
        assertEquals(1, message.getChannelId());
        assertEquals(T8803StoredMultimediaDataUploadCommand.EventCode.PLATFORM_COMMAND, message.getEventCode());
        assertEquals(startTime, message.getStartTime());
        assertEquals(endTime, message.getEndTime());
        assertEquals(T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP, message.getDeleteFlag());
    }
    
    @Test
    void testEncodeAndDecode() {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 24, 10, 30, 45);
        LocalDateTime endTime = LocalDateTime.of(2024, 7, 24, 11, 30, 45);
        
        T8803StoredMultimediaDataUploadCommand original = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.VIDEO,
            2,
            T8803StoredMultimediaDataUploadCommand.EventCode.ROBBERY_ALARM,
            startTime,
            endTime,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.DELETE
        );
        
        // 编码
        Buffer encoded = original.encode();
        assertNotNull(encoded);
        assertEquals(16, encoded.length()); // 1+1+1+6+6+1 = 16字节
        
        // 解码
        T8803StoredMultimediaDataUploadCommand decoded = T8803StoredMultimediaDataUploadCommand.decode(encoded);
        
        // 验证解码结果
        assertEquals(original.getMultimediaType(), decoded.getMultimediaType());
        assertEquals(original.getChannelId(), decoded.getChannelId());
        assertEquals(original.getEventCode(), decoded.getEventCode());
        assertEquals(original.getStartTime(), decoded.getStartTime());
        assertEquals(original.getEndTime(), decoded.getEndTime());
        assertEquals(original.getDeleteFlag(), decoded.getDeleteFlag());
    }
    
    @Test
    void testDecodeBodyMethod() {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 24, 10, 30, 45);
        LocalDateTime endTime = LocalDateTime.of(2024, 7, 24, 11, 30, 45);
        
        T8803StoredMultimediaDataUploadCommand original = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.AUDIO,
            3,
            T8803StoredMultimediaDataUploadCommand.EventCode.COLLISION_ROLLOVER_ALARM,
            startTime,
            endTime,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP
        );
        
        Buffer encoded = original.encodeBody();
        
        T8803StoredMultimediaDataUploadCommand target = new T8803StoredMultimediaDataUploadCommand();
        target.decodeBody(encoded);
        
        assertEquals(original.getMultimediaType(), target.getMultimediaType());
        assertEquals(original.getChannelId(), target.getChannelId());
        assertEquals(original.getEventCode(), target.getEventCode());
        assertEquals(original.getStartTime(), target.getStartTime());
        assertEquals(original.getEndTime(), target.getEndTime());
        assertEquals(original.getDeleteFlag(), target.getDeleteFlag());
    }
    
    @Test
    void testMultimediaTypeEnum() {
        assertEquals(0, T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE.getValue());
        assertEquals(1, T8803StoredMultimediaDataUploadCommand.MultimediaType.AUDIO.getValue());
        assertEquals(2, T8803StoredMultimediaDataUploadCommand.MultimediaType.VIDEO.getValue());
        
        assertEquals("图像", T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE.getDescription());
        assertEquals("音频", T8803StoredMultimediaDataUploadCommand.MultimediaType.AUDIO.getDescription());
        assertEquals("视频", T8803StoredMultimediaDataUploadCommand.MultimediaType.VIDEO.getDescription());
        
        assertEquals(T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE, 
                    T8803StoredMultimediaDataUploadCommand.MultimediaType.fromValue(0));
        assertEquals(T8803StoredMultimediaDataUploadCommand.MultimediaType.AUDIO, 
                    T8803StoredMultimediaDataUploadCommand.MultimediaType.fromValue(1));
        assertEquals(T8803StoredMultimediaDataUploadCommand.MultimediaType.VIDEO, 
                    T8803StoredMultimediaDataUploadCommand.MultimediaType.fromValue(2));
    }
    
    @Test
    void testEventCodeEnum() {
        assertEquals(0, T8803StoredMultimediaDataUploadCommand.EventCode.PLATFORM_COMMAND.getValue());
        assertEquals(1, T8803StoredMultimediaDataUploadCommand.EventCode.TIMED_ACTION.getValue());
        assertEquals(2, T8803StoredMultimediaDataUploadCommand.EventCode.ROBBERY_ALARM.getValue());
        assertEquals(3, T8803StoredMultimediaDataUploadCommand.EventCode.COLLISION_ROLLOVER_ALARM.getValue());
        
        assertEquals("平台下发指令", T8803StoredMultimediaDataUploadCommand.EventCode.PLATFORM_COMMAND.getDescription());
        assertEquals("定时动作", T8803StoredMultimediaDataUploadCommand.EventCode.TIMED_ACTION.getDescription());
        assertEquals("抢劫报警触发", T8803StoredMultimediaDataUploadCommand.EventCode.ROBBERY_ALARM.getDescription());
        assertEquals("碰撞侧翻报警触发", T8803StoredMultimediaDataUploadCommand.EventCode.COLLISION_ROLLOVER_ALARM.getDescription());
    }
    
    @Test
    void testDeleteFlagEnum() {
        assertEquals(0, T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP.getValue());
        assertEquals(1, T8803StoredMultimediaDataUploadCommand.DeleteFlag.DELETE.getValue());
        
        assertEquals("保留", T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP.getDescription());
        assertEquals("删除", T8803StoredMultimediaDataUploadCommand.DeleteFlag.DELETE.getDescription());
    }
    
    @Test
    void testSetters() {
        T8803StoredMultimediaDataUploadCommand message = new T8803StoredMultimediaDataUploadCommand();
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 24, 10, 30, 45);
        LocalDateTime endTime = LocalDateTime.of(2024, 7, 24, 11, 30, 45);
        
        message.setMultimediaType(T8803StoredMultimediaDataUploadCommand.MultimediaType.VIDEO);
        message.setChannelId(5);
        message.setEventCode(T8803StoredMultimediaDataUploadCommand.EventCode.TIMED_ACTION);
        message.setStartTime(startTime);
        message.setEndTime(endTime);
        message.setDeleteFlag(T8803StoredMultimediaDataUploadCommand.DeleteFlag.DELETE);
        
        assertEquals(T8803StoredMultimediaDataUploadCommand.MultimediaType.VIDEO, message.getMultimediaType());
        assertEquals(5, message.getChannelId());
        assertEquals(T8803StoredMultimediaDataUploadCommand.EventCode.TIMED_ACTION, message.getEventCode());
        assertEquals(startTime, message.getStartTime());
        assertEquals(endTime, message.getEndTime());
        assertEquals(T8803StoredMultimediaDataUploadCommand.DeleteFlag.DELETE, message.getDeleteFlag());
    }
    
    @Test
    void testToString() {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 24, 10, 30, 45);
        LocalDateTime endTime = LocalDateTime.of(2024, 7, 24, 11, 30, 45);
        
        T8803StoredMultimediaDataUploadCommand message = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE,
            1,
            T8803StoredMultimediaDataUploadCommand.EventCode.PLATFORM_COMMAND,
            startTime,
            endTime,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP
        );
        
        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("T8803StoredMultimediaDataUploadCommand"));
        assertTrue(result.contains("IMAGE"));
        assertTrue(result.contains("channelId=1"));
        assertTrue(result.contains("PLATFORM_COMMAND"));
        assertTrue(result.contains("KEEP"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 24, 10, 30, 45);
        LocalDateTime endTime = LocalDateTime.of(2024, 7, 24, 11, 30, 45);
        
        T8803StoredMultimediaDataUploadCommand message1 = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE,
            1,
            T8803StoredMultimediaDataUploadCommand.EventCode.PLATFORM_COMMAND,
            startTime,
            endTime,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP
        );
        
        T8803StoredMultimediaDataUploadCommand message2 = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE,
            1,
            T8803StoredMultimediaDataUploadCommand.EventCode.PLATFORM_COMMAND,
            startTime,
            endTime,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP
        );
        
        T8803StoredMultimediaDataUploadCommand message3 = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.VIDEO,
            2,
            T8803StoredMultimediaDataUploadCommand.EventCode.ROBBERY_ALARM,
            startTime,
            endTime,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.DELETE
        );
        
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertEquals(message1.hashCode(), message2.hashCode());
        assertNotEquals(message1.hashCode(), message3.hashCode());
    }
    
    @Test
    void testInvalidBufferLength() {
        Buffer shortBuffer = Buffer.buffer(new byte[10]); // 少于16字节
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8803StoredMultimediaDataUploadCommand.decode(shortBuffer);
        });
    }
    
    @Test
    void testInvalidEnumValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            T8803StoredMultimediaDataUploadCommand.MultimediaType.fromValue(99);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8803StoredMultimediaDataUploadCommand.EventCode.fromValue(99);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.fromValue(99);
        });
    }
    
    @Test
    void testFactoryCreation() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(MessageTypes.Platform.STORED_MULTIMEDIA_DATA_UPLOAD_COMMAND);
        
        assertNotNull(message);
        assertInstanceOf(T8803StoredMultimediaDataUploadCommand.class, message);
        assertEquals(MessageTypes.Platform.STORED_MULTIMEDIA_DATA_UPLOAD_COMMAND, message.getMessageId());
    }
    
    @Test
    void testBcdTimeEncoding() {
        LocalDateTime time1 = LocalDateTime.of(2024, 7, 24, 10, 30, 45);
        LocalDateTime time2 = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        
        T8803StoredMultimediaDataUploadCommand message = new T8803StoredMultimediaDataUploadCommand(
            T8803StoredMultimediaDataUploadCommand.MultimediaType.IMAGE,
            1,
            T8803StoredMultimediaDataUploadCommand.EventCode.PLATFORM_COMMAND,
            time1,
            time2,
            T8803StoredMultimediaDataUploadCommand.DeleteFlag.KEEP
        );
        
        Buffer encoded = message.encode();
        T8803StoredMultimediaDataUploadCommand decoded = T8803StoredMultimediaDataUploadCommand.decode(encoded);
        
        assertEquals(time1, decoded.getStartTime());
        assertEquals(time2, decoded.getEndTime());
    }
}