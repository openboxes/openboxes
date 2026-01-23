import React, { useEffect, useMemo, useState } from 'react';

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

  useEffect(() => {
    if (!initialValue && disabledExpirationDateFields?.[id]) {
      dispatch(
        updateFieldValue({
          cycleCountId,
          rowId: id,
          field: cycleCountColumn.EXPIRATION_DATE,
          value: disabledExpirationDateFields?.[id],
        }),
      );
    }

    setValue(initialValue || disabledExpirationDateFields?.[id]);
  }, [initialValue]);

  useEffect(() => {
    setValue(disabledExpirationDateFields?.[id]);
    if (!disabledExpirationDateFields?.[id]) {
      dispatch(
        updateFieldValue({
          cycleCountId,
          rowId: id,
          field: cycleCountColumn.EXPIRATION_DATE,
          value: undefined,
        }),
      );
    }
  }, [disabledExpirationDateFields?.[id]]);

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

  const isDisabled = disabledExpirationDateFields?.[id]
    || !id?.includes(NEW_ROW);

  const onChange = (date) => {
    setValue(date);
    onChangeValidationHandler();
  };

  const onBlur = () => {
    dispatch(
      updateFieldValue({
        cycleCountId,
        rowId: id,
        field: cycleCountColumn.EXPIRATION_DATE,
        value,
      }),
    );
    onBlurValidationHandler();
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
        onBlur={onBlur}
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
