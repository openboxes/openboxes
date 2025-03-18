import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import fileDownload from 'js-file-download';
import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { startResolution } from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT_CANDIDATES } from 'api/urls';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import Checkbox from 'components/form-elements/v2/Checkbox';
import { CYCLE_COUNT, INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import CycleCountCandidateStatus from 'consts/cycleCountCandidateStatus';
import cycleCountColumn from 'consts/cycleCountColumn';
import MimeType from 'consts/mimeType';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';
import useTableCheckboxes from 'hooks/useTableCheckboxes';
import useTableDataV2 from 'hooks/useTableDataV2';
import useTableSorting from 'hooks/useTableSorting';
import useTranslate from 'hooks/useTranslate';
import Badge from 'utils/Badge';
import exportFileFromAPI, { extractFilenameFromHeader } from 'utils/file-download-util';
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
  const { tab } = useQueryParams();

  const { currentLocale, currentLocation } = useSelector((state) => ({
    currentLocale: state.session.activeLanguage,
    currentLocation: state.session.currentLocation,
  }));

  const dispatch = useDispatch();

  const history = useHistory();

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
    statuses: [
      CycleCountCandidateStatus.COUNTED,
      CycleCountCandidateStatus.INVESTIGATING,
    ],
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
    shouldFetch: filterParams.tab && tab === filterParams.tab,
    getParams,
    pageSize,
    offset,
    sort,
    order,
    searchTerm,
    filterParams,
  });

  const getCycleCountRequestsIds = () => tableData.data.map((row) => row.cycleCountRequest.id);

  const checkboxesColumn = columnHelper.accessor(cycleCountColumn.SELECTED, {
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
    columnHelper.accessor(cycleCountColumn.STATUS, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.status.label', 'Status')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          <StatusIndicator
            variant="primary"
            status={translate(`react.cycleCount.CycleCountCandidateStatus.${getValue()}.label`, 'To resolve')}
          />
        </TableCell>
      ),
      meta: {
        flexWidth: 180,
      },
    }),
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
    columnHelper.accessor(cycleCountColumn.CATEGORY_NAME, {
      header: () => (
        <TableHeaderCell sortable columnId={cycleCountColumn.CATEGORY} {...sortableProps}>
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
    columnHelper.accessor(cycleCountColumn.INTERNAL_LOCATIONS, {
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
      id: cycleCountColumn.TAGS,
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
      id: cycleCountColumn.PRODUCT_CATALOGS,
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

  const printResolveForm = async (format) => {
    spinner.show();
    const payload = {
      requests: checkedCheckboxes.map((cycleCountRequestId) => ({
        cycleCountRequest: cycleCountRequestId,
        countIndex: 1, // We only ever allow for a single recount, so index is always 1.
      })),
    };
    const response = await cycleCountApi.startRecount({
      payload,
      locationId: currentLocation?.id,
      format,
      config: { responseType: 'blob' },
    });
    const filename = extractFilenameFromHeader(response.headers['content-disposition']);
    fileDownload(response.data, filename, MimeType[format]);
    spinner.hide();
  };

  const moveToResolving = async () => {
    spinner.show();
    try {
      await dispatch(startResolution(checkedCheckboxes, currentLocation?.id));
      history.push(CYCLE_COUNT.resolveStep());
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
    moveToResolving,
    printResolveForm,
  };
};

export default useToResolveTab;
