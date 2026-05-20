<template>
  <sidebar/>
  
</template>

<script setup lang="ts">
import Sidebar from '@/components/Sidebar.vue'

import { ref, computed, onMounted } from 'vue';
import { fetchModelsStats } from '@/api/modeltraining.api';
import type { FullModelsResponse } from '@/api/modeltraining.api';
import type { RetrainStatusResponse } from '@/api/modeltraining.api';

import {
  CHART_COLORS,
  COMMON_TOOLTIP,
  TOOLTIP_ALT,
  AXIS_STYLE,
  COMMON_GRID
} from '@/utils/chartConstants';

const data = ref<FullModelsResponse | null>(null);

</script>



<style scoped lang="scss">
@use '@/styles/variables' as *;


</style>
