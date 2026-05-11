<template>
    <div class="glass-card">

        <!-- left-side -->
        <div class="intro">
          <div class="logo-and-text">
            <div class="logo">
              <img src="@/assets/logo.png" alt="logo"/>
            </div>
            <div class="nii-text">НИИ КПССЗ</div>
          </div>

            <div class="intro-text">
                <div class="intro-header">
                    Прогнозирование<br/> риска ССЗ
                </div>
                <div class="intro-subtext">
                    Потому что ваше сердце заслуживает внимания.<br/>
                    Проверьтесь сегодня — живите спокойно завтра.
                </div>
            </div>
        </div>

        <!-- right-side -->
         <div class="basic-card">
            <div class="basic-card-header">
                Добро пожаловать!
            </div>
            <div class="basic-card-text">
                    <p>
                    <strong>Болезни сердечно-сосудистой системы</strong>
                    остаются ведущей причиной инвалидизации и смертности во всём мире.
                    </p>
                    <p>
                    Предлагаем вам выяснить, <strong>каков Ваш риск развития
                    сердечно-сосудистых заболеваний</strong> на основе ваших медицинских
                    показателей, профессии, района проживания и других факторов.
                    </p>
                    <p>
                    Расчёт будет произведён с использованием методов
                    <strong>искусственного интеллекта.</strong>
                    </p>
                    <p>
                    Перед прохождением опроса желательно иметь при себе медицинскую
                    карту или знать результаты своих обследований.
                    </p>
                </div>
                <button class="basic-card-main-btn" @click="goToSurvey">Начать</button>
                <button class="basic-card-sub-btn"  @click="goToLogin">Войти как врач</button>
         </div>
    </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goToSurvey = () => {
  router.push('/survey')
}
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as *;

.glass-card {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 40px;

  max-width: 1000px;
  width: 90%;
  margin: 0 auto;
  min-height: 50vh;
  max-height: 80vh;

  padding: 48px;
  border-radius: $radius-glass-card;

  background: rgba($color-glass-white, 0.25);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba($color-glass-white, 0.4);
}

// left-side
.intro {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 100px;
  color: #ffffff;
}

.logo-and-text {
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
width: 128px;
height: 128px;
}

.nii-text {
  font-size: 2.5rem;
  font-weight: 600;
  color: $color-white;
}

.intro-header {
  font-size: 2.4rem;
  font-weight: 500;
  line-height: 1.2;
  color: $color-white;
}

.intro-text {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.intro-subtext {
  font-size: 1.2rem;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.85);
}

// right-side in basic-card.scss; buttons too

</style>