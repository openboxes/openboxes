import _ from 'lodash';
import { useDispatch } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import invoiceApi from 'api/services/InvoiceApi';
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

  const dispatch = useDispatch();

  const downloadInvoices = async (invoiceItems = false) => {
    try {
      dispatch(showSpinner());
      const params = {
        ..._.omit(tableData.currentParams, 'offset', 'max'),
        invoiceItems,
      };
      await invoiceApi.downloadInvoices(params);
    } finally {
      dispatch(hideSpinner());
    }
  };

  return {
    tableRef,
    tableData,
    loading,
    onFetchHandler,
    downloadInvoices,
  };
};

export default useInvoiceListTableData;
