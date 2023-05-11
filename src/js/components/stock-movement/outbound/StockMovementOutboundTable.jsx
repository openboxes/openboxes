import React, { useCallback, useMemo } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import {
  RiArrowRightSLine,
  RiDeleteBinLine,
  RiDownload2Line,
  RiInformationLine,
  RiPencilLine,
} from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import DataTable, { TableCell } from 'components/DataTable';
import DateCell from 'components/DataTable/DateCell';
import Button from 'components/form-elements/Button';
import ShipmentIdentifier from 'components/stock-movement/common/ShipmentIdentifier';
import useOutboundListTableData from 'hooks/list-pages/outbound/useOutboundListTableData';
import ActionDots from 'utils/ActionDots';
import { getShipmentTypeTooltip } from 'utils/list-utils';
import { mapShipmentTypes } from 'utils/option-utils';
import StatusIndicator from 'utils/StatusIndicator';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const StockMovementOutboundTable = ({
  filterParams,
  translate,
  requisitionStatuses,
  currentLocation,
  isRequestsOpen,
  isUserAdmin,
}) => {
  const {
    tableData,
    tableRef,
    loading,
    onFetchHandler,
    exportStockMovements,
    exportPendingShipmentItems,
    deleteConfirmAlert,
  } = useOutboundListTableData(filterParams);

  const getStatusTooltip = status => translate(
    `react.stockMovement.status.${status.toLowerCase()}.description.label`,
    status.toLowerCase(),
  );

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
      headerClassName: 'header justify-content-center',
      minWidth: 100,
      Cell: (row) => {
        const { id, shipmentType } = row.original;
        return (
          <TableCell
            {...row}
            link={`/openboxes/stockMovement/show/${id}`}
            tooltip
            tooltipLabel={getShipmentTypeTooltip(translate, shipmentType?.displayName)}
          >
            <ShipmentIdentifier
              shipmentType={mapShipmentTypes(shipmentType)}
              identifier={row?.value}
            />
          </TableCell>
        );
      },
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
      Cell: row => (<DateCell {...row} />),
    },
    {
      Header: <Translate id="react.stockMovement.column.dateCreated.label" defaultMessage="Date Created" />,
      accessor: 'dateCreated',
      width: 150,
      Cell: row => (<DateCell {...row} />),
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
  currentLocation: state.session.currentLocation,
  isUserAdmin: state.session.isUserAdmin,
  currentLocale: state.session.activeLanguage,
});

export default connect(mapStateToProps)(StockMovementOutboundTable);


StockMovementOutboundTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  translate: PropTypes.func.isRequired,
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
