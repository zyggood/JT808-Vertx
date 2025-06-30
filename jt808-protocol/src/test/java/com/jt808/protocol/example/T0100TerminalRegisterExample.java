package com.jt808.protocol.example;

import com.jt808.protocol.factory.JT808MessageFactory;
import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T0100TerminalRegister;
import com.jt808.protocol.util.ChecksumUtils;
import com.jt808.protocol.util.EscapeUtils;
import com.jt808.common.exception.ProtocolException;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

/**
 * T0100终端注册消息使用示例
 */
public class T0100TerminalRegisterExample {
    
    @Test
    public void demonstrateT0100Usage() throws ProtocolException {
        System.out.println("=== T0100终端注册消息使用示例 ===");
        
        // 1. 使用工厂创建消息
        JT808MessageFactory factory = JT808MessageFactory.getInstance();
        JT808Message message = factory.createMessage(0x0100);
        
        System.out.println("1. 工厂创建的消息类型: " + message.getClass().getSimpleName());
        System.out.println("   消息ID: 0x" + Integer.toHexString(message.getMessageId()).toUpperCase());
        
        // 2. 直接创建T0100消息并设置数据
        T0100TerminalRegister register = new T0100TerminalRegister();
        
        // 设置省域ID和市县域ID (GB/T 2260行政区划代码)
        register.setProvinceId(11); // 北京市
        register.setCityId(100);    // 北京市市辖区
        
        // 设置制造商信息
        register.setManufacturerId("TESLA"); // 5字节制造商ID
        register.setTerminalModel("MODEL3_TERMINAL_V1.0"); // 20字节终端型号
        register.setTerminalId("TES001A"); // 7字节终端ID
        
        // 设置车牌信息
        register.setPlateColor((byte) 1); // 蓝色车牌
        register.setPlateNumber("京A12345"); // 车牌号
        
        System.out.println("\n2. 终端注册信息:");
        System.out.println("   省域ID: " + register.getProvinceId() + " (北京)");
        System.out.println("   市县域ID: " + register.getCityId());
        System.out.println("   制造商ID: " + register.getManufacturerId());
        System.out.println("   终端型号: " + register.getTerminalModel());
        System.out.println("   终端ID: " + register.getTerminalId());
        System.out.println("   车牌颜色: " + register.getPlateColor() + " (" + register.getPlateColorDescription() + ")");
        System.out.println("   车牌号码: " + register.getPlateNumber());
        
        // 3. 设置消息头
        JT808Header header = new JT808Header(0x0100, "13800138000", 1001);
        register.setHeader(header);
        
        System.out.println("\n3. 消息头信息:");
        System.out.println("   终端手机号: " + header.getPhoneNumber());
        System.out.println("   消息流水号: " + header.getSerialNumber());
        
        // 4. 编码消息体
        Buffer encodedBody = register.encodeBody();
        System.out.println("\n4. 编码后的消息体:");
        System.out.println("   消息体长度: " + encodedBody.length() + " 字节");
        System.out.print("   消息体内容: ");
        for (int i = 0; i < encodedBody.length(); i++) {
            System.out.printf("%02X ", encodedBody.getByte(i) & 0xFF);
            if ((i + 1) % 16 == 0) {
                System.out.println();
                System.out.print("                ");
            }
        }
        System.out.println();
        
        // 5. 解析消息体字段
        System.out.println("\n5. 消息体字段解析:");
        analyzeMessageBody(encodedBody);
        
        // 6. 解码验证
        T0100TerminalRegister decoded = new T0100TerminalRegister();
        decoded.decodeBody(encodedBody);
        
        System.out.println("\n6. 解码验证:");
        System.out.println("   编码解码一致性: " + isDataConsistent(register, decoded));
        
        // 7. 使用工厂编码完整消息
        Buffer completeMessage = factory.encodeMessage(register);
        
        System.out.println("\n7. 完整消息编码:");
        System.out.println("   完整消息长度: " + completeMessage.length() + " 字节");
        System.out.print("   完整消息: ");
        for (int i = 0; i < Math.min(completeMessage.length(), 30); i++) {
            System.out.printf("%02X ", completeMessage.getByte(i) & 0xFF);
        }
        if (completeMessage.length() > 30) {
            System.out.print("...");
        }
        System.out.println();
        
        // 8. 校验码验证
        ChecksumUtils.ChecksumResult checksumResult = ChecksumUtils.verifyCompleteMessage(completeMessage);
        System.out.println("\n8. 校验码验证: " + checksumResult.getMessage());
        
        // 9. 转义处理
        boolean needsEscape = EscapeUtils.needsEscape(completeMessage);
        System.out.println("\n9. 转义处理:");
        System.out.println("   是否需要转义: " + needsEscape);
        
        if (needsEscape) {
            Buffer escaped = EscapeUtils.escape(completeMessage);
            System.out.println("   转义后长度: " + escaped.length() + " 字节");
        }
        
        // 10. 工厂解析消息
        JT808Message parsed = factory.parseMessage(completeMessage);
        System.out.println("\n10. 工厂解析结果:");
        System.out.println("    解析的消息类型: " + parsed.getClass().getSimpleName());
        System.out.println("    消息ID匹配: " + (parsed.getMessageId() == 0x0100));
        
        if (parsed instanceof T0100TerminalRegister) {
            T0100TerminalRegister parsedRegister = (T0100TerminalRegister) parsed;
            System.out.println("    解析的制造商ID: " + parsedRegister.getManufacturerId());
            System.out.println("    解析的终端型号: " + parsedRegister.getTerminalModel());
            System.out.println("    解析的车牌号: " + parsedRegister.getPlateNumber());
        }
        
        // 11. 实际应用场景
        System.out.println("\n11. 实际应用场景:");
        demonstrateRealWorldScenarios();
    }
    
