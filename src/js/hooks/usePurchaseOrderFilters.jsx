import { useEffect, useState } from 'react';

import queryString from 'query-string';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { fetchBuyers, fetchPurchaseOrderStatuses } from 'actions';
import filterFields from 'components/purchaseOrder/FilterFields';
import apiClient from 'utils/apiClient';
import { getParamList, transformFilterParams } from 'utils/list-utils';

const usePurchaseOrderFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const history = useHistory();
  const dispatch = useDispatch();
  const {
    supportedActivities, buyers, currentLocation, statuses, currentUser, loading,
  } = useSelector(state => ({
    supportedActivities: state.session.supportedActivities,
    buyers: state.organizations.buyers,
    currentLocation: state.session.currentLocation,
    statuses: state.purchaseOrder.statuses,
    currentUser: state.session.user,
    loading: state.session.loading,
  }));

  const isCentralPurchasingEnabled = supportedActivities.includes('ENABLE_CENTRAL_PURCHASING');

  useEffect(() => {
    if (!statuses || !statuses.length) {
      dispatch(fetchPurchaseOrderStatuses());
    }
    // TODO: If editing organizations is in React,
    //  fetch only if !buyers || buyers.length === 0
    dispatch(fetchBuyers());
  }, []);

  const fetchLocationById = async (id) => {
    const response = await apiClient(`/openboxes/api/locations/${id}`);
    return response.data?.data;
  };

  const fetchUserById = async (id) => {
    const response = await apiClient(`/openboxes/api/generic/person/${id}`);
    return response.data?.data;
  };

  const clearFilterValues = () => {
    const { pathname } = history.location;
    history.push({ pathname });
  };

  const initializeDefaultFilterValues = async () => {
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    // SET STATIC DEFAULT VALUES
    const currentLocationOption = {
      id: currentLocation?.id,
      value: currentLocation?.id,
      name: currentLocation?.name,
      label: currentLocation?.name,
    };

    if (isCentralPurchasingEnabled) {
      defaultValues.destinationParty = buyers
        .find(org => org.id === currentLocation.organization.id);
    }

    const queryProps = queryString.parse(history.location.search);
    // IF VALUE IS IN A SEARCH QUERY SET DEFAULT VALUES
    if (queryProps.status) {
      const statusesFromParams = getParamList(queryProps.status);
      defaultValues.status = statuses
        .filter(({ value }) => statusesFromParams.includes(value));
    }
    if (queryProps.statusStartDate) {
      defaultValues.statusStartDate = queryProps.statusStartDate;
    }
    if (queryProps.statusEndDate) {
      defaultValues.statusEndDate = queryProps.statusEndDate;
    }
    if (queryProps.origin) {
      defaultValues.origin = currentLocation.id === queryProps.origin
        ? currentLocationOption
        : await fetchLocationById(queryProps.origin);
    }
    if (queryProps.destination) {
      defaultValues.destination = currentLocation.id === queryProps.destination
        ? currentLocationOption
        : await fetchLocationById(queryProps.destination);
    } else if (!isCentralPurchasingEnabled && queryProps.destination === undefined) {
      defaultValues.destination = currentLocationOption;
    }
    if (queryProps.destinationParty) {
      defaultValues.destinationParty = buyers
        .find(({ id }) => id === queryProps.destinationParty);
    }
    if (queryProps.orderedBy) {
      defaultValues.orderedBy = queryProps.orderedBy === currentUser.id
        ? currentUser
        : await fetchUserById(queryProps.orderedBy);
    }
    if (queryProps.createdBy) {
      defaultValues.createdBy = queryProps.createdBy === currentUser.id
        ? currentUser
        : await fetchUserById(queryProps.createdBy);
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
    if (currentLocation?.id && buyers && !loading) {
      initializeDefaultFilterValues();
    }
  }, [currentLocation.id, buyers, loading]);

  const setFilterValues = (values) => {
    const filterAccessors = {
      destination: { name: 'destination', accessor: 'id' },
      origin: { name: 'origin', accessor: 'id' },
      status: { name: 'status', accessor: 'id' },
      statusStartDate: { name: 'statusStartDate' },
      statusEndDate: { name: 'statusEndDate' },
      destinationParty: { name: 'destinationParty', accessor: 'id' },
      orderedBy: { name: 'orderedBy', accessor: 'id' },
      createdBy: { name: 'createdBy', accessor: 'id' },
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
    filterParams,
    isCentralPurchasingEnabled,
  };
};

export default usePurchaseOrderFilters;
