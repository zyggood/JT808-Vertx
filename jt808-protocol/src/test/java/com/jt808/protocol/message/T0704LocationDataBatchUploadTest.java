package com.jt808.protocol.message;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T0704定位数据批量上传消息测试
 */
class T0704LocationDataBatchUploadTest {

    private static final Logger logger = LoggerFactory.getLogger(T0704LocationDataBatchUploadTest.class);

    private T0704LocationDataBatchUpload message;
    private List<T0200LocationReport> locationReports;

    @BeforeEach
    void setUp() {
        message = new T0704LocationDataBatchUpload();
        locationReports = new ArrayList<>();

        // 创建测试用的位置汇报数据
        for (int i = 0; i < 3; i++) {
            T0200LocationReport report = new T0200LocationReport();
            report.setAlarmFlag(0);
            report.setStatusFlag(0x02); // 定位状态
            report.setLatitude(31123456 + i * 1000); // 纬度
            report.setLongitude(121123456 + i * 1000); // 经度
            report.setAltitude(100 + i * 10); // 高程
            report.setSpeed(60 + i * 5); // 速度
            report.setDirection(90 + i * 10); // 方向
            report.setDateTime(LocalDateTime.of(2024, 1, 15, 10, 30, i * 10)); // 时间
            locationReports.add(report);
        }
    }

    @Test
    void testMessageId() {
        assertEquals(0x0704, message.getMessageId());
    }

    @Test
    void testEncodeDecodeNormalBatch() {
        // 设置正常位置批量汇报
        message.setLocationType((byte) 0);
        for (T0200LocationReport report : locationReports) {
            message.addLocationReport(report);
        }

        // 编码
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertTrue(encoded.length() > 0);

        logger.info("编码后的数据长度: {} 字节", encoded.length());
        logger.info("编码后的数据: {}", bytesToHex(encoded.getBytes()));

        // 解码
        T0704LocationDataBatchUpload decoded = new T0704LocationDataBatchUpload();
        decoded.decodeBody(encoded);

        // 验证基本字段
        assertEquals(message.getItemCount(), decoded.getItemCount());
        assertEquals(message.getLocationType(), decoded.getLocationType());
        assertEquals(locationReports.size(), decoded.getLocationDataItems().size());

        // 验证位置数据项
        List<T0704LocationDataBatchUpload.LocationDataItem> decodedItems = decoded.getLocationDataItems();
        for (int i = 0; i < locationReports.size(); i++) {
            T0200LocationReport originalReport = locationReports.get(i);
            T0200LocationReport decodedReport = decodedItems.get(i).getLocationReport();

            assertNotNull(decodedReport);
            assertEquals(originalReport.getAlarmFlag(), decodedReport.getAlarmFlag());
            assertEquals(originalReport.getStatusFlag(), decodedReport.getStatusFlag());
            assertEquals(originalReport.getLatitude(), decodedReport.getLatitude());
            assertEquals(originalReport.getLongitude(), decodedReport.getLongitude());
            assertEquals(originalReport.getAltitude(), decodedReport.getAltitude());
            assertEquals(originalReport.getSpeed(), decodedReport.getSpeed());
            assertEquals(originalReport.getDirection(), decodedReport.getDirection());
            assertEquals(originalReport.getDateTime(), decodedReport.getDateTime());
        }

        logger.info("解码后的消息: {}", decoded.toString());
    }

    @Test
    void testEncodeDecodeBlindAreaReport() {
        // 设置盲区补报
        message.setLocationType((byte) 1);
        
        // 只添加一个位置汇报
        message.addLocationReport(locationReports.get(0));

        // 编码
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);

        // 解码
        T0704LocationDataBatchUpload decoded = new T0704LocationDataBatchUpload();
        decoded.decodeBody(encoded);

        // 验证
        assertEquals(1, decoded.getItemCount());
        assertEquals((byte) 1, decoded.getLocationType());
        assertEquals("盲区补报", decoded.getLocationTypeDescription());
        assertEquals(1, decoded.getLocationDataItems().size());

        logger.info("盲区补报消息: {}", decoded.toString());
    }

    @Test
    void testEmptyLocationDataItems() {
        // 测试空的位置数据项列表
        message.setLocationType((byte) 0);
        message.setItemCount(0);

        // 编码
        Buffer encoded = message.encodeBody();
        assertNotNull(encoded);
        assertEquals(3, encoded.length()); // 2字节itemCount + 1字节locationType

        // 解码
        T0704LocationDataBatchUpload decoded = new T0704LocationDataBatchUpload();
        decoded.decodeBody(encoded);

        assertEquals(0, decoded.getItemCount());
        assertEquals((byte) 0, decoded.getLocationType());
        assertTrue(decoded.getLocationDataItems().isEmpty());
    }

    @Test
    void testLocationTypeDescription() {
        message.setLocationType((byte) 0);
        assertEquals("正常位置批量汇报", message.getLocationTypeDescription());

        message.setLocationType((byte) 1);
        assertEquals("盲区补报", message.getLocationTypeDescription());

        message.setLocationType((byte) 99);
        assertEquals("未知类型(99)", message.getLocationTypeDescription());
    }

    @Test
    void testAddLocationDataItem() {
        T0704LocationDataBatchUpload.LocationDataItem item = 
            new T0704LocationDataBatchUpload.LocationDataItem(locationReports.get(0));
        
        message.addLocationDataItem(item);
        
        assertEquals(1, message.getItemCount());
        assertEquals(1, message.getLocationDataItems().size());
        assertEquals(locationReports.get(0), message.getLocationDataItems().get(0).getLocationReport());
    }

    @Test
    void testSetLocationDataItems() {
        List<T0704LocationDataBatchUpload.LocationDataItem> items = new ArrayList<>();
        for (T0200LocationReport report : locationReports) {
            items.add(new T0704LocationDataBatchUpload.LocationDataItem(report));
        }

        message.setLocationDataItems(items);
        
        assertEquals(locationReports.size(), message.getItemCount());
        assertEquals(items.size(), message.getLocationDataItems().size());
    }

    @Test
    void testLocationDataItemEncodeDecode() {
        T0704LocationDataBatchUpload.LocationDataItem item = 
            new T0704LocationDataBatchUpload.LocationDataItem(locationReports.get(0));

        // 编码
        Buffer buffer = Buffer.buffer();
        item.encode(buffer);
        assertTrue(buffer.length() > 2); // 至少包含2字节长度字段

        // 解码
        T0704LocationDataBatchUpload.LocationDataItem decodedItem = 
            new T0704LocationDataBatchUpload.LocationDataItem();
        int nextIndex = decodedItem.decode(buffer, 0);
        
        assertEquals(buffer.length(), nextIndex);
        assertNotNull(decodedItem.getLocationReport());
        assertEquals(locationReports.get(0).getLatitude(), 
                    decodedItem.getLocationReport().getLatitude());
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString().trim();
    }
}