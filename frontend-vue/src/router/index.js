import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },

  {
    path: '/community',
    component: () => import('@/layouts/WorkbenchLayout.vue'),
    meta: { role: 'community_worker' },
    redirect: '/community/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/community/DashboardView.vue'), meta: { title: '工作台' } },
      { path: 'elderly', component: () => import('@/views/community/ElderlyManage.vue'), meta: { title: '老年人管理' } },
      { path: 'doctors', component: () => import('@/views/community/DoctorManage.vue'), meta: { title: '医生管理' } },
      { path: 'orders', component: () => import('@/views/community/OrderManage.vue'), meta: { title: '派医工单' } },
      { path: 'visits', component: () => import('@/views/community/VisitRecords.vue'), meta: { title: '巡访记录' } },
      { path: 'devices', component: () => import('@/views/community/DeviceManage.vue'), meta: { title: '监护设备' } },
      { path: 'events', component: () => import('@/views/community/HelpEvents.vue'), meta: { title: '求助事件' } },
      { path: 'followups', component: () => import('@/views/community/FollowUpPlans.vue'), meta: { title: '随访计划' } }
    ]
  },

  {
    path: '/doctor',
    component: () => import('@/layouts/WorkbenchLayout.vue'),
    meta: { role: 'doctor' },
    redirect: '/doctor/tasks',
    children: [
      { path: 'tasks', component: () => import('@/views/doctor/MyTasks.vue'), meta: { title: '我的工单' } },
      { path: 'elderly', component: () => import('@/views/doctor/ElderlyFiles.vue'), meta: { title: '老人档案' } },
      { path: 'visits', component: () => import('@/views/doctor/MyVisits.vue'), meta: { title: '我的巡访记录' } },
      { path: 'ai', component: () => import('@/views/doctor/AiDiagnosis.vue'), meta: { title: 'AI辅助诊断' } }
    ]
  },

  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { role: 'system_admin' },
    redirect: '/admin/elderly',
    children: [
      { path: ':table', component: () => import('@/views/admin/AdminTableView.vue'), meta: { title: '数据库管理' } }
    ]
  },

  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.public) {

    if (to.path === '/login' && auth.isLoggedIn) return auth.homePath
    return true
  }
  if (!auth.isLoggedIn) return '/login'
  const needRole = to.matched.find((r) => r.meta.role)?.meta.role
  if (needRole && auth.role !== needRole) return auth.homePath
  return true
})

export default router
