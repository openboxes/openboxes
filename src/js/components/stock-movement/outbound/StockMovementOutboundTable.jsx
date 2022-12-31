import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';

import { CancelToken } from 'axios';
import _ from 'lodash';
import moment from 'moment';
import PropTypes from 'prop-types';
import queryString from 'query-string';
import { confirmAlert } from 'react-confirm-alert';
import {
  RiArrowRightSLine,
  RiDeleteBinLine,
  RiDownload2Line,
  RiInformationLine,
  RiPencilLine,
} from 'react-icons/all';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { fetchRequisitionStatusCodes, hideSpinner, showSpinner } from 'actions';
import stockMovementApi from 'api/services/StockMovementApi';
import DataTable, { TableCell } from 'components/DataTable';
import Button from 'components/form-elements/Button';
import ActionDots from 'utils/ActionDots';
import exportFileFromAPI from 'utils/file-download-util';
import StatusIndicator from 'utils/StatusIndicator';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const StockMovementOutboundTable = ({
  filterParams,
  translate,
  fetchStatuses,
  requisitionStatuses,
  isRequisitionStatusesFetched,
  currentLocation,
  showTheSpinner,
  hideTheSpinner,
  isRequestsOpen,
  isUserAdmin,
}) => {
  const [tableData, setTableData] = useState({
    data: [],
    pages: -1,
    totalCount: 0,
    currentParams: {},
  });
  const [loading, setLoading] = useState(true);

  const tableRef = useRef(null);

  // Cancel token/signal for fetching data
  const sourceRef = useRef(CancelToken.source());

  const fireFetchData = () => {
    sourceRef.current = CancelToken.source();
    tableRef.current.fireFetchData();
  };

  useEffect(() => () => {
    if (currentLocation?.id) {
      sourceRef.current.cancel('Fetching canceled');
    }
  }, [currentLocation?.id]);

  // If filterParams change, refetch the data with applied filters
  useEffect(() => {
    fireFetchData();
  }, [filterParams]);

  useEffect(() => {
    if (!isRequisitionStatusesFetched || requisitionStatuses.length === 0) {
      fetchStatuses();
    }
  }, []);

  const exportStockMovements = () => {
    exportFileFromAPI({
      url: '/openboxes/api/stockMovements',
      params: tableData.currentParams,
    });
  };

  const exportPendingShipmentItems = () => {
    exportFileFromAPI({
      url: '/openboxes/api/stockMovements/pendingRequisitionItems',
      params: tableData.currentParams,
    });
  };

  const getStatusTooltip = status => translate(
    `react.stockMovement.status.${status.toLowerCase()}.description.label`,
    status.toLowerCase(),
  );

  const onFetchHandler = useCallback(async (state) => {
    if (!_.isEmpty(filterParams)) {
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

      // Fetch data
      setLoading(true);
      try {
        const config = {
          paramsSerializer: parameters => queryString.stringify(parameters),
          params,
          cancelToken: sourceRef.current?.token,
        };
        const { data } = await stockMovementApi.getStockMovements(config);
        setTableData({
          data: data.data,
          pages: Math.ceil(data.totalCount / state.pageSize),
          totalCount: data.totalCount,
          currentParams: params,
        });
      } catch {
        Promise.reject(new Error(translate('react.stockMovement.outbound.fetching.error', 'Unable to fetch outbound movements')));
      } finally {
        setLoading(false);
      }
    }
  }, [filterParams]);

  const deleteStockMovement = async (id) => {
    showTheSpinner();
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
      hideTheSpinner();
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

  // List of all actions for outbound Stock Movement rows
  const getActions = useCallback((row) => {
    const {
      isPending, isReturn, order, origin, isReceived, isPartiallyReceived, currentStatus,
    } = row.original;
    const actions = [];

    // Show
    const showAction = {
      defaultLabel: 'Show Stock Movement',
      label: 'react.stockMovement.action.show.label',
      leftIcon: <RiInformationLine />,
      href: '/openboxes/stockMovement/show',
    };
    actions.push(showAction);

    // Edit
    if (!isReceived && !isPartiallyReceived) {
      const editAction = {
        defaultLabel: 'Edit Stock Movement',
        label: 'react.stockMovement.action.edit.label',
        leftIcon: <RiPencilLine />,
      };
      if (isReturn) {
        editAction.href = `/openboxes/stockTransfer/edit/${order?.id}`;
        editAction.appendId = false;
      } else {
        editAction.href = '/openboxes/stockMovement/edit';
      }
      actions.push(editAction);
    }

    const isSameOrigin = currentLocation.id === origin?.id;
    // Delete
    if ((isPending || !currentStatus) && (isSameOrigin || !origin?.isDepot)) {
      const deleteAction = {
        defaultLabel: 'Delete Stock Movement',
        label: 'react.stockMovement.action.delete.label',
        leftIcon: <RiDeleteBinLine />,
        variant: 'danger',
        onClick: deleteConfirmAlert,
      };
      // deleting returns should only be available to admin or higher
      if (!isReturn || (isReturn && isUserAdmin)) {
        actions.push(deleteAction);
      }
    }
    return actions;
  }, []);

  // Columns for react-table
  const columns = useMemo(() => [
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
      Header: <Translate id="react.stockMovement.column.itemsCount.label" defaultMessage="# items" />,
      accessor: 'lineItemCount',
      fixed: 'left',
      className: 'active-circle d-flex justify-content-center',
      headerClassName: 'header justify-content-center',
      width: 80,
      sortable: false,
      Cell: row => (
        <TableCell {...row} defaultValue={0} className="items-count-circle" />),
    },
    {
      Header: <Translate id="react.stockMovement.column.status.label" defaultMessage="Status" />,
      accessor: 'status',
      fixed: 'left',
      width: 150,
      sortable: false,
      Cell: (row) => {
        const status = _.find(requisitionStatuses, _.matchesProperty('id', row.value));
        return (
          <TableCell
            {...row}
            tooltip
            tooltipLabel={getStatusTooltip(row.value)}
          >
            <StatusIndicator variant={status?.variant} status={status?.label} />
          </TableCell>);
      },
    },
    {
      Header: <Translate id="react.stockMovement.column.identifier.label" defaultMessage="Identifier" />,
      accessor: 'identifier',
      fixed: 'left',
      minWidth: 100,
      Cell: row => (
        <TableCell {...row} link={`/openboxes/stockMovement/show/${row.original.id}`} />),
    },
    {
      Header: <Translate id="react.stockMovement.column.name.label" defaultMessage="Name" />,
      accessor: 'name',
      minWidth: 250,
      sortable: false,
      Cell: row => (
        <TableCell
          {...row}
          tooltip
          tooltipLabel={row.original.description || row.original.name}
          link={`/openboxes/stockMovement/show/${row.original.id}`}
        >
          <span className="mx-1">
            {translate(
              `react.StockMovementType.enum.${row.original.stockMovementType}`,
              row.original.stockMovementType,
            )}
          </span>
          <RiArrowRightSLine />
          <span>{row.original.description || row.original.name}</span>
        </TableCell>),
    },
    {
      Header: <Translate id="react.stockMovement.destination.label" defaultMessage="Destination" />,
      accessor: 'destination.name',
      minWidth: 250,
      Cell: row => (<TableCell {...row} tooltip />),
    },
    {
      Header: <Translate id="react.stockMovement.stocklist.label" defaultMessage="Stocklist" />,
      accessor: 'stocklist.name',
      minWidth: 150,
      Cell: row => (<TableCell {...row} tooltip defaultValue="None" />),
    },
    {
      Header: <Translate id="react.stockMovement.requestedBy.label" defaultMessage="Requested by" />,
      accessor: 'requestedBy.name',
      minWidth: 250,
      sortable: false,
      Cell: row => (<TableCell {...row} defaultValue="None" />),
    },
    {
      Header: <Translate id="react.stockMovement.outbound.column.dateRequested.label" defaultMessage="Date Requested" />,
      accessor: 'dateRequested',
      width: 150,
      Cell: row => (<TableCell {...row} value={moment(row.value).format('MMM DD, yyyy')} />),
    },
    {
      Header: <Translate id="react.stockMovement.column.dateCreated.label" defaultMessage="Date Created" />,
      accessor: 'dateCreated',
      width: 150,
      Cell: row => (<TableCell {...row} value={moment(row.value).format('MMM DD, yyyy')} />),
    },
  ], [requisitionStatuses]);

  return (
    <div className="list-page-list-section">
      <div className="title-text p-3 d-flex justify-content-between align-items-center">
        <div>
          {
            isRequestsOpen
            ? <Translate id="react.stockMovement.requests.label" defaultMessage="Requests" />
            : <Translate id="react.stockMovement.outbound.label" defaultMessage="Outbound" />
          }
          <span className="ml-1">{`(${tableData.totalCount})`}</span>
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
          <a className="dropdown-item" onClick={exportPendingShipmentItems} href="#">
            <Translate
              id="react.stockMovement.export.pendingShipmentItems.label"
              defaultMessage="Export pending shipment items"
            />
          </a>
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
  requisitionStatuses: state.requisitionStatuses.data,
  isRequisitionStatusesFetched: state.requisitionStatuses.fetched,
  currentLocation: state.session.currentLocation,
  isUserAdmin: state.session.isUserAdmin,
});

const mapDispatchToProps = {
  showSpinner,
  hideSpinner,
  fetchStatuses: fetchRequisitionStatusCodes,
  showTheSpinner: showSpinner,
  hideTheSpinner: hideSpinner,
};

export default connect(mapStateToProps, mapDispatchToProps)(StockMovementOutboundTable);


StockMovementOutboundTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  translate: PropTypes.func.isRequired,
  fetchStatuses: PropTypes.func.isRequired,
  showTheSpinner: PropTypes.func.isRequired,
  hideTheSpinner: PropTypes.func.isRequired,
  isRequisitionStatusesFetched: PropTypes.bool.isRequired,
  isRequestsOpen: PropTypes.bool.isRequired,
  isUserAdmin: PropTypes.bool.isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  requisitionStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    variant: PropTypes.string,
    label: PropTypes.string,
  })).isRequired,
};
