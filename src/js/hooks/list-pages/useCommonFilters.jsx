import { useState } from 'react';

import { useDispatch } from 'react-redux';
import { useHistory } from 'react-router-dom';

const useCommonFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const history = useHistory();
  const dispatch = useDispatch();

  return {
    filterParams,
    setFilterParams,
    defaultFilterValues,
    setDefaultFilterValues,
    filtersInitialized,
    setFiltersInitialized,
    history,
    dispatch,
  };
};

export default useCommonFilters;
