<template>
  <sidebar/>
  <div class="dashboards-grid-container">

  <!-- ================= СТРОКА 1 (Маленькие карточки, Высота 1) ================= -->

  <!-- Карточка 1: Всего записей -->
   <div class="dashboard-card col-2 row-1">
    <div class="card-header">
      <div class="card-icon">
        <svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M18.3333 26.6667V35H8.33333C7.49237 35.0003 6.68239 34.6827 6.06575 34.1108C5.44912 33.539 5.07141 32.7553 5.00833 31.9167L5 31.6667V26.6667H18.3333ZM35 26.6667V31.6667C35.0003 32.5076 34.6827 33.3176 34.1108 33.9342C33.539 34.5509 32.7553 34.9286 31.9167 34.9917L31.6667 35H21.6667V26.6667H35ZM18.3333 16.6667V23.3333H5V16.6667H18.3333ZM35 16.6667V23.3333H21.6667V16.6667H35ZM18.3333 5V13.3333H5V8.33333C4.99973 7.49237 5.31734 6.68239 5.88916 6.06575C6.46098 5.44912 7.24474 5.07141 8.08333 5.00833L8.33333 5H18.3333ZM31.6667 5C32.5507 5 33.3986 5.35119 34.0237 5.97631C34.6488 6.60143 35 7.44928 35 8.33333V13.3333H21.6667V5H31.6667Z" fill="#242942"/>
        </svg>
      </div>
      <div class="card-title">Всего записей о пациентах</div>
    </div>
    <span class="card-number">{{databaseMock.amountOfRecords}}</span>
    <div class="last-update">
        <div class="dynamics">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M4 12L12 4M12 4H6.5M12 4V9.5" stroke="#04A040" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          <span class="dynamics-span">{{ databaseMock.dynamic }}</span>
        </div>
        <span class="last-update-date">{{databaseMock.lastUpdateDate}}</span>
      </div>
    </div>

  <!-- Карточка 2: Соотношение полов -->
  <div class="dashboard-card col-2 row-1">
    <div class="card-header">
      <div class="card-icon">
        <svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12.5001 3.33337C13.3841 3.33337 14.232 3.68456 14.8571 4.30968C15.4822 4.93481 15.8334 5.78265 15.8334 6.66671C15.8334 7.55076 15.4822 8.39861 14.8571 9.02373C14.232 9.64885 13.3841 10 12.5001 10C11.616 10 10.7682 9.64885 10.1431 9.02373C9.51794 8.39861 9.16675 7.55076 9.16675 6.66671C9.16675 5.78265 9.51794 4.93481 10.1431 4.30968C10.7682 3.68456 11.616 3.33337 12.5001 3.33337ZM10.0001 11.6667H15.0001C15.8841 11.6667 16.732 12.0179 17.3571 12.643C17.9822 13.2681 18.3334 14.116 18.3334 15V24.1667H15.8334V36.6667H9.16675V24.1667H6.66675V15C6.66675 14.116 7.01794 13.2681 7.64306 12.643C8.26818 12.0179 9.11603 11.6667 10.0001 11.6667ZM27.5001 3.33337C28.3841 3.33337 29.232 3.68456 29.8571 4.30968C30.4822 4.93481 30.8334 5.78265 30.8334 6.66671C30.8334 7.55076 30.4822 8.39861 29.8571 9.02373C29.232 9.64885 28.3841 10 27.5001 10C26.616 10 25.7682 9.64885 25.1431 9.02373C24.5179 8.39861 24.1667 7.55076 24.1667 6.66671C24.1667 5.78265 24.5179 4.93481 25.1431 4.30968C25.7682 3.68456 26.616 3.33337 27.5001 3.33337ZM25.0001 11.6667H30.0001C30.8841 11.6667 31.732 12.0179 32.3571 12.643C32.9822 13.2681 33.3334 14.116 33.3334 15V24.1667H30.8334V36.6667H24.1667V24.1667H21.6667V15C21.6667 14.116 22.0179 13.2681 22.6431 12.643C23.2682 12.0179 24.116 11.6667 25.0001 11.6667Z" fill="#242942"/>
        </svg>
      </div>
      <div class="card-title">Соотношение полов</div>
    </div>
    <div class="card-two-numbers">
      <div class="percentage">
        <span class="card-number color-blue">58%</span>
        <span class="gender-label color-blue">мужчины</span>
      </div>
      <div class="percentage">
        <span class="card-number color-pink">42%</span>
        <span class="gender-label color-pink">женщины</span>
      </div>
    </div>
  </div>

  <!-- 3. Обучение модели -->

  <!-- 4. Точность модели -->

  <!-- 5. Распределение диагнозов (возраст/пол) - Базовый Bar -->

  <!-- 6. Самые частые болезни (Горизонтальный Bar) -->

  <!-- 7. Связь Возраст / Частота / Пол (Bubble Chart) -->

  <!-- 8. Показатели: Норма/Выше/Ниже (Stacked Bar) На примере Глюкозы -->

  <!-- 10. список районов (Кастомный HTML) -->
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from '@/components/Sidebar.vue'

