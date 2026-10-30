<template>
  <el-row :gutter="12">
    <el-col :span="12">
      <el-form-item label="收缩压">
        <el-input-number v-model="model.bpSystolic" :min="0" :max="300" placeholder="mmHg"
          controls-position="right" style="width:100%" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="舒张压">
        <el-input-number v-model="model.bpDiastolic" :min="0" :max="200" placeholder="mmHg"
          controls-position="right" style="width:100%" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="心率">
        <el-input-number v-model="model.heartRate" :min="0" :max="250" placeholder="次/分"
          controls-position="right" style="width:100%" />
      </el-form-item>
    </el-col>
    <el-col :span="12">
      <el-form-item label="血糖">
        <el-input-number v-model="model.bloodGlucose" :min="0" :max="60" :precision="1" :step="0.1"
          placeholder="mmol/L" controls-position="right" style="width:100%" />
      </el-form-item>
    </el-col>
  </el-row>
</template>

<script setup>

import { reactive, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['update:modelValue'])

const model = reactive({
  bpSystolic: null,
  bpDiastolic: null,
  heartRate: null,
  bloodGlucose: null,
  ...props.modelValue
})

watch(model, (v) => emit('update:modelValue', { ...v }), { deep: true })
</script>
