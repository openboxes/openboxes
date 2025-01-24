import React from 'react';

import PropTypes from 'prop-types';

import cycleCountFilterFields from 'components/cycleCount/CycleCountFilterFields';
import FilterForm from 'components/Filter/FilterForm';
import ListFilterFormWrapper from 'wrappers/ListFilterFormWrapper';

const CycleCountFilters = ({
  setFilterParams,
  defaultValues,
  formProps,
}) => (
  <ListFilterFormWrapper>
    <FilterForm
      filterFields={cycleCountFilterFields}
      updateFilterParams={(values) => setFilterParams({ ...values })}
      formProps={formProps}
      defaultValues={defaultValues}
      allowEmptySubmit
      searchFieldDefaultPlaceholder="Search..."
      searchFieldPlaceholder="react.cycleCount.filter.search.label"
      ignoreClearFilters={['tab']}
      hidden={false}
    />
  </ListFilterFormWrapper>
);

export default CycleCountFilters;

CycleCountFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
  formProps: PropTypes.shape({}),
};

CycleCountFilters.defaultProps = {
  formProps: {},
};
