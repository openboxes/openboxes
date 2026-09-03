import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { useSelector } from 'react-redux';
import {
  getCurrentLocale,
  getHasBinLocationSupport,
  getIsShipmentFromPurchaseOrder,
  getReceivingBin,
  getReceivingBinLocations,
} from 'selectors';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import LocationAutofillHeader from 'components/receivingV2/LocationAutofillHeader';
import receivingColumns from 'consts/receivingColumns';
import receivingLocationOptions from 'consts/receivingLocationOptions';
import ReceivingRowType from 'consts/receivingRowType';
import { ReceivingView } from 'consts/receivingViewOptions';
import useFormatNumber from 'hooks/useFormatNumber';
import useTranslate from 'hooks/useTranslate';
import ActionsCell from 'utils/cells/ActionsCell';
import AutosaveQuantityInputCell from 'utils/cells/AutosaveQuantityInputCell';
import ExpirationDateCell from 'utils/cells/ExpirationDateCell';
import MultilineCell from 'utils/cells/MultilineCell';
import PackLevelCell from 'utils/cells/PackLevelCell';
import PackLevelGroupCell from 'utils/cells/receiving/PackLevelGroupCell';
import ProductCodeCell from 'utils/cells/receiving/ProductCodeCell';
import ShippedQuantityCell from 'utils/cells/receiving/ShippedQuantityCell';
import SelectCell from 'utils/cells/SelectCell';
import ValueCell from 'utils/cells/ValueCell';
import getReceivingRowActions, { getReceivingSplitItemActions } from 'utils/receiving/getReceivingRowActions';
import getReceivingRowStatus from 'utils/receiving/getReceivingRowStatus';
import hasRowSavedQuantity from 'utils/receiving/hasRowSavedQuantity';
import hasSplitItemInDifferentBinThanReplacedRow from 'utils/receiving/hasSplitItemInDifferentBinThanReplacedRow';
import struckIfChanged from 'utils/receiving/struckIfChanged';
import sumSplitItemsQuantityReceiving from 'utils/receiving/sumSplitItemsQuantityReceiving';
import VerticalStripeIndicator from 'utils/VerticalStripeIndicator';

