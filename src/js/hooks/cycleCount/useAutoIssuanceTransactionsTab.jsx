import React, { useEffect, useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocationId, getFormatLocalizedDate } from 'selectors';

import { AUTO_ISSUANCE_TRANSACTIONS_REPORT } from 'api/urls';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import { INVENTORY_ITEM_URL, INVENTORY_URL, STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import autoIssuanceTransactionReportColumn from 'consts/autoIssuanceTransactionReportColumn';
import { DateFormat } from 'consts/timeFormat';
import useTableDataV2 from 'hooks/useTableDataV2';
import useTableSorting from 'hooks/useTableSorting';
import useTranslate from 'hooks/useTranslate';
import dateWithoutTimeZone from 'utils/dateUtils';

// Rows are outbound (TRANSFER_OUT) transaction entries for auto-issued requisitions, grouped by
// product + bin + transaction on the backend. A negative Quantity On Hand is highlighted in red.
const useAutoIssuanceTransactionsTab = ({
  filterParams,
  offset,
  pageSize,
  shouldFetch,
  setShouldFetch,
  serializedParams,
  filtersInitialized,
}) => {
  const columnHelper = createColumnHelper();
  const translate = useTranslate();
  const {
    products, binLocations, endDate, startDate,
  } = filterParams;

  const currentLocationId = useSelector(getCurrentLocationId);
  const formatLocalizedDate = useSelector(getFormatLocalizedDate);
  const { sortableProps, sort, order } = useTableSorting();

  // Only Product and Bin Location are sortable - the backend whitelists just these two column ids
  // (see resolveOrderByClause in AutoIssuanceTransactionReportService), so don't add `sortable` to
  // other columns without adding a matching case there too.
  //
  // shouldFetch is reset to false after each fetch (see useTableDataV2), so a sort toggle needs to
  // flip it back to true itself, the same way page/pageSize changes do in useTablePagination.
  const sortableColumnProps = {
    ...sortableProps,
    toggleSort: (columnId) => () => {
      sortableProps.toggleSort(columnId)();
      setShouldFetch(true);
    },
  };

  const getParams = ({
    sortingParams,
  }) => _.omitBy({
    offset: `${offset}`,
    max: `${pageSize}`,
    ...sortingParams,
    ...filterParams,
    endDate: dateWithoutTimeZone({
      date: endDate,
    }),
    startDate: dateWithoutTimeZone({
      date: startDate,
    }),
    products: (products)?.map?.(({ id }) => id),
    binLocations: (binLocations)?.map?.(({ id }) => id),
    facility: currentLocationId,
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
    url: AUTO_ISSUANCE_TRANSACTIONS_REPORT,
    errorMessageId: 'react.cycleCount.table.errorMessage.label',
    defaultErrorMessage: 'Unable to fetch auto-issuance transactions',
    // We should start fetching only after clicking the button
    // or after refreshing the page with filters selected
    shouldFetch: !!(shouldFetch && endDate && startDate),
    setShouldFetch,
    disableInitialLoading: true,
    getParams,
    pageSize,
    sort,
    order,
    offset,
    searchTerm: null,
    filterParams,
    serializedParams,
    filtersInitialized,
  });

  useEffect(() => {
    setTableData({ data: [], totalCount: 0 });
  }, [currentLocationId]);

  const columns = useMemo(() => [
    columnHelper.accessor(autoIssuanceTransactionReportColumn.PRODUCT, {
      header: () => (
        <TableHeaderCell
          sortable
          columnId={autoIssuanceTransactionReportColumn.PRODUCT}
          {...sortableColumnProps}
        >
          {translate('react.cycleCount.table.product.label', 'Product')}
        </TableHeaderCell>
      ),
      cell: ({
        row: {
          original: {
            product: { id, productCode, name },
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
      size: 320,
    }), columnHelper.accessor(autoIssuanceTransactionReportColumn.LAST_COUNTED, {
      header: () => (
        <TableHeaderCell columnId={autoIssuanceTransactionReportColumn.LAST_COUNTED}>
          {translate('react.cycleCount.table.lastCounted.label', 'Last Counted')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue() ? formatLocalizedDate(getValue(), DateFormat.DD_MMM_YYYY) : ''}
        </TableCell>
      ),
      size: 160,
    }), columnHelper.accessor(autoIssuanceTransactionReportColumn.BIN_LOCATION, {
      header: () => (
        <TableHeaderCell
          sortable
          columnId={autoIssuanceTransactionReportColumn.BIN_LOCATION}
          {...sortableColumnProps}
        >
          {translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
        </TableHeaderCell>
      ),
      cell: ({
        row: {
          original: {
            binLocation,
          },
        },
      }) => (
        <TableCell className="rt-td">
          {binLocation?.name || (
            <span className="bin-location-default-label">
              {translate('react.cycleCount.table.autoIssuance.defaultBinLocation.label', 'Default')}
            </span>
          )}
        </TableCell>
      ),
      size: 160,
    }), columnHelper.accessor(autoIssuanceTransactionReportColumn.QUANTITY_ON_HAND, {
      header: () => (
        <TableHeaderCell columnId={autoIssuanceTransactionReportColumn.QUANTITY_ON_HAND}>
          {translate('react.cycleCount.table.autoIssuance.quantityOnHand.label', 'Quantity On Hand')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => {
        const quantityOnHand = getValue() ?? 0;
        return (
          <TableCell className="rt-td d-flex justify-content-end">
            <span className={quantityOnHand < 0 ? 'negative-quantity' : undefined}>
              {String(quantityOnHand)}
            </span>
          </TableCell>
        );
      },
      size: 150,
    }), columnHelper.accessor(autoIssuanceTransactionReportColumn.TRANSACTION_NUMBER, {
      header: () => (
        <TableHeaderCell columnId={autoIssuanceTransactionReportColumn.TRANSACTION_NUMBER}>
          {translate('react.cycleCount.table.transactionId.label', 'Transaction ID')}
        </TableHeaderCell>
      ),
      cell: ({
        row: {
          original: {
            transaction,
          },
        },
      }) => (
        <TableCell
          link={transaction?.id ? INVENTORY_URL.showTransaction(transaction.id) : null}
          className="rt-td"
        >
          {transaction?.transactionNumber}
        </TableCell>
      ),
      size: 160,
    }), columnHelper.accessor(autoIssuanceTransactionReportColumn.REQUISITION_NUMBER, {
      header: () => (
        <TableHeaderCell columnId={autoIssuanceTransactionReportColumn.REQUISITION_NUMBER}>
          {translate('react.cycleCount.table.autoIssuance.requisitionNumber.label', 'Requisition Number')}
        </TableHeaderCell>
      ),
      cell: ({
        row: {
          original: {
            requisition,
          },
        },
      }) => (
        <TableCell
          link={requisition?.id ? STOCK_MOVEMENT_URL.show(requisition.id) : null}
          className="rt-td"
        >
          {requisition?.requestNumber}
        </TableCell>
      ),
      size: 180,
    }), columnHelper.accessor(autoIssuanceTransactionReportColumn.QUANTITY_REDUCED, {
      header: () => (
        <TableHeaderCell columnId={autoIssuanceTransactionReportColumn.QUANTITY_REDUCED}>
          {translate('react.cycleCount.table.autoIssuance.quantityReduced.label', 'Quantity Reduced')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td d-flex justify-content-end">
          {String(getValue() ?? 0)}
        </TableCell>
      ),
      size: 140,
    }), columnHelper.accessor(autoIssuanceTransactionReportColumn.TRANSACTION_DATE, {
      header: () => (
        <TableHeaderCell columnId={autoIssuanceTransactionReportColumn.TRANSACTION_DATE}>
          {translate('react.cycleCount.table.autoIssuance.transactionDate.label', 'Transaction Date')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue() ? formatLocalizedDate(getValue(), DateFormat.DD_MMM_YYYY) : ''}
        </TableCell>
      ),
      size: 160,
    })], [translate, sort, order]);

  const emptyTableMessage = !filterParams.startDate || !filterParams.endDate
    ? {
      id: 'react.cycleCount.reporting.emptyTable.label',
      defaultMessage: 'Select a time range from above filters to load the table.',
    }
    : {
      id: 'react.cycleCount.table.noResultFound.label',
      defaultMessage: 'No result found.',
    };

  return {
    columns,
    tableData,
    loading,
    emptyTableMessage,
  };
};

export default useAutoIssuanceTransactionsTab;
