<template>
  <div class="question-row">
    <!-- Radio type -->
    <template v-if="field.type === 'radio'">
      <span class="field-label">{{ field.label }}:</span>
      <div class="radio-group-horizontal">
        <label v-for="opt in field.options" :key="opt.valueId" class="radio-label">
          <span>{{ opt.label }}</span>
          <input type="radio" :name="field.key" :value="opt.valueId" v-model="answers[field.key]" />
        </label>
      </div>
    </template>

    <!-- Number type -->
    <template v-else-if="field.type === 'number'">
      <span class="field-label">{{ field.label }}:</span>
      <div class="input-group">
        <input
          type="number"
          class="input-field"
          :placeholder="field.placeholder || ''"
          v-model.number="answers[field.key]"
        />
        <span class="unit">{{ field.unit }}</span>
      </div>
    </template>

    <!-- Yes/No type with optional year input -->
    <template v-else-if="field.type === 'yesno'">
      <div class="question-wrapper">
        <div class="question-block">
          <span class="disease-name">{{ field.label }}</span>

          <div class="radio-group-horizontal">
            <label class="radio-label">
              <span>Нет</span>
              <input type="radio" :name="field.key" :value="0" v-model="answers[field.key]" />
            </label>
            <label class="radio-label">
              <span>Да</span>
              <input type="radio" :name="field.key" :value="1" v-model="answers[field.key]" />
            </label>
          </div>

          <Transition name="expand-down">
            <div v-if="answers[field.key] === 1 && field.yearKey" class="conditional-block">
              <div class="connecting-line-graphic"></div>
              <span class="year-label">Год первого диагностирования:</span>
              <div class="input-group">
                <input
                  type="number"
                  class="input-field year-input"
                  placeholder="2012"
                  v-model.number="answers[field.yearKey!]"
                  min="1950"
                  :max="new Date().getFullYear()"
                />
                <span class="unit">г.</span>
              </div>
            </div>
          </Transition>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { useSurveyStore } from '@/stores/survey.store'
import type { SurveyField } from '@/stores/survey.store'

defineProps<{
  field: SurveyField
}>()

const store = useSurveyStore()
const answers = store.answers
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.question-row {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.field-label {
  font-size: 1rem;
  font-weight: 700;
  color: $color-text;
  white-space: nowrap;
  min-width: 100px;
}

.input-group {
  display: flex;
  align-items: center;
  gap: 8px;

  .input-field {
    width: 80px;
    padding: 8px 12px;
    border: 1.5px solid $color-accent-lighter2;
    border-radius: 10px;
    font-size: 1rem;
    color: $color-text;
    outline: none;
    transition: border-color 0.2s;

    &:focus { border-color: $color-accent; }

    &.year-input { width: 80px; flex: none; }
  }

  .unit {
    color: $color-text;
    font-size: 0.9rem;
    white-space: nowrap;
  }
}

.radio-group-horizontal {
  display: flex;
  gap: 24px;
  flex-direction: row;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 10px;
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

/* Yes/No section */
.question-wrapper {
  width: 100%;
}

.question-block {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16px;
  padding-bottom: 16px;
  padding-left: 80px;
  position: relative;
}

.disease-name {
  font-weight: 700;
  color: $color-text;
  font-size: 1rem;
  line-height: 1.3;
}

.conditional-block {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  position: relative;
  padding-left: 32px;
}

.connecting-line-graphic {
  position: absolute;
  top: -64px;
  left: -10px;
  width: 32px;
  height: 78px;
  border-left: 4px solid $color-accent;
  border-bottom: 4px solid $color-accent;
  border-bottom-left-radius: 6px;
  z-index: 1;
  pointer-events: none;
}

.year-label {
  font-size: 0.95rem;
  color: $color-text;
  white-space: nowrap;
}

/* Animations */
.expand-down-enter-active {
  animation: expandBlock 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.expand-down-leave-active {
  animation: expandBlock 0.3s cubic-bezier(0.4, 0, 0.2, 1) reverse forwards;
}

@keyframes expandBlock {
  0% { opacity: 0; max-height: 0; transform: translateY(-10px); }
  100% { opacity: 1; max-height: 100px; transform: translateY(0); }
}

.expand-down-enter-active .connecting-line-graphic {
  animation: drawLine 0.5s linear forwards;
}

.expand-down-leave-active .connecting-line-graphic {
  animation: drawLine 0.5s linear reverse forwards;
}

@keyframes drawLine {
  0% { clip-path: inset(0 0 100% 0); opacity: 0; }
  20% { opacity: 0.3; }
  50% { opacity: 0.5; }
  100% { clip-path: inset(0 0 0 0); opacity: 1; }
}
</style>
