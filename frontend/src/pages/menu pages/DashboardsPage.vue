<template>
  <sidebar/>
  <div class="dashboards-grid">

    <div class="small-card">
      <div class="small-card-header">
        <div class="small-card-icon"></div>
        <div class="small-card-header"></div>
      </div>
      <span class="small-card-number">12352</span>
      <div class="last-update">
        <div class="dynamics">
          <div class="dynamics-icon"></div>
          <span class="dynamics-span">+100</span>
        </div>
        <span class="last-update">02.04.2026</span>
      </div>
    </div>

    <div class="small-card">
      <div class="small-card-header">
        <div class="small-card-icon"></div>
        <div class="small-card-header"></div>
      </div>
      <div class="small-card-two-numbers">
        <div class="percentage">
          <span class="small-card-number">58%</span>
          <span class="men">мужчины</span>
        </div>
        <div class="percentage">
          <span class="small-card-number">42%</span>
          <span class="men">женщины</span>
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

.dashboards-grid {
  margin-left: 240px; 
  padding: 40px;
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 24px;
}

.small-card {
  grid-column: span 2;
  display: flex;
  flex-direction: column;
  
  padding: 24px;
  border-radius: 24px;
  
  background: rgba($color-glass-white, 0.4); 
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba($color-glass-white, 0.6);
  
  box-shadow: 0 4px 24px rgba(7, 14, 44, 0.05);
}

</style>