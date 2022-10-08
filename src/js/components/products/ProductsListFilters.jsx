import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import apiClient from 'utils/apiClient';


const ProductsListFilters = ({
  setFilterParams,
  filterFields,
  defaultValues,
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
  filterFields: PropTypes.shape({}).isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
};
