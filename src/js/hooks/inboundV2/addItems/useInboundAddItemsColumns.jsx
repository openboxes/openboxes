import React, {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { Controller, useController } from 'react-hook-form';
import { RiDeleteBinLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import inboundColumns from 'consts/inboundColumns';
import StockMovementDirection from 'consts/StockMovementDirection';
import useArrowsNavigation from 'hooks/useArrowsNavigation';
import useTranslate from 'hooks/useTranslate';
import { debounceProductsFetch, debounceUsersFetch } from 'utils/option-utils';

const useInboundAddItemsColumns = ({
  errors,
  control,
  remove,
  trigger,
  getValues,
  setValue,
  removeItem,
  updateTotalCount,
  currentLineItems,
  append,
  refreshFocusCounter,
}) => {
  const [headerRecipient, setHeaderRecipient] = useState(null);
  const [rowIndex, setRowIndex] = useState(null);
  const [columnId, setColumnId] = useState(null);
  // If prevForceResetFocus is different from refreshFocusCounter,
  // it triggers a reset of rowIndex and columnId.
  const [prevForceResetFocus, setPrevForceResetFocus] = useState(0);
  useEffect(() => {
    if (refreshFocusCounter !== prevForceResetFocus) {
      setRowIndex(null);
      setColumnId(null);
      setPrevForceResetFocus(refreshFocusCounter);
    }
  }, [refreshFocusCounter]);

  const columnHelper = createColumnHelper();
  const translate = useTranslate();

  const {
    debounceTime,
    minSearchLength,
    users,
    locale,
  } = useSelector((state) => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
    users: state.users.data,
    locale: state.session.activeLanguage,
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

  const debouncedUsersFetch = useCallback(
    debounceUsersFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );

  const handleDelete = async (row) => {
    if (currentLineItems.find((item) => item.id === row?.original?.itemId)) {
      await removeItem(row?.original?.itemId);
      updateTotalCount(-1);
    }
    remove(row.index);
  };

  const handleBlur = (
    field,
    additionalFieldToOnBlur = null,
    customLogic = null,
  ) => {
    field.onBlur();

    if (rowIndex !== null && columnId !== null) {
      setRowIndex(null);
      setColumnId(null);
    }

    if (additionalFieldToOnBlur) {
      additionalFieldToOnBlur.onBlur();
    }

    if (customLogic) {
      customLogic();
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
    addNewRow: () => append({}),
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

    const lineItems = getValues('values.lineItems');

    _.forEach(lineItems, (item, index) => {
      setValue(`values.lineItems.${index}.recipient`, selectedRecipient);
    });
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
        const hasErrors = !!errors?.[row.index]?.palletName?.message;
        const { field: boxNameField } = useController({
          name: `values.lineItems.${row.index}.boxName`,
          control,
        });

        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            tooltip={hasErrors}
            tooltipForm
            tooltipLabel={hasErrors && errors[row.index].palletName.message}
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
                  onWheel={(event) => event.currentTarget.blur()}
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
        const hasErrors = !!errors?.[row.index]?.boxName?.message;
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            tooltip={hasErrors}
            tooltipForm
            tooltipLabel={hasErrors && errors[row.index].boxName.message}
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
        const hasErrors = !!errors?.[row.index]?.product?.message;
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            tooltip={getValues(`values.lineItems.${row.index}.product.label`) || hasErrors}
            tooltipForm
            tooltipLabel={hasErrors ? errors[row.index].product.message : getValues(`values.lineItems.${row.index}.product.label`)}
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
        const hasErrors = !!errors?.[row.index]?.lotNumber?.message;
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            tooltip={hasErrors}
            tooltipForm
            tooltipLabel={hasErrors && errors[row.index].lotNumber.message}
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
        const hasErrors = !!errors?.[row.index]?.expirationDate?.message;
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            tooltip={hasErrors}
            tooltipForm
            tooltipLabel={hasErrors && errors[row.index].expirationDate.message}
          >
            <Controller
              name={`values.lineItems.${row.index}.expirationDate`}
              control={control}
              render={({ field }) => (
                <DateField
                  {...field}
                  onChange={(val) => {
                    field?.onChange(val);
                    trigger(`values.lineItems.${row.index}.lotNumber`);
                  }}
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
        const hasErrors = !!errors?.[row.index]?.quantityRequested?.message;
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            tooltip={hasErrors}
            tooltipForm
            tooltipLabel={hasErrors && errors[row.index].quantityRequested.message}
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
                  onBlur={(e) => handleBlur(
                    field,
                    null,
                    () => {
                      const parsedValue = e.target.value ? Number(e.target.value) : undefined;
                      setValue(`values.lineItems.${row.index}.quantityRequested`, parsedValue);
                      trigger(`values.lineItems.${row.index}.quantityRequested`);
                    },
                  )}
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
            loadOptions={debouncedUsersFetch}
            className="select-xs dark-select-xs"
            onChange={handleHeaderRecipientChange}
            value={headerRecipient}
            placeholder={translate('react.stockMovement.recipient.label', 'Recipient')}
          />
        </TableHeaderCell>
      ),
      cell: ({ row, column }) => {
        const hasErrors = !!errors?.[row.index]?.recipient?.message;
        return (
          <TableCell
            className="rt-td rt-td-xs rt-td-add-items"
            tooltip={hasErrors}
            tooltipForm
            tooltipLabel={hasErrors && errors[row.index].recipient.message}
          >
            <Controller
              name={`values.lineItems.${row.index}.recipient`}
              control={control}
              render={({ field }) => (
                <SelectField
                  {...field}
                  async
                  loadOptions={debouncedUsersFetch}
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
    locale,
    headerRecipient,
    rowIndex,
    columnId,
  ]);

  return { columns };
};

export default useInboundAddItemsColumns;

useInboundAddItemsColumns.propTypes = {
  errors: PropTypes.shape({}).isRequired,
  control: PropTypes.shape({}).isRequired,
  remove: PropTypes.func.isRequired,
  trigger: PropTypes.func.isRequired,
  getValues: PropTypes.func.isRequired,
  setValue: PropTypes.func.isRequired,
  removeItem: PropTypes.func.isRequired,
  updateTotalCount: PropTypes.func.isRequired,
  currentLineItems: PropTypes.arrayOf(
    PropTypes.shape({}),
  ).isRequired,
  append: PropTypes.func.isRequired,
};
