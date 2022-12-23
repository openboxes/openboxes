import { useEffect, useState } from 'react';

import queryString from 'query-string';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { fetchShipmentStatusCodes } from 'actions';
import filterFields from 'components/stock-movement/inbound/FilterFields';
import apiClient from 'utils/apiClient';
import { getParamList, transformFilterParams } from 'utils/list-utils';

const useInboundFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const history = useHistory();
  const dispatch = useDispatch();
  const {
    currentLocation, shipmentStatuses, currentUser, isShipmentStatusesFetched,
  } = useSelector(state => ({
    currentLocation: state.session.currentLocation,
    shipmentStatuses: state.shipmentStatuses.data,
    currentUser: state.session.user,
    isShipmentStatusesFetched: state.shipmentStatuses.fetched,
  }));

  useEffect(() => {
    if (!isShipmentStatusesFetched || !shipmentStatuses.length) {
      dispatch(fetchShipmentStatusCodes());
    }
  }, []);
  const fetchUserById = async (id) => {
    const response = await apiClient(`/openboxes/api/generic/person/${id}`);
    return response.data?.data;
  };

  const fetchLocationById = async (id) => {
    const response = await apiClient(`/openboxes/api/locations/${id}`);
    return response.data?.data;
  };

  const clearFilterValues = () => {
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), { direction: 'INBOUND' });
    const transformedParams = transformFilterParams(defaultValues, { direction: { name: 'direction' } });
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = history.location;
    if (queryFilterParams) {
      history.push({ pathname, search: queryFilterParams });
    }
  };

  const initializeDefaultFilterValues = async () => {
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    // SET STATIC DEFAULT VALUES
    defaultValues.direction = 'INBOUND';

    const queryProps = queryString.parse(history.location.search);
    // IF VALUE IS IN A SEARCH QUERY SET DEFAULT VALUES
    if (queryProps.destination === undefined) {
      defaultValues.destination = {
        id: currentLocation?.id,
        value: currentLocation?.id,
        name: currentLocation?.name,
        label: currentLocation?.name,
      };
    }

    if (queryProps.receiptStatusCode) {
      const statuses = getParamList(queryProps.receiptStatusCode);
      defaultValues.receiptStatusCode = shipmentStatuses
        .filter(({ value }) => statuses.includes(value));
    }
    if (queryProps.origin) {
      defaultValues.origin = await fetchLocationById(queryProps.origin);
    }
    if (queryProps.requestedBy) {
      defaultValues.requestedBy = queryProps.requestedBy === currentUser.id
        ? currentUser
        : await fetchUserById(queryProps.requestedBy);
    }
    if (queryProps.createdBy) {
      defaultValues.createdBy = queryProps.createdBy === currentUser.id
        ? currentUser
        : await fetchUserById(queryProps.createdBy);
    }
    if (queryProps.updatedBy) {
      defaultValues.updatedBy = queryProps.updatedBy === currentUser.id
        ? currentUser
        : await fetchUserById(queryProps.updatedBy);
    }
    if (queryProps.createdAfter) {
      defaultValues.createdAfter = queryProps.createdAfter;
    }
    if (queryProps.createdBefore) {
      defaultValues.createdBefore = queryProps.createdBefore;
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
    // Avoid unnecessary re-fetches if getAppContext triggers fetching session info
    // but currentLocation doesn't change
    if (currentLocation?.id) {
      initializeDefaultFilterValues();
    }
  }, [currentLocation.id]);

  const selectFiltersForMyStockMovements = () => {
    const currentUserValue = {
      id: currentUser.id,
      value: currentUser.id,
      label: currentUser.name,
      name: currentUser.name,
    };

    const searchQuery = {
      direction: 'INBOUND',
      requestedBy: currentUserValue.id,
      createdBy: currentUserValue.id,
    };
    history.push({
      pathname: '/openboxes/stockMovement/list',
      search: queryString.stringify(searchQuery),
    });

    setDefaultFilterValues(values => ({
      ...values,
      requestedBy: currentUserValue,
      createdBy: currentUserValue,
    }));
  };

  const setFilterValues = (values) => {
    const filterAccessors = {
      direction: { name: 'direction' },
      origin: { name: 'origin', accessor: 'id' },
      requestedBy: { name: 'requestedBy', accessor: 'id' },
      createdBy: { name: 'createdBy', accessor: 'id' },
      updatedBy: { name: 'updatedBy', accessor: 'id' },
      createdAfter: { name: 'createdAfter' },
      createdBefore: { name: 'createdBefore' },
      receiptStatusCode: { name: 'receiptStatusCode', accessor: 'id' },
    };

    const transformedParams = transformFilterParams(values, filterAccessors);
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = history.location;
    if (queryFilterParams) {
      history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  };

  return {
    selectFiltersForMyStockMovements, defaultFilterValues, setFilterValues, filterParams,
  };
};

export default useInboundFilters;
