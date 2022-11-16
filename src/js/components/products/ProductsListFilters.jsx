import React from 'react';

import PropTypes from 'prop-types';

import FilterForm from 'components/Filter/FilterForm';

const ProductsListFilters = ({
  setFilterParams,
  filterFields,
  defaultValues,
  formProps,
}) => (
  <div className="d-flex flex-column list-page-filters">
    <FilterForm
      filterFields={filterFields}
      updateFilterParams={values => setFilterParams({ ...values })}
      formProps={formProps}
      searchFieldPlaceholder="react.productsList.filters.search.placeholder.label"
      searchFieldDefaultPlaceholder="Search by product name"
      searchFieldId="q"
      allowEmptySubmit
      hidden={false}
      defaultValues={defaultValues}
    />
  </div>
);

export default ProductsListFilters;

ProductsListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  filterFields: PropTypes.shape({}).isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
  formProps: PropTypes.shape({}).isRequired,
};
