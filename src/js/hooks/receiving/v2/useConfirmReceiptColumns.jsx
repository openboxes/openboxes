import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import Checkbox from 'components/form-elements/v2/Checkbox';
import receivingColumns from 'consts/receivingColumns';
import ReceivingRowType from 'consts/receivingRowType';
import { ReceivingView } from 'consts/receivingViewOptions';
import useFormatNumber from 'hooks/useFormatNumber';
import useTranslate from 'hooks/useTranslate';
import ActionsCell from 'utils/cells/ActionsCell';
import ExpirationDateCell from 'utils/cells/ExpirationDateCell';
import MultilineCell from 'utils/cells/MultilineCell';
import PackLevelCell from 'utils/cells/PackLevelCell';
import PackLevelGroupCell from 'utils/cells/receiving/PackLevelGroupCell';
import ProductCodeCell from 'utils/cells/receiving/ProductCodeCell';
import ValueCell from 'utils/cells/ValueCell';
import { getConfirmReceiptRowActions } from 'utils/receiving/getReceivingRowActions';
import getReceivingRowStatus from 'utils/receiving/getReceivingRowStatus';

const useConfirmReceiptColumns = ({ view, putawayEnabled } = {}) => {
  const translate = useTranslate();
  const formatNumber = useFormatNumber();
  const columnHelper = createColumnHelper();
  const currentLocale = useSelector(getCurrentLocale);
  const isPackingListView = view === ReceivingView.PACKING_LIST;

  // Rows are { id, meta } objects; the entities live in the normalized state
  // passed through the table `meta`, so each cell reads its item by id at render
  // time. The row `meta` drives row-level greying/disabling of fully received lines.
  const getItem = (row, table) => table.options.meta?.entities?.[row.original.id];

  // Replaced rows of a changed item show the original shipment values struck through
  // (everything except quantities).
  const struckIfReplaced = (rowType) => (rowType === ReceivingRowType.REPLACED ? 'receiving-table__struck' : '');

  // Shipment-level columns (quantities, status, cancel remaining) don't apply to the rows
  // of a changes group.
  const isSplitItemOrToggle = (item) => item?.rowType === ReceivingRowType.SPLIT_ITEM
    || item?.rowType === ReceivingRowType.TOGGLE;

  const quantityCell = (value, label, defaultLabel) => (
    <ValueCell
      value={value}
      tooltipLabel={value}
      label={label}
      defaultLabel={defaultLabel}
    />
  );

  const quantityHeader = (label, defaultLabel) => (
    <TableHeaderCell tooltip tooltipLabel={translate(label, defaultLabel)}>
      {translate(label, defaultLabel)}
    </TableHeaderCell>
  );

  const columns = useMemo(() => {
    const packLevelHeader = () => (
      <TableHeaderCell
        tooltip
        tooltipLabel={translate('react.receiving.packLevel.label', 'Pack Level')}
      >
        {translate('react.receiving.packLevel.label', 'Pack Level')}
      </TableHeaderCell>
    );

    // Third column in table view: shows the item's full pack levels
    const packLevelColumn = columnHelper.display({
      id: receivingColumns.PACK_LEVEL,
      header: packLevelHeader,
      cell: ({ row, table }) => {
        const { container, parentContainer } = getItem(row, table) || {};
        const packLevel1 = parentContainer ? parentContainer.name : container?.name;
        const packLevel2 = parentContainer ? container?.name : null;
        return (
          <PackLevelCell
            packLevel1={packLevel1}
            packLevel2={packLevel2}
            label="react.receiving.packLevel.label"
            defaultLabel="Pack Level"
          />
        );
      },
      size: 140,
    });

    // Leftmost column in packing list view: the item's own pack level.
    // The parent group name is rendered on the separator rows between groups.
    const packLevelGroupColumn = columnHelper.display({
      id: receivingColumns.PACK_LEVEL_GROUP,
      header: packLevelHeader,
      cell: ({ row, table }) => (
        <PackLevelGroupCell
          item={getItem(row, table)}
          isExpanded={row.getIsExpanded()}
          onToggle={row.getToggleExpandedHandler()}
        />
      ),
      meta: {
        pinned: 'left',
        // Light indent on item rows in packing list view.
        getCellContext: () => ({ className: 'receiving-table__pack-level-group' }),
        renderSeparator: ({ row }) => (
          <TableCell
            className="rt-td receiving-table__separator"
            customTooltip
            tooltipLabel={row.original.name}
          >
            <span className="receiving-table__separator-label">
              {row.original.name}
            </span>
          </TableCell>
        ),
      },
      size: 110,
    });

    return [
      // In the packing list view, the first column is the pack level group (parent group name).
      // In the table view, the first column is the product code.
      ...(isPackingListView ? [packLevelGroupColumn] : []),
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
        cell: ({ row, table }) => (
          <ProductCodeCell
            item={getItem(row, table)}
            isPackingListView={isPackingListView}
            isExpanded={row.getIsExpanded()}
            onToggle={row.getToggleExpandedHandler()}
          />
        ),
        meta: {
          pinned: 'left',
        },
        size: 90,
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
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          // Product name is displayed once per changed item - on its first split item row.
          if (item?.rowType === ReceivingRowType.TOGGLE
            || (item?.rowType === ReceivingRowType.SPLIT_ITEM && !item?.isFirstSplitItem)) {
            return <TableCell className="rt-td" />;
          }
          return (
            <MultilineCell
              value={item?.product?.name}
              className={struckIfReplaced(item?.rowType)}
              label="react.receiving.product.label"
              defaultLabel="Product"
              maxLines={2}
            />
          );
        },
        meta: {
          pinned: 'left',
        },
        size: 300,
      }),
      // In the packing list view, the pack level column is not needed
      // because the parent group name is rendered on the separator rows.
      ...(isPackingListView ? [] : [packLevelColumn]),
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
          const item = getItem(row, table);
          const value = item?.lotNumber;
          return (
            <ValueCell
              value={value}
              tooltipLabel={value}
              className={struckIfReplaced(item?.rowType)}
              label="react.receiving.lotSerialNo.short.label"
              defaultLabel="Lot/SN"
              truncate
            />
          );
        },
        size: 125,
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
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          return (
            <ExpirationDateCell
              value={item?.expirationDate}
              localeKey={currentLocale}
              className={struckIfReplaced(item?.rowType)}
              label="react.receiving.expirationDate.short.label"
              defaultLabel="Exp Date"
              showExpiryStatus={item?.rowType !== ReceivingRowType.REPLACED}
            />
          );
        },
        size: 110,
      }),
      columnHelper.display({
        id: receivingColumns.QUANTITY_SHIPPED,
        header: () => quantityHeader('react.receiving.shipped.label', 'Shipped'),
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          if (isSplitItemOrToggle(item)) {
            return null;
          }
          return quantityCell(
            formatNumber(item?.quantityShipped),
            'react.receiving.shipped.label',
            'Shipped',
          );
        },
        size: 100,
      }),
      columnHelper.display({
        id: receivingColumns.QUANTITY_RECEIVED,
        header: () => quantityHeader('react.receiving.received.label', 'Received'),
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          if (isSplitItemOrToggle(item)) {
            return null;
          }
          return quantityCell(
            formatNumber(item?.quantityReceived),
            'react.receiving.received.label',
            'Received',
          );
        },
        size: 100,
      }),
      columnHelper.display({
        id: receivingColumns.QUANTITY_TO_RECEIVE,
        header: () => quantityHeader('react.receiving.toReceive.label', 'To Receive'),
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          if (isSplitItemOrToggle(item)) {
            return null;
          }
          return quantityCell(
            formatNumber(item?.quantityAvailableToReceive),
            'react.receiving.toReceive.label',
            'To Receive',
          );
        },
        size: 110,
      }),
      columnHelper.display({
        id: receivingColumns.QUANTITY_RECEIVING,
        header: () => quantityHeader('react.receiving.receivingNow.label', 'Receiving Now'),
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          if (item?.rowType === ReceivingRowType.REPLACED
            || item?.rowType === ReceivingRowType.TOGGLE) {
            return null;
          }
          const value = item?.quantityReceiving == null
            ? null
            : formatNumber(item.quantityReceiving);
          return quantityCell(value, 'react.receiving.receivingNow.label', 'Receiving Now');
        },
        size: 110,
      }),
      columnHelper.display({
        id: receivingColumns.STATUS,
        header: () => (
          <TableHeaderCell
            tooltip
            tooltipLabel={translate('react.receiving.status.label', 'Status')}
          >
            {translate('react.receiving.status.label', 'Status')}
          </TableHeaderCell>
        ),
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          if (isSplitItemOrToggle(item)) {
            return null;
          }
          const { className, value } = getReceivingRowStatus({
            quantityRemaining: item?.quantityRemaining,
            isCompleted: item?.isCompleted || item?.quantityRemaining === 0,
            translate,
            formatNumber,
          });
          return (
            <ValueCell
              value={value}
              tooltipLabel={value}
              className={className}
              label="react.receiving.status.label"
              defaultLabel="Status"
              truncate
            />
          );
        },
        size: 125,
      }),
      // The Location (putaway bin) column is only shown when "Enable Putaway" is on.
      ...(putawayEnabled ? [
        columnHelper.display({
          id: receivingColumns.LOCATION,
          header: () => (
            <TableHeaderCell
              tooltip
              tooltipLabel={translate('react.receiving.location.label', 'Location')}
            >
              {translate('react.receiving.location.label', 'Location')}
            </TableHeaderCell>
          ),
          cell: ({ row, table }) => {
            const item = getItem(row, table);
            if (item?.rowType === ReceivingRowType.TOGGLE) {
              return null;
            }
            const value = item?.binLocation?.name;
            return (
              <ValueCell
                value={value}
                tooltipLabel={value}
                className={struckIfReplaced(item?.rowType)}
                label="react.receiving.location.label"
                defaultLabel="Location"
                truncate
              />
            );
          },
          size: 125,
        }),
      ] : []),
      columnHelper.display({
        id: 'actions',
        header: () => (
          <TableHeaderCell>
            {translate('react.receiving.actions.label', 'Actions')}
          </TableHeaderCell>
        ),
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          if (isSplitItemOrToggle(item)) {
            return null;
          }
          return (
            <ActionsCell
              actions={getConfirmReceiptRowActions({
                itemId: row.original.id,
                hasComment: Boolean(item?.comment),
                onOpenCommentModal: table.options.meta?.onOpenCommentModal,
              })}
              disabled={item?.isCompleted}
              label="react.receiving.actions.label"
              defaultLabel="Actions"
            />
          );
        },
        size: 90,
      }),
      columnHelper.display({
        id: receivingColumns.CANCEL_REMAINING,
        header: () => (
          <TableHeaderCell
            tooltip
            tooltipLabel={translate('react.receiving.cancelRemaining.label', 'Cancel Remaining')}
          >
            {translate('react.receiving.cancelRemaining.label', 'Cancel Remaining')}
          </TableHeaderCell>
        ),
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          if (isSplitItemOrToggle(item)) {
            return null;
          }
          return (
            <TableCell className="rt-td confirm-receipt__cancel-remaining-cell">
              <Checkbox
                noWrapper
                value={Boolean(table.options.meta?.cancelRemainingIds?.has(row.original.id))}
                onChange={() => table.options.meta?.onToggleCancelRemaining?.(row.original.id)}
                disabled={item?.isCompleted || (item?.quantityAvailableToReceive ?? 0) <= 0}
              />
            </TableCell>
          );
        },
        size: 110,
      }),
    ];
  }, [translate, currentLocale, isPackingListView, putawayEnabled]);

  return { columns };
};

export default useConfirmReceiptColumns;
