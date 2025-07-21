package com.jt808.protocol.processor.impl;

import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import com.jt808.protocol.message.T0100TerminalRegister;
import com.jt808.protocol.message.T0200LocationReport;
import com.jt808.protocol.processor.ProcessContext;
import com.jt808.protocol.processor.ProcessResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageValidator 测试类
 */
class MessageValidatorTest {

    private static final Logger logger = LoggerFactory.getLogger(MessageValidatorTest.class);
    
    private MessageValidator validator;
    private Vertx vertx;
    
    @BeforeEach
    void setUp() {
        validator = new MessageValidator(false); // 非严格模式
        vertx = Vertx.vertx();
    }
    
    @Test
    void testValidTerminalRegisterMessage() {
        logger.info("测试有效的终端注册消息");
        
        // 创建有效的终端注册消息
        T0100TerminalRegister register = new T0100TerminalRegister();
        register.setProvinceId(11); // 北京
        register.setCityId(100); // 北京市
        register.setManufacturerId("TEST1");
        register.setTerminalModel("MODEL001");
        register.setTerminalId("1234567");
        register.setPlateColor((byte) 1); // 蓝色
        register.setPlateNumber("京A12345");
        
        ProcessContext context = new ProcessContext(UUID.randomUUID().toString(), register, vertx);
        
        Future<ProcessResult> future = validator.process(context);
        assertTrue(future.succeeded());
        
        ProcessResult result = future.result();
        assertTrue(result.isSuccess());
        logger.info("终端注册消息验证通过");
    }
    
    @Test
    void testInvalidTerminalRegisterMessage() {
        logger.info("测试无效的终端注册消息");
        
        // 创建无效的终端注册消息（省域ID超出范围）
        T0100TerminalRegister register = new T0100TerminalRegister();
        register.setProvinceId(100); // 无效的省域ID
        register.setCityId(100);
        register.setManufacturerId("TEST1");
        register.setTerminalModel("MODEL001");
        register.setTerminalId("1234567");
        register.setPlateColor((byte) 1);
        register.setPlateNumber("京A12345");
        
        ProcessContext context = new ProcessContext(UUID.randomUUID().toString(), register, vertx);
        
        Future<ProcessResult> future = validator.process(context);
        assertTrue(future.succeeded());
        
        // 在非严格模式下，验证失败会产生警告但不会失败
        ProcessResult result = future.result();
        assertTrue(result.isSuccess());
        
        // 检查是否有验证警告
        Object warnings = context.getAttribute("validationWarnings");
        assertNotNull(warnings);
        logger.info("终端注册消息验证产生警告: {}", warnings);
    }
    
    @Test
    void testValidLocationReportMessage() {
        logger.info("测试有效的位置信息汇报消息");
        
        // 创建有效的位置信息汇报消息
        T0200LocationReport locationReport = new T0200LocationReport();
        locationReport.setAlarmFlag(0);
        locationReport.setStatusFlag(0);
        locationReport.setLatitude(39_123456); // 北京纬度
        locationReport.setLongitude(116_123456); // 北京经度
        locationReport.setAltitude(50); // 海拔50米
        locationReport.setSpeed(600); // 60km/h
        locationReport.setDirection(90); // 正东方向
        locationReport.setDateTime(LocalDateTime.now());
        
        ProcessContext context = new ProcessContext(UUID.randomUUID().toString(), locationReport, vertx);
        
        Future<ProcessResult> future = validator.process(context);
        assertTrue(future.succeeded());
        
        ProcessResult result = future.result();
        assertTrue(result.isSuccess());
        logger.info("位置信息汇报消息验证通过");
    }
    
    @Test
    void testInvalidLocationReportMessage() {
        logger.info("测试无效的位置信息汇报消息");
        
        // 创建无效的位置信息汇报消息（纬度超出范围）
        T0200LocationReport locationReport = new T0200LocationReport();
        locationReport.setAlarmFlag(0);
        locationReport.setStatusFlag(0);
        locationReport.setLatitude(100_000_000); // 无效的纬度
        locationReport.setLongitude(116_123456);
        locationReport.setAltitude(50);
        locationReport.setSpeed(600);
        locationReport.setDirection(90);
        locationReport.setDateTime(LocalDateTime.now());
        
        ProcessContext context = new ProcessContext(UUID.randomUUID().toString(), locationReport, vertx);
        
        Future<ProcessResult> future = validator.process(context);
        assertTrue(future.succeeded());
        
        // 在非严格模式下，验证失败会产生警告但不会失败
        ProcessResult result = future.result();
        assertTrue(result.isSuccess());
        
        // 检查是否有验证警告
        Object warnings = context.getAttribute("validationWarnings");
        assertNotNull(warnings);
        logger.info("位置信息汇报消息验证产生警告: {}", warnings);
    }
    
    @Test
    void testStrictModeValidation() {
        logger.info("测试严格模式验证");
        
        MessageValidator strictValidator = new MessageValidator(true);
        
        // 创建无效的终端注册消息
        T0100TerminalRegister register = new T0100TerminalRegister();
        register.setProvinceId(100); // 无效的省域ID
        register.setCityId(100);
        register.setManufacturerId("TEST1");
        register.setTerminalModel("MODEL001");
        register.setTerminalId("1234567");
        register.setPlateColor((byte) 1);
        register.setPlateNumber("京A12345");
        
        ProcessContext context = new ProcessContext(UUID.randomUUID().toString(), register, vertx);
        
        Future<ProcessResult> future = strictValidator.process(context);
        assertTrue(future.succeeded());
        
        // 在严格模式下，验证失败应该返回失败结果
        ProcessResult result = future.result();
        assertFalse(result.isSuccess());
        logger.info("严格模式验证失败: {}", result.getMessage());
    }
    
    @Test
    void testNullMessage() {
        logger.info("测试空消息验证");
        
        ProcessContext context = new ProcessContext(UUID.randomUUID().toString(), null, vertx);
        
        Future<ProcessResult> future = validator.process(context);
        assertTrue(future.succeeded());
        
        ProcessResult result = future.result();
        assertFalse(result.isSuccess());
        logger.info("空消息验证失败: {}", result.getMessage());
    }
    
    @Test
    void testValidatorStats() {
        logger.info("测试验证器统计信息");
        
        var stats = validator.getValidationStats();
        assertNotNull(stats);
        assertTrue((Integer) stats.get("globalRulesCount") > 0);
        assertTrue((Integer) stats.get("messageSpecificRulesCount") > 0);
        assertFalse((Boolean) stats.get("strictMode"));
        
        logger.info("验证器统计信息: {}", stats);
    }
}