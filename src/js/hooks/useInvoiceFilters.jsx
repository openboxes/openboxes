import { useCallback, useEffect } from 'react';

import queryString from 'query-string';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { fetchInvoiceStatuses, fetchInvoiceTypeCodes, fetchSuppliers } from 'actions';
import filterFields from 'components/invoice/FilterFields';
import apiClient from 'utils/apiClient';
import { transformFilterParams } from 'utils/list-utils';
import { translateWithDefaultMessage } from 'utils/Translate';

const useInvoiceFilters = ({ setDefaultValues, history, setFilterParams }) => {
  const {
    statuses, suppliers, typeCodes, currentLocation, currentUser, translate,
  } = useSelector(state => ({
    statuses: state.invoices.statuses,
    suppliers: state.organizations.suppliers,
    typeCodes: state.invoices.typeCodes,
    currentLocation: state.session.currentLocation,
    currentUser: state.session.user,
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  const dispatch = useDispatch();

  const fetchUserById = useCallback(async (id) => {
    try {
      const response = await apiClient.get(`/openboxes/api/generic/person/${id}`);
      return response.data?.data;
    } catch (e) {
      Alert.error(translate(
        'react.invoice.error.userFetching.label',
        'Could not fetch user for createdBy filter',
      ));
    }
    return null;
  }, []);

  useEffect(() => {
    (async () => {
      // Avoid unnecessary re-fetches if getAppContext triggers fetching session info
      // but currentLocation doesn't change
      if (currentLocation?.id) {
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
      }
    })();
  }, [currentLocation?.id]);

  useEffect(() => {
    // If statuses or invoice type codes not yet in store, fetch them
    if (!statuses || !statuses.length) {
      dispatch(fetchInvoiceStatuses());
    }
    if (!typeCodes || !typeCodes.length) {
      dispatch(fetchInvoiceTypeCodes());
    }

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

  return { setFilterValues };
};

export default useInvoiceFilters;
