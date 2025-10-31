import React, { useEffect, useMemo, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocation } from 'selectors';

import { EXPIRATION_HISTORY_REPORT } from 'api/urls';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import { INVENTORY_ITEM_URL, INVENTORY_URL } from 'consts/applicationUrls';
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
    columnHelper.accessor('transactionNumber', {
      header: () => (
        <TableHeaderCell columnId="transactionNumber">
          {translate('react.report.expirationHistory.transactionId.label', 'Transaction Id')}
        </TableHeaderCell>
      ),
      cell: ({ getValue, row: { original: { transactionId } } }) => (
        <TableCell
          link={INVENTORY_URL.showTransaction(transactionId)}
          className="rt-td pb-0"
        >
          <div>
            {getValue()}
          </div>
        </TableCell>
      ),
      meta: {
        pinned: 'left',
      },
      size: 145,
    }),
    columnHelper.accessor('transactionDate', {
      header: () => (
        <TableHeaderCell columnId="transactionDate">
          {translate('react.report.expirationHistory.transactionDate.label', 'Transaction Date')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <div className="rt-td pb-0 d-flex align-items-start">
          {formatISODate(getValue(), DateFormatDateFns.DD_MMM_YYYY)}
        </div>
      ),
      meta: {
        pinned: 'left',
      },
      size: 145,
    }), columnHelper.accessor('productCode', {
      header: () => (
        <TableHeaderCell columnId="productCode">
          {translate('react.report.expirationHistory.code.label', 'Code')}
        </TableHeaderCell>
      ),
      cell: ({ getValue, row: { original: { productId } } }) => (
        <TableCell
          link={INVENTORY_ITEM_URL.showStockCard(productId)}
          className="rt-td pb-0"
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
    }), columnHelper.accessor('productName', {
      header: () => (
        <TableHeaderCell columnId="productName">
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
    columnHelper.accessor('category.name', {
      header: () => (
        <TableHeaderCell columnId="category.name">
          {translate('react.report.expirationHistory.category.label', 'Category')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td multiline-cell"
          tooltip
          tooltipLabel={getValue()}
        >
          <div className="truncate-text">{getValue()}</div>
        </TableCell>
      ),
      size: 180,
    }),
    columnHelper.accessor('lotNumber', {
      header: () => (
        <TableHeaderCell columnId="lotNumber">
          {translate('react.report.expirationHistory.lotNumber.label', 'Lot Number')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <div className="rt-td pb-0 d-flex align-items-start">
          {getValue()}
        </div>
      ),
    }), columnHelper.accessor('expirationDate', {
      header: () => (
        <TableHeaderCell columnId="expirationDate">
          {translate('react.report.expirationHistory.expirationDate.label', 'Expiration Date')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <div className="rt-td pb-0 d-flex align-items-start">
          {formatDateToDateOnlyString(getValue())}
        </div>
      ),
    }), columnHelper.accessor('quantityLostToExpiry', {
      header: () => (
        <TableHeaderCell columnId="quantityLostToExpiry">
          {translate('react.report.expirationHistory.quantityLostToExpiry.label', 'Quantity Lost to Expiry')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <div className="rt-td pb-0 d-flex align-items-start">
          {getValue() || '0'}
        </div>
      ),
    }), columnHelper.accessor('unitPrice', {
      header: () => (
        <TableHeaderCell columnId="unitPrice">
          {translate('react.report.expirationHistory.unitPrice.label', 'Unit Price')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <div className="rt-td pb-0 d-flex align-items-start">
          {getValue() || '0'}
        </div>
      ),
    }), columnHelper.accessor('valueLostToExpiry', {
      header: () => (
        <TableHeaderCell columnId="valueLostToExpiry">
          {translate('react.report.expirationHistory.valueLostToExpiry.label', 'Value Lost to Expiry')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <div className="rt-td pb-0 d-flex align-items-start">
          {getValue() || '0'}
        </div>
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
