import type { SurveyConfig } from '@/stores/survey.store'

export const SURVEY_CONFIG: SurveyConfig = {
  groups: [
    {
      id: 1,
      title: 'Выберите модель и укажите пол и возраст:',
      requiredFields: ['modelKind', 'year', 'gender', 'age'],
      fields: [
        {
          key: 'modelKind', type: 'select', label: 'Модель прогнозирования',
          options: [
            { valueId: 'city', label: 'City — отдельное обследование' },
            { valueId: 'region', label: 'Region — сводные данные' },
          ],
        },
        {
          key: 'year', type: 'select', label: 'Период экологических данных',
          visibleWhen: { key: 'modelKind', value: 'city' },
          options: [2013, 2017, 2019, 2021].map(year => ({ valueId: year, label: String(year) })),
        },
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
          validation: { min: 18, max: 120, integer: true },
        },
      ],
    },
    {
      id: 2,
      title: 'Ваши параметры:',
      requiredFields: ['height', 'weight'],
      fields: [
        {
          key: 'height',
          type: 'number',
          label: 'Рост',
          unit: 'см',
          placeholder: '165',
          validation: { min: 50, max: 250 },
        },
        {
          key: 'weight',
          type: 'number',
          label: 'Вес',
          unit: 'кг',
          placeholder: '60',
          validation: { min: 20, max: 300 },
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
            { valueId: 3, label: 'Употребляю' },
            { valueId: 2, label: 'Употреблял ранее' },
            { valueId: 1, label: 'Никогда не употреблял' },
          ],
        },
      ],
    },
    {
      id: 4,
      title: 'Выберите Ваш основной род деятельности:',
      requiredFields: ['profession'],
      fields: [
        {
          key: 'profession',
          type: 'radio',
          label: 'Профессия',
          vertical: true,
          options: [
            { valueId: 1, label: "Руководители и представители власти" },
            { valueId: 2, label: "Специалисты высшего уровня квалификации" },
            { valueId: 3, label: "Специалисты среднего уровня квалификации" },
            { valueId: 4, label: "Служащие по подготовке и оформлению документации" },
            { valueId: 5, label: "Работники сферы услуг, охраны и продавцы" },
            { valueId: 6, label: "Квалифицированные работники сельского/лесного хозяйства, рыбоводства" },
            { valueId: 7, label: "Рабочие промышленных предприятий, строительства и транспорта" },
            { valueId: 8, label: "Операторы установок и машин, сборщики" },
            { valueId: 9, label: "Неквалифицированные рабочие" },
            { valueId: 10, label: "Военнослужащие" },
            { valueId: 11, label: "Пенсионер" },
            { valueId: 12, label: "Ведение домашнего хозяйства" },
            { valueId: 13, label: "Другое" },
          ],
        },
      ],
    },
    {
      id: 5,
      title: 'Выберите Ваш район проживания:',
      requiredFields: ['region'],
      fields: [
        {
          key: 'region',
          type: 'radio',
          label: 'Район',
          vertical: true,
          options: [
            { valueId: 6, label: 'Сельская местность' },
            { valueId: 2, label: 'Центральный' },
            { valueId: 3, label: 'Заводской' },
            { valueId: 1, label: 'Рудничный' },
            { valueId: 4, label: 'Кировский' },
            { valueId: 5, label: 'Ленинский' },
          ],
        },
      ],
    },
    {
      id: 6,
      title: 'Ваши лабораторные параметры',
      description: 'Заполните только известные значения. Неизвестные показатели можно пропустить.',
      requiredFields: [],
      fields: [
        { key: 'glucose', type: 'number', label: 'Глюкоза', unit: 'ммоль/л', placeholder: '4,5', validation: { min: 0 } },
        { key: 'cholesterol', type: 'number', label: 'Холестерин', unit: 'ммоль/л', placeholder: '5,3', validation: { min: 0 } },
        { key: 'hdlCholesterol', type: 'number', label: 'Холестерин ЛПВП', unit: 'ммоль/л', placeholder: '1,2', validation: { min: 0 } },
        { key: 'ldlCholesterol', type: 'number', label: 'Холестерин ЛПНП', unit: 'ммоль/л', placeholder: '3,0', validation: { min: 0 } },
        { key: 'triglycerides', type: 'number', label: 'Триглицериды', unit: 'ммоль/л', placeholder: '1,7', validation: { min: 0 } },
      ],
    },
    {
      id: 7,
      title: 'Заболевания лёгких',
      requiredFields: ['chronicLungDisease'],
      fields: [
        { key: 'chronicLungDisease', type: 'yesno', label: 'Есть ли хронические заболевания лёгких?' },
      ],
    },
  ],
}
