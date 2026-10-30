<template>
  <div>
    <div class="toolbar">
      <div class="toolbar-left">
        <el-radio-group v-model="scope" @change="render">
          <el-radio-button label="responsible">我负责的老人</el-radio-button>
          <el-radio-button label="all">全部老人</el-radio-button>
        </el-radio-group>
        <el-tag>共 {{ filtered.length }} 位</el-tag>
      </div>
      <el-input v-model="keyword" placeholder="搜索" clearable style="width:220px" @keyup.enter="load" @clear="load" />
    </div>

    <div v-loading="loading" class="card-grid">
      <el-card v-for="e in filtered" :key="e.id" shadow="hover">
        <div class="card-head">
          <el-avatar :size="44">{{ (e.name || '?').charAt(0) }}</el-avatar>
          <div class="head-name">
            <h4>{{ e.name }}</h4>
            <el-tag size="small" effect="plain">{{ e.gender }}<template v-if="e.age"> · {{ e.age }}岁</template></el-tag>
          </div>
          <el-tag v-if="e.healthScore && e.healthScore.total !== null && e.healthScore.total !== undefined"
            size="small" :type="scoreTag(e.healthScore.level)">{{ e.healthScore.total }}分 · {{ e.healthScore.levelText }}</el-tag>
          <el-tag v-else-if="e.healthScore" size="small" type="info">暂无巡访记录</el-tag>
        </div>
        <el-descriptions :column="1" size="small" style="margin:8px 0">
          <el-descriptions-item label="联系电话">{{ e.phone || '未登记' }}</el-descriptions-item>
          <el-descriptions-item label="住址">{{ e.address || '未登记' }}</el-descriptions-item>
          <el-descriptions-item label="现病史">{{ e.currentIllness || '无' }}</el-descriptions-item>
          <el-descriptions-item label="用药情况">{{ e.medications || '无' }}</el-descriptions-item>
          <el-descriptions-item label="身体现状">{{ e.bodyStatus || '未评估' }}</el-descriptions-item>
        </el-descriptions>
        <div style="text-align:right">
          <el-button :type="isResponsible(e.id) ? 'primary' : 'default'" size="small" @click="view(e.id)">
            {{ isResponsible(e.id) ? '编辑健康信息' : '查看档案' }}
          </el-button>
        </div>
      </el-card>
    </div>
    <el-empty v-if="!loading && !filtered.length" :description="scope === 'responsible' ? '暂无您负责的老人，请等待社区人员派单' : '暂无老人档案'" />

    <el-dialog v-model="dialogVisible" :title="'老人档案 - ' + (detail.name || '')" width="760px">
      <el-form label-width="100px">
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="姓名"><el-input v-model="detail.name" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="性别"><el-input v-model="detail.gender" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="年龄"><el-input :model-value="detail.age" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电话"><el-input v-model="detail.phone" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="住址"><el-input v-model="detail.address" disabled /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="现病史"><el-input v-model="detail.currentIllness" type="textarea" :rows="2" :disabled="!canEdit" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="用药情况"><el-input v-model="detail.medications" type="textarea" :rows="2" :disabled="!canEdit" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="身体现状"><el-input v-model="detail.bodyStatus" type="textarea" :rows="2" :disabled="!canEdit" /></el-form-item></el-col>
        </el-row>
      </el-form>

      <el-button v-if="canEdit" type="primary" :loading="saving" @click="saveHealth">保存健康信息</el-button>

      <el-divider content-position="left">历史巡访记录</el-divider>
      <RecordTimeline :visits="historyVisits" :falls="historyFalls" empty-text="暂无历史记录" />
    </el-dialog>
  </div>
</template>

<script setup>

import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ElderlyApi, VisitApi, FallVisitApi } from '@/api'
import RecordTimeline from '@/components/RecordTimeline.vue'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const responsibleIds = ref([])
const scope = ref('responsible')
const keyword = ref('')

const dialogVisible = ref(false)
const canEdit = ref(false)
const detail = reactive({})
const historyVisits = ref([])
const historyFalls = ref([])

const filtered = computed(() =>
  scope.value === 'responsible' ? list.value.filter((e) => responsibleIds.value.includes(e.id)) : list.value
)
const isResponsible = (id) => responsibleIds.value.includes(id)

function scoreTag(level) {
  if (['good', 'safe'].includes(level)) return 'success'
  if (['warn', 'medium'].includes(level)) return 'warning'
  if (['risk', 'danger', 'high'].includes(level)) return 'danger'
  return 'info'
}
function render() {  }

async function load() {
  loading.value = true
  const res = await ElderlyApi.list(keyword.value)
  loading.value = false
  if (!res.success) return
  list.value = res.data.list || res.data || []
  responsibleIds.value = res.data.responsibleIds || []
}

async function view(id) {
  const res = await ElderlyApi.get(id)
  if (!res.success) { ElMessage.error(res.message); return }
  Object.keys(detail).forEach((k) => delete detail[k])
  Object.assign(detail, res.data)
  canEdit.value = responsibleIds.value.includes(id)
  dialogVisible.value = true

  const [vRes, fRes] = await Promise.all([VisitApi.getByElderly(id), FallVisitApi.getByElderly(id)])
  historyVisits.value = vRes.success ? vRes.data || [] : []
  historyFalls.value = fRes.success ? fRes.data || [] : []
}

async function saveHealth() {
  saving.value = true
  const res = await ElderlyApi.updateHealth(detail.id, {
    currentIllness: detail.currentIllness, medications: detail.medications, bodyStatus: detail.bodyStatus
  })
  saving.value = false
  if (res.success) { ElMessage.success('健康信息保存成功'); dialogVisible.value = false; load() }
  else ElMessage.error(res.message || '保存失败')
}

onMounted(load)
</script>

<style scoped>
.card-head { display: flex; align-items: center; gap: 10px; }
.head-name { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.head-name h4 { margin: 0; }
</style>
