package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8805单条存储多媒体数据检索上传命令测试类
 */
class T8805SingleMultimediaDataRetrievalUploadCommandTest {
    
    @Test
    void testMessageId() {
        T8805SingleMultimediaDataRetrievalUploadCommand command = new T8805SingleMultimediaDataRetrievalUploadCommand();
        assertEquals(MessageTypes.Platform.SINGLE_MULTIMEDIA_DATA_RETRIEVAL_UPLOAD_COMMAND, command.getMessageId());
        assertEquals(0x8805, command.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        T8805SingleMultimediaDataRetrievalUploadCommand command = new T8805SingleMultimediaDataRetrievalUploadCommand();
        assertEquals(0L, command.getMultimediaId());
        assertEquals(T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP, command.getDeleteFlag());
    }
    
    @Test
    void testParameterizedConstructor() {
        T8805SingleMultimediaDataRetrievalUploadCommand command = new T8805SingleMultimediaDataRetrievalUploadCommand(
            12345L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE);
        
        assertEquals(12345L, command.getMultimediaId());
        assertEquals(T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE, command.getDeleteFlag());
    }
    
    @Test
    void testInvalidMultimediaId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new T8805SingleMultimediaDataRetrievalUploadCommand(0L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new T8805SingleMultimediaDataRetrievalUploadCommand(-1L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP);
        });
    }
    
    @Test
    void testGettersAndSetters() {
        T8805SingleMultimediaDataRetrievalUploadCommand command = new T8805SingleMultimediaDataRetrievalUploadCommand();
        
        // 测试多媒体ID
        command.setMultimediaId(98765L);
        assertEquals(98765L, command.getMultimediaId());
        
        // 测试删除标志
        command.setDeleteFlag(T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE);
        assertEquals(T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE, command.getDeleteFlag());
        
        // 测试设置null删除标志
        command.setDeleteFlag(null);
        assertEquals(T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP, command.getDeleteFlag());
    }
    
    @Test
    void testSetInvalidMultimediaId() {
        T8805SingleMultimediaDataRetrievalUploadCommand command = new T8805SingleMultimediaDataRetrievalUploadCommand(
            1L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP);
        
        assertThrows(IllegalArgumentException.class, () -> {
            command.setMultimediaId(0L);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            command.setMultimediaId(-100L);
        });
    }
    
    @Test
    void testEncodeAndDecode() {
        T8805SingleMultimediaDataRetrievalUploadCommand original = new T8805SingleMultimediaDataRetrievalUploadCommand(
            0x12345678L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE);
        
        // 编码
        Buffer encoded = original.encode();
        assertEquals(5, encoded.length()); // 4字节多媒体ID + 1字节删除标志
        
        // 验证编码内容
        assertEquals(0x12345678L, encoded.getUnsignedInt(0)); // 多媒体ID
        assertEquals(1, encoded.getByte(4) & 0xFF); // 删除标志
        
        // 解码
        T8805SingleMultimediaDataRetrievalUploadCommand decoded = T8805SingleMultimediaDataRetrievalUploadCommand.decode(encoded);
        
        assertEquals(original.getMultimediaId(), decoded.getMultimediaId());
        assertEquals(original.getDeleteFlag(), decoded.getDeleteFlag());
        assertEquals(original, decoded);
    }
    
    @Test
    void testDecodeInvalidBuffer() {
        // 测试缓冲区长度不足
        Buffer shortBuffer = Buffer.buffer(new byte[]{0x01, 0x02, 0x03}); // 只有3字节
        assertThrows(IllegalArgumentException.class, () -> {
            T8805SingleMultimediaDataRetrievalUploadCommand.decode(shortBuffer);
        });
        
        // 测试多媒体ID为0的情况
        Buffer zeroIdBuffer = Buffer.buffer();
        zeroIdBuffer.appendUnsignedInt(0L); // 多媒体ID为0
        zeroIdBuffer.appendByte((byte) 0); // 删除标志
        
        assertThrows(IllegalArgumentException.class, () -> {
            T8805SingleMultimediaDataRetrievalUploadCommand.decode(zeroIdBuffer);
        });
    }
    
    @Test
    void testDeleteFlagEnum() {
        // 测试枚举值
        assertEquals(0, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP.getValue());
        assertEquals(1, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE.getValue());
        
        assertEquals("保留", T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP.getDescription());
        assertEquals("删除", T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE.getDescription());
        
        // 测试fromValue方法
        assertEquals(T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP, 
                    T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.fromValue(0));
        assertEquals(T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE, 
                    T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.fromValue(1));
        
        // 测试无效值
        assertThrows(IllegalArgumentException.class, () -> {
            T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.fromValue(99);
        });
    }
    
    @Test
    void testToString() {
        T8805SingleMultimediaDataRetrievalUploadCommand command = new T8805SingleMultimediaDataRetrievalUploadCommand(
            0xABCDEF12L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE);
        
        String str = command.toString();
        assertTrue(str.contains("T8805SingleMultimediaDataRetrievalUploadCommand"));
        assertTrue(str.contains("multimediaId=" + 0xABCDEF12L));
        assertTrue(str.contains("deleteFlag=删除"));
        assertTrue(str.contains("messageId=0x8805"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        T8805SingleMultimediaDataRetrievalUploadCommand command1 = new T8805SingleMultimediaDataRetrievalUploadCommand(
            12345L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP);
        T8805SingleMultimediaDataRetrievalUploadCommand command2 = new T8805SingleMultimediaDataRetrievalUploadCommand(
            12345L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP);
        T8805SingleMultimediaDataRetrievalUploadCommand command3 = new T8805SingleMultimediaDataRetrievalUploadCommand(
            54321L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE);
        
        // 测试equals
        assertEquals(command1, command2);
        assertNotEquals(command1, command3);
        assertNotEquals(command1, null);
        assertNotEquals(command1, "not a command");
        
        // 测试hashCode
        assertEquals(command1.hashCode(), command2.hashCode());
        assertNotEquals(command1.hashCode(), command3.hashCode());
    }
    
    @Test
    void testFactoryCreation() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(MessageTypes.Platform.SINGLE_MULTIMEDIA_DATA_RETRIEVAL_UPLOAD_COMMAND);
        
        assertNotNull(message);
        assertInstanceOf(T8805SingleMultimediaDataRetrievalUploadCommand.class, message);
        assertEquals(0x8805, message.getMessageId());
    }
    
    @Test
    void testMaxMultimediaId() {
        // 测试最大多媒体ID值 (DWORD最大值)
        long maxId = 0xFFFFFFFFL;
        T8805SingleMultimediaDataRetrievalUploadCommand command = new T8805SingleMultimediaDataRetrievalUploadCommand(
            maxId, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP);
        
        assertEquals(maxId, command.getMultimediaId());
        
        // 编码解码测试
        Buffer encoded = command.encode();
        T8805SingleMultimediaDataRetrievalUploadCommand decoded = T8805SingleMultimediaDataRetrievalUploadCommand.decode(encoded);
        assertEquals(maxId, decoded.getMultimediaId());
    }
    
    @Test
    void testDifferentDeleteFlags() {
        // 测试保留标志
        T8805SingleMultimediaDataRetrievalUploadCommand keepCommand = new T8805SingleMultimediaDataRetrievalUploadCommand(
            1000L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP);
        
        Buffer keepEncoded = keepCommand.encode();
        assertEquals(0, keepEncoded.getByte(4) & 0xFF);
        
        T8805SingleMultimediaDataRetrievalUploadCommand keepDecoded = T8805SingleMultimediaDataRetrievalUploadCommand.decode(keepEncoded);
        assertEquals(T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.KEEP, keepDecoded.getDeleteFlag());
        
        // 测试删除标志
        T8805SingleMultimediaDataRetrievalUploadCommand deleteCommand = new T8805SingleMultimediaDataRetrievalUploadCommand(
            2000L, T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE);
        
        Buffer deleteEncoded = deleteCommand.encode();
        assertEquals(1, deleteEncoded.getByte(4) & 0xFF);
        
        T8805SingleMultimediaDataRetrievalUploadCommand deleteDecoded = T8805SingleMultimediaDataRetrievalUploadCommand.decode(deleteEncoded);
        assertEquals(T8805SingleMultimediaDataRetrievalUploadCommand.DeleteFlag.DELETE, deleteDecoded.getDeleteFlag());
    }
}