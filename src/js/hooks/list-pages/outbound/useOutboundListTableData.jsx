import { useEffect } from 'react';

import _ from 'lodash';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { fetchRequisitionStatusCodes, hideSpinner, showSpinner } from 'actions';
import stockMovementApi from 'api/services/StockMovementApi';
import { STOCK_MOVEMENT_API, STOCK_MOVEMENT_PENDING_SHIPMENT_ITEMS } from 'api/urls';
import useTableData from 'hooks/list-pages/useTableData';
import exportFileFromAPI from 'utils/file-download-util';
import { translateWithDefaultMessage } from 'utils/Translate';

const useOutboundListTableData = (filterParams) => {
  const errorMessageId = 'react.stockMovement.outbound.fetching.error';
  const defaultErrorMessage = 'Unable to fetch outbound movements';

  const getParams = ({
    offset,
    state,
    sortingParams,
  }) => {
    const {
      requisitionStatusCode,
      requestType,
      origin,
      destination,
      requestedBy,
      createdBy,
      updatedBy,
      shipmentType,
    } = filterParams;
    return _.omitBy({
      ...filterParams,
      offset: `${offset}`,
      max: `${state.pageSize}`,
      requisitionStatusCode: requisitionStatusCode && requisitionStatusCode.map(({ id }) => id),
      requestType: requestType?.value,
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
  const { isRequisitionStatusesFetched, requisitionStatuses, translate } = useSelector(state => ({
    isRequisitionStatusesFetched: state.requisitionStatuses.fetched,
    requisitionStatuses: state.requisitionStatuses.data,
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  useEffect(() => {
    if (!isRequisitionStatusesFetched || !requisitionStatuses.length) {
      dispatch(fetchRequisitionStatusCodes());
    }
  }, []);

  const exportStockMovements = () => {
    exportFileFromAPI({
      url: STOCK_MOVEMENT_API,
      params: tableData.currentParams,
    });
  };

  const exportPendingShipmentItems = () => {
    exportFileFromAPI({
      url: STOCK_MOVEMENT_PENDING_SHIPMENT_ITEMS,
      params: tableData.currentParams,
    });
  };

  const deleteStockMovement = async (id) => {
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
