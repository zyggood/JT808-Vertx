package com.jt808.protocol.message;

import com.jt808.protocol.codec.JT808Decoder;
import com.jt808.protocol.codec.JT808Encoder;
import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;

/**
 * T8107查询终端属性消息使用示例
 * 
 * 该示例演示了如何创建、编码、解码和使用T8107查询终端属性消息。
 * T8107是平台向终端发送的查询终端属性的指令，消息体为空。
 * 
 * @author JT808 Protocol Team
 */
public class T8107QueryTerminalPropertyExample {
    
    public static void main(String[] args) {
        System.out.println("=== T8107查询终端属性消息示例 ===");
        
        try {
            // 1. 使用构造函数创建消息
            demonstrateConstructorCreation();
            
            // 2. 使用工厂创建消息
            demonstrateFactoryCreation();
            
            // 3. 消息编解码
            demonstrateEncodeDecode();
            
            // 4. 完整的消息处理流程
            demonstrateCompleteFlow();
            
            System.out.println("\n=== 所有示例执行成功 ===");
            
        } catch (Exception e) {
            System.err.println("示例执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 演示使用构造函数创建消息
     */
    private static void demonstrateConstructorCreation() {
        System.out.println("\n1. 使用构造函数创建T8107消息:");
        
        T8107QueryTerminalProperty message = new T8107QueryTerminalProperty();
        
        System.out.println("   消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
        System.out.println("   消息类型: " + message.getClass().getSimpleName());
        System.out.println("   消息体长度: " + message.encodeBody().length());
        System.out.println("   消息描述: " + message.toString());
    }
    
    /**
     * 演示使用工厂创建消息
     */
    private static void demonstrateFactoryCreation() {
        System.out.println("\n2. 使用工厂创建T8107消息:");
        
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(0x8107);
        
        System.out.println("   工厂创建的消息类型: " + message.getClass().getSimpleName());
        System.out.println("   消息ID验证: " + (message.getMessageId() == 0x8107 ? "通过" : "失败"));
        System.out.println("   是否为T8107实例: " + (message instanceof T8107QueryTerminalProperty ? "是" : "否"));
    }
    
    /**
     * 演示消息编解码
     */
    private static void demonstrateEncodeDecode() {
        System.out.println("\n3. 消息编解码演示:");
        
        // 创建原始消息
        T8107QueryTerminalProperty original = new T8107QueryTerminalProperty();
        
        // 编码消息体
        Buffer encoded = original.encodeBody();
        System.out.println("   编码后的消息体长度: " + encoded.length() + " 字节");
        System.out.println("   编码后的消息体内容: " + (encoded.length() == 0 ? "空" : "非空"));
        
        // 解码消息体
        T8107QueryTerminalProperty decoded = new T8107QueryTerminalProperty();
        decoded.decodeBody(encoded);
        
        System.out.println("   解码后的消息ID: 0x" + Integer.toHexString(decoded.getMessageId()).toUpperCase());
        System.out.println("   编解码一致性: " + (original.getMessageId() == decoded.getMessageId() ? "通过" : "失败"));
    }
    
    /**
     * 演示完整的消息处理流程
     */
    private static void demonstrateCompleteFlow() {
        System.out.println("\n4. 完整消息处理流程:");
        
        try {
            // 创建消息
            T8107QueryTerminalProperty message = new T8107QueryTerminalProperty();
            
            // 设置消息头
            JT808Header header = new JT808Header();
            header.setMessageId(0x8107);
            header.setPhoneNumber("13800138000");
            header.setSerialNumber(1);
            header.setMessageProperty(0); // 无分包，消息体长度为0
            message.setHeader(header);
            
            // 编码完整消息
            JT808Encoder encoder = new JT808Encoder();
            Buffer encodedMessage = encoder.encode(message);
            
            System.out.println("   完整消息编码长度: " + encodedMessage.length() + " 字节");
            System.out.println("   消息开始标志: 0x" + Integer.toHexString(encodedMessage.getByte(0) & 0xFF).toUpperCase());
            System.out.println("   消息结束标志: 0x" + Integer.toHexString(encodedMessage.getByte(encodedMessage.length() - 1) & 0xFF).toUpperCase());
            
            // 解码完整消息
            JT808Decoder decoder = new JT808Decoder();
            JT808Message decodedMessage = decoder.decode(encodedMessage);
            
            System.out.println("   解码后的消息类型: " + decodedMessage.getClass().getSimpleName());
            System.out.println("   解码后的消息ID: 0x" + Integer.toHexString(decodedMessage.getMessageId()).toUpperCase());
            System.out.println("   解码后的手机号: " + decodedMessage.getHeader().getPhoneNumber());
            System.out.println("   解码后的流水号: " + decodedMessage.getHeader().getSerialNumber());
            
            // 验证类型转换
            if (decodedMessage instanceof T8107QueryTerminalProperty) {
                T8107QueryTerminalProperty t8107 = (T8107QueryTerminalProperty) decodedMessage;
                System.out.println("   类型转换: 成功");
                System.out.println("   消息描述: " + t8107.toString());
            } else {
                System.out.println("   类型转换: 失败");
            }
            
        } catch (Exception e) {
            System.err.println("   完整流程处理失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}