// ECharts 按需引入，避免全量打包（全量 ~1MB，按需 ~200KB）
import * as echarts from 'echarts/core'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent,
  GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  PieChart, BarChart, LineChart,
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
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 10,
      top: 'middle',
      textStyle: { fontSize: 11 },
      pageIconColor: '#409EFF',
      pageIconInactiveColor: '#C0C4CC',
      pageTextStyle: { color: '#606266' }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['38%', '52%'],
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
 * 创建折线图（预警趋势）
 * @param {HTMLElement} dom
 * @param {Object} opts - { xData: [], series: [{ name, data, color }] }
 */
export function createLineChart(dom, opts) {
  const chart = echarts.init(dom)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: opts.xData, boundaryGap: false, axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', axisLabel: { fontSize: 11 } },
    series: opts.series.map(s => ({
      name: s.name,
      type: 'line',
      data: s.data,
      smooth: true,
      lineStyle: { color: s.color || '#F56C6C', width: 2 },
      itemStyle: { color: s.color || '#F56C6C' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: (s.color || '#F56C6C').replace(')', ',0.3)').replace('rgb', 'rgba') },
        { offset: 1, color: 'rgba(255,255,255,0)' }
      ]) }
    }))
  })
  return chart
}

/**
 * 创建饼图（完成率）
 */
export function createPieChart(dom, data, title) {
  const chart = echarts.init(dom)
  chart.setOption({
    title: title ? { text: title, left: 'center', top: 5, textStyle: { fontSize: 13 } } : undefined,
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 5 },
    series: [{
      type: 'pie',
      radius: title ? ['45%', '68%'] : ['40%', '65%'],
      center: ['50%', title ? '48%' : '45%'],
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