    /**
     * 分析消息体字段
     */
    private void analyzeMessageBody(Buffer body) {
        int index = 0;
        
        // 省域ID (2字节)
        int provinceId = body.getUnsignedShort(index);
        System.out.println("   省域ID: " + provinceId + " (偏移: " + index + ", 长度: 2字节)");
        index += 2;
        
        // 市县域ID (2字节)
        int cityId = body.getUnsignedShort(index);
        System.out.println("   市县域ID: " + cityId + " (偏移: " + index + ", 长度: 2字节)");
        index += 2;
        
        // 制造商ID (5字节)
        byte[] manufacturerBytes = body.getBytes(index, index + 5);
        String manufacturerId = new String(manufacturerBytes).trim().replace("\0", "");
        System.out.println("   制造商ID: '" + manufacturerId + "' (偏移: " + index + ", 长度: 5字节)");
        index += 5;
        
        // 终端型号 (20字节)
        byte[] modelBytes = body.getBytes(index, index + 20);
        String terminalModel = new String(modelBytes).trim().replace("\0", "");
        System.out.println("   终端型号: '" + terminalModel + "' (偏移: " + index + ", 长度: 20字节)");
        index += 20;
        
        // 终端ID (7字节)
        byte[] terminalIdBytes = body.getBytes(index, index + 7);
        String terminalId = new String(terminalIdBytes).trim().replace("\0", "");
        System.out.println("   终端ID: '" + terminalId + "' (偏移: " + index + ", 长度: 7字节)");
        index += 7;
        
        // 车牌颜色 (1字节)
        byte plateColor = body.getByte(index);
        System.out.println("   车牌颜色: " + (plateColor & 0xFF) + " (偏移: " + index + ", 长度: 1字节)");
        index += 1;
        
        // 车辆标识 (剩余字节)
        if (index < body.length()) {
            byte[] plateBytes = body.getBytes(index, body.length());
            String plateNumber = new String(plateBytes);
            System.out.println("   车辆标识: '" + plateNumber + "' (偏移: " + index + ", 长度: " + plateBytes.length + "字节)");
        }
    }
    
    /**
     * 检查数据一致性
     */
    private boolean isDataConsistent(T0100TerminalRegister original, T0100TerminalRegister decoded) {
        return original.getProvinceId() == decoded.getProvinceId() &&
               original.getCityId() == decoded.getCityId() &&
               original.getManufacturerId().equals(decoded.getManufacturerId()) &&
               original.getTerminalModel().equals(decoded.getTerminalModel()) &&
               original.getTerminalId().equals(decoded.getTerminalId()) &&
               original.getPlateColor() == decoded.getPlateColor() &&
               original.getPlateNumber().equals(decoded.getPlateNumber());
    }
    
