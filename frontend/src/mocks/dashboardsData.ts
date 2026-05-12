export const mockKpiData = {
  totalRecords: 1240  // всего записей пациентов в БД
};

// бьём пациентов на группы по возрасту,
// в каждой группе находим число мужчин БЕЗ ЕДИНОГО ДИАГНОЗА + число мужчин с хотя бы одним диагнозом
// и также находим общее число БЕЗ ЕДИНОГО ДИАГНОЗА женщин + число женщин с хотя бы одним диагнозом
export const mockAgeGenderData = [
  { ageGroup: '<30', healthyMale: 45, diagnosedMale: 20, healthyFemale: 30, diagnosedFemale: 10 },
  { ageGroup: '30-39', healthyMale: 45, diagnosedMale: 20, healthyFemale: 30, diagnosedFemale: 10 },
  { ageGroup: '40-49', healthyMale: 45, diagnosedMale: 20, healthyFemale: 30, diagnosedFemale: 10 },
  { ageGroup: '50-59', healthyMale: 45, diagnosedMale: 20, healthyFemale: 30, diagnosedFemale: 10 },
  { ageGroup: '60-69', healthyMale: 45, diagnosedMale: 20, healthyFemale: 30, diagnosedFemale: 10 },
  { ageGroup: '70+', healthyMale: 45, diagnosedMale: 20, healthyFemale: 30, diagnosedFemale: 10 },
];

// сколько диагностировано (мужчин/женщин) и процент от общего числа мужчин/женщин
export const mockGenderDiagnosesData = [
    { maleDiagnosed: 100, malePercent: 10 },
    { femaleDiagnosed: 80, femalePercent: 20 }
];

// для каждого пациента считаем ИМТ ( weight / height^2 в метрах )
// пример: 65кг 170 см => 65 : 1,7² = 22,5 (округляем до одной цифры после ,)
// подсчитываем сколько в каждой группе и выдаём сюда
export const mockWeightData = [
    { category: 'Недостаточный', healthyCount: 10, diagnosedCount: 5, diagnosedPercent: 50 }, // дефицит: < 18,5
    // категория, кол-во пациентов без единого диагноза, кол-во пациентов с хотя бы одним диагнозом, процент диагностированных от общего числа пациентов
    { category: 'Нормальный', healthyCount: 20, diagnosedCount: 12, diagnosedPercent: 60 },   // норм. вес: 18,5 - 24,9
    { category: 'Избыточный', healthyCount: 10, diagnosedCount: 4, diagnosedPercent: 40 },   // избыточный вес: 25 - 29,9
    { category: 'Ожирение', healthyCount: 5, diagnosedCount: 2, diagnosedPercent: 50 }        // ожирение: 30+
];

// "употребляете ли вы алкоголь"
export const mockAlcoholConsumptionData = [
    { category: 'Употребляю', healthyCount: 100, diagnosedCount: 50, diagnosedPercent: 50 },
    { category: 'Употреблял ранее', healthyCount: 400, diagnosedCount: 230, diagnosedPercent: 55 },
    { category: 'Никогда не употреблял', healthyCount: 200, diagnosedCount: 10, diagnosedPercent: 0.5 }
]; // сколько здоровых (без единого ССЗ), сколько больных (1 и более ССЗ), процент диагностированных

// сначала группируем данные по профессими
// затем в каждой группе смотрим число заболеваний (где им диагностировали хотя бы одно ССЗ)
// выводим топ-5 групп с наибольшей заболеваемостью ССЗ
// имя профессии, число диагностированных, % диагностированных от общего числа человек
export const mockProfessionData = [
    { profName: "Профессия 1", diagnosedCount: 50, diagnosedPercent: 50 },
    { profName: "Профессия 2", diagnosedCount: 50, diagnosedPercent: 50 },
    { profName: "Профессия 3", diagnosedCount: 50, diagnosedPercent: 50 },
    { profName: "Профессия 4", diagnosedCount: 50, diagnosedPercent: 50 },
    { profName: "Профессия 5", diagnosedCount: 50, diagnosedPercent: 50 }
]; // ВАЖНО!!!!! если у нас мало данных (т.е. внутри профессии у нас всего лишь 10 записей и у 8 из них)
// заболевания, то это слишком малая выборка и её мы в рейтинг не включаем.
// вопрос какой threshold (порог) поставить в данном случае после которого мы не отсеиваем

// считаем по каждому диагнозу у скольких людей (строк в бд)
// он был диагностирован, сортируем по убыванию (descending)
// и выдаём сюда в топ: имя болезни (маппинг словаря с бека), число диагностированных + процент от общего числа диагнозов
export const mockMostDiagnosedData = [
    { diagName: "ССЗ 1", diagnosedCount: 10, percent: 10 }, // все варики:
    { diagName: "ССЗ 1", diagnosedCount: 20, percent: 20 }, // инсульт,
    { diagName: "ССЗ 1", diagnosedCount: 10, percent: 10 }, // сердечная недостаточность,
    { diagName: "ССЗ 1", diagnosedCount: 10, percent: 10 }, // ишемические болезни сердца,
    { diagName: "ССЗ 1", diagnosedCount: 10, percent: 10 }, // стенокардия,
    { diagName: "ССЗ 1", diagnosedCount: 40, percent: 40 }  // инфаркт миокарда,  
];                                                          // артериальная гипертензия

export const mockDistrictData = [
  { name: 'Рудничный', numberOfDiagnoses: 1234, EcologyData: 1000 },
  { name: 'Центральный', numberOfDiagnoses: 1234, EcologyData: 1000 },
  { name: 'Ленинский', numberOfDiagnoses: 1234, EcologyData: 1000 },
  { name: 'Кировский', numberOfDiagnoses: 1234, EcologyData: 1000 },
  { name: 'Заводский', numberOfDiagnoses: 1234, EcologyData: 1000 },
  { name: 'Сельск. местн.', numberOfDiagnoses: 1234, EcologyData: 1000 }
]; // подсчитываем число диагностированных по каждому району +
   // связываем это с данными об экологии, но пока непонятно что будет в "данные об экологии", какой-то параметр

export const mockLabParams = [
    // ...?
];
// потом надо придумать как лабораторные данные приплести