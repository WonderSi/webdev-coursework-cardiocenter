<template>
  <div class="form-container">
    <div 
      v-for="disease in diseasesMeta" 
      :key="disease.id" 
      class="question-wrapper"
    >
      <div class="question-block">
        <span class="disease-name">{{ disease.name }}</span>
        
        <div class="radio-group-horizontal">
          <label class="radio-label">
            <span>Нет</span>
            <input type="radio" :value="0" v-model="form[disease.keys.selected]" />
          </label>
          <label class="radio-label">
            <span>Да</span>
            <input type="radio" :value="1" v-model="form[disease.keys.selected]" />
          </label>
        </div>

        <Transition name="expand-down">
          <div v-if="form[disease.keys.selected] === 1" class="conditional-block">
            
            <div class="connecting-line-graphic"></div>
            
            <span class="year-label">Год первого диагностирования:</span>
            
            <div class="input-group">
              <input 
                type="number" 
                class="input-field year-input" 
                placeholder="2012" 
                v-model="form[disease.keys.year]" 
                min="1950"
                :max="new Date().getFullYear()"
              />
              <span class="unit">г.</span>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  form: Record<string, any>
}>()

const diseasesMeta = ref([
  {
    id: 1,
    name: 'Диагностировался инсульт:',
    keys: { selected: 'hadStroke', year: 'strokeYear' }
  },
  {
    id: 2,
    name: 'Диагностировалась сердечная недостаточность (СН):',
    keys: { selected: 'hadHeartFailure', year: 'heartFailureYear' }
  },
  {
    id: 3,
    name: 'Диагностировалось нарушение ритма или другие ишемические болезни сердца (ИБС):',
    keys: { selected: 'hadCad', year: 'cadYear' }
  },
  {
    id: 4,
    name: 'Диагностировалась стенокардия:',
    keys: { selected: 'hadAngina', year: 'anginaYear' }
  },
  {
    id: 5,
    name: 'Диагностировался инфаркт миокарда:',
    keys: { selected: 'hadMyocardialInfarction', year: 'myocardialInfarctionYear' }
  },
  {
    id: 6,
    name: 'Диагностировалась артериальная гипертензия (АГ):',
    keys: { selected: 'hadArterialHypertension', year: 'arterialHypertensionYear' }
  }
])
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.question-wrapper {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.question-block {
  display: flex;
  flex-direction: column;
  align-items: left; 
  text-align: left;
  position: relative;
  gap: 16px;
  padding-bottom: 16px;
  padding-left: 80px;
}

.disease-name {
  font-weight: 700;
  color: $color-text;
  font-size: 1rem;
  line-height: 1.3;
}

.radio-group-horizontal {
  justify-content: left;
  padding-left: 32px;
  flex-shrink: 0;
  gap: 24px;
  .radio-label {
    gap: 8px;
  }
}

// Условный блок (год диагностирования)
.conditional-block {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding-left: 32px;
  gap: 12px;
  width: 100%;
  position: relative;
}

// ГРАФИКА: Сплошная линия
.connecting-line-graphic {
  position: absolute;
  top: -64px;
  left: -10px;
  
  width: 32px;
  height: 78px; // Высота линии
  
  border-left: 4px solid $color-accent;
  border-bottom: 4px solid $color-accent; 
  border-bottom-left-radius: 6px;
  
  z-index: 1;
  pointer-events: none;
}

.year-label {
  font-size: 1 rem;
  color: $color-text;
}

.input-group {
  .year-input {
    width: 70px; 
  }
}

// АНИМАЦИИ Vue Transition (expand-down)

.expand-down-enter-active {
  animation: expandBlock 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.expand-down-leave-active {
  animation: expandBlock 0.3s cubic-bezier(0.4, 0, 0.2, 1) reverse forwards;
}

// Анимация блока: он плавно раскрывается по высоте
@keyframes expandBlock {
  0% {
    opacity: 0;
    max-height: 0;
    transform: translateY(-10px);
  }
  100% {
    opacity: 1;
    max-height: 100px;
    transform: translateY(0);
  }
}

// анимация появления линии
.expand-down-enter-active .connecting-line-graphic {
  animation: drawLine 0.5s linear forwards;
}

// анимация исчезновения линии
.expand-down-leave-active .connecting-line-graphic {
  animation: drawLine 0.5s linear reverse forwards;
}

@keyframes drawLine {
  // Линия спрятана "шторкой" снизу
  0% { clip-path: inset(0 0 100% 0); opacity: 0; }
  20% { opacity: 0.3; }
  50% { opacity: 0.5; }
  // Шторка плавно опускается, линия рисуется сверху вниз
  100% { clip-path: inset(0 0 0 0); opacity: 1; }
}
</style>