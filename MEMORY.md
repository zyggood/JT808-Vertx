# JT808-Vertx 项目记忆文档

> **重要提示**: 所有编码会话必须优先参考此文档，并将新发现的架构规则实时更新到该文件

## 技术栈约束

### 核心技术栈
- **JDK版本**: OpenJDK 21 (严格要求)
- **框架**: Vert.x 4.5.1 (事件驱动、非阻塞I/O)
- **构建工具**: Maven 3.8+
- **编码**: UTF-8
- **协议支持**: JT/T 808-2011/2013/2019、JT/T 1078-2016

### 依赖版本管理
```xml
<properties>
    <vertx.version>4.5.1</vertx.version>
    <junit.version>5.10.1</junit.version>
    <logback.version>1.4.14</logback.version>
    <jackson.version>2.16.1</jackson.version>
    <mockito.version>5.8.0</mockito.version>
</properties>
```

### 关键依赖
- **Vert.x Stack**: 使用BOM管理版本
- **Jackson**: 2.16.1 (JSON处理)
- **Logback**: 1.4.14 (日志框架)
- **JUnit 5**: 5.10.1 (单元测试)
- **Mockito**: 5.8.0 (Mock测试)

## 项目结构图

```
jt808-vertx/
├── pom.xml                           # 根POM，定义版本管理
├── MEMORY.md                         # 项目记忆文档 (本文件)
├── OPTIMIZATION_GUIDE.md             # 优化指南
├── project.md                        # 项目设计文档
├── readme.md                         # 项目说明
├── doc/                              # 协议文档
│   ├── JT808消息架构设计文档.md
│   ├── JT808组件使用指南.md
│   └── JTT 808-*.pdf                # 协议标准文档
├── jt808-common/                     # 公共模块
│   ├── pom.xml
│   └── src/main/java/com/jt808/common/
│       ├── exception/               # 异常定义
│       └── util/                    # 工具类
├── jt808-protocol/                   # 协议编解码模块 (核心)
│   ├── pom.xml
│   └── src/main/java/com/jt808/protocol/
│       ├── codec/                   # 编解码器
│       │   ├── JT808Decoder.java
│       │   └── JT808Encoder.java
│       ├── common/                  # 公共定义
│       │   └── AlarmType.java       # 报警类型枚举
│       ├── factory/                 # 工厂模式
│       │   └── JT808MessageFactory.java  # 消息工厂 (单例)
│       ├── message/                 # 消息定义
│       │   ├── JT808Message.java    # 消息基类 (抽象)
│       │   ├── JT808Header.java     # 消息头
│       │   ├── T0xxx*.java          # 终端消息
│       │   └── T8xxx*.java          # 平台消息
│       ├── processor/               # 处理器链模式
│       │   ├── MessageProcessor.java
│       │   └── MessageProcessorChain.java
│       ├── validator/               # 验证器
│       │   ├── MessageValidator.java
│       │   └── ValidationChain.java
│       ├── util/                    # 协议工具类
│       │   ├── ChecksumUtils.java
│       │   └── EscapeUtils.java
│       └── version/                 # 版本管理
│           ├── ProtocolVersion.java
│           └── VersionCompatibilityManager.java
└── jt808-server/                     # 服务器模块
    ├── pom.xml
    └── src/main/java/com/jt808/server/
        ├── verticle/                # Vert.x Verticle
        ├── handler/                 # 消息处理器
        ├── session/                 # 会话管理
        └── web/                     # Web接口
```

## 核心架构约束

### 1. 消息工厂模式 (关键约束)
```java
// ❌ 错误用法 - 构造函数是私有的
JT808MessageFactory factory = new JT808MessageFactory();

// ✅ 正确用法 - 使用单例
JT808MessageFactory factory = JT808MessageFactory.getInstance();
```

