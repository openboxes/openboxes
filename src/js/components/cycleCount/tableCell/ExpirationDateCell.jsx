import React, { useState } from 'react';

import { useDispatch } from 'react-redux';

import { updateFieldValue } from 'actions';
import { TableCell } from 'components/DataTable';
import DatePicker from 'components/form-elements/v2/DateField';

const ExpirationDateCell = ({
  id,
  cycleCountId,
  initialValue,
  disabledExpirationDateFields,
}) => {
  const [value, setValue] = useState(initialValue || disabledExpirationDateFields?.[id]);

  const dispatch = useDispatch();

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
    <TableCell className="rt-td rt-td-count-step">
      <DatePicker
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        disabled={isDisabled}
      />
    </TableCell>
  );
};

export default ExpirationDateCell;
