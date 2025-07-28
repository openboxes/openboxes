import React, { useCallback } from 'react';

import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import { debouncePeopleFetch } from 'utils/option-utils';
import ListFilterFormWrapper from 'wrappers/ListFilterFormWrapper';

const CycleCountFilters = ({
  setFilterParams,
  defaultValues,
  formProps,
  isLoading,
  filterFields,
}) => {
  const {
    debounceTime,
    minSearchLength,
  } = useSelector((state) => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
  }));

  const debouncedPeopleFetch = useCallback(
    debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );

  return (
    <ListFilterFormWrapper>
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={(values) => setFilterParams({ ...values })}
        formProps={{
          ...formProps,
          debouncedPeopleFetch,
        }}
        defaultValues={defaultValues}
        allowEmptySubmit
        searchFieldDefaultPlaceholder="Search..."
        searchFieldPlaceholder="react.cycleCount.filter.search.label"
        ignoreClearFilters={['tab']}
        hidden={false}
        isLoading={isLoading}
        customSubmitButtonLabel="react.button.filter.label"
        customSubmitButtonDefaultLabel="Filter"
      />
    </ListFilterFormWrapper>
  );
};

export default CycleCountFilters;

CycleCountFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
  formProps: PropTypes.shape({}),
  isLoading: PropTypes.bool.isRequired,
  filterFields: PropTypes.shape({}).isRequired,
};

CycleCountFilters.defaultProps = {
  formProps: {},
};