### 2. 消息基类设计
- **JT808Message**: 抽象基类，所有消息必须继承
- **必须实现的抽象方法**:
  - `getMessageId()`: 返回消息ID
  - `encodeBody()`: 编码消息体
  - `decodeBody(Buffer body)`: 解码消息体

### 3. 消息命名规范
- **终端消息**: `T0xxx` (如 T0001, T0200, T0201)
- **平台消息**: `T8xxx` (如 T8001, T8100, T8201)
- **测试类**: `消息类名 + Test`
- **示例类**: `消息类名 + Example`

### 4. Buffer使用约束
- **数据类型**: 统一使用 `io.vertx.core.buffer.Buffer`
- **字节序**: 网络字节序 (大端序)
- **编解码**: 使用Buffer的 `appendXxx()` 和 `getXxx()` 方法

### 5. 工具类使用规范
```java
// 校验码计算
ChecksumUtils.calculateChecksum(buffer);

// 转义处理
EscapeUtils.escape(buffer);    // 编码时转义
EscapeUtils.unescape(buffer);  // 解码时反转义
```

## 历史踩坑记录

### 1. JT808MessageFactory 访问控制问题
**问题**: 尝试使用 `new JT808MessageFactory()` 创建实例
**错误信息**: `JT808MessageFactory() has private access in JT808MessageFactory`
**解决方案**: 使用 `JT808MessageFactory.getInstance()` 获取单例
**影响文件**: 所有使用消息工厂的代码

### 2. 字节序问题
**问题**: 应答流水号 99999 被解码为 34463
**原因**: WORD类型值超出范围 (0-65535)，导致字节序错误
**解决方案**: 使用合适范围内的值进行测试 (如 54321)
**教训**: 测试数据必须符合协议规范的数据类型范围

### 3. equals/hashCode 空值处理
**问题**: 测试中 `locationReport` 为 `null` 时 equals 方法失败
**原因**: 构造函数会自动创建新实例，即使传入 `null`
**解决方案**: 先创建对象，再通过 setter 方法设置 `null` 值
```java
// ❌ 错误方式
T0201PositionInfoQueryResponse response = new T0201PositionInfoQueryResponse(12345, null);

// ✅ 正确方式
T0201PositionInfoQueryResponse response = new T0201PositionInfoQueryResponse();
response.setResponseSerialNumber(12345);
response.setLocationReport(null);
```

### 4. 测试数据范围约束
**约束**: 
- WORD类型: 0-65535
- DWORD类型: 0-4294967295
- BCD码: 必须是有效的BCD格式
**建议**: 使用协议规范内的合理测试数据

### 5. 工厂编码消息头依赖问题
**问题**: `T8202` 示例中使用 `factory.encodeMessage()` 时出现空指针异常，因为消息头未初始化
**解决方案**: 在示例中只演示消息体编码，避免依赖完整消息头
**教训**: 工厂的 `encodeMessage()` 需要完整的消息对象（包括消息头），示例代码应明确这一点

### 6. WORD/DWORD 数据类型处理
**经验**: `T8202` 实现中正确处理了 WORD(2字节) 和 DWORD(4字节) 数据类型
**方法**: 使用 `appendUnsignedShort()` 处理 WORD，`appendUnsignedInt()` 处理 DWORD
**注意**: 边界值测试要覆盖数据类型的最大值（WORD: 65535, DWORD: 4294967295）

### 7. T8203 位标志处理经验
**问题**: T8203消息需要处理报警类型的位标志
**解决方案**: 使用常量定义各个位的含义，提供位操作方法
**教训**: 位标志处理需要提供人性化的描述方法，便于调试和理解
**实现要点**: 
- 消息体固定6字节（WORD + DWORD）
- 对流水号和报警类型进行范围验证
- 提供静态工厂方法创建常用消息

