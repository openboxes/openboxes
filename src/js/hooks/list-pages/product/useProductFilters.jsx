import { useState } from 'react';

import queryString from 'query-string';
import { useHistory } from 'react-router-dom';

import filterFields from 'components/products/FilterFields';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import { getParamList, transformFilterParams } from 'utils/list-utils';
import {
  fetchProductsCatalogs,
  fetchProductsCategories, fetchProductsGlAccounts,
  fetchProductsTags,
} from 'utils/option-utils';

const useProductFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [categories, setCategories] = useState([]);
  const [catalogs, setCatalogs] = useState([]);
  const [tags, setTags] = useState([]);
  const [glAccounts, setGlAccounts] = useState([]);
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const history = useHistory();

  const clearFilterValues = () => {
    const { pathname } = history.location;
    history.push({ pathname });
  };

  const setDefaultValue = (queryPropsParam, elementsList) => {
    if (queryPropsParam) {
      const idList = getParamList(queryPropsParam);
      return elementsList
        .filter(({ id }) => idList.includes(id))
        .map(({ id, label }) => ({
          id, label, name: label, value: id,
        }));
    }
    return null;
  };

  const initializeDefaultFilterValues = async () => {
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    const queryProps = queryString.parse(history.location.search);

    const {
      catalogId, tagId, categoryId, glAccountsId,
    } = queryProps;

    // IF VALUE IS IN A SEARCH QUERY SET DEFAULT VALUES
    if (queryProps.includeInactive) {
      defaultValues.includeInactive = queryProps.includeInactive;
    }
    if (queryProps.includeCategoryChildren) {
      defaultValues.includeCategoryChildren = queryProps.includeCategoryChildren;
    }
    // If there are no values for catalogs, tags, glAccounts or categories
    // then set default filters without waiting for those options to load
    if (!catalogId && !tagId && !categoryId && !glAccountsId) {
      setDefaultFilterValues(defaultValues);
    }
    const [categoryList, catalogList, tagList, glAccountsList] = await Promise.all([
      fetchProductsCategories(),
      fetchProductsCatalogs(),
      fetchProductsTags(),
      fetchProductsGlAccounts(),
    ]);
    setCatalogs(catalogList);
    setCategories(categoryList);
    setTags(tagList);
    setGlAccounts(glAccountsList);

    defaultValues.catalogId = setDefaultValue(catalogId, catalogList);
    defaultValues.tagId = setDefaultValue(tagId, tagList);
    defaultValues.categoryId = setDefaultValue(categoryId, categoryList);
    defaultValues.glAccountsId = setDefaultValue(glAccountsId, glAccountsList);

    if (catalogId || tagId || categoryId || glAccountsId) {
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
      glAccountsId: { name: 'glAccountsId', accessor: 'id' },
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
    defaultFilterValues, setFilterValues, categories, catalogs, tags, glAccounts, filterParams,
  };
};

export default useProductFilters;
