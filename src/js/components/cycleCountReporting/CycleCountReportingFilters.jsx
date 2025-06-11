import React, { useCallback, useRef } from 'react';

import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import { INDICATORS_TAB } from 'consts/cycleCount';
import useQueryParams from 'hooks/useQueryParams';
import { debounceProductsFetch } from 'utils/option-utils';
import ListFilterFormWrapper from 'wrappers/ListFilterFormWrapper';

const CycleCountReportingFilters = ({
  setFilterParams,
  defaultValues,
  formProps,
  isLoading,
  filterFields,
  setShouldFetch,
  tablePaginationProps,
}) => {
  const updateCounter = useRef(0);
  const {
    debounceTime,
    minSearchLength,
  } = useSelector((state) => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
  }));
  const { tab } = useQueryParams();
  const { setSerializedParams } = tablePaginationProps;
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
        updateFilterParams={(values) => {
          updateCounter.current += 1;
          const valuesWithCounter = {
            ...values,
            updateCounter: updateCounter.current,
          };
          setFilterParams({ ...values });
          setShouldFetch(true);
          setSerializedParams(JSON.stringify(valuesWithCounter));
        }}
        formProps={{
          ...formProps,
          debouncedProductsFetch,
        }}
        defaultValues={defaultValues}
        ignoreClearFilters={['tab']}
        hidden={false}
        isLoading={isLoading}
        customSubmitButtonLabel={tab === INDICATORS_TAB ? 'react.cycleCount.filters.loadData.label' : 'react.cycleCount.filters.loadTable.label'}
        customSubmitButtonDefaultLabel={tab === INDICATORS_TAB ? 'Load data' : 'Load table'}
        showFilterVisibilityToggler={false}
        showSearchField={false}
        disableAutoUpdateFilterParams
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
  setShouldFetch: PropTypes.func,
  tablePaginationProps: PropTypes.shape({}).isRequired,
};

CycleCountReportingFilters.defaultProps = {
  formProps: {},
  filterFields: {},
  setShouldFetch: null,
};
