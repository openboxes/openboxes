import React, { useEffect, useRef, useState } from 'react';

import fileDownload from 'js-file-download';
import _ from 'lodash';
import moment from 'moment';
import PropTypes from 'prop-types';
import queryString from 'query-string';
import { confirmAlert } from 'react-confirm-alert';
import {
  RiDeleteBinLine,
  RiDownload2Line,
  RiInformationLine, RiPencilLine,
} from 'react-icons/all';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import Alert from 'react-s-alert';

import { fetchShipmentStatusCodes, hideSpinner, showSpinner } from 'actions';
import DataTable, { TableCell } from 'components/DataTable';
import Button from 'components/form-elements/Button';
import ActionDots from 'utils/ActionDots';
import apiClient from 'utils/apiClient';
import StatusIndicator from 'utils/StatusIndicator';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const StockMovementInboundTable = ({
  filterParams,
  translate,
  fetchStatuses,
  shipmentStatuses,
  isShipmentStatusesFetched,
  currentLocation,
  history,
  showTheSpinner,
  hideTheSpinner,
}) => {
  const [tableData, setTableData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pages, setPages] = useState(-1);
  // Stored searching params for export case
  const [currentParams, setCurrentParams] = useState({});
  const [totalData, setTotalData] = useState(0);

  const tableRef = useRef(null);

  const fireFetchData = () => tableRef.current.fireFetchData();

  // If filterParams change, refetch the data with applied filters
  useEffect(() => {
    fireFetchData();
    if (!isShipmentStatusesFetched) fetchStatuses();
  }, [filterParams]);

  const exportStockMovements = () => {
    apiClient.get('/openboxes/api/stockMovements', {
      params: {
        ...currentParams,
        format: 'csv',
      },
      paramsSerializer: params => queryString.stringify(params),
    })
      .then((res) => {
        const filename = res.headers['content-disposition'].split('filename="')[1].split('.')[0];
        fileDownload(res.data, filename, 'text/csv');
      });
  };

  const exportAllIncomingItems = () => {
    apiClient.get('/openboxes/api/stockMovements/shippedItems', {
      params: {
        ...currentParams,
        format: 'csv',
      },
      paramsSerializer: params => queryString.stringify(params),
    })
      .then((res) => {
        const filename = res.headers['content-disposition'].split('filename="')[1].split('.')[0];
        fileDownload(res.data, filename, 'text/csv');
      });
  };


  const getStatusTooltip = status => translate(
    `react.stockMovement.status.${status.toLowerCase()}.description.label`,
    status.toLowerCase(),
  );


  const onFetchHandler = (state) => {
    const offset = state.page > 0 ? (state.page) * state.pageSize : 0;
    const sortingParams = state.sorted.length > 0 ?
      {
        sort: state.sorted[0].id,
        order: state.sorted[0].desc ? 'desc' : 'asc',
      } : undefined;

    const params = _.omitBy({
      ...filterParams,
      offset: `${offset}`,
      max: `${state.pageSize}`,
      direction: 'INBOUND',
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

    // Fetch data
    setLoading(true);
    apiClient.get('/openboxes/api/stockMovements', {
      paramsSerializer: parameters => queryString.stringify(parameters),
      params,
    })
      .then((res) => {
        setLoading(false);
        setPages(Math.ceil(res.data.totalCount / state.pageSize));
        setTotalData(res.data.totalCount);
        setTableData(res.data.data);
        // Store currently used params for export case
        setCurrentParams(params);
      });
  };

  const deleteReturnStockMovement = (id) => {
    showTheSpinner();
    apiClient.delete(`/openboxes/stockMovements/${id}`)
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
      .finally(() => hideTheSpinner());
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

  // List of all actions for inbound Stock Movement rows
  const getActions = (row) => {
    const actions = [];

    // Show
    actions[0] = {
      defaultLabel: 'Show Stock Movement',
      label: 'react.stockMovement.action.show.label',
      leftIcon: <RiInformationLine />,
      href: '/openboxes/stockMovement/show',
    };

    // Edit
    actions[1] = {
      defaultLabel: 'Edit Stock Movement',
      label: 'react.stockMovement.action.edit.label',
      leftIcon: <RiPencilLine />,
    };
    if (row.original.isReturn) {
      actions[1].onClick = () => history.push(`/openboxes/stockTransfer/createInboundReturn/${row.original.order?.id}`);
    } else {
      actions[1].onClick = () => history.push(`/openboxes/stockMovement/createCombinedShipments/${row.original?.id}`);
    }

    const isSameOrigin = currentLocation.id === row.original.origin?.id;
    // Delete
    if (row.original.isPending && (isSameOrigin || !row.original.origin?.isDepot)) {
      actions[2] = {
        defaultLabel: 'Delete Stock Movement',
        label: 'react.stockMovement.action.delete.label',
        leftIcon: <RiDeleteBinLine />,
        variant: 'danger',
        onClick: deleteConfirmAlert,
      };
    }
    return actions;
  };

  // Columns for react-table
  const columns = [
    {
      Header: ' ',
      width: 50,
      sortable: false,
      style: { overflow: 'visible', zIndex: 1 },
      fixed: 'left',
      Cell: row => (
        <ActionDots
          dropdownPlacement="right"
          dropdownClasses="action-dropdown-offset"
          actions={getActions(row)}
          id={row.original.id}
        />),
    },
    {
      Header: 'Items',
      accessor: 'lineItemCount',
      fixed: 'left',
      className: 'text-right',
      headerClassName: 'justify-content-end',
      width: 80,
      sortable: false,
    },
    {
      Header: 'Status',
      accessor: 'shipmentStatus',
      fixed: 'left',
      width: 150,
      sortable: false,
      Cell: row => (
        <TableCell
          {...row}
          tooltip
          tooltipLabel={getStatusTooltip(row.value)}
        >
          <StatusIndicator
            status={row.value}
            variant={_.find(shipmentStatuses, _.matchesProperty('id', row.value))?.variant}
          />
        </TableCell>),
    },
    {
      Header: 'Identifier',
      accessor: 'identifier',
      fixed: 'left',
      minWidth: 100,
      sortable: false,
      Cell: row => (
        <TableCell {...row} link={`/openboxes/stockMovement/show/${row.original.id}`} />),
    },
    {
      Header: 'Name',
      accessor: 'name',
      minWidth: 250,
      Cell: row => (
        <TableCell
          {...row}
          tooltip
          link={`/openboxes/stockMovement/show/${row.original.id}`}
          value={row.original.description || row.original.name}
        />),
    },
    {
      Header: 'Origin',
      accessor: 'origin.name',
      minWidth: 250,
      sortable: false,
      Cell: row => (<TableCell {...row} tooltip />),
    },
    {
      Header: 'Stocklist',
      accessor: 'stocklist.name',
      sortable: false,
      minWidth: 150,
      Cell: row => (<TableCell {...row} value={row.value || 'None'} />),
    },
    {
      Header: 'Requested by',
      accessor: 'requestedBy.name',
      minWidth: 250,
      sortable: false,
      Cell: row => (<TableCell {...row} value={row.value || 'None'} />),
    },
    {
      Header: 'Date Created',
      accessor: 'dateCreated',
      width: 150,
      sortable: false,
      Cell: row => (<TableCell {...row} value={moment(row.value).format('MMM DD, yyyy')} />),
    },
    {
      Header: 'Expected Receipt Date',
      accessor: 'expectedDeliveryDate',
      width: 200,
      sortable: false,
      Cell: row =>
        (<TableCell
          {...row}
          defaultValue="-"
          value={row.value && moment(row.value).format('MMM DD, yyyy')}
        />),
    },
  ];

  return (
    <div className="list-page-list-section">
      <div className="title-text p-3 d-flex justify-content-between align-items-center">
        <div>
          <Translate id="react.stockMovement.inbound.label" defaultMessage="Inbound" />
          <span className="ml-1">{`(${totalData})`}</span>
        </div>
        <Button
          isDropdown
          defaultLabel="Export"
          label="react.default.button.export.label"
          variant="secondary"
          EndIcon={<RiDownload2Line />}
        />
        <div className="dropdown-menu dropdown-menu-right nav-item padding-8" aria-labelledby="dropdownMenuButton">
          <a href="#" className="dropdown-item" onClick={exportStockMovements} role="button" tabIndex={0}>
            <Translate
              id="react.stockMovement.export.label"
              defaultMessage="Export Stock Movements"
            />
          </a>
          <a className="dropdown-item" onClick={exportAllIncomingItems} href="#">
            <Translate
              id="react.stockMovement.export.allIncomingItems.label"
              defaultMessage="Export all incoming items"
            />
          </a>
        </div>
      </div>
      <DataTable
        manual
        sortable
        ref={tableRef}
        columns={columns}
        data={tableData}
        loading={loading}
        defaultPageSize={10}
        pages={pages}
        totalData={totalData}
        onFetchData={onFetchHandler}
        noDataText={translate(
          'react.stockMovement.empty.list.label',
          'No Stock Movements match the given criteria',
        )}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  shipmentStatuses: state.shipmentStatuses.data,
  isShipmentStatusesFetched: state.shipmentStatuses.fetched,
  currentLocation: state.session.currentLocation,
});

const mapDispatchToProps = {
  showSpinner,
  hideSpinner,
  fetchStatuses: fetchShipmentStatusCodes,
  showTheSpinner: showSpinner,
  hideTheSpinner: hideSpinner,
};

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(StockMovementInboundTable));


StockMovementInboundTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  translate: PropTypes.func.isRequired,
  fetchStatuses: PropTypes.func.isRequired,
  showTheSpinner: PropTypes.func.isRequired,
  hideTheSpinner: PropTypes.func.isRequired,
  isShipmentStatusesFetched: PropTypes.bool.isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  shipmentStatuses: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  }).isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
};
