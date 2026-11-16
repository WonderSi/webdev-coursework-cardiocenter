<script setup lang="ts">
const { label = 'Закрыть', disabled = false } = defineProps<{
  label?: string
  disabled?: boolean
}>()

const emit = defineEmits<{ click: [event: MouseEvent] }>()
</script>

<template>
  <button
    type="button"
    class="close-btn"
    :aria-label="label"
    :disabled="disabled"
    @click="emit('click', $event)"
  >
    <svg
      class="close-icon"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="1.75"
      stroke-linecap="round"
      aria-hidden="true"
      focusable="false"
    >
      <path d="m6 6 12 12M18 6 6 18" />
    </svg>
  </button>
</template>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.close-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: $color-text;
  line-height: 1;
  cursor: pointer;
  transition: color 0.2s ease, background-color 0.2s ease;
}

.close-icon {
  display: block;
  flex: 0 0 16px;
  width: 16px;
  height: 16px;
}

.close-btn:hover:not(:disabled) {
  background: $color-red-bg;
  color: $color-cold-red;
}

.close-btn:focus-visible {
  @include focus-ring(3px, 3px);
}

.close-btn:disabled {
  opacity: 0.55;
  cursor: default;
}
</style>
