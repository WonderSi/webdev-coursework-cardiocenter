<script setup lang="ts" generic="T extends string | number | null">
import { computed, nextTick, onMounted, onScopeDispose, shallowRef, useId, useTemplateRef, watch } from 'vue'

const value = defineModel<T>({ required: true })
const { options, label, labelledby, id, placeholder = 'Выберите вариант', disabled = false, invalid = false } = defineProps<{
  options: readonly { value: T; label: string }[]
  label?: string
  labelledby?: string
  id?: string
  placeholder?: string
  disabled?: boolean
  invalid?: boolean
}>()
const root = useTemplateRef<HTMLDivElement>('root')
const trigger = useTemplateRef<HTMLButtonElement>('trigger')
const list = useTemplateRef<HTMLUListElement>('list')
const listId = useId()
const open = shallowRef(false)
const activeIndex = shallowRef(0)
const popupStyle = shallowRef<Record<string, string>>({})
const selectedIndex = computed(() => options.findIndex(option => option.value === value.value))
const selectedLabel = computed(() => options[selectedIndex.value]?.label ?? placeholder)

function positionPopup() {
  if (!open.value || !trigger.value) return
  const rect = trigger.value.getBoundingClientRect()
  const below = innerHeight - rect.bottom - 14
  const above = rect.top - 14
  const upwards = below < 160 && above > below
  const height = Math.max(40, Math.min(200, upwards ? above : below))
  const top = upwards ? rect.top - height - 6 : rect.bottom + 6
  popupStyle.value = {
    left: Math.max(8, Math.min(rect.left, innerWidth - rect.width - 8)) + 'px',
    width: Math.min(rect.width, innerWidth - 16) + 'px',
    maxHeight: height + 'px',
    '--select-height': height + 'px',
    top: Math.max(8, Math.min(top, innerHeight - height - 8)) + 'px',
  }
}

function show() {
  if (disabled || !options.length) return
  activeIndex.value = Math.max(0, selectedIndex.value)
  open.value = true
  positionPopup()
}

function choose(index: number) {
  const option = options[index]
  if (!option) return
  value.value = option.value
  open.value = false
  trigger.value?.focus()
}

function onKeydown(event: KeyboardEvent) {
  if (disabled || !options.length) return
  const key = event.key
  if (['ArrowDown', 'ArrowUp', 'Home', 'End', 'Enter', ' '].includes(key)) {
    event.preventDefault()
    if (!open.value) {
      show()
      if (key === 'End') activeIndex.value = options.length - 1
      return
    }
    if (key === 'Enter' || key === ' ') choose(activeIndex.value)
    else if (key === 'Home') activeIndex.value = 0
    else if (key === 'End') activeIndex.value = options.length - 1
    else activeIndex.value = (activeIndex.value + (key === 'ArrowDown' ? 1 : -1) + options.length) % options.length
  } else if (key === 'Escape' || key === 'Tab') {
    if (open.value && key === 'Escape') event.preventDefault()
    open.value = false
  } else if (key.length === 1 && !event.ctrlKey && !event.metaKey && !event.altKey) {
    const index = options.findIndex(option => option.label.toLocaleLowerCase().startsWith(key.toLocaleLowerCase()))
    if (index >= 0) {
      if (!open.value) show()
      activeIndex.value = index
    }
  }
}

function onOutsidePointer(event: PointerEvent) {
  if (event.target instanceof Node && !root.value?.contains(event.target) && !list.value?.contains(event.target)) open.value = false
}

watch(() => disabled, isDisabled => {
  if (isDisabled) open.value = false
})
watch(() => options, () => { open.value = false })
watch([open, activeIndex], async () => {
  if (!open.value) return
  await nextTick()
  list.value?.children[activeIndex.value]?.scrollIntoView({ block: 'nearest', inline: 'nearest' })
})
onMounted(() => {
  document.addEventListener('pointerdown', onOutsidePointer)
  window.addEventListener('resize', positionPopup)
  window.addEventListener('scroll', positionPopup, true)
})
onScopeDispose(() => {
  document.removeEventListener('pointerdown', onOutsidePointer)
  window.removeEventListener('resize', positionPopup)
  window.removeEventListener('scroll', positionPopup, true)
})
</script>

<template>
  <div ref="root" class="app-select">
    <button
      ref="trigger"
      :id="id"
      type="button"
      class="filter-select"
      role="combobox"
      :aria-label="label"
      :aria-labelledby="labelledby"
      :aria-invalid="invalid || undefined"
      aria-haspopup="listbox"
      :aria-expanded="open"
      :aria-controls="listId"
      :aria-activedescendant="open ? `${listId}-${activeIndex}` : undefined"
      :disabled="disabled"
      @click="open ? open = false : show()"
      @keydown="onKeydown"
      @blur="open = false"
    >
      <span class="select-label">{{ selectedLabel }}</span>
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
    <Teleport to="body">
      <div v-if="open" class="select-popup" :style="popupStyle">
        <ul
          ref="list"
          :id="listId"
          class="filter-options"
          role="listbox"
          :aria-label="label"
          :aria-labelledby="labelledby"
          @pointerdown.prevent
        >
          <li
            v-for="(option, index) in options"
            :id="`${listId}-${index}`"
            :key="option.value ?? index"
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
    </Teleport>
  </div>
</template>

<style scoped lang="scss">
.app-select {
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
  border: 1px solid $color-accent-lighter;
  border-radius: 10px;
  background: rgba($color-white, 0.75);
  color: $color-blue-dark;
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

.filter-select[aria-invalid='true'] {
  border-color: $color-red;
}

.filter-select:disabled {
  color: $color-secondary-text;
  background: #f1f4f9;
  cursor: default;
}

.select-label {
  min-width: 0;
  overflow-wrap: anywhere;
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

.select-popup {
  position: fixed;
  z-index: 1000;
  box-sizing: border-box;
  padding: 6px;
  border: 1px solid $color-ui-border;
  border-radius: 10px;
  background: $color-white;
  box-shadow: 0 8px 24px rgba($color-blue-dark, 0.12);
  overflow: hidden;
}

.filter-options {
  @include custom-scrollbar;

  margin: 0;
  padding: 0;
  list-style: none;
  max-height: max(24px, calc(var(--select-height) - 14px));
  overflow-y: auto;
}

.filter-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 8px;
  border-radius: 6px;
  color: $color-blue-dark;
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
