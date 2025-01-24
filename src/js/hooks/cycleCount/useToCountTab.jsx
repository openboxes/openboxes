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
import CycleCountStatus from 'consts/cycleCountStatus';
import useSpinner from 'hooks/useSpinner';
import useTableCheckboxes from 'hooks/useTableCheckboxes';
import useTableDataV2 from 'hooks/useTableDataV2';
import useTableSorting from 'hooks/useTableSorting';
import useTranslate from 'hooks/useTranslate';
import Badge from 'utils/Badge';
import exportFileFromAPI from 'utils/file-download-util';
import { mapStringToList } from 'utils/form-values-utils';
import StatusIndicator from 'utils/StatusIndicator';

const useToCountTab = ({
  filterParams,
  offset,
  pageSize,
}) => {
  const columnHelper = createColumnHelper();
  const translate = useTranslate();
  const spinner = useSpinner();

  const { currentLocale, currentLocation } = useSelector((state) => ({
    currentLocale: state.session.activeLanguage,
    currentLocation: state.session.currentLocation,
  }));

  const {
    dateLastCount,
    categories,
    internalLocations,
    tags,
    catalogs,
    negativeQuantity,
    searchTerm,
  } = filterParams;

  const getParams = ({
    sortingParams,
  }) => _.omitBy({
    status: CycleCountStatus.CREATED,
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
    abcClass: [],
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
    selectRow,
    isChecked,
    selectHeaderCheckbox,
    selectedCheckboxesAmount,
    headerCheckboxProps,
    checkedCheckboxes,
  } = useTableCheckboxes();

  const {
    tableData,
    loading,
  } = useTableDataV2({
    url: CYCLE_COUNT_CANDIDATES(currentLocation?.id),
    errorMessageId: 'react.cycleCount.table.errorMessage.label',
    defaultErrorMessage: 'Unable to fetch products',
    shouldFetch: filterParams.tab,
    getParams,
    pageSize,
    offset,
    sort,
    order,
    searchTerm,
    filterParams,
  });

  const getProductIds = () => tableData.data.map((row) => row.product.id);

  const checkboxesColumn = columnHelper.accessor('selected', {
    header: () => (
      <TableHeaderCell>
        <Checkbox
          noWrapper
          {...headerCheckboxProps}
          onClick={selectHeaderCheckbox(getProductIds)}
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
    },
  });

  const columns = useMemo(() => [
    columnHelper.accessor('status', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.status.label', 'Status')}
        </TableHeaderCell>
      ),
      cell: () => (
        // TODO: Use variant and status fetched from the API
        <TableCell className="rt-td">
          <StatusIndicator
            variant="danger"
            status={translate('react.cycleCount.toCount.status.label', 'To count')}
          />
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
      row?.tags?.map?.((tag) => <Badge label={tag?.tag} variant="badge--purple" key={tag.id} />), {
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
      row?.productCatalogs?.map((catalog) => <Badge label={catalog?.name} variant="badge--blue" key={catalog.id} />), {
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

  const printCountForm = () => {
    console.log('print count form pressed');
  };

  const startCount = async () => {
    const payload = {
      requests: checkedCheckboxes.map((productId) => ({
        product: productId,
      })),
    };
    spinner.show();
    try {
      // Mocked request for start counting
      await cycleCountApi.startCount(payload, currentLocation?.id);
    } finally {
      spinner.hide();
    }
  };

  return {
    tableData,
    loading,
    columns: [checkboxesColumn, ...columns],
    emptyTableMessage,
    exportTableData,
    selectedCheckboxesAmount,
    startCount,
    printCountForm,
  };
};

export default useToCountTab;
