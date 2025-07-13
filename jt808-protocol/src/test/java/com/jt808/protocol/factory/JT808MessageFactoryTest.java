package com.jt808.protocol.factory;

import com.jt808.protocol.factory.JT808MessageFactory.GenericJT808Message;
import com.jt808.protocol.message.*;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JT808消息工厂测试类
 */
class JT808MessageFactoryTest {

    private JT808MessageFactory factory;

    @BeforeEach
    void setUp() {
        factory = JT808MessageFactory.getInstance();
    }

    @Test
    @DisplayName("测试单例模式")
    void testSingleton() {
        JT808MessageFactory factory1 = JT808MessageFactory.getInstance();
        JT808MessageFactory factory2 = JT808MessageFactory.getInstance();
        assertSame(factory1, factory2);
    }

    @Test
    @DisplayName("测试创建已知消息类型")
    void testCreateKnownMessage() {
        // 测试终端消息
        JT808Message message1 = factory.createMessage(0x0001);
        assertInstanceOf(T0001TerminalCommonResponse.class, message1);

        JT808Message message2 = factory.createMessage(0x0002);
        assertInstanceOf(T0002TerminalHeartbeat.class, message2);

        JT808Message message3 = factory.createMessage(0x0100);
        assertInstanceOf(T0100TerminalRegister.class, message3);

        JT808Message message4 = factory.createMessage(0x0102);
        assertInstanceOf(T0102TerminalAuth.class, message4);

        JT808Message message5 = factory.createMessage(0x0200);
        assertInstanceOf(T0200LocationReport.class, message5);

        JT808Message message6 = factory.createMessage(0x0301);
        assertInstanceOf(T0301EventReport.class, message6);

        // 测试平台消息
        JT808Message message7 = factory.createMessage(0x8001);
        assertInstanceOf(T8001PlatformCommonResponse.class, message7);

        JT808Message message8 = factory.createMessage(0x8100);
        assertInstanceOf(T8100TerminalRegisterResponse.class, message8);

        JT808Message message9 = factory.createMessage(0x8302);
        assertInstanceOf(T8302QuestionDistribution.class, message9);
    }

    @Test
    @DisplayName("测试创建未知消息类型")
    void testCreateUnknownMessage() {
        JT808Message message = factory.createMessage(0x9999);
        assertInstanceOf(GenericJT808Message.class, message);
        assertEquals(0x9999, message.getMessageId());
    }

    @Test
    @DisplayName("测试消息类型支持检查")
    void testIsSupported() {
        assertTrue(factory.isSupported(0x0001));
        assertTrue(factory.isSupported(0x0002));
        assertTrue(factory.isSupported(0x0100));
        assertTrue(factory.isSupported(0x0301));
        assertTrue(factory.isSupported(0x8001));
        assertTrue(factory.isSupported(0x8100));
        assertTrue(factory.isSupported(0x8302));

        assertFalse(factory.isSupported(0x9999));
    }

    @Test
    @DisplayName("测试获取支持的消息ID")
    void testGetSupportedMessageIds() {
        var supportedIds = factory.getSupportedMessageIds();

        assertTrue(supportedIds.contains(0x0001));
        assertTrue(supportedIds.contains(0x0002));
        assertTrue(supportedIds.contains(0x0100));
        assertTrue(supportedIds.contains(0x0102));
        assertTrue(supportedIds.contains(0x0200));
        assertTrue(supportedIds.contains(0x0301));
        assertTrue(supportedIds.contains(0x8001));
        assertTrue(supportedIds.contains(0x8100));
        assertTrue(supportedIds.contains(0x8302));

        assertFalse(supportedIds.contains(0x9999));
    }

    @Test
    @DisplayName("测试注册自定义消息类型")
    void testRegisterMessage() {
        // 注册前检查
        assertFalse(factory.isSupported(0x9001));

        // 注册自定义消息
        factory.registerMessage(0x9001, () -> new CustomMessage(0x9001));

        // 注册后检查
        assertTrue(factory.isSupported(0x9001));

        JT808Message message = factory.createMessage(0x9001);
        assertInstanceOf(CustomMessage.class, message);
        assertEquals(0x9001, message.getMessageId());
    }

    @Test
    @DisplayName("测试通用消息的功能")
    void testGenericMessage() {
        GenericJT808Message message = new GenericJT808Message(0x9999);

        assertEquals(0x9999, message.getMessageId());

        // 测试消息体数据
        Buffer testData = Buffer.buffer("test data");
        message.setBodyData(testData);
        assertEquals(testData, message.getBodyData());

        // 测试编码
        Buffer encoded = message.encodeBody();
        assertEquals(testData, encoded);

        // 测试解码
        Buffer newData = Buffer.buffer("new test data");
        message.decodeBody(newData);
        assertEquals(newData, message.getBodyData());
    }

