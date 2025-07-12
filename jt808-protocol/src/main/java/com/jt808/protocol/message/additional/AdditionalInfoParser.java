package com.jt808.protocol.message.additional;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * 附加信息解析器
 * 用于解析和编码T0200消息中的附加信息
 */
public class AdditionalInfoParser {

    /**
     * 解析附加信息列表
     *
     * @param buffer 数据缓冲区
     * @param length 附加信息总长度
     * @return 附加信息列表
     */
    public static List<AdditionalInfo> parseAdditionalInfoList(ByteBuf buffer, int length) {
        List<AdditionalInfo> additionalInfoList = new ArrayList<>();

        int startIndex = buffer.readerIndex();
        int endIndex = startIndex + length;

        while (buffer.readerIndex() < endIndex) {
            try {
                // 读取附加信息ID
                int id = buffer.readUnsignedByte();

                // 读取附加信息长度
                int infoLength = buffer.readUnsignedByte();

                // 读取附加信息数据
                byte[] data = new byte[infoLength];
                buffer.readBytes(data);

                // 创建并解析附加信息
                AdditionalInfo info = AdditionalInfoFactory.createAdditionalInfo(id, data);
                if (info != null) {
                    additionalInfoList.add(info);
                }

            } catch (Exception e) {
                // 如果解析失败，跳出循环避免无限循环
                System.err.println("解析附加信息时发生错误: " + e.getMessage());
                break;
            }
        }

        return additionalInfoList;
    }

    /**
     * 编码附加信息列表
     *
     * @param buffer             数据缓冲区
     * @param additionalInfoList 附加信息列表
     */
    public static void encodeAdditionalInfoList(ByteBuf buffer, List<AdditionalInfo> additionalInfoList) {
        if (additionalInfoList == null || additionalInfoList.isEmpty()) {
            return;
        }

        for (AdditionalInfo info : additionalInfoList) {
            try {
                // 编码附加信息数据
                byte[] data = info.encodeData();

                // 写入附加信息ID
                buffer.writeByte(info.getId());

                // 写入附加信息长度
                buffer.writeByte(data.length);

                // 写入附加信息数据
                buffer.writeBytes(data);

            } catch (Exception e) {
                System.err.println("编码附加信息时发生错误: " + e.getMessage());
            }
        }
    }

    /**
     * 计算附加信息列表的总长度
     *
     * @param additionalInfoList 附加信息列表
     * @return 总长度
     */
    public static int calculateTotalLength(List<AdditionalInfo> additionalInfoList) {
        if (additionalInfoList == null || additionalInfoList.isEmpty()) {
            return 0;
        }

        int totalLength = 0;
        for (AdditionalInfo info : additionalInfoList) {
            // 每个附加信息的格式：ID(1字节) + 长度(1字节) + 数据(N字节)
            totalLength += 2 + info.getLength();
        }

        return totalLength;
    }

    /**
     * 格式化附加信息列表为字符串
     *
     * @param additionalInfoList 附加信息列表
     * @return 格式化字符串
     */
    public static String formatAdditionalInfoList(List<AdditionalInfo> additionalInfoList) {
        if (additionalInfoList == null || additionalInfoList.isEmpty()) {
            return "无附加信息";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("附加信息列表 (").append(additionalInfoList.size()).append("项):\n");

        for (int i = 0; i < additionalInfoList.size(); i++) {
            AdditionalInfo info = additionalInfoList.get(i);
            sb.append(String.format("  [%d] %s\n", i + 1, info.getDescription()));
        }

        return sb.toString();
    }
}