const formatNumber = (value, locale = 'en-US') =>
  new Intl.NumberFormat(locale).format(value);

export default formatNumber;
