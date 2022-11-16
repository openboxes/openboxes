import React from 'react';

import PropTypes from 'prop-types';

import FilterForm from 'components/Filter/FilterForm';

const StockListFilters = ({
  setFilterParams, filterFields, defaultValues, formProps,
}) => (
  <div className="d-flex flex-column list-page-filters">
    <FilterForm
      searchFieldId="q"
      filterFields={filterFields}
      updateFilterParams={values => setFilterParams({ ...values })}
      formProps={formProps}
      allowEmptySubmit
      searchFieldPlaceholder="react.stocklists.filters.search.placeholder.label"
      searchFieldDefaultPlaceholder="Search by stocklist name"
      hidden={false}
      defaultValues={defaultValues}
    />
  </div>
);

export default StockListFilters;

StockListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
  filterFields: PropTypes.shape({}).isRequired,
  formProps: PropTypes.shape({}).isRequired,
};
