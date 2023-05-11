import { useEffect, useState } from 'react';

import queryString from 'query-string';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { fetchBuyers, fetchPaymentTerms, fetchPurchaseOrderStatuses } from 'actions';
import filterFields from 'components/purchaseOrder/FilterFields';
import usePurchaseOrderFiltersCleaner from 'hooks/list-pages/purchase-order/usePurchaseOrderFiltersCleaner';
import { getParamList, transformFilterParams } from 'utils/list-utils';
import { fetchLocationById, fetchUserById, selectNullOption } from 'utils/option-utils';

const usePurchaseOrderFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const history = useHistory();
  const dispatch = useDispatch();
  const {
    supportedActivities,
    buyers,
    currentLocation,
    statuses,
    paymentTerms,
    currentUser,
    currentLocale,
  } = useSelector(state => ({
    supportedActivities: state.session.supportedActivities,
    buyers: state.organizations.buyers,
    currentLocation: state.session.currentLocation,
    statuses: state.purchaseOrder.statuses,
    paymentTerms: state.purchaseOrder.paymentTerms,
    currentUser: state.session.user,
    currentLocale: state.session.activeLanguage,
  }));

  const isCentralPurchasingEnabled = supportedActivities.includes('ENABLE_CENTRAL_PURCHASING');
  useEffect(() => {
    // TODO: If editing organizations is in React,
    //  fetch only if !buyers || buyers.length === 0
    dispatch(fetchBuyers());
  }, []);

  useEffect(() => {
    // TODO: When having full React, if once fetched, fetch only if a current language differs
    // TODO: from the language, that we were fetching this for
    dispatch(fetchPurchaseOrderStatuses());
    dispatch(fetchPaymentTerms());
  }, [currentLocale]);

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
    if (queryProps.paymentTerm) {
      const paymentTermsFromParams = getParamList(queryProps.paymentTerm);
      defaultValues.paymentTerm = paymentTerms
        .filter(({ id }) => paymentTermsFromParams.includes(id));

      if (paymentTermsFromParams.includes(selectNullOption.id)) {
        defaultValues.paymentTerm.push(selectNullOption);
      }
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

  // Custom hook for changing location/filters rebuilding logic
  usePurchaseOrderFiltersCleaner({
    clearFilterValues,
    initializeDefaultFilterValues,
    filtersInitialized,
  });

  const setFilterValues = (values) => {
    const filterAccessors = {
      destination: { name: 'destination', accessor: 'id' },
      origin: { name: 'origin', accessor: 'id' },
      status: { name: 'status', accessor: 'id' },
      statusStartDate: { name: 'statusStartDate' },
      statusEndDate: { name: 'statusEndDate' },
      destinationParty: { name: 'destinationParty', accessor: 'id' },
      paymentTerm: { name: 'paymentTerm', accessor: 'id' },
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
