export const CHART_COLORS = {
  primaryBlue: '#1567E2',  // $color-accent:
  darkBlue: '#0f356e',     // $color-blue-dark
  blueMed: '#5e9bf7',      // $color-blue-med
  red: '#DF2242',          // $$color-red
  lightRed: '#ff8095',     // $color-light-red
  coldRed: '#EA566F',      // $color-cold-red
  ecology: '#20B2AA',      // $color-ecology
  gridLine: '#8AB3F1',     // $color-accent-lighter
  white: '#FFFFFF'         // $color-white
};

const TOOLTIP_BASE = {
  borderRadius: 12,
  padding: 12,
  backgroundColor: 'rgba(255, 255, 255, 0.9)',
  borderWidth: 1,
};

export const COMMON_TOOLTIP = {
  trigger: 'axis',
  axisPointer: { type: 'none' },
  ...TOOLTIP_BASE,
  borderColor: 'rgba(13, 60, 129, 0.2)',
  textStyle: { 
    color: CHART_COLORS.darkBlue, 
    fontSize: 14,
    fontFamily: 'Inter, sans-serif'
  }
};

export const TOOLTIP_ALT = {
  trigger: 'axis',
  axisPointer: { type: 'none' },
  ...TOOLTIP_BASE,
  borderColor: 'rgba(32, 92, 182, 0.35)',
  textStyle: {
    color: CHART_COLORS.darkBlue,
    fontSize: 16,
    fontFamily: 'Inter, sans-serif'
  }
};

export const AXIS_STYLE = {
  nameTextStyle: { 
    color: CHART_COLORS.darkBlue, 
    fontWeight: 400, 
    fontSize: 16 
  },
  labelRegular: { 
    color: CHART_COLORS.darkBlue, 
    fontWeight: 400, 
    fontSize: 14 
  },
  labelBold: { 
    color: CHART_COLORS.darkBlue, 
    fontWeight: 600, 
    fontSize: 16 
  },
  lineStyle: { 
    color: CHART_COLORS.darkBlue, 
    width: 2
  },
  splitLine: { 
    lineStyle: { color: CHART_COLORS.gridLine, show: true } 
  }
};

export const COMMON_GRID = { 
  top: '0%', left: '0%', right: '7%', bottom: '15%', containLabel: true 
};
