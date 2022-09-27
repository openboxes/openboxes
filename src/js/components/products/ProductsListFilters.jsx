import React, { useEffect, useReducer } from 'react';

import PropTypes from 'prop-types';

import FilterForm from 'components/Filter/FilterForm';
import CheckboxField from 'components/form-elements/CheckboxField';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import { fetchProductsCatalogs, fetchProductsCategories, fetchProductsTags } from 'components/products/actions';
import productsFiltersReducer from 'components/products/reducers/productsFiltersReducer';

const filterFields = {
  categoryId: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'Category',
      showLabelTooltip: true,
      multi: true,
    },
    getDynamicAttr: ({ categories }) => ({
      options: categories,
    }),
  },
  includeCategoryChildren: {
    type: CheckboxField,
    label: 'react.productsList.includeSubcategories.label',
    defaultMessage: 'Include all products in all subcategories',
    attributes: {
      filterElement: true,
    },
  },
  catalogId: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'Formulary',
      showLabelTooltip: true,
      multi: true,
    },
    getDynamicAttr: ({ catalogs }) => ({
      options: catalogs,
    }),
  },
  tagId: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'Tags',
      showLabelTooltip: true,
      multi: true,
    },
    getDynamicAttr: ({ tags }) => ({
      options: tags,
    }),
  },
  includeInactive: {
    type: CheckboxField,
    label: 'react.productsList.includeInactive.label',
    defaultMessage: 'Include inactive',
    attributes: {
      filterElement: true,
    },
  },
};

const INITIAL_STATE = {
  categories: [],
  catalogs: [],
  tags: [],
};

const ProductsListFilters = ({
  setFilterParams,
}) => {
  const [state, dispatch] = useReducer(productsFiltersReducer, INITIAL_STATE);

  useEffect(() => {
    if (!state.categories || state.categories.length === 0) {
      fetchProductsCategories(dispatch);
    }
    if (!state.catalogs || state.catalogs.length === 0) {
      fetchProductsCatalogs(dispatch);
    }
    if (!state.tags || state.tags.length === 0) {
      fetchProductsTags(dispatch);
    }
  }, []);

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        filterFields={filterFields}
        onSubmit={values => setFilterParams({ ...values })}
        formProps={{
          categories: state.categories,
          catalogs: state.catalogs,
          tags: state.tags,
        }}
        searchFieldPlaceholder="Search by name"
        searchFieldId="q"
        allowEmptySubmit
      />
    </div>
  );
};


export default ProductsListFilters;

ProductsListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
};
