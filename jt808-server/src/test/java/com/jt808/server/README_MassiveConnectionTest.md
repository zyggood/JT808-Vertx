# JT808 万级设备连接测试指南

## 概述

`JT808MassiveConnectionTest` 是专门用于测试JT808-Vertx服务器支持万级设备同时在线能力的测试套件。该测试验证单节点服务器在高并发场景下的性能表现。

## 测试场景

### 1. 连接规模测试
- **1K设备连接测试** - 验证基础连接能力
- **5K设备连接测试** - 验证中等规模连接处理
- **10K设备连接测试** - 验证万级设备连接目标
- **15K设备压力测试** - 极限压力测试（默认禁用）

### 2. 性能测试
- **10K设备并发消息处理测试** - 验证万级设备同时发送消息的处理能力
- **长时间稳定性测试** - 验证5K设备持续在线5分钟的稳定性

## 系统要求

### 硬件要求
- **内存**: 最少8GB，推荐16GB+
- **CPU**: 4核心以上，推荐8核心+
- **网络**: 千兆网卡

### 操作系统配置

#### 1. 文件描述符限制
```bash
# 临时设置（当前会话有效）
ulimit -n 65536

# 永久设置（需要重启）
echo "* soft nofile 65536" >> /etc/security/limits.conf
echo "* hard nofile 65536" >> /etc/security/limits.conf
```

#### 2. 网络参数优化
```bash
# 增加TCP连接队列大小
echo 'net.core.somaxconn = 65536' >> /etc/sysctl.conf
echo 'net.ipv4.tcp_max_syn_backlog = 65536' >> /etc/sysctl.conf

# 优化端口范围
echo 'net.ipv4.ip_local_port_range = 1024 65535' >> /etc/sysctl.conf

# 应用配置
sudo sysctl -p
```

### JVM配置

#### 推荐JVM参数
```bash
# 基础配置（8GB内存系统）
-Xmx4g -Xms2g

# 高性能配置（16GB+内存系统）
-Xmx8g -Xms4g

# GC优化
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UnlockExperimentalVMOptions
-XX:+UseZGC  # Java 11+

# 网络优化
-Djava.net.preferIPv4Stack=true
-Dio.netty.allocator.maxOrder=11
```

## 运行测试

### 1. 准备环境
```bash
# 检查文件描述符限制
ulimit -n

# 检查可用内存
free -h

# 检查CPU核心数
nproc
```

### 2. 运行特定测试
```bash
# 运行1K设备连接测试
mvn test -Dtest=JT808MassiveConnectionTest#test1KDeviceConnections

# 运行5K设备连接测试
mvn test -Dtest=JT808MassiveConnectionTest#test5KDeviceConnections

# 运行10K设备连接测试
mvn test -Dtest=JT808MassiveConnectionTest#test10KDeviceConnections

# 运行10K设备并发消息处理测试
mvn test -Dtest=JT808MassiveConnectionTest#test10KDeviceConcurrentMessaging

# 运行稳定性测试
mvn test -Dtest=JT808MassiveConnectionTest#testLongTermStability5KDevices
```

### 3. 运行完整测试套件
```bash
# 运行所有测试（不包括压力测试）
mvn test -Dtest=JT808MassiveConnectionTest

# 包括压力测试（需要大量资源）
mvn test -Dtest=JT808MassiveConnectionTest -Dtest.include.stress=true
```

## 性能指标

### 连接性能指标
- **连接成功率**: ≥95%
- **连接建立速度**: 目标 >100 连接/秒
- **内存使用**: 监控内存增长趋势

### 消息处理性能指标
- **总QPS**: 10K设备测试 >1000 QPS
- **平均延迟**: <100ms
- **错误率**: <1%

### 稳定性指标
- **心跳成功率**: ≥90%
- **连接保持率**: ≥95%
- **内存稳定性**: 无明显内存泄漏

## 监控和调试

