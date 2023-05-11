import { useEffect } from 'react';

import _ from 'lodash';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { fetchShipmentStatusCodes, hideSpinner, showSpinner } from 'actions';
import stockMovementApi from 'api/services/StockMovementApi';
import { STOCK_MOVEMENT_API, STOCK_MOVEMENT_INCOMING_ITEMS } from 'api/urls';
import useTableData from 'hooks/list-pages/useTableData';
import exportFileFromAPI from 'utils/file-download-util';
import { translateWithDefaultMessage } from 'utils/Translate';

const useInboundListTableData = (filterParams) => {
  const errorMessageId = 'react.stockMovement.inbound.fetching.error';
  const defaultErrorMessage = 'Unable to fetch inbound movements';
  const getParams = ({
    offset,
    state,
    sortingParams,
  }) => {
    const {
      receiptStatusCode, origin, destination, requestedBy, createdBy, updatedBy, shipmentType,
    } = filterParams;
    return _.omitBy({
      ...filterParams,
      offset: `${offset}`,
      max: `${state.pageSize}`,
      receiptStatusCode: receiptStatusCode && receiptStatusCode.map(({ id }) => id),
      origin: origin?.id,
      destination: destination?.id,
      requestedBy: requestedBy?.id,
      createdBy: createdBy?.id,
      updatedBy: updatedBy?.id,
      shipmentType: shipmentType?.map?.(({ id }) => id),
      ...sortingParams,
    }, (value) => {
      if (typeof value === 'object' && _.isEmpty(value)) return true;
      return !value;
    });
  };
  const {
    tableRef,
    fireFetchData,
    loading,
    tableData,
    onFetchHandler,
  } = useTableData({
    filterParams,
    url: STOCK_MOVEMENT_API,
    errorMessageId,
    defaultErrorMessage,
    getParams,
  });

  const dispatch = useDispatch();
  const { isShipmentStatusesFetched, shipmentStatuses, translate } = useSelector(state => ({
    isShipmentStatusesFetched: state.shipmentStatuses.data,
    shipmentStatuses: state.shipmentStatuses.data,
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));


  useEffect(() => {
    if (!isShipmentStatusesFetched || !shipmentStatuses.length) {
      dispatch(fetchShipmentStatusCodes());
    }
  }, []);

  const exportStockMovements = () => {
    exportFileFromAPI({
      url: STOCK_MOVEMENT_API,
      params: tableData.currentParams,
    });
  };

  const exportAllIncomingItems = () => {
    exportFileFromAPI({
      url: STOCK_MOVEMENT_INCOMING_ITEMS,
      params: tableData.currentParams,
    });
  };

  const deleteReturnStockMovement = async (id) => {
    dispatch(showSpinner());
    try {
      const { status } = await stockMovementApi.deleteStockMovement(id);
      if (status === 204) {
        const successMessage = translate(
          'react.stockMovement.deleted.success.message.label',
          'Stock Movement has been deleted successfully',
        );
        Alert.success(successMessage);
        fireFetchData();
      }
    } finally {
      dispatch(hideSpinner());
    }
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
