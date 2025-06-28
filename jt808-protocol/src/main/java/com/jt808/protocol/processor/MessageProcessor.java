package com.jt808.protocol.processor;

import com.jt808.protocol.message.JT808Message;
import io.vertx.core.Future;

/**
 * 消息处理器接口
 * 支持链式处理和中间件模式
 */
public interface MessageProcessor {
    
    /**
     * 处理消息
     * @param context 处理上下文
     * @return 处理结果的Future
     */
    Future<ProcessResult> process(ProcessContext context);
    
    /**
     * 获取处理器名称
     */
    String getName();
    
    /**
     * 获取处理器优先级，数值越小优先级越高
     */
    default int getPriority() {
        return 100;
    }
    
    /**
     * 判断是否可以处理指定类型的消息
     */
    default boolean canProcess(JT808Message message) {
        return true;
    }
    
    /**
     * 处理器是否为异步处理
     */
    default boolean isAsync() {
        return true;
    }
}