### 8. T8300 文本信息下发消息实现经验
**问题**: T8300消息需要处理GBK编码的文本内容和复杂的标志位
**解决方案**: 正确处理GBK字符集编码，设计清晰的标志位检查方法
**教训**: 中文字符编码需要特别注意字节长度计算，标志位组合需要提供描述方法
**实现要点**:
- 文本内容使用GBK编码，最长1024字节
- 标志位设计要提供便捷的检查方法
- 提供针对不同场景的工厂方法（紧急文本、普通文本、CAN故障码等）
- 编解码一致性测试和边界值测试必不可少

### 9. T0200 位置信息报告延迟解析优化
**问题**: `setAdditionalInfo` 方法每次调用都会立即解析附加信息，在高频场景下成为性能瓶颈
**解决方案**: 实现延迟解析机制，只在首次访问 `getParsedAdditionalInfo()` 时才执行解析
**性能提升**: 编解码性能显著提升，特别是在大量位置报告处理场景下
**实现要点**:
- `setAdditionalInfo()` 只设置原始数据，将 `parsedAdditionalInfo` 置为 `null`
- `getParsedAdditionalInfo()` 检查是否已解析，未解析则执行解析并缓存结果
- 构造函数中 `parsedAdditionalInfo` 初始化为 `null` 而非空 `HashMap`
- `decodeBody()` 方法中移除立即解析逻辑，改为延迟解析
- 保持向后兼容性，API接口不变
**测试验证**: 通过 `LazyParsingTest` 验证延迟解析的正确性和性能

### 10. T8303 信息点播菜单设置消息实现经验

**问题**: T8303消息需要处理可变长度的信息项列表和GBK编码的信息名称
**解决方案**: 设计灵活的信息项管理机制，正确处理GBK编码和长度限制
**教训**: 可变长度列表消息需要特别注意编解码的一致性和边界条件处理
**实现要点**:

- 信息名称使用GBK编码，最长65535字节，需要在设置时进行长度验证
- 设置类型为0（删除全部）时，消息体只包含设置类型字节，无后续内容
- 信息项列表支持动态添加、删除、查找操作，提供便捷的管理方法
- 提供针对不同设置类型的静态工厂方法（删除全部、更新、追加、修改）
- 编解码时需要正确处理信息项总数和每个信息项的结构（类型+名称长度+名称内容）
- 测试覆盖边界情况：空信息名称、超长信息名称、无效消息体等
- 类型转换注意事项：测试中int类型需要显式转换为byte类型，避免编译错误
  **性能考虑**: 信息项列表使用ArrayList实现，支持高效的随机访问和动态扩容

### 10. T0301 事件报告消息实现经验

**需求**: 实现消息ID为 0x0301 的事件报告消息，消息体只包含1字节的事件ID
**实现要点**:

- 消息体结构简单：仅包含1字节事件ID (BYTE类型)
- 提供无符号值获取方法 `getEventIdUnsigned()` 处理字节的符号扩展问题
- 静态工厂方法支持常见事件类型（紧急报警、故障报警等）
- 完整的编解码实现，包括边界值测试（0x00, 0xFF）
  **集成要点**:
- 在 `JT808Decoder.createMessage()` 中添加 0x0301 消息支持
- 在 `JT808MessageFactory.initMessageCreators()` 中注册消息创建器
- 消息工厂测试验证创建、编解码和集成功能
  **文件清单**:
- `T0301EventReport.java` - 消息实现
- `T0301EventReportTest.java` - 单元测试
- `T0301EventReportExample.java` - 使用示例
- `JT808Decoder.java` - 添加解码支持
- `JT808MessageFactory.java` - 添加工厂支持
- `JT808MessageFactoryTest.java` - 更新工厂测试
  **协议关联**: 与 T8301 事件设置消息配对使用，实现事件的预设和报告机制

### 11. T8302 提问下发消息实现经验

**需求**: 实现消息ID为 0x8302 的提问下发消息，支持复杂的消息体结构和GBK编码
**实现要点**:

