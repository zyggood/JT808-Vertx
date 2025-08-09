package com.jt808.server;

import com.jt808.common.JT808Constants;
import com.jt808.server.handler.JT808MessageHandler;
import com.jt808.server.session.SessionManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JT808服务器主类
 */
public class JT808Server extends AbstractVerticle {
    
    private static final Logger logger = LoggerFactory.getLogger(JT808Server.class);
    
    private NetServer tcpServer;
    private NetServer udpServer;
    private SessionManager sessionManager;
    private JT808MessageHandler messageHandler;
    
    public static void main(String[] args) {
        // 配置Vert.x选项
        VertxOptions options = new VertxOptions()
                .setEventLoopPoolSize(Runtime.getRuntime().availableProcessors() * 2)
                .setWorkerPoolSize(20)
                .setInternalBlockingPoolSize(20);
        
        Vertx vertx = Vertx.vertx(options);
        
        // 部署服务器
        vertx.deployVerticle(new JT808Server())
                .onSuccess(id -> logger.info("JT808服务器启动成功，部署ID: {}", id))
                .onFailure(throwable -> {
                    logger.error("JT808服务器启动失败", throwable);
                    vertx.close();
                });
    }
    
    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("正在启动JT808服务器...");
        
        // 初始化组件
        sessionManager = new SessionManager(vertx);
        messageHandler = new JT808MessageHandler(sessionManager);
        
        // 获取配置
        JsonObject config = config();
        int tcpPort = config.getInteger("tcp.port", JT808Constants.DEFAULT_TCP_PORT);
        int udpPort = config.getInteger("udp.port", JT808Constants.DEFAULT_UDP_PORT);
        
        // 启动TCP服务器
        startTcpServer(tcpPort).future()
                .compose(v -> startUdpServer(udpPort).future())
                .onSuccess(v -> {
                    logger.info("JT808服务器启动完成");
                    logger.info("TCP端口: {}", tcpPort);
                    logger.info("UDP端口: {}", udpPort);
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }
    
    @Override
    public void stop(Promise<Void> stopPromise) {
        logger.info("正在停止JT808服务器...");
        
        Promise<Void> tcpClosePromise = Promise.promise();
        Promise<Void> udpClosePromise = Promise.promise();
        
        // 关闭TCP服务器
        if (tcpServer != null) {
            tcpServer.close(tcpClosePromise);
        } else {
            tcpClosePromise.complete();
        }
        
        // 关闭UDP服务器
        if (udpServer != null) {
            udpServer.close(udpClosePromise);
        } else {
            udpClosePromise.complete();
        }
        
        // 等待所有服务器关闭
        Future.all(tcpClosePromise.future(), udpClosePromise.future())
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        logger.info("JT808服务器已停止");
                        stopPromise.complete();
                    } else {
                        logger.error("停止JT808服务器时发生错误", ar.cause());
                        stopPromise.fail(ar.cause());
                    }
                });
    }
    
    /**
     * 启动TCP服务器
     * @param port 端口号
     * @return Future
     */
    private Promise<Void> startTcpServer(int port) {
        Promise<Void> promise = Promise.promise();
        
        NetServerOptions options = new NetServerOptions()
                .setPort(port)
                .setHost("0.0.0.0")
                .setTcpKeepAlive(true)
                .setTcpNoDelay(true)
                .setReuseAddress(true)
                .setReusePort(true)  // 启用端口复用提高并发性能
                .setAcceptBacklog(65536);  // 增加连接队列大小支持大量并发连接
        
        tcpServer = vertx.createNetServer(options);
        
        tcpServer.connectHandler(socket -> {
            logger.debug("新的TCP连接: {}", socket.remoteAddress());
            
            // 创建会话
            String sessionId = sessionManager.createSession(socket);
            
            // 设置数据处理器
            socket.handler(buffer -> {
                try {
                    messageHandler.handleTcpMessage(sessionId, buffer);
                } catch (Exception e) {
                    logger.error("处理TCP消息时发生错误", e);
                }
            });
            
            // 设置异常处理器
            socket.exceptionHandler(throwable -> {
                logger.error("TCP连接异常: {}", socket.remoteAddress(), throwable);
                sessionManager.removeSession(sessionId);
            });
            
            // 设置关闭处理器
            socket.closeHandler(v -> {
                logger.debug("TCP连接关闭: {}", socket.remoteAddress());
                sessionManager.removeSession(sessionId);
            });
        });
        
        tcpServer.listen(ar -> {
            if (ar.succeeded()) {
                logger.info("TCP服务器启动成功，监听端口: {}", port);
                promise.complete();
            } else {
                logger.error("TCP服务器启动失败", ar.cause());
                promise.fail(ar.cause());
            }
        });
        
        return promise;
    }
    
    /**
     * 启动UDP服务器
     * @param port 端口号
     * @return Future
     */
    private Promise<Void> startUdpServer(int port) {
        Promise<Void> promise = Promise.promise();
        
        // UDP服务器实现（简化版）
        vertx.createDatagramSocket()
                .listen(port, "0.0.0.0", ar -> {
                    if (ar.succeeded()) {
                        logger.info("UDP服务器启动成功，监听端口: {}", port);
                        
                        ar.result().handler(packet -> {
                            try {
                                messageHandler.handleUdpMessage(packet);
                            } catch (Exception e) {
                                logger.error("处理UDP消息时发生错误", e);
                            }
                        });
                        
                        promise.complete();
                    } else {
                        logger.error("UDP服务器启动失败", ar.cause());
                        promise.fail(ar.cause());
                    }
                });
        
        return promise;
    }
}