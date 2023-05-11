import React, { useCallback, useMemo } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import {
  RiDeleteBinLine,
  RiDownload2Line,
  RiInformationLine, RiPencilLine,
} from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import DataTable, { TableCell } from 'components/DataTable';
import DateCell from 'components/DataTable/DateCell';
import Button from 'components/form-elements/Button';
import ShipmentIdentifier from 'components/stock-movement/common/ShipmentIdentifier';
import useInboundListTableData from 'hooks/list-pages/inbound/useInboundListTableData';
import ActionDots from 'utils/ActionDots';
import { getShipmentTypeTooltip } from 'utils/list-utils';
import { mapShipmentTypes } from 'utils/option-utils';
import StatusIndicator from 'utils/StatusIndicator';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const StockMovementInboundTable = ({
  filterParams,
  translate,
  shipmentStatuses,
  currentLocation,
  isUserAdmin,
}) => {
  const {
    tableData,
    tableRef,
    loading,
    onFetchHandler,
    exportAllIncomingItems,
    exportStockMovements,
    deleteConfirmAlert,
  } = useInboundListTableData(filterParams);

  const getStatusTooltip = status => translate(
    `react.stockMovement.status.${status.toLowerCase()}.description.label`,
    status.toLowerCase(),
  );

  // List of all actions for inbound Stock Movement rows
  const getActions = useCallback((row) => {
    const {
      id, isPending, isReturn, order, origin, isReceived, isPartiallyReceived,
    } = row.original;
    const actions = [];

    // Show
    const showAction = {
      defaultLabel: 'Show Stock Movement',
      label: 'react.stockMovement.action.show.label',
      leftIcon: <RiInformationLine />,
      href: '/openboxes/stockMovement/show',
    };
    if (isReturn) {
      showAction.href = `/openboxes/stockMovement/show/${order?.id}`;
      showAction.appendId = false;
    }
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
    if (isPending && (isSameOrigin || !origin?.isDepot)) {
      const deleteAction = {
        defaultLabel: 'Delete Stock Movement',
        label: 'react.stockMovement.action.delete.label',
        leftIcon: <RiDeleteBinLine />,
        variant: 'danger',
        onClick: () => deleteConfirmAlert(isReturn ? order?.id : id),
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
      Cell: row => (<TableCell defaultValue={0} {...row} className="items-count-circle" />),
    },
    {
      Header: <Translate id="react.stockMovement.column.status.label" defaultMessage="Status" />,
      accessor: 'shipmentStatus',
      fixed: 'left',
      width: 170,
      sortable: false,
      Cell: row => (
        <TableCell
          {...row}
          tooltip
          tooltipLabel={getStatusTooltip(row.value)}
        >
          <StatusIndicator
            status={shipmentStatuses.find(status => status.id === row.value)?.label}
            variant={_.find(shipmentStatuses, _.matchesProperty('id', row.value))?.variant}
          />
        </TableCell>),
    },
    {
      Header: <Translate id="react.stockMovement.column.identifier.label" defaultMessage="Identifier" />,
      accessor: 'identifier',
      headerClassName: 'header justify-content-center',
      fixed: 'left',
      minWidth: 100,
      Cell: (row) => {
        const {
          isReturn, id, order, shipmentType,
        } = row.original;
        const stockMovementId = isReturn ? order?.id : id;
        return (
          <TableCell
            {...row}
            link={`/openboxes/stockMovement/show/${stockMovementId}`}
            tooltip
            tooltipLabel={getShipmentTypeTooltip(translate, shipmentType?.displayName)}
          >
            <ShipmentIdentifier
              shipmentType={mapShipmentTypes(shipmentType)}
              identifier={row?.value}
            />
          </TableCell>);
      },
    },
    {
      Header: <Translate id="react.stockMovement.column.name.label" defaultMessage="Name" />,
      accessor: 'name',
      minWidth: 250,
      sortable: false,
      Cell: (row) => {
        const {
          isReturn, id, order, description, name,
        } = row.original;
        const stockMovementId = isReturn ? order?.id : id;
        return (<TableCell
          {...row}
          tooltip
          link={`/openboxes/stockMovement/show/${stockMovementId}`}
          value={description || name}
        />);
      },
    },
    {
      Header: <Translate id="react.stockMovement.origin.label" defaultMessage="Origin" />,
      accessor: 'origin.name',
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
      Header: <Translate id="react.stockMovement.column.dateCreated.label" defaultMessage="Date Created" />,
      accessor: 'dateCreated',
      width: 150,
      Cell: row => (<DateCell {...row} />),
    },
    {
      Header: <Translate id="react.stockMovement.column.expectedReceiptDate" defaultMessage="Expected Receipt Date" />,
      accessor: 'expectedDeliveryDate',
      width: 200,
      Cell: row => (<DateCell {...row} />),
    },
  ], [shipmentStatuses]);

  return (
    <div className="list-page-list-section">
      <div className="title-text p-3 d-flex justify-content-between align-items-center">
        <div>
          <Translate id="react.stockMovement.inbound.label" defaultMessage="Inbound" />
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
  shipmentStatuses: state.shipmentStatuses.data,
  currentLocation: state.session.currentLocation,
  isUserAdmin: state.session.isUserAdmin,
  currentLocale: state.session.activeLanguage,
});

export default connect(mapStateToProps)(StockMovementInboundTable);


StockMovementInboundTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  translate: PropTypes.func.isRequired,
  isUserAdmin: PropTypes.bool.isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  shipmentStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    variant: PropTypes.string,
    label: PropTypes.string,
  })).isRequired,
};
