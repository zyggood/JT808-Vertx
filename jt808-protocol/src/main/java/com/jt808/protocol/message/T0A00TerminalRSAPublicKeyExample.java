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
 * T0A00终端RSA公钥消息示例程序
 * 
 * 演示如何使用T0A00TerminalRSAPublicKey消息类进行RSA公钥的创建、编码、解码和验证
 * 
 * @author JT808-Vertx
 * @since 1.0.0
 */
public class T0A00TerminalRSAPublicKeyExample {
    
    public static void main(String[] args) {
        System.out.println("=== T0A00终端RSA公钥消息示例 ===");
        
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
            
            // 示例6：与平台RSA公钥消息的互操作性
            demonstrateInteroperability();
            
            // 示例7：BigInteger操作
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
            modulus[i] = (byte) (0x34 + (i % 16));
        }
        
        T0A00TerminalRSAPublicKey message = new T0A00TerminalRSAPublicKey(exponent, modulus);
        
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
            T0A00TerminalRSAPublicKey message = new T0A00TerminalRSAPublicKey(
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
        Arrays.fill(originalModulus, (byte) 0xCD);
        
        T0A00TerminalRSAPublicKey originalMessage = new T0A00TerminalRSAPublicKey(originalExponent, originalModulus);
        
        System.out.println("原始消息: " + originalMessage);
        
        // 编码消息
        Buffer encoded = originalMessage.encodeBody();
        System.out.println("编码后长度: " + encoded.length() + " 字节");
        System.out.println("编码数据(前16字节): " + bytesToHex(encoded.getBytes(0, Math.min(16, encoded.length()))));
        
        // 解码消息
        T0A00TerminalRSAPublicKey decodedMessage = new T0A00TerminalRSAPublicKey();
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
        T0A00TerminalRSAPublicKey validMessage = new T0A00TerminalRSAPublicKey();
        validMessage.setPublicExponent(65537L);
        byte[] validModulus = new byte[128];
        Arrays.fill(validModulus, (byte) 0x01);
        validMessage.setModulus(validModulus);
        
        System.out.println("有效RSA密钥验证: " + validMessage.isValidRSAKey());
        
        // 测试无效的RSA密钥（全零模数）
        T0A00TerminalRSAPublicKey invalidMessage = new T0A00TerminalRSAPublicKey();
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
        
        // 测试模数长度处理
        byte[] shortModulus = new byte[64];
        Arrays.fill(shortModulus, (byte) 0xFF);
        validMessage.setModulus(shortModulus);
        System.out.println("短模数处理后长度: " + validMessage.getModulus().length);
        
        byte[] longModulus = new byte[256];
        Arrays.fill(longModulus, (byte) 0xAA);
        validMessage.setModulus(longModulus);
        System.out.println("长模数处理后长度: " + validMessage.getModulus().length);
    }
    
