package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8A00平台RSA公钥消息测试类
 * 
 * @author JT808-Vertx
 * @since 1.0.0
 */
class T8A00PlatformRSAPublicKeyTest {
    
    @Test
    void testMessageId() {
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey();
        assertEquals(MessageTypes.Platform.PLATFORM_RSA_PUBLIC_KEY, message.getMessageId());
        assertEquals(0x8A00, message.getMessageId());
    }
    
    @Test
    void testDefaultConstructor() {
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey();
        assertEquals(0L, message.getPublicExponent());
        assertNotNull(message.getModulus());
        assertEquals(128, message.getModulus().length);
        assertFalse(message.isValidRSAKey());
    }
    
    @Test
    void testConstructorWithLongAndByteArray() {
        long exponent = 65537L;
        byte[] modulus = new byte[128];
        Arrays.fill(modulus, (byte) 0x01);
        
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey(exponent, modulus);
        
        assertEquals(exponent, message.getPublicExponent());
        assertArrayEquals(modulus, message.getModulus());
        assertTrue(message.isValidRSAKey());
    }
    
    @Test
    void testConstructorWithBigInteger() {
        BigInteger exponent = BigInteger.valueOf(65537L);
        BigInteger modulus = new BigInteger("123456789012345678901234567890123456789012345678901234567890");
        
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey(exponent, modulus);
        
        assertEquals(65537L, message.getPublicExponent());
        assertEquals(exponent, message.getPublicExponentAsBigInteger());
        assertNotNull(message.getModulus());
        assertEquals(128, message.getModulus().length);
    }
    
    @Test
    void testGettersAndSetters() {
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey();
        
        // 测试公钥指数
        long exponent = 65537L;
        message.setPublicExponent(exponent);
        assertEquals(exponent, message.getPublicExponent());
        assertEquals(BigInteger.valueOf(exponent), message.getPublicExponentAsBigInteger());
        
        // 测试模数
        byte[] modulus = new byte[128];
        Arrays.fill(modulus, (byte) 0xFF);
        message.setModulus(modulus);
        assertArrayEquals(modulus, message.getModulus());
        
        // 测试BigInteger格式的模数
        BigInteger modulusBig = new BigInteger(1, modulus);
        assertEquals(modulusBig, message.getModulusAsBigInteger());
        
        // 测试设置BigInteger格式的模数
        BigInteger newModulus = new BigInteger("987654321098765432109876543210987654321098765432109876543210");
        message.setModulusFromBigInteger(newModulus);
        assertEquals(128, message.getModulus().length);
    }
    
    @Test
    void testEncodeAndDecode() {
        long exponent = 65537L;
        byte[] modulus = new byte[128];
        for (int i = 0; i < 128; i++) {
            modulus[i] = (byte) (i % 256);
        }
        
        T8A00PlatformRSAPublicKey originalMessage = new T8A00PlatformRSAPublicKey(exponent, modulus);
        
        // 编码
        Buffer encoded = originalMessage.encodeBody();
        assertEquals(132, encoded.length()); // 4 + 128 = 132
        
        // 解码
        T8A00PlatformRSAPublicKey decodedMessage = new T8A00PlatformRSAPublicKey();
        decodedMessage.decodeBody(encoded);
        
        // 验证解码结果
        assertEquals(originalMessage.getPublicExponent(), decodedMessage.getPublicExponent());
        assertArrayEquals(originalMessage.getModulus(), decodedMessage.getModulus());
        assertEquals(originalMessage, decodedMessage);
    }
    
    @Test
    void testValidRSAKey() {
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey();
        
        // 默认情况下应该无效
        assertFalse(message.isValidRSAKey());
        
        // 设置有效的公钥指数
        message.setPublicExponent(65537L);
        assertFalse(message.isValidRSAKey()); // 模数仍然全零
        
        // 设置有效的模数
        byte[] modulus = new byte[128];
        Arrays.fill(modulus, (byte) 0x01);
        message.setModulus(modulus);
        assertTrue(message.isValidRSAKey());
        
        // 测试其他有效的公钥指数
        message.setPublicExponent(3L);
        assertTrue(message.isValidRSAKey());
        
        message.setPublicExponent(17L);
        assertTrue(message.isValidRSAKey());
        
        // 测试无效的公钥指数
        message.setPublicExponent(2L);
        assertFalse(message.isValidRSAKey());
    }
    
