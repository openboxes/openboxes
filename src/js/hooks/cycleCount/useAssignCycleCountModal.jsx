/* eslint-disable no-restricted-syntax */
/* eslint-disable no-await-in-loop */

import React, { useCallback, useEffect } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { useSelector } from 'react-redux';
import {
  getCurrentLocation,
  getCycleCountsIds,
  getDebounceTime,
  getMinSearchLength,
} from 'selectors';

import cycleCountApi from 'api/services/CycleCountApi';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import notification from 'components/Layout/notifications/notification';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import CountIndex from 'consts/countIndex';
import cycleCountColumn from 'consts/cycleCountColumn';
import NotificationType from 'consts/notificationTypes';
import { DateFormat } from 'consts/timeFormat';
import useForceRender from 'hooks/useForceRender';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import dateWithoutTimeZone from 'utils/dateUtils';
import { debouncePeopleFetch } from 'utils/option-utils';

const useAssignCycleCountModal = ({
  selectedCycleCounts,
  isRecount,
  refetchData,
  closeModal,
  assignDataDirectly,
}) => {
  const spinner = useSpinner();
  const {
    currentLocation,
    debounceTime,
    minSearchLength,
    cycleCountIds,
  } = useSelector((state) => ({
    debounceTime: getDebounceTime(state),
    minSearchLength: getMinSearchLength(state),
    currentLocation: getCurrentLocation(state),
    cycleCountIds: getCycleCountsIds(state),
  }));
  const debouncedPeopleFetch = useCallback(
    debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );
  const translate = useTranslate();
  const columnHelper = createColumnHelper();
  const { forceRerender } = useForceRender();

  const handleUpdateAssignees = (cycleCountRequestIds, field, value) => {
    // eslint-disable-next-line no-param-reassign
    selectedCycleCounts.current = selectedCycleCounts.current.map((item) =>
      (cycleCountRequestIds.includes(item.cycleCountRequestId)
        ? { ...item, [field]: value }
        : item));
  };

  const getCycleCountItemsWithAssignedCountData = (cycleCounts, assigneeData) => {
    const mappedData = cycleCounts.map((cycleCount) => {
      const dataToAssign = assigneeData.find((data) =>
        data.cycleCountRequest === cycleCount.requestId)?.assignments;
      const cycleCountItems = cycleCount.cycleCountItems.filter((item) =>
        item.countIndex === cycleCount.maxCountIndex);
      return cycleCountItems.map((item) => ({
        id: item.id,
        recount: true,
        assignee: dataToAssign?.[cycleCount.maxCountIndex]?.assignee,
      }));
    });
    return _.flatten(mappedData);
  };

  const handleAssign = async () => {
    try {
      spinner.show();
      const commands = selectedCycleCounts.current.map((item) => {
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

      const response = await cycleCountApi.updateCycleCountRequests(
        currentLocation?.id,
        {
          commands,
        },
      );

      if (assignDataDirectly) {
        const { data } = await cycleCountApi.getCycleCounts(
          currentLocation?.id,
          cycleCountIds,
        );
        const countData = getCycleCountItemsWithAssignedCountData(data.data, commands);
        for (const cycleCount of cycleCountIds) {
          await cycleCountApi
            .updateCycleCountItems({
              itemsToUpdate: countData,
            }, currentLocation?.id, cycleCount);
        }
      }
      if (response.status === 200) {
        notification(NotificationType.SUCCESS)({
          message: translate(
            'react.cycleCount.assignSuccessfully.label',
            'Products were assigned successfully',
          ),
        });
      }

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
          <SelectField
            placeholder={translate('react.cycleCount.table.assignee.label', 'Assignee')}
            async
            loadOptions={debouncedPeopleFetch}
            onChange={(selectedOption) => {
              handleUpdateAssignees(
                selectedCycleCounts.current.map((item) => item.cycleCountRequestId),
                cycleCountColumn.ASSIGNEE,
                selectedOption,
              );
              // Force a re-render so that all rows are updated to display the selected value
              forceRerender();
            }}
          />
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
                [row.original.cycleCountRequestId],
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
          <DateField
            className="date-counted-date-picker date-field-input"
            placeholder={translate('react.cycleCount.table.deadline.label', 'Deadline')}
            clearable
            customDateFormat={DateFormat.DD_MMM_YYYY}
            onChange={(newDate) => {
              handleUpdateAssignees(
                selectedCycleCounts.current.map((item) => item.cycleCountRequestId),
                cycleCountColumn.DEADLINE,
                newDate,
              );
              // Force a re-render so that all rows are updated to display the selected value
              forceRerender();
            }}
          />
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
            onChange={(newDate) => {
              handleUpdateAssignees(
                [row.original.cycleCountRequestId],
                cycleCountColumn.DEADLINE,
                newDate,
              );
              // Rerender to display date in date field
              forceRerender();
            }}
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
