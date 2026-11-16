<template>
  <div class="step-container">
    <div v-for="field in group.fields" :key="field.key">
      <div
        class="radio-group-vertical"
        :class="{ 'radio-error': errors[field.key] }"
        role="radiogroup"
        :aria-label="field.label"
        :aria-invalid="!!errors[field.key] || undefined"
        :aria-describedby="errors[field.key] ? `${field.key}-error` : undefined"
      >
        <label v-for="opt in field.options" :key="opt.valueId" class="radio-label">
          <input type="radio" :name="field.key" :value="opt.valueId" v-model="answers[field.key]" />
          <span>{{ opt.label }}</span>
        </label>
      </div>
      <span v-if="errors[field.key]" :id="`${field.key}-error`" class="survey-field-error">{{ errors[field.key] }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SurveyGroup } from '@/stores/survey.store'

defineProps<{
  group: SurveyGroup
  answers: Record<string, any>
  errors: Record<string, string>
}>()
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.step-container {
  min-width: 0;

  > div {
    min-width: 0;
    max-width: 100%;
  }
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.radio-group-vertical {
  min-width: 0;
  max-width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.radio-label {
  min-width: 0;
  max-width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 1rem;
  color: $color-text;

  input[type="radio"] {
    accent-color: $color-accent;
    width: 20px;
    height: 20px;
    cursor: pointer;
    flex-shrink: 0;
  }

  span {
    min-width: 0;
    white-space: normal;
    overflow-wrap: anywhere;
  }
}

@media (max-width: 600px) {
  .radio-label {
    font-size: 0.95rem;
  }
}
@container survey-card (max-width: 440px) {
  .radio-group-vertical {
    gap: 4px;
  }

  .radio-label {
    min-height: 36px;
    font-size: 0.95rem;
    line-height: 1.4;
  }
}
</style>
