package com.ruoyi.tuyt.business.grid.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("grid_enterprise")
public class GridEnterprise {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long gridId;
    private Long enterpriseId;
    private Long inspectorId;
}
