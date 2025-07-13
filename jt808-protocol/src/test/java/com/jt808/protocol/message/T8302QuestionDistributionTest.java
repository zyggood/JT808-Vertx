package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8302提问下发消息测试类
 */
@DisplayName("T8302提问下发消息测试")
class T8302QuestionDistributionTest {

    private T8302QuestionDistribution message;

    @BeforeEach
    void setUp() {
        message = new T8302QuestionDistribution();
    }

    @Test
    @DisplayName("测试消息ID")
    void testMessageId() {
        assertEquals(0x8302, message.getMessageId());
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        assertEquals(0, message.getQuestionFlag());
        assertNull(message.getQuestionContent());
        assertNotNull(message.getAnswerList());
        assertTrue(message.getAnswerList().isEmpty());
    }

    @Test
    @DisplayName("测试带参数构造函数")
    void testParameterConstructor() {
        List<T8302QuestionDistribution.Answer> answers = new ArrayList<>();
        answers.add(new T8302QuestionDistribution.Answer((byte) 1, "是"));
        answers.add(new T8302QuestionDistribution.Answer((byte) 2, "否"));

        T8302QuestionDistribution msg = new T8302QuestionDistribution(
                (byte) 0x01, "您是否同意此操作？", answers);

        assertEquals((byte) 0x01, msg.getQuestionFlag());
        assertEquals("您是否同意此操作？", msg.getQuestionContent());
        assertEquals(2, msg.getAnswerList().size());
        assertEquals("是", msg.getAnswerList().get(0).getAnswerContent());
        assertEquals("否", msg.getAnswerList().get(1).getAnswerContent());
    }

    @Test
    @DisplayName("测试静态工厂方法 - 紧急提问")
    void testCreateEmergencyQuestion() {
        List<T8302QuestionDistribution.Answer> answers = new ArrayList<>();
        answers.add(new T8302QuestionDistribution.Answer((byte) 1, "确认"));
        answers.add(new T8302QuestionDistribution.Answer((byte) 2, "取消"));

        T8302QuestionDistribution msg = T8302QuestionDistribution.createEmergencyQuestion(
                "紧急情况，请确认！", answers, true, false);

        assertTrue(msg.isEmergency());
        assertTrue(msg.isTerminalTTS());
        assertFalse(msg.isAdvertisementDisplay());
        assertEquals("紧急情况，请确认！", msg.getQuestionContent());
    }

    @Test
    @DisplayName("测试静态工厂方法 - 普通提问")
    void testCreateNormalQuestion() {
        List<T8302QuestionDistribution.Answer> answers = new ArrayList<>();
        answers.add(new T8302QuestionDistribution.Answer((byte) 1, "同意"));

        T8302QuestionDistribution msg = T8302QuestionDistribution.createNormalQuestion(
                "请选择路线", answers, false, true);

        assertFalse(msg.isEmergency());
        assertFalse(msg.isTerminalTTS());
        assertTrue(msg.isAdvertisementDisplay());
    }

    @Test
    @DisplayName("测试消息体编码 - 基本功能")
    void testEncodeBodyBasic() {
        List<T8302QuestionDistribution.Answer> answers = new ArrayList<>();
        answers.add(new T8302QuestionDistribution.Answer((byte) 1, "是"));
        answers.add(new T8302QuestionDistribution.Answer((byte) 2, "否"));

        message.setQuestionFlag((byte) 0x01);
        message.setQuestionContent("测试问题");
        message.setAnswerList(answers);

        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);

        // 验证编码结果
        int index = 0;
        assertEquals((byte) 0x01, encoded.getByte(index)); // 标志
        index += 1;

        int questionLength = encoded.getUnsignedByte(index); // 问题长度
        index += 1;
        assertTrue(questionLength > 0);

        // 跳过问题内容
        index += questionLength;

        // 验证第一个答案
        assertEquals((byte) 1, encoded.getByte(index)); // 答案ID
        index += 1;
        int answer1Length = encoded.getUnsignedShort(index); // 答案长度
        index += 2;
        assertTrue(answer1Length > 0);
        index += answer1Length;