### 1. 实时监控
```bash
# 监控连接数
netstat -an | grep :17611 | wc -l

# 监控内存使用
top -p $(pgrep java)

# 监控文件描述符使用
lsof -p $(pgrep java) | wc -l
```

### 2. JVM监控
```bash
# 使用jstat监控GC
jstat -gc $(pgrep java) 1s

# 使用jmap分析内存
jmap -histo $(pgrep java)

# 生成堆转储（如果出现内存问题）
jmap -dump:format=b,file=heap.hprof $(pgrep java)
```

### 3. 应用日志
测试过程中会输出详细的性能统计信息：
- 连接建立进度
- 性能测试结果
- 内存使用情况
- 错误统计

## 故障排除

### 常见问题

#### 1. 连接失败率高
**可能原因**:
- 文件描述符限制不足
- 系统负载过高
- 网络配置问题

**解决方案**:
```bash
# 检查并增加文件描述符限制
ulimit -n 65536

# 检查系统负载
top
iostat 1

# 检查网络连接
ss -tuln | grep 17611
```

#### 2. 内存不足
**症状**: OutOfMemoryError 或测试中断

**解决方案**:
```bash
# 增加JVM堆内存
export MAVEN_OPTS="-Xmx8g -Xms4g"

# 或者减少测试规模
# 修改测试类中的常量值
```

#### 3. 测试超时
**可能原因**:
- 系统性能不足
- 网络延迟高
- 服务器响应慢

**解决方案**:
- 增加测试超时时间
- 减少并发连接数
- 优化系统配置

### 性能调优建议

#### 1. Vert.x配置优化
```java
// 在测试类中调整VertxOptions
VertxOptions options = new VertxOptions()
    .setEventLoopPoolSize(Runtime.getRuntime().availableProcessors() * 4)
    .setWorkerPoolSize(100)
    .setInternalBlockingPoolSize(100)
    .setMaxEventLoopExecuteTime(5000000000L);
```

#### 2. 网络配置优化
```java
// 优化NetServerOptions
NetServerOptions serverOptions = new NetServerOptions()
    .setTcpKeepAlive(true)
    .setTcpNoDelay(true)
    .setReuseAddress(true)
    .setAcceptBacklog(8192)
    .setReceiveBufferSize(65536)
    .setSendBufferSize(65536);
```

## 测试结果分析

### 预期性能基准
基于典型硬件配置（8核CPU，16GB内存）的预期结果：

| 测试场景 | 连接数 | 预期成功率 | 预期QPS | 预期内存使用 |
|---------|--------|------------|---------|-------------|
| 1K设备 | 1,000 | >99% | >200 | <500MB |
| 5K设备 | 5,000 | >98% | >800 | <1.5GB |
| 10K设备 | 10,000 | >95% | >1000 | <3GB |
| 15K设备 | 15,000 | >90% | >1200 | <4GB |

### 性能瓶颈分析
1. **CPU瓶颈**: 消息编解码和网络I/O处理
2. **内存瓶颈**: 连接会话存储和消息缓冲
3. **网络瓶颈**: 网络带宽和连接数限制
4. **系统瓶颈**: 文件描述符和内核参数限制

## 生产环境建议

基于测试结果，对生产环境部署的建议：

1. **单节点容量**: 建议单节点支持5K-8K设备，保留性能余量
2. **集群部署**: 超过8K设备建议使用集群部署
3. **负载均衡**: 使用一致性哈希实现设备到节点的固定映射
4. **监控告警**: 设置连接数、QPS、内存使用的监控告警
5. **容量规划**: 按照峰值流量的1.5-2倍进行容量规划

## 注意事项

1. **资源消耗**: 大规模测试会消耗大量系统资源，建议在专门的测试环境运行
2. **测试时间**: 完整测试套件可能需要30-60分钟
3. **网络影响**: 测试会产生大量网络连接，可能影响其他网络服务
4. **清理工作**: 测试结束后会自动清理连接，但建议检查是否有残留进程
5. **并发限制**: 避免同时运行多个大规模测试，可能导致系统不稳定