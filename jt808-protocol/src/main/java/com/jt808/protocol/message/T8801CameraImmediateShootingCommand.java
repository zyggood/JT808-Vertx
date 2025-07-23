package com.jt808.protocol.message;

import com.jt808.protocol.constants.MessageTypes;
import io.vertx.core.buffer.Buffer;

import java.util.Objects;

/**
 * T8801 摄像头立即拍摄命令消息 (0x8801)
 * 
 * 消息体数据格式：
 * 起始字节 字段 数据类型 描述及要求
 * 0 通道ID BYTE >0
 * 1 拍摄命令 WORD 0表示停止拍摄；0xFFFF表示录像；其它表示拍照张数
 * 3 拍照间隔/录像时间 WORD 秒，0表示按最小间隔拍照或一直录像
 * 5 保存标志 BYTE 1：保存；0：实时上传
 * 6 分辨率 BYTE 0x01:320*240；0x02:640*480；0x03:800*600；0x04:1024*768；0x05:176*144[Qcif]；0x06:352*288[Cif]；0x07:704*288[HALF D1]；0x08:704*576[D1]
 * 7 图像/视频质量 BYTE 1-10，1代表质量损失最小，10表示压缩比最大
 * 8 亮度 BYTE 0-255
 * 9 对比度 BYTE 0-127
 * 10 饱和度 BYTE 0-127
 * 11 色度 BYTE 0-255
 * 
 * @author JT808 Protocol
 * @version 1.0
 */
public class T8801CameraImmediateShootingCommand extends JT808Message {

    /** 通道ID */
    private int channelId;
    
    /** 拍摄命令 */
    private int shootingCommand;
    
    /** 拍照间隔/录像时间 */
    private int intervalOrDuration;
    
    /** 保存标志 */
    private int saveFlag;
    
    /** 分辨率 */
    private int resolution;
    
    /** 图像/视频质量 */
    private int quality;
    
    /** 亮度 */
    private int brightness;
    
    /** 对比度 */
    private int contrast;
    
    /** 饱和度 */
    private int saturation;
    
    /** 色度 */
    private int chromaticity;

    /**
     * 默认构造函数
     */
    public T8801CameraImmediateShootingCommand() {
        super();
    }

    /**
     * 带消息头的构造函数
     * 
     * @param header 消息头
     */
    public T8801CameraImmediateShootingCommand(JT808Header header) {
        super(header);
    }

    @Override
    public int getMessageId() {
        return 0x8801;
    }

    /**
     * 创建停止拍摄命令
     * 
     * @param channelId 通道ID
     * @return 停止拍摄命令
     */
    public static T8801CameraImmediateShootingCommand createStopCommand(int channelId) {
        T8801CameraImmediateShootingCommand command = new T8801CameraImmediateShootingCommand();
        command.setChannelId(channelId);
        command.setShootingCommand(0); // 0表示停止拍摄
        command.setIntervalOrDuration(0);
        command.setSaveFlag(0);
        command.setResolution(Resolution.VGA_640_480.getValue());
        command.setQuality(5);
        command.setBrightness(128);
        command.setContrast(64);
        command.setSaturation(64);
        command.setChromaticity(128);
        return command;
    }

    /**
     * 创建拍照命令
     * 
     * @param channelId 通道ID
     * @param photoCount 拍照张数
     * @param interval 拍照间隔（秒）
     * @param saveFlag 保存标志（1：保存；0：实时上传）
     * @param resolution 分辨率
     * @return 拍照命令
     */
    public static T8801CameraImmediateShootingCommand createPhotoCommand(int channelId, int photoCount, 
            int interval, int saveFlag, Resolution resolution) {
        T8801CameraImmediateShootingCommand command = new T8801CameraImmediateShootingCommand();
        command.setChannelId(channelId);
        command.setShootingCommand(photoCount);
        command.setIntervalOrDuration(interval);
        command.setSaveFlag(saveFlag);
        command.setResolution(resolution.getValue());
        command.setQuality(5);
        command.setBrightness(128);
        command.setContrast(64);
        command.setSaturation(64);
        command.setChromaticity(128);
        return command;
    }

