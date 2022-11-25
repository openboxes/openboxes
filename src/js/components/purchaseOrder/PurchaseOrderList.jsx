import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import queryString from 'query-string';
import { connect } from 'react-redux';

import { fetchBuyers, fetchPurchaseOrderStatuses, fetchTranslations } from 'actions';
import filterFields from 'components/purchaseOrder/FilterFields';
import PurchaseOrderListFilters from 'components/purchaseOrder/PurchaseOrderListFilters';
import PurchaseOrderListHeader from 'components/purchaseOrder/PurchaseOrderListHeader';
import PurchaseOrderListTable from 'components/purchaseOrder/PurchaseOrderListTable';
import apiClient from 'utils/apiClient';
import { getParamList, transformFilterParams } from 'utils/list-utils';


const PurchaseOrderList = (props) => {
  // Filter params are stored here, to be able to pass them to table component
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  useEffect(() => {
    props.fetchTranslations(props.locale, 'purchaseOrder');
  }, [props.locale]);

  const isCentralPurchasingEnabled = props.supportedActivities.includes('ENABLE_CENTRAL_PURCHASING');

  useEffect(() => {
    if (!props.statuses || props.statuses.length === 0) {
      props.fetchStatuses();
    }
    // TODO: If editing organizations is in React,
    //  fetch only if !buyers || buyers.length === 0
    props.fetchBuyerOrganizations();
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
    const { pathname } = props.history.location;
    props.history.push({ pathname });
  };

  const initializeDefaultFilterValues = async () => {
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    // SET STATIC DEFAULT VALUES
    const currentLocationOption = {
      id: props.currentLocation?.id,
      value: props.currentLocation?.id,
      name: props.currentLocation?.name,
      label: props.currentLocation?.name,
    };

    if (isCentralPurchasingEnabled) {
      defaultValues.destinationParty = props.buyers
        .find(org => org.id === props.currentLocation.organization.id);
    }

    const queryProps = queryString.parse(props.history.location.search);
    // IF VALUE IS IN A SEARCH QUERY SET DEFAULT VALUES
    if (queryProps.status) {
      const statuses = getParamList(queryProps.status);
      defaultValues.status = props.statuses
        .filter(({ value }) => statuses.includes(value));
    }
    if (queryProps.statusStartDate) {
      defaultValues.statusStartDate = queryProps.statusStartDate;
    }
    if (queryProps.statusEndDate) {
      defaultValues.statusEndDate = queryProps.statusEndDate;
    }
    if (queryProps.origin) {
      defaultValues.origin = props.currentLocation.id === queryProps.origin
        ? currentLocationOption
        : await fetchLocationById(queryProps.origin);
    }
    if (queryProps.destination) {
      defaultValues.destination = props.currentLocation.id === queryProps.destination
        ? currentLocationOption
        : await fetchLocationById(queryProps.destination);
    } else if (!isCentralPurchasingEnabled && queryProps.destination === undefined) {
      defaultValues.destination = currentLocationOption;
    }
    if (queryProps.destinationParty) {
      defaultValues.destinationParty = props.buyers
        .find(({ id }) => id === queryProps.destinationParty);
    }
    if (queryProps.orderedBy) {
      defaultValues.orderedBy = queryProps.orderedBy === props.currentUser.id
        ? props.currentUser
        : await fetchUserById(queryProps.orderedBy);
    }
    if (queryProps.createdBy) {
      defaultValues.createdBy = queryProps.createdBy === props.currentUser.id
        ? props.currentUser
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
  }, [props.currentLocation?.id]);

  useEffect(() => {
    if (props.currentLocation?.id && props.buyers && !props.loading) {
      initializeDefaultFilterValues();
    }
  }, [props.currentLocation.id, props.buyers, props.loading]);

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
    const { pathname } = props.history.location;
    if (Object.keys(values).length > 0) {
      props.history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  };

  return (
    <div className="d-flex flex-column list-page-main">
      <PurchaseOrderListHeader />
      <PurchaseOrderListFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{
          statuses: props.statuses,
          buyers: props.buyers,
          isCentralPurchasingEnabled,
        }}
      />
      <PurchaseOrderListTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  currentLocation: state.session.currentLocation,
  supportedActivities: state.session.supportedActivities,
  currentUser: state.session.user,
  buyers: state.organizations.buyers,
  statuses: state.purchaseOrder.statuses,
  loading: state.session.loading,
});

export default connect(mapStateToProps, {
  fetchTranslations,
  fetchStatuses: fetchPurchaseOrderStatuses,
  fetchBuyerOrganizations: fetchBuyers,
})(PurchaseOrderList);


PurchaseOrderList.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  fetchBuyerOrganizations: PropTypes.func.isRequired,
  fetchStatuses: PropTypes.func.isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
  currentUser: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
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
  statuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    value: PropTypes.string,
    label: PropTypes.string,
    variant: PropTypes.string,
  })).isRequired,
  buyers: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    value: PropTypes.string,
    label: PropTypes.string,
    variant: PropTypes.string,
  })).isRequired,
  loading: PropTypes.bool.isRequired,
};

