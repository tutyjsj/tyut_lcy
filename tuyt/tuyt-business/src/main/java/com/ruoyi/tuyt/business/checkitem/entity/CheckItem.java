package com.ruoyi.tuyt.business.checkitem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("check_item")
public class CheckItem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String itemName;
    private String itemType;
    private String monitorType;
    private String inputType;
    private Integer status;
}
