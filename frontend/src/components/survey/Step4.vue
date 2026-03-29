<template>
  <div class="form-container">
    <div class="radio-group-vertical">
      <label class="radio-label">
        <input type="radio" :value="6" v-model="form.region" />
        <span>Сельская местность</span>
      </label>
      <label class="radio-label">
        <input type="radio" :value="2" v-model="form.region" />
        <span>Центральный</span>
      </label>
      <label class="radio-label">
        <input type="radio" :value="1" v-model="form.region" />
        <span>Рудничный</span>
      </label>
      <label class="radio-label">
        <input type="radio" :value="3" v-model="form.region" />
        <span>Заводский</span>
      </label>
      <label class="radio-label">
        <input type="radio" :value="4" v-model="form.region" />
        <span>Кировский</span>
      </label>
      <label class="radio-label">
        <input type="radio" :value="5" v-model="form.region" />
        <span>Ленинский</span>
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