// --- Настройка Chart.js ---
import { Bar, Bubble, Radar } from 'vue-chartjs'
import { 
  Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, 
  LinearScale, PointElement, LineElement, RadialLinearScale, Filler
} from 'chart.js'

ChartJS.register(
  Title, Tooltip, Legend, BarElement, CategoryScale, 
  LinearScale, PointElement, LineElement, RadialLinearScale, Filler
)

// ==========================================
// MOCK DATA (Имитация ответа от Backend)
// // CONNECT TO BACKEND HERE IN onMounted
// Опираемся на сущности: PatientEntity, DiagnosisEntity, GlossaryValueEntity
// ==========================================

const databaseMock = ref({
  amountOfRecords: 1253,
  lastUpdateDate: '01.05.2026',
  dynamic: 'positive'
})

const mlModelMock = ref({
  lastTrained: '01.05.2026',
  recordsUsed: 12050,
  accuracy: 94.2
})

// --- 5. Диагнозы по возрасту и полу (Bar) ---
// Группировка: PatientEntity.age -> бакеты, PatientEntity.gender -> цвет
const ageGenderData = ref({
  labels:['<30', '30-39', '40-49', '50-59', '60-69', '70+'],
  datasets:[
    { label: 'Мужчины', backgroundColor: '#1567E2', data:[50, 120, 250, 320, 180, 90] },
    { label: 'Женщины', backgroundColor: '#CF4688', data:[40, 100, 210, 350, 200, 110] }
  ]
})
const baseBarOptions = ref({
  responsive: true, maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: { x: { grid: { display: false } }, y: { beginAtZero: true } }
})

// --- 6. Самые частые болезни (Horizontal Bar) ---
// Группировка: DiagnosisEntity.diagnosis (Glossary) -> Count
const topDiseasesData = ref({
  labels:['Артер. Гипертензия', 'ИБС', 'Стенокардия', 'Серд. недостат.', 'Инсульт', 'Инфаркт'],
  datasets:[{
    label: 'Случаев',
    backgroundColor: '#5D8DD3',
    data:[850, 620, 410, 320, 150, 90],
    borderRadius: 4
  }]
})
const horizontalBarOptions = ref({
  indexAxis: 'y', // КЛЮЧЕВАЯ ФИЧА ДЛЯ ГОРИЗОНТАЛЬНОГО ГРАФИКА
  responsive: true, maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: { x: { beginAtZero: true }, y: { grid: { display: false } } }
})

// --- 7. Связь: Возраст (X), Кол-во диагнозов (Y), Популяция (Радиус) (Bubble Chart) ---
// Группировка: PatientEntity.age, Count(DiagnosisEntity)
const bubbleCorrelationData = ref({
  datasets:[
    {
      label: 'Мужчины',
      backgroundColor: 'rgba(21, 103, 226, 0.6)', // $color-accent with opacity
      borderColor: '#1567E2',
      // x: Возраст, y: Среднее кол-во диагнозов на человека, r: размер выборки пациентов
      data:[{x: 35, y: 0.5, r: 10}, {x: 45, y: 1.2, r: 25}, {x: 55, y: 2.8, r: 40}, {x: 65, y: 4.1, r: 35}, {x: 75, y: 5.5, r: 15}]
    },
    {
      label: 'Женщины',
      backgroundColor: 'rgba(207, 70, 136, 0.6)', // $color-pink-women
      borderColor: '#CF4688',
      data:[{x: 35, y: 0.4, r: 8}, {x: 45, y: 1.0, r: 20}, {x: 55, y: 2.5, r: 45}, {x: 65, y: 4.5, r: 40}, {x: 75, y: 6.0, r: 20}]
    }
  ]
})
const bubbleOptions = ref({
  responsive: true, maintainAspectRatio: false,
  plugins: { legend: { display: true, position: 'bottom' } },
  scales: { x: { title: { display: true, text: 'Возраст' } }, y: { title: { display: true, text: 'Ср. кол-во диагнозов' } } }
})

