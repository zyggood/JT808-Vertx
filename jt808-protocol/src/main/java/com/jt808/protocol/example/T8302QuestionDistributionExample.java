package com.jt808.protocol.example;

import com.jt808.protocol.message.T8302QuestionDistribution;
import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;

/**
 * T8302提问下发消息使用示例
 */
public class T8302QuestionDistributionExample {

    public static void main(String[] args) {
        // 示例1：创建紧急提问
        System.out.println("=== 示例1：紧急提问 ===");
        List<T8302QuestionDistribution.Answer> emergencyAnswers = new ArrayList<>();
        emergencyAnswers.add(new T8302QuestionDistribution.Answer((byte) 1, "确认"));
        emergencyAnswers.add(new T8302QuestionDistribution.Answer((byte) 2, "取消"));

        T8302QuestionDistribution emergencyQuestion = T8302QuestionDistribution.createEmergencyQuestion(
                "检测到紧急情况，是否立即停车？", emergencyAnswers, true, false);

        System.out.println("消息ID: 0x" + Integer.toHexString(emergencyQuestion.getMessageId()).toUpperCase());
        System.out.println("是否紧急: " + emergencyQuestion.isEmergency());
        System.out.println("是否TTS播读: " + emergencyQuestion.isTerminalTTS());
        System.out.println("问题内容: " + emergencyQuestion.getQuestionContent());
        System.out.println("答案数量: " + emergencyQuestion.getAnswerList().size());

        // 编码测试
        Buffer encoded = emergencyQuestion.encodeBody();
        System.out.println("编码后长度: " + encoded.length() + " 字节");

        // 解码测试
        T8302QuestionDistribution decoded = new T8302QuestionDistribution();
        decoded.decodeBody(encoded);
        System.out.println("解码后问题: " + decoded.getQuestionContent());
        System.out.println("解码后答案数量: " + decoded.getAnswerList().size());

        System.out.println();

        // 示例2：创建普通提问（多选题）
        System.out.println("=== 示例2：普通多选题 ===");
        List<T8302QuestionDistribution.Answer> multipleAnswers = new ArrayList<>();
        multipleAnswers.add(new T8302QuestionDistribution.Answer((byte) 1, "高速公路"));
        multipleAnswers.add(new T8302QuestionDistribution.Answer((byte) 2, "国道"));
        multipleAnswers.add(new T8302QuestionDistribution.Answer((byte) 3, "省道"));
        multipleAnswers.add(new T8302QuestionDistribution.Answer((byte) 4, "市区道路"));

        T8302QuestionDistribution normalQuestion = T8302QuestionDistribution.createNormalQuestion(
                "请选择您希望行驶的道路类型：", multipleAnswers, false, true);

        System.out.println("问题内容: " + normalQuestion.getQuestionContent());
        System.out.println("是否紧急: " + normalQuestion.isEmergency());
        System.out.println("是否广告屏显示: " + normalQuestion.isAdvertisementDisplay());
        System.out.println("标志描述: " + normalQuestion.getQuestionFlagDescription());

        for (int i = 0; i < normalQuestion.getAnswerList().size(); i++) {
            T8302QuestionDistribution.Answer answer = normalQuestion.getAnswerList().get(i);
            System.out.println("答案" + (i + 1) + ": ID=" + answer.getAnswerIdUnsigned() +
                    ", 内容=" + answer.getAnswerContent());
        }

        System.out.println();

        // 示例3：手动构建复杂提问
        System.out.println("=== 示例3：手动构建复杂提问 ===");
        T8302QuestionDistribution complexQuestion = new T8302QuestionDistribution();

        // 设置标志：紧急 + TTS播读 + 广告屏显示
        byte complexFlag = (byte) (T8302QuestionDistribution.QuestionFlag.EMERGENCY |
                T8302QuestionDistribution.QuestionFlag.TERMINAL_TTS |
                T8302QuestionDistribution.QuestionFlag.ADVERTISEMENT_DISPLAY);
        complexQuestion.setQuestionFlag(complexFlag);
        complexQuestion.setQuestionContent("系统检测到车辆异常，请选择处理方式：");

        // 添加答案
        complexQuestion.addAnswer((byte) 1, "立即停车检查");
        complexQuestion.addAnswer((byte) 2, "继续行驶到服务区");
        complexQuestion.addAnswer((byte) 3, "联系救援");
        complexQuestion.addAnswer((byte) 4, "忽略警告");

        System.out.println("复杂提问: " + complexQuestion);
        System.out.println("标志描述: " + complexQuestion.getQuestionFlagDescription());

        // 编解码一致性测试
        Buffer complexEncoded = complexQuestion.encodeBody();
        T8302QuestionDistribution complexDecoded = new T8302QuestionDistribution();
        complexDecoded.decodeBody(complexEncoded);

        System.out.println("编解码一致性检查: " + complexQuestion.equals(complexDecoded));

        System.out.println();

        // 示例4：边界情况测试
        System.out.println("=== 示例4：边界情况测试 ===");

        // 空问题内容
        T8302QuestionDistribution emptyQuestion = new T8302QuestionDistribution();
        emptyQuestion.setQuestionFlag((byte) 0x01);
        emptyQuestion.setQuestionContent("");
        System.out.println("空问题内容编码长度: " + emptyQuestion.encodeBody().length());

        // 长问题内容（接近255字节限制）
        StringBuilder longQuestion = new StringBuilder();
        for (int i = 0; i < 80; i++) {
            longQuestion.append("测试");
        }

        try {
            T8302QuestionDistribution longQuestionMsg = new T8302QuestionDistribution();
            longQuestionMsg.setQuestionContent(longQuestion.toString());
            System.out.println("长问题内容设置成功，长度: " +
                    longQuestion.toString().getBytes("GBK").length + " 字节");
        } catch (Exception e) {
            System.out.println("长问题内容设置失败: " + e.getMessage());
        }

        // 长答案内容测试
        StringBuilder longAnswer = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longAnswer.append("答案");
        }

        try {
            T8302QuestionDistribution.Answer longAnswerObj =
                    new T8302QuestionDistribution.Answer((byte) 1, longAnswer.toString());
            System.out.println("长答案内容设置成功，长度: " +
                    longAnswer.toString().getBytes("GBK").length + " 字节");
        } catch (Exception e) {
            System.out.println("长答案内容设置失败: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== T8302提问下发消息示例完成 ===");
    }
}