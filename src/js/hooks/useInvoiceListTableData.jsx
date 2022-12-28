import { useCallback, useState } from 'react';

import _ from 'lodash';
import queryString from 'query-string';
import { getTranslate } from 'react-localize-redux';
import { useSelector } from 'react-redux';

import useTableData from 'hooks/useTableData';
import apiClient from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

const useInvoiceListTableData = (filterParams) => {
  const [tableData, setTableData] = useState({
    data: [],
    pages: -1,
    totalData: 0,
  });
  const [loading, setLoading] = useState(true);
  const { sourceRef, tableRef } = useTableData(filterParams);
  const { translate } = useSelector(state => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  const onFetchHandler = useCallback((state) => {
    if (!_.isEmpty(filterParams)) {
      const offset = state.page > 0 ? (state.page) * state.pageSize : 0;
      const sortingParams = state.sorted.length > 0 ?
        {
          sort: state.sorted[0].id,
          order: state.sorted[0].desc ? 'desc' : 'asc',
        } :
        {
          sort: 'dateInvoiced',
          order: 'desc',
        };


      const params = _.omitBy({
        offset: `${offset}`,
        max: `${state.pageSize}`,
        ...sortingParams,
        ...filterParams,
        status: filterParams.status && filterParams.status.value,
        invoiceTypeCode: filterParams.invoiceTypeCode && filterParams.invoiceTypeCode.id,
        vendor: filterParams.vendor && filterParams.vendor.id,
        createdBy: filterParams.createdBy && filterParams.createdBy.id,
        buyerOrganization: filterParams.buyerOrganization && filterParams.buyerOrganization.id,
      }, _.isEmpty);

      // Fetch data
      setLoading(true);
      apiClient.get('/openboxes/api/invoices', {
        params,
        paramsSerializer: parameters => queryString.stringify(parameters),
        cancelToken: sourceRef.current?.token,
      })
        .then((res) => {
          setLoading(false);
          setTableData({
            data: res.data.data,
            pages: Math.ceil(res.data.totalCount / state.pageSize),
            totalData: res.data.totalCount,
          });
        })
        .catch(() => Promise.reject(new Error(translate('react.invoice.error.fetching.label', 'Unable to fetch invoices'))));
    }
  }, [filterParams]);

  return {
    tableRef, tableData, loading, onFetchHandler,
  };
};

export default useInvoiceListTableData;
