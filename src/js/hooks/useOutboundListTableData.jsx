import { useCallback, useEffect, useState } from 'react';

import _ from 'lodash';
import queryString from 'query-string';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { fetchRequisitionStatusCodes, hideSpinner, showSpinner } from 'actions';
import useTableData from 'hooks/useTableData';
import apiClient from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';
import { translateWithDefaultMessage } from 'utils/Translate';

const useOutboundListTableData = (filterParams) => {
  const { sourceRef, tableRef, fireFetchData } = useTableData(filterParams);
  const [tableData, setTableData] = useState({
    data: [],
    pages: -1,
    totalCount: 0,
    currentParams: {},
  });
  const [loading, setLoading] = useState(true);

  const dispatch = useDispatch();
  const { isRequisitionStatusesFetched, requisitionStatuses, translate } = useSelector(state => ({
    isRequisitionStatusesFetched: state.requisitionStatuses.fetched,
    requisitionStatuses: state.requisitionStatuses.data,
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  useEffect(() => {
    if (!isRequisitionStatusesFetched || requisitionStatuses.length === 0) {
      dispatch(fetchRequisitionStatusCodes());
    }
  }, []);

  const exportStockMovements = () => {
    exportFileFromAPI({
      url: '/openboxes/api/stockMovements',
      params: tableData.currentParams,
    });
  };

  const exportPendingShipmentItems = () => {
    exportFileFromAPI({
      url: '/openboxes/api/stockMovements/pendingRequisitionItems',
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
        requisitionStatusCode: filterParams.requisitionStatusCode &&
          filterParams.requisitionStatusCode?.map(({ id }) => id),
        requestType: filterParams?.requestType?.value,
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
        .catch(() => Promise.reject(new Error(translate('react.stockMovement.outbound.fetching.error', 'Unable to fetch outbound movements'))));
    }
  }, [filterParams]);

  const deleteStockMovement = (id) => {
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
      onClick: () => deleteStockMovement(id),
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
    exportStockMovements,
    exportPendingShipmentItems,
    deleteConfirmAlert,
  };
};

export default useOutboundListTableData;
