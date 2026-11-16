<template>
  <Sidebar />
  <div class="dashboards-grid-container" :aria-busy="loading">
    <div class="dashboard-card dashboard-controls col-6">
      <div class="card-title">Аналитика обследований</div>
      <div class="model-filter">
        <label for="dashboard-model">Тип записей:</label>
        <AppSelect
          id="dashboard-model"
          v-model="modelKind"
          :options="modelOptions"
          label="Тип записей"
        />
      </div>
      <p>Показатели относятся к выбранному типу записей. Количество записей не равно количеству уникальных людей.</p>
      <p v-if="loading" role="status">Загрузка данных…</p>
      <p v-else-if="errorMessage" role="alert">{{ errorMessage }} <button type="button" @click="load">Повторить</button></p>
      <p v-else-if="stats?.totalRecords === 0" role="status">Для выбранного типа пока нет обследований.</p>
    </div>

    <div class="kpi-column col-3 row-2">
      <div class="dashboard-card kpi-card">
        <div class="card-header"><div class="card-title">Всего записей</div></div>
        <span class="card-number-total-records">{{ stats?.totalRecords ?? '—' }}</span>
      </div>
      <div class="dashboard-card kpi-card">
        <div class="card-header"><div class="card-title">Средний возраст</div></div>
        <span class="card-number-total-records">{{ stats?.averageAge == null ? '—' : stats.averageAge.toFixed(1) }}</span>
        <span class="sub-label"></span>
      </div>
    </div>
    <ChartCard title="Распределение по полу" :option="chartGender" :empty-text="emptyChart" gridClass="col-3 row-2" />

    <ChartCard title="Возрастно-половая структура" :option="chartAgeGender" :empty-text="emptyChart" gridClass="col-3 row-2" />
    <ChartCard title="Распределение ИМТ" :option="chartWeight" :empty-text="emptyChart" gridClass="col-3 row-2" />
    <ChartCard title="Употребление алкоголя" :option="chartAlcohol" :empty-text="emptyChart" gridClass="col-3 row-2" />
    <ChartCard title="Хронические заболевания лёгких" :option="chartLung" :empty-text="emptyChart" gridClass="col-3 row-2" />
    <ChartCard title="Распределение по профессиям" :option="chartProfessions" :empty-text="emptyChart" gridClass="col-6 row-2" />
    <ChartCard title="Распределение по районам" :option="chartDistricts" :empty-text="emptyChart" gridClass="col-3 row-2" />
    <ChartCard title="Заполненность лабораторных данных" :option="chartLabs" :empty-text="emptyChart" gridClass="col-3 row-2" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Sidebar from '@/components/Sidebar.vue'
import AppSelect from '@/components/ui/AppSelect.vue'
import ChartCard from '@/components/dashboards/ChartCard.vue'
import { useDashboardCharts } from '@/composables/useDashboardsCharts'
import { use } from 'echarts/core'
import { SVGRenderer } from 'echarts/renderers'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'

const modelOptions = [
  { value: 'city' as const, label: 'City — отдельные обследования' },
  { value: 'region' as const, label: 'Region — сводные записи' },
]

use([SVGRenderer, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent])
const { stats, modelKind, loading, errorMessage, load, chartAlcohol, chartGender, chartLung,
  chartAgeGender, chartWeight, chartProfessions, chartDistricts, chartLabs } = useDashboardCharts()
const emptyChart = computed(() => loading.value ? 'Загрузка…' : errorMessage.value
  ? 'Данные не загружены.' : !stats.value || stats.value.totalRecords === 0 ? 'Нет данных.' : undefined)
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

// УТИЛИТЫ СЕТКИ
.col-1, .col-2, .col-3 { grid-column: span 1; }
.col-6 { grid-column: 1 / -1; }
.row-1 { grid-row: span 1; }
.row-2 { grid-row: span 2; }

.color-blue { color: $color-accent !important; }
.color-pink { color: $color-pink-women !important; }
.color-green { color: $color-green !important; }

.dashboards-grid-container {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));

  grid-auto-rows: minmax(180px, auto);

  grid-auto-flow: dense;
  align-content: start;
  min-height: 100vh;
  box-sizing: border-box;
  width: calc(100% - 240px);
  max-width: 2200px;
  margin-left: 240px;
  padding: 40px;
  gap: 24px;

  & > * {
    min-height: 0;
    min-width: 0;
  }
}

@media (max-width: 768px) {
  .dashboards-grid-container {
    width: 100%;
    margin-left: 0;
    padding: 16px;
  }
}

// KPI КОЛОНКА И КАРТОЧКИ
.kpi-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

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

.kpi-card {
  flex: 1; 
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.card-icon {
  width: 40px; height: 40px;
  display: flex; align-items: center; justify-content: center;
}
.card-title {
  color: $color-blue-dark;
  font-size: 1.4rem;
  font-weight: 800;
  line-height: 1.1;
}


.card-number-total-records {
  font-size: 3rem;
  font-weight: 700;
  color: $color-blue-dark;
  line-height: 1;
  align-self: center;
  margin-top: auto;
  margin-bottom: auto;
}

.card-two-numbers {
  display: flex;
  justify-content: space-around;
  margin-top: auto;
}

.percentage {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  .card-number { font-size: 2.5rem; font-weight: 700; color: $color-text; line-height: 1.4; }
  .gender-label { font-size: 1rem; font-weight: 500; }
}

.sub-label {
  font-size: 0.9rem; font-weight: 600; opacity: 0.8; margin-top: -4px;
}

// ДОП СТИЛИ ДЛЯ ЛЕГЕНД ECHARTS
.doughnut-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 0px;

  .legend-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 1.2rem;
    font-weight: 500;
    color: $color-blue-dark;
  }

  .dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
  }
}

.custom-legend {
  display: flex; align-items: center; gap: 4px; font-size: 1.1rem; font-weight: 700; color: $color-blue-dark;
  .legend-men { color: $color-accent; } 
  .legend-women { color: $color-red; } 
  .legend-divider { color: $color-blue-dark; }
}

.custom-legend-ecology {
  display: flex; gap: 10px; font-size: 1.1rem; font-weight: 700; color: $color-blue-dark;
  .legend-ecology-label { color: $color-ecology-dark; }
  .legend-diagnoses-label { color: $color-accent; }
}
.dashboard-controls {
  gap: 12px;
  color: $color-blue-dark;
  p { margin: 0; }

}

.model-filter {
  display: grid;
  gap: 12px;
  min-width: 0;
}

.dashboards-grid-container { min-width: 0; }
.card-header { flex-wrap: wrap; min-width: 0; }
.card-title, .dashboard-controls p { overflow-wrap: anywhere; }
.model-filter { max-width: 520px; width: 100%; }
@media (min-width: 2100px) {
  .dashboards-grid-container { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 1100px) {
  .dashboards-grid-container { grid-template-columns: minmax(0, 1fr); }
}
@media (max-width: 600px) {
  .dashboards-grid-container { padding: 12px; gap: 16px; grid-auto-rows: auto; }
  .dashboard-card { padding: 16px; }
  .kpi-column { gap: 16px; }
  .kpi-card { min-height: 150px; }
}

</style>
