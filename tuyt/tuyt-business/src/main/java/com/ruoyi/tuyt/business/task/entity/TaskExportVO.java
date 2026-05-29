package com.ruoyi.tuyt.business.task.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 任务台账导出 VO — 定义 Excel 列顺序和表头
 */
@Data
public class TaskExportVO {

    @ExcelProperty("任务单号")
    @ColumnWidth(18)
    private String taskNo;

    @ExcelProperty("任务标题")
    @ColumnWidth(28)
    private String taskTitle;

    @ExcelProperty("任务类型")
    @ColumnWidth(12)
    private String taskType;

    @ExcelProperty("紧急程度")
    @ColumnWidth(10)
    private String urgency;

    @ExcelProperty("开始时间")
    @ColumnWidth(18)
    private String startTime;

    @ExcelProperty("截止时间")
    @ColumnWidth(18)
    private String deadline;

    @ExcelProperty("派发时间")
    @ColumnWidth(18)
    private String dispatchTime;

    @ExcelProperty("发起人")
    @ColumnWidth(12)
    private String initiatorName;

    @ExcelProperty("处理单位")
    @ColumnWidth(16)
    private String handlerUnitName;

    @ExcelProperty("处理人")
    @ColumnWidth(12)
    private String handlerName;

    @ExcelProperty("任务状态")
    @ColumnWidth(10)
    private String status;

    @ExcelProperty("完成时间")
    @ColumnWidth(18)
    private String finishTime;

    @ExcelProperty("检查模板")
    @ColumnWidth(18)
    private String checkTemplate;

    @ExcelProperty("任务内容")
    @ColumnWidth(36)
    private String taskContent;

    @ExcelProperty("抄送人")
    @ColumnWidth(16)
    private String ccUsers;

    @ExcelProperty("企业名称")
    @ColumnWidth(22)
    private String enterpriseName;

    @ExcelProperty("企业地址")
    @ColumnWidth(30)
    private String enterpriseAddress;

    @ExcelProperty("关联问题编号")
    @ColumnWidth(18)
    private String problemNo;

    @ExcelProperty("催办/督办记录")
    @ColumnWidth(40)
    private String urgeSuperviseHistory;

    @ExcelProperty("处理信息")
    @ColumnWidth(40)
    private String processInfo;
}
