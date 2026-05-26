package com.ruoyi.tuyt.business.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.task.entity.TaskInfo;
import com.ruoyi.tuyt.business.task.mapper.TaskInfoMapper;
import com.ruoyi.tuyt.business.task.service.ITaskInfoService;
import com.ruoyi.tuyt.common.enums.TaskStatusEnum;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TaskInfoServiceImpl extends ServiceImpl<TaskInfoMapper, TaskInfo> implements ITaskInfoService {

    @Override
    public PageResult<TaskInfo> queryPage(String keyword, String status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(TaskInfo::getTaskNo, keyword).or()
                   .like(TaskInfo::getTaskTitle, keyword);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(TaskInfo::getStatus, status);
        }
        wrapper.orderByDesc(TaskInfo::getCreateTime);
        Page<TaskInfo> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public TaskInfo getById(Long id) {
        TaskInfo task = super.getById(id);
        if (task == null) throw new BusinessException("任务不存在");
        return task;
    }

    @Override
    @Transactional
    public void dispatch(TaskInfo task) {
        task.setStatus(TaskStatusEnum.DISPATCHED.name());
        task.setDispatchTime(LocalDateTime.now());
        save(task);
    }

    @Override
    @Transactional
    public void urge(Long id) {
        TaskInfo task = getById(id);
        // 催办逻辑仅记录操作，由 Controller 层处理
        updateById(task);
    }

    @Override
    @Transactional
    public void supervise(Long id) {
        TaskInfo task = getById(id);
        // 督办逻辑仅记录操作
        updateById(task);
    }

    @Override
    @Transactional
    public void revoke(Long id) {
        TaskInfo task = getById(id);
        if (!TaskStatusEnum.DISPATCHED.name().equals(task.getStatus())) {
            throw new BusinessException("只有已派发状态的任务可以撤销");
        }
        task.setStatus(TaskStatusEnum.REVOKED.name());
        updateById(task);
    }

    @Override
    @Transactional
    public void returnTask(Long id, String reason) {
        TaskInfo task = getById(id);
        if (TaskStatusEnum.DISPATCHED.name().equals(task.getStatus())
                || TaskStatusEnum.SIGNED.name().equals(task.getStatus())) {
            task.setStatus(TaskStatusEnum.RETURNED.name());
            updateById(task);
        } else {
            throw new BusinessException("当前状态不允许退回");
        }
    }

    @Override
    @Transactional
    public void auditReturn(Long id, String result) {
        TaskInfo task = getById(id);
        if (TaskStatusEnum.RETURNED.name().equals(task.getStatus())) {
            task.setStatus(TaskStatusEnum.REVOKED.name());
            updateById(task);
        } else {
            throw new BusinessException("只有已退回的任务可以审核");
        }
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) { removeByIds(ids); }

    @Override
    @Transactional
    public void processTask(Long id, Map<String, Object> data) {
        TaskInfo task = getById(id);
        if (!TaskStatusEnum.DISPATCHED.name().equals(task.getStatus())) {
            throw new BusinessException("只有已派发状态的任务可以处理");
        }
        task.setStatus(TaskStatusEnum.DONE.name());
        task.setFinishTime(LocalDateTime.now());
        // 前端提交的处理结果存储到 task_content 中
        String conclusion = (String) data.getOrDefault("conclusion", "");
        String suggestion = (String) data.getOrDefault("suggestion", "");
        String rectifyStatus = (String) data.getOrDefault("rectifyStatus", "");
        String productionStatus = (String) data.getOrDefault("productionStatus", "");
        String remark = (String) data.getOrDefault("remark", "");
        String content = task.getTaskContent();
        task.setTaskContent((content != null ? content + "\n" : "") +
                "=== 处理结果 ===\n" +
                "检查结论: " + conclusion + "\n" +
                "处置建议: " + suggestion + "\n" +
                "整改情况: " + rectifyStatus + "\n" +
                "生产经营: " + productionStatus + "\n" +
                "备注: " + remark);
        updateById(task);
    }
}
