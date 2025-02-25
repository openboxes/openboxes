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
import useTranslate from 'hooks/useTranslate';
import groupBinLocationsByZone from 'utils/groupBinLocationsByZone';
import { fetchBins } from 'utils/option-utils';
import { checkBinLocationSupport } from 'utils/supportedActivitiesUtils';
import CustomTooltip from 'wrappers/CustomTooltip';

// Managing state for single table, mainly table configuration (from resolve step)
const useResolveStepTable = ({
  cycleCountId,
  removeRow,
  validationErrors,
  tableData,
}) => {
  const columnHelper = createColumnHelper();
  // State for saving data for binLocation dropdown
  const [binLocations, setBinLocations] = useState([]);
  const translate = useTranslate();
  const events = new EventEmitter();

  const {
    users,
    currentLocation,
    reasonCodes,
  } = useSelector((state) => ({
    users: state.users.data,
    currentLocation: state.session.currentLocation,
    reasonCodes: state.reasonCodes.data,
  }));

  const dispatch = useDispatch();

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocation.supportedActivities), [currentLocation?.id]);

  useEffect(() => {
    if (showBinLocation) {
      (async () => {
        const fetchedBins = await fetchBins(currentLocation?.id);
        setBinLocations(fetchedBins);
      })();
    }
  }, [currentLocation?.id]);

  useEffect(() => {
    if (!reasonCodes.length) {
      dispatch(fetchReasonCodes());
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
  const getFieldProps = (fieldName) => {
    if (fieldName === cycleCountColumn.BIN_LOCATION && showBinLocation) {
      return {
        labelKey: 'name',
        options: groupBinLocationsByZone(binLocations),
      };
    }

    if (fieldName === cycleCountColumn.ROOT_CAUSE) {
      return {
        options: reasonCodes,
        placeholder: translate('react.cycleCount.selectPlaceholder.label', 'Select'),
      };
    }

    return {};
  };

  const defaultColumn = {
    cell: ({
      getValue, row: { original, index }, column: { id }, table,
    }) => {
      const isFieldEditable = !original.id.includes('newRow')
        && ![
          cycleCountColumn.QUANTITY_RECOUNTED,
          cycleCountColumn.ROOT_CAUSE,
          cycleCountColumn.COMMENT,
        ].includes(id);
      // We shouldn't allow users edit fetched data (quantityRecounted, rootCause and comment
      // field are editable)
      if (isFieldEditable) {
        return (
          <TableCell className="rt-td rt-td-count-step static-cell-count-step d-flex align-items-center">
            {getValue()}
          </TableCell>
        );
      }
      // Keep and update the state of the cell during rerenders
      const columnPath = id.replaceAll('_', '.');
      const initialValue = _.get(tableData, `[${index}].${columnPath}`);
      const errorMessage = validationErrors?.[cycleCountId]?.errors?.[index]?.[columnPath]?._errors;

      const [value, setValue] = useState(initialValue);
      const [error, setError] = useState(errorMessage);
      // If the value at the end of entering data is the same as it was initially,
      // we don't want to trigger rerender
      const isEdited = initialValue !== value;
      // When the input is blurred, we'll call the table meta's updateData function
      const onBlur = () => {
        if (!isEdited) {
          return;
        }
        table.options.meta?.updateData(cycleCountId, original.id, id, value);
        setError(null);
        if (id === cycleCountColumn.QUANTITY_RECOUNTED) {
          events.emit('refreshRecountDifference');
        }
      };

      // on change function expects e.target.value for text fields,
      // in other cases it expects just the value
      const onChange = (e) => {
        setValue(e?.target?.value ?? e);
      };

      // Table consists of text fields, one numerical field for quantity recounted,
      // select field for bin locations and root cause and one date picker for the expiration date.
      const type = getFieldType(id);
      const Component = getFieldComponent(id);
      const fieldProps = getFieldProps(id);

      return (
        <TableCell className="rt-td rt-td-count-step pb-0">
          <Component
            type={type}
            value={value}
            onChange={onChange}
            onBlur={onBlur}
            className="w-75 m-1"
            showErrorBorder={error}
            hideErrorMessageWrapper
            {...fieldProps}
          />
          {error && (
            <CustomTooltip
              content={error}
              className="error-icon"
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
          <TableHeaderCell>
            {translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
          </TableHeaderCell>
        ),
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
      cell: useCallback(({ row: { original: { id } } }) => (
        // TODO: Remove check if id is equal to quantityCounted
        //  after quantityCounted will be added to the response
        <TableCell className="rt-td rt-td-count-step static-cell-count-step d-flex align-items-center">
          {id.includes('newRow')
            ? <ArrowValueIndicator variant={ArrowValueIndicatorVariant.EMPTY} />
            : Math.floor(Math.random() * 10).toString()}
          {!id.includes('newRow') ? (
            <Tooltip
              arrow="true"
              delay="150"
              duration="250"
              hideDelay="50"
              // TODO: Should be replaced with comment fetched from the API
              html={<span className="p-2">Comment from count step</span>}
            >
              <RiChat3Line
                role="button"
                size={16}
                className="ml-2"
              />
            </Tooltip>
          ) : ''}
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
      cell: useCallback(({ row: { original: { id, quantityOnHand } } }) => {
        // TODO: Replace random value with quantityCounted from response
        const value = Math.floor(Math.random() * 10) - quantityOnHand;
        const variant = getCycleCountDifferencesVariant(value, id);
        return (
          <TableCell className="rt-td rt-td-count-step static-cell-count-step d-flex align-items-center">
            <ArrowValueIndicator value={value} variant={variant} showAbsoluteValue />
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
      cell: ({ row: { original: { quantityOnHand, id }, index } }) => {
        const [value, setValue] = useState(tableData?.[index]?.quantityRecounted);
        const recountDifference = value - (quantityOnHand || 0);
        const variant = getCycleCountDifferencesVariant(recountDifference, id);
        events.on('refreshRecountDifference', () => {
          setValue(tableData?.[index]?.quantityRecounted);
        });

        return (
          <TableCell className="rt-td rt-td-count-step static-cell-count-step d-flex align-items-center">
            <ArrowValueIndicator value={recountDifference} variant={variant} showAbsoluteValue />
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
        flexWidth: 120,
      },
    }),
    columnHelper.accessor(cycleCountColumn.COMMENT, {
      header: useMemo(() => (
        <TableHeaderCell className="rt-th-count-step">
          {translate('react.cycleCount.table.comment.label', 'Comment')}
        </TableHeaderCell>
      ), []),
      meta: {
        flexWidth: 228,
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
      ), []),
      meta: {
        flexWidth: 50,
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
