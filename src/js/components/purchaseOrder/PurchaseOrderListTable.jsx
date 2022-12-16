import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';

import { CancelToken } from 'axios';
import _ from 'lodash';
import PropTypes from 'prop-types';
import queryString from 'query-string';
import { confirmAlert } from 'react-confirm-alert';
import {
  RiArrowGoBackLine,
  RiChat3Line,
  RiDeleteBinLine,
  RiDownload2Line,
  RiFileLine,
  RiInformationLine,
  RiListUnordered,
  RiPencilLine,
  RiPrinterLine,
  RiShoppingCartLine,
} from 'react-icons/all';
import { RiCloseLine } from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import DataTable, { TableCell } from 'components/DataTable';
import Button from 'components/form-elements/Button';
import PurchaseOrderStatus from 'components/purchaseOrder/PurchaseOrderStatus';
import ActionDots from 'utils/ActionDots';
import apiClient from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';
import { findActions } from 'utils/list-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';


const PurchaseOrderListTable = ({
  filterParams,
  supportedActivities,
  highestRole,
  showTheSpinner,
  hideTheSpinner,
  translate,
  currencyCode,
  currentLocation,
  allStatuses,
  isUserApprover,
  locale,
}) => {
  const [ordersData, setOrdersData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pages, setPages] = useState(-1);
  const [totalData, setTotalData] = useState(0);
  const [totalPrice, setTotalPrice] = useState(0.0);
  // Stored searching params for export case
  const [currentParams, setCurrentParams] = useState({});

  // Util ref for react-table to force the fetch of data
  const tableRef = useRef(null);

  // Cancel token/signal for fetching data
  const sourceRef = useRef(CancelToken.source());

  const fireFetchData = () => {
    // Each time we fetch, we want to 'reset' the token/signal
    sourceRef.current = CancelToken.source();
    tableRef.current.fireFetchData();
  };

  useEffect(() => () => {
    if (currentLocation?.id) {
      sourceRef.current.cancel('Fetching canceled');
    }
  }, [currentLocation?.id]);

  // If filterParams change, refetch the data with applied filters
  useEffect(() => fireFetchData(), [filterParams]);

  // If orderItems is true, download orders line items details, else download orders
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
    showTheSpinner();
    apiClient.delete(`/openboxes/api/purchaseOrders/${id}`)
      .then((res) => {
        if (res.status === 204) {
          hideTheSpinner();
          const successMessage = translate('react.purchaseOrder.delete.success.label', 'Purchase order has been deleted successfully');
          Alert.success(successMessage);
          fireFetchData();
        }
      })
      .catch(() => {
        hideTheSpinner();
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

  const getStatusTooltip = status => translate(
    `react.purchaseOrder.status.${status.toLowerCase()}.description.label`,
    status.toLowerCase(),
  );

  // List of all actions for PO rows
  const actions = useMemo(() => [
    {
      label: 'react.purchaseOrder.viewOrderDetails.label',
      defaultLabel: 'View order details',
      leftIcon: <RiInformationLine />,
      href: '/openboxes/order/show',
    },
    {
      label: 'react.purchaseOrder.addComment.label',
      defaultLabel: 'Add comment',
      leftIcon: <RiChat3Line />,
      activityCode: ['PLACE_ORDER'],
      href: '/openboxes/order/addComment',
    },
    {
      label: 'react.purchaseOrder.addDocument.label',
      defaultLabel: 'Add document',
      leftIcon: <RiFileLine />,
      activityCode: ['PLACE_ORDER'],
      href: '/openboxes/order/addDocument',
    },
    {
      label: 'react.purchaseOrder.edit.label',
      defaultLabel: 'Edit order',
      leftIcon: <RiPencilLine />,
      statuses: ['PENDING'],
      activityCode: ['PLACE_ORDER'],
      href: '/openboxes/purchaseOrder/edit',
    },
    {
      label: 'react.purchaseOrder.editLineItems.label',
      defaultLabel: 'Edit line items',
      leftIcon: <RiListUnordered />,
      statuses: ['PENDING'],
      activityCode: ['PLACE_ORDER'],
      href: '/openboxes/purchaseOrder/addItems',
    },
    {
      label: 'react.purchaseOrder.placeOrder.label',
      defaultLabel: 'Place order',
      leftIcon: <RiShoppingCartLine />,
      statuses: ['PENDING'],
      activityCode: ['PLACE_ORDER'],
      href: '/openboxes/order/placeOrder',
    },
    {
      label: 'react.purchaseOrder.printOrder.label',
      defaultLabel: 'Print order',
      leftIcon: <RiPrinterLine />,
      activityCode: ['PLACE_ORDER'],
      onClick: id => printOrder(id),
    },
    {
      label: 'react.purchaseOrder.cancelOrder.label',
      defaultLabel: 'Cancel order',
      leftIcon: <RiCloseLine />,
      activityCode: ['PLACE_ORDER'],
      onClick: () => cancelOrder(),
    },
    {
      label: 'react.purchaseOrder.rollbackOrder.label',
      defaultLabel: 'Rollback Order',
      leftIcon: <RiArrowGoBackLine />,
      minimumRequiredRole: 'Superuser',
      activityCode: ['PLACE_ORDER'],
      // Display for statuses > PENDING
      statuses: allStatuses.filter(stat => stat.id !== 'PENDING')
        .map(status => status.id),
      onClick: id => rollbackHandler(id),
    },
    {
      label: 'react.purchaseOrder.delete.label',
      defaultLabel: 'Delete',
      leftIcon: <RiDeleteBinLine />,
      minimumRequiredRole: 'Assistant',
      variant: 'danger',
      onClick: id => deleteHandler(id),
    },
  ], [allStatuses]);

  // Columns for react-table
  const columns = useMemo(() => [
    {
      Header: ' ',
      width: 50,
      fixed: 'left',
      sortable: false,
      style: {
        overflow: 'visible',
        zIndex: 1,
      },
      Cell: row => (
        <ActionDots
          dropdownPlacement="right"
          dropdownClasses="action-dropdown-offset"
          actions={findActions(actions, row, {
            supportedActivities,
            highestRole,
          })}
          id={row.original.id}
        />),
    },
    {
      Header: <Translate id="react.purchaseOrder.column.status.label" defaultMessage="Status" />,
      accessor: 'status',
      className: 'active-circle',
      headerClassName: 'header',
      fixed: 'left',
      width: 160,
      Cell: row => (
        <TableCell {...row} tooltip tooltipLabel={getStatusTooltip(row.original.status)}>
          <PurchaseOrderStatus status={row.original.status} />
        </TableCell>),
    },
    {
      Header: <Translate
        id="react.purchaseOrder.column.orderNumber.label"
        defaultMessage="Order Number"
      />,
      accessor: 'orderNumber',
      fixed: 'left',
      width: 150,
      Cell: row => <TableCell {...row} link={`/openboxes/order/show/${row.original.id}`} />,
    },
    {
      Header: <Translate id="react.purchaseOrder.column.name.label" defaultMessage="Name" />,
      accessor: 'name',
      fixed: 'left',
      minWidth: 250,
      Cell: row => <TableCell {...row} tooltip link={`/openboxes/order/show/${row.original.id}`} />,
    },
    {
      Header: <Translate id="react.purchaseOrder.column.supplier.label" defaultMessage="Supplier" />,
      accessor: 'origin',
      minWidth: 300,
      Cell: row => <TableCell {...row} tooltip />,
    },
    {
      Header: <Translate
        id="react.purchaseOrder.column.destination.label"
        defaultMessage="Destination"
      />,
      accessor: 'destination',
      minWidth: 300,
      Cell: row => <TableCell {...row} tooltip />,
    },
    {
      Header: <Translate
        id="react.purchaseOrder.column.orderedOn.label"
        defaultMessage="Ordered On"
      />,
      accessor: 'dateOrdered',
      minWidth: 120,
    },
    {
      Header: <Translate
        id="react.purchaseOrder.column.orderedBy.label"
        defaultMessage="Ordered By"
      />,
      accessor: 'orderedBy',
      headerClassName: 'text-left',
      minWidth: 150,
      Cell: row => <TableCell {...row} tooltip />,
    },
    {
      Header: <Translate
        id="react.purchaseOrder.column.lineItems.label"
        defaultMessage="Line Items"
      />,
      accessor: 'orderItemsCount',
      className: 'text-right',
      headerClassName: 'justify-content-end',
      sortable: false,
    },
    {
      Header: <Translate id="react.purchaseOrder.column.ordered.label" defaultMessage="Ordered" />,
      accessor: 'orderedOrderItemsCount',
      className: 'text-right',
      headerClassName: 'justify-content-end',
      sortable: false,
    },
    {
      Header: <Translate id="react.purchaseOrder.column.shipped.label" defaultMessage="Shipped" />,
      accessor: 'shippedItemsCount',
      className: 'text-right',
      headerClassName: 'justify-content-end',
      sortable: false,
    },
    {
      Header: <Translate id="react.purchaseOrder.column.received.label" defaultMessage="Received" />,
      accessor: 'receivedItemsCount',
      className: 'text-right',
      headerClassName: 'justify-content-end',
      sortable: false,
    },
    {
      Header: <Translate id="react.purchaseOrder.column.invoiced.label" defaultMessage="Invoiced" />,
      accessor: 'invoicedItemsCount',
      className: 'text-right',
      headerClassName: 'justify-content-end',
      sortable: false,
    },
    {
      Header: <Translate
        id="react.purchaseOrder.column.totalAmountLocalCurrency.label"
        defaultMessage="Total amount (local currency)"
      />,
      accessor: 'total',
      className: 'text-right',
      headerClassName: 'justify-content-end',
      sortable: false,
      minWidth: 230,
    },
    {
      Header: <Translate
        id="react.purchaseOrder.column.totalAmountDefaultCurrency.label"
        defaultMessage="Total amount (default currency)"
      />,
      accessor: 'totalNormalized',
      className: 'text-right',
      headerClassName: 'justify-content-end',
      sortable: false,
      minWidth: 230,
    },
  ], [supportedActivities, highestRole, actions]);

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

  const totalAmount = () => `${translate('react.purchaseOrder.totalAmount.label', 'Total amount')}: ${totalPrice.toLocaleString([locale, 'en'])} ${currencyCode}`;

  return (
    <div className="list-page-list-section">
      <div className="title-text p-3 d-flex justify-content-between align-items-center">
        <span>
          <Translate id="react.purchaseOrder.listOrders.label" defaultMessage="List Orders" />
          &nbsp;
          ({totalAmount()})
        </span>
        <div className="btn-group">
          <Button
            isDropdown
            defaultLabel="Export"
            label="react.default.button.export.label"
            variant="secondary"
            EndIcon={<RiDownload2Line />}
          />
          <div
            className="dropdown-menu dropdown-menu-right nav-item padding-8"
            aria-labelledby="dropdownMenuButton"
          >
            <a
              href="#"
              className="dropdown-item"
              onClick={() => downloadOrders(true)}
              role="button"
              tabIndex={0}
            >
              <Translate
                id="react.purchaseOrder.export.orderLineDetails.label"
                defaultMessage="Export order line details"
              />
            </a>
            <a className="dropdown-item" onClick={() => downloadOrders(false)} href="#">
              <Translate
                id="react.purchaseOrder.export.orders.label"
                defaultMessage="Export orders"
              />
            </a>
          </div>
        </div>
      </div>
      <DataTable
        manual
        sortable
        ref={tableRef}
        columns={columns}
        data={ordersData}
        loading={loading}
        defaultPageSize={10}
        pages={pages}
        totalData={totalData}
        onFetchData={onFetchHandler}
        noDataText="No orders match the given criteria"
        footerComponent={() => (
          <span className="title-text p-1 d-flex flex-1 justify-content-end">
            {totalAmount()}
          </span>
        )}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  supportedActivities: state.session.supportedActivities,
  highestRole: state.session.highestRole,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  currencyCode: state.session.currencyCode,
  currentLocation: state.session.currentLocation,
  buyers: state.organizations.buyers,
  allStatuses: state.purchaseOrder.statuses,
  isUserApprover: state.session.isUserApprover,
  locale: state.session.activeLanguage,
});

const mapDispatchToProps = {
  showTheSpinner: showSpinner,
  hideTheSpinner: hideSpinner,
};

export default connect(mapStateToProps, mapDispatchToProps)(PurchaseOrderListTable);


PurchaseOrderListTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
  highestRole: PropTypes.string.isRequired,
  showTheSpinner: PropTypes.func.isRequired,
  hideTheSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  currencyCode: PropTypes.string.isRequired,
  currentLocation: PropTypes.shape({}).isRequired,
  allStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    value: PropTypes.string,
    label: PropTypes.string,
    variant: PropTypes.string,
  })).isRequired,
  isUserApprover: PropTypes.bool,
  locale: PropTypes.string.isRequired,
};

PurchaseOrderListTable.defaultProps = {
  isUserApprover: false,
};
