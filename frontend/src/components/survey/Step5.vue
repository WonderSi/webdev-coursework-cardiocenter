<template>
  <div class="form-container">
    <div class="form-group">
      <label class="label">Глюкоза:</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="4,5" v-model="form.glucose" />
        <span class="unit">ммоль/л</span>
      </div>
    </div>
    
    <div class="form-group">
      <label class="label">Холестерин:</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="5,3" v-model="form.cholesterol" />
        <span class="unit">ммоль/л</span>
      </div>
    </div>

    <div class="form-group">
      <label class="label">Холестерин не-ЛПВП:</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="3,2" v-model="form.nonHdlCholesterol" />
        <span class="unit">ммоль/л</span>
      </div>
    </div>

    <div class="form-group">
      <label class="label">Холестерин ЛПОНП:</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="0,8" v-model="form.vldlCholesterol" />
        <span class="unit">ммоль/л</span>
      </div>
    </div>

    <div class="form-group">
      <label class="label">Холестерин ЛПВП:</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="1,2" v-model="form.hdlCholesterol" />
        <span class="unit">ммоль/л</span>
      </div>
    </div>

    <div class="form-group">
      <label class="label">Холестерин ЛПНП:</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="3,0" v-model="form.ldlCholesterol" />
        <span class="unit">ммоль/л</span>
      </div>
    </div>

    <div class="form-group">
      <label class="label">Аполипопротеин А (Апо А):</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="1,5" v-model="form.apolipoproteinA" />
        <span class="unit">г/л</span>
      </div>
    </div>

    <div class="form-group">
      <label class="label">Аполипопротеин B (Апо B):</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="0,9" v-model="form.apolipoproteinB" />
        <span class="unit">г/л</span>
      </div>
    </div>

    <div class="form-group">
      <label class="label">Триглицериды:</label>
      <div class="input-group">
        <input type="number" class="input-field" placeholder="1,7" v-model="form.triglycerides" />
        <span class="unit">ммоль/л</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  form: Record<string, any>
}>()
</script>

<style scoped lang="scss">
.form-container {
  gap: 24px;
}

.label {
  width: 220px;
}
</style>