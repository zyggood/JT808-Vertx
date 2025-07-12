package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;

/**
 * 补传分包请求消息 (0x8003)
 * 平台请求终端重新传输指定的分包数据
 */
public class T8003ResendSubpackageRequest extends JT808Message {

    /**
     * 原始消息流水号
     */
    private int originalSerialNumber;

    /**
     * 重传包序号列表
     */
    private List<Integer> retransmitPackageIds;

    public T8003ResendSubpackageRequest() {
        super();
        this.retransmitPackageIds = new ArrayList<>();
    }

    public T8003ResendSubpackageRequest(JT808Header header) {
        super(header);
        this.retransmitPackageIds = new ArrayList<>();
    }

    /**
     * 构造补传分包请求
     *
     * @param originalSerialNumber 原始消息流水号
     * @param retransmitPackageIds 重传包序号列表
     */
    public T8003ResendSubpackageRequest(int originalSerialNumber, List<Integer> retransmitPackageIds) {
        this.originalSerialNumber = originalSerialNumber;
        this.retransmitPackageIds = retransmitPackageIds != null ? new ArrayList<>(retransmitPackageIds) : new ArrayList<>();
    }

    /**
     * 创建补传分包请求
     *
     * @param originalSerialNumber 原始消息流水号
     * @param packageIds           重传包序号数组
     */
    public static T8003ResendSubpackageRequest create(int originalSerialNumber, int... packageIds) {
        T8003ResendSubpackageRequest request = new T8003ResendSubpackageRequest();
        request.setOriginalSerialNumber(originalSerialNumber);

        for (int packageId : packageIds) {
            request.addRetransmitPackageId(packageId);
        }

        return request;
    }

    /**
     * 创建补传分包请求
     *
     * @param originalSerialNumber 原始消息流水号
     * @param packageIds           重传包序号列表
     */
    public static T8003ResendSubpackageRequest create(int originalSerialNumber, List<Integer> packageIds) {
        return new T8003ResendSubpackageRequest(originalSerialNumber, packageIds);
    }

    @Override
    public int getMessageId() {
        return 0x8003;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 原始消息流水号 (2字节)
        buffer.appendUnsignedShort(originalSerialNumber);

        // 重传包总数 (1字节)
        buffer.appendByte((byte) retransmitPackageIds.size());

        // 重传包序号列表 (每个包序号1字节)
        for (Integer packageId : retransmitPackageIds) {
            buffer.appendByte(packageId.byteValue());
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        int index = 0;

        // 原始消息流水号 (2字节)
        originalSerialNumber = body.getUnsignedShort(index);
        index += 2;

        // 重传包总数 (1字节)
        int packageCount = body.getUnsignedByte(index);
        index += 1;

        // 重传包序号列表
        retransmitPackageIds.clear();
        for (int i = 0; i < packageCount; i++) {
            int packageId = body.getUnsignedByte(index);
            retransmitPackageIds.add(packageId);
            index += 1;
        }
    }

    /**
     * 添加重传包序号
     *
     * @param packageId 包序号
     */
    public void addRetransmitPackageId(int packageId) {
        if (packageId >= 1 && packageId <= 255) {
            retransmitPackageIds.add(packageId);
        }
    }

    /**
     * 移除重传包序号
     *
     * @param packageId 包序号
     */
    public void removeRetransmitPackageId(int packageId) {
        retransmitPackageIds.remove(Integer.valueOf(packageId));
    }

    /**
     * 清空重传包序号列表
     */
    public void clearRetransmitPackageIds() {
        retransmitPackageIds.clear();
    }

    /**
     * 获取重传包数量
     */
    public int getRetransmitPackageCount() {
        return retransmitPackageIds.size();
    }

    /**
     * 检查是否包含指定的包序号
     */
    public boolean containsPackageId(int packageId) {
        return retransmitPackageIds.contains(packageId);
    }

    /**
     * 获取重传包序号数组
     */
    public int[] getRetransmitPackageIdsArray() {
        return retransmitPackageIds.stream().mapToInt(Integer::intValue).toArray();
    }

    // Getters and Setters
    public int getOriginalSerialNumber() {
        return originalSerialNumber;
    }

    public void setOriginalSerialNumber(int originalSerialNumber) {
        this.originalSerialNumber = originalSerialNumber;
    }

    public List<Integer> getRetransmitPackageIds() {
        return new ArrayList<>(retransmitPackageIds);
    }

    public void setRetransmitPackageIds(List<Integer> retransmitPackageIds) {
        this.retransmitPackageIds = retransmitPackageIds != null ? new ArrayList<>(retransmitPackageIds) : new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("T8003ResendSubpackageRequest{");
        sb.append("originalSerialNumber=").append(originalSerialNumber);
        sb.append(", retransmitPackageCount=").append(retransmitPackageIds.size());
        sb.append(", retransmitPackageIds=").append(retransmitPackageIds);
        sb.append(", header=").append(getHeader());
        sb.append('}');
        return sb.toString();
    }
}