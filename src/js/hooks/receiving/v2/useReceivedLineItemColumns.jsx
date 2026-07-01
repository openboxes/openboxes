import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { RiArrowDownLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { getCurrentLocationId } from 'selectors';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateFieldDateFns from 'components/form-elements/v2/DateFieldDateFns';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import LocationAutofillHeader from 'components/receivingV2/LocationAutofillHeader';
import receivingColumns from 'consts/receivingColumns';
import { DateFormatDateFns } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';

/**
 * Columns for the read-only "Received" table inside the edit modal - the records
 * already received for the line. It mirrors the editable "Receiving now" table
 * (`useReceivingLineItemColumns`), but every field is `disabled`; the only
 * interaction is the row action that copies the record down into that table.
 * Each cell is a react-hook-form `Controller` bound to `receivedItems.${index}.<field>`.
 */
const useReceivedLineItemColumns = ({ control, copyToReceive }) => {
  const translate = useTranslate();
  const columnHelper = createColumnHelper();
  const locationId = useSelector(getCurrentLocationId);

  const columns = useMemo(() => [
    columnHelper.accessor(receivingColumns.PRODUCT, {
      header: () => (
        <TableHeaderCell>
          {translate('react.receiving.product.label', 'Product')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => (
        <TableCell className="rt-td">
          <Controller
            key={row.original.rowId}
            name={`receivedItems.${row.index}.product`}
            control={control}
            render={({ field }) => (
              <SelectField
                {...field}
                productSelect
                locationId={locationId}
                disabled
                hideErrorMessageWrapper
                ariaLabel={{ id: 'react.receiving.product.label', defaultMessage: 'Product' }}
              />
            )}
          />
        </TableCell>
      ),
      footer: () => translate('react.receiving.totalReceived.label', 'Total Received'),
      size: 220,
    }),
    columnHelper.accessor(receivingColumns.LOT_NUMBER, {
      header: () => (
        <TableHeaderCell>
          {translate('react.receiving.lotSerialNo.short.label', 'Lot/SN')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => (
        <TableCell className="rt-td">
          <Controller
            key={row.original.rowId}
            name={`receivedItems.${row.index}.lotNumber`}
            control={control}
            render={({ field }) => (
              <TextInput
                {...field}
                autoComplete="off"
                disabled
                hideErrorMessageWrapper
                ariaLabel={{ id: 'react.receiving.lotSerialNo.short.label', defaultMessage: 'Lot/SN' }}
              />
            )}
          />
        </TableCell>
      ),
      size: 130,
    }),
    columnHelper.accessor(receivingColumns.EXPIRATION_DATE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.receiving.expirationDate.short.label', 'Exp. Date')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => (
        <TableCell className="rt-td">
          <Controller
            key={row.original.rowId}
            name={`receivedItems.${row.index}.expirationDate`}
            control={control}
            render={({ field }) => (
              <DateFieldDateFns
                {...field}
                showCustomInput={false}
                customDateFormat={DateFormatDateFns.DD_MMM_YYYY}
                disabled
                hideErrorMessageWrapper
                ariaLabel={{ id: 'react.receiving.expirationDate.short.label', defaultMessage: 'Exp. Date' }}
              />
            )}
          />
        </TableCell>
      ),
      size: 130,
    }),
    columnHelper.accessor(receivingColumns.RECIPIENT, {
      header: () => (
        <TableHeaderCell>
          {translate('react.receiving.recipient.label', 'Recipient')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => (
        <TableCell className="rt-td">
          <Controller
            key={row.original.rowId}
            name={`receivedItems.${row.index}.recipient`}
            control={control}
            render={({ field }) => (
              <SelectField
                {...field}
                disabled
                hideErrorMessageWrapper
                ariaLabel={{ id: 'react.receiving.recipient.label', defaultMessage: 'Recipient' }}
              />
            )}
          />
        </TableCell>
      ),
      size: 150,
    }),
    columnHelper.accessor(receivingColumns.QUANTITY_RECEIVED, {
      header: () => (
        <TableHeaderCell>
          {translate('react.receiving.received.label', 'Received')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => (
        <TableCell className="rt-td">
          <Controller
            key={row.original.rowId}
            name={`receivedItems.${row.index}.quantityReceived`}
            control={control}
            render={({ field }) => (
              <TextInput
                {...field}
                type="number"
                className="hide-arrows"
                autoComplete="off"
                disabled
                hideErrorMessageWrapper
                ariaLabel={{ id: 'react.receiving.received.label', defaultMessage: 'Received' }}
              />
            )}
          />
        </TableCell>
      ),
      footer: ({ table }) => table.options.meta?.totalReceived ?? 0,
      size: 90,
    }),
    columnHelper.accessor(receivingColumns.LOCATION, {
      header: () => <LocationAutofillHeader />,
      cell: ({ row }) => (
        <TableCell className="rt-td">
          <Controller
            key={row.original.rowId}
            name={`receivedItems.${row.index}.location`}
            control={control}
            render={({ field }) => (
              <SelectField
                {...field}
                disabled
                hideErrorMessageWrapper
                ariaLabel={{ id: 'react.receiving.location.label', defaultMessage: 'Location' }}
              />
            )}
          />
        </TableCell>
      ),
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
            className="receiving-edit-modal__copy-to-receive d-flex align-items-center justify-content-end gap-8 w-100 h-100 p-0 border-0 bg-transparent cursor-pointer text-nowrap"
            onClick={() => copyToReceive(row.original.rowId)}
          >
            <RiArrowDownLine size={18} />
            {translate('react.receiving.copyToReceive.label', 'Copy To Receive')}
          </button>
        </TableCell>
      ),
      // Wide enough to always fit the "Copy to receive" label + icon so it never
      // clips. Columns don't shrink (see getCommonPinningStyles), so the table
      // scrolls instead of squeezing this action on narrow screens. Kept in sync
      // with the "Receiving now" actions column so both tables' columns align.
      size: 160,
      // Keep the "Copy to receive" action fully opaque and usable while the rest
      // of the (read-only) table is faded via the DataTable `disabled` prop.
      meta: { getCellContext: () => ({ className: 'data-table__interactive' }) },
    }),
  ], [translate, control, locationId, copyToReceive]);

  return { columns };
};

useReceivedLineItemColumns.propTypes = {
  control: PropTypes.shape({}).isRequired,
  copyToReceive: PropTypes.func.isRequired,
};

export default useReceivedLineItemColumns;
