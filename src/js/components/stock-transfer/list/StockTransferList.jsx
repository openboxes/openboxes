import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import queryString from 'query-string';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchTranslations } from 'actions';
import filterFields from 'components/stock-transfer/list/FilterFields';
import StockTransferListFilters from 'components/stock-transfer/list/StockTransferListFilters';
import StockTransferListHeader from 'components/stock-transfer/list/StockTransferListHeader';
import StockTransferListTable from 'components/stock-transfer/list/StockTransferListTable';
import apiClient from 'utils/apiClient';
import { getParamList, transformFilterParams } from 'utils/list-utils';

const StockTransferList = (props) => {
  // Filter params are stored here, to be able to pass them to table component
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  useEffect(() => {
    props.fetchTranslations(props.locale, 'stockTransfer');
  }, [props.locale]);

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

    const queryProps = queryString.parse(props.history.location.search);

    // SET STATIC DEFAULT VALUES
    if (queryProps.status) {
      const statuses = getParamList(queryProps.status);
      defaultValues.status = props.statuses.filter(({ id }) => statuses.includes(id));
    }
    if (queryProps.createdBy) {
      defaultValues.createdBy = queryProps.createdBy === props.currentUser.id
        ? props.currentUser
        : await fetchUserById(queryProps.createdBy);
    }
    if (queryProps.lastUpdatedStartDate) {
      defaultValues.lastUpdatedStartDate = queryProps.lastUpdatedStartDate;
    }
    if (queryProps.lastUpdatedEndDate) {
      defaultValues.lastUpdatedEndDate = queryProps.lastUpdatedEndDate;
    }
    setDefaultFilterValues({ ...defaultValues });
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
    if (props.currentLocation?.id) {
      initializeDefaultFilterValues();
    }
  }, [props.currentLocation?.id]);

  const setFilterValues = (values) => {
    const filterAccessors = {
      status: { name: 'status', accessor: 'id' },
      createdBy: { name: 'createdBy', accessor: 'id' },
      lastUpdatedStartDate: { name: 'lastUpdatedStartDate' },
      lastUpdatedEndDate: { name: 'lastUpdatedEndDate' },
    };

    const transformedParams = transformFilterParams(values, filterAccessors);
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = props.history.location;
    if (Object.keys(values).length > 0) {
      props.history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams({ ...values });
  };

  return (
    <div className="d-flex flex-column list-page-main">
      <StockTransferListHeader />
      <StockTransferListFilters
        filterFields={filterFields}
        setFilterParams={setFilterValues}
        defaultValues={defaultFilterValues}
        formProps={{ statuses: props.statuses }}
      />
      <StockTransferListTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  currentUser: state.session.user,
  statuses: state.stockTransfer.statuses,
  currentLocation: state.session.currentLocation,
});

const mapDispatchToProps = {
  fetchTranslations,
};
export default withRouter(connect(mapStateToProps, mapDispatchToProps)(StockTransferList));


StockTransferList.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  currentUser: PropTypes.shape({
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
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  })).isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
  }).isRequired,
};
