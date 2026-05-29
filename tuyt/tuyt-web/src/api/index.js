import request from '@/utils/request'

// ===== 登录 =====
export function loginApi(data) {
  return request.post('/login', data)
}
export function logoutApi() {
  return request.post('/logout')
}

// ===== 我的工作 =====
export function getTodoList(params) {
  return request.get('/work/todo', { params })
}
export function getTransferList(params) {
  return request.get('/work/transfer', { params })
}
export function getDoneList(params) {
  return request.get('/work/done', { params })
}
export function processTask(id, data) {
  return request.put(`/work/todo/${id}`, data)
}

// ===== 网格管理 =====
export function getGridList(params) {
  return request.get('/grid/list', { params })
}
export function getGridTree() {
  return request.get('/grid/tree')
}
export function createGrid(data) {
  return request.post('/grid', data)
}
export function updateGrid(id, data) {
  return request.put(`/grid/${id}`, data)
}
export function deleteGrid(id) {
  return request.delete(`/grid/${id}`)
}
export function getGridEnterprises(gridId) {
  return request.get(`/grid/${gridId}/enterprises`)
}
export function addGridEnterprise(gridId, data) {
  return request.post(`/grid/${gridId}/enterprises`, data)
}
export function removeGridEnterprise(gridId, enterpriseIds) {
  return request.delete(`/grid/${gridId}/enterprises`, { data: enterpriseIds })
}
export function setPatrolPerson(enterpriseId, data) {
  return request.put(`/grid/enterprise/${enterpriseId}/patrol`, data)
}

// ===== 污染源档案 =====
export function getEnterpriseList(params) {
  return request.get('/enterprise/list', { params })
}
export function getEnterpriseDetail(id) {
  return request.get(`/enterprise/${id}`)
}
export function createEnterprise(data) {
  return request.post('/enterprise', data)
}
export function updateEnterprise(id, data) {
  return request.put(`/enterprise/${id}`, data)
}
export function deleteEnterprise(id) {
  return request.delete(`/enterprise/${id}`)
}

// ===== 环境问题 =====
export function getProblemList(params) {
  return request.get('/problem/list', { params })
}
export function getProblemDetail(id) {
  return request.get(`/problem/${id}`)
}
export function createProblem(data) {
  return request.post('/problem', data)
}
export function updateProblem(id, data) {
  return request.put(`/problem/${id}`, data)
}
export function closeProblem(data) {
  return request.put('/problem/close', data)
}
export function mergeProblems(data) {
  return request.put('/problem/merge', data)
}
export function changeProblemLevel(id, level) {
  return request.put(`/problem/${id}/level`, { level })
}
export function getProblemStatistics(params) {
  return request.get('/problem/statistics', { params })
}
/** 问题预警专用统计（支持筛选参数，基于全部数据库数据，不依赖分页） */
export function getWarningStatistics(params) {
  return request.get('/problem/warning-stats', { params })
}
export function getGridRanking(params) {
  return request.get('/problem/ranking', { params })
}
export function getProblemMap(params) {
  return request.get('/problem/map', { params })
}
export function getProblemLogs(id) {
  return request.get(`/problem/${id}/logs`)
}

// ===== 任务调度 =====
export function getTaskList(params) {
  return request.get('/task/list', { params })
}
export function getTaskDetail(id) {
  return request.get(`/task/${id}`)
}
export function dispatchTask(data) {
  return request.post('/task/dispatch', data)
}
export function updateTask(id, data) {
  return request.put(`/task/${id}`, data)
}
export function urgeTask(id, data) {
  return request.post(`/task/${id}/urge`, data)
}
export function superviseTask(id, data) {
  return request.post(`/task/${id}/supervise`, data)
}
export function deleteTask(ids) {
  return request.delete('/task/batch', { data: ids })
}
export function publishTask(id) {
  return request.put(`/task/${id}/publish`)
}
export function revokeTask(id, data) {
  return request.put(`/task/${id}/revoke`, data)
}
export function returnTask(id, data) {
  return request.post(`/task/${id}/return`, data)
}
export function getReturnedTasks(params) {
  return request.get('/task/returned', { params })
}
export function auditReturnedTask(id, data) {
  return request.post(`/task/${id}/audit-return`, data)
}

