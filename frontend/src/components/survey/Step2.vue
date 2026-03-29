<template>
  <div class="form-container">
    <div class="radio-group-vertical">
      <label class="radio-label">
        <input type="radio" :value="3" v-model="form.alcohol" />
        <span>Употребляю</span>
      </label>
      <label class="radio-label">
        <input type="radio" :value="2" v-model="form.alcohol" />
        <span>Употреблял ранее</span>
      </label>
      <label class="radio-label">
        <input type="radio" :value="1" v-model="form.alcohol" />
        <span>Никогда не употреблял</span>
      </label>
    </div>
  </div>
</template>

<script setup lang="ts">
// Получаем доступ к объекту form из родительского компонента
defineProps<{
  form: Record<string, any>
}>()
</script>
