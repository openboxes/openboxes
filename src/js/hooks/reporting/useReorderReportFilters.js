import { useState } from 'react';

import _ from 'lodash';
import queryString from 'query-string';
import { useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { getCurrentLocation } from 'selectors';

import { REORDER_REPORT } from 'api/urls';
import filterFields
  from 'components/reporting/reorderReport/ReorderReportFilterFields';
import ActivityCode from 'consts/activityCode';
import {
  EXPIRATION_FILTER,
  getExpiredStockOptions,
  getFilterProductOptions,
} from 'consts/filterOptions';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import useSpinner from 'hooks/useSpinner';
import fileDownloadUtil from 'utils/file-download-util';
import { getParamList, transformFilterParams } from 'utils/list-utils';
import {
  fetchLocations,
  fetchProductsCategories,
  fetchProductsTags,
} from 'utils/option-utils';

const useReorderReportFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});

  const [categories, setCategories] = useState([]);
  const [tags, setTags] = useState([]);
  const [locations, setLocations] = useState([]);

  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const history = useHistory();

  const spinner = useSpinner();

  const currentLocation = useSelector(getCurrentLocation);

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

  const getSelectedOption = (selectedId, optionsList, defaultId) => {
    const selectedOption = optionsList.find(({ id }) => id === selectedId);
    if (selectedOption) {
      return selectedOption;
    }

    if (defaultId) {
      return optionsList.find(({ id }) => id === defaultId);
    }

    return null;
  };

  const initializeDefaultFilterValues = async () => {
    spinner.show();
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    const queryProps = queryString.parse(history.location.search);

    const {
      additionalInventoryLocations,
      expiredStock,
      filterProducts: selectedFilterProducts,
      categories: selectedCategories,
      tags: selectedTags,
    } = queryProps;

    // If there are no values for categories, tags, filter products, expired stock or inventory
    // locations, then set default filters without waiting for those options to load
    if (!selectedCategories
      && !selectedTags
      && !selectedFilterProducts
      && !expiredStock
      && !additionalInventoryLocations) {
      setDefaultFilterValues(defaultValues);
    }

    const [
      categoryList,
      tagList,
      locationList,
    ] = await Promise.all([
      fetchProductsCategories(),
      fetchProductsTags(),
      fetchLocations({ activityCodes: [ActivityCode.MANAGE_INVENTORY] }),
    ]);
    setCategories(categoryList);
    setTags(tagList);
    setLocations(locationList);

    defaultValues.tags = setDefaultValue(selectedTags, tagList);
    defaultValues.categories = setDefaultValue(selectedCategories, categoryList);
    defaultValues.filterProducts = getSelectedOption(
      selectedFilterProducts,
      getFilterProductOptions(),
    );
    defaultValues.expiredStock = getSelectedOption(
      expiredStock,
      getExpiredStockOptions(),
      EXPIRATION_FILTER.REMOVE_EXPIRED_STOCK,
    );
    defaultValues.additionalInventoryLocations = setDefaultValue(
      additionalInventoryLocations,
      locationList,
    );

    if (selectedTags
      || selectedCategories
      || selectedFilterProducts
      || expiredStock
      || additionalInventoryLocations) {
      setDefaultFilterValues(defaultValues);
    }
    setFiltersInitialized(true);
    spinner.hide();
  };

  // Custom hook for changing location/filters rebuilding logic
  useCommonFiltersCleaner({ filtersInitialized, initializeDefaultFilterValues, clearFilterValues });

  const setFilterValues = (values) => {
    const filterAccessors = {
      tags: { name: 'tags', accessor: 'id' },
      categories: { name: 'categories', accessor: 'id' },
      filterProducts: { name: 'filterProducts', accessor: 'id' },
      expiredStock: { name: 'expiredStock', accessor: 'id' },
      additionalInventoryLocations: { name: 'additionalInventoryLocations', accessor: 'id' },
    };
    const transformedParams = transformFilterParams(values, filterAccessors);
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = history.location;
    if (Object.keys(values).length) {
      history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  };

  const getParams = (values) => _.omitBy({
    expiration: values?.expiredStock?.id,
    inventoryLevelStatus: values?.filterProducts?.id,
    categories: values?.categories?.map?.(({ id }) => id),
    tags: values?.tags?.map?.(({ id }) => id),
    additionalLocations: values?.additionalInventoryLocations?.map?.(({ id }) => id),
  }, _.isNil);

  const downloadCsv = async (values) => {
    spinner.show();
    await fileDownloadUtil({
      url: REORDER_REPORT(currentLocation?.id),
      params: getParams(values),
    });
    spinner.hide();
  };

  return {
    defaultFilterValues,
    setFilterValues,
    categories,
    tags,
    locations,
    filterParams,
    downloadCsv,
  };
};

export default useReorderReportFilters;
