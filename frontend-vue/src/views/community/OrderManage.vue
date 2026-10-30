<template>
  <div>
    <div class="toolbar">
      <el-radio-group v-model="status" @change="load">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="待分配医生">待分配医生</el-radio-button>
        <el-radio-button label="待接收">待接收</el-radio-button>
        <el-radio-button label="进行中">进行中</el-radio-button>
        <el-radio-button label="已完成">已完成</el-radio-button>
        <el-radio-button label="已退回">已退回</el-radio-button>
      </el-radio-group>
      <el-button type="primary" :icon="Plus" @click="openCreate">创建工单</el-button>
    </div>

    <div v-loading="loading">
      <el-card v-for="o in orders" :key="o.id" shadow="never" style="margin-bottom:14px"
        :class="urgent(o) ? 'urgent-card' : ''">
        <div class="order-top">
          <div class="order-info">
            <div class="muted">工单号 {{ o.orderNo }} · 创建于 {{ fmt(o.createTime) }}</div>
            <div class="order-title">{{ o.elderlyName }} <span class="reason">{{ o.reason || '（未填写巡访事由）' }}</span></div>
            <div class="order-meta">
              <el-tag size="small" type="info" effect="plain">指派医生：{{ o.doctorName || '待分配' }}</el-tag>
              <el-tag size="small" type="info" effect="plain">计划巡访：{{ o.visitDate || '未定日期' }}</el-tag>
            </div>
            <el-alert v-if="o.eventType" type="warning" :closable="false" style="margin-top:8px"
              :title="`📡 设备事件：${o.eventType} · ${fmt(o.eventTime)}`" />
          </div>
          <el-tag :type="statusTag(o.status)" size="large">{{ o.status }}</el-tag>
        </div>
        <OrderFlowSteps :status="o.status" perspective="community" style="margin:10px 0" />
        <div class="order-footer">
          <template v-if="o.status === '待分配医生'">
            <el-button type="primary" size="small" @click="openDispatch(o.id)">指派医生</el-button>
            <el-button type="danger" size="small" @click="removeOrder(o.id)">作废工单</el-button>
          </template>
          <el-button v-if="o.status === '已退回'" type="primary" size="small" @click="openReassign(o)">重新指派医生</el-button>
          <el-button v-if="o.status === '待接收'" type="danger" size="small" @click="removeOrder(o.id)">作废工单</el-button>
        </div>
      </el-card>
      <el-empty v-if="!loading && !orders.length" description="当前筛选下暂无工单" />
    </div>

    <el-dialog v-model="orderDialogVisible" :title="editingId ? '重新指派医生' : '创建派医工单'" width="520px">
      <el-form :model="orderForm" label-width="90px">
        <el-form-item label="老人" required>
          <el-select v-model="orderForm.elderlyId" placeholder="请选择老人" style="width:100%" :disabled="!!editingId">
            <el-option v-for="e in elderlyOptions" :key="e.id" :value="e.id"
              :label="`${e.name} (${e.phone || '无电话'})`" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生" required>
          <el-select v-model="orderForm.doctorId" placeholder="请选择医生" style="width:100%">
            <el-option v-for="d in doctorOptions" :key="d.id" :value="d.id"
              :label="`${d.name} (${d.specialization || '无专长'})`" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划日期"><el-date-picker v-model="orderForm.visitDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="巡访事由"><el-input v-model="orderForm.reason" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveOrder">{{ editingId ? '保存改派' : '创建工单' }}</el-button>
      </template>
    </el-dialog>

    <DispatchDialog v-model="dispatchVisible" :order-id="dispatchOrderId" @dispatched="load" />
  </div>
</template>

<script setup>

import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OrderApi, ElderlyApi, DoctorApi, EventApi } from '@/api'
import { formatDate } from '@/utils/format'
import { orderStatusTag } from '@/utils/constants'
import OrderFlowSteps from '@/components/OrderFlowSteps.vue'
import DispatchDialog from '@/components/DispatchDialog.vue'

