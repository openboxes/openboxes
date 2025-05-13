import React, { useCallback } from 'react';

import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import { debounceProductsFetch } from 'utils/option-utils';
import ListFilterFormWrapper from 'wrappers/ListFilterFormWrapper';

const CycleCountReportingFilters = ({
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

  const debouncedProductsFetch = useCallback(
    debounceProductsFetch(
      debounceTime,
      minSearchLength,
    ), [debounceTime, minSearchLength],
  );
  return (
    <ListFilterFormWrapper>
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={(values) => setFilterParams({ ...values })}
        formProps={{
          ...formProps,
          debouncedProductsFetch,
        }}
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
};

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
