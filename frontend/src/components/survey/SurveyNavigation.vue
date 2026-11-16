<template>
  <div class="actions">
    <button 
      class="prev-btn"
      :class="{ hidden: currentStepIndex === 0, 'keyboard-active': activeKeyBtn === 'prev' }"
      @click="$emit('prev')"
    >
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
      Назад
    </button>

    <button 
      v-if="!isLastStep"
      class="next-btn" 
      :class="{ disabled: isNextBtnDisabled, expanded: currentStepIndex === 0, 'keyboard-active': activeKeyBtn === 'next' }"
      :disabled="isNextBtnDisabled"
      @click="$emit('next')"
    >
      Далее
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"></polyline></svg>
    </button>

    <button 
      v-else
      class="finish-btn" 
      :class="{ disabled: isNextBtnDisabled, 'keyboard-active': activeKeyBtn === 'next' }"
      :disabled="isNextBtnDisabled"
      @click="$emit('next')"
    >
      Завершить
    </button>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  currentStepIndex: number
  isLastStep: boolean
  isNextBtnDisabled: boolean
  activeKeyBtn?: 'next' | 'prev' | null
}>()

defineEmits<{
  (e: 'next'): void;
  (e: 'prev'): void;
}>()
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;
@use '@/styles/survey-scale' as survey;

.actions {
  min-width: 0;
  display: flex;
  justify-content: space-between;
  width: 100%;
  max-width: survey.size(360);
  height: survey.size(48);
  flex-shrink: 0;
}

.prev-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: survey.size(8);
  padding-top: survey.size(2);
  padding-right: survey.size(12);
  width: survey.size(130);
  height: 100%;
  background: transparent;
  color: $color-accent;
  border: 1.5px solid $color-accent;
  border-radius: $radius-rect-buttons;
  font-size: survey.text(1);
  font-weight: 600;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  white-space: nowrap;

  transition: all 0.2s ease;
  
  &:hover, &.keyboard-active {
    background: $color-accent-lighter;
    border: $color-accent-lighter;
    color: $color-white;
  }
  
  &.hidden { 
    width: 0;
    opacity: 0;
    padding: 0;
    border-width: 0;
    pointer-events: none; 
  }
}

.next-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: survey.size(8);
  padding-top: survey.size(2);
  padding-left: survey.size(12);
  width: survey.size(130);
  height: 100%;
  background: $color-accent;
  color: $color-white;
  border: none;
  border-radius: $radius-rect-buttons;
  font-size: survey.text(1);
  font-weight: 600;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;

  transition: all 0.2s ease;

  &:hover:not(.disabled), &.keyboard-active:not(.disabled) {
    background: $color-accent-lighter;
  }

  &.disabled {
    background: $color-accent-lighter;
    cursor: not-allowed;
  }

  &.expanded {
    width: 100%;
  }
}

.finish-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: survey.size(130);
  height: 100%;
  background: $color-accent;
  color: $color-white;
  border: none;
  border-radius: $radius-rect-buttons;
  font-size: survey.text(1);
  font-weight: 600;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;

  &:hover:not(.disabled), &.keyboard-active:not(.disabled) {
    background: $color-accent-lighter;
  }

  &.disabled {
    background: $color-accent-lighter;
    cursor: not-allowed;
  }
}

.actions svg {
  width: survey.size(20);
  height: survey.size(20);
  flex-shrink: 0;
}

@container survey-card (max-width: 440px) {
  .actions {
    gap: 8px;
    height: 40px;
  }

  .prev-btn,
  .next-btn,
  .finish-btn {
    width: auto;
    min-width: 0;
    flex: 1;
    padding: 0 12px;
    font-size: 0.9375rem;
    letter-spacing: 0;
  }

  .prev-btn.hidden {
    display: none;
  }
}

@container survey-card (max-width: 250px) {
  .actions {
    flex-direction: column;
    height: auto;
  }

  .prev-btn,
  .next-btn,
  .finish-btn {
    width: 100%;
    height: 40px;
    flex: none;
  }
}
</style>
