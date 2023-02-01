import { useCallback, useEffect, useState } from 'react';

import queryString from 'query-string';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { fetchInvoiceStatuses, fetchInvoiceTypeCodes, fetchSuppliers } from 'actions';
import filterFields from 'components/invoice/FilterFields';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import { transformFilterParams } from 'utils/list-utils';
import { fetchUserById } from 'utils/option-utils';
import { translateWithDefaultMessage } from 'utils/Translate';


const useInvoiceFilters = ({ setFilterParams }) => {
  const {
    statuses,
    suppliers,
    typeCodes,
    currentLocation,
    currentUser,
    currentLocale,
  } = useSelector(state => ({
    statuses: state.invoices.statuses,
    suppliers: state.organizations.suppliers,
    typeCodes: state.invoices.typeCodes,
    currentLocation: state.session.currentLocation,
    currentUser: state.session.user,
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
    shouldRebuildParams: state.filterForm.shouldRebuildParams,
    currentLocale: state.session.activeLanguage,
  }));
  const [defaultValues, setDefaultValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const dispatch = useDispatch();
  const history = useHistory();

  const clearFilterValues = () => {
    const { pathname } = history.location;
    history.push({ pathname });
  };

  const initializeDefaultFilterValues = async () => {
    // Avoid unnecessary re-fetches if getAppContext triggers fetching session info
    // but currentLocation doesn't change
    const initialEmptyValues = Object.keys(filterFields).reduce((acc, key) => {
      if (!acc[key]) return { ...acc, [key]: '' };
      return acc;
    }, {});
    const queryProps = queryString.parse(history.location.search);

    // IF VALUE IS IN A SEARCH QUERY SET DEFAULT VALUES
    if (queryProps.status) {
      initialEmptyValues.status = statuses
        .find(({ value }) => value === queryProps.status);
    }
    if (queryProps.vendor) {
      initialEmptyValues.vendor = suppliers.find(({ value }) => value === queryProps.vendor);
    }
    if (queryProps.invoiceTypeCode) {
      initialEmptyValues.invoiceTypeCode =
          typeCodes.find(({ value }) => value === queryProps.invoiceTypeCode);
    }
    if (queryProps.dateInvoiced) {
      initialEmptyValues.dateInvoiced = queryProps.dateInvoiced;
    }
    if (queryProps.createdBy) {
      initialEmptyValues.createdBy = queryProps.createdBy === currentUser?.id
        ? currentUser
        : await fetchUserById(queryProps.createdBy);
    }

    setDefaultValues({
      ...initialEmptyValues,
      buyerOrganization: {
        id: currentLocation?.organization?.id,
        value: currentLocation?.organization?.id,
        name: currentLocation?.organization?.name,
        label: currentLocation?.organization?.name,
      },
    });
    setFiltersInitialized(true);
  };

  // Custom hook for changing location/filters rebuilding logic
  useCommonFiltersCleaner({ initializeDefaultFilterValues, clearFilterValues, filtersInitialized });

  useEffect(() => {
    // TODO: When having full React, if once fetched, fetch only if a current language differs
    // TODO: from the language, that we were fetching this for
    dispatch(fetchInvoiceStatuses());
    dispatch(fetchInvoiceTypeCodes());
  }, [currentLocale]);

  useEffect(() => {
    // TODO: If editing organizations is in React,
    //  fetch only if length === 0, as edit would should force refetch anyway
    dispatch(fetchSuppliers());
  }, []);

  const setFilterValues = useCallback((values) => {
    const filterAccessors = {
      buyerOrganization: { name: 'buyerOrganization', accessor: 'id' },
      status: { name: 'status', accessor: 'id' },
      vendor: { name: 'vendor', accessor: 'id' },
      invoiceTypeCode: { name: 'invoiceTypeCode', accessor: 'id' },
      dateInvoiced: { name: 'dateInvoiced' },
      createdBy: { name: 'createdBy', accessor: 'id' },
    };

    if (Object.keys(values).length) {
      const transformedParams = transformFilterParams(values, filterAccessors);
      const queryFilterParams = queryString.stringify(transformedParams);
      const { pathname } = history.location;
      history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  }, []);

  return { defaultValues, setFilterValues };
};

export default useInvoiceFilters;
