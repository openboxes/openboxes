import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';

import FilterForm from 'components/Filter/FilterForm';
import CheckboxField from 'components/form-elements/CheckboxField';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import apiClient from 'utils/apiClient';

const filterFields = {
  categoryId: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'Category',
      showLabelTooltip: true,
      multi: true,
      closeMenuOnSelect: false,
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
      closeMenuOnSelect: false,
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
      closeMenuOnSelect: false,
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

const ProductsListFilters = ({
  setFilterParams,
}) => {
  const [categories, setCategories] = useState([]);
  const [catalogs, setCatalogs] = useState([]);
  const [tags, setTags] = useState([]);

  const fetchProductsCategories = () => {
    apiClient.get('/openboxes/api/categoryOptions').then((res) => {
      setCategories(res.data.data);
    });
  };

  const fetchProductsCatalogs = () => {
    apiClient.get('/openboxes/api/catalogOptions').then((res) => {
      setCatalogs(res.data.data);
    });
  };

  const fetchProductsTags = () => {
    apiClient.get('/openboxes/api/tagOptions').then((res) => {
      setTags(res.data.data);
    });
  };

  useEffect(() => {
    if (!categories || categories.length === 0) {
      fetchProductsCategories();
    }
    if (!catalogs || catalogs.length === 0) {
      fetchProductsCatalogs();
    }
    if (!tags || tags.length === 0) {
      fetchProductsTags();
    }
  }, []);

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={values => setFilterParams({ ...values })}
        formProps={{
          categories,
          catalogs,
          tags,
        }}
        searchFieldPlaceholder="Search by product name"
        searchFieldId="q"
        allowEmptySubmit
        hidden={false}
      />
    </div>
  );
};


export default ProductsListFilters;

ProductsListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
};
