import { useCallback, useState } from 'react';

import fileDownload from 'js-file-download';
import _ from 'lodash';
import queryString from 'query-string';
import { getTranslate } from 'react-localize-redux';
import { useSelector } from 'react-redux';

import useTableData from 'hooks/useTableData';
import apiClient from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

const useProductsListTableData = (filterParams) => {
  const { tableRef } = useTableData(filterParams);
  const [loading, setLoading] = useState(false);
  const [tableData, setTableData] = useState({
    productsData: [],
    pages: -1,
    totalCount: 0,
    currentParams: {},
  });

  const { translate } = useSelector(state => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  const onFetchHandler = useCallback((tableState) => {
    if (!_.isEmpty(filterParams)) {
      const offset = tableState.page > 0 ? (tableState.page) * tableState.pageSize : 0;
      const sortingParams = tableState.sorted.length > 0 ?
        {
          sort: tableState.sorted[0].id,
          order: tableState.sorted[0].desc ? 'desc' : 'asc',
        } :
        {
          sort: 'lastUpdated',
          order: 'desc',
        };

      const params = _.omitBy({
        offset: `${offset}`,
        max: `${tableState.pageSize}`,
        ...sortingParams,
        ...filterParams,
        catalogId: filterParams.catalogId && filterParams.catalogId.map(({ id }) => id),
        categoryId: filterParams.categoryId && filterParams.categoryId.map(({ id }) => id),
        tagId: filterParams.tagId && filterParams.tagId.map(({ id }) => id),
      }, (val) => {
        if (typeof val === 'boolean') {
          return !val;
        }
        return _.isEmpty(val);
      });

      // Fetch data
      setLoading(true);
      apiClient.get('/openboxes/api/products', {
        params,
        paramsSerializer: parameters => queryString.stringify(parameters),
      })
        .then((res) => {
          setTableData({
            productsData: res.data.data,
            totalCount: res.data.totalCount,
            pages: Math.ceil(res.data.totalCount / tableState.pageSize),
            currentParams: params,
          });
          setLoading(false);
        })
        .catch(() => {
          setLoading(false);
          return Promise.reject(new Error(translate('react.productsList.fetch.fail.label', 'Unable to fetch products')));
        });
    }
  }, [filterParams]);

  const exportProducts = (allProducts = false, withAttributes = false) => {
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

    apiClient.get('/openboxes/api/products', {
      params: params(),
      paramsSerializer: parameters => queryString.stringify(parameters),
    })
      .then((res) => {
        const date = new Date();
        const [month, day, year] = [date.getMonth(), date.getDate(), date.getFullYear()];
        const [hour, minutes, seconds] = [date.getHours(), date.getMinutes(), date.getSeconds()];
        fileDownload(res.data, `Products-${year}${month}${day}-${hour}${minutes}${seconds}`, 'text/csv');
      });
  };
  return {
    tableData, tableRef, loading, onFetchHandler, exportProducts,
  };
};

export default useProductsListTableData;
