import React from 'react';

import FilterForm from 'components/Filter/FilterForm';
import expirationHistoryReportFilterFields
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportFilterFields';
import useExpirationHistoryReportFilters from 'hooks/reporting/useExpirationHistoryReportFilters';
import ListFilterFormWrapper from 'wrappers/ListFilterFormWrapper';

const ExpirationHistoryReportFilters = () => {
  const {
    defaultFilterValues,
    setFilterValues,
  } = useExpirationHistoryReportFilters();

  return (
    <ListFilterFormWrapper>
      <FilterForm
        filterFields={expirationHistoryReportFilterFields}
        updateFilterParams={(values) => {
          setFilterValues({ ...values });
        }}
        formProps={{}}
        defaultValues={defaultFilterValues}
        ignoreClearFilters={[]}
        hidden={false}
        customSubmitButtonLabel="react.cycleCount.filters.loadTable.label"
        customSubmitButtonDefaultLabel="Load table"
        showSearchField
        disableAutoUpdateFilterParams
      />
    </ListFilterFormWrapper>
  );
};

export default ExpirationHistoryReportFilters;
