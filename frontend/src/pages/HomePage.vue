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
         <div class="welcome-card">
            <div class="welcome-header">
                Добро пожаловать!
            </div>
            <div class="welcome-text">
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
                <button class="start-btn">Начать</button>
                <button class="enter-as-doctor-btn">Войти как врач</button>
         </div>
    </div>
</template>

<script setup lang="ts">
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
  border-radius: $radius-auth-card;

  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
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

// right-side
.welcome-card {
  flex: 0 0 420px;
  display: flex;
  flex-direction: column;
  gap: 16px;

  background: $color-white;
  border-radius: $radius-welcome-card;
  padding: 32px 32px 24px 32px;
  filter: drop-shadow(0 5 20 #070E2C, 0.2); // поправить
}

.welcome-header {
  font-size: 1.5rem;
  font-weight: 700;
  color: $color-text;
  text-align: center;
}

.welcome-text {
  display: flex;
  flex-direction: column;
  gap: 12px;

  p {
    font-size: 0.9rem;
    line-height: 1.6;
    color: $color-text;
    text-align: justify;
  }
}

// buttons

.start-btn {
  width: 100%;
  padding: 16px;
  border: none;
  border-radius: $radius-rect-buttons;
  background: $color-accent;
  color: #ffffff;
  font-size: 1rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.2s ease;

  &:hover {
    background: $color-accent-lighter;
  }
}

.enter-as-doctor-btn {
  width: 100%;
  padding: 10px;
  border: none;
  background: transparent;
  color: $color-accent;
  font-size: 0.9rem;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 0.7;
  }
}
</style>