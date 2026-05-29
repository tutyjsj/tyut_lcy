package com.ruoyi.tuyt.business.checktemplate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.checktemplate.entity.CheckTemplate;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

/**
 * 检查模板 Service 接口
 */
public interface ICheckTemplateService extends IService<CheckTemplate> {

    PageResult<CheckTemplate> queryPage(String keyword, Integer pageNum, Integer pageSize);

    CheckTemplate getById(Long id);

    void add(CheckTemplate template);

    void update(CheckTemplate template);

    void delete(List<Long> ids);
}
