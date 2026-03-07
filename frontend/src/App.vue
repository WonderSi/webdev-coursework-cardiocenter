<template>
  <component :is="currentLayout">
    <RouterView />
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const route = useRoute()

const currentLayout = DefaultLayout

</script>

<style scoped>

</style>
