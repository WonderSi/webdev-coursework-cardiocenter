<template>
  <sidebar/>
  <div class="dashboards-grid-container">

    <!-- ================= СТРОКА 1 (Маленькие карточки, Высота 1) ================= -->

    <!-- Карточка 1: Всего записей -->
    <div class="dashboard-card col-2 row-1">
      <div class="card-header">
        <div class="card-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18.3333 26.6667V35H8.33333C7.49237 35.0003 6.68239 34.6827 6.06575 34.1108C5.44912 33.539 5.07141 32.7553 5.00833 31.9167L5 31.6667V26.6667H18.3333ZM35 26.6667V31.6667C35.0003 32.5076 34.6827 33.3176 34.1108 33.9342C33.539 34.5509 32.7553 34.9286 31.9167 34.9917L31.6667 35H21.6667V26.6667H35ZM18.3333 16.6667V23.3333H5V16.6667H18.3333ZM35 16.6667V23.3333H21.6667V16.6667H35ZM18.3333 5V13.3333H5V8.33333C4.99973 7.49237 5.31734 6.68239 5.88916 6.06575C6.46098 5.44912 7.24474 5.07141 8.08333 5.00833L8.33333 5H18.3333ZM31.6667 5C32.5507 5 33.3986 5.35119 34.0237 5.97631C34.6488 6.60143 35 7.44928 35 8.33333V13.3333H21.6667V5H31.6667Z" fill="#0D3C81"/>
          </svg>
        </div>
        <div class="card-title">Всего записей о пациентах</div>
      </div>
      <span class="card-number-total-records">{{ mockKpiData.totalRecords }}</span>
    </div>

    <!-- Карточка 2: Соотношение полов
    <div class="dashboard-card col-2 row-1">
      <div class="card-header">
        <div class="card-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12.5001 3.33337C13.3841 3.33337 14.232 3.68456 14.8571 4.30968C15.4822 4.93481 15.8334 5.78265 15.8334 6.66671C15.8334 7.55076 15.4822 8.39861 14.8571 9.02373C14.232 9.6488 13.3841 10 12.5001 10C11.616 10 10.7682 9.64885 10.1431 9.02373C9.51794 8.39861 9.16675 7.55076 9.16675 6.66671C9.16675 5.78265 9.51794 4.93481 10.1431 4.30968C10.7682 3.68456 11.616 3.33337 12.5001 3.33337ZM10.0001 11.6667H15.0001C15.8841 11.6667 16.732 12.0179 17.3571 12.643C17.9822 13.2681 18.3334 14.116 18.3334 15V24.1667H15.8334V36.6667H9.16675V24.1667H6.66675V15C6.66675 14.116 7.01794 13.2681 7.64306 12.643C8.26818 12.0179 9.11603 11.6667 10.0001 11.6667ZM27.5001 3.33337C28.3841 3.33337 29.232 3.68456 29.8571 4.30968C30.4822 4.93481 30.8334 5.78265 30.8334 6.66671C30.8334 7.55076 30.4822 8.39861 29.8571 9.02373C29.232 9.64885 28.3841 10 27.5001 10C26.616 10 25.7682 9.64885 25.1431 9.02373C24.5179 8.39861 24.1667 7.55076 24.1667 6.66671C24.1667 5.78265 24.5179 4.93481 25.1431 4.30968C25.7682 3.68456 26.616 3.33337 27.5001 3.33337ZM25.0001 11.6667H30.0001C30.8841 11.6667 31.732 12.0179 32.3571 12.643C32.9822 13.2681 33.3334 14.116 33.3334 15V24.1667H30.8334V36.6667H24.1667V24.1667H21.6667V15C21.6667 14.116 22.0179 13.2681 22.6431 12.643C23.2682 12.0179 24.116 11.6667 25.0001 11.6667Z" fill="#0D3C81"/>
          </svg>
        </div>
        <div class="card-title">Соотношение полов</div>
      </div>
      <div class="card-two-numbers">
        <div class="percentage">
          <span class="card-number color-blue">{{ mockKpiData.malePercentage }}%</span>
          <span class="gender-label color-blue">мужчины</span>
        </div>
        <div class="percentage">
          <span class="card-number color-pink">{{ mockKpiData.femalePercentage }}%</span>
          <span class="gender-label color-pink">женщины</span>
        </div>
      </div>
    </div> -->
    <!-- ПО СУТИ БЕСПОЛЕЗНАЯ ДЛЯ НАС КАРТОЧКА, ПОЭТОМУ УБИРАЕМ -->

    <!-- Карточка 2: % диагностированных м-ж -->
    <div class="dashboard-card col-2 row-1">
      <div class="card-header">
        <div class="card-icon">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#0D3C81"/>
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

    <!-- Карточка 3: Распределение пол-возраст -->
    <div class="dashboard-card col-4 row-2">
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

    <!-- Карточка 4: Вес - диагнозы -->
    <div class="dashboard-card col-4 row-2">
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

  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { use } from 'echarts/core'
import { SVGRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import Sidebar from '@/components/Sidebar.vue'
import { mockKpiData, mockAgeGenderData, mockGenderDiagnosesData, mockWeightData } from '@/mocks/dashboardsData'

// ECharts модули
use([SVGRenderer, BarChart, GridComponent, TooltipComponent]);

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
        color: '#0D3C81', // $color-blue-dark
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
    grid: {
      top: '0%',
      left: '5%',
      right: '0%',
      bottom: '12%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      z: 10,
      name: 'возрастная группа',
      nameLocation: 'center',
      nameGap: 35,
      nameTextStyle: {
        color: '#0D3C81', // $color-blue-dark;
        fontWeight: 600,
        fontSize: 16
      },
      axisLabel: { 
        color: '#0D3C81',
        fontWeight: 400,
        fontSize: 14
      },
      axisLine: {
        show: true,
        lineStyle: { color: '#0D3C81', width: 2 }
      },
      data: mockAgeGenderData.map(item => item.ageGroup),
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: 'число пациентов',
      nameLocation: 'middle',
      nameGap: 35,
      nameRotate: -90,
      nameTextStyle: {
        color: '#0D3C81', // $color-blue-dark;
        fontWeight: 600,
        fontSize: 16
      },
      axisLabel: { 
        color: '#0D3C81',
        fontWeight: 400,
        fontSize: 14,
        verticalAlign: 'bottom',
        padding: [0, 0, 0, 0]
      },                       // $color-accent-lighter;
      splitLine: { lineStyle: { color: '#8AB3F1' } },
      axisLine: {
        show: true,
        lineStyle: { color: '#0D3C81', width: 2 }
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
      borderRadius: 12,
      padding: 12,
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(32, 92, 182, 0.35)',
      borderWidth: 1,
      textStyle: {
        color: '#0D3C81', // $color-blue-dark
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
    grid: {
      top: '0%',
      left: '0%',
      right: '0%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'value',
        name: 'число пацентов',
        nameLocation: 'center',
        nameGap: 30,
        nameTextStyle: { color: '#0D3C81', fontWeight: 400, fontSize: 16},
        axisLabel: { color: '#0D3C81', fontWeight: 400, fontSize: 14 },
        splitLine: { lineStyle: { color: '#8AB3F1' } } // $color-accent-lighter
      }
    ],
    yAxis: {
      type: 'category',
      z: 10,
      data: mockWeightData.map(item => item.category),
      axisLabel: { color: '#0D3C81', fontWeight: 600, fontSize: 16 },
      axisLine: { lineStyle: { color: '#0D3C81', width: 2 } }
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
          position: 'left',
          distance: 10,
          formatter: (params: any) => {
            const percent = mockWeightData[params.dataIndex].diagnosedPercent;
            return percent + '%'
          },
          color: '#0D3C81', // $color-blue-dark
          fontWeight: 600,
          fontSize: 14
        }
      }
    ]
  };
});
  
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

// Утилиты
.col-2 { grid-column: span 2; }
.col-3 { grid-column: span 3; }
.col-4 { grid-column: span 4; }
.col-6 { grid-column: span 6; }

.row-1 { grid-row: span 1; }
.row-2 { grid-row: span 2; }

// большие экраны
@media (max-width: 1400px) {
  .col-2 { grid-column: span 4; } // Расширяем маленькие карточки
  .col-4 { grid-column: span 8; } // Графики шире
}

// Маленькие экраны
@media (max-width: 1024px) {
  .col-2, .col-3, .col-4, .col-6 {
    grid-column: span 12; // Всё выстраивается в одну колонку
  }
  .row-2 {
    grid-row: span 1;
    min-height: 350px; // мин высота для графиков
  }
}

// -----------------------------------------------------------------

.color-blue { color: $color-accent !important; }
.color-pink { color: $color-pink-women !important; }
.color-green { color: $color-green !important; }

.dashboards-grid-container {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  grid-auto-rows: minmax(160px, auto);
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
}

.percentage {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;

  .card-number {
    font-size: 3rem;
    font-weight: 700;
    color: $color-text;
    line-height: 1;
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

  .legend-men { color: $color-accent; } // Blue
  .legend-women { color: $color-red; } // Red
  .legend-divider { color: $color-text; }
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

</style>