<template>
  <div class="dashboard-card" :class="gridClass">
    <div class="card-header chart-header">
      <div class="card-title">{{ title }}</div>
        <slot name="legend"></slot>
    </div>
    <slot name="legend-doughnut"></slot>
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
  gridClass?: string;
}>();
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.dashboard-card {
  display: flex;
  flex-direction: column;
  padding: 20px;
  border-radius: $radius-glass-card-dashboards;
  background: rgba($color-glass-white, 0.6); 
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba($color-glass-white, 0.6);
  box-shadow: 0 4px 24px rgba(7, 14, 44, 0.05);
  transition: all 0.3s ease;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.card-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-blue-dark;
}

.chart-header {
  justify-content: space-between;
  width: 100%;
}

.card-title {
  color: $color-blue-dark;
  font-size: 1.4rem;
  font-weight: 800;
  line-height: 1.1;
}

.chart-container {
  width: 100%;
  flex-grow: 1; 
  min-height: 0;
  
  .chart {
    height: 100%;
    width: 100%;
  }
}
</style>