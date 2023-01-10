import { useState } from 'react';

import queryString from 'query-string';
import { useHistory } from 'react-router-dom';

import filterFields from 'components/products/FilterFields';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import { getParamList, transformFilterParams } from 'utils/list-utils';
import {
  fetchProductsCatalogs,
  fetchProductsCategories,
  fetchProductsTags,
} from 'utils/option-utils';

const useProductFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [categories, setCategories] = useState([]);
  const [catalogs, setCatalogs] = useState([]);
  const [tags, setTags] = useState([]);
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const history = useHistory();

  const clearFilterValues = () => {
    const { pathname } = history.location;
    history.push({ pathname });
  };

  const initializeDefaultFilterValues = async () => {
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    const queryProps = queryString.parse(history.location.search);
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

  // Custom hook for changing location/filters rebuilding logic
  useCommonFiltersCleaner({ filtersInitialized, initializeDefaultFilterValues, clearFilterValues });

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
    const { pathname } = history.location;
    if (Object.keys(values).length) {
      history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  };

  return {
    defaultFilterValues, setFilterValues, categories, catalogs, tags, filterParams,
  };
};

export default useProductFilters;
