import { EventEmitter } from 'events';

import React, {
  useCallback, useEffect, useMemo, useState,
} from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { RiChat3Line, RiDeleteBinLine, RiErrorWarningLine } from 'react-icons/ri';
import { useDispatch, useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';

import { fetchReasonCodes } from 'actions';
import { FETCH_CYCLE_COUNT_REASON_CODES } from 'actions/types';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import ArrowValueIndicator from 'components/DataTable/v2/ArrowValueIndicator';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import ArrowValueIndicatorVariant, {
  getCycleCountDifferencesVariant,
} from 'consts/arrowValueIndicatorVariant';
import cycleCountColumn from 'consts/cycleCountColumn';
import { DateFormat } from 'consts/timeFormat';
import useArrowsNavigation from 'hooks/useArrowsNavigation';
import useTranslate from 'hooks/useTranslate';
import groupBinLocationsByZone from 'utils/groupBinLocationsByZone';
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
  refreshFocusCounter,
}) => {
  const columnHelper = createColumnHelper();
  const [rowIndex, setRowIndex] = useState(null);
  const [columnId, setColumnId] = useState(null);
  // If prevForceResetFocus is different from refreshFocusCounter,
  // it triggers a reset of rowIndex and columnId.
  const [prevForceResetFocus, setPrevForceResetFocus] = useState(0);

  // State for saving data for binLocation dropdown
  const translate = useTranslate();
  const events = new EventEmitter();

  const {
    users,
    currentLocation,
    formatLocalizedDate,
    reasonCodes,
    binLocations,
  } = useSelector((state) => ({
    users: state.users.data,
    currentLocation: state.session.currentLocation,
    formatLocalizedDate: formatDate(state.localize),
    reasonCodes: state.cycleCount.reasonCodes,
    binLocations: state.cycleCount.binLocations,
  }));

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocation.supportedActivities), [currentLocation?.id]);

  const dispatch = useDispatch();

  useEffect(() => {
    if (refreshFocusCounter !== prevForceResetFocus) {
      setRowIndex(null);
      setColumnId(null);
      setPrevForceResetFocus(refreshFocusCounter);
    }
  }, [refreshFocusCounter]);

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
  const getFieldProps = (fieldName, hasTooltipIcon) => {
    if (fieldName === cycleCountColumn.BIN_LOCATION && showBinLocation) {
      return {
        labelKey: 'name',
        options: groupBinLocationsByZone(binLocations),
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

  // this function is required because there is a problem w getValue
  const getValueToDisplay = (id, value) => {
    if (id === cycleCountColumn.EXPIRATION_DATE) {
      return formatLocalizedDate(value, DateFormat.DD_MMM_YYYY);
    }

    if (id === cycleCountColumn.QUANTITY_COUNTED) {
      return value?.toString();
    }

    if (id === cycleCountColumn.BIN_LOCATION) {
      return value?.name;
    }

    if (id === cycleCountColumn.ROOT_CAUSE) {
      return value?.label;
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

      const isFieldEditable = !original.id.includes('newRow')
        && ![
          cycleCountColumn.QUANTITY_RECOUNTED,
          cycleCountColumn.ROOT_CAUSE,
          cycleCountColumn.COMMENT,
        ].includes(id);
      const showStaticTooltip = [
        cycleCountColumn.ROOT_CAUSE,
        cycleCountColumn.COMMENT,
        cycleCountColumn.BIN_LOCATION,
      ].includes(id);
      // We shouldn't allow users edit fetched data (quantityRecounted, rootCause and comment
      // field are editable)
      if (!isStepEditable) {
        return (
          <CustomTooltip
            content={getValueToDisplay(columnPath, value)}
            show={showStaticTooltip}
          >
            <TableCell
              className="static-cell-count-step align-items-center resolve-table-limit-lines"
            >
              <div className={showStaticTooltip ? 'limit-lines-1' : 'limit-lines-3 text-break'}>
                {getValueToDisplay(columnPath, value)}
              </div>
            </TableCell>
          </CustomTooltip>
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
          const parsedValue = parseInt(value, 10) || 0;
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
      }, [rowIndex, columnId]);

      // on change function expects e.target.value for text fields,
      // in other cases it expects just the value
      const onChange = (e) => {
        if ([
          cycleCountColumn.BIN_LOCATION,
          cycleCountColumn.ROOT_CAUSE,
        ].includes(id)) {
          setError(null);
          setWarning(null);
          triggerValidation();
        }
        setValue(e?.target?.value ?? e);
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
      const fieldProps = getFieldProps(columnPath, tooltipContent);

      // Columns allowed for focus in new rows
      const newRowFocusableCells = [
        cycleCountColumn.LOT_NUMBER,
        cycleCountColumn.EXPIRATION_DATE,
        cycleCountColumn.QUANTITY_RECOUNTED,
        cycleCountColumn.ROOT_CAUSE,
        cycleCountColumn.COMMENT,
      ];

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
      ].includes(id);
      return (
        <TableCell
          className="rt-td rt-td-count-step pb-0"
          customTooltip={showTooltip && getValueToDisplay(id, value)}
          tooltipClassname="w-100"
          tooltipLabel={getValueToDisplay(id, value)}
        >
          <Component
            disabled={isFieldEditable}
            type={type}
            value={value}
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
      (row) => (row?.binLocation?.label ? row?.binLocation : row.binLocation?.name), {
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
            ? <ArrowValueIndicator variant={ArrowValueIndicatorVariant.EMPTY} />
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
      cell: useCallback(({ row: { original: { quantityVariance, quantityCounted } } }) => {
        const variant = (quantityCounted || quantityCounted === 0)
          ? getCycleCountDifferencesVariant(quantityVariance, quantityCounted)
          : ArrowValueIndicatorVariant.EMPTY;
        return (
          <TableCell className="rt-td rt-td-count-step static-cell-count-step d-flex align-items-center">
            <ArrowValueIndicator value={quantityVariance} variant={variant} showAbsoluteValue />
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
        const variant = getCycleCountDifferencesVariant(recountDifference, value);
        events.on('refreshRecountDifference', () => {
          setValue(tableData?.[index]?.quantityRecounted);
        });

        return (
          <TableCell className="rt-td rt-td-count-step static-cell-count-step d-flex align-items-center">
            <ArrowValueIndicator
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
                onClick={() => removeRow(cycleCountId, original.id)}
                size={22}
              />
            )}
          </Tooltip>
        </TableCell>
      ), []),
      meta: {
        flexWidth: 50,
        hide: !tableData.some((row) => row.id?.includes('newRow') || row.custom) || !isStepEditable,
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
