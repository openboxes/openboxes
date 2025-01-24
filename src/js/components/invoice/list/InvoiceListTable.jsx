import React, { useMemo } from 'react';

import PropTypes from 'prop-types';
import {
  RiDownload2Line,
  RiFileLine,
  RiInformationLine,
  RiPencilLine,
} from 'react-icons/ri';
import { connect } from 'react-redux';

import DataTable, { TableCell } from 'components/DataTable';
import Button from 'components/form-elements/Button';
import InvoiceStatus from 'components/invoice/list/InvoiceStatus';
import { INVOICE_URL } from 'consts/applicationUrls';
import useInvoiceListTableData from 'hooks/list-pages/invoice/useInvoiceListTableData';
import ContextMenu from 'utils/ContextMenu';
import { findActions } from 'utils/list-utils';
import Translate from 'utils/Translate';

import 'react-table/react-table.css';

const InvoiceListTable = ({
  filterParams,
  supportedActivities,
  highestRole,
  invoiceStatuses,
}) => {
  const {
    tableRef,
    tableData,
    loading,
    onFetchHandler,
    downloadInvoices,
  } = useInvoiceListTableData(filterParams);

  // List of all actions for invoice rows
  const actions = useMemo(() => [
    {
      label: 'react.invoice.viewDetails.label',
      defaultLabel: 'View Invoice Details',
      leftIcon: <RiInformationLine />,
      href: INVOICE_URL.show,
    },
    {
      label: 'react.invoice.addDocument.label',
      defaultLabel: 'Add document',
      leftIcon: <RiFileLine />,
      href: INVOICE_URL.addDocument,
    },
    {
      label: 'react.invoice.edit.label',
      defaultLabel: 'Edit Invoice',
      leftIcon: <RiPencilLine />,
      href: INVOICE_URL.edit,
    },
  ], []);

  // Columns for react-table
  const columns = useMemo(() => [
    {
      Header: ' ',
      width: 50,
      sortable: false,
      style: { overflow: 'visible', zIndex: 1 },
      Cell: (row) => (
        <ContextMenu
          positions={['right']}
          dropdownClasses="action-dropdown-offset"
          actions={findActions(actions, row, { supportedActivities, highestRole })}
          id={row.original.id}
        />
      ),
    },
    {
      Header: <Translate id="react.invoice.column.itemCount.label" defaultMessage="# items" />,
      accessor: 'itemCount',
      className: 'active-circle d-flex justify-content-center',
      headerClassName: 'header justify-content-center',
      maxWidth: 100,
      Cell: (row) => (<TableCell {...row} defaultValue={0} className="items-count-circle" />),
    },
    {
      Header: <Translate id="react.invoice.column.status.label" defaultMessage="Status" />,
      accessor: 'status',
      width: 250,
      Cell: (row) => {
        const label = invoiceStatuses
          && invoiceStatuses.find((status) => status.id === row.original.status).label;
        return (<InvoiceStatus status={label || row.original.status} />);
      },
    },
    {
      Header: <Translate id="react.invoice.typeCode.label" defaultMessage="Invoice Type" />,
      accessor: 'invoiceTypeCode',
      Cell: (row) => (<TableCell {...row} tooltip />),
    },
    {
      Header: <Translate id="react.invoice.column.invoiceNumber.label" defaultMessage="Invoice Number" />,
      accessor: 'invoiceNumber',
      sortable: false,
      Cell: (row) => <TableCell {...row} link={INVOICE_URL.show(row.original.id)} />,
    },
    {
      Header: <Translate id="react.invoice.vendor.label" defaultMessage="Vendor" />,
      accessor: 'partyCode',
    },
    {
      Header: <Translate id="react.invoice.column.vendorInvoiceNumber" defaultMessage="Vendor invoice number" />,
      accessor: 'vendorInvoiceNumber',
      minWidth: 200,
      Cell: (row) => (<TableCell {...row} tooltip />),
    },
    {
      Header: <Translate id="react.invoice.column.totalValue" defaultMessage="Total Value" />,
      accessor: 'totalValue',
      headerClassName: 'text-left',
      sortable: false,
    },
    {
      Header: <Translate id="react.invoice.column.currency" defaultMessage="Currency" />,
      accessor: 'currency',
      className: 'text-left',
    },
  ], [supportedActivities, highestRole, invoiceStatuses]);

  return (
    <div className="list-page-list-section">
      <div className="title-text p-3 d-flex justify-content-between align-items-center">
        <span>
          <Translate id="react.invoice.list.label" defaultMessage="List Invoices" />
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
            <button
              type="button"
              className="dropdown-item"
              onClick={() => downloadInvoices()}
            >
              <Translate
                id="react.invoice.export.invoice.label"
                defaultMessage="Export Invoices"
              />
            </button>
            <button
              type="button"
              className="dropdown-item"
              onClick={() => downloadInvoices(true)}
            >
              <Translate
                id="react.invoice.export.invoiceLineDetails.label"
                defaultMessage="Export Invoice Line Details"
              />
            </button>
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
        totalData={tableData.totalCount}
        defaultPageSize={10}
        pages={tableData.pages}
        onFetchData={onFetchHandler}
        noDataText="No invoices match the given criteria"
      />
    </div>
  );
};

const mapStateToProps = (state) => ({
  supportedActivities: state.session.supportedActivities,
  highestRole: state.session.highestRole,
  invoiceStatuses: state.invoices.statuses,
});

export default connect(mapStateToProps)(InvoiceListTable);

InvoiceListTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
  highestRole: PropTypes.string.isRequired,
  invoiceStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  })).isRequired,
};
