import _ from 'lodash';

import { INVOICE_API } from 'api/urls';
import useTableData from 'hooks/list-pages/useTableData';

const useInvoiceListTableData = (filterParams) => {
  const errorMessageId = 'react.invoice.error.fetching.label';
  const defaultErrorMessage = 'Unable to fetch invoices';
  const defaultSorting = {
    sort: 'dateInvoiced',
    order: 'desc',
  };

  const getParams = ({
    offset,
    state,
    sortingParams,
  }) => {
    const {
      status, invoiceTypeCode, vendor, createdBy, buyerOrganization,
    } = filterParams;
    return _.omitBy({
      offset: `${offset}`,
      max: `${state.pageSize}`,
      ...sortingParams,
      ...filterParams,
      status: status?.value,
      invoiceTypeCode: invoiceTypeCode?.id,
      vendor: vendor?.id,
      createdBy: createdBy?.id,
      buyerOrganization: buyerOrganization?.id,
    }, _.isEmpty);
  };

  const {
    tableRef,
    loading,
    tableData,
    onFetchHandler,
  } = useTableData({
    filterParams,
    url: INVOICE_API,
    errorMessageId,
    defaultErrorMessage,
    defaultSorting,
    getParams,
  });

  return {
    tableRef, tableData, loading, onFetchHandler,
  };
};

export default useInvoiceListTableData;
