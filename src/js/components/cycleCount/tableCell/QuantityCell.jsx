import React, { useEffect, useMemo, useState } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { makeGetCycleCountItem } from 'selectors';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import InputField from 'components/form-elements/v2/TextInput';

const QuantityCell = ({
  id,
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
    setValue(initialValue);
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

  const dispatch = useDispatch();

  const onChange = (enteredValue) => {
    const parsedValue = enteredValue
      ? (parseInt(enteredValue, 10) || 0)
      : enteredValue;
    setValue(parsedValue);
  };

  const onBlur = () => {
    dispatch(
      updateFieldValue({
        cycleCountId,
        rowId: id,
        field: 'quantityCounted',
        value,
      }),
    );
  };

  return (
    <TableCell className="rt-td rt-td-count-step">
      <InputField
        type="number"
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        className="m-1 w-75"
        hideErrorMessageWrapper
        min="0"
      />
    </TableCell>
  );
};

export default QuantityCell;
