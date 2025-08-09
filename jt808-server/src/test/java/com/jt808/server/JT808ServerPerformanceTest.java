package com.jt808.server;

import com.jt808.protocol.codec.JT808Encoder;
import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.T0002TerminalHeartbeat;
import com.jt808.protocol.message.T0200LocationReport;
import com.jt808.server.handler.JT808MessageHandler;
import com.jt808.server.session.SessionManager;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808服务器性能测试
 * 测试服务器在高并发场景下的消息接收和处理性能
 */
@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JT808ServerPerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(JT808ServerPerformanceTest.class);
    
    private static final int TEST_PORT = 17611;
    private static final String TEST_HOST = "localhost";
    
    private JT808Server server;
    private SessionManager sessionManager;
    private JT808MessageHandler messageHandler;
    private JT808Encoder encoder;
    
    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        encoder = new JT808Encoder();
        
        // 部署服务器
        server = new JT808Server();
        vertx.deployVerticle(server)
            .onSuccess(id -> {
                logger.info("测试服务器启动成功，部署ID: {}", id);
                testContext.completeNow();
            })
            .onFailure(testContext::failNow);
    }
    
    @AfterEach
    void tearDown(Vertx vertx, VertxTestContext testContext) {
        if (server != null) {
            vertx.undeploy(server.deploymentID())
                .onComplete(ar -> testContext.completeNow());
        } else {
            testContext.completeNow();
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("单连接消息处理性能测试")
    void testSingleConnectionPerformance(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        int messageCount = 1000;
        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong startTime = new AtomicLong();
        AtomicLong endTime = new AtomicLong();
        AtomicInteger sentCount = new AtomicInteger(0);
        
        NetClient client = vertx.createNetClient();
        
        client.connect(TEST_PORT, TEST_HOST)
            .onSuccess(socket -> {
                logger.info("客户端连接成功");
                startTime.set(System.currentTimeMillis());
                
                // 发送心跳消息
                sendHeartbeatMessages(socket, messageCount, sentCount, () -> {
                    endTime.set(System.currentTimeMillis());
                    latch.countDown();
                });
            })
            .onFailure(testContext::failNow);
        
        // 等待测试完成
        assertTrue(latch.await(30, TimeUnit.SECONDS), "测试超时");
        
        long duration = endTime.get() - startTime.get();
        double qps = (double) messageCount / duration * 1000;
        
        logger.info("单连接性能测试结果:");
        logger.info("  消息数量: {}", messageCount);
        logger.info("  总耗时: {} ms", duration);
        logger.info("  QPS: {:.2f}", qps);
        logger.info("  平均延迟: {:.2f} ms", (double) duration / messageCount);
        
        // 验证性能指标
        assertTrue(qps > 100, "QPS应该大于100，实际: " + qps);
        assertTrue(duration < 20000, "总耗时应该小于20秒，实际: " + duration + "ms");
        
        testContext.completeNow();
    }
    
    @Test
    @Order(2)
    @DisplayName("多连接并发性能测试")
    void testMultiConnectionPerformance(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        int connectionCount = 10;
        int messagesPerConnection = 100;
        int totalMessages = connectionCount * messagesPerConnection;
        
        CountDownLatch latch = new CountDownLatch(connectionCount);
        AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
        AtomicLong endTime = new AtomicLong();
        AtomicInteger totalSent = new AtomicInteger(0);
        
        NetClient client = vertx.createNetClient();
        
        // 创建多个连接
        for (int i = 0; i < connectionCount; i++) {
            final int connectionId = i;
            
            client.connect(TEST_PORT, TEST_HOST)
                .onSuccess(socket -> {
                    logger.debug("连接 {} 建立成功", connectionId);
                    
                    AtomicInteger sentCount = new AtomicInteger(0);
                    sendHeartbeatMessages(socket, messagesPerConnection, sentCount, () -> {
                        int currentTotal = totalSent.addAndGet(messagesPerConnection);
                        if (currentTotal == totalMessages) {
                            endTime.set(System.currentTimeMillis());
                        }
                        latch.countDown();
                    });
                })
                .onFailure(testContext::failNow);
        }
        
        // 等待所有连接完成
        assertTrue(latch.await(60, TimeUnit.SECONDS), "多连接测试超时");
        
        long duration = endTime.get() - startTime.get();
        double qps = (double) totalMessages / duration * 1000;
        
        logger.info("多连接性能测试结果:");
        logger.info("  连接数: {}", connectionCount);
        logger.info("  每连接消息数: {}", messagesPerConnection);
        logger.info("  总消息数: {}", totalMessages);
        logger.info("  总耗时: {} ms", duration);
        logger.info("  总QPS: {:.2f}", qps);
        logger.info("  平均每连接QPS: {:.2f}", qps / connectionCount);
        
        // 验证性能指标
        assertTrue(qps > 500, "总QPS应该大于500，实际: " + qps);
        assertTrue(duration < 30000, "总耗时应该小于30秒，实际: " + duration + "ms");
        
        testContext.completeNow();
    }
    
    @Test
    @Order(3)
    @DisplayName("混合消息类型性能测试")
    void testMixedMessageTypePerformance(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        int messageCount = 500;
        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong startTime = new AtomicLong();
        AtomicLong endTime = new AtomicLong();
        AtomicInteger sentCount = new AtomicInteger(0);
        
        NetClient client = vertx.createNetClient();
        
        client.connect(TEST_PORT, TEST_HOST)
            .onSuccess(socket -> {
                logger.info("混合消息测试连接成功");
                startTime.set(System.currentTimeMillis());
                
                // 发送混合消息（心跳和位置汇报）
                sendMixedMessages(socket, messageCount, sentCount, () -> {
                    endTime.set(System.currentTimeMillis());
                    latch.countDown();
                });
            })
            .onFailure(testContext::failNow);
        
        // 等待测试完成
        assertTrue(latch.await(30, TimeUnit.SECONDS), "混合消息测试超时");
        
        long duration = endTime.get() - startTime.get();
        double qps = (double) messageCount / duration * 1000;
        
        logger.info("混合消息性能测试结果:");
        logger.info("  消息数量: {} (心跳 + 位置汇报)", messageCount);
        logger.info("  总耗时: {} ms", duration);
        logger.info("  QPS: {:.2f}", qps);
        logger.info("  平均延迟: {:.2f} ms", (double) duration / messageCount);
        
        // 验证性能指标（混合消息处理稍慢）
        assertTrue(qps > 50, "混合消息QPS应该大于50，实际: " + qps);
        assertTrue(duration < 25000, "总耗时应该小于25秒，实际: " + duration + "ms");
        
        testContext.completeNow();
    }
    
    @Test
    @Order(4)
    @DisplayName("长时间稳定性测试")
    void testLongTermStabilityPerformance(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        int testDurationSeconds = 10; // 10秒稳定性测试
        int messagesPerSecond = 100;
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong startTime = new AtomicLong();
        AtomicLong endTime = new AtomicLong();
        AtomicInteger totalSent = new AtomicInteger(0);
        
        NetClient client = vertx.createNetClient();
        
        client.connect(TEST_PORT, TEST_HOST)
            .onSuccess(socket -> {
                logger.info("稳定性测试连接成功");
                startTime.set(System.currentTimeMillis());
                
                // 定时发送消息
                long timerId = vertx.setPeriodic(1000 / messagesPerSecond, id -> {
                    if (totalSent.get() >= testDurationSeconds * messagesPerSecond) {
                        vertx.cancelTimer(id);
                        endTime.set(System.currentTimeMillis());
                        latch.countDown();
                        return;
                    }
                    
                    // 发送心跳消息
                    T0002TerminalHeartbeat heartbeat = createHeartbeatMessage("13800138000", totalSent.incrementAndGet());
                    Buffer buffer = encoder.encode(heartbeat);
                    socket.write(buffer);
                });
            })
            .onFailure(testContext::failNow);
        
        // 等待测试完成
        assertTrue(latch.await(testDurationSeconds + 10, TimeUnit.SECONDS), "稳定性测试超时");
        
        long duration = endTime.get() - startTime.get();
        int expectedMessages = testDurationSeconds * messagesPerSecond;
        double actualQps = (double) totalSent.get() / duration * 1000;
        
        logger.info("稳定性测试结果:");
        logger.info("  测试时长: {} 秒", testDurationSeconds);
        logger.info("  期望消息数: {}", expectedMessages);
        logger.info("  实际发送数: {}", totalSent.get());
        logger.info("  实际耗时: {} ms", duration);
        logger.info("  实际QPS: {:.2f}", actualQps);
        
        // 验证稳定性
        assertTrue(totalSent.get() >= expectedMessages * 0.95, "发送消息数应该达到期望的95%以上");
        assertTrue(actualQps >= messagesPerSecond * 0.9, "实际QPS应该达到期望的90%以上");
        
        testContext.completeNow();
    }
    
    /**
     * 发送心跳消息
     */
    private void sendHeartbeatMessages(NetSocket socket, int count, AtomicInteger sentCount, Runnable onComplete) {
        if (sentCount.get() >= count) {
            onComplete.run();
            return;
        }
        
        // 发送心跳消息
        T0002TerminalHeartbeat heartbeat = createHeartbeatMessage("13800138000", sentCount.incrementAndGet());
        Buffer buffer = encoder.encode(heartbeat);
        
        socket.write(buffer)
            .onSuccess(v -> {
                // 继续发送下一条消息
                sendHeartbeatMessages(socket, count, sentCount, onComplete);
            })
            .onFailure(throwable -> {
                logger.error("发送消息失败", throwable);
                onComplete.run();
            });
    }
    
    /**
     * 发送混合消息（心跳和位置汇报）
     */
    private void sendMixedMessages(NetSocket socket, int count, AtomicInteger sentCount, Runnable onComplete) {
        if (sentCount.get() >= count) {
            onComplete.run();
            return;
        }
        
        Buffer buffer;
        int current = sentCount.incrementAndGet();
        
        if (current % 2 == 0) {
            // 发送心跳消息
            T0002TerminalHeartbeat heartbeat = createHeartbeatMessage("13800138000", current);
            buffer = encoder.encode(heartbeat);
        } else {
            // 发送位置汇报消息
            T0200LocationReport location = createLocationMessage("13800138000", current);
            buffer = encoder.encode(location);
        }
        
        socket.write(buffer)
            .onSuccess(v -> {
                // 继续发送下一条消息
                sendMixedMessages(socket, count, sentCount, onComplete);
            })
            .onFailure(throwable -> {
                logger.error("发送混合消息失败", throwable);
                onComplete.run();
            });
    }
    
    /**
     * 创建心跳消息
     */
    private T0002TerminalHeartbeat createHeartbeatMessage(String phoneNumber, int serialNumber) {
        T0002TerminalHeartbeat heartbeat = new T0002TerminalHeartbeat();
        JT808Header header = new JT808Header(0x0002, phoneNumber, serialNumber);
        heartbeat.setHeader(header);
        return heartbeat;
    }
    
    /**
     * 创建位置汇报消息
     */
    private T0200LocationReport createLocationMessage(String phoneNumber, int serialNumber) {
        T0200LocationReport location = new T0200LocationReport();
        location.setAlarmFlag(0x00000000);
        location.setStatusFlag(0x00000002); // ACC开
        location.setLatitudeDegrees(31.230416 + (serialNumber % 100) * 0.001); // 模拟位置变化
        location.setLongitudeDegrees(121.473701 + (serialNumber % 100) * 0.001);
        location.setAltitude(10 + serialNumber % 50);
        location.setSpeedKmh(60.0 + serialNumber % 40);
        location.setDirection(serialNumber % 360);
        location.setDateTime(LocalDateTime.now());
        
        JT808Header header = new JT808Header(0x0200, phoneNumber, serialNumber);
        location.setHeader(header);
        return location;
    }
}