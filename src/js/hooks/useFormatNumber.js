import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import { Locale, LocaleConverter } from 'consts/locale';

const useFormatNumber = () => {
  const currentLocale = useSelector(getCurrentLocale);

  return (value, locale = currentLocale || Locale.EN) => {
    const normalizedLocale = LocaleConverter[locale?.toUpperCase?.()] || locale;

    return new Intl.NumberFormat(normalizedLocale).format(value);
  };
};

export default useFormatNumber;
