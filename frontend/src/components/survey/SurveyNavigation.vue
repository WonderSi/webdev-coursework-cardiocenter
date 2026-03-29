<template>
  <div class="actions">
    <button 
      class="prev-btn" 
      :class="{ hidden: currentStepIndex === 0 }"
      @click="$emit('prev')"
    >
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
      Назад
    </button>

    <button 
      class="next-btn" 
      :class="{ disabled: isNextBtnDisabled, expanded: currentStepIndex === 0 }"
      :disabled="isNextBtnDisabled"
      @click="$emit('next')"
    >
      {{ isLastStep ? 'Завершить' : 'Далее' }}
      <svg v-if="!isLastStep" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"></polyline></svg>
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  currentStepIndex: number;
  isLastStep: boolean;
  isNextBtnDisabled: boolean;
}>()

defineEmits<{
  (e: 'next'): void;
  (e: 'prev'): void;
}>()
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.actions {
  display: flex;
  justify-content: space-between;
  width: 100%;
  max-width: 360px;
  height: 48px;
  flex-shrink: 0;
}

.prev-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding-top: 2px;
  padding-right: 12px;
  width: 130px;
  height: 100%;
  background: transparent;
  color: $color-accent;
  border: 1.5px solid $color-accent;
  border-radius: $radius-rect-buttons;
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  white-space: nowrap;
  &:hover {
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
  gap: 8px;
  padding-top: 2px;
  padding-left: 12px;
  width: 130px;
  height: 100%;
  background: $color-accent;
  color: $color-white;
  border: none;
  border-radius: $radius-rect-buttons;
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  &:hover:not(.disabled) { background: $color-accent-lighter; }
  &.disabled { background: $color-accent-lighter; cursor: not-allowed; }
  &.expanded { width: 100%; }
}
</style>