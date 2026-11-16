<template>
  <div class="dashboard-card" :class="gridClass">
    <div class="card-header chart-header">
      <div class="card-title">{{ title }}</div>
        <slot name="legend"></slot>
    </div>
    <slot name="legend-doughnut"></slot>
    <div ref="chartContainer" class="chart-container" :style="{ minHeight: `${chartHeight}px` }">
      <p v-if="emptyText" class="chart-empty">{{ emptyText }}</p>
      <v-chart v-else class="chart" :option="displayOption" autoresize />
    </div>
  </div>
</template>

<script setup lang="ts">
import VChart from 'vue-echarts';
import { computed, onMounted, onScopeDispose, shallowRef, useTemplateRef } from 'vue'

const props = defineProps<{
  title: string;
  option: any;
  gridClass?: string;
  emptyText?: string;
}>();

const chartContainer = useTemplateRef<HTMLDivElement>('chartContainer')
const chartWidth = shallowRef(0)
let observer: ResizeObserver | undefined
onMounted(() => {
  observer = new ResizeObserver(([entry]) => { chartWidth.value = entry?.contentRect.width ?? 0 })
  if (chartContainer.value) observer.observe(chartContainer.value)
})
onScopeDispose(() => observer?.disconnect())

const chartHeight = computed(() => Math.max(260, (props.option.yAxis?.data?.length ?? 0) * 28 + 60))
const displayOption = computed(() => {
  const option = props.option
  const compact = chartWidth.value > 0 && chartWidth.value <= 400
  const categoryAxis = option.yAxis?.type === 'category'
  return {
    ...option,
    tooltip: {
      ...option.tooltip,
      confine: true,
      extraCssText: `${option.tooltip?.extraCssText ?? ''};max-width:min(360px,calc(100vw - 32px));white-space:normal;overflow-wrap:anywhere;`,
    },
    ...(option.legend ? { legend: { ...option.legend, type: compact ? 'scroll' : (option.legend.type ?? 'plain') } } : {}),
    ...(categoryAxis ? {
      yAxis: { ...option.yAxis, axisLabel: {
        ...option.yAxis.axisLabel,
        width: compact ? Math.max(75, Math.min(120, chartWidth.value * 0.38)) : (option.yAxis.axisLabel?.width ?? null),
        overflow: compact ? 'truncate' : (option.yAxis.axisLabel?.overflow ?? 'none'),
      } },
      grid: { ...option.grid, right: compact ? 24 : option.grid?.right },
    } : {}),
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.dashboard-card {
  min-width: 0;
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
  min-width: 0;
  flex-wrap: wrap;
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
  min-width: 0;
  overflow-wrap: anywhere;
  color: $color-blue-dark;
  font-size: 1.4rem;
  font-weight: 800;
  line-height: 1.1;
}

.chart-container {
  position: relative;
  min-width: 0;
  width: 100%;
  flex-grow: 1; 
  min-height: 0;
  
  .chart {
    position: absolute;
    inset: 0;
    height: 100%;
    width: 100%;
  }
}
.chart-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-blue-dark;
  text-align: center;
}

@media (max-width: 600px) {
  .dashboard-card { padding: 16px; }
}
</style>
