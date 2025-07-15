import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

const useFormatNumber = () => {
  const currentLocale = useSelector(getCurrentLocale);

  return (value, locale = currentLocale || 'en-US') =>
    new Intl.NumberFormat(locale).format(value);
};

export default useFormatNumber;
