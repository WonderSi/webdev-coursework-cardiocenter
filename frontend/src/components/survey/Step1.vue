<template>
  <div class="step-container">
    <div class="form-row" v-for="field in group.fields" :key="field.key">
      <span :id="`${field.key}-label`" class="field-label">{{ field.label }}:</span>

      <AppSelect
        v-if="field.type === 'select'"
        class="select-field"
        :labelledby="`${field.key}-label`"
        :describedby="errors[field.key] ? `${field.key}-error` : undefined"
        :invalid="!!errors[field.key]"
        :options="(field.options ?? []).map(option => ({ value: option.valueId, label: option.label }))"
        v-model="answers[field.key]"
      />

      <!-- Radio -->
      <div
        v-else-if="field.type === 'radio'"
        class="radio-group-horizontal"
        :class="{ 'radio-error': errors[field.key] }"
        role="radiogroup"
        :aria-labelledby="`${field.key}-label`"
        :aria-invalid="!!errors[field.key] || undefined"
        :aria-describedby="errors[field.key] ? `${field.key}-error` : undefined"
      >
        <label v-for="opt in field.options" :key="opt.valueId" class="radio-label">
          <span>{{ opt.label }}</span>
          <input type="radio" :name="field.key" :value="opt.valueId" v-model="answers[field.key]" />
        </label>
      </div>

      <!-- Number -->
      <div v-else-if="field.type === 'number'" class="input-group">
        <input
          type="number"
          :name="field.key"
          :aria-labelledby="`${field.key}-label`"
          :aria-invalid="!!errors[field.key] || undefined"
          :aria-describedby="errors[field.key] ? `${field.key}-error` : undefined"
          :min="field.validation?.min"
          :max="field.validation?.max"
          :step="field.validation?.integer ? 1 : 'any'"
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
import AppSelect from '@/components/ui/AppSelect.vue'
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

.select-field {
  width: 100%;
  min-width: 0;
}

.field-label {
  overflow-wrap: anywhere;
  font-size: 0.95rem;
  font-weight: 700;
  color: $color-text;
  white-space: normal;
  min-width: 80px;
}

.input-group {
  flex-shrink: 0;
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

    &.input-error {
      border-color: $color-red;
      background-color: $color-red-bg;
    }
  }

  .unit {
    color: $color-text;
    font-size: 0.9rem;
    white-space: nowrap;
  }
}

.radio-group-horizontal {
  min-width: 0;
  max-width: 100%;
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.radio-label {
  min-width: 0;
  max-width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  color: $color-text;

  span {
    min-width: 0;
    overflow-wrap: anywhere;
    order: 1;
  }

  input[type="radio"] {
    flex-shrink: 0;
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
    font-size: 0.95rem;
  }
}

@container survey-card (max-width: 440px) {
  .step-container {
    gap: 12px;
  }

  .form-row {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .field-label {
    min-width: auto;
    font-size: 0.95rem;
    line-height: 1.4;
  }

  .input-group .input-field {
    min-height: 36px;
    width: 76px;
    font-size: 1rem;
  }

  .input-group .unit {
    font-size: 0.875rem;
  }

  .select-field :deep(.filter-select) {
    min-height: 36px;
    padding: 8px 10px;
    gap: 8px;
    font-size: 0.875rem;
  }

  .radio-label {
    min-height: 36px;
    font-size: 0.95rem;
    line-height: 1.4;
  }
}
</style>
