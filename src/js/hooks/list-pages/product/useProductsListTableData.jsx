import fileDownload from 'js-file-download';
import _ from 'lodash';
import queryString from 'query-string';

import productApi from 'api/services/ProductApi';
import { PRODUCT_API } from 'api/urls';
import useTableData from 'hooks/list-pages/useTableData';

const useProductsListTableData = (filterParams) => {
  const errorMessageId = 'react.productsList.fetch.fail.label';
  const defaultErrorMessage = 'Unable to fetch products';
  const defaultSorting = {
    sort: 'lastUpdated',
    order: 'desc',
  };
  const getParams = ({
    offset,
    state,
    sortingParams,
  }) => {
    const {
      catalogId, categoryId, tagId, glAccountsId, productFamilyId,
    } = filterParams;
    return _.omitBy({
      format: 'list',
      offset: `${offset}`,
      max: `${state.pageSize}`,
      ...sortingParams,
      ...filterParams,
      catalogId: catalogId && catalogId.map(({ id }) => id),
      categoryId: categoryId && categoryId.map(({ id }) => id),
      tagId: tagId && tagId.map(({ id }) => id),
      glAccountsId: glAccountsId && glAccountsId.map(({ id }) => id),
      productFamilyId: productFamilyId && productFamilyId.map(({ id }) => id),
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
    tableData,
    onFetchHandler,
  } = useTableData({
    filterParams,
    url: PRODUCT_API,
    errorMessageId,
    defaultErrorMessage,
    defaultSorting,
    getParams,
  });

  const exportProducts = async (allProducts = false, withAttributes = false) => {
    const params = () => {
      if (allProducts) {
        return { format: 'csv' };
      }
      if (withAttributes) {
        return { format: 'csv', includeAttributes: true };
      }
      return {
        ..._.omit(tableData.currentParams, ['offset', 'max']),
        format: 'csv',
      };
    };

    const config = {
      params: params(),
      paramsSerializer: parameters => queryString.stringify(parameters),
    };
    const { data } = await productApi.getProducts(config);
    const date = new Date();
    const [month, day, year] = [date.getMonth(), date.getDate(), date.getFullYear()];
    const [hour, minutes, seconds] = [date.getHours(), date.getMinutes(), date.getSeconds()];
    fileDownload(`\uFEFF${data}`, `Products-${year}${month}${day}-${hour}${minutes}${seconds}`, 'text/csv');
  };
  return {
    tableData, tableRef, loading, onFetchHandler, exportProducts,
  };
};

export default useProductsListTableData;
