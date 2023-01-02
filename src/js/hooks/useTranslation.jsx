import { useEffect } from 'react';

import { useDispatch, useSelector } from 'react-redux';

import { fetchTranslations } from 'actions';

const useTranslation = (...toTranslate) => {
  const { locale } = useSelector(state => ({
    locale: state.session.activeLanguage,
  }));

  const dispatch = useDispatch();

  useEffect(() => {
    toTranslate.forEach((element) => {
      dispatch(fetchTranslations(locale, element));
    });
  }, [locale]);
};

export default useTranslation;
