import React, { useCallback, useEffect } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { useSelector } from 'react-redux';
import { getCurrentLocation, getDebounceTime, getMinSearchLength } from 'selectors';

import cycleCountApi from 'api/services/CycleCountApi';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import CountIndex from 'consts/countIndex';
import cycleCountColumn from 'consts/cycleCountColumn';
import { DateFormat } from 'consts/timeFormat';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import dateWithoutTimeZone from 'utils/dateUtils';
import { debouncePeopleFetch } from 'utils/option-utils';

const useAssignCycleCountModal = ({
  selectedCycleCounts,
  setSelectedCycleCounts,
  isRecount,
  refetchData,
  closeModal,
}) => {
  const spinner = useSpinner();
  const {
    currentLocation,
    debounceTime,
    minSearchLength,
  } = useSelector((state) => ({
    debounceTime: getDebounceTime(state),
    minSearchLength: getMinSearchLength(state),
    currentLocation: getCurrentLocation(state),
  }));
  const debouncedPeopleFetch = useCallback(
    debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );
  const translate = useTranslate();
  const columnHelper = createColumnHelper();

  const handleUpdateAssignees = (cycleCountRequestId, field, value) => {
    setSelectedCycleCounts((prevItems) =>
      prevItems.map((item) =>
        (item.cycleCountRequestId === cycleCountRequestId
          ? { ...item, [field]: value }
          : item)));
  };

  const handleAssign = async () => {
    try {
      spinner.show();
      const commands = selectedCycleCounts.map((item) => {
        const { cycleCountRequestId, assignee, deadline } = item;

        const assignments = isRecount
          ? {
            [CountIndex.RECOUNT_INDEX]: {
              assignee: assignee?.id,
              deadline: dateWithoutTimeZone({
                date: deadline,
              }),
            },
          }
          : {
            [CountIndex.COUNT_INDEX]: {
              assignee: assignee?.id,
              deadline: dateWithoutTimeZone({
                date: deadline,
              }),
            },
          };

        return {
          assignments,
          cycleCountRequest: cycleCountRequestId,
        };
      });

      await cycleCountApi.updateCycleCountRequests(
        currentLocation?.id,
        {
          commands,
        },
      );

      if (refetchData) {
        refetchData();
      }
    } finally {
      closeModal();
      spinner.hide();
    }
  };

  useEffect(() => {
    document.body.style.overflowY = 'hidden';
  }, []);

  const columns = [
    columnHelper.accessor(
      (row) => `${row.product.productCode} ${row.product.name}`,
      {
        id: cycleCountColumn.PRODUCT,
        header: () => (
          <TableHeaderCell>
            {translate('react.cycleCount.table.products.label', 'Products')}
          </TableHeaderCell>
        ),
        cell: ({ getValue, row }) => (
          <TableCell
            customTooltip
            tooltipLabel={getValue()}
            link={INVENTORY_ITEM_URL.showStockCard(row.original.product.id)}
            className="rt-td multiline-cell"
          >
            <div className="limit-lines-2">{getValue()}</div>
          </TableCell>
        ),
        size: 250,
      },
    ),
    columnHelper.accessor(cycleCountColumn.ASSIGNEE, {
      id: cycleCountColumn.ASSIGNEE,
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.assignee.label', 'Assignee')}
        </TableHeaderCell>
      ),
      cell: ({ getValue, row }) => (
        <TableCell className="rt-td">
          <SelectField
            placeholder={translate('react.cycleCount.selectAnAssignee.placeholder.label', 'Select an assignee')}
            async
            loadOptions={debouncedPeopleFetch}
            defaultValue={getValue()}
            onChange={(selectedOption) =>
              handleUpdateAssignees(
                row.original.cycleCountRequestId,
                cycleCountColumn.ASSIGNEE,
                selectedOption,
              )}
          />
        </TableCell>
      ),
      size: 150,
    }),
    columnHelper.accessor(cycleCountColumn.DEADLINE, {
      id: cycleCountColumn.DEADLINE,
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.deadline.label', 'Deadline')}
        </TableHeaderCell>
      ),
      cell: ({ getValue, row }) => (
        <TableCell className="rt-td">
          <DateField
            className="date-counted-date-picker date-field-input"
            placeholder={translate('react.default.dateInput.placeholder.label', 'Select a date')}
            value={getValue()}
            clearable
            customDateFormat={DateFormat.DD_MMM_YYYY}
            onChange={(newDate) =>
              handleUpdateAssignees(
                row.original.cycleCountRequestId,
                cycleCountColumn.DEADLINE,
                newDate,
              )}
          />
        </TableCell>
      ),
      size: 150,
    }),
    columnHelper.accessor(cycleCountColumn.INVENTORY_ITEMS_COUNT, {
      id: cycleCountColumn.INVENTORY_ITEMS_COUNT,
      header: () => (
        <TableHeaderCell>
          #
          {' '}
          {translate('react.cycleCount.table.inventoryItems.label', 'Inventory Items')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td text-center">{getValue()}</TableCell>
      ),
      size: 100,
    }),
  ];
  return {
    handleAssign,
    handleUpdateAssignees,
    columns,
  };
};

export default useAssignCycleCountModal;
