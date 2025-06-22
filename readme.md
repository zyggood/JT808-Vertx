# JT808-Vertx

基于Vert.x框架实现的JT/T 808协议网关，支持TCP/UDP通信，提供高并发Web接口服务。

## 项目特性

- 🚀 **高性能**: 基于Vert.x的事件驱动、非阻塞I/O模型
- 📡 **多协议支持**: 支持JT/T 808-2011/2013/2019、JT/T 1078-2016
- 🔧 **可扩展**: 模块化设计，易于扩展新的协议版本和功能
- 💾 **多数据库**: 支持H2、MySQL、PostgreSQL
- 🌐 **RESTful API**: 提供标准化的Web接口
- 📊 **实时监控**: 设备连接状态和消息流的实时监控
- 🔌 **WebSocket**: 实时推送设备消息和状态变更
- ☁️ **集群支持**: 基于Vert.x的集群功能，支持水平扩展

## 技术栈

- **JDK**: OpenJDK 21
- **框架**: Vert.x 4.x
- **构建工具**: Maven
- **数据库**: H2/MySQL/PostgreSQL
- **API文档**: OpenAPI 3.0 (Swagger)

## 项目结构

```
jt808-vertx/
├── jt808-common/          # 公共模块
│   ├── src/main/java/
│   │   └── com/jt808/common/
│   │       ├── JT808Constants.java     # 协议常量
│   │       ├── exception/              # 异常定义
│   │       └── util/                   # 工具类
│   └── pom.xml
├── jt808-protocol/        # 协议编解码模块
│   ├── src/main/java/
│   │   └── com/jt808/protocol/
│   │       ├── message/                # 消息定义
│   │       └── codec/                  # 编解码器
│   └── pom.xml
├── jt808-server/          # 服务器模块
│   ├── src/main/java/
│   │   └── com/jt808/server/
│   │       ├── JT808Server.java        # 主服务器类
│   │       ├── session/                # 会话管理
│   │       └── handler/                # 消息处理器
│   ├── src/main/resources/
│   │   ├── application.json            # 应用配置
│   │   └── logback.xml                 # 日志配置
│   └── pom.xml
└── pom.xml                # 根POM文件
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+

### 编译项目

```bash
# 克隆项目
git clone <repository-url>
cd jt808-vertx

# 编译项目
mvn clean compile
```

### 运行服务器

```bash
# 运行服务器
mvn -pl jt808-server exec:java -Dexec.mainClass="com.jt808.server.JT808Server"

# 或者使用Vert.x插件运行
mvn -pl jt808-server vertx:run
```

### 配置说明

服务器配置文件位于 `jt808-server/src/main/resources/application.json`：

```json
{
  "tcp": {
    "port": 7611,
    "host": "0.0.0.0"
  },
  "udp": {
    "port": 7612,
    "host": "0.0.0.0"
  },
  "http": {
    "port": 8080,
    "host": "0.0.0.0"
  },
  "database": {
    "type": "h2",
    "url": "jdbc:h2:./data/jt808;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1"
  }
}
```

## 性能目标

- 单节点支持10,000+设备同时在线
- 消息处理延迟<10ms
- 支持每秒1,000+消息的处理能力
- 系统资源占用低，适合在容器环境中运行

## 开发计划

### 阶段一：基础框架搭建 ✅
- [x] 项目结构设计
- [x] 核心组件实现
- [x] 基本协议支持

### 阶段二：功能完善
- [ ] 完整协议支持
- [ ] Web管理界面
- [ ] 监控和统计功能

### 阶段三：性能优化和测试
- [ ] 性能测试和调优
- [ ] 压力测试
- [ ] 稳定性测试

### 阶段四：文档和示例
- [ ] API文档完善
- [ ] 使用示例
- [ ] 部署文档

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue
- 发送邮件
- 加入讨论群

## 特性

- 基于Vert.x 4.x的高性能、非阻塞I/O实现
- 支持JT/T 808-2011/2013/2019协议
- 支持JT/T 1078-2016协议
- 支持苏标、粤标扩展协议
- 支持TCP和UDP协议
- 提供RESTful API接口
- 实时消息推送（WebSocket）
- 可独立部署，也可嵌入到现有系统
- 支持集群部署

## 快速开始

### 环境要求

- OpenJDK 21+
- Maven 3.8+

### 构建项目

```bash
mvn clean package