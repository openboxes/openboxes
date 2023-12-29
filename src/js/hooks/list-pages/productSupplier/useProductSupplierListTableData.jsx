import { useState } from 'react';

import { PRODUCT_SUPPLIER_API } from 'api/urls';
import useTableData from 'hooks/list-pages/useTableData';

const useProductSupplierListTableData = () => {
  const errorMessageId = 'react.productSupplier.error.productSupplierList.label';
  const defaultErrorMessage = 'Unable to fetch product sources';

  const defaultSorting = {
    sort: 'dateCreated',
    order: 'asc',
  };

  // TODO: To be removed after adding filters
  const [filterParams] = useState({ offset: 0, max: 10 });

  const getParams = () => {};

  const {
    tableRef,
    // fireFetchData,
    loading,
    onFetchHandler,
    tableData,
  } = useTableData({
    filterParams,
    url: PRODUCT_SUPPLIER_API,
    errorMessageId,
    defaultErrorMessage,
    defaultSorting,
    getParams,
  });

  return {
    tableData,
    loading,
    tableRef,
    onFetchHandler,
  };
};

export default useProductSupplierListTableData;
