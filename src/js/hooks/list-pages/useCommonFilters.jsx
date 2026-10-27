import { useState } from 'react';

import { useDispatch } from 'react-redux';
import { useLocation, useNavigate } from 'react-router-dom';

const useCommonFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();

  return {
    filterParams,
    setFilterParams,
    defaultFilterValues,
    setDefaultFilterValues,
    filtersInitialized,
    setFiltersInitialized,
    navigate,
    location,
    dispatch,
  };
};

export default useCommonFilters;
