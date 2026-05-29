package com.ruoyi.tuyt.business.contact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.contact.entity.Contact;
import com.ruoyi.tuyt.business.contact.mapper.ContactMapper;
import com.ruoyi.tuyt.business.contact.service.IContactService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ContactServiceImpl extends ServiceImpl<ContactMapper, Contact> implements IContactService {

    @Override
    public PageResult<Contact> queryPage(String keyword, String contactType, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Contact> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(Contact::getName, keyword)
                .or().like(Contact::getPhone, keyword)
                .or().like(Contact::getOrgName, keyword)
                .or().like(Contact::getPosition, keyword)
            );
        }
        if (StringUtils.hasText(contactType)) {
            wrapper.eq(Contact::getContactType, contactType);
        }
        wrapper.orderByAsc(Contact::getCreateTime);
        Page<Contact> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public Contact getById(Long id) {
        Contact contact = super.getById(id);
        if (contact == null) throw new BusinessException("联系人不存在");
        return contact;
    }

    @Override
    @Transactional
    public void add(Contact contact) { save(contact); }

    @Override
    @Transactional
    public void update(Contact contact) {
        if (!updateById(contact)) throw new BusinessException("联系人不存在");
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) { removeByIds(ids); }
}
