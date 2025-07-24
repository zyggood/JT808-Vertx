package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

/**
 * T8A00平台RSA公钥消息示例程序
 * 
 * 演示如何使用T8A00PlatformRSAPublicKey消息类进行RSA公钥的创建、编码、解码和验证
 * 
 * @author JT808-Vertx
 * @since 1.0.0
 */
public class T8A00PlatformRSAPublicKeyExample {
    
    public static void main(String[] args) {
        System.out.println("=== T8A00平台RSA公钥消息示例 ===");
        
        try {
            // 示例1：创建基本的RSA公钥消息
            demonstrateBasicUsage();
            
            // 示例2：使用真实的RSA密钥对
            demonstrateRealRSAKey();
            
            // 示例3：消息编码和解码
            demonstrateEncodingDecoding();
            
            // 示例4：消息验证和错误处理
            demonstrateValidationAndErrorHandling();
            
            // 示例5：与工厂模式集成
            demonstrateFactoryIntegration();
            
            // 示例6：BigInteger操作
            demonstrateBigIntegerOperations();
            
        } catch (Exception e) {
            System.err.println("示例执行过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 演示基本用法
     */
    private static void demonstrateBasicUsage() {
        System.out.println("\n--- 示例1：基本用法 ---");
        
        // 创建RSA公钥消息
        long exponent = 65537L; // 常用的RSA公钥指数
        byte[] modulus = new byte[128];
        
        // 填充模拟的模数数据
        for (int i = 0; i < 128; i++) {
            modulus[i] = (byte) (0x12 + (i % 16));
        }
        
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey(exponent, modulus);
        
        System.out.println("消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
        System.out.println("公钥指数: " + message.getPublicExponent());
        System.out.println("模数长度: " + message.getModulus().length + " 字节");
        System.out.println("RSA密钥有效性: " + message.isValidRSAKey());
        System.out.println("消息详情: " + message.toString());
    }
    
    /**
     * 演示使用真实的RSA密钥对
     */
    private static void demonstrateRealRSAKey() {
        System.out.println("\n--- 示例2：真实RSA密钥对 ---");
        
        try {
            // 生成真实的RSA密钥对
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024); // 使用1024位密钥长度
            KeyPair keyPair = keyGen.generateKeyPair();
            
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            
            // 从真实密钥创建消息
            T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey(
                publicKey.getPublicExponent(),
                publicKey.getModulus()
            );
            
            System.out.println("真实RSA公钥指数: " + message.getPublicExponent());
            System.out.println("真实RSA模数(BigInteger): " + message.getModulusAsBigInteger());
            System.out.println("RSA密钥有效性: " + message.isValidRSAKey());
            
            // 验证密钥参数
            System.out.println("原始公钥指数: " + publicKey.getPublicExponent());
            System.out.println("消息公钥指数: " + message.getPublicExponentAsBigInteger());
            System.out.println("指数匹配: " + publicKey.getPublicExponent().equals(message.getPublicExponentAsBigInteger()));
            
        } catch (Exception e) {
            System.err.println("生成RSA密钥对时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 演示消息编码和解码
     */
    private static void demonstrateEncodingDecoding() {
        System.out.println("\n--- 示例3：消息编码和解码 ---");
        
        // 创建原始消息
        long originalExponent = 65537L;
        byte[] originalModulus = new byte[128];
        Arrays.fill(originalModulus, (byte) 0xAB);
        
        T8A00PlatformRSAPublicKey originalMessage = new T8A00PlatformRSAPublicKey(originalExponent, originalModulus);
        
        System.out.println("原始消息: " + originalMessage);
        
        // 编码消息
        Buffer encoded = originalMessage.encodeBody();
        System.out.println("编码后长度: " + encoded.length() + " 字节");
        System.out.println("编码数据(前16字节): " + bytesToHex(encoded.getBytes(0, Math.min(16, encoded.length()))));
        
        // 解码消息
        T8A00PlatformRSAPublicKey decodedMessage = new T8A00PlatformRSAPublicKey();
        decodedMessage.decodeBody(encoded);
        
        System.out.println("解码后消息: " + decodedMessage);
        
        // 验证编码解码的一致性
        boolean isEqual = originalMessage.equals(decodedMessage);
        System.out.println("编码解码一致性: " + isEqual);
        
        if (isEqual) {
            System.out.println("✓ 编码解码测试通过");
        } else {
            System.out.println("✗ 编码解码测试失败");
        }
    }
    
    /**
     * 演示消息验证和错误处理
     */
    private static void demonstrateValidationAndErrorHandling() {
        System.out.println("\n--- 示例4：消息验证和错误处理 ---");
        
        // 测试有效的RSA密钥
        T8A00PlatformRSAPublicKey validMessage = new T8A00PlatformRSAPublicKey();
        validMessage.setPublicExponent(65537L);
        byte[] validModulus = new byte[128];
        Arrays.fill(validModulus, (byte) 0x01);
        validMessage.setModulus(validModulus);
        
        System.out.println("有效RSA密钥验证: " + validMessage.isValidRSAKey());
        
        // 测试无效的RSA密钥（全零模数）
        T8A00PlatformRSAPublicKey invalidMessage = new T8A00PlatformRSAPublicKey();
        invalidMessage.setPublicExponent(65537L);
        // 模数保持默认的全零状态
        
        System.out.println("无效RSA密钥验证: " + invalidMessage.isValidRSAKey());
        
        // 测试无效的公钥指数
        invalidMessage.setPublicExponent(2L); // 2不是有效的RSA公钥指数
        System.out.println("无效公钥指数验证: " + invalidMessage.isValidRSAKey());
        
        // 测试解码错误处理
        try {
            Buffer invalidBuffer = Buffer.buffer(new byte[100]); // 长度不足
            invalidMessage.decodeBody(invalidBuffer);
            System.out.println("✗ 应该抛出异常但没有");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ 正确捕获解码异常: " + e.getMessage());
        }
    }
    
    /**
     * 演示与工厂模式的集成
     */
    private static void demonstrateFactoryIntegration() {
        System.out.println("\n--- 示例5：工厂模式集成 ---");
        
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 检查消息是否被支持
        boolean isSupported = factory.isSupported(MessageTypes.Platform.PLATFORM_RSA_PUBLIC_KEY);
        System.out.println("工厂支持T8A00消息: " + isSupported);
        
        if (isSupported) {
            // 通过工厂创建消息
            JT808Message message = factory.createMessage(MessageTypes.Platform.PLATFORM_RSA_PUBLIC_KEY);
            System.out.println("工厂创建的消息类型: " + message.getClass().getSimpleName());
            System.out.println("消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
            
            // 验证是否为正确的类型
            if (message instanceof T8A00PlatformRSAPublicKey) {
                System.out.println("✓ 工厂创建了正确的消息类型");
                
                T8A00PlatformRSAPublicKey rsaMessage = (T8A00PlatformRSAPublicKey) message;
                rsaMessage.setPublicExponent(65537L);
                
                byte[] modulus = new byte[128];
                Arrays.fill(modulus, (byte) 0xCC);
                rsaMessage.setModulus(modulus);
                
                System.out.println("设置参数后的消息: " + rsaMessage);
            } else {
                System.out.println("✗ 工厂创建了错误的消息类型");
            }
        }
        
        // 显示所有支持的消息ID
        System.out.println("工厂支持的消息ID数量: " + factory.getSupportedMessageIds().size());
    }
    
    /**
     * 演示BigInteger操作
     */
    private static void demonstrateBigIntegerOperations() {
        System.out.println("\n--- 示例6：BigInteger操作 ---");
        
        T8A00PlatformRSAPublicKey message = new T8A00PlatformRSAPublicKey();
        
        // 使用BigInteger设置大数值
        BigInteger largeExponent = new BigInteger("65537");
        BigInteger largeModulus = new BigInteger("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        
        message.setPublicExponent(largeExponent.longValue());
        message.setModulusFromBigInteger(largeModulus);
        
        System.out.println("设置的公钥指数(BigInteger): " + largeExponent);
        System.out.println("获取的公钥指数(BigInteger): " + message.getPublicExponentAsBigInteger());
        System.out.println("指数匹配: " + largeExponent.equals(message.getPublicExponentAsBigInteger()));
        
        System.out.println("设置的模数(BigInteger): " + largeModulus);
        System.out.println("获取的模数(BigInteger): " + message.getModulusAsBigInteger());
        
        // 测试模数的字节数组转换
        byte[] modulusBytes = message.getModulus();
        System.out.println("模数字节数组长度: " + modulusBytes.length);
        System.out.println("模数前8字节: " + bytesToHex(Arrays.copyOf(modulusBytes, 8)));
        
        // 验证BigInteger和字节数组的一致性
        BigInteger reconstructed = new BigInteger(1, modulusBytes);
        System.out.println("重构的BigInteger: " + reconstructed);
    }
    
    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString().trim();
    }
}