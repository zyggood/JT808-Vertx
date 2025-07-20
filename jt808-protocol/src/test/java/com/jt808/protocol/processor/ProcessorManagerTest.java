package com.jt808.protocol.processor;

import com.jt808.protocol.message.JT808Header;
import com.jt808.protocol.message.JT808Message;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class ProcessorManagerTest {

    private ProcessorManager processorManager;
    private Vertx vertx;

    @BeforeEach
    void setUp(Vertx vertx) {
        this.vertx = vertx;
        JsonObject config = new JsonObject()
                .put("performance.detailedLogging", true)
                .put("validation.strictMode", false)
                .put("session.timeoutMs", 60000L);
        
        this.processorManager = new ProcessorManager(vertx, config);
    }

    @Test
    void testProcessorManagerInitialization() {
        // 验证处理器管理器初始化
        assertNotNull(processorManager);
        assertNotNull(processorManager.getPerformanceMonitor());
        assertNotNull(processorManager.getMessageValidator());
        assertNotNull(processorManager.getSessionHandler());
        assertNotNull(processorManager.getMessageRouter());
        assertNotNull(processorManager.getProcessorChain());
    }

    @Test
    void testHealthCheck() {
        // 验证健康检查
        assertTrue(processorManager.isHealthy());
    }

    @Test
    void testSystemStatus() {
        // 验证系统状态获取
        JsonObject status = processorManager.getSystemStatus();
        assertNotNull(status);
        assertTrue(status.containsKey("processorsCount"));
        assertTrue(status.containsKey("activeProcessors"));
        assertTrue(status.containsKey("performance"));
        assertTrue(status.containsKey("activeSessions"));
        assertTrue(status.containsKey("validation"));
        assertTrue(status.containsKey("processorChain"));
    }

    @Test
    void testProcessMessage(VertxTestContext testContext) {
        // 创建测试消息
        TestMessage testMessage = new TestMessage();
        testMessage.setHeader(new JT808Header(0x0002, "13800138001", 1));

        // 处理消息
        processorManager.processMessage(testMessage)
                .onComplete(testContext.succeeding(results -> testContext.verify(() -> {
                    assertNotNull(results);
                    assertFalse(results.isEmpty());
                    
                    // 验证所有处理器都被执行了
                    assertEquals(4, results.size()); // 4个处理器
                    
                    // 验证处理结果
                    for (ProcessResult result : results) {
                        assertNotNull(result);
                        assertNotNull(result.getProcessorName());
                        assertTrue(result.getProcessingDuration() >= 0);
                    }
                    
                    testContext.completeNow();
                })));
    }

    @Test
    void testProcessorEnableDisable() {
        // 测试处理器启用/禁用
        String processorName = "PerformanceMonitor";
        
        // 禁用处理器
        assertTrue(processorManager.setProcessorEnabled(processorName, false));
        
        // 重新启用处理器
        assertTrue(processorManager.setProcessorEnabled(processorName, true));
        
        // 测试不存在的处理器
        assertFalse(processorManager.setProcessorEnabled("NonExistentProcessor", false));
    }

    @Test
    void testStatsReset() {
        // 测试统计信息重置
        assertDoesNotThrow(() -> processorManager.resetStats());
    }

    @Test
    void testConfigReload() {
        // 测试配置重新加载
        JsonObject newConfig = new JsonObject()
                .put("performance.detailedLogging", false)
                .put("validation.strictMode", true);
        
        assertDoesNotThrow(() -> processorManager.reloadConfig(newConfig));
    }

    @Test
    void testShutdown() {
        // 测试关闭
        assertDoesNotThrow(() -> processorManager.shutdown());
    }

    /**
     * 测试消息类
     */
    private static class TestMessage extends JT808Message {
        @Override
        public int getMessageId() {
            return 0x0002; // 心跳消息
        }

        @Override
        public Buffer encodeBody() {
            // 心跳消息没有消息体
            return Buffer.buffer();
        }

        @Override
        public void decodeBody(Buffer body) {
            // 心跳消息没有消息体，无需解码
        }

        @Override
        public String toString() {
            return "TestMessage{messageId=0x0002}";
        }
    }
}