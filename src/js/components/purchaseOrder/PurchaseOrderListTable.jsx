import React, { useEffect, useRef, useState } from 'react';

import fileDownload from 'js-file-download';
import _ from 'lodash';
import PropTypes from 'prop-types';
import queryString from 'query-string';
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
import ReactTable from 'react-table';

import { hideSpinner, showSpinner } from 'actions';
import Button from 'components/form-elements/Button';
import PurchaseOrderStatus from 'components/purchaseOrder/PurchaseOrderStatus';
import ActionDots from 'utils/ActionDots';
import apiClient from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-table/react-table.css';


// Custom function to parse date to show like: May, 23, 2022
const dateParser = (date) => {
  const parsedDate = new Date(date);
  const day = parsedDate.getDay();
  const month = parsedDate.getMonth();
  const year = parsedDate.getFullYear();
  const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec',
  ];
  return `${monthNames[month]} ${day}, ${year}`;
};


const PurchaseOrderListTable = ({
  filterParams, supportedActivities, highestRole, showTheSpinner, hideTheSpinner, translate,
}) => {
  // Temporary 'hard-coded' checking for role to display an action in dropdown or not
  const hasMinimumRequiredRole = (role) => {
    // TODO: Figure out better way to check roles
    const roles = ['Superuser', 'Admin', 'Manager', 'Assistant', 'Browser', 'Authenticated', 'Anonymous'];
    return roles.indexOf(highestRole) <= roles.indexOf(role);
  };

  // Determine list of actions for each row individually
  const findActions = (actionList, row) => {
    // Firstly filter out by status if any provided
    const filteredByStatus = actionList.filter((action) => {
      if (action.statuses) {
        return action.statuses.includes(row.original.status);
      }
      return true;
    });
    // Secondly filter by activity code if any provided
    const filteredByActivityCode = filteredByStatus.filter(action =>
      (action.activityCode ?
        action.activityCode.every(code => supportedActivities.some(activity => activity === code))
        : true
      ));
    // Lastly filter by required user's role if provided
    return filteredByActivityCode.filter((action) => {
      if (action.minimumRequiredRole) {
        return hasMinimumRequiredRole(action.minimumRequiredRole);
      }
      return true;
    });
  };

  const [ordersData, setOrdersData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pages, setPages] = useState(-1);
  const [totalPrice, setTotalPrice] = useState(0.0);
  // Stored searching params for export case
  const [currParams, setCurrParams] = useState({});

  // Util ref for react-table to force the fetch of data
  const tableRef = useRef(null);
  const fireFetchData = () => {
    tableRef.current.fireFetchData();
  };

  // If filterParams change, refetch the data with applied filters
  useEffect(() => fireFetchData(), [filterParams]);

  // If orderItems is true, download orders line items details, else download orders
  const downloadOrders = (orderItems) => {
    apiClient.get('/openboxes/api/purchaseOrders', {
      params: {
        ..._.omit(currParams, 'offset', 'max'),
        format: 'csv',
        orderItems,
      },
      paramsSerializer: params => queryString.stringify(params),
    })
      .then((res) => {
        fileDownload(res.data, orderItems ? 'OrdersLineDetails.csv' : 'Orders', 'text/csv');
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
        const errorMessage = translate('react.purchaseOrder.delete.error.label', 'Error while deleting a purchase order');
        Alert.error(errorMessage);
        fireFetchData();
      });
  };


  // List of all actions for PO rows
  const actions = [
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
      href: '/openboxes/order/print',
    },
    {
      label: 'react.purchaseOrder.cancelOrder.label',
      defaultLabel: 'Cancel order',
      leftIcon: <RiCloseLine />,
      activityCode: ['PLACE_ORDER'],
      statuses: ['PLACED'],
      href: '/openboxes/order/withdraw',
    },
    {
      label: 'react.purchaseOrder.rollbackOrder.label',
      defaultLabel: 'Rollback Order',
      leftIcon: <RiArrowGoBackLine />,
      minimumRequiredRole: 'Superuser',
      activityCode: ['PLACE_ORDER'],
      statuses: ['PLACED'],
      href: '/openboxes/order/rollbackOrderStatus',
    },
    {
      label: 'react.purchaseOrder.delete.label',
      defaultLabel: 'Delete',
      leftIcon: <RiDeleteBinLine />,
      minimumRequiredRole: 'Assistant',
      variant: 'danger',
      onClickActionWithId: id => deleteOrder(id),
    },
  ];

  // Columns for react-table
  const columns = [
    {
      Header: 'Status',
      accessor: 'status',
      className: 'active-circle',
      headerClassName: 'header',
      style: { overflow: 'visible' },
      Cell: row => (
        <div className="d-flex gap-8">
          <ActionDots actions={findActions(actions, row)} id={row.original.id} />
          <PurchaseOrderStatus status={row.original.status} />
        </div>
      ),
    },
    {
      Header: 'Order Number',
      accessor: 'orderNumber',
      className: 'cell',
      headerClassName: 'header text-align-left',
    },
    {
      Header: 'Name',
      accessor: 'name',
      className: 'cell',
      headerClassName: 'header text-align-left',
    },
    {
      Header: 'Supplier',
      accessor: 'origin',
      className: 'cell',
      headerClassName: 'header text-align-left',
    },
    {
      Header: 'Destination',
      accessor: 'destination',
      className: 'cell',
      headerClassName: 'header text-align-left',
    },
    {
      Header: 'Ordered On',
      accessor: 'dateOrdered',
      className: 'cell',
      headerClassName: 'header text-align-left',
      Cell: row => (
        <span>{dateParser(row.original.dateOrdered)}</span>
      ),
      Footer: (
        <span>
          <Translate id="react.purchaseOrder.totalAmount.label" defaultMessage="Total amount" />: {totalPrice} USD
        </span>
      ),
      footerClassName: 'border-0',
    },
  ];


  return (
    <div className="purchase-order-list-list-section">
      <div className="title-box d-flex justify-content-between align-items-center">
        <span>
          <Translate id="react.purchaseOrder.listOrders.label" defaultMessage="List Orders" />
          &nbsp;
          (<Translate id="react.purchaseOrder.totalAmount.label" defaultMessage="Total amount" />: {totalPrice} USD)
        </span>
        <div className="btn-group">
          <Button
            defaultLabel="Export"
            label="react.purchaseOrder.export.button.label"
            variant="dropdown"
            EndIcon={<RiDownload2Line />}
          />
          <div className="dropdown-menu dropdown-menu-right nav-item padding-8" aria-labelledby="dropdownMenuButton">
            <a href="#" className="dropdown-item" onClick={() => downloadOrders(true)} role="button" tabIndex={0}>
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
      <ReactTable
        data={ordersData}
        ref={tableRef}
        columns={columns}
        loading={loading}
        pages={pages}
        defaultPageSize={10}
        manual
        className="-striped -highlight zoneTable"
        resizable={false}
        sortable
        collapseOnSortingChange={false}
        previousText={<i className="fa fa-chevron-left" aria-hidden="true" />}
        nextText={<i className="fa fa-chevron-right" aria-hidden="true" />}
        pageText=""
        onFetchData={(state) => {
          const offset = state.page > 0 ? (state.page) * state.pageSize : 0;
          const sortingParams = state.sorted.length > 0 ?
            {
              sort: state.sorted[0].id,
              order: state.sorted[0].desc ? 'desc' : 'asc',
            } : '';
          const statusParam = filterParams.status &&
            filterParams.status.map(status => status.value);

          const params = _.omitBy({
            offset: `${offset}`,
            max: `${state.pageSize}`,
            ...sortingParams,
            ..._.omit(filterParams, 'status'),
            status: statusParam,
            origin: filterParams.origin && filterParams.origin.id,
            destination: filterParams.destination && filterParams.destination.id,
            orderedBy: filterParams.orderedBy && filterParams.orderedBy.id,
            destinationParty: filterParams.destinationParty && filterParams.destinationParty.id,
          }, _.isEmpty);

          // Fetch data
          apiClient.get('/openboxes/api/purchaseOrders', {
            params,
            paramsSerializer: parameters => queryString.stringify(parameters),
          })
            .then((res) => {
              setLoading(false);
              setPages(Math.ceil(res.data.totalCount / state.pageSize));
              setOrdersData(res.data.data);
              setTotalPrice(res.data.totalPrice);
              // Store currently used params for export case
              setCurrParams({
                offset: `${offset}`,
                max: `${state.pageSize}`,
                ...sortingParams,
                ..._.omit(filterParams, 'status'),
                status: statusParam,
              });
            })
            .catch(() => Promise.reject(new Error(this.props.translate('react.purchaseOrder.error.purchaseOrderList.label', 'Could not fetch purchase order list'))));
        }}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  supportedActivities: state.session.supportedActivities,
  highestRole: state.session.highestRole,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
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
};
