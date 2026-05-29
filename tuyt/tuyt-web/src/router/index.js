import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

// 布局
import Layout from '@/layout/index.vue'

// 需要权限的页面放在 Layout 的 children 下
export const asyncRoutes = [
  {
    path: '/work',
    component: Layout,
    redirect: '/work/todo',
    meta: { title: '我的工作', icon: 'List' },
    children: [
      {
        path: '/work/todo',
        name: 'MyTodo',
        component: () => import('@/views/work/TodoList.vue'),
        meta: { title: '我的待办件' }
      },
      {
        path: '/work/transfer',
        name: 'MyTransfer',
        component: () => import('@/views/work/TransferList.vue'),
        meta: { title: '我的运转件' }
      },
      {
        path: '/work/done',
        name: 'MyDone',
        component: () => import('@/views/work/DoneList.vue'),
        meta: { title: '我的完结件' }
      }
    ]
  },
  {
    path: '/grid',
    component: Layout,
    redirect: '/grid/manage',
    meta: { title: '网格管理', icon: 'Grid' },
    children: [
      {
        path: '/grid/manage',
        name: 'GridManage',
        component: () => import('@/views/grid/GridManage.vue'),
        meta: { title: '网格划分及管理' }
      },
      {
        path: '/grid/map',
        name: 'GridMap',
        component: () => import('@/views/grid/GridMap.vue'),
        meta: { title: '网格地图' }
      }
    ]
  },
  {
    path: '/enterprise',
    component: Layout,
    redirect: '/enterprise/list',
    meta: { title: '档案管理', icon: 'Folder' },
    children: [
      {
        path: '/enterprise/list',
        name: 'EnterpriseList',
        component: () => import('@/views/enterprise/EnterpriseList.vue'),
        meta: { title: '污染源档案' }
      },
      {
        path: '/enterprise/detail/:id',
        name: 'EnterpriseDetail',
        component: () => import('@/views/enterprise/EnterpriseDetail.vue'),
        meta: { title: '一企一档', hidden: true }
      }
    ]
  },
  {
    path: '/problem',
    component: Layout,
    redirect: '/problem/list',
    meta: { title: '环境问题管理', icon: 'WarningFilled' },
    children: [
      {
        path: '/problem/list',
        name: 'ProblemList',
        component: () => import('@/views/problem/ProblemList.vue'),
        meta: { title: '问题管理' }
      },
      {
        path: '/problem/statistics',
        name: 'ProblemStatistics',
        component: () => import('@/views/problem/ProblemStatistics.vue'),
        meta: { title: '问题统计' }
      },
      {
        path: '/problem/ranking',
        name: 'GridRanking',
        component: () => import('@/views/problem/GridRanking.vue'),
        meta: { title: '网格排名' }
      }
    ]
  },
  {
    path: '/dispatch',
    component: Layout,
    redirect: '/dispatch/screen',
    meta: { title: '综合指挥调度', icon: 'Odometer' },
    children: [
      {
        path: '/dispatch/screen',
        name: 'DispatchScreen',
        component: () => import('@/views/dispatch/Screen.vue'),
        meta: { title: '调度大屏' }
      },
      {
        path: '/dispatch/problem/:id',
        name: 'ProblemIdentify',
        component: () => import('@/views/dispatch/ProblemIdentify.vue'),
        meta: { title: '问题甄别', hidden: true }
      },
      {
        path: '/dispatch/warning',
        name: 'ProblemWarning',
        component: () => import('@/views/dispatch/ProblemWarning.vue'),
        meta: { title: '问题预警' }
      },
      {
        path: '/dispatch/task',
        name: 'TaskDispatch',
        component: () => import('@/views/dispatch/TaskDispatch.vue'),
        meta: { title: '任务调度' }
      },
      {
        path: '/dispatch/patrol',
        name: 'PatrolPlan',
        component: () => import('@/views/dispatch/PatrolPlan.vue'),
        meta: { title: '巡查计划' }
      },
      {
        path: '/dispatch/calendar',
        name: 'WorkCalendar',
        component: () => import('@/views/dispatch/WorkCalendar.vue'),
        meta: { title: '工作日历' }
      },
      {
        path: '/dispatch/voice',
        name: 'VoiceDispatch',
        component: () => import('@/views/dispatch/VoiceDispatch.vue'),
        meta: { title: '语音调度' }
      },
      {
        path: '/dispatch/returned',
        name: 'ReturnedTask',
        component: () => import('@/views/dispatch/ReturnedTask.vue'),
        meta: { title: '退回任务' }
      },
      {
        path: '/dispatch/statistics',
        redirect: '/problem/statistics',
        meta: { title: '问题统计', hidden: false }
      },
      {
        path: '/dispatch/ranking',
        redirect: '/problem/ranking',
        meta: { title: '网格排名', hidden: false }
      },
      {
        path: '/dispatch/map',
        redirect: '/map/full',
        meta: { title: '问题地图', hidden: false }
      }
    ]
  },
  {
    path: '/ledger',
    component: Layout,
    redirect: '/ledger/problem',
    meta: { title: '业务数据管理', icon: 'Document' },
    children: [
      {
        path: '/ledger/problem',
        name: 'ProblemLedger',
        component: () => import('@/views/ledger/ProblemLedger.vue'),
        meta: { title: '问题台账' }
      },
      {
        path: '/ledger/task',
        name: 'TaskLedger',
        component: () => import('@/views/ledger/TaskLedger.vue'),
        meta: { title: '任务台账' }
      },
      {
        path: '/ledger/report',
        name: 'ReportManage',
        component: () => import('@/views/ledger/ReportManage.vue'),
        meta: { title: '报表管理' }
      }
    ]
  },
  {
    path: '/assessment',
    component: Layout,
    redirect: '/assessment/manage',
    meta: { title: '考评管理', icon: 'Trophy' },
    children: [
      {
        path: '/assessment/manage',
        name: 'AssessmentManage',
        component: () => import('@/views/assessment/AssessmentManage.vue'),
        meta: { title: '考评管理' }
      },
      {
        path: '/assessment/query',
        name: 'AssessmentQuery',
        component: () => import('@/views/assessment/AssessmentQuery.vue'),
        meta: { title: '考评结果查询' }
      },
      {
        path: '/assessment/config',
        name: 'AssessmentConfig',
        component: () => import('@/views/assessment/AssessmentConfig.vue'),
        meta: { title: '考评模板配置' }
      }
    ]
  },
  {
    path: '/config',
    component: Layout,
    redirect: '/config/checkitem',
    meta: { title: '配置管理', icon: 'Setting' },
    children: [
      {
        path: '/config/checkitem',
        name: 'CheckItemManage',
        component: () => import('@/views/config/CheckItemManage.vue'),
        meta: { title: '检查项管理' }
      },
      {
        path: '/config/template',
        name: 'CheckTemplate',
        component: () => import('@/views/config/CheckTemplate.vue'),
        meta: { title: '检查模板配置' }
      },
      {
        path: '/config/contact',
        name: 'ContactBook',
        component: () => import('@/views/config/ContactBook.vue'),
        meta: { title: '通讯录' }
      }
    ]
  },
  {
    path: '/map',
    component: Layout,
    redirect: '/map/full',
    meta: { title: '电子地图', icon: 'MapLocation' },
    children: [
      {
        path: '/map/full',
        name: 'FullMap',
        component: () => import('@/views/map/FullMap.vue'),
        meta: { title: '电子地图' }
      }
    ]
  }
]

// 公开路由（无需权限）
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    redirect: '/work/todo'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: [...constantRoutes, ...asyncRoutes],
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫 — 登录拦截
router.beforeEach((to, from, next) => {
  const token = getToken()
  if (token) {
    if (to.path === '/login') {
      next('/')
    } else {
      next()
    }
  } else {
    if (to.path === '/login') {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
