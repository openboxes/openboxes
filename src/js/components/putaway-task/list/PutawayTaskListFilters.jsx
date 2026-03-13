import React, { useCallback } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import { debounceInternalLocationsFetch } from 'utils/option-utils';

const PutawayTaskListFilters = ({
  setFilterParams,
  filterFields,
  defaultValues,
  debounceTime,
  minSearchLength,
  currentLocationId,
}) => {
  const debouncedContainerFetch = useCallback(
    debounceInternalLocationsFetch(debounceTime, minSearchLength, currentLocationId, { activityCodes: 'PUTAWAY_CART' }),
    [debounceTime, minSearchLength, currentLocationId],
  );

  const debouncedDestinationFetch = useCallback(
    debounceInternalLocationsFetch(debounceTime, minSearchLength, currentLocationId),
    [debounceTime, minSearchLength, currentLocationId],
  );

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={(values) => setFilterParams({ ...values })}
        formProps={{
          debouncedContainerFetch,
          debouncedDestinationFetch,
        }}
        defaultValues={defaultValues}
        searchFieldPlaceholder="react.putawayTask.filters.searchField.placeholder.label"
        searchFieldDefaultPlaceholder="Search by putaway task ID, product code, name, or description"
        searchFieldId="searchTerm"
        allowEmptySubmit
        autoSubmitOnFilterChange
        hidden={false}
      />
    </div>
  );
};

const mapStateToProps = (state) => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  currentLocationId: state.session.currentLocation?.id,
});

export default connect(mapStateToProps)(PutawayTaskListFilters);

PutawayTaskListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
  filterFields: PropTypes.shape({}).isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  currentLocationId: PropTypes.string.isRequired,
};
