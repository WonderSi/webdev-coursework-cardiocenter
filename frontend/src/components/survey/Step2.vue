<template>
  <div class="step-container">
    <div class="form-row" v-for="field in group.fields" :key="field.key">
      <span class="field-label">{{ field.label }}:</span>

      <div class="input-group">
        <input
          type="number"
          class="input-field"
          :class="{ 'input-error': errors[field.key] }"
          :placeholder="field.placeholder || ''"
          v-model.number="answers[field.key]"
        />
        <span class="unit">{{ field.unit }}</span>
      </div>

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
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
  max-width: 250px;
  margin: 0 auto;
}

.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.field-label {
  font-size: 0.95rem;
  font-weight: 700;
  color: $color-text;
  white-space: nowrap;
  min-width: 100px;
}

.input-group {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  width: 100px;

  .input-field {
    width: 55px;
    padding: 6px 10px;
    border: 1.5px solid $color-accent-lighter2;
    border-radius: 10px;
    font-size: 0.95rem;
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
    font-size: 0.85rem;
    white-space: nowrap;
    width: 20px;
    text-align: left;
  }
}

@media (max-width: 600px) {
  .step-container {
    max-width: 100%;
    gap: 16px;
  }

  .field-label {
    min-width: 80px;
    font-size: 0.9rem;
  }

  .input-group .input-field {
    width: 55px;
  }
}

@media (max-width: 400px) {
  .form-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .field-label {
    min-width: auto;
  }
}
</style>
