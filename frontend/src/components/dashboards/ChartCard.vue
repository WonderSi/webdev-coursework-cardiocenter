<template>
  <div class="dashboard-card" :class="gridClass">
    <div class="card-header chart-header">
      <div class="card-title">{{ title }}</div>
      <slot name="legend"></slot>
    </div>
    <div class="chart-container">
      <v-chart class="chart" :option="option" autoresize />
    </div>
  </div>
</template>

<script setup lang="ts">
import VChart from 'vue-echarts';

defineProps<{
  title: string;
  option: any;
  gridClass: string;
}>();
</script>
