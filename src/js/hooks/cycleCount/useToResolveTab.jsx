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
import { mapStringToLimitedList } from 'utils/form-values-utils';
import StatusIndicator from 'utils/StatusIndicator';

const useToResolveTab = ({
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
    // TODO: We need to display only the rows where cycle count in [INVESTIGATING, COUNTED].
    //       This will require us to be able to filter on the cycle count status, not just the cycle
    //       count request status! https://pihemr.atlassian.net/browse/OBPIH-6931
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

  const getCycleCountRequestsIds = () => tableData.data.map((row) => row.cycleCountRequest.id);

  const checkboxesColumn = columnHelper.accessor('selected', {
    header: () => (
      <TableHeaderCell>
        <Checkbox
          noWrapper
          {...headerCheckboxProps}
          onClick={selectHeaderCheckbox(getCycleCountRequestsIds)}
        />
      </TableHeaderCell>
    ),
    cell: ({ row }) => (
      <TableCell className="rt-td">
        <Checkbox
          noWrapper
          onChange={selectRow(row.original.cycleCountRequest.id)}
          value={isChecked(row.original.cycleCountRequest.id)}
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
    columnHelper.accessor('cycleCountRequest.cycleCount.status', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.status.label', 'Status')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        // TODO: Use variant fetched from the API https://pihemr.atlassian.net/browse/OBPIH-6931
        <TableCell className="rt-td">
          <StatusIndicator
            variant="primary"
            status={translate(`react.cycleCount.status.${getValue()}.label`, 'To Resolve')}
          />
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
        <TableCell className="rt-td multiline-cell">
          {getValue()}
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
      meta: {
        flexWidth: 200,
      },
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

  const printResolveForm = () => {
    // TODO: implement me!
    console.log('print resolve form pressed');
  };

  const startResolution = async () => {
    const payload = {
      requests: checkedCheckboxes.map((cycleCountRequestId) => ({
        cycleCountRequest: cycleCountRequestId,
        countIndex: 1, // We only ever allow for a single recount, so index is always 1.
      })),
    };
    spinner.show();
    try {
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
    startResolution,
    printResolveForm,
  };
};

export default useToResolveTab;
