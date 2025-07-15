import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { useSelector } from 'react-redux';
import {
  getCurrentLocale,
  getCurrentLocation,
  getCycleCountMaxSelectedProducts,
  getFormatLocalizedDate,
} from 'selectors';

import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT_CANDIDATES } from 'api/urls';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import Checkbox from 'components/form-elements/v2/Checkbox';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import { TO_COUNT_TAB } from 'consts/cycleCount';
import CycleCountCandidateStatus from 'consts/cycleCountCandidateStatus';
import cycleCountColumn from 'consts/cycleCountColumn';
import { DateFormat } from 'consts/timeFormat';
import useCycleCountProductAvailability from 'hooks/cycleCount/useCycleCountProductAvailability';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';
import useTableCheckboxes from 'hooks/useTableCheckboxes';
import useTableDataV2 from 'hooks/useTableDataV2';
import useTableSorting from 'hooks/useTableSorting';
import useThrowError from 'hooks/useThrowError';
import useTranslate from 'hooks/useTranslate';
import Badge from 'utils/Badge';
import dateWithoutTimeZone from 'utils/dateUtils';
import exportFileFromAPI from 'utils/file-download-util';
import { mapStringToLimitedList } from 'utils/form-values-utils';

const useAllProductsTab = ({
  filterParams,
  switchTab,
  offset,
  pageSize,
  resetForm,
  setToCountCheckedCheckboxes,
  serializedParams,
}) => {
  const columnHelper = createColumnHelper();
  const spinner = useSpinner();
  const translate = useTranslate();
  const { tab } = useQueryParams();

  const {
    currentLocale,
    currentLocation,
    cycleCountMaxSelectedProducts,
    formatLocalizedDate,
  } = useSelector((state) => ({
    currentLocale: getCurrentLocale(state),
    currentLocation: getCurrentLocation(state),
    cycleCountMaxSelectedProducts: getCycleCountMaxSelectedProducts(state),
    formatLocalizedDate: getFormatLocalizedDate(state),
  }));

  const {
    dateLastCount,
    categories,
    internalLocations,
    tags,
    catalogs,
    abcClasses,
    negativeQuantity,
    searchTerm,
  } = filterParams;

  const {
    selectRow,
    isChecked,
    selectHeaderCheckbox,
    selectedCheckboxesAmount,
    checkedCheckboxes,
    headerCheckboxProps,
    resetCheckboxes,
  } = useTableCheckboxes();

  const getParams = ({
    sortingParams,
  }) => _.omitBy({
    statuses: [
      CycleCountCandidateStatus.CREATED,
      CycleCountCandidateStatus.REQUESTED,
      CycleCountCandidateStatus.COUNTING,
      CycleCountCandidateStatus.COUNTED,
      CycleCountCandidateStatus.INVESTIGATING,
    ],
    showCycleCountsInProgress: true,
    offset: `${offset}`,
    max: `${pageSize}`,
    ...sortingParams,
    ...filterParams,
    searchTerm,
    dateLastCount: dateWithoutTimeZone({
      date: dateLastCount,
    }),
    categories: categories?.map?.(({ id }) => id),
    internalLocations: internalLocations?.map?.(({ name }) => name),
    tags: tags?.map?.(({ id }) => id),
    catalogs: catalogs?.map?.(({ id }) => id),
    abcClasses: abcClasses?.map?.(({ id }) => id),
    negativeQuantity,
  }, (val) => {
    if (typeof val === 'boolean') {
      return !val;
    }
    return _.isEmpty(val);
  });

  const {
    sortableProps,
    sort,
    order,
  } = useTableSorting();

  const {
    tableData,
    loading,
  } = useTableDataV2({
    url: CYCLE_COUNT_CANDIDATES(currentLocation?.id),
    errorMessageId: 'react.cycleCount.table.errorMessage.label',
    defaultErrorMessage: 'Unable to fetch products',
    // We should start fetching after initializing the filters to avoid re-fetching
    shouldFetch: filterParams.tab && tab === filterParams.tab,
    getParams,
    pageSize,
    offset,
    sort,
    order,
    searchTerm,
    filterParams,
    serializedParams,
  });

  const extendedDataTable = useMemo(() => {
    if (!tableData.data) {
      return tableData;
    }
    return {
      ...tableData,
      data: tableData.data.map((row) => ({
        ...row,
        // Added meta to rows because TanStack Table doesn't support meta at the row level
        meta: useCycleCountProductAvailability(row),
      })),
    };
  }, [tableData]);

  const productIds = extendedDataTable.data
    .filter((row) => !row.meta.isRowDisabled)
    .map((row) => row.product.id);

  // Separated from columns to reduce the amount of rerenders of
  // the rest columns (on checked checkboxes change)
  const checkboxesColumn = columnHelper.accessor(cycleCountColumn.SELECTED, {
    header: () => (
      <TableHeaderCell>
        <Checkbox
          noWrapper
          {...headerCheckboxProps}
          onClick={selectHeaderCheckbox(productIds)}
        />
      </TableHeaderCell>
    ),
    cell: ({ row }) => {
      const { isRowDisabled, isFromOtherTab } = row.original.meta;
      return (
        <TableCell className="rt-td">
          <Checkbox
            noWrapper
            onChange={selectRow(row.original.product.id)}
            value={isRowDisabled && isFromOtherTab
              ? true
              : isChecked(row.original.product.id)}
            disabled={isRowDisabled}
          />
        </TableCell>
      );
    },
    meta: {
      getCellContext: () => ({
        className: 'checkbox-column',
      }),
      flexWidth: 40,
    },
  });

  const dateLastCountColumn = columnHelper.accessor(cycleCountColumn.DATE_LAST_COUNTED, {
    header: () => (
      <TableHeaderCell
        sortable
        columnId={cycleCountColumn.DATE_LAST_COUNTED}
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
    meta: {
      flexWidth: 180,
    },
  });

  const columns = useMemo(() => [
    columnHelper.accessor((row) => `${row.product.productCode} ${row.product.name}`, {
      id: cycleCountColumn.PRODUCT,
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.PRODUCT} {...sortableProps}>
          {translate('react.cycleCount.table.products.label', 'Products')}
        </TableHeaderCell>
      ),
      cell: ({ getValue, row }) => (
        <TableCell
          tooltip
          tooltipLabel={getValue()}
          // If isFromOtherTab is true, we don't want the link to work.
          // This means that the product is already in To Count tab or To Resolve tab.
          link={!row.original.meta.isFromOtherTab
            && INVENTORY_ITEM_URL.showStockCard(row.original.product.id)}
          className="rt-td multiline-cell"
        >
          <div className="limit-lines-2">
            {getValue()}
          </div>
        </TableCell>
      ),
      meta: {
        flexWidth: 370,
      },
    }),
    columnHelper.accessor(cycleCountColumn.CATEGORY_NAME, {
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.CATEGORY} {...sortableProps}>
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
      meta: {
        flexWidth: 200,
      },
    }),
    columnHelper.accessor(cycleCountColumn.INTERNAL_LOCATIONS, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => {
        if (!getValue()) {
          return null;
        }
        const binLocationList = mapStringToLimitedList(getValue(), ',');
        const hiddenBinLocationsLength = binLocationList.length - 4 > 0
          ? binLocationList.length - 4
          : null;

        return (
          <TableCell
            className="rt-td"
            tooltip
            tooltipLabel={`${getValue()} (${binLocationList.length})`}
          >
            {_.take(binLocationList, 4).map((binLocationName) => (
              <div className="truncate-text" key={crypto.randomUUID()}>
                {binLocationName}
              </div>
            ))}
            {hiddenBinLocationsLength && (
            <p>
              +
              {hiddenBinLocationsLength}
              {' '}
              more
            </p>
            )}
          </TableCell>
        );
      },
      meta: {
        flexWidth: 200,
      },
    }),
    columnHelper.accessor((row) =>
      row?.tags?.map?.((tag) => <Badge label={tag?.tag} variant="badge--purple" tooltip key={tag.id} />), {
      id: cycleCountColumn.TAGS,
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.tag.label', 'Tag')}
        </TableHeaderCell>
      ),
      cell: ({ getValue, row }) => (
        <TableCell className="rt-td multiline-cell">
          <div className={`badge-container ${row.original.meta.isRowDisabled && 'disabled'}`}>
            {getValue()}
          </div>
        </TableCell>
      ),
      meta: {
        flexWidth: 200,
      },
    }),
    columnHelper.accessor((row) =>
      row?.productCatalogs?.map((catalog) => <Badge label={catalog?.name} variant="badge--blue" tooltip key={catalog.id} />), {
      id: cycleCountColumn.PRODUCT_CATALOGS,
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.productCatalogue.label', 'Product Catalogue')}
        </TableHeaderCell>
      ),
      cell: ({ getValue, row }) => (
        <TableCell className="rt-td multiline-cell">
          <div className={`badge-container ${row.original.meta.isRowDisabled && 'disabled'}`}>
            {getValue()}
          </div>
        </TableCell>
      ),
      meta: {
        flexWidth: 200,
      },
    }),
    columnHelper.accessor(cycleCountColumn.ABC_CLASS, {
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.ABC_CLASS} {...sortableProps}>
          {translate('react.cycleCount.table.abcClass.label', 'ABC Class')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue()}
        </TableCell>
      ),
      meta: {
        flexWidth: 150,
      },
    }),
    columnHelper.accessor(cycleCountColumn.QUANTITY_ON_HAND, {
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.QUANTITY_ON_HAND} {...sortableProps}>
          {translate('react.cycleCount.table.quantity.label', 'Quantity')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue().toString()}
        </TableCell>
      ),
      meta: {
        flexWidth: 150,
      },
    }),
  ], [currentLocale, sort, order]);

  const emptyTableMessage = {
    id: 'react.cycleCount.table.emptyTable.label',
    defaultMessage: 'No products match the given criteria',
  };

  const exportTableData = () => {
    spinner.show();
    const date = new Date();
    const [month, day, year] = [date.getMonth(), date.getDate(), date.getFullYear()];
    const [hour, minutes, seconds] = [date.getHours(), date.getMinutes(), date.getSeconds()];
    exportFileFromAPI({
      url: CYCLE_COUNT_CANDIDATES(currentLocation?.id),
      params: getParams({}),
      filename: `CycleCountReport-${currentLocation?.name}-${year}${month}${day}-${hour}${minutes}${seconds}`,
      afterExporting: spinner.hide,
    });
  };

  const countSelected = async () => {
    const payload = {
      requests: checkedCheckboxes.map((productId) => ({
        product: productId,
        blindCount: true,
      })),
    };
    spinner.show();
    try {
      const response = await cycleCountApi.createRequest(payload, currentLocation?.id);
      setToCountCheckedCheckboxes((prev) =>
        [...prev, ...response.data.data.map((item) => item.id)]);
      switchTab(TO_COUNT_TAB, resetForm);
    } finally {
      resetCheckboxes();
      spinner.hide();
    }
  };

  const { verifyCondition } = useThrowError({
    condition: checkedCheckboxes.length <= cycleCountMaxSelectedProducts,
    callWhenValid: countSelected,
    errorMessageLabel: 'react.cycleCount.selectedMoreThanAllowed.error',
    errorMessageDefault: `Sorry, we cannot support counting more than ${cycleCountMaxSelectedProducts} products at once at the moment.
     Please start counting fewer products and then continue on the remaining products.`,
    translateData: {
      maxProductsNumber: cycleCountMaxSelectedProducts,
    },
  });

  return {
    columns: [checkboxesColumn, dateLastCountColumn, ...columns],
    tableData: extendedDataTable,
    loading,
    emptyTableMessage,
    exportTableData,
    selectedCheckboxesAmount,
    countSelected: verifyCondition,
  };
};

export default useAllProductsTab;
