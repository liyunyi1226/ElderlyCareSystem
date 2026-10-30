<template>
  <div v-if="hs" class="score-panel">
    <el-divider content-position="left">健康评分</el-divider>
    <div class="score-top">
      <el-tag :type="hasScore ? levelTag : 'info'" size="large" effect="dark">
        总分 {{ hasScore ? hs.total : '--' }}
      </el-tag>
      <el-tag :type="hasScore ? levelTag : 'info'">{{ hasScore ? hs.levelText : '暂无巡访记录' }}</el-tag>
    </div>
    <el-table v-if="hs.details && hs.details.length" :data="hs.details" size="small" border style="margin-top:10px">
      <el-table-column prop="label" label="评分项" />
      <el-table-column prop="basis" label="依据" />
      <el-table-column label="得分" width="110">
        <template #default="{ row }">{{ row.score }} / {{ row.max }}</template>
      </el-table-column>
    </el-table>
    <div v-if="hs.formula" class="formula">计算公式：{{ hs.formula }}</div>
  </div>
</template>

<script setup>

import { computed } from 'vue'

const props = defineProps({
  hs: { type: Object, default: null }
})

const hasScore = computed(() => props.hs && props.hs.total !== null && props.hs.total !== undefined)
const levelTag = computed(() => {
  const lv = props.hs?.level
  if (lv === 'good' || lv === 'safe') return 'success'
  if (lv === 'warn' || lv === 'medium') return 'warning'
  if (lv === 'risk' || lv === 'danger' || lv === 'high') return 'danger'
  return 'info'
})
</script>

<style scoped>
.score-panel { margin-top: 8px; }
.score-top { display: flex; gap: 10px; align-items: center; }
.formula { margin-top: 8px; font-size: 12px; color: #909399; }
</style>
