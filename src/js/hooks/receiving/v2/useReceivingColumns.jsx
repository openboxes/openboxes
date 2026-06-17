import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import * as locales from 'date-fns/locale';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import TextInput from 'components/form-elements/v2/TextInput';
import receivingColumns from 'consts/receivingColumns';
import { DateFormatDateFns } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString } from 'utils/dateUtils';

const useReceivingColumns = () => {
  const translate = useTranslate();
  const columnHelper = createColumnHelper();
  const currentLocale = useSelector(getCurrentLocale);

  const formatDate = (date) =>
    formatDateToString({
      date,
      dateFormat: DateFormatDateFns.DD_MMM_YYYY,
      options: { locale: locales[currentLocale] },
    });

  const columns = useMemo(() => [
    columnHelper.accessor(receivingColumns.PRODUCT_CODE, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.code.label', 'Code')}
        >
          {translate('react.receiving.code.label', 'Code')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td"
          customTooltip
          tooltipLabel={getValue()}
        >
          <div
            className="text-truncate"
            aria-label={translate('react.receiving.code.label', 'Code')}
          >
            {getValue()}
          </div>
        </TableCell>
      ),
      size: 60,
    }),
    columnHelper.accessor(receivingColumns.PRODUCT, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.product.label', 'Product')}
        >
          {translate('react.receiving.product.label', 'Product')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td multiline-cell"
          customTooltip
          tooltipLabel={getValue()?.name}
        >
          <div className="limit-lines-2">
            {getValue()?.name}
          </div>
        </TableCell>
      ),
      size: 300,
    }),
    columnHelper.display({
      id: receivingColumns.PACK_LEVEL,
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.packLevel.label', 'Pack Level')}
        >
          {translate('react.receiving.packLevel.label', 'Pack Level')}
        </TableHeaderCell>
      ),
      cell: ({ row }) => {
        const { container, parentContainer } = row.original;
        const packLevel1 = parentContainer?.name;
        const packLevel2 = container?.name;
        return (
          <TableCell
            className="rt-td"
            customTooltip
            tooltipLabel={[packLevel1, packLevel2].filter(Boolean).join(' / ')}
          >
            <div aria-label={translate('react.receiving.packLevel.label', 'Pack Level')}>
              {packLevel1 && <div className="text-truncate">{packLevel1}</div>}
              {packLevel2 && <div className="text-truncate">{packLevel2}</div>}
            </div>
          </TableCell>
        );
      },
      size: 100,
    }),
    columnHelper.accessor(receivingColumns.LOT_NUMBER, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.lotSerialNo.label', 'Lot/Serial No.')}
        >
          {translate('react.receiving.lotSerialNo.short.label', 'Lot/SN')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td"
          customTooltip
          tooltipLabel={getValue()}
        >
          <div
            className="text-truncate"
            aria-label={translate('react.receiving.lotSerialNo.short.label', 'Lot/SN')}
          >
            {getValue()}
          </div>
        </TableCell>
      ),
      size: 100,
    }),
    columnHelper.accessor(receivingColumns.EXPIRATION_DATE, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.expirationDate.label', 'Expiration date')}
        >
          {translate('react.receiving.expirationDate.short.label', 'Exp Date')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td"
          customTooltip
          tooltipLabel={formatDate(getValue())}
        >
          <div aria-label={translate('react.receiving.expirationDate.short.label', 'Exp Date')}>
            {formatDate(getValue())}
          </div>
        </TableCell>
      ),
      size: 100,
    }),
    columnHelper.accessor(receivingColumns.RECIPIENT, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.recipient.label', 'Recipient')}
        >
          {translate('react.receiving.recipient.label', 'Recipient')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td"
          customTooltip
          tooltipLabel={getValue()?.name}
        >
          <div
            className="text-truncate"
            aria-label={translate('react.receiving.recipient.label', 'Recipient')}
          >
            {getValue()?.name}
          </div>
        </TableCell>
      ),
      size: 100,
    }),
    columnHelper.accessor(receivingColumns.QUANTITY_SHIPPED, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.shipped.label', 'Shipped')}
        >
          {translate('react.receiving.shipped.label', 'Shipped')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td"
          customTooltip
          tooltipLabel={getValue()?.toString()}
        >
          <div aria-label={translate('react.receiving.shipped.label', 'Shipped')}>
            {getValue()}
          </div>
        </TableCell>
      ),
      size: 80,
    }),
    columnHelper.accessor(receivingColumns.QUANTITY_RECEIVING, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.receivingNow.label', 'Receiving now')}
        >
          {translate('react.receiving.receivingNow.label', 'Receiving Now')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          <TextInput
            type="number"
            className="hide-arrows input-xs"
            defaultValue={getValue()}
            ariaLabel={{
              id: 'react.receiving.receivingNow.label',
              defaultMessage: 'Receiving Now',
            }}
            onWheel={(e) => e.currentTarget.blur()}
          />
        </TableCell>
      ),
      size: 100,
    }),
    columnHelper.accessor(receivingColumns.QUANTITY_REMAINING, {
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.remaining.label', 'Remaining')}
        >
          {translate('react.receiving.remaining.label', 'Remaining')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => {
        const value = getValue();
        return (
          <TableCell
            className="rt-td"
            customTooltip
            tooltipLabel={value?.toString()}
          >
            <div aria-label={translate('react.receiving.remaining.label', 'Remaining')}>
              {value}
            </div>
          </TableCell>
        );
      },
      size: 80,
    }),
    columnHelper.display({
      id: 'actions',
      header: () => (
        <TableHeaderCell>
          {translate('react.receiving.actions.label', 'Actions')}
        </TableHeaderCell>
      ),
      cell: () => <TableCell className="rt-td" />,
      size: 60,
    }),
  ], [translate, currentLocale]);

  return { columns };
};

export default useReceivingColumns;
