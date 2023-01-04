import _ from 'lodash';

import { INVOICE_API } from 'api/urls';
import useTableData from 'hooks/list-pages/useTableData';

const useInvoiceListTableData = (filterParams) => {
  const messageId = 'react.invoice.error.fetching.label';
  const defaultMessage = 'Unable to fetch invoices';
  const defaultSorting = {
    sort: 'dateInvoiced',
    order: 'desc',
  };

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
    url: INVOICE_API,
    messageId,
    defaultMessage,
    defaultSorting,
    getParams,
  });

  return {
    tableRef, tableData, loading, onFetchHandler,
  };
};

export default useInvoiceListTableData;
