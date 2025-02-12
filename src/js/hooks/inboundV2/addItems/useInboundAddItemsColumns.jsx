import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { RiDeleteBinLine, RiErrorWarningLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import StockMovementDirection from 'consts/StockMovementDirection';
import useTranslate from 'hooks/useTranslate';
import { debounceProductsFetch } from 'utils/option-utils';
import Translate from 'utils/Translate';

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
}) => {
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
  const debouncedProductsFetch = debounceProductsFetch(
    debounceTime,
    minSearchLength,
    null,
    false,
    false,
    true,
    false,
    StockMovementDirection.INBOUND,
  );
  const getCustomSelectErrorPlaceholder = ({
    id,
    defaultMessage,
    displayIcon,
  }) => (
    <div className="custom-select-error-placeholder">
      {displayIcon && <RiErrorWarningLine />}
      <span>
        <Translate
          id={id}
          defaultMessage={defaultMessage}
        />
      </span>
    </div>
  );

  const handleDelete = async (row) => {
    if (currentLineItems.find((item) => item.id === row?.original?.itemId)) {
      await removeItem(row?.original?.itemId);
      updateTotalCount(-1);
    }
    remove(row.index);
  };

  const columns = useMemo(() => [
    columnHelper.accessor('palletName', {
      header: () => (
        <TableHeaderCell style={{ justifyContent: 'center' }}>
          {translate('react.stockMovement.packLevel1.label', 'Pack Level 1')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const hasErrors = !!errors?.[row.index]?.palletName?.message;
        return (
          <TableCell
            className="rt-td"
            tooltip={hasErrors}
            tooltipForm
            tooltipLabel={hasErrors && errors[row.index].palletName.message}
          >
            <Controller
              name={`values.lineItems.${row.index}.palletName`}
              control={control}
              render={({ field }) => (
                <TextInput
                  {...field}
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
    columnHelper.accessor('boxName', {
      header: () => (
        <TableHeaderCell style={{ justifyContent: 'center' }}>
          {translate('react.stockMovement.packLevel2.label', 'Pack Level 2')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const isBoxNameDisabled = !getValues(`values.lineItems.${row.index}.palletName`);
        if (isBoxNameDisabled && getValues(`values.lineItems.${row.index}.boxName`) !== '') {
          setValue(`values.lineItems.${row.index}.boxName`, '');
        }
        const hasErrors = !!errors?.[row.index]?.boxName?.message;
        return (
          <TableCell
            className="rt-td"
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
                  disabled={isBoxNameDisabled}
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
    columnHelper.accessor('product', {
      header: () => (
        <TableHeaderCell required>
          {translate('react.stockMovement.product.label', 'Product')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const hasErrors = !!errors?.[row.index]?.product?.message;
        return (
          <TableCell
            className="rt-td"
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
                  placeholder={getCustomSelectErrorPlaceholder({
                    id: 'react.stockMovement.product.label',
                    defaultMessage: 'Product',
                    displayIcon: hasErrors,
                  })}
                  onChange={(val) => {
                    field?.onChange(val);
                    trigger(`values.lineItems.${row.index}.quantityRequested`);
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
    columnHelper.accessor('lotNumber', {
      header: () => (
        <TableHeaderCell style={{ justifyContent: 'center' }}>
          {translate('react.stockMovement.lot.label', 'Lot')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const hasErrors = !!errors?.[row.index]?.lotNumber?.message;
        return (
          <TableCell
            className="rt-td"
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
                  showErrorBorder={hasErrors}
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
    columnHelper.accessor('expirationDate', {
      header: () => (
        <TableHeaderCell style={{ justifyContent: 'center' }}>
          {translate('react.stockMovement.expiry.label', 'Expiry')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const hasErrors = !!errors?.[row.index]?.expirationDate?.message;
        return (
          <TableCell
            className="rt-td"
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
                  hasErrors={hasErrors}
                  showErrorBorder={hasErrors}
                  placeholder="MM/DD/YYYY"
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
    columnHelper.accessor('quantityRequested', {
      header: () => (
        <TableHeaderCell required style={{ justifyContent: 'center' }}>
          {translate('react.stockMovement.quantity.label', 'Quantity')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const hasErrors = !!errors?.[row.index]?.quantityRequested?.message;
        return (
          <TableCell
            className="rt-td"
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
                  hasErrors={hasErrors}
                  showErrorBorder={hasErrors}
                  onBlur={() => {
                    field.onBlur();
                    trigger(`values.lineItems.${row.index}.quantityRequested`);
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
    columnHelper.accessor('recipient', {
      header: () => (
        <TableHeaderCell style={{ justifyContent: 'center' }}>
          {translate('react.stockMovement.recipient.label', 'Recipient')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const hasErrors = !!errors?.[row.index]?.recipient?.message;
        return (
          <TableCell
            className="rt-td"
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
                  options={users}
                  hasErrors={hasErrors}
                  placeholder={getCustomSelectErrorPlaceholder({
                    id: 'react.stockMovement.recipient.label',
                    defaultMessage: 'Recipient',
                    displayIcon: hasErrors,
                  })}
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
      id: 'delete',
      header: () => (
        <TableHeaderCell style={{ justifyContent: 'center' }}>
          {translate('react.default.button.delete.label', 'Delete')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => (
        <TableCell className="rt-td">
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
};
