import React, { useMemo } from 'react';

import PropTypes from 'prop-types';
import {
  RiArrowGoBackLine,
  RiChat3Line,
  RiCloseLine,
  RiDeleteBinLine,
  RiDownload2Line,
  RiFileLine,
  RiInformationLine,
  RiListUnordered,
  RiPencilLine,
  RiPrinterLine,
  RiShoppingCartLine,
} from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import DataTable, { TableCell } from 'components/DataTable';
import DateCell from 'components/DataTable/DateCell';
import Button from 'components/form-elements/Button';
import PurchaseOrderStatus from 'components/purchaseOrder/PurchaseOrderStatus';
import usePurchaseOrderListTableData from 'hooks/list-pages/purchase-order/usePurchaseOrderListTableData';
import ActionDots from 'utils/ActionDots';
import { findActions } from 'utils/list-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';


const PurchaseOrderListTable = ({
  supportedActivities,
  highestRole,
  translate,
  currencyCode,
  allStatuses,
  locale,
  filterParams,
}) => {
  const {
    tableData,
    loading,
    tableRef,
    printOrder,
    cancelOrder,
    rollbackHandler,
    deleteHandler,
    downloadOrders,
    onFetchHandler,
  } = usePurchaseOrderListTableData(filterParams);


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
        id="react.purchaseOrder.column.paymentTerms.label"
        defaultMessage="Payment Terms"
      />,
      accessor: 'paymentTerm',
      minWidth: 150,
      Cell: row => <TableCell {...row} tooltip value={row.value?.name} />,
    },
    {
      Header: <Translate
        id="react.purchaseOrder.column.orderedOn.label"
        defaultMessage="Ordered On"
      />,
      accessor: 'dateOrdered',
      minWidth: 120,
      Cell: row => <DateCell {...row} />,
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
      minWidth: 260,
    },
  ], [supportedActivities, highestRole, actions]);

  const totalAmount = () => `${translate('react.purchaseOrder.totalAmount.label', 'Total amount')}: ${tableData.totalPrice.toLocaleString([locale, 'en'])} ${currencyCode}`;

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
        data={tableData.data}
        loading={loading}
        defaultPageSize={10}
        pages={tableData.pages}
        totalData={tableData.totalCount}
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
  allStatuses: state.purchaseOrder.statuses,
  locale: state.session.activeLanguage,
});

export default connect(mapStateToProps)(PurchaseOrderListTable);


PurchaseOrderListTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
  highestRole: PropTypes.string.isRequired,
  translate: PropTypes.func.isRequired,
  currencyCode: PropTypes.string.isRequired,
  allStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    value: PropTypes.string,
    label: PropTypes.string,
    variant: PropTypes.string,
  })).isRequired,
  locale: PropTypes.string.isRequired,
};