    /**
     * 创建录像命令
     * 
     * @param channelId 通道ID
     * @param duration 录像时间（秒，0表示一直录像）
     * @param saveFlag 保存标志（1：保存；0：实时上传）
     * @param resolution 分辨率
     * @return 录像命令
     */
    public static T8801CameraImmediateShootingCommand createVideoCommand(int channelId, int duration, 
            int saveFlag, Resolution resolution) {
        T8801CameraImmediateShootingCommand command = new T8801CameraImmediateShootingCommand();
        command.setChannelId(channelId);
        command.setShootingCommand(0xFFFF); // 0xFFFF表示录像
        command.setIntervalOrDuration(duration);
        command.setSaveFlag(saveFlag);
        command.setResolution(resolution.getValue());
        command.setQuality(5);
        command.setBrightness(128);
        command.setContrast(64);
        command.setSaturation(64);
        command.setChromaticity(128);
        return command;
    }

    @Override
    public Buffer encodeBody() {
        Buffer buffer = Buffer.buffer();
        
        // 通道ID (1字节)
        buffer.appendByte((byte) channelId);
        
        // 拍摄命令 (2字节)
        buffer.appendShort((short) shootingCommand);
        
        // 拍照间隔/录像时间 (2字节)
        buffer.appendShort((short) intervalOrDuration);
        
        // 保存标志 (1字节)
        buffer.appendByte((byte) saveFlag);
        
        // 分辨率 (1字节)
        buffer.appendByte((byte) resolution);
        
        // 图像/视频质量 (1字节)
        buffer.appendByte((byte) quality);
        
        // 亮度 (1字节)
        buffer.appendByte((byte) brightness);
        
        // 对比度 (1字节)
        buffer.appendByte((byte) contrast);
        
        // 饱和度 (1字节)
        buffer.appendByte((byte) saturation);
        
        // 色度 (1字节)
        buffer.appendByte((byte) chromaticity);
        
        return buffer;
    }

    @Override
    public void decodeBody(Buffer buffer) {
        if (buffer.length() < 12) {
            throw new IllegalArgumentException("消息体长度不足，期望12字节，实际" + buffer.length() + "字节");
        }
        
        int index = 0;
        
        // 通道ID (1字节)
        this.channelId = buffer.getByte(index++) & 0xFF;
        
        // 拍摄命令 (2字节)
        this.shootingCommand = buffer.getShort(index) & 0xFFFF;
        index += 2;
        
        // 拍照间隔/录像时间 (2字节)
        this.intervalOrDuration = buffer.getShort(index) & 0xFFFF;
        index += 2;
        
        // 保存标志 (1字节)
        this.saveFlag = buffer.getByte(index++) & 0xFF;
        
        // 分辨率 (1字节)
        this.resolution = buffer.getByte(index++) & 0xFF;
        
        // 图像/视频质量 (1字节)
        this.quality = buffer.getByte(index++) & 0xFF;
        
        // 亮度 (1字节)
        this.brightness = buffer.getByte(index++) & 0xFF;
        
        // 对比度 (1字节)
        this.contrast = buffer.getByte(index++) & 0xFF;
        
        // 饱和度 (1字节)
        this.saturation = buffer.getByte(index++) & 0xFF;
        
        // 色度 (1字节)
        this.chromaticity = buffer.getByte(index) & 0xFF;
    }

    /**
     * 分辨率枚举
     */
    public enum Resolution {
        QVGA_320_240(0x01, "320*240"),
        VGA_640_480(0x02, "640*480"),
        SVGA_800_600(0x03, "800*600"),
        XGA_1024_768(0x04, "1024*768"),
        QCIF_176_144(0x05, "176*144[Qcif]"),
        CIF_352_288(0x06, "352*288[Cif]"),
        HALF_D1_704_288(0x07, "704*288[HALF D1]"),
        D1_704_576(0x08, "704*576[D1]");

        private final int value;
        private final String description;

        Resolution(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static Resolution fromValue(int value) {
            for (Resolution resolution : values()) {
                if (resolution.value == value) {
                    return resolution;
                }
            }
            throw new IllegalArgumentException("未知的分辨率值: " + value);
        }
    }

    /**
     * 保存标志枚举
     */
    public enum SaveFlag {
        REAL_TIME_UPLOAD(0, "实时上传"),
        SAVE(1, "保存");

        private final int value;
        private final String description;

        SaveFlag(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static SaveFlag fromValue(int value) {
            for (SaveFlag flag : values()) {
                if (flag.value == value) {
                    return flag;
                }
            }
            throw new IllegalArgumentException("未知的保存标志值: " + value);
        }
    }

    // Getter和Setter方法
    
    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        if (channelId <= 0) {
            throw new IllegalArgumentException("通道ID必须大于0");
        }
        this.channelId = channelId;
    }

    public int getShootingCommand() {
        return shootingCommand;
    }

