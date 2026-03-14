import React, { useMemo } from 'react';

import PropTypes from 'prop-types';
import { RiInformationLine, RiRefreshLine } from 'react-icons/ri';

import DataTable, { TableCell } from 'components/DataTable';
import DateCell from 'components/DataTable/DateCell';
import { PUTAWAY_URL } from 'consts/applicationUrls';
import usePutawayTaskListTableData from 'hooks/list-pages/putaway-task/usePutawayTaskListTableData';
import ContextMenu from 'utils/ContextMenu';
import { findActions } from 'utils/list-utils';
import StatusIndicator from 'utils/StatusIndicator';
import Translate from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const STATUS_VARIANT_MAP = {
  PENDING: 'secondary',
  STARTED: 'info',
  IN_PROGRESS: 'warning',
  COMPLETED: 'success',
  CANCELED: 'danger',
};

const PutawayTaskListTable = ({ filterParams }) => {
  const {
    onFetchHandler,
    rerunHandler,
    loading,
    tableData,
    tableRef,
  } = usePutawayTaskListTableData(filterParams);

  const getActions = (row) => [
    {
      label: 'react.putawayTask.showDetails.label',
      defaultLabel: 'Show Details',
      leftIcon: <RiInformationLine />,
      href: row?.putawayOrder?.id
        ? () => PUTAWAY_URL.show(row.putawayOrder.id)
        : undefined,
    },
    {
      label: 'react.putawayTask.rerunStrategy.label',
      defaultLabel: 'Rerun Strategy',
      leftIcon: <RiRefreshLine />,
      statuses: ['PENDING'],
      onClick: (id) => rerunHandler(id),
    },
  ];

  const columns = useMemo(() => [
    {
      Header: ' ',
      width: 50,
      sortable: false,
      className: 'active-circle d-flex align-items-center',
      style: { overflow: 'visible', zIndex: 1 },
      Cell: (row) => (
        <ContextMenu
          positions={['right']}
          dropdownClasses="action-dropdown-offset"
          actions={findActions(getActions(row.original), row, {})}
          id={row.original.id}
        />
      ),
    },
    {
      Header: <Translate id="react.putawayTask.column.identifier.label" defaultMessage="Putaway #" />,
      accessor: 'putawayOrder.orderNumber',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
      Cell: (row) => (
        <TableCell
          {...row}
          link={row.original.putawayOrder?.id
            ? PUTAWAY_URL.show(row.original.putawayOrder.id)
            : null}
        />
      ),
    },
    {
      Header: <Translate id="react.putawayTask.column.status.label" defaultMessage="Status" />,
      accessor: 'status',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
      maxWidth: 200,
      Cell: (row) => (
        <StatusIndicator
          status={row.value}
          variant={STATUS_VARIANT_MAP[row.value] || 'primary'}
        />
      ),
    },
    {
      Header: <Translate id="react.putawayTask.column.productCode.label" defaultMessage="Product Code" />,
      accessor: 'inventoryItem.product.productCode',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
      maxWidth: 150,
    },
    {
      Header: <Translate id="react.putawayTask.column.productName.label" defaultMessage="Product Name" />,
      accessor: 'inventoryItem.product.name',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
      minWidth: 200,
    },
    {
      Header: <Translate id="react.putawayTask.column.lotNumber.label" defaultMessage="Lot Number" />,
      accessor: 'inventoryItem.lotNumber',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
      maxWidth: 150,
    },
    {
      Header: <Translate id="react.putawayTask.column.location.label" defaultMessage="Location" />,
      accessor: 'location.name',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
    },
    {
      Header: <Translate id="react.putawayTask.column.quantity.label" defaultMessage="Qty" />,
      accessor: 'quantity',
      className: 'd-flex align-items-center justify-content-end',
      headerClassName: 'header',
      maxWidth: 80,
    },
    {
      Header: <Translate id="react.putawayTask.column.container.label" defaultMessage="Container" />,
      accessor: 'container.name',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
    },
    {
      Header: <Translate id="react.putawayTask.column.destination.label" defaultMessage="Destination" />,
      accessor: 'destination.name',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
    },
    {
      Header: <Translate id="react.putawayTask.column.assignee.label" defaultMessage="Assignee" />,
      accessor: 'assignee.name',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
    },
    {
      Header: <Translate id="react.putawayTask.column.strategy.label" defaultMessage="Strategy" />,
      accessor: 'comment',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
    },
    {
      Header: <Translate id="react.putawayTask.column.dateCreated.label" defaultMessage="Date Created" />,
      accessor: 'dateCreated',
      className: 'd-flex align-items-center',
      Cell: (row) => <DateCell {...row} />,
    },
  ], [rerunHandler]);

  return (
    <div className="list-page-list-section">
      <div className="title-text p-3 d-flex justify-content-between align-items-center">
        <span>
          <Translate id="react.putawayTask.list.label" defaultMessage="List Putaway Tasks" />
        </span>
      </div>
      <DataTable
        manual
        sortable
        resizable
        ref={tableRef}
        columns={columns}
        data={tableData.data}
        loading={loading}
        defaultPageSize={10}
        pages={tableData.pages}
        totalData={tableData.totalCount}
        onFetchData={onFetchHandler}
        noDataText="No putaway tasks match the given criteria"
      />
    </div>
  );
};

export default PutawayTaskListTable;

PutawayTaskListTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
};
