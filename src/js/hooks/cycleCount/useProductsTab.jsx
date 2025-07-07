import React, { useEffect, useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getFormatLocalizedDate } from 'selectors';

import { INVENTORY_AUDIT_SUMMARY_REPORT } from 'api/urls';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import ValueIndicator from 'components/DataTable/v2/ValueIndicator';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import cycleCountColumn from 'consts/cycleCountColumn';
import { DateFormat } from 'consts/timeFormat';
import { getCycleCountDifferencesVariant } from 'consts/valueIndicatorVariant';
import useTableDataV2 from 'hooks/useTableDataV2';
import useTableSorting from 'hooks/useTableSorting';
import useTranslate from 'hooks/useTranslate';
import Badge from 'utils/Badge';
import dateWithoutTimeZone from 'utils/dateUtils';
import formatCurrency from 'utils/formatCurrency';
import formatNumber from 'utils/formatNumber';

const useProductsTab = ({
  filterParams,
  offset,
  pageSize,
  shouldFetch,
  setShouldFetch,
  serializedParams,
}) => {
  const columnHelper = createColumnHelper();
  const translate = useTranslate();
  const {
    products,
    endDate,
    startDate,
  } = filterParams;
  const {
    currentLocale,
    currentLocation,
    formatLocalizedDate,
  } = useSelector((state) => ({
    currentLocale: state.session.activeLanguage,
    currentLocation: state.session.currentLocation,
    formatLocalizedDate: getFormatLocalizedDate(state),
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
      date: endDate,
    }),
    startDate: dateWithoutTimeZone({
      date: startDate,
    }),
    products: products?.map?.(({ id }) => id),
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
    url: INVENTORY_AUDIT_SUMMARY_REPORT,
    errorMessageId: 'react.cycleCount.table.errorMessage.label',
    defaultErrorMessage: 'Unable to fetch products',
    // We should start fetching only after clicking the "Load Table" button
    shouldFetch,
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
  });

  const getStyledCurrency = (value) => {
    const formattedValue = formatCurrency(value);

    if (value < 0) {
      return {
        formattedValue,
        className: 'font-red-ob',
      };
    }

    if (value > 0) {
      return {
        formattedValue: `+${formattedValue}`,
        className: 'font-green-ob',
      };
    }

    return {
      formattedValue,
      className: '',
    };
  };

  useEffect(() => {
    setTableData({
      data: [],
      totalCount: 0,
    });
  }, [currentLocation?.id]);

  const lastCountedColumn = columnHelper.accessor(cycleCountColumn.LAST_COUNTED, {
    header: () => (
      <TableHeaderCell
        sortable
        columnId={cycleCountColumn.LAST_COUNTED}
        {...sortableProps}
      >
        {translate('react.cycleCount.table.lastCounted.label', 'Last Counted')}
      </TableHeaderCell>
    ),
    cell: ({ getValue }) => (
      <TableCell className="rt-td">
        {formatLocalizedDate(getValue(), DateFormat.DD_MMM_YYYY)}
      </TableCell>
    ),
    size: 160,
  });

  const columns = useMemo(() => [
    columnHelper.accessor(cycleCountColumn.PRODUCT, {
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.PRODUCT} {...sortableProps}>
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
          className="rt-td multiline-cell"
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
    }),
    columnHelper.accessor(cycleCountColumn.PRODUCTS_TAB_CATEGORY, {
      header: () => (
        <TableHeaderCell
          sortable
          columnId={cycleCountColumn.PRODUCTS_TAB_CATEGORY}
          {...sortableProps}
        >
          {translate('react.cycleCount.table.category.label', 'Category')}
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
    columnHelper.accessor((row) =>
      row?.product.tags?.map?.((tag) => <Badge label={tag?.name} variant="badge--purple" tooltip key={tag.id} />), {
      id: cycleCountColumn.PRODUCTS_TAB_TAGS,
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.tag.label', 'Tag')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td multiline-cell">
          <div className="badge-container">
            {getValue()}
          </div>
        </TableCell>
      ),
      size: 130,
    }),
    columnHelper.accessor((row) =>
      row?.product.catalogs?.map((catalog) => <Badge label={catalog?.name} variant="badge--blue" tooltip key={catalog.id} />), {
      id: cycleCountColumn.PRODUCTS_TAB_CATALOGS,
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.productCatalogue.label', 'Product Catalogue')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td multiline-cell">
          <div className="badge-container">
            {getValue()}
          </div>
        </TableCell>
      ),
      size: 220,
    }),
    columnHelper.accessor(cycleCountColumn.PRODUCTS_TAB_ABC_CLASS, {
      header: () => (
        <TableHeaderCell
          sortable
          columnId={cycleCountColumn.PRODUCTS_TAB_ABC_CLASS}
          {...sortableProps}
        >
          {translate('react.cycleCount.table.abcClass.label', 'ABC Class')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue()}
        </TableCell>
      ),
      size: 120,
    }),
    columnHelper.accessor(cycleCountColumn.NUMBER_OF_COUNTS, {
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.NUMBER_OF_COUNTS} {...sortableProps}>
          {translate('react.cycleCount.table.numberOfCounts.label', 'Number Of Counts')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td d-flex justify-content-end">
          {getValue()?.toString()}
        </TableCell>
      ),
      size: 160,
    }),
    columnHelper.accessor(cycleCountColumn.NUMBER_OF_ADJUSTMENTS, {
      header: () => (
        <TableHeaderCell
          sortable
          columnId={cycleCountColumn.NUMBER_OF_ADJUSTMENTS}
          {...sortableProps}
        >
          {translate('react.cycleCount.table.numberOfAdjustments.label', 'Number Of Adjustments')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td d-flex justify-content-end">
          {getValue()?.toString()}
        </TableCell>
      ),
      size: 200,
    }),
    columnHelper.accessor(cycleCountColumn.TOTAL_OF_ADJUSTMENTS, {
      header: () => (
        <TableHeaderCell
          sortable
          columnId={cycleCountColumn.TOTAL_OF_ADJUSTMENTS}
          {...sortableProps}
        >
          {translate('react.cycleCount.table.totalOfAdjustments.label', 'Total Of Adjustments')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => {
        const value = getValue();
        const variant = getCycleCountDifferencesVariant({ firstValue: value });
        const className = value > 0 ? 'value-indicator--more' : 'value-indicator--less';
        return (
          <TableCell className="rt-td d-flex justify-content-end">
            <div>
              <ValueIndicator
                className={`pr-2 pl-1 py-1 value-indicator ${value !== 0 && className}`}
                value={value?.toString()}
                variant={variant}
                showAbsoluteValue
              />
            </div>
          </TableCell>
        );
      },
      size: 180,
    }),
    columnHelper.accessor(cycleCountColumn.ADJUSTMENTS_VALUE, {
      header: () => (
        <TableHeaderCell
          sortable
          columnId={cycleCountColumn.ADJUSTMENTS_VALUE}
          {...sortableProps}
        >
          {translate('react.cycleCount.table.adjustmentsValue.label', 'Adjustments Value')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => {
        const { formattedValue, className } = getStyledCurrency(getValue());
        return (
          <TableCell className={`rt-td d-flex justify-content-end ${className}`}>
            {formattedValue}
          </TableCell>
        );
      },
      size: 160,
    }),
    columnHelper.accessor(cycleCountColumn.MONTHS_OF_STOCK_CHANGE, {
      header: () => (
        <TableHeaderCell
          sortable
          columnId={cycleCountColumn.MONTHS_OF_STOCK_CHANGE}
          {...sortableProps}
        >
          {translate('react.cycleCount.table.monthsOfStockChange.label', 'Months Of Stock Change')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => {
        const value = getValue();
        return (
          <TableCell className={`rt-td d-flex justify-content-end ${value > 0 ? 'font-green-ob' : value < 0 && 'font-red-ob'}`}>
            {value > 0 && '+'}
            {value?.toFixed(1)}
          </TableCell>
        );
      },
      size: 200,
    }),
    columnHelper.accessor(cycleCountColumn.QUANTITY_ON_HAND, {
      header: () => (
        <TableHeaderCell
          sortable
          columnId={cycleCountColumn.QUANTITY_ON_HAND}
          {...sortableProps}
        >
          {translate('react.cycleCount.table.currentlyInStock.label', 'Currently in Stock')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td d-flex justify-content-end">
          {formatNumber(getValue()?.toString())}
        </TableCell>
      ),
      size: 155,
    }),
    columnHelper.accessor(cycleCountColumn.VALUE_ON_HAND, {
      header: () => (
        <TableHeaderCell
          sortable
          columnId={cycleCountColumn.VALUE_ON_HAND}
          {...sortableProps}
        >
          {translate('react.cycleCount.table.valueInStock.label', 'Value in Stock')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td d-flex justify-content-end font-weight-bold">
          {formatCurrency(getValue())}
        </TableCell>
      ),
      size: 130,
    }),
  ], [currentLocale]);

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
    columns: [
      ...columns.slice(0, 5),
      lastCountedColumn,
      ...columns.slice(5),
    ],
    tableData,
    loading,
    emptyTableMessage,
    exportData,
  };
};

export default useProductsTab;
