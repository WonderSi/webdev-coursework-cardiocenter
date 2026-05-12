<template>
  <sidebar/>
  <div class="dashboards-grid-container">

    <!-- ================= СТРОКА 1 (Маленькие карточки, Высота 1) ================= -->

    <!-- Карточка: Всего записей -->
    <div class="dashboard-card col-1 row-1">
      <div class="card-header">
        <div class="card-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18.3333 26.6667V35H8.33333C7.49237 35.0003 6.68239 34.6827 6.06575 34.1108C5.44912 33.539 5.07141 32.7553 5.00833 31.9167L5 31.6667V26.6667H18.3333ZM35 26.6667V31.6667C35.0003 32.5076 34.6827 33.3176 34.1108 33.9342C33.539 34.5509 32.7553 34.9286 31.9167 34.9917L31.6667 35H21.6667V26.6667H35ZM18.3333 16.6667V23.3333H5V16.6667H18.3333ZM35 16.6667V23.3333H21.6667V16.6667H35ZM18.3333 5V13.3333H5V8.33333C4.99973 7.49237 5.31734 6.68239 5.88916 6.06575C6.46098 5.44912 7.24474 5.07141 8.08333 5.00833L8.33333 5H18.3333ZM31.6667 5C32.5507 5 33.3986 5.35119 34.0237 5.97631C34.6488 6.60143 35 7.44928 35 8.33333V13.3333H21.6667V5H31.6667Z" fill="#0f356e"/>
          </svg>
        </div>
        <div class="card-title">Всего записей о пациентах</div>
      </div>
      <span class="card-number-total-records">{{ mockKpiData.totalRecords }}</span>
    </div>

    <!-- Карточка: % диагностированных м-ж -->
    <div class="dashboard-card col-1 row-1">
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

    <!-- Карточка алкоголь -->
    <div class="dashboard-card col-2 row-2">
      <div class="card-header chart-header">
        <div class="card-title">Употребление алкоголя</div>
      </div>
      <div class="doughnut-legend">
        <div class="legend-item">
          <span class="dot" style="background-color: #EA566F;"></span>
          <span>Регулярно</span>
        </div>
        <div class="legend-item">
          <span class="dot" style="background-color: #ff8095;"></span>
          <span>Ранее</span>
        </div>
        <div class="legend-item">
          <span class="dot" style="background-color: #1567E2;"></span>
          <span>Никогда</span>
        </div>
      </div>
      <div class="chart-container">
        <v-chart class="chart" :option="chartAlcohol" autoresize />
      </div>
    </div>

    <!-- Карточка: Чаще всего диагностируют -->
    <div class="dashboard-card col-3 row-2">
      <div class="card-header chart-header">
        <div class="card-title">Частые диагнозы</div>
      </div>
      <div class="chart-container">
        <v-chart class="chart" :option="chartDiagnoses" autoresize />
      </div>
    </div>

    <!-- Карточка: Распределение пол-возраст -->
    <div class="dashboard-card col-3 row-2">
      <div class="card-header chart-header">
        <div class="card-title">Возрастно-половая структура</div>
        <div class="custom-legend">
          <span class="legend-men">м</span>
          <span class="legend-divider">-</span>
          <span class="legend-women">ж</span>
        </div>
      </div>
      <div class="chart-container">
        <v-chart class="chart" :option="chartAgeGender" autoresize />
      </div>
    </div>

    <!-- Карточка: Вес - диагнозы -->
    <div class="dashboard-card col-3 row-2">
      <div class="card-header chart-header">
        <div class="card-title">Корреляция веса и ССЗ</div>
        <div class="custom-legend">
          <span style="color: #1567E2">●</span> Здоровые 
          <span style="color: #DF2242; margin-left: 10px">●</span> ССЗ
        </div>
      </div>
      <div class="chart-container">
        <v-chart class="chart" :option="chartWeight" autoresize />
      </div>
    </div>

    <!-- Карточка: Профессии -->
    <div class="dashboard-card col-6 row-2">
      <div class="card-header chart-header">
        <div class="card-title">Профессии и Риск ССЗ</div>
      </div>
      <div class="chart-container">
        <v-chart class="chart" :option="chartProfessions" autoresize />
      </div>
    </div>

    <!-- Карточка: Районы и экология -->
    <div class="dashboard-card col-6 row-2">
      <div class="card-header chart-header">
        <div class="card-title">Районы и Экология</div>
        <div class="custom-legend-ecology">
          <span class="legend-diagnoses-label">диагнозы</span>
          <span class="legend-divider"> / </span>
          <span class="legend-ecology-label">экология</span>
        </div>
      </div>
      <div class="chart-container">
        <v-chart class="chart" :option="chartDistricts" autoresize />
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { use } from 'echarts/core'
import { SVGRenderer } from 'echarts/renderers'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import Sidebar from '@/components/Sidebar.vue'
import { mockKpiData, mockAgeGenderData, mockGenderDiagnosesData, mockWeightData, mockAlcoholConsumptionData, mockProfessionData, mockMostDiagnosedData, mockDistrictData } from '@/mocks/dashboardsData'

