import { getTranslate } from 'react-localize-redux';
import { useSelector } from 'react-redux';

import { translateWithDefaultMessage } from 'utils/Translate';

/**
 * Custom hook for accessing the translation function.
 * @returns {TranslationCallback} The translation function.
 */
const useTranslate = () => {
  const { translate } = useSelector((state) => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  return translate;
};

export default useTranslate;