// ===== 巡查计划 =====
export function getPatrolPlanList(params) {
  return request.get('/patrol-plan/list', { params })
}
export function createPatrolPlan(data) {
  return request.post('/patrol-plan', data)
}
export function updatePatrolPlan(id, data) {
  return request.put(`/patrol-plan/${id}`, data)
}
export function deletePatrolPlan(id) {
  return request.delete(`/patrol-plan/${id}`)
}

// ===== 台账 =====
export function exportProblemLedger(params) {
  return request.get('/ledger/problem/export', { params, responseType: 'blob' })
}
export function exportTaskLedger(params) {
  return request.get('/ledger/task/export', { params, responseType: 'blob' })
}
export function getReportData(params) {
  return request.get('/ledger/report', { params })
}
export function getReportTasks(params) {
  return request.get('/ledger/report/tasks', { params })
}
export function getReportOrgTree(parentId) {
  return request.get('/ledger/report/org-tree', { params: parentId ? { parentId } : {} })
}

// ===== 考评 =====
export function getAssessRuleList(params) {
  return request.get('/assessment/rules', { params })
}
export function saveAssessRule(data) {
  return request.post('/assessment/rules', data)
}
export function getAssessIndicatorList(params) {
  return request.get('/assessment/indicators', { params })
}
export function saveAssessIndicator(data) {
  return request.post('/assessment/indicators', data)
}
export function getAssessResult(params) {
  return request.get('/assessment/results', { params })
}
export function runAssess(data) {
  return request.post('/assessment/run', data)
}
// 考评模板
export function getAssessTemplateList(params) {
  return request.get('/assessment/templates', { params })
}
export function getAssessTemplatesEnabled() {
  return request.get('/assessment/templates/enabled')
}
export function saveAssessTemplate(data) {
  return request.post('/assessment/templates', data)
}
export function deleteAssessTemplate(id) {
  return request.delete(`/assessment/templates/${id}`)
}
export function getAssessTemplateItems(templateId) {
  return request.get(`/assessment/templates/${templateId}/items`)
}
export function saveAssessTemplateItems(templateId, data) {
  return request.post(`/assessment/templates/${templateId}/items`, data)
}

// ===== 配置 =====
export function getCheckItemList(params) {
  return request.get('/config/check-items', { params })
}
export function saveCheckItem(data) {
  return request.post('/config/check-items', data)
}
export function deleteCheckItem(id) {
  return request.delete(`/config/check-items/${id}`)
}
export function getCheckTemplateList(params) {
  return request.get('/config/check-templates', { params })
}
export function saveCheckTemplate(data) {
  return request.post('/config/check-templates', data)
}
export function deleteCheckTemplate(id) {
  return request.delete(`/config/check-templates/${id}`)
}
export function getContactList(params) {
  return request.get('/config/contacts', { params })
}
export function saveContact(data) {
  return request.post('/config/contacts', data)
}
export function deleteContact(id) {
  return request.delete(`/config/contacts/${id}`)
}

// ===== 短信/语音调度 =====
/** 短信记录列表 */
export function getSmsList(params) {
  return request.get('/dispatch/sms/list', { params })
}
/** 发送短信(单个) */
export function sendSms(data) {
  return request.post('/dispatch/sms/send', data)
}
/** 批量发送短信 */
export function batchSendSms(data) {
  return request.post('/dispatch/sms/batch-send', data)
}
/** 近期通话记录 */
export function getRecentCalls(limit) {
  return request.get('/dispatch/call/recent', { params: { limit: limit || 10 } })
}
/** 记录通话 */
export function recordCallApi(data) {
  return request.post('/dispatch/call/record', data)
}
