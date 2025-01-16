import React, { useMemo, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { useSelector } from 'react-redux';

import { CYCLE_COUNT_CANDIDATES } from 'api/urls';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import useSpinner from 'hooks/useSpinner';
import useTableDataV2 from 'hooks/useTableDataV2';
import useTableSorting from 'hooks/useTableSorting';
import useTranslate from 'hooks/useTranslate';
import exportFileFromAPI from 'utils/file-download-util';
import { mapStringToList } from 'utils/form-values-utils';

const useAllProductsTab = ({ filterParams }) => {
  const columnHelper = createColumnHelper();
  const spinner = useSpinner();
  const translate = useTranslate();
  const [pageSize, setPageSize] = useState(5);
  const [offset, setOffset] = useState(0);

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

  const getParams = ({
    sortingParams,
  }) => _.omitBy({
    offset: `${offset}`,
    max: `${pageSize}`,
    ...sortingParams,
    ...filterParams,
    searchTerm,
    facility: currentLocation?.id,
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
    shouldFetch: filterParams.tab,
    getParams,
    pageSize,
    offset,
    sort,
    order,
    searchTerm,
    filterParams,
  });

  const columns = useMemo(() => [
    columnHelper.accessor('lastCountDate', {
      header: () => (
        <TableHeaderCell sortable columnId="dateLastCount" {...sortableProps}>
          {translate('react.cycleCount.table.lastCounted.label', 'Last Counted')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue()}
        </TableCell>
      ),
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
          link={INVENTORY_ITEM_URL.showStockCard(row.original.product.productCode)}
          className="rt-td multiline-cell"
        >
          {getValue()}
        </TableCell>
      ),
    }),
    columnHelper.accessor('category.name', {
      header: () => (
        <TableHeaderCell sortable columnId="category" {...sortableProps}>
          {translate('react.cycleCount.table.category.label', 'Category')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td multiline-cell">
          {getValue()}
        </TableCell>
      ),
    }),
    columnHelper.accessor('internalLocations', {
      header: () => (
        <TableHeaderCell sortable columnId="internalLocations" {...sortableProps}>
          {translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td"
          tooltip
          tooltipLabel={getValue()}
        >
          {mapStringToList(getValue(), ',', 100).map((binLocationName) => <div>{binLocationName}</div>)}
        </TableCell>
      ),
    }),
    columnHelper.accessor((row) =>
      row?.tags?.map?.((tag) => <div>{tag?.tag}</div>), {
      id: 'tags',
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.tag.label', 'Tag')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td multiline-cell">
          {getValue()}
        </TableCell>
      ),
    }),
    columnHelper.accessor((row) =>
      row?.productCatalogs?.map((catalog) => <div>{catalog?.name}</div>), {
      id: 'productCatalogs',
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.productCatalogue.label', 'Product Catalogue')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td multiline-cell">
          {getValue()}
        </TableCell>
      ),
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

  return {
    columns,
    tableData,
    loading,
    emptyTableMessage,
    exportTableData,
    setPageSize,
    setOffset,
  };
};

export default useAllProductsTab;
