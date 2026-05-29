package com.ruoyi.tuyt.business.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.assessment.entity.AssessTemplateItem;
import com.ruoyi.tuyt.business.assessment.mapper.AssessTemplateItemMapper;
import com.ruoyi.tuyt.business.assessment.service.IAssessTemplateItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssessTemplateItemServiceImpl extends ServiceImpl<AssessTemplateItemMapper, AssessTemplateItem> implements IAssessTemplateItemService {

    @Override
    public List<AssessTemplateItem> listByTemplateId(Long templateId) {
        return list(new LambdaQueryWrapper<AssessTemplateItem>()
                .eq(AssessTemplateItem::getTemplateId, templateId)
                .orderByAsc(AssessTemplateItem::getSortOrder));
    }

    @Override
    @Transactional
    public void saveItems(Long templateId, List<AssessTemplateItem> items) {
        // 清除旧项
        remove(new LambdaQueryWrapper<AssessTemplateItem>().eq(AssessTemplateItem::getTemplateId, templateId));
        // 批量插入新项
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setTemplateId(templateId);
            items.get(i).setSortOrder(i + 1);
        }
        saveBatch(items);
    }

    @Override
    public void deleteByTemplateId(Long templateId) {
        remove(new LambdaQueryWrapper<AssessTemplateItem>().eq(AssessTemplateItem::getTemplateId, templateId));
    }
}
