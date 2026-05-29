package com.ruoyi.tuyt.business.problem.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 问题台账导出 VO
 */
@Data
public class ProblemExportVO {

    @ExcelProperty("问题编号")
    @ColumnWidth(18)
    private String problemNo;

    @ExcelProperty("问题等级")
    @ColumnWidth(12)
    private String problemLevel;

    @ExcelProperty("报警时间")
    @ColumnWidth(20)
    private String alarmTime;

    @ExcelProperty("问题来源")
    @ColumnWidth(12)
    private String problemSource;

    @ExcelProperty("问题类型")
    @ColumnWidth(12)
    private String problemType;

    @ExcelProperty("污染类型")
    @ColumnWidth(14)
    private String pollutionType;

    @ExcelProperty("问题描述")
    @ColumnWidth(40)
    private String problemDesc;

    @ExcelProperty("事发地址")
    @ColumnWidth(30)
    private String address;

    @ExcelProperty("事发企业")
    @ColumnWidth(30)
    private String enterpriseName;

    @ExcelProperty("所属区域")
    @ColumnWidth(16)
    private String areaName;

    @ExcelProperty("处理状态")
    @ColumnWidth(12)
    private String handleStatus;

    @ExcelProperty("处罚状态")
    @ColumnWidth(12)
    private String penaltyStatus;

    @ExcelProperty("关闭原因")
    @ColumnWidth(30)
    private String closeReason;

    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    private String createTime;
}
