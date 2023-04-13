import React, { useMemo } from 'react';

import PropTypes from 'prop-types';
import {
  RiDeleteBinLine,
  RiDownload2Line,
  RiDownloadLine,
  RiEraserLine,
  RiFile3Line,
  RiFileCopyLine,
  RiFileForbidLine,
  RiInformationLine,
  RiListUnordered,
  RiPencilLine,
  RiUploadLine,
} from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import DataTable, { TableCell } from 'components/DataTable';
import Button from 'components/form-elements/Button';
import useStockListTableData from 'hooks/list-pages/stock-list/useStockListTableData';
import ActionDots from 'utils/ActionDots';
import { findActions } from 'utils/list-utils';
import StatusIndicator from 'utils/StatusIndicator';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const StockListTable = ({
  filterParams,
  translate,
  highestRole,
}) => {
  const {
    tableData,
    tableRef,
    loading,
    onFetchHandler,
    exportStockList,
    onClickClearStocklists,
    onClickDeleteStocklists,
    unpublishStocklists,
    publishStocklists,
    cloneStocklists,
    exportStockListItems,
  } = useStockListTableData(filterParams);

  const customActionFilter = ({ isPublished }, row) => {
    // skip actions that don't have isPublished property
    if (isPublished === undefined) return true;
    // show actions that have same boolean value in row and in action
    return row.original.isPublished === isPublished;
  };

  // List of all actions for Stocklists rows
  const actions = useMemo(() => [
    {
      defaultLabel: 'Show stock list',
      label: 'react.stocklists.show.label',
      leftIcon: <RiInformationLine />,
      href: '/openboxes/requisitionTemplate/show',
    },
    {
      defaultLabel: 'Edit stock list',
      label: 'react.stocklists.editStock.label',
      leftIcon: <RiPencilLine />,
      href: '/openboxes/requisitionTemplate/editHeader',
      minimumRequiredRole: 'Admin',
    },
    {
      defaultLabel: 'Edit stock list items',
      label: 'react.stocklists.items.editStock.label',
      leftIcon: <RiListUnordered />,
      href: '/openboxes/requisitionTemplate/edit',
      minimumRequiredRole: 'Admin',
    },
    {
      defaultLabel: 'Import stock list items',
      label: 'react.stocklists.items.import.label',
      leftIcon: <RiUploadLine />,
      href: '/openboxes/requisitionTemplate/batch',
      minimumRequiredRole: 'Admin',
    },
    {
      defaultLabel: 'Export stock list items',
      label: 'react.stocklists.items.export.label',
      leftIcon: <RiDownloadLine />,
      onClick: exportStockListItems,
      minimumRequiredRole: 'Admin',
    },
    {
      defaultLabel: 'Clone stock list',
      label: 'react.stocklists.clone.label',
      leftIcon: <RiFileCopyLine />,
      onClick: cloneStocklists,
      minimumRequiredRole: 'Admin',
    },
    {
      defaultLabel: 'Publish stock list',
      label: 'react.stocklists.publish.label',
      leftIcon: <RiFile3Line />,
      isPublished: false,
      onClick: publishStocklists,
      minimumRequiredRole: 'Admin',
    },
    {
      defaultLabel: 'Unpublish stock list',
      label: 'react.stocklists.unpubish.label',
      leftIcon: <RiFileForbidLine />,
      isPublished: true,
      onClick: unpublishStocklists,
      minimumRequiredRole: 'Admin',
    },
    {
      defaultLabel: 'Clear stock list items',
      label: 'react.stocklists.items.clear.label',
      leftIcon: <RiEraserLine />,
      variant: 'danger',
      onClick: onClickClearStocklists,
      minimumRequiredRole: 'Admin',
    },
    {
      defaultLabel: 'Delete stock list',
      label: 'react.stocklists.delete.label',
      leftIcon: <RiDeleteBinLine />,
      variant: 'danger',
      onClick: onClickDeleteStocklists,
      minimumRequiredRole: 'Admin',
    },
  ], []);

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
          actions={findActions(actions, row, { customFilter: customActionFilter, highestRole })}
          id={row.original.id}
        />),
    },
    {
      Header: <Translate id="react.stocklists.column.status.label" defaultMessage="Status" />,
      accessor: 'isPublished',
      fixed: 'left',
      width: 150,
      Cell: row => (
        <StatusIndicator
          status={row.original.isPublished ? 'Published' : 'Draft'}
          variant={row.original.isPublished ? 'success' : 'danger'}
        />),
    },
    {
      Header: <Translate id="react.stocklists.column.name.label" defaultMessage="Name" />,
      accessor: 'name',
      fixed: 'left',
      minWidth: 250,
      Cell: row => (
        <TableCell
          {...row}
          tooltip
          link={`/openboxes/requisitionTemplate/show/${row.original.id}`}
        />),
    },
    {
      Header: <Translate id="react.stocklists.filters.origin.label" defaultMessage="Origin" />,
      accessor: 'origin',
      minWidth: 250,
      fixed: 'left',
      Cell: row => (<TableCell {...row} tooltip />),
    },
    {
      Header: <Translate id="react.stocklists.filters.destination.label" defaultMessage="Destination" />,
      accessor: 'destination',
      minWidth: 250,
      fixed: 'left',
      Cell: row => (<TableCell {...row} tooltip />),
    },
    {
      Header: <Translate id="react.stocklists.column.requisitionItems.label" defaultMessage="Requisition items" />,
      accessor: 'requisitionItemCount',
      sortable: false,
      minWidth: 150,
    },
    {
      Header: <Translate id="react.stocklists.column.requestedBy.label" defaultMessage="Requested by" />,
      accessor: 'requestedBy',
      minWidth: 250,
    },
    {
      Header: <Translate id="react.stocklists.column.createdBy.label" defaultMessage="Created by" />,
      accessor: 'createdBy',
      minWidth: 250,
    },
    {
      Header: <Translate id="react.stocklists.column.updatedBy.label" defaultMessage="Updated by" />,
      accessor: 'updatedBy',
      minWidth: 250,
    },
    {
      Header: <Translate id="react.stocklists.column.dateCreated.label" defaultMessage="Date Created" />,
      accessor: 'dateCreated',
      width: 150,
    },
    {
      Header: <Translate id="react.stocklists.column.lastUpdated.label" defaultMessage="Last Updated" />,
      accessor: 'lastUpdated',
      width: 150,
    },
  ], [highestRole]);

  return (
    <div className="list-page-list-section">
      <div className="title-text p-3 d-flex justify-content-between align-items-center">
        <div>
          <Translate id="react.stocklists.label" defaultMessage="Stock Lists" />
          <span className="ml-1">{`(${tableData.totalCount})`}</span>
        </div>
        <Button
          label="react.default.button.export.label"
          defaultLabel="Export"
          variant="secondary"
          EndIcon={<RiDownload2Line />}
          onClick={exportStockList}
        />
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
          'react.stocklists.empty.label',
          'No stock lists match the given criteria',
        )}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  highestRole: state.session.highestRole,
});

export default connect(mapStateToProps)(StockListTable);


StockListTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  translate: PropTypes.func.isRequired,
  highestRole: PropTypes.string.isRequired,
};
