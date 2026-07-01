import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { useSelector } from 'react-redux';
import { getCurrentLocale, getIsShipmentFromPurchaseOrder } from 'selectors';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import LocationAutofillHeader from 'components/receivingV2/LocationAutofillHeader';
import receivingColumns from 'consts/receivingColumns';
import receivingLocationOptions from 'consts/receivingLocationOptions';
import { ReceivingView } from 'consts/receivingViewOptions';
import useFormatNumber from 'hooks/useFormatNumber';
import useTranslate from 'hooks/useTranslate';
import ActionsCell from 'utils/cells/ActionsCell';
import ExpirationDateCell from 'utils/cells/ExpirationDateCell';
import MultilineCell from 'utils/cells/MultilineCell';
import PackLevelCell from 'utils/cells/PackLevelCell';
import QuantityInputCell from 'utils/cells/QuantityInputCell';
import ShippedInPoCell from 'utils/cells/receiving/ShippedInPoCell';
import SelectCell from 'utils/cells/SelectCell';
import ValueCell from 'utils/cells/ValueCell';
import getReceivingRowActions from 'utils/receiving/getReceivingRowActions';

const useReceivingColumns = ({
  view,
  putawayEnabled,
} = {}) => {
  const translate = useTranslate();
  const formatNumber = useFormatNumber();
  const columnHelper = createColumnHelper();
  const currentLocale = useSelector(getCurrentLocale);
  const isShipmentFromPurchaseOrder = useSelector(getIsShipmentFromPurchaseOrder);
  const isPackingListView = view === ReceivingView.PACKING_LIST;

  // Rows are { id, meta } objects; the entities live in the normalized state
  // passed through the table `meta`, so each cell reads its item by id at render
  // time. The row `meta` drives row-level greying/disabling of fully received lines.
  const getItem = (row, table) => table.options.meta?.entities?.[row.original.id];

  const getStatus = (quantityRemaining, isCompleted) => {
    if (isCompleted) {
      return {
        className: 'status-cell status-cell--completed',
        value: translate('react.receiving.status.completed.label', 'Complete'),
      };
    }
    if (quantityRemaining < 0) {
      const quantityOver = formatNumber(Math.abs(quantityRemaining));
      return {
        className: 'status-cell status-cell--over',
        value: translate('react.receiving.status.over.label', `${quantityOver} over`, [quantityOver]),
      };
    }
    if (quantityRemaining === 0) {
      return {
        className: 'status-cell status-cell--equal',
        value: translate('react.receiving.status.equal.label', 'Equal'),
      };
    }
    // TODO (OBPIH-7864): show the remaining status only once something has been
    // entered in the input or already saved for the row.
    const quantityRemainingFormatted = formatNumber(quantityRemaining);
    return {
      className: 'status-cell',
      value: translate('react.receiving.status.remaining.label', `${quantityRemainingFormatted} remaining`, [quantityRemainingFormatted]),
    };
  };

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
        const value = getItem(row, table)?.packLevelGroup;
        return (
          <ValueCell
            value={value}
            tooltipLabel={value}
            label="react.receiving.packLevel.label"
            defaultLabel="Pack Level"
            truncate
          />
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
              label="react.receiving.code.label"
              defaultLabel="Code"
              truncate
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
            tooltip
            tooltipLabel={translate('react.receiving.product.label', 'Product')}
          >
            {translate('react.receiving.product.label', 'Product')}
          </TableHeaderCell>
        ),
        cell: ({ row, table }) => (
          <MultilineCell
            value={getItem(row, table)?.product?.name}
            label="react.receiving.product.label"
            defaultLabel="Product"
            maxLines={2}
          />
        ),
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
          const value = getItem(row, table)?.lotNumber;
          return (
            <ValueCell
              value={value}
              tooltipLabel={value}
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
        cell: ({ row, table }) => (
          <ExpirationDateCell
            value={getItem(row, table)?.expirationDate}
            localeKey={currentLocale}
            label="react.receiving.expirationDate.short.label"
            defaultLabel="Exp Date"
            showExpiryStatus
          />
        ),
        size: 110,
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
              label="react.receiving.recipient.label"
              defaultLabel="Recipient"
              truncate
            />
          );
        },
        size: 125,
      }),
      // When receiving against a purchase order, an extra column shows the shipped
      // quantity in the PO's unit of measure (packs) before the per-each quantity.
      ...(isShipmentFromPurchaseOrder ? [
        columnHelper.display({
          id: receivingColumns.QUANTITY_SHIPPED_IN_PO,
          header: () => (
            <TableHeaderCell
              tooltip
              tooltipLabel={translate('react.receiving.shippedInPo.label', 'Shipped (in PO UoM)')}
            >
              {translate('react.receiving.shippedInPo.label', 'Shipped (in PO UoM)')}
            </TableHeaderCell>
          ),
          cell: ({ row, table }) => {
            const { quantityShipped, packSize, unitOfMeasure } = getItem(row, table) || {};
            const packs = packSize
              ? Math.round((quantityShipped / packSize) * 100) / 100
              : quantityShipped;
            return (
              <ShippedInPoCell
                packs={packs}
                unitOfMeasure={unitOfMeasure}
                label="react.receiving.shippedInPo.label"
                defaultLabel="Shipped (in PO UoM)"
              />
            );
          },
          size: 125,
        }),
      ] : []),
      columnHelper.display({
        id: receivingColumns.QUANTITY_SHIPPED,
        header: () => {
          const labelKey = isShipmentFromPurchaseOrder
            ? 'react.receiving.shippedEach.label'
            : 'react.receiving.shipped.label';
          const defaultLabel = isShipmentFromPurchaseOrder ? 'Shipped (each)' : 'Shipped';
          return (
            <TableHeaderCell tooltip tooltipLabel={translate(labelKey, defaultLabel)}>
              {translate(labelKey, defaultLabel)}
            </TableHeaderCell>
          );
        },
        cell: ({ row, table }) => {
          const value = formatNumber(getItem(row, table)?.quantityShipped);
          return (
            <ValueCell
              value={value}
              tooltipLabel={value}
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
          >
            {translate('react.receiving.receivingNow.label', 'Receiving Now')}
          </TableHeaderCell>
        ),
        cell: ({ row, table }) => {
          const item = getItem(row, table);
          return (
            <QuantityInputCell
              value={item?.quantityReceiving}
              onCommit={(quantityReceiving) =>
                table.options.meta?.updateLineItem(row.original.id, { quantityReceiving })}
              disabled={item?.isCompleted}
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
          const { className, value } = getStatus(item?.quantityRemaining, item?.isCompleted);
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
          header: () => <LocationAutofillHeader />,
          cell: ({ row, table }) => (
            <SelectCell
              options={receivingLocationOptions}
              disabled={getItem(row, table)?.isCompleted}
              label="react.receiving.location.label"
              defaultLabel="Location"
            />
          ),
          // Separator rows also get a select, used to autofill the location for the whole group.
          meta: {
            renderSeparator: () => (
              <SelectCell
                options={receivingLocationOptions}
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
        cell: ({ row, table }) => (
          <ActionsCell
            actions={getReceivingRowActions({
              itemId: row.original.id,
              onOpenCommentModal: table.options.meta?.onOpenCommentModal,
            })}
            disabled={getItem(row, table)?.isCompleted}
            label="react.receiving.actions.label"
            defaultLabel="Actions"
          />
        ),
        size: 90,
      }),
    ];
  }, [translate, currentLocale, isPackingListView, putawayEnabled, isShipmentFromPurchaseOrder]);

  return { columns };
};

export default useReceivingColumns;
