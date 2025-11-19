import React from 'react';

import FilterForm from 'components/Filter/FilterForm';
import expirationHistoryReportFilterFields
  from 'components/reporting/expirationHistoryReport/ExpirationHistoryReportFilterFields';
import ListFilterFormWrapper from 'wrappers/ListFilterFormWrapper';

const ExpirationHistoryReportFilters = ({
  setFilterValues,
  defaultFilterValues,
  setShouldFetch,
  setSerializedParams,
}) => (
  <ListFilterFormWrapper>
    <FilterForm
      filterFields={expirationHistoryReportFilterFields}
      updateFilterParams={(values) => {
        setFilterValues({ ...values });
        setShouldFetch(true);
        setSerializedParams(JSON.stringify(values));
      }}
      defaultValues={defaultFilterValues}
      hidden={false}
      customSubmitButtonLabel="react.cycleCount.filters.loadTable.label"
      customSubmitButtonDefaultLabel="Load table"
      showSearchField
      disableAutoUpdateFilterParams
    />
  </ListFilterFormWrapper>
);

export default ExpirationHistoryReportFilters;
