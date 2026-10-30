<template>
  <div>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6"><el-card shadow="hover" class="mini-stat"><div class="n">{{ orders.length }}</div><div class="l">我的工单</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="mini-stat"><div class="n">{{ count['待接收'] }}</div><div class="l">待接收</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="mini-stat"><div class="n">{{ count['进行中'] + count['巡访中'] }}</div><div class="l">进行中</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="mini-stat"><div class="n">{{ count['已完成'] }}</div><div class="l">已完成</div></el-card></el-col>
    </el-row>

    <div class="toolbar">
      <el-radio-group v-model="status">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="待接收">待接收</el-radio-button>
        <el-radio-button label="进行中">进行中</el-radio-button>
        <el-radio-button label="巡访中">巡访中</el-radio-button>
        <el-radio-button label="已完成">已完成</el-radio-button>
        <el-radio-button label="已退回">已退回</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading">
      <el-card v-for="o in filteredOrders" :key="o.id" shadow="never" style="margin-bottom:14px">
        <div class="task-top">
          <div class="task-info">
            <div class="muted">工单号 {{ o.orderNo }}<template v-if="o.visitDate"> · 计划 {{ o.visitDate }}</template></div>
            <div class="task-title">{{ o.elderlyName }} <span class="reason">{{ o.reason || '（未填写巡访事由）' }}</span></div>
            <el-tag size="small" type="info" effect="plain">老人电话：{{ o.elderlyPhone || '未登记' }}</el-tag>
            <el-alert v-if="isDeviceAlert(o.eventType)" type="error" :closable="false" style="margin-top:8px" :title="`🚨 设备告警：${o.eventType}`" />
          </div>
          <el-tag :type="statusTag(o.status)" size="large">{{ o.status }}</el-tag>
        </div>
        <OrderFlowSteps :status="o.status" :event-type="o.eventType" perspective="doctor" style="margin:10px 0" />
        <div class="task-footer">
          <template v-if="o.status === '待接收'">
            <el-button type="success" size="small" @click="receive(o.id)">接收工单</el-button>
            <el-button type="warning" size="small" @click="returnOrder(o.id)">退回工单</el-button>
          </template>
          <template v-else-if="o.status === '进行中'">
            <el-button v-if="isDeviceAlert(o.eventType)" type="primary" size="small" @click="openFall(o)">🚨 填写处置记录</el-button>
            <el-button v-else type="primary" size="small" @click="startVisit(o)">上门巡访</el-button>
            <el-button type="warning" size="small" @click="returnOrder(o.id)">退回工单</el-button>
          </template>
          <template v-else-if="o.status === '巡访中'">
            <el-button v-if="isDeviceAlert(o.eventType)" type="primary" size="small" @click="openFall(o)">🚨 填写处置记录</el-button>
            <el-button v-else type="primary" size="small" @click="openVisit(o)">填写巡访记录</el-button>
            <el-button type="warning" size="small" @click="returnOrder(o.id)">退回工单</el-button>
          </template>
        </div>
      </el-card>
      <el-empty v-if="!loading && !filteredOrders.length" description="当前筛选下暂无工单" />
    </div>

    <el-dialog v-model="visitVisible" title="填写巡访记录" width="680px">
      <el-form label-width="90px">
        <VitalSignsForm v-model="visitVitals" />
        <el-form-item label="身体状态"><el-input v-model="visitForm.bodyCondition" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="生活起居"><el-input v-model="visitForm.dailyLife" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="用药情况"><el-input v-model="visitForm.medicationSituation" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="主观症状"><el-input v-model="visitForm.subjectiveSymptoms" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div style="text-align:right;margin-bottom:10px">
        <el-button :loading="aiLoading" @click="runVisitAI">🤖 AI 辅助分析</el-button>
      </div>
      <el-card v-if="visitAi" shadow="never" class="ai-result">
        <div>置信度：<b>{{ (visitAi.confidence * 100).toFixed(1) }}%</b></div>
        <p><b>初步诊断：</b>{{ visitAi.diagnosis }}</p>
        <p><b>健康建议：</b>{{ visitAi.advice }}</p>
      </el-card>
      <template #footer>
        <el-button @click="visitVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitVisit">提交巡访记录</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="fallVisible" title="🚨 填写紧急处置记录" width="680px">
      <el-form label-width="90px">
        <el-form-item label="工单"><el-input :model-value="fallOrderNo" disabled /></el-form-item>
        <el-form-item label="现场情况" required><el-input v-model="fallForm.siteCondition" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="受伤部位"><el-input v-model="fallForm.injurySite" /></el-form-item>
        <el-form-item label="伤情程度">
          <el-select v-model="fallForm.injurySeverity" style="width:100%">
            <el-option label="无" value="无" /><el-option label="轻" value="轻" /><el-option label="中" value="中" /><el-option label="重" value="重" />
          </el-select>
        </el-form-item>
        <VitalSignsForm v-model="fallVitals" />
        <el-form-item label="处置措施" required><el-input v-model="fallForm.disposalMeasure" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="是否送医">
          <el-radio-group v-model="fallForm.needHospitalization">
            <el-radio label="否">否</el-radio><el-radio label="是">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="fallForm.needHospitalization === '是'" label="送医医院">
          <el-input v-model="fallForm.hospital" placeholder="请填写送医医院" />
        </el-form-item>
        <el-form-item label="处置结果"><el-input v-model="fallForm.result" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fallVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitFall">提交处置记录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OrderApi, VisitApi, FallVisitApi, AiApi } from '@/api'
