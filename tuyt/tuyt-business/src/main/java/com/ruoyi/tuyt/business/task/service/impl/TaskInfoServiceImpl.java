package com.ruoyi.tuyt.business.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.task.entity.TaskExportVO;
import com.ruoyi.tuyt.business.task.entity.TaskInfo;
import com.ruoyi.tuyt.business.task.entity.TaskTransfer;
import com.ruoyi.tuyt.business.task.mapper.TaskInfoMapper;
import com.ruoyi.tuyt.business.task.service.ITaskInfoService;
import com.ruoyi.tuyt.business.task.service.ITaskTransferService;
import com.ruoyi.tuyt.business.enterprise.entity.Enterprise;
import com.ruoyi.tuyt.business.enterprise.service.IEnterpriseService;
import com.ruoyi.tuyt.business.grid.entity.GridEnterprise;
import com.ruoyi.tuyt.business.grid.entity.GridInfo;
import com.ruoyi.tuyt.business.grid.mapper.GridEnterpriseMapper;
import com.ruoyi.tuyt.business.grid.service.IGridInfoService;
import com.ruoyi.tuyt.business.message.entity.MessageNotification;
import com.ruoyi.tuyt.business.message.service.IMessageNotificationService;
import com.ruoyi.tuyt.business.problem.entity.EnvProblem;
import com.ruoyi.tuyt.business.problem.mapper.EnvProblemMapper;
import com.ruoyi.tuyt.business.system.service.ISysOrganizationService;
import com.ruoyi.tuyt.common.enums.TaskStatusEnum;
import com.ruoyi.tuyt.common.exception.BusinessException;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.framework.config.LoginUserHolder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskInfoServiceImpl extends ServiceImpl<TaskInfoMapper, TaskInfo> implements ITaskInfoService {

    private final IGridInfoService gridInfoService;
    private final IEnterpriseService enterpriseService;
    private final ITaskTransferService taskTransferService;
    private final GridEnterpriseMapper gridEnterpriseMapper;
    private final EnvProblemMapper envProblemMapper;
    private final ISysOrganizationService organizationService;
    private final IMessageNotificationService messageNotificationService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PageResult<TaskInfo> queryPage(String taskNo, String title, String taskType, String status,
                                          String urgency, String overdueType, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
        // 任务编号模糊匹配
        if (StringUtils.hasText(taskNo)) {
            wrapper.like(TaskInfo::getTaskNo, taskNo);
        }
        // 任务标题模糊匹配
        if (StringUtils.hasText(title)) {
            wrapper.like(TaskInfo::getTaskTitle, title);
        }
        // 任务类型精确匹配
        if (StringUtils.hasText(taskType)) {
            wrapper.eq(TaskInfo::getTaskType, taskType);
        }
        // 任务状态精确匹配
        if (StringUtils.hasText(status)) {
            wrapper.eq(TaskInfo::getStatus, status);
        }
        // 紧急程度精确匹配
        if (StringUtils.hasText(urgency)) {
            wrapper.eq(TaskInfo::getUrgency, urgency);
        }
        // 超期类型：overdue=已超期(deadline<now), nearly=即将超期24h
        if (StringUtils.hasText(overdueType)) {
            LocalDateTime now = LocalDateTime.now();
            if ("overdue".equals(overdueType)) {
                wrapper.lt(TaskInfo::getDeadline, now);
                wrapper.ne(TaskInfo::getStatus, TaskStatusEnum.DONE.name());
            } else if ("nearly".equals(overdueType)) {
                wrapper.gt(TaskInfo::getDeadline, now);
                wrapper.le(TaskInfo::getDeadline, now.plusHours(24));
                wrapper.ne(TaskInfo::getStatus, TaskStatusEnum.DONE.name());
            }
        }
        wrapper.orderByDesc(TaskInfo::getCreateTime);
        Page<TaskInfo> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillGridName(page.getRecords());
        fillUrgeSuperviseCount(page.getRecords());
        // 运转件：填充运转时间、运转类型、运转说明
        if ("RETURNED".equals(status)) {
            fillTransferInfo(page.getRecords());
        }
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 批量填充网格名称 */
    private void fillGridName(List<TaskInfo> records) {
        Set<Long> gridIds = records.stream().map(TaskInfo::getGridId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (gridIds.isEmpty()) return;
        Map<Long, String> nameMap = gridInfoService.listByIds(gridIds).stream()
                .collect(Collectors.toMap(GridInfo::getId, GridInfo::getGridName));
        for (TaskInfo t : records) {
            if (t.getGridId() != null) {
                t.setGridName(nameMap.getOrDefault(t.getGridId(), "-"));
            }
        }
    }

    /** 从taskContent解析催办/督办次数 */
    private void fillUrgeSuperviseCount(List<TaskInfo> records) {
        for (TaskInfo t : records) {
            String content = t.getTaskContent();
            if (content == null) {
                t.setUrgeCount(0);
                t.setSuperviseCount(0);
                continue;
            }
            int urgeCount = 0, superviseCount = 0;
            int idx = 0;
            while ((idx = content.indexOf("=== 催办记录", idx)) != -1) {
                urgeCount++;
                idx += "=== 催办记录".length();
            }
            idx = 0;
            while ((idx = content.indexOf("=== 督办记录", idx)) != -1) {
                superviseCount++;
                idx += "=== 督办记录".length();
            }
            t.setUrgeCount(urgeCount);
            t.setSuperviseCount(superviseCount);
        }
    }

    /** 运转件：从 task_transfer 表批量填充运转时间、运转类型、运转说明 */
    private void fillTransferInfo(List<TaskInfo> records) {
        if (records == null || records.isEmpty()) return;
        Set<Long> taskIds = records.stream().map(TaskInfo::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (taskIds.isEmpty()) return;
        // 查询这些任务的所有流转记录，按创建时间倒序
        List<TaskTransfer> transferList = taskTransferService.list(
                new LambdaQueryWrapper<TaskTransfer>()
                        .in(TaskTransfer::getTaskId, taskIds)
                        .orderByDesc(TaskTransfer::getCreateTime));
        // 按 taskId 分组，取最新的运转记录
        Map<Long, TaskTransfer> latestMap = new HashMap<>();
        for (TaskTransfer tf : transferList) {
            latestMap.compute(tf.getTaskId(), (k, existing) ->
                    existing == null || (tf.getCreateTime() != null && existing.getCreateTime() != null
                            && tf.getCreateTime().isAfter(existing.getCreateTime())) ? tf : existing);
        }
        // 填充到 TaskInfo
        for (TaskInfo t : records) {
            TaskTransfer tf = latestMap.get(t.getId());
            if (tf != null) {
                t.setTransferType(tf.getTransferType());
                t.setTransferTime(tf.getCreateTime());
                t.setTransferReason(tf.getReason());
            }
        }
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
        // 自动生成任务编号（仅新建时）
        if (!StringUtils.hasText(task.getTaskNo())) {
            String no = "RW" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + String.format("%04d", (int) (Math.random() * 10000));
            task.setTaskNo(no);
        }
        // 设置发起人
        if (task.getInitiatorId() == null) {
            Long initiatorId = LoginUserHolder.getUserId();
            if (initiatorId != null) {
                task.setInitiatorId(initiatorId);
            }
        }
        // 区分暂存(DRAFT)与派发(DISPATCHED)
        String status = task.getStatus();
        if (TaskStatusEnum.DRAFT.name().equals(status)) {
            task.setStatus(TaskStatusEnum.DRAFT.name());
        } else {
            task.setStatus(TaskStatusEnum.DISPATCHED.name());
            task.setDispatchTime(LocalDateTime.now());
        }
        // 有id则更新，无id则新建
        saveOrUpdate(task);
    }

    @Override
    @Transactional
    public void updateTask(TaskInfo task) {
        TaskInfo exist = getById(task.getId());
        String existStatus = exist.getStatus();
        String newStatus = task.getStatus();

        if (TaskStatusEnum.DRAFT.name().equals(existStatus)) {
            // 草稿状态：允许全字段编辑，支持保存草稿或直接发布
            exist.setTaskTitle(task.getTaskTitle());
            exist.setTaskType(task.getTaskType());
            exist.setUrgency(task.getUrgency());
            exist.setDeadline(task.getDeadline());
            exist.setGridId(task.getGridId());
            exist.setTaskContent(task.getTaskContent());
            exist.setProblemId(task.getProblemId());
            exist.setStatus(newStatus);
            if (!TaskStatusEnum.DRAFT.name().equals(newStatus)) {
                exist.setDispatchTime(LocalDateTime.now());
            }
            updateById(exist);
        } else if (TaskStatusEnum.DISPATCHED.name().equals(existStatus)
                || TaskStatusEnum.SIGNED.name().equals(existStatus)) {
            // 已发布/已签收状态：只允许更新部分字段（截止时间、处理单位、内容、紧急程度）
            if (task.getDeadline() != null) exist.setDeadline(task.getDeadline());
            if (task.getGridId() != null) exist.setGridId(task.getGridId());
            if (task.getUrgency() != null) exist.setUrgency(task.getUrgency());
            if (StringUtils.hasText(task.getTaskContent())) exist.setTaskContent(task.getTaskContent());
            if (StringUtils.hasText(task.getTaskTitle())) exist.setTaskTitle(task.getTaskTitle());
            // 状态保持原样，不允许通过编辑改变状态
            updateById(exist);
        } else {
            throw new BusinessException("当前任务状态不支持编辑");
        }
    }

    @Override
    @Transactional
    public void urge(Long id, String reason) {
        TaskInfo task = getById(id);
        String status = task.getStatus();
        if (!TaskStatusEnum.DISPATCHED.name().equals(status)
                && !TaskStatusEnum.SIGNED.name().equals(status)) {
            throw new BusinessException("当前任务状态不允许催办");
        }
        // 追加催办记录到任务内容
        String log = "\n=== 催办记录 [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "] ==="
                + (reason != null && !reason.isBlank() ? "\n原因: " + reason : "");
        task.setTaskContent((task.getTaskContent() != null ? task.getTaskContent() : "") + log);
        updateById(task);

        // 创建消息通知给任务处理人
        Long currentUserId = LoginUserHolder.getUserId();
        String currentUsername = LoginUserHolder.getUsername();
        if (task.getHandlerId() != null && task.getHandlerId() > 0) {
            MessageNotification msg = new MessageNotification();
            msg.setType("URGE");
            msg.setTitle("催办：" + (task.getTaskTitle() != null ? task.getTaskTitle() : "任务"));
            msg.setContent("调度员" + (currentUsername != null ? currentUsername : "系统")
                    + "催办您尽快完成【" + task.getTaskTitle() + "】（" + task.getTaskNo() + "）任务的处理"
                    + (reason != null && !reason.isBlank() ? "，原因：" + reason : "，请立即处理。"));
            msg.setSourceName(currentUsername);
            msg.setSourceId(currentUserId);
            msg.setTargetUserId(task.getHandlerId());
            msg.setRelatedId(task.getId());
            msg.setRelatedType("task");
            msg.setReadStatus(0);
            messageNotificationService.save(msg);
        }
    }

    @Override
    @Transactional
    public void supervise(Long id, String reason) {
        TaskInfo task = getById(id);
        String status = task.getStatus();
        if (!TaskStatusEnum.DISPATCHED.name().equals(status)
                && !TaskStatusEnum.SIGNED.name().equals(status)) {
            throw new BusinessException("当前任务状态不允许督办");
        }
        // 追加督办记录到任务内容
        String log = "\n=== 督办记录 [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "] ==="
                + (reason != null && !reason.isBlank() ? "\n原因: " + reason : "");
        task.setTaskContent((task.getTaskContent() != null ? task.getTaskContent() : "") + log);
        updateById(task);

        // 创建消息通知给任务处理人
        Long currentUserId = LoginUserHolder.getUserId();
        String currentUsername = LoginUserHolder.getUsername();
        if (task.getHandlerId() != null && task.getHandlerId() > 0) {
            MessageNotification msg = new MessageNotification();
            msg.setType("SUPERVISE");
            msg.setTitle("督办：" + (task.getTaskTitle() != null ? task.getTaskTitle() : "任务"));
            msg.setContent("上级部门" + (currentUsername != null ? currentUsername : "系统")
                    + "督办您对【" + task.getTaskTitle() + "】（" + task.getTaskNo() + "）任务进行限期处理"
                    + (reason != null && !reason.isBlank() ? "，督办原因：" + reason : "，请尽快完成。"));
            msg.setSourceName(currentUsername);
            msg.setSourceId(currentUserId);
            msg.setTargetUserId(task.getHandlerId());
            msg.setRelatedId(task.getId());
            msg.setRelatedType("task");
            msg.setReadStatus(0);
            messageNotificationService.save(msg);
        }
    }

    @Override
    @Transactional
    public void revoke(Long id) {
        TaskInfo task = getById(id);
        String status = task.getStatus();
        if (!TaskStatusEnum.DISPATCHED.name().equals(status)
                && !TaskStatusEnum.SIGNED.name().equals(status)) {
            throw new BusinessException("只有已派发或已签收状态的任务可以撤销");
        }
        task.setStatus(TaskStatusEnum.REVOKED.name());
        updateById(task);
    }

    @Override
    @Transactional
    public void publish(Long id) {
        TaskInfo task = getById(id);
        if (!TaskStatusEnum.DRAFT.name().equals(task.getStatus())) {
            throw new BusinessException("只有已拟定状态的任务可以发布");
        }
        task.setStatus(TaskStatusEnum.DISPATCHED.name());
        task.setDispatchTime(LocalDateTime.now());
        updateById(task);
    }

    @Override
    @Transactional
    public void returnTask(Long id, String reason, String suggestHandler, String suggestUnit) {
        TaskInfo task = getById(id);
        if (TaskStatusEnum.DISPATCHED.name().equals(task.getStatus())
                || TaskStatusEnum.SIGNED.name().equals(task.getStatus())) {
            task.setStatus(TaskStatusEnum.RETURNED.name());
            // 追加退回记录到 task_content
            StringBuilder log = new StringBuilder();
            log.append("\n=== 退回记录 [").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("] ===");
            if (StringUtils.hasText(reason)) log.append("\n退回原因: ").append(reason);
            if (StringUtils.hasText(suggestHandler)) log.append("\n建议处理人: ").append(suggestHandler);
            if (StringUtils.hasText(suggestUnit)) log.append("\n建议单位: ").append(suggestUnit);
            task.setTaskContent((task.getTaskContent() != null ? task.getTaskContent() : "") + log.toString());
            updateById(task);
            // 创建运转记录
            TaskTransfer transfer = new TaskTransfer();
            transfer.setTaskId(task.getId());
            transfer.setTransferType("RETURN");
            transfer.setFromUserId(LoginUserHolder.getUserId());
            transfer.setToUserId(task.getInitiatorId());
            transfer.setReason(reason);
            transfer.setSuggestHandler(suggestHandler);
            transfer.setSuggestUnit(suggestUnit);
            taskTransferService.save(transfer);
        } else {
            throw new BusinessException("当前状态不允许退回");
        }
    }

    @Override
    @Transactional
    public void auditReturn(Long id, String auditResult, String auditComment) {
        TaskInfo task = getById(id);
        if (TaskStatusEnum.RETURNED.name().equals(task.getStatus())) {
            if ("APPROVED".equals(auditResult)) {
                task.setStatus(TaskStatusEnum.REVOKED.name());
            }
            // 追加审核记录
            String log = "\n=== 审核记录 [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "] ==="
                    + "\n审核结果: " + auditResult
                    + (StringUtils.hasText(auditComment) ? "\n审核意见: " + auditComment : "");
            task.setTaskContent((task.getTaskContent() != null ? task.getTaskContent() : "") + log);
            updateById(task);
        } else {
            throw new BusinessException("只有已退回的任务可以审核");
        }
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            TaskInfo task = getById(id);
            if (!TaskStatusEnum.DRAFT.name().equals(task.getStatus())) {
                throw new BusinessException("任务[" + task.getTaskNo() + "]不是已拟定状态，无法删除");
            }
        }
        removeByIds(ids);
    }

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

    // ========== 巡查计划 ==========

    @Override
    public PageResult<TaskInfo> queryPatrolPlans(String type, String title, String startTime, String cycle, String status,
                                                  Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
        // 巡查计划的任务类型为 PATROL 或 SHUTDOWN
        wrapper.in(TaskInfo::getTaskType, List.of("PATROL", "SHUTDOWN"));
        if (StringUtils.hasText(type)) {
            wrapper.eq(TaskInfo::getTaskType, type);
        }
        if (StringUtils.hasText(title)) {
            wrapper.like(TaskInfo::getTaskTitle, title);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(TaskInfo::getStatus, status);
        }
        wrapper.orderByDesc(TaskInfo::getCreateTime);
        // 先用 DB 字段筛选基础数据，再用元数据做内存过滤
        List<TaskInfo> allRecords = list(wrapper);
        for (TaskInfo t : allRecords) {
            parsePatrolPlanMeta(t);
        }
        // 内存过滤：startTime（匹配日期部分前缀）和 cycle（元数据中的周期）
        List<TaskInfo> filtered = allRecords;
        if (StringUtils.hasText(startTime)) {
            filtered = filtered.stream()
                    .filter(t -> t.getStartTime() != null
                            && t.getStartTime().toLocalDate().toString().equals(startTime))
                    .collect(Collectors.toList());
        }
        if (StringUtils.hasText(cycle)) {
            filtered = filtered.stream()
                    .filter(t -> cycle.equals(t.getCycle()))
                    .collect(Collectors.toList());
        }
        // 内存分页
        int total = filtered.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        List<TaskInfo> pageList = from < total ? filtered.subList(from, to) : Collections.emptyList();
        fillGridName(pageList);
        return PageResult.of(pageList, (long) total, (long) pageNum, (long) pageSize);
    }

    @Override
    @Transactional
    public void dispatchPatrolPlan(TaskInfo task) {
        // 巡查计划状态：ENABLED/DISABLED
        task.setStatus(StringUtils.hasText(task.getStatus()) ? task.getStatus() : "ENABLED");
        task.setTaskNo("PLAN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000)));
        if (task.getInitiatorId() == null) {
            Long initiatorId = LoginUserHolder.getUserId();
            if (initiatorId != null) task.setInitiatorId(initiatorId);
        }
        // 将巡查计划的附加字段序列化到 task_content
        task.setTaskContent(buildPatrolPlanMetaJson(task));
        save(task);
    }

    @Override
    @Transactional
    public void updatePatrolPlan(TaskInfo task) {
        TaskInfo exist = getById(task.getId());
        exist.setTaskTitle(task.getTaskTitle());
        exist.setTaskType(task.getTaskType());
        exist.setStatus(StringUtils.hasText(task.getStatus()) ? task.getStatus() : exist.getStatus());
        // 合并巡查计划元数据
        exist.setTaskContent(buildPatrolPlanMetaJson(task));
        updateById(exist);
    }

    /** 构建巡查计划的元数据 JSON（存储于 task_content，自动填充企业名称） */
    private String buildPatrolPlanMetaJson(TaskInfo task) {
        try {
            Map<String, Object> meta = new HashMap<>();
            // 保留原有描述信息
            String existingDesc = task.getTaskContent();
            if (StringUtils.hasText(existingDesc) && !existingDesc.trim().startsWith("{")) {
                meta.put("description", existingDesc);
            }
            if (task.getCycle() != null) meta.put("cycle", task.getCycle());
            if (task.getStartTime() != null) meta.put("startTime", task.getStartTime().toString());
            if (StringUtils.hasText(task.getCheckTemplateName())) meta.put("checkTemplateName", task.getCheckTemplateName());
            // 提取企业ID并自动填充企业名称
            if (task.getEnterprises() != null && !task.getEnterprises().isEmpty()) {
                List<Long> enterpriseIds = new ArrayList<>();
                for (Object e : task.getEnterprises()) {
                    if (e instanceof Map) {
                        Object idVal = ((Map<?, ?>) e).get("id");
                        if (idVal instanceof Number) enterpriseIds.add(((Number) idVal).longValue());
                    } else if (e instanceof Number) {
                        enterpriseIds.add(((Number) e).longValue());
                    }
                }
                // 查询企业信息并存储到JSON中
                if (!enterpriseIds.isEmpty()) {
                    List<Enterprise> enterprises = enterpriseService.listByIds(enterpriseIds);
                    List<Map<String, Object>> enterpriseList = enterprises.stream().map(ent -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", ent.getId());
                        map.put("enterpriseCode", ent.getEnterpriseCode());
                        map.put("enterpriseName", ent.getEnterpriseName());
                        map.put("address", ent.getAddress());
                        map.put("status", ent.getStatus());
                        map.put("enterpriseType", ent.getEnterpriseType());
                        return map;
                    }).collect(Collectors.toList());
                    meta.put("enterprises", enterpriseList);
                    meta.put("enterpriseIds", enterpriseIds); // 兼容旧格式
                }
            }
            if (task.getLastExecuteTime() != null) meta.put("lastExecuteTime", task.getLastExecuteTime().toString());
            if (task.getNextExecuteTime() != null) meta.put("nextExecuteTime", task.getNextExecuteTime().toString());
            meta.put("metadataType", "PATROL_PLAN");
            return objectMapper.writeValueAsString(meta);
        } catch (Exception e) {
            return task.getTaskContent() != null ? task.getTaskContent() : "{}";
        }
    }

    /** 从 task_content 解析巡查计划元数据 */
    private void parsePatrolPlanMeta(TaskInfo task) {
        String content = task.getTaskContent();
        if (!StringUtils.hasText(content) || !content.trim().startsWith("{")) {
            task.setCycle("-");
            task.setEnterpriseCount(0);
            task.setEnterprises(Collections.emptyList());
            return;
        }
        try {
            Map<String, Object> meta = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
            task.setCycle((String) meta.getOrDefault("cycle", "-"));
            task.setCheckTemplateName((String) meta.getOrDefault("checkTemplateName", ""));
            if (meta.containsKey("startTime") && meta.get("startTime") != null) {
                task.setStartTime(LocalDateTime.parse(meta.get("startTime").toString().replace("T", " ").substring(0, 19),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            // 解析企业信息（优先读取新格式 enterprises 字段，已包含名称；兼容旧格式 enterpriseIds）
            List<Map<String, Object>> enterpriseList = (List<Map<String, Object>>) meta.get("enterprises");
            if (enterpriseList != null && !enterpriseList.isEmpty()) {
                // 新格式：JSON中已有企业名称，直接使用，但仍需查询网格名称
                List<Long> longIds = enterpriseList.stream()
                        .map(m -> Long.valueOf(m.get("id").toString())).collect(Collectors.toList());
                List<GridEnterprise> gridEnterpriseList = gridEnterpriseMapper.selectList(
                        new LambdaQueryWrapper<GridEnterprise>().in(GridEnterprise::getEnterpriseId, longIds));
                Map<Long, Long> enterpriseGridMap = gridEnterpriseList.stream()
                        .collect(Collectors.toMap(GridEnterprise::getEnterpriseId, GridEnterprise::getGridId, (a, b) -> a));
                Set<Long> gridIdSet = new HashSet<>(enterpriseGridMap.values());
                Map<Long, String> gridNameMap = gridInfoService.listByIds(gridIdSet).stream()
                        .collect(Collectors.toMap(GridInfo::getId, GridInfo::getGridName, (a, b) -> a));
                for (Map<String, Object> map : enterpriseList) {
                    Long eid = Long.valueOf(map.get("id").toString());
                    Long gid = enterpriseGridMap.get(eid);
                    map.put("gridName", gid != null ? gridNameMap.getOrDefault(gid, "-") : "-");
                }
                task.setEnterprises(enterpriseList);
                task.setEnterpriseCount(enterpriseList.size());
            } else {
                // 旧格式兼容：从 enterpriseIds 查询企业
                List<Integer> enterpriseIds = (List<Integer>) meta.getOrDefault("enterpriseIds", Collections.emptyList());
                List<Long> longIds = enterpriseIds.stream().map(Long::valueOf).collect(Collectors.toList());
                if (!longIds.isEmpty()) {
                    List<Enterprise> enterprises = enterpriseService.listByIds(longIds);
                    List<GridEnterprise> gridEnterpriseList = gridEnterpriseMapper.selectList(
                            new LambdaQueryWrapper<GridEnterprise>().in(GridEnterprise::getEnterpriseId, longIds));
                    Map<Long, Long> enterpriseGridMap = gridEnterpriseList.stream()
                            .collect(Collectors.toMap(GridEnterprise::getEnterpriseId, GridEnterprise::getGridId, (a, b) -> a));
                    Set<Long> gridIdSet = new HashSet<>(enterpriseGridMap.values());
                    Map<Long, String> gridNameMap = gridInfoService.listByIds(gridIdSet).stream()
                            .collect(Collectors.toMap(GridInfo::getId, GridInfo::getGridName, (a, b) -> a));
                    task.setEnterprises(enterprises.stream().map(e -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", e.getId());
                        map.put("enterpriseCode", e.getEnterpriseCode());
                        map.put("enterpriseName", e.getEnterpriseName());
                        map.put("address", e.getAddress());
                        map.put("status", e.getStatus());
                        map.put("enterpriseType", e.getEnterpriseType());
                        Long gid = enterpriseGridMap.get(e.getId());
                        map.put("gridName", gid != null ? gridNameMap.getOrDefault(gid, "-") : "-");
                        return map;
                    }).collect(Collectors.toList()));
                    task.setEnterpriseCount(task.getEnterprises().size());
                } else {
                    task.setEnterpriseCount(0);
                    task.setEnterprises(Collections.emptyList());
                }
            }
        } catch (Exception e) {
            task.setCycle("-");
            task.setEnterpriseCount(0);
            task.setEnterprises(Collections.emptyList());
        }
    }

    // ========== 任务导出 ==========

    @Override
    public List<TaskExportVO> exportTasks(String taskNo, String title, String taskType,
                                          String status, String urgency, String overdueType) {
        // 1. 构建查询条件（与 queryPage 一致，但不分页）
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(taskNo)) {
            wrapper.like(TaskInfo::getTaskNo, taskNo);
        }
        if (StringUtils.hasText(title)) {
            wrapper.like(TaskInfo::getTaskTitle, title);
        }
        if (StringUtils.hasText(taskType)) {
            wrapper.eq(TaskInfo::getTaskType, taskType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(TaskInfo::getStatus, status);
        }
        if (StringUtils.hasText(urgency)) {
            wrapper.eq(TaskInfo::getUrgency, urgency);
        }
        if (StringUtils.hasText(overdueType)) {
            LocalDateTime now = LocalDateTime.now();
            if ("overdue".equals(overdueType)) {
                wrapper.lt(TaskInfo::getDeadline, now);
                wrapper.ne(TaskInfo::getStatus, TaskStatusEnum.DONE.name());
            } else if ("nearly".equals(overdueType)) {
                wrapper.gt(TaskInfo::getDeadline, now);
                wrapper.le(TaskInfo::getDeadline, now.plusHours(24));
                wrapper.ne(TaskInfo::getStatus, TaskStatusEnum.DONE.name());
            }
        }
        wrapper.orderByDesc(TaskInfo::getCreateTime);
        List<TaskInfo> tasks = list(wrapper);

        // 2. 批量填充关联数据
        fillGridName(tasks);
        fillUrgeSuperviseCount(tasks);

        // 收集关联ID
        Set<Long> enterpriseIds = tasks.stream().map(TaskInfo::getEnterpriseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> problemIds = tasks.stream().map(TaskInfo::getProblemId).filter(Objects::nonNull).collect(Collectors.toSet());

        // 批量查询企业信息
        Map<Long, Enterprise> enterpriseMap = enterpriseIds.isEmpty() ? Collections.emptyMap()
                : enterpriseService.listByIds(enterpriseIds).stream().collect(Collectors.toMap(Enterprise::getId, e -> e, (a, b) -> a));

        // 批量查询问题信息
        Map<Long, String> problemNoMap = problemIds.isEmpty() ? Collections.emptyMap()
                : envProblemMapper.selectList(new LambdaQueryWrapper<EnvProblem>().in(EnvProblem::getId, problemIds))
                        .stream().collect(Collectors.toMap(EnvProblem::getId, EnvProblem::getProblemNo, (a, b) -> a));

        // 3. 映射为导出VO
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return tasks.stream().map(t -> {
            TaskExportVO vo = new TaskExportVO();
            vo.setTaskNo(t.getTaskNo());
            vo.setTaskTitle(t.getTaskTitle());
            vo.setTaskType(mapTaskType(t.getTaskType()));
            vo.setUrgency(mapUrgency(t.getUrgency()));
            vo.setStartTime(t.getStartTime() != null ? t.getStartTime().format(dtf) : "");
            vo.setDeadline(t.getDeadline() != null ? t.getDeadline().format(dtf) : "");
            vo.setDispatchTime(t.getDispatchTime() != null ? t.getDispatchTime().format(dtf) : "");
            vo.setInitiatorName(t.getInitiatorId() != null ? "用户" + t.getInitiatorId() : "");
            vo.setHandlerUnitName(t.getGridName() != null ? t.getGridName() : "");
            vo.setHandlerName(t.getHandlerId() != null ? "用户" + t.getHandlerId() : "");
            vo.setStatus(mapStatus(t.getStatus()));
            vo.setFinishTime(t.getFinishTime() != null ? t.getFinishTime().format(dtf) : "");
            vo.setCheckTemplate(t.getCheckTemplateId() != null ? "模板" + t.getCheckTemplateId() : "");

            // 任务内容（去掉巡查计划元数据JSON）
            String content = t.getTaskContent();
            if (content != null && content.trim().startsWith("{")) {
                try {
                    Map<String, Object> meta = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
                    content = (String) meta.getOrDefault("description", content);
                } catch (Exception ignored) {}
            }
            vo.setTaskContent(content);
            vo.setCcUsers(t.getCcUsers());

            // 企业信息
            Enterprise ent = enterpriseMap.get(t.getEnterpriseId());
            vo.setEnterpriseName(ent != null ? ent.getEnterpriseName() : "");
            vo.setEnterpriseAddress(ent != null ? (ent.getAddress() != null ? ent.getAddress() : "") : "");

            // 关联问题编号
            vo.setProblemNo(t.getProblemId() != null ? problemNoMap.getOrDefault(t.getProblemId(), "问题" + t.getProblemId()) : "");

            // 催办/督办记录
            vo.setUrgeSuperviseHistory(extractUrgeSuperviseHistory(t.getTaskContent()));

            // 处理信息（从 taskContent 解析）
            vo.setProcessInfo(extractProcessInfo(t.getTaskContent()));

            return vo;
        }).collect(Collectors.toList());
    }

    private String mapTaskType(String type) {
        if (type == null) return "";
        return switch (type) {
            case "PATROL" -> "日常巡查";
            case "SPECIAL" -> "专项检查";
            case "CHECK" -> "检查";
            case "COMPLAINT" -> "信访投诉";
            case "EMERGENCY" -> "应急处理";
            case "RECTIFY" -> "整改复查";
            case "RECHECK" -> "复查";
            case "SHUTDOWN" -> "停产巡查";
            default -> type;
        };
    }

    private String mapUrgency(String urgency) {
        if (urgency == null) return "";
        return switch (urgency) {
            case "NORMAL" -> "一般";
            case "URGENT" -> "紧急";
            case "CRITICAL" -> "特急";
            default -> urgency;
        };
    }

    private String mapStatus(String status) {
        if (status == null) return "";
        return switch (status) {
            case "DRAFT" -> "已拟定";
            case "DISPATCHED" -> "已派发";
            case "SIGNED" -> "已签收";
            case "DONE" -> "已完成";
            case "REVOKED" -> "已撤销";
            case "RETURNED" -> "已退回";
            default -> status;
        };
    }

    /** 提取催办/督办历史摘要 */
    private String extractUrgeSuperviseHistory(String content) {
        if (!StringUtils.hasText(content)) return "";
        StringBuilder sb = new StringBuilder();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "=== (催办|督办)记录 \\[([^\\]]+)\\] ===\\n?(?:原因:\\s*(.*?))?(?=\\n===|\\n?$)").matcher(content);
        while (m.find()) {
            if (!sb.isEmpty()) sb.append("; ");
            sb.append(m.group(1)).append("[").append(m.group(2)).append("]");
            if (m.group(3) != null && !m.group(3).isBlank()) {
                sb.append(":").append(m.group(3).trim());
            }
        }
        return sb.toString();
    }

    /** 提取处理信息 */
    private String extractProcessInfo(String content) {
        if (!StringUtils.hasText(content)) return "";
        StringBuilder sb = new StringBuilder();
        // 退回记录
        java.util.regex.Matcher rm = java.util.regex.Pattern.compile(
                "=== 退回记录 \\[([^\\]]+)\\] ===\\n退回原因: (.*?)(?:\\n建议处理人: (.*?))?(?:\\n建议单位: (.*?))?(?=\\n===|\\n?$)").matcher(content);
        while (rm.find()) {
            if (!sb.isEmpty()) sb.append("; ");
            sb.append("退回[").append(rm.group(1)).append("]:").append(rm.group(2).trim());
        }
        // 审核记录
        java.util.regex.Matcher am = java.util.regex.Pattern.compile(
                "=== 审核记录 \\[([^\\]]+)\\] ===\\n审核结果: (.*?)(?:\\n审核意见: (.*?))?(?=\\n===|\\n?$)").matcher(content);
        while (am.find()) {
            if (!sb.isEmpty()) sb.append("; ");
            sb.append("审核[").append(am.group(1)).append("]:").append(am.group(2));
            if (am.group(3) != null && !am.group(3).isBlank()) {
                sb.append("(").append(am.group(3).trim()).append(")");
            }
        }
        return sb.toString();
    }

    /* ==================== 报表统计 ==================== */

    @Override
    public Map<String, Object> taskReport(String month, Long orgId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 构建月份时间范围（默认当月）
        LocalDateTime monthStart;
        LocalDateTime monthEnd;
        if (StringUtils.hasText(month)) {
            String[] parts = month.split("-");
            int year = Integer.parseInt(parts[0]);
            int mon = Integer.parseInt(parts[1]);
            monthStart = LocalDateTime.of(year, mon, 1, 0, 0, 0);
            monthEnd = mon == 12 ? LocalDateTime.of(year + 1, 1, 1, 0, 0, 0) : LocalDateTime.of(year, mon + 1, 1, 0, 0, 0);
        } else {
            monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            monthEnd = monthStart.plusMonths(1);
        }

        // 2. 获取机构列表（如果指定了 orgId，获取其子机构；否则获取一级机构）
        List<Map<String, Object>> orgList = organizationService.getOrgTree(orgId);

        // 3. 查询所有任务（按月份筛选）
        LambdaQueryWrapper<TaskInfo> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.ge(TaskInfo::getCreateTime, monthStart);
        baseWrapper.lt(TaskInfo::getCreateTime, monthEnd);
        if (orgId != null) {
            Set<Long> gridIds = collectGridIdsUnderOrg(orgId);
            if (!gridIds.isEmpty()) {
                baseWrapper.in(TaskInfo::getGridId, gridIds);
            } else {
                // 无关联网格，返回空
                result.put("completed", 0);
                result.put("processing", 0);
                result.put("pending", 0);
                result.put("overdue", 0);
                result.put("supervised", 0);
                result.put("units", List.of());
                return result;
            }
        }
        List<TaskInfo> allTasks = list(baseWrapper);
        LocalDateTime now = LocalDateTime.now();

        // 4. 全局状态统计
        long completed = allTasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
        long processing = allTasks.stream().filter(t -> List.of("DISPATCHED", "SIGNED", "RECEIVED", "PROCESSING").contains(t.getStatus())).count();
        long pending = allTasks.stream().filter(t -> "DRAFT".equals(t.getStatus())).count();
        long overdue = allTasks.stream()
                .filter(t -> !List.of("DONE", "REVOKED").contains(t.getStatus()) && t.getDeadline() != null && t.getDeadline().isBefore(now))
                .count();
        long supervised = allTasks.stream().filter(t -> t.getSuperviseCount() != null && t.getSuperviseCount() > 0).count();

        result.put("completed", completed);
        result.put("processing", processing);
        result.put("pending", pending);
        result.put("overdue", overdue);
        result.put("supervised", supervised);

        // 5. 各单位统计
        List<Map<String, Object>> unitStats = new ArrayList<>();
        for (Object obj : orgList) {
            @SuppressWarnings("unchecked")
            Map<String, Object> orgMap = (Map<String, Object>) obj;
            Long oId = ((Number) orgMap.get("id")).longValue();
            String name = (String) orgMap.get("name");
            Integer level = (Integer) orgMap.getOrDefault("level", 1);
            boolean hasChildren = Boolean.TRUE.equals(orgMap.get("hasChildren"));

            Set<Long> childGridIds = collectGridIdsUnderOrg(oId);
            List<TaskInfo> unitTasks;
            if (childGridIds.isEmpty()) {
                unitTasks = List.of();
            } else {
                unitTasks = allTasks.stream()
                        .filter(t -> t.getGridId() != null && childGridIds.contains(t.getGridId()))
                        .toList();
            }

            long uTotal = unitTasks.size();
            long uDone = unitTasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
            long uProc = unitTasks.stream().filter(t -> List.of("DISPATCHED", "SIGNED", "RECEIVED", "PROCESSING").contains(t.getStatus())).count();
            long uWait = unitTasks.stream().filter(t -> "DRAFT".equals(t.getStatus())).count();
            long uOver = unitTasks.stream()
                    .filter(t -> !List.of("DONE", "REVOKED").contains(t.getStatus()) && t.getDeadline() != null && t.getDeadline().isBefore(now))
                    .count();
            long uSuper = unitTasks.stream().filter(t -> t.getSuperviseCount() != null && t.getSuperviseCount() > 0).count();

            double rate = uTotal > 0 ? Math.round((double) uDone / uTotal * 10000) / 100.0 : 0;

            Map<String, Object> stat = new HashMap<>();
            stat.put("orgId", oId);
            stat.put("name", name);
            stat.put("level", level);
            stat.put("hasChildren", hasChildren);
            stat.put("total", uTotal);
            stat.put("done", uDone);
            stat.put("processing", uProc);
            stat.put("pending", uWait);
            stat.put("overdue", uOver);
            stat.put("supervised", uSuper);
            stat.put("rate", rate);

            // 任务类型分布
            Map<String, Long> typeCount = unitTasks.stream()
                    .collect(Collectors.groupingBy(
                            t -> t.getTaskType() != null ? t.getTaskType() : "OTHER",
                            Collectors.counting()));
            stat.put("typeDistribution", typeCount);
            unitStats.add(stat);
        }

        result.put("units", unitStats);
        return result;
    }

    @Override
    public PageResult<TaskInfo> queryTasksByOrg(Long orgId, String status, String month, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();

        // 月份筛选
        if (StringUtils.hasText(month)) {
            String[] parts = month.split("-");
            int year = Integer.parseInt(parts[0]);
            int mon = Integer.parseInt(parts[1]);
            LocalDateTime ms = LocalDateTime.of(year, mon, 1, 0, 0, 0);
            LocalDateTime me = mon == 12 ? LocalDateTime.of(year + 1, 1, 1, 0, 0, 0) : LocalDateTime.of(year, mon + 1, 1, 0, 0, 0);
            wrapper.ge(TaskInfo::getCreateTime, ms);
            wrapper.lt(TaskInfo::getCreateTime, me);
        }

        // 机构筛选 → 转为网格ID
        if (orgId != null) {
            Set<Long> gridIds = collectGridIdsUnderOrg(orgId);
            if (gridIds.isEmpty()) {
                return PageResult.of(List.of(), 0L, pageNum.longValue(), pageSize.longValue());
            }
            wrapper.in(TaskInfo::getGridId, gridIds);
        }

        // 状态筛选
        if (StringUtils.hasText(status)) {
            wrapper.eq(TaskInfo::getStatus, status);
        }

        wrapper.orderByDesc(TaskInfo::getCreateTime);
        Page<TaskInfo> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillGridNames(page.getRecords());
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 收集机构及其下级机构关联的所有网格ID */
    private Set<Long> collectGridIdsUnderOrg(Long orgId) {
        Set<Long> result = new HashSet<>();
        List<com.ruoyi.tuyt.business.system.entity.SysOrganization> allOrgs = organizationService.list();
        Set<Long> descendantOrgIds = collectDescendantOrgIds(orgId, allOrgs);
        descendantOrgIds.add(orgId);
        for (Long oid : descendantOrgIds) {
            // 通过网格的org_id查找
            LambdaQueryWrapper<GridInfo> gw = new LambdaQueryWrapper<>();
            gw.eq(GridInfo::getOrgId, oid);
            List<GridInfo> grids = gridInfoService.list(gw);
            grids.forEach(g -> result.add(g.getId()));
        }
        return result;
    }

    /** 递归收集下级组织ID */
    private Set<Long> collectDescendantOrgIds(Long parentId, List<com.ruoyi.tuyt.business.system.entity.SysOrganization> allOrgs) {
        Set<Long> result = new HashSet<>();
        for (com.ruoyi.tuyt.business.system.entity.SysOrganization org : allOrgs) {
            if (parentId.equals(org.getParentId())) {
                result.add(org.getId());
                result.addAll(collectDescendantOrgIds(org.getId(), allOrgs));
            }
        }
        return result;
    }

    /** 填充网格名称 */
    private void fillGridNames(List<TaskInfo> records) {
        Set<Long> gridIds = records.stream()
                .map(TaskInfo::getGridId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (gridIds.isEmpty()) return;
        Map<Long, String> nameMap = gridInfoService.listByIds(gridIds).stream()
                .collect(Collectors.toMap(GridInfo::getId, GridInfo::getGridName));
        for (TaskInfo t : records) {
            t.setGridName(nameMap.getOrDefault(t.getGridId(), "-"));
        }
    }

    @Override
    public PageResult<TaskInfo> queryReturnedTasks(String taskNo, String title, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskInfo::getStatus, "RETURNED");
        if (StringUtils.hasText(taskNo)) {
            wrapper.like(TaskInfo::getTaskNo, taskNo);
        }
        if (StringUtils.hasText(title)) {
            wrapper.like(TaskInfo::getTaskTitle, title);
        }
        wrapper.orderByDesc(TaskInfo::getCreateTime);
        Page<TaskInfo> page = page(new Page<>(pageNum, pageSize), wrapper);
        fillGridName(page.getRecords());
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}
