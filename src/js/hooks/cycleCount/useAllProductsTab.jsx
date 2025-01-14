import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import fileDownload from 'js-file-download';
import queryString from 'query-string';
import { useSelector } from 'react-redux';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import cycleCountMockedData from 'consts/cycleCountMockedData';
import useTableData from 'hooks/list-pages/useTableData';
import useTranslate from 'hooks/useTranslate';
import Badge from 'utils/Badge';

import 'utils/utils.scss';

const useAllProductsTab = () => {
  const columnHelper = createColumnHelper();
  const translate = useTranslate();

  const currentLocale = useSelector((state) => ({
    currentLocale: state.session.activeLanguage,
  }));

  const { tableData, loading } = useTableData({
    filterParams: {},
    // Should be replaced after integrating with backend
    url: 'cycleCount',
    errorMessageId: 'react.cycleCount.table.errorMessage.label',
    defaultErrorMessage: 'Unable to fetch products',
    fetchManually: true,
    getParams: () => {},
    onFetchedData: () => {},
  });

  const {
    currentLocation,
  } = useSelector((state) => ({
    currentLocation: state.session.currentLocation,
  }));

  const columns = useMemo(() => [
    columnHelper.accessor('lastCountDate', {
      header: () => (
        <TableHeaderCell sortable>
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
        <TableHeaderCell sortable>
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
    columnHelper.accessor('product.category', {
      header: () => (
        <TableHeaderCell sortable>
          {translate('react.cycleCount.table.category.label', 'Category')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td multiline-cell">
          {getValue()}
        </TableCell>
      ),
    }),
    columnHelper.accessor('binLocation.name', {
      header: () => (
        <TableHeaderCell sortable>
          {translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell
          className="rt-td"
          tooltip
          tooltipLabel={getValue()}
        >
          {getValue()}
        </TableCell>
      ),
    }),
    columnHelper.accessor((row) =>
      row?.product?.tags?.map((tag) => <Badge label={tag.name} variant="badge--purple" key={tag.id} />), {
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
    }),
    columnHelper.accessor((row) =>
      row?.product.catalogs?.map((catalog) => <Badge label={catalog.name} variant="badge--blue" key={catalog.id} />),
    {
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
    }),
    columnHelper.accessor('product.abcClass', {
      header: () => (
        <TableHeaderCell sortable>
          {translate('react.cycleCount.table.abcClass.label', 'ABC Class')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue()}
        </TableCell>
      ),
    }),
    columnHelper.accessor('product.quantity', {
      header: () => (
        <TableHeaderCell sortable>
          {translate('react.cycleCount.table.quantity.label', 'Quantity')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue()}
        </TableCell>
      ),
    }),
  ], [currentLocale]);

  const emptyTableMessage = {
    id: 'react.cycleCount.table.emptyTable.label',
    defaultMessage: 'No products match the given criteria',
  };

  const getFilterParams = () =>
    // Add filter params according to applied filters
    ({ format: 'csv' });

  const exportTableData = () => {
    // eslint-disable-next-line no-unused-vars
    const config = {
      params: getFilterParams(),
      paramsSerializer: (parameters) => queryString.stringify(parameters),
    };
    // replace with appropriate API call (+pass config as a parameter,
    // remove line disabling eslint rule)
    const data = cycleCountMockedData.csvData;
    const date = new Date();
    const [month, day, year] = [date.getMonth(), date.getDate(), date.getFullYear()];
    const [hour, minutes, seconds] = [date.getHours(), date.getMinutes(), date.getSeconds()];
    fileDownload(`\uFEFF${data}`, `CycleCountReport-${currentLocation?.name}-${year}${month}${day}-${hour}${minutes}${seconds}`, 'text/csv');
  };

  return {
    columns,
    tableData,
    loading,
    emptyTableMessage,
    exportTableData,
  };
};

export default useAllProductsTab;
