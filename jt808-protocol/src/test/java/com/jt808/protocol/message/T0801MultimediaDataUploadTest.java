package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * T0801多媒体数据上传消息测试类
 */
@DisplayName("T0801多媒体数据上传消息测试")
public class T0801MultimediaDataUploadTest {

    private T0801MultimediaDataUpload message;
    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        message = new T0801MultimediaDataUpload();
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x0801, message.getMessageId());
        assertEquals(MessageTypes.Terminal.MULTIMEDIA_DATA_UPLOAD, message.getMessageId());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        T0801MultimediaDataUpload msg = new T0801MultimediaDataUpload();
        assertEquals(0, msg.getMultimediaId());
        assertEquals(0, msg.getMultimediaType());
        assertEquals(0, msg.getFormatCode());
        assertEquals(0, msg.getEventCode());
        assertEquals(0, msg.getChannelId());
        assertNotNull(msg.getLocationInfo());
        assertNull(msg.getMultimediaData());
    }

    @Test
    @DisplayName("测试带参数构造函数")
    void testParameterizedConstructor() {
        T0200LocationReport locationInfo = new T0200LocationReport();
        Buffer multimediaData = Buffer.buffer("test data");
        
        T0801MultimediaDataUpload msg = new T0801MultimediaDataUpload(
            12345L, 0, 0, 1, 2, locationInfo, multimediaData
        );
        
        assertEquals(12345L, msg.getMultimediaId());
        assertEquals(0, msg.getMultimediaType());
        assertEquals(0, msg.getFormatCode());
        assertEquals(1, msg.getEventCode());
        assertEquals(2, msg.getChannelId());
        assertSame(locationInfo, msg.getLocationInfo());
        assertSame(multimediaData, msg.getMultimediaData());
    }

    @Test
    @DisplayName("测试基本编解码")
    void testEncodeDecodeBasic() {
        // 设置测试数据
        message.setMultimediaId(98765L);
        message.setMultimediaType(0); // 图像
        message.setFormatCode(0); // JPEG
        message.setEventCode(2); // 抢劫报警触发
        message.setChannelId(1);
        
        // 设置位置信息
        T0200LocationReport locationInfo = new T0200LocationReport();
        locationInfo.setAlarmFlag((int)0x00000001L);
        locationInfo.setStatusFlag((int)0x00000002L);
        locationInfo.setLatitude((int)(31.123456 * 1000000));
        locationInfo.setLongitude((int)(121.654321 * 1000000));
        locationInfo.setAltitude(100);
        locationInfo.setSpeed(60);
        locationInfo.setDirection(90);
        locationInfo.setDateTime(LocalDateTime.now());
        message.setLocationInfo(locationInfo);
        
        // 设置多媒体数据
        Buffer testData = Buffer.buffer("test multimedia data");
        message.setMultimediaData(testData);
        
        // 编码
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() >= 36); // 最小长度检查
        
        // 解码
        T0801MultimediaDataUpload decoded = new T0801MultimediaDataUpload();
        decoded.decodeBody(encoded);
        
        // 验证解码结果
        assertEquals(message.getMultimediaId(), decoded.getMultimediaId());
        assertEquals(message.getMultimediaType(), decoded.getMultimediaType());
        assertEquals(message.getFormatCode(), decoded.getFormatCode());
        assertEquals(message.getEventCode(), decoded.getEventCode());
        assertEquals(message.getChannelId(), decoded.getChannelId());
        assertNotNull(decoded.getLocationInfo());
        assertEquals(testData.length(), decoded.getMultimediaDataSize());
    }

    @Test
    @DisplayName("测试编解码一致性")
    void testEncodeDecodeConsistency() {
        // 设置完整的测试数据
        message.setMultimediaId(123456789L);
        message.setMultimediaType(1); // 音频
        message.setFormatCode(3); // WAV
        message.setEventCode(0); // 平台下发指令
        message.setChannelId(5);
        
        // 设置位置信息
        T0200LocationReport locationInfo = new T0200LocationReport();
        locationInfo.setAlarmFlag((int)0x12345678L);
        locationInfo.setStatusFlag((int)0x87654321L);
        locationInfo.setLatitude((int)(39.908722 * 1000000));
        locationInfo.setLongitude((int)(116.397496 * 1000000));
        locationInfo.setAltitude(50);
        locationInfo.setSpeed(80);
        locationInfo.setDirection(180);
        locationInfo.setDateTime(LocalDateTime.now());
        message.setLocationInfo(locationInfo);
        
        // 设置多媒体数据
        byte[] testBytes = new byte[1024];
        for (int i = 0; i < testBytes.length; i++) {
            testBytes[i] = (byte) (i % 256);
        }
        Buffer testData = Buffer.buffer(testBytes);
        message.setMultimediaData(testData);
        
        // 第一次编解码
        Buffer encoded1 = message.encodeBody();
        T0801MultimediaDataUpload decoded1 = new T0801MultimediaDataUpload();
        decoded1.decodeBody(encoded1);
        
        // 第二次编解码
        Buffer encoded2 = decoded1.encodeBody();
        T0801MultimediaDataUpload decoded2 = new T0801MultimediaDataUpload();
        decoded2.decodeBody(encoded2);
        
        // 验证一致性
        assertEquals(decoded1.getMultimediaId(), decoded2.getMultimediaId());
        assertEquals(decoded1.getMultimediaType(), decoded2.getMultimediaType());
        assertEquals(decoded1.getFormatCode(), decoded2.getFormatCode());
        assertEquals(decoded1.getEventCode(), decoded2.getEventCode());
        assertEquals(decoded1.getChannelId(), decoded2.getChannelId());
        assertEquals(decoded1.getMultimediaDataSize(), decoded2.getMultimediaDataSize());
        
        // 验证编码结果一致
        assertArrayEquals(encoded1.getBytes(), encoded2.getBytes());
    }

    @Test
    @DisplayName("测试多媒体类型描述")
    void testMultimediaTypeDescription() {
        message.setMultimediaType(0);
        assertEquals("图像", message.getMultimediaTypeDescription());
        
        message.setMultimediaType(1);
        assertEquals("音频", message.getMultimediaTypeDescription());
        
        message.setMultimediaType(2);
        assertEquals("视频", message.getMultimediaTypeDescription());
        
        message.setMultimediaType(99);
        assertEquals("未知类型(99)", message.getMultimediaTypeDescription());
    }

    @Test
    @DisplayName("测试格式编码描述")
    void testFormatCodeDescription() {
        message.setFormatCode(0);
        assertEquals("JPEG", message.getFormatCodeDescription());
        
        message.setFormatCode(1);
        assertEquals("TIF", message.getFormatCodeDescription());
        
        message.setFormatCode(2);
        assertEquals("MP3", message.getFormatCodeDescription());
        
        message.setFormatCode(3);
        assertEquals("WAV", message.getFormatCodeDescription());
        
        message.setFormatCode(4);
        assertEquals("WMV", message.getFormatCodeDescription());
        
        message.setFormatCode(99);
        assertEquals("保留格式(99)", message.getFormatCodeDescription());
    }

    @Test
    @DisplayName("测试事件项编码描述")
    void testEventCodeDescription() {
        message.setEventCode(0);
        assertEquals("平台下发指令", message.getEventCodeDescription());
        
        message.setEventCode(1);
        assertEquals("定时动作", message.getEventCodeDescription());
        
        message.setEventCode(2);
        assertEquals("抢劫报警触发", message.getEventCodeDescription());
        
        message.setEventCode(3);
        assertEquals("碰撞侧翻报警触发", message.getEventCodeDescription());
        
        message.setEventCode(99);
        assertEquals("保留事件(99)", message.getEventCodeDescription());
    }

    @Test
    @DisplayName("测试多媒体数据大小")
    void testMultimediaDataSize() {
        // 空数据
        assertEquals(0, message.getMultimediaDataSize());
        
        // 设置数据
        Buffer testData = Buffer.buffer("test data");
        message.setMultimediaData(testData);
        assertEquals(testData.length(), message.getMultimediaDataSize());
        
        // 大数据
        Buffer largeData = Buffer.buffer(new byte[10240]);
        message.setMultimediaData(largeData);
        assertEquals(10240, message.getMultimediaDataSize());
    }

    @Test
    @DisplayName("测试解码异常情况")
    void testDecodeBodyExceptions() {
        // 空缓冲区
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });
        
        // 长度不足
        Buffer shortBuffer = Buffer.buffer(new byte[10]);
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(shortBuffer);
        });
        
        // 最小长度边界测试
        Buffer minBuffer = Buffer.buffer(new byte[36]);
        assertDoesNotThrow(() -> {
            message.decodeBody(minBuffer);
        });
    }

    @Test
    @DisplayName("测试工厂创建消息")
    void testFactoryCreateMessage() {
        assertTrue(factory.isSupported(0x0801));
        
        JT808Message created = factory.createMessage(0x0801);
        assertNotNull(created);
        assertInstanceOf(T0801MultimediaDataUpload.class, created);
        assertEquals(0x0801, created.getMessageId());
    }

    @Test
    @DisplayName("测试位置信息为空的情况")
    void testNullLocationInfo() {
        message.setMultimediaId(12345L);
        message.setMultimediaType(0);
        message.setFormatCode(0);
        message.setEventCode(1);
        message.setChannelId(2);
        message.setLocationInfo(null);
        message.setMultimediaData(Buffer.buffer("test"));
        
        // 编码时应该填充28个0字节
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() >= 36);
        
        // 解码
        T0801MultimediaDataUpload decoded = new T0801MultimediaDataUpload();
        decoded.decodeBody(encoded);
        
        assertEquals(message.getMultimediaId(), decoded.getMultimediaId());
        assertEquals(message.getMultimediaType(), decoded.getMultimediaType());
        assertNotNull(decoded.getLocationInfo()); // 解码时会创建新的位置信息对象
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        message.setMultimediaId(12345L);
        message.setMultimediaType(0);
        message.setFormatCode(0);
        message.setEventCode(1);
        message.setChannelId(2);
        message.setMultimediaData(Buffer.buffer("test data"));
        
        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("T0801MultimediaDataUpload"));
        assertTrue(result.contains("multimediaId=12345"));
        assertTrue(result.contains("图像"));
        assertTrue(result.contains("JPEG"));
        assertTrue(result.contains("定时动作"));
        assertTrue(result.contains("channelId=2"));
        assertTrue(result.contains("multimediaDataSize=9"));
    }

    @Test
    @DisplayName("测试边界值")
    void testBoundaryValues() {
        // 测试最大值
        message.setMultimediaId(0xFFFFFFFFL); // DWORD最大值
        message.setMultimediaType(255); // BYTE最大值
        message.setFormatCode(255);
        message.setEventCode(255);
        message.setChannelId(255);
        
        Buffer encoded = message.encodeBody();
        T0801MultimediaDataUpload decoded = new T0801MultimediaDataUpload();
        decoded.decodeBody(encoded);
        
        assertEquals(0xFFFFFFFFL, decoded.getMultimediaId());
        assertEquals(255, decoded.getMultimediaType());
        assertEquals(255, decoded.getFormatCode());
        assertEquals(255, decoded.getEventCode());
        assertEquals(255, decoded.getChannelId());
    }
}