import React, { useCallback, useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { useSelector } from 'react-redux';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import { INVENTORY_ITEM_URL } from 'consts/applicationUrls';
import cycleCountColumn from 'consts/cycleCountColumn';
import { DateFormat } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';
import { debouncePeopleFetch } from 'utils/option-utils';

const useAssignModalTable = ({ onUpdate }) => {
  const translate = useTranslate();
  const {
    debounceTime,
    minSearchLength,
  } = useSelector((state) => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
  }));
  const debouncedPeopleFetch = useCallback(
    debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );

  const columnHelper = createColumnHelper();
  const columns = useMemo(
    () => [
      columnHelper.accessor((row) => `${row.product.productCode} ${row.product.name}`, {
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
      }),
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
              placeholder={translate('react.cycleCount.selectAssigneePlaceholder.label', 'Select Assignee')}
              async
              loadOptions={debouncedPeopleFetch}
              defaultValue={getValue()}
              onChange={(selectedOption) =>
                onUpdate(
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
              placeholder={translate('react.cycleCount.selectDate.label', 'Select Date')}
              value={getValue()}
              clearable
              customDateFormat={DateFormat.DD_MMM_YYYY}
              onChange={(newDate) =>
                onUpdate(row.original.cycleCountRequestId, cycleCountColumn.DEADLINE, newDate)}
            />
          </TableCell>
        ),
        size: 150,
      }),
      columnHelper.accessor(cycleCountColumn.INVENTORY_ITEMS, {
        id: cycleCountColumn.INVENTORY_ITEMS,
        header: () => (
          <TableHeaderCell>
            #
            {' '}
            {translate('react.cycleCount.table.inventoryItems.label', 'Inventory Items')}
          </TableHeaderCell>
        ),
        cell: ({ getValue }) => <TableCell className="rt-td text-center">{getValue()}</TableCell>,
        size: 100,
      }),
    ],
    [],
  );
  return {
    columns,
  };
};

export default useAssignModalTable;
