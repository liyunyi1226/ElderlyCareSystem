<template>
  <el-steps :active="activeIndex" :finish-status="isReturned ? 'error' : 'finish'"
    align-center :process-status="isReturned ? 'error' : 'process'" class="flow-steps">
    <el-step v-for="(s, i) in steps" :key="i" :title="s" />
  </el-steps>
</template>

<script setup>

import { computed } from 'vue'
import { isDeviceAlert } from '@/utils/constants'

const props = defineProps({
  status: { type: String, default: '' },
  eventType: { type: String, default: '' },

  perspective: { type: String, default: 'community' }
})

const steps = computed(() => {
  if (props.perspective === 'doctor') {
    const third = isDeviceAlert(props.eventType) ? '上门处置' : '上门巡访'
    return ['社区派单', '已接收', third, '已完成']
  }
  return ['已派单', '医生接收', '上门巡访', '已完成']
})

const isReturned = computed(() => props.status === '已退回')

const activeIndex = computed(() => {
  if (props.perspective === 'doctor') {
    switch (props.status) {
      case '待接收': return 0
      case '进行中': return 1
      case '巡访中': return 2
      case '已完成': return 4
      case '已退回': return 0
      default: return 0
    }
  }

  switch (props.status) {
    case '待分配医生': return 0
    case '待接收': return 1
    case '进行中':
    case '巡访中': return 2
    case '已完成': return 4
    case '已退回': return 0
    default: return 0
  }
})
</script>

<style scoped>
.flow-steps { padding: 6px 8px; }
</style>
