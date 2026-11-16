<script setup lang="ts">
import { computed } from 'vue'
import AccessLevelTag from './AccessLevelTag.vue'
import SidebarPageIcon from '@/components/SidebarPageIcon.vue'
import { doctorAccessByLevel } from '@/config/doctor-access'
import type { DoctorAccessLevel } from '@/types/doctor'

const { level } = defineProps<{ level: DoctorAccessLevel }>()
const details = computed(() => doctorAccessByLevel[level])
</script>

<template>
  <span class="access-details">
    <span class="access-details-title">
      <AccessLevelTag :level="level" />
    </span>
    <span class="access-details-description" lang="ru">{{ details.description }}</span>
    <span class="access-pages-title">Доступны вкладки:</span>
    <span class="access-pages-list">
      <span
        v-for="page in details.pages"
        :key="page.page"
        class="access-page"
      >
        <SidebarPageIcon :page="page.page" class="access-page-icon" />
        <span class="access-page-label">{{ page.label }}</span>
      </span>
    </span>
  </span>
</template>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.access-details {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.access-details-title {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.access-details-description {
  color: var(--access-description-color, inherit);
  font-size: var(--access-description-font-size, inherit);
  line-height: var(--access-description-line-height, 1.5);
  text-align: justify;
  text-align-last: left;
  hyphens: auto;
}

.access-pages-title {
  margin-top: 16px;
  margin-bottom: 8px;
  font-size: var(--access-pages-title-size, inherit);
  font-weight: 600;
}

.access-pages-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.access-page {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding: var(--access-page-padding, 8px 10px);
  border: 1px solid $color-accent;
  border-radius: $radius-rect-buttons;
  background: transparent;
  color: $color-blue-dark;
  font-size: var(--access-page-font-size, inherit);
  line-height: 1.4;
}

.access-page-icon {
  display: block;
  flex-shrink: 0;
}

.access-page-label {
  min-width: 0;
  overflow-wrap: anywhere;
}
</style>
