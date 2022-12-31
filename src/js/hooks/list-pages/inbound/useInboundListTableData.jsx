import { useEffect } from 'react';

import _ from 'lodash';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { fetchShipmentStatusCodes, hideSpinner, showSpinner } from 'actions';
import useTableData from 'hooks/list-pages/useTableData';
import apiClient from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';
import { translateWithDefaultMessage } from 'utils/Translate';

const useInboundListTableData = (filterParams) => {
  const url = '/openboxes/api/stockMovements';
  const messageId = 'react.stockMovement.inbound.fetching.error';
  const defaultMessage = 'Unable to fetch inbound movements';
  const getSortingParams = state => (state.sorted.length > 0 ?
    {
      sort: state.sorted[0].id,
      order: state.sorted[0].desc ? 'desc' : 'asc',
    } : undefined);
  const getParams = (offset, currentLocation, state, sortingParams) => _.omitBy({
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
  const {
    tableRef,
    fireFetchData,
    loading,
    tableData,
    onFetchHandler,
  } = useTableData({
    filterParams,
    url,
    messageId,
    defaultMessage,
    getSortingParams,
    getParams,
  });

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
