<template>
  <div class="form-container">
      <div class="radio-group-vertical">
      <label class="radio-label">
          <input type="radio" :value="14" v-model="form.profession" />
          <span>Представители законодат. органов власти,  высокопосталенные долж. лица, менеджеры и руководители</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="4" v-model="form.profession" />
          <span>Низкоквалифицированные и неквалифицированные работники, рабочие, ручной труд</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="10" v-model="form.profession" />
          <span>Квалифицированные специалисты сельского хозяйства и рыболовного</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="12" v-model="form.profession" />
          <span>Ремесленники и представители других отраслей промышленности</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="5" v-model="form.profession" />
          <span>Операторы и монтажники установок и машинного оборудования</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="6" v-model="form.profession" />
          <span>Служащие, сфера обслуживания, работники среднего звена</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="8" v-model="form.profession" />
          <span>Дипломированные специалисты, умственный труд</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="7" v-model="form.profession" />
          <span>Никогда не работающие домохозяйки</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="13" v-model="form.profession" />
          <span>Техники и младшие специалисты</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="1" v-model="form.profession" />
          <span>Ведение домашнего хозяйства</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="3" v-model="form.profession" />
          <span>Лица свободных профессий</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="2" v-model="form.profession" />
          <span>Вооруженные силы</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="11" v-model="form.profession" />
          <span>Пенсионеры</span>
      </label>
      <label class="radio-label">
          <input type="radio" :value="9" v-model="form.profession" />
          <span>Другое</span>
      </label>
      </div>
  </div>
</template>

<script setup lang="ts">
// Получаем доступ к объекту form из родительского компонента
defineProps<{
  form: Record<string, any>
}>()
</script>
