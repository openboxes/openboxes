import { useEffect } from 'react';

import queryString from 'query-string';
import { useSelector } from 'react-redux';

import { fetchPreferenceTypes } from 'actions';
import filterFields from 'components/productSupplier/FilterFields';
import { DETAILS_TAB } from 'consts/productSupplierList';
import useCommonFilters from 'hooks/list-pages/useCommonFilters';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import { clearQueryParams, transformFilterParams } from 'utils/list-utils';
import { fetchOrganization, fetchProduct } from 'utils/option-utils';

const useProductSupplierFilters = (ignoreClearFilters) => {
  const {
    filterParams,
    setFilterParams,
    defaultFilterValues,
    setDefaultFilterValues,
    filtersInitialized,
    setFiltersInitialized,
    history,
    dispatch,
  } = useCommonFilters();

  const {
    preferenceTypes,
  } = useSelector((state) => ({
    preferenceTypes: state.productSupplier.preferenceTypes,
  }));

  useEffect(() => {
    const controller = new AbortController();
    const config = {
      signal: controller.signal,
      params: {
        includeNone: true,
      },
    };
    dispatch(fetchPreferenceTypes(config));

    return () => {
      controller.abort();
    };
  }, []);

  const clearFilterValues = () => {
    const { pathname, search } = history.location;
    const queryParams = queryString.parse(search);
    const clearedParams = clearQueryParams({ fieldsToIgnore: ignoreClearFilters, queryParams });
    history.push({ pathname, search: clearedParams });
  };

  const initializeDefaultFilterValues = async () => {
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({
        ...acc,
        [key]: '',
      }), {});

    if (!defaultValues.tab) {
      defaultValues.tab = DETAILS_TAB;
    }
    const queryProps = queryString.parse(history.location.search);

    if (queryProps.searchTerm) {
      defaultValues.searchTerm = queryProps.searchTerm;
    }
    if (queryProps.product) {
      const product = await fetchProduct(queryProps.product);
      if (product) {
        product.label = `${product.productCode} - ${product.displayName ?? product.name}`;
        defaultValues.product = product;
      }
    }
    if (queryProps.supplier) {
      const supplier = await fetchOrganization(queryProps.supplier);
      if (supplier) {
        supplier.label = `${supplier.code} ${supplier.name}`;
        defaultValues.supplier = supplier;
      }
    }
    if (queryProps.createdFrom) {
      defaultValues.createdFrom = queryProps.createdFrom;
    }
    if (queryProps.createdTo) {
      defaultValues.createdTo = queryProps.createdTo;
    }
    if (queryProps.active) {
      defaultValues.active = queryProps.active;
    }
    if (queryProps.defaultPreferenceTypes) {
      defaultValues.defaultPreferenceTypes = preferenceTypes
        .filter(({ id }) => queryProps?.defaultPreferenceTypes?.includes(id));
    }

    setDefaultFilterValues(defaultValues);
    setFiltersInitialized(true);
  };

  const setFilterValues = (values) => {
    const filterAccessors = {
      searchTerm: { name: 'searchTerm' },
      product: { name: 'product', accessor: 'id' },
      tab: { name: 'tab' },
      supplier: { name: 'supplier', accessor: 'id' },
      createdFrom: { name: 'createdFrom' },
      createdTo: { name: 'createdTo' },
      defaultPreferenceTypes: { name: 'defaultPreferenceTypes', accessor: 'id' },
      includeInactive: { name: 'includeInactive' },
    };
    const transformedParams = transformFilterParams(values, filterAccessors);
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = history.location;
    if (Object.keys(values).length) {
      history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  };

  // Custom hook for changing location/filters rebuilding logic
  useCommonFiltersCleaner({ filtersInitialized, initializeDefaultFilterValues, clearFilterValues });

  return {
    defaultFilterValues,
    setFilterValues,
    filterParams,
  };
};

export default useProductSupplierFilters;
