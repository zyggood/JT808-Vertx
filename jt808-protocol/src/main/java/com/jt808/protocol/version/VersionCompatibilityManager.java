package com.jt808.protocol.version;

import com.jt808.protocol.message.JT808Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 协议版本兼容性管理器
 * 处理不同版本间的兼容性和字段映射
 */
public class VersionCompatibilityManager {
    
    private static final Logger logger = LoggerFactory.getLogger(VersionCompatibilityManager.class);
    
    private final Map<String, FieldMapping> fieldMappings;
    private final Map<Integer, MessageVersionInfo> messageVersionInfo;
    private final Set<ProtocolVersion> supportedVersions;
    
    public VersionCompatibilityManager() {
        this.fieldMappings = new ConcurrentHashMap<>();
        this.messageVersionInfo = new ConcurrentHashMap<>();
        this.supportedVersions = new HashSet<>();
        
        initializeDefaultMappings();
        initializeMessageVersionInfo();
    }
    
    /**
     * 初始化默认字段映射
     */
    private void initializeDefaultMappings() {
        // 添加支持的版本
        supportedVersions.add(ProtocolVersion.V2011);
        supportedVersions.add(ProtocolVersion.V2013);
        supportedVersions.add(ProtocolVersion.V2019);
        
        // 位置信息汇报消息的字段映射
        addFieldMapping("T0200LocationReport.alarmFlag", 
                ProtocolVersion.V2011, "alarmFlag", FieldType.UINT32,
                ProtocolVersion.V2019, "alarmFlag", FieldType.UINT32);
        
        addFieldMapping("T0200LocationReport.statusFlag", 
                ProtocolVersion.V2011, "statusFlag", FieldType.UINT32,
                ProtocolVersion.V2019, "statusFlag", FieldType.UINT32);
        
        // 2019版本新增的协议版本标识字段
        addFieldMapping("JT808Header.protocolVersion", 
                ProtocolVersion.V2019, "protocolVersion", FieldType.UINT8,
                null, null, null); // 旧版本没有此字段
        
        // 终端手机号字段在不同版本中的长度变化
        addFieldMapping("JT808Header.terminalPhone", 
                ProtocolVersion.V2011, "terminalPhone", FieldType.BCD_6,
                ProtocolVersion.V2019, "terminalPhone", FieldType.BCD_10);
    }
    
    /**
     * 初始化消息版本信息
     */
    private void initializeMessageVersionInfo() {
        // 位置信息汇报 (0x0200)
        messageVersionInfo.put(0x0200, new MessageVersionInfo(0x0200, "位置信息汇报")
                .addVersionSupport(ProtocolVersion.V2011, true)
                .addVersionSupport(ProtocolVersion.V2013, true)
                .addVersionSupport(ProtocolVersion.V2019, true)
                .addVersionChange(ProtocolVersion.V2019, "增加了协议版本标识字段"));
        
        // 终端注册 (0x0100)
        messageVersionInfo.put(0x0100, new MessageVersionInfo(0x0100, "终端注册")
                .addVersionSupport(ProtocolVersion.V2011, true)
                .addVersionSupport(ProtocolVersion.V2013, true)
                .addVersionSupport(ProtocolVersion.V2019, true)
                .addVersionChange(ProtocolVersion.V2013, "增强了设备信息字段")
                .addVersionChange(ProtocolVersion.V2019, "支持更长的设备ID"));
        
        // 多媒体数据上传 (0x0801) - 2013版本引入
        messageVersionInfo.put(0x0801, new MessageVersionInfo(0x0801, "多媒体数据上传")
                .addVersionSupport(ProtocolVersion.V2011, false)
                .addVersionSupport(ProtocolVersion.V2013, true)
                .addVersionSupport(ProtocolVersion.V2019, true)
                .addVersionChange(ProtocolVersion.V2013, "新增消息类型")
                .addVersionChange(ProtocolVersion.V2019, "优化了数据传输格式"));
    }
    
