package com.jt808.protocol.version;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JT808协议版本管理
 * 支持协议版本兼容性和扩展字段
 */
public class ProtocolVersion implements Comparable<ProtocolVersion> {

    // 预定义的协议版本
    public static final ProtocolVersion V2011 = new ProtocolVersion(2011, 0, 0, "JT/T 808-2011");
    public static final ProtocolVersion V2013 = new ProtocolVersion(2013, 0, 0, "JT/T 808-2013");
    public static final ProtocolVersion V2019 = new ProtocolVersion(2019, 0, 0, "JT/T 808-2019");

    // 默认版本
    public static final ProtocolVersion DEFAULT = V2019;

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d{4})\\.(\\d+)\\.(\\d+)");

    private final int year;
    private final int major;
    private final int minor;
    private final String description;
    private final String versionString;

    public ProtocolVersion(int year, int major, int minor) {
        this(year, major, minor, null);
    }

    public ProtocolVersion(int year, int major, int minor, String description) {
        if (year < 2000 || year > 9999) {
            throw new IllegalArgumentException("Year must be between 2000 and 9999");
        }
        if (major < 0 || major > 99) {
            throw new IllegalArgumentException("Major version must be between 0 and 99");
        }
        if (minor < 0 || minor > 99) {
            throw new IllegalArgumentException("Minor version must be between 0 and 99");
        }

        this.year = year;
        this.major = major;
        this.minor = minor;
        this.description = description;
        this.versionString = String.format("%d.%d.%d", year, major, minor);
    }

    /**
     * 从字符串解析版本
     */
    public static ProtocolVersion parse(String versionString) {
        if (versionString == null || versionString.trim().isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be null or empty");
        }

        // 尝试匹配预定义版本
        switch (versionString.trim()) {
            case "2011":
            case "JT/T 808-2011":
                return V2011;
            case "2013":
            case "JT/T 808-2013":
                return V2013;
            case "2019":
            case "JT/T 808-2019":
                return V2019;
        }

        // 尝试解析格式化版本字符串
        Matcher matcher = VERSION_PATTERN.matcher(versionString.trim());
        if (matcher.matches()) {
            int year = Integer.parseInt(matcher.group(1));
            int major = Integer.parseInt(matcher.group(2));
            int minor = Integer.parseInt(matcher.group(3));
            return new ProtocolVersion(year, major, minor);
        }

        throw new IllegalArgumentException("Invalid version string format: " + versionString);
    }

    /**
     * 检查是否兼容指定版本
     */
    public boolean isCompatibleWith(ProtocolVersion other) {
        if (other == null) {
            return false;
        }

        // 同一年份的版本通常兼容
        if (this.year == other.year) {
            return true;
        }

        // 新版本向后兼容旧版本
        return this.compareTo(other) >= 0;
    }

    /**
     * 检查是否支持指定特性
     */
    public boolean supportsFeature(ProtocolFeature feature) {
        return feature.isSupportedIn(this);
    }

    /**
     * 获取版本差异
     */
    public VersionDifference getDifference(ProtocolVersion other) {
        if (other == null) {
            return VersionDifference.INCOMPATIBLE;
        }

        int comparison = this.compareTo(other);

        if (comparison == 0) {
            return VersionDifference.SAME;
        } else if (comparison > 0) {
            if (this.year == other.year) {
                return VersionDifference.MINOR_UPGRADE;
            } else {
                return VersionDifference.MAJOR_UPGRADE;
            }
        } else {
            if (this.year == other.year) {
                return VersionDifference.MINOR_DOWNGRADE;
            } else {
                return VersionDifference.MAJOR_DOWNGRADE;
            }
        }
    }

    /**
     * 检查是否为标准版本
     */
    public boolean isStandardVersion() {
        return this.equals(V2011) || this.equals(V2013) || this.equals(V2019);
    }

    /**
     * 获取下一个主版本
     */
    public ProtocolVersion nextMajorVersion() {
        return new ProtocolVersion(year, major + 1, 0);
    }

    /**
     * 获取下一个次版本
     */
    public ProtocolVersion nextMinorVersion() {
        return new ProtocolVersion(year, major, minor + 1);
    }

    // Getters
    public int getYear() {
        return year;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public String getDescription() {
        return description;
    }

    public String getVersionString() {
        return versionString;
    }

    @Override
    public int compareTo(ProtocolVersion other) {
        if (other == null) {
            return 1;
        }

        int yearComparison = Integer.compare(this.year, other.year);
        if (yearComparison != 0) {
            return yearComparison;
        }

        int majorComparison = Integer.compare(this.major, other.major);
        if (majorComparison != 0) {
            return majorComparison;
        }

        return Integer.compare(this.minor, other.minor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ProtocolVersion that = (ProtocolVersion) obj;
        return year == that.year && major == that.major && minor == that.minor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, major, minor);
    }

    @Override
    public String toString() {
        if (description != null) {
            return description + " (" + versionString + ")";
        }
        return versionString;
    }

    /**
     * 版本差异枚举
     */
    public enum VersionDifference {
        SAME("相同版本"),
        MINOR_UPGRADE("次版本升级"),
        MAJOR_UPGRADE("主版本升级"),
        MINOR_DOWNGRADE("次版本降级"),
        MAJOR_DOWNGRADE("主版本降级"),
        INCOMPATIBLE("不兼容版本");

        private final String description;

        VersionDifference(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isUpgrade() {
            return this == MINOR_UPGRADE || this == MAJOR_UPGRADE;
        }

        public boolean isDowngrade() {
            return this == MINOR_DOWNGRADE || this == MAJOR_DOWNGRADE;
        }

        public boolean isCompatible() {
            return this != INCOMPATIBLE;
        }
    }

    /**
     * 协议特性枚举
     */
    public enum ProtocolFeature {
        // 基础特性
        BASIC_MESSAGING("基础消息", V2011),
        TERMINAL_REGISTRATION("终端注册", V2011),
        LOCATION_REPORTING("位置汇报", V2011),

        // 2013版本新增特性
        MULTIMEDIA_UPLOAD("多媒体上传", V2013),
        BATCH_LOCATION_UPLOAD("批量位置上传", V2013),

        // 2019版本新增特性
        ENHANCED_AUTHENTICATION("增强鉴权", V2019),
        EXTENDED_MESSAGE_FORMAT("扩展消息格式", V2019),
        IMPROVED_ENCRYPTION("改进加密", V2019),
        PROTOCOL_VERSION_IDENTIFICATION("协议版本标识", V2019);

        private final String description;
        private final ProtocolVersion introducedIn;

        ProtocolFeature(String description, ProtocolVersion introducedIn) {
            this.description = description;
            this.introducedIn = introducedIn;
        }

        public boolean isSupportedIn(ProtocolVersion version) {
            return version != null && version.compareTo(introducedIn) >= 0;
        }

        public String getDescription() {
            return description;
        }

        public ProtocolVersion getIntroducedIn() {
            return introducedIn;
        }
    }
}