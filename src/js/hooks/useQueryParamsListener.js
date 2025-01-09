import { useEffect, useMemo } from 'react';

import { useLocation } from 'react-router-dom';

// Hook calling callback passed as an argument on changes in specified params
const useQueryParamsListener = ({ callback, params }) => {
  const { search } = useLocation();

  const query = useMemo(() => new URLSearchParams(search), [search]);

  const paramsDependencies = useMemo(
    () =>
      params.map((param) => query.get(param)).join('|'),
    [query, params],
  );

  useEffect(() => {
    callback();
  }, [paramsDependencies]);
};

export default useQueryParamsListener;
