import React, { useEffect, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { RiDeleteBinLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import { DateFormat } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';
import { fetchBins } from 'utils/option-utils';

// Managing state for single table, mainly table configuration (from count step)
const useCountStepTable = ({
  cycleCountId,
  removeRow,
  validationErrors,
  tableData,
  isEditableStep,
  formatLocalizedDate,
}) => {
  const columnHelper = createColumnHelper();
  // State for saving data for binLocation dropdown
  const [binLocations, setBinLocations] = useState([]);

  const translate = useTranslate();

  const { recipients, currentLocation } = useSelector((state) => ({
    recipients: state.users.data,
    currentLocation: state.session.currentLocation,
  }));

  useEffect(() => {
    (async () => {
      const fetchedBins = await fetchBins(currentLocation?.id);
      setBinLocations(fetchedBins);
    })();
  }, [currentLocation?.id]);

  // Get appropriate input component based on table column
  const getFieldComponent = (fieldName) => {
    if (fieldName === 'inventoryItem_expirationDate') {
      return DateField;
    }

    if (fieldName === 'binLocation') {
      return SelectField;
    }

    return TextInput;
  };

  // Get text input type: quantityCounted expects a number,
  // the rest of the inputs should be text
  const getFieldType = (fieldName) => {
    if (fieldName === 'quantityCounted') {
      return 'number';
    }

    return 'text';
  };

  // Get field props, for the binLocation dropdown we have to pass options
  const getFieldProps = (fieldName) => {
    if (fieldName === 'binLocation') {
      const groupedByZone = binLocations.reduce((acc, bin) => {
        const { zoneId, zoneName } = bin;

        return {
          ...acc,
          [zoneId]: {
            zoneId,
            zoneName,
            bins: [...(acc[zoneId]?.bins || []), {
              id: bin.id,
              name: bin.name,
              label: bin.name,
              value: bin.id,
            }],
          },
        };
      }, {});

      const groupedOptions = Object.values(groupedByZone);

      return {
        labelKey: 'name',
        options: groupedOptions.map((group) => ({
          id: `zone-${group.zoneId}`,
          name: group.zoneName,
          label: (
            <span className="zone-label">
              {group.zoneName}
            </span>
          ),
          isDisabled: true,
          options: group.bins,
        })),
      };
    }

    return {};
  };

  // this function is required because there is a problem w getValue
  const getValueToDisplay = (id, value) => {
    let valueToDisplay = null;

    if (id === 'quantityCounted') {
      valueToDisplay = value === 0 ? '0' : value;
    } else if (id === 'inventoryItem_expirationDate') {
      valueToDisplay = formatLocalizedDate(value, DateFormat.DD_MMM_YYYY);
    } else if (id === 'inventoryItem_lotNumber' || id === 'comment') {
      valueToDisplay = value;
    }
    if (id === 'binLocation') {
      valueToDisplay = value?.name;
    }

    return valueToDisplay;
  };

  const defaultColumn = {
    cell: ({
      getValue, row: { original, index }, column: { id }, table,
    }) => {
      const isFieldEditable = !original.id.includes('newRow') && id !== 'quantityCounted';
      // We shouldn't allow users edit fetched data (only quantity counted is editable)

      // Keep and update the state of the cell during rerenders
      const columnPath = id.replaceAll('_', '.');
      const initialValue = _.get(tableData, `[${index}].${columnPath}`);

      const [value, setValue] = useState(initialValue);

      if (isFieldEditable || !isEditableStep) {
        const valueToDisplay = getValueToDisplay(id, value);
        return (
          <TableCell className="static-cell-count-step">
            {valueToDisplay || getValue()}
          </TableCell>
        );
      }

      const errorMessage = validationErrors?.[cycleCountId]?.errors?.[index]?.[columnPath]?._errors;
      const [error, setError] = useState(errorMessage);

      // If the value at the end of entering data is the same as it was initially,
      // we don't want to trigger rerender
      const isEdited = initialValue !== value;
      // When the input is blurred, we'll call the table meta's updateData function
      const onBlur = () => {
        if (isEdited) {
          table.options.meta?.updateData(cycleCountId, original.id, id, value);
          setError(null);
        }
      };

      // on change function expects e.target.value for text fields,
      // in other cases it expects just the value
      const onChange = (e) => {
        setValue(e?.target?.value ?? e);
      };

      // Table consists of text fields, one numerical field for quantity counted,
      // select field for bin locations and one date picker for the expiration date.
      const type = getFieldType(id);
      const Component = getFieldComponent(id);
      const fieldProps = getFieldProps(id);
      return (
        <TableCell
          className="rt-td rt-td-count-step pb-0"
          tooltip={id === 'binLocation'}
          tooltipForm={id === 'binLocation'}
          customTooltipStyles="bin-location-tooltip"
          tooltipLabel={value?.name || translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
        >
          <Component
            type={type}
            value={value}
            onChange={onChange}
            onBlur={onBlur}
            className={`m-1 ${id === 'binLocation' ? 'w-99' : 'w-75'}`}
            errorMessage={error}
            {...fieldProps}
          />
        </TableCell>
      );
    },
  };

  const columns = [
    columnHelper.accessor(
      (row) => row?.binLocation?.label || row?.binLocation?.name, {
        id: 'binLocation',
        header: () => (
          <TableHeaderCell>
            {translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
          </TableHeaderCell>
        ),
      },
    ),
    columnHelper.accessor('inventoryItem.lotNumber', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.lotNumber.label', 'Serial / Lot Number')}
        </TableHeaderCell>
      ),
    }),
    columnHelper.accessor('inventoryItem.expirationDate', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.expirationDate.label', 'Expiration Date')}
        </TableHeaderCell>
      ),
      meta: {
        getCellContext: () => ({
          className: 'split-table-right',
        }),
      },
    }),
    columnHelper.accessor('quantityCounted', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.quantityCounted.label', 'Quantity Counted')}
        </TableHeaderCell>
      ),
    }),
    columnHelper.accessor('comment', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.comment.label', 'Comment')}
        </TableHeaderCell>
      ),
    }),
    columnHelper.accessor(null, {
      id: 'actions',
      header: () => <TableHeaderCell className="count-step-actions" />,
      cell: ({ row: { original } }) => (
        <TableCell className="rt-td d-flex justify-content-center count-step-actions">
          <Tooltip
            arrow="true"
            delay="150"
            duration="250"
            hideDelay="50"
            className="text-overflow-ellipsis"
            html={(
              <span className="p-2">
                {translate('react.default.button.delete.label', 'Delete')}
              </span>
            )}
            disabled={original.id}
          >
            {original.id.includes('newRow') && (
              <RiDeleteBinLine
                onClick={() => removeRow(cycleCountId, original.id)}
                size={22}
              />
            )}
          </Tooltip>
        </TableCell>
      ),
      meta: {
        getCellContext: () => ({
          className: 'count-step-actions',
        }),
      },
    }),
  ];

  return {
    columns,
    defaultColumn,
    recipients,
  };
};

export default useCountStepTable;
