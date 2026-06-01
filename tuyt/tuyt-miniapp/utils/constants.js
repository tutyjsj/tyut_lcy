// 常量映射 - 与Web端constants.js保持一致

// 问题处理状态
export const handleStatusMap = {
  PENDING: '待处理',
  PROCESSING: '处理中',
  PROCESSED: '已处理',
  DONE: '处理完成',
  CLOSED: '已关闭',
  IGNORED: '已忽略'
}

export const handleStatusTagType = {
  PENDING: 'warning',
  PROCESSING: 'primary',
  PROCESSED: 'primary',
  DONE: 'success',
  CLOSED: 'info',
  IGNORED: 'danger'
}

// 问题等级
export const problemLevelMap = {
  I: { label: '严重', tagType: 'danger' },
  II: { label: '较严重', tagType: 'warning' },
  III: { label: '一般', tagType: '' }
}

// 污染类型
export const pollutionTypeMap = {
  WASTE_WATER: '废水污染',
  WASTE_GAS: '废气污染',
  NOISE: '噪声污染',
  SOLID_WASTE: '固危废污染',
  RADIATION: '放辐射污染',
  HAZARDOUS: '危化品',
  OTHER: '其他'
}

export const pollutionTypeOptions = [
  { value: 'WASTE_WATER', label: '废水污染' },
  { value: 'WASTE_GAS', label: '废气污染' },
  { value: 'NOISE', label: '噪声污染' },
  { value: 'SOLID_WASTE', label: '固危废污染' },
  { value: 'RADIATION', label: '放辐射污染' },
  { value: 'OTHER', label: '其他' }
]

// 问题来源
export const problemSourceMap = {
  PATROL: '巡查发现',
  MONITOR: '在线监测',
  COMPLAINT: '群众举报',
  SUPERIOR: '上级交办'
}

export const problemSourceOptions = [
  { value: 'PATROL', label: '巡查发现' },
  { value: 'MONITOR', label: '在线监测' },
  { value: 'COMPLAINT', label: '群众举报' },
  { value: 'SUPERIOR', label: '上级交办' }
]

// 任务状态
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

// 紧急程度
export const urgencyMap = {
  CRITICAL: '特急',
  URGENT: '紧急',
  HIGH: '紧急',
  NORMAL: '一般'
}

// 网格级别
export const gridLevelOptions = [
  { value: 'CITY', label: '市级' },
  { value: 'DISTRICT', label: '区县级' },
  { value: 'TOWN', label: '乡镇/街道' }
]

// 监管等级
export const superviseTypeMap = {
  I: '省重点',
  II: '市重点',
  III: '区属重点'
}

// 经营状态
export const productionStatusMap = {
  NORMAL: '正常生产',
  SHUTDOWN: '停产',
  CLOSED: '关闭'
}

// 获取tag样式类名
export function getTagClass(tagType) {
  if (!tagType) return 'tag-info'
  const map = {
    danger: 'tag-danger',
    warning: 'tag-warning',
    success: 'tag-success',
    primary: 'tag-primary',
    info: 'tag-info'
  }
  return map[tagType] || 'tag-info'
}
