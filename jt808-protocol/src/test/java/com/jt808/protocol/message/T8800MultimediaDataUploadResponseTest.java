package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8800 多媒体数据上传应答消息测试类
 * 
 * @author JT808 Protocol
 * @version 1.0
 */
class T8800MultimediaDataUploadResponseTest {

    @Test
    void testMessageId() {
        T8800MultimediaDataUploadResponse message = new T8800MultimediaDataUploadResponse();
        assertEquals(MessageTypes.Platform.MULTIMEDIA_DATA_UPLOAD_RESPONSE, message.getMessageId());
    }

    @Test
    void testCompleteResponse() {
        // 测试完整接收应答（无重传包）
        long multimediaId = 0x12345678L;
        T8800MultimediaDataUploadResponse message = T8800MultimediaDataUploadResponse.createCompleteResponse(multimediaId);
        
        assertEquals(multimediaId, message.getMultimediaId());
        assertEquals(0, message.getRetransmissionPacketCount());
        assertNotNull(message.getRetransmissionPacketIds());
        assertEquals(0, message.getRetransmissionPacketIds().size());
        
        // 编码测试
        Buffer encoded = message.encodeBody();
        assertEquals(4, encoded.length()); // 只有多媒体ID，4字节
        assertEquals(0x12, encoded.getByte(0));
        assertEquals(0x34, encoded.getByte(1));
        assertEquals(0x56, encoded.getByte(2));
        assertEquals(0x78, encoded.getByte(3));
        
        // 解码测试
        T8800MultimediaDataUploadResponse decoded = new T8800MultimediaDataUploadResponse();
        decoded.decodeBody(encoded);
        assertEquals(multimediaId, decoded.getMultimediaId());
        assertEquals(0, decoded.getRetransmissionPacketCount());
        assertEquals(0, decoded.getRetransmissionPacketIds().size());
    }

    @Test
    void testRetransmissionResponse() {
        // 测试需要重传的应答
        long multimediaId = 0xABCDEF01L;
        int[] retransmissionIds = {1, 3, 5, 7};
        
        T8800MultimediaDataUploadResponse message = T8800MultimediaDataUploadResponse
                .createRetransmissionResponse(multimediaId, retransmissionIds);
        
        assertEquals(multimediaId, message.getMultimediaId());
        assertEquals(4, message.getRetransmissionPacketCount());
        assertEquals(java.util.Arrays.asList(1, 3, 5, 7), message.getRetransmissionPacketIds());
        
        // 编码测试
        Buffer encoded = message.encodeBody();
        assertEquals(13, encoded.length()); // 4字节多媒体ID + 1字节重传包总数 + 8字节重传包ID列表
        
        // 验证多媒体ID
        assertEquals((byte)0xAB, encoded.getByte(0));
        assertEquals((byte)0xCD, encoded.getByte(1));
        assertEquals((byte)0xEF, encoded.getByte(2));
        assertEquals((byte)0x01, encoded.getByte(3));
        
        // 验证重传包总数
        assertEquals(4, encoded.getByte(4));
        
        // 验证重传包ID列表
        assertEquals(1, encoded.getShort(5));
        assertEquals(3, encoded.getShort(7));
        assertEquals(5, encoded.getShort(9));
        assertEquals(7, encoded.getShort(11));
        
        // 解码测试
        T8800MultimediaDataUploadResponse decoded = new T8800MultimediaDataUploadResponse();
        decoded.decodeBody(encoded);
        assertEquals(multimediaId, decoded.getMultimediaId());
        assertEquals(4, decoded.getRetransmissionPacketCount());
        assertEquals(java.util.Arrays.asList(1, 3, 5, 7), decoded.getRetransmissionPacketIds());
    }

    @Test
    void testEmptyRetransmissionList() {
        // 测试空重传列表
        long multimediaId = 0x11223344L;
        int[] emptyIds = {};
        
        T8800MultimediaDataUploadResponse message = T8800MultimediaDataUploadResponse
                .createRetransmissionResponse(multimediaId, emptyIds);
        
        assertEquals(multimediaId, message.getMultimediaId());
        assertEquals(0, message.getRetransmissionPacketCount());
        assertNotNull(message.getRetransmissionPacketIds());
        assertEquals(0, message.getRetransmissionPacketIds().size());
        
        // 编码测试
        Buffer encoded = message.encodeBody();
        assertEquals(4, encoded.length()); // 4字节多媒体ID，无重传包时没有重传包总数字段
        
        // 解码测试
        T8800MultimediaDataUploadResponse decoded = new T8800MultimediaDataUploadResponse();
        decoded.decodeBody(encoded);
        assertEquals(multimediaId, decoded.getMultimediaId());
        assertEquals(0, decoded.getRetransmissionPacketCount());
        assertEquals(0, decoded.getRetransmissionPacketIds().size());
    }

