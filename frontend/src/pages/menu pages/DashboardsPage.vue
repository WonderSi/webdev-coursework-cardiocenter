<template>
  <sidebar/>
  <div class="dashboards-grid-container">

    <div class="small-card">
      <div class="small-card-header">
        <div class="small-card-icon"></div>
        <div class="small-card-title">Всего записей о пацентах</div>
      </div>
      <span class="small-card-number">12352</span>
      <div class="last-update">
        <span class="last-update-date">02.04.2026</span>
        <div class="dynamics">
          <div class="dynamics-icon"></div>
          <span class="dynamics-span">+100</span>
        </div>
      </div>
    </div>

    <div class="small-card">
      <div class="small-card-header">
        <div class="small-card-icon"></div>
        <div class="small-card-title">Соотношение полов</div>
      </div>
      <div class="small-card-two-numbers">
        <div class="percentage">
          <span class="small-card-number">58%</span>
          <span class="gender-label" color="red">мужчины</span>
        </div>
        <div class="percentage">
          <span class="small-card-number">42%</span>
          <span class="gender-label">женщины</span>
        </div>
      </div>
    </div>


  </div>


  
</template>

<script setup lang="ts">
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import Sidebar from '@/components/Sidebar.vue'

</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.dashboards-grid-container {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  grid-template-rows: repeat(5, 1fr);
  height: 100vh;
  margin-left: 240px; 
  padding: 40px;
  gap: 24px;
}

.small-card {
  display: flex;
  flex-direction: column;
  grid-column: span 2fr;

  padding: 24px;
  border-radius: 24px;
  
  background: rgba($color-glass-white, 0.4); 
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba($color-glass-white, 0.6);
  
  box-shadow: 0 4px 24px rgba(7, 14, 44, 0.05);
}

.small-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px; // Отступ от заголовка до цифр
  
  color: $color-text;
  font-size: 2rem;
  font-weight: 600;
}

.small-card-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-text;
}

.small-card-number {
  font-size: 2.5rem;
  font-weight: 700;
  color: $color-text;
  line-height: 1;
}

// ПЕРВАЯ КАРТОЧКА

.last-update {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 16px;
}

.dynamics {
  display: flex;
  align-items: center;
  gap: 4px;
  
  .dynamics-span {
    color: $color-green;
    font-weight: 600;
    font-size: 1rem;
  }
}

last-update-date {
  font-size: 0.8rem;
  color: $color-secondary;
}

// ВТОРАЯ КАРТОЧКА 

.small-card-two-numbers {
  display: flex;
  gap: 24px;
  margin-top: auto;
  justify-content: center;
}

.percentage {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .gender-label {
  font-size: 1rem;
  font-weight: 400;
  align-self: center;
  }

  &:first-child .small-card-number {
    color: $color-blue-dark;
  }
  &:last-child .small-card-number {
    color: $color-pink-women;
  }
}



</style>