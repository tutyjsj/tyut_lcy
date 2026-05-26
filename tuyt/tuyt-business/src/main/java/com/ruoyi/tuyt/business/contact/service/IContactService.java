package com.ruoyi.tuyt.business.contact.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.contact.entity.Contact;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface IContactService extends IService<Contact> {
    PageResult<Contact> queryPage(String keyword, String contactType, Integer pageNum, Integer pageSize);
    Contact getById(Long id);
    void add(Contact contact);
    void update(Contact contact);
    void delete(List<Long> ids);
}
