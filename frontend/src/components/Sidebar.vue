<template>
  <aside class="sidebar-glass">
    <div class="sidebar-header">
      <div class="logo">
        <img src="@/assets/logo-sidebar.png" alt="logo"/>
      </div>
      <div class="nii-text">НИИ КПССЗ</div>
    </div>

    <div class="spacer-32"></div>
    <div class="divider"></div>
    <div class="spacer-32"></div>

    <nav class="main-menu">
      <router-link to="/data-view" class="menu-item">
        <div class="icon-placeholder"></div>
        <span>Просмотр данных</span>
      </router-link>
      
      <router-link to="/data-upload" class="menu-item">
        <div class="icon-placeholder"></div>
        <span>Загрузка данных</span>
      </router-link>
      
      <router-link to="/model-training" class="menu-item">
        <div class="icon-placeholder"></div>
        <span>Обучение модели</span>
      </router-link>
      
      <router-link to="/dashboards" class="menu-item">
        <div class="icon-placeholder"></div>
        <span>Аналитика</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <div class="user-profile">
        <div class="user-info">
          <div class="icon-placeholder circle"></div>
          <span class="username">Имя Врача</span>
        </div>
        <button class="logout-btn" title="Выйти">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/></svg>
        </button>
      </div>

      <div class="divider"></div>
      <div class="spacer-24"></div>

      <button class="support-btn">
        <div class="icon-placeholder small"></div>
        <span>Сообщить о проблеме</span>
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">

</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.sidebar-glass {
  width: 240px;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  
  display: flex;
  flex-direction: column;
  padding: 24px;
  box-sizing: border-box;

  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.5);
  z-index: 100;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-start;

  .sidebar-logo {
    width: 50px;
    height: 50px;
    object-fit: contain;
  }

  .nii-text {
    font-size: 1.3rem;
    font-weight: 700;
    color: $color-white;
    line-height: 1.2;
  }
}

.spacer-32 { height: 32px; }
.spacer-24 { height: 24px; }

.main-menu {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 16px;
  text-decoration: none;
  color: $color-white;
  font-weight: 500;
  font-size: 0.95rem;
  transition: all 0.2s ease;

  &:hover, &.router-link-active {
    color: #ffffff;
    .icon-placeholder { background: rgba(255, 255, 255, 0.4); }
  }
}

.sidebar-footer {
  margin-top: auto; /* Прижимает футер к низу */
  display: flex;
  flex-direction: column;
}

.user-profile {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    color: #ffffff;
    font-weight: 600;
  }
}

.logout-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  transition: color 0.2s;
  &:hover { color: #ffffff; }
}

.support-btn {
  background: none;
  border: none;
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-family: inherit;
  font-size: 0.9rem;
  cursor: pointer;
  padding: 0;
  text-align: left;
  
  &:hover { color: #ffffff; }
}

.icon-placeholder {
  width: 24px;
  height: 24px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  flex-shrink: 0;

  &.circle { border-radius: 50%; }
  &.small { width: 20px; height: 20px; }
}
</style>