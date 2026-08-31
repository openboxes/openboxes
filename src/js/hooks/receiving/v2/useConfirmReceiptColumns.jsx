import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { useSelector } from 'react-redux';
import {
  getCurrentLocale,
  getHasBinLocationSupport,
  getHasPartialReceivingSupport,
  getIsShipmentFromPurchaseOrder,
} from 'selectors';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import Checkbox from 'components/form-elements/v2/Checkbox';
import receivingColumns from 'consts/receivingColumns';
import ReceivingRowType from 'consts/receivingRowType';
import { ReceivingView } from 'consts/receivingViewOptions';
import { isCancellableRow } from 'hooks/receiving/v2/useCancelRemaining';
import useFormatNumber from 'hooks/useFormatNumber';
import useTranslate from 'hooks/useTranslate';
import ActionsCell from 'utils/cells/ActionsCell';
import ExpirationDateCell from 'utils/cells/ExpirationDateCell';
import MultilineCell from 'utils/cells/MultilineCell';
import PackLevelCell from 'utils/cells/PackLevelCell';
import PackLevelGroupCell from 'utils/cells/receiving/PackLevelGroupCell';
import ProductCodeCell from 'utils/cells/receiving/ProductCodeCell';
import ShippedQuantityCell from 'utils/cells/receiving/ShippedQuantityCell';
import ValueCell from 'utils/cells/ValueCell';
import { getConfirmReceiptRowActions } from 'utils/receiving/getReceivingRowActions';
import getReceivingRowStatus from 'utils/receiving/getReceivingRowStatus';
import struckIfChanged from 'utils/receiving/struckIfChanged';

