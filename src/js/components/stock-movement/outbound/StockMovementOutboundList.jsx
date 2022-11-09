import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import queryString from 'query-string';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchRequisitionStatusCodes, fetchTranslations } from 'actions';
import filterFields from 'components/stock-movement/outbound/FilterFields';
import StockMovementOutboundFilters from 'components/stock-movement/outbound/StockMovementOutboundFilters';
import StockMovementOutboundHeader from 'components/stock-movement/outbound/StockMovementOutboundHeader';
import StockMovementOutboundTable from 'components/stock-movement/outbound/StockMovementOutboundTable';
import apiClient from 'utils/apiClient';
import { getParamList, transformFilterParams } from 'utils/list-utils';

const StockMovementOutboundList = (props) => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  useEffect(() => {
    props.fetchTranslations(props.locale, 'stockMovement');
    props.fetchTranslations(props.locale, 'StockMovementType');
  }, [props.locale]);

  useEffect(() => {
    if (!props.isRequisitionStatusesFetched || props.requisitionStatuses.length === 0) {
      props.fetchStatuses();
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
      .reduce((acc, key) => ({ ...acc, [key]: '' }), { direction: 'OUTBOUND' });
    const transformedParams = transformFilterParams(defaultValues, { direction: { name: 'direction' } });
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = props.history.location;
    if (queryFilterParams) {
      props.history.push({ pathname, search: queryFilterParams });
    }
  };

  const initializeDefaultFilterValues = async () => {
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    // SET STATIC DEFAULT VALUES
    defaultValues.origin = {
      id: props.currentLocation?.id,
      value: props.currentLocation?.id,
      name: props.currentLocation?.name,
      label: props.currentLocation?.name,
    };

    defaultValues.direction = 'OUTBOUND';

    if (props.isRequestsList) {
      defaultValues.sourceType = 'ELECTRONIC';
    }

    const queryProps = queryString.parse(props.history.location.search);
    // IF VALUE IS IN A SEARCH QUERY SET DEFAULT VALUES
    if (queryProps.requisitionStatusCode) {
      const statuses = getParamList(queryProps.requisitionStatusCode);
      defaultValues.requisitionStatusCode = props.requisitionStatuses
        .filter(({ value }) => statuses.includes(value));
    }
    if (queryProps.receiptStatusCode) {
      defaultValues.receiptStatusCode = getParamList(queryProps.receiptStatusCode);
    }
    if (queryProps.destination) {
      defaultValues.destination = await fetchLocationById(queryProps.destination);
    }
    if (queryProps.requestedBy) {
      defaultValues.requestedBy = queryProps.requestedBy === props.currentUser.id
        ? props.currentUser
        : await fetchUserById(queryProps.requestedBy);
    }
    if (queryProps.createdBy) {
      defaultValues.createdBy = queryProps.createdBy === props.currentUser.id
        ? props.currentUser
        : await fetchUserById(queryProps.createdBy);
    }
    if (queryProps.updatedBy) {
      defaultValues.updatedBy = queryProps.updatedBy === props.currentUser.id
        ? props.currentUser
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
  }, [props.currentLocation?.id]);

  useEffect(() => {
    // Avoid unnecessary re-fetches if getAppContext triggers fetching session info
    // but currentLocation doesn't change
    if (props.currentLocation?.id) {
      initializeDefaultFilterValues();
    }
  }, [props.currentLocation.id]);


  const selectFiltersForMyStockMovements = () => {
    const currentUserValue = {
      id: props.currentUser.id,
      value: props.currentUser.id,
      label: props.currentUser.name,
      name: props.currentUser.name,
    };

    const searchQuery = {
      direction: 'OUTBOUND',
      requestedBy: currentUserValue.id,
      createdBy: currentUserValue.id,
    };
    props.history.push({
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
      sourceType: { name: 'sourceType' },
      destination: { name: 'destination', accessor: 'id' },
      requestedBy: { name: 'requestedBy', accessor: 'id' },
      createdBy: { name: 'createdBy', accessor: 'id' },
      updatedBy: { name: 'updatedBy', accessor: 'id' },
      createdAfter: { name: 'createdAfter' },
      createdBefore: { name: 'createdBefore' },
      requisitionStatusCode: { name: 'requisitionStatusCode', accessor: 'id' },
      receiptStatusCode: { name: 'receiptStatusCode' },
    };

    const transformedParams = transformFilterParams(values, filterAccessors);
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = props.history.location;
    if (queryFilterParams) {
      props.history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  };

  return (
    <div className="d-flex flex-column list-page-main">
      <StockMovementOutboundHeader
        isRequestsOpen={props.isRequestsList}
        showMyStockMovements={selectFiltersForMyStockMovements}
      />
      <StockMovementOutboundFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{ requisitionStatuses: props.requisitionStatuses }}
      />
      <StockMovementOutboundTable
        isRequestsOpen={props.isRequestsList}
        filterParams={filterParams}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  currentUser: state.session.user,
  currentLocation: state.session.currentLocation,
  requisitionStatuses: state.requisitionStatuses.data,
  isRequisitionStatusesFetched: state.requisitionStatuses.fetched,
});

export default withRouter(connect(mapStateToProps, {
  fetchTranslations,
  fetchStatuses: fetchRequisitionStatusCodes,
})(StockMovementOutboundList));

StockMovementOutboundList.propTypes = {
  locale: PropTypes.string.isRequired,
  isRequestsList: PropTypes.bool.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  fetchStatuses: PropTypes.func.isRequired,
  isRequisitionStatusesFetched: PropTypes.bool.isRequired,
  currentUser: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  requisitionStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    variant: PropTypes.string,
    label: PropTypes.string,
  })).isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
    replace: PropTypes.func,
    location: PropTypes.shape({
      search: PropTypes.string,
    }),
  }).isRequired,
};