    @Test
    @DisplayName("测试通用消息的空数据处理")
    void testGenericMessageEmptyData() {
        GenericJT808Message message = new GenericJT808Message(0x9999);

        // 未设置数据时应返回空Buffer
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(0, encoded.length());

        assertNull(message.getBodyData());
    }

    @Test
    @DisplayName("测试T0301事件报告消息工厂支持")
    void testT0301EventReportFactory() {
        // 测试创建 T0301EventReport 消息
        JT808Message message = factory.createMessage(0x0301);

        assertNotNull(message, "消息不应为空");
        assertInstanceOf(T0301EventReport.class, message, "消息应为 T0301EventReport 类型");
        assertEquals(0x0301, message.getMessageId(), "消息ID应为 0x0301");

        // 测试消息功能
        T0301EventReport eventReport = (T0301EventReport) message;
        eventReport.setEventId((byte) 0x01);
        assertEquals(0x01, eventReport.getEventIdUnsigned(), "事件ID应正确设置");

        // 测试编码
        Buffer encoded = eventReport.encodeBody();
        assertNotNull(encoded, "编码结果不应为空");
        assertEquals(1, encoded.length(), "编码后长度应为1字节");
        assertEquals(0x01, encoded.getByte(0), "编码后的事件ID应正确");

        // 测试创建多个实例
        T0301EventReport report1 = (T0301EventReport) factory.createMessage(0x0301);
        T0301EventReport report2 = (T0301EventReport) factory.createMessage(0x0301);
        assertNotSame(report1, report2, "每次创建应返回新的实例");
    }

    @Test
    @DisplayName("测试T8302提问下发消息工厂支持")
    void testT8302QuestionDistributionFactory() {
        // 测试创建 T8302QuestionDistribution 消息
        JT808Message message = factory.createMessage(0x8302);

        assertNotNull(message, "消息不应为空");
        assertInstanceOf(T8302QuestionDistribution.class, message, "消息应为 T8302QuestionDistribution 类型");
        assertEquals(0x8302, message.getMessageId(), "消息ID应为 0x8302");

        // 测试消息功能
        T8302QuestionDistribution questionMsg = (T8302QuestionDistribution) message;
        questionMsg.setQuestionFlag((byte) 0x01);
        questionMsg.setQuestionContent("测试问题");
        questionMsg.addAnswer((byte) 1, "答案1");

        assertEquals((byte) 0x01, questionMsg.getQuestionFlag(), "提问标志应正确设置");
        assertEquals("测试问题", questionMsg.getQuestionContent(), "问题内容应正确设置");
        assertEquals(1, questionMsg.getAnswerList().size(), "答案列表大小应正确");

        // 测试编码
        Buffer encoded = questionMsg.encodeBody();
        assertNotNull(encoded, "编码结果不应为空");
        assertTrue(encoded.length() > 0, "编码后长度应大于0");

        // 测试创建多个实例
        T8302QuestionDistribution question1 = (T8302QuestionDistribution) factory.createMessage(0x8302);
        T8302QuestionDistribution question2 = (T8302QuestionDistribution) factory.createMessage(0x8302);
        assertNotSame(question1, question2, "每次创建应返回新的实例");

        // 测试不同类型的提问
        T8302QuestionDistribution emergencyQuestion = T8302QuestionDistribution.createEmergencyQuestion(
                "紧急提问", new java.util.ArrayList<>(), true, false);
        assertEquals(0x8302, emergencyQuestion.getMessageId(), "紧急提问消息ID应正确");
        assertTrue(emergencyQuestion.isEmergency(), "应为紧急提问");

        T8302QuestionDistribution normalQuestion = T8302QuestionDistribution.createNormalQuestion(
                "普通提问", new java.util.ArrayList<>(), false, true);
        assertEquals(0x8302, normalQuestion.getMessageId(), "普通提问消息ID应正确");
        assertFalse(normalQuestion.isEmergency(), "应为普通提问");
    }

    /**
     * 自定义消息类，用于测试
     */
    private static class CustomMessage extends JT808Message {
        private final int messageId;

        public CustomMessage(int messageId) {
            this.messageId = messageId;
        }

        @Override
        public int getMessageId() {
            return messageId;
        }

        @Override
        public Buffer encodeBody() {
            return Buffer.buffer();
        }

        @Override
        public void decodeBody(Buffer body) {
            // 空实现
        }
    }
}