package com.ruoyi.tuyt.business.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.task.entity.TaskTransfer;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;

public interface ITaskTransferService extends IService<TaskTransfer> {
    PageResult<TaskTransfer> queryPage(Long taskId, Integer pageNum, Integer pageSize);
    TaskTransfer getById(Long id);
    void add(TaskTransfer transfer);
    void delete(List<Long> ids);
}
