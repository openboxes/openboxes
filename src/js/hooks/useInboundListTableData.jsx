import { useCallback, useEffect, useState } from 'react';

import _ from 'lodash';
import queryString from 'query-string';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { fetchShipmentStatusCodes, hideSpinner, showSpinner } from 'actions';
import useTableData from 'hooks/useTableData';
import apiClient from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';
import { translateWithDefaultMessage } from 'utils/Translate';

const useInboundListTableData = (filterParams) => {
  const [tableData, setTableData] = useState({
    data: [],
    pages: -1,
    totalCount: 0,
    currentParams: {},
  });
  const [loading, setLoading] = useState(true);

  const { sourceRef, tableRef, fireFetchData } = useTableData(filterParams);

  const dispatch = useDispatch();
  const { isShipmentStatusesFetched, shipmentStatuses, translate } = useSelector(state => ({
    isShipmentStatusesFetched: state.shipmentStatuses.data,
    shipmentStatuses: state.shipmentStatuses.data,
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));


  useEffect(() => {
    if (!isShipmentStatusesFetched || shipmentStatuses.length === 0) {
      dispatch(fetchShipmentStatusCodes());
    }
  }, []);

  const exportStockMovements = () => {
    exportFileFromAPI({
      url: '/openboxes/api/stockMovements',
      params: tableData.currentParams,
    });
  };

  const exportAllIncomingItems = () => {
    exportFileFromAPI({
      url: '/openboxes/api/stockMovements/shippedItems',
      params: tableData.currentParams,
    });
  };

  const onFetchHandler = useCallback((state) => {
    if (!_.isEmpty(filterParams)) {
      const offset = state.page > 0 ? (state.page) * state.pageSize : 0;
      const sortingParams = state.sorted.length > 0 ?
        {
          sort: state.sorted[0].id,
          order: state.sorted[0].desc ? 'desc' : 'asc',
        } : undefined;

      const params = _.omitBy({
        ...filterParams,
        offset: `${offset}`,
        max: `${state.pageSize}`,
        receiptStatusCode: filterParams.receiptStatusCode &&
          filterParams.receiptStatusCode?.map(({ id }) => id),
        origin: filterParams?.origin?.id,
        destination: filterParams?.destination?.id,
        requestedBy: filterParams.requestedBy?.id,
        createdBy: filterParams.createdBy?.id,
        updatedBy: filterParams.updatedBy?.id,
        ...sortingParams,
      }, (value) => {
        if (typeof value === 'object' && _.isEmpty(value)) return true;
        return !value;
      });

      // Fetch data
      setLoading(true);
      apiClient.get('/openboxes/api/stockMovements', {
        paramsSerializer: parameters => queryString.stringify(parameters),
        params,
        cancelToken: sourceRef.current?.token,
      })
        .then((res) => {
          setLoading(false);
          setTableData({
            data: res.data.data,
            pages: Math.ceil(res.data.totalCount / state.pageSize),
            totalCount: res.data.totalCount,
            currentParams: params,
          });
        })
        .catch(() => Promise.reject(new Error(translate('react.stockMovement.inbound.fetching.error', 'Unable to fetch inbound movements'))));
    }
  }, [filterParams]);

  const deleteReturnStockMovement = (id) => {
    dispatch(showSpinner());
    apiClient.delete(`/openboxes/api/stockMovements/${id}`)
      .then((res) => {
        if (res.status === 204) {
          const successMessage = translate(
            'react.stockMovement.deleted.success.message.label',
            'Stock Movement has been deleted successfully',
          );
          Alert.success(successMessage);
          fireFetchData();
        }
      })
      .finally(() => dispatch(hideSpinner()));
  };

  const deleteConfirmAlert = (id) => {
    const confirmButton = {
      label: translate('react.default.yes.label', 'Yes'),
      onClick: () => deleteReturnStockMovement(id),
    };
    const cancelButton = {
      label: translate('react.default.no.label', 'No'),
    };
    confirmAlert({
      title: translate('react.default.areYouSure.label', 'Are you sure?'),
      message: translate(
        'react.stockMovement.areYouSure.label',
        'Are you sure you want to delete this Stock Movement?',
      ),
      buttons: [confirmButton, cancelButton],
    });
  };

  return {
    tableData,
    tableRef,
    loading,
    onFetchHandler,
    exportAllIncomingItems,
    exportStockMovements,
    deleteConfirmAlert,
  };
};

export default useInboundListTableData;
