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
import { getBinLocationToDisplay, groupBinLocationsByZone } from 'utils/groupBinLocationsByZone';
import { checkBinLocationSupport } from 'utils/supportedActivitiesUtils';
import CustomTooltip from 'wrappers/CustomTooltip';

// Managing state for single table, mainly table configuration (from count step)
const useCountStepTable = ({
  cycleCountId,
  productId,
  removeRow,
  validationErrors,
  tableData,
  isStepEditable,
  formatLocalizedDate,
  addEmptyRow,
  triggerValidation,
  isFormDisabled,
  forceRerender,
}) => {
  const columnHelper = createColumnHelper();
  const [rowIndex, setRowIndex] = useState(null);
  const [columnId, setColumnId] = useState(null);
  const translate = useTranslate();

  const { users, currentLocation, binLocations } = useSelector((state) => ({
    users: state.users.data,
    binLocations: state.cycleCount.binLocations,
    currentLocation: state.session.currentLocation,
  }));

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocation.supportedActivities), [currentLocation?.id]);

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
  const getFieldProps = (fieldName, value, isFieldDisabled) => {
    if (fieldName === cycleCountColumn.BIN_LOCATION && showBinLocation) {
      return {
        labelKey: 'name',
        options: groupBinLocationsByZone(binLocations, translate),
      };
    }

    if (fieldName === cycleCountColumn.EXPIRATION_DATE) {
      return {
        customDateFormat: DateFormat.DD_MMM_YYYY,
      };
    }

    if (fieldName === cycleCountColumn.LOT_NUMBER && isFieldDisabled) {
      return {
        placeholder: translate('react.cycleCount.emptyLotNumber.label', 'NO LOT'),
      };
    }

    return {};
  };

  /**
   * Override the behaviour of getValue when displaying fields on the confirmation step.
   */
  const getNonEditableValueToDisplay = (columnPath, value) => {
    if (columnPath === cycleCountColumn.EXPIRATION_DATE) {
      return formatLocalizedDate(value, DateFormat.DD_MMM_YYYY);
    }

    if (columnPath === cycleCountColumn.QUANTITY_COUNTED) {
      return value?.toString();
    }

    if (columnPath === cycleCountColumn.BIN_LOCATION && showBinLocation) {
      return getBinLocationToDisplay(value);
    }

    return value;
  };

  /**
   Override the behaviour of getValue when displaying fields on the count step.
   */
  const getValueToDisplay = (columnPath, value) => {
    if (columnPath === cycleCountColumn.BIN_LOCATION && showBinLocation) {
      return { ...value, name: getBinLocationToDisplay(value) };
    }

    return value;
  };

  const defaultColumn = {
    cell: ({
      row: { original, index }, column: { id }, table,
    }) => {
      const columnPath = id.replaceAll('_', '.');
      const initialValue = _.get(tableData, `[${index}].${columnPath}`);
      const [value, setValue] = useState(initialValue);
      const showTooltip = columnPath === cycleCountColumn.BIN_LOCATION;

      const isFieldDisabled = !original.id.includes('newRow') && ![
        cycleCountColumn.QUANTITY_COUNTED,
        cycleCountColumn.COMMENT,
      ].includes(id);

      // We shouldn't allow users edit fetched data (only quantity counted and comment are editable)
      if (!isStepEditable) {
        return (
          <TableCell
            className="static-cell-count-step d-flex align-items-center"
            tooltip={showTooltip}
            tooltipForm={showTooltip}
            tooltipLabel={getBinLocationToDisplay(value) || translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
          >
            {getNonEditableValueToDisplay(columnPath, value)}
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
        if (!isEdited) {
          return;
        }
        if (columnPath === cycleCountColumn.QUANTITY_COUNTED) {
          const parsedValue = value ? (parseInt(value, 10) || 0) : value;
          setValue(parsedValue);
          table.options.meta?.updateData(cycleCountId, original.id, id, parsedValue);
          setError(null);
          triggerValidation();
          return;
        }
        table.options.meta?.updateData(cycleCountId, original.id, id, value);
        setError(null);
        triggerValidation();
      };

      // Resets the error when rowIndex or columnId changes
      // since validationErrors don’t update properly.
      // Old errors reappear on rerender, and using arrow keys
      // triggers a rerender with every key press, causing outdated errors to show.
      useEffect(() => {
        if (rowIndex !== null && columnId && error !== null) {
          setError(null);
        }

        // Thanks to this function, we can reset the focus only after finishing arrow navigation.
        // Previously, we triggered a focus reset on almost every user interaction,
        // which caused excessive re-renders in all tables
        const handleClick = (event) => {
          if (rowIndex === null && columnId === null) {
            return;
          }
          const { target } = event;

          // Elements that should keep focus (avoid resetting)
          const isInputElement = target.closest('input, select, textarea, .date-field-input, .react-datepicker, .react-select__control');

          // Specific clickable UI parts that should reset focus
          // These are elements that close components, e.g. a date picker when clicking a day,
          // or a select dropdown when selecting an option
          const isDatePickerDayElement = target.closest('.react-datepicker__day');
          const isDropdownOptionElement = target.closest('.react-select__option');

          // if this is input element, then we don't want to reset rowIndex and columnId,
          // and re-render the component again because then all tables will be re-rendered
          // which will cause performance issues
          if (!isInputElement && !isDatePickerDayElement && !isDropdownOptionElement) {
            setRowIndex(null);
            setColumnId(null);
            forceRerender();
          }

          // if this is isDatePickerDayElement or isDropdownOptionElement, then we want to reset
          // rowIndex and columnId because then we close the date picker or select dropdown
          if (isDatePickerDayElement || isDropdownOptionElement) {
            setRowIndex(null);
            setColumnId(null);
          }
        };
        document.addEventListener('click', handleClick);

        return () => {
          document.removeEventListener('click', handleClick);
        };
      }, [rowIndex, columnId]);

      // on change function expects e.target.value for text fields,
      // in other cases it expects just the value
      const onChange = (e) => {
        setValue(e?.target?.value ?? e);
      };

      // After pulling the latest changes, table.options.meta?.updateData no longer
      // works on onChange, so for now, I put it inside useEffect
      useEffect(() => {
        table.options.meta?.updateData(cycleCountId, original.id, id, value);
      }, [value]);

      // Table consists of text fields, one numerical field for quantity counted,
      // select field for bin locations and one date picker for the expiration date.
      const type = getFieldType(columnPath);
      const Component = getFieldComponent(columnPath);
      const fieldProps = getFieldProps(columnPath, value, isFieldDisabled);

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
        setColumnId,
        setRowIndex,
        addNewRow: () => addEmptyRow(productId, cycleCountId, false),
        isNewRow,
        onBlur,
      });
      return (
        <TableCell
          className="rt-td rt-td-count-step pb-0"
          tooltip={showTooltip}
          tooltipForm={showTooltip}
          tooltipClassname={showTooltip && 'bin-location-tooltip'}
          tooltipLabel={getBinLocationToDisplay(value) || translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
        >
          <Component
            disabled={isFieldDisabled || isFormDisabled}
            type={type}
            value={getValueToDisplay(columnPath, value)}
            onChange={onChange}
            onBlur={onBlur}
            className={`m-1 hide-arrows ${showTooltip ? 'w-99' : 'w-75'} ${error && 'border border-danger input-has-error'}`}
            showErrorBorder={error}
            hideErrorMessageWrapper
            onKeyDown={(e) => handleKeyDown(e, index, columnPath)}
            focusProps={{
              fieldIndex: index,
              fieldId: columnPath,
              rowIndex,
              columnId,
            }}
            onWheel={(event) => event.currentTarget.blur()}
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
      (row) => getBinLocationToDisplay(row?.binLocation), {
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
                className={isFormDisabled ? 'disabled-icon' : 'cursor-pointer'}
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
  };
};

export default useCountStepTable;
