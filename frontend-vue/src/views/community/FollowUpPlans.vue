<template>
  <div>
    <div class="toolbar">
      <el-tag>共 {{ plans.length }} 条</el-tag>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增随访计划</el-button>
    </div>

    <div v-loading="loading">
      <el-card v-for="p in plans" :key="p.id" shadow="never" style="margin-bottom:14px">
        <div class="plan-top">
          <div class="plan-info">
            <div class="muted">计划 #{{ p.id }} · 创建于 {{ fmt(p.createTime) }}</div>
            <div class="plan-title">{{ p.elderlyName || '未知老人' }} <span class="reason">{{ p.planType || '随访' }}</span></div>
            <div class="plan-meta">
              <el-tag size="small" type="info" effect="plain">周期：{{ p.cycleDays ? p.cycleDays + ' 天' : '未设置' }}</el-tag>
              <el-tag size="small" type="info" effect="plain">开始：{{ p.startDate || '未定' }}</el-tag>
              <el-tag size="small" type="info" effect="plain">下次随访：{{ p.nextVisitDate || '未定' }}</el-tag>
              <el-tag size="small" type="info" effect="plain">负责社区：{{ p.communityName || '未指定' }}</el-tag>
            </div>
          </div>
          <el-tag :type="followUpStatusTag[p.status] || 'info'" size="large">{{ p.status }}</el-tag>
        </div>
        <div v-if="p.status === '进行中'" class="plan-footer">
          <el-button size="small" @click="openEdit(p)">编辑</el-button>
          <el-button type="primary" size="small" @click="end(p.id)">结束计划</el-button>
        </div>
      </el-card>
      <el-empty v-if="!loading && !plans.length" description="暂无随访计划" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑随访计划' : '新增随访计划'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="老人" required>
          <el-select v-model="form.elderlyId" placeholder="请选择老人" style="width:100%" @change="syncCommunity">
            <el-option v-for="e in elderlyOptions" :key="e.id" :value="e.id" :label="`${e.name}（${e.phone || '无电话'}）`" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属社区">
          <el-input :model-value="communityName" disabled />
        </el-form-item>
        <el-form-item label="计划类型">
          <el-select v-model="form.planType" style="width:100%">
            <el-option label="慢性病随访" value="慢性病随访" />
            <el-option label="健康随访" value="健康随访" />
            <el-option label="康复随访" value="康复随访" />
          </el-select>
        </el-form-item>
        <el-form-item label="随访周期"><el-input-number v-model="form.cycleDays" :min="1" :max="365" controls-position="right" /> 天</el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="下次随访"><el-date-picker v-model="form.nextVisitDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">{{ editingId ? '更新' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

import { ref, reactive, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FollowUpApi, ElderlyApi } from '@/api'
import { formatDate } from '@/utils/format'
import { followUpStatusTag } from '@/utils/constants'

const fmt = formatDate
const loading = ref(false)
const saving = ref(false)
const plans = ref([])
const elderlyOptions = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const blank = () => ({ elderlyId: null, communityId: '', planType: '慢性病随访', cycleDays: null, startDate: '', nextVisitDate: '' })
const form = reactive(blank())

const communityName = computed(() => {
  const e = elderlyOptions.value.find((x) => x.id === form.elderlyId)
  return e?.communityName ? '所属社区：' + e.communityName : '所属社区：未指定'
})

async function load() {
  loading.value = true
  const res = await FollowUpApi.list()
  loading.value = false
  if (res.success) plans.value = res.data
  else ElMessage.error(res.message || '加载失败')
}

async function loadElderly() {
  const res = await ElderlyApi.list()
  if (res.success) elderlyOptions.value = res.data
}

function syncCommunity() {
  const e = elderlyOptions.value.find((x) => x.id === form.elderlyId)
  form.communityId = e && e.communityId ? e.communityId : ''
}

function openAdd() {
  Object.assign(form, blank())
  editingId.value = null
  loadElderly()
  dialogVisible.value = true
}

async function openEdit(p) {
  editingId.value = p.id
  await loadElderly()
  Object.assign(form, {
    elderlyId: p.elderlyId, communityId: p.communityId || '', planType: p.planType || '慢性病随访',
    cycleDays: p.cycleDays ?? null, startDate: p.startDate || '', nextVisitDate: p.nextVisitDate || ''
  })
  dialogVisible.value = true
}

async function save() {
  if (!form.elderlyId) { ElMessage.warning('请选择老人'); return }
  syncCommunity()
  saving.value = true
  const data = { ...form }
  const res = editingId.value ? await FollowUpApi.update(editingId.value, data) : await FollowUpApi.create(data)
  saving.value = false
  if (res.success) { ElMessage.success(editingId.value ? '更新成功' : '随访计划创建成功'); dialogVisible.value = false; load() }
  else ElMessage.error(res.message)
}

async function end(id) {
  try { await ElMessageBox.confirm('确定结束该随访计划吗？', '提示', { type: 'warning' }) } catch { return }
  const res = await FollowUpApi.end(id)
  if (res.success) { ElMessage.success('计划已结束'); load() }
  else ElMessage.error(res.message)
}

onMounted(load)
</script>

<style scoped>
.plan-top { display: flex; justify-content: space-between; align-items: flex-start; }
.plan-title { font-size: 16px; font-weight: 600; margin: 4px 0; }
.plan-title .reason { color: #909399; font-weight: 400; font-size: 13px; }
.plan-meta { display: flex; gap: 8px; flex-wrap: wrap; }
.plan-footer { text-align: right; }
</style>
