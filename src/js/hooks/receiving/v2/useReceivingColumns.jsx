import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateCell from 'components/receivingV2/cells/DateCell';
import MultilineCell from 'components/receivingV2/cells/MultilineCell';
import PackLevelCell from 'components/receivingV2/cells/PackLevelCell';
import QuantityInputCell from 'components/receivingV2/cells/QuantityInputCell';
import ValueCell from 'components/receivingV2/cells/ValueCell';
import receivingColumns from 'consts/receivingColumns';
import useTranslate from 'hooks/useTranslate';

const useReceivingColumns = () => {
  const translate = useTranslate();
  const columnHelper = createColumnHelper();
  const currentLocale = useSelector(getCurrentLocale);

  // Rows are line item ids; the entities live in the normalized state passed
  // through the table `meta`, so each cell reads its item by id at render time.
  const getItem = (row, table) => table.options.meta?.entities?.[row.original];

  const columns = useMemo(() => [
    columnHelper.display({
      id: receivingColumns.PRODUCT_CODE,
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.code.label', 'Code')}
        >
          {translate('react.receiving.code.label', 'Code')}
        </TableHeaderCell>
      ),
      cell: ({ row, table }) => {
        const value = getItem(row, table)?.productCode;
        return (
          <ValueCell
            value={value}
            tooltipLabel={value}
            ariaLabel={translate('react.receiving.code.label', 'Code')}
            truncate
          />
        );
      },
      size: 60,
    }),
    columnHelper.display({
      id: receivingColumns.PRODUCT,
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.product.label', 'Product')}
        >
          {translate('react.receiving.product.label', 'Product')}
        </TableHeaderCell>
      ),
      cell: ({ row, table }) => (
        <MultilineCell value={getItem(row, table)?.product?.name} />
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
      cell: ({ row, table }) => {
        const { container, parentContainer } = getItem(row, table) || {};
        const packLevel1 = parentContainer ? parentContainer.name : container?.name;
        const packLevel2 = parentContainer ? container?.name : null;
        return (
          <PackLevelCell
            packLevel1={packLevel1}
            packLevel2={packLevel2}
            ariaLabel={translate('react.receiving.packLevel.label', 'Pack Level')}
          />
        );
      },
      size: 100,
    }),
    columnHelper.display({
      id: receivingColumns.LOT_NUMBER,
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.lotSerialNo.label', 'Lot/Serial No.')}
        >
          {translate('react.receiving.lotSerialNo.short.label', 'Lot/SN')}
        </TableHeaderCell>
      ),
      cell: ({ row, table }) => {
        const value = getItem(row, table)?.lotNumber;
        return (
          <ValueCell
            value={value}
            tooltipLabel={value}
            ariaLabel={translate('react.receiving.lotSerialNo.short.label', 'Lot/SN')}
            truncate
          />
        );
      },
      size: 100,
    }),
    columnHelper.display({
      id: receivingColumns.EXPIRATION_DATE,
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.expirationDate.label', 'Expiration date')}
        >
          {translate('react.receiving.expirationDate.short.label', 'Exp Date')}
        </TableHeaderCell>
      ),
      cell: ({ row, table }) => (
        <DateCell
          value={getItem(row, table)?.expirationDate}
          localeKey={currentLocale}
          ariaLabel={translate('react.receiving.expirationDate.short.label', 'Exp Date')}
        />
      ),
      size: 100,
    }),
    columnHelper.display({
      id: receivingColumns.RECIPIENT,
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.recipient.label', 'Recipient')}
        >
          {translate('react.receiving.recipient.label', 'Recipient')}
        </TableHeaderCell>
      ),
      cell: ({ row, table }) => {
        const recipient = getItem(row, table)?.recipient;
        return (
          <ValueCell
            value={recipient?.name}
            tooltipLabel={recipient?.name}
            ariaLabel={translate('react.receiving.recipient.label', 'Recipient')}
            truncate
          />
        );
      },
      size: 100,
    }),
    columnHelper.display({
      id: receivingColumns.QUANTITY_SHIPPED,
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.shipped.label', 'Shipped')}
        >
          {translate('react.receiving.shipped.label', 'Shipped')}
        </TableHeaderCell>
      ),
      cell: ({ row, table }) => {
        const value = getItem(row, table)?.quantityShipped;
        return (
          <ValueCell
            value={value}
            tooltipLabel={value?.toString()}
            ariaLabel={translate('react.receiving.shipped.label', 'Shipped')}
          />
        );
      },
      size: 80,
    }),
    columnHelper.display({
      id: receivingColumns.QUANTITY_RECEIVING,
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.receivingNow.label', 'Receiving now')}
        >
          {translate('react.receiving.receivingNow.label', 'Receiving Now')}
        </TableHeaderCell>
      ),
      cell: ({ row, table }) => (
        <QuantityInputCell defaultValue={getItem(row, table)?.quantityReceiving} />
      ),
      size: 100,
    }),
    columnHelper.display({
      id: receivingColumns.QUANTITY_REMAINING,
      header: () => (
        <TableHeaderCell
          tooltip
          tooltipLabel={translate('react.receiving.remaining.label', 'Remaining')}
        >
          {translate('react.receiving.remaining.label', 'Remaining')}
        </TableHeaderCell>
      ),
      cell: ({ row, table }) => {
        const value = getItem(row, table)?.quantityRemaining;
        return (
          <ValueCell
            value={value}
            tooltipLabel={value?.toString()}
            ariaLabel={translate('react.receiving.remaining.label', 'Remaining')}
          />
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
