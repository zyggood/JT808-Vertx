package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T8801 摄像头立即拍摄命令消息测试类
 * 
 * @author JT808 Protocol
 * @version 1.0
 */
class T8801CameraImmediateShootingCommandTest {

    @Test
    void testMessageId() {
        T8801CameraImmediateShootingCommand command = new T8801CameraImmediateShootingCommand();
        assertEquals(MessageTypes.Platform.CAMERA_IMMEDIATE_SHOOTING_COMMAND, command.getMessageId());
    }

    @Test
    void testStopCommand() {
        // 测试停止拍摄命令
        int channelId = 1;
        T8801CameraImmediateShootingCommand command = T8801CameraImmediateShootingCommand.createStopCommand(channelId);
        
        assertEquals(channelId, command.getChannelId());
        assertEquals(0, command.getShootingCommand());
        assertTrue(command.isStopCommand());
        assertFalse(command.isVideoCommand());
        assertFalse(command.isPhotoCommand());
        
        // 编码测试
        Buffer encoded = command.encodeBody();
        assertEquals(12, encoded.length());
        
        // 验证通道ID
        assertEquals(channelId, encoded.getByte(0) & 0xFF);
        
        // 验证拍摄命令（停止）
        assertEquals(0, encoded.getShort(1) & 0xFFFF);
        
        // 解码测试
        T8801CameraImmediateShootingCommand decoded = new T8801CameraImmediateShootingCommand();
        decoded.decodeBody(encoded);
        
        assertEquals(channelId, decoded.getChannelId());
        assertEquals(0, decoded.getShootingCommand());
        assertTrue(decoded.isStopCommand());
    }

    @Test
    void testPhotoCommand() {
        // 测试拍照命令
        int channelId = 2;
        int photoCount = 5;
        int interval = 10;
        int saveFlag = 1;
        T8801CameraImmediateShootingCommand.Resolution resolution = T8801CameraImmediateShootingCommand.Resolution.VGA_640_480;
        
        T8801CameraImmediateShootingCommand command = T8801CameraImmediateShootingCommand
                .createPhotoCommand(channelId, photoCount, interval, saveFlag, resolution);
        
        assertEquals(channelId, command.getChannelId());
        assertEquals(photoCount, command.getShootingCommand());
        assertEquals(interval, command.getIntervalOrDuration());
        assertEquals(saveFlag, command.getSaveFlag());
        assertEquals(resolution.getValue(), command.getResolution());
        
        assertTrue(command.isPhotoCommand());
        assertFalse(command.isStopCommand());
        assertFalse(command.isVideoCommand());
        assertEquals(photoCount, command.getPhotoCount());
        
        // 编码测试
        Buffer encoded = command.encodeBody();
        assertEquals(12, encoded.length());
        
        // 验证关键字段
        assertEquals(channelId, encoded.getByte(0) & 0xFF);
        assertEquals(photoCount, encoded.getShort(1) & 0xFFFF);
        assertEquals(interval, encoded.getShort(3) & 0xFFFF);
        assertEquals(saveFlag, encoded.getByte(5) & 0xFF);
        assertEquals(resolution.getValue(), encoded.getByte(6) & 0xFF);
        
        // 解码测试
        T8801CameraImmediateShootingCommand decoded = new T8801CameraImmediateShootingCommand();
        decoded.decodeBody(encoded);
        
        assertEquals(channelId, decoded.getChannelId());
        assertEquals(photoCount, decoded.getShootingCommand());
        assertEquals(interval, decoded.getIntervalOrDuration());
        assertEquals(saveFlag, decoded.getSaveFlag());
        assertEquals(resolution.getValue(), decoded.getResolution());
        assertTrue(decoded.isPhotoCommand());
    }

