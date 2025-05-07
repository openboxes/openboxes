/* eslint-disable */
import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { useSelector } from 'react-redux';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import ValueIndicator from 'components/DataTable/v2/ValueIndicator';
import { INVENTORY_ITEM_URL, INVENTORY_URL } from 'consts/applicationUrls';
import cycleCountColumn from 'consts/cycleCountColumn';
import inventoryTransactionsData from 'consts/inventoryTransactionsData.json';
import { DateFormat } from 'consts/timeFormat';
import transactionType from 'consts/transactionType';
import valueIndicatorVariant, {
  getCycleCountDifferencesVariant
} from 'consts/valueIndicatorVariant';
import useTableSorting from 'hooks/useTableSorting';
import useTranslate from 'hooks/useTranslate';
import dateWithoutTimeZone from 'utils/dateUtils';

const useInventoryTransactionsTab = () => {
  const columnHelper = createColumnHelper();
  const translate = useTranslate();

  const {
    currentLocale,
  } = useSelector((state) => ({
    currentLocale: state.session.activeLanguage,
  }));

  const {
    sortableProps,
    sort,
    order,
  } = useTableSorting();

  const columns = useMemo(() => [
    columnHelper.accessor(cycleCountColumn.ALIGNMENT, {
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.ALIGNMENT} {...sortableProps}>
          {translate('react.cycleCount.inventoryTransactionsTable.alignment.label', 'Alignment')}
        </TableHeaderCell>
      ),
      cell: ({
        row: {
          original: {
            quantity_on_hand_after,
            quantity_on_hand_before
          }
        }
      }) => {
        const quantityVariance = quantity_on_hand_before - quantity_on_hand_after;
        if (quantityVariance === 0) {
          return (
            <div className="rt-td d-flex align-items-start">
              <ValueIndicator variant={valueIndicatorVariant.EQUAL}/>
            </div>
          );
        }

        const variant = quantityVariance < 0
          ? valueIndicatorVariant.LESS
          : valueIndicatorVariant.MORE;
        return (
          <div className="rt-td d-flex align-items-start">
            <ValueIndicator variant={variant}/>
          </div>
        );
      },
      meta: {
        width: 145,
        fixed: true,
      },
    }),
    columnHelper.accessor(cycleCountColumn.PRODUCT, {
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.PRODUCT} {...sortableProps}>
          {translate('react.cycleCount.inventoryTransactionsTable.product.label', 'Product')}
        </TableHeaderCell>
      ),
      cell: ({
        row: {
          original: {
            product_code,
            product_name,
            product_id,
          }
        }
      }) => {
        return (
          <TableCell
            link={INVENTORY_ITEM_URL.showStockCard(product_id)}
            className="rt-td multiline-cell"
          >
            {product_code} {product_name}
          </TableCell>
        );
      },
      meta: {
        width: 280,
        fixed: true,
      },
    }),
    columnHelper.accessor(cycleCountColumn.TRANSACTION_TYPE, {
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.TRANSACTION_TYPE} {...sortableProps}>
          {translate('react.cycleCount.inventoryTransactionsTable.transactionType.label', 'Transaction Type')}
        </TableHeaderCell>
      ),
      cell: () => (
        <TableCell
          className="rt-td d-flex align-items-start"
        >
          <ValueIndicator
            variant={valueIndicatorVariant.TRANSACTION}
            value={transactionType.CYCLE_COUNT}
          />
        </TableCell>
      ),
      meta: {
        width: 145,
        fixed: true,
      },
    }),
    columnHelper.accessor(cycleCountColumn.RECORDED, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.inventoryTransactionsTable.recorded.label', 'Recorded')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <div className="rt-td d-flex flex-column">
          <span className="font-weight-500">Devon Lane</span>
          <span>
            {dateWithoutTimeZone({
              date: getValue(),
              outputDateFormat: DateFormat.MM_DD_YYYY,
            })}
          </span>
        </div>
      ),
      meta: {
        width: 145,
        fixed: true,
      },
    }),
    columnHelper.accessor(cycleCountColumn.TRANSACTION_ID, {
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.TRANSACTION_ID} {...sortableProps}>
          {translate('react.cycleCount.inventoryTransactionsTable.transactionId.label', 'Transaction ID')}
        </TableHeaderCell>
      ),
      meta: {
        width: 145,
        getCellContext: () => ({
          className: 'split-table-right',
        }),
        fixed: true,
      },
      cell: ({ getValue }) => (
        <TableCell
          link={INVENTORY_URL.showTransaction('transaction-id')}
          className="rt-td"
        >
          {_.take(getValue(), 9)
            .join('')
            .match(/.{1,3}/g)
            .join('-')
            .toUpperCase()}
        </TableCell>
      )
    }),
    columnHelper.accessor(cycleCountColumn.QTY_BEFORE, {
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.QTY_BEFORE} {...sortableProps}>
          {translate('react.cycleCount.inventoryTransactionsTable.qtyBefore.label', 'Qty Before')}
        </TableHeaderCell>
      ),
      meta: {
        width: 145,
      },
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td d-flex justify-content-end"
        >
          {getValue()
            .toString()}
        </TableCell>
      )
    }),
    columnHelper.accessor(cycleCountColumn.QTY_AFTER, {
        header: () => (
          <TableHeaderCell sortable columnId={cycleCountColumn.QTY_AFTER} {...sortableProps}>
            {translate('react.cycleCount.inventoryTransactionsTable.qtyAfter.label', 'Qty After')}
          </TableHeaderCell>
        ),
        meta: {
          width: 145,
        },
        cell: ({ getValue }) => (
          <TableCell
            className="rt-td d-flex justify-content-end"
          >
            {getValue()
              ?.toString()}
          </TableCell>
        )
      }
    ),
    columnHelper.accessor(cycleCountColumn.DIFFERENCE, {
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.DIFFERENCE} {...sortableProps}>
          {translate('react.cycleCount.inventoryTransactionsTable.difference.label', 'Difference')}
        </TableHeaderCell>
      ),
      cell: ({
        row: {
          original: {
            quantity_on_hand_after,
            quantity_on_hand_before
          }
        }
      }) => {
        const difference = quantity_on_hand_after - quantity_on_hand_before;
        const variant = getCycleCountDifferencesVariant(difference, quantity_on_hand_before);
        const percentageValue = Math.round(
          (Math.abs(difference) / (quantity_on_hand_after || quantity_on_hand_before)) * 1000
        ) / 10;
        const className = difference > 0 ? 'value-indicator--more' : 'value-indicator--less';

        return (
          <TableCell className="rt-td d-flex flex-column align-items-center">
            <ValueIndicator
              className={`pr-2 pl-1 py-1 value-indicator ${difference !== 0 && className}`}
              value={difference}
              variant={variant}
              showAbsoluteValue
            />
            {variant !== valueIndicatorVariant.EQUAL && <p>({percentageValue} %)</p>}
          </TableCell>
        );
      },
      meta: {
        width: 175,
      },
    }),
    columnHelper.accessor(cycleCountColumn.ROOT_CAUSES, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.inventoryTransactionsTable.rootCauses.label', 'Root Causes')}
        </TableHeaderCell>
      ),
      meta: {
        width: 260,
      },
      cell: ({ getValue }) => (
        <TableCell
          customTooltip
          tooltipLabel={getValue()}
          className="rt-td multiline-cell"
        >
          <div className="limit-lines-1">
            {_.capitalize(getValue())}
          </div>
        </TableCell>
      )
    }),
    columnHelper.accessor(cycleCountColumn.COMMENTS, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.inventoryTransactionsTable.comments.label', 'Comments')}
        </TableHeaderCell>
      ),
      meta: {
        width: 280,
      },
      cell: ({ getValue }) => (
        <TableCell
          customTooltip
          tooltipLabel={getValue()}
          className="rt-td multiline-cell"
        >
          <div className="limit-lines-2">
            {getValue()}
          </div>
        </TableCell>
      )
    }),
  ], [currentLocale, sort, order]);

  const emptyTableMessage = {
    id: 'react.cycleCount.table.emptyTable.label',
    defaultMessage: 'No products match the given criteria',
  };

  const exportData = () => {
    console.log('Button pressed');
  };

  return {
    columns,
    // Those two properties should be returned from hook making API calls
    tableData: {
      totalCount: inventoryTransactionsData.length,
      data: _.take(inventoryTransactionsData, 5)
    },
    loading: false,
    emptyTableMessage,
    exportData,
  };
};

export default useInventoryTransactionsTab;
