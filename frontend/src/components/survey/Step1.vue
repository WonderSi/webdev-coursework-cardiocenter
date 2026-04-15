<template>
  <div class="step-container">
    <div class="form-row" v-for="field in group.fields" :key="field.key">
      <span class="field-label">{{ field.label }}:</span>

      <!-- Radio -->
      <div v-if="field.type === 'radio'" class="radio-group-horizontal">
        <label v-for="opt in field.options" :key="opt.valueId" class="radio-label">
          <span>{{ opt.label }}</span>
          <input type="radio" :name="field.key" :value="opt.valueId" v-model="answers[field.key]" />
        </label>
      </div>

      <!-- Number -->
      <div v-else-if="field.type === 'number'" class="input-group">
        <input
          type="number"
          class="input-field"
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
}>()
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.step-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
  max-width: 400px;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.field-label {
  font-size: 0.95rem;
  font-weight: 700;
  color: $color-text;
  white-space: nowrap;
  min-width: 80px;
}

.input-group {
  display: flex;
  align-items: center;
  gap: 8px;

  .input-field {
    width: 70px;
    padding: 8px 12px;
    border: 1.5px solid $color-accent-lighter2;
    border-radius: 10px;
    font-size: 1rem;
    color: $color-text;
    outline: none;
    transition: border-color 0.2s;
    text-align: center;

    &:focus { border-color: $color-accent; }
  }

  .unit {
    color: $color-text;
    font-size: 0.9rem;
    white-space: nowrap;
  }
}

.radio-group-horizontal {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  color: $color-text;

  span {
    order: 1;
  }

  input[type="radio"] {
    order: 2;
    accent-color: $color-accent;
    width: 18px;
    height: 18px;
    cursor: pointer;
  }
}

@media (max-width: 600px) {
  .step-container {
    max-width: 100%;
    gap: 20px;
  }

  .form-row {
    gap: 12px;
  }

  .field-label {
    min-width: 70px;
    font-size: 0.95rem;
  }

  .radio-group-horizontal {
    gap: 16px;
  }

  .radio-label {
    font-size: 0.9rem;
  }
}

@media (max-width: 400px) {
  .form-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .field-label {
    min-width: auto;
  }
}
</style>
