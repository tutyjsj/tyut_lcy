// 问题等级
export const problemLevelMap = {
  I: { label: '严重', color: '#F56C6C', tagType: 'danger' },
  II: { label: '较严重', color: '#E6A23C', tagType: 'warning' },
  III: { label: '一般', color: '#409EFF', tagType: '' }
}

// 污染类型
export const pollutionTypeOptions = [
  { value: 'WASTE_WATER', label: '废水污染' },
  { value: 'WASTE_GAS', label: '废气污染' },
  { value: 'NOISE', label: '噪声污染' },
  { value: 'SOLID_WASTE', label: '固危废污染' },
  { value: 'RADIATION', label: '放辐射污染' },
  { value: 'OTHER', label: '其他' }
]

// 污染类型简写映射（用于 quick display）
export const pollutionTypeMap = {
  WASTE_WATER: '废水', WASTE_GAS: '废气', NOISE: '噪声',
  SOLID_WASTE: '固危废', RADIATION: '辐射', OTHER: '其他'
}

// 问题处理状态
export const handleStatusMap = {
  PENDING: '待处理',
  PROCESSING: '处理中',
  PROCESSED: '已处理',
  DONE: '处理完成',
  CLOSED: '已关闭',
  IGNORED: '已忽略'
}

// 问题处理状态对应的 tag 类型
export const handleStatusTagType = {
  PENDING: 'warning',
  PROCESSING: 'primary',
  PROCESSED: 'primary',
  DONE: 'success',
  CLOSED: 'info',
  IGNORED: 'danger'
}

// 任务状态（与后端 TaskStatusEnum 一致，保留旧值兼容历史数据）
export const taskStatusMap = {
  DRAFT: '已拟定',
  DISPATCHED: '已派发',
  SIGNED: '已签收',
  RECEIVED: '已签收',
  PROCESSING: '处理中',
  DONE: '已完成',
  COMPLETED: '已完成',
  REVOKED: '已撤销',
  RETURNED: '已退回'
}

// 整改情况
export const rectifyStatusMap = {
  DONE: '整改完成',
  LIMIT: '限期整改'
}

// 生产经营情况
export const productionStatusMap = {
  NORMAL: '正常生产',
  SHUTDOWN: '停产',
  CLOSED: '关闭'
}

// 生产状态（数字型，企业档案用）
export const productionStatusNumMap = {
  0: { label: '关闭', tagType: 'danger' },
  1: { label: '正常生产', tagType: 'success' },
  2: { label: '停产', tagType: 'warning' }
}

// 紧急程度
export const urgencyMap = {
  CRITICAL: '特急',
  URGENT: '紧急',
  HIGH: '紧急',
  NORMAL: '一般'
}

// 紧急程度对应的 tag 类型
export const urgencyTagType = {
  CRITICAL: 'danger',
  URGENT: 'danger',
  HIGH: 'warning',
  NORMAL: ''
}

// 问题来源（与数据库字典 sys_dict_data - problem_source 统一）
export const problemSourceMap = {
  PATROL: '巡查发现',
  MONITOR: '在线监测',
  COMPLAINT: '群众举报',
  SUPERIOR: '上级交办'
}

export const problemSourceOptions = Object.entries(problemSourceMap).map(([value, label]) => ({ value, label }))

// 污染类型/问题类型（与数据库字典 sys_dict_data - pollution_type / problem_type 统一）
export const problemTypeMap = {
  WASTE_WATER: '水污染',
  WASTE_GAS: '大气污染',
  NOISE: '噪声污染',
  SOLID_WASTE: '固废污染',
  HAZARDOUS: '危化品',
  RADIATION: '放辐射污染',
  OTHER: '其他'
}

// 监管等级（与数据库 enterprise.supervise_type 统一：I/II/III）
export const superviseTypeMap = {
  I: '省重点',
  II: '市重点',
  III: '区属重点'
}

export const supervisionTypeOptions = [
  { value: 'I', label: '省重点' },
  { value: 'II', label: '市重点' },
  { value: 'III', label: '区属重点' }
]

// 网格级别
export const gridLevelOptions = [
  { value: 'CITY', label: '市级' },
  { value: 'DISTRICT', label: '区县级' },
  { value: 'TOWN', label: '乡镇/街道' }
]

// 任务类型（与数据库字典 sys_dict_data - task_type 统一）
export const taskTypeMap = {
  PATROL: '日常巡查',
  SPECIAL: '专项检查',
  SHUTDOWN: '停产巡查',
  CHECK: '问题核查',
  COMPLAINT: '投诉处理',
  RECTIFY: '整改通知',
  RECHECK: '复查验收',
  EMERGENCY: '应急任务'
}

export const taskTypeOptions = Object.entries(taskTypeMap).map(([value, label]) => ({ value, label }))
