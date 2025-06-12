import { useState } from 'react';

import moment from 'moment';
import queryString from 'query-string';
import { useDispatch } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { setShouldRebuildFilterParams } from 'actions';
import cycleCountReportingFilterFields from 'components/cycleCountReporting/CycleCountReportingFilterFields';
import { INDICATORS_TAB } from 'consts/cycleCount';
import { DateFormat } from 'consts/timeFormat';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import useQueryParams from 'hooks/useQueryParams';
import { transformFilterParams } from 'utils/list-utils';
import { fetchProduct } from 'utils/option-utils';

const useCycleCountReportingFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [shouldFetch, setShouldFetch] = useState(false);

  const dispatch = useDispatch();
  const history = useHistory();
  const { tab } = useQueryParams();

  const clearFilterValues = () => {
    const queryProps = queryString.parse(history.location.search);
    const defaultValues = Object.keys(cycleCountReportingFilterFields).reduce(
      (acc, key) => ({ ...acc, [key]: '' }),
      { tab: queryProps.tab },
    );
    const transformedParams = transformFilterParams(defaultValues, {
      tab: { name: 'tab' },
    });
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = history.location;
    history.push({ pathname, search: queryFilterParams });
    setShouldFetch(false);
    setFilterParams(queryFilterParams);
  };

  const resetForm = () => {
    clearFilterValues();
    dispatch(setShouldRebuildFilterParams(true));
  };

  const initializeDefaultFilterValues = async () => {
    setIsLoading(true);
    try {
      const queryProps = queryString.parse(history.location.search);
      const defaultValues = Object.keys(cycleCountReportingFilterFields).reduce(
        (acc, key) => ({ ...acc, [key]: '' }),
        { tab: queryProps.tab },
      );

      if (queryProps.startDate || tab === INDICATORS_TAB) {
        // If tab === INDICATORS_TAB and queryProps.startDate is not provided,
        // we want to use data from the last 3 months as the default.
        defaultValues.startDate = queryProps.startDate || moment()
          .subtract(3, 'months').format(DateFormat.DD_MMM_YYYY);
      }
      if (queryProps.endDate || tab === INDICATORS_TAB) {
        // If tab === INDICATORS_TAB and queryProps.endDate is not provided,
        // we want to use the current day as the default.
        defaultValues.endDate = queryProps.endDate
          || moment().format(DateFormat.DD_MMM_YYYY);
      }
      if (queryProps.products) {
        const productIds = Array.isArray(queryProps.products)
          ? queryProps.products
          : [queryProps.products];

        const products = await Promise.all(
          productIds.map((id) => fetchProduct(id)),
        );

        defaultValues.products = products.map((product) => ({
          ...product,
          label: `${product.productCode} - ${product.displayName ?? product.name}`,
          value: product.id,
        }));
      }

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
      startDate: { name: 'startDate' },
      endDate: { name: 'endDate' },
      products: { name: 'products', accessor: 'id' },
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
    isLoading,
    shouldFetch,
    setShouldFetch,
  };
};

export default useCycleCountReportingFilters;
