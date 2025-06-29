package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * T0200位置信息汇报性能测试
 * 测试在高负载情况下的性能表现
 */
class T0200PerformanceTest {
    
    private T0200LocationReport report;
    private Buffer complexAdditionalInfo;
    
    @BeforeEach
    void setUp() {
        report = new T0200LocationReport();
        
        // 设置基本位置信息
        report.setAlarmFlag(0x12345678);
        report.setStatusFlag(0x87654321);
        report.setLatitude(399086920);
        report.setLongitude(1163974770);
        report.setAltitude(100);
        report.setSpeed(80);
        report.setDirection(90);
        report.setDateTime(LocalDateTime.of(2024, 3, 15, 14, 30, 22));
        
        // 构建复杂的附加信息
        complexAdditionalInfo = Buffer.buffer();
        
        // 里程信息
        complexAdditionalInfo.appendByte((byte) 0x01);
        complexAdditionalInfo.appendByte((byte) 0x04);
        complexAdditionalInfo.appendInt(123456);
        
        // 油量信息
        complexAdditionalInfo.appendByte((byte) 0x02);
        complexAdditionalInfo.appendByte((byte) 0x02);
        complexAdditionalInfo.appendShort((short) 800);
        
        // 行驶记录速度
        complexAdditionalInfo.appendByte((byte) 0x03);
        complexAdditionalInfo.appendByte((byte) 0x02);
        complexAdditionalInfo.appendShort((short) 605);
        
        // 报警事件ID
        complexAdditionalInfo.appendByte((byte) 0x04);
        complexAdditionalInfo.appendByte((byte) 0x02);
        complexAdditionalInfo.appendShort((short) 2001);
        
        // 超速报警附加信息
        complexAdditionalInfo.appendByte((byte) 0x11);
        complexAdditionalInfo.appendByte((byte) 0x05);
        complexAdditionalInfo.appendByte((byte) 0x01);
        complexAdditionalInfo.appendInt(0x12345678);
        
        // 扩展车辆信号状态位
        complexAdditionalInfo.appendByte((byte) 0x25);
        complexAdditionalInfo.appendByte((byte) 0x04);
        complexAdditionalInfo.appendInt(0x00000017);
        
        // IO状态位
        complexAdditionalInfo.appendByte((byte) 0x2A);
        complexAdditionalInfo.appendByte((byte) 0x02);
        complexAdditionalInfo.appendShort((short) 0x0101);
        
        // 模拟量
        complexAdditionalInfo.appendByte((byte) 0x2B);
        complexAdditionalInfo.appendByte((byte) 0x04);
        complexAdditionalInfo.appendInt(0xABCD1234);
        
        // 信号强度
        complexAdditionalInfo.appendByte((byte) 0x30);
        complexAdditionalInfo.appendByte((byte) 0x01);
        complexAdditionalInfo.appendByte((byte) 92);
        
        // 卫星数
        complexAdditionalInfo.appendByte((byte) 0x31);
        complexAdditionalInfo.appendByte((byte) 0x01);
        complexAdditionalInfo.appendByte((byte) 14);
        
        report.setAdditionalInfo(complexAdditionalInfo);
    }
    
    @Test
    void testBasicOperationsPerformance() {
        // 测试基本操作的性能
        int iterations = 10000;
        
        // 测试报警检查性能
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            report.hasEmergencyAlarm();
            report.hasSpeedingAlarm();
            report.hasFatigueAlarm();
            report.hasDangerWarning();
            report.hasCollisionWarning();
        }
        long alarmCheckTime = System.nanoTime() - startTime;
        
