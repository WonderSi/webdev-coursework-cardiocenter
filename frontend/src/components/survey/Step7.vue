<template>
  <div class="step-container">
    <fieldset class="question-block" v-for="field in group.fields" :key="field.key">
      <legend class="question-text">{{ field.label }}</legend>

      <div class="radio-group-horizontal">
        <label class="radio-label">
          <span>Нет</span>
          <input type="radio" :name="field.key" :value="0" :required="group.requiredFields.includes(field.key)" v-model="answers[field.key]" />
        </label>
        <label class="radio-label">
          <span>Да</span>
          <input type="radio" :name="field.key" :value="1" :required="group.requiredFields.includes(field.key)" v-model="answers[field.key]" />
        </label>
      </div>
    </fieldset>
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
  width: 100%;
  max-width: 400px;
}

.question-block {
  min-width: 0;
  border: 0;
  margin: 0;
  padding: 0;
  text-align: left;
}

.question-text {
  padding: 0;
  margin-bottom: 16px;
  font-weight: 700;
  color: $color-text;
  font-size: 1rem;
  line-height: 1.3;
}

.radio-group-horizontal {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 0.95rem;
  color: $color-text;

  input[type="radio"] {
    accent-color: $color-accent;
    width: 18px;
    height: 18px;
    cursor: pointer;
  }
}
</style>
