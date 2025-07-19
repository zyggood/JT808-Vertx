package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0702DriverIdentityInfoReport 测试类
 */
class T0702DriverIdentityInfoReportTest {

    private T0702DriverIdentityInfoReport message;
    private LocalDateTime testTime;
    private LocalDateTime testValidityPeriod;

    @BeforeEach
    void setUp() {
        message = new T0702DriverIdentityInfoReport();
        testTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45);
        testValidityPeriod = LocalDateTime.of(2025, 6, 15, 0, 0, 0);
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x0702, message.getMessageId());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        T0702DriverIdentityInfoReport newMessage = new T0702DriverIdentityInfoReport();
        assertNotNull(newMessage);
        assertEquals(0x0702, newMessage.getMessageId());
    }

    @Test
    @DisplayName("测试IC卡拔出构造函数")
    void testCardRemoveConstructor() {
        T0702DriverIdentityInfoReport cardRemoveMessage = new T0702DriverIdentityInfoReport(testTime);
        assertEquals(T0702DriverIdentityInfoReport.STATUS_CARD_REMOVE, cardRemoveMessage.getStatus());
        assertEquals(testTime, cardRemoveMessage.getOperationTime());
        assertTrue(cardRemoveMessage.isCardRemove());
        assertFalse(cardRemoveMessage.isCardInsert());
    }

    @Test
    @DisplayName("测试IC卡插入但读卡失败构造函数")
    void testCardInsertFailedConstructor() {
        T0702DriverIdentityInfoReport cardInsertMessage = new T0702DriverIdentityInfoReport(
                testTime, T0702DriverIdentityInfoReport.READ_FAIL_AUTH);
        assertEquals(T0702DriverIdentityInfoReport.STATUS_CARD_INSERT, cardInsertMessage.getStatus());
        assertEquals(testTime, cardInsertMessage.getOperationTime());
        assertEquals(T0702DriverIdentityInfoReport.READ_FAIL_AUTH, cardInsertMessage.getIcCardReadResult());
        assertTrue(cardInsertMessage.isCardInsert());
        assertFalse(cardInsertMessage.isReadSuccess());
    }

    @Test
    @DisplayName("测试IC卡插入且读卡成功构造函数")
    void testCardInsertSuccessConstructor() {
        String driverName = "张三";
        String qualificationCode = "12345678901234567890";
        String issuerName = "交通运输部";
        
        T0702DriverIdentityInfoReport cardInsertMessage = new T0702DriverIdentityInfoReport(
                testTime, driverName, qualificationCode, issuerName, testValidityPeriod);
        
        assertEquals(T0702DriverIdentityInfoReport.STATUS_CARD_INSERT, cardInsertMessage.getStatus());
        assertEquals(testTime, cardInsertMessage.getOperationTime());
        assertEquals(T0702DriverIdentityInfoReport.READ_SUCCESS, cardInsertMessage.getIcCardReadResult());
        assertEquals(driverName, cardInsertMessage.getDriverName());
        assertEquals(qualificationCode, cardInsertMessage.getQualificationCode());
        assertEquals(issuerName, cardInsertMessage.getIssuerName());
        assertEquals(testValidityPeriod, cardInsertMessage.getValidityPeriod());
        assertTrue(cardInsertMessage.isCardInsert());
        assertTrue(cardInsertMessage.isReadSuccess());
        
        // 验证长度自动计算
        assertNotNull(cardInsertMessage.getDriverNameLength());
        assertNotNull(cardInsertMessage.getIssuerNameLength());
    }

    @Test
    @DisplayName("测试静态工厂方法 - 创建IC卡拔出消息")
    void testCreateCardRemove() {
        T0702DriverIdentityInfoReport cardRemoveMessage = T0702DriverIdentityInfoReport.createCardRemove(testTime);
        assertEquals(T0702DriverIdentityInfoReport.STATUS_CARD_REMOVE, cardRemoveMessage.getStatus());
        assertEquals(testTime, cardRemoveMessage.getOperationTime());
        assertTrue(cardRemoveMessage.isCardRemove());
    }

    @Test
    @DisplayName("测试静态工厂方法 - 创建IC卡插入失败消息")
    void testCreateCardInsertFailed() {
        T0702DriverIdentityInfoReport cardInsertMessage = T0702DriverIdentityInfoReport.createCardInsertFailed(
                testTime, T0702DriverIdentityInfoReport.READ_FAIL_LOCKED);
        assertEquals(T0702DriverIdentityInfoReport.STATUS_CARD_INSERT, cardInsertMessage.getStatus());
        assertEquals(testTime, cardInsertMessage.getOperationTime());
        assertEquals(T0702DriverIdentityInfoReport.READ_FAIL_LOCKED, cardInsertMessage.getIcCardReadResult());
        assertFalse(cardInsertMessage.isReadSuccess());
    }

    @Test
    @DisplayName("测试静态工厂方法 - 创建IC卡插入成功消息")
    void testCreateCardInsertSuccess() {
        String driverName = "李四";
        String qualificationCode = "98765432109876543210";
        String issuerName = "省交通厅";
        
        T0702DriverIdentityInfoReport cardInsertMessage = T0702DriverIdentityInfoReport.createCardInsertSuccess(
                testTime, driverName, qualificationCode, issuerName, testValidityPeriod);
        
        assertEquals(T0702DriverIdentityInfoReport.STATUS_CARD_INSERT, cardInsertMessage.getStatus());
        assertEquals(testTime, cardInsertMessage.getOperationTime());
        assertEquals(T0702DriverIdentityInfoReport.READ_SUCCESS, cardInsertMessage.getIcCardReadResult());
        assertEquals(driverName, cardInsertMessage.getDriverName());
        assertEquals(qualificationCode, cardInsertMessage.getQualificationCode());
        assertEquals(issuerName, cardInsertMessage.getIssuerName());
        assertEquals(testValidityPeriod, cardInsertMessage.getValidityPeriod());
        assertTrue(cardInsertMessage.isReadSuccess());
    }

    @Test
    @DisplayName("测试静态工厂方法 - 创建空消息")
    void testCreate() {
        T0702DriverIdentityInfoReport emptyMessage = T0702DriverIdentityInfoReport.create();
        assertNotNull(emptyMessage);
        assertEquals(0x0702, emptyMessage.getMessageId());
    }

    @Test
    @DisplayName("测试IC卡拔出消息编码解码")
    void testCardRemoveEncodeDecodeConsistency() {
        T0702DriverIdentityInfoReport originalMessage = T0702DriverIdentityInfoReport.createCardRemove(testTime);
        
        // 编码
        Buffer encoded = originalMessage.encodeBody();
        assertNotNull(encoded);
        assertEquals(7, encoded.length()); // 1(状态) + 6(时间) = 7字节
        
        // 解码
        T0702DriverIdentityInfoReport decodedMessage = new T0702DriverIdentityInfoReport();
        assertDoesNotThrow(() -> decodedMessage.decodeBody(encoded));
        
        // 验证一致性
        assertEquals(originalMessage.getStatus(), decodedMessage.getStatus());
        assertEquals(originalMessage.getOperationTime(), decodedMessage.getOperationTime());
        assertEquals(originalMessage, decodedMessage);
    }

    @Test
    @DisplayName("测试IC卡插入失败消息编码解码")
    void testCardInsertFailedEncodeDecodeConsistency() {
        T0702DriverIdentityInfoReport originalMessage = T0702DriverIdentityInfoReport.createCardInsertFailed(
                testTime, T0702DriverIdentityInfoReport.READ_FAIL_CHECKSUM);
        
        // 编码
        Buffer encoded = originalMessage.encodeBody();
        assertNotNull(encoded);
        assertEquals(8, encoded.length()); // 1(状态) + 6(时间) + 1(读取结果) = 8字节
        
        // 解码
        T0702DriverIdentityInfoReport decodedMessage = new T0702DriverIdentityInfoReport();
        assertDoesNotThrow(() -> decodedMessage.decodeBody(encoded));
        
        // 验证一致性
        assertEquals(originalMessage.getStatus(), decodedMessage.getStatus());
        assertEquals(originalMessage.getOperationTime(), decodedMessage.getOperationTime());
        assertEquals(originalMessage.getIcCardReadResult(), decodedMessage.getIcCardReadResult());
        assertEquals(originalMessage, decodedMessage);
    }

    @Test
    @DisplayName("测试IC卡插入成功消息编码解码")
    void testCardInsertSuccessEncodeDecodeConsistency() {
        String driverName = "王五";
        String qualificationCode = "ABCDEFGHIJ1234567890";
        String issuerName = "市交通局";
        
        T0702DriverIdentityInfoReport originalMessage = T0702DriverIdentityInfoReport.createCardInsertSuccess(
                testTime, driverName, qualificationCode, issuerName, testValidityPeriod);
        
        // 编码
        Buffer encoded = originalMessage.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 30); // 基本字段 + 可变长度字段
        
        // 解码
        T0702DriverIdentityInfoReport decodedMessage = new T0702DriverIdentityInfoReport();
        assertDoesNotThrow(() -> decodedMessage.decodeBody(encoded));
        
        // 验证一致性
        assertEquals(originalMessage.getStatus(), decodedMessage.getStatus());
        assertEquals(originalMessage.getOperationTime(), decodedMessage.getOperationTime());
        assertEquals(originalMessage.getIcCardReadResult(), decodedMessage.getIcCardReadResult());
        assertEquals(originalMessage.getDriverName(), decodedMessage.getDriverName());
        assertEquals(originalMessage.getQualificationCode(), decodedMessage.getQualificationCode());
        assertEquals(originalMessage.getIssuerName(), decodedMessage.getIssuerName());
        assertEquals(originalMessage.getValidityPeriod(), decodedMessage.getValidityPeriod());
        assertEquals(originalMessage, decodedMessage);
    }

    @Test
    @DisplayName("测试状态常量")
    void testStatusConstants() {
        assertEquals(0x01, T0702DriverIdentityInfoReport.STATUS_CARD_INSERT);
        assertEquals(0x02, T0702DriverIdentityInfoReport.STATUS_CARD_REMOVE);
    }

    @Test
    @DisplayName("测试IC卡读取结果常量")
    void testReadResultConstants() {
        assertEquals(0x00, T0702DriverIdentityInfoReport.READ_SUCCESS);
        assertEquals(0x01, T0702DriverIdentityInfoReport.READ_FAIL_AUTH);
        assertEquals(0x02, T0702DriverIdentityInfoReport.READ_FAIL_LOCKED);
        assertEquals(0x03, T0702DriverIdentityInfoReport.READ_FAIL_REMOVED);
        assertEquals(0x04, T0702DriverIdentityInfoReport.READ_FAIL_CHECKSUM);
    }

    @Test
    @DisplayName("测试状态判断方法")
    void testStatusMethods() {
        // 测试IC卡插入状态
        message.setStatus(T0702DriverIdentityInfoReport.STATUS_CARD_INSERT);
        assertTrue(message.isCardInsert());
        assertFalse(message.isCardRemove());
        
        // 测试IC卡拔出状态
        message.setStatus(T0702DriverIdentityInfoReport.STATUS_CARD_REMOVE);
        assertFalse(message.isCardInsert());
        assertTrue(message.isCardRemove());
    }

    @Test
    @DisplayName("测试读取成功判断方法")
    void testReadSuccessMethod() {
        // 测试读取成功
        message.setIcCardReadResult(T0702DriverIdentityInfoReport.READ_SUCCESS);
        assertTrue(message.isReadSuccess());
        
        // 测试读取失败
        message.setIcCardReadResult(T0702DriverIdentityInfoReport.READ_FAIL_AUTH);
        assertFalse(message.isReadSuccess());
        
        // 测试null情况
        message.setIcCardReadResult(null);
        assertFalse(message.isReadSuccess());
    }

    @Test
    @DisplayName("测试状态描述")
    void testStatusDescription() {
        message.setStatus(T0702DriverIdentityInfoReport.STATUS_CARD_INSERT);
        assertEquals("IC卡插入（驾驶员上班）", message.getStatusDescription());
        
        message.setStatus(T0702DriverIdentityInfoReport.STATUS_CARD_REMOVE);
        assertEquals("IC卡拔出（驾驶员下班）", message.getStatusDescription());
        
        message.setStatus((byte) 0xFF);
        assertTrue(message.getStatusDescription().contains("未知状态"));
    }

    @Test
    @DisplayName("测试读取结果描述")
    void testReadResultDescription() {
        message.setIcCardReadResult(T0702DriverIdentityInfoReport.READ_SUCCESS);
        assertEquals("IC卡读卡成功", message.getReadResultDescription());
        
        message.setIcCardReadResult(T0702DriverIdentityInfoReport.READ_FAIL_AUTH);
        assertEquals("读卡失败，原因为卡片密钥认证未通过", message.getReadResultDescription());
        
        message.setIcCardReadResult(T0702DriverIdentityInfoReport.READ_FAIL_LOCKED);
        assertEquals("读卡失败，原因为卡片已被锁定", message.getReadResultDescription());
        
        message.setIcCardReadResult(T0702DriverIdentityInfoReport.READ_FAIL_REMOVED);
        assertEquals("读卡失败，原因为卡片被拔出", message.getReadResultDescription());
        
        message.setIcCardReadResult(T0702DriverIdentityInfoReport.READ_FAIL_CHECKSUM);
        assertEquals("读卡失败，原因为数据校验错误", message.getReadResultDescription());
        
        message.setIcCardReadResult(null);
        assertEquals("不适用", message.getReadResultDescription());
        
        message.setIcCardReadResult((byte) 0xFF);
        assertTrue(message.getReadResultDescription().contains("未知读取结果"));
    }

    @Test
    @DisplayName("测试消息描述")
    void testGetMessageDescription() {
        assertEquals("驾驶员身份信息采集上报", message.getMessageDescription());
    }

    @Test
    @DisplayName("测试Getter和Setter方法")
    void testGettersAndSetters() {
        // 测试状态
        message.setStatus(T0702DriverIdentityInfoReport.STATUS_CARD_INSERT);
        assertEquals(T0702DriverIdentityInfoReport.STATUS_CARD_INSERT, message.getStatus());
        
        // 测试操作时间
        message.setOperationTime(testTime);
        assertEquals(testTime, message.getOperationTime());
        
        // 测试IC卡读取结果
        message.setIcCardReadResult(T0702DriverIdentityInfoReport.READ_SUCCESS);
        assertEquals(T0702DriverIdentityInfoReport.READ_SUCCESS, message.getIcCardReadResult());
        
        // 测试驾驶员姓名（自动计算长度）
        String driverName = "测试驾驶员";
        message.setDriverName(driverName);
        assertEquals(driverName, message.getDriverName());
        assertNotNull(message.getDriverNameLength());
        
        // 测试从业资格证编码
        String qualificationCode = "1234567890ABCDEFGHIJ";
        message.setQualificationCode(qualificationCode);
        assertEquals(qualificationCode, message.getQualificationCode());
        
        // 测试发证机构名称（自动计算长度）
        String issuerName = "测试发证机构";
        message.setIssuerName(issuerName);
        assertEquals(issuerName, message.getIssuerName());
        assertNotNull(message.getIssuerNameLength());
        
        // 测试证件有效期
        message.setValidityPeriod(testValidityPeriod);
        assertEquals(testValidityPeriod, message.getValidityPeriod());
    }

    @Test
    @DisplayName("测试驾驶员姓名长度自动计算")
    void testDriverNameLengthAutoCalculation() {
        String chineseName = "张三";
        message.setDriverName(chineseName);
        // GBK编码下，中文字符占2字节
        assertEquals(4, message.getDriverNameLength().byteValue());
        
        message.setDriverName(null);
        assertNull(message.getDriverNameLength());
    }

    @Test
    @DisplayName("测试发证机构名称长度自动计算")
    void testIssuerNameLengthAutoCalculation() {
        String chineseIssuer = "交通运输部";
        message.setIssuerName(chineseIssuer);
        // GBK编码下，中文字符占2字节
        assertEquals(10, message.getIssuerNameLength().byteValue());
        
        message.setIssuerName(null);
        assertNull(message.getIssuerNameLength());
    }

    @Test
    @DisplayName("测试从业资格证编码长度处理")
    void testQualificationCodeLengthHandling() {
        // 测试正好20位
        String code20 = "12345678901234567890";
        T0702DriverIdentityInfoReport testMessage = T0702DriverIdentityInfoReport.createCardInsertSuccess(
                testTime, "测试", code20, "测试机构", testValidityPeriod);
        
        Buffer encoded = testMessage.encodeBody();
        T0702DriverIdentityInfoReport decoded = new T0702DriverIdentityInfoReport();
        decoded.decodeBody(encoded);
        assertEquals(code20, decoded.getQualificationCode());
        
        // 测试少于20位（会补0x00）
        String code10 = "1234567890";
        testMessage = T0702DriverIdentityInfoReport.createCardInsertSuccess(
                testTime, "测试", code10, "测试机构", testValidityPeriod);
        
        encoded = testMessage.encodeBody();
        decoded = new T0702DriverIdentityInfoReport();
        decoded.decodeBody(encoded);
        assertEquals(code10, decoded.getQualificationCode());
    }

    @Test
    @DisplayName("测试异常处理 - 消息体长度不足")
    void testDecodeBodyInsufficientLength() {
        Buffer shortBuffer = Buffer.buffer(new byte[]{0x01, 0x23, 0x12, 0x25}); // 只有4字节
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> message.decodeBody(shortBuffer)
        );
        
        assertTrue(exception.getMessage().contains("驾驶员身份信息采集上报消息体长度不足"));
    }

    @Test
    @DisplayName("测试异常处理 - IC卡插入状态缺少读取结果")
    void testDecodeBodyMissingReadResult() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(T0702DriverIdentityInfoReport.STATUS_CARD_INSERT);
        buffer.appendBytes(new byte[]{0x23, 0x12, 0x25, 0x14, 0x30, 0x45}); // 只有状态和时间
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> message.decodeBody(buffer)
        );
        
        assertTrue(exception.getMessage().contains("IC卡插入状态下缺少IC卡读取结果字段"));
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        T0702DriverIdentityInfoReport testMessage = T0702DriverIdentityInfoReport.createCardInsertSuccess(
                testTime, "张三", "12345678901234567890", "交通运输部", testValidityPeriod);
        
        String str = testMessage.toString();
        assertNotNull(str);
        assertTrue(str.contains("T0702DriverIdentityInfoReport"));
        assertTrue(str.contains("messageId=0x702"));
        assertTrue(str.contains("张三"));
        assertTrue(str.contains("12345678901234567890"));
        assertTrue(str.contains("交通运输部"));
    }

    @Test
    @DisplayName("测试equals方法")
    void testEquals() {
        T0702DriverIdentityInfoReport message1 = T0702DriverIdentityInfoReport.createCardRemove(testTime);
        T0702DriverIdentityInfoReport message2 = T0702DriverIdentityInfoReport.createCardRemove(testTime);
        T0702DriverIdentityInfoReport message3 = T0702DriverIdentityInfoReport.createCardInsertFailed(
                testTime, T0702DriverIdentityInfoReport.READ_FAIL_AUTH);
        
        // 测试相等性
        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        
        // 测试自反性
        assertEquals(message1, message1);
        
        // 测试与null的比较
        assertNotEquals(message1, null);
        
        // 测试与不同类型对象的比较
        assertNotEquals(message1, "not a message");
    }

    @Test
    @DisplayName("测试hashCode方法")
    void testHashCode() {
        T0702DriverIdentityInfoReport message1 = T0702DriverIdentityInfoReport.createCardRemove(testTime);
        T0702DriverIdentityInfoReport message2 = T0702DriverIdentityInfoReport.createCardRemove(testTime);
        
        // 相等的对象应该有相同的hashCode
        assertEquals(message1.hashCode(), message2.hashCode());
    }

    @Test
    @DisplayName("测试边界值 - 空字符串处理")
    void testEmptyStringHandling() {
        T0702DriverIdentityInfoReport testMessage = T0702DriverIdentityInfoReport.createCardInsertSuccess(
                testTime, "", "", "", testValidityPeriod);
        
        Buffer encoded = testMessage.encodeBody();
        T0702DriverIdentityInfoReport decoded = new T0702DriverIdentityInfoReport();
        assertDoesNotThrow(() -> decoded.decodeBody(encoded));
    }

    @Test
    @DisplayName("测试边界值 - null时间处理")
    void testNullTimeHandling() {
        T0702DriverIdentityInfoReport testMessage = new T0702DriverIdentityInfoReport();
        testMessage.setStatus(T0702DriverIdentityInfoReport.STATUS_CARD_REMOVE);
        testMessage.setOperationTime(null);
        
        Buffer encoded = testMessage.encodeBody();
        assertNotNull(encoded);
        assertEquals(7, encoded.length()); // 1(状态) + 6(时间，全0) = 7字节
    }

    @Test
    @DisplayName("测试真实场景 - 驾驶员上班流程")
    void testRealScenarioDriverOnDuty() {
        // 模拟驾驶员插卡上班
        LocalDateTime onDutyTime = LocalDateTime.of(2023, 12, 25, 8, 0, 0);
        String driverName = "李明";
        String qualificationCode = "A1234567890123456789";
        String issuerName = "北京市交通委员会";
        LocalDateTime validityPeriod = LocalDateTime.of(2025, 12, 31, 0, 0, 0);
        
        T0702DriverIdentityInfoReport onDutyReport = T0702DriverIdentityInfoReport.createCardInsertSuccess(
                onDutyTime, driverName, qualificationCode, issuerName, validityPeriod);
        
        // 验证消息内容
        assertTrue(onDutyReport.isCardInsert());
        assertTrue(onDutyReport.isReadSuccess());
        assertEquals("IC卡插入（驾驶员上班）", onDutyReport.getStatusDescription());
        assertEquals("IC卡读卡成功", onDutyReport.getReadResultDescription());
        
        // 编码传输
        Buffer encoded = onDutyReport.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 30);
        
        // 平台接收解码
        T0702DriverIdentityInfoReport receivedReport = new T0702DriverIdentityInfoReport();
        assertDoesNotThrow(() -> receivedReport.decodeBody(encoded));
        
        // 验证接收数据
        assertEquals(onDutyReport, receivedReport);
        assertEquals(driverName, receivedReport.getDriverName());
        assertEquals(qualificationCode, receivedReport.getQualificationCode());
        assertEquals(issuerName, receivedReport.getIssuerName());
        assertEquals(validityPeriod, receivedReport.getValidityPeriod());
    }

    @Test
    @DisplayName("测试真实场景 - 驾驶员下班流程")
    void testRealScenarioDriverOffDuty() {
        // 模拟驾驶员拔卡下班
        LocalDateTime offDutyTime = LocalDateTime.of(2023, 12, 25, 18, 0, 0);
        
        T0702DriverIdentityInfoReport offDutyReport = T0702DriverIdentityInfoReport.createCardRemove(offDutyTime);
        
        // 验证消息内容
        assertTrue(offDutyReport.isCardRemove());
        assertEquals("IC卡拔出（驾驶员下班）", offDutyReport.getStatusDescription());
        
        // 编码传输
        Buffer encoded = offDutyReport.encodeBody();
        assertNotNull(encoded);
        assertEquals(7, encoded.length());
        
        // 平台接收解码
        T0702DriverIdentityInfoReport receivedReport = new T0702DriverIdentityInfoReport();
        assertDoesNotThrow(() -> receivedReport.decodeBody(encoded));
        
        // 验证接收数据
        assertEquals(offDutyReport, receivedReport);
        assertEquals(offDutyTime, receivedReport.getOperationTime());
    }

    @Test
    @DisplayName("测试真实场景 - IC卡读取失败")
    void testRealScenarioCardReadFailure() {
        // 模拟IC卡插入但读取失败
        LocalDateTime insertTime = LocalDateTime.of(2023, 12, 25, 9, 30, 0);
        
        T0702DriverIdentityInfoReport failureReport = T0702DriverIdentityInfoReport.createCardInsertFailed(
                insertTime, T0702DriverIdentityInfoReport.READ_FAIL_LOCKED);
        
        // 验证消息内容
        assertTrue(failureReport.isCardInsert());
        assertFalse(failureReport.isReadSuccess());
        assertEquals("读卡失败，原因为卡片已被锁定", failureReport.getReadResultDescription());
        
        // 编码传输
        Buffer encoded = failureReport.encodeBody();
        assertNotNull(encoded);
        assertEquals(8, encoded.length());
        
        // 平台接收解码
        T0702DriverIdentityInfoReport receivedReport = new T0702DriverIdentityInfoReport();
        assertDoesNotThrow(() -> receivedReport.decodeBody(encoded));
        
        // 验证接收数据
        assertEquals(failureReport, receivedReport);
        assertEquals(T0702DriverIdentityInfoReport.READ_FAIL_LOCKED, receivedReport.getIcCardReadResult());
    }
}