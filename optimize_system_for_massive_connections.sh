#!/bin/bash

# JT808-Vertx 大规模连接测试系统优化脚本
# 用于优化macOS系统参数以支持10K+并发连接测试

echo "=== JT808-Vertx 大规模连接测试系统优化 ==="
echo "正在检查和优化系统参数..."

# 检查当前文件描述符限制
echo "\n1. 检查文件描述符限制:"
echo "当前软限制: $(ulimit -n)"
echo "当前硬限制: $(ulimit -Hn)"

# 临时增加文件描述符限制
echo "\n2. 临时增加文件描述符限制到65536:"
ulimit -n 65536
echo "新的软限制: $(ulimit -n)"

# 检查网络参数
echo "\n3. 检查关键网络参数:"
echo "TCP连接队列大小 (net.core.somaxconn): $(sysctl -n net.inet.tcp.somaxconn 2>/dev/null || echo '不支持')"
echo "TCP端口范围: $(sysctl -n net.inet.ip.portrange.first 2>/dev/null || echo '不支持') - $(sysctl -n net.inet.ip.portrange.last 2>/dev/null || echo '不支持')"

# macOS特定优化建议
echo "\n4. macOS系统优化建议:"
echo "   - 当前脚本已临时设置文件描述符限制为65536"
echo "   - 如需永久设置，请编辑 /etc/security/limits.conf (需要root权限)"
echo "   - 建议在测试前关闭不必要的应用程序释放系统资源"

# JVM参数建议
echo "\n5. JVM优化参数建议:"
echo "   建议使用以下JVM参数运行测试:"
echo "   -Xmx4g -Xms2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
echo "   -XX:+UnlockExperimentalVMOptions -XX:+UseZGC (Java 11+)"
echo "   -Dvertx.options.maxEventLoopExecuteTime=2000000000"

# 监控建议
echo "\n6. 测试期间监控建议:"
echo "   - 使用 'lsof -p <pid> | wc -l' 监控进程文件描述符使用"
echo "   - 使用 'netstat -an | grep ESTABLISHED | wc -l' 监控活跃连接数"
echo "   - 使用 'top' 或 'htop' 监控CPU和内存使用"

# 测试前检查
echo "\n7. 测试前最终检查:"
echo "   ✓ 文件描述符限制: $(ulimit -n)"
echo "   ✓ 可用内存: $(vm_stat | grep 'Pages free' | awk '{print $3}' | sed 's/\.//' | awk '{print $1*4096/1024/1024 " MB"}' 2>/dev/null || echo '请手动检查')"
echo "   ✓ Java版本: $(java -version 2>&1 | head -n 1 || echo '未安装Java')"

echo "\n=== 优化完成 ==="
echo "现在可以运行大规模连接测试了！"
echo "\n运行测试命令示例:"
echo "cd jt808-server && mvn test -Dtest=JT808MassiveConnectionTest#test10KDeviceConnections"
echo "\n注意: 此脚本的优化仅在当前shell会话中有效"
echo "如需永久优化，请参考 README_MassiveConnectionTest.md 文档"