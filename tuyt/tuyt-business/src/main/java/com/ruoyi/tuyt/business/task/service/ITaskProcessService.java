package com.ruoyi.tuyt.business.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.task.entity.TaskProcess;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface ITaskProcessService extends IService<TaskProcess> {
    PageResult<TaskProcess> queryPage(Long taskId, Integer pageNum, Integer pageSize);
    TaskProcess getById(Long id);
    void add(TaskProcess process);
    void update(TaskProcess process);
    void delete(List<Long> ids);
}