    /**
     * 添加字段映射
     */
    public void addFieldMapping(String fieldPath, 
                               ProtocolVersion sourceVersion, String sourceField, FieldType sourceType,
                               ProtocolVersion targetVersion, String targetField, FieldType targetType) {
        String key = fieldPath + ":" + sourceVersion + "->" + targetVersion;
        FieldMapping mapping = new FieldMapping(fieldPath, 
                sourceVersion, sourceField, sourceType,
                targetVersion, targetField, targetType);
        fieldMappings.put(key, mapping);
        
        logger.debug("Added field mapping: {}", mapping);
    }
    
    /**
     * 检查消息是否在指定版本中受支持
     */
    public boolean isMessageSupported(int messageId, ProtocolVersion version) {
        MessageVersionInfo info = messageVersionInfo.get(messageId);
        if (info == null) {
            // 如果没有版本信息，假设在所有版本中都支持
            return true;
        }
        return info.isSupportedIn(version);
    }
    
    /**
     * 获取消息的版本变更信息
     */
    public List<String> getMessageVersionChanges(int messageId, ProtocolVersion version) {
        MessageVersionInfo info = messageVersionInfo.get(messageId);
        if (info == null) {
            return Collections.emptyList();
        }
        return info.getChangesIn(version);
    }
    
    /**
     * 转换消息以适配目标版本
     */
    public JsonObject adaptMessageForVersion(JT808Message message, ProtocolVersion targetVersion) {
        JsonObject result = new JsonObject();
        
        // 基础字段转换
        result.put("messageId", message.getHeader().getMessageId());
        result.put("terminalPhone", message.getHeader().getPhoneNumber());
        result.put("serialNumber", message.getHeader().getSerialNumber());
        result.put("targetVersion", targetVersion.getVersionString());
        
        // 根据目标版本添加或移除字段
        if (targetVersion.supportsFeature(ProtocolVersion.ProtocolFeature.PROTOCOL_VERSION_IDENTIFICATION)) {
            result.put("protocolVersion", targetVersion.getYear());
        }
        
        // 应用字段映射
        applyFieldMappings(message, result, targetVersion);
        
        logger.debug("Adapted message {} for version {}", 
                Integer.toHexString(message.getHeader().getMessageId()).toUpperCase(), 
                targetVersion);
        
        return result;
    }
    
    /**
     * 应用字段映射
     */
    private void applyFieldMappings(JT808Message message, JsonObject result, ProtocolVersion targetVersion) {
        String messageType = message.getClass().getSimpleName();
        
        for (FieldMapping mapping : fieldMappings.values()) {
            if (mapping.getFieldPath().startsWith(messageType) && 
                mapping.getTargetVersion() != null &&
                mapping.getTargetVersion().equals(targetVersion)) {
                
                // 这里应该根据具体的消息类型和字段进行转换
                // 由于这是一个通用的框架，具体的字段转换逻辑需要在子类中实现
                logger.debug("Applied field mapping: {}", mapping);
            }
        }
    }
    
    /**
     * 检查两个版本之间的兼容性
     */
    public CompatibilityResult checkCompatibility(ProtocolVersion sourceVersion, ProtocolVersion targetVersion) {
        if (!supportedVersions.contains(sourceVersion) || !supportedVersions.contains(targetVersion)) {
            return new CompatibilityResult(false, "不支持的协议版本", Collections.emptyList());
        }
        
        if (sourceVersion.equals(targetVersion)) {
            return new CompatibilityResult(true, "版本相同", Collections.emptyList());
        }
        
        List<String> warnings = new ArrayList<>();
        boolean compatible = true;
        
        ProtocolVersion.VersionDifference difference = targetVersion.getDifference(sourceVersion);
        
        switch (difference) {
            case MAJOR_DOWNGRADE:
                compatible = false;
                warnings.add("主版本降级可能导致功能丢失");
                break;
            case MINOR_DOWNGRADE:
                warnings.add("次版本降级可能导致部分功能不可用");
                break;
            case MAJOR_UPGRADE:
                warnings.add("主版本升级可能包含不兼容的变更");
                break;
            case MINOR_UPGRADE:
                warnings.add("次版本升级通常向后兼容");
                break;
        }
        
        return new CompatibilityResult(compatible, difference.getDescription(), warnings);
    }
    
