<template>
  <div class="background" :class="{ 'is-dimmed': dimmed }">
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  dimmed?: boolean
}>()
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.background {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: background-color 0.5s ease;

  background-color: $color-bg-main;
  background-image:
    radial-gradient(ellipse at 20% 60%, rgba($color-bg-grad2, 0.25) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba($color-bg-grad1, 1.0) 0%, transparent 45%),
    radial-gradient(ellipse at 30% 30%, rgba($color-bg-grad3, 0.2) 0%, transparent 35%),
    radial-gradient(ellipse at 70% 80%, rgba($color-bg-grad4, 0.75) 0%, transparent 40%),
    radial-gradient(ellipse at 60% 60%, rgba($color-bg-grad5, 1.0) 0%, transparent 30%),
    radial-gradient(ellipse at 10% 90%, rgba($color-bg-grad6, 0.2) 0%, transparent 40%),
    radial-gradient(ellipse at 50% 10%, rgba($color-bg-grad8, 1.0) 0%, transparent 40%),
    radial-gradient(ellipse at 10% 80%, rgba($color-bg-grad7, 0.9) 0%, transparent 50%);

    &.is-dimmed {
    background-blend-mode: soft-light;
    background-color: rgba($color-white, 0.25); 
  }
}
</style>