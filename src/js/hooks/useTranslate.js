import { useSelector } from 'react-redux';
import { getAppTranslate } from 'selectors';

/**
 * Custom hook for accessing the translation function.
 * @returns {TranslationCallback} The translation function.
 */
const useTranslate = () => useSelector(getAppTranslate);

export default useTranslate;
