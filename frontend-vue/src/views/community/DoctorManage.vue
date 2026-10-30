<template>
  <div>
    <div class="toolbar">
      <el-tag>共 {{ doctors.length }} 位</el-tag>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增医生</el-button>
    </div>

    <div v-loading="loading" class="card-grid">
      <el-card v-for="d in doctors" :key="d.id" shadow="hover">
        <div class="card-head">
          <el-avatar :size="44" style="background:#67c23a">{{ (d.name || '?').charAt(0) }}</el-avatar>
          <div class="head-name">
            <h4>{{ d.name }} <el-tag size="small" effect="plain">{{ d.gender }}</el-tag></h4>
            <el-tag size="small" type="success" effect="plain">{{ d.specialization || '全科' }}</el-tag>
          </div>
        </div>
        <el-descriptions :column="1" size="small" style="margin:8px 0">
          <el-descriptions-item label="所属医院">{{ d.hospital || '未登记' }}</el-descriptions-item>
          <el-descriptions-item label="所属社区">{{ d.communityName || '未指定' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ d.phone || '未登记' }}</el-descriptions-item>
        </el-descriptions>
        <div style="text-align:right">
          <el-button size="small" @click="openEdit(d.id)">编辑资料</el-button>
        </div>
      </el-card>
    </div>
    <el-empty v-if="!loading && !doctors.length" description="暂无医生" />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑医生 - ' + form.name : '新增医生'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" style="width:100%"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select>
        </el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="专长"><el-input v-model="form.specialization" /></el-form-item>
        <el-form-item label="所属医院"><el-input v-model="form.hospital" /></el-form-item>
        <el-form-item label="所属社区" required>
          <el-select v-model="form.communityId" placeholder="请选择社区" style="width:100%">
            <el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
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
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { DoctorApi, CommunityApi } from '@/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const doctors = ref([])
const communities = ref([])
const staffList = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const blank = () => ({ name: '', gender: '男', phone: '', specialization: '', hospital: '', communityId: '' })
const form = reactive(blank())

async function load() {
  loading.value = true
  const res = await DoctorApi.list()
  loading.value = false
  if (res.success) doctors.value = res.data
  else ElMessage.error(res.message || '加载失败')
}

async function loadOptions() {
  const [cRes, sRes] = await Promise.all([CommunityApi.list(), CommunityApi.staffList()])
  if (cRes.success) communities.value = cRes.data
  if (sRes.success) staffList.value = sRes.data
}

function defaultCommunity() {
  const mine = staffList.value.find((s) => s.userId === auth.user?.id)
  return mine ? mine.communityId : ''
}

function openAdd() {
  Object.assign(form, blank(), { communityId: defaultCommunity() })
  editingId.value = null
  dialogVisible.value = true
}

async function openEdit(id) {
  const res = await DoctorApi.get(id)
  if (!res.success) { ElMessage.error(res.message); return }
  const d = res.data
  editingId.value = d.id
  Object.assign(form, {
    name: d.name || '', gender: d.gender || '男', phone: d.phone || '',
    specialization: d.specialization || '', hospital: d.hospital || '', communityId: d.communityId ?? ''
  })
  dialogVisible.value = true
}

async function save() {
  if (!form.communityId) { ElMessage.warning('请选择医生所属社区'); return }
  saving.value = true
  const res = editingId.value
    ? await DoctorApi.update(editingId.value, { ...form })
    : await DoctorApi.create({ ...form })
  saving.value = false
  if (res.success) {
    let msg = editingId.value ? '更新成功' : '新增成功'
    if (!editingId.value && res.data && res.data.username) {
      msg += `，登录账号：${res.data.username}（初始密码 123456）`
    }
    ElMessage.success(msg)
    dialogVisible.value = false
    load()
  } else {
    ElMessage.error(res.message)
  }
}

onMounted(() => {
  loadOptions()
  load()
})
</script>

<style scoped>
.card-head { display: flex; align-items: center; gap: 10px; }
.head-name { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.head-name h4 { margin: 0; }
</style>
