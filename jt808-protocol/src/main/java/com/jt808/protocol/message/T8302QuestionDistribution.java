package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 提问下发消息 (0x8302)
 * 平台通过发送提问下发消息，将带有候选答案的提问发到终端，终端立即显示，驾驶员选择后终端向平台发出提问应答消息
 */
public class T8302QuestionDistribution extends JT808Message {

    /**
     * 提问下发标志
     */
    private byte questionFlag;

    /**
     * 问题内容
     */
    private String questionContent;

    /**
     * 候选答案列表
     */
    private List<Answer> answerList;

    public T8302QuestionDistribution() {
        super();
        this.answerList = new ArrayList<>();
    }

    public T8302QuestionDistribution(JT808Header header) {
        super(header);
        this.answerList = new ArrayList<>();
    }

    /**
     * 构造提问下发消息
     *
     * @param questionFlag    提问下发标志
     * @param questionContent 问题内容
     * @param answerList      候选答案列表
     */
    public T8302QuestionDistribution(byte questionFlag, String questionContent, List<Answer> answerList) {
        this.questionFlag = questionFlag;
        this.questionContent = questionContent;
        this.answerList = answerList != null ? new ArrayList<>(answerList) : new ArrayList<>();
    }

    /**
     * 创建紧急提问
     *
     * @param questionContent 问题内容
     * @param answerList      候选答案列表
     * @param terminalTTS     是否终端TTS播读
     * @param adDisplay       是否广告屏显示
     * @return 提问下发消息
     */
    public static T8302QuestionDistribution createEmergencyQuestion(String questionContent, List<Answer> answerList,
                                                                    boolean terminalTTS, boolean adDisplay) {
        byte flag = QuestionFlag.EMERGENCY;
        if (terminalTTS) {
            flag |= QuestionFlag.TERMINAL_TTS;
        }
        if (adDisplay) {
            flag |= QuestionFlag.ADVERTISEMENT_DISPLAY;
        }
        return new T8302QuestionDistribution(flag, questionContent, answerList);
    }

    /**
     * 创建普通提问
     *
     * @param questionContent 问题内容
     * @param answerList      候选答案列表
     * @param terminalTTS     是否终端TTS播读
     * @param adDisplay       是否广告屏显示
     * @return 提问下发消息
     */
    public static T8302QuestionDistribution createNormalQuestion(String questionContent, List<Answer> answerList,
                                                                 boolean terminalTTS, boolean adDisplay) {
        byte flag = 0;
        if (terminalTTS) {
            flag |= QuestionFlag.TERMINAL_TTS;
        }
        if (adDisplay) {
            flag |= QuestionFlag.ADVERTISEMENT_DISPLAY;
        }
        return new T8302QuestionDistribution(flag, questionContent, answerList);
    }

