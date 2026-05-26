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

// 任务状态
export const taskStatusMap = {
  DRAFT: '已拟定',
  DISPATCHED: '已派发',
  RECEIVED: '已签收',
  COMPLETED: '已完成',
  REVOKED: '已撤销',
  RETURNED: '已退回'
}

// 问题来源
export const problemSourceOptions = [
  { value: 'PUBLIC_COMPLAINT', label: '公众投诉' },
  { value: 'FIELD_INSPECTION', label: '现场监察' },
  { value: 'ONLINE_MONITOR', label: '在线监测' },
  { value: 'CROSS_ASSIGN', label: '横纵向交办' },
  { value: 'MANUAL', label: '手动添加' }
]

// 监管类型
export const supervisionTypeOptions = [
  { value: 'KEY_PROVINCE', label: '省重点' },
  { value: 'KEY_CITY', label: '市重点' },
  { value: 'KEY_DISTRICT', label: '区属重点' },
  { value: 'GENERAL', label: '一般' },
  { value: 'SPECIAL', label: '特殊' }
]

// 网格级别
export const gridLevelOptions = [
  { value: 'CITY', label: '市级' },
  { value: 'DISTRICT', label: '区县级' },
  { value: 'TOWN', label: '乡镇/街道' }
]
