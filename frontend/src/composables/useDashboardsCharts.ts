import { ref, computed, watch, onScopeDispose } from 'vue'
import { fetchDashboardStats, type DashboardStatsResponse, type DashboardModelKind, type DashboardCategory } from '@/api/dashboard.api'
import { CHART_COLORS, COMMON_TOOLTIP, AXIS_STYLE } from '@/utils/chartConstants'

export function useDashboardCharts() {
  const stats = ref<DashboardStatsResponse | null>(null)
  const modelKind = ref<DashboardModelKind>('city')
  const loading = ref(false)
  const errorMessage = ref('')
  let pending: AbortController | null = null
  let sequence = 0

  async function load(): Promise<void> {
    const request = ++sequence
    pending?.abort()
    pending = new AbortController()
    loading.value = true
    errorMessage.value = ''
    stats.value = null
    try {
      const result = await fetchDashboardStats(modelKind.value, pending.signal)
      if (request === sequence) stats.value = result
    } catch (error) {
      if (request === sequence && !pending.signal.aborted) {
        errorMessage.value = error instanceof Error ? error.message : 'Не удалось загрузить аналитику.'
      }
    } finally {
      if (request === sequence) loading.value = false
    }
  }

  watch(modelKind, load, { immediate: true })
  onScopeDispose(() => { sequence++; pending?.abort() })

  const categoryColors: Record<string, string> = {
    'Мужской': CHART_COLORS.primaryBlue,
    'Женский': CHART_COLORS.lightRed,
    'Да': CHART_COLORS.lightRed,
    'Нет': CHART_COLORS.primaryBlue,
    'Нет данных': CHART_COLORS.gridLine,
    'Употребляю': CHART_COLORS.coldRed,
    'Употреблял ранее': CHART_COLORS.lightRed,
    'Никогда не употреблял': CHART_COLORS.primaryBlue,
    'Недостаточный': CHART_COLORS.blueMed,
    'Нормальный': CHART_COLORS.primaryBlue,
    'Избыточный': CHART_COLORS.lightRed,
    'Ожирение': CHART_COLORS.coldRed,
  }
  const pie = (data: DashboardCategory[]) => ({
    tooltip: { ...COMMON_TOOLTIP, trigger: 'item', formatter: '{b}: {c} записей ({d}%)' },
    legend: { bottom: 0, textStyle: AXIS_STYLE.labelRegular },
    color: [CHART_COLORS.primaryBlue, CHART_COLORS.lightRed, CHART_COLORS.blueMed, CHART_COLORS.coldRed],
    series: [{ type: 'pie', radius: ['35%', '65%'], center: ['50%', '43%'],
      label: { show: false }, data: data.filter(item => item.count > 0).map(item => ({
        name: item.name, value: item.count, itemStyle: { color: categoryColors[item.name] ?? CHART_COLORS.primaryBlue },
      })) }],
  })
  const bars = (data: DashboardCategory[], coloredCategories = false) => ({
    tooltip: { ...COMMON_TOOLTIP, valueFormatter: (value: number) => `${value} записей` },
    grid: { top: 5, left: 0, right: 40, bottom: 30, containLabel: true },
    xAxis: { type: 'value', minInterval: 1, axisLabel: AXIS_STYLE.labelRegular, splitLine: AXIS_STYLE.splitLine },
    yAxis: { type: 'category', inverse: true, data: data.map(item => item.name),
      axisLabel: { ...AXIS_STYLE.labelRegular, width: 240, overflow: 'truncate' }, axisTick: { show: false } },
    series: [{ type: 'bar', data: data.map(item => coloredCategories
      ? { value: item.count, itemStyle: { color: categoryColors[item.name] ?? CHART_COLORS.primaryBlue } }
      : item.count), barMaxWidth: 28,
      itemStyle: { color: CHART_COLORS.primaryBlue }, label: { show: true, position: 'right' } }],
  })

  const chartAlcohol = computed(() => pie(stats.value?.alcohol ?? []))
  const chartGender = computed(() => pie(stats.value?.genderDistribution ?? []))
  const chartLung = computed(() => pie(stats.value?.lungDisease ?? []))
  const chartWeight = computed(() => bars(
    (stats.value?.bmi ?? []).filter(item => item.name !== 'Нет данных' || item.count > 0), true,
  ))
  const chartProfessions = computed(() => bars(stats.value?.professions ?? []))
  const chartDistricts = computed(() => bars(stats.value?.districts ?? []))
  const chartAgeGender = computed(() => ({
    tooltip: COMMON_TOOLTIP,
    legend: { bottom: 0, textStyle: AXIS_STYLE.labelRegular },
    grid: { top: 10, left: 10, right: 10, bottom: 45, containLabel: true },
    xAxis: { type: 'category', data: stats.value?.ageGender.map(item => item.ageGroup) ?? [], axisLabel: AXIS_STYLE.labelRegular },
    yAxis: { type: 'value', minInterval: 1, axisLabel: AXIS_STYLE.labelRegular, splitLine: AXIS_STYLE.splitLine },
    series: [
      { name: 'Мужчины', type: 'bar', data: stats.value?.ageGender.map(item => item.maleCount) ?? [], itemStyle: { color: CHART_COLORS.primaryBlue } },
      { name: 'Женщины', type: 'bar', data: stats.value?.ageGender.map(item => item.femaleCount) ?? [], itemStyle: { color: CHART_COLORS.lightRed } },
    ],
  }))
  const chartLabs = computed(() => ({
    tooltip: COMMON_TOOLTIP,
    legend: { bottom: 0, textStyle: AXIS_STYLE.labelRegular },
    grid: { top: 10, left: 0, right: 10, bottom: 40, containLabel: true },
    xAxis: { type: 'value', minInterval: 1, axisLabel: AXIS_STYLE.labelRegular, splitLine: AXIS_STYLE.splitLine },
    yAxis: { type: 'category', inverse: true, data: stats.value?.labCoverage.map(item => item.label) ?? [], axisLabel: AXIS_STYLE.labelRegular },
    series: [
      { name: 'Заполнено', type: 'bar', stack: 'coverage', data: stats.value?.labCoverage.map(item => item.filledCount) ?? [], itemStyle: { color: CHART_COLORS.primaryBlue } },
      { name: 'Нет данных', type: 'bar', stack: 'coverage', data: stats.value?.labCoverage.map(item => item.missingCount) ?? [], itemStyle: { color: CHART_COLORS.lightRed } },
    ],
  }))

  return { stats, modelKind, loading, errorMessage, load, chartAlcohol, chartGender, chartLung,
    chartAgeGender, chartWeight, chartProfessions, chartDistricts, chartLabs }
}