    @Override
    public int getMessageId() {
        return 0x8302;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 标志 (1字节)
        buffer.appendByte(questionFlag);

        // 问题内容长度 (1字节) + 问题内容 (STRING，GBK编码)
        if (questionContent != null && !questionContent.isEmpty()) {
            byte[] questionBytes = questionContent.getBytes(Charset.forName("GBK"));
            if (questionBytes.length > 255) {
                throw new IllegalArgumentException("问题内容长度不能超过255字节，当前长度: " + questionBytes.length + " 字节");
            }
            buffer.appendByte((byte) questionBytes.length);
            buffer.appendBytes(questionBytes);
        } else {
            buffer.appendByte((byte) 0);
        }

        // 候选答案列表
        if (answerList != null) {
            for (Answer answer : answerList) {
                // 答案ID (1字节)
                buffer.appendByte(answer.getAnswerId());

                // 答案内容长度 (2字节，WORD) + 答案内容 (STRING，GBK编码)
                if (answer.getAnswerContent() != null && !answer.getAnswerContent().isEmpty()) {
                    byte[] answerBytes = answer.getAnswerContent().getBytes(Charset.forName("GBK"));
                    if (answerBytes.length > 65535) {
                        throw new IllegalArgumentException("答案内容长度不能超过65535字节，当前长度: " + answerBytes.length + " 字节");
                    }
                    buffer.appendUnsignedShort(answerBytes.length);
                    buffer.appendBytes(answerBytes);
                } else {
                    buffer.appendUnsignedShort(0);
                }
            }
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        if (body == null || body.length() < 2) {
            throw new IllegalArgumentException("提问下发消息体长度至少为2字节，实际长度: " + (body != null ? body.length() : 0) + " 字节");
        }

        int index = 0;

        // 标志 (1字节)
        questionFlag = body.getByte(index);
        index += 1;

        // 问题内容长度 (1字节)
        int questionLength = body.getUnsignedByte(index);
        index += 1;

        // 问题内容 (STRING，GBK编码)
        if (questionLength > 0) {
            if (index + questionLength > body.length()) {
                throw new IllegalArgumentException("问题内容长度超出消息体范围");
            }
            byte[] questionBytes = body.getBytes(index, index + questionLength);
            questionContent = new String(questionBytes, Charset.forName("GBK"));
            index += questionLength;
        } else {
            questionContent = "";
        }

        // 候选答案列表
        answerList = new ArrayList<>();
        while (index < body.length()) {
            if (index + 3 > body.length()) {
                throw new IllegalArgumentException("候选答案数据不完整");
            }

            // 答案ID (1字节)
            byte answerId = body.getByte(index);
            index += 1;

            // 答案内容长度 (2字节，WORD)
            int answerLength = body.getUnsignedShort(index);
            index += 2;

            // 答案内容 (STRING，GBK编码)
            String answerContent = "";
            if (answerLength > 0) {
                if (index + answerLength > body.length()) {
                    throw new IllegalArgumentException("答案内容长度超出消息体范围");
                }
                byte[] answerBytes = body.getBytes(index, index + answerLength);
                answerContent = new String(answerBytes, Charset.forName("GBK"));
                index += answerLength;
            }

            answerList.add(new Answer(answerId, answerContent));
        }
    }

    /**
     * 检查是否为紧急提问
     *
     * @return true表示紧急
     */
    public boolean isEmergency() {
        return (questionFlag & QuestionFlag.EMERGENCY) != 0;
    }

    /**
     * 检查是否需要终端TTS播读
     *
     * @return true表示需要播读
     */
    public boolean isTerminalTTS() {
        return (questionFlag & QuestionFlag.TERMINAL_TTS) != 0;
    }

    /**
     * 检查是否需要广告屏显示
     *
     * @return true表示需要显示
     */
    public boolean isAdvertisementDisplay() {
        return (questionFlag & QuestionFlag.ADVERTISEMENT_DISPLAY) != 0;
    }

    /**
     * 获取提问标志描述
     *
     * @return 标志描述
     */
    public String getQuestionFlagDescription() {
        StringBuilder sb = new StringBuilder();

        if (isEmergency()) {
            sb.append("紧急; ");
        }
        if (isTerminalTTS()) {
            sb.append("终端TTS播读; ");
        }
        if (isAdvertisementDisplay()) {
            sb.append("广告屏显示; ");
        }

        String result = sb.toString();
        return result.endsWith("; ") ? result.substring(0, result.length() - 2) : result;
    }

    /**
     * 添加候选答案
     *
     * @param answerId      答案ID
     * @param answerContent 答案内容
     */
    public void addAnswer(byte answerId, String answerContent) {
        if (answerList == null) {
            answerList = new ArrayList<>();
        }
        answerList.add(new Answer(answerId, answerContent));
    }

    /**
     * 添加候选答案
     *
     * @param answer 答案对象
     */
    public void addAnswer(Answer answer) {
        if (answerList == null) {
            answerList = new ArrayList<>();
        }
        if (answer != null) {
            answerList.add(answer);
        }
    }

    // Getters and Setters
    public byte getQuestionFlag() {
        return questionFlag;
    }

    public void setQuestionFlag(byte questionFlag) {
        this.questionFlag = questionFlag;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        if (questionContent != null) {
            byte[] questionBytes = questionContent.getBytes(Charset.forName("GBK"));
            if (questionBytes.length > 255) {
                throw new IllegalArgumentException("问题内容长度不能超过255字节，当前长度: " + questionBytes.length + " 字节");
            }
        }
        this.questionContent = questionContent;
    }

    public List<Answer> getAnswerList() {
        return answerList;
    }

    public void setAnswerList(List<Answer> answerList) {
        this.answerList = answerList != null ? new ArrayList<>(answerList) : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "T8302QuestionDistribution{" +
                "questionFlag=0x" + String.format("%02X", questionFlag & 0xFF) +
                " (" + getQuestionFlagDescription() + ")" +
                ", questionContent='" + questionContent + '\'' +
                ", answerList=" + answerList +
                ", header=" + getHeader() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        T8302QuestionDistribution that = (T8302QuestionDistribution) o;
        return questionFlag == that.questionFlag &&
                Objects.equals(questionContent, that.questionContent) &&
                Objects.equals(answerList, that.answerList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionFlag, questionContent, answerList);
    }

    /**
     * 提问下发标志位常量
     */
    public static class QuestionFlag {
        /**
         * 位0: 紧急
         */
        public static final byte EMERGENCY = 0x01;

        /**
         * 位1: 保留
         */
        public static final byte RESERVED_1 = 0x02;

        /**
         * 位2: 保留
         */
        public static final byte RESERVED_2 = 0x04;

        /**
         * 位3: 终端TTS播读
         */
        public static final byte TERMINAL_TTS = 0x08;

        /**
         * 位4: 广告屏显示
         */
        public static final byte ADVERTISEMENT_DISPLAY = 0x10;

        /**
         * 位5-7: 保留
         */
        public static final byte RESERVED_5_7 = (byte) 0xE0;
    }

    /**
     * 候选答案类
     */
    public static class Answer {
        /**
         * 答案ID
         */
        private byte answerId;

        /**
         * 答案内容
         */
        private String answerContent;

        public Answer() {
        }

        public Answer(byte answerId, String answerContent) {
            this.answerId = answerId;
            setAnswerContent(answerContent);
        }

        public byte getAnswerId() {
            return answerId;
        }

        public void setAnswerId(byte answerId) {
            this.answerId = answerId;
        }

        public String getAnswerContent() {
            return answerContent;
        }

        public void setAnswerContent(String answerContent) {
            if (answerContent != null) {
                byte[] answerBytes = answerContent.getBytes(Charset.forName("GBK"));
                if (answerBytes.length > 65535) {
                    throw new IllegalArgumentException("答案内容长度不能超过65535字节，当前长度: " + answerBytes.length + " 字节");
                }
            }
            this.answerContent = answerContent;
        }

        /**
         * 获取答案ID的无符号值
         *
         * @return 无符号答案ID (0-255)
         */
        public int getAnswerIdUnsigned() {
            return answerId & 0xFF;
        }

        @Override
        public String toString() {
            return "Answer{" +
                    "answerId=" + getAnswerIdUnsigned() +
                    ", answerContent='" + answerContent + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Answer answer = (Answer) o;
            return answerId == answer.answerId && Objects.equals(answerContent, answer.answerContent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(answerId, answerContent);
        }
    }
}