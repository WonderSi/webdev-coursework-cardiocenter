<script setup lang="ts">
import { computed } from 'vue'
import { doctorAccessStates } from '@/config/doctor-access'
import type { DoctorAccessState } from '@/types/doctor'

const { level, size = 'default' } = defineProps<{
  level?: DoctorAccessState
  size?: 'default' | 'compact'
}>()
const label = computed(() => doctorAccessStates.find(item => item.value === level)?.label ?? 'Не указан')
const tagClasses = computed(() => [
  { 'access-level-tag--compact': size === 'compact' },
  level ? `access-level-tag--${level.toLowerCase()}` : undefined,
])
</script>

<template>
  <span class="access-level-tag" :class="tagClasses">
    {{ label }}
  </span>
</template>

<style scoped lang="scss">
.access-level-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  max-width: 100%;
  padding: 8px 12px 6px 12px;
  border-radius: 8px;
  background: #eef0f4;
  color: #565c66;
  font-size: 1rem;
  font-weight: 400;
  line-height: 1;
  white-space: nowrap;
}

.access-level-tag--compact {
  min-width: 0;
  padding: 5px 8px;
  font-size: inherit;
  white-space: normal;
  overflow-wrap: anywhere;
}

.access-level-tag--view {
  background: $color-view-tag-bg;
  color: $color-view-tag-text;
}

.access-level-tag--edit {
  background: $color-edit-tag-bg;
  color: $color-accent;
}

.access-level-tag--retrain {
  background: $color-lighter-red;
  color: $color-red;
}
</style>
