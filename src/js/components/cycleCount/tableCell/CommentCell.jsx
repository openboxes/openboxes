import React, { useState } from 'react';

import { TableCell } from 'components/DataTable';
import InputField from 'components/form-elements/v2/TextInput';

const CommentCell = ({
  initialValue,
}) => {
  const [value, setValue] = useState(initialValue);

  const onChange = (e) => {
    setValue(e.target.value);
  };

  return (
    <TableCell className="rt-td rt-td-count-step">
      <InputField
        value={value}
        onChange={onChange}
      />
    </TableCell>
  );
};

export default CommentCell;