    /**
     * 演示实际应用场景
     */
    private void demonstrateRealWorldScenarios() {
        System.out.println("    场景1: 新车辆终端首次注册");
        demonstrateNewVehicleRegistration();
        
        System.out.println("\n    场景2: 未上牌车辆注册(使用VIN码)");
        demonstrateUnplatedVehicleRegistration();
        
        System.out.println("\n    场景3: 不同车牌颜色的车辆注册");
        demonstrateDifferentPlateColors();
    }
    
    /**
     * 新车辆终端首次注册场景
     */
    private void demonstrateNewVehicleRegistration() {
        T0100TerminalRegister register = new T0100TerminalRegister();
        
        // 广东深圳的一辆蓝牌小汽车
        register.setProvinceId(44);  // 广东省
        register.setCityId(300);     // 深圳市
        register.setManufacturerId("BYD01");
        register.setTerminalModel("BYD_TERMINAL_2024");
        register.setTerminalId("BYD2024");
        register.setPlateColor((byte) 1); // 蓝色
        register.setPlateNumber("粤B88888");
        
        JT808Header header = new JT808Header(0x0100, "13900139000", 1);
        register.setHeader(header);
        
        System.out.println("      终端手机号: " + header.getPhoneNumber());
        System.out.println("      注册地区: 广东深圳");
        System.out.println("      制造商: " + register.getManufacturerId());
        System.out.println("      车牌: " + register.getPlateColorDescription() + "牌 " + register.getPlateNumber());
    }
    
    /**
     * 未上牌车辆注册场景
     */
    private void demonstrateUnplatedVehicleRegistration() {
        T0100TerminalRegister register = new T0100TerminalRegister();
        
        // 上海的一辆未上牌新能源车
        register.setProvinceId(31);  // 上海市
        register.setCityId(100);     // 上海市区
        register.setManufacturerId("TESLA");
        register.setTerminalModel("MODEL_Y_2024");
        register.setTerminalId("TSL2024");
        register.setPlateColor((byte) 0); // 未上牌
        register.setPlateNumber("LRWXB2B41JG123456"); // VIN码
        
        JT808Header header = new JT808Header(0x0100, "13700137000", 1);
        register.setHeader(header);
        
        System.out.println("      终端手机号: " + header.getPhoneNumber());
        System.out.println("      注册地区: 上海");
        System.out.println("      制造商: " + register.getManufacturerId());
        System.out.println("      车辆状态: 未上牌");
        System.out.println("      VIN码: " + register.getPlateNumber());
    }
    
    /**
     * 不同车牌颜色场景
     */
    private void demonstrateDifferentPlateColors() {
        // 黄牌大货车
        T0100TerminalRegister yellowPlate = new T0100TerminalRegister();
        yellowPlate.setProvinceId(37); // 山东
        yellowPlate.setCityId(100);
        yellowPlate.setManufacturerId("SINOTR");
        yellowPlate.setTerminalModel("HOWO_TRUCK_2024");
        yellowPlate.setTerminalId("HOW2024");
        yellowPlate.setPlateColor((byte) 2); // 黄色
        yellowPlate.setPlateNumber("鲁A12345");
        
        System.out.println("      黄牌货车: " + yellowPlate.getPlateColorDescription() + "牌 " + yellowPlate.getPlateNumber());
        
        // 绿牌新能源车
        T0100TerminalRegister greenPlate = new T0100TerminalRegister();
        greenPlate.setProvinceId(32); // 江苏
        greenPlate.setCityId(100);
        greenPlate.setManufacturerId("NIO01");
        greenPlate.setTerminalModel("NIO_ES8_2024");
        greenPlate.setTerminalId("NIO2024");
        greenPlate.setPlateColor((byte) 9); // 其他(绿牌)
        greenPlate.setPlateNumber("苏AD12345");
        
        System.out.println("      新能源车: " + greenPlate.getPlateColorDescription() + "牌 " + greenPlate.getPlateNumber());
    }
}