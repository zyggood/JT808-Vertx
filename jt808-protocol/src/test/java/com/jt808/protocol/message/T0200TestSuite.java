package com.jt808.protocol.message;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * T0200位置信息汇报测试套件
 * 包含所有T0200相关的测试类
 */
@Suite
@SuiteDisplayName("T0200位置信息汇报完整测试套件")
@SelectClasses({
    T0200LocationReportTest.class,
    AlarmFlagTest.class,
    StatusFlagTest.class,
    AdditionalInfoTest.class,
    T0200IntegrationTest.class,
    T0200EncodingDecodingTest.class,
    T0200PerformanceTest.class
})
public class T0200TestSuite {
    // 测试套件类，用于组织和运行所有T0200相关测试
    // 使用JUnit 5 Platform Suite的测试套件功能
    
    /*
     * 测试覆盖范围：
     * 
     * 1. T0200LocationReportTest - 基础功能测试
     *    - 基本位置数据的设置和获取
     *    - 报警标志位的基本功能
     *    - 状态标志位的基本功能
     *    - 附加信息的基本解析
     *    - toString方法的基本功能
     * 
     * 2. AlarmFlagTest - 报警标志位专项测试
     *    - 32位报警标志位的详细测试
     *    - 单个报警位的测试
     *    - 多个报警位组合的测试
     *    - 报警描述获取的测试
     *    - 特殊报警类型的测试
     * 
     * 3. StatusFlagTest - 状态标志位专项测试
     *    - 32位状态标志位的详细测试
     *    - ACC状态、定位状态等基本状态
     *    - 经纬度方向、运营状态等扩展状态
     *    - 车辆电路、车门状态等车辆状态
     *    - GNSS定位系统状态
     *    - 状态位组合和边界值测试
     * 
     * 4. AdditionalInfoTest - 附加信息专项测试
     *    - 里程、油量、速度等基本附加信息
     *    - 报警事件、超速报警等报警相关附加信息
     *    - 扩展车辆信号状态位、IO状态位等状态附加信息
     *    - 模拟量、信号强度、卫星数等环境附加信息
     *    - 附加信息格式化显示
     *    - 空、无效、未知ID附加信息处理
     * 
     * 5. T0200IntegrationTest - 集成测试
     *    - 完整位置汇报场景测试
     *    - 紧急情况场景测试
     *    - 正常行驶场景测试
     *    - 停车场景测试
     *    - 数据一致性测试
     *    - 边界值测试
     *    - 基础性能测试
     * 
     * 6. T0200EncodingDecodingTest - 编解码测试
     *    - 基本位置信息的编解码
     *    - 附加信息的编解码
     *    - 复杂附加信息的编解码
     *    - 空附加信息的编解码
     *    - 无效附加信息的编解码
     *    - 未知ID附加信息的编解码
     * 
     * 7. T0200PerformanceTest - 性能测试
     *    - 基本操作性能测试
     *    - 附加信息解析性能测试
     *    - toString方法性能测试
     *    - 报警描述获取性能测试
     *    - 高负载性能测试
     *    - 并发访问性能测试
     *    - 内存使用测试
     *    - 大量附加信息性能测试
     */
}