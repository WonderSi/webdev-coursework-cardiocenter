<script setup lang="ts">
import { computed, onMounted, onScopeDispose, shallowRef, useId, useTemplateRef, watch } from 'vue'
import { doctorAccessLevels } from '@/config/doctor-access'
import type { DoctorAccessLevel } from '@/types/doctor'

type FilterValue = DoctorAccessLevel | 'all'

const value = defineModel<FilterValue>({ required: true })
const { disabled = false } = defineProps<{ disabled?: boolean }>()
const root = useTemplateRef<HTMLDivElement>('root')
const listId = useId()
const open = shallowRef(false)
const activeIndex = shallowRef(0)
const options = [
  { value: 'all' as const, label: 'Все уровни доступа' },
  ...doctorAccessLevels,
]
const selectedIndex = computed(() => options.findIndex(option => option.value === value.value))
const selectedLabel = computed(() => options[selectedIndex.value]?.label)

function show() {
  activeIndex.value = Math.max(0, selectedIndex.value)
  open.value = true
}

function choose(index: number) {
  const option = options[index]
  if (!option) return
  value.value = option.value
  open.value = false
}

function onKeydown(event: KeyboardEvent) {
  if (disabled) return
  switch (event.key) {
    case 'ArrowDown':
    case 'ArrowUp':
      event.preventDefault()
      if (!open.value) show()
      else activeIndex.value = (activeIndex.value + (event.key === 'ArrowDown' ? 1 : -1) + options.length) % options.length
      break
    case 'Home':
    case 'End':
      event.preventDefault()
      if (!open.value) show()
      activeIndex.value = event.key === 'Home' ? 0 : options.length - 1
      break
    case 'Enter':
    case ' ':
      event.preventDefault()
      if (open.value) choose(activeIndex.value)
      else show()
      break
    case 'Escape':
      if (open.value) event.preventDefault()
      open.value = false
      break
    case 'Tab':
      open.value = false
      break
  }
}

function onOutsidePointer(event: PointerEvent) {
  if (event.target instanceof Node && !root.value?.contains(event.target)) open.value = false
}

watch(() => disabled, isDisabled => {
  if (isDisabled) open.value = false
})
onMounted(() => document.addEventListener('pointerdown', onOutsidePointer))
onScopeDispose(() => document.removeEventListener('pointerdown', onOutsidePointer))
</script>

<template>
  <div ref="root" class="access-dropdown">
    <button
      type="button"
      class="filter-select"
      role="combobox"
      aria-label="Фильтр по правам доступа"
      aria-haspopup="listbox"
      :aria-expanded="open"
      :aria-controls="listId"
      :aria-activedescendant="open ? `${listId}-${activeIndex}` : undefined"
      :disabled="disabled"
      @click="open ? open = false : show()"
      @keydown="onKeydown"
      @blur="open = false"
    >
      <span>{{ selectedLabel }}</span>
      <svg
        class="filter-chevron"
        :class="{ 'is-open': open }"
        viewBox="0 0 20 20"
        fill="none"
        stroke="currentColor"
        stroke-width="1.5"
        aria-hidden="true"
      >
        <path d="m5 7.5 5 5 5-5" />
      </svg>
    </button>
    <ul
      v-show="open"
      :id="listId"
      class="filter-options"
      role="listbox"
      aria-label="Права доступа"
      @pointerdown.prevent
    >
      <li
        v-for="(option, index) in options"
        :id="`${listId}-${index}`"
        :key="option.value"
        class="filter-option"
        :class="{ 'is-active': activeIndex === index }"
        role="option"
        :aria-selected="value === option.value"
        @pointermove="activeIndex = index"
        @click="choose(index)"
      >
        <span>{{ option.label }}</span>
        <span v-if="value === option.value" aria-hidden="true">✓</span>
      </li>
    </ul>
  </div>
</template>

<style scoped lang="scss">
.access-dropdown {
  position: relative;
  min-width: 0;
}

.filter-select {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  min-height: 43px;
  padding: 10px 14px;
  border: 1px solid $color-ui-border;
  border-radius: 10px;
  background: rgba($color-white, 0.75);
  color: $color-text;
  font: inherit;
  font-size: 0.875rem;
  text-align: left;
  cursor: pointer;
}

.filter-select:focus,
.filter-select[aria-expanded='true'] {
  border-color: $color-accent;
  outline: none;
}

.filter-select:disabled {
  color: $color-secondary-text;
  background: #f1f4f9;
  cursor: default;
}

.filter-chevron {
  flex: 0 0 18px;
  width: 18px;
  height: 18px;
  color: $color-accent;
}

.filter-chevron.is-open {
  transform: rotate(180deg);
}

.filter-options {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  left: 0;
  z-index: 2;
  margin: 0;
  padding: 6px;
  border: 1px solid $color-ui-border;
  border-radius: 10px;
  background: $color-white;
  box-shadow: 0 8px 24px rgba($color-blue-dark, 0.12);
  list-style: none;
}

.filter-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 8px;
  border-radius: 6px;
  color: $color-text;
  font-size: 0.875rem;
  line-height: 1.4;
  cursor: pointer;
}

.filter-option.is-active {
  background: $color-ui-surface-hover;
}

.filter-option[aria-selected='true'] {
  color: $color-accent;
}
</style>
