import React, {
  useCallback,
  useMemo,
  useState,
} from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import * as locales from 'date-fns/locale';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { Controller, useController } from 'react-hook-form';
import { RiDeleteBinLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import {
  getCurrentLocale,
  getDebounceTime,
  getMinSearchLength,
  getUsers,
} from 'selectors';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateFieldDateFns from 'components/form-elements/v2/DateFieldDateFns';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import inboundColumns from 'consts/inboundColumns';
import StockMovementDirection from 'consts/StockMovementDirection';
import { DateFormatDateFns } from 'consts/timeFormat';
import useArrowsNavigation from 'hooks/useArrowsNavigation';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString } from 'utils/dateUtils';
import { debouncePeopleFetch, debounceProductsFetch } from 'utils/option-utils';

const useInboundAddItemsColumns = ({
  errors,
  control,
  removeRow,
  trigger,
  getValues,
  setValue,
  removeSavedRow,
  addNewLine,
}) => {
  const [headerRecipient, setHeaderRecipient] = useState(null);
  const [rowIndex, setRowIndex] = useState(null);
  const [columnId, setColumnId] = useState(null);
  const columnHelper = createColumnHelper();
  const translate = useTranslate();

  const {
    debounceTime,
    minSearchLength,
    users,
    currentLocale,
  } = useSelector((state) => ({
    debounceTime: getDebounceTime(state),
    minSearchLength: getMinSearchLength(state),
    users: getUsers(state),
    currentLocale: getCurrentLocale(state),
  }));
  const debouncedProductsFetch = useCallback(
    debounceProductsFetch(
      debounceTime,
      minSearchLength,
      null,
      false,
      false,
      true,
      false,
      StockMovementDirection.INBOUND,
    ),
    [debounceTime, minSearchLength],
  );
  const lineItems = getValues('values.lineItems');
  const debouncedPeopleFetch = useCallback(
    debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );

  const handleDelete = async (row) => {
    if (getValues('currentLineItems').find((item) => item.id === row?.original?.itemId)) {
      await removeSavedRow(row?.original?.itemId);
    }
    removeRow(row.index);
  };

  const handleBlur = (
    field,
    additionalFieldToOnBlur = null,
  ) => {
    field.onBlur();

    // If some cell is focused, we clear this focus state
    if (rowIndex !== null && columnId !== null) {
      setRowIndex(null);
      setColumnId(null);
    }

    if (additionalFieldToOnBlur) {
      additionalFieldToOnBlur.onBlur();
    }
  };
  const focusableCells = [
    inboundColumns.PALLET_NAME,
    inboundColumns.BOX_NAME,
    inboundColumns.PRODUCT,
    inboundColumns.LOT_NUMBER,
    inboundColumns.EXPIRATION_DATE,
    inboundColumns.QUANTITY_REQUESTED,
    inboundColumns.RECIPIENT,
  ];

  const { handleKeyDown } = useArrowsNavigation({
    newRowFocusableCells: focusableCells,
    existingRowFocusableCells: focusableCells,
    tableData: getValues('values.lineItems') || [],
    setColumnId,
    setRowIndex,
    addNewRow: () => addNewLine(),
    isNewRow: () => true,
    getValues,
    setValue,
    onBlur: () => trigger(),
  });

  const handleHeaderRecipientChange = (selectedRecipient) => {
    setHeaderRecipient(selectedRecipient);
    if (!selectedRecipient) {
      return;
    }

    // we update all the rows with the selected recipient
    _.forEach(lineItems, (item, index) => {
      setValue(`values.lineItems.${index}.recipient`, selectedRecipient);
    });
  };

  const handleFocus = (e) => {
    const input = e.target;
    // We use setTimeout to wait for the input to fully render before calling select()
    setTimeout(() => {
      input.select();
    }, 0);
  };

  const columns = useMemo(() => [
    columnHelper.accessor(inboundColumns.PALLET_NAME, {
      header: () => (
        <TableHeaderCell
          className="justify-content-center rt-th-add-items"
          tooltip
          tooltipLabel={translate('react.stockMovement.packLevel1.label', 'Pack Level 1')}
        >
          {translate('react.stockMovement.packLevel1.label', 'Pack Level 1')}
        </TableHeaderCell>
      ),
      cell: ({ row, column }) => {
        const hasErrors = !!errors?.values?.lineItems?.[row.index]?.palletName?.message;
        const { field: boxNameField } = useController({
          name: `values.lineItems.${row.index}.boxName`,
          control,
        });
        const value = getValues(`values.lineItems.${row.index}.palletName`);
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            customTooltip
            tooltipLabel={errors?.values?.lineItems?.[row.index]?.palletName?.message ?? value}
          >
            <Controller
              name={`values.lineItems.${row.index}.palletName`}
              control={control}
              render={({ field }) => (
                <TextInput
                  className="input-xs"
                  {...field}
                  onKeyDown={(e) => handleKeyDown(e, row.index, column.id)}
                  onBlur={() => handleBlur(field, boxNameField)}
                  onChange={(e) => setValue(`values.lineItems.${row.index}.palletName`, e.target.value ?? null)}
                  focusProps={{
                    fieldIndex: row.index,
                    fieldId: column.id,
                    rowIndex,
                    columnId,
                  }}
                  onFocus={handleFocus}
                  onWheel={(event) => event.currentTarget.blur()}
                  autoComplete="off"
                  hasErrors={hasErrors}
                />
              )}
            />
          </TableCell>
        );
      },
      meta: {
        flexWidth: 1,
      },
    }),
    columnHelper.accessor(inboundColumns.BOX_NAME, {
      header: () => (
        <TableHeaderCell
          className="justify-content-center rt-th-add-items"
          tooltip
          tooltipLabel={translate('react.stockMovement.packLevel2.label', 'Pack Level 2')}
        >
          {translate('react.stockMovement.packLevel2.label', 'Pack Level 2')}
        </TableHeaderCell>
      ),
      cell: ({ row, column }) => {
        const hasErrors = !!errors?.values?.lineItems?.[row.index]?.boxName?.message;
        const value = getValues(`values.lineItems.${row.index}.boxName`);
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            customTooltip
            tooltipLabel={errors?.values?.lineItems?.[row.index]?.boxName?.message ?? value}
          >
            <Controller
              name={`values.lineItems.${row.index}.boxName`}
              control={control}
              render={({ field }) => (
                <TextInput
                  {...field}
                  onKeyDown={(e) => handleKeyDown(e, row.index, column.id)}
                  onBlur={() => handleBlur(field)}
                  focusProps={{
                    fieldIndex: row.index,
                    fieldId: column.id,
                    rowIndex,
                    columnId,
                  }}
                  onFocus={handleFocus}
                  hasErrors={hasErrors}
                  showErrorBorder={hasErrors}
                  className="input-xs"
                  onChange={(e) => setValue(`values.lineItems.${row.index}.boxName`, e.target.value ?? null)}
                  autoComplete="off"
                />
              )}
            />
          </TableCell>
        );
      },
      meta: {
        flexWidth: 1,
      },
    }),
    columnHelper.accessor(inboundColumns.PRODUCT, {
      header: () => (
        <TableHeaderCell
          className="rt-th-add-items"
          required
          tooltip
          tooltipLabel={translate('react.stockMovement.product.label', 'Product')}
        >
          {translate('react.stockMovement.product.label', 'Product')}
        </TableHeaderCell>
      ),
      cell: ({ row, column }) => {
        const hasErrors = !!errors?.values?.lineItems?.[row.index]?.product?.message;
        const value = getValues(`values.lineItems.${row.index}.product`);
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            customTooltip
            tooltipLabel={errors?.values?.lineItems?.[row.index]?.product?.message ?? value?.label}
          >
            <Controller
              name={`values.lineItems.${row.index}.product`}
              control={control}
              render={({ field }) => (
                <SelectField
                  {...field}
                  async
                  loadOptions={debouncedProductsFetch}
                  onChange={(val) => {
                    field?.onChange(val);
                    trigger(`values.lineItems.${row.index}.quantityRequested`);
                  }}
                  onKeyDown={(e) => handleKeyDown(e, row.index, column.id)}
                  onBlur={() => handleBlur(field)}
                  className="select-xs dark-select-xs"
                  focusProps={{
                    fieldIndex: row.index,
                    fieldId: column.id,
                    rowIndex,
                    columnId,
                  }}
                  hasErrors={hasErrors}
                  productSelect
                  // When using ProductSelect instead of Select, ProductSelect sets
                  // showValueTooltip to true by default. This causes the old tooltip
                  // to appear, which we don't want because we're using the new tooltip.
                  // Therefore, we need to explicitly set it to false here.
                  showValueTooltip={false}
                />
              )}
            />
          </TableCell>
        );
      },
      meta: {
        flexWidth: 4,
      },
    }),
    columnHelper.accessor(inboundColumns.LOT_NUMBER, {
      header: () => (
        <TableHeaderCell
          className="justify-content-center rt-th-add-items"
          tooltip
          tooltipLabel={translate('react.stockMovement.lot.label', 'Lot')}
        >
          {translate('react.stockMovement.lot.label', 'Lot')}
        </TableHeaderCell>
      ),
      cell: ({ row, column }) => {
        const hasErrors = !!errors?.values?.lineItems?.[row.index]?.lotNumber?.message;
        const value = getValues(`values.lineItems.${row.index}.lotNumber`);
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            customTooltip
            tooltipLabel={errors?.values?.lineItems?.[row.index]?.lotNumber?.message ?? value}
          >
            <Controller
              name={`values.lineItems.${row.index}.lotNumber`}
              control={control}
              render={({ field }) => (
                <TextInput
                  {...field}
                  hasErrors={hasErrors}
                  className="input-xs"
                  showErrorBorder={hasErrors}
                  onKeyDown={(e) => handleKeyDown(e, row.index, column.id)}
                  onChange={(e) => setValue(`values.lineItems.${row.index}.lotNumber`, e.target.value ?? null)}
                  onBlur={() => handleBlur(field)}
                  onFocus={handleFocus}
                  focusProps={{
                    fieldIndex: row.index,
                    fieldId: column.id,
                    rowIndex,
                    columnId,
                  }}
                  autoComplete="off"
                />
              )}
            />
          </TableCell>
        );
      },
      meta: {
        flexWidth: 1,
      },
    }),
    columnHelper.accessor(inboundColumns.EXPIRATION_DATE, {
      header: () => (
        <TableHeaderCell
          className="justify-content-center rt-th-add-items"
          tooltip
          tooltipLabel={translate('react.stockMovement.expiry.label', 'Expiry')}
        >
          {translate('react.stockMovement.expiry.label', 'Expiry')}
        </TableHeaderCell>
      ),
      cell: ({ row, column }) => {
        const hasErrors = !!errors?.values?.lineItems?.[row.index]?.expirationDate?.message;
        const value = getValues(`values.lineItems.${row.index}.expirationDate`);
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            customTooltip
            tooltipLabel={errors?.values?.lineItems?.[row.index]?.expirationDate?.message
              ?? formatDateToString({
                date: value,
                dateFormat: DateFormatDateFns.DD_MMM_YYYY,
                options: { locale: locales[currentLocale] },
              })}
          >
            <Controller
              name={`values.lineItems.${row.index}.expirationDate`}
              control={control}
              render={({ field }) => (
                <DateFieldDateFns
                  {...field}
                  className="input-xs"
                  hasErrors={hasErrors}
                  showErrorBorder={hasErrors}
                  onKeyDown={(e) => handleKeyDown(e, row.index, column.id)}
                  onBlur={() => handleBlur(field)}
                  focusProps={{
                    fieldIndex: row.index,
                    fieldId: column.id,
                    rowIndex,
                    columnId,
                  }}
                  customDateFormat={DateFormatDateFns.DD_MMM_YYYY}
                  onChange={async (newDate) => {
                    setValue(`values.lineItems.${row.index}.expirationDate`, newDate);
                    await trigger();
                  }}
                />
              )}
            />
          </TableCell>
        );
      },
      meta: {
        flexWidth: 1.5,
      },
    }),
    columnHelper.accessor(inboundColumns.QUANTITY_REQUESTED, {
      header: () => (
        <TableHeaderCell
          required
          className="justify-content-center rt-th-add-items"
          tooltip
          tooltipLabel={translate('react.stockMovement.quantity.label', 'Quantity')}
        >
          {translate('react.stockMovement.quantity.label', 'Quantity')}
        </TableHeaderCell>
      ),
      cell: ({ row, column }) => {
        const hasErrors = !!errors?.values?.lineItems?.[row.index]?.quantityRequested?.message;
        const value = getValues(`values.lineItems.${row.index}.quantityRequested`);
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            customTooltip
            tooltipLabel={errors?.values?.lineItems?.[row.index]?.quantityRequested?.message
              ?? value}
          >
            <Controller
              name={`values.lineItems.${row.index}.quantityRequested`}
              control={control}
              render={({ field }) => (
                <TextInput
                  {...field}
                  type="number"
                  className="hide-arrows input-xs"
                  hasErrors={hasErrors}
                  showErrorBorder={hasErrors}
                  onChange={(e) => setValue(`values.lineItems.${row.index}.quantityRequested`, e ?? null)}
                  onBlur={() => handleBlur(field)}
                  onFocus={handleFocus}
                  onKeyDown={(e) => handleKeyDown(e, row.index, column.id)}
                  focusProps={{
                    fieldIndex: row.index,
                    fieldId: column.id,
                    rowIndex,
                    columnId,
                  }}
                />
              )}
            />
          </TableCell>
        );
      },
      meta: {
        flexWidth: 1,
      },
    }),
    columnHelper.accessor(inboundColumns.RECIPIENT, {
      header: () => (
        <TableHeaderCell className="justify-content-center">
          <SelectField
            async
            loadOptions={debouncedPeopleFetch}
            className="select-xs dark-select-xs"
            onChange={handleHeaderRecipientChange}
            value={headerRecipient}
            placeholder={translate('react.stockMovement.recipient.label', 'Recipient')}
            customTooltip
          />
        </TableHeaderCell>
      ),
      cell: ({ row, column }) => {
        const hasErrors = !!errors?.values?.lineItems?.[row.index]?.recipient?.message;
        const value = getValues(`values.lineItems.${row.index}.recipient`);
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            customTooltip
            tooltipLabel={errors?.values?.lineItems?.[row.index]?.recipient?.message
              ?? value?.label}
          >
            <Controller
              name={`values.lineItems.${row.index}.recipient`}
              control={control}
              render={({ field }) => (
                <SelectField
                  {...field}
                  async
                  loadOptions={debouncedPeopleFetch}
                  hasErrors={hasErrors}
                  onKeyDown={(e) => handleKeyDown(e, row.index, column.id)}
                  onBlur={() => handleBlur(field)}
                  className="select-xs dark-select-xs"
                  focusProps={{
                    fieldIndex: row.index,
                    fieldId: column.id,
                    rowIndex,
                    columnId,
                  }}
                />
              )}
            />
          </TableCell>
        );
      },
      meta: {
        flexWidth: 1.5,
      },
    }),
    columnHelper.display({
      id: inboundColumns.DELETE,
      header: () => (
        <TableHeaderCell
          className="justify-content-center rt-th-add-items"
          tooltip
          tooltipLabel={translate('react.default.button.delete.label', 'Delete')}
        >
          {translate('react.default.button.delete.label', 'Delete')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => (
        <TableCell className="rt-td rt-td-xs rt-td-add-items">
          <div className="bin-container">
            <RiDeleteBinLine
              className="inbound-bin"
              display={row?.original?.statusCode === 'SUBSTITUTED'}
              onClick={() => handleDelete(row)}
            />
          </div>
        </TableCell>
      ),
      meta: {
        flexWidth: 0.5,
      },
    }),
  ], [
    errors,
    control,
    debounceTime,
    minSearchLength,
    users,
    currentLocale,
    headerRecipient,
    rowIndex,
    columnId,
    lineItems,
  ]);

  return { columns };
};

export default useInboundAddItemsColumns;

useInboundAddItemsColumns.propTypes = {
  errors: PropTypes.shape({}).isRequired,
  control: PropTypes.shape({}).isRequired,
  removeRow: PropTypes.func.isRequired,
  trigger: PropTypes.func.isRequired,
  getValues: PropTypes.func.isRequired,
  setValue: PropTypes.func.isRequired,
  removeSavedRow: PropTypes.func.isRequired,
  addNewLine: PropTypes.func.isRequired,
};
