package com.ruoyi.tuyt.business.enterprise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.enterprise.entity.Enterprise;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface IEnterpriseService extends IService<Enterprise> {
    PageResult<Enterprise> queryPage(String keyword, String superviseType, String enterpriseType, Long gridId, String status, String excludeStatus, Integer pageNum, Integer pageSize);
    Enterprise getById(Long id);
    void add(Enterprise enterprise);
    void update(Enterprise enterprise);
    void delete(List<Long> ids);
}
