import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { useSelector } from 'react-redux';

import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT_CANDIDATES } from 'api/urls';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import Checkbox from 'components/form-elements/v2/Checkbox';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import { TO_COUNT_TAB } from 'consts/cycleCount';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';
import useTableCheckboxes from 'hooks/useTableCheckboxes';
import useTableDataV2 from 'hooks/useTableDataV2';
import useTableSorting from 'hooks/useTableSorting';
import useTranslate from 'hooks/useTranslate';
import Badge from 'utils/Badge';
import exportFileFromAPI from 'utils/file-download-util';
import { mapStringToLimitedList } from 'utils/form-values-utils';

const useAllProductsTab = ({
  filterParams,
  switchTab,
  offset,
  pageSize,
  resetForm,
  setToCountCheckedCheckboxes,
}) => {
  const columnHelper = createColumnHelper();
  const spinner = useSpinner();
  const translate = useTranslate();
  const { tab } = useQueryParams();

  const {
    currentLocale,
    currentLocation,
  } = useSelector((state) => ({
    currentLocale: state.session.activeLanguage,
    currentLocation: state.session.currentLocation,
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
    offset: `${offset}`,
    max: `${pageSize}`,
    ...sortingParams,
    ...filterParams,
    searchTerm,
    dateLastCount,
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
  });

  const productIds = tableData.data.map((row) => row.product.id);

  // Separated from columns to reduce the amount of rerenders of
  // the rest columns (on checked checkboxes change)
  const checkboxesColumn = columnHelper.accessor('selected', {
    header: () => (
      <TableHeaderCell>
        <Checkbox
          noWrapper
          {...headerCheckboxProps}
          onClick={selectHeaderCheckbox(productIds)}
        />
      </TableHeaderCell>
    ),
    cell: ({ row }) => (
      <TableCell className="rt-td">
        <Checkbox
          noWrapper
          onChange={selectRow(row.original.product.id)}
          value={isChecked(row.original.product.id)}
        />
      </TableCell>
    ),
    meta: {
      getCellContext: () => ({
        className: 'checkbox-column',
      }),
      flexWidth: 40,
    },
  });

  const columns = useMemo(() => [
    columnHelper.accessor('lastCountDate', {
      header: () => (
        <TableHeaderCell
          sortable
          columnId="dateLastCount"
          {...sortableProps}
        >
          {translate('react.cycleCount.table.lastCounted.label', 'Last Counted')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue()}
        </TableCell>
      ),
      meta: {
        flexWidth: 180,
      },
    }),
    columnHelper.accessor((row) => `${row.product.productCode} ${row.product.name}`, {
      id: 'product',
      header: () => (
        <TableHeaderCell sortable columnId="product" {...sortableProps}>
          {translate('react.cycleCount.table.products.label', 'Products')}
        </TableHeaderCell>
      ),
      cell: ({ getValue, row }) => (
        <TableCell
          tooltip
          tooltipLabel={getValue()}
          link={INVENTORY_ITEM_URL.showStockCard(row.original.product.id)}
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
    columnHelper.accessor('category.name', {
      header: () => (
        <TableHeaderCell sortable columnId="category" {...sortableProps}>
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
    columnHelper.accessor('internalLocations', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => {
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
      id: 'tags',
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
      meta: {
        flexWidth: 200,
      },
    }),
    columnHelper.accessor((row) =>
      row?.productCatalogs?.map((catalog) => <Badge label={catalog?.name} variant="badge--blue" tooltip key={catalog.id} />), {
      id: 'productCatalogs',
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
      meta: {
        flexWidth: 200,
      },
    }),
    columnHelper.accessor('abcClass', {
      header: () => (
        <TableHeaderCell sortable columnId="abcClass" {...sortableProps}>
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
    columnHelper.accessor('quantityOnHand', {
      header: () => (
        <TableHeaderCell sortable columnId="quantityOnHand" {...sortableProps}>
          {translate('react.cycleCount.table.quantity.label', 'Quantity')}
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

  return {
    columns: [checkboxesColumn, ...columns],
    tableData,
    loading,
    emptyTableMessage,
    exportTableData,
    selectedCheckboxesAmount,
    countSelected,
  };
};

export default useAllProductsTab;
