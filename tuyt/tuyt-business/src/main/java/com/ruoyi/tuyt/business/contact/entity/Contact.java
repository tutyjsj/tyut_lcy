package com.ruoyi.tuyt.business.contact.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.tuyt.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("contact")
public class Contact extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String contactType;
    private String name;
    private String phone;
    private Long orgId;
    private String orgName;
    private String position;
    private String photo;
}