const route = useRoute()
const fmt = formatDate
const loading = ref(false)
const saving = ref(false)
const orders = ref([])
const status = ref('')
const elderlyOptions = ref([])
const doctorOptions = ref([])

const orderDialogVisible = ref(false)
const editingId = ref(null)
const orderForm = reactive({ elderlyId: null, doctorId: null, visitDate: '', reason: '' })

const dispatchVisible = ref(false)
const dispatchOrderId = ref(null)

function statusTag(s) { return orderStatusTag[s] || 'info' }
function urgent(o) { return o.status === '待分配医生' }

async function load() {
  loading.value = true
  const res = await OrderApi.list(status.value)
  loading.value = false
  if (res.success) orders.value = res.data
  else ElMessage.error(res.message || '加载失败')
}

async function loadOptions() {
  const [eRes, dRes] = await Promise.all([ElderlyApi.list(), DoctorApi.list()])
  if (eRes.success) elderlyOptions.value = eRes.data
  if (dRes.success) doctorOptions.value = dRes.data
}

async function openCreate() {
  editingId.value = null
  Object.assign(orderForm, { elderlyId: null, doctorId: null, visitDate: '', reason: '' })
  await loadOptions()
  if (route.query.createElderlyId) orderForm.elderlyId = parseInt(route.query.createElderlyId)
  orderDialogVisible.value = true
}

async function openReassign(o) {
  editingId.value = o.id
  Object.assign(orderForm, { elderlyId: o.elderlyId, doctorId: o.doctorId, visitDate: o.visitDate, reason: o.reason })
  await loadOptions()
  orderForm.elderlyId = o.elderlyId
  orderForm.doctorId = o.doctorId
  orderDialogVisible.value = true
}

async function saveOrder() {
  if (!orderForm.elderlyId || !orderForm.doctorId) { ElMessage.warning('请先选择老人和医生'); return }
  saving.value = true
  const data = {
    elderlyId: orderForm.elderlyId,
    doctorId: orderForm.doctorId,
    visitDate: orderForm.visitDate,
    reason: orderForm.reason
  }
  const res = editingId.value ? await OrderApi.update(editingId.value, data) : await OrderApi.create(data)
  saving.value = false
  if (res.success) {
    ElMessage.success(editingId.value ? '工单医生已重新指派' : '工单创建成功，已派发给医生')
    orderDialogVisible.value = false
    load()
  } else {
    ElMessage.error(res.message)
  }
}

async function removeOrder(id) {
  try {
    await ElMessageBox.confirm('确定要作废该工单吗？此操作不可恢复。', '提示', { type: 'warning' })
  } catch { return }
  const res = await OrderApi.delete(id)
  if (res.success) { ElMessage.success('工单已作废'); load() }
  else ElMessage.error(res.message)
}

async function openDispatch(id) {
  dispatchOrderId.value = id
  dispatchVisible.value = true
}

async function handleEventDispatch(eventId) {
  const res = await OrderApi.list('待分配医生')
  if (!res.success) return
  const order = res.data.find((o) => o.eventId === eventId)
  if (!order) { ElMessage.warning('该事件暂无待分配的工单，请在「派医工单」页处理'); return }
  openDispatch(order.id)
}

onMounted(async () => {
  await load()
  if (route.query.dispatchEventId) {
    handleEventDispatch(parseInt(route.query.dispatchEventId))
  } else if (route.query.createElderlyId) {
    openCreate()
  }
})
</script>

<style scoped>
.urgent-card { border-left: 4px solid #f56c6c; }
.order-top { display: flex; justify-content: space-between; align-items: flex-start; }
.order-title { font-size: 16px; font-weight: 600; margin: 4px 0; }
.order-title .reason { color: #909399; font-weight: 400; font-size: 13px; }
.order-meta { display: flex; gap: 8px; flex-wrap: wrap; }
.order-footer { text-align: right; }
</style>