        // 测试状态检查性能
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            report.isACCOn();
            report.isPositioned();
            report.isOutOfService();
            report.isDoorLocked();
            report.isGPSPositioning();
        }
        long statusCheckTime = System.nanoTime() - startTime;
        
        // 测试getter性能
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            report.getAlarmFlag();
            report.getStatusFlag();
            report.getLatitude();
            report.getLongitude();
            report.getSpeed();
        }
        long getterTime = System.nanoTime() - startTime;
        
        // 输出性能结果
        System.out.printf("基本操作性能测试 (%d次迭代):\n", iterations);
        System.out.printf("报警检查: %.2f ms (平均 %.2f ns/次)\n", 
            alarmCheckTime / 1_000_000.0, (double) alarmCheckTime / iterations / 5);
        System.out.printf("状态检查: %.2f ms (平均 %.2f ns/次)\n", 
            statusCheckTime / 1_000_000.0, (double) statusCheckTime / iterations / 5);
        System.out.printf("Getter操作: %.2f ms (平均 %.2f ns/次)\n", 
            getterTime / 1_000_000.0, (double) getterTime / iterations / 5);
        
        // 性能断言（这些值可能需要根据实际环境调整）
        assertTrue(alarmCheckTime < 100_000_000, "报警检查性能应在100ms内"); // 100ms
        assertTrue(statusCheckTime < 100_000_000, "状态检查性能应在100ms内"); // 100ms
        assertTrue(getterTime < 50_000_000, "Getter操作性能应在50ms内"); // 50ms
    }
    
    @Test
    void testAdditionalInfoParsingPerformance() {
        // 测试附加信息解析性能
        int iterations = 1000;
        
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            Map<Integer, Object> parsedInfo = report.getParsedAdditionalInfo();
            // 确保解析结果被使用，避免JVM优化
            assertNotNull(parsedInfo);
        }
        long parsingTime = System.nanoTime() - startTime;
        
        System.out.printf("附加信息解析性能测试 (%d次迭代):\n", iterations);
        System.out.printf("解析时间: %.2f ms (平均 %.2f μs/次)\n", 
            parsingTime / 1_000_000.0, (double) parsingTime / iterations / 1000);
        
        // 性能断言
        assertTrue(parsingTime < 1_000_000_000, "附加信息解析性能应在1秒内"); // 1秒
    }
    
    @Test
    void testToStringPerformance() {
        // 测试toString性能
        int iterations = 100;
        
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String result = report.toString();
            // 确保结果被使用，避免JVM优化
            assertNotNull(result);
            assertTrue(result.length() > 0);
        }
        long toStringTime = System.nanoTime() - startTime;
        
        System.out.printf("toString性能测试 (%d次迭代):\n", iterations);
        System.out.printf("toString时间: %.2f ms (平均 %.2f ms/次)\n", 
            toStringTime / 1_000_000.0, (double) toStringTime / iterations / 1_000_000);
        
        // 性能断言
        assertTrue(toStringTime < 5_000_000_000L, "toString性能应在5秒内"); // 5秒
    }
    
    @Test
    void testGetActiveAlarmDescriptionsPerformance() {
        // 测试获取激活报警描述的性能
        int iterations = 1000;
        
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            List<String> alarms = report.getActiveAlarmDescriptions();
            // 确保结果被使用，避免JVM优化
            assertNotNull(alarms);
        }
        long alarmDescTime = System.nanoTime() - startTime;
        
        System.out.printf("获取报警描述性能测试 (%d次迭代):\n", iterations);
        System.out.printf("获取报警描述时间: %.2f ms (平均 %.2f μs/次)\n", 
            alarmDescTime / 1_000_000.0, (double) alarmDescTime / iterations / 1000);
        
        // 性能断言
        assertTrue(alarmDescTime < 500_000_000, "获取报警描述性能应在500ms内"); // 500ms
    }
    
    @Test
    @Disabled("高负载测试，仅在需要时运行")
    void testHighLoadPerformance() {
        // 高负载性能测试
        int iterations = 100000;
        
        System.out.println("开始高负载性能测试...");
        
        long totalStartTime = System.nanoTime();
        
        // 模拟高频率的位置报告处理
        for (int i = 0; i < iterations; i++) {
            // 更新位置信息
            report.setLatitude(399086920 + (i % 1000) * 1000);
            report.setLongitude(1163974770 + (i % 1000) * 1000);
            report.setSpeed(60 + (i % 40));
            report.setDirection(i % 360);
            report.setDateTime(LocalDateTime.of(2024, 3, 15, 14, 30, 22));
            
            // 执行常见操作
            if (i % 10 == 0) {
                report.hasEmergencyAlarm();
                report.isACCOn();
            }
            
            if (i % 100 == 0) {
                report.getParsedAdditionalInfo();
            }
            
            if (i % 1000 == 0) {
                report.toString();
            }
        }
        
        long totalTime = System.nanoTime() - totalStartTime;
        
        System.out.printf("高负载性能测试完成 (%d次迭代):\n", iterations);
        System.out.printf("总时间: %.2f ms\n", totalTime / 1_000_000.0);
        System.out.printf("平均每次操作: %.2f μs\n", (double) totalTime / iterations / 1000);
        System.out.printf("吞吐量: %.0f 操作/秒\n", iterations * 1_000_000_000.0 / totalTime);
        
        // 性能断言
        assertTrue(totalTime < 30_000_000_000L, "高负载测试应在30秒内完成"); // 30秒
    }
    
    @Test
    void testConcurrentAccess() throws InterruptedException {
        // 测试并发访问性能
        int threadCount = 10;
        int iterationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Long> threadTimes = new CopyOnWriteArrayList<>();
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    long threadStartTime = System.nanoTime();
                    
                    for (int j = 0; j < iterationsPerThread; j++) {
                        // 创建独立的报告实例避免并发修改
                        T0200LocationReport threadReport = new T0200LocationReport();
                        threadReport.setAlarmFlag(0x12345678 + threadId);
                        threadReport.setStatusFlag(0x87654321 + threadId);
                        threadReport.setLatitude(399086920 + threadId * 1000);
                        threadReport.setLongitude(1163974770 + threadId * 1000);
                        threadReport.setAdditionalInfo(complexAdditionalInfo);
                        
                        // 执行各种操作
                        threadReport.hasEmergencyAlarm();
                        threadReport.isACCOn();
                        threadReport.getParsedAdditionalInfo();
                        
                        if (j % 100 == 0) {
                            threadReport.toString();
                        }
                        
                        successCount.incrementAndGet();
                    }
                    
                    long threadTime = System.nanoTime() - threadStartTime;
                    threadTimes.add(threadTime);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有线程完成
        assertTrue(latch.await(30, TimeUnit.SECONDS), "并发测试应在30秒内完成");
        
        long totalTime = System.nanoTime() - startTime;
        
        executor.shutdown();
        
        // 计算统计信息
        long minThreadTime = threadTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        long maxThreadTime = threadTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        double avgThreadTime = threadTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        
        System.out.printf("并发访问性能测试 (%d线程, 每线程%d次迭代):\n", threadCount, iterationsPerThread);
        System.out.printf("总时间: %.2f ms\n", totalTime / 1_000_000.0);
        System.out.printf("成功操作数: %d/%d\n", successCount.get(), threadCount * iterationsPerThread);
        System.out.printf("线程时间 - 最小: %.2f ms, 最大: %.2f ms, 平均: %.2f ms\n", 
            minThreadTime / 1_000_000.0, maxThreadTime / 1_000_000.0, avgThreadTime / 1_000_000.0);
        System.out.printf("总吞吐量: %.0f 操作/秒\n", 
            successCount.get() * 1_000_000_000.0 / totalTime);
        
        // 验证所有操作都成功完成
        assertEquals(threadCount * iterationsPerThread, successCount.get(), "所有操作都应成功完成");
    }
    
    @Test
    void testMemoryUsage() {
        // 测试内存使用情况
        Runtime runtime = Runtime.getRuntime();
        
        // 强制垃圾回收
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 创建大量位置报告对象
        List<T0200LocationReport> reports = new ArrayList<>();
        int objectCount = 1000;
        
        for (int i = 0; i < objectCount; i++) {
            T0200LocationReport newReport = new T0200LocationReport();
            newReport.setAlarmFlag(0x12345678 + i);
            newReport.setStatusFlag(0x87654321 + i);
            newReport.setLatitude(399086920 + i * 1000);
            newReport.setLongitude(1163974770 + i * 1000);
            newReport.setAdditionalInfo(complexAdditionalInfo);
            
            // 触发附加信息解析
            newReport.getParsedAdditionalInfo();
            
            reports.add(newReport);
        }
        
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = usedMemory - initialMemory;
        
        System.out.printf("内存使用测试 (%d个对象):\n", objectCount);
        System.out.printf("初始内存: %.2f MB\n", initialMemory / 1024.0 / 1024.0);
        System.out.printf("使用内存: %.2f MB\n", usedMemory / 1024.0 / 1024.0);
        System.out.printf("内存增长: %.2f MB\n", memoryIncrease / 1024.0 / 1024.0);
        System.out.printf("平均每对象: %.2f KB\n", (double) memoryIncrease / objectCount / 1024.0);
        
        // 内存使用断言（这些值可能需要根据实际环境调整）
        assertTrue(memoryIncrease < 100 * 1024 * 1024, "内存增长应小于100MB"); // 100MB
        
        // 清理引用
        reports.clear();
        System.gc();
    }
    
    @Test
    void testLargeAdditionalInfoPerformance() {
        // 测试大量附加信息的性能
        Buffer largeAdditionalInfo = Buffer.buffer();
        
        // 添加50个附加信息项
        for (int i = 1; i <= 50; i++) {
            largeAdditionalInfo.appendByte((byte) (i % 256));
            largeAdditionalInfo.appendByte((byte) 0x04);
            largeAdditionalInfo.appendInt(i * 1000);
        }
        
        T0200LocationReport largeReport = new T0200LocationReport();
        largeReport.setAlarmFlag(0x12345678);
        largeReport.setStatusFlag(0x87654321);
        largeReport.setLatitude(399086920);
        largeReport.setLongitude(1163974770);
        largeReport.setAdditionalInfo(largeAdditionalInfo);
        
        int iterations = 100;
        
        // 测试解析性能
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            Map<Integer, Object> parsedInfo = largeReport.getParsedAdditionalInfo();
            assertNotNull(parsedInfo);
        }
        long parsingTime = System.nanoTime() - startTime;
        
        // 测试toString性能
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String result = largeReport.toString();
            assertNotNull(result);
        }
        long toStringTime = System.nanoTime() - startTime;
        
        System.out.printf("大量附加信息性能测试 (50个附加信息项, %d次迭代):\n", iterations);
        System.out.printf("解析时间: %.2f ms (平均 %.2f ms/次)\n", 
            parsingTime / 1_000_000.0, (double) parsingTime / iterations / 1_000_000);
        System.out.printf("toString时间: %.2f ms (平均 %.2f ms/次)\n", 
            toStringTime / 1_000_000.0, (double) toStringTime / iterations / 1_000_000);
        
        // 性能断言
        assertTrue(parsingTime < 5_000_000_000L, "大量附加信息解析应在5秒内完成");
        assertTrue(toStringTime < 10_000_000_000L, "大量附加信息toString应在10秒内完成");
    }
}