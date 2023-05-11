import { useState } from 'react';

import _ from 'lodash';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import purchaseOrderApi from 'api/services/PurchaseOrderApi';
import { PURCHASE_ORDER_API } from 'api/urls';
import useTableData from 'hooks/list-pages/useTableData';
import exportFileFromAPI from 'utils/file-download-util';
import { translateWithDefaultMessage } from 'utils/Translate';

const usePurchaseOrderListTableData = (filterParams) => {
  const errorMessageId = 'react.purchaseOrder.error.purchaseOrderList.label';
  const defaultErrorMessage = 'Unable to fetch purchase orders';
  const [totalPrice, setTotalPrice] = useState(0);
  const defaultSorting = {
    sort: 'dateOrdered',
    order: 'desc',
  };
  const getParams = ({
    offset,
    state,
    sortingParams,
  }) => {
    const {
      status, origin, orderedBy, createdBy, destinationParty, destination, paymentTerm,
    } = filterParams;
    return {
      ..._.omitBy({
        offset: `${offset}`,
        max: `${state.pageSize}`,
        ...sortingParams,
        ...filterParams,
        paymentTerm: paymentTerm?.map?.(({ id }) => id),
        status: status?.map?.(statusElement => statusElement.value),
        origin: origin?.id,
        orderedBy: orderedBy?.id,
        createdBy: createdBy?.id,
        destinationParty: destinationParty?.id,
      }, _.isEmpty),
      destination: destination?.id,
    };
  };

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
    url: PURCHASE_ORDER_API,
    errorMessageId,
    defaultErrorMessage,
    defaultSorting,
    getParams,
    onFetchedData: data => setTotalPrice(data.totalPrice),
  });

  const downloadOrders = (orderItems) => {
    exportFileFromAPI({
      url: PURCHASE_ORDER_API,
      filename: orderItems ? 'OrdersLineDetails.csv' : 'Orders',
      params: {
        ..._.omit(tableData.currentParams, 'offset', 'max'),
        orderItems,
      },
    });
  };

  const deleteOrder = async (id) => {
    dispatch(showSpinner());
    try {
      const { status } = await purchaseOrderApi.deleteOrder(id);
      if (status === 204) {
        const successMessage = translate('react.purchaseOrder.delete.success.label', 'Purchase order has been deleted successfully');
        Alert.success(successMessage);
      }
    } finally {
      dispatch(hideSpinner());
      fireFetchData();
    }
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

  const rollbackOrder = async (id) => {
    dispatch(showSpinner());
    try {
      const { status } = await purchaseOrderApi.rollbackOrder(id);
      if (status === 200) {
        Alert.success(translate(
          'react.purchaseOrder.rollback.success.label',
          'Rollback of order status has been done successfully',
        ));
        fireFetchData();
      }
    } finally {
      dispatch(hideSpinner());
    }
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
    tableData: { ...tableData, totalPrice },
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
