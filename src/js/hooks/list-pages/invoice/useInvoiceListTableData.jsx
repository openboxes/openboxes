import _ from 'lodash';

import useTableData from 'hooks/list-pages/useTableData';

const useInvoiceListTableData = (filterParams) => {
  const url = '/openboxes/api/invoices';
  const messageId = 'react.invoice.error.fetching.label';
  const defaultMessage = 'Unable to fetch invoices';
  const getSortingParams = state => (state.sorted.length > 0 ?
    {
      sort: state.sorted[0].id,
      order: state.sorted[0].desc ? 'desc' : 'asc',
    } :
    {
      sort: 'dateInvoiced',
      order: 'desc',
    });
  const getParams = (offset, currentLocation, state, sortingParams) => _.omitBy({
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

  const {
    tableRef,
    loading,
    tableData,
    onFetchHandler,
  } = useTableData({
    filterParams,
    url,
    messageId,
    defaultMessage,
    getSortingParams,
    getParams,
  });

  return {
    tableRef, tableData, loading, onFetchHandler,
  };
};

export default useInvoiceListTableData;
