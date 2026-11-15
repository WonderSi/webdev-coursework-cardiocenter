<script setup lang="ts">
defineProps<{ message: string }>()
</script>

<template>
  <span class="field-alert" role="alert">
    <svg
      class="field-alert-icon"
      viewBox="0 0 24 24"
      aria-hidden="true"
      focusable="false"
    >
      <rect x="2" y="2" width="20" height="20" rx="3" fill="currentColor" />
      <path d="M12 7v6" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" />
      <circle cx="12" cy="17" r="1" fill="white" />
    </svg>
    <span class="field-alert-text">{{ message }}</span>
  </span>
</template>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.field-alert {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border: 1px solid $color-red;
  border-radius: $radius-round;
  background: $color-red-bg;
  color: $color-red;
  font-size: 0.85rem;
  font-weight: 400;
  line-height: 1.5;
}

.field-alert-icon {
  display: block;
  flex: 0 0 20px;
  width: 20px;
  height: 20px;
  color: $color-red;
}

.field-alert-text {
  min-width: 0;
  overflow-wrap: anywhere;
}
</style>
