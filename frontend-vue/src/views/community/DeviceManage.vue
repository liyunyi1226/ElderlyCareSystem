<template>
  <div>
    <div class="toolbar">
      <el-tag>共 {{ devices.length }} 台</el-tag>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增设备</el-button>
    </div>

    <div v-loading="loading" class="card-grid">
      <el-card v-for="d in devices" :key="d.id" shadow="hover">
        <div class="card-head">
          <el-avatar :size="44" style="background:#5c6bc0">{{ d.deviceType === 'SOS求助' ? '🆘' : '⌚' }}</el-avatar>
          <div class="head-name">
            <h4>{{ d.deviceNo }}</h4>
            <el-tag size="small" effect="plain">{{ d.deviceType || '未知类型' }}</el-tag>
          </div>
          <el-tag :type="deviceStatusTag[d.status] || 'info'" size="small">{{ d.status || '未知' }}</el-tag>
        </div>
        <el-descriptions :column="1" size="small" style="margin:8px 0">
          <el-descriptions-item label="绑定老人">{{ d.elderlyId > 0 ? d.elderlyName : '未绑定' }}</el-descriptions-item>
          <el-descriptions-item label="安装时间">{{ d.installTime || '未登记' }}</el-descriptions-item>
        </el-descriptions>
        <div style="text-align:right">
          <el-button v-if="d.elderlyId > 0" type="danger" size="small" @click="openTrigger(d.id)">⚠️ 模拟告警</el-button>
          <el-button size="small" @click="openEdit(d.id)">编辑</el-button>
          <el-button type="danger" size="small" @click="remove(d.id)">删除</el-button>
        </div>
      </el-card>
    </div>
    <el-empty v-if="!loading && !devices.length" description="暂无监护设备" />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑设备 - ' + form.deviceNo : '新增设备'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="设备编号" required><el-input v-model="form.deviceNo" /></el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="form.deviceType" style="width:100%">
            <el-option label="跌倒监测" value="跌倒监测" />
            <el-option label="SOS求助" value="SOS求助" />
          </el-select>
        </el-form-item>
        <el-form-item label="安装时间"><el-date-picker v-model="form.installTime" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="设备状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="正常" value="正常" /><el-option label="离线" value="离线" /><el-option label="故障" value="故障" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定老人">
          <el-select v-model="form.elderlyId" clearable placeholder="暂不绑定" style="width:100%">
            <el-option v-for="e in elderlyOptions" :key="e.id" :value="e.id" :label="`${e.name}（${e.phone || '无电话'}）`" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="triggerVisible" title="模拟设备告警" width="520px">
      <el-form label-width="100px">
        <el-form-item label="选择设备" required>
          <el-select v-model="triggerForm.deviceId" style="width:100%">
            <el-option v-for="d in boundDevices" :key="d.id" :value="d.id"
              :label="`${d.deviceNo}（${d.deviceType || ''} · ${d.elderlyName || ''}）`" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件类型">
          <el-select v-model="triggerForm.eventType" style="width:100%">
            <el-option label="摔倒告警" value="摔倒告警" />
            <el-option label="手动呼救" value="手动呼救" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件地点"><el-input v-model="triggerForm.location" placeholder="如：客厅 / 卧室" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="triggerVisible = false">取消</el-button>
        <el-button type="danger" :loading="triggering" @click="doTrigger">触发告警</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

import { ref, reactive, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DeviceApi, ElderlyApi, EventApi } from '@/api'
import { deviceStatusTag } from '@/utils/constants'

const loading = ref(false)
const saving = ref(false)
const triggering = ref(false)
const devices = ref([])
const elderlyOptions = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const blank = () => ({ deviceNo: '', deviceType: '跌倒监测', installTime: '', status: '正常', elderlyId: '' })
const form = reactive(blank())

const triggerVisible = ref(false)
const triggerForm = reactive({ deviceId: null, eventType: '摔倒告警', location: '' })
const boundDevices = computed(() => devices.value.filter((d) => d.elderlyId > 0))

async function load() {
  loading.value = true
  const res = await DeviceApi.list()
  loading.value = false
  if (res.success) devices.value = res.data
  else ElMessage.error(res.message || '加载失败')
}

async function loadElderly() {
  const res = await ElderlyApi.list()
  if (res.success) elderlyOptions.value = res.data
}

function openAdd() {
  Object.assign(form, blank())
  editingId.value = null
  loadElderly()
  dialogVisible.value = true
}

async function openEdit(id) {
  const res = await DeviceApi.get(id)
  if (!res.success) { ElMessage.error(res.message); return }
  const d = res.data
  editingId.value = d.id
  Object.assign(form, {
    deviceNo: d.deviceNo || '', deviceType: d.deviceType || '跌倒监测',
    installTime: d.installTime || '', status: d.status || '正常',
    elderlyId: d.elderlyId > 0 ? d.elderlyId : ''
  })
  loadElderly()
  dialogVisible.value = true
}

async function save() {
  if (!form.deviceNo) { ElMessage.warning('请填写设备编号'); return }
  saving.value = true
  const res = editingId.value ? await DeviceApi.update(editingId.value, { ...form }) : await DeviceApi.create({ ...form })
  saving.value = false
  if (res.success) { ElMessage.success(editingId.value ? '更新成功' : '设备新增成功'); dialogVisible.value = false; load() }
  else ElMessage.error(res.message)
}

async function remove(id) {
  try { await ElMessageBox.confirm('确定删除该设备吗？', '提示', { type: 'warning' }) } catch { return }
  const res = await DeviceApi.delete(id)
  if (res.success) { ElMessage.success('删除成功'); load() }
  else ElMessage.error(res.message)
}

async function openTrigger(presetId) {
  await load()
  triggerForm.deviceId = presetId
  triggerForm.eventType = '摔倒告警'
  triggerForm.location = ''
  triggerVisible.value = true
}

async function doTrigger() {
  if (!triggerForm.deviceId) { ElMessage.warning('请选择已绑定老人的设备'); return }
  triggering.value = true
  const res = await EventApi.trigger({
    deviceId: triggerForm.deviceId, eventType: triggerForm.eventType, location: triggerForm.location
  })
  triggering.value = false
  if (res.success) {
    ElMessage.success(`告警已触发：生成求助事件 #${res.data.eventId}，自动创建急救工单 #${res.data.orderId}（待分配医生）`)
    triggerVisible.value = false
    load()
  } else ElMessage.error(res.message)
}

onMounted(load)
</script>

<style scoped>
.card-head { display: flex; align-items: center; gap: 10px; }
.head-name { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.head-name h4 { margin: 0; }
</style>
