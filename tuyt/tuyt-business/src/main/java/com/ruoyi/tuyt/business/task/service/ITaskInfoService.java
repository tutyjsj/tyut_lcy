package com.ruoyi.tuyt.business.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.task.entity.TaskInfo;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;
import java.util.Map;

public interface ITaskInfoService extends IService<TaskInfo> {
    PageResult<TaskInfo> queryPage(String keyword, String status, Integer pageNum, Integer pageSize);
    TaskInfo getById(Long id);
    void dispatch(TaskInfo task);
    void urge(Long id);
    void supervise(Long id);
    void revoke(Long id);
    void returnTask(Long id, String reason);
    void auditReturn(Long id, String result);
    void delete(List<Long> ids);
    void processTask(Long id, Map<String, Object> data);
}
