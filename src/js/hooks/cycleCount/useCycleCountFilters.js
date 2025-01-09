import { useState } from 'react';

import queryString from 'query-string';
import { useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import cycleCountFilterFields from 'components/cycleCount/CycleCountFilterFields';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import { getParamList, transformFilterParams } from 'utils/list-utils';
import {
  fetchBins,
  fetchProductsCatalogs,
  fetchProductsCategories,
  fetchProductsTags,
} from 'utils/option-utils';

const useCycleCountFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [dateLastCount] = useState(null);
  const [categories, setCategories] = useState([]);
  const [internalLocations, setInternalLocations] = useState([]);
  const [tags, setTags] = useState([]);
  const [catalogs, setCatalogs] = useState([]);
  const [abcClasses] = useState([]);
  const [negativeQuantity] = useState(false);
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const {
    currentLocation,
  } = useSelector((state) => ({
    currentLocation: state.session.currentLocation,
  }));

  const history = useHistory();

  const clearFilterValues = () => {
    const queryProps = queryString.parse(history.location.search);
    const defaultValues = Object.keys(cycleCountFilterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), { tab: queryProps.tab });
    const transformedParams = transformFilterParams(defaultValues, {
      tab: { name: 'tab' },
    });
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = history.location;
    history.push({ pathname, search: queryFilterParams });
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
    const queryProps = queryString.parse(history.location.search);

    const defaultValues = Object.keys(cycleCountFilterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), { tab: queryProps.tab });

    if (queryProps.negativeQuantity) {
      defaultValues.negativeQuantity = queryProps.negativeQuantity;
    }

    if (queryProps.dateLastCount) {
      defaultValues.dateLastCount = queryProps.dateLastCount;
    }

    const [
      categoryList,
      tagList,
      catalogList,
      binList,
    ] = await Promise.all([
      fetchProductsCategories(),
      fetchProductsTags(),
      fetchProductsCatalogs(),
      fetchBins(currentLocation?.id),
    ]);

    setCategories(categoryList);
    setCatalogs(catalogList);
    setTags(tagList);
    setInternalLocations(binList);

    defaultValues.catalogs = setDefaultValue(queryProps.catalogs, catalogList);
    defaultValues.tags = setDefaultValue(queryProps.tags, tagList);
    defaultValues.categories = setDefaultValue(queryProps.categories, categoryList);
    defaultValues.internalLocations = setDefaultValue(queryProps.internalLocations, binList);

    setDefaultFilterValues(defaultValues);
    setFiltersInitialized(true);
  };

  useCommonFiltersCleaner({ filtersInitialized, initializeDefaultFilterValues, clearFilterValues });

  const setFilterValues = (values) => {
    const filterAccessors = {
      dateLastCount: { name: 'dateLastCount' },
      categories: { name: 'categories', accessor: 'id' },
      internalLocations: { name: 'internalLocations', accessor: 'id' },
      tags: { name: 'tags', accessor: 'id' },
      catalogs: { name: 'catalogs', accessor: 'id' },
      abcClasses: { name: 'abcClasses' },
      negativeQuantity: { name: 'negativeQuantity' },
      tab: { name: 'tab' },
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
    defaultFilterValues,
    setFilterValues,
    categories,
    catalogs,
    tags,
    filterParams,
    dateLastCount,
    internalLocations,
    abcClasses,
    negativeQuantity,
  };
};

export default useCycleCountFilters;
