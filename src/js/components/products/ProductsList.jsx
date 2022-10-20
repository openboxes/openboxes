import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import queryString from 'query-string';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchTranslations } from 'actions';
import filterFields from 'components/products/FilterFields';
import ProductsListFilters from 'components/products/ProductsListFilters';
import ProductsListHeader from 'components/products/ProductsListHeader';
import ProductsListTable from 'components/products/ProductsListTable';
import apiClient from 'utils/apiClient';
import { getParamList, transformFilterParams } from 'utils/list-utils';


const ProductsList = (props) => {
  // Filter params are stored here, to be able to pass them to table component
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [categories, setCategories] = useState([]);
  const [catalogs, setCatalogs] = useState([]);
  const [tags, setTags] = useState([]);
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const fetchProductsCategories = async () => {
    const response = await apiClient.get('/openboxes/api/categoryOptions');
    return response.data.data;
  };

  const fetchProductsCatalogs = async () => {
    const response = await apiClient.get('/openboxes/api/catalogOptions');
    return response.data.data;
  };

  const fetchProductsTags = async () => {
    const response = await apiClient.get('/openboxes/api/tagOptions');
    return response.data.data;
  };

  useEffect(() => {
    props.fetchTranslations(props.locale, 'productsList');
  }, [props.locale]);


  const clearFilterValues = () => {
    const { pathname } = props.history.location;
    props.history.push({ pathname });
  };


  const initializeDefaultFilterValues = async () => {
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    const queryProps = queryString.parse(props.history.location.search);
    // IF VALUE IS IN A SEARCH QUERY SET DEFAULT VALUES
    if (queryProps.includeInactive) {
      defaultValues.includeInactive = queryProps.includeInactive;
    }
    if (queryProps.includeCategoryChildren) {
      defaultValues.includeCategoryChildren = queryProps.includeCategoryChildren;
    }
    // If there are no values for catalogs, tags or categories
    // then set default filters without waiting for those options to load
    if (!queryProps.catalogId && !queryProps.tagId && !queryProps.categoryId) {
      setDefaultFilterValues(defaultValues);
    }
    const [categoryList, catalogList, tagList] = await Promise.all([
      fetchProductsCategories(),
      fetchProductsCatalogs(),
      fetchProductsTags(),
    ]);
    setCatalogs(catalogList);
    setCategories(categoryList);
    setTags(tagList);

    if (queryProps.catalogId) {
      const catalogIdList = getParamList(queryProps.catalogId);
      defaultValues.catalogId = catalogList
        .filter(({ id }) => catalogIdList.includes(id))
        .map(({ id, label }) => ({
          id, label, name: label, value: id,
        }));
    }
    if (queryProps.tagId) {
      const tagIdList = getParamList(queryProps.tagId);
      defaultValues.tagId = tagList
        .filter(({ id }) => tagIdList.includes(id))
        .map(({ id, label }) => ({
          id, label, name: label, value: id,
        }));
    }
    if (queryProps.categoryId) {
      const categoryIdList = getParamList(queryProps.categoryId);
      defaultValues.categoryId = categoryList
        .filter(({ id }) => categoryIdList.includes(id))
        .map(({ id, label }) => ({
          id, label, name: label, value: id,
        }));
    }

    if (queryProps.catalogId || queryProps.tagId || queryProps.categoryId) {
      setDefaultFilterValues(defaultValues);
    }
    setFiltersInitialized(true);
  };

  useEffect(() => {
    // Don't clear the query params while doing first filter initialization
    // clear the filters only when changing location, but not refreshing page
    if (filtersInitialized) {
      clearFilterValues();
    }
  }, [props.currentLocation?.id]);

  useEffect(() => {
    if (props.currentLocation?.id) {
      initializeDefaultFilterValues();
    }
  }, [props.currentLocation?.id]);

  const setFilterValues = (values) => {
    const filterAccessors = {
      includeInactive: { name: 'includeInactive' },
      includeCategoryChildren: { name: 'includeCategoryChildren' },
      catalogId: { name: 'catalogId', accessor: 'id' },
      tagId: { name: 'tagId', accessor: 'id' },
      categoryId: { name: 'categoryId', accessor: 'id' },
    };
    const transformedParams = transformFilterParams(values, filterAccessors);
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = props.history.location;
    if (Object.keys(values).length > 0) {
      props.history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  };

  return (
    <div className="d-flex flex-column list-page-main">
      <ProductsListHeader />
      <ProductsListFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{ categories, catalogs, tags }}
      />
      <ProductsListTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  currentLocation: state.session.currentLocation,
});

const mapDispatchToProps = {
  fetchTranslations,
};

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(ProductsList));


ProductsList.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
    replace: PropTypes.func,
    location: PropTypes.shape({
      search: PropTypes.string,
    }),
  }).isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
  }).isRequired,
};
