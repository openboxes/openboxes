import React, { useEffect, useMemo, useState } from 'react';

import PropTypes from 'prop-types';
import { RiErrorWarningLine } from 'react-icons/ri';
import { useDispatch, useSelector } from 'react-redux';
import { getFormatLocalizedDate, makeGetCycleCountItem } from 'selectors';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import DatePicker from 'components/form-elements/v2/DateField';
import { NEW_ROW } from 'consts/cycleCount';
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
          field: 'inventoryItem.expirationDate',
          value: disabledExpirationDateFields?.[id],
        }),
      );
    }
    setValue(initialValue || disabledExpirationDateFields?.[id]);
  }, [initialValue]);

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
    shouldShowError,
  } = useCellValidation({
    initialValue,
    cycleCountId,
    index,
    fieldName: 'inventoryItem.expirationDate',
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
        field: 'inventoryItem.expirationDate',
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
        hasErrors={shouldShowError}
        onChange={onChange}
        onBlur={onBlur}
        disabled={isDisabled}
        className={`m-1 w-75 ${shouldShowError ? 'input-has-error' : ''}`}
        hideErrorMessageWrapper
      />
      {shouldShowError && (
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
