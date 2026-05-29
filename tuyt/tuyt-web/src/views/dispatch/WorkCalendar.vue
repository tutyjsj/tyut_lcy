<template>
  <div>
    <div class="page-title">工作日历</div>

    <!-- 头部导航 -->
    <div class="calendar-toolbar">
      <div class="toolbar-nav">
        <div class="nav-controls">
          <button class="nav-btn nav-prev" :title="viewMode === 'week' ? '上一周' : '上个月'" @click="viewMode === 'week' ? prevWeek() : prevMonth()">
            <el-icon><ArrowLeft /></el-icon>
          </button>
          <button class="nav-btn nav-today" @click="goToday">今天</button>
          <button class="nav-btn nav-next" :title="viewMode === 'week' ? '下一周' : '下个月'" @click="viewMode === 'week' ? nextWeek() : nextMonth()">
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
        <!-- 年月 / 周范围标题 -->
        <h2 class="month-title">{{ displayTitle }}</h2>
        <!-- 视图切换 -->
        <div class="view-switcher" :class="{ 'active-week': viewMode === 'week' }">
          <button class="switch-btn" :class="{ active: viewMode === 'month' }" @click="switchToMonth">月视图</button>
          <span class="switch-divider"></span>
          <button class="switch-btn" :class="{ active: viewMode === 'week' }" @click="switchToWeek">周视图</button>
        </div>
      </div>
      <div class="toolbar-stats">
        <div class="stat-chip problem-chip"><i class="chip-dot dot-problem"></i>问题 <strong>{{ stats.problems }}</strong></div>
        <div class="stat-chip task-chip"><i class="chip-dot dot-task"></i>任务 <strong>{{ stats.tasks }}</strong></div>
        <div class="stat-chip total-chip">合计 <strong>{{ stats.total }}</strong></div>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="19">
        <div class="calendar-card" v-loading="loading">
          <!-- 星期头（月视图显示日-六，周视图显示完整日期） -->
          <div class="calendar-weekdays">
            <div
              v-for="(cell, idx) in headerCells"
              :key="idx"
              class="weekday-cell"
              :class="{
                weekend: idx >= 5,
                today: cell.isToday,
                selected: isSelected(cell)
              }">
              <div class="wd-day-name">{{ weekDays[idx] }}</div>
              <div v-if="viewMode === 'week'" class="wd-date-num" :class="{ other: cell.isOtherMonth }">{{ cell.day }}</div>
              <div v-if="viewMode === 'week' && cell.isToday" class="wd-today-mark">今天</div>
            </div>
          </div>

          <!-- 月视图网格 -->
          <div v-if="viewMode === 'month'" class="calendar-grid calendar-grid-month">
            <div
              v-for="cell in flatCalendarCells"
              :key="cell.key"
              class="calendar-cell cell-month"
              :class="{
                today: cell.isToday,
                'other-month': cell.isOtherMonth,
                selected: isSelected(cell),
                weekend: new Date(cell.date).getDay() >= 5
              }"
              @click="selectDate(cell)">
              <div class="cell-header">
                <span class="cell-day">{{ cell.day }}</span>
                <span v-if="cell.isToday" class="today-mark">今天</span>
                <span v-if="cell.items.length" class="cell-count">{{ cell.items.length }}</span>
              </div>
              <div v-if="cell.items.length" class="cell-items">
                <div v-for="item in cell.items.slice(0, maxVisibleItems(cell))" :key="item.id" class="cell-item" :class="item.type" @click.stop="openItem(item)">
                  <span class="dot" :class="item.type"></span>
                  <span class="item-text">{{ item.text }}</span>
                </div>
                <div v-if="cell.items.length > maxVisibleItems(cell)" class="cell-more" @click.stop="toggleExpandDay(cell)">
                  {{ expandedDay === cell.key ? '收起' : `+${cell.items.length - maxVisibleItems(cell)} 更多` }}
                </div>
              </div>
            </div>
          </div>

          <!-- 周视图网格 -->
          <div v-else class="calendar-grid calendar-grid-week">
            <div
              v-for="cell in weekCells"
              :key="cell.key"
              class="calendar-cell cell-week"
              :class="{
                today: cell.isToday,
                selected: isSelected(cell),
                weekend: cell.dateObj.getDay() >= 5
              }"
              @click="selectDate(cell)">
              <div class="cell-header-week">
                <div class="ch-left">
                  <span class="cell-day-lg">{{ cell.day }}</span>
                  <span v-if="cell.isToday" class="today-mark">今天</span>
                </div>
                <span v-if="cell.items.length" class="cell-count-lg">{{ cell.items.length }} 项</span>
              </div>
              <div v-if="cell.items.length" class="cell-items-week">
                <div v-for="item in cell.items" :key="item.id" class="cell-item-week" :class="item.type" @click.stop="openItem(item)">
                  <span class="ci-dot" :class="item.type"></span>
                  <div class="ci-body">
                    <span class="ci-text">{{ item.text }}</span>
                    <div class="ci-meta">
                      <span class="ci-type">{{ item.type === 'problem' ? '问题' : '任务' }}</span>
                      <span v-if="item.time" class="ci-time">{{ item.time }}</span>
                      <span v-if="item.extra" class="ci-extra">{{ item.extra }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="cell-empty-hint">暂无安排</div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧详情 -->
      <el-col :span="5">
        <div class="side-panel">
          <div class="panel-header">
            <div class="panel-date">
              <span class="date-text">{{ selectedDateStr || '选择日期查看' }}</span>
              <el-tag v-if="isToday" size="small" type="success" effect="dark">今天</el-tag>
            </div>
            <span v-if="selectedItems.length" class="panel-total">共 {{ selectedItems.length }} 项</span>
          </div>
          <el-empty v-if="!selectedDateStr" description="点击日历上的日期查看详情" :image-size="60" />
          <el-empty v-else-if="!selectedItems.length" description="该日期暂无工作安排" :image-size="60" />
          <div v-else class="item-list">
            <div v-for="item in selectedItems" :key="item.id" class="item-card" :class="item.type" @click="openItem(item)">
              <div class="item-card-top">
                <span class="dot" :class="item.type"></span>
                <el-tag :type="item.type === 'problem' ? 'danger' : 'primary'" size="small" effect="plain">
                  {{ item.type === 'problem' ? '问题' : '任务' }}
                </el-tag>
                <span v-if="item.time" class="item-time">{{ item.time }}</span>
              </div>
              <div class="item-card-title">{{ item.text }}</div>
              <div v-if="item.extra" class="item-card-extra">{{ item.extra }}</div>
              <div v-if="item.type === 'task' && item.status" class="item-card-footer">
                <el-tag size="small" :type="item.statusTagType">{{ item.status }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProblemList, getTaskList } from '@/api'
import { taskStatusMap } from '@/utils/constants'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const currentDate = ref(new Date())
const viewMode = ref('month')
const selectedDate = ref(null)
const calendarItems = ref([])
const loading = ref(false)
const expandedDay = ref(null)

const formatDateKey = (d) =>
  `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`

// ========== 计算属性 ==========

const currentYear = computed(() => currentDate.value.getFullYear())
const currentMonth = computed(() => currentDate.value.getMonth() + 1)

// 显示标题：月模式 → "2026年5月"，周模式 → "5/25 - 5/31"
const displayTitle = computed(() => {
  if (viewMode.value !== 'week') {
    return `${currentYear.value}年 ${currentMonth.value}月`
  }
  const cells = weekCells.value
  if (!cells.length) return ''
  const first = new Date(cells[0].dateObj)
  const last = new Date(cells[cells.length - 1].dateObj)
  const fmt = d => `${d.getMonth() + 1}/${d.getDate()}`
  const sameYear = first.getFullYear() === last.getFullYear()
  const sameMonth = first.getMonth() === last.getMonth()
  let title = `${fmt(first)} - ${fmt(last)}`
  // 如果跨年或跨月则加年份和月份前缀
  if (!sameYear) {
    title = `${first.getFullYear()}${fmt(first)} - ${last.getFullYear()}${fmt(last)}`
  } else if (!sameMonth) {
    title = `${fmt(first)} - ${last.getMonth() + 1}/${last.getDate()}`
  }
  return title
})

const isCurrentMonth = computed(() => {
  const now = new Date()
  return currentYear.value === now.getFullYear() && currentMonth.value === now.getMonth() + 1
})

const isToday = computed(() => {
  if (!selectedDate.value) return false
  return new Date(selectedDate.value).toDateString() === new Date().toDateString()
})

const stats = computed(() => {
  const all = calendarItems.value.flatMap(c => c.items || [])
  return { problems: all.filter(i => i.type === 'problem').length, tasks: all.filter(i => i.type === 'task').length, total: all.length }
})

const selectedDateStr = computed(() => {
  if (!selectedDate.value) return ''
  const d = new Date(selectedDate.value)
  return formatDateKey(d)
})

const selectedItems = computed(() => {
  if (!selectedDate.value || !calendarItems.value.length) return []
  const key = formatDateKey(new Date(selectedDate.value))
  return calendarItems.value.find(c => c.date === key)?.items || []
})

// ========== 月视图数据 ==========

const flatCalendarCells = computed(() => {
  const y = currentYear.value, m = currentDate.value.getMonth()
  const firstDay = new Date(y, m, 1).getDay()
  const dim = new Date(y, m + 1, 0).getDate()
  const now = new Date(), todayKey = formatDateKey(now), cells = []
  const prevDim = new Date(y, m, 0).getDate()
  for (let i = firstDay - 1; i >= 0; i--) {
    const d = new Date(y, m - 1, prevDim - i)
    cells.push(buildCell(prevDim - i, d, true, false))
  }
  for (let i = 1; i <= dim; i++) {
    const d = new Date(y, m, i)
    cells.push(buildCell(i, d, false, formatDateKey(d) === todayKey))
  }
  const rem = (7 - (cells.length % 7)) % 7
  for (let i = 1; i <= rem; i++) {
    const d = new Date(y, m + 1, i)
    cells.push(buildCell(i, d, true, false))
  }
  return cells
})

// ========== 周视图数据 ==========

// 获取当前选中日期所在周的周一
const getWeekStart = (date) => {
  const d = new Date(date)
  const day = d.getDay()
  const diff = day === 0 ? -6 : 1 - day
  d.setDate(d.getDate() + diff)
  d.setHours(0, 0, 0, 0)
  return d
}

const weekCells = computed(() => {
  const base = selectedDate.value ? new Date(selectedDate.value) : new Date()
  const monday = getWeekStart(base)
  const today = new Date()
  const todayKey = formatDateKey(today)
  const cells = []
  for (let i = 0; i < 7; i++) {
    const d = new Date(monday)
    d.setDate(d.getDate() + i)
    cells.push({
      key: formatDateKey(d), day: d.getDate(),
      date: d.getTime(), dateObj: d,
      isToday: formatDateKey(d) === todayKey,
      items: getItemsForDate(d)
    })
  }
  return cells
})

// 周视图表头单元格（用于显示星期 + 日期）
const headerCells = computed(() => viewMode.value === 'week' ? weekCells.value : flatCalendarCells.value.slice(0, 7))

// ========== 工具方法 ==========

const buildCell = (day, date, isOtherMonth, isToday) => ({
  key: formatDateKey(date), day, date: date.getTime(), dateObj: date,
  isOtherMonth, isToday, items: getItemsForDate(date)
})

const getItemsForDate = (date) => {
  if (!calendarItems.value.length) return []
  const found = calendarItems.value.find(c => c.date === formatDateKey(date))
  return found ? found.items : []
}

const isSelected = (cell) => {
  if (!selectedDate.value || !cell?.date) return false
  return formatDateKey(new Date(cell.date)) === formatDateKey(new Date(selectedDate.value))
}

const maxVisibleItems = (cell) => expandedDay.value === cell.key ? 99 : 3

const toggleExpandDay = (cell) => { expandedDay.value = expandedDay.value === cell.key ? null : cell.key }

// ========== 导航 ==========

const prevMonth = () => { const d = new Date(currentDate.value); d.setMonth(d.getMonth() - 1); currentDate.value = d }
const nextMonth = () => { const d = new Date(currentDate.value); d.setMonth(d.getMonth() + 1); currentDate.value = d }
const prevWeek = () => {
  const d = selectedDate.value ? new Date(selectedDate.value) : new Date()
  d.setDate(d.getDate() - 7); selectedDate.value = d
}
const nextWeek = () => {
  const d = selectedDate.value ? new Date(selectedDate.value) : new Date()
  d.setDate(d.getDate() + 7); selectedDate.value = d
}
const goToday = () => {
  currentDate.value = new Date(); selectDate({ date: new Date() })
}

const switchToMonth = () => {
  viewMode.value = 'month'
  if (selectedDate.value) currentDate.value = new Date(selectedDate.value)
}
const switchToWeek = () => {
  viewMode.value = 'week'
  // 保持当前选中日期不变即可（周视图会基于它计算）
}

const selectDate = (cell) => { selectedDate.value = new Date(cell.date) }

const openItem = (item) => {
  router.push(item.type === 'problem' ? `/dispatch/problem/${item.id}` : '/dispatch/task')
}

// ========== 数据加载 ==========

const fetchData = async () => {
  const y = currentYear.value, m = currentMonth.value
  const dim = new Date(y, m, 0).getDate()
  const startDate = `${y}-${String(m).padStart(2,'0')}-01`
  const endDate = `${y}-${String(m).padStart(2,'0')}-${String(dim).padStart(2,'0')}`

  loading.value = true; calendarItems.value = []; expandedDay.value = null

  try {
    const pRes = await getProblemList({ pageNum: 1, pageSize: 500, startTime: startDate, endTime: endDate })
    ;(pRes.data?.records || pRes.data?.list || []).forEach(p => {
      if (!p.alarmTime) return
      const key = p.alarmTime.substring(0, 10)
      let exist = calendarItems.value.find(c => c.date === key)
      if (!exist) { exist = { date: key, items: [] }; calendarItems.value.push(exist) }
      exist.items.push({ id: p.id, type: 'problem', text: p.problemDesc?.substring(0, 20) || '环境问题', time: p.alarmTime?.substring(11, 16) || '', extra: p.enterpriseName || '' })
    })

    const tRes = await getTaskList({ pageNum: 1, pageSize: 500, startTime: startDate, endTime: endDate })
    ;(tRes.data?.records || tRes.data?.list || []).forEach(t => {
      const ds = t.deadline || t.dispatchTime || t.startTime
      if (!ds) return
      const key = ds.substring(0, 10)
      let exist = calendarItems.value.find(c => c.date === key)
      if (!exist) { exist = { date: key, items: [] }; calendarItems.value.push(exist) }
      const st = taskStatusMap[t.status] || t.status || '-'
      exist.items.push({
        id: t.id, type: 'task',
        text: t.taskTitle || t.title || t.taskNo || '巡查任务',
        time: ds.substring(11, 16) || '',
        extra: t.enterpriseName || t.taskContent?.substring(0, 15) || '',
        status: st,
        statusTagType: t.status === 'DONE' ? 'success' : t.status === 'DISPATCHED' ? 'warning' : t.status === 'REVOKED' ? 'info' : ''
      })
    })
  } catch {}
  finally { loading.value = false }
}

watch(() => currentMonth.value, fetchData)
onMounted(() => { selectDate({ date: new Date() }); fetchData() })
</script>

<style scoped>
/* ========== 工具栏 ========== */
.calendar-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; flex-wrap: wrap; gap: 12px; }
.toolbar-nav { display: flex; align-items: center; gap: 18px; background: #fff; border-radius: 10px; padding: 6px 16px; box-shadow: 0 1px 6px rgba(0,0,0,.06); }

.nav-controls { display: inline-flex; align-items: center; border: 1px solid #E4E7ED; border-radius: 8px; overflow: hidden; background: #F5F7FA; }
.nav-btn { display: inline-flex; align-items: center; justify-content: center; border: none; outline: none; cursor: pointer; font-size: 13px; font-weight: 500; color: #606266; padding: 5px 14px; transition: all .2s; background: transparent; }
.nav-btn:hover { color: #409EFF; background: #EBF2FF; }
.nav-prev, .nav-next { padding: 5px 10px; }
.nav-today { border-left: 1px solid #E4E7ED; border-right: 1px solid #E4E7ED; min-width: 48px; font-weight: 600; }
.nav-today:hover { background: #D6E8FB !important; color: #2979FF; }

.month-title { font-size: 19px; font-weight: 700; color: #303133; letter-spacing: 0.5px; margin: 0; line-height: 1.3; min-width: 100px; }

.view-switcher { display: inline-flex; align-items: center; background: #F0F2F5; border-radius: 8px; padding: 2px; }
.switch-btn { border: none; outline: none; cursor: pointer; font-size: 13px; font-weight: 500; color: #909399; padding: 5px 14px; border-radius: 6px; background: transparent; transition: all .22s; }
.switch-btn.active { background: #fff; color: #303133; box-shadow: 0 1px 3px rgba(0,0,0,.08); font-weight: 600; }
.switch-divider { width: 1px; height: 16px; background: #DCDFE6; }

.toolbar-stats { display: flex; align-items: center; gap: 8px; }
.stat-chip { display: inline-flex; align-items: center; gap: 5px; font-size: 13px; padding: 5px 12px; border-radius: 20px; white-space: nowrap; }
.stat-chip strong { font-weight: 700; }
.stat-chip.problem-chip { background: #FEF0F0; color: #C45656; }
.stat-chip.task-chip { background: #ECF5FF; color: #337ECC; }
.stat-chip.total-chip { background: #F4F4F5; color: #909399; }
.chip-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.dot-problem { background: #F56C6C; }
.dot-task { background: #409EFF; }

/* ========== 日历主体 ========== */
.calendar-card { background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,.06); min-height: 480px; }

/* 星期头 */
.calendar-weekdays { display: grid; grid-template-columns: repeat(7, 1fr); background: #FAFBFC; border-bottom: 1px solid #EBEEF5; }
.weekday-cell { text-align: center; padding: 8px 4px; position: relative; }
.weekday-cell.weekend .wd-day-name { color: #F56C6C; }
.wd-day-name { font-size: 13px; font-weight: 600; color: #606266; line-height: 1.3; }
.wd-date-num { font-size: 15px; font-weight: 600; color: #303133; margin-top: 2px; }
.wd-date-num.other { color: #C0C4CC; }
.weekday-cell.today .wd-date-num { color: #409EFF; }
.wd-today-mark { font-size: 10px; background: #409EFF; color: #fff; padding: 1px 6px; border-radius: 10px; display: inline-block; margin-top: 2px; }
.weekday-cell.selected { background: #ECF5FF; }

/* ====== 月视图网格 ====== */
.calendar-grid-month { display: grid; grid-template-columns: repeat(7, 1fr); }

.cell-month { min-height: 95px; padding: 5px 5px 7px; border-right: 1px solid #F2F3F5; border-bottom: 1px solid #F2F3F5; cursor: pointer; transition: background .15s, box-shadow .15s; position: relative; }
.cell-month:nth-child(7n) { border-right: none; }
.cell-month:hover { background: #F0F5FF; }
.cell-month.selected { background: #ECF5FF; box-shadow: inset 0 0 0 2px #409EFF; border-radius: 2px; z-index: 1; }
.cell-month.other-month { background: #FAFBFC; }
.cell-month.other-month .cell-day { color: #C0C4CC; font-weight: 400; }
.cell-month.today { background: linear-gradient(135deg, #E8F4FD 0%, #DCE9FC 100%); }
.cell-month.today.selected { background: #D6E8FB; box-shadow: inset 0 0 0 2px #409EFF; }
.cell-month.weekend { background: #FFFBFB; }
.cell-month.weekend.other-month { background: #FAF9F9; }
.cell-month.weekend.today { background: linear-gradient(135deg, #FEEFEF 0%, #FDE2E2 100%); }
.cell-month.weekend:hover { background: #FFF5F5; }

.cell-header { display: flex; align-items: center; gap: 4px; margin-bottom: 3px; }
.cell-day { font-size: 14px; font-weight: 600; color: #303133; line-height: 1; }
.today-mark { font-size: 10px; background: #409EFF; color: #fff; padding: 1px 6px; border-radius: 10px; font-weight: 500; flex-shrink: 0; }
.cell-count { font-size: 11px; color: #909399; margin-left: auto; background: #F4F4F5; padding: 1px 6px; border-radius: 10px; flex-shrink: 0; }
.cell-items { display: flex; flex-direction: column; gap: 1px; }
.cell-item { display: flex; align-items: center; gap: 4px; padding: 2px 4px; border-radius: 3px; font-size: 11px; cursor: pointer; transition: background .12s; }
.cell-item:hover { background: rgba(0,0,0,.04); }
.item-text { overflow: hidden; white-space: nowrap; text-overflow: ellipsis; color: #606266; line-height: 1.4; }
.cell-item.problem .item-text { color: #C45656; }
.cell-item.task .item-text { color: #3A7BDE; }
.cell-more { font-size: 11px; color: #409EFF; cursor: pointer; padding: 2px 4px; border-radius: 3px; font-weight: 500; }
.cell-more:hover { background: #ECF5FF; }

/* ====== 周视图网格 ====== */
.calendar-grid-week { display: grid; grid-template-columns: repeat(7, 1fr); }

.cell-week { min-height: 360px; padding: 10px 8px; border-right: 1px solid #ECEEF2; cursor: pointer; transition: background .18s, box-shadow .18s; overflow-y: auto; position: relative; }
.cell-week:nth-child(7n) { border-right: none; }
.cell-week:hover { background: #F7FAFF; }
.cell-week.selected { background: #EEF3FC; box-shadow: inset 0 0 0 2px #409EFF inset; z-index: 1; }
.cell-week.today { background: linear-gradient(180deg, #EDF4FD 0%, #F0F6FD 100%); }
.cell-week.today.selected { background: #DFEAF9; }
.cell-week.weekend { background: #FFFDFD; }
.cell-week.weekend:hover { background: #FEFAFA; }
.cell-week.weekend.today { background: linear-gradient(180deg, #FDF0F0 0%, #FDE8E8 100%); }

.cell-header-week { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; padding-bottom: 8px; border-bottom: 1px dashed #E8EAED; }
.ch-left { display: flex; align-items: center; gap: 6px; }
.cell-day-lg { font-size: 22px; font-weight: 700; color: #303133; line-height: 1; }
.cell-week.today .cell-day-lg { color: #409EFF; }
.cell-week.weekend .cell-day-lg { color: #F56C6C; }
.cell-count-lg { font-size: 12px; color: #909399; font-weight: 500; white-space: nowrap; background: #F5F7FA; padding: 2px 8px; border-radius: 10px; }

.cell-items-week { display: flex; flex-direction: column; gap: 6px; }
.cell-item-week { display: flex; align-items: flex-start; gap: 8px; padding: 8px 10px; border-radius: 8px; cursor: pointer; transition: all .18s; border: 1px solid transparent; }
.cell-item-week:hover { background: #fff; box-shadow: 0 1px 6px rgba(0,0,0,.07); border-color: #E4E7ED; }
.cell-item-week.problem:hover { border-color: #FBC4C4; }
.cell-item-week.task:hover { border-color: #BBDDFF; }
.ci-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; margin-top: 4px; }
.ci-dot.problem { background: #F56C6C; }
.ci-dot.task { background: #409EFF; }
.ci-body { flex: 1; min-width: 0; }
.ci-text { font-size: 13px; font-weight: 500; color: #303133; line-height: 1.4; display: block; margin-bottom: 3px; word-break: break-all; }
.ci-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.ci-type { font-size: 11px; color: #909399; padding: 1px 6px; border-radius: 3px; background: #F5F7FA; }
.ci-time { font-size: 11px; color: #C0C4CC; }
.ci-extra { font-size: 11px; color: #909399; max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.cell-empty-hint { font-size: 12px; color: #C0C4CC; text-align: center; padding: 20px 0; }

/* ========== 圆点通用 ========== */
.dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.dot.problem { background: #F56C6C; }
.dot.task { background: #409EFF; }

/* ========== 右侧面板 ========== */
.side-panel { background: #fff; border-radius: 10px; padding: 18px; box-shadow: 0 2px 12px rgba(0,0,0,.06); min-height: 400px; max-height: calc(100vh - 160px); overflow-y: auto; }
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px solid #EBEEF5; }
.panel-date { display: flex; align-items: center; gap: 8px; }
.date-text { font-size: 15px; font-weight: 600; color: #303133; }
.panel-total { font-size: 12px; color: #909399; }
.item-list { display: flex; flex-direction: column; gap: 8px; }
.item-card { padding: 12px; border-radius: 8px; cursor: pointer; border-left: 3px solid #E4E7ED; background: #FAFBFC; transition: background .18s, box-shadow .18s; }
.item-card:hover { background: #F0F5FF; box-shadow: 0 2px 6px rgba(64,158,255,.1); }
.item-card.problem { border-left-color: #F56C6C; }
.item-card.problem:hover { background: #FFF5F5; box-shadow: 0 2px 6px rgba(245,108,108,.1); }
.item-card.task { border-left-color: #409EFF; }
.item-card-top { display: flex; align-items: center; gap: 6px; margin-bottom: 6px; }
.item-time { font-size: 11px; color: #C0C4CC; margin-left: auto; }
.item-card-title { font-size: 14px; font-weight: 500; color: #303133; line-height: 1.5; margin-bottom: 4px; }
.item-card-extra { font-size: 12px; color: #909399; line-height: 1.4; }
.item-card-footer { margin-top: 6px; }
</style>
