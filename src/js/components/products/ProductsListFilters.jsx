import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

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
  currentLocation,
}) => {
  const [categories, setCategories] = useState([]);
  const [catalogs, setCatalogs] = useState([]);
  const [tags, setTags] = useState([]);
  const [defaultValues, setDefaultValues] = useState({});

  useEffect(() => {
    // Avoid unnecessary re-fetches if getAppContext triggers fetching session info
    // but currentLocation doesn't change
    // eslint-disable-next-line max-len
    if (currentLocation?.id) {
      const initialEmptyValues = Object.keys(filterFields).reduce((acc, key) => {
        if (!acc[key]) return { ...acc, [key]: '' };
        return acc;
      }, {});
      setDefaultValues({
        ...initialEmptyValues,
      });
    }
  }, [currentLocation?.id]);

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
        defaultValues={defaultValues}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  currentLocation: state.session.currentLocation,
});

export default connect(mapStateToProps)(ProductsListFilters);

ProductsListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
  }).isRequired,
};
