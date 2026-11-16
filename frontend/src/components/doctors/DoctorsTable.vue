<script setup lang="ts">
import AccessLevelTag from './AccessLevelTag.vue'
import DoctorLockIcon from './DoctorLockIcon.vue'
import type { Doctor, DoctorRow } from '@/types/doctor'

const { rows } = defineProps<{
  rows: readonly DoctorRow[]
}>()
const emit = defineEmits<{
  edit: [doctor: Doctor]
  block: [doctor: Doctor]
}>()
</script>

<template>
  <div
    class="table-container"
    tabindex="0"
    role="region"
    aria-label="Учётные записи врачей"
  >
    <table class="doctors-table">
      <caption class="sr-only">Имя, email для входа, смена пароля, права и статус врачей</caption>
      <thead class="table-head">
        <tr>
          <th scope="col">Врач</th>
          <th scope="col">Email (логин)</th>
          <th scope="col">Пароль</th>
          <th scope="col">Права доступа</th>
          <th scope="col">Статус</th>
          <th scope="col">
            <span class="sr-only">Действия</span>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="{ doctor, displayName, initials, accessLevel } in rows"
          :key="doctor.id"
          class="doctor-row"
          :class="{ 'is-blocked': doctor.isBlocked }"
        >
          <td class="doctor-name-cell" data-label="Врач">
            <div class="doctor-identity">
              <span class="doctor-avatar" aria-hidden="true">{{ initials }}</span>
              <div class="doctor-name-block">
                <span class="doctor-name">{{ displayName }}</span>
              </div>
            </div>
          </td>
          <td data-label="Email (логин)">
            <span class="doctor-login" :title="doctor.email">{{ doctor.email }}</span>
          </td>
          <td class="password-cell" data-label="Пароль">
            <button
              type="button"
              class="row-button password-action"
              :aria-label="`Изменить пароль: ${doctor.name || doctor.email}`"
              @click="emit('edit', doctor)"
            >
              Изменить пароль
            </button>
          </td>
          <td data-label="Права доступа">
            <AccessLevelTag :level="accessLevel" size="compact" />
          </td>
          <td data-label="Статус">
            <button
              type="button"
              class="status-lock-button"
              :class="doctor.isBlocked ? 'status-blocked' : 'status-active'"
              :aria-label="`${doctor.isBlocked ? 'Разблокировать' : 'Заблокировать'} ${doctor.name || doctor.email}`"
              :aria-pressed="doctor.isBlocked"
              :title="doctor.isBlocked ? 'Заблокирован. Восстановить доступ' : 'Активен. Заблокировать'"
              @click="emit('block', doctor)"
            >
              <DoctorLockIcon :locked="doctor.isBlocked" class="status-lock-icon" />
            </button>
          </td>
          <td class="actions-cell">
            <div class="row-actions">
              <button
                type="button"
                class="row-button"
                :aria-label="`Редактировать ${doctor.name || doctor.email}`"
                @click="emit('edit', doctor)"
              >
                Редактировать
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.table-container {
  @include custom-scrollbar;

  position: relative;
  overflow-x: auto;
  // Для предовтращения некорректного отображения в Firefox вставляем скролл сам в себя
  margin: 0 20px 16px;

  &::-webkit-scrollbar-track {
    margin: 0;
  }
}

.doctors-table {
  border-collapse: collapse;
  width: 100%;
  text-align: left;
  font-size: 0.8125rem;
}

.table-head {
  background: rgba($color-accent-lighter2, 0.28);
}

.table-head th {
  padding: 14px 16px;
  font-weight: 500;
  color: $color-secondary-text;
  white-space: nowrap;
}

.doctor-row td {
  padding: 20px 16px;
  border-bottom: 1px solid #e0e6f0;
  vertical-align: middle;
}

.doctors-table th:first-child,
.doctor-row td:first-child {
  padding-left: 28px;
}

.doctors-table th:last-child,
.doctor-row td:last-child {
  padding-right: 28px;
}

.doctor-row:last-child td {
  border-bottom: 0;
}

.doctor-row:hover {
  background: rgba($color-white, 0.5);
}

.doctor-identity {
  display: flex;
  gap: 12px;
  align-items: center;
  min-width: 170px;
}

.doctor-avatar {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  color: $color-accent;
  background: #dfebfe;
  font-size: 1rem;
  font-weight: 600;
}

.is-blocked .doctor-avatar {
  background: #e6e9ef;
  color: $color-secondary-text;
}

.doctor-name-block {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.doctor-name {
  color: $color-blue-dark;
  font-weight: 600;
  line-height: 1.5;
  overflow-wrap: anywhere;
  min-width: 100px;
  max-width: 260px;
}

.doctor-login {
  overflow-wrap: anywhere;
}

.password-cell {
  min-width: 130px;
}

.status-lock-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: 0;
  border-radius: 8px;
  cursor: pointer;
}

.status-lock-icon {
  display: block;
  width: 18px;
  height: 20px;
}

.status-lock-button:hover:not(:disabled) {
  box-shadow: inset 0 0 0 1px currentColor;
}

.status-lock-button:focus-visible {
  @include focus-ring(2px, 3px);
}

.status-lock-button:disabled {
  opacity: 0.55;
  cursor: wait;
}

.status-active {
  background: $color-status-active-bg;
  color: $color-status-active-text;
}

.status-blocked {
  background: $color-status-blocked-bg;
  color: $color-status-blocked-text;
}

.row-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.row-button {
  padding: 4px 0;
  color: $color-accent;
  background: transparent;
  border: 0;
  font: inherit;
  font-size: 0.8125rem;
  cursor: pointer;
}

.row-button:hover {
  text-decoration: underline;
  text-underline-offset: 3px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip-path: inset(50%);
  white-space: nowrap;
  border: 0;
}

.row-button:focus-visible,
.table-container:focus-visible {
  @include focus-ring(3px, 3px);
}

@media (max-width: 1100px) {
  .doctors-table {
    min-width: 900px;
  }
}

@media (max-width: 560px) {
  .table-container {
    margin: 0;
  }

  .doctors-table {
    min-width: 0;
    display: block;
  }

  .table-head {
    position: absolute;
    width: 1px;
    height: 1px;
    overflow: hidden;
    clip-path: inset(50%);
  }

  .doctors-table tbody {
    display: block;
  }

  .doctor-row {
    display: grid;
    grid-template-columns: 1fr;
    padding: 20px;
    border-top: 1px solid $color-ui-divider;
  }

  .doctor-row td,
  .doctor-row td:first-child,
  .doctor-row td:last-child {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    align-items: center;
    padding: 8px 0;
    border: 0;
    max-width: none;
    min-width: 0;
  }

  .doctor-row td[data-label]::before {
    content: attr(data-label);
    color: $color-secondary-text;
    font-size: 0.75rem;
    flex-shrink: 0;
  }

  .doctor-row .doctor-name-cell {
    padding: 0 0 12px;
  }

  .doctor-row .doctor-name-cell::before {
    display: none;
  }

  .doctor-name {
    max-width: none;
  }

  .doctor-identity {
    min-width: 0;
  }

  .doctor-login {
    min-width: 0;
    text-align: right;
  }

  .doctor-row .actions-cell {
    margin-top: 12px;
    padding-top: 16px;
    border-top: 1px solid $color-ui-divider;
  }

  .row-actions {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    gap: 16px;
  }
}
</style>
