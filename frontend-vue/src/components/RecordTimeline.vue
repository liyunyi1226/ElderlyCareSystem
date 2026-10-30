<template>
  <el-timeline v-if="items.length">
    <el-timeline-item v-for="(it, idx) in items" :key="idx" :timestamp="fmt(it.time)" placement="top"
      :type="it.type === 'fall' ? 'danger' : 'primary'" :hollow="it.type !== 'fall'">
      <el-card shadow="never" class="record-card">
        <div class="record-header">
          <h4>{{ it.elderlyName }}</h4>
          <span class="muted">工单 {{ it.orderNo }}</span>
          <span class="muted">{{ it.type === 'fall' ? '处置医生' : '巡访医生' }}：{{ it.doctorName }}</span>
          <el-tag v-if="it.type === 'fall'" type="danger" size="small">⚠️ 紧急处置</el-tag>
        </div>

        <template v-if="it.type === 'visit'">
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="体征数据">{{ it.vitalSigns || '未记录' }}</el-descriptions-item>
            <el-descriptions-item label="身体状态">{{ it.bodyCondition || '未记录' }}</el-descriptions-item>
            <el-descriptions-item label="生活起居">{{ it.dailyLife || '未记录' }}</el-descriptions-item>
            <el-descriptions-item label="用药情况">{{ it.medicationSituation || '未记录' }}</el-descriptions-item>
            <el-descriptions-item label="主观症状">{{ it.subjectiveSymptoms || '无' }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="it.aiAdvice" class="ai-advice">🤖 AI 建议：{{ it.aiAdvice }}</div>
        </template>

        <template v-else>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="事件类型">
              {{ it.eventType || '设备告警' }} · 事发 {{ fmt(it.eventTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="现场情况">{{ it.siteCondition || '未记录' }}</el-descriptions-item>
            <el-descriptions-item label="受伤部位 / 伤情">
              {{ it.injurySite || '无' }} / {{ it.injurySeverity || '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="体征数据">{{ it.vitalSigns || '未记录' }}</el-descriptions-item>
            <el-descriptions-item label="处置措施">{{ it.disposalMeasure || '未记录' }}</el-descriptions-item>
            <el-descriptions-item label="是否送医">
              {{ it.needHospitalization === '是' ? '是 → ' + (it.hospital || '未登记医院') : '否' }}
            </el-descriptions-item>
            <el-descriptions-item label="处置结果">{{ it.result || '未记录' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-card>
    </el-timeline-item>
  </el-timeline>
  <el-empty v-else :description="emptyText" />
</template>

<script setup>

import { computed } from 'vue'
import { formatDate } from '@/utils/format'

const props = defineProps({
  visits: { type: Array, default: () => [] },
  falls: { type: Array, default: () => [] },
  merge: { type: Boolean, default: true },
  emptyText: { type: String, default: '暂无记录' }
})

const fmt = formatDate

const items = computed(() => {
  const v = props.visits.map((r) => ({ ...r, type: 'visit', time: r.createTime }))
  const f = props.falls.map((r) => ({ ...r, type: 'fall', time: r.createTime }))
  return [...v, ...f].sort((a, b) => new Date(b.time) - new Date(a.time))
})
</script>

<style scoped>
.record-card { margin-bottom: 0; }
.record-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; flex-wrap: wrap; }
.record-header h4 { margin: 0; }
</style>