const useConfirmReceiptColumns = ({
  view,
  hasPreviousReceipts,
  sortableProps,
  sort,
  order,
  showLotNumber,
  showExpirationDate,
  showRecipient,
  showPackLevel,
} = {}) => {
  const translate = useTranslate();
  const formatNumber = useFormatNumber();
  const columnHelper = createColumnHelper();
  const currentLocale = useSelector(getCurrentLocale);
  const hasPartialReceivingSupport = useSelector(getHasPartialReceivingSupport);
  const hasBinLocationSupport = useSelector(getHasBinLocationSupport);
  const isShipmentFromPurchaseOrder = useSelector(getIsShipmentFromPurchaseOrder);
  const isPackingListView = view === ReceivingView.PACKING_LIST;

  const sortHeaderProps = (columnId) => (isPackingListView ? {} : {
    sortable: true,
    columnId,
    ...sortableProps,
  });

  // Rows are { id, meta } objects; the entities live in the normalized state
  // passed through the table `meta`, so each cell reads its item by id at render
  // time. The row `meta` drives row-level greying/disabling of fully received lines.
  const getItem = (row, table) => table.options.meta?.entities?.[row.original.id];

  // Shipment-level columns (quantities, status, cancel remaining) don't apply to the rows
  // of a changes group.
  const isSplitItemOrToggle = (item) => item?.rowType === ReceivingRowType.SPLIT_ITEM
    || item?.rowType === ReceivingRowType.TOGGLE;

  const quantityCell = (value, label, defaultLabel) => (
    <ValueCell
      value={value}
      tooltipLabel={value}
      className="receiving-table__quantity"
      label={label}
      defaultLabel={defaultLabel}
    />
  );

  const quantityHeader = (label, defaultLabel, columnId) => (
    <TableHeaderCell
      {...(columnId ? sortHeaderProps(columnId) : {})}
      tooltip
      tooltipLabel={translate(label, defaultLabel)}
      className="receiving-table__quantity"
    >
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
            {...sortHeaderProps(receivingColumns.PRODUCT_CODE)}
            tooltip
            tooltipLabel={translate('react.receiving.code.label', 'Code')}
          >
            {translate('react.receiving.code.label', 'Code')}
          </TableHeaderCell>
        ),
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          return (
            <ProductCodeCell
              item={item}
              isPackingListView={isPackingListView}
              isExpanded={row.getIsExpanded()}
              onToggle={row.getToggleExpandedHandler()}
              className={struckIfChanged(item, 'productChanged')}
            />
          );
        },
        meta: {
          pinned: 'left',
        },
        size: 90,
      }),
      columnHelper.display({
        id: receivingColumns.PRODUCT,
        header: () => (
          <TableHeaderCell
            {...sortHeaderProps(receivingColumns.PRODUCT)}
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
              className={struckIfChanged(item, 'productChanged')}
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
      ...(isShipmentFromPurchaseOrder ? [
        columnHelper.display({
          id: receivingColumns.SUPPLIER_CODE,
          header: () => (
            <TableHeaderCell
              {...sortHeaderProps(receivingColumns.SUPPLIER_CODE)}
              tooltip
              tooltipLabel={translate('react.receiving.supplierItemCode.label', 'Supplier Item Code')}
              className="text-left"
            >
              {translate('react.receiving.supplierItemCode.label', 'Supplier Item Code')}
            </TableHeaderCell>
          ),
          cell: ({ row, table }) => {
            const item = getItem(row, table);
            if (isSplitItemOrToggle(item)) {
              return null;
            }
            return (
              <ValueCell
                value={item?.supplierCode}
                tooltipLabel={item?.supplierCode}
                label="react.receiving.supplierItemCode.label"
                defaultLabel="Supplier Item Code"
                truncate
              />
            );
          },
          size: 125,
        }),
      ] : []),
      // In the packing list view, the pack level column is not needed
      // because the parent group name is rendered on the separator rows.
      ...(isPackingListView || !showPackLevel ? [] : [packLevelColumn]),
      ...(showLotNumber ? [
        columnHelper.display({
          id: receivingColumns.LOT_NUMBER,
          header: () => (
            <TableHeaderCell
              {...sortHeaderProps(receivingColumns.LOT_NUMBER)}
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
                className={struckIfChanged(item, 'lotChanged')}
                label="react.receiving.lotSerialNo.short.label"
                defaultLabel="Lot/SN"
                truncate
              />
            );
          },
          size: 125,
        }),
      ] : []),
      ...(showExpirationDate ? [
        columnHelper.display({
          id: receivingColumns.EXPIRATION_DATE,
          header: () => (
            <TableHeaderCell
              {...sortHeaderProps(receivingColumns.EXPIRATION_DATE)}
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
                className={struckIfChanged(item, 'expirationChanged')}
                label="react.receiving.expirationDate.short.label"
                defaultLabel="Exp Date"
                showExpiryStatus={item?.rowType !== ReceivingRowType.REPLACED}
              />
            );
          },
          size: 110,
        }),
      ] : []),
      ...(showRecipient ? [
        columnHelper.display({
          id: receivingColumns.RECIPIENT,
          header: () => (
            <TableHeaderCell
              {...sortHeaderProps(receivingColumns.RECIPIENT)}
              tooltip
              tooltipLabel={translate('react.receiving.recipient.label', 'Recipient')}
            >
              {translate('react.receiving.recipient.label', 'Recipient')}
            </TableHeaderCell>
          ),
          cell: ({ row, table }) => {
            const item = getItem(row, table);
            const recipient = item?.recipient;
            return (
              <ValueCell
                value={recipient?.name}
                tooltipLabel={recipient?.name}
                className={struckIfChanged(item, 'recipientChanged')}
                label="react.receiving.recipient.label"
                defaultLabel="Recipient"
                truncate
              />
            );
          },
          size: 125,
        }),
      ] : []),
      columnHelper.display({
        id: receivingColumns.QUANTITY_SHIPPED,
        header: () => quantityHeader(
          'react.receiving.shipped.label',
          'Shipped',
          receivingColumns.QUANTITY_SHIPPED,
        ),
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          if (isSplitItemOrToggle(item)) {
            return null;
          }
          return (
            <ShippedQuantityCell
              item={item}
              isShipmentFromPurchaseOrder={isShipmentFromPurchaseOrder}
              label="react.receiving.shipped.label"
              defaultLabel="Shipped"
            />
          );
        },
        size: 100,
      }),
      ...(hasPreviousReceipts ? [
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
      ] : []),
      columnHelper.display({
        id: receivingColumns.QUANTITY_RECEIVING,
        header: () => quantityHeader('react.receiving.receivingNow.label', 'Receiving Now'),
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          if (item?.rowType === ReceivingRowType.REPLACED
            || item?.rowType === ReceivingRowType.TOGGLE) {
            return null;
          }
          // A location without partial receiving receives every line of the shipment, so a line
          // left blank is a line received as zero - the completion writes that zero over it
          // (ReceiptV2Service#zeroOutEmptyReceivedQuantities), the confirmation on the receiving
          // step warns about it.
          const quantityReceiving = item?.quantityReceiving
            ?? (hasPartialReceivingSupport ? null : 0);
          const value = quantityReceiving === null ? null : formatNumber(quantityReceiving);
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
          const { cancelRemainingIds } = table.options.meta ?? {};
          // A location without partial receiving cancels whatever is left on every line of the
          // receipt (ReceiptV2Service#cancelRemainingQuantities), so those leftovers read as
          // canceled right away - there is no checkbox to flag them with.
          const isRemainingCanceled = hasPartialReceivingSupport
            ? Boolean(cancelRemainingIds?.has(item?.originalReceiptItemId))
            : (item?.quantityRemaining ?? 0) > 0;
          const { className, value } = getReceivingRowStatus({
            quantityRemaining: item?.quantityRemaining,
            isCompleted: item?.isCompleted || item?.quantityRemaining === 0,
            isRemainingCanceled,
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
      // The Location (putaway bin) column is shown even when "Enable Putaway" is off on the
      // receiving step, so the user sees which bin the shipment is received into.
      ...(hasBinLocationSupport ? [
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
      ...(hasPartialReceivingSupport ? [
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
            // Only the lines that still have a quantity left to cancel get a checkbox - the
            // predicate also covers the split item and toggle rows of a changes group.
            if (!isCancellableRow(item)) {
              return null;
            }
            const { originalReceiptItemId } = item;
            const { cancelRemainingIds, onToggleCancelRemaining } = table.options.meta ?? {};
            return (
              <TableCell className="rt-td d-flex justify-content-center align-items-center">
                <Checkbox
                  noWrapper
                  value={Boolean(cancelRemainingIds?.has(originalReceiptItemId))}
                  onChange={() => onToggleCancelRemaining?.(originalReceiptItemId)}
                />
              </TableCell>
            );
          },
          size: 110,
        }),
      ] : []),
    ];
  }, [
    translate,
    currentLocale,
    isPackingListView,
    hasPartialReceivingSupport,
    hasPreviousReceipts,
    isShipmentFromPurchaseOrder,
    showLotNumber,
    showExpirationDate,
    showRecipient,
    showPackLevel,
    sort,
    order,
  ]);

  return { columns };
};

export default useConfirmReceiptColumns;
