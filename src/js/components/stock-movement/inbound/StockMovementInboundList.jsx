import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import queryString from 'query-string';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchShipmentStatusCodes, fetchTranslations } from 'actions';
import filterFields from 'components/stock-movement/inbound/FilterFields';
import StockMovementInboundFilters from 'components/stock-movement/inbound/StockMovementInboundFilters';
import StockMovementInboundHeader from 'components/stock-movement/inbound/StockMovementInboundHeader';
import StockMovementInboundTable from 'components/stock-movement/inbound/StockMovementInboundTable';
import apiClient from 'utils/apiClient';
import { getParamList, transformFilterParams } from 'utils/list-utils';

const StockMovementInboundList = (props) => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  useEffect(() => {
    props.fetchTranslations(props.locale, 'stockMovement');
    props.fetchTranslations(props.locale, 'reactTable');
  }, [props.locale]);

  useEffect(() => {
    if (!props.isShipmentStatusesFetched || props.shipmentStatuses.length === 0) {
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
      .reduce((acc, key) => ({ ...acc, [key]: '' }), { direction: 'INBOUND' });
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
    defaultValues.direction = 'INBOUND';

    const queryProps = queryString.parse(props.history.location.search);
    // IF VALUE IS IN A SEARCH QUERY SET DEFAULT VALUES
    if (queryProps.destination === undefined) {
      defaultValues.destination = {
        id: props.currentLocation?.id,
        value: props.currentLocation?.id,
        name: props.currentLocation?.name,
        label: props.currentLocation?.name,
      };
    }

    if (queryProps.receiptStatusCode) {
      const statuses = getParamList(queryProps.receiptStatusCode);
      defaultValues.receiptStatusCode = props.shipmentStatuses
        .filter(({ value }) => statuses.includes(value));
    }
    if (queryProps.origin) {
      defaultValues.origin = await fetchLocationById(queryProps.origin);
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
      direction: 'INBOUND',
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
    const { pathname } = props.history.location;
    if (queryFilterParams) {
      props.history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  };


  return (
    <div className="d-flex flex-column list-page-main">
      <StockMovementInboundHeader showMyStockMovements={selectFiltersForMyStockMovements} />
      <StockMovementInboundFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{ shipmentStatuses: props.shipmentStatuses }}
      />
      <StockMovementInboundTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  currentUser: state.session.user,
  currentLocation: state.session.currentLocation,
  shipmentStatuses: state.shipmentStatuses.data,
  isShipmentStatusesFetched: state.shipmentStatuses.fetched,

});

export default withRouter(connect(mapStateToProps, {
  fetchTranslations,
  fetchStatuses: fetchShipmentStatusCodes,
})(StockMovementInboundList));

StockMovementInboundList.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  fetchStatuses: PropTypes.func.isRequired,
  isShipmentStatusesFetched: PropTypes.bool.isRequired,
  currentUser: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  shipmentStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    variant: PropTypes.string,
    label: PropTypes.string,
  })).isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
    replace: PropTypes.func,
    location: PropTypes.shape({
      search: PropTypes.string,
    }),
  }).isRequired,
};
