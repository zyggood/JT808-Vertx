# JT808-Vertx 项目开发规范

## 代码规范

### 命名规范

1. **包命名**
   - 采用小写字母，使用点分隔
   - 基础包名：`io.github.username.jt808vertx`

2. **类命名**
   - 采用大驼峰命名法（UpperCamelCase）
   - 类名应当是名词或名词短语
   - 测试类以Test结尾

3. **方法命名**
   - 采用小驼峰命名法（lowerCamelCase）
   - 方法名应当是动词或动词短语

4. **变量命名**
   - 采用小驼峰命名法
   - 常量使用大写字母，单词间用下划线分隔

5. **协议消息类命名**
   - 使用字母T加消息ID，例如：`T0001`、`T8103`

### 编码规范

1. **文件编码**：UTF-8

2. **缩进**：使用4个空格进行缩进，不使用Tab

3. **行宽**：每行不超过120个字符

4. **注释**
   - 类注释：描述类的用途和功能
   - 方法注释：描述方法的功能、参数和返回值
   - 复杂逻辑注释：对复杂逻辑进行必要的注释说明

5. **异常处理**
   - 不捕获异常后不处理或只打印日志
   - 尽量使用具体的异常类型而非通用Exception
   - 使用自定义异常表达业务异常

## 项目结构规范

### 模块划分

1. **jt808-protocol**：协议定义和编解码
   - `io.github.username.jt808vertx.protocol.message`：消息定义
   - `io.github.username.jt808vertx.protocol.codec`：编解码器
   - `io.github.username.jt808vertx.protocol.common`：公共定义

2. **jt808-server**：服务器实现
   - `io.github.username.jt808vertx.server.verticle`：Vert.x Verticle
   - `io.github.username.jt808vertx.server.handler`：消息处理器
   - `io.github.username.jt808vertx.server.session`：会话管理
   - `io.github.username.jt808vertx.server.web`：Web接口

3. **jt808-common**：公共模块
   - `io.github.username.jt808vertx.common.util`：工具类
   - `io.github.username.jt808vertx.common.model`：通用模型

### 资源文件组织

1. **配置文件**：放置在`src/main/resources`目录下
   - `config.json`：Vert.x配置文件
   - `logback.xml`：日志配置文件

2. **静态资源**：放置在`src/main/resources/webroot`目录下

## 开发流程规范

1. **版本控制**
   - 使用Git进行版本控制
   - 遵循GitFlow工作流
   - 提交信息格式：`[模块] 操作：详细描述`

2. **分支管理**
   - `main`：主分支，保持稳定可发布状态
   - `develop`：开发分支，最新开发代码
   - `feature/*`：功能分支，用于开发新功能
   - `bugfix/*`：修复分支，用于修复bug

3. **代码审查**
   - 所有代码合并到develop或main前必须经过代码审查
   - 关注代码质量、性能和安全性

4. **测试要求**
   - 单元测试覆盖率不低于80%
   - 核心功能必须有集成测试
   - 提交前必须通过所有测试

## 文档规范

1. **代码文档**
   - 使用Javadoc注释
   - 关键类和方法必须有文档注释

2. **项目文档**
   - README.md：项目概述和快速开始
   - CONTRIBUTING.md：贡献指南
   - CHANGELOG.md：版本变更记录

3. **API文档**
   - 使用OpenAPI规范
   - 每个API端点必须有描述、参数说明和返回值说明

## 性能规范

1. **响应时间**
   - API响应时间不超过200ms
   - 消息处理延迟不超过10ms

2. **资源使用**
   - 内存泄漏零容忍
   - CPU使用率峰值不超过70%

3. **并发处理**
   - 正确使用Vert.x的异步模型
   - 避免阻塞Event Loop

## 安全规范

1. **数据安全**
   - 敏感数据加密存储
   - 传输数据使用TLS/SSL

2. **认证与授权**
   - API访问必须进行身份验证
   - 实施最小权限原则

3. **输入验证**
   - 所有外部输入必须验证
   - 防止SQL注入、XSS等常见攻击