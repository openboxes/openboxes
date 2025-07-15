import { useState } from 'react';

import queryString from 'query-string';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { setShouldRebuildFilterParams } from 'actions';
import cycleCountFilterFields from 'components/cycleCount/CycleCountFilterFields';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import useTranslate from 'hooks/useTranslate';
import { groupBinLocationsByZone } from 'utils/groupBinLocationsByZone';
import { getParamList, transformFilterParams } from 'utils/list-utils';
import {
  fetchBins,
  fetchProductClassifications,
  fetchProductsCatalogs,
  fetchProductsCategories,
  fetchProductsTags,
} from 'utils/option-utils';

const useCycleCountFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [dateLastCount] = useState(null);
  const [negativeQuantity] = useState(false);
  const [filtersInitialized, setFiltersInitialized] = useState(false);
  // This boolean ensures that we avoid issues with outdated bin location data
  const [isLoading, setIsLoading] = useState(false);

  const [selectOptions, setSelectOptions] = useState({
    categories: [],
    catalogs: [],
    internalLocations: [],
    tags: [],
    abcClasses: [],
  });

  const {
    currentLocation,
  } = useSelector((state) => ({
    currentLocation: state.session.currentLocation,
  }));

  const dispatch = useDispatch();

  const history = useHistory();
  const translate = useTranslate();

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

  const resetForm = () => {
    clearFilterValues();
    dispatch(setShouldRebuildFilterParams(true));
  };

  const setDefaultValue = (queryPropsParam, elementsList) => {
    if (queryPropsParam) {
      const idList = getParamList(queryPropsParam);
      return elementsList
        .filter(({ id }) => idList.includes(id))
        .map(({ id, label, name }) => ({
          id, label: label ?? name, name: name ?? label, value: id,
        }));
    }
    return null;
  };

  const initializeDefaultFilterValues = async () => {
    setIsLoading(true);

    try {
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
        classificationList,
        binList,
      ] = await Promise.all([
        fetchProductsCategories(),
        fetchProductsTags({ hideNumbers: true }),
        fetchProductsCatalogs({ hideNumbers: true }),
        fetchProductClassifications(currentLocation?.id),
        fetchBins(currentLocation?.id, [], 'sortOrder,locationType,name'),
      ]);

      setSelectOptions({
        categories: categoryList,
        catalogs: catalogList,
        tags: tagList,
        internalLocations: groupBinLocationsByZone(binList, translate),
        abcClasses: classificationList,
      });

      defaultValues.catalogs = setDefaultValue(queryProps.catalogs, catalogList);
      defaultValues.tags = setDefaultValue(queryProps.tags, tagList);
      defaultValues.categories = setDefaultValue(queryProps.categories, categoryList);
      defaultValues.internalLocations = setDefaultValue(queryProps.internalLocations, binList);
      defaultValues.abcClasses = setDefaultValue(queryProps.abcClasses, classificationList);

      setDefaultFilterValues(defaultValues);
      setFiltersInitialized(true);
    } finally {
      setIsLoading(false);
    }
  };

  useCommonFiltersCleaner({
    filtersInitialized,
    initializeDefaultFilterValues,
    clearFilterValues,
  });

  const setFilterValues = (values) => {
    const filterAccessors = {
      dateLastCount: { name: 'dateLastCount' },
      categories: { name: 'categories', accessor: 'id' },
      internalLocations: { name: 'internalLocations', accessor: 'id' },
      tags: { name: 'tags', accessor: 'id' },
      catalogs: { name: 'catalogs', accessor: 'id' },
      abcClasses: { name: 'abcClasses', accessor: 'id' },
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
    resetForm,
    filterParams,
    dateLastCount,
    negativeQuantity,
    isLoading,
    ...selectOptions,
  };
};

export default useCycleCountFilters;
