package com.ruoyi.tuyt.business.checkitem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.checkitem.entity.CheckItem;
import com.ruoyi.tuyt.business.checkitem.mapper.CheckItemMapper;
import com.ruoyi.tuyt.business.checkitem.service.ICheckItemService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CheckItemServiceImpl extends ServiceImpl<CheckItemMapper, CheckItem> implements ICheckItemService {

    @Override
    public PageResult<CheckItem> queryPage(String keyword, String itemType, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<CheckItem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(CheckItem::getItemName, keyword);
        }
        if (StringUtils.hasText(itemType)) {
            wrapper.eq(CheckItem::getItemType, itemType);
        }
        wrapper.orderByAsc(CheckItem::getCreateTime);
        Page<CheckItem> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public CheckItem getById(Long id) {
        CheckItem item = super.getById(id);
        if (item == null) throw new BusinessException("检查项不存在");
        return item;
    }

    @Override
    @Transactional
    public void add(CheckItem item) { save(item); }

    @Override
    @Transactional
    public void update(CheckItem item) {
        if (!updateById(item)) throw new BusinessException("检查项不存在");
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) { removeByIds(ids); }
}
