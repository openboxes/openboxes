import React, { useCallback } from 'react';

import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import RoleType from 'consts/roleType';
import { debounceOrganizationsFetch, debounceProductsFetch } from 'utils/option-utils';
import ListFilterFormWrapper from 'wrappers/ListFilterFormWrapper';

const ProductSupplierListFilters = ({
  filterFields,
  setFilterParams,
  defaultValues,
  ignoreClearFilters,
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

  const debouncedOrganizationsFetch = useCallback(
    debounceOrganizationsFetch(
      debounceTime,
      minSearchLength,
      [RoleType.ROLE_SUPPLIER],
      true,
    ), [debounceTime, minSearchLength],
  );

  return (
    <ListFilterFormWrapper>
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={(values) => setFilterParams({ ...values })}
        formProps={{
          debouncedProductsFetch,
          debouncedOrganizationsFetch,
        }}
        defaultValues={defaultValues}
        allowEmptySubmit
        searchFieldDefaultPlaceholder="Search by source code, product code, supplier code"
        searchFieldPlaceholder="react.productSupplier.searchField.placeholder.label"
        hidden={false}
        ignoreClearFilters={ignoreClearFilters}
      />
    </ListFilterFormWrapper>
  );
};

export default ProductSupplierListFilters;

ProductSupplierListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  filterFields: PropTypes.shape({}).isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
  formProps: PropTypes.shape({}),
  ignoreClearFilters: PropTypes.arrayOf(PropTypes.string).isRequired,
};

ProductSupplierListFilters.defaultProps = {
  formProps: {},
};