// ECharts модули
use([SVGRenderer, BarChart, GridComponent, TooltipComponent, PieChart]);

// ECharts конфиг для распределения возраст-пол-здоровье:
const chartAgeGender = computed(() => {
  return {
    render: 'svg',
    tooltip: {
      trigger: 'axis',
      borderRadius: 12,
      padding: 12,
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(32, 92, 182, 0.35)',
      borderWidth: 1,
      textStyle: {
        color: '#0f356e', // $color-blue-dark
        fontSize: 16,
        fontFamily: 'Inter, sans-serif'
      },
      formatter: function (params: any) {
        let res = `<div style="margin-bottom: 8px; font-size: 16px;">
                      <strong>${params[0].axisValue}</strong>
                    </div>`;
        params.forEach((item: any) => {
          res += `<div style="display: flex; justify-content: space-between; gap: 20px; margin-bottom: 4px;">
                    <span>${item.marker} ${item.seriesName}</span>
                    <span style="font-weight: 700;">${item.value}</span>
                  </div>`;
        });
        return res;
      }
    },
    legend: { show: false },
    grid: { top: '0%', left: '5%', right: '0%', bottom: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      z: 10,
      name: 'возрастная группа',
      nameLocation: 'center',
      nameGap: 40,
      nameTextStyle: {
        color: '#0f356e', // $color-blue-dark;
        fontWeight: 400,
        fontSize: 16
      },
      axisLabel: { 
        color: '#0f356e',
        fontWeight: 400,
        fontSize: 14
      },
      axisLine: {
        show: true,
        lineStyle: { color: '#0f356e', width: 2 }
      },
      data: mockAgeGenderData.map(item => item.ageGroup),
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: 'число пациентов',
      nameLocation: 'middle',
      nameGap: 40,
      nameRotate: -90,
      nameTextStyle: {
        color: '#0f356e', // $color-blue-dark;
        fontWeight: 400,
        fontSize: 16
      },
      axisLabel: { 
        color: '#0f356e',
        fontWeight: 400,
        fontSize: 14,
        verticalAlign: 'bottom',
        padding: [0, 0, 0, 0]
      },                       // $color-accent-lighter;
      splitLine: { lineStyle: { color: '#8AB3F1' } },
      axisLine: {
        show: true,
        lineStyle: { color: '#0f356e', width: 2 }
      },
    },
    series: [
      // --- МУЖЧИНЫ
      {
        name: 'Диаг',
        type: 'bar',
        stack: 'male',
        barWidth: 30,
        data: mockAgeGenderData.map(item => item.healthyMale),
        itemStyle: { color: '#5e9bf7' } // $color-blue-med
      },
      {
        name: 'Диагностированные (М)',
        type: 'bar',
        stack: 'male',
        data: mockAgeGenderData.map(item => item.diagnosedMale),
        itemStyle: { 
          color: '#1567E2', // $color-accent
          borderRadius: [2, 2, 0, 0]
        }
      },
      // --- ЖЕНЩИНЫ
      {
        name: 'Здоровые (Ж)',
        type: 'bar',
        stack: 'female',
        barWidth: 30,
        data: mockAgeGenderData.map(item => item.healthyFemale),
        itemStyle: { color: '#ff8095' } // $color-light-red
      },
      {
        name: 'Диагностированные (Ж)',
        type: 'bar',
        stack: 'female',
        data: mockAgeGenderData.map(item => item.diagnosedFemale),
        itemStyle: { 
          color: '#DF2242', // $color-red
          borderRadius: [2, 2, 0, 0] 
        }
      }
    ]
  };
});

