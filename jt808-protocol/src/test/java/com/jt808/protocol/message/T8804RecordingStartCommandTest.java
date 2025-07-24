package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8804录音开始命令测试类
 */
class T8804RecordingStartCommandTest {
    
    @Test
    void testMessageId() {
        T8804RecordingStartCommand message = new T8804RecordingStartCommand();
        assertEquals(MessageTypes.Platform.RECORDING_START_COMMAND, message.getMessageId());
        assertEquals(0x8804, message.getMessageId());
    }
    
    @Test
    void testConstructorAndGetters() {
        T8804RecordingStartCommand message = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            300, // 5分钟
            T8804RecordingStartCommand.SaveFlag.SAVE,
            T8804RecordingStartCommand.AudioSampleRate.RATE_8K
        );
        
        assertEquals(T8804RecordingStartCommand.RecordingCommand.START, message.getRecordingCommand());
        assertEquals(300, message.getRecordingTime());
        assertEquals(T8804RecordingStartCommand.SaveFlag.SAVE, message.getSaveFlag());
        assertEquals(T8804RecordingStartCommand.AudioSampleRate.RATE_8K, message.getAudioSampleRate());
    }
    
    @Test
    void testEncodeAndDecode() {
        T8804RecordingStartCommand original = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            600, // 10分钟
            T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD,
            T8804RecordingStartCommand.AudioSampleRate.RATE_32K
        );
        
        // 编码
        Buffer encoded = original.encode();
        assertNotNull(encoded);
        assertEquals(5, encoded.length()); // 1+2+1+1 = 5字节
        
        // 解码
        T8804RecordingStartCommand decoded = T8804RecordingStartCommand.decode(encoded);
        
        // 验证解码结果
        assertEquals(original.getRecordingCommand(), decoded.getRecordingCommand());
        assertEquals(original.getRecordingTime(), decoded.getRecordingTime());
        assertEquals(original.getSaveFlag(), decoded.getSaveFlag());
        assertEquals(original.getAudioSampleRate(), decoded.getAudioSampleRate());
    }
    
    @Test
    void testDecodeBodyMethod() {
        T8804RecordingStartCommand original = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.STOP,
            0, // 停止录音
            T8804RecordingStartCommand.SaveFlag.SAVE,
            T8804RecordingStartCommand.AudioSampleRate.RATE_11K
        );
        
        Buffer encoded = original.encodeBody();
        
        T8804RecordingStartCommand target = new T8804RecordingStartCommand();
        target.decodeBody(encoded);
        
        assertEquals(original.getRecordingCommand(), target.getRecordingCommand());
        assertEquals(original.getRecordingTime(), target.getRecordingTime());
        assertEquals(original.getSaveFlag(), target.getSaveFlag());
        assertEquals(original.getAudioSampleRate(), target.getAudioSampleRate());
    }
    
    @Test
    void testRecordingCommandEnum() {
        assertEquals(0, T8804RecordingStartCommand.RecordingCommand.STOP.getValue());
        assertEquals(1, T8804RecordingStartCommand.RecordingCommand.START.getValue());
        
        assertEquals("停止录音", T8804RecordingStartCommand.RecordingCommand.STOP.getDescription());
        assertEquals("开始录音", T8804RecordingStartCommand.RecordingCommand.START.getDescription());
        
        assertEquals(T8804RecordingStartCommand.RecordingCommand.STOP, 
                    T8804RecordingStartCommand.RecordingCommand.fromValue(0));
        assertEquals(T8804RecordingStartCommand.RecordingCommand.START, 
                    T8804RecordingStartCommand.RecordingCommand.fromValue(1));
    }
    
    @Test
    void testSaveFlagEnum() {
        assertEquals(0, T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD.getValue());
        assertEquals(1, T8804RecordingStartCommand.SaveFlag.SAVE.getValue());
        
        assertEquals("实时上传", T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD.getDescription());
        assertEquals("保存", T8804RecordingStartCommand.SaveFlag.SAVE.getDescription());
        
        assertEquals(T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD, 
                    T8804RecordingStartCommand.SaveFlag.fromValue(0));
        assertEquals(T8804RecordingStartCommand.SaveFlag.SAVE, 
                    T8804RecordingStartCommand.SaveFlag.fromValue(1));
    }
    
    @Test
    void testAudioSampleRateEnum() {
        assertEquals(0, T8804RecordingStartCommand.AudioSampleRate.RATE_8K.getValue());
        assertEquals(1, T8804RecordingStartCommand.AudioSampleRate.RATE_11K.getValue());
        assertEquals(2, T8804RecordingStartCommand.AudioSampleRate.RATE_23K.getValue());
        assertEquals(3, T8804RecordingStartCommand.AudioSampleRate.RATE_32K.getValue());
        
        assertEquals("8K", T8804RecordingStartCommand.AudioSampleRate.RATE_8K.getDescription());
        assertEquals("11K", T8804RecordingStartCommand.AudioSampleRate.RATE_11K.getDescription());
        assertEquals("23K", T8804RecordingStartCommand.AudioSampleRate.RATE_23K.getDescription());
        assertEquals("32K", T8804RecordingStartCommand.AudioSampleRate.RATE_32K.getDescription());
        
        assertEquals(T8804RecordingStartCommand.AudioSampleRate.RATE_8K, 
                    T8804RecordingStartCommand.AudioSampleRate.fromValue(0));
        assertEquals(T8804RecordingStartCommand.AudioSampleRate.RATE_11K, 
                    T8804RecordingStartCommand.AudioSampleRate.fromValue(1));
        assertEquals(T8804RecordingStartCommand.AudioSampleRate.RATE_23K, 
                    T8804RecordingStartCommand.AudioSampleRate.fromValue(2));
        assertEquals(T8804RecordingStartCommand.AudioSampleRate.RATE_32K, 
                    T8804RecordingStartCommand.AudioSampleRate.fromValue(3));
    }
    
    @Test
    void testSetters() {
        T8804RecordingStartCommand message = new T8804RecordingStartCommand();
        
        message.setRecordingCommand(T8804RecordingStartCommand.RecordingCommand.START);
        message.setRecordingTime(1800); // 30分钟
        message.setSaveFlag(T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD);
        message.setAudioSampleRate(T8804RecordingStartCommand.AudioSampleRate.RATE_23K);
        
        assertEquals(T8804RecordingStartCommand.RecordingCommand.START, message.getRecordingCommand());
        assertEquals(1800, message.getRecordingTime());
        assertEquals(T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD, message.getSaveFlag());
        assertEquals(T8804RecordingStartCommand.AudioSampleRate.RATE_23K, message.getAudioSampleRate());
    }
    
    @Test
    void testToString() {
        T8804RecordingStartCommand message = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            300,
            T8804RecordingStartCommand.SaveFlag.SAVE,
            T8804RecordingStartCommand.AudioSampleRate.RATE_8K
        );
        
        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("T8804RecordingStartCommand"));
        assertTrue(result.contains("START"));
        assertTrue(result.contains("recordingTime=300"));
        assertTrue(result.contains("SAVE"));
        assertTrue(result.contains("RATE_8K"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        T8804RecordingStartCommand message1 = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            300,
            T8804RecordingStartCommand.SaveFlag.SAVE,
            T8804RecordingStartCommand.AudioSampleRate.RATE_8K
        );
        
        T8804RecordingStartCommand message2 = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            300,
            T8804RecordingStartCommand.SaveFlag.SAVE,
            T8804RecordingStartCommand.AudioSampleRate.RATE_8K
        );
        
        T8804RecordingStartCommand message3 = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.STOP,
            0,
            T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD,
            T8804RecordingStartCommand.AudioSampleRate.RATE_32K
        );
        
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertEquals(message1.hashCode(), message2.hashCode());
        assertNotEquals(message1.hashCode(), message3.hashCode());
    }
    
    @Test
    void testInvalidBufferLength() {
        Buffer shortBuffer = Buffer.buffer(new byte[3]); // 少于5字节
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8804RecordingStartCommand.decode(shortBuffer);
        });
    }
    
    @Test
    void testInvalidEnumValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            T8804RecordingStartCommand.RecordingCommand.fromValue(99);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8804RecordingStartCommand.SaveFlag.fromValue(99);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8804RecordingStartCommand.AudioSampleRate.fromValue(99);
        });
    }
    
    @Test
    void testFactoryCreation() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(MessageTypes.Platform.RECORDING_START_COMMAND);
        
        assertNotNull(message);
        assertInstanceOf(T8804RecordingStartCommand.class, message);
        assertEquals(MessageTypes.Platform.RECORDING_START_COMMAND, message.getMessageId());
    }
    
    @Test
    void testContinuousRecording() {
        // 测试一直录音的情况（录音时间为0）
        T8804RecordingStartCommand message = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            0, // 0表示一直录音
            T8804RecordingStartCommand.SaveFlag.REAL_TIME_UPLOAD,
            T8804RecordingStartCommand.AudioSampleRate.RATE_8K
        );
        
        Buffer encoded = message.encode();
        T8804RecordingStartCommand decoded = T8804RecordingStartCommand.decode(encoded);
        
        assertEquals(0, decoded.getRecordingTime());
        assertEquals(T8804RecordingStartCommand.RecordingCommand.START, decoded.getRecordingCommand());
    }
    
    @Test
    void testMaxRecordingTime() {
        // 测试最大录音时间（WORD最大值65535）
        T8804RecordingStartCommand message = new T8804RecordingStartCommand(
            T8804RecordingStartCommand.RecordingCommand.START,
            65535,
            T8804RecordingStartCommand.SaveFlag.SAVE,
            T8804RecordingStartCommand.AudioSampleRate.RATE_32K
        );
        
        Buffer encoded = message.encode();
        T8804RecordingStartCommand decoded = T8804RecordingStartCommand.decode(encoded);
        
        assertEquals(65535, decoded.getRecordingTime());
    }
    
    @Test
    void testAllSampleRates() {
        // 测试所有采样率
        T8804RecordingStartCommand.AudioSampleRate[] rates = {
            T8804RecordingStartCommand.AudioSampleRate.RATE_8K,
            T8804RecordingStartCommand.AudioSampleRate.RATE_11K,
            T8804RecordingStartCommand.AudioSampleRate.RATE_23K,
            T8804RecordingStartCommand.AudioSampleRate.RATE_32K
        };
        
        for (T8804RecordingStartCommand.AudioSampleRate rate : rates) {
            T8804RecordingStartCommand message = new T8804RecordingStartCommand(
                T8804RecordingStartCommand.RecordingCommand.START,
                300,
                T8804RecordingStartCommand.SaveFlag.SAVE,
                rate
            );
            
            Buffer encoded = message.encode();
            T8804RecordingStartCommand decoded = T8804RecordingStartCommand.decode(encoded);
            
            assertEquals(rate, decoded.getAudioSampleRate());
        }
    }
}