    /**
     * 演示与工厂模式的集成
     */
    private static void demonstrateFactoryIntegration() {
        System.out.println("\n--- 示例5：工厂模式集成 ---");
        
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        
        // 检查消息是否被支持
        boolean isSupported = factory.isSupported(MessageTypes.Terminal.TERMINAL_RSA_PUBLIC_KEY);
        System.out.println("工厂支持T0A00消息: " + isSupported);
        
        if (isSupported) {
            // 通过工厂创建消息
            JT808Message message = factory.createMessage(MessageTypes.Terminal.TERMINAL_RSA_PUBLIC_KEY);
            System.out.println("工厂创建的消息类型: " + message.getClass().getSimpleName());
            System.out.println("消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
            
            // 验证是否为正确的类型
            if (message instanceof T0A00TerminalRSAPublicKey) {
                System.out.println("✓ 工厂创建了正确的消息类型");
                
                T0A00TerminalRSAPublicKey rsaMessage = (T0A00TerminalRSAPublicKey) message;
                rsaMessage.setPublicExponent(65537L);
                
                byte[] modulus = new byte[128];
                Arrays.fill(modulus, (byte) 0xEE);
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
     * 演示与平台RSA公钥消息的互操作性
     */
    private static void demonstrateInteroperability() {
        System.out.println("\n--- 示例6：与平台RSA公钥消息的互操作性 ---");
        
        // 创建相同参数的终端和平台RSA公钥消息
        long exponent = 65537L;
        byte[] modulus = new byte[128];
        Arrays.fill(modulus, (byte) 0x42);
        
        T0A00TerminalRSAPublicKey terminalKey = new T0A00TerminalRSAPublicKey(exponent, modulus);
        T8A00PlatformRSAPublicKey platformKey = new T8A00PlatformRSAPublicKey(exponent, modulus);
        
        System.out.println("终端RSA公钥消息ID: 0x" + Integer.toHexString(terminalKey.getMessageId()).toUpperCase());
        System.out.println("平台RSA公钥消息ID: 0x" + Integer.toHexString(platformKey.getMessageId()).toUpperCase());
        
        // 验证RSA参数的一致性
        boolean exponentMatch = terminalKey.getPublicExponent() == platformKey.getPublicExponent();
        boolean modulusMatch = Arrays.equals(terminalKey.getModulus(), platformKey.getModulus());
        boolean bigIntegerExponentMatch = terminalKey.getPublicExponentAsBigInteger().equals(platformKey.getPublicExponentAsBigInteger());
        boolean bigIntegerModulusMatch = terminalKey.getModulusAsBigInteger().equals(platformKey.getModulusAsBigInteger());
        
        System.out.println("公钥指数匹配: " + exponentMatch);
        System.out.println("模数匹配: " + modulusMatch);
        System.out.println("BigInteger公钥指数匹配: " + bigIntegerExponentMatch);
        System.out.println("BigInteger模数匹配: " + bigIntegerModulusMatch);
        
        // 验证编码格式的一致性
        Buffer terminalEncoded = terminalKey.encodeBody();
        Buffer platformEncoded = platformKey.encodeBody();
        
        boolean encodingMatch = Arrays.equals(terminalEncoded.getBytes(), platformEncoded.getBytes());
        System.out.println("编码格式匹配: " + encodingMatch);
        System.out.println("编码长度匹配: " + (terminalEncoded.length() == platformEncoded.length()));
        
        if (exponentMatch && modulusMatch && encodingMatch) {
            System.out.println("✓ 终端和平台RSA公钥消息完全兼容");
        } else {
            System.out.println("✗ 终端和平台RSA公钥消息存在不兼容");
        }
        
        // 测试交叉解码
        T0A00TerminalRSAPublicKey decodedTerminal = new T0A00TerminalRSAPublicKey();
        decodedTerminal.decodeBody(platformEncoded);
        
        T8A00PlatformRSAPublicKey decodedPlatform = new T8A00PlatformRSAPublicKey();
        decodedPlatform.decodeBody(terminalEncoded);
        
        boolean crossDecodeMatch = decodedTerminal.getPublicExponent() == decodedPlatform.getPublicExponent() &&
                                  Arrays.equals(decodedTerminal.getModulus(), decodedPlatform.getModulus());
        
        System.out.println("交叉解码兼容性: " + crossDecodeMatch);
    }
    
    /**
     * 演示BigInteger操作
     */
    private static void demonstrateBigIntegerOperations() {
        System.out.println("\n--- 示例7：BigInteger操作 ---");
        
        T0A00TerminalRSAPublicKey message = new T0A00TerminalRSAPublicKey();
        
        // 使用BigInteger设置大数值
        BigInteger largeExponent = new BigInteger("65537");
        BigInteger largeModulus = new BigInteger("987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210");
        
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
        System.out.println("模数后8字节: " + bytesToHex(Arrays.copyOfRange(modulusBytes, 120, 128)));
        
        // 验证BigInteger和字节数组的一致性
        BigInteger reconstructed = new BigInteger(1, modulusBytes);
        System.out.println("重构的BigInteger: " + reconstructed);
        
        // 测试边界情况
        BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
        message.setPublicExponent(maxLong.longValue());
        System.out.println("最大Long值作为指数: " + message.getPublicExponent());
        
        // 测试小数值
        BigInteger smallModulus = new BigInteger("12345");
        message.setModulusFromBigInteger(smallModulus);
        System.out.println("小模数设置后的字节数组长度: " + message.getModulus().length);
        System.out.println("小模数重构: " + message.getModulusAsBigInteger());
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