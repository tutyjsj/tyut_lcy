package com.ruoyi.tuyt.business.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.task.entity.TaskProcess;
import com.ruoyi.tuyt.business.task.mapper.TaskProcessMapper;
import com.ruoyi.tuyt.business.task.service.ITaskProcessService;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskProcessServiceImpl extends ServiceImpl<TaskProcessMapper, TaskProcess> implements ITaskProcessService {

    @Override
    public PageResult<TaskProcess> queryPage(Long taskId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<TaskProcess> wrapper = new LambdaQueryWrapper<>();
        if (taskId != null) {
            wrapper.eq(TaskProcess::getTaskId, taskId);
        }
        wrapper.orderByDesc(TaskProcess::getCreateTime);
        Page<TaskProcess> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public TaskProcess getById(Long id) {
        TaskProcess process = super.getById(id);
        if (process == null) throw new BusinessException("处理记录不存在");
        return process;
    }

    @Override
    @Transactional
    public void add(TaskProcess process) { save(process); }

    @Override
    @Transactional
    public void update(TaskProcess process) {
        if (!updateById(process)) throw new BusinessException("处理记录不存在");
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) { removeByIds(ids); }
}
