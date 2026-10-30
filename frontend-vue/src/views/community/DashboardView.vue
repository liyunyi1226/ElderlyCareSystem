<template>
  <div v-loading="loading">

    <el-card shadow="never" class="welcome">
      <h2>欢迎回来，{{ auth.user?.realName || auth.user?.username }}</h2>
      <div class="muted">{{ dateText }}</div>
      <el-tag type="success" style="margin-top:8px">{{ communityBadge }}</el-tag>
    </el-card>

    <el-row :gutter="16" style="margin:16px 0">
      <el-col :span="6">
        <el-card shadow="hover" class="stat stat-1"><div class="num">{{ stats.elderly }}</div><div class="label">老人档案</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat stat-2"><div class="num">{{ stats.doctors }}</div><div class="label">在册医生</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat stat-3"><div class="num">{{ stats.orders }}</div><div class="label">工单总数</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat stat-4"><div class="num">{{ stats.todo }}</div><div class="label">待办工单</div></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">

      <el-col :span="14">
        <el-card shadow="never">
          <template #header><b>待办工单</b></template>
          <div v-for="o in todoOrders" :key="o.id" class="todo-item" @click="go('/community/orders')">
            <el-avatar :size="38" :style="{ background: todoColor(o.status) }">
              {{ o.status === '待分配医生' ? '🚨' : o.status === '待接收' ? '⏳' : '🔄' }}
            </el-avatar>
            <div class="todo-body">
              <h4>{{ o.elderlyName }} - {{ o.reason || '巡访事由未填写' }}</h4>
              <div class="muted">指派医生: {{ o.doctorName || '待分配' }} | 计划日期: {{ o.visitDate || '未定' }} | {{ o.orderNo }}</div>
            </div>
            <el-tag :type="statusTag(o.status)" size="small">{{ o.status }}</el-tag>
          </div>
          <el-empty v-if="!todoOrders.length" description="暂无待办工单 🎉" />
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never" style="margin-bottom:16px">
          <template #header><b>最近工单</b></template>
          <div v-for="o in recentOrders" :key="'r' + o.id" class="recent-item" @click="go('/community/orders')">
            <div>
              <div>{{ o.elderlyName }} - {{ o.reason || '巡访' }}</div>
              <div class="muted">{{ o.doctorName || '待分配' }} · {{ o.visitDate || '未定日期' }}</div>
            </div>
            <el-tag :type="statusTag(o.status)" size="small">{{ o.status }}</el-tag>
          </div>
          <el-empty v-if="!recentOrders.length" description="暂无工单" />
        </el-card>

        <el-card shadow="never">
          <template #header><b>快捷入口</b></template>
          <el-space wrap>
            <el-button type="primary" @click="go('/community/elderly')">老年人管理</el-button>
            <el-button type="success" @click="go('/community/orders')">派医工单</el-button>
            <el-button type="warning" @click="go('/community/devices')">监护设备</el-button>
            <el-button @click="go('/community/events')">求助事件</el-button>
            <el-button @click="go('/community/followups')">随访计划</el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>

import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElderlyApi, DoctorApi, OrderApi, CommunityApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { orderStatusTag } from '@/utils/constants'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const stats = reactive({ elderly: 0, doctors: 0, orders: 0, todo: 0 })
const allOrders = ref([])
const communityBadge = ref('当前社区：未绑定')

const dateText = computed(() => {
  const now = new Date()
  const days = ['日', '一', '二', '三', '四', '五', '六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${days[now.getDay()]}`
})

const todoOrders = computed(() =>
  allOrders.value.filter((o) => ['待分配医生', '待接收', '进行中'].includes(o.status))
)
const recentOrders = computed(() => allOrders.value.slice(0, 5))

function statusTag(s) { return orderStatusTag[s] || 'info' }
function todoColor(status) {
  if (status === '待分配医生') return '#EF5350'
  if (status === '待接收') return '#FFA726'
  return '#42A5F5'
}
function go(path) { router.push(path) }

async function load() {
  loading.value = true
  const [eRes, dRes, oRes] = await Promise.all([ElderlyApi.list(), DoctorApi.list(), OrderApi.list('')])
  loading.value = false
  if (eRes.success) stats.elderly = eRes.data.length
  if (dRes.success) stats.doctors = dRes.data.length
  if (oRes.success) {
    allOrders.value = oRes.data
    stats.orders = oRes.data.length
    stats.todo = oRes.data.filter((o) => ['待分配医生', '待接收', '进行中'].includes(o.status)).length
  }
  const sRes = await CommunityApi.staffList()
  if (sRes.success) {
    const mine = sRes.data.find((s) => s.userId === auth.user?.id)
    communityBadge.value = mine ? `当前社区：${mine.communityName || '社区 #' + mine.communityId}` : '当前社区：未绑定'
  }
}

onMounted(load)
</script>

<style scoped>
.welcome h2 { margin: 0 0 6px; }
.stat { text-align: center; color: #fff; }
.stat :deep(.el-card__body) { padding: 18px; }
.stat-1 { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-2 { background: linear-gradient(135deg, #43cea2, #185a9d); }
.stat-3 { background: linear-gradient(135deg, #f6d365, #fda085); }
.stat-4 { background: linear-gradient(135deg, #ff6a6a, #ee0979); }
.stat .num { font-size: 30px; font-weight: 700; }
.stat .label { font-size: 13px; opacity: .9; }
.todo-item { display: flex; align-items: center; gap: 12px; padding: 10px; cursor: pointer; border-bottom: 1px solid #f0f0f0; }
.todo-item:hover { background: #f8f9fb; }
.todo-body { flex: 1; }
.todo-body h4 { margin: 0 0 4px; font-size: 14px; }
.recent-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 4px; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.recent-item:hover { background: #f8f9fb; }
</style>
