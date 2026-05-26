// ECharts 按需引入，避免全量打包（全量 ~1MB，按需 ~200KB）
import * as echarts from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent,
  GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  PieChart, BarChart,
  TitleComponent, TooltipComponent, LegendComponent, GridComponent,
  CanvasRenderer
])

/**
 * 创建环形图（污染类型分布 / 问题来源分布）
 * @param {HTMLElement} dom - 图表容器
 * @param {Array} data - [{ name, value }]
 * @param {string} title - 标题
 */
export function createRingChart(dom, data, title = '') {
  const chart = echarts.init(dom)
  chart.setOption({
    title: { text: title, left: 'center', top: 10, textStyle: { fontSize: 14, color: '#606266' } },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 5, textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontWeight: 'bold' } },
      data
    }]
  })
  return chart
}

/**
 * 创建柱状图（任务统计）
 * @param {HTMLElement} dom
 * @param {Array} categories - x 轴类别
 * @param {Array} data - y 轴数据 { name, value }
 */
export function createBarChart(dom, categories, series) {
  const chart = echarts.init(dom)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 5, textStyle: { fontSize: 11 } },
    xAxis: { type: 'category', data: categories, axisLabel: { rotate: 30, fontSize: 11 } },
    yAxis: { type: 'value' },
    series: series.map(s => ({ ...s, type: 'bar', barMaxWidth: 40 }))
  })
  return chart
}

/**
 * 创建饼图（完成率）
 */
export function createPieChart(dom, data) {
  const chart = echarts.init(dom)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 5 },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{d}%' },
      data
    }]
  })
  return chart
}

/**
 * 响应式 resize（自动清理监听器，避免内存泄漏）
 */
export function autoResize(chart) {
  const handler = () => chart.resize()
  window.addEventListener('resize', handler)
  // 返回清理函数，组件 onBeforeUnmount 时调用
  return () => window.removeEventListener('resize', handler)
}
