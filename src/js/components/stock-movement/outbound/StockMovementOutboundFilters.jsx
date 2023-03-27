import React, { useCallback } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchRequisitionStatusCodes } from 'actions';
import FilterForm from 'components/Filter/FilterForm';
import { debounceLocationsFetch, debouncePeopleFetch, debounceUsersFetch } from 'utils/option-utils';

const StockMovementOutboundFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  formProps,
  filterFields,
  defaultValues,
}) => {
  const fetchUsers = useCallback(
    debounceUsersFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );
  const fetchPeople = useCallback(
    debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );

  const fetchLocations = useCallback(
    debounceLocationsFetch(
      debounceTime,
      minSearchLength,
      [],
      true,
      false,
      false,
    ),
    [debounceTime, minSearchLength],
  );

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        searchFieldId="q"
        searchFieldPlaceholder="react.stockMovement.search.placeholder.label"
        searchFieldDefaultPlaceholder="Search by requisition number, name etc."
        filterFields={filterFields}
        defaultValues={defaultValues}
        ignoreClearFilters={['origin', 'direction', 'sourceType']}
        updateFilterParams={values => setFilterParams({ ...values })}
        hidden={false}
        formProps={{
          ...formProps,
          fetchUsers,
          fetchLocations,
          fetchPeople,
        }}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
});

const mapDispatchToProps = {
  fetchStatuses: fetchRequisitionStatusCodes,
};

export default connect(mapStateToProps, mapDispatchToProps)(StockMovementOutboundFilters);

StockMovementOutboundFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  filterFields: PropTypes.shape({}).isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
  formProps: PropTypes.shape({}).isRequired,
};
