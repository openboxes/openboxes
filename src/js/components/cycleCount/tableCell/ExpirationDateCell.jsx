import React, { useState } from 'react';

import { TableCell } from 'components/DataTable';
import DatePicker from 'components/form-elements/v2/DateField';

const ExpirationDateCell = ({
  id,
  initialValue,
  disabledExpirationDateFields,
}) => {
  const [value, setValue] = useState(initialValue || disabledExpirationDateFields?.[id]);

  const isDisabled = disabledExpirationDateFields?.[id]
    || !id.includes('newRow');

  const onChange = (date) => {
    setValue(date);
  };

  return (
    <TableCell className="rt-td rt-td-count-step">
      <DatePicker
        value={value}
        onChange={onChange}
        disabled={isDisabled}
      />
    </TableCell>
  );
};

export default ExpirationDateCell;