    @Test
    void testMaxRetransmissionPackets() {
        // 测试最大重传包数量（255个）
        long multimediaId = 0xFFEEDDCCL;
        int[] maxIds = new int[255];
        for (int i = 0; i < 255; i++) {
            maxIds[i] = i;
        }
        
        T8800MultimediaDataUploadResponse message = T8800MultimediaDataUploadResponse
                .createRetransmissionResponse(multimediaId, maxIds);
        
        assertEquals(multimediaId, message.getMultimediaId());
        assertEquals(255, message.getRetransmissionPacketCount());
        assertEquals(255, message.getRetransmissionPacketIds().size());
        
        // 编码测试
        Buffer encoded = message.encodeBody();
        assertEquals(515, encoded.length()); // 4字节多媒体ID + 1字节重传包总数 + 510字节重传包ID列表
        
        // 解码测试
        T8800MultimediaDataUploadResponse decoded = new T8800MultimediaDataUploadResponse();
        decoded.decodeBody(encoded);
        assertEquals(multimediaId, decoded.getMultimediaId());
        assertEquals(255, decoded.getRetransmissionPacketCount());
        assertEquals(255, decoded.getRetransmissionPacketIds().size());
    }

    @Test
    void testBoundaryValues() {
        // 测试边界值
        long maxMultimediaId = 0xFFFFFFFFL;
        int[] boundaryIds = {0, 1, 254, 255, 65534, 65535};
        
        T8800MultimediaDataUploadResponse message = T8800MultimediaDataUploadResponse
                .createRetransmissionResponse(maxMultimediaId, boundaryIds);
        
        assertEquals(maxMultimediaId, message.getMultimediaId());
        assertEquals(6, message.getRetransmissionPacketCount());
        assertEquals(java.util.Arrays.asList(0, 1, 254, 255, 65534, 65535), message.getRetransmissionPacketIds());
        
        // 编码解码测试
        Buffer encoded = message.encodeBody();
        T8800MultimediaDataUploadResponse decoded = new T8800MultimediaDataUploadResponse();
        decoded.decodeBody(encoded);
        
        assertEquals(maxMultimediaId, decoded.getMultimediaId());
        assertEquals(6, decoded.getRetransmissionPacketCount());
        assertEquals(java.util.Arrays.asList(0, 1, 254, 255, 65534, 65535), decoded.getRetransmissionPacketIds());
    }

    @Test
    void testSettersAndGetters() {
        T8800MultimediaDataUploadResponse message = new T8800MultimediaDataUploadResponse();
        
        // 测试设置和获取多媒体ID
        long testId = 0x87654321L;
        message.setMultimediaId(testId);
        assertEquals(testId, message.getMultimediaId());
        
        // 测试设置和获取重传包总数
        int testCount = 5;
        message.setRetransmissionPacketCount(testCount);
        assertEquals(testCount, message.getRetransmissionPacketCount());
        
        // 测试设置和获取重传包ID列表
        java.util.List<Integer> testIds = java.util.Arrays.asList(10, 20, 30);
        message.setRetransmissionPacketIds(testIds);
        assertEquals(testIds, message.getRetransmissionPacketIds());
        
        // 测试辅助方法
        assertTrue(message.needsRetransmission());
        assertEquals(testId, message.getMultimediaIdUnsigned());
        assertEquals(3, message.getRetransmissionPacketCountUnsigned());
        assertEquals(3, message.getRetransmissionPacketIds().size());
    }

    @Test
    void testToString() {
        T8800MultimediaDataUploadResponse message = T8800MultimediaDataUploadResponse
                .createRetransmissionResponse(0x12345678L, 1, 2, 3);
        
        String result = message.toString();
        assertNotNull(result);
        assertTrue(result.contains("T8800MultimediaDataUploadResponse"));
        assertTrue(result.contains("multimediaId=305419896"));
        assertTrue(result.contains("retransmissionPacketCount=3"));
        assertTrue(result.contains("retransmissionPacketIds=1, 2, 3"));
    }

    @Test
    void testInvalidDecoding() {
        T8800MultimediaDataUploadResponse message = new T8800MultimediaDataUploadResponse();
        
        // 测试空缓冲区
        Buffer emptyBuffer = Buffer.buffer();
        assertThrows(Exception.class, () -> message.decodeBody(emptyBuffer));
        
        // 测试不完整的缓冲区（只有3字节，少于最小的4字节多媒体ID）
        Buffer incompleteBuffer = Buffer.buffer().appendBytes(new byte[]{0x01, 0x02, 0x03});
        assertThrows(Exception.class, () -> message.decodeBody(incompleteBuffer));
        
        // 测试重传包数量与实际数据不匹配
        Buffer mismatchBuffer = Buffer.buffer()
                .appendInt(0x12345678) // 多媒体ID
                .appendByte((byte) 2)  // 声明有2个重传包
                .appendShort((short) 1); // 但只提供1个重传包ID
        assertThrows(Exception.class, () -> message.decodeBody(mismatchBuffer));
    }
}