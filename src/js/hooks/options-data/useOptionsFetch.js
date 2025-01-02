import { useEffect } from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';

const useOptionsFetch = (fnArray, settings) => {
  const dispatch = useDispatch();

  const {
    currentLocation,
    currentLocale,
  } = useSelector((state) => ({
    currentLocation: state.session.currentLocation,
    currentLocale: state.session.activeLanguage,
  }));

  const refetchOnLocaleChange = _.get(settings, 'refetchOnLocaleChange', true);
  const refetchOnLocationChange = _.get(settings, 'refetchOnLocationChange', true);

  const dependencies = [
    refetchOnLocationChange ? currentLocation : null,
    refetchOnLocaleChange ? currentLocale : null,
  ];

  useEffect(() => {
    const controller = new AbortController();
    const mainConfig = {
      signal: controller.signal,
    };
    fnArray.forEach((fn) => {
      dispatch(fn(mainConfig));
    });

    return () => {
      controller.abort();
    };
  }, dependencies);
};

export default useOptionsFetch;
