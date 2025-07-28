import React from 'react';

import PropTypes from 'prop-types';

import FilterForm from 'components/Filter/FilterForm';
import ListFilterFormWrapper from 'wrappers/ListFilterFormWrapper';

const CycleCountFilters = ({
  setFilterParams,
  defaultValues,
  formProps,
  isLoading,
  filterFields,
}) => (
  <ListFilterFormWrapper>
    <FilterForm
      filterFields={filterFields}
      updateFilterParams={(values) => setFilterParams({ ...values })}
      formProps={formProps}
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