import { orderStatusTag, isDeviceAlert } from '@/utils/constants'
import OrderFlowSteps from '@/components/OrderFlowSteps.vue'
import VitalSignsForm from '@/components/VitalSignsForm.vue'

const loading = ref(false)
const submitting = ref(false)
const aiLoading = ref(false)
const orders = ref([])
const status = ref('')

const filteredOrders = computed(() =>
  status.value ? orders.value.filter((o) => o.status === status.value) : orders.value
)
const count = computed(() => {
  const c = { 待接收: 0, 进行中: 0, 巡访中: 0, 已完成: 0, 已退回: 0 }
  orders.value.forEach((o) => { c[o.status] = (c[o.status] || 0) + 1 })
  return c
})
function statusTag(s) { return orderStatusTag[s] || 'info' }

async function load() {
  loading.value = true
  const res = await OrderApi.list('')
  loading.value = false
  if (res.success) orders.value = res.data
  else ElMessage.error(res.message || '加载失败')
}

async function receive(id) {
  try { await ElMessageBox.confirm('确定要接收该工单吗？', '提示', { type: 'success' }) } catch { return }
  const res = await OrderApi.receive(id)
  if (res.success) { ElMessage.success('工单已接收'); load() }
  else ElMessage.error(res.message)
}

async function returnOrder(id) {
  try { await ElMessageBox.confirm('确定要退回该工单吗？', '提示', { type: 'warning' }) } catch { return }
  const res = await OrderApi.return(id)
  if (res.success) { ElMessage.success('工单已退回'); load() }
  else ElMessage.error(res.message)
}

async function startVisit(o) {
  const res = await OrderApi.start(o.id)
  if (res.success) {
    ElMessage.success('已开始巡访，请填写巡访记录')
    openVisit(o)
    load()
  } else ElMessage.error(res.message)
}

const visitVisible = ref(false)
const visitAi = ref(null)
const visitCtx = reactive({ orderId: null, elderlyId: null })
const visitVitals = ref({ bpSystolic: null, bpDiastolic: null, heartRate: null, bloodGlucose: null })
const visitForm = reactive({ bodyCondition: '', dailyLife: '', medicationSituation: '', subjectiveSymptoms: '' })

function openVisit(o) {
  visitCtx.orderId = o.id
  visitCtx.elderlyId = o.elderlyId
  visitVitals.value = { bpSystolic: null, bpDiastolic: null, heartRate: null, bloodGlucose: null }
  Object.assign(visitForm, { bodyCondition: '', dailyLife: '', medicationSituation: '', subjectiveSymptoms: '' })
  visitAi.value = null
  visitVisible.value = true
}

function hasVitals(v) {
  return !!(v.bpSystolic || v.bpDiastolic || v.heartRate || v.bloodGlucose)
}