    @Test
    void testVideoCommand() {
        // 测试录像命令
        int channelId = 3;
        int duration = 60; // 60秒
        int saveFlag = 0; // 实时上传
        T8801CameraImmediateShootingCommand.Resolution resolution = T8801CameraImmediateShootingCommand.Resolution.D1_704_576;
        
        T8801CameraImmediateShootingCommand command = T8801CameraImmediateShootingCommand
                .createVideoCommand(channelId, duration, saveFlag, resolution);
        
        assertEquals(channelId, command.getChannelId());
        assertEquals(0xFFFF, command.getShootingCommand());
        assertEquals(duration, command.getIntervalOrDuration());
        assertEquals(saveFlag, command.getSaveFlag());
        assertEquals(resolution.getValue(), command.getResolution());
        
        assertTrue(command.isVideoCommand());
        assertFalse(command.isStopCommand());
        assertFalse(command.isPhotoCommand());
        
        // 编码测试
        Buffer encoded = command.encodeBody();
        assertEquals(12, encoded.length());
        
        // 验证关键字段
        assertEquals(channelId, encoded.getByte(0) & 0xFF);
        assertEquals(0xFFFF, encoded.getShort(1) & 0xFFFF);
        assertEquals(duration, encoded.getShort(3) & 0xFFFF);
        assertEquals(saveFlag, encoded.getByte(5) & 0xFF);
        assertEquals(resolution.getValue(), encoded.getByte(6) & 0xFF);
        
        // 解码测试
        T8801CameraImmediateShootingCommand decoded = new T8801CameraImmediateShootingCommand();
        decoded.decodeBody(encoded);
        
        assertEquals(channelId, decoded.getChannelId());
        assertEquals(0xFFFF, decoded.getShootingCommand());
        assertEquals(duration, decoded.getIntervalOrDuration());
        assertEquals(saveFlag, decoded.getSaveFlag());
        assertEquals(resolution.getValue(), decoded.getResolution());
        assertTrue(decoded.isVideoCommand());
    }

    @Test
    void testAllParameters() {
        // 测试所有参数的设置和获取
        T8801CameraImmediateShootingCommand command = new T8801CameraImmediateShootingCommand();
        
        // 设置所有参数
        command.setChannelId(4);
        command.setShootingCommand(10);
        command.setIntervalOrDuration(5);
        command.setSaveFlag(1);
        command.setResolution(T8801CameraImmediateShootingCommand.Resolution.XGA_1024_768.getValue());
        command.setQuality(8);
        command.setBrightness(200);
        command.setContrast(100);
        command.setSaturation(80);
        command.setChromaticity(150);
        
        // 验证所有参数
        assertEquals(4, command.getChannelId());
        assertEquals(10, command.getShootingCommand());
        assertEquals(5, command.getIntervalOrDuration());
        assertEquals(1, command.getSaveFlag());
        assertEquals(T8801CameraImmediateShootingCommand.Resolution.XGA_1024_768.getValue(), command.getResolution());
        assertEquals(8, command.getQuality());
        assertEquals(200, command.getBrightness());
        assertEquals(100, command.getContrast());
        assertEquals(80, command.getSaturation());
        assertEquals(150, command.getChromaticity());
        
        // 编码解码测试
        Buffer encoded = command.encodeBody();
        T8801CameraImmediateShootingCommand decoded = new T8801CameraImmediateShootingCommand();
        decoded.decodeBody(encoded);
        
        assertEquals(command.getChannelId(), decoded.getChannelId());
        assertEquals(command.getShootingCommand(), decoded.getShootingCommand());
        assertEquals(command.getIntervalOrDuration(), decoded.getIntervalOrDuration());
        assertEquals(command.getSaveFlag(), decoded.getSaveFlag());
        assertEquals(command.getResolution(), decoded.getResolution());
        assertEquals(command.getQuality(), decoded.getQuality());
        assertEquals(command.getBrightness(), decoded.getBrightness());
        assertEquals(command.getContrast(), decoded.getContrast());
        assertEquals(command.getSaturation(), decoded.getSaturation());
        assertEquals(command.getChromaticity(), decoded.getChromaticity());
    }

