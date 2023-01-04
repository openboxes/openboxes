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
  const messageId = 'react.stockMovement.outbound.fetching.error';
  const defaultMessage = 'Unable to fetch outbound movements';

  const getParams = (offset, currentLocation, state, sortingParams) => _.omitBy({
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
  const {
    tableRef,
    fireFetchData,
    loading,
    tableData,
    onFetchHandler,
  } = useTableData({
    filterParams,
    url: STOCK_MOVEMENT_API,
    messageId,
    defaultMessage,
    getParams,
  });

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
