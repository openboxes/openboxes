import React, { useEffect, useMemo, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocation } from 'selectors';

import { EXPIRATION_HISTORY_REPORT } from 'api/urls';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import { INVENTORY_ITEM_URL, INVENTORY_URL } from 'consts/applicationUrls';
import expirationHistoryReportColumn from 'consts/expirationHistoryReportColumn';
import { DateFormatDateFns } from 'consts/timeFormat';
import useSpinner from 'hooks/useSpinner';
import useTableDataV2 from 'hooks/useTableDataV2';
import useTablePagination from 'hooks/useTablePagination';
import useTranslate from 'hooks/useTranslate';
import dateWithoutTimeZone, { formatDateToDateOnlyString, formatISODate } from 'utils/dateUtils';
import exportFileFromAPI from 'utils/file-download-util';

const useExpirationHistoryReport = ({
  filterParams,
  defaultFilterValues,
  shouldFetch,
  setShouldFetch,
  filtersInitialized,
}) => {
  const columnHelper = createColumnHelper();

  const translate = useTranslate();

  const spinner = useSpinner();

  const [totalCount, setTotalCount] = useState(0);

  const currentLocation = useSelector(getCurrentLocation);

  const {
    paginationProps,
    offset,
    pageSize,
    serializedParams,
    setSerializedParams,
  } = useTablePagination({
    defaultPageSize: 10,
    totalCount,
    filterParams,
    setShouldFetch,
    disableAutoUpdateFilterParams: true,
  });

  const paginationParams = (paginate) => (paginate ? {
    'paginationParams.offset': `${offset}`,
    'paginationParams.max': `${pageSize}`,
  } : {});

  const getParams = ({
    paginate = true,
  }) => _.omitBy({
    ...paginationParams(paginate),
    endDate: dateWithoutTimeZone({
      date: filterParams.endDate || defaultFilterValues.endDate,
    }),
    startDate: dateWithoutTimeZone({
      date: filterParams.startDate || defaultFilterValues.startDate,
    }),
    searchTerm: filterParams.searchTerm,
  }, (val) => {
    if (typeof val === 'boolean') {
      return !val;
    }
    return _.isEmpty(val);
  });

  const exportData = () => {
    spinner.show();
    const date = new Date();
    const [month, day, year] = [date.getMonth(), date.getDate(), date.getFullYear()];
    const [hour, minutes, seconds] = [date.getHours(), date.getMinutes(), date.getSeconds()];
    exportFileFromAPI({
      url: EXPIRATION_HISTORY_REPORT,
      params: getParams({ paginate: false }),
      filename: `ExpirationHistoryReport-${currentLocation?.name}-${year}${month}${day}-${hour}${minutes}${seconds}`,
      afterExporting: spinner.hide,
    });
  };

  const {
    tableData,
    loading,
  } = useTableDataV2({
    url: EXPIRATION_HISTORY_REPORT,
    errorMessageId: 'react.report.expirationHistory.unableToLoadData.label',
    defaultErrorMessage: 'Unable to load data',
    shouldFetch: shouldFetch
      && (filterParams.endDate || defaultFilterValues.endDate)
      && (filterParams.startDate || defaultFilterValues.startDate),
    disableInitialLoading: true,
    getParams,
    pageSize,
    offset,
    searchTerm: filterParams.searchTerm,
    filterParams,
    serializedParams,
    setShouldFetch,
    filtersInitialized,
  });

  useEffect(() => {
    setTotalCount(tableData.totalCount);
  }, [tableData]);

  const columns = useMemo(() => [
    columnHelper.accessor(expirationHistoryReportColumn.TRANSACTION_NUMBER, {
      header: () => (
        <TableHeaderCell>
          {translate('react.report.expirationHistory.transactionId.label', 'Transaction Id')}
        </TableHeaderCell>
      ),
      cell: ({ getValue, row: { original: { transactionId } } }) => (
        <TableCell
          link={INVENTORY_URL.showTransaction(transactionId)}
          className="rt-td pb-0"
          customTooltip
          tooltipLabel={getValue()}
        >
          {getValue()}
        </TableCell>
      ),
      meta: {
        pinned: 'left',
      },
      size: 145,
    }),
    columnHelper.accessor(expirationHistoryReportColumn.TRANSACTION_DATE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.report.expirationHistory.transactionDate.label', 'Transaction Date')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td multiline-cell"
          customTooltip
          tooltipLabel={formatISODate(getValue(), DateFormatDateFns.DD_MMM_YYYY)}
        >
          {formatISODate(getValue(), DateFormatDateFns.DD_MMM_YYYY)}
        </TableCell>
      ),
      meta: {
        pinned: 'left',
      },
      size: 145,
    }), columnHelper.accessor(expirationHistoryReportColumn.PRODUCT_CODE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.report.expirationHistory.code.label', 'Code')}
        </TableHeaderCell>
      ),
      cell: ({ getValue, row: { original: { productId } } }) => (
        <TableCell
          link={INVENTORY_ITEM_URL.showStockCard(productId)}
          className="rt-td pb-0"
          customTooltip
          tooltipLabel={getValue()}
        >
          <div>
            {getValue()}
          </div>
        </TableCell>
      ),
      meta: {
        pinned: 'left',
      },
      size: 80,
    }), columnHelper.accessor(expirationHistoryReportColumn.PRODUCT_NAME, {
      header: () => (
        <TableHeaderCell>
          {translate('react.report.expirationHistory.productName.label', 'Product Name')}
        </TableHeaderCell>
      ),
      cell: ({ getValue, row: { original: { productId } } }) => (
        <TableCell
          link={INVENTORY_ITEM_URL.showStockCard(productId)}
          className="rt-td pb-0 multiline-cell"
          customTooltip
          tooltipLabel={getValue()}
        >
          <div className="limit-lines-2">
            {getValue()}
          </div>
        </TableCell>
      ),
      size: 360,
    }),
    columnHelper.accessor(expirationHistoryReportColumn.CATEGORY_NAME, {
      header: () => (
        <TableHeaderCell>
          {translate('react.report.expirationHistory.category.label', 'Category')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td multiline-cell"
          customTooltip
          tooltipLabel={getValue()}
        >
          <div className="limit-lines-2">{getValue()}</div>
        </TableCell>
      ),
      size: 180,
    }),
    columnHelper.accessor(expirationHistoryReportColumn.LOT_NUMBER, {
      header: () => (
        <TableHeaderCell>
          {translate('react.report.expirationHistory.lotNumber.label', 'Lot Number')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td multiline-cell"
          customTooltip
          tooltipLabel={getValue()}
        >
          {getValue()}
        </TableCell>
      ),
    }), columnHelper.accessor(expirationHistoryReportColumn.EXPIRATION_DATE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.report.expirationHistory.expirationDate.label', 'Expiration Date')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td multiline-cell"
          customTooltip
          tooltipLabel={formatDateToDateOnlyString(getValue())}
        >
          {formatDateToDateOnlyString(getValue())}
        </TableCell>
      ),
    }), columnHelper.accessor(expirationHistoryReportColumn.QUANTITY_LOST_TO_EXPIRY, {
      header: () => (
        <TableHeaderCell>
          {translate('react.report.expirationHistory.quantityLostToExpiry.label', 'Quantity Lost to Expiry')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td multiline-cell"
          customTooltip
          tooltipLabel={getValue() || '0'}
        >
          {getValue() || '0'}
        </TableCell>
      ),
    }), columnHelper.accessor(expirationHistoryReportColumn.UNIT_PRICE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.report.expirationHistory.unitPrice.label', 'Unit Price')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td multiline-cell"
          customTooltip
          tooltipLabel={getValue() || '0'}
        >
          {getValue() || '0'}
        </TableCell>
      ),
    }), columnHelper.accessor(expirationHistoryReportColumn.VALUE_LOST_TO_EXPIRY, {
      header: () => (
        <TableHeaderCell>
          {translate('react.report.expirationHistory.valueLostToExpiry.label', 'Value Lost to Expiry')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td multiline-cell"
          customTooltip
          tooltipLabel={getValue() || '0'}
        >
          {getValue() || '0'}
        </TableCell>
      ),
    }),
  ], []);

  const emptyTableMessage = !filterParams.startDate && !filterParams.endDate
    ? {
      id: 'react.report.expirationHistory.selectATimeRange.label',
      defaultMessage: 'Select a time range from above filters to load the table.',
    }
    : {
      id: 'react.report.expirationHistory.noResultFound.label',
      defaultMessage: 'No result found.',
    };

  return {
    columns,
    loading,
    emptyTableMessage,
    tableData,
    paginationProps,
    setSerializedParams,
    exportData,
  };
};

export default useExpirationHistoryReport;
