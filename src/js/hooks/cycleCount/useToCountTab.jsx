import React, { useMemo, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import fileDownload from 'js-file-download';
import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import {
  getCurrentLocale,
  getCurrentLocation,
  getCycleCountMaxSelectedProducts,
  getCycleCountTranslations,
  getFormatLocalizedDate,
} from 'selectors';

import { startCount } from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT_CANDIDATES, CYCLE_COUNT_PENDING_REQUESTS } from 'api/urls';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import Checkbox from 'components/form-elements/v2/Checkbox';
import { CYCLE_COUNT, INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import CycleCountCandidateStatus from 'consts/cycleCountCandidateStatus';
import cycleCountColumn from 'consts/cycleCountColumn';
import MimeType from 'consts/mimeType';
import { DateFormat } from 'consts/timeFormat';
import useCycleCountProductAvailability from 'hooks/cycleCount/useCycleCountProductAvailability';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';
import useTableDataV2 from 'hooks/useTableDataV2';
import useTableSorting from 'hooks/useTableSorting';
import useThrowError from 'hooks/useThrowError';
import useTranslate from 'hooks/useTranslate';
import Badge from 'utils/Badge';
import confirmationModal from 'utils/confirmationModalUtils';
import dateWithoutTimeZone from 'utils/dateUtils';
import exportFileFromAPI, { extractFilenameFromHeader } from 'utils/file-download-util';
import { mapStringToLimitedList } from 'utils/form-values-utils';
import StatusIndicator from 'utils/StatusIndicator';

const useToCountTab = ({
  filterParams,
  offset,
  pageSize,
  toCountTabCheckboxes,
  serializedParams,
}) => {
  const [assignCountModalData, setAssignCountModalData] = useState([]);
  const [isAssignCountModalOpen, setIsAssignCountModalOpen] = useState(false);
  const columnHelper = createColumnHelper();
  const translate = useTranslate();
  const spinner = useSpinner();
  const history = useHistory();
  const { tab } = useQueryParams();

  const {
    currentLocale,
    currentLocation,
    cycleCountMaxSelectedProducts,
    translationsFetched,
    formatLocalizedDate,
  } = useSelector((state) => ({
    currentLocale: getCurrentLocale(state),
    currentLocation: getCurrentLocation(state),
    cycleCountMaxSelectedProducts: getCycleCountMaxSelectedProducts(state),
    translationsFetched: getCycleCountTranslations(state),
    formatLocalizedDate: getFormatLocalizedDate(state),
  }));

  const dispatch = useDispatch();

  const {
    dateLastCount,
    categories,
    internalLocations,
    tags,
    catalogs,
    negativeQuantity,
    searchTerm,
  } = filterParams;

  const {
    selectRow,
    isChecked,
    selectHeaderCheckbox,
    headerCheckboxProps,
    checkedCheckboxes,
    resetCheckboxes,
  } = toCountTabCheckboxes;

  const getParams = ({
    sortingParams,
  }) => _.omitBy({
    statuses: [
      CycleCountCandidateStatus.CREATED,
      CycleCountCandidateStatus.REQUESTED,
      CycleCountCandidateStatus.COUNTING,
    ],
    offset: `${offset}`,
    max: `${pageSize}`,
    ...sortingParams,
    ...filterParams,
    searchTerm,
    facility: currentLocation?.id,
    dateLastCount: dateWithoutTimeZone({
      date: dateLastCount,
    }),
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
    tableData,
    loading,
    fetchData,
  } = useTableDataV2({
    url: CYCLE_COUNT_PENDING_REQUESTS(currentLocation?.id),
    errorMessageId: 'react.cycleCount.table.errorMessage.label',
    defaultErrorMessage: 'Unable to fetch products',
    shouldFetch: filterParams.tab && tab === filterParams.tab,
    getParams,
    pageSize,
    sort,
    order,
    serializedParams,
  });

  const cancelCounts = async (id) => {
    try {
      spinner.show();
      await cycleCountApi.deleteRequests(currentLocation?.id, id || checkedCheckboxes);
    } finally {
      spinner.hide();
      fetchData();
      resetCheckboxes();
    }
  };

  const cancelCountsModalButtons = (id) => (onClose) => ([
    {
      variant: 'transparent',
      defaultLabel: 'Back',
      label: 'react.default.button.back.label',
      onClick: () => {
        onClose?.();
      },
    },
    {
      variant: 'primary',
      defaultLabel: 'Confirm',
      label: 'react.default.button.confirm.label',
      onClick: async () => {
        onClose?.();
        await cancelCounts(id);
      },
    },
  ]);

  const openCancelCountsModal = ({ id }) => {
    confirmationModal({
      hideCloseButton: false,
      closeOnClickOutside: true,
      buttons: cancelCountsModalButtons(id),
      title: {
        label: 'react.cycleCount.modal.cancelCounts.title.label',
        default: 'Cancel Counts?',
      },
      content: {
        label: 'react.cycleCount.modal.cancelCounts.content.label',
        default: 'The Cycle Count will be canceled for the products selected. Your products will be removed from the To Count tab and brought back to Cycle Count All Products list. If you have started a count on these products, it will be erased. Choose this option if you want to abandon the cycle count. Are you sure you want to Cancel?',
      },
    });
  };

  const extendedDataTable = useMemo(() => {
    if (!tableData.data) {
      return tableData;
    }
    return {
      ...tableData,
      data: tableData.data.map((row) => ({
        ...row,
        meta: useCycleCountProductAvailability(row),
      })),
    };
  }, [tableData]);

  const getCycleCountRequestsIds = () => extendedDataTable.data
    .filter((row) => !row.meta.showCancelCheckbox)
    .map((row) => row.cycleCountRequest.id);

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
    cell: ({ row }) => {
      const { showCancelCheckbox } = row.original.meta;
      return (
        <TableCell className="rt-td">
          <Checkbox
            noWrapper
            onChange={showCancelCheckbox
              ? null
              : selectRow(row.original.cycleCountRequest.id)}
            value={isChecked(row.original.cycleCountRequest.id)}
            className={`${showCancelCheckbox && 'cancel-icon'}`}
            onClick={() => {
              if (showCancelCheckbox) {
                openCancelCountsModal({ id: row.original.cycleCountRequest.id });
              }
            }}
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

  const columns = useMemo(() => [
    columnHelper.accessor(cycleCountColumn.STATUS, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.status.label', 'Status')}
        </TableHeaderCell>
      ),
      cell: ({ getValue, row }) => (
        <TableCell className="rt-td">
          <StatusIndicator
            variant={row.original.meta.isRowDisabled ? 'gray' : 'danger'}
            status={translate(`react.cycleCount.CycleCountCandidateStatus.${getValue()}.label`, 'To count')}
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
        flexWidth: 150,
      },
    }),
    columnHelper.accessor(cycleCountColumn.INITIAL_COUNT_ASSIGNEE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.assignee.label', 'Assignee')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => {
        const value = getValue();
        return (
          <TableCell className="rt-td badge-container">
            <Badge
              label={value?.name?.toString()}
              variant="badge--purple"
              tooltip
            />
          </TableCell>
        );
      },
      meta: {
        flexWidth: 150,
      },
    }),
    columnHelper.accessor(cycleCountColumn.INITIAL_COUNT_DEADLINE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.deadline.label', 'Deadline')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => {
        const date = formatLocalizedDate(getValue(), DateFormat.DD_MMM_YYYY);
        return (
          <TableCell className="rt-td badge-container">
            <Badge
              label={date}
              variant="badge--blue"
              tooltip
            />
          </TableCell>
        );
      },
      meta: {
        flexWidth: 150,
      },
    }),
    columnHelper.accessor(cycleCountColumn.INVENTORY_ITEMS, {
      header: () => (
        <TableHeaderCell>
          #
          {' '}
          {translate('react.cycleCount.table.inventoryItems.label', 'Inventory Items')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td">
          {getValue()?.toString()}
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
  ], [currentLocale, sort, order, translationsFetched]);

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

  const printCountForm = async (format) => {
    spinner.show();
    const payload = {
      requests: checkedCheckboxes.map((cycleCountRequestId) => ({
        cycleCountRequest: cycleCountRequestId,
      })),
    };
    const response = await cycleCountApi.startCount({
      payload,
      locationId: currentLocation?.id,
      format,
      config: { responseType: 'blob' },
    });
    const filename = extractFilenameFromHeader(response.headers['content-disposition']);
    fileDownload(response.data, filename, MimeType[format]);
    spinner.hide();
  };

  const moveToCounting = async () => {
    const payload = {
      requests: checkedCheckboxes.map((cycleCountRequestId) => ({
        cycleCountRequest: cycleCountRequestId,
      })),
    };
    spinner.show();
    try {
      await dispatch(startCount(payload, currentLocation?.id));
      history.push(CYCLE_COUNT.countStep());
    } finally {
      spinner.hide();
    }
  };

  const { verifyCondition } = useThrowError({
    condition: checkedCheckboxes.length <= cycleCountMaxSelectedProducts,
    callWhenValid: moveToCounting,
    errorMessageLabel: 'react.cycleCount.selectedMoreThanAllowed.error',
    errorMessageDefault: `Sorry, we cannot support counting more than ${cycleCountMaxSelectedProducts} products at once at the moment.
     Please start counting fewer products and then continue on the remaining products.`,
    translateData: {
      maxProductsNumber: cycleCountMaxSelectedProducts,
    },
  });

  const mapSelectedRowsToModalData = async () => {
    spinner.show();
    const { data } = await cycleCountApi.getPendingRequests({
      locationId: currentLocation?.id,
      requestIds: checkedCheckboxes,
    });
    const modalData = data.data.map((pendingCycleCountRequest) => {
      const {
        cycleCountRequest: {
          initialCount: {
            assignee,
            deadline,
          },
          inventoryItemsCount,
          product,
          id,
        },
      } = pendingCycleCountRequest;
      return {
        cycleCountRequestId: id,
        product,
        assignee: assignee
          ? {
            ...assignee,
            // Properties for displaying already selected assignee as a default
            // value on the select field
            value: assignee?.id,
            label: assignee?.name,
          } : null,
        deadline,
        inventoryItemsCount,
      };
    });
    spinner.hide();
    setAssignCountModalData(modalData);
  };

  const closeAssignCountModal = () => {
    setIsAssignCountModalOpen(false);
    setAssignCountModalData([]);
  };

  const openAssignCountModal = async () => {
    await mapSelectedRowsToModalData();
    setIsAssignCountModalOpen(true);
  };

  return {
    tableData: extendedDataTable,
    loading,
    columns: [checkboxesColumn, ...columns],
    emptyTableMessage,
    exportTableData,
    moveToCounting: verifyCondition,
    printCountForm,
    openCancelCountsModal,
    openAssignCountModal,
    closeAssignCountModal,
    fetchData,
    isAssignCountModalOpen,
    assignCountModalData,
    setAssignCountModalData,
  };
};

export default useToCountTab;
