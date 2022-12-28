import { useCallback, useState } from 'react';

import _ from 'lodash';
import queryString from 'query-string';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import useTableData from 'hooks/useTableData';
import apiClient from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';
import { translateWithDefaultMessage } from 'utils/Translate';

const usePurchaseOrderListTableData = (filterParams) => {
  const [currentParams, setCurrentParams] = useState({});
  const [ordersData, setOrdersData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalData, setTotalData] = useState(0);
  const [pages, setPages] = useState(-1);
  const [totalPrice, setTotalPrice] = useState(0.0);
  const { translate, isUserApprover } = useSelector(state => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
    isUserApprover: state.session.isUserApprover,
  }));

  const {
    sourceRef, tableRef, fireFetchData,
  } = useTableData(filterParams);

  const downloadOrders = (orderItems) => {
    exportFileFromAPI({
      url: '/openboxes/api/purchaseOrders',
      filename: orderItems ? 'OrdersLineDetails.csv' : 'Orders',
      params: {
        ..._.omit(currentParams, 'offset', 'max'),
        orderItems,
      },
    });
  };

  const deleteOrder = (id) => {
    showSpinner();
    apiClient.delete(`/openboxes/api/purchaseOrders/${id}`)
      .then((res) => {
        if (res.status === 204) {
          const successMessage = translate('react.purchaseOrder.delete.success.label', 'Purchase order has been deleted successfully');
          Alert.success(successMessage);
        }
      })
      .finally(() => {
        hideSpinner();
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
    const order = ordersData.find(ord => ord.id === id);
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
    const order = ordersData.find(ord => ord.id === id);
    if (order && order.status && order.status.toUpperCase() === 'PENDING') {
      Alert.error('Order must be placed in order to print');
      return;
    }
    window.open(`/openboxes/order/print/${id}`, '_blank');
  };

  const cancelOrder = () => {
    Alert.error(translate('react.default.featureNotSupported', 'This feature is not currently supported'));
  };


  const onFetchHandler = useCallback((state) => {
    if (!_.isEmpty(filterParams)) {
      const offset = state.page > 0 ? (state.page) * state.pageSize : 0;
      const sortingParams = state.sorted.length > 0 ?
        {
          sort: state.sorted[0].id,
          order: state.sorted[0].desc ? 'desc' : 'asc',
        } :
        {
          sort: 'dateOrdered',
          order: 'desc',
        };
      const statusParam = filterParams.status &&
        filterParams.status.map(status => status.value);
      const params = {
        ..._.omitBy({
          offset: `${offset}`,
          max: `${state.pageSize}`,
          ...sortingParams,
          ...filterParams,
          status: statusParam,
          origin: filterParams.origin && filterParams.origin.id,
          orderedBy: filterParams.orderedBy && filterParams.orderedBy.id,
          createdBy: filterParams.createdBy && filterParams.createdBy.id,
          destinationParty: filterParams.destinationParty?.id,
        }, _.isEmpty),
        destination: filterParams.destination?.id,
      };

      // Fetch data
      setLoading(true);
      apiClient.get('/openboxes/api/purchaseOrders', {
        params,
        paramsSerializer: parameters => queryString.stringify(parameters),
        cancelToken: sourceRef.current?.token,
      })
        .then((res) => {
          setLoading(false);
          setPages(Math.ceil(res.data.totalCount / state.pageSize));
          setTotalData(res.data.totalCount);
          setOrdersData(res.data.data);
          setTotalPrice(res.data.totalPrice);
          // Store currently used params for export case
          setCurrentParams(params);
        })
        .catch(() => Promise.reject(new Error(translate('react.purchaseOrder.error.purchaseOrderList.label', 'Unable to fetch purchase orders'))));
    }
  }, [filterParams]);

  return {
    ordersData,
    loading,
    pages,
    totalData,
    totalPrice,
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
