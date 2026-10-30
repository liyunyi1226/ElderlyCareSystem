<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">数据管理后台</div>
      <el-menu :default-active="activeMenu" router class="menu" background-color="#304156"
        text-color="#bfcbd9" active-text-color="#f56c6c" :default-openeds="groups.map(g => g.title)">
        <el-sub-menu v-for="g in groups" :key="g.title" :index="g.title">
          <template #title>
            <el-icon><component :is="g.icon" /></el-icon>
            <span>{{ g.title }}</span>
          </template>
          <el-menu-item v-for="t in g.items" :key="t.key" :index="'/admin/' + t.key">
            {{ t.label }}
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">系统管理 · {{ tableName }}</div>
        <div class="header-user">
          <el-avatar :size="34" class="avatar">{{ avatarChar }}</el-avatar>
          <span class="user-name">{{ auth.user?.realName || auth.user?.username }}</span>
          <el-tag size="small" type="danger">系统管理员</el-tag>
          <el-button size="small" @click="onLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>

import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { AuthApi } from '@/api'
import { tableSchemas } from '@/views/admin/tableSchemas'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const groups = [
  { title: '老人管理', icon: 'User', items: [{ key: 'elderly', label: '老人档案' }] },
  { title: '医疗调度', icon: 'Tickets', items: [{ key: 'doctors', label: '医生信息' }, { key: 'orders', label: '派医工单' }] },
  { title: '巡访与处置', icon: 'Notebook', items: [{ key: 'visits', label: '巡访记录' }, { key: 'fallrecords', label: '摔倒处置记录' }] },
  { title: '设备与应急', icon: 'Cpu', items: [{ key: 'devices', label: '监护设备' }, { key: 'events', label: '求助事件' }] },
  { title: '健康随访', icon: 'Calendar', items: [{ key: 'followups', label: '随访计划' }] },
  { title: '系统管理', icon: 'Setting', items: [{ key: 'communities', label: '社区信息' }, { key: 'staff', label: '社区人员' }, { key: 'users', label: '用户账号' }] }
]

const activeMenu = computed(() => route.path)
const tableName = computed(() => tableSchemas[route.params.table]?.name || '')
const avatarChar = computed(() => (auth.user?.realName || auth.user?.username || '?').charAt(0))

async function onLogout() {
  try {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await AuthApi.logout()
  auth.clear()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #304156; overflow-y: auto; }
.logo {
  height: 60px; line-height: 60px; text-align: center;
  color: #fff; font-size: 17px; font-weight: 600; background: #2b3648;
}
.menu { border-right: none; }
.menu :deep(.el-sub-menu__title:hover), .menu :deep(.el-menu-item:hover) { background: #263445 !important; }
.header {
  background: #fff; display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
}
.header-title { font-size: 18px; font-weight: 600; }
.header-user { display: flex; align-items: center; gap: 10px; }
.avatar { background: #f56c6c; color: #fff; }
.user-name { font-weight: 600; }
.main { background: #f0f2f5; padding: 20px; }
</style>
