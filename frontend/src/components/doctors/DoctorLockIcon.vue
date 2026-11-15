<script setup lang="ts">
const { locked = false } = defineProps<{ locked?: boolean }>()
</script>

<template>
  <svg
    width="16"
    height="18"
    viewBox="0 0 20 22"
    fill="none"
    stroke="currentColor"
    stroke-width="1.6"
    aria-hidden="true"
    focusable="false"
  >
    <rect x="3" y="9" width="14" height="10" rx="2" />
    <path :d="locked ? 'M6 9V6a4 4 0 0 1 8 0v3' : 'M14 9V6a4 4 0 0 0-8 0'" />
    <path d="M10 13v3" />
  </svg>
</template>