async function runVisitAI() {
  aiLoading.value = true
  const res = await AiApi.diagnose({
    bpSystolic: visitVitals.value.bpSystolic, bpDiastolic: visitVitals.value.bpDiastolic,
    heartRate: visitVitals.value.heartRate, bloodGlucose: visitVitals.value.bloodGlucose,
    bodyCondition: visitForm.bodyCondition, subjectiveSymptoms: visitForm.subjectiveSymptoms
  })
  aiLoading.value = false
  if (res.success) visitAi.value = res.data
  else ElMessage.error('AI诊断失败')
}

async function submitVisit() {
  if (!hasVitals(visitVitals.value)) { ElMessage.warning('请至少填写一项体征数据（血压/心率/血糖）'); return }
  submitting.value = true
  const res = await VisitApi.create({
    orderId: visitCtx.orderId, elderlyId: visitCtx.elderlyId,
    ...visitVitals.value,
    bodyCondition: visitForm.bodyCondition, dailyLife: visitForm.dailyLife,
    medicationSituation: visitForm.medicationSituation, subjectiveSymptoms: visitForm.subjectiveSymptoms
  })
  submitting.value = false
  if (res.success) { ElMessage.success('巡访记录提交成功，工单已完成'); visitVisible.value = false; load() }
  else ElMessage.error(res.message)
}

const fallVisible = ref(false)
const fallOrderNo = ref('')
const fallCtx = reactive({ orderId: null, elderlyId: null })
const fallVitals = ref({ bpSystolic: null, bpDiastolic: null, heartRate: null, bloodGlucose: null })
const blankFall = () => ({ siteCondition: '', injurySite: '', injurySeverity: '中', disposalMeasure: '', needHospitalization: '否', hospital: '', result: '' })
const fallForm = reactive(blankFall())

function composeVitalSigns(v) {
  const parts = []
  if (v.bpSystolic || v.bpDiastolic) parts.push(`血压 ${v.bpSystolic || ''}/${v.bpDiastolic || ''} mmHg`)
  if (v.heartRate) parts.push(`心率 ${v.heartRate} 次/分`)
  if (v.bloodGlucose) parts.push(`血糖 ${v.bloodGlucose} mmol/L`)
  return parts.join('，')
}

function openFall(o) {
  fallCtx.orderId = o.id
  fallCtx.elderlyId = o.elderlyId
  fallOrderNo.value = o.orderNo
  fallVitals.value = { bpSystolic: null, bpDiastolic: null, heartRate: null, bloodGlucose: null }
  Object.assign(fallForm, blankFall())
  fallVisible.value = true
}

async function submitFall() {
  const vitalStr = composeVitalSigns(fallVitals.value)
  if (!fallForm.siteCondition && !fallForm.disposalMeasure && !vitalStr) {
    ElMessage.warning('请至少填写一项：现场情况 / 处置措施 / 体征数据'); return
  }
  submitting.value = true
  const res = await FallVisitApi.create({
    orderId: fallCtx.orderId, elderlyId: fallCtx.elderlyId,
    siteCondition: fallForm.siteCondition, injurySite: fallForm.injurySite,
    injurySeverity: fallForm.injurySeverity, vitalSigns: vitalStr,
    ...fallVitals.value,
    disposalMeasure: fallForm.disposalMeasure, needHospitalization: fallForm.needHospitalization,
    hospital: fallForm.needHospitalization === '是' ? fallForm.hospital : '', result: fallForm.result
  })
  submitting.value = false
  if (res.success) { ElMessage.success('处置记录提交成功，工单已完成'); fallVisible.value = false; load() }
  else ElMessage.error(res.message || '提交失败')
}

onMounted(load)
</script>

<style scoped>
.mini-stat { text-align: center; }
.mini-stat .n { font-size: 26px; font-weight: 700; color: #409eff; }
.mini-stat .l { font-size: 13px; color: #909399; }
.task-top { display: flex; justify-content: space-between; align-items: flex-start; }
.task-title { font-size: 16px; font-weight: 600; margin: 4px 0; }
.task-title .reason { color: #909399; font-weight: 400; font-size: 13px; }
.task-footer { text-align: right; }
.ai-result { background: #ecf5ff; margin-bottom: 12px; }
.ai-result p { margin: 6px 0; }
</style>
