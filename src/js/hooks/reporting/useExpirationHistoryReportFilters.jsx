import { useEffect, useRef, useState } from 'react';

import queryString from 'query-string';
import { useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { getCurrentLocation } from 'selectors';

import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import useSpinner from 'hooks/useSpinner';
import { transformFilterParams } from 'utils/list-utils';

const useExpirationHistoryReportFilters = ({ filterFields }) => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [shouldFetch, setShouldFetch] = useState(false);

  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const history = useHistory();

  const spinner = useSpinner();

  const currentLocation = useSelector(getCurrentLocation);

  const previousLocation = useRef(currentLocation?.id);

  const clearFilterValues = () => {
    const { pathname } = history.location;
    history.push({ pathname });
    setShouldFetch(false);
  };

  const initializeDefaultFilterValues = async () => {
    spinner.show();
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    const queryProps = queryString.parse(history.location.search);

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
    const { pathname } = history.location;
    if (Object.keys(values).length) {
      history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  };

  // If the user switches to a different location, reset the filters.
  // This ensures that filters from a previous location are cleared
  useEffect(() => {
    // This if statement ensures that when the page loads for the first time,
    // we don't trigger an unnecessary reset so this only runs when the location actually changes
    if (previousLocation.current !== currentLocation?.id) {
      // Reset filter params to have clear filters for the new location
      setFilterParams({});
      // Update the reference to the current location for future checks
      previousLocation.current = currentLocation?.id;
    }
  }, [currentLocation?.id]);

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
