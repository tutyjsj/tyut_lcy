package com.ruoyi.tuyt.business.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.task.entity.TaskTransfer;
import com.ruoyi.tuyt.business.task.mapper.TaskTransferMapper;
import com.ruoyi.tuyt.business.task.service.ITaskTransferService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskTransferServiceImpl extends ServiceImpl<TaskTransferMapper, TaskTransfer> implements ITaskTransferService {

    @Override
    public PageResult<TaskTransfer> queryPage(Long taskId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<TaskTransfer> wrapper = new LambdaQueryWrapper<>();
        if (taskId != null) {
            wrapper.eq(TaskTransfer::getTaskId, taskId);
        }
        wrapper.orderByDesc(TaskTransfer::getCreateTime);
        Page<TaskTransfer> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public TaskTransfer getById(Long id) {
        TaskTransfer transfer = super.getById(id);
        if (transfer == null) throw new BusinessException("流转记录不存在");
        return transfer;
    }

    @Override
    @Transactional
    public void add(TaskTransfer transfer) { save(transfer); }

    @Override
    @Transactional
    public void delete(List<Long> ids) { removeByIds(ids); }
}
