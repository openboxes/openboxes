const formatCurrency = (value, locale = 'en-US', currency = 'USD') => new Intl.NumberFormat(locale, {
  style: 'currency',
  currency,
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
}).format(value);

export default formatCurrency;