    @Test
    void testResolutionEnum() {
        // 测试分辨率枚举
        assertEquals(0x01, T8801CameraImmediateShootingCommand.Resolution.QVGA_320_240.getValue());
        assertEquals(0x02, T8801CameraImmediateShootingCommand.Resolution.VGA_640_480.getValue());
        assertEquals(0x03, T8801CameraImmediateShootingCommand.Resolution.SVGA_800_600.getValue());
        assertEquals(0x04, T8801CameraImmediateShootingCommand.Resolution.XGA_1024_768.getValue());
        assertEquals(0x05, T8801CameraImmediateShootingCommand.Resolution.QCIF_176_144.getValue());
        assertEquals(0x06, T8801CameraImmediateShootingCommand.Resolution.CIF_352_288.getValue());
        assertEquals(0x07, T8801CameraImmediateShootingCommand.Resolution.HALF_D1_704_288.getValue());
        assertEquals(0x08, T8801CameraImmediateShootingCommand.Resolution.D1_704_576.getValue());
        
        // 测试fromValue方法
        assertEquals(T8801CameraImmediateShootingCommand.Resolution.VGA_640_480, 
                T8801CameraImmediateShootingCommand.Resolution.fromValue(0x02));
        
        // 测试无效值
        assertThrows(IllegalArgumentException.class, () -> 
                T8801CameraImmediateShootingCommand.Resolution.fromValue(0xFF));
    }

    @Test
    void testSaveFlagEnum() {
        // 测试保存标志枚举
        assertEquals(0, T8801CameraImmediateShootingCommand.SaveFlag.REAL_TIME_UPLOAD.getValue());
        assertEquals(1, T8801CameraImmediateShootingCommand.SaveFlag.SAVE.getValue());
        
        // 测试fromValue方法
        assertEquals(T8801CameraImmediateShootingCommand.SaveFlag.SAVE, 
                T8801CameraImmediateShootingCommand.SaveFlag.fromValue(1));
        
        // 测试无效值
        assertThrows(IllegalArgumentException.class, () -> 
                T8801CameraImmediateShootingCommand.SaveFlag.fromValue(2));
    }

    @Test
    void testParameterValidation() {
        T8801CameraImmediateShootingCommand command = new T8801CameraImmediateShootingCommand();
        
        // 测试通道ID验证
        assertThrows(IllegalArgumentException.class, () -> command.setChannelId(0));
        assertThrows(IllegalArgumentException.class, () -> command.setChannelId(-1));
        
        // 测试质量参数验证
        assertThrows(IllegalArgumentException.class, () -> command.setQuality(0));
        assertThrows(IllegalArgumentException.class, () -> command.setQuality(11));
        
        // 测试亮度参数验证
        assertThrows(IllegalArgumentException.class, () -> command.setBrightness(-1));
        assertThrows(IllegalArgumentException.class, () -> command.setBrightness(256));
        
        // 测试对比度参数验证
        assertThrows(IllegalArgumentException.class, () -> command.setContrast(-1));
        assertThrows(IllegalArgumentException.class, () -> command.setContrast(128));
        
        // 测试饱和度参数验证
        assertThrows(IllegalArgumentException.class, () -> command.setSaturation(-1));
        assertThrows(IllegalArgumentException.class, () -> command.setSaturation(128));
        
        // 测试色度参数验证
        assertThrows(IllegalArgumentException.class, () -> command.setChromaticity(-1));
        assertThrows(IllegalArgumentException.class, () -> command.setChromaticity(256));
    }

    @Test
    void testDescriptionMethods() {
        T8801CameraImmediateShootingCommand command = T8801CameraImmediateShootingCommand
                .createPhotoCommand(1, 3, 5, 1, T8801CameraImmediateShootingCommand.Resolution.VGA_640_480);
        
        // 测试分辨率描述
        assertEquals("640*480", command.getResolutionDescription());
        
        // 测试保存标志描述
        assertEquals("保存", command.getSaveFlagDescription());
        
        // 测试未知值的描述
        command.setResolution(0xFF);
        assertTrue(command.getResolutionDescription().contains("未知分辨率"));
        
        command.setSaveFlag(99);
        assertTrue(command.getSaveFlagDescription().contains("未知保存标志"));
    }

