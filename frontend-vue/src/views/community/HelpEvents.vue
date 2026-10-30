<template>
  <div>
    <div class="toolbar"><el-tag>共 {{ events.length }} 条</el-tag></div>

    <div v-loading="loading">
      <el-card v-for="ev in events" :key="ev.id" shadow="never" style="margin-bottom:14px"
        :class="ev.status === '待处理' ? 'pending-card' : ''">
        <div class="event-top">
          <div class="event-info">
            <div class="muted">事件 #{{ ev.id }} · {{ fmt(ev.eventTime) }}</div>
            <div class="event-title">{{ ev.eventType }} <span class="reason">{{ ev.elderlyName || '未知老人' }}</span></div>
            <div class="event-meta">
              <el-tag size="small" type="info" effect="plain">设备：{{ ev.deviceNo || '未知设备' }}</el-tag>
              <el-tag size="small" type="info" effect="plain">地点：{{ ev.location || '未登记' }}</el-tag>
              <el-tag v-if="ev.orderNo" size="small" type="info" effect="plain">关联工单：{{ ev.orderNo }}（{{ ev.orderStatus || '' }}）</el-tag>
            </div>
          </div>
          <el-tag :type="eventStatusTag[ev.status] || 'info'" size="large">{{ ev.status }}</el-tag>
        </div>
        <div v-if="ev.status === '待处理'" class="event-footer">
          <el-button type="primary" size="small" @click="goDispatch(ev.id)">派单处理</el-button>
        </div>
      </el-card>
      <el-empty v-if="!loading && !events.length" description="暂无求助事件，设备触发告警后的事件存档展示在这里" />
    </div>
  </div>
</template>

<script setup>

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { EventApi } from '@/api'
import { formatDate } from '@/utils/format'
import { eventStatusTag } from '@/utils/constants'

const router = useRouter()
const fmt = formatDate
const loading = ref(false)
const events = ref([])

async function load() {
  loading.value = true
  const res = await EventApi.list()
  loading.value = false
  if (res.success) events.value = res.data
  else ElMessage.error(res.message || '加载失败')
}

function goDispatch(eventId) {
  router.push({ path: '/community/orders', query: { dispatchEventId: eventId } })
}

onMounted(load)
</script>

<style scoped>
.pending-card { border-left: 4px solid #e6a23c; }
.event-top { display: flex; justify-content: space-between; align-items: flex-start; }
.event-title { font-size: 16px; font-weight: 600; margin: 4px 0; }
.event-title .reason { color: #909399; font-weight: 400; font-size: 13px; }
.event-meta { display: flex; gap: 8px; flex-wrap: wrap; }
.event-footer { text-align: right; }
</style>
