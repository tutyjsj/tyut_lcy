package com.ruoyi.tuyt.business.grid.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("grid_info")
public class GridInfo extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String gridName;
    private Integer gridLevel;
    private Long parentId;
    private Long orgId;
    private String leader;
    private String responsiblePerson;
    private String responsiblePhone;
    private String polygonData;
    private Integer status;
}