// -------- КАРТОЧКА ВЕС + ССЗ
const chartWeight = computed(() => {
  return {
    renderer: 'svg',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'none' },
      borderRadius: 12,
      padding: 12,
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(32, 92, 182, 0.35)',
      borderWidth: 1,
      textStyle: {
        color: '#0f356e', // $color-blue-dark
        fontSize: 16,
        fontFamily: 'Inter, sans-serif'
      },
      formatter: function (params: any) {
        let res = `<div style="margin-bottom: 8px; font-size: 16px;">
                      <strong>${params[0].axisValue}</strong>
                    </div>`;
        params.forEach((item: any) => {
          res += `<div style="display: flex; justify-content: space-between; gap: 20px; margin-bottom: 4px;">
                    <span>${item.marker} ${item.seriesName}</span>
                    <span style="font-weight: 700;">${item.value}</span>
                  </div>`;
        });
        return res;
      }
    },
    grid: { top: '0%', left: '0%', right: '7%', bottom: '15%', containLabel: true },
    xAxis: [
      {
        type: 'value',
        name: 'число пациентов',
        nameLocation: 'center',
        nameGap: 40,
        nameTextStyle: { color: '#0f356e', fontWeight: 400, fontSize: 16},
        axisLabel: { color: '#0f356e', fontWeight: 400, fontSize: 14 },
        splitLine: { lineStyle: { color: '#8AB3F1' } } // $color-accent-lighter
      }
    ],
    yAxis: {
      type: 'category',
      z: 10,
      data: mockWeightData.map(item => item.category),
      axisLabel: { color: '#0f356e', fontWeight: 600, fontSize: 16 },
      axisLine: { lineStyle: { color: '#0f356e', width: 2 } }
    },
    series: [
      {
        name: 'Здоровые',
        type: 'bar',
        stack: 'total',
        barWidth: 30,
        data: mockWeightData.map(item => item.healthyCount),
        itemStyle: { color: '#5e9bf7' } // $color-blue-med
      },
      {
        name: 'С диагнозом',
        type: 'bar',
        stack: 'total',
        data: mockWeightData.map(item => item.diagnosedCount),
        itemStyle: { color: '#EA566F' }, // $color-cold-red
        // % диагностированных поверх палок
        label: {
          show: true,
          position: 'right',
          distance: 4,
          formatter: (params: any) => {
            const percent = mockWeightData[params.dataIndex].diagnosedPercent;
            return percent + '%'
          },
          color: '#0f356e', // $color-blue-dark
          fontWeight: 600,
          fontSize: 16
        }
      }
    ]
  };
});

const chartAlcohol = computed(() => {
  return {
    renderer: 'svg',
    tooltip: {
      trigger: 'item',
      borderRadius: 12,
      padding: 12,
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(13, 60, 129, 0.2)',
      borderWidth: 1,
      textStyle: { color: '#0f356e', fontSize: 14, fontFamily: 'Inter, sans-serif' },
      formatter: (params: any) => {
        return `
          <div style="margin-bottom: 8px; font-size: 15px;">
            <strong>${params.name}</strong>
          </div>
          <div style="display: flex; justify-content: space-between; gap: 20px; margin-bottom: 4px;">
            <span>${params.marker} Всего пациентов:</span>
            <span style="font-weight: 700;">${params.value} чел.</span>
          </div>
          <div style="display: flex; justify-content: space-between; gap: 20px;">
            <span style="margin-left: 14px; opacity: 0.8;">Процент ССЗ:</span>
            <span style="font-weight: 700; color: #DF2242;">${params.data.diagnosedPercent}%</span>
          </div>
        `;
      }
    },
    legend: { show: false },
    series: [
      {
        name: 'Алкоголь',
        type: 'pie',
        radius: ['40%', '80%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 0,
          borderColor: 'transparent',
          borderWidth: 0
        },
        label: {
          show: false,
          position: 'center'
        },
        labelLine: { show: false },
        data: mockAlcoholConsumptionData.map(item => {
          let sliceColor = '#1567E2'; // $color-accent (Никогда не употреблял)
          if (item.category === 'Употребляю') sliceColor = '#EA566F'; // $color-cold-red
          if (item.category === 'Употреблял ранее') sliceColor = '#ff8095'; // $color-light-red

          return {
            value: item.healthyCount,
            name: item.category,
            diagnosedPercent: item.diagnosedPercent,
            itemStyle: { color: sliceColor }
          };
        })
      }
    ]
  };
});

