import { EventEmitter } from 'events';

import React, {
  useCallback, useEffect, useMemo, useState,
} from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { RiChat3Line, RiDeleteBinLine, RiErrorWarningLine } from 'react-icons/ri';
import { useDispatch, useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';
import { getLotNumbersByProductId } from 'selectors';

import { fetchReasonCodes } from 'actions';
import { FETCH_CYCLE_COUNT_REASON_CODES } from 'actions/types';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import ValueIndicator from 'components/DataTable/v2/ValueIndicator';
import LotSelectorField from 'components/form-elements/LotSelectorField';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import cycleCountColumn from 'consts/cycleCountColumn';
import { DateFormat } from 'consts/timeFormat';
import valueIndicatorVariant, { getCycleCountDifferencesVariant } from 'consts/valueIndicatorVariant';
import useArrowsNavigation from 'hooks/useArrowsNavigation';
import useTranslate from 'hooks/useTranslate';
import { getBinLocationToDisplay, groupBinLocationsByZone } from 'utils/groupBinLocationsByZone';
import { checkBinLocationSupport } from 'utils/supportedActivitiesUtils';
import { formatDate } from 'utils/translation-utils';
import CustomTooltip from 'wrappers/CustomTooltip';

// Managing state for single table, mainly table configuration (from resolve step)
const useResolveStepTable = ({
  cycleCountId,
  removeRow,
  triggerValidation,
  validationErrors,
  shouldHaveRootCause,
  isStepEditable,
  tableData,
  productId,
  addEmptyRow,
  isFormDisabled,
  forceRerender,
}) => {
  const columnHelper = createColumnHelper();
  const [rowIndex, setRowIndex] = useState(null);
  const [columnId, setColumnId] = useState(null);
  const [disabledExpirationDateFields, setDisabledExpirationDateFields] = useState({});

  // State for saving data for binLocation dropdown
  const translate = useTranslate();
  const events = new EventEmitter();

  const {
    users,
    currentLocation,
    formatLocalizedDate,
    reasonCodes,
    binLocations,
    lotNumbers,
  } = useSelector((state) => ({
    users: state.users.data,
    currentLocation: state.session.currentLocation,
    formatLocalizedDate: formatDate(state.localize),
    reasonCodes: state.cycleCount.reasonCodes,
    binLocations: state.cycleCount.binLocations,
    lotNumbers: getLotNumbersByProductId(state, productId),
  }));

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocation.supportedActivities), [currentLocation?.id]);

  const dispatch = useDispatch();

  useEffect(() => {
    if (!reasonCodes?.length) {
      dispatch(fetchReasonCodes('ADJUST_INVENTORY', FETCH_CYCLE_COUNT_REASON_CODES));
    }
  }, []);

  // Get appropriate input component based on table column
  const getFieldComponent = (fieldName) => {
    if (fieldName === cycleCountColumn.EXPIRATION_DATE) {
      return DateField;
    }

    if ([cycleCountColumn.BIN_LOCATION, cycleCountColumn.ROOT_CAUSE].includes(fieldName)) {
      return SelectField;
    }

    if (fieldName === cycleCountColumn.LOT_NUMBER) {
      return LotSelectorField;
    }

    return TextInput;
  };

  // Get text input type: quantityCounted expects a number,
  // the rest of the inputs should be text
  const getFieldType = (fieldName) => {
    if ([
      cycleCountColumn.QUANTITY_COUNTED,
      cycleCountColumn.QUANTITY_RECOUNTED,
    ].includes(fieldName)) {
      return 'number';
    }

    return 'text';
  };

  // Get field props, for the binLocation dropdown we have to pass options
  const getFieldProps = (fieldName, hasTooltipIcon, value, isFieldDisabled) => {
    if (fieldName === cycleCountColumn.BIN_LOCATION && showBinLocation) {
      return {
        labelKey: 'name',
        options: groupBinLocationsByZone(binLocations, translate),
      };
    }

    if (fieldName === cycleCountColumn.ROOT_CAUSE) {
      return {
        options: reasonCodes,
        placeholder: (
          <span className={hasTooltipIcon ? 'pl-12px' : ''}>
            {translate('react.cycleCount.selectPlaceholder.label', 'Select')}
          </span>
        ),
      };
    }

    if (fieldName === cycleCountColumn.EXPIRATION_DATE) {
      return {
        customDateFormat: DateFormat.DD_MMM_YYYY,
      };
    }

    if (fieldName === cycleCountColumn.LOT_NUMBER) {
      return {
        placeholder: isFieldDisabled && translate('react.cycleCount.emptyLotNumber.label', 'NO LOT'),
        productId,
      };
    }

    return {};
  };

  const getTooltipMessage = (error, warning, id) => {
    if (error) {
      return error;
    }

    // Warning is applicable only for root cause field
    if (warning && id === cycleCountColumn.ROOT_CAUSE) {
      return translate(
        'react.cycleCount.rootCauseWarning.label',
        'Specify result of investigation if applicable.',
      );
    }

    return null;
  };

  /**
   * Override the behaviour of getValue when displaying fields on the confirmation step.
   */
  const getNonEditableValueToDisplay = (id, value) => {
    if (id === cycleCountColumn.EXPIRATION_DATE) {
      return formatLocalizedDate(value, DateFormat.DD_MMM_YYYY);
    }

    if (id === cycleCountColumn.QUANTITY_COUNTED) {
      return value?.toString();
    }

    if (id === cycleCountColumn.BIN_LOCATION && showBinLocation) {
      return getBinLocationToDisplay(value);
    }

    if (id === cycleCountColumn.ROOT_CAUSE) {
      return value?.label;
    }

    return value;
  };

  const getValueToDisplayTooltipLabel = (id, value) => {
    if (id === cycleCountColumn.BIN_LOCATION) {
      return getBinLocationToDisplay(value) || translate('react.cycleCount.table.binLocation.label', 'Bin Location');
    }

    if (id === cycleCountColumn.LOT_NUMBER) {
      return value || translate('react.cycleCount.table.lotNumber.label', 'Serial / Lot Number');
    }

    if (id === cycleCountColumn.ROOT_CAUSE) {
      return value?.label || translate('react.cycleCount.table.rootCause.label', 'Root Cause');
    }

    return value;
  };

  /**
   Override the behaviour of getValue when displaying fields on the count step.
   */
  const getValueToDisplay = (id, value) => {
    if (id === cycleCountColumn.BIN_LOCATION && showBinLocation) {
      return { ...value, name: getBinLocationToDisplay(value) };
    }
    if (id === cycleCountColumn.LOT_NUMBER && value) {
      return { label: value, value };
    }
    return value;
  };

  const defaultColumn = {
    cell: ({
      row: { original, index }, column: { id }, table,
    }) => {
      const columnPath = id.replaceAll('_', '.');
      const initialValue = _.get(tableData, `[${index}].${columnPath}`);
      // Keep and update the state of the cell during rerenders
      const [value, setValue] = useState(initialValue);

      const isFieldDisabled =
        (
          !original.id.includes('newRow') &&
          ![
            cycleCountColumn.QUANTITY_RECOUNTED,
            cycleCountColumn.ROOT_CAUSE,
            cycleCountColumn.COMMENT,
          ].includes(id)
        ) ||
        (
          columnPath === cycleCountColumn.EXPIRATION_DATE &&
          disabledExpirationDateFields[original.id]
        );

      const showStaticTooltip = [
        cycleCountColumn.ROOT_CAUSE,
        cycleCountColumn.COMMENT,
        cycleCountColumn.BIN_LOCATION,
      ].includes(columnPath);
      // We shouldn't allow users edit fetched data (quantityRecounted, rootCause and comment
      // field are editable)
      if (!isStepEditable) {
        return (
          <TableCell
            className="static-cell-count-step align-items-center resolve-table-limit-lines"
            customTooltip={showStaticTooltip && getValueToDisplayTooltipLabel(columnPath, value)}
            tooltipLabel={getValueToDisplayTooltipLabel(columnPath, value)}
          >
            <div className={showStaticTooltip ? 'limit-lines-1' : 'limit-lines-3 text-break'}>
              {getNonEditableValueToDisplay(columnPath, value)}
            </div>
          </TableCell>
        );
      }
      const errorMessage = validationErrors?.[cycleCountId]?.errors?.[index]?.[columnPath]?._errors;
      const [error, setError] = useState(errorMessage);
      const [warning, setWarning] = useState(error ? null : shouldHaveRootCause(original?.id));
      // If the value at the end of entering data is the same as it was initially,
      // we don't want to trigger rerender
      const isEdited = initialValue !== value;
      // When the input is blurred, we'll call the table meta's updateData function
      const onBlur = () => {
        if (!isEdited) {
          return;
        }
        if (![
          cycleCountColumn.BIN_LOCATION,
          cycleCountColumn.ROOT_CAUSE,
        ].includes(id)) {
          table.options.meta?.updateData(cycleCountId, original.id, id, value);
          setError(null);
          triggerValidation();
        }
        if (id === cycleCountColumn.QUANTITY_RECOUNTED) {
          const parsedValue = value ? (parseInt(value, 10) || 0) : value;
          setValue(parsedValue);
          table.options.meta?.updateData(cycleCountId, original.id, id, parsedValue);
          events.emit('refreshRecountDifference');
        }
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

      const handleLotNumberChange = (selectedLotNumber) => {
        const isLotAlreadyExist = lotNumbers.find(lot => lot.lotNumber === selectedLotNumber);

        setDisabledExpirationDateFields(prev => ({
          ...prev,
          [original.id]: !!isLotAlreadyExist,
        }));

        table.options.meta?.updateData(
          cycleCountId,
          original.id,
          cycleCountColumn.LOT_NUMBER,
          selectedLotNumber,
        );

        const formattedExpirationDate = isLotAlreadyExist
          ? formatLocalizedDate(
            isLotAlreadyExist.expirationDate,
            DateFormat.DD_MMM_YYYY,
          )
          : null;

        // when we change the lot number, we also want to update the expiration date
        table.options.meta?.updateData(
          cycleCountId,
          original.id,
          cycleCountColumn.EXPIRATION_DATE,
          formattedExpirationDate,
        );

        setValue(selectedLotNumber);
      };

      // on change function expects e.target.value for text fields,
      // in other cases it expects just the value
      const onChange = (e) => {
        if (columnPath === cycleCountColumn.LOT_NUMBER) {
          return handleLotNumberChange(e?.value);
        }

        if ([
          cycleCountColumn.BIN_LOCATION,
          cycleCountColumn.ROOT_CAUSE,
        ].includes(id)) {
          setError(null);
          setWarning(null);
          triggerValidation();
        }
        return setValue(e?.target?.value ?? e);
      };

      // After pulling the latest changes, table.options.meta?.updateData no longer
      // works on onChange, so for now, I put it inside useEffect
      useEffect(() => {
        table.options.meta?.updateData(cycleCountId, original.id, id, value);
      }, [value]);

      // Table consists of text fields, one numerical field for quantity recounted,
      // select field for bin locations and root cause and one date picker for the expiration date.
      const type = getFieldType(columnPath);
      const Component = getFieldComponent(columnPath);
      const tooltipContent = getTooltipMessage(errorMessage, warning, columnPath);
      const fieldProps = getFieldProps(columnPath, tooltipContent, value, isFieldDisabled);

      // Columns allowed for focus in new rows
      const newRowFocusableCells = [
        cycleCountColumn.LOT_NUMBER,
        !disabledExpirationDateFields[original.id] ? cycleCountColumn.EXPIRATION_DATE : null,
        cycleCountColumn.QUANTITY_RECOUNTED,
        cycleCountColumn.ROOT_CAUSE,
        cycleCountColumn.COMMENT,
      ].filter(Boolean);

      if (showBinLocation) {
        newRowFocusableCells.splice(0, 0, cycleCountColumn.BIN_LOCATION);
      }

      // Columns allowed for focus in existing rows
      const existingRowFocusableCells = [
        cycleCountColumn.QUANTITY_RECOUNTED,
        cycleCountColumn.ROOT_CAUSE,
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
      const isAutoWidth = [
        cycleCountColumn.ROOT_CAUSE,
        cycleCountColumn.COMMENT,
        cycleCountColumn.EXPIRATION_DATE,
      ].includes(columnPath);
      const showTooltip = [
        cycleCountColumn.ROOT_CAUSE,
        cycleCountColumn.BIN_LOCATION,
        cycleCountColumn.LOT_NUMBER,
      ].includes(columnPath);
      return (
        <TableCell
          className="rt-td rt-td-count-step pb-0"
          customTooltip={showTooltip && getValueToDisplayTooltipLabel(columnPath, value)}
          tooltipClassname="w-100"
          tooltipLabel={getValueToDisplayTooltipLabel(columnPath, value)}
        >
          <Component
            disabled={isFieldDisabled || isFormDisabled}
            type={type}
            value={getValueToDisplay(columnPath, value)}
            onChange={onChange}
            onBlur={onBlur}
            className={`${isAutoWidth ? 'w-auto' : 'w-75'} m-1 hide-arrows ${error && 'border border-danger input-has-error'}`}
            showErrorBorder={error}
            hideErrorMessageWrapper
            warning={tooltipContent && warning}
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
          {(error || warning) && tooltipContent && (
            <CustomTooltip
              content={tooltipContent}
              className={`tooltip-icon tooltip-icon--${error ? 'error' : 'warning'}`}
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
        header: useMemo(() => (
          <TableHeaderCell className="rt-th-count-step">
            {translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
          </TableHeaderCell>
        ), []),
        meta: {
          flexWidth: 160,
          hide: !showBinLocation,
        },
      },
    ),
    columnHelper.accessor(cycleCountColumn.LOT_NUMBER, {
      header: useMemo(() => (
        <TableHeaderCell className="rt-th-count-step">
          {translate('react.cycleCount.table.lotNumber.label', 'Serial / Lot Number')}
        </TableHeaderCell>
      ), []),
      meta: {
        flexWidth: 160,
      },
    }),
    columnHelper.accessor(cycleCountColumn.EXPIRATION_DATE, {
      header: useMemo(() => (
        <TableHeaderCell className="rt-th-count-step">
          {translate('react.cycleCount.table.expirationDate.label', 'Expiration Date')}
        </TableHeaderCell>
      ), []),
      meta: {
        getCellContext: () => ({
          className: 'split-table-right',
        }),
        flexWidth: 160,
      },
    }),
    columnHelper.accessor(cycleCountColumn.QUANTITY_COUNTED, {
      header: useMemo(() => (
        <TableHeaderCell className="rt-th-count-step">
          {translate('react.cycleCount.table.quantityCounted.label', 'Quantity Counted')}
        </TableHeaderCell>
      ), []),
      cell: useCallback(({ row: { original: { quantityCounted, commentFromCount } } }) => (
        <TableCell className="rt-td rt-td-count-step static-cell-count-step d-flex align-items-center">
          {quantityCounted === null
            ? <ValueIndicator variant={valueIndicatorVariant.EMPTY} />
            : quantityCounted}
          {commentFromCount && (
            <Tooltip
              arrow="true"
              delay="150"
              duration="250"
              hideDelay="50"
              html={<span className="p-2">{commentFromCount}</span>}
            >
              <RiChat3Line
                role="button"
                size={16}
                className="ml-2"
              />
            </Tooltip>
          )}
        </TableCell>
      ), []),
      meta: {
        flexWidth: 120,
      },
    }),
    columnHelper.accessor(cycleCountColumn.COUNT_DIFFERENCE, {
      header: useMemo(() => (
        <TableHeaderCell className="rt-th-count-step">
          {translate('react.cycleCount.table.countDifference.label', 'Count Difference')}
        </TableHeaderCell>
      ), []),
      cell: useCallback(({ row: { original: { quantityOnHand, quantityCounted } } }) => {
        const quantityVariance = quantityCounted - (quantityOnHand || 0);
        const variant = (quantityCounted || quantityCounted === 0)
          ? getCycleCountDifferencesVariant({ firstValue: quantityVariance })
          : valueIndicatorVariant.EMPTY;
        return (
          <TableCell className="rt-td rt-td-count-step static-cell-count-step d-flex align-items-center">
            <ValueIndicator value={quantityVariance} variant={variant} showAbsoluteValue />
          </TableCell>
        );
      }, []),
      meta: {
        flexWidth: 120,
      },
    }),
    columnHelper.accessor(cycleCountColumn.QUANTITY_RECOUNTED, {
      header: useMemo(() => (
        <TableHeaderCell className="rt-th-count-step">
          {translate('react.cycleCount.table.quantityRecounted.label', 'Quantity Recounted')}
        </TableHeaderCell>
      ), []),
      meta: {
        flexWidth: 120,
      },
    }),
    columnHelper.accessor(cycleCountColumn.RECOUNT_DIFFERENCE, {
      header: useMemo(() => (
        <TableHeaderCell className="rt-th-count-step">
          {translate('react.cycleCount.table.recountDifference.label', 'Recount Difference')}
        </TableHeaderCell>
      ), []),
      cell: ({ row: { original: { quantityOnHand }, index } }) => {
        const [value, setValue] = useState(tableData?.[index]?.quantityRecounted);
        const recountDifference = value - (quantityOnHand || 0);
        // We want to show variant only when value is not null
        const variant = getCycleCountDifferencesVariant({
          firstValue: recountDifference,
          secondValue: value,
          shouldCheckSecondValue: true,
        });
        events.on('refreshRecountDifference', () => {
          setValue(tableData?.[index]?.quantityRecounted);
        });

        return (
          <TableCell className="rt-td rt-td-count-step static-cell-count-step d-flex align-items-center">
            <ValueIndicator
              value={recountDifference}
              variant={variant}
              showAbsoluteValue
            />
          </TableCell>
        );
      },
      meta: {
        flexWidth: 120,
      },
    }),
    columnHelper.accessor(cycleCountColumn.ROOT_CAUSE, {
      header: useMemo(() => (
        <TableHeaderCell className="rt-th-count-step">
          {translate('react.cycleCount.table.rootCause.label', 'Root Cause')}
        </TableHeaderCell>
      ), []),
      meta: {
        flexWidth: 160,
      },
    }),
    columnHelper.accessor(cycleCountColumn.COMMENT, {
      header: useMemo(() => (
        <TableHeaderCell className="rt-th-count-step">
          {translate('react.cycleCount.table.comment.label', 'Comment')}
        </TableHeaderCell>
      ), []),
      meta: {
        flexWidth: 160,
        getCellContext: () => ({
          className: 'overflow-hidden',
        }),
      },
    }),
    columnHelper.accessor(null, {
      id: cycleCountColumn.ACTIONS,
      header: useMemo(() => <TableHeaderCell />, []),
      cell: useCallback(({ row: { original } }) => (
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
          >
            {(original.id.includes('newRow') || original.custom) && (
              <RiDeleteBinLine
                className={isFormDisabled ? 'disabled-icon' : 'cursor-pointer'}
                onClick={() => removeRow(cycleCountId, original.id)}
                size={22}
              />
            )}
          </Tooltip>
        </TableCell>
      ), [isFormDisabled]),
      meta: {
        flexWidth: 50,
        hide: !tableData?.some((row) => row.id?.includes('newRow') || row.custom) || !isStepEditable,
      },
    }),
  ];

  return {
    columns,
    defaultColumn,
    users,
  };
};

export default useResolveStepTable;