        // 验证第二个答案
        assertEquals((byte) 2, encoded.getByte(index)); // 答案ID
    }

    @Test
    @DisplayName("测试消息体解码 - 基本功能")
    void testDecodeBodyBasic() {
        // 创建测试数据
        Buffer buffer = Buffer.buffer();
        buffer.appendByte((byte) 0x09); // 标志：紧急 + TTS播读
        buffer.appendByte((byte) 8); // 问题长度
        buffer.appendBytes("测试问题".getBytes(java.nio.charset.Charset.forName("GBK")));

        // 第一个答案
        buffer.appendByte((byte) 1); // 答案ID
        byte[] answer1Bytes = "是".getBytes(java.nio.charset.Charset.forName("GBK"));
        buffer.appendUnsignedShort(answer1Bytes.length); // 答案长度
        buffer.appendBytes(answer1Bytes);

        // 第二个答案
        buffer.appendByte((byte) 2); // 答案ID
        byte[] answer2Bytes = "否".getBytes(java.nio.charset.Charset.forName("GBK"));
        buffer.appendUnsignedShort(answer2Bytes.length); // 答案长度
        buffer.appendBytes(answer2Bytes);

        message.decodeBody(buffer);

        assertEquals((byte) 0x09, message.getQuestionFlag());
        assertTrue(message.isEmergency());
        assertTrue(message.isTerminalTTS());
        assertEquals("测试问题", message.getQuestionContent());
        assertEquals(2, message.getAnswerList().size());
        assertEquals("是", message.getAnswerList().get(0).getAnswerContent());
        assertEquals("否", message.getAnswerList().get(1).getAnswerContent());
    }

    @Test
    @DisplayName("测试编解码一致性")
    void testEncodeDecodeConsistency() {
        List<T8302QuestionDistribution.Answer> answers = new ArrayList<>();
        answers.add(new T8302QuestionDistribution.Answer((byte) 1, "选项A"));
        answers.add(new T8302QuestionDistribution.Answer((byte) 2, "选项B"));
        answers.add(new T8302QuestionDistribution.Answer((byte) 3, "选项C"));

        T8302QuestionDistribution original = new T8302QuestionDistribution(
                (byte) 0x19, "请选择一个选项：", answers);

        Buffer encoded = original.encodeBody();
        T8302QuestionDistribution decoded = new T8302QuestionDistribution();
        decoded.decodeBody(encoded);

        assertEquals(original.getQuestionFlag(), decoded.getQuestionFlag());
        assertEquals(original.getQuestionContent(), decoded.getQuestionContent());
        assertEquals(original.getAnswerList().size(), decoded.getAnswerList().size());

        for (int i = 0; i < original.getAnswerList().size(); i++) {
            T8302QuestionDistribution.Answer originalAnswer = original.getAnswerList().get(i);
            T8302QuestionDistribution.Answer decodedAnswer = decoded.getAnswerList().get(i);
            assertEquals(originalAnswer.getAnswerId(), decodedAnswer.getAnswerId());
            assertEquals(originalAnswer.getAnswerContent(), decodedAnswer.getAnswerContent());
        }
    }

    @Test
    @DisplayName("测试空问题内容")
    void testEmptyQuestionContent() {
        message.setQuestionFlag((byte) 0x01);
        message.setQuestionContent("");

        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);

        T8302QuestionDistribution decoded = new T8302QuestionDistribution();
        decoded.decodeBody(encoded);

        assertEquals((byte) 0x01, decoded.getQuestionFlag());
        assertEquals("", decoded.getQuestionContent());
    }

    @Test
    @DisplayName("测试空答案列表")
    void testEmptyAnswerList() {
        message.setQuestionFlag((byte) 0x01);
        message.setQuestionContent("只有问题，没有答案");
        message.setAnswerList(new ArrayList<>());

        Buffer encoded = message.encodeBody();
        T8302QuestionDistribution decoded = new T8302QuestionDistribution();
        decoded.decodeBody(encoded);

        assertEquals("只有问题，没有答案", decoded.getQuestionContent());
        assertTrue(decoded.getAnswerList().isEmpty());
    }

    @Test
    @DisplayName("测试问题内容长度限制")
    void testQuestionContentLengthLimit() {
        StringBuilder longQuestion = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longQuestion.append("测");
        }

        assertThrows(IllegalArgumentException.class, () -> {
            message.setQuestionContent(longQuestion.toString());
        });
    }

    @Test
    @DisplayName("测试答案内容长度限制")
    void testAnswerContentLengthLimit() {
        StringBuilder longAnswer = new StringBuilder();
        for (int i = 0; i < 70000; i++) {
            longAnswer.append("答");
        }

        assertThrows(IllegalArgumentException.class, () -> {
            new T8302QuestionDistribution.Answer((byte) 1, longAnswer.toString());
        });
    }

    @Test
    @DisplayName("测试标志位检查方法")
    void testFlagCheckMethods() {
        message.setQuestionFlag((byte) 0x19); // 紧急 + TTS播读 + 广告屏显示

        assertTrue(message.isEmergency());
        assertTrue(message.isTerminalTTS());
        assertTrue(message.isAdvertisementDisplay());

        String description = message.getQuestionFlagDescription();
        assertTrue(description.contains("紧急"));
        assertTrue(description.contains("终端TTS播读"));
        assertTrue(description.contains("广告屏显示"));
    }

    @Test
    @DisplayName("测试添加答案方法")
    void testAddAnswerMethods() {
        message.addAnswer((byte) 1, "答案1");
        message.addAnswer(new T8302QuestionDistribution.Answer((byte) 2, "答案2"));

        assertEquals(2, message.getAnswerList().size());
        assertEquals("答案1", message.getAnswerList().get(0).getAnswerContent());
        assertEquals("答案2", message.getAnswerList().get(1).getAnswerContent());
    }

    @Test
    @DisplayName("测试Answer类的无符号ID方法")
    void testAnswerUnsignedId() {
        T8302QuestionDistribution.Answer answer = new T8302QuestionDistribution.Answer((byte) 0xFF, "测试");
        assertEquals(255, answer.getAnswerIdUnsigned());
        assertEquals((byte) 0xFF, answer.getAnswerId());
    }

    @Test
    @DisplayName("测试异常处理 - 消息体长度不足")
    void testInvalidBodyLength() {
        Buffer invalidBuffer = Buffer.buffer();
        invalidBuffer.appendByte((byte) 0x01); // 只有标志，没有问题长度

        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(invalidBuffer);
        });
    }

    @Test
    @DisplayName("测试异常处理 - 问题内容长度超出范围")
    void testQuestionContentOutOfRange() {
        Buffer invalidBuffer = Buffer.buffer();
        invalidBuffer.appendByte((byte) 0x01); // 标志
        invalidBuffer.appendByte((byte) 10); // 声明问题长度为10
        invalidBuffer.appendBytes("短".getBytes(java.nio.charset.Charset.forName("GBK"))); // 实际只有2字节

        assertThrows(IllegalArgumentException.class, () -> {
            message.decodeBody(invalidBuffer);
        });
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        List<T8302QuestionDistribution.Answer> answers = new ArrayList<>();
        answers.add(new T8302QuestionDistribution.Answer((byte) 1, "是"));

        message.setQuestionFlag((byte) 0x01);
        message.setQuestionContent("测试问题");
        message.setAnswerList(answers);

        String result = message.toString();
        assertTrue(result.contains("T8302QuestionDistribution"));
        assertTrue(result.contains("0x01"));
        assertTrue(result.contains("测试问题"));
    }

    @Test
    @DisplayName("测试equals和hashCode方法")
    void testEqualsAndHashCode() {
        List<T8302QuestionDistribution.Answer> answers1 = new ArrayList<>();
        answers1.add(new T8302QuestionDistribution.Answer((byte) 1, "是"));

        List<T8302QuestionDistribution.Answer> answers2 = new ArrayList<>();
        answers2.add(new T8302QuestionDistribution.Answer((byte) 1, "是"));

        T8302QuestionDistribution msg1 = new T8302QuestionDistribution((byte) 0x01, "问题", answers1);
        T8302QuestionDistribution msg2 = new T8302QuestionDistribution((byte) 0x01, "问题", answers2);
        T8302QuestionDistribution msg3 = new T8302QuestionDistribution((byte) 0x02, "问题", answers1);

        assertEquals(msg1, msg2);
        assertEquals(msg1.hashCode(), msg2.hashCode());
        assertNotEquals(msg1, msg3);
    }

    @Test
    @DisplayName("测试Answer类的equals和hashCode方法")
    void testAnswerEqualsAndHashCode() {
        T8302QuestionDistribution.Answer answer1 = new T8302QuestionDistribution.Answer((byte) 1, "答案");
        T8302QuestionDistribution.Answer answer2 = new T8302QuestionDistribution.Answer((byte) 1, "答案");
        T8302QuestionDistribution.Answer answer3 = new T8302QuestionDistribution.Answer((byte) 2, "答案");

        assertEquals(answer1, answer2);
        assertEquals(answer1.hashCode(), answer2.hashCode());
        assertNotEquals(answer1, answer3);
    }
}