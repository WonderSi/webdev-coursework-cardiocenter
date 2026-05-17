import { ref, computed, onMounted } from 'vue';
import type { DashboardStatsResponse } from '@/api/dashboard.api';
import { fetchDashboardStats } from '@/api/dashboard.api';
import {
  CHART_COLORS,
  COMMON_TOOLTIP,
  TOOLTIP_ALT,
  AXIS_STYLE,
  COMMON_GRID
} from '@/utils/chartConstants';

export function useDashboardCharts() {
  const stats = ref<DashboardStatsResponse | null>(null);

  onMounted(async () => {
    try {
      stats.value = await fetchDashboardStats();
    } catch (e) {
      console.error('Dashboard load error:', e);
    }
  });

  // Употребление алкоголя
  const chartAlcohol = computed(() => {
    const data = stats.value?.alcohol ?? [];
    return {
      renderer: 'svg',
      tooltip: {
        ...COMMON_TOOLTIP,
        trigger: 'item',
        formatter: (params: any) => `
          <div style="margin-bottom: 8px; font-size: 15px;"><strong>${params.name}</strong></div>
          <div style="display: flex; justify-content: space-between; gap: 20px; margin-bottom: 4px;">
            <span>${params.marker} Всего:</span>
            <span style="font-weight: 700;">${params.value} чел.</span>
          </div>
          <div style="display: flex; justify-content: space-between; gap: 20px;">
            <span style="margin-left: 14px; opacity: 0.8;">Риск ССЗ:</span>
            <span style="font-weight: 700; color: ${CHART_COLORS.red};">${params.data.diagnosedPercent}%</span>
          </div>
        `
      },
      series: [{
        type: 'pie',
        radius: ['30%', '80%'],
        center: ['50%', '50%'],
        itemStyle: { borderRadius: 0, borderColor: 'transparent', borderWidth: 4 },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: 16, fontWeight: 'bold', color: CHART_COLORS.darkBlue, formatter: '{b}' }
        },
        data: data.map(item => {
          let sliceColor = CHART_COLORS.primaryBlue;
          if (item.category === 'Употребляю') sliceColor = CHART_COLORS.coldRed;
          if (item.category === 'Употреблял ранее') sliceColor = CHART_COLORS.lightRed;
          return {
            value: item.healthyCount + item.diagnosedCount,
            name: item.category,
            diagnosedPercent: item.diagnosedPercent,
            itemStyle: { color: sliceColor }
          };
        })
      }]
    };
  });

  // Частые диагнозы (Horizontal Bar)
  const chartDiagnoses = computed(() => {
    const data = stats.value?.topDiagnoses ?? [];
    return {
      renderer: 'svg',
      tooltip: {
        ...COMMON_TOOLTIP,
        formatter: (params: any) => {
          const item = data[params[0].dataIndex];
          if (!item) return '';
          return `
            <div style="margin-bottom: 8px; font-size: 15px;"><strong>${item.diagName}</strong></div>
            <div style="display: flex; justify-content: space-between; gap: 20px; margin-bottom: 4px;">
              <span>Диагностировано:</span>
              <span style="font-weight: 700;">${item.diagnosedCount}</span>
            </div>
            <div style="display: flex; justify-content: space-between; gap: 20px;">
              <span>Доля от всех ССЗ:</span>
              <span style="font-weight: 700; color: ${CHART_COLORS.primaryBlue};">${item.percent}%</span>
            </div>
          `;
        }
      },
      grid: COMMON_GRID,
      xAxis: {
        type: 'value',
        name: 'число пациентов',
        nameLocation: 'center',
        nameGap: 40,
        nameTextStyle: AXIS_STYLE.nameTextStyle,
        splitLine: AXIS_STYLE.splitLine,
        axisLabel: AXIS_STYLE.labelRegular,
      },
      yAxis: {
        type: 'category',
        z: 10,
        inverse: true,
        data: data.map(item => item.diagName),
        axisLabel: AXIS_STYLE.labelBold,
        axisLine: { lineStyle: AXIS_STYLE.lineStyle },
        axisTick: { show: false }
      },
      series: [{
        name: 'Случаи',
        type: 'bar',
        data: data.map(item => item.diagnosedCount),
        itemStyle: { color: CHART_COLORS.primaryBlue, borderRadius: [0, 2, 2, 0] },
        barWidth: 25,
        label: {
          show: true,
          position: 'right',
          formatter: '{c}',
          color: CHART_COLORS.darkBlue,
          fontWeight: 600,
          fontSize: 16
        }
      }]
    };
  });

  // Возрастно-половая структура
  const chartAgeGender = computed(() => {
    const data = stats.value?.ageGender ?? [];
    return {
      renderer: 'svg',
      tooltip: {
        ...TOOLTIP_ALT,
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
        nameTextStyle: AXIS_STYLE.nameTextStyle,
        axisLabel: AXIS_STYLE.labelRegular,
        axisLine: { show: true, lineStyle: AXIS_STYLE.lineStyle },
        data: data.map(item => item.ageGroup),
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        name: 'число пациентов',
        nameLocation: 'middle',
        nameGap: 40,
        nameRotate: -90,
        nameTextStyle: AXIS_STYLE.nameTextStyle,
        axisLabel: { ...AXIS_STYLE.labelRegular, verticalAlign: 'bottom', padding: [0, 0, 0, 0] },
        splitLine: AXIS_STYLE.splitLine,
        axisLine: { show: true, lineStyle: AXIS_STYLE.lineStyle },
      },
      series: [
        {
          name: 'Здоровые (М)',
          type: 'bar',
          stack: 'male',
          barWidth: 30,
          data: data.map(item => item.healthyMale),
          itemStyle: { color: CHART_COLORS.blueMed }
        },
        {
          name: 'Диагностированные (М)',
          type: 'bar',
          stack: 'male',
          data: data.map(item => item.diagnosedMale),
          itemStyle: { color: CHART_COLORS.primaryBlue, borderRadius: [2, 2, 0, 0] }
        },
        {
          name: 'Здоровые (Ж)',
          type: 'bar',
          stack: 'female',
          barWidth: 30,
          data: data.map(item => item.healthyFemale),
          itemStyle: { color: CHART_COLORS.lightRed }
        },
        {
          name: 'Диагностированные (Ж)',
          type: 'bar',
          stack: 'female',
          data: data.map(item => item.diagnosedFemale),
          itemStyle: { color: CHART_COLORS.red, borderRadius: [2, 2, 0, 0] }
        }
      ]
    };
  });

  // Вес и ССЗ
  const chartWeight = computed(() => {
    const data = stats.value?.weightBmi ?? [];
    return {
      renderer: 'svg',
      tooltip: {
        ...TOOLTIP_ALT,
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
      grid: COMMON_GRID,
      xAxis: [
        {
          type: 'value',
          name: 'число пациентов',
          nameLocation: 'center',
          nameGap: 40,
          nameTextStyle: AXIS_STYLE.nameTextStyle,
          axisLabel: AXIS_STYLE.labelRegular,
          splitLine: AXIS_STYLE.splitLine
        }
      ],
      yAxis: {
        type: 'category',
        z: 10,
        data: data.map(item => item.category),
        axisLabel: AXIS_STYLE.labelBold,
        axisLine: { lineStyle: AXIS_STYLE.lineStyle }
      },
      series: [
        {
          name: 'Здоровые',
          type: 'bar',
          stack: 'total',
          barWidth: 30,
          data: data.map(item => item.healthyCount),
          itemStyle: { color: CHART_COLORS.blueMed }
        },
        {
          name: 'С диагнозом',
          type: 'bar',
          stack: 'total',
          data: data.map(item => item.diagnosedCount),
          itemStyle: { color: CHART_COLORS.coldRed },
          label: {
            show: true,
            position: 'right',
            distance: 4,
            formatter: (params: any) => {
              const item = data[params.dataIndex];
              return item ? item.diagnosedPercent + '%' : '';
            },
            color: CHART_COLORS.darkBlue,
            fontWeight: 600,
            fontSize: 16
          }
        }
      ]
    };
  });

  // Профессии
  const chartProfessions = computed(() => {
    const data = stats.value?.topProfessions ?? [];
    return {
      renderer: 'svg',
      tooltip: {
        ...COMMON_TOOLTIP,
        formatter: (params: any) => {
          const item = data[params[0].dataIndex];
          if (!item) return '';
          return `
            <div style="margin-bottom: 8px; font-size: 15px;"><strong>${item.profName}</strong></div>
            <div style="display: flex; justify-content: space-between; gap: 20px; margin-bottom: 4px;">
              <span>Диагностировано:</span>
              <span style="font-weight: 700;">${item.diagnosedCount} чел.</span>
            </div>
            <div style="display: flex; justify-content: space-between; gap: 20px;">
              <span>% имеющих ССЗ от общего числа:</span>
              <span style="font-weight: 700; color: ${CHART_COLORS.red};">${item.diagnosedPercent}%</span>
            </div>
          `;
        }
      },
      grid: COMMON_GRID,
      xAxis: {
        type: 'value',
        name: 'число пациентов',
        nameLocation: 'center',
        nameGap: 40,
        nameTextStyle: AXIS_STYLE.nameTextStyle,
        splitLine: AXIS_STYLE.splitLine,
        axisLabel: AXIS_STYLE.labelRegular,
      },
      yAxis: {
        type: 'category',
        inverse: true,
        z: 10,
        data: data.map(item => item.profName),
        axisLabel: AXIS_STYLE.labelBold,
        axisLine: { lineStyle: AXIS_STYLE.lineStyle },
        axisTick: { show: false }
      },
      series: [{
        name: 'случаев ССЗ',
        type: 'bar',
        data: data.map(item => item.diagnosedCount),
        itemStyle: { color: CHART_COLORS.red, borderRadius: [0, 2, 2, 0] },
        barWidth: 30,
        label: {
          show: true,
          position: 'right',
          formatter: '{c}',
          color: CHART_COLORS.darkBlue,
          fontWeight: 600,
          fontSize: 16
        }
      }]
    };
  });

  // Районы
  const chartDistricts = computed(() => {
    const data = stats.value?.districts ?? [];
    return {
      renderer: 'svg',
      tooltip: { ...COMMON_TOOLTIP },
      grid: { top: '0%', left: '0%', right: '0%', bottom: '0%', containLabel: true },
      xAxis: { type: 'value', splitLine: AXIS_STYLE.splitLine, axisLabel: AXIS_STYLE.labelRegular },
      yAxis: {
        type: 'category',
        z: 10,
        inverse: true,
        data: data.map(item => item.name),
        axisLabel: AXIS_STYLE.labelBold,
        axisLine: { lineStyle: AXIS_STYLE.lineStyle },
        axisTick: { show: false }
      },
      series: [{
        name: 'Диагнозы',
        type: 'bar',
        data: data.map(item => item.numberOfDiagnoses),
        itemStyle: { color: CHART_COLORS.primaryBlue, borderRadius: [0, 2, 2, 0] }
      }]
    };
  });

  return { stats, chartAlcohol, chartDiagnoses, chartAgeGender, chartWeight, chartProfessions, chartDistricts };
}
