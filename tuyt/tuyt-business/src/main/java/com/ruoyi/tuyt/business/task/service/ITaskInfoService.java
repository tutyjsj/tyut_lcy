package com.ruoyi.tuyt.business.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.tuyt.business.task.entity.TaskExportVO;
import com.ruoyi.tuyt.business.task.entity.TaskInfo;
import com.ruoyi.tuyt.common.result.PageResult;

import java.util.List;
import java.util.Map;

public interface ITaskInfoService extends IService<TaskInfo> {
    /** 任务报表统计（按月/单位筛选） */
    Map<String, Object> taskReport(String month, Long orgId);
    /** 按机构获取任务列表（用于点击数字查看详情） */
    PageResult<TaskInfo> queryTasksByOrg(Long orgId, String status, String month, Integer pageNum, Integer pageSize);
    PageResult<TaskInfo> queryPage(String taskNo, String title, String taskType, String status, String urgency, String overdueType, Integer pageNum, Integer pageSize);
    /** 巡查计划分页查询 */
    PageResult<TaskInfo> queryPatrolPlans(String type, String title, String startTime, String cycle, String status, Integer pageNum, Integer pageSize);
    TaskInfo getById(Long id);
    void dispatch(TaskInfo task);
    /** 创建巡查计划 */
    void dispatchPatrolPlan(TaskInfo task);
    void updateTask(TaskInfo task);
    /** 更新巡查计划 */
    void updatePatrolPlan(TaskInfo task);
    void urge(Long id, String reason);
    void supervise(Long id, String reason);
    void revoke(Long id);
    void publish(Long id);
    void returnTask(Long id, String reason, String suggestHandler, String suggestUnit);
    void auditReturn(Long id, String auditResult, String auditComment);
    void delete(List<Long> ids);
    void processTask(Long id, Map<String, Object> data);
    /** 退回任务分页查询 */
    PageResult<TaskInfo> queryReturnedTasks(String taskNo, String title, Integer pageNum, Integer pageSize);
    /** 导出任务台账（返回 VO 列表，带完整关联数据填充） */
    List<TaskExportVO> exportTasks(String taskNo, String title, String taskType, String status, String urgency, String overdueType);
}
