import React from 'react';

import PropTypes from 'prop-types';

import FilterForm from 'components/Filter/FilterForm';
import ListFilterFormWrapper from 'wrappers/ListFilterFormWrapper';

const CycleCountReportingFilters = ({
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
      ignoreClearFilters={['tab']}
      hidden={false}
      isLoading={isLoading}
      customSubmitButtonLabel="react.cycleCountReporting.filter.loadTable.label"
      customSubmitButtonDefaultLabel="Load table"
      showFilterVisibilityToggler={false}
      showSearchField={false}
    />
  </ListFilterFormWrapper>
);

export default CycleCountReportingFilters;

CycleCountReportingFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
  formProps: PropTypes.shape({}),
  filterFields: PropTypes.shape({}),
  isLoading: PropTypes.bool.isRequired,
};

CycleCountReportingFilters.defaultProps = {
  formProps: {},
  filterFields: {},
};
