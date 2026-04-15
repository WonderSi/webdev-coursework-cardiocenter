<template>
  <div class="step-container">
    <div v-for="field in group.fields" :key="field.key">
      <div v-if="field.type === 'radio'" class="radio-group-vertical">
        <label v-for="opt in field.options" :key="opt.valueId" class="radio-label">
          <input type="radio" :name="field.key" :value="opt.valueId" v-model="answers[field.key]" />
          <span>{{ opt.label }}</span>
        </label>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SurveyGroup } from '@/stores/survey.store'

defineProps<{
  group: SurveyGroup
  answers: Record<string, any>
}>()
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.step-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.radio-group-vertical {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 0.9rem;
  color: $color-text;

  input[type="radio"] {
    accent-color: $color-accent;
    width: 18px;
    height: 18px;
    cursor: pointer;
    flex-shrink: 0;
  }

  span {
    flex: 1;
  }
}
</style>
