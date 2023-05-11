import React, { useMemo } from 'react';

import PropTypes from 'prop-types';
import {
  RiDeleteBinLine,
  RiInformationLine,
} from 'react-icons/ri';
import { connect } from 'react-redux';

import DataTable, { TableCell } from 'components/DataTable';
import DateCell from 'components/DataTable/DateCell';
import StockTransferStatus from 'components/stock-transfer/list/StockTransferStatus';
import useStockTransferListTableData from 'hooks/list-pages/stock-transfer/useStockTransferListTableData';
import ActionDots from 'utils/ActionDots';
import { findActions } from 'utils/list-utils';
import Translate from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';


const StockTransferListTable = ({
  filterParams,
  highestRole,
  statuses,
}) => {
  const {
    onFetchHandler,
    deleteHandler,
    loading,
    tableData,
    tableRef,
  } = useStockTransferListTableData(filterParams);

  const actions = useMemo(() => [
    {
      label: 'react.stockTransfer.view.label',
      defaultLabel: 'View stock transfer',
      leftIcon: <RiInformationLine />,
      href: '/openboxes/stockTransfer/show',
    },
    {
      label: 'react.stockTransfer.delete.label',
      defaultLabel: 'Delete',
      leftIcon: <RiDeleteBinLine />,
      minimumRequiredRole: 'Manager',
      statuses: ['PENDING', 'APPROVED'],
      variant: 'danger',
      onClick: id => deleteHandler(id),
    },
  ], []);


  // Columns for react-table
  const columns = useMemo(() => [
    {
      Header: ' ',
      width: 50,
      sortable: false,
      className: 'active-circle d-flex align-items-center',
      style: { overflow: 'visible', zIndex: 1 },
      Cell: row => (
        <ActionDots
          dropdownPlacement="right"
          dropdownClasses="action-dropdown-offset"
          actions={findActions(actions, row, { highestRole })}
          id={row.original.id}
        />),
    },
    {
      Header: <Translate id="react.stockTransfer.column.lineItems.label" defaultMessage="Line items" />,
      accessor: 'orderItemsCount',
      className: 'd-flex align-items-center justify-content-center',
      sortable: false,
      maxWidth: 100,
      Cell: row => (<span className="items-count-circle d-flex align-items-center justify-content-center align-self-center">{row.original.orderItemsCount}</span>),
    },
    {
      Header: <Translate id="react.stockTransfer.column.status.label" defaultMessage="Status" />,
      accessor: 'status',
      className: 'active-circle d-flex',
      headerClassName: 'header',
      Cell: (row) => {
        const label = statuses?.find(status => status.id === row.original.status)?.label;
        return (<StockTransferStatus status={label ?? row.original.status} />);
      },
      maxWidth: 250,
    },
    {
      Header: <Translate id="react.stockTransfer.column.transferNumber.label" defaultMessage="Transfer number" />,
      accessor: 'orderNumber',
      className: 'active-circle d-flex align-items-center',
      headerClassName: 'header',
      Cell: row => <TableCell {...row} link={`/openboxes/stockTransfer/show/${row.original.id}`} />,
    },
    {
      Header: <Translate id="react.stockTransfer.column.createdBy.label" defaultMessage="Created by" />,
      accessor: 'createdBy',
      className: 'active-circle d-flex align-items-center',
      headerClassName: 'header',
    },
    {
      Header: <Translate id="react.stockTransfer.column.dateCreated.label" defaultMessage="Date created" />,
      accessor: 'dateCreated',
      className: 'd-flex align-items-center',
      Cell: row => <DateCell {...row} tooltip />,
    },
  ], [highestRole, statuses]);

  return (
    <div className="list-page-list-section">
      <div className="title-text p-3 d-flex justify-content-between align-items-center">
        <span>
          <Translate id="react.stockTransfer.list.label" defaultMessage="List Stock Transfers" />
        </span>
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
        noDataText="No stock transfers match the given criteria"
      />
    </div>
  );
};

const mapStateToProps = state => ({
  highestRole: state.session.highestRole,
  statuses: state.stockTransfer.statuses,
});


export default connect(mapStateToProps)(StockTransferListTable);


StockTransferListTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  highestRole: PropTypes.string.isRequired,
  statuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  })).isRequired,
};
