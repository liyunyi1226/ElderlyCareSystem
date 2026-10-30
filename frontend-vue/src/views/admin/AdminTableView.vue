<template>
  <div v-loading="loading">
    <div class="toolbar">
      <div class="toolbar-left">
        <h3 style="margin:0">{{ schema?.name }} <span class="muted">({{ table }})</span></h3>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增记录</el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="rows" border stripe style="width:100%">
        <el-table-column v-for="col in columns" :key="col.key" :label="col.label" :min-width="col.format === 'datetime' ? 150 : 100">
          <template #default="{ row }">
            <span v-if="col.format === 'datetime'">{{ fmt(row[col.key]) }}</span>
            <el-tag v-else-if="col.format === 'status'" :type="statusTagType(row[col.key])" size="small">{{ row[col.key] || '-' }}</el-tag>
            <el-tag v-else-if="col.format === 'role'" :type="roleTagType(row[col.key])" size="small">{{ roleLabel(row[col.key]) }}</el-tag>
            <el-tag v-else-if="col.format === 'userStatus'" :type="row[col.key] == 1 ? 'success' : 'danger'" size="small">
              {{ row[col.key] == 1 ? '正常' : '禁用' }}
            </el-tag>
            <span v-else :title="row[col.key]">{{ truncate(row[col.key]) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="muted" style="margin-top:12px">共 {{ rows.length }} 条记录</div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑记录 #' + editingId : '新增' + (schema?.name || '')" width="720px">
      <el-form :model="form" label-width="150px">
        <el-row :gutter="12">
          <el-col v-for="f in visibleFields" :key="f.key" :span="f.type === 'textarea' ? 24 : 12">
            <el-form-item :label="f.label" :required="isFieldRequired(f)">
              <el-input v-if="f.type === 'text'" v-model="form[f.key]" />
              <el-input-number v-else-if="f.type === 'number'" v-model="form[f.key]" controls-position="right" style="width:100%" />
              <el-date-picker v-else-if="f.type === 'date'" v-model="form[f.key]" type="date" value-format="YYYY-MM-DD" style="width:100%" />
              <el-select v-else-if="f.type === 'select'" v-model="form[f.key]" style="width:100%" clearable placeholder="请选择">
                <el-option
                  v-for="opt in fieldOptions(f)"
                  :key="typeof opt === 'object' ? opt.value : opt"
                  :label="typeof opt === 'object' ? opt.label : opt"
                  :value="typeof opt === 'object' ? opt.value : opt"
                />
              </el-select>
              <el-input v-else-if="f.type === 'textarea'" v-model="form[f.key]" type="textarea" :rows="2" />
              <div v-if="f.hint" class="form-hint">{{ f.hint }}</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AdminApi } from '@/api'
import { formatDate } from '@/utils/format'
import { orderStatusTag, roleMap } from '@/utils/constants'
import { tableSchemas, tableColumns } from './tableSchemas'

const route = useRoute()
const fmt = formatDate

const table = computed(() => route.params.table)
const schema = computed(() => tableSchemas[table.value])
const columns = computed(() => tableColumns[table.value] || [])
const writableFields = computed(() => (schema.value?.fields || []).filter((f) => !f.readonly))

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({})

// 根据编辑/新增状态及表单联动值（如角色）决定实际显示的字段
const visibleFields = computed(() => writableFields.value.filter((f) => {
  if (f.showOn === 'add' && editingId.value) return false
  if (f.showOn === 'edit' && !editingId.value) return false
  if (f.showWhen) {
    for (const [key, allowed] of Object.entries(f.showWhen)) {
      const allow = Array.isArray(allowed) ? allowed : [allowed]
      if (!allow.includes(form[key])) return false
    }
  }
  return true
}))

function isFieldRequired(f) {
  if (f.requiredWhen) {
    for (const [key, allowed] of Object.entries(f.requiredWhen)) {
      const allow = Array.isArray(allowed) ? allowed : [allowed]
      if (allow.includes(form[key])) return true
    }
  }
  return !!f.required
}

// 远程下拉选项（如社区列表），打开弹窗前按需加载并缓存
const remoteOptions = ref({})
async function ensureRemoteOptions() {
  const keys = [...new Set(writableFields.value.filter((f) => f.optionsRemote).map((f) => f.optionsRemote))]
  await Promise.all(keys.map(async (key) => {
    if (remoteOptions.value[key]) return
    if (key === 'communities') {
      const res = await AdminApi.getTable('communities')
      if (res.success) {
        remoteOptions.value[key] = (res.data || []).map((c) => ({ label: c.name, value: c.id }))
      }
    }
  }))
}
function fieldOptions(f) {
  return f.optionsRemote ? (remoteOptions.value[f.optionsRemote] || []) : (f.options || [])
}

function statusTagType(s) { return orderStatusTag[s] || 'info' }
function roleTagType(r) { return roleMap[r]?.tag || 'info' }
function roleLabel(r) { return roleMap[r]?.label || r }
function truncate(v) {
  if (v === null || v === undefined || v === '') return '-'
  const s = String(v)
  return s.length > 30 ? s.substring(0, 30) + '...' : s
}

async function load() {
  if (!schema.value) return
  loading.value = true
  const res = await AdminApi.getTable(table.value)
  loading.value = false
  if (res.success) rows.value = res.data || []
  else ElMessage.error(res.message || '加载失败')
}

function resetForm() {
  Object.keys(form).forEach((k) => delete form[k])
  writableFields.value.forEach((f) => {
    if (f.type === 'select') form[f.key] = ''
    else form[f.key] = f.type === 'number' ? null : ''
  })
}

async function openAdd() {
  editingId.value = null
  resetForm()
  await ensureRemoteOptions()
  dialogVisible.value = true
}

async function openEdit(row) {
  editingId.value = row.id
  resetForm()
  await ensureRemoteOptions()
  writableFields.value.forEach((f) => { form[f.key] = row[f.key] ?? (f.type === 'number' ? null : '') })
  dialogVisible.value = true
}

async function save() {
  const data = {}
  visibleFields.value.forEach((f) => {
    if (form[f.key] !== null && form[f.key] !== undefined && form[f.key] !== '') data[f.key] = form[f.key]
  })

  if (table.value === 'orders' && !editingId.value) {
    data.orderNo = 'ORD' + Date.now()
  }
  saving.value = true
  const res = editingId.value
    ? await AdminApi.update(table.value, editingId.value, data)
    : await AdminApi.insert(table.value, data)
  saving.value = false
  if (res.success) {
    ElMessage.success(editingId.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    load()
  } else {
    ElMessage.error(res.message || '操作失败')
  }
}

async function remove(id) {
  const tip = schema.value?.deleteTip
  const message = tip
    ? `${tip}<br/><br/>确定要删除这条记录吗？此操作不可恢复！`
    : '确定要删除这条记录吗？此操作不可恢复！'
  try {
    await ElMessageBox.confirm(message, '删除确认', {
      type: 'warning',
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
  } catch { return }
  const res = await AdminApi.delete(table.value, id)
  if (res.success) { ElMessage.success(res.message || '删除成功'); load() }
  else ElMessage.error(res.message || '删除失败')
}

watch(table, load, { immediate: true })
onMounted(load)
</script>

<style scoped>
.form-hint {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}
</style>
