<template>
  <aside class="sidebar">
    
    <div class="logo-and-text">
      <div class="logo">
        <img src="@/assets/logo-sidebar.png" alt="logo"/>
      </div>
      <div class="nii-text">НИИ КПССЗ</div>
    </div>

    <nav class="sidebar-nav">
      <router-link to="/dashboards" class="nav-item">
        <span class="icon"></span>
        Дашборды
      </router-link>
      <router-link to="/patients" class="nav-item">
        <span class="icon"></span>
        Пациенты
      </router-link>
      </nav>

    <div class="sidebar-footer">
      <button class="logout-btn">Выйти</button>
    </div>
  </aside>
</template>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.logo-and-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nii-text {
  font-size: 1rem;
  font-weight: 700;
  color: $color-text;
}

.sidebar {
  width: 240px;
  height: 100%;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  color: #fff;
  text-decoration: none;
  transition: all 0.3s ease;

  &:hover, &.router-link-active {
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(5px);
  }
}
</style>