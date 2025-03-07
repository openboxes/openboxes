import React, { useEffect, useMemo, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { RiDeleteBinLine, RiErrorWarningLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import cycleCountColumn from 'consts/cycleCountColumn';
import { DateFormat } from 'consts/timeFormat';
import useArrowsNavigation from 'hooks/useArrowsNavigation';
import useTranslate from 'hooks/useTranslate';
import groupBinLocationsByZone from 'utils/groupBinLocationsByZone';
import { fetchBins } from 'utils/option-utils';
import { checkBinLocationSupport } from 'utils/supportedActivitiesUtils';
import CustomTooltip from 'wrappers/CustomTooltip';

// Managing state for single table, mainly table configuration (from count step)
const useCountStepTable = ({
  cycleCountId,
  productCode,
  removeRow,
  validationErrors,
  tableData,
  isStepEditable,
  formatLocalizedDate,
  addEmptyRow,
}) => {
  const columnHelper = createColumnHelper();
  // State for saving data for binLocation dropdown
  const [binLocations, setBinLocations] = useState([]);
  const [focusIndex, setFocusIndex] = useState(null);
  const [focusId, setFocusId] = useState(null);

  const translate = useTranslate();

  const { users, currentLocation } = useSelector((state) => ({
    users: state.users.data,
    currentLocation: state.session.currentLocation,
  }));

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocation.supportedActivities), [currentLocation?.id]);

  useEffect(() => {
    if (showBinLocation) {
      (async () => {
        const fetchedBins = await fetchBins(currentLocation?.id, []);
        setBinLocations(fetchedBins);
      })();
    }
  }, [currentLocation?.id]);

  // Get appropriate input component based on table column
  const getFieldComponent = (fieldName) => {
    if (fieldName === cycleCountColumn.EXPIRATION_DATE) {
      return DateField;
    }

    if (fieldName === cycleCountColumn.BIN_LOCATION) {
      return SelectField;
    }

    return TextInput;
  };

  // Get text input type: quantityCounted expects a number,
  // the rest of the inputs should be text
  const getFieldType = (fieldName) => {
    if (fieldName === cycleCountColumn.QUANTITY_COUNTED) {
      return 'number';
    }

    return 'text';
  };

  // Get field props, for the binLocation dropdown we have to pass options
  const getFieldProps = (fieldName) => {
    if (fieldName === cycleCountColumn.BIN_LOCATION && showBinLocation) {
      return {
        labelKey: 'name',
        options: groupBinLocationsByZone(binLocations),
      };
    }

    if (fieldName === cycleCountColumn.EXPIRATION_DATE) {
      return {
        customDateFormat: DateFormat.DD_MMM_YYYY,
      };
    }

    return {};
  };

  // this function is required because there is a problem with getValue
  const getValueToDisplay = (id, value) => {
    const columnPath = id.replaceAll('_', '.');
    if (columnPath === cycleCountColumn.EXPIRATION_DATE) {
      return formatLocalizedDate(value, DateFormat.DD_MMM_YYYY);
    }

    if (columnPath === cycleCountColumn.QUANTITY_COUNTED) {
      return value?.toString();
    }

    if (columnPath === cycleCountColumn.BIN_LOCATION && showBinLocation) {
      return value?.name;
    }

    return value;
  };

  const resetFocus = () => {
    setFocusIndex(null);
    setFocusId(null);
  };

  const defaultColumn = {
    cell: ({
      row: { original, index }, column: { id }, table,
    }) => {
      const columnPath = id.replaceAll('_', '.');
      const initialValue = _.get(tableData, `[${index}].${columnPath}`);
      const [value, setValue] = useState(initialValue);

      const isFieldEditable = !original.id.includes('newRow') && ![
        cycleCountColumn.QUANTITY_COUNTED,
        cycleCountColumn.COMMENT,
      ].includes(id);
      // We shouldn't allow users edit fetched data (only quantity counted and comment are editable)
      if (isFieldEditable || !isStepEditable) {
        return (
          <TableCell className="static-cell-count-step d-flex align-items-center">
            {getValueToDisplay(id, value)}
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
        const enteredValue = e?.target?.value ?? e;
        table.options.meta?.updateData(cycleCountId, original.id, id, enteredValue);
        setValue(enteredValue);
      };

      const onChangeRaw = (e) => {
        const valueToUpdate = (e?.target?.value ?? e)?.format();
        setValue(valueToUpdate);
      };

      // Table consists of text fields, one numerical field for quantity counted,
      // select field for bin locations and one date picker for the expiration date.
      const type = getFieldType(columnPath);
      const Component = getFieldComponent(columnPath);
      const fieldProps = getFieldProps(columnPath);
      const showTooltip = columnPath === cycleCountColumn.BIN_LOCATION;

      // Columns allowed for focus in new rows
      const newRowFocusableCells = [
        cycleCountColumn.LOT_NUMBER,
        cycleCountColumn.EXPIRATION_DATE,
        cycleCountColumn.QUANTITY_COUNTED,
        cycleCountColumn.COMMENT,
      ];

      if (showBinLocation) {
        newRowFocusableCells.splice(0, 0, cycleCountColumn.BIN_LOCATION);
      }

      // Columns allowed for focus in existing rows
      const existingRowFocusableCells = [
        cycleCountColumn.QUANTITY_COUNTED,
        cycleCountColumn.COMMENT,
      ];

      // Checks if the row is a new one (i.e., added by user and contains 'newRow' in id),
      // and if yes, allow navigation through `newRowFocusableCells`.
      const isNewRow = (row) => row?.id?.includes('newRow');

      const { handleKeyDown } = useArrowsNavigation({
        newRowFocusableCells,
        existingRowFocusableCells,
        tableData,
        setFocusId,
        setFocusIndex,
        addNewRow: () => addEmptyRow(productCode, cycleCountId),
        isNewRow,
      });

      return (
        <TableCell
          className="rt-td rt-td-count-step pb-0"
          tooltip={showTooltip}
          tooltipForm={showTooltip}
          tooltipClassname={showTooltip && 'bin-location-tooltip'}
          tooltipLabel={value?.name || translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
        >
          <Component
            type={type}
            value={value}
            onChange={onChange}
            onChangeRaw={onChangeRaw}
            onBlur={onBlur}
            className={`m-1 hide-arrows ${showTooltip ? 'w-99' : 'w-75'} ${error && 'border border-danger input-has-error'}`}
            showErrorBorder={error}
            hideErrorMessageWrapper
            onKeyDown={(e) => handleKeyDown(e, index, columnPath)}
            focusProps={{
              fieldIndex: index,
              fieldId: columnPath,
              focusIndex,
              focusId,
            }}
            {...fieldProps}
          />
          {error && (
            <CustomTooltip
              content={error}
              className="tooltip-icon tooltip-icon--error"
              icon={RiErrorWarningLine}
            />
          )}
        </TableCell>
      );
    },
  };

  const columns = [
    columnHelper.accessor(
      (row) => (row?.binLocation?.label ? row?.binLocation : row.binLocation?.name), {
        id: cycleCountColumn.BIN_LOCATION,
        header: () => (
          <TableHeaderCell className="rt-th-count-step">
            {translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
          </TableHeaderCell>
        ),
        meta: {
          flexWidth: 100,
          hide: !showBinLocation,
        },
      },
    ),
    columnHelper.accessor(cycleCountColumn.LOT_NUMBER, {
      header: () => (
        <TableHeaderCell className="rt-th-count-step">
          {translate('react.cycleCount.table.lotNumber.label', 'Serial / Lot Number')}
        </TableHeaderCell>
      ),
      meta: {
        flexWidth: 100,
      },
    }),
    columnHelper.accessor(cycleCountColumn.EXPIRATION_DATE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.expirationDate.label', 'Expiration Date')}
        </TableHeaderCell>
      ),
      meta: {
        flexWidth: 100,
        getCellContext: () => ({
          className: 'split-table-right',
        }),
      },
    }),
    columnHelper.accessor(cycleCountColumn.QUANTITY_COUNTED, {
      header: () => (
        <TableHeaderCell className="rt-th-count-step">
          {translate('react.cycleCount.table.quantityCounted.label', 'Quantity Counted')}
        </TableHeaderCell>
      ),
      meta: {
        flexWidth: 50,
      },
    }),
    columnHelper.accessor(cycleCountColumn.COMMENT, {
      header: () => (
        <TableHeaderCell className="rt-th-count-step">
          {translate('react.cycleCount.table.comment.label', 'Comment')}
        </TableHeaderCell>
      ),
      meta: {
        flexWidth: 100,
      },
    }),
    columnHelper.accessor(null, {
      id: cycleCountColumn.ACTIONS,
      header: () => <TableHeaderCell />,
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
            {(original.id.includes('newRow') || original.custom) && isStepEditable && (
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
        flexWidth: 25,
      },
    }),
  ];

  return {
    columns,
    defaultColumn,
    users,
    resetFocus,
  };
};

export default useCountStepTable;
