<template>
  <div class="step-container">
    <div class="form-row" v-for="field in group.fields" :key="field.key">
      <label :for="field.key" class="field-label">{{ field.label }}:</label>

      <div class="input-group">
        <input
          type="number"
          :id="field.key"
          :name="field.key"
          :aria-invalid="!!errors[field.key] || undefined"
          :aria-describedby="errors[field.key] ? `${field.key}-error` : undefined"
          :min="field.validation?.min"
          step="any"
          class="input-field"
          :class="{ 'input-error': errors[field.key] }"
          :placeholder="field.placeholder || ''"
          v-model.number="answers[field.key]"
        />
        <span class="unit">{{ field.unit }}</span>
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
@use '@/styles/survey-scale' as survey;

.step-container {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: survey.size(24);
  width: 100%;
  max-width: survey.size(400);
  margin: 0 auto;
}

.form-row {
  gap: survey.size(12);
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.field-label {
  font-size: survey.text(0.95);
  font-weight: 700;
  color: $color-text;
  white-space: normal;
  overflow-wrap: anywhere;
  min-width: survey.size(100);
}

.input-group {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: survey.size(8);
  width: survey.size(122);

  .input-field {
    height: var(--survey-input-height, 24px);
    width: survey.size(55);
    padding: survey.size(6) survey.size(10);
    border: 1.5px solid $color-accent-lighter2;
    border-radius: survey.size(10);
    font-size: survey.text(0.95);
    color: $color-text;
    outline: none;
    transition: border-color 0.2s;
    text-align: center;

    &:focus { border-color: $color-accent; }

    &.input-error {
      border-color: $color-red;
      background-color: $color-red-bg;
    }
  }

  .unit {
    color: $color-text;
    font-size: survey.text(0.85);
    white-space: nowrap;
    min-width: max-content;
    text-align: left;
  }
}

/* Responsive */
@media (max-width: 600px) {
  .step-container {
    max-width: 100%;
    gap: survey.size(16);
  }

  .field-label {
    min-width: survey.size(80);
    font-size: survey.text(0.95);
  }

  .input-group .input-field {
    width: survey.size(55);
  }
}

@container survey-card (max-width: 440px) {
  .step-container {
    gap: 12px;
  }

  .form-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .field-label {
    min-width: auto;
    font-size: 0.95rem;
    line-height: 1.4;
  }

  .input-group {
    width: auto;
  }

  .input-group .input-field {
    min-height: 36px;
    width: 76px;
    font-size: 1rem;
  }

  .input-group .unit {
    font-size: 0.875rem;
  }
}
</style>
