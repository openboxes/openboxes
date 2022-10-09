import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import { debounceUsersFetch } from 'utils/option-utils';


const StockTransferListFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  statuses,
  filterFields,
  defaultValues,
}) => {
  const debouncedUsersFetch = debounceUsersFetch(debounceTime, minSearchLength);

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={values => setFilterParams({ ...values })}
        formProps={{
          debouncedUsersFetch,
          statuses,
        }}
        defaultValues={defaultValues}
        searchFieldPlaceholder="Search by transfer number"
        searchFieldId="q"
        allowEmptySubmit
        hidden={false}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  statuses: state.stockTransfer.statuses,
  currentLocation: state.session.currentLocation,
});


export default connect(mapStateToProps)(StockTransferListFilters);

StockTransferListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  statuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  })).isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
  filterFields: PropTypes.shape({}).isRequired,
};
