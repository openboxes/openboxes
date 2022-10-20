import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import { debounceLocationsFetch, debounceUsersFetch } from 'utils/option-utils';

const StockMovementInboundFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  formProps,
  filterFields,
  defaultValues,
}) => {
  const fetchUsers = debounceUsersFetch(debounceTime, minSearchLength);
  const fetchLocations = debounceLocationsFetch(
    debounceTime,
    minSearchLength,
    [],
    true,
    false,
    false,
  );

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        searchFieldId="q"
        searchFieldPlaceholder="Search by requisition number, name etc."
        filterFields={filterFields}
        defaultValues={defaultValues}
        ignoreClearFilters={['destination', 'direction']}
        updateFilterParams={values => setFilterParams({ ...values })}
        hidden={false}
        formProps={{
          ...formProps,
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
});


export default connect(mapStateToProps)(StockMovementInboundFilters);

StockMovementInboundFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  filterFields: PropTypes.shape({}).isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
  formProps: PropTypes.shape({}).isRequired,
};
