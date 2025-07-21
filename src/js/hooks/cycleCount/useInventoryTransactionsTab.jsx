import React, { useEffect, useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { useSelector } from 'react-redux';

import { INVENTORY_TRANSACTIONS_SUMMARY } from 'api/urls';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import ValueIndicator from 'components/DataTable/v2/ValueIndicator';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import inventoryTransactionReportColumn from 'consts/inventoryTransactionReportColumn';
import reasonCodes from 'consts/reasonCodes';
import { DateFormat } from 'consts/timeFormat';
import valueIndicatorVariant, {
  getCycleCountDifferencesVariant,
} from 'consts/valueIndicatorVariant';
import useTableDataV2 from 'hooks/useTableDataV2';
import useTableSorting from 'hooks/useTableSorting';
import useTranslate from 'hooks/useTranslate';
import dateWithoutTimeZone from 'utils/dateUtils';

const useInventoryTransactionsTab = ({
  filterParams,
  offset,
  pageSize,
  shouldFetch,
  setShouldFetch,
  serializedParams,
  filtersInitialized,
  defaultFilterValues,
}) => {
  const columnHelper = createColumnHelper();
  const translate = useTranslate();
  const { products, endDate, startDate } = filterParams;
  const {
    currentLocale,
    currentLocation,
    defaultTranslationsFetched,
  } = useSelector((state) => ({
    currentLocale: state.session.activeLanguage,
    currentLocation: state.session.currentLocation,
    defaultTranslationsFetched: state.session.fetchedTranslations.default,
  }));
  const {
    sortableProps,
    sort,
    order,
  } = useTableSorting();

  const getParams = ({
    sortingParams,
  }) => _.omitBy({
    offset: `${offset}`,
    max: `${pageSize}`,
    ...sortingParams,
    ...filterParams,
    endDate: dateWithoutTimeZone({
      date: endDate || defaultFilterValues.endDate,
    }),
    startDate: dateWithoutTimeZone({
      date: startDate || defaultFilterValues.startDate,
    }),
    products: (products || defaultFilterValues.products)?.map?.(({ id }) => id),
    facility: currentLocation?.id,
  }, (val) => {
    if (typeof val === 'boolean') {
      return !val;
    }
    return _.isEmpty(val);
  });

  const {
    tableData,
    loading,
    setTableData,
  } = useTableDataV2({
    url: INVENTORY_TRANSACTIONS_SUMMARY,
    errorMessageId: 'react.cycleCount.table.errorMessage.label',
    defaultErrorMessage: 'Unable to fetch products',
    // We should start fetching only after clicking the button
    // or after refreshing the page with filters selected
    shouldFetch: shouldFetch
      && (endDate || defaultFilterValues.endDate)
      && (startDate || defaultFilterValues.startDate),
    setShouldFetch,
    disableInitialLoading: true,
    getParams,
    pageSize,
    offset,
    sort,
    order,
    searchTerm: null,
    filterParams,
    serializedParams,
    filtersInitialized,
  });

  useEffect(() => {
    setTableData({ data: [], totalCount: 0 });
  }, [currentLocation?.id]);

  const calculatePercentage = (quantityOnHand, quantityCounted, quantityVariance) => {
    // If quantityOnHand is less than or equal to 0 and quantityCounted is 0,
    // set percentageValue to 0
    if (quantityOnHand <= 0 && quantityCounted === 0) {
      return 0;
    }

    // If quantityOnHand is less than or equal to 0 and quantityCounted is not 0,
    // set percentageValue to 100
    if (quantityOnHand <= 0 && quantityCounted !== 0) {
      return 100;
    }

    // Calculate percentage value only if quantityOnHand is greater than 0
    if (quantityOnHand > 0) {
      return Math.round((Math.abs(quantityVariance) / Math.abs(quantityOnHand)) * 100);
    }

    return 100;
  };

  const columns = useMemo(() => [columnHelper.accessor(inventoryTransactionReportColumn.ALIGNMENT, {
    header: () => (
      <TableHeaderCell
        sortable
        columnId={inventoryTransactionReportColumn.ALIGNMENT}
        {...sortableProps}
      >
        {translate('react.cycleCount.table.alignment.label', 'Alignment')}
      </TableHeaderCell>
    ),
    cell: ({
      row: {
        original: {
          varianceTypeCode,
        },
      },
    }) => (
      <div className="rt-td pb-0 d-flex align-items-start">
        <ValueIndicator variant={valueIndicatorVariant[varianceTypeCode]} />
      </div>
    ),
    meta: {
      pinned: 'left',
    },
    size: 100,
  }), columnHelper.accessor(inventoryTransactionReportColumn.PRODUCT, {
    header: () => (
      <TableHeaderCell
        sortable
        columnId={inventoryTransactionReportColumn.PRODUCT}
        {...sortableProps}
      >
        {translate('react.cycleCount.table.product.label', 'Product')}
      </TableHeaderCell>
    ),
    cell: ({
      row: {
        original: {
          product: {
            id,
            name,
            productCode,
          },
        },
      },
    }) => (
      <TableCell
        link={INVENTORY_ITEM_URL.showStockCard(id)}
        className="rt-td pb-0 multiline-cell"
        customTooltip
        tooltipLabel={`${productCode} ${name}`}
      >
        <div className="limit-lines-2">
          {productCode}
          {' '}
          {name}
        </div>
      </TableCell>
    ),
    meta: {
      pinned: 'left',
    },
    size: 360,
  }), columnHelper.accessor(inventoryTransactionReportColumn.TRANSACTION_TYPE, {
    header: () => (
      <TableHeaderCell
        sortable
        columnId={inventoryTransactionReportColumn.TRANSACTION_TYPE}
        {...sortableProps}
      >
        {translate('react.cycleCount.table.type.label', 'Type')}
      </TableHeaderCell>
    ),
    cell: ({ getValue }) => {
      const value = getValue()?.toUpperCase()?.replaceAll(' ', '_');
      const translatedValue = translate(`react.default.enum.TransactionAction.${value}`, value);
      return (
        <TableCell
          className="rt-td pb-0 d-flex align-items-start"
        >
          <ValueIndicator
            variant={valueIndicatorVariant.TRANSACTION}
            value={translatedValue}
          />
        </TableCell>
      );
    },
    meta: {
      pinned: 'left',
    },
    size: 75,
  }), columnHelper.accessor(inventoryTransactionReportColumn.RECORDED, {
    header: () => (
      <TableHeaderCell>
        {translate('react.cycleCount.table.recorded.label', 'Recorded')}
      </TableHeaderCell>
    ),
    cell: ({
      row: {
        original: {
          dateRecorded,
          recordedBy: { name },
        },
      },
    }) => (
      <div className="rt-td pb-0 d-flex flex-column">
        <span className="font-weight-500">{name}</span>
        <span>
          {dateWithoutTimeZone({
            date: dateRecorded,
            outputDateFormat: DateFormat.DD_MMM_YYYY,
          })}
        </span>
      </div>
    ),
    meta: {
      pinned: 'left',
    },
    size: 130,
  }), columnHelper.accessor(inventoryTransactionReportColumn.TRANSACTION_ID, {
    header: () => (
      <TableHeaderCell
        sortable
        columnId={inventoryTransactionReportColumn.TRANSACTION_ID}
        {...sortableProps}
      >
        {translate('react.cycleCount.table.transactionId.label', 'Transaction ID')}
      </TableHeaderCell>
    ),
    meta: {
      pinned: 'left',
    },
    size: 145,
    cell: ({ getValue }) => (
      <TableCell
        className="rt-td pb-0"
      >
        {getValue()?.toString()}
      </TableCell>
    ),
  }), columnHelper.accessor(inventoryTransactionReportColumn.QTY_BEFORE, {
    header: () => (
      <TableHeaderCell
        sortable
        columnId={inventoryTransactionReportColumn.QTY_BEFORE}
        {...sortableProps}
      >
        {translate('react.cycleCount.table.qtyBefore.label', 'Qty Before')}
      </TableHeaderCell>
    ),
    size: 100,
    cell: ({ getValue }) => (
      <TableCell className="rt-td pb-0 d-flex justify-content-end">
        {(getValue() ?? 0).toString()}
      </TableCell>
    ),
  }), columnHelper.accessor(inventoryTransactionReportColumn.QTY_AFTER, {
    header: () => (
      <TableHeaderCell
        sortable
        columnId={inventoryTransactionReportColumn.QTY_AFTER}
        {...sortableProps}
      >
        {translate('react.cycleCount.table.qtyAfter.label', 'Qty After')}
      </TableHeaderCell>
    ),
    size: 100,
    cell: ({ getValue }) => (
      <TableCell
        className="rt-td pb-0 d-flex justify-content-end"
      >
        {getValue()?.toString()}
      </TableCell>
    ),
  }), columnHelper.accessor(inventoryTransactionReportColumn.QTY_DIFFERENCE, {
    header: () => (
      <TableHeaderCell
        sortable
        columnId={inventoryTransactionReportColumn.QTY_DIFFERENCE}
        {...sortableProps}
      >
        {translate('react.cycleCount.table.difference.label', 'Difference')}
      </TableHeaderCell>
    ),
    cell: ({
      row: {
        original: {
          quantityBefore,
          quantityAfter,
          quantityDifference,
        },
      },
    }) => {
      const variant = getCycleCountDifferencesVariant({ firstValue: quantityDifference });
      const percentageValue =
        calculatePercentage(quantityBefore, quantityAfter, quantityDifference);
      const className = quantityDifference > 0 ? 'value-indicator--more' : 'value-indicator--less';

      return (
        <TableCell className="rt-td pb-0 d-flex flex-column align-items-center">
          <ValueIndicator
            className={`pr-2 pl-1 py-1 value-indicator ${quantityDifference !== 0 && className}`}
            value={quantityDifference}
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
  }), columnHelper.accessor(inventoryTransactionReportColumn.ROOT_CAUSES, {
    header: () => (
      <TableHeaderCell>
        {translate('react.cycleCount.table.rootCauses.label', 'Root Causes')}
      </TableHeaderCell>
    ),
    size: 200,
    cell: ({ getValue }) => {
      const rootCauses = getValue() ? getValue().map((cause) => reasonCodes[cause]).join(', ') : '';
      return (
        <TableCell
          customTooltip
          tooltipLabel={rootCauses}
          className="rt-td pb-0 multiline-cell"
        >
          <div className="limit-lines-2">
            {rootCauses}
          </div>
        </TableCell>
      );
    },
  }), columnHelper.accessor(inventoryTransactionReportColumn.COMMENTS, {
    header: () => (
      <TableHeaderCell>
        {translate('react.cycleCount.table.comments.label', 'Comments')}
      </TableHeaderCell>
    ),
    size: 200,
    cell: ({ getValue }) => {
      const comments = getValue() ? getValue().join(', ') : '';
      return (
        <TableCell
          customTooltip
          tooltipLabel={comments}
          className="rt-td pb-0 multiline-cell"
        >
          <div className="limit-lines-2">
            {comments}
          </div>
        </TableCell>
      );
    },
  })], [currentLocale, defaultTranslationsFetched]);

  const emptyTableMessage = !filterParams.startDate && !filterParams.endDate
    ? {
      id: 'react.cycleCount.reporting.emptyTable.label',
      defaultMessage: 'Select a time range from above filters to load the table.',
    }
    : {
      id: 'react.cycleCount.table.noResultFound.label',
      defaultMessage: 'No result found.',
    };
  const exportData = () => {
    console.log('Button pressed');
  };

  return {
    columns,
    tableData,
    loading,
    emptyTableMessage,
    exportData,
  };
};

export default useInventoryTransactionsTab;
