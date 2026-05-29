package com.ruoyi.tuyt.business.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.assessment.entity.AssessTemplateItem;

import java.util.List;

public interface IAssessTemplateItemService extends IService<AssessTemplateItem> {

    /** 获取模板下的所有考评项 */
    List<AssessTemplateItem> listByTemplateId(Long templateId);

    /** 批量保存模板项 */
    void saveItems(Long templateId, List<AssessTemplateItem> items);

    /** 删除模板下的所有项 */
    void deleteByTemplateId(Long templateId);
}
