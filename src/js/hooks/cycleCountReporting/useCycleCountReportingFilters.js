import { useState } from 'react';

import queryString from 'query-string';
import { useDispatch } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { setShouldRebuildFilterParams } from 'actions';
import cycleCountReportingFilterFields
  from 'components/cycleCountReporting/CycleCountReportingFilterFields';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import { transformFilterParams } from 'utils/list-utils';

const useCycleCountReportingFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [createdBefore] = useState(null);
  const [createdAfter] = useState(null);
  const [filtersInitialized, setFiltersInitialized] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const dispatch = useDispatch();
  const history = useHistory();

  const clearFilterValues = () => {
    const queryProps = queryString.parse(history.location.search);
    const defaultValues = Object.keys(cycleCountReportingFilterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), { tab: queryProps.tab });
    const transformedParams = transformFilterParams(defaultValues, {
      tab: { name: 'tab' },
    });
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = history.location;
    history.push({ pathname, search: queryFilterParams });
  };

  const resetForm = () => {
    clearFilterValues();
    dispatch(setShouldRebuildFilterParams(true));
  };

  const initializeDefaultFilterValues = async () => {
    setIsLoading(true);
    try {
      const queryProps = queryString.parse(history.location.search);
      const defaultValues = {
        tab: queryProps.tab,
        createdBefore: queryProps.createdBefore || '',
        createdAfter: queryProps.createdAfter || '',
      };

      setDefaultFilterValues(defaultValues);
      setFiltersInitialized(true);
    } finally {
      setIsLoading(false);
    }
  };

  useCommonFiltersCleaner({
    filtersInitialized,
    initializeDefaultFilterValues,
    clearFilterValues,
  });

  const setFilterValues = (values) => {
    const filterAccessors = {
      createdBefore: { name: 'createdBefore' },
      createdAfter: { name: 'createdAfter' },
      tab: { name: 'tab' },
    };
    const transformedParams = transformFilterParams(values, filterAccessors);
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = history.location;
    if (Object.keys(values).length) {
      history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  };

  return {
    defaultFilterValues,
    setFilterValues,
    resetForm,
    filterParams,
    createdBefore,
    createdAfter,
    isLoading,
  };
};

export default useCycleCountReportingFilters;