const useReceivingColumns = ({
  view,
  putawayEnabled,
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
  const isShipmentFromPurchaseOrder = useSelector(getIsShipmentFromPurchaseOrder);
  const binLocations = useSelector(getReceivingBinLocations);
  const receivingBin = useSelector(getReceivingBin);
  const hasBinLocationSupport = useSelector(getHasBinLocationSupport);
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

  // Remaining quantity kept live while editing: the quantity the row can still take
  // (quantityAvailableToReceive, fixed at load) minus the quantity entered in the input.
  const getCurrentQuantityRemaining = (item, entities) => {
    if (!item) {
      return null;
    }
    // A replaced row shows the status of the whole group, so it subtracts the quantities
    // of all its split items.
    if (item.rowType === ReceivingRowType.REPLACED) {
      return item.quantityAvailableToReceive - sumSplitItemsQuantityReceiving(item, entities);
    }
    return item.quantityAvailableToReceive - (Number(item.quantityReceiving) || 0);
  };

  // Shipment-level columns (quantities, status) don't apply to the rows of a changes group.
  const isSplitItemOrToggle = (item) => item?.rowType === ReceivingRowType.SPLIT_ITEM
    || item?.rowType === ReceivingRowType.TOGGLE;

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
      cell: ({ row, table }) => {
        const item = getItem(row, table);
        return (
          <>
            {/* The stripe marks rows whose quantity is saved. It lives in the first (pinned)
                column so its absolutely positioned span anchors to the row's left edge. */}
            <VerticalStripeIndicator display={hasRowSavedQuantity(item)} />
            <PackLevelGroupCell
              item={item}
              isExpanded={row.getIsExpanded()}
              onToggle={row.getToggleExpandedHandler()}
            />
          </>
        );
      },
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
            <span className={`receiving-table__separator-label ${putawayEnabled ? 'py-0' : ''}`}>
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
            <>
              {/* In packing list view the saved stripe is rendered by the pack level group
                  column, which is the leftmost one there. */}
              {!isPackingListView
                && <VerticalStripeIndicator display={hasRowSavedQuantity(item)} />}
              <ProductCodeCell
                item={item}
                isPackingListView={isPackingListView}
                isExpanded={row.getIsExpanded()}
                onToggle={row.getToggleExpandedHandler()}
                className={struckIfChanged(item?.rowType, item?.productChanged)}
              />
            </>
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
              className={struckIfChanged(item?.rowType, item?.productChanged)}
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
                className={struckIfChanged(item?.rowType, item?.lotChanged)}
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
                className={struckIfChanged(item?.rowType, item?.expirationChanged)}
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
                className={struckIfChanged(item?.rowType, item?.recipientChanged)}
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
        header: () => (
          <TableHeaderCell
            {...sortHeaderProps(receivingColumns.QUANTITY_SHIPPED)}
            tooltip
            tooltipLabel={translate('react.receiving.shipped.label', 'Shipped')}
            className="receiving-table__quantity"
          >
            {translate('react.receiving.shipped.label', 'Shipped')}
          </TableHeaderCell>
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
      columnHelper.display({
        id: receivingColumns.QUANTITY_RECEIVING,
        header: () => (
          <TableHeaderCell
            tooltip
            tooltipLabel={translate('react.receiving.receivingNow.label', 'Receiving now')}
            className="receiving-table__quantity"
          >
            {translate('react.receiving.receivingNow.label', 'Receiving Now')}
          </TableHeaderCell>
        ),
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          if (item?.rowType === ReceivingRowType.TOGGLE) {
            return null;
          }
          // The replaced row shows the sum of its split items' receiving-now quantities
          // as a read-only value (no input) so it stays out of the editing flow.
          if (item?.rowType === ReceivingRowType.REPLACED) {
            const sum = sumSplitItemsQuantityReceiving(item, table.options.meta?.entities);
            const value = formatNumber(sum);
            return (
              <ValueCell
                value={value}
                tooltipLabel={value}
                className="receiving-table__quantity"
                label="react.receiving.receivingNow.label"
                defaultLabel="Receiving Now"
              />
            );
          }
          return (
            <AutosaveQuantityInputCell
              value={item?.quantityReceiving}
              onCommit={(quantityReceiving) =>
                table.options.meta?.updateLineItem(row.original.id, { quantityReceiving })}
              disabled={item?.isCompleted}
              className="receiving-table__quantity"
              label="react.receiving.receivingNow.label"
              defaultLabel="Receiving Now"
            />
          );
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
          const quantityRemaining = getCurrentQuantityRemaining(item, table.options.meta?.entities);
          const { className, value } = getReceivingRowStatus({
            quantityRemaining,
            isCompleted: item?.isCompleted,
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
            />
          );
        },
        size: 125,
      }),
      // The Location (putaway bin) column is only shown when "Enable Putaway" is on
      // and a bin tracking location.
      ...(putawayEnabled && hasBinLocationSupport ? [
        columnHelper.display({
          id: receivingColumns.LOCATION,
          header: ({ table }) => (
            <LocationAutofillHeader onSelect={table.options.meta?.onLocationAutofill} />
          ),
          cell: ({ row, table }) => {
            const item = getItem(row, table);
            if (item?.rowType === ReceivingRowType.TOGGLE) {
              return null;
            }
            // The parent shows the receiving bin, struck when a split item's bin differs from it.
            if (item?.rowType === ReceivingRowType.REPLACED) {
              const isBinLocationChanged = hasSplitItemInDifferentBinThanReplacedRow(
                item,
                table.options.meta?.entities,
                receivingBin,
              );
              const struckBin = struckIfChanged(item?.rowType, isBinLocationChanged);
              return (
                <ValueCell
                  value={receivingBin?.name}
                  tooltipLabel={receivingBin?.name}
                  className={`receiving-table__parent-location ${struckBin}`}
                  label="react.receiving.location.label"
                  defaultLabel="Location"
                  truncate
                />
              );
            }
            return (
              <SelectCell
                options={binLocations}
                value={item?.binLocation}
                onChange={(binLocation) =>
                  table.options.meta?.updateLineItem(row.original.id, { binLocation })}
                disabled={item?.isCompleted}
                label="react.receiving.location.label"
                defaultLabel="Location"
              />
            );
          },
          // Separator rows also get a select, used to autofill the location for the whole group.
          meta: {
            renderSeparator: ({ row, table }) => (
              <SelectCell
                options={receivingLocationOptions(translate)}
                onChange={(option) =>
                  option && table.options.meta?.onLocationAutofill(option.id, row.original.id)}
                label="react.receiving.location.label"
                defaultLabel="Location"
              />
            ),
          },
          size: 170,
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
          if (item?.rowType === ReceivingRowType.TOGGLE) {
            return null;
          }
          // A split item row offers its own actions (removing the single change);
          // all other rows carry the standard row actions.
          const actions = item?.rowType === ReceivingRowType.SPLIT_ITEM
            ? getReceivingSplitItemActions({
              rowId: row.original.id,
              onRemove: table.options.meta?.removeSplitItem,
              // The original line rendered among the split rows cannot be removed (it backs
              // the cancel-remaining flow on completion), so its delete action is only shown
              // disabled - the line can be zeroed by removing it in the edit modal.
              isOriginalLine: !item?.isSplitItem,
            })
            : getReceivingRowActions({
              itemId: row.original.id,
              // Only rows backed by a receipt item can carry a comment. The replaced row of a
              // split group is backed by the original line of its shipment item.
              canComment: Boolean(item?.receiptItemId),
              hasComment: Boolean(item?.comment),
              onOpenCommentModal: table.options.meta?.onOpenCommentModal,
              onOpenEditModal: table.options.meta?.onOpenEditModal,
            });
          return (
            <ActionsCell
              actions={actions}
              // isDeleteInProgress disables the delete button of a split item while its request
              // is in flight, so fast repeated clicks cannot fire multiple deletes.
              disabled={item?.isCompleted || item?.isDeleteInProgress}
              label="react.receiving.actions.label"
              defaultLabel="Actions"
            />
          );
        },
        size: 90,
      }),
    ];
  }, [
    translate,
    currentLocale,
    isPackingListView,
    putawayEnabled,
    hasBinLocationSupport,
    isShipmentFromPurchaseOrder,
    binLocations,
    receivingBin,
    showLotNumber,
    showExpirationDate,
    showRecipient,
    showPackLevel,
    sort,
    order,
  ]);

  return { columns };
};

export default useReceivingColumns;
