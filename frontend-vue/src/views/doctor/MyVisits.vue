<template>
  <div v-loading="loading">
    <el-tabs v-model="tab">
      <el-tab-pane label="普通巡访" name="visit">
        <el-card shadow="never">
          <div class="muted" style="margin-bottom:10px">共 {{ visits.length }} 条</div>
          <RecordTimeline :visits="visits" :falls="[]" empty-text="暂无巡访记录" />
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="紧急处置" name="fall">
        <el-card shadow="never">
          <div class="muted" style="margin-bottom:10px">共 {{ falls.length }} 条</div>
          <RecordTimeline :visits="[]" :falls="falls" empty-text="暂无紧急处置记录" />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>

import { ref, onMounted } from 'vue'
import { VisitApi, FallVisitApi } from '@/api'
import RecordTimeline from '@/components/RecordTimeline.vue'

const loading = ref(false)
const tab = ref('visit')
const visits = ref([])
const falls = ref([])

async function load() {
  loading.value = true
  const [vRes, fRes] = await Promise.all([VisitApi.list(), FallVisitApi.list()])
  loading.value = false
  if (vRes.success) visits.value = vRes.data || []
  if (fRes.success) falls.value = fRes.data || []
}

onMounted(load)
</script>
