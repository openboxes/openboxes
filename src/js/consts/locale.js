export const Locale = {
  AR: 'ar',
  ACH: 'ach',
  DE: 'de',
  EN: 'en',
  ES: 'es',
  ES_MX: 'es_MX',
  FR: 'fr',
  HT: 'ht',
  IT: 'IT',
  PT: 'pt',
  FI: 'fi',
  ZH: 'zh',
};

// Used for mapping locales between libraries that are not supporting codes like es_MX
export const LocaleConverter = {
  ...Locale,
  ES_MX: 'es',
};
