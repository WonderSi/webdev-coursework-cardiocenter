<script setup lang="ts">
import AppSelect from '@/components/ui/AppSelect.vue'
import { doctorAccessStates } from '@/config/doctor-access'
import type { DoctorAccessState } from '@/types/doctor'

const value = defineModel<DoctorAccessState | 'all'>({ required: true })
const { disabled = false } = defineProps<{ disabled?: boolean }>()
const options = [
  { value: 'all' as const, label: 'Все уровни доступа' },
  ...doctorAccessStates,
]
</script>

<template>
  <AppSelect
    v-model="value"
    :options="options"
    :disabled="disabled"
    label="Фильтр по правам доступа"
  />
</template>