// -------- КАРТОЧКА 6: ПРОФЕССИИ
const chartProfessions = computed(() => {
  return {
    renderer: 'svg',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'none' },
      borderRadius: 12,
      padding: 12,
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(13, 60, 129, 0.2)',
      borderWidth: 1,
      textStyle: { color: '#0f356e', fontSize: 14 },
      formatter: (params: any) => {
        const data = mockProfessionData[params[0].dataIndex];
        return `
          <div style="margin-bottom: 8px; font-size: 15px;"><strong>${data.profName}</strong></div>
          <div style="display: flex; justify-content: space-between; gap: 20px; margin-bottom: 4px;">
            <span>Диагностировано:</span>
            <span style="font-weight: 700;">${data.diagnosedCount} чел.</span>
          </div>
          <div style="display: flex; justify-content: space-between; gap: 20px;">
            <span>% имеющих ССЗ от общего числа:</span>
            <span style="font-weight: 700; color: #DF2242;">${data.diagnosedPercent}%</span>
          </div>
        `;
      }
    },
    grid: { top: '0%', left: '0%', right: '7%', bottom: '15%', containLabel: true },
    xAxis: { 
      type: 'value', 
      name: 'число пациентов',
      nameLocation: 'center',
      nameGap: 40,
      nameTextStyle: { color: '#0f356e', fontWeight: 400, fontSize: 16},
      splitLine: { lineStyle: { color: '#8AB3F1' } }, // $color-accent-lighter 
      axisLabel: { color: '#0f356e', fontWeight: 400, fontSize: 14 },
    },
    yAxis: { 
      type: 'category', 
      inverse: true,
      z: 10,
      data: mockProfessionData.map(item => item.profName),
      axisLabel: { color: '#0f356e', fontWeight: 600, fontSize: 16 },
      axisLine: { lineStyle: { color: '#0f356e', width: 2 } },
      axisTick: { show: false }
    },
    series: [{
      name: 'случаев ССЗ',
      type: 'bar',
      data: mockProfessionData.map(item => item.diagnosedCount),
      itemStyle: { color: '#DF2242', borderRadius: [0, 2, 2, 0] },
      barWidth: 30,
      label: { 
        show: true, 
        position: 'right', 
        formatter: '{c}', // Цифра справа от бара
        color: '#0f356e', 
        fontWeight: 600,
        fontSize: 16
      }
    }]
  };
});

// -------- КАРТОЧКА 7: ТОП ДИАГНОЗОВ
const chartDiagnoses = computed(() => {
  return {
    renderer: 'svg',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'none' },
      borderRadius: 12,
      padding: 12,
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(13, 60, 129, 0.2)',
      borderWidth: 1,
      textStyle: { color: '#0f356e', fontSize: 14 },
      formatter: (params: any) => {
        const data = mockMostDiagnosedData[params[0].dataIndex];
        return `
          <div style="margin-bottom: 8px; font-size: 15px;"><strong>${data.diagName}</strong></div>
          <div style="display: flex; justify-content: space-between; gap: 20px; margin-bottom: 4px;">
            <span>Диагностировано:</span>
            <span style="font-weight: 700;">${data.diagnosedCount}</span>
          </div>
          <div style="display: flex; justify-content: space-between; gap: 20px;">
            <span>Доля от всех ССЗ:</span>
            <span style="font-weight: 700; color: #1567E2;">${data.percent}%</span>
          </div>
        `;
      }
    },
    grid: { top: '0%', left: '0%', right: '7%', bottom: '15%', containLabel: true },
    xAxis: { 
      type: 'value', 
      name: 'число пациентов',
      nameLocation: 'center',
      nameGap: 40,
      nameTextStyle: { color: '#0f356e', fontWeight: 400, fontSize: 16},
      splitLine: { lineStyle: { color: '#8AB3F1' } }, // $color-accent-lighter 
      axisLabel: { color: '#0f356e', fontWeight: 400, fontSize: 14 },
    },
    yAxis: { 
      type: 'category', 
      z: 10,
      inverse: true, 
      data: mockMostDiagnosedData.map(item => item.diagName),
      axisLabel: { color: '#0f356e', fontWeight: 600, fontSize: 16 },
      axisLine: { lineStyle: { color: '#0f356e', width: 2 } },
      axisTick: { show: false }
    },
    series: [{
      name: 'Случаи',
      type: 'bar',
      data: mockMostDiagnosedData.map(item => item.diagnosedCount),
      itemStyle: { color: '#1567E2', borderRadius: [0, 4, 4, 0] },
      barWidth: 30,
      label: { 
        show: true, 
        position: 'right', 
        formatter: '{c}%',
        color: '#0f356e', 
        fontWeight: 600,
        fontSize: 16
      }
    }]
  };
});

