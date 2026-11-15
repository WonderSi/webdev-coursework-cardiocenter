<script setup lang="ts">
import CloseButton from '@/components/CloseButton.vue'
import DoctorLockIcon from './DoctorLockIcon.vue'
import DoctorAccessDetails from './DoctorAccessDetails.vue'
import { accessGuide } from '@/config/doctor-access'

const emit = defineEmits<{ close: [] }>()
</script>

<template>
  <aside
    id="doctors-page-help"
    class="info-card"
    aria-label="Справка по управлению учётными записями"
  >
    <CloseButton
      class="info-close-btn"
      label="Закрыть справку"
      @click="emit('close')"
    />
    <!-- 1. Поля учётной записи -->
    <section class="info-card-section" aria-labelledby="account-fields-title">
      <h2 id="account-fields-title" class="info-card-header">Поля учётной записи</h2>
      <div class="info-card-content">
        <p>Учётная запись состоит из трёх полей для заполнения:</p>
        <dl class="account-field-list">
          <div class="account-field-row">
            <dt class="field-example">Имя</dt>
            <dd>
              <strong>Имя</strong>
              — то, что видите только вы. Можно указать ФИО сотрудника для удобства.
            </dd>
          </div>
          <div class="account-field-row">
            <dt class="field-example">Email (логин)</dt>
            <dd>
              <strong>Email</strong>
              — адрес почты сотрудника для входа в систему.
            </dd>
          </div>
          <div class="account-field-row">
            <dt class="field-example">Пароль</dt>
            <dd>
              <strong>Пароль</strong>
              — используется сотрудником для входа в комбинации с логином.
            </dd>
          </div>
        </dl>
        <p>
          Так, данные для входа: email и пароль. Email должен быть
          <u>уникальным</u>
          .
        </p>
      </div>
    </section>
    <div class="info-card-divider" aria-hidden="true"></div>
    <!-- 2. Права доступа: каркас трёх колонок для дальнейшей доработки -->
    <section class="info-card-section" aria-labelledby="account-access-title">
      <h2 id="account-access-title" class="info-card-header">Права доступа</h2>
      <div class="info-card-content">
        <p>Есть три уровня прав доступа. Они определяют, какие возможности доступны сотруднику.</p>
        <div class="access-levels-grid">
          <article
            v-for="level in accessGuide"
            :key="level.level"
            class="access-level"
          >
            <DoctorAccessDetails :level="level.level" />
          </article>
        </div>
        <p class="info-note">Управление врачами доступно только суперадминистратору.</p>
      </div>
    </section>
    <div class="info-card-divider" aria-hidden="true"></div>
    
    <!-- 3. Блокировка пользователя -->
    <section class="info-card-section" aria-labelledby="account-block-title">
      <h2 id="account-block-title" class="info-card-header">Блокировка пользователя</h2>
      <div class="info-card-content">
        <p>
          Блокировка временно закрывает пользователю вход в систему. Учётная запись и данные сохраняются.
        </p>
        <div class="lock-examples">
          <p class="lock-example-row">
            <span class="lock-example lock-example-active" aria-hidden="true">
              <DoctorLockIcon />
            </span>
            <span>
              <strong>Замок открыт</strong>
              — доступ есть, учётная запись работает.
            </span>
          </p>
          <p class="lock-example-row">
            <span class="lock-example lock-example-blocked" aria-hidden="true">
              <DoctorLockIcon locked />
            </span>
            <span>
              <strong>Замок закрыт</strong>
              — доступ закрыт, учётная запись заблокирована.
            </span>
          </p>
        </div>
      </div>
    </section>
  </aside>
</template>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.info-card {
  position: relative;
  margin-bottom: 24px;
  padding: 20px;
  border-radius: 16px;
  background: $color-white;
  color: $color-blue-dark;
  font-size: 0.8125rem;
  line-height: 1.5;
}

.info-card-section {
  margin-inline: 16px;
}

.info-close-btn {
  position: absolute;
  top: 8px;
  right: 8px;
}

.info-card-divider {
  height: 2px;
  margin: 16px 0px;
  background: $color-accent-lighter;
}

.info-card-header {
  margin-bottom: 6px;
  padding-right: 28px;
  font-size: 1.15rem;
  font-weight: 800;
}

.info-card-content {
  padding: 0;
  font-size: 1rem;
}

.account-field-list {
  display: grid;
  gap: 4px;
  margin-block: 6px;
}

.account-field-row {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  align-items: baseline;
  gap: 12px;
}

.field-example {
  justify-self: end;
  padding: 0px 8px;
  border: 1px solid $color-accent;
  border-radius: 7px;
  color: $color-secondary-text;
  font-size: 0.75rem;
  line-height: 1.4;
  text-wrap: nowrap;
}

.access-levels-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  align-items: start;
  gap: 32px;
  margin-block: 20px;
}

.access-level {
  min-width: 0;
}

.info-note {
  margin-top: 12px;
}

.lock-examples {
  display: grid;
  gap: 8px;
  margin-top: 12px;
}

.lock-example-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.lock-example {
  display: grid;
  flex-shrink: 0;
  place-items: center;
  width: 22px;
  height: 22px;
  border-radius: 4px;
}

.lock-example-active {
  background: $color-status-active-bg;
  color: $color-status-active-text;
}

.lock-example-blocked {
  background: $color-status-blocked-bg;
  color: $color-status-blocked-text;
}

@media (max-width: 1100px) {
  .access-levels-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .access-levels-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

@media (max-width: 560px) {
  .account-field-row {
    grid-template-columns: 1fr;
    gap: 6px;
  }

  .field-example {
    justify-self: start;
  }
}
</style>
