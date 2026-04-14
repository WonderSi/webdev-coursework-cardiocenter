import type { SurveyConfig } from '@/stores/survey.store'

export const SURVEY_CONFIG: SurveyConfig = {
  groups: [
    {
      id: 1,
      title: 'Укажите, пожалуйста, ваш пол и возраст:',
      requiredFields: ['gender', 'age'],
      fields: [
        {
          key: 'gender',
          type: 'radio',
          label: 'Пол',
          horizontal: true,
          options: [
            { valueId: 1, label: 'Мужской' },
            { valueId: 2, label: 'Женский' },
          ],
        },
        {
          key: 'age',
          type: 'number',
          label: 'Возраст',
          unit: 'лет',
          placeholder: '27',
        },
      ],
    },
    {
      id: 2,
      title: 'Укажите, пожалуйста, ваши антропометрические параметры:',
      requiredFields: ['height', 'weight'],
      fields: [
        {
          key: 'height',
          type: 'number',
          label: 'Рост',
          unit: 'см',
          placeholder: '165',
        },
        {
          key: 'weight',
          type: 'number',
          label: 'Вес',
          unit: 'кг',
          placeholder: '60',
        },
        {
          key: 'hipMeasurement',
          type: 'number',
          label: 'Объём бёдер',
          unit: 'см',
          placeholder: '90',
        },
      ],
    },
    {
      id: 3,
      title: 'Употребляете ли вы алкоголь?',
      requiredFields: ['alcohol'],
      fields: [
        {
          key: 'alcohol',
          type: 'radio',
          label: 'Алкоголь',
          vertical: true,
          options: [
            { valueId: 1, label: 'Нет' },
            { valueId: 2, label: 'Употреблял ранее' },
            { valueId: 3, label: 'Да, в настоящее время' },
          ],
        },
      ],
    },
    {
      id: 4,
      title: 'Выберите ваш род деятельности',
      requiredFields: ['profession'],
      fields: [
        {
          key: 'profession',
          type: 'radio',
          label: 'Профессия',
          vertical: true,
          options: [
            { valueId: 1, label: 'Ведение домашнего хозяйства' },
            { valueId: 2, label: 'Вооружённые силы' },
            { valueId: 3, label: 'Лица свободных профессий' },
            { valueId: 4, label: 'Низкоквалифицированные работники' },
            { valueId: 5, label: 'Операторы и монтажники' },
            { valueId: 6, label: 'Служащие, сфера обслуживания' },
            { valueId: 7, label: 'Никогда не работающие домохозяйки' },
            { valueId: 8, label: 'Дипломированные специалисты' },
            { valueId: 9, label: 'Другое' },
            { valueId: 10, label: 'Сельское хозяйство и рыболовство' },
            { valueId: 11, label: 'Пенсионеры' },
            { valueId: 12, label: 'Ремесленники' },
            { valueId: 13, label: 'Техники и младшие специалисты' },
            { valueId: 14, label: 'Руководители и должностные лица' },
          ],
        },
      ],
    },
    {
      id: 5,
      title: 'Где вы проживаете?',
      requiredFields: ['region'],
      fields: [
        {
          key: 'region',
          type: 'radio',
          label: 'Район',
          vertical: true,
          options: [
            { valueId: 1, label: 'Рудничный' },
            { valueId: 2, label: 'Центральный' },
            { valueId: 3, label: 'Заводской' },
            { valueId: 4, label: 'Кировский' },
            { valueId: 5, label: 'Ленинский' },
            { valueId: 6, label: 'Сельская местность' },
          ],
        },
      ],
    },
    {
      id: 6,
      title: 'Ваши лабораторные параметры',
      requiredFields: [],
      fields: [
        { key: 'glucose', type: 'number', label: 'Глюкоза', unit: 'ммоль/л', placeholder: '4,5' },
        { key: 'cholesterol', type: 'number', label: 'Холестерин', unit: 'ммоль/л', placeholder: '5,3' },
        { key: 'nonHdlCholesterol', type: 'number', label: 'Холестерин не-ЛПВП', unit: 'ммоль/л', placeholder: '3,2' },
        { key: 'vldlCholesterol', type: 'number', label: 'Холестерин ЛПОНП', unit: 'ммоль/л', placeholder: '0,8' },
        { key: 'hdlCholesterol', type: 'number', label: 'Холестерин ЛПВП', unit: 'ммоль/л', placeholder: '1,2' },
        { key: 'ldlCholesterol', type: 'number', label: 'Холестерин ЛПНП', unit: 'ммоль/л', placeholder: '3,0' },
        { key: 'apolipoproteinA', type: 'number', label: 'Аполипопротеин А (Апо А)', unit: 'г/л', placeholder: '1,5' },
        { key: 'apolipoproteinB', type: 'number', label: 'Аполипопротеин B (Апо B)', unit: 'г/л', placeholder: '0,9' },
        { key: 'triglycerides', type: 'number', label: 'Триглицериды', unit: 'ммоль/л', placeholder: '1,7' },
      ],
    },
    {
      id: 7,
      title: 'Хронические заболевания и диагнозы',
      requiredFields: [],
      fields: [
        { key: 'stroke', type: 'yesno', label: 'Диагностировался ли инсульт?', yearKey: 'strokeYear' },
        { key: 'heartFailure', type: 'yesno', label: 'Диагностировалась ли сердечная недостаточность (СН)?', yearKey: 'heartFailureYear' },
        { key: 'cad', type: 'yesno', label: 'Диагностировалось ли нарушение ритма или другие ишемические болезни сердца (ИБС)?', yearKey: 'cadYear' },
        { key: 'angina', type: 'yesno', label: 'Диагностировалась ли стенокардия?', yearKey: 'anginaYear' },
        { key: 'myocardialInfarction', type: 'yesno', label: 'Диагностировался ли инфаркт миокарда?', yearKey: 'myocardialInfarctionYear' },
        { key: 'arterialHypertension', type: 'yesno', label: 'Диагностировалась ли артериальная гипертензия (АГ)?', yearKey: 'arterialHypertensionYear' },
      ],
    },
  ],
}
