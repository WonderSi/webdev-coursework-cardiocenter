import { computed } from 'vue';
import { 
  mockAlcoholConsumptionData, 
  mockAgeGenderData, 
  mockWeightData, 
  mockProfessionData, 
  mockMostDiagnosedData, 
  mockDistrictData 
} from '@/mocks/dashboardsData';
import { CHART_COLORS, COMMON_TOOLTIP, AXIS_STYLE } from '@/utils/chartConstants';

export function useDashboardCharts() {
  
  // Употребление алкоголя
  const chartAlcohol = computed(() => ({
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
      data: mockAlcoholConsumptionData.map(item => {
        let sliceColor = CHART_COLORS.primaryBlue;
        if (item.category === 'Употребляю') sliceColor = CHART_COLORS.coldRed;
        if (item.category === 'Употреблял ранее') sliceColor = CHART_COLORS.lightRed;
        return { value: item.healthyCount, name: item.category, diagnosedPercent: item.diagnosedPercent, itemStyle: { color: sliceColor } };
      })
    }]
  }));

  // Частые диагнозы (Horizontal Bar)
  const chartDiagnoses = computed(() => ({
    renderer: 'svg',
    tooltip: {
      ...COMMON_TOOLTIP,
      trigger: 'axis',
      axisPointer: { type: 'none' },
      formatter: (params: any) => `
        <div style="margin-bottom: 8px; font-size: 15px;"><strong>${mockMostDiagnosedData[params[0].dataIndex].diagName}</strong></div>
        <div style="display: flex; justify-content: space-between; gap: 20px; margin-bottom: 4px;">
          <span>Случаев:</span><span style="font-weight: 700;">${mockMostDiagnosedData[params[0].dataIndex].diagnosedCount}</span>
        </div>
        <div style="display: flex; justify-content: space-between; gap: 20px;">
          <span>Доля от всех ССЗ:</span><span style="font-weight: 700; color: ${CHART_COLORS.primaryBlue};">${mockMostDiagnosedData[params[0].dataIndex].percent}%</span>
        </div>
      `
    },
    grid: { top: '5%', left: '2%', right: '12%', bottom: '5%', containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: CHART_COLORS.gridLine } } },
    yAxis: { 
      type: 'category', 
      inverse: true, 
      data: mockMostDiagnosedData.map(item => item.diagName),
      axisLabel: AXIS_STYLE.axisLabel,
      axisLine: AXIS_STYLE.lineStyle,
      axisTick: { show: false }
    },
    series: [{
      type: 'bar',
      data: mockMostDiagnosedData.map(item => item.diagnosedCount),
      itemStyle: { color: CHART_COLORS.primaryBlue, borderRadius: [0, 2, 2, 0] },
      barWidth: 20,
      label: { show: true, position: 'right', color: CHART_COLORS.darkBlue, fontWeight: 700 }
    }]
  }));

  // Возрастно-половая структура
  const chartAgeGender = computed(() => ({
    renderer: 'svg',
    tooltip: { ...COMMON_TOOLTIP, trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: '12%', left: '5%', right: '5%', bottom: '15%', containLabel: true },
    xAxis: {
      type: 'category', z: 10,
      name: 'Возрастная группа', nameLocation: 'center', nameGap: 40, nameTextStyle: AXIS_STYLE.nameTextStyle,
      axisLabel: AXIS_STYLE.axisLabel, axisLine: AXIS_STYLE.lineStyle,
      data: mockAgeGenderData.map(item => item.ageGroup), axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: 'Численность', nameLocation: 'middle', nameGap: 50, nameRotate: 90, nameTextStyle: AXIS_STYLE.nameTextStyle,
      axisLabel: { ...AXIS_STYLE.axisLabel, inside: true },
      splitLine: { lineStyle: { color: CHART_COLORS.gridLine } },
      axisLine: AXIS_STYLE.lineStyle,
    },
    series: [
      { name: 'Здоровые (М)', type: 'bar', stack: 'male', barWidth: 25, data: mockAgeGenderData.map(item => item.healthyMale), itemStyle: { color: CHART_COLORS.lightBlue } },
      { name: 'Диагноз (М)', type: 'bar', stack: 'male', data: mockAgeGenderData.map(item => item.diagnosedMale), itemStyle: { color: CHART_COLORS.primaryBlue, borderRadius: [2, 2, 0, 0] } },
      { name: 'Здоровые (Ж)', type: 'bar', stack: 'female', barWidth: 25, data: mockAgeGenderData.map(item => item.healthyFemale), itemStyle: { color: CHART_COLORS.lightRed } },
      { name: 'Диагноз (Ж)', type: 'bar', stack: 'female', data: mockAgeGenderData.map(item => item.diagnosedFemale), itemStyle: { color: CHART_COLORS.red, borderRadius: [2, 2, 0, 0] } }
    ]
  }));

  // Вес и ССЗ
  const chartWeight = computed(() => ({
    renderer: 'svg',
    tooltip: { ...COMMON_TOOLTIP, trigger: 'axis', axisPointer: { type: 'none' } },
    grid: { top: '5%', left: '2%', right: '8%', bottom: '12%', containLabel: true },
    xAxis: {
      type: 'value',
      name: 'Пациенты', nameLocation: 'center', nameGap: 30, nameTextStyle: AXIS_STYLE.nameTextStyle,
      axisLabel: { ...AXIS_STYLE.axisLabel, fontWeight: 400 },
      splitLine: { lineStyle: { color: CHART_COLORS.gridLine } }
    },
    yAxis: {
      type: 'category', z: 10,
      data: mockWeightData.map(item => item.category),
      axisLabel: AXIS_STYLE.axisLabel, axisLine: AXIS_STYLE.lineStyle
    },
    series: [
      { name: 'Здоровые', type: 'bar', stack: 'total', barWidth: 35, data: mockWeightData.map(item => item.healthyCount), itemStyle: { color: CHART_COLORS.lightBlue } },
      { 
        name: 'С диагнозом', type: 'bar', stack: 'total', data: mockWeightData.map(item => item.diagnosedCount), itemStyle: { color: CHART_COLORS.coldRed },
        label: {
          show: true, position: 'right', distance: 10,
          formatter: (params: any) => `${mockWeightData[params.dataIndex].diagnosedPercent}%`,
          color: CHART_COLORS.darkBlue, fontWeight: 800, fontSize: 14,
        }
      }
    ]
  }));

  // Профессии
  const chartProfessions = computed(() => ({
    renderer: 'svg',
    tooltip: {
      ...COMMON_TOOLTIP, trigger: 'axis', axisPointer: { type: 'none' },
      formatter: (params: any) => `
        <div style="margin-bottom: 8px; font-size: 15px;"><strong>${mockProfessionData[params[0].dataIndex].profName}</strong></div>
        <div style="display: flex; justify-content: space-between; gap: 20px; margin-bottom: 4px;">
          <span>Диагностировано:</span><span style="font-weight: 700;">${mockProfessionData[params[0].dataIndex].diagnosedCount} чел.</span>
        </div>
        <div style="display: flex; justify-content: space-between; gap: 20px;">
          <span>Доля риска:</span><span style="font-weight: 700; color: ${CHART_COLORS.red};">${mockProfessionData[params[0].dataIndex].diagnosedPercent}%</span>
        </div>
      `
    },
    grid: { top: '5%', left: '2%', right: '8%', bottom: '5%', containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: CHART_COLORS.gridLine } } },
    yAxis: { type: 'category', inverse: true, data: mockProfessionData.map(item => item.profName), axisLabel: AXIS_STYLE.axisLabel, axisLine: AXIS_STYLE.lineStyle, axisTick: { show: false } },
    series: [{
      name: 'Риск', type: 'bar', data: mockProfessionData.map(item => item.diagnosedPercent),
      itemStyle: { color: CHART_COLORS.red, borderRadius: [0, 2, 2, 0] }, barWidth: 20,
      label: { show: true, position: 'right', formatter: '{c}%', color: CHART_COLORS.darkBlue, fontWeight: 700 }
    }]
  }));

  // Районы и Экология
  const chartDistricts = computed(() => ({
    renderer: 'svg',
    tooltip: { ...COMMON_TOOLTIP, trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: '5%', left: '2%', right: '5%', bottom: '5%', containLabel: true },
    xAxis: [
      { type: 'value', splitLine: { lineStyle: { color: CHART_COLORS.gridLine } } },
      { type: 'value', splitLine: { show: false } }
    ],
    yAxis: { type: 'category', z: 10, inverse: true, data: mockDistrictData.map(item => item.name), axisLabel: AXIS_STYLE.axisLabel, axisLine: AXIS_STYLE.lineStyle, axisTick: { show: false } },
    series: [
      { name: 'Диагнозы', type: 'bar', xAxisIndex: 0, data: mockDistrictData.map(item => item.numberOfDiagnoses), itemStyle: { color: CHART_COLORS.primaryBlue, borderRadius: [0, 2, 2, 0] }, barGap: '10%' },
      { name: 'Экология', type: 'bar', xAxisIndex: 1, data: mockDistrictData.map(item => item.EcologyData), itemStyle: { color: CHART_COLORS.ecology, borderRadius: [0, 2, 2, 0] } }
    ]
  }));

  return { chartAlcohol, chartDiagnoses, chartAgeGender, chartWeight, chartProfessions, chartDistricts };
}