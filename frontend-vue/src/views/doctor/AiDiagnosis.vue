<template>
  <div>
    <el-card shadow="never">
      <el-form label-width="100px">
        <el-form-item label="选择老人">
          <el-select v-model="form.elderlyId" clearable placeholder="选择老人（用于读取健康档案）" style="width:100%">
            <el-option v-for="e in elderlyOptions" :key="e.id" :value="e.id" :label="e.name" />
          </el-select>
        </el-form-item>
        <VitalSignsForm v-model="vitals" />
        <el-form-item label="身体状态"><el-input v-model="form.bodyCondition" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="主观症状"><el-input v-model="form.subjectiveSymptoms" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div style="text-align:center">
        <el-button type="primary" :loading="loading" @click="diagnose" size="large">🤖 开始 AI 诊断</el-button>
      </div>
    </el-card>

    <el-card v-if="result" shadow="never" style="margin-top:16px" class="ai-card">
      <template #header><b>🤖 AI 分析结果</b></template>
      <el-space wrap>
        <el-tag type="success">置信度：{{ (result.confidence * 100).toFixed(1) }}%</el-tag>
        <el-tag v-if="result.riskLevel" :type="riskTag(result.riskLevel)">风险等级：{{ result.riskLevel }}</el-tag>
        <el-tag v-if="result.source === 'ai'" type="primary">大模型生成</el-tag>
        <el-tag v-else type="info">规则兜底</el-tag>
      </el-space>
      <h4 style="margin-top:14px">初步诊断</h4>
      <div class="content">{{ result.diagnosis }}</div>
      <h4 style="margin-top:14px">健康建议</h4>
      <div class="content">{{ result.advice }}</div>
      <div class="disclaimer">以上内容由 AI 或规则生成，仅供辅助参考，不能替代医生诊断。</div>
    </el-card>
  </div>
</template>

<script setup>

import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { AiApi, ElderlyApi } from '@/api'
import VitalSignsForm from '@/components/VitalSignsForm.vue'

const loading = ref(false)
const result = ref(null)
const elderlyOptions = ref([])
const vitals = ref({ bpSystolic: null, bpDiastolic: null, heartRate: null, bloodGlucose: null })
const form = reactive({ elderlyId: null, bodyCondition: '', subjectiveSymptoms: '' })

async function loadElderly() {
  const res = await ElderlyApi.list()
  if (res.success) elderlyOptions.value = res.data.list || res.data || []
}

async function diagnose() {
  const v = vitals.value
  if (!v.bpSystolic && !v.bpDiastolic && !v.heartRate && !v.bloodGlucose && !form.subjectiveSymptoms) {
    ElMessage.warning('请填写至少一项体征或症状'); return
  }
  loading.value = true
  const res = await AiApi.diagnose({
    elderlyId: form.elderlyId,
    ...v,
    bodyCondition: form.bodyCondition,
    subjectiveSymptoms: form.subjectiveSymptoms
  })
  loading.value = false
  if (res.success) result.value = res.data
  else ElMessage.error(res.message || 'AI诊断失败')
}

function riskTag(level) {
  if (level === '危急') return 'danger'
  if (level === '高') return 'warning'
  if (level === '中') return 'primary'
  return 'success'
}

onMounted(loadElderly)
</script>

<style scoped>
.ai-card { background: #f8fbff; }
.content { line-height: 1.8; color: #555; white-space: pre-wrap; }
.disclaimer { margin-top: 16px; padding-top: 10px; border-top: 1px dashed #e4e7ed; font-size: 12px; color: #909399; }
</style>
