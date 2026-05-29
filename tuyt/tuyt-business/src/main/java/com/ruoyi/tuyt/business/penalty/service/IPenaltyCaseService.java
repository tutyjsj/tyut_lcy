package com.ruoyi.tuyt.business.penalty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.penalty.entity.PenaltyCase;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface IPenaltyCaseService extends IService<PenaltyCase> {

    /** 分页查询 */
    PageResult<PenaltyCase> queryPage(String caseNo, String status, String penaltyType,
                                       Integer pageNum, Integer pageSize);

    /** 根据ID获取详情 */
    PenaltyCase getDetail(Long id);

    /** 新增立案 */
    void file(PenaltyCase penaltyCase);

    /** 更新案件 */
    void updateCase(PenaltyCase penaltyCase);

    /** 作出处罚裁决 */
    void rule(Long id, String rulingResult);

    /** 结案 */
    void close(Long id);

    /** 删除（支持批量） */
    void deleteCases(List<Long> ids);
}
