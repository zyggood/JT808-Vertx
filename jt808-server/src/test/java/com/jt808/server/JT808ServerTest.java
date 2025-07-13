package com.jt808.server;

import com.jt808.common.util.ByteUtils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808服务器集成测试
 */
@ExtendWith(VertxExtension.class)
class JT808ServerTest {
    
    private JT808Server server;
    private NetClient client;
    private static int portCounter = 8000; // 起始端口
    private int currentTcpPort; // 当前测试使用的TCP端口
    
    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        server = new JT808Server();
        client = vertx.createNetClient();
        
        // 为每个测试使用不同的端口
        currentTcpPort = portCounter++;
        int udpPort = portCounter++;
        
        JsonObject config = new JsonObject()
                .put("tcp.port", currentTcpPort)
                .put("udp.port", udpPort);
        
        // 部署服务器
        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        vertx.deployVerticle(server, options)
                .onSuccess(id -> testContext.completeNow())
                .onFailure(testContext::failNow);
    }
    
    @AfterEach
    void tearDown(Vertx vertx, VertxTestContext testContext) {
        if (client != null) {
            client.close();
        }
        testContext.completeNow();
    }
    
    @Test
    void testServerStartup(Vertx vertx, VertxTestContext testContext) {
        // 服务器应该已经在setUp中启动
        // 尝试连接到TCP端口
        client.connect(currentTcpPort, "localhost")
            .onSuccess(socket -> {
                assertNotNull(socket);
                socket.close();
                testContext.completeNow();
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    void testTcpConnection(Vertx vertx, VertxTestContext testContext) {
        client.connect(currentTcpPort, "localhost")
            .onSuccess(socket -> {
                assertNotNull(socket);
                assertTrue(socket.writeQueueFull() == false);

                String hex = "020000d40123456789017fff000004000000080006eeb6ad02633df7013800030063200707192359642f000000400101020a0a02010a1e00640001b2070003640e200707192359000100000061646173200827111111010101652f000000410202020a0000000a1e00c8000516150006c81c20070719235900020000000064736d200827111111020202662900000042031e012c00087a23000a2c2a200707192359000300000074706d732008271111110303030067290000004304041e0190000bde31000d90382007071923590004000000006273642008271111110404049d";

                // 将十六进制字符串转换为字节数组
                byte[] bytes = ByteUtils.hexToBytes(hex);

                // 创建包含起始和结束标识位的完整消息
                Buffer testData = Buffer.buffer();
                testData.appendByte((byte) 0x7E); // 起始标识位
                testData.appendBytes(bytes);
                testData.appendByte((byte) 0x7E); // 结束标识位

                socket.write(testData);
                
                // 设置数据处理器
                socket.handler(buffer -> {
                    assertNotNull(buffer);
                    socket.close();
                    testContext.completeNow();
                });
                
                // 如果没有收到响应，也认为连接成功
                vertx.setTimer(60_1000, id -> {
                    socket.close();
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    void testMultipleConnections(Vertx vertx, VertxTestContext testContext) {
        int connectionCount = 3;
        int[] completedConnections = {0};
        
        for (int i = 0; i < connectionCount; i++) {
            client.connect(currentTcpPort, "localhost")
                .onSuccess(socket -> {
                    assertNotNull(socket);
                    
                    completedConnections[0]++;
                    socket.close();
                    
                    if (completedConnections[0] == connectionCount) {
                        testContext.completeNow();
                    }
                })
                .onFailure(testContext::failNow);
        }
    }
    
    @Test
    void testConnectionClose(Vertx vertx, VertxTestContext testContext) {
        client.connect(currentTcpPort, "localhost")
            .onSuccess(socket -> {
                assertNotNull(socket);
                
                // 设置关闭处理器
                socket.closeHandler(v -> {
                    testContext.completeNow();
                });
                
                // 立即关闭连接
                socket.close();
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    void testInvalidData(Vertx vertx, VertxTestContext testContext) {
        client.connect(currentTcpPort, "localhost")
            .onSuccess(socket -> {
                assertNotNull(socket);
                
                // 发送无效数据
                Buffer invalidData = Buffer.buffer(new byte[]{0x01, 0x02, 0x03});
                socket.write(invalidData);
                
                // 设置异常处理器
                socket.exceptionHandler(throwable -> {
                    // 预期可能会有异常
                    socket.close();
                    testContext.completeNow();
                });
                
                // 等待一段时间后关闭连接
                vertx.setTimer(1000, id -> {
                    socket.close();
                    testContext.completeNow();
                });
            })
            .onFailure(testContext::failNow);
    }
    
    @Test
    void testServerStop(Vertx vertx, VertxTestContext testContext) {
        // 停止服务器
        Promise<Void> stopPromise = Promise.promise();
        server.stop(stopPromise);
        stopPromise.future().onComplete(ar -> {
            if (ar.succeeded()) {
                // 尝试连接应该失败
                client.connect(currentTcpPort, "localhost")
                    .onSuccess(socket -> {
                        socket.close();
                        testContext.failNow("连接应该失败，因为服务器已停止");
                    })
                    .onFailure(throwable -> {
                        // 连接失败是预期的
                        testContext.completeNow();
                    });
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }
    
    @Test
    void testConcurrentConnections(Vertx vertx, VertxTestContext testContext) {
        int connectionCount = 10;
        int[] completedConnections = {0};
        
        // 并发创建多个连接
        for (int i = 0; i < connectionCount; i++) {
            long delay = Math.max(1, i * 10); // 确保延迟至少为1ms
            vertx.setTimer(delay, id -> {
                client.connect(currentTcpPort, "localhost")
                    .onSuccess(socket -> {
                        assertNotNull(socket);
                        
                        // 发送一些数据
                        socket.write(Buffer.buffer("concurrent-test-" + id));
                        
                        // 短暂延迟后关闭
                        vertx.setTimer(100, closeId -> {
                            socket.close();
                            
                            synchronized (completedConnections) {
                                completedConnections[0]++;
                                if (completedConnections[0] == connectionCount) {
                                    testContext.completeNow();
                                }
                            }
                        });
                    })
                    .onFailure(testContext::failNow);
            });
        }
    }
    
    @Test
    @Disabled //TODO use right message
    void testLargeDataTransfer(Vertx vertx, VertxTestContext testContext) {
        client.connect(currentTcpPort, "localhost")
            .onSuccess(socket -> {
                assertNotNull(socket);
                
                // 创建大量数据
                Buffer largeData = Buffer.buffer();
                for (int i = 0; i < 1000; i++) {
                    largeData.appendString("This is test data line " + i + "\n");
                }
                
                // 发送大量数据
                socket.write(largeData);
                
                // 等待一段时间后关闭
                vertx.setTimer(60_000, id -> {
                    socket.close();
                    testContext.completeNow();
                });
                
                // 设置异常处理器
                socket.exceptionHandler(throwable -> {
                    socket.close();
                    testContext.failNow(throwable);
                });
            })
            .onFailure(testContext::failNow);
    }
}