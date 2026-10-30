<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">老年监护系统</div>
      <el-menu :default-active="activeMenu" router class="menu" background-color="#304156"
        text-color="#bfcbd9" active-text-color="#409eff">
        <template v-for="item in menus" :key="item.path">
          <el-menu-item :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">{{ currentTitle }}</div>
        <div class="header-user">
          <el-avatar :size="34" class="avatar">{{ avatarChar }}</el-avatar>
          <span class="user-name">{{ auth.user?.realName || auth.user?.username }}</span>
          <el-tag size="small" type="info">{{ roleText }}</el-tag>
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
import { roleLabel } from '@/utils/constants'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const communityMenus = [
  { path: '/community/dashboard', label: '工作台', icon: 'Odometer' },
  { path: '/community/elderly', label: '老年人管理', icon: 'User' },
  { path: '/community/doctors', label: '医生管理', icon: 'Avatar' },
  { path: '/community/orders', label: '派医工单', icon: 'Tickets' },
  { path: '/community/visits', label: '巡访记录', icon: 'Notebook' },
  { path: '/community/devices', label: '监护设备', icon: 'Cpu' },
  { path: '/community/events', label: '求助事件', icon: 'Bell' },
  { path: '/community/followups', label: '随访计划', icon: 'Calendar' }
]

const doctorMenus = [
  { path: '/doctor/tasks', label: '我的工单', icon: 'Tickets' },
  { path: '/doctor/elderly', label: '老人档案', icon: 'User' },
  { path: '/doctor/visits', label: '我的巡访记录', icon: 'Notebook' },
  { path: '/doctor/ai', label: 'AI辅助诊断', icon: 'MagicStick' }
]

const menus = computed(() => auth.role === 'doctor' ? doctorMenus : communityMenus)
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')
const roleText = computed(() => roleLabel(auth.role))
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
.aside { background: #304156; }
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
  background: #2b3648;
}
.menu { border-right: none; }
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
}
.header-title { font-size: 18px; font-weight: 600; }
.header-user { display: flex; align-items: center; gap: 10px; }
.avatar { background: #409eff; color: #fff; }
.user-name { font-weight: 600; }
.main { background: #f0f2f5; padding: 20px; }
</style>