    public void setShootingCommand(int shootingCommand) {
        this.shootingCommand = shootingCommand;
    }

    public int getIntervalOrDuration() {
        return intervalOrDuration;
    }

    public void setIntervalOrDuration(int intervalOrDuration) {
        this.intervalOrDuration = intervalOrDuration;
    }

    public int getSaveFlag() {
        return saveFlag;
    }

    public void setSaveFlag(int saveFlag) {
        this.saveFlag = saveFlag;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        if (quality < 1 || quality > 10) {
            throw new IllegalArgumentException("图像/视频质量必须在1-10之间");
        }
        this.quality = quality;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        if (brightness < 0 || brightness > 255) {
            throw new IllegalArgumentException("亮度必须在0-255之间");
        }
        this.brightness = brightness;
    }

    public int getContrast() {
        return contrast;
    }

    public void setContrast(int contrast) {
        if (contrast < 0 || contrast > 127) {
            throw new IllegalArgumentException("对比度必须在0-127之间");
        }
        this.contrast = contrast;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setSaturation(int saturation) {
        if (saturation < 0 || saturation > 127) {
            throw new IllegalArgumentException("饱和度必须在0-127之间");
        }
        this.saturation = saturation;
    }

    public int getChromaticity() {
        return chromaticity;
    }

    public void setChromaticity(int chromaticity) {
        if (chromaticity < 0 || chromaticity > 255) {
            throw new IllegalArgumentException("色度必须在0-255之间");
        }
        this.chromaticity = chromaticity;
    }

    // 辅助方法
    
    /**
     * 是否为停止拍摄命令
     * 
     * @return true表示停止拍摄
     */
    public boolean isStopCommand() {
        return shootingCommand == 0;
    }

    /**
     * 是否为录像命令
     * 
     * @return true表示录像
     */
    public boolean isVideoCommand() {
        return shootingCommand == 0xFFFF;
    }

    /**
     * 是否为拍照命令
     * 
     * @return true表示拍照
     */
    public boolean isPhotoCommand() {
        return shootingCommand > 0 && shootingCommand != 0xFFFF;
    }

    /**
     * 获取拍照张数（仅当为拍照命令时有效）
     * 
     * @return 拍照张数
     */
    public int getPhotoCount() {
        return isPhotoCommand() ? shootingCommand : 0;
    }

    /**
     * 获取分辨率描述
     * 
     * @return 分辨率描述
     */
    public String getResolutionDescription() {
        try {
            return Resolution.fromValue(resolution).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知分辨率(" + resolution + ")";
        }
    }

    /**
     * 获取保存标志描述
     * 
     * @return 保存标志描述
     */
    public String getSaveFlagDescription() {
        try {
            return SaveFlag.fromValue(saveFlag).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知保存标志(" + saveFlag + ")";
        }
    }

    @Override
    public String toString() {
        return "T8801CameraImmediateShootingCommand{" +
                "channelId=" + channelId +
                ", shootingCommand=" + shootingCommand +
                ", intervalOrDuration=" + intervalOrDuration +
                ", saveFlag=" + saveFlag +
                ", resolution=" + resolution +
                ", quality=" + quality +
                ", brightness=" + brightness +
                ", contrast=" + contrast +
                ", saturation=" + saturation +
                ", chromaticity=" + chromaticity +
                ", commandType=" + (isStopCommand() ? "停止" : isVideoCommand() ? "录像" : "拍照" + getPhotoCount() + "张") +
                ", resolutionDesc='" + getResolutionDescription() + '\'' +
                ", saveFlagDesc='" + getSaveFlagDescription() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        T8801CameraImmediateShootingCommand that = (T8801CameraImmediateShootingCommand) o;
        
        return channelId == that.channelId &&
                shootingCommand == that.shootingCommand &&
                intervalOrDuration == that.intervalOrDuration &&
                saveFlag == that.saveFlag &&
                resolution == that.resolution &&
                quality == that.quality &&
                brightness == that.brightness &&
                contrast == that.contrast &&
                saturation == that.saturation &&
                chromaticity == that.chromaticity;
    }

    @Override
    public int hashCode() {
        int result = channelId;
        result = 31 * result + shootingCommand;
        result = 31 * result + intervalOrDuration;
        result = 31 * result + saveFlag;
        result = 31 * result + resolution;
        result = 31 * result + quality;
        result = 31 * result + brightness;
        result = 31 * result + contrast;
        result = 31 * result + saturation;
        result = 31 * result + chromaticity;
        return result;
    }
}