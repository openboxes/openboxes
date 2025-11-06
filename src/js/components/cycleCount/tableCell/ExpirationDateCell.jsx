import React, { useState } from 'react';

import { useDispatch, useSelector } from 'react-redux';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import DatePicker from 'components/form-elements/v2/DateField';
import { DateFormat } from 'consts/timeFormat';
import { getFormatLocalizedDate } from 'selectors';

const ExpirationDateCell = ({
  id,
  cycleCountId,
  initialValue,
  disabledExpirationDateFields,
  isStepEditable,
}) => {
  const [value, setValue] = useState(initialValue || disabledExpirationDateFields?.[id]);

  const dispatch = useDispatch();

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

  const isDisabled = disabledExpirationDateFields?.[id]
    || !id.includes('newRow');

  const onChange = (date) => {
    setValue(date);
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
  };

  return (
    <TableCell
      className="rt-td rt-td-count-step"
    >
      <DatePicker
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        disabled={isDisabled}
        className="m-1 w-75"
        hideErrorMessageWrapper
      />
    </TableCell>
  );
};

export default ExpirationDateCell;
