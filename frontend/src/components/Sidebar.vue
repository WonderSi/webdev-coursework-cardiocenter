<template>
  <aside class="sidebar-glass">
    <div class="sidebar-header">
      <router-link to="/" class="logo-link">
        <div class="logo">
          <img src="@/assets/logo-sidebar.png" alt="logo"/>
        </div>
        <div class="nii-text">НИИ КПССЗ</div>
      </router-link>
    </div>

    <div class="divider"></div>

    <nav class="main-menu">
      <router-link
        v-if="userStore.canAccessClinicalData"
        to="/questionnaire"
        class="menu-item"
        :class="{ 'router-link-active': route.path.startsWith('/questionnaire') }"
        aria-label="Получить прогноз"
        title="Получить прогноз"
      >
        <div class="icon"><SidebarPageIcon page="questionnaire" /></div>
        <span class="menu-label">Получить прогноз</span>
      </router-link>
      <router-link
        v-if="userStore.canAccessSection('VIEW_DATA')"
        to="/view-data"
        class="menu-item"
        aria-label="Просмотр данных"
        title="Просмотр данных"
      >
        <div class="icon">
          <SidebarPageIcon page="view-data" />
        </div>
        <span class="menu-label">Просмотр данных</span>
      </router-link>
      <!-- ИСКЛЮЧАЕМ. ФУНКЦИЯ НЕ ГОТОВА -->
      <router-link
        v-if="userStore.canAccessSection('DASHBOARDS')"
        to="/dashboards"
        class="menu-item"
        aria-label="Аналитика"
        title="Аналитика"
      >
        <div class="icon">
          <SidebarPageIcon page="dashboards" />
        </div>
        <span class="menu-label">Аналитика</span>
      </router-link>
      
      <router-link
        v-if="userStore.canAccessSection('UPLOAD_DATA')"
        to="/upload-data"
        class="menu-item"
        aria-label="Внесение данных"
        title="Внесение данных"
      >
        <div class="icon">
          <SidebarPageIcon page="upload-data" />
        </div>
        <span class="menu-label">Внесение данных</span>
      </router-link>
      
      <router-link
        v-if="userStore.canAccessSection('MODEL_TRAINING')"
        to="/model-training"
        class="menu-item"
        aria-label="Модели"
        title="Модели"
      >
        <div class="icon">
          <SidebarPageIcon page="model-training" />
        </div>
        <span class="menu-label">Модели</span>
      </router-link>

      <router-link
        v-if="userStore.isSuperAdmin"
        to="/doctors"
        class="menu-item"
        aria-label="Врачи"
        title="Врачи"
      >
        <div class="icon" aria-hidden="true">
          <svg
            width="24"
            height="24"
            viewBox="0 0 22 22"
            fill="currentColor"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path d="M14.6667 12.8334C15.8478 12.8334 16.9832 13.2895 17.8363 14.1063C18.6894 14.9232 19.1942 16.0378 19.2454 17.2178L19.25 17.4167V18.3334C19.2501 18.7959 19.0755 19.2414 18.761 19.5805C18.4465 19.9197 18.0154 20.1274 17.5542 20.1621L17.4167 20.1667H4.58333C4.12081 20.1669 3.67531 19.9922 3.33616 19.6777C2.99701 19.3632 2.78927 18.9321 2.75458 18.4709L2.75 18.3334V17.4167C2.75007 16.2356 3.20608 15.1001 4.02294 14.2471C4.8398 13.394 5.95444 12.8892 7.13442 12.838L7.33333 12.8334H14.6667ZM11 1.83337C12.2156 1.83337 13.3814 2.31626 14.2409 3.1758C15.1004 4.03534 15.5833 5.20113 15.5833 6.41671C15.5833 7.63228 15.1004 8.79807 14.2409 9.65761C13.3814 10.5172 12.2156 11 11 11C9.78442 11 8.61864 10.5172 7.75909 9.65761C6.89955 8.79807 6.41667 7.63228 6.41667 6.41671C6.41667 5.20113 6.89955 4.03534 7.75909 3.1758C8.61864 2.31626 9.78442 1.83337 11 1.83337Z" />
          </svg>
        </div>
        <span class="menu-label">Врачи</span>
      </router-link>

    </nav>

    <div class="retrain-model" v-if="userStore.canAccessSection('RETRAIN') && showRetrainBlock">
      <p class="retrain-text">Переобучение из интерфейса временно недоступно.</p>
      <button class="retrain-btn" disabled>Переобучить</button>
    </div>

    <div class="sidebar-footer">
      <p v-if="logoutError" role="alert">{{ logoutError }}</p>
      <div class="user-profile">
        <div class="icon">
          <svg width="18" height="18" viewBox="1 0 20 20" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
            <path d="M14.6667 12.8334C15.8478 12.8334 16.9832 13.2895 17.8363 14.1063C18.6894 14.9232 19.1942 16.0378 19.2454 17.2178L19.25 17.4167V18.3334C19.2501 18.7959 19.0755 19.2414 18.761 19.5805C18.4465 19.9197 18.0154 20.1274 17.5542 20.1621L17.4167 20.1667H4.58333C4.12081 20.1669 3.67531 19.9922 3.33616 19.6777C2.99701 19.3632 2.78927 18.9321 2.75458 18.4709L2.75 18.3334V17.4167C2.75007 16.2356 3.20608 15.1001 4.02294 14.2471C4.8398 13.394 5.95444 12.8892 7.13442 12.838L7.33333 12.8334H14.6667ZM11 1.83337C12.2156 1.83337 13.3814 2.31626 14.2409 3.1758C15.1004 4.03534 15.5833 5.20113 15.5833 6.41671C15.5833 7.63228 15.1004 8.79807 14.2409 9.65761C13.3814 10.5172 12.2156 11 11 11C9.78442 11 8.61864 10.5172 7.75909 9.65761C6.89955 8.79807 6.41667 7.63228 6.41667 6.41671C6.41667 5.20113 6.89955 4.03534 7.75909 3.1758C8.61864 2.31626 9.78442 1.83337 11 1.83337Z" fill="currentColor" stroke="currentColor"/>
          </svg>
        </div>
        <span class="username" :title="userStore.email ?? undefined">{{ userStore.email }}</span>
        <button class="logout-btn" title="Выйти" @click="handleLogout">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="4">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/>
          </svg>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import SidebarPageIcon from '@/components/SidebarPageIcon.vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user.store'
