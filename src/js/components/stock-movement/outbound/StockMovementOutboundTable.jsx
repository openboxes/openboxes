import React, { useCallback, useMemo, useState } from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';
import {
  RiArrowGoBackLine,
  RiArrowRightSLine, RiCheckFill,
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
import RejectRequestModal from 'components/stock-movement/modals/RejectRequestModal';
import ActivityCode from 'consts/activityCode';
import { STOCK_MOVEMENT_URL, STOCK_TRANSFER_URL } from 'consts/applicationUrls';
import DateFormat from 'consts/dateFormat';
import RequisitionStatus from 'consts/requisitionStatus';
import useOutboundListTableData from 'hooks/list-pages/outbound/useOutboundListTableData';
import ContextMenu from 'utils/ContextMenu';
import { getShipmentTypeTooltip } from 'utils/list-utils';
import { mapShipmentTypes } from 'utils/option-utils';
import canEditRequest from 'utils/permissionUtils';
import StatusIndicator from 'utils/StatusIndicator';
import { supports } from 'utils/supportedActivitiesUtils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const StockMovementOutboundTable = ({
  filterParams,
  translate,
  requisitionStatuses,
  currentLocation,
  currentUser,
  isRequestsOpen,
  isUserAdmin,
  isUserRequestApprover,
}) => {
  const {
    tableData,
    tableRef,
    loading,
    onFetchHandler,
    exportStockMovements,
    exportPendingShipmentItems,
    deleteConfirmAlert,
    approveRequest,
    rejectRequest,
    rollbackRequest,
  } = useOutboundListTableData(filterParams);
  const [isOpenRejectionModal, setIsOpenRejectionModal] = useState(false);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const getStatusTooltip = (status) => translate(
    `react.stockMovement.status.${status.toLowerCase()}.description.label`,
    status.toLowerCase(),
  );

  const openRejectionModal = (request) => {
    setIsOpenRejectionModal(true);
    setSelectedRequest(request);
  };

  const closeRejectionModal = () => {
    setIsOpenRejectionModal(false);
    setSelectedRequest(null);
  };

  const approverActions = useCallback(
    (row) => {
      const {
        statusCode,
        identifier,
      } = row.original;
      const isUserRequestor = row.original?.requestedBy?.id === currentUser?.id;
      const actions = [];

      const showAction = {
        defaultLabel: 'Show Stock Movement',
        label: 'react.stockMovement.action.show.label',
        leftIcon: <RiInformationLine />,
        href: '/openboxes/stockMovement/show',
      };
      actions.push(showAction);

      if (canEditRequest(currentUser, row.original, currentLocation)) {
        const editAction = {
          defaultLabel: 'Edit Stock Movement',
          label: 'react.stockMovement.action.edit.label',
          leftIcon: <RiPencilLine />,
          href: '/openboxes/stockMovement/edit',
        };

        actions.push(editAction);
      }

      if (statusCode === RequisitionStatus.PENDING_APPROVAL && isUserRequestApprover) {
        const approveAction = {
          defaultLabel: 'Approve',
          label: 'react.stockMovement.action.approve.label',
          leftIcon: <RiCheckFill />,
          onClick: (id) => approveRequest(id, identifier),
        };
        actions.push(approveAction);

        const rejectAction = {
          defaultLabel: 'Reject',
          label: 'react.stockMovement.action.reject.label',
          leftIcon: <RiCloseFill />,
          variant: 'danger',
          onClick: () => {
            openRejectionModal(row.original);
          },
        };
        actions.push(rejectAction);
      }
      if ((statusCode === RequisitionStatus.APPROVED
          || statusCode === RequisitionStatus.REJECTED)
        && (isUserRequestApprover || isUserRequestor || isUserAdmin)) {
        const rollbackAction = {
          defaultLabel: 'Rolllback',
          label: 'react.stockMovement.action.rollback.label',
          leftIcon: <RiArrowGoBackLine />,
          onClick: rollbackRequest,
        };
        actions.push(rollbackAction);
      }
      return actions;
    },
    [],
  );

  // List of all actions for outbound Stock Movement rows
  const getActions = useCallback((row) => {
    const {
      isPending,
      isReturn,
      order,
      origin,
      isReceived,
      isPartiallyReceived,
      currentStatus,
      isApprovalRequired,
    } = row.original;
    const actions = [];

    if (isApprovalRequired
        && supports(origin?.supportedActivities, ActivityCode.APPROVE_REQUEST)
    ) {
      return approverActions(row);
    }

    // Show
    const showAction = {
      defaultLabel: 'Show Stock Movement',
      label: 'react.stockMovement.action.show.label',
      leftIcon: <RiInformationLine />,
      href: STOCK_MOVEMENT_URL.show,
    };
    actions.push(showAction);

    // Edit
    if (
      !isReceived && !isPartiallyReceived
      && canEditRequest(currentUser, row.original, currentLocation)
    ) {
      const editAction = {
        defaultLabel: 'Edit Stock Movement',
        label: 'react.stockMovement.action.edit.label',
        leftIcon: <RiPencilLine />,
      };
      if (isReturn) {
        editAction.href = () => STOCK_TRANSFER_URL.genericEdit(order?.id);
      } else {
        editAction.href = STOCK_MOVEMENT_URL.genericEdit;
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
      Cell: (row) => (
        <ContextMenu
          positions={['right']}
          dropdownClasses="action-dropdown-offset"
          actions={getActions(row)}
          id={row.original.id}
        />
      ),
    },
    {
      Header: <Translate id="react.stockMovement.column.itemsCount.label" defaultMessage="# items" />,
      accessor: 'lineItemCount',
      fixed: 'left',
      className: 'active-circle d-flex justify-content-center',
      headerClassName: 'header justify-content-center',
      width: 80,
      sortable: false,
      Cell: (row) => (
        <TableCell {...row} defaultValue={0} className="items-count-circle" />),
    },
    {
      Header: <Translate id="react.stockMovement.column.status.label" defaultMessage="Status" />,
      accessor: 'displayStatus',
      fixed: 'left',
      width: 170,
      sortable: false,
      Cell: (row) => (
        <TableCell
          {...row}
          tooltip
          tooltipLabel={getStatusTooltip(row.value?.name)}
        >
          <StatusIndicator
            variant={row?.value?.variant}
            status={row?.value?.label}
          />
        </TableCell>
      ),
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
            link={STOCK_MOVEMENT_URL.show(id)}
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
      Cell: (row) => (
        <TableCell
          {...row}
          tooltip
          tooltipLabel={row.original.description || row.original.name}
          link={STOCK_MOVEMENT_URL.show(row.original.id)}
        >
          <span className="mx-1">
            {translate(
              `react.StockMovementType.enum.${row.original.stockMovementType}`,
              row.original.stockMovementType,
            )}
          </span>
          <RiArrowRightSLine />
          <span>{row.original.description || row.original.name}</span>
        </TableCell>
      ),
    },
    {
      Header: <Translate id="react.stockMovement.destination.label" defaultMessage="Destination" />,
      accessor: 'destination.name',
      minWidth: 250,
      Cell: (row) => (<TableCell {...row} tooltip />),
    },
    {
      Header: <Translate id="react.stockMovement.stocklist.label" defaultMessage="Stocklist" />,
      accessor: 'stocklist.name',
      minWidth: 150,
      Cell: (row) => (<TableCell {...row} tooltip defaultValue="None" />),
    },
    {
      Header: <Translate id="react.stockMovement.requestedBy.label" defaultMessage="Requested by" />,
      accessor: 'requestedBy.name',
      minWidth: 250,
      sortable: false,
      Cell: (row) => (<TableCell {...row} defaultValue="None" />),
    },
    {
      Header: <Translate id="react.stockMovement.request.approvers.label" defaultMessage="Approvers" />,
      accessor: 'approvers',
      minWidth: 250,
      sortable: false,
      show: isRequestsOpen,
      Cell: (row) =>
        (
          <TableCell
            {...row}
            tooltip
            defaultValue="None"
            value={row.value?.map((approver) => approver.name)?.join(', ')}
          />
        ),
    },
    {
      Header: <Translate id="react.stockMovement.outbound.column.dateRequested.label" defaultMessage="Date Requested" />,
      accessor: 'dateRequested',
      width: 150,
      Cell: (row) => (
        <DateCell
          localizeDate
          formatLocalizedDate={DateFormat.DISPLAY}
          {...row}
        />
      ),
    },
    {
      Header: <Translate id="react.stockMovement.column.dateCreated.label" defaultMessage="Date Created" />,
      accessor: 'dateCreated',
      width: 150,
      Cell: (row) => (
        <DateCell
          localizeDate
          formatLocalizedDate={DateFormat.DISPLAY}
          {...row}
        />
      ),
    },
  ], [requisitionStatuses, translate, isRequestsOpen]);

  return (
    <>
      <RejectRequestModal
        request={selectedRequest}
        closeRejectionModal={closeRejectionModal}
        isOpenRejectionModal={isOpenRejectionModal}
        rejectRequest={rejectRequest}
      />
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
    </>
  );
};

const mapStateToProps = (state) => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  requisitionStatuses: state.requisitionStatuses.data,
  currentLocation: state.session.currentLocation,
  isUserAdmin: state.session.isUserAdmin,
  isUserRequestApprover: state.session.isUserRequestApprover,
  currentLocale: state.session.activeLanguage,
  currentUser: state.session.user,
});

export default connect(mapStateToProps)(StockMovementOutboundTable);

StockMovementOutboundTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  translate: PropTypes.func.isRequired,
  isRequestsOpen: PropTypes.bool.isRequired,
  isUserAdmin: PropTypes.bool.isRequired,
  isUserRequestApprover: PropTypes.bool.isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  currentUser: PropTypes.shape({
    id: PropTypes.string.isRequired,
  }).isRequired,
  requisitionStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    variant: PropTypes.string,
    label: PropTypes.string,
  })).isRequired,
};