- **消息体结构**: 标志位(1字节) + 问题内容长度(1字节) + 问题内容(GBK) + 候选答案列表
- **标志位处理**: 定义QuestionFlag内部类，提供位运算检查方法和描述功能
- **GBK编码处理**: 正确处理中文字符编码，验证长度限制（问题255字节，答案65535字节）
- **答案列表设计**: Answer内部类表示候选答案，支持动态添加和有符号/无符号ID访问
- **工厂方法**: 提供createEmergencyQuestion()和createNormalQuestion()便捷创建方法
  **集成要点**:
- 在 `JT808Decoder.createMessage()` 中添加 0x8302 消息支持
- 在 `JT808MessageFactory.initMessageCreators()` 中注册消息创建器
- 完整的编解码一致性测试和边界值验证
  **文件清单**:
- `T8302QuestionDistribution.java` - 消息实现
- `T8302QuestionDistributionTest.java` - 完整单元测试
- `T8302QuestionDistributionExample.java` - 使用示例
- `JT808Decoder.java` - 添加解码支持
- `JT808MessageFactory.java` - 添加工厂支持
- `JT808MessageFactoryTest.java` - 更新工厂测试
  **协议关联**: 平台向终端发送带候选答案的提问，需要终端回复通用应答消息
  **技术特点**: 协议兼容、类型安全、编码安全、易用性强、可维护性好

## 编码规范约束

### 1. 包命名规范
```
com.jt808.protocol.message     # 消息定义
com.jt808.protocol.codec       # 编解码器
com.jt808.protocol.factory     # 工厂类
com.jt808.protocol.util        # 工具类
com.jt808.protocol.validator   # 验证器
com.jt808.protocol.processor   # 处理器
```

### 2. 异步编程约束
- **必须使用**: Vert.x的 `Future` 和 `Promise`
- **禁止阻塞**: Event Loop线程
- **耗时操作**: 使用 `vertx.executeBlocking()`

### 3. 日志规范
- **框架**: Logback
- **格式**: 结构化日志 (JSON格式)
- **级别**: DEBUG < INFO < WARN < ERROR

### 4. 测试约束
- **覆盖率**: 不低于80%
- **框架**: JUnit 5 + Mockito
- **命名**: 测试方法使用 `test` 前缀

## 性能约束

### 1. 响应时间目标
- **API响应**: < 200ms
- **消息处理**: < 10ms
- **编解码**: < 5ms

### 2. 并发支持
- **单节点**: 10,000+ 设备同时在线
- **消息吞吐**: 1,000+ 消息/秒
- **内存使用**: 避免内存泄漏

### 3. 资源管理
- **Buffer**: 及时释放
- **连接**: 使用连接池
- **线程**: 合理配置线程池大小

## 扩展指南

### 1. 添加新消息类型
1. 继承 `JT808Message` 抽象类
2. 实现三个抽象方法
3. 在 `JT808MessageFactory` 中注册
4. 编写单元测试
5. 创建使用示例

### 2. 添加新的报警类型
1. 在 `AlarmType` 枚举中添加
2. 更新位标志位定义
3. 添加描述信息

### 3. 自定义处理器
1. 实现 `MessageProcessor` 接口
2. 设置合适的优先级
3. 考虑异步处理需求

## 调试技巧

### 1. 消息编解码调试
```java
// 启用详细日志
System.setProperty("jt808.debug", "true");

// 打印Buffer内容
System.out.println("Buffer: " + buffer.toString());
System.out.println("Hex: " + buffer.toString("hex"));
```

### 2. 常用调试命令
```bash
# 运行特定测试
mvn test -Dtest=T0201*

# 跳过测试构建
mvn clean package -DskipTests

# 查看依赖树
mvn dependency:tree
```

## 更新记录

- **2024-01-XX**: 初始创建，基于T0201消息实现经验
- **待更新**: 根据后续开发经验持续更新

---

> **注意**: 此文档应在每次遇到新的架构约束或踩坑经验时及时更新，确保团队开发的一致性和效率。