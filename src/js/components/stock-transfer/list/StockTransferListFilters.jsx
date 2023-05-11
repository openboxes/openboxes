import React, { useCallback } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import { debouncePeopleFetch } from 'utils/option-utils';


const StockTransferListFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  formProps,
  filterFields,
  defaultValues,
}) => {
  const debouncedPeopleFetch = useCallback(
    debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={values => setFilterParams({ ...values })}
        formProps={{
          ...formProps,
          debouncedPeopleFetch,
        }}
        defaultValues={defaultValues}
        searchFieldPlaceholder="react.stockTransfer.filters.searchField.placeholder.label"
        searchFieldDefaultPlaceholder="Search by transfer number"
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
});


export default connect(mapStateToProps)(StockTransferListFilters);

StockTransferListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
  filterFields: PropTypes.shape({}).isRequired,
  formProps: PropTypes.shape({}).isRequired,
};
