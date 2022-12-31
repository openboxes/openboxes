import _ from 'lodash';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import useTableData from 'hooks/list-pages/useTableData';
import apiClient from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';
import { translateWithDefaultMessage } from 'utils/Translate';

const usePurchaseOrderListTableData = (filterParams) => {
  const url = '/openboxes/api/purchaseOrders';
  const messageId = 'react.purchaseOrder.error.purchaseOrderList.label';
  const defaultMessage = 'Unable to fetch purchase orders';
  const getSortingParams = state => (state.sorted.length > 0 ?
    {
      sort: state.sorted[0].id,
      order: state.sorted[0].desc ? 'desc' : 'asc',
    } :
    {
      sort: 'dateOrdered',
      order: 'desc',
    });
  const getParams = (offset, currentLocation, state, sortingParams) => ({
    ..._.omitBy({
      offset: `${offset}`,
      max: `${state.pageSize}`,
      ...sortingParams,
      ...filterParams,
      status: filterParams.status &&
        filterParams.status.map(status => status.value),
      origin: filterParams.origin && filterParams.origin.id,
      orderedBy: filterParams.orderedBy && filterParams.orderedBy.id,
      createdBy: filterParams.createdBy && filterParams.createdBy.id,
      destinationParty: filterParams.destinationParty?.id,
    }, _.isEmpty),
    destination: filterParams.destination?.id,
  });

  const { translate, isUserApprover } = useSelector(state => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
    isUserApprover: state.session.isUserApprover,
  }));

  const dispatch = useDispatch();

  const {
    tableRef,
    fireFetchData,
    loading,
    onFetchHandler,
    tableData,
  } = useTableData({
    filterParams,
    url,
    messageId,
    defaultMessage,
    getSortingParams,
    getParams,
  });

  const downloadOrders = (orderItems) => {
    exportFileFromAPI({
      url: '/openboxes/api/purchaseOrders',
      filename: orderItems ? 'OrdersLineDetails.csv' : 'Orders',
      params: {
        ..._.omit(tableData.currentParams, 'offset', 'max'),
        orderItems,
      },
    });
  };

  const deleteOrder = (id) => {
    dispatch(showSpinner());
    apiClient.delete(`/openboxes/api/purchaseOrders/${id}`)
      .then((res) => {
        if (res.status === 204) {
          const successMessage = translate('react.purchaseOrder.delete.success.label', 'Purchase order has been deleted successfully');
          Alert.success(successMessage);
        }
      })
      .finally(() => {
        dispatch(hideSpinner());
        fireFetchData();
      });
  };

  const deleteHandler = (id) => {
    confirmAlert({
      title: translate('react.default.areYouSure.label', 'Are you sure?'),
      message: translate(
        'react.purchaseOrder.delete.confirm.title.label',
        'Are you sure you want to delete this purchase order?',
      ),
      buttons: [
        {
          label: translate('react.default.yes.label', 'Yes'),
          onClick: () => deleteOrder(id),
        },
        {
          label: translate('react.default.no.label', 'No'),
        },
      ],
    });
  };

  const rollbackOrder = (id) => {
    apiClient.post(`/openboxes/api/purchaseOrders/${id}/rollback`)
      .then((response) => {
        if (response.status === 200) {
          Alert.success(translate(
            'react.purchaseOrder.rollback.success.label',
            'Rollback of order status has been done successfully',
          ));
          fireFetchData();
        }
      });
  };

  const rollbackHandler = (id) => {
    if (!isUserApprover) {
      Alert.error(translate(
        'react.default.errors.noPermissions.label',
        'You do not have permissions to perform this action',
      ));
      return;
    }
    const order = tableData.data.find(ord => ord.id === id);
    if (order && order.shipmentsCount > 0) {
      Alert.error(translate(
        'react.purchaseOrder.rollback.error.label',
        'Cannot rollback order with associated shipments',
      ));
      return;
    }
    confirmAlert({
      title: translate('react.default.areYouSure.label', 'Are you sure?'),
      message: translate(
        'react.purchaseOrder.rollback.confirm.title.label',
        'Are you sure you want to rollback this order?',
      ),
      buttons: [
        {
          label: translate('react.default.yes.label', 'Yes'),
          onClick: () => rollbackOrder(id),
        },
        {
          label: translate('react.default.no.label', 'No'),
        },
      ],
    });
  };

  const printOrder = (id) => {
    const order = tableData.data.find(ord => ord.id === id);
    if (order && order.status && order.status.toUpperCase() === 'PENDING') {
      Alert.error('Order must be placed in order to print');
      return;
    }
    window.open(`/openboxes/order/print/${id}`, '_blank');
  };

  const cancelOrder = () => {
    Alert.error(translate('react.default.featureNotSupported', 'This feature is not currently supported'));
  };

  return {
    tableData,
    loading,
    tableRef,
    printOrder,
    cancelOrder,
    rollbackHandler,
    deleteHandler,
    downloadOrders,
    onFetchHandler,
  };
};

export default usePurchaseOrderListTableData;
