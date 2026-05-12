export const CHART_COLORS = {
  primaryBlue: '#1567E2',
  darkBlue: '#0f356e',
  lightBlue: '#76a5f0',
  red: '#DF2242',
  lightRed: '#ff8095',
  coldRed: '#EA566F',
  ecology: '#20B2AA',
  gridLine: '#8AB3F1'
};

export const COMMON_TOOLTIP = {
  trigger: 'axis',
  axisPointer: { type: 'none' },
  borderRadius: 12,
  padding: 12,
  backgroundColor: 'rgba(255, 255, 255, 0.9)',
  borderColor: 'rgba(13, 60, 129, 0.2)',
  borderWidth: 1,
  textStyle: { 
    color: CHART_COLORS.darkBlue, 
    fontSize: 14,
    fontFamily: 'Inter, sans-serif'
  }
};

export const AXIS_STYLE = {
  nameTextStyle: { 
    color: CHART_COLORS.darkBlue, 
    fontWeight: 400, 
    fontSize: 16 
  },
  axisLabel: { 
    color: CHART_COLORS.darkBlue, 
    fontWeight: 400, 
    fontSize: 14 
  },
  lineStyle: { 
    color: CHART_COLORS.darkBlue, 
    width: 2
  }
};
