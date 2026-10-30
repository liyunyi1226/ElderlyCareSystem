<template>
  <div>
    <div class="toolbar">
      <div class="toolbar-left">
        <el-tag>共 {{ elderly.length }} 位</el-tag>
        <el-input v-model="keyword" placeholder="搜索姓名 / 电话 / 病史" clearable style="width:260px"
          :prefix-icon="Search" @keyup.enter="load" @clear="load" />
        <el-button type="primary" :icon="Search" @click="load">搜索</el-button>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增老人档案</el-button>
    </div>

    <div v-loading="loading" class="card-grid">
      <el-card v-for="e in elderly" :key="e.id" shadow="hover">
        <div class="card-head">
          <el-avatar :size="44">{{ (e.name || '?').charAt(0) }}</el-avatar>
          <div class="head-name">
            <h4>{{ e.name }}</h4>
            <el-tag size="small" effect="plain">{{ e.gender }}<template v-if="e.age"> · {{ e.age }}岁</template></el-tag>
            <el-tag v-if="e.communityName" size="small" type="info" effect="plain">🏘️ {{ e.communityName }}</el-tag>
          </div>
          <el-tag v-if="e.healthScore && e.healthScore.total !== null && e.healthScore.total !== undefined"
            size="small" :type="scoreTag(e.healthScore.level)">
            {{ e.healthScore.total }}分 · {{ e.healthScore.levelText }}
          </el-tag>
          <el-tag v-else-if="e.healthScore" size="small" type="info">暂无巡访记录</el-tag>
        </div>
        <el-descriptions :column="1" size="small" style="margin:8px 0">
          <el-descriptions-item label="联系电话">{{ e.phone || '未登记' }}</el-descriptions-item>
          <el-descriptions-item label="住址">{{ e.address || '未登记' }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系">{{ e.emergencyContact || '未登记' }}</el-descriptions-item>
          <el-descriptions-item label="既往病史">{{ e.chronicDiseases || '无' }}</el-descriptions-item>
          <el-descriptions-item label="身体现状">{{ e.bodyStatus || '未评估' }}</el-descriptions-item>
        </el-descriptions>
        <div style="text-align:right">
          <el-button size="small" @click="openEdit(e.id)">编辑档案</el-button>
          <el-button size="small" type="primary" @click="goDispatch(e.id)">派医生上门</el-button>
        </div>
      </el-card>
    </div>
    <el-empty v-if="!loading && !elderly.length" description="暂无老人档案" />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑老人 - ' + (form.name || '') : '新增老人'"
      width="760px" @closed="resetForm">
      <el-form :model="form" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="姓名" required><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender" style="width:100%"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="年龄"><el-input-number v-model="form.age" :min="0" :max="130" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="所属社区">
              <el-select v-model="form.communityId" clearable placeholder="未指定" style="width:100%">
                <el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="紧急联系人"><el-input v-model="form.emergencyContact" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="紧急电话"><el-input v-model="form.emergencyPhone" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="住址"><el-input v-model="form.address" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="既往病史"><el-input v-model="form.chronicDiseases" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="过敏史"><el-input v-model="form.allergies" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="现病史"><el-input v-model="form.currentIllness" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="用药情况"><el-input v-model="form.medications" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="身体现状"><el-input v-model="form.bodyStatus" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
        <HealthScorePanel v-if="editingId && currentHs" :hs="currentHs" />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ElderlyApi, CommunityApi } from '@/api'
import HealthScorePanel from '@/components/HealthScorePanel.vue'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const elderly = ref([])
const communities = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const editingId = ref(null)
const currentHs = ref(null)
const blank = () => ({
  name: '', gender: '男', age: null, phone: '', communityId: '', address: '',
  emergencyContact: '', emergencyPhone: '', chronicDiseases: '', allergies: '',
  currentIllness: '', medications: '', bodyStatus: ''
})
const form = reactive(blank())

function scoreTag(level) {
  if (['good', 'safe'].includes(level)) return 'success'
  if (['warn', 'medium'].includes(level)) return 'warning'
  if (['risk', 'danger', 'high'].includes(level)) return 'danger'
  return 'info'
}

async function load() {
  loading.value = true
  const res = await ElderlyApi.list(keyword.value)
  loading.value = false
  if (res.success) elderly.value = res.data
  else ElMessage.error(res.message || '加载失败')
}

async function loadCommunities() {
  const res = await CommunityApi.list()
  if (res.success) communities.value = res.data
}

function goDispatch(id) {
  router.push({ path: '/community/orders', query: { createElderlyId: id } })
}

function openAdd() {
  Object.assign(form, blank())
  editingId.value = null
  currentHs.value = null
  dialogVisible.value = true
}

async function openEdit(id) {
  const res = await ElderlyApi.get(id)
  if (!res.success) { ElMessage.error(res.message); return }
  const e = res.data
  editingId.value = e.id
  currentHs.value = e.healthScore || null
  Object.assign(form, {
    ...blank(),
    name: e.name || '', gender: e.gender || '男', age: e.age ?? null, phone: e.phone || '',
    communityId: e.communityId ?? '', address: e.address || '',
    emergencyContact: e.emergencyContact || '', emergencyPhone: e.emergencyPhone || '',
    chronicDiseases: e.chronicDiseases || '', allergies: e.allergies || '',
    currentIllness: e.currentIllness || '', medications: e.medications || '',
    bodyStatus: e.bodyStatus || ''
  })
  dialogVisible.value = true
}

function resetForm() {
  currentHs.value = null
}

async function save() {
  if (!form.name) { ElMessage.warning('请填写姓名'); return }
  saving.value = true
  const res = editingId.value
    ? await ElderlyApi.update(editingId.value, { ...form })
    : await ElderlyApi.create({ ...form })
  saving.value = false
  if (res.success) {
    ElMessage.success(editingId.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    load()
  } else {
    ElMessage.error(res.message)
  }
}

onMounted(() => {
  loadCommunities()
  load()
})
</script>

<style scoped>
.card-head { display: flex; align-items: center; gap: 10px; }
.head-name { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.head-name h4 { margin: 0; }
</style>
