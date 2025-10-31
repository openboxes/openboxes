import { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import queryString from 'query-string';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { fetchAvailableApprovers, fetchRequisitionStatusCodes, fetchShipmentTypes } from 'actions';
import filterFields from 'components/stock-movement/outbound/FilterFields';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import { getParamList, transformFilterParams } from 'utils/list-utils';
import { fetchLocationById, fetchUserById, selectNullOption } from 'utils/option-utils';

const useOutboundFilters = (sourceType) => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const history = useHistory();
  const dispatch = useDispatch();
  const {
    requisitionStatuses,
    currentLocation,
    currentUser,
    currentLocale,
    shipmentTypes,
    availableApprovers,
  } = useSelector((state) => ({
    requisitionStatuses: state.requisitionStatuses.data,
    currentLocation: state.session.currentLocation,
    currentUser: state.session.user,
    currentLocale: state.session.activeLanguage,
    shipmentTypes: state.stockMovementCommon.shipmentTypes,
    availableApprovers: state.approvers.data,
  }));

  useEffect(() => {
    // TODO: When having full React, if once fetched, fetch only if a current language differs
    // TODO: from the language, that we were fetching this for
    dispatch(fetchShipmentTypes());
  }, [currentLocale]);

  useEffect(() => {
    dispatch(fetchRequisitionStatusCodes(sourceType));
  }, [currentLocale, currentLocation.id]);

  useEffect(() => {
    dispatch(fetchAvailableApprovers());
  }, [currentLocation.id]);

  const filters = filterFields(sourceType === 'ELECTRONIC');

  const clearFilterValues = () => {
    const defaultValues = Object.keys(filters)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), { direction: 'OUTBOUND' });
    const transformedParams = transformFilterParams(defaultValues, { direction: { name: 'direction' } });
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = history.location;
    if (queryFilterParams) {
      history.push({ pathname, search: queryFilterParams });
    }
  };

  const initializeDefaultFilterValues = async () => {
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filters)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    // SET STATIC DEFAULT VALUES
    defaultValues.origin = {
      id: currentLocation?.id,
      value: currentLocation?.id,
      name: currentLocation?.name,
      label: currentLocation?.name,
    };

    defaultValues.direction = 'OUTBOUND';

    if (sourceType === 'ELECTRONIC') {
      defaultValues.sourceType = 'ELECTRONIC';
    }

    const queryProps = queryString.parse(history.location.search);
    // IF VALUE IS IN A SEARCH QUERY SET DEFAULT VALUES
    if (queryProps.requisitionStatusCode) {
      const statuses = getParamList(queryProps.requisitionStatusCode);
      defaultValues.requisitionStatusCode = requisitionStatuses
        .filter(({ value }) => statuses.includes(value));
    }
    if (queryProps.receiptStatusCode) {
      defaultValues.receiptStatusCode = getParamList(queryProps.receiptStatusCode);
    }
    if (queryProps.destination) {
      defaultValues.destination = await fetchLocationById(queryProps.destination);
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
    if (queryProps.shipmentType) {
      const shipTypes = getParamList(queryProps.shipmentType);
      defaultValues.shipmentType = shipmentTypes.filter(({ id }) => shipTypes.includes(id));
    }
    if (sourceType === 'ELECTRONIC' && queryProps.approver) {
      const approvers = getParamList(queryProps.approver);
      defaultValues.approver = availableApprovers.filter(({ id }) => approvers.includes(id));
      if (approvers.includes(selectNullOption.id)) {
        defaultValues.approver.push(selectNullOption);
      }
    }

    setDefaultFilterValues(defaultValues);
    setFiltersInitialized(true);
  };

  // Custom hook for changing location/filters rebuilding logic
  useCommonFiltersCleaner({ clearFilterValues, filtersInitialized, initializeDefaultFilterValues });

  const selectFiltersForMyStockMovements = () => {
    const currentUserValue = {
      id: currentUser.id,
      value: currentUser.id,
      label: currentUser.name,
      name: currentUser.name,
    };

    const searchQuery = {
      direction: 'OUTBOUND',
      requestedBy: currentUserValue.id,
      createdBy: currentUserValue.id,
    };
    history.push({
      pathname: STOCK_MOVEMENT_URL.list(),
      search: queryString.stringify(searchQuery),
    });

    setDefaultFilterValues((values) => ({
      ...values,
      requestedBy: currentUserValue,
      createdBy: currentUserValue,
    }));
  };

  const setFilterValues = (values) => {
    const filterAccessors = {
      direction: { name: 'direction' },
      sourceType: { name: 'sourceType' },
      destination: { name: 'destination', accessor: 'id' },
      requestedBy: { name: 'requestedBy', accessor: 'id' },
      createdBy: { name: 'createdBy', accessor: 'id' },
      updatedBy: { name: 'updatedBy', accessor: 'id' },
      createdAfter: { name: 'createdAfter' },
      createdBefore: { name: 'createdBefore' },
      requisitionStatusCode: { name: 'requisitionStatusCode', accessor: 'id' },
      receiptStatusCode: { name: 'receiptStatusCode' },
      shipmentType: { name: 'shipmentType', accessor: 'id' },
      approver: { name: 'approver', accessor: 'id' },
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
    selectFiltersForMyStockMovements,
    defaultFilterValues,
    setFilterValues,
    filterParams,
  };
};

export default useOutboundFilters;

useOutboundFilters.propTypes = {
  sourceType: PropTypes.string.isRequired,
};
