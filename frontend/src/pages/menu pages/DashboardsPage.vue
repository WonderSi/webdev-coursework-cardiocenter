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


</style>