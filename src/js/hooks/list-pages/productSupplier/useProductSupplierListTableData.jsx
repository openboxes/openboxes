import _ from 'lodash';

import { PRODUCT_SUPPLIER_API } from 'api/urls';
import useTableData from 'hooks/list-pages/useTableData';

const useProductSupplierListTableData = (filterParams) => {
  const errorMessageId = 'react.productSupplier.error.productSupplierList.label';
  const defaultErrorMessage = 'Unable to fetch product sources';

  const defaultSorting = {
    sort: 'dateCreated',
    order: 'desc',
  };

  const getParams = ({
    offset,
    state,
    sortingParams,
  }) => {
    const {
      product,
      supplier,
      defaultPreferenceTypes,
    } = filterParams;
    return _.omitBy({
      offset: `${offset}`,
      max: `${state.pageSize}`,
      ...sortingParams,
      ...filterParams,
      product: product?.id,
      supplier: supplier?.id,
      defaultPreferenceTypes: defaultPreferenceTypes?.map?.(({ id }) => id),
    }, (val) => {
      if (typeof val === 'boolean') {
        return !val;
      }
      return _.isEmpty(val);
    });
  };

  const {
    tableRef,
    loading,
    onFetchHandler,
    tableData,
    fireFetchData,
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
    fireFetchData,
  };
};

export default useProductSupplierListTableData;
