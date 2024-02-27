import { getTranslate } from 'react-localize-redux';
import { useSelector } from 'react-redux';

import { translateWithDefaultMessage } from 'utils/Translate';

const useTranslate = () => {
  const {
    translate,
  } = useSelector((state) => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  return translate;
};

export default useTranslate;
