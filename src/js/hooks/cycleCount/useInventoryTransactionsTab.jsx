import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { useSelector } from 'react-redux';

import { CYCLE_COUNT_DETAILS_REPORT } from 'api/urls';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import ValueIndicator from 'components/DataTable/v2/ValueIndicator';
import { INVENTORY_ITEM_URL, INVENTORY_URL } from 'consts/applicationUrls';
import cycleCountColumn from 'consts/cycleCountColumn';
import { DateFormat } from 'consts/timeFormat';
import transactionType from 'consts/transactionType';
import valueIndicatorVariant, {
  getCycleCountDifferencesVariant,
} from 'consts/valueIndicatorVariant';
import useTableDataV2 from 'hooks/useTableDataV2';
import useTableSorting from 'hooks/useTableSorting';
import useTranslate from 'hooks/useTranslate';
import dateWithoutTimeZone from 'utils/dateUtils';

const useInventoryTransactionsTab = ({
  filterParams,
  paginationProps,
}) => {
  const columnHelper = createColumnHelper();
  const translate = useTranslate();

  const {
    currentLocale,
    currentLocation,
  } = useSelector((state) => ({
    currentLocale: state.session.activeLanguage,
    currentLocation: state.session.currentLocation,
  }));

  const {
    sortableProps,
    sort,
    order,
  } = useTableSorting();

  const getParams = () => ({
    facility: currentLocation?.id,
  });

  const {
    tableData,
    loading,
    fetchData,
  } = useTableDataV2({
    url: CYCLE_COUNT_DETAILS_REPORT,
    errorMessageId: 'react.cycleCount.table.errorMessage.label',
    defaultErrorMessage: 'Unable to fetch products',
    // We should start fetching only after clicking the button
    shouldFetch: false,
    disableInitialLoading: true,
    getParams,
    pageSize: `${paginationProps.pageSize}`,
    offset: `${paginationProps.offset}`,
    sort,
    order,
    searchTerm: null,
    filterParams,
  });

  const columns = useMemo(() => [columnHelper.accessor(cycleCountColumn.ALIGNMENT, {
    header: () => (
      <TableHeaderCell sortable columnId={cycleCountColumn.ALIGNMENT} {...sortableProps}>
        {translate('react.cycleCount.inventoryTransactionsTable.alignment.label', 'Alignment')}
      </TableHeaderCell>
    ),
    cell: ({
      row: {
        original: {
          verificationCount: {
            varianceTypeCode,
          },
        },
      },
    }) => (
      <div className="rt-td d-flex align-items-start">
        <ValueIndicator variant={valueIndicatorVariant[varianceTypeCode]} />
      </div>
    ),
    meta: {
      pinned: 'left',
    },
    size: 120,
  }), columnHelper.accessor(cycleCountColumn.PRODUCT, {
    header: () => (
      <TableHeaderCell sortable columnId={cycleCountColumn.PRODUCT} {...sortableProps}>
        {translate('react.cycleCount.inventoryTransactionsTable.product.label', 'Product')}
      </TableHeaderCell>
    ),
    cell: ({
      row: {
        original: {
          inventoryItem: {
            product: {
              id,
              name,
              productCode,
            },
          },
        },
      },
    }) => (
      <TableCell
        link={INVENTORY_ITEM_URL.showStockCard(id)}
        className="rt-td multiline-cell"
      >
        {productCode}
        {' '}
        {name}
      </TableCell>
    ),
    meta: {
      pinned: 'left',
    },
    size: 240,
  }), columnHelper.accessor(cycleCountColumn.TRANSACTION_TYPE, {
    header: () => (
      <TableHeaderCell sortable columnId={cycleCountColumn.TRANSACTION_TYPE} {...sortableProps}>
        {translate('react.cycleCount.inventoryTransactionsTable.transactionType.label', 'Transaction Type')}
      </TableHeaderCell>
    ),
    cell: ({ getValue }) => {
      const value = getValue()
        .toUpperCase()
        .replaceAll(' ', '_');
      return (
        <TableCell
          className="rt-td d-flex align-items-start"
        >
          <ValueIndicator
            variant={valueIndicatorVariant.TRANSACTION}
            value={transactionType[value]}
          />
        </TableCell>
      );
    },
    meta: {
      pinned: 'left',
    },
    size: 150,
  }), columnHelper.accessor(cycleCountColumn.RECORDED, {
    header: () => (
      <TableHeaderCell>
        {translate('react.cycleCount.inventoryTransactionsTable.recorded.label', 'Recorded')}
      </TableHeaderCell>
    ),
    cell: ({
      row: {
        original: {
          cycleCount: {
            dateRecorded,
            recordedBy: { name },
          },
        },
      },
    }) => (
      <div className="rt-td d-flex flex-column">
        <span className="font-weight-500">{name}</span>
        <span>
          {dateWithoutTimeZone({
            date: dateRecorded,
            outputDateFormat: DateFormat.MM_DD_YYYY,
          })}
        </span>
      </div>
    ),
    meta: {
      pinned: 'left',
    },
    size: 130,
  }), columnHelper.accessor(cycleCountColumn.TRANSACTION_ID, {
    header: () => (
      <TableHeaderCell sortable columnId={cycleCountColumn.TRANSACTION_ID} {...sortableProps}>
        {translate('react.cycleCount.inventoryTransactionsTable.transactionId.label', 'Transaction ID')}
      </TableHeaderCell>
    ),
    meta: {
      pinned: 'left',
    },
    size: 145,
    cell: ({
      getValue,
      row: { original: { cycleCount: { id } } },
    }) => (
      <TableCell
        link={INVENTORY_URL.showTransaction(id)}
        className="rt-td"
      >
        {getValue()?.toString()}
      </TableCell>
    ),
  }), columnHelper.accessor(cycleCountColumn.QTY_BEFORE, {
    header: () => (
      <TableHeaderCell sortable columnId={cycleCountColumn.QTY_BEFORE} {...sortableProps}>
        {translate('react.cycleCount.inventoryTransactionsTable.qtyBefore.label', 'Qty Before')}
      </TableHeaderCell>
    ),
    size: 130,
    cell: ({ getValue }) => (
      <TableCell
        className="rt-td d-flex justify-content-end"
      >
        {getValue()?.toString()}
      </TableCell>
    ),
  }), columnHelper.accessor(cycleCountColumn.QTY_AFTER, {
    header: () => (
      <TableHeaderCell sortable columnId={cycleCountColumn.QTY_AFTER} {...sortableProps}>
        {translate('react.cycleCount.inventoryTransactionsTable.qtyAfter.label', 'Qty After')}
      </TableHeaderCell>
    ),
    size: 120,
    cell: ({ getValue }) => (
      <TableCell
        className="rt-td d-flex justify-content-end"
      >
        {getValue()?.toString()}
      </TableCell>
    ),
  }), columnHelper.accessor(cycleCountColumn.DIFFERENCE, {
    header: () => (
      <TableHeaderCell sortable columnId={cycleCountColumn.DIFFERENCE} {...sortableProps}>
        {translate('react.cycleCount.inventoryTransactionsTable.difference.label', 'Difference')}
      </TableHeaderCell>
    ),
    cell: ({
      row: {
        original: {
          verificationCount: {
            quantityOnHand,
            quantityVariance,
            quantityCounted,
          },
        },
      },
    }) => {
      const variant = getCycleCountDifferencesVariant(quantityVariance, quantityOnHand);
      const percentageValue = Math.round(
        (Math.abs(quantityVariance) / (quantityCounted || quantityOnHand)) * 1000,
      ) / 10;
      const className = quantityVariance > 0 ? 'value-indicator--more' : 'value-indicator--less';

      return (
        <TableCell className="rt-td d-flex flex-column align-items-center">
          <ValueIndicator
            className={`pr-2 pl-1 py-1 value-indicator ${quantityVariance !== 0 && className}`}
            value={quantityVariance}
            variant={variant}
            showAbsoluteValue
          />
          {variant !== valueIndicatorVariant.EQUAL && (
          <p>
            (
              {percentageValue}
            {' '}
            %)
          </p>
          )}
        </TableCell>
      );
    },
    size: 120,
  }), columnHelper.accessor(cycleCountColumn.ROOT_CAUSES, {
    header: () => (
      <TableHeaderCell>
        {translate('react.cycleCount.inventoryTransactionsTable.rootCauses.label', 'Root Causes')}
      </TableHeaderCell>
    ),
    size: 200,
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
    ),
  }), columnHelper.accessor(cycleCountColumn.COMMENTS, {
    header: () => (
      <TableHeaderCell>
        {translate('react.cycleCount.inventoryTransactionsTable.comments.label', 'Comments')}
      </TableHeaderCell>
    ),
    size: 200,
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
    ),
  })], [currentLocale]);

  const emptyTableMessage = {
    id: 'react.cycleCount.inventoryTransactionTable.emptyTable.label',
    defaultMessage: 'Select a time range from above filters to load the table.',
  };

  const exportData = () => {
    console.log('Button pressed');
  };

  return {
    columns,
    tableData,
    loading,
    emptyTableMessage,
    fetchData,
    exportData,
  };
};

export default useInventoryTransactionsTab;
