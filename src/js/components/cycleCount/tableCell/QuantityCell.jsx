import React, { useEffect, useMemo, useState } from 'react';

import PropTypes from 'prop-types';
import { RiErrorWarningLine } from 'react-icons/ri';
import { useDispatch, useSelector } from 'react-redux';
import {
  makeGetCycleCountItem,
} from 'selectors';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import InputField from 'components/form-elements/v2/TextInput';
import cycleCountColumn from 'consts/cycleCountColumn';
import useCellValidation from 'hooks/cycleCount/useCellValidation';
import CustomTooltip from 'wrappers/CustomTooltip';

const QuantityCell = ({
  id,
  index,
  cycleCountId,
  isStepEditable,
}) => {
  const getCycleCountItem = useMemo(() => makeGetCycleCountItem(), []);
  const item = useSelector(
    (state) => getCycleCountItem(state, cycleCountId, id),
  );

  const { quantityCounted: initialValue } = item;

  const [value, setValue] = useState(initialValue);

  useEffect(() => {
    setValue(initialValue || '');
  }, [initialValue]);

  if (!isStepEditable) {
    return (
      <TableCell
        className="static-cell-count-step d-flex align-items-center"
      >
        {value?.toString()}
      </TableCell>
    );
  }

  const {
    onChangeValidationHandler,
    onBlurValidationHandler,
    error,
    showError,
  } = useCellValidation({
    initialValue,
    cycleCountId,
    index,
    fieldName: cycleCountColumn.QUANTITY_COUNTED,
  });

  const dispatch = useDispatch();

  const onChange = (enteredValue) => {
    const parsedValue = enteredValue
      ? (parseInt(enteredValue, 10) || 0)
      : enteredValue;
    setValue(parsedValue);
    onChangeValidationHandler();
  };

  const onBlur = () => {
    dispatch(
      updateFieldValue({
        cycleCountId,
        rowId: id,
        field: cycleCountColumn.QUANTITY_COUNTED,
        value,
      }),
    );
    onBlurValidationHandler();
  };

  return (
    <TableCell className="rt-td rt-td-count-step">
      <InputField
        type="number"
        value={value}
        showErrorBorder={showError}
        onChange={onChange}
        onBlur={onBlur}
        className="m-1 w-75"
        hideErrorMessageWrapper
        min="0"
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

export default QuantityCell;

QuantityCell.propTypes = {
  id: PropTypes.string.isRequired,
  index: PropTypes.string.isRequired,
  cycleCountId: PropTypes.string.isRequired,
  isStepEditable: PropTypes.bool.isRequired,
};
