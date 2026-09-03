import React, { useCallback, useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { RiAddCircleLine, RiDeleteBinLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import {
  getCurrentLocationId,
  getDebounceTime,
  getHasBinLocationSupport,
  getMinSearchLength,
  getReceivingBinLocations,
} from 'selectors';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateFieldDateFns from 'components/form-elements/v2/DateFieldDateFns';
import TextInput from 'components/form-elements/v2/TextInput';
import LocationAutofillHeader from 'components/receivingV2/LocationAutofillHeader';
import receivingColumns from 'consts/receivingColumns';
import { DateFormatDateFns } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';
import QuantityInputCell from 'utils/cells/QuantityInputCell';
import SelectCell from 'utils/cells/SelectCell';
import { debouncePeopleFetch } from 'utils/option-utils';
import CustomTooltip from 'wrappers/CustomTooltip';

// The original line of the shipment item: its product and lot identify the line being received,
// so they are read-only here, and it cannot be removed (it backs the cancel-remaining flow on
// completion) - receiving a different product or lot is done on a split row.
const isOriginalLine = (row) => !row.original?.isSplitItem;

/**
 * Columns for the editable "Receiving now" table in the edit modal.
 */
const useReceivingLineItemColumns = ({
  control,
  addRow,
  removeRow,
  onLocationAutofill,
  errors,
}) => {
  const translate = useTranslate();
  const columnHelper = createColumnHelper();
  const locationId = useSelector(getCurrentLocationId);
  const debounceTime = useSelector(getDebounceTime);
  const minSearchLength = useSelector(getMinSearchLength);
  const binLocationOptions = useSelector(getReceivingBinLocations);
  const hasBinLocationSupport = useSelector(getHasBinLocationSupport);

  const debouncedPeopleFetch = useCallback(
    debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );

  const columns = useMemo(() => [
    columnHelper.accessor(receivingColumns.PRODUCT, {
      header: () => (
        <TableHeaderCell>
          {translate('react.receiving.product.label', 'Product')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => (
        <Controller
          key={row.original.rowId}
          name={`lineItems.${row.index}.product`}
          control={control}
          render={({ field }) => (
            <SelectCell
              {...field}
              productSelect
              locationId={locationId}
              disabled={isOriginalLine(row)}
              label="react.receiving.product.label"
              defaultLabel="Product"
            />
          )}
        />
      ),
      footer: () => (
        <>
          <button
            type="button"
            className="receiving-edit-modal__add-record d-flex align-items-center gap-8 border-0 bg-transparent cursor-pointer font-size-xs"
            data-testid="add-new-record"
            onClick={addRow}
          >
            <RiAddCircleLine size={18} />
            {translate('react.receiving.addNewRecord.label', 'Add new record')}
          </button>
          {translate('react.receiving.totalReceivingNow.label', 'Total Receiving Now')}
        </>
      ),
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
            name={`lineItems.${row.index}.lotNumber`}
            control={control}
            render={({ field }) => (
              <TextInput
                {...field}
                autoComplete="off"
                disabled={isOriginalLine(row)}
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
            name={`lineItems.${row.index}.expirationDate`}
            control={control}
            render={({ field }) => (
              <DateFieldDateFns
                {...field}
                showCustomInput={false}
                customDateFormat={DateFormatDateFns.DD_MMM_YYYY}
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
        <Controller
          key={row.original.rowId}
          name={`lineItems.${row.index}.recipient`}
          control={control}
          render={({ field }) => (
            <SelectCell
              {...field}
              async
              loadOptions={debouncedPeopleFetch}
              showValueTooltip
              label="react.receiving.recipient.label"
              defaultLabel="Recipient"
            />
          )}
        />
      ),
      size: 150,
    }),
    columnHelper.accessor(receivingColumns.QUANTITY_RECEIVING, {
      header: () => (
        <TableHeaderCell className="receiving-table__quantity">
          {translate('react.receiving.receivingNow.label', 'Receiving Now')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const errorMessage = errors?.lineItems?.[row.index]?.quantityReceiving?.message;
        return (
          <Controller
            key={row.original.rowId}
            name={`lineItems.${row.index}.quantityReceiving`}
            control={control}
            render={({ field }) => (
              <QuantityInputCell
                value={field.value}
                onCommit={(quantityReceiving) => {
                  field.onChange(quantityReceiving ?? '');
                  field.onBlur();
                }}
                errorMessage={errorMessage}
                className="receiving-table__quantity"
                label="react.receiving.receivingNow.label"
                defaultLabel="Receiving Now"
              />
            )}
          />
        );
      },
      footer: ({ table }) => (
        <span className="receiving-table__quantity w-100">
          {table.options.meta?.totalReceivingNow ?? 0}
        </span>
      ),
      size: 120,
    }),
    ...(hasBinLocationSupport ? [
      columnHelper.accessor(receivingColumns.LOCATION, {
        header: () => <LocationAutofillHeader onSelect={onLocationAutofill} />,
        cell: ({ row }) => (
          <Controller
            key={row.original.rowId}
            name={`lineItems.${row.index}.binLocation`}
            control={control}
            render={({ field }) => (
              <SelectCell
                {...field}
                options={binLocationOptions}
                label="react.receiving.location.label"
                defaultLabel="Location"
              />
            )}
          />
        ),
        size: 150,
      }),
    ] : []),
    columnHelper.display({
      id: 'actions',
      header: () => (
        <TableHeaderCell className="justify-content-end">
          {translate('react.receiving.actions.label', 'Actions')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const isOriginal = isOriginalLine(row);
        return (
          <TableCell className="rt-td">
            <div
              className="receiving-edit-modal__delete d-flex align-items-center justify-content-end w-100 h-100"
              role="button"
              aria-disabled={isOriginal}
              aria-label={translate('react.default.button.delete.label', 'Delete')}
            >
              <CustomTooltip
                content={translate(
                  'react.receiving.deleteOriginalLine.tooltip.label',
                  'This line cannot be deleted because it represents the original product and lot entered by the shipper. If you did not receive this lot, enter zero in the receiving now field.',
                )}
                show={isOriginal}
              >
                <RiDeleteBinLine
                  size={20}
                  className={isOriginal ? 'disabled-icon' : 'cursor-pointer'}
                  onClick={isOriginal ? undefined : () => removeRow(row.original.rowId)}
                />
              </CustomTooltip>
            </div>
          </TableCell>
        );
      },
      size: hasBinLocationSupport ? 130 : 108,
    }),
  ], [
    translate,
    control,
    addRow,
    locationId,
    debouncedPeopleFetch,
    removeRow,
    binLocationOptions,
    hasBinLocationSupport,
    onLocationAutofill,
    errors,
  ]);

  return { columns };
};

useReceivingLineItemColumns.propTypes = {
  control: PropTypes.shape({}).isRequired,
  addRow: PropTypes.func.isRequired,
  removeRow: PropTypes.func.isRequired,
  onLocationAutofill: PropTypes.func.isRequired,
  errors: PropTypes.shape({}),
};

useReceivingLineItemColumns.defaultProps = {
  errors: {},
};

export default useReceivingLineItemColumns;
