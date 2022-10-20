import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import queryString from 'query-string';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import filterFields from 'components/stock-list/FilterFields';
import StockListFilters from 'components/stock-list/StockListFilters';
import StockListHeader from 'components/stock-list/StockListHeader';
import StockListTable from 'components/stock-list/StockListTable';
import apiClient from 'utils/apiClient';
import { transformFilterParams } from 'utils/list-utils';

const StockList = (props) => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const [locations, setLocations] = useState([]);

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
    const { pathname } = props.history.location;
    props.history.push({ pathname });
  };

  const initializeDefaultFilterValues = async () => {
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    const queryProps = queryString.parse(props.history.location.search);

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
  }, [props.currentLocation?.id]);

  useEffect(() => {
    if (props.currentLocation?.id) {
      initializeDefaultFilterValues();
    }
  }, [props.currentLocation?.id]);

  const setFilterValues = (values) => {
    const filterAccessors = {
      destination: { name: 'destination', accessor: 'id' },
      origin: { name: 'origin', accessor: 'id' },
      isPublished: { name: 'isPublished' },
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
      <StockListHeader />
      <StockListFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{ locations }}
      />
      <StockListTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  currentLocation: state.session.currentLocation,
});

StockList.propTypes = {
  history: PropTypes.shape({
    push: PropTypes.func,
    replace: PropTypes.func,
    location: PropTypes.shape({
      search: PropTypes.string,
    }),
  }).isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
  }).isRequired,
};

export default withRouter(connect(mapStateToProps)(StockList));
