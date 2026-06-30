import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import PropTypes from 'prop-types';
import { RiArrowDownLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import receivingColumns from 'consts/receivingColumns';
import useTranslate from 'hooks/useTranslate';
import ExpirationDateCell from 'utils/cells/ExpirationDateCell';
import MultilineCell from 'utils/cells/MultilineCell';
import ValueCell from 'utils/cells/ValueCell';

/**
 * Columns for the read-only "Received" table inside the edit modal - the records
 * that have already been received for the line. The whole section is view-only,
 * so every cell is a plain display cell; the only interaction is the row action
 * that copies the record down into the editable "Receiving now" table.
 *
 * Kept separate from `useEditLineItemColumns` (the editable table) so the two
 * tables' configs don't share state.
 */
const useReceivedLineItemColumns = ({ copyToReceive }) => {
  const translate = useTranslate();
  const columnHelper = createColumnHelper();
  const currentLocale = useSelector(getCurrentLocale);

  const columns = useMemo(() => [
    columnHelper.accessor(receivingColumns.PRODUCT, {
      header: () => (
        <TableHeaderCell>
          {translate('react.receiving.product.label', 'Product')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => (
        <MultilineCell
          value={row.original.product?.name}
          label="react.receiving.product.label"
          defaultLabel="Product"
          maxLines={2}
        />
      ),
      size: 220,
    }),
    columnHelper.accessor(receivingColumns.LOT_NUMBER, {
      header: () => (
        <TableHeaderCell>
          {translate('react.receiving.lotSerialNo.short.label', 'Lot/SN')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const value = row.original.lotNumber;
        return (
          <ValueCell
            value={value}
            tooltipLabel={value}
            label="react.receiving.lotSerialNo.short.label"
            defaultLabel="Lot/SN"
            truncate
          />
        );
      },
      size: 130,
    }),
    columnHelper.accessor(receivingColumns.EXPIRATION_DATE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.receiving.expirationDate.short.label', 'Exp. Date')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => (
        <ExpirationDateCell
          value={row.original.expirationDate}
          localeKey={currentLocale}
          label="react.receiving.expirationDate.short.label"
          defaultLabel="Exp. Date"
          showExpiryStatus
        />
      ),
      size: 130,
    }),
    columnHelper.accessor(receivingColumns.RECIPIENT, {
      header: () => (
        <TableHeaderCell>
          {translate('react.receiving.recipient.label', 'Recipient')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const recipient = row.original.recipient;
        return (
          <ValueCell
            value={recipient?.name}
            tooltipLabel={recipient?.name}
            label="react.receiving.recipient.label"
            defaultLabel="Recipient"
            truncate
          />
        );
      },
      size: 150,
    }),
    columnHelper.accessor(receivingColumns.QUANTITY_RECEIVED, {
      header: () => (
        <TableHeaderCell className="justify-content-end">
          {translate('react.receiving.received.label', 'Received')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const value = row.original.quantityReceived;
        return (
          <ValueCell
            value={value}
            tooltipLabel={value?.toString()}
            className="text-right w-100"
            label="react.receiving.received.label"
            defaultLabel="Received"
          />
        );
      },
      size: 120,
    }),
    columnHelper.accessor(receivingColumns.LOCATION, {
      header: () => (
        <TableHeaderCell>
          {translate('react.receiving.location.label', 'Location')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const location = row.original.location;
        return (
          <ValueCell
            value={location?.name}
            tooltipLabel={location?.name}
            label="react.receiving.location.label"
            defaultLabel="Location"
            truncate
          />
        );
      },
      size: 150,
    }),
    columnHelper.display({
      id: 'actions',
      header: () => (
        <TableHeaderCell className="justify-content-end">
          {translate('react.receiving.actions.label', 'Actions')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => (
        <TableCell className="rt-td">
          <button
            type="button"
            className="receiving-edit-modal__copy-to-receive d-flex align-items-center justify-content-end gap-8 w-100 h-100 p-0 border-0 bg-transparent cursor-pointer font-weight-500"
            onClick={() => copyToReceive(row.original.rowId)}
          >
            <RiArrowDownLine size={18} />
            {translate('react.receiving.copyToReceive.label', 'Copy To Receive')}
          </button>
        </TableCell>
      ),
      size: 170,
    }),
  ], [translate, currentLocale, copyToReceive]);

  return { columns };
};

useReceivedLineItemColumns.propTypes = {
  copyToReceive: PropTypes.func.isRequired,
};

export default useReceivedLineItemColumns;