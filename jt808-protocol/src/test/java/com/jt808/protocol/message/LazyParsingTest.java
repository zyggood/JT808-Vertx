package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 延迟解析功能测试
 */
class LazyParsingTest {

    private static final Logger logger = LoggerFactory.getLogger(LazyParsingTest.class);
    
    private T0200LocationReport report;
    
    @BeforeEach
    void setUp() {
        report = new T0200LocationReport();
    }
    
    @Test
    void testLazyParsingInitialization() {
        // 测试初始化时parsedAdditionalInfo为null（因为没有设置附加信息）
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        assertNull(parsedInfo, "初始化时应返回null");
        logger.info("初始化测试通过：parsedAdditionalInfo初始为null");
    }
    
    @Test
    void testLazyParsingAfterSetAdditionalInfo() {
        // 创建附加信息
        Buffer additionalInfo = Buffer.buffer();
        
        // 添加里程信息 (ID: 0x01, 长度: 4字节)
        additionalInfo.appendByte((byte) 0x01);
        additionalInfo.appendByte((byte) 0x04);
        additionalInfo.appendInt(123456); // 12345.6 km
        
        // 添加油量信息 (ID: 0x02, 长度: 2字节)
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendByte((byte) 0x02);
        additionalInfo.appendShort((short) 800); // 80.0 L
        
        // 设置附加信息
        report.setAdditionalInfo(additionalInfo);
        
        // 验证附加信息已设置但未解析
        assertNotNull(report.getAdditionalInfo());
        assertEquals(additionalInfo.length(), report.getAdditionalInfo().length());
        
        // 第一次访问时触发解析
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        assertNotNull(parsedInfo);
        assertFalse(parsedInfo.isEmpty());
        
        // 验证解析结果
        assertTrue(parsedInfo.containsKey(0x01)); // 里程
        assertTrue(parsedInfo.containsKey(0x02)); // 油量
        
        logger.info("延迟解析测试通过：附加信息在首次访问时正确解析");
        logger.info("解析结果: {}", parsedInfo);
    }
    
    @Test
    void testLazyParsingPerformance() {
        // 创建复杂的附加信息
        Buffer additionalInfo = Buffer.buffer();
        
        // 添加多个附加信息项
        for (int i = 0; i < 10; i++) {
            additionalInfo.appendByte((byte) 0x01); // 里程
            additionalInfo.appendByte((byte) 0x04);
            additionalInfo.appendInt(123456 + i);
        }
        
        // 测试设置性能（应该很快，因为不解析）
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            report.setAdditionalInfo(additionalInfo);
        }
        long setTime = System.nanoTime() - startTime;
        
        // 测试首次访问性能（包含解析）
        startTime = System.nanoTime();
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        long firstAccessTime = System.nanoTime() - startTime;
        
        // 测试后续访问性能（应该很快，因为已缓存）
        startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            report.getParsedAdditionalInfo();
        }
        long cachedAccessTime = System.nanoTime() - startTime;
        
        logger.info("延迟解析性能测试:");
        logger.info("设置附加信息 (1000次): {} μs", setTime / 1000);
        logger.info("首次解析访问: {} μs", firstAccessTime / 1000);
        logger.info("缓存访问 (1000次): {} μs", cachedAccessTime / 1000);
        
        // 验证解析结果正确
        assertNotNull(parsedInfo);
        assertFalse(parsedInfo.isEmpty());
        
        // 验证延迟解析的基本功能：设置操作不应该太慢
        assertTrue(setTime < 10_000_000, "设置操作应该在10ms内完成"); // 10ms
        
        logger.info("延迟解析性能测试通过");
    }
    
    @Test
    void testLazyParsingWithEmptyAdditionalInfo() {
        // 测试空附加信息的延迟解析
        report.setAdditionalInfo(Buffer.buffer());
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        assertNull(parsedInfo, "空附加信息应返回null");
        
        logger.info("空附加信息延迟解析测试通过");
    }
    
    @Test
    void testLazyParsingWithNullAdditionalInfo() {
        // 测试null附加信息的延迟解析
        report.setAdditionalInfo(null);
        
        Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
        assertNull(parsedInfo, "null附加信息应返回null");
        
        logger.info("null附加信息延迟解析测试通过");
    }
}