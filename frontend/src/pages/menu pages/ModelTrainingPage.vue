<template>
  <sidebar/>
  
</template>

<script setup lang="ts">

import Sidebar from '@/components/Sidebar.vue'

</script>

<!-- 
Я там запушил в ml-integration рабочие эндпоинты на предсказание и переобучение.
Теперь /predict выдает ответ в следующем виде:
{
  "predictions": [
    "ah:0.5439886275142737",
    "angina:0.8225585300362147",
    "arrhythmia_or_ihd:0.3869375505439214",
    "general:0.9611948506174232",
    "heart_failure:0.8080380413220196",
    "mi:0.4994191252180964",
    "stroke:0.257937752275411"
  ],
  "model_version": "1.0.0"
}
/retrain на вход требует на вход csv'шку, с теми же колонками, что я кидал. ответ подобный:
{
  "status": "ok",
  "models_dir": "/home/parli/PycharmProjects/webdev-coursework-cardiocenter/ml/app/models",
  "saved_models": [
    "ah_model.cbm",
    "angina_model.cbm",
    "arrhythmia_or_ihd_model.cbm",
    "general_model.cbm",
    "heart_failure_model.cbm",
    "mi_model.cbm",
    "stroke_model.cbm"
  ],
  "metrics_json": "/home/parli/PycharmProjects/webdev-coursework-cardiocenter/ml/app/models/metrics.json",
  "metrics_csv": "/home/parli/PycharmProjects/webdev-coursework-cardiocenter/ml/app/models/metrics.csv"
}
-->

<style scoped lang="scss">
@use '@/styles/variables' as *;


</style>
