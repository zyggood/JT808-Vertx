package com.jt808.protocol.example;

import com.jt808.protocol.codec.JT808Decoder;
import com.jt808.protocol.codec.JT808Encoder;
import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T0107QueryTerminalPropertyResponse;
import io.vertx.core.buffer.Buffer;

/**
 * T0107 查询终端属性应答消息示例
 * 消息ID: 0x0107
 * 功能: 终端响应平台的查询终端属性请求，返回终端的详细属性信息
 */
public class T0107QueryTerminalPropertyResponseExample {
    
    public static void main(String[] args) {
        System.out.println("=== T0107 查询终端属性应答消息示例 ===");
        
        // 示例1: 使用构造函数创建消息
        System.out.println("\n1. 使用构造函数创建消息:");
        T0107QueryTerminalPropertyResponse message1 = new T0107QueryTerminalPropertyResponse(
            0x1234,                    // 终端类型
            "ABCDE",                   // 制造商ID
            "Model-2024-GPS",          // 终端型号
            "1234567",                 // 终端ID
            "12345678901234567890",    // SIM卡ICCID
            "HW-V1.0",                 // 硬件版本号
            "FW-V2.1",                 // 固件版本号
            0x01,                      // GNSS模块属性
            0x02                       // 通信模块属性
        );
        System.out.println("消息ID: 0x" + Integer.toHexString(message1.getMessageId()).toUpperCase());
        System.out.println("消息内容: " + message1.toString());
        
        // 示例2: 使用工厂创建消息
        System.out.println("\n2. 使用工厂创建消息:");
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message2 = factory.createMessage(0x0107);
        System.out.println("工厂创建的消息类型: " + message2.getClass().getSimpleName());
        
        // 示例3: 消息体编解码
        System.out.println("\n3. 消息体编解码测试:");
        
        // 编码消息体
        Buffer encodedBody = message1.encodeBody();
        System.out.println("编码后的消息体长度: " + encodedBody.length() + " 字节");
        System.out.println("编码后的消息体(十六进制): " + bytesToHex(encodedBody.getBytes()));
        
        // 解码消息体
        T0107QueryTerminalPropertyResponse decodedMessage = new T0107QueryTerminalPropertyResponse();
        decodedMessage.decodeBody(encodedBody);
        System.out.println("解码后的消息: " + decodedMessage.toString());
        
        // 验证编解码一致性
        boolean isConsistent = message1.toString().equals(decodedMessage.toString());
        System.out.println("编解码一致性验证: " + (isConsistent ? "通过" : "失败"));
        
        // 示例4: 完整消息处理流程
        System.out.println("\n4. 完整消息处理流程:");
        
        JT808Encoder encoder = new JT808Encoder();
        JT808Decoder decoder = new JT808Decoder();
        
        try {
            // 为消息设置消息头
            JT808Header header = new JT808Header(message1.getMessageId(), "123456789012", 1);
            message1.setHeader(header);
            
            // 编码完整消息
            Buffer fullMessage = encoder.encode(message1);
            System.out.println("完整消息长度: " + fullMessage.length() + " 字节");
            System.out.println("完整消息(十六进制): " + bytesToHex(fullMessage.getBytes()));
            
            // 解码完整消息
            JT808Message decodedFullMessage = decoder.decode(fullMessage);
            System.out.println("解码后的消息类型: " + decodedFullMessage.getClass().getSimpleName());
            System.out.println("解码后的消息内容: " + decodedFullMessage.toString());
            
            System.out.println("\n完整消息处理流程验证: 成功");
            
        } catch (Exception e) {
            System.err.println("完整消息处理流程验证失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 示例5: 最小消息测试
        System.out.println("\n5. 最小消息测试:");
        T0107QueryTerminalPropertyResponse minMessage = new T0107QueryTerminalPropertyResponse();
        minMessage.setTerminalType(0x0001);
        minMessage.setManufacturerId("A");
        minMessage.setTerminalModel("M");
        minMessage.setTerminalId("1");
        minMessage.setIccid("12345678901234567890");
        minMessage.setHardwareVersion("");
        minMessage.setFirmwareVersion("");
        minMessage.setGnssAttribute(0x00);
        minMessage.setCommunicationAttribute(0x00);
        
        Buffer minEncodedBody = minMessage.encodeBody();
        System.out.println("最小消息体长度: " + minEncodedBody.length() + " 字节");
        System.out.println("最小消息: " + minMessage.toString());
        
        System.out.println("\n=== T0107 消息示例完成 ===");
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}