    /**
     * 获取支持的版本列表
     */
    public Set<ProtocolVersion> getSupportedVersions() {
        return new HashSet<>(supportedVersions);
    }
    
    /**
     * 添加支持的版本
     */
    public void addSupportedVersion(ProtocolVersion version) {
        supportedVersions.add(version);
        logger.info("Added supported version: {}", version);
    }
    
    /**
     * 字段映射类
     */
    public static class FieldMapping {
        private final String fieldPath;
        private final ProtocolVersion sourceVersion;
        private final String sourceField;
        private final FieldType sourceType;
        private final ProtocolVersion targetVersion;
        private final String targetField;
        private final FieldType targetType;
        
        public FieldMapping(String fieldPath,
                           ProtocolVersion sourceVersion, String sourceField, FieldType sourceType,
                           ProtocolVersion targetVersion, String targetField, FieldType targetType) {
            this.fieldPath = fieldPath;
            this.sourceVersion = sourceVersion;
            this.sourceField = sourceField;
            this.sourceType = sourceType;
            this.targetVersion = targetVersion;
            this.targetField = targetField;
            this.targetType = targetType;
        }
        
        // Getters
        public String getFieldPath() { return fieldPath; }
        public ProtocolVersion getSourceVersion() { return sourceVersion; }
        public String getSourceField() { return sourceField; }
        public FieldType getSourceType() { return sourceType; }
        public ProtocolVersion getTargetVersion() { return targetVersion; }
        public String getTargetField() { return targetField; }
        public FieldType getTargetType() { return targetType; }
        
        @Override
        public String toString() {
            return String.format("FieldMapping{%s: %s.%s(%s) -> %s.%s(%s)}",
                    fieldPath, sourceVersion, sourceField, sourceType,
                    targetVersion, targetField, targetType);
        }
    }
    
    /**
     * 字段类型枚举
     */
    public enum FieldType {
        UINT8, UINT16, UINT32, UINT64,
        INT8, INT16, INT32, INT64,
        BCD_6, BCD_10, BCD_12,
        STRING, BYTES
    }
    
    /**
     * 消息版本信息
     */
    public static class MessageVersionInfo {
        private final int messageId;
        private final String description;
        private final Map<ProtocolVersion, Boolean> versionSupport;
        private final Map<ProtocolVersion, List<String>> versionChanges;
        
        public MessageVersionInfo(int messageId, String description) {
            this.messageId = messageId;
            this.description = description;
            this.versionSupport = new HashMap<>();
            this.versionChanges = new HashMap<>();
        }
        
        public MessageVersionInfo addVersionSupport(ProtocolVersion version, boolean supported) {
            versionSupport.put(version, supported);
            return this;
        }
        
        public MessageVersionInfo addVersionChange(ProtocolVersion version, String change) {
            versionChanges.computeIfAbsent(version, k -> new ArrayList<>()).add(change);
            return this;
        }
        
        public boolean isSupportedIn(ProtocolVersion version) {
            return versionSupport.getOrDefault(version, true);
        }
        
        public List<String> getChangesIn(ProtocolVersion version) {
            return versionChanges.getOrDefault(version, Collections.emptyList());
        }
        
        // Getters
        public int getMessageId() { return messageId; }
        public String getDescription() { return description; }
    }
    
    /**
     * 兼容性检查结果
     */
    public static class CompatibilityResult {
        private final boolean compatible;
        private final String description;
        private final List<String> warnings;
        
        public CompatibilityResult(boolean compatible, String description, List<String> warnings) {
            this.compatible = compatible;
            this.description = description;
            this.warnings = warnings != null ? warnings : Collections.emptyList();
        }
        
        // Getters
        public boolean isCompatible() { return compatible; }
        public String getDescription() { return description; }
        public List<String> getWarnings() { return warnings; }
        
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        
        @Override
        public String toString() {
            return String.format("CompatibilityResult{compatible=%s, description='%s', warnings=%d}",
                    compatible, description, warnings.size());
        }
    }
}