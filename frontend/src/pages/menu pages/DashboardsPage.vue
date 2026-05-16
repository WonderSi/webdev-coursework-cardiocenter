<template>
  <Sidebar/>
  <div class="dashboards-grid-container">

    <div class="kpi-column col-1 row-2">
      <div class="dashboard-card kpi-card">
        <div class="card-header">
          <div class="card-icon">
            <svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18.3333 26.6667V35H8.33333C7.49237 35.0003 6.68239 34.6827 6.06575 34.1108C5.44912 33.539 5.07141 32.7553 5.00833 31.9167L5 31.6667V26.6667H18.3333ZM35 26.6667V31.6667C35.0003 32.5076 34.6827 33.3176 34.1108 33.9342C33.539 34.5509 32.7553 34.9286 31.9167 34.9917L31.6667 35H21.6667V26.6667H35ZM18.3333 16.6667V23.3333H5V16.6667H18.3333ZM35 16.6667V23.3333H21.6667V16.6667H35ZM18.3333 5V13.3333H5V8.33333C4.99973 7.49237 5.31734 6.68239 5.88916 6.06575C6.46098 5.44912 7.24474 5.07141 8.08333 5.00833L8.33333 5H18.3333ZM31.6667 5C32.5507 5 33.3986 5.35119 34.0237 5.97631C34.6488 6.60143 35 7.44928 35 8.33333V13.3333H21.6667V5H31.6667Z" fill="#0f356e"/>
            </svg>
          </div>
          <div class="card-title">Всего записей</div>
        </div>
        <span class="card-number-total-records">{{ mockKpiData.totalRecords }}</span>
      </div>

      <div class="dashboard-card kpi-card">
        <div class="card-header">
          <div class="card-icon">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#0f356e"/>
            </svg>
          </div>
          <div class="card-title">Пациентов с ССЗ</div>
        </div>
        <div class="card-two-numbers">
          <div class="percentage">
            <span class="card-number color-blue">{{ mockGenderDiagnosesData[0].malePercent }}%</span>
            <span class="sub-label color-blue">{{ mockGenderDiagnosesData[0].maleDiagnosed }} чел (М)</span>
          </div>
          <div class="percentage">
            <span class="card-number color-pink">{{ mockGenderDiagnosesData[1].femalePercent }}%</span>
            <span class="sub-label color-pink">{{ mockGenderDiagnosesData[1].femaleDiagnosed }} чел (Ж)</span>
          </div>
        </div>
      </div>
    </div>

    <ChartCard title="Употребление алкоголя" :option="chartAlcohol" gridClass="col-2 row-2">
      <template #legend-doughnut>
        <div class="doughnut-legend">
          <div class="legend-item"><span class="dot" style="background-color: #EA566F;"></span><span>Регулярно</span></div>
          <div class="legend-item"><span class="dot" style="background-color: #ff8095;"></span><span>Ранее</span></div>
          <div class="legend-item"><span class="dot" style="background-color: #1567E2;"></span><span>Никогда</span></div>
        </div>
      </template>
    </ChartCard>

    <ChartCard title="Частые диагнозы" :option="chartDiagnoses" gridClass="col-3 row-2" />

    <ChartCard title="Профессии и Риск ССЗ" :option="chartProfessions" gridClass="col-6 row-2" />

    <ChartCard title="Корреляция веса и ССЗ" :option="chartWeight" gridClass="col-3 row-2">
      <template #legend>
        <div class="custom-legend">
          <span style="color: #1567E2">●</span> Здоровые 
          <span style="color: #DF2242; margin-left: 10px">●</span> ССЗ
        </div>
      </template>
    </ChartCard>

    <ChartCard title="Возрастно-половая структура" :option="chartAgeGender" gridClass="col-3 row-2">
      <template #legend>
        <div class="custom-legend">
          <span class="legend-men">м</span>
          <span class="legend-divider">-</span>
          <span class="legend-women">ж</span>
        </div>
      </template>
    </ChartCard>

    <ChartCard title="Районы и Экология" :option="chartDistricts" gridClass="col-6 row-2">
      <template #legend>
        <div class="custom-legend-ecology">
          <span class="legend-diagnoses-label">диагнозы</span>
          <span class="legend-divider"> / </span>
          <span class="legend-ecology-label">экология</span>
        </div>
      </template>
    </ChartCard>

  </div>
</template>

<script setup lang="ts">
import Sidebar from '@/components/Sidebar.vue';
import ChartCard from '@/components/dashboards/ChartCard.vue';
import { useDashboardCharts } from '@/composables/useDashboardsCharts';
import { mockKpiData, mockGenderDiagnosesData } from '@/mocks/dashboardsData';

// ECharts
import { use } from 'echarts/core';
import { SVGRenderer } from 'echarts/renderers';
import { BarChart, PieChart } from 'echarts/charts';
import { GridComponent, TooltipComponent } from 'echarts/components';

use([SVGRenderer, BarChart, PieChart, GridComponent, TooltipComponent]);

const { 
  chartAlcohol, 
  chartDiagnoses, 
  chartAgeGender, 
  chartWeight, 
  chartProfessions, 
  chartDistricts 
} = useDashboardCharts();
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

// УТИЛИТЫ СЕТКИ
.col-1 { grid-column: span 1; }
.col-2 { grid-column: span 2; }
.col-3 { grid-column: span 3; }
.col-6 { grid-column: span 6; }

.row-1 { grid-row: span 1; }
.row-2 { grid-row: span 2; }

@media (max-width: 1400px) {
  .col-3 { grid-column: span 6; } 
  .col-1 { grid-column: span 3; }
  .col-2 { grid-column: span 6; }
}
@media (max-width: 1024px) {
  .col-1, .col-2, .col-3, .col-6 { grid-column: span 6; }
}

.color-blue { color: $color-accent !important; }
.color-pink { color: $color-pink-women !important; }
.color-green { color: $color-green !important; }

.dashboards-grid-container {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));

  // ИЗМЕНЕНО: Убираем minmax и fr. Задаем жесткий базовый шаг строки.
  // Теперь карточка .row-2 будет железно занимать: (160px * 2) + gap(24px) = 344px
  grid-auto-rows: 180px;

  grid-auto-flow: dense;
  align-content: start;
  min-height: 100vh;
  margin-left: 240px;
  padding: 40px;
  gap: 24px;
  max-width: 1600px;

  & > * {
    min-height: 0;
    min-width: 0;
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
</style>