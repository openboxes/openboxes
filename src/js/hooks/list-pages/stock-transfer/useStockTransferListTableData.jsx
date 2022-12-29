import { useCallback, useState } from 'react';

import _ from 'lodash';
import queryString from 'query-string';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import useTableData from 'hooks/list-pages/useTableData';
import apiClient from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

const useStockTransferListTableData = (filterParams) => {
  const { sourceRef, tableRef, fireFetchData } = useTableData(filterParams);

  const [loading, setLoading] = useState(false);
  const [tableData, setTableData] = useState({
    stockTransfersData: [],
    loading: false,
    pages: -1,
    totalCount: 0,
    currentParams: {},
  });

  const dispatch = useDispatch();
  const { translate, currentLocation } = useSelector(state => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
    currentLocation: state.session.currentLocation,
  }));

  const deleteStockTransfer = (id) => {
    dispatch(showSpinner());
    apiClient.delete(`/openboxes/api/stockTransfers/${id}`)
      .then((res) => {
        if (res.status === 204) {
          dispatch(hideSpinner());
          const successMessage = translate('react.stockTransfer.delete.success.label', 'Stock transfer has been deleted successfully');
          Alert.success(successMessage);
        }
      }).finally(() => {
        dispatch(hideSpinner());
        fireFetchData();
      });
  };

  const deleteHandler = (id) => {
    confirmAlert({
      title: translate('react.default.areYouSure.label', 'Are you sure?'),
      message: translate(
        'react.stockTransfer.delete.confirm.label',
        'Are you sure you want to delete this stock transfer?',
      ),
      buttons: [
        {
          label: translate('react.default.yes.label', 'Yes'),
          onClick: () => deleteStockTransfer(id),
        },
        {
          label: translate('react.default.no.label', 'No'),
        },
      ],
    });
  };

  const onFetchHandler = useCallback((tableState) => {
    if (!_.isEmpty(filterParams)) {
      const offset = tableState.page > 0 ? (tableState.page) * tableState.pageSize : 0;
      const sortingParams = tableState.sorted.length > 0 ?
        {
          sort: tableState.sorted[0].id,
          order: tableState.sorted[0].desc ? 'desc' : 'asc',
        } :
        {
          sort: 'dateCreated',
          order: 'desc',
        };

      const params = _.omitBy({
        location: currentLocation?.id,
        offset: `${offset}`,
        max: `${tableState.pageSize}`,
        ...sortingParams,
        ...filterParams,
        createdBy: filterParams.createdBy?.id,
        status: filterParams.status && filterParams.status.map(({ value }) => value),
      }, _.isEmpty);

      // Fetch data
      setLoading(true);
      apiClient.get('/openboxes/api/stockTransfers', {
        params,
        paramsSerializer: parameters => queryString.stringify(parameters),
        cancelToken: sourceRef.current?.token,
      })
        .then((res) => {
          setTableData({
            stockTransfersData: res.data.data,
            totalCount: res.data.totalCount,
            pages: Math.ceil(res.data.totalCount / tableState.pageSize),
            currentParams: params,
          });
        })
        .catch(() => Promise.reject(new Error(translate('react.stockTransfer.fetch.fail.label', 'Unable to fetch stock transfers'))))
        .finally(() => setLoading(false));
    }
  }, [filterParams]);

  return {
    onFetchHandler,
    deleteHandler,
    loading,
    tableData,
    tableRef,
  };
};

export default useStockTransferListTableData;
