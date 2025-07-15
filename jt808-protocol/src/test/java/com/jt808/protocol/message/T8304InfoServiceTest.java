package com.jt808.protocol.message;

import com.jt808.protocol.factory.JT808MessageFactory;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("T8304信息服务消息测试")
class T8304InfoServiceTest {

    private T8304InfoService message;
    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        message = new T8304InfoService();
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x8304, message.getMessageId());
        assertEquals(0x8304, T8304InfoService.MESSAGE_ID);
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        T8304InfoService msg = new T8304InfoService();
        assertEquals(0, msg.getInfoType());
        assertEquals(0, msg.getInfoLength());
        assertNull(msg.getInfoContent());
    }

    @Test
    @DisplayName("测试带JT808Header的构造函数")
    void testConstructorWithHeader() {
        JT808Header header = new JT808Header();
        header.setMessageId(0x8304);
        header.setPhoneNumber("13800138000");
        
        T8304InfoService msg = new T8304InfoService(header);
        assertEquals(header, msg.getHeader());
        assertEquals(0x8304, msg.getMessageId());
    }

    @Test
    @DisplayName("测试带参数的构造函数")
    void testConstructorWithParameters() {
        String content = "今日天气晴朗";
        T8304InfoService msg = new T8304InfoService((byte) 0x02, content);
        
        assertEquals(0x02, msg.getInfoType());
        assertEquals(content, msg.getInfoContent());
        assertEquals(content.getBytes(Charset.forName("GBK")).length, msg.getInfoLength());
    }

    @Test
    @DisplayName("测试静态工厂方法 - createInfoService")
    void testCreateInfoService() {
        String content = "测试信息";
        T8304InfoService msg = T8304InfoService.createInfoService((byte) 0x01, content);
        
        assertEquals(0x01, msg.getInfoType());
        assertEquals(content, msg.getInfoContent());
        assertEquals(content.getBytes(Charset.forName("GBK")).length, msg.getInfoLength());
    }

    @Test
    @DisplayName("测试静态工厂方法 - createNewsService")
    void testCreateNewsService() {
        String newsContent = "今日新闻：科技发展迅速";
        T8304InfoService msg = T8304InfoService.createNewsService(newsContent);
        
        assertEquals(T8304InfoService.InfoType.NEWS, msg.getInfoType());
        assertEquals(newsContent, msg.getInfoContent());
        assertTrue(msg.isNewsInfo());
    }

    @Test
    @DisplayName("测试静态工厂方法 - createWeatherService")
    void testCreateWeatherService() {
        String weatherContent = "今日天气：晴，25°C";
        T8304InfoService msg = T8304InfoService.createWeatherService(weatherContent);
        
        assertEquals(T8304InfoService.InfoType.WEATHER, msg.getInfoType());
        assertEquals(weatherContent, msg.getInfoContent());
        assertTrue(msg.isWeatherInfo());
    }

    @Test
    @DisplayName("测试静态工厂方法 - createTrafficService")
    void testCreateTrafficService() {
        String trafficContent = "交通状况：主干道畅通";
        T8304InfoService msg = T8304InfoService.createTrafficService(trafficContent);
        
        assertEquals(T8304InfoService.InfoType.TRAFFIC, msg.getInfoType());
        assertEquals(trafficContent, msg.getInfoContent());
        assertTrue(msg.isTrafficInfo());
    }

    @Test
    @DisplayName("测试静态工厂方法 - createStockService")
    void testCreateStockService() {
        String stockContent = "股市行情：上证指数3000点";
        T8304InfoService msg = T8304InfoService.createStockService(stockContent);
        
        assertEquals(T8304InfoService.InfoType.STOCK, msg.getInfoType());
        assertEquals(stockContent, msg.getInfoContent());
        assertTrue(msg.isStockInfo());
    }

    @Test
    @DisplayName("测试编码功能")
    void testEncode() {
        String content = "测试信息内容";
        message.setInfoType((byte) 0x01);
        message.setInfoContent(content);
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        
        // 验证编码结果
        assertEquals(0x01, encoded.getByte(0)); // 信息类型
        
        byte[] contentBytes = content.getBytes(Charset.forName("GBK"));
        int expectedLength = contentBytes.length;
        assertEquals(expectedLength, encoded.getUnsignedShort(1)); // 信息长度
        
        // 验证信息内容
        byte[] actualContent = encoded.getBytes(3, 3 + expectedLength);
        assertArrayEquals(contentBytes, actualContent);
    }

    @Test
    @DisplayName("测试解码功能")
    void testDecode() {
        String content = "解码测试内容";
        byte[] contentBytes = content.getBytes(Charset.forName("GBK"));
        
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x02); // 信息类型
        buffer.appendUnsignedShort(contentBytes.length); // 信息长度
        buffer.appendBytes(contentBytes); // 信息内容
        
        message.decodeBody(buffer);
        
        assertEquals(0x02, message.getInfoType());
        assertEquals(contentBytes.length, message.getInfoLength());
        assertEquals(content, message.getInfoContent());
    }

    @Test
    @DisplayName("测试编解码一致性")
    void testEncodeDecodeConsistency() {
        String content = "编解码一致性测试";
        T8304InfoService original = new T8304InfoService((byte) 0x03, content);
        
        // 编码
        Buffer encoded = original.encodeBody();
        
        // 解码
        T8304InfoService decoded = new T8304InfoService();
        decoded.decodeBody(encoded);
        
        // 验证一致性
        assertEquals(original.getInfoType(), decoded.getInfoType());
        assertEquals(original.getInfoLength(), decoded.getInfoLength());
        assertEquals(original.getInfoContent(), decoded.getInfoContent());
        assertEquals(original, decoded);
    }

    @Test
    @DisplayName("测试空内容处理")
    void testEmptyContent() {
        message.setInfoType((byte) 0x01);
        message.setInfoContent("");
        
        Buffer encoded = message.encodeBody();
        assertEquals(3, encoded.length()); // 1字节类型 + 2字节长度
        assertEquals(0x01, encoded.getByte(0));
        assertEquals(0, encoded.getUnsignedShort(1));
        
        // 解码测试
        T8304InfoService decoded = new T8304InfoService();
        decoded.decodeBody(encoded);
        assertEquals(0x01, decoded.getInfoType());
        assertEquals(0, decoded.getInfoLength());
        assertEquals("", decoded.getInfoContent());
    }

    @Test
    @DisplayName("测试null内容处理")
    void testNullContent() {
        message.setInfoType((byte) 0x01);
        message.setInfoContent(null);
        
        Buffer encoded = message.encodeBody();
        assertEquals(3, encoded.length());
        assertEquals(0x01, encoded.getByte(0));
        assertEquals(0, encoded.getUnsignedShort(1));
    }

    @Test
    @DisplayName("测试GBK编码处理")
    void testGBKEncoding() {
        String chineseContent = "中文测试内容：天气预报";
        message.setInfoType((byte) 0x02);
        message.setInfoContent(chineseContent);
        
        Buffer encoded = message.encodeBody();
        
        T8304InfoService decoded = new T8304InfoService();
        decoded.decodeBody(encoded);
        
        assertEquals(chineseContent, decoded.getInfoContent());
        assertEquals(chineseContent.getBytes(Charset.forName("GBK")).length, decoded.getInfoLength());
    }

    @Test
    @DisplayName("测试无符号值获取")
    void testUnsignedValues() {
        message.setInfoType((byte) 0xFF); // -1 as signed, 255 as unsigned
        
        assertEquals(-1, message.getInfoType()); // signed
        assertEquals(255, message.getInfoTypeUnsigned()); // unsigned
    }

    @Test
    @DisplayName("测试信息类型常量")
    void testInfoTypeConstants() {
        assertEquals(0x01, T8304InfoService.InfoType.NEWS);
        assertEquals(0x02, T8304InfoService.InfoType.WEATHER);
        assertEquals(0x03, T8304InfoService.InfoType.TRAFFIC);
        assertEquals(0x04, T8304InfoService.InfoType.STOCK);
        assertEquals(0x05, T8304InfoService.InfoType.LOTTERY);
        assertEquals(0x06, T8304InfoService.InfoType.ENTERTAINMENT);
        assertEquals(0x07, T8304InfoService.InfoType.ADVERTISEMENT);
        assertEquals(0x08, T8304InfoService.InfoType.OTHER);
    }

    @Test
    @DisplayName("测试信息类型判断方法")
    void testInfoTypeCheckers() {
        // 测试新闻类型
        message.setInfoType(T8304InfoService.InfoType.NEWS);
        assertTrue(message.isNewsInfo());
        assertFalse(message.isWeatherInfo());
        assertFalse(message.isTrafficInfo());
        assertFalse(message.isStockInfo());
        
        // 测试天气类型
        message.setInfoType(T8304InfoService.InfoType.WEATHER);
        assertFalse(message.isNewsInfo());
        assertTrue(message.isWeatherInfo());
        assertFalse(message.isTrafficInfo());
        assertFalse(message.isStockInfo());
        
        // 测试交通类型
        message.setInfoType(T8304InfoService.InfoType.TRAFFIC);
        assertFalse(message.isNewsInfo());
        assertFalse(message.isWeatherInfo());
        assertTrue(message.isTrafficInfo());
        assertFalse(message.isStockInfo());
        
        // 测试股票类型
        message.setInfoType(T8304InfoService.InfoType.STOCK);
        assertFalse(message.isNewsInfo());
        assertFalse(message.isWeatherInfo());
        assertFalse(message.isTrafficInfo());
        assertTrue(message.isStockInfo());
    }

    @Test
    @DisplayName("测试信息类型描述")
    void testInfoTypeDescription() {
        message.setInfoType(T8304InfoService.InfoType.NEWS);
        assertEquals("新闻", message.getInfoTypeDescription());
        
        message.setInfoType(T8304InfoService.InfoType.WEATHER);
        assertEquals("天气", message.getInfoTypeDescription());
        
        message.setInfoType(T8304InfoService.InfoType.TRAFFIC);
        assertEquals("交通", message.getInfoTypeDescription());
        
        message.setInfoType(T8304InfoService.InfoType.STOCK);
        assertEquals("股票", message.getInfoTypeDescription());
        
        message.setInfoType(T8304InfoService.InfoType.LOTTERY);
        assertEquals("彩票", message.getInfoTypeDescription());
        
        message.setInfoType(T8304InfoService.InfoType.ENTERTAINMENT);
        assertEquals("娱乐", message.getInfoTypeDescription());
        
        message.setInfoType(T8304InfoService.InfoType.ADVERTISEMENT);
        assertEquals("广告", message.getInfoTypeDescription());
        
        message.setInfoType(T8304InfoService.InfoType.OTHER);
        assertEquals("其他", message.getInfoTypeDescription());
        
        // 测试未知类型
        message.setInfoType((byte) 0xFF);
        assertEquals("未知类型(255)", message.getInfoTypeDescription());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        message.setInfoType(T8304InfoService.InfoType.NEWS);
        message.setInfoContent("新闻内容");
        
        String result = message.toString();
        assertTrue(result.contains("T8304InfoService"));
        assertTrue(result.contains("infoType=1"));
        assertTrue(result.contains("新闻"));
        assertTrue(result.contains("新闻内容"));
    }

    @Test
    @DisplayName("测试equals和hashCode方法")
    void testEqualsAndHashCode() {
        T8304InfoService msg1 = new T8304InfoService((byte) 0x01, "测试内容");
        T8304InfoService msg2 = new T8304InfoService((byte) 0x01, "测试内容");
        T8304InfoService msg3 = new T8304InfoService((byte) 0x02, "测试内容");
        T8304InfoService msg4 = new T8304InfoService((byte) 0x01, "不同内容");
        
        // 测试equals
        assertEquals(msg1, msg2);
        assertNotEquals(msg1, msg3);
        assertNotEquals(msg1, msg4);
        assertNotEquals(msg1, null);
        assertNotEquals(msg1, "string");
        
        // 测试hashCode
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    @DisplayName("测试异常处理 - 空消息体")
    void testDecodeWithNullBody() {
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(null);
        });
    }

    @Test
    @DisplayName("测试异常处理 - 消息体长度不足")
    void testDecodeWithInsufficientLength() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x01); // 只有1字节，不足3字节
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
    }

    @Test
    @DisplayName("测试异常处理 - 信息内容长度不匹配")
    void testDecodeWithMismatchedContentLength() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x01); // 信息类型
        buffer.appendUnsignedShort(10); // 声明长度为10
        buffer.appendBytes("短内容".getBytes(Charset.forName("GBK"))); // 实际长度不足10
        
        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(buffer);
        });
    }

    @Test
    @DisplayName("测试边界值 - 最大信息长度")
    void testMaxInfoLength() {
        // 创建接近最大长度的内容
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 65535; i++) {
            sb.append("a");
        }
        String maxContent = sb.toString();
        
        message.setInfoType((byte) 0x01);
        message.setInfoContent(maxContent);
        
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        
        T8304InfoService decoded = new T8304InfoService();
        decoded.decodeBody(encoded);
        assertEquals(maxContent, decoded.getInfoContent());
    }

    @Test
    @DisplayName("测试消息工厂创建")
    void testFactoryCreation() {
        JT808Message factoryMessage = factory.createMessage(0x8304);
        assertNotNull(factoryMessage);
        assertInstanceOf(T8304InfoService.class, factoryMessage);
        assertEquals(0x8304, factoryMessage.getMessageId());
    }

    @Test
    @DisplayName("测试消息工厂支持检查")
    void testFactorySupport() {
        assertTrue(factory.isSupported(0x8304));
        assertTrue(factory.getSupportedMessageIds().contains(0x8304));
    }

    @Test
    @DisplayName("测试实际使用场景 - 新闻推送")
    void testRealWorldScenario_NewsService() {
        String newsContent = "【重要新闻】科技公司发布新产品，预计将改变行业格局。详情请关注后续报道。";
        T8304InfoService newsMsg = T8304InfoService.createNewsService(newsContent);
        
        // 编码
        Buffer encoded = newsMsg.encodeBody();
        
        // 模拟传输后解码
        T8304InfoService received = new T8304InfoService();
        received.decodeBody(encoded);
        
        // 验证接收结果
        assertTrue(received.isNewsInfo());
        assertEquals("新闻", received.getInfoTypeDescription());
        assertEquals(newsContent, received.getInfoContent());
        assertEquals(newsContent.getBytes(Charset.forName("GBK")).length, received.getInfoLength());
    }

    @Test
    @DisplayName("测试实际使用场景 - 天气服务")
    void testRealWorldScenario_WeatherService() {
        String weatherContent = "今日天气：晴转多云，气温18-25°C，东南风3-4级，空气质量良好。";
        T8304InfoService weatherMsg = T8304InfoService.createWeatherService(weatherContent);
        
        Buffer encoded = weatherMsg.encodeBody();
        T8304InfoService received = new T8304InfoService();
        received.decodeBody(encoded);
        
        assertTrue(received.isWeatherInfo());
        assertEquals("天气", received.getInfoTypeDescription());
        assertEquals(weatherContent, received.getInfoContent());
    }

    @Test
    @DisplayName("测试实际使用场景 - 交通信息")
    void testRealWorldScenario_TrafficService() {
        String trafficContent = "交通提醒：G4京港澳高速北京段发生交通事故，请绕行G6京藏高速。";
        T8304InfoService trafficMsg = T8304InfoService.createTrafficService(trafficContent);
        
        Buffer encoded = trafficMsg.encodeBody();
        T8304InfoService received = new T8304InfoService();
        received.decodeBody(encoded);
        
        assertTrue(received.isTrafficInfo());
        assertEquals("交通", received.getInfoTypeDescription());
        assertEquals(trafficContent, received.getInfoContent());
    }

    @Test
    @DisplayName("测试setInfoContent自动更新长度")
    void testSetInfoContentAutoUpdateLength() {
        String content1 = "短内容";
        String content2 = "这是一个比较长的内容，用于测试自动更新长度功能";
        
        message.setInfoContent(content1);
        assertEquals(content1.getBytes(Charset.forName("GBK")).length, message.getInfoLength());
        
        message.setInfoContent(content2);
        assertEquals(content2.getBytes(Charset.forName("GBK")).length, message.getInfoLength());
        
        message.setInfoContent(null);
        assertEquals(0, message.getInfoLength());
    }
}