    @Test
    void testToString() {
        T8801CameraImmediateShootingCommand command = T8801CameraImmediateShootingCommand
                .createPhotoCommand(1, 5, 10, 1, T8801CameraImmediateShootingCommand.Resolution.VGA_640_480);
        
        String result = command.toString();
        assertNotNull(result);
        assertTrue(result.contains("T8801CameraImmediateShootingCommand"));
        assertTrue(result.contains("channelId=1"));
        assertTrue(result.contains("shootingCommand=5"));
        assertTrue(result.contains("拍照5张"));
        assertTrue(result.contains("640*480"));
        assertTrue(result.contains("保存"));
    }

    @Test
    void testEqualsAndHashCode() {
        T8801CameraImmediateShootingCommand command1 = T8801CameraImmediateShootingCommand
                .createPhotoCommand(1, 3, 5, 1, T8801CameraImmediateShootingCommand.Resolution.VGA_640_480);
        
        T8801CameraImmediateShootingCommand command2 = T8801CameraImmediateShootingCommand
                .createPhotoCommand(1, 3, 5, 1, T8801CameraImmediateShootingCommand.Resolution.VGA_640_480);
        
        T8801CameraImmediateShootingCommand command3 = T8801CameraImmediateShootingCommand
                .createPhotoCommand(2, 3, 5, 1, T8801CameraImmediateShootingCommand.Resolution.VGA_640_480);
        
        // 测试equals
        assertEquals(command1, command2);
        assertNotEquals(command1, command3);
        assertNotEquals(command1, null);
        assertNotEquals(command1, "string");
        
        // 测试hashCode
        assertEquals(command1.hashCode(), command2.hashCode());
        assertNotEquals(command1.hashCode(), command3.hashCode());
    }

    @Test
    void testInvalidDecoding() {
        T8801CameraImmediateShootingCommand command = new T8801CameraImmediateShootingCommand();
        
        // 测试空缓冲区
        Buffer emptyBuffer = Buffer.buffer();
        assertThrows(IllegalArgumentException.class, () -> command.decodeBody(emptyBuffer));
        
        // 测试不完整的缓冲区
        Buffer incompleteBuffer = Buffer.buffer().appendBytes(new byte[]{0x01, 0x02, 0x03});
        assertThrows(IllegalArgumentException.class, () -> command.decodeBody(incompleteBuffer));
    }

    @Test
    void testBoundaryValues() {
        T8801CameraImmediateShootingCommand command = new T8801CameraImmediateShootingCommand();
        
        // 测试边界值
        command.setChannelId(1);
        command.setShootingCommand(65535); // 最大值
        command.setIntervalOrDuration(65535); // 最大值
        command.setSaveFlag(1);
        command.setResolution(8);
        command.setQuality(10); // 最大值
        command.setBrightness(255); // 最大值
        command.setContrast(127); // 最大值
        command.setSaturation(127); // 最大值
        command.setChromaticity(255); // 最大值
        
        // 编码解码测试
        Buffer encoded = command.encodeBody();
        T8801CameraImmediateShootingCommand decoded = new T8801CameraImmediateShootingCommand();
        decoded.decodeBody(encoded);
        
        assertEquals(command.getChannelId(), decoded.getChannelId());
        assertEquals(command.getShootingCommand(), decoded.getShootingCommand());
        assertEquals(command.getIntervalOrDuration(), decoded.getIntervalOrDuration());
        assertEquals(command.getSaveFlag(), decoded.getSaveFlag());
        assertEquals(command.getResolution(), decoded.getResolution());
        assertEquals(command.getQuality(), decoded.getQuality());
        assertEquals(command.getBrightness(), decoded.getBrightness());
        assertEquals(command.getContrast(), decoded.getContrast());
        assertEquals(command.getSaturation(), decoded.getSaturation());
        assertEquals(command.getChromaticity(), decoded.getChromaticity());
    }
}