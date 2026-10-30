<template>
  <el-dialog title="指派医生" :model-value="modelValue" width="520px" @update:model-value="$emit('update:modelValue', $event)">
    <div v-loading="loading">
      <el-descriptions :column="1" border size="small" v-if="order">
        <el-descriptions-item label="老人 / 事由">
          <b>{{ order.elderlyName }}</b> — {{ order.reason || '设备告警自动生成' }}
        </el-descriptions-item>
        <el-descriptions-item label="工单号">
          {{ order.orderNo }}<span v-if="order.eventType"> · 设备事件：{{ order.eventType }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-form label-width="90px" style="margin-top:16px">
        <el-form-item label="指派医生">
          <el-select v-model="doctorId" placeholder="请选择医生" style="width:100%">
            <el-option v-for="d in doctors" :key="d.id" :value="d.id"
              :label="`${d.name}（${d.communityName || '未指定社区'} · ${d.specialization || '全科'}）`" />
          </el-select>
          <div v-if="noDoctor" class="muted" style="margin-top:4px;color:#f56c6c">
            该社区暂无医生，请先在「医生名册」新增
          </div>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="confirm">确认派单</el-button>
    </template>
  </el-dialog>
</template>

<script setup>

import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { OrderApi, DoctorApi, ElderlyApi } from '@/api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  orderId: { type: Number, default: null }
})
const emit = defineEmits(['update:modelValue', 'dispatched'])

const loading = ref(false)
const submitting = ref(false)
const order = ref(null)
const doctors = ref([])
const doctorId = ref(null)
const noDoctor = ref(false)

watch(() => props.modelValue, async (visible) => {
  if (visible && props.orderId) await load()
})

async function load() {
  loading.value = true
  noDoctor.value = false
  doctorId.value = null
  const [oRes, dRes] = await Promise.all([OrderApi.list(''), DoctorApi.list()])
  loading.value = false
  if (!oRes.success) { ElMessage.error(oRes.message); return }
  order.value = oRes.data.find((o) => o.id === props.orderId)
  if (!order.value) { ElMessage.error('工单不存在'); return }
  if (!dRes.success) return

  let candidate = dRes.data
  let elderlyCommunityId = null
  const eRes = await ElderlyApi.get(order.value.elderlyId)
  if (eRes.success && eRes.data) elderlyCommunityId = eRes.data.communityId
  if (elderlyCommunityId) {
    const same = candidate.filter((d) => d.communityId === elderlyCommunityId)
    if (same.length) candidate = same
    else noDoctor.value = true
  }
  doctors.value = candidate
}

async function confirm() {
  if (!doctorId.value) { ElMessage.warning('请选择医生'); return }
  submitting.value = true
  const res = await OrderApi.dispatch(props.orderId, doctorId.value)
  submitting.value = false
  if (res.success) {
    ElMessage.success('派单成功，工单已转为待医生接收')
    emit('update:modelValue', false)
    emit('dispatched')
  } else {
    ElMessage.error(res.message)
  }
}
</script>
