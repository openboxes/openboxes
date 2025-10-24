import React from 'react';

import FilterForm from 'components/Filter/FilterForm';
import reorderReportFilterFields
  from 'components/reporting/reorderReport/ReorderReportFilterFields';
import useReorderReportFilters from 'hooks/reporting/useReorderReportFilters';
import useTranslate from 'hooks/useTranslate';
import ListFilterFormWrapper from 'wrappers/ListFilterFormWrapper';

const ReorderReportFilters = () => {
  const translate = useTranslate();

  const {
    categories,
    tags,
    locations,
    defaultFilterValues,
    setFilterValues,
  } = useReorderReportFilters({ filterFields: reorderReportFilterFields });

  const downloadCsv = (values) => {
    console.log(values);
  };

  return (
    <ListFilterFormWrapper>
      <FilterForm
        filterFields={reorderReportFilterFields}
        updateFilterParams={(values) => {
          setFilterValues({ ...values });
        }}
        onSubmit={downloadCsv}
        defaultValues={defaultFilterValues}
        hidden={false}
        showFilterVisibilityToggler={false}
        showSearchField={false}
        allowEmptySubmit
        customSubmitButtonDefaultLabel="Load table"
        customSubmitButtonLabel="react.report.reorder.loadTable.label"
        formProps={{
          categories,
          tags,
          locations,
          translate,
        }}
      />
    </ListFilterFormWrapper>
  );
};

export default ReorderReportFilters;