import { ref, computed } from 'vue'

const route = useRoute()
const userStore = useUserStore()

// когда отображаем блок переобучения
const showRetrainBlock = computed(() => {
  return route.path === '/upload-data' || route.path === '/model-training'
})

const logoutError = ref('')

async function handleLogout() {
  logoutError.value = ''
  try {
    await userStore.logout()
  } catch (cause) {
    logoutError.value = cause instanceof Error ? cause.message : 'Не удалось выйти.'
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.sidebar-glass {
  @include custom-scrollbar;

  flex-shrink: 0;
  overflow-y: auto;
  width: 240px;
  height: 100vh;
  height: 100dvh;
  position: fixed;
  left: 0;
  top: 0;
  
  display: flex;
  flex-direction: column;
  padding: 24px;
  box-sizing: border-box;

  background: rgba($color-glass-white, 0.35);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba($color-glass-white, 0.5);
  z-index: 100;
}

.sidebar-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: flex-start;

  .logo-link {
    display: flex;
    align-items: center;
    gap: 10px;
    text-decoration: none;
    transition: opacity 0.2s ease;

    &:hover {
      opacity: 0.8;
    }
  }

  .nii-text {
    font-size: 1.3rem;
    font-weight: 700;
    color: $color-white;
    line-height: 1.2;
  }
}

.divider {
  width: 100%;
  height: 0;
  border-radius: 10px;
  border-top: 2px solid rgba($color-glass-white, 0.7);
  margin-top: 24px;
  margin-bottom: 32px;
}


.main-menu {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.menu-item {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;

  text-decoration: none;
  color: $color-white;
  font-weight: 500;
  font-size: 0.95rem;

  .icon {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;

    svg {
      path {
        stroke: none; 
        fill: currentColor;
      }
    }
  }

  &:hover, &.router-link-active {
    color: $color-accent;
    padding-left: 8px;
    padding-block: 4px;
    background-color: $color-white;
    border-radius: 10px;
  }
}

.menu-label {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.menu-item:focus-visible,
.logout-btn:focus-visible {
  @include focus-ring;
}

.sidebar-footer {
  flex-shrink: 0;
  margin-top: auto; // Прижимает футер к низу
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}

.user-profile {
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
  color: rgba($color-glass-white, 0.7);
  .username {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: $color-white;
    padding-left: 4px;
  }
  font-weight: 500;

  .logout-btn {
  background: none;
  border: none;
  color: rgba($color-glass-white, 0.7);
  cursor: pointer;
  padding-left: 4px;
  display: flex;
  align-items: center;
  transition: color 0.2s;
  &:hover { color: $color-cold-red; }
    svg {
      fill: none;
      stroke-width: 2px;
    }
  }
}

.user-profile > .icon,
.user-profile > .logout-btn {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .sidebar-glass {
    align-self: flex-start;
    position: relative;
    width: 100%;
    height: auto;
    padding: 20px;
    border-right: 0;
    border-bottom: 1px solid rgba($color-white, 0.5);
  }

  .divider {
    margin: 16px 0;
  }

  .main-menu {
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .sidebar-header {
    justify-content: center;
  }

  .logo-link {
    max-width: 100%;
  }

  .menu-item,
  .menu-item:hover,
  .menu-item.router-link-active {
    max-width: 100%;
    min-height: 44px;
    padding: 8px 12px;
    border-radius: 10px;
    justify-content: center;
    transition: color 0.2s ease, background-color 0.2s ease;
  }

  .menu-label {
    white-space: normal;
    overflow: visible;
    overflow-wrap: anywhere;
    text-overflow: clip;
  }

  .sidebar-footer {
    margin: 24px 0 0;
    align-items: center;
  }

  .user-profile {
    width: max-content;
    max-width: 100%;
    justify-content: center;
    gap: 12px;
  }

  .retrain-model {
    align-self: center;
    width: 100%;
    max-width: 560px;
    text-align: center;
  }
}

@media (max-width: 560px) {
  .menu-item,
  .menu-item:hover,
  .menu-item.router-link-active {
    justify-content: center;
    min-width: 44px;
    min-height: 44px;
    padding: 10px;
    box-sizing: border-box;
    border-radius: 10px;
  }

  .menu-label {
    display: none;
  }

}

.retrain-model {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  padding: 12px;
  gap: 10px;
  background-color: $color-white;
  border-radius: 10px;

  .retrain-text {
    font-size: 0.85rem;
    font-weight: 400;
    color: $color-blue-dark;
  }
  .retrain-btn {
    background-color: $color-accent;
    color: $color-white;
    border: none;
    align-self: center;
    text-align: center;
    font-size: 1rem;
    font-weight: 600;
    width: 100%;
    padding: 8px;
    border-radius: 6px;

    &:hover {
      background-color: $color-accent-lighter;
      cursor: pointer;
    }
  }
}


</style>
