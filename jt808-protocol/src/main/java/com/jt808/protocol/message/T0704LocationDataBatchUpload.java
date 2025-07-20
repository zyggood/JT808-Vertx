package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;

/**
 * 定位数据批量上传消息 (0x0704)
 * 终端批量上传位置信息
 */
public class T0704LocationDataBatchUpload extends JT808Message {

    /**
     * 数据项个数 (包含的位置汇报数据项个数，>0)
     */
    private int itemCount;

    /**
     * 位置数据类型 (0：正常位置批量汇报，1：盲区补报)
     */
    private byte locationType;

    /**
     * 位置汇报数据项列表
     */
    private List<LocationDataItem> locationDataItems;

    public T0704LocationDataBatchUpload() {
        super();
        this.locationDataItems = new ArrayList<>();
    }

    public T0704LocationDataBatchUpload(JT808Header header) {
        super(header);
        this.locationDataItems = new ArrayList<>();
    }

    @Override
    public int getMessageId() {
        return 0x0704;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();

        // 数据项个数 (2字节)
        buffer.appendUnsignedShort(itemCount);

        // 位置数据类型 (1字节)
        buffer.appendByte(locationType);

        // 位置汇报数据项
        if (locationDataItems != null) {
            for (LocationDataItem item : locationDataItems) {
                item.encode(buffer);
            }
        }

        return buffer;
    }

    @Override
    public void decodeBody(Buffer body) {
        int index = 0;

        // 数据项个数 (2字节)
        itemCount = body.getUnsignedShort(index);
        index += 2;

        // 位置数据类型 (1字节)
        locationType = body.getByte(index);
        index += 1;

        // 位置汇报数据项
        locationDataItems = new ArrayList<>();
        for (int i = 0; i < itemCount && index < body.length(); i++) {
            LocationDataItem item = new LocationDataItem();
            index = item.decode(body, index);
            locationDataItems.add(item);
        }
    }

    /**
     * 位置汇报数据项
     */
    public static class LocationDataItem {
        /**
         * 位置汇报数据体长度
         */
        private int dataLength;

        /**
         * 位置汇报数据体 (T0200位置信息汇报的消息体)
         */
        private T0200LocationReport locationReport;

        public LocationDataItem() {
        }

        public LocationDataItem(T0200LocationReport locationReport) {
            this.locationReport = locationReport;
        }

        /**
         * 编码位置汇报数据项
         */
        public void encode(Buffer buffer) {
            if (locationReport != null) {
                // 编码位置汇报数据体
                Buffer locationData = locationReport.encodeBody();
                dataLength = locationData.length();

                // 位置汇报数据体长度 (2字节)
                buffer.appendUnsignedShort(dataLength);

                // 位置汇报数据体
                buffer.appendBuffer(locationData);
            } else {
                // 如果位置汇报为空，长度为0
                buffer.appendUnsignedShort(0);
            }
        }

        /**
         * 解码位置汇报数据项
         * @param buffer 数据缓冲区
         * @param startIndex 开始索引
         * @return 解码后的索引位置
         */
        public int decode(Buffer buffer, int startIndex) {
            int index = startIndex;

            // 位置汇报数据体长度 (2字节)
            dataLength = buffer.getUnsignedShort(index);
            index += 2;

            // 位置汇报数据体
            if (dataLength > 0 && index + dataLength <= buffer.length()) {
                Buffer locationData = buffer.getBuffer(index, index + dataLength);
                locationReport = new T0200LocationReport();
                locationReport.decodeBody(locationData);
                index += dataLength;
            }

            return index;
        }

        // Getters and Setters
        public int getDataLength() {
            return dataLength;
        }

        public void setDataLength(int dataLength) {
            this.dataLength = dataLength;
        }

        public T0200LocationReport getLocationReport() {
            return locationReport;
        }

        public void setLocationReport(T0200LocationReport locationReport) {
            this.locationReport = locationReport;
        }
    }

    // Getters and Setters
    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public byte getLocationType() {
        return locationType;
    }

    public void setLocationType(byte locationType) {
        this.locationType = locationType;
    }

    public List<LocationDataItem> getLocationDataItems() {
        return locationDataItems;
    }

    public void setLocationDataItems(List<LocationDataItem> locationDataItems) {
        this.locationDataItems = locationDataItems;
        this.itemCount = locationDataItems != null ? locationDataItems.size() : 0;
    }

    /**
     * 添加位置汇报数据项
     */
    public void addLocationDataItem(LocationDataItem item) {
        if (locationDataItems == null) {
            locationDataItems = new ArrayList<>();
        }
        locationDataItems.add(item);
        this.itemCount = locationDataItems.size();
    }

    /**
     * 添加位置汇报数据项
     */
    public void addLocationReport(T0200LocationReport locationReport) {
        addLocationDataItem(new LocationDataItem(locationReport));
    }

    /**
     * 获取位置数据类型描述
     */
    public String getLocationTypeDescription() {
        switch (locationType) {
            case 0:
                return "正常位置批量汇报";
            case 1:
                return "盲区补报";
            default:
                return "未知类型(" + locationType + ")";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("T0704LocationDataBatchUpload{\n");
        sb.append("  itemCount=").append(itemCount).append(",\n");
        sb.append("  locationType=").append(locationType).append(" (").append(getLocationTypeDescription()).append("),\n");
        sb.append("  locationDataItems=[\n");
        if (locationDataItems != null) {
            for (int i = 0; i < locationDataItems.size(); i++) {
                LocationDataItem item = locationDataItems.get(i);
                sb.append("    [" + i + "] dataLength=").append(item.getDataLength());
                if (item.getLocationReport() != null) {
                    sb.append(", locationReport={");
                    T0200LocationReport report = item.getLocationReport();
                    sb.append("lat=").append(report.getLatitudeDegrees());
                    sb.append(", lng=").append(report.getLongitudeDegrees());
                    sb.append(", time=").append(report.getDateTime());
                    sb.append("}");
                }
                sb.append("\n");
            }
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }
}