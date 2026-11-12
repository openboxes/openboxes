import { useState } from 'react';

import queryString from 'query-string';
import { useSelector } from 'react-redux';
import { useLocation, useNavigate } from 'react-router-dom';
import { getCurrentLocation } from 'selectors';

import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import useOnLocationChange from 'hooks/useOnLocationChange';
import useSpinner from 'hooks/useSpinner';
import { transformFilterParams } from 'utils/list-utils';

const useExpirationHistoryReportFilters = ({ filterFields }) => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [shouldFetch, setShouldFetch] = useState(false);

  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();

  const spinner = useSpinner();

  const currentLocation = useSelector(getCurrentLocation);

  const clearFilterValues = () => {
    const { pathname } = location;
    navigate({ pathname });
    setShouldFetch(false);
  };

  const initializeDefaultFilterValues = async () => {
    spinner.show();
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    const queryProps = queryString.parse(window.location.search);

    defaultValues.location = {
      id: currentLocation?.id,
      value: currentLocation?.id,
      name: currentLocation?.name,
      label: currentLocation?.name,
    };

    if (queryProps.startDate) {
      defaultValues.startDate = queryProps.startDate;
    }

    if (queryProps.endDate) {
      defaultValues.endDate = queryProps.endDate;
    }

    setDefaultFilterValues(defaultValues);

    setFiltersInitialized(true);

    if (queryProps.startDate && queryProps.endDate) {
      setShouldFetch(true);
    }

    spinner.hide();
  };

  // Custom hook for changing location/filters rebuilding logic
  useCommonFiltersCleaner({ filtersInitialized, initializeDefaultFilterValues, clearFilterValues });

  const setFilterValues = (values) => {
    const filterAccessors = {
      startDate: { name: 'startDate' },
      endDate: { name: 'endDate' },
    };
    const transformedParams = transformFilterParams(values, filterAccessors);
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = location;
    if (Object.keys(values).length) {
      navigate({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  };

  useOnLocationChange(() => setFilterParams({}));

  return {
    shouldFetch,
    filtersInitialized,
    setShouldFetch,
    defaultFilterValues,
    setFilterValues,
    filterParams,
  };
};

export default useExpirationHistoryReportFilters;