// -------- КАРТОЧКА 8: РАЙОНЫ И ЭКОЛОГИЯ
const chartDistricts = computed(() => {
  return {
    renderer: 'svg',
    tooltip: {
      trigger: 'axis',
      borderRadius: 12,
      padding: 12,
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(13, 60, 129, 0.2)',
      borderWidth: 1,
      textStyle: { color: '#0f356e', fontSize: 14 },
      formatter: (params: any) => {
        const data = mockDistrictData[params[0].dataIndex];
        return `
          <div style="margin-bottom: 8px; font-size: 15px;"><strong>${data.name}</strong></div>
          <div style="display: flex; justify-content: space-between; gap: 20px; margin-bottom: 4px;">
            <span><span style="color:#1567E2">●</span> Диагнозы:</span>
            <span style="font-weight: 700;">${data.numberOfDiagnoses}</span>
          </div>
          <div style="display: flex; justify-content: space-between; gap: 20px;">
            <span><span style="color:#20B2AA">●</span> Экология (...?):</span>
            <span style="font-weight: 700;">${data.EcologyData}</span>
          </div>
        `;
      }
    },
    legend: {
      bottom: 0,
      icon: 'circle',
      textStyle: { color: '#0f356e', fontWeight: 600 }
    },
    grid: { top: '0%', left: '0%', right: '7%', bottom: '0%', containLabel: true },
    xAxis: [
      { 
        type: 'value', 
        splitLine: { lineStyle: { color: '#8AB3F1' } },
        axisLabel: { color: '#0f356e', fontWeight: 400, fontSize: 14 },
      },
      { 
        type: 'value', 
        splitLine: { show: false },
        axisLabel: { color: '#20B2AA', fontWeight: 400, fontSize: 14 },
      }
    ],
    yAxis: { 
      type: 'category',
      z: 10,
      inverse: true, 
      data: mockDistrictData.map(item => item.name),
      axisLabel: { color: '#0f356e', fontWeight: 600, fontSize: 16 },
      axisLine: { lineStyle: { color: '#0f356e', width: 2 } },
      axisTick: { show: false }
    },
    series: [
      {
        name: 'Диагнозы',
        type: 'bar',
        xAxisIndex: 0, // Привязываем к первой оси X
        data: mockDistrictData.map(item => item.numberOfDiagnoses),
        itemStyle: { color: '#1567E2', borderRadius: [0, 1, 1, 0] },
        barGap: '10%'
      },
      {
        name: 'Экология',
        type: 'bar',
        xAxisIndex: 1, // Привязываем ко второй оси X
        data: mockDistrictData.map(item => item.EcologyData),
        itemStyle: { color: '#20B2AA', borderRadius: [0, 1, 1, 0] }
      }
    ]
  };
});
  
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

// Утилиты
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

// -----------------------------------------------------------------

.color-blue { color: $color-accent !important; }
.color-pink { color: $color-pink-women !important; }
.color-green { color: $color-green !important; }

.dashboards-grid-container {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  grid-auto-rows: minmax(160px, auto);
  grid-auto-flow: dense;
  align-content: start;
  
  min-height: 100vh;
  margin-left: 240px; // для сайдбара
  padding: 40px;
  gap: 24px;

  max-width: 1600px;
}

// -------------- ОБЩИЙ СТИЛЬ ДЛЯ ВСЕХ КАРТОЧЕК
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

// -----------------------------------------------------------------

// --- СТИЛИ ДЛЯ МАЛЕНЬКИХ ВЕРХНИХ КАРТОЧЕК
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
  align-self: start;
  line-height: 1.4; // костыль чтобы было по центру карточки
}

.card-two-numbers {
  display: flex;
  justify-content: space-around;
  gap: 16px;
}

.percentage {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;

  .card-number {
    font-size: 2.5rem;
    font-weight: 700;
    color: $color-text;
    line-height: 1.4; // костыль чтобы было по центру карточки
  }

  .gender-label {
    font-size: 1rem;
    font-weight: 500;
  }
}

.sub-label {
  font-size: 0.9rem;
  font-weight: 600;
  opacity: 0.8;
  margin-top: -4px; // Подтягиваем ближе к числу
}

// ------------------------------- доп для ECharts
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.custom-legend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 1.1rem;
  font-weight: 700;
  color: $color-blue-dark;

  .legend-men { color: $color-accent; } // Blue
  .legend-women { color: $color-red; } // Red
  .legend-divider { color: $color-blue-dark; }
}

.custom-legend-ecology {
  display: flex;
  align-items: right;
  gap: 10px;
  font-size: 1.1rem;
  font-weight: 700;
  color: $color-blue-dark;

  .legend-ecology-label { color: $color-ecology-dark; }
  .legend-diagnoses-label { color: $color-accent; }
  .legend-divider { color: $color-blue-dark; }
}

.chart-container {
  width: 100%;
  flex-grow: 1; // Заставляет график занимать оставшееся пространство высоты
  min-height: 0;
  
  .chart {
    height: 100%;
    width: 100%;
  }
}

// ---- для алкоголя

.doughnut-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 0px;

  .legend-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 1rem;
    font-weight: 500;
    color: $color-blue-dark;
  }

  .dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
  }
}

</style>