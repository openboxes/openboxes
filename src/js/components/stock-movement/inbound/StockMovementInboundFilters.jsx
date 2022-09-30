import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchShipmentStatusCodes } from 'actions';
import FilterForm from 'components/Filter/FilterForm';
import { debounceLocationsFetch, debounceUsersFetch } from 'utils/option-utils';

const StockMovementInboundFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  fetchStatuses,
  shipmentStatuses,
  isShipmentStatusesFetched,
  filterFields,
  defaultValues,
}) => {
  const fetchUsers = debounceUsersFetch(debounceTime, minSearchLength);
  // eslint-disable-next-line max-len
  const fetchLocations = debounceLocationsFetch(
    debounceTime,
    minSearchLength,
    [],
    true,
    false,
    false,
  );

  useEffect(() => {
    if (!isShipmentStatusesFetched || shipmentStatuses.length === 0) {
      fetchStatuses();
    }
  }, []);


  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        searchFieldId="q"
        searchFieldPlaceholder="Search by order number of description"
        filterFields={filterFields}
        defaultValues={defaultValues}
        onClear={form => form.reset({ destination: defaultValues.destination })}
        updateFilterParams={values => setFilterParams({ ...values })}
        hidden={false}
        formProps={{
          shipmentStatuses,
          fetchUsers,
          fetchLocations,
        }}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  shipmentStatuses: state.shipmentStatuses.data,
  isShipmentStatusesFetched: state.shipmentStatuses.fetched,
});

const mapDispatchToProps = {
  fetchStatuses: fetchShipmentStatusCodes,
};

export default connect(mapStateToProps, mapDispatchToProps)(StockMovementInboundFilters);


StockMovementInboundFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  fetchStatuses: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  isShipmentStatusesFetched: PropTypes.bool.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  shipmentStatuses: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  }).isRequired,
  filterFields: PropTypes.shape({}).isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
};
