import React, { useState } from 'react';

import { TableCell } from 'components/DataTable';
import InputField from 'components/form-elements/v2/TextInput';

const QuantityCell = ({
  initialValue,
}) => {
  const [value, setValue] = useState(initialValue ?? '');

  const onChange = (enteredValue) => {
    const parsedValue = enteredValue
      ? (parseInt(enteredValue, 10) || 0)
      : enteredValue;
    setValue(parsedValue);
  };

  return (
    <TableCell className="rt-td rt-td-count-step">
      <InputField
        type="number"
        value={value}
        onChange={onChange}
        min="0"
      />
    </TableCell>
  );
};

export default QuantityCell;