    @Test
    void testModulusHandling() {
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey();
        
        // 测试null模数
        message.setModulus(null);
        assertEquals(128, message.getModulus().length);
        
        // 测试短模数
        byte[] shortModulus = new byte[64];
        Arrays.fill(shortModulus, (byte) 0xFF);
        message.setModulus(shortModulus);
        assertEquals(128, message.getModulus().length);
        
        // 验证前64字节被复制
        byte[] result = message.getModulus();
        for (int i = 0; i < 64; i++) {
            assertEquals((byte) 0xFF, result[i]);
        }
        for (int i = 64; i < 128; i++) {
            assertEquals((byte) 0x00, result[i]);
        }
        
        // 测试长模数
        byte[] longModulus = new byte[256];
        Arrays.fill(longModulus, (byte) 0xAA);
        message.setModulus(longModulus);
        assertEquals(128, message.getModulus().length);
        
        // 验证只有前128字节被复制
        result = message.getModulus();
        for (int i = 0; i < 128; i++) {
            assertEquals((byte) 0xAA, result[i]);
        }
    }
    
    @Test
    void testToString() {
        long exponent = 65537L;
        byte[] modulus = new byte[128];
        Arrays.fill(modulus, (byte) 0x12);
        
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey(exponent, modulus);
        String result = message.toString();
        
        assertTrue(result.contains("T8A00PlatformRSAPublicKey"));
        assertTrue(result.contains("messageId=0x8A00"));
        assertTrue(result.contains("publicExponent=65537"));
        assertTrue(result.contains("modulus="));
        assertTrue(result.contains("isValid=true"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        long exponent = 65537L;
        byte[] modulus = new byte[128];
        Arrays.fill(modulus, (byte) 0x33);
        
        T8A00PlatformRSAPublicKey message1 = new T8A00PlatformRSAPublicKey(exponent, modulus);
        T8A00PlatformRSAPublicKey message2 = new T8A00PlatformRSAPublicKey(exponent, modulus);
        T8A00PlatformRSAPublicKey message3 = new T8A00PlatformRSAPublicKey(3L, modulus);
        
        // 测试equals
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "string");
        
        // 测试hashCode
        assertEquals(message1.hashCode(), message2.hashCode());
        assertNotEquals(message1.hashCode(), message3.hashCode());
    }
    
    @Test
    void testInvalidDecodeBuffer() {
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey();
        
        // 测试空缓冲区
        Buffer emptyBuffer = Buffer.buffer();
        assertThrows(IllegalArgumentException.class, () -> message.decodeBody(emptyBuffer));
        
        // 测试长度不足的缓冲区
        Buffer shortBuffer = Buffer.buffer(new byte[131]); // 少1字节
        assertThrows(IllegalArgumentException.class, () -> message.decodeBody(shortBuffer));
    }
    
    @Test
    void testFactoryCreation() {
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 测试工厂创建
        JT808Message message = factory.createMessage(MessageTypes.Platform.PLATFORM_RSA_PUBLIC_KEY);
        assertNotNull(message);
        assertInstanceOf(T8A00PlatformRSAPublicKey.class, message);
        assertEquals(0x8A00, message.getMessageId());
        
        // 测试消息是否被支持
        assertTrue(factory.isSupported(MessageTypes.Platform.PLATFORM_RSA_PUBLIC_KEY));
        assertTrue(factory.getSupportedMessageIds().contains(MessageTypes.Platform.PLATFORM_RSA_PUBLIC_KEY));
    }
    
    @Test
    void testBigIntegerConversion() {
        // 测试大数转换
        BigInteger largeModulus = new BigInteger("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey(BigInteger.valueOf(65537), largeModulus);
        
        assertEquals(128, message.getModulus().length);
        assertNotNull(message.getModulusAsBigInteger());
        
        // 测试小数转换
        BigInteger smallModulus = new BigInteger("12345");
        message.setModulusFromBigInteger(smallModulus);
        assertEquals(128, message.getModulus().length);
        
        // 验证转换后的值
        BigInteger converted = message.getModulusAsBigInteger();
        assertNotNull(converted);
    }
    
    @Test
    void testEdgeCases() {
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey();
        
        // 测试最大公钥指数值
        message.setPublicExponent(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, message.getPublicExponent());
        
        // 测试零公钥指数
        message.setPublicExponent(0L);
        assertEquals(0L, message.getPublicExponent());
        
        // 测试全FF模数
        byte[] maxModulus = new byte[128];
        Arrays.fill(maxModulus, (byte) 0xFF);
        message.setModulus(maxModulus);
        assertArrayEquals(maxModulus, message.getModulus());
        
        // 测试编码解码边界情况
        Buffer encoded = message.encodeBody();
        assertEquals(132, encoded.length());
        
        T8A00PlatformRSAPublicKey decoded = new T8A00PlatformRSAPublicKey();
        decoded.decodeBody(encoded);
        assertEquals(message, decoded);
    }
}