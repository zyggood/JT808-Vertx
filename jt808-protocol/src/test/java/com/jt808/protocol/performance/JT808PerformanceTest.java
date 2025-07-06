package com.jt808.protocol.performance;

import com.jt808.protocol.codec.JT808Decoder;
import com.jt808.protocol.codec.JT808Encoder;
import com.jt808.protocol.message.*;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808性能和并发测试
 */
@Execution(ExecutionMode.CONCURRENT)
class JT808PerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(JT808PerformanceTest.class);

    private JT808Encoder encoder;
    private JT808Decoder decoder;

    @BeforeEach
    void setUp() {
        encoder = new JT808Encoder();
        decoder = new JT808Decoder();
    }

    @Test
    @DisplayName("测试心跳消息编码性能")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testHeartbeatEncodingPerformance() {
        int messageCount = 10000;

        // 预热
        for (int i = 0; i < 1000; i++) {
            T0002TerminalHeartbeat heartbeat = createHeartbeat(i);
            encoder.encode(heartbeat);
        }

        // 性能测试
        long startTime = System.nanoTime();

        for (int i = 0; i < messageCount; i++) {
            T0002TerminalHeartbeat heartbeat = createHeartbeat(i);
            Buffer encoded = encoder.encode(heartbeat);
            assertNotNull(encoded);
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double avgTimeMs = (duration / 1_000_000.0) / messageCount;

        logger.info("心跳消息编码性能: {}条消息, 总耗时: {}ms, 平均: {}ms/条",
                messageCount, String.format("%.2f", duration / 1_000_000.0), String.format("%.4f", avgTimeMs));

        // 验证性能要求：平均每条消息编码时间应小于0.1ms
        assertTrue(avgTimeMs < 0.1, "心跳消息编码性能不达标: " + avgTimeMs + "ms/条");
    }

    @Test
    @DisplayName("测试位置信息编码性能")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testLocationReportEncodingPerformance() {
        int messageCount = 5000;

        // 预热
        for (int i = 0; i < 500; i++) {
            T0200LocationReport location = createLocationReport(i);
            encoder.encode(location);
        }

        // 性能测试
        long startTime = System.nanoTime();

        for (int i = 0; i < messageCount; i++) {
            T0200LocationReport location = createLocationReport(i);
            Buffer encoded = encoder.encode(location);
            assertNotNull(encoded);
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double avgTimeMs = (duration / 1_000_000.0) / messageCount;

        logger.info("位置信息编码性能: {}条消息, 总耗时: {}ms, 平均: {}ms/条",
                messageCount, String.format("%.2f", duration / 1_000_000.0), String.format("%.4f", avgTimeMs));

        // 验证性能要求：平均每条消息编码时间应小于0.5ms
        assertTrue(avgTimeMs < 0.5, "位置信息编码性能不达标: " + avgTimeMs + "ms/条");
    }

    @Test
    @DisplayName("测试消息解码性能")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testMessageDecodingPerformance() {
        int messageCount = 5000;

        // 预先编码消息
        List<Buffer> encodedMessages = new ArrayList<>();
        for (int i = 0; i < messageCount; i++) {
            T0200LocationReport location = createLocationReport(i);
            encodedMessages.add(encoder.encode(location));
        }

        // 预热
        for (int i = 0; i < 500; i++) {
            try {
                decoder.decode(encodedMessages.get(i % encodedMessages.size()));
            } catch (Exception e) {
                // 性能测试中忽略异常
            }
        }

        // 性能测试
        long startTime = System.nanoTime();

        for (Buffer encoded : encodedMessages) {
            try {
                JT808Message decoded = decoder.decode(encoded);
                assertNotNull(decoded);
            } catch (Exception e) {
                // 性能测试中忽略异常
            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double avgTimeMs = (duration / 1_000_000.0) / messageCount;

        logger.info("消息解码性能: {}条消息, 总耗时: {}ms, 平均: {}ms/条",
                messageCount, String.format("%.2f", duration / 1_000_000.0), String.format("%.4f", avgTimeMs));

        // 验证性能要求：平均每条消息解码时间应小于0.5ms
        assertTrue(avgTimeMs < 0.5, "消息解码性能不达标: " + avgTimeMs + "ms/条");
    }

    @Test
    @DisplayName("测试编解码往返性能")
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testRoundTripPerformance() {
        int messageCount = 3000;

        // 预热
        for (int i = 0; i < 300; i++) {
            T0200LocationReport location = createLocationReport(i);
            Buffer encoded = encoder.encode(location);
            try {
                decoder.decode(encoded);
            } catch (Exception e) {
                // 性能测试中忽略异常
            }
        }

        // 性能测试
        long startTime = System.nanoTime();

        for (int i = 0; i < messageCount; i++) {
            T0200LocationReport location = createLocationReport(i);
            Buffer encoded = encoder.encode(location);
            try {
                JT808Message decoded = decoder.decode(encoded);
                assertNotNull(encoded);
                assertNotNull(decoded);
                assertEquals(0x0200, decoded.getMessageId());
            } catch (Exception e) {
                // 性能测试中忽略异常
            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double avgTimeMs = (duration / 1_000_000.0) / messageCount;

        logger.info("编解码往返性能: {}条消息, 总耗时: {}ms, 平均: {}ms/条",
                messageCount, String.format("%.2f", duration / 1_000_000.0), String.format("%.4f", avgTimeMs));

        // 验证性能要求：平均每条消息往返时间应小于1ms
        assertTrue(avgTimeMs < 1.0, "编解码往返性能不达标: " + avgTimeMs + "ms/条");
    }

    @Test
    @DisplayName("测试并发编码")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testConcurrentEncoding() throws InterruptedException {
        int threadCount = 10;
        int messagesPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicLong totalTime = new AtomicLong(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    JT808Encoder threadEncoder = new JT808Encoder();
                    long threadStartTime = System.nanoTime();

                    for (int i = 0; i < messagesPerThread; i++) {
                        T0200LocationReport location = createLocationReport(threadId * messagesPerThread + i);
                        Buffer encoded = threadEncoder.encode(location);
                        if (encoded != null && encoded.length() > 0) {
                            successCount.incrementAndGet();
                        }
                    }

                    long threadEndTime = System.nanoTime();
                    totalTime.addAndGet(threadEndTime - threadStartTime);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(25, TimeUnit.SECONDS), "并发编码测试超时");
        executor.shutdown();

        int expectedTotal = threadCount * messagesPerThread;
        assertEquals(expectedTotal, successCount.get(), "并发编码成功数量不匹配");

        double avgTimeMs = (totalTime.get() / 1_000_000.0) / expectedTotal;
        logger.info("并发编码性能: {}线程, 每线程{}条消息, 平均: {}ms/条",
                threadCount, messagesPerThread, String.format("%.4f", avgTimeMs));

        // 验证并发性能
        assertTrue(avgTimeMs < 2.0, "并发编码性能不达标: " + avgTimeMs + "ms/条");
    }

    @Test
    @DisplayName("测试并发解码")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testConcurrentDecoding() throws InterruptedException {
        int threadCount = 10;
        int messagesPerThread = 1000;

        // 预先准备编码消息
        List<Buffer> encodedMessages = new ArrayList<>();
        for (int i = 0; i < threadCount * messagesPerThread; i++) {
            T0200LocationReport location = createLocationReport(i);
            encodedMessages.add(encoder.encode(location));
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicLong totalTime = new AtomicLong(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    JT808Decoder threadDecoder = new JT808Decoder();
                    long threadStartTime = System.nanoTime();

                    for (int i = 0; i < messagesPerThread; i++) {
                        int messageIndex = threadId * messagesPerThread + i;
                        Buffer encoded = encodedMessages.get(messageIndex);
                        try {
                            JT808Message decoded = threadDecoder.decode(encoded);
                            if (decoded != null && decoded.getMessageId() == 0x0200) {
                                successCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            // 并发测试中忽略异常
                        }

                    }

                    long threadEndTime = System.nanoTime();
                    totalTime.addAndGet(threadEndTime - threadStartTime);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(25, TimeUnit.SECONDS), "并发解码测试超时");
        executor.shutdown();

        int expectedTotal = threadCount * messagesPerThread;
        assertEquals(expectedTotal, successCount.get(), "并发解码成功数量不匹配");

        double avgTimeMs = (totalTime.get() / 1_000_000.0) / expectedTotal;
        logger.info("并发解码性能: {}线程, 每线程{}条消息, 平均: {}ms/条",
                threadCount, messagesPerThread, String.format("%.4f", avgTimeMs));

        // 验证并发性能
        assertTrue(avgTimeMs < 2.0, "并发解码性能不达标: " + avgTimeMs + "ms/条");
    }

    @Test
    @DisplayName("测试内存使用")
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        // 强制垃圾回收
        System.gc();
        Thread.yield();

        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // 创建大量消息
        int messageCount = 10000;
        List<JT808Message> messages = new ArrayList<>();

        for (int i = 0; i < messageCount; i++) {
            T0200LocationReport location = createLocationReport(i);
            messages.add(location);

            // 编码消息
            Buffer encoded = encoder.encode(location);
            assertNotNull(encoded);
        }

        long peakMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = peakMemory - initialMemory;

        // 清理引用
        messages.clear();
        System.gc();
        Thread.yield();

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryLeaked = finalMemory - initialMemory;

        logger.info("内存使用情况: 初始={}KB, 峰值={}KB, 使用={}KB, 泄漏={}KB",
                initialMemory / 1024, peakMemory / 1024, memoryUsed / 1024, memoryLeaked / 1024);

        // 验证内存使用合理性
        double avgMemoryPerMessage = (double) memoryUsed / messageCount;
        assertTrue(avgMemoryPerMessage < 1024, "平均每条消息内存使用过高: " + avgMemoryPerMessage + "字节");

        // 验证没有严重的内存泄漏
        assertTrue(memoryLeaked < memoryUsed * 0.1, "检测到可能的内存泄漏: " + memoryLeaked + "字节");
    }

    @Test
    @DisplayName("测试大批量消息处理")
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testBulkMessageProcessing() {
        int batchSize = 50000;

        long startTime = System.currentTimeMillis();

        // 批量编码
        List<Buffer> encodedMessages = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            JT808Message message;
            if (i % 3 == 0) {
                message = createHeartbeat(i);
            } else if (i % 3 == 1) {
                message = createLocationReport(i);
            } else {
                message = createTerminalAuth(i);
            }

            Buffer encoded = encoder.encode(message);
            encodedMessages.add(encoded);
        }

        long encodeTime = System.currentTimeMillis();

        // 批量解码
        List<JT808Message> decodedMessages = new ArrayList<>(batchSize);
        for (Buffer encoded : encodedMessages) {
            try {
                JT808Message decoded = decoder.decode(encoded);
                decodedMessages.add(decoded);
            } catch (Exception e) {
                // 性能测试中忽略异常
            }
        }

        long decodeTime = System.currentTimeMillis();

        // 验证结果
        assertEquals(batchSize, encodedMessages.size());
        assertEquals(batchSize, decodedMessages.size());

        long totalTime = decodeTime - startTime;
        double avgTimeMs = (double) totalTime / batchSize;

        logger.info("大批量处理性能: {}条消息, 编码耗时: {}ms, 解码耗时: {}ms, 总耗时: {}ms, 平均: {}ms/条",
                batchSize, encodeTime - startTime, decodeTime - encodeTime, totalTime, String.format("%.4f", avgTimeMs));

        // 验证批量处理性能
        assertTrue(avgTimeMs < 2.0, "大批量处理性能不达标: " + avgTimeMs + "ms/条");
    }

    // 辅助方法
    private T0002TerminalHeartbeat createHeartbeat(int index) {
        T0002TerminalHeartbeat heartbeat = new T0002TerminalHeartbeat();
        JT808Header header = new JT808Header(0x0002, "1380013800" + (index % 10), index);
        heartbeat.setHeader(header);
        return heartbeat;
    }

    private T0200LocationReport createLocationReport(int index) {
        T0200LocationReport location = new T0200LocationReport();
        location.setAlarmFlag(index % 2);
        location.setStatusFlag(index % 4);
        location.setLatitudeDegrees(31.230416 + (index % 100) * 0.001);
        location.setLongitudeDegrees(121.473701 + (index % 100) * 0.001);
        location.setAltitude(10 + index % 1000);
        location.setSpeedKmh((index % 120) + 0.5);
        location.setDirection(index % 360);
        location.setDateTime(LocalDateTime.now().plusSeconds(index));

        JT808Header header = new JT808Header(0x0200, "1380013800" + (index % 10), index);
        location.setHeader(header);
        return location;
    }

    private T0102TerminalAuth createTerminalAuth(int index) {
        T0102TerminalAuth auth = new T0102TerminalAuth("AUTH_CODE_" + index);
        JT808Header header = new JT808Header(0x0102, "1380013800" + (index % 10), index);
        auth.setHeader(header);
        return auth;
    }
}