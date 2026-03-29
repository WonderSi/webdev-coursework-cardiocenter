<template>
  <div class="form-container">
    <div class="form-group">
      <label class="label">Пол:</label>
      <div class="radio-group-horizontal">
        <label class="radio-label">
          <span>Мужской</span>
          <input type="radio" :value="1" v-model="form.gender" />
        </label>
        <label class="radio-label">
          <span>Женский</span>
          <input type="radio" :value="2" v-model="form.gender" />
        </label>
      </div>
    </div>

    <div class="form-group">
      <label class="label">Возраст:</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="27" v-model="form.age" />
        <span class="unit">лет</span>
      </div>
    </div>
    
    <div class="form-group">
      <label class="label">Рост:</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="165" v-model="form.height" />
        <span class="unit">см</span>
      </div>
    </div>

    <div class="form-group">
      <label class="label">Вес:</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="60" v-model="form.weight" />
        <span class="unit">кг</span>
      </div>
    </div>

    <div class="form-group">
      <label class="label">Объём бёдер:</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="90" v-model="form.hipMeasurement" />
        <span class="unit">см</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Получаем доступ к объекту form из родительского компонента
defineProps<{
  form: Record<string, any>
}>()
</script>
