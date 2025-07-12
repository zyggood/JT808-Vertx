package com.jt808.protocol.message.additional;

/**
 * 需要人工确认报警事件ID (ID: 0x04)
 * WORD，从1开始计数
 */
public class ManualAlarmEventInfo extends AdditionalInfo {

    /**
     * 报警事件ID
     */
    private int eventId;

    public ManualAlarmEventInfo() {
        super(0x04, 2);
    }

    public ManualAlarmEventInfo(int eventId) {
        super(0x04, 2);
        this.eventId = eventId;
    }

    @Override
    public String getTypeName() {
        return "需要人工确认报警事件ID";
    }

    @Override
    public String getDescription() {
        return String.format("需要人工确认报警事件ID: %d", eventId);
    }

    @Override
    public void parseData(byte[] data) {
        this.eventId = parseWORD(data);
    }

    @Override
    public byte[] encodeData() {
        return encodeWORD(eventId);
    }

    /**
     * 获取事件ID
     *
     * @return 事件ID
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * 设置事件ID
     *
     * @param eventId 事件ID
     */
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return String.format("ManualAlarmEventInfo{id=0x%02X, eventId=%d}", id, eventId);
    }
}
