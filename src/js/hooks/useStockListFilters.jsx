import { useEffect, useState } from 'react';

import queryString from 'query-string';
import { useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import filterFields from 'components/stock-list/FilterFields';
import apiClient from 'utils/apiClient';
import { transformFilterParams } from 'utils/list-utils';

const useStockListFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const [locations, setLocations] = useState([]);
  const history = useHistory();

  const { currentLocation } = useSelector(state => ({
    currentLocation: state.session.currentLocation,
  }));

  const fetchLocationById = async (id) => {
    const response = await apiClient(`/openboxes/api/locations/${id}`);
    return response.data?.data;
  };

  useEffect(() => {
    apiClient.get('/openboxes/api/locations')
      .then((response) => {
        const { data } = response.data;
        setLocations(data);
      });
  }, []);

  const clearFilterValues = () => {
    const { pathname } = history.location;
    history.push({ pathname });
  };

  const initializeDefaultFilterValues = async () => {
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    const queryProps = queryString.parse(history.location.search);

    // SET STATIC DEFAULT VALUES
    if (queryProps.origin) {
      const originLocationsIds = Array.isArray(queryProps.origin)
        ? queryProps.origin
        : [queryProps.origin];
      const fetchedLocations = originLocationsIds.map(fetchLocationById);
      const paramLocations = await Promise.all(fetchedLocations);
      defaultValues.origin = paramLocations.map(({ id, name }) => ({
        id, name, value: id, label: name,
      }));
    }
    if (queryProps.destination) {
      const destinationLocationsIds = Array.isArray(queryProps.destination)
        ? queryProps.destination
        : [queryProps.destination];
      const fetchedLocations = destinationLocationsIds.map(fetchLocationById);
      const paramLocations = await Promise.all(fetchedLocations);
      defaultValues.destination = paramLocations.map(({ id, name }) => ({
        id, name, value: id, label: name,
      }));
    }
    if (queryProps.isPublished) {
      defaultValues.isPublished = queryProps.isPublished === 'true';
    }
    setDefaultFilterValues(defaultValues);
    setFiltersInitialized(true);
  };

  useEffect(() => {
    // Don't clear the query params while doing first filter initialization
    // clear the filters only when changing location, but not refreshing page
    if (filtersInitialized) {
      clearFilterValues();
    }
  }, [currentLocation?.id]);

  useEffect(() => {
    if (currentLocation?.id) {
      initializeDefaultFilterValues();
    }
  }, [currentLocation?.id]);

  const setFilterValues = (values) => {
    const filterAccessors = {
      destination: { name: 'destination', accessor: 'id' },
      origin: { name: 'origin', accessor: 'id' },
      isPublished: { name: 'isPublished' },
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
    defaultFilterValues, setFilterValues, locations, filterParams,
  };
};

export default useStockListFilters;
