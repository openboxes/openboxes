import React, { useCallback, useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { RiDeleteBinLine } from 'react-icons/ri';
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
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import LocationAutofillHeader from 'components/receivingV2/LocationAutofillHeader';
import receivingColumns from 'consts/receivingColumns';
import { DateFormatDateFns } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';
import QuantityInputCell from 'utils/cells/QuantityInputCell';
import { debouncePeopleFetch } from 'utils/option-utils';

/**
 * Columns for the editable "Receiving now" table in the edit modal.
 */
const useReceivingLineItemColumns = ({
  control,
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
        <TableCell className="rt-td">
          <Controller
            key={row.original.rowId}
            name={`lineItems.${row.index}.product`}
            control={control}
            render={({ field }) => (
              <SelectField
                {...field}
                productSelect
                locationId={locationId}
                hideErrorMessageWrapper
                ariaLabel={{ id: 'react.receiving.product.label', defaultMessage: 'Product' }}
              />
            )}
          />
        </TableCell>
      ),
      footer: () => translate('react.receiving.totalReceivingNow.label', 'Total Receiving Now'),
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
        <TableCell className="rt-td">
          <Controller
            key={row.original.rowId}
            name={`lineItems.${row.index}.recipient`}
            control={control}
            render={({ field }) => (
              <SelectField
                {...field}
                async
                loadOptions={debouncedPeopleFetch}
                hideErrorMessageWrapper
                showValueTooltip
                ariaLabel={{ id: 'react.receiving.recipient.label', defaultMessage: 'Recipient' }}
              />
            )}
          />
        </TableCell>
      ),
      size: 150,
    }),
    columnHelper.accessor(receivingColumns.QUANTITY_RECEIVING, {
      header: () => (
        <TableHeaderCell>
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
                label="react.receiving.receivingNow.label"
                defaultLabel="Receiving Now"
              />
            )}
          />
        );
      },
      footer: ({ table }) => <span style={{ paddingLeft: '14px' }}>{table.options.meta?.totalReceivingNow ?? 0}</span>,
      size: 120,
    }),
    ...(hasBinLocationSupport ? [
      columnHelper.accessor(receivingColumns.LOCATION, {
        header: () => <LocationAutofillHeader onSelect={onLocationAutofill} />,
        cell: ({ row }) => (
          <TableCell className="rt-td">
            <Controller
              key={row.original.rowId}
              name={`lineItems.${row.index}.binLocation`}
              control={control}
              render={({ field }) => (
                <SelectField
                  {...field}
                  options={binLocationOptions}
                  hideErrorMessageWrapper
                  ariaLabel={{ id: 'react.receiving.location.label', defaultMessage: 'Location' }}
                />
              )}
            />
          </TableCell>
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
      cell: ({ row }) => (
        <TableCell className="rt-td">
          <div
            className="receiving-edit-modal__delete d-flex align-items-center justify-content-end w-100 h-100"
            role="button"
            aria-label={translate('react.default.button.delete.label', 'Delete')}
          >
            <RiDeleteBinLine
              size={20}
              className="cursor-pointer"
              onClick={() => removeRow(row.original.rowId)}
            />
          </div>
        </TableCell>
      ),
      size: hasBinLocationSupport ? 130 : 108,
    }),
  ], [
    translate,
    control,
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
  removeRow: PropTypes.func.isRequired,
  onLocationAutofill: PropTypes.func.isRequired,
  errors: PropTypes.shape({}),
};

useReceivingLineItemColumns.defaultProps = {
  errors: {},
};

export default useReceivingLineItemColumns;
