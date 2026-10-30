<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-tag>共 {{ total }} 条（含紧急处置）</el-tag>
    </div>
    <el-card shadow="never">
      <RecordTimeline :visits="visits" :falls="falls" empty-text="暂无巡访 / 处置记录，医生完成上门巡访或紧急处置后展示在这里" />
    </el-card>
  </div>
</template>

<script setup>

import { ref, computed, onMounted } from 'vue'
import { VisitApi, FallVisitApi } from '@/api'
import RecordTimeline from '@/components/RecordTimeline.vue'

const loading = ref(false)
const visits = ref([])
const falls = ref([])
const total = computed(() => visits.value.length + falls.value.length)

async function load() {
  loading.value = true
  const [vRes, fRes] = await Promise.all([VisitApi.list(), FallVisitApi.list()])
  loading.value = false
  if (vRes.success) visits.value = vRes.data || []
  if (fRes.success) falls.value = fRes.data || []
}

onMounted(load)
</script>
