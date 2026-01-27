import React, {
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';

import PropTypes from 'prop-types';
import { RiErrorWarningLine } from 'react-icons/ri';
import { useDispatch, useSelector } from 'react-redux';
import { getFormatLocalizedDate, makeGetCycleCountItem } from 'selectors';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import DatePicker from 'components/form-elements/v2/DateField';
import { NEW_ROW } from 'consts/cycleCount';
import cycleCountColumn from 'consts/cycleCountColumn';
import { DateFormat } from 'consts/timeFormat';
import useCellValidation from 'hooks/cycleCount/useCellValidation';
import CustomTooltip from 'wrappers/CustomTooltip';

const ExpirationDateCell = ({
  id,
  index,
  cycleCountId,
  disabledExpirationDateFields,
  isStepEditable,
}) => {
  const getCycleCountItem = useMemo(() => makeGetCycleCountItem(), []);
  const item = useSelector(
    (state) => getCycleCountItem(state, cycleCountId, id),
  );
  const { inventoryItem: { expirationDate: initialValue } } = item;

  const [value, setValue] = useState(initialValue);

  const dispatch = useDispatch();

  const isNewRow = id?.includes(NEW_ROW);

  const isDisabled = disabledExpirationDateFields?.[id]
    || !isNewRow;

  const lastAutoValue = useRef(null);

  useEffect(() => {
    if (!isNewRow) {
      return;
    }

    // Keep local state in sync with redux,
    // but do not clear the input when redux temporarily has no value
    if (initialValue !== undefined) {
      setValue(initialValue);
    }
  }, [initialValue, isNewRow]);

  useEffect(() => {
    // Backend response can return rows in different order.
    // When the row id changes, this component may now represent
    // a different inventory item.
    // Reset local state so the expiration date matches the new row.
    setValue(initialValue);
  }, [id]);

  useEffect(() => {
    if (!isNewRow) {
      return;
    }

    // Check if there is an option for auto value for expiration date
    const autoValue = disabledExpirationDateFields?.[id];
    if (!autoValue) {
      return;
    }

    // Update only if the previous auto value is different from the current value
    if (lastAutoValue.current !== autoValue) {
      lastAutoValue.current = autoValue;
      setValue(autoValue);
      dispatch(
        updateFieldValue({
          cycleCountId,
          rowId: id,
          field: cycleCountColumn.EXPIRATION_DATE,
          value: autoValue,
        }),
      );
    }
  }, [
    disabledExpirationDateFields?.[id],
    isNewRow,
    value,
  ]);

  if (!isStepEditable) {
    const formatLocalizedDate = useSelector(getFormatLocalizedDate);

    return (
      <TableCell
        className="static-cell-count-step d-flex align-items-center"
      >
        {formatLocalizedDate(value, DateFormat.DD_MMM_YYYY)}
      </TableCell>
    );
  }

  const {
    onBlurValidationHandler,
    onChangeValidationHandler,
    error,
    showError,
  } = useCellValidation({
    initialValue,
    cycleCountId,
    index,
    fieldName: cycleCountColumn.EXPIRATION_DATE,
  });

  const onChange = (date) => {
    setValue(date);
    dispatch(
      updateFieldValue({
        cycleCountId,
        rowId: id,
        field: cycleCountColumn.EXPIRATION_DATE,
        value: date,
      }),
    );
    onChangeValidationHandler();
  };

  return (
    <TableCell
      className="rt-td rt-td-count-step"
    >
      <DatePicker
        value={value}
        customDateFormat={DateFormat.DD_MMM_YYYY}
        hasErrors={showError}
        onChange={onChange}
        onBlur={onBlurValidationHandler}
        disabled={isDisabled}
        className={`m-1 w-75 ${showError ? 'input-has-error' : ''}`}
        hideErrorMessageWrapper
      />
      {showError && (
        <CustomTooltip
          content={error}
          className="tooltip-icon tooltip-icon--error"
          icon={RiErrorWarningLine}
        />
      )}
    </TableCell>
  );
};

export default ExpirationDateCell;

ExpirationDateCell.propTypes = {
  id: PropTypes.string.isRequired,
  index: PropTypes.string.isRequired,
  cycleCountId: PropTypes.string.isRequired,
  disabledExpirationDateFields: PropTypes.objectOf(PropTypes.string).isRequired,
  isStepEditable: PropTypes.bool.isRequired,
};