// --- 8. Показатели: Уровень глюкозы по районам (Stacked Bar) ---
// Данные: PatientEntity.glucose (Разбивка: <3.3 Ниже, 3.3-5.5 Норма, >5.5 Выше), сгруппировано по PatientEntity.region
const glucoseStackedData = ref({
  labels:['Рудничный', 'Центр.', 'Ленинский', 'Кировский', 'Заводский'],
  datasets: [
    { label: 'Ниже нормы', backgroundColor: '#8AB3F1', data:[10, 5, 8, 12, 15] },
    { label: 'Норма', backgroundColor: '#04A040', data:[60, 70, 65, 50, 45] },
    { label: 'Выше нормы', backgroundColor: '#EA566F', data: [30, 25, 27, 38, 40] }
  ]
})
const stackedBarOptions = ref({
  responsive: true, maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    x: { stacked: true, grid: { display: false } }, // СТЕКИНГ ВКЛЮЧЕН
    y: { stacked: true, beginAtZero: true }
  }
})

// --- 9. Профессии и диагнозы (Radar Chart) ---
// Оси: Профессии (GlossaryValue). Линии: Вид диагноза. Значение: % больных.
const professionRadarData = ref({
  labels:['Офис', 'Шахтер', 'Водитель', 'Строитель', 'Учитель', 'Врач'],
  datasets:[
    {
      label: 'ИБС',
      backgroundColor: 'rgba(21, 103, 226, 0.2)', borderColor: '#1567E2',
      pointBackgroundColor: '#1567E2',
      data:[30, 60, 55, 40, 25, 35]
    },
    {
      label: 'Гипертензия',
      backgroundColor: 'rgba(234, 86, 111, 0.2)', borderColor: '#EA566F',
      pointBackgroundColor: '#EA566F',
      data:[65, 50, 70, 45, 80, 75]
    }
  ]
})
const radarOptions = ref({
  responsive: true, maintainAspectRatio: false,
  scales: { r: { ticks: { display: false } } }, // Скрываем цифры на паутине для чистоты
  plugins: { legend: { display: true, position: 'bottom' } }
})

// --- 10. Данные для районов (Оставляем как было) ---
const districtData = ref([
  { name: 'Рудничный', numberOfDiagnoses: 1234, EcologyData: 1000 },
  { name: 'Центральный', numberOfDiagnoses: 1234, EcologyData: 1000 },
  { name: 'Ленинский', numberOfDiagnoses: 1234, EcologyData: 1000 },
  { name: 'Кировский', numberOfDiagnoses: 1234, EcologyData: 1000 },
  { name: 'Заводский', numberOfDiagnoses: 1234, EcologyData: 1000 },
  { name: 'Сельск. местн.', numberOfDiagnoses: 1234, EcologyData: 1000 }
])

// API FETCH SIMULATION
// onMounted(() => {
  // CONNECT TO BACKEND
  // axios.get('/api/v1/dashboards/summary').then(...)
// })
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.dashboards-grid-container {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  grid-auto-rows: 160px; // базовая высота ячейки
  min-height: 100vh;
  margin-left: 240px; 
  padding: 40px;
  gap: 24px;
}

// Утилиты
.col-2 { grid-column: span 2; }
.col-3 { grid-column: span 3; }
.col-4 { grid-column: span 4; }
.col-6 { grid-column: span 6; }

.row-1 { grid-row: span 1; }
.row-2 { grid-row: span 2; }

.color-blue { color: $color-accent !important; }
.color-pink { color: $color-pink-women !important; }
.color-green { color: $color-green !important; }

// --- ОБЩИЙ СТИЛЬ ДЛЯ ВСЕХ КАРТОЧЕК
.dashboard-card {
  display: flex;
  flex-direction: column;
  padding: 16px;
  border-radius: $radius-glass-card;
  background: rgba($color-glass-white, 0.6); 
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba($color-glass-white, 0.6);
  box-shadow: 0 4px 24px rgba(7, 14, 44, 0.05);
}

// --- СТИЛИ ДЛЯ МАЛЕНЬКИХ ВЕРХНИХ КАРТОЧЕК
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.card-icon {
  width: 40px;
  height: px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-title {
  color: $color-text;
  font-size: 1.25rem;
  font-weight: 600;
}

.card-number {
  font-size: 3.5rem;
  font-weight: 700;
  color: $color-text;
  line-height: 1;
}

.last-update {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.dynamics {
  display: flex;
  align-items: center;
  gap: 4px;
  
  .dynamics-span {
    color: $color-green;
    font-weight: 600;
    font-size: 1.1rem;
  }
}

.last-update-date {
  font-size: 0.9rem;
  color: $color-blue-med;
  font-weight: 500;
}

.card-two-numbers {
  display: flex;
  justify-content: space-around;
  margin-top: auto;
}

.percentage {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;

  .card-number {
    font-size: 3rem;
  }

  .gender-label {
    font-size: 1rem;
    font-weight: 500;
  }
}